/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ozhera.intelligence.service;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.ozhera.intelligence.domain.rootanalysis.TraceTreeNode;
import org.apache.ozhera.trace.etl.api.service.TraceQueryService;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerAttribute;
import org.apache.ozhera.trace.etl.domain.tracequery.Span;
import org.apache.ozhera.trace.etl.domain.tracequery.Trace;
import org.apache.ozhera.trace.etl.domain.tracequery.TraceIdQueryVo;
import org.springframework.stereotype.Service;
import org.apache.ozhera.intelligence.domain.rootanalysis.TraceQueryParam;
import org.apache.ozhera.trace.etl.domain.jaegeres.JaegerProcess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dingtao
 * @date 2025/1/20 11:26
 */
@Service
public class TraceService {

    @DubboReference(interfaceClass = TraceQueryService.class, group = "${trace.query.group}", version = "${trace.query.version}")
    private TraceQueryService traceQueryService;

    /**
     * The time range threshold for querying before and after the given timestamp.
     * The actual query range is calculated by subtracting and adding QUERY_TIME_RANGE to the given timestamp.
     */
    private static final long QUERY_TIME_RANGE = 30 * 60 * 1000;

    /**
     * The maximum depth of the analyzed trace to be retrieved.
     */
    private static final int TRACE_DEEP = 5;


    private static final String SERVICE_ENV_ID_KEY = "service.env.id";
    private static final List<String> NOT_RETAIN_SPAN_NAMES = List.of("UDS");
    private static final List<String> RETAIN_TAGS = Arrays.asList("net.sock.peer.addr", "span.kind", "net.sock.peer.port", "http.target", "net.host.name", "net.peer.name", "net.peer.port", "rpc.service", "rpc.method", "rpc.system", "db.statement", "error", "http.url");
    private static final List<String> RETAIN_PROCESS_TAGS = Arrays.asList(SERVICE_ENV_ID_KEY, "ip", "service.env", "service.env.id", "service.container.name", "host.name");

    /**
     * Query trace based on the specified trace query conditions.
     *
     * @param param
     * @return
     */
    public List<Span> queryTraceRootAnalysis(TraceQueryParam param) {
        Trace traceResult = traceQueryService.getByTraceId(buildTraceIdQueryVo(param));
        List<Span> result = new ArrayList<>();
        if (traceResult != null && traceResult.getSpans() != null && !traceResult.getSpans().isEmpty()) {
            // analyze and cut span
            List<Span> analyze = analyzeTrace(traceResult.getSpans(), TRACE_DEEP);
            result = getSpansFilter(analyze);
        }
        return result;
    }

    /**
     * Analyze trace information and return a list of key Spans
     * <p>
     * This method first builds a TraceTreeNode tree, then analyzes the trace information based on whether errors exist.
     * If there are errors, it will find and return the most recent error node and its child nodes.
     * If there are no errors, it will search for time-consuming nodes.
     *
     * @param spans The list of Spans to be analyzed
     * @param N     The number of child nodes to return in case of errors
     * @return A list containing key Spans, representing error nodes or time-consuming nodes
     */
    public List<Span> analyzeTrace(List<Span> spans, int N) {
        // Build TraceTreeNode tree

        // Build TraceTreeNode tree
        Map<String, TraceTreeNode> nodeMap = new HashMap<>();
        boolean hasError = false;
        for (Span span : spans) {
            if (hasException(span)) {
                hasError = true;
            }
            TraceTreeNode node = new TraceTreeNode(span);

            nodeMap.put(span.getSpanID(), node);
        }
        List<TraceTreeNode> roots = new ArrayList<>();
        for (TraceTreeNode node : nodeMap.values()) {
            if (node.getSpan().getReferences().isEmpty() ||
                    !nodeMap.containsKey(node.getSpan().getReferences().get(0).getSpanID())) {
                roots.add(node);
            } else {
                TraceTreeNode parentNode = nodeMap.get(node.getSpan().getReferences().get(0).getSpanID());
                parentNode.addChild(node);
            }
        }
        // Sort sibling nodes by startTime
        for (TraceTreeNode root : roots) {
            sortChildren(root);
        }
        List<Span> result = new ArrayList<>();
        if (hasError) {
            // Find error nodes
            List<TraceTreeNode> errorNodes = new ArrayList<>();
            for (TraceTreeNode root : roots) {
                findErrorTargetNode(root, errorNodes);
                if (errorNodes != null && errorNodes.size() > 0) {
                    int currentChildCount = 0;
                    // Sort by Span.startTime, analyze the Span with the latest start time, which is likely the most relevant
                    errorNodes.sort(Comparator.comparing(s -> s.getSpan().getStartTime()));
                    TraceTreeNode lastErrorNode = errorNodes.get(errorNodes.size() - 1);
                    result.add(lastErrorNode.getSpan());
                    addNChild(result, lastErrorNode, N, currentChildCount);
                }
            }
        } else {
            // Finding time-consuming nodes is more complex. We start with the root node's duration as a baseline,
            // and search level by level until there are no more child nodes or no child nodes with significantly high duration.
            List<TraceTreeNode> slowNodes = new ArrayList<>();
            for (TraceTreeNode root : roots) {
                long rootDuration = root.getSpan().getDuration();
                slowNodes.add(root);
                findSlowNode(root, slowNodes, rootDuration);
                for (TraceTreeNode node : slowNodes) {
                    result.add(node.getSpan());

                }
            }
        }

        return result;
    }

    /**
     * Filter and process the given list of Spans
     *
     * @param spans The list of Spans to be processed
     * @return The filtered and processed list of Spans
     * <p>
     * This method mainly performs the following operations:
     * 1. Filters out Spans that don't need to be retained based on NOT_RETAIN_SPAN_NAMES
     * 2. For retained Spans, only keeps tags specified in RETAIN_TAGS
     * 3. For each Span's Process, only keeps tags specified in RETAIN_PROCESS_TAGS
     * If the input spans is null or empty, it returns the original spans directly
     */
    private List<Span> getSpansFilter(List<Span> spans) {
        if (spans != null && spans.size() > 0) {
            List<Span> newSpans = new ArrayList<>();
            for (Span span : spans) {
                String operationName = span.getOperationName();

                boolean isRetain = true;
                for (String filter : NOT_RETAIN_SPAN_NAMES) {
                    if (operationName.contains(filter)) {
                        isRetain = false;
                    }
                }
                if (isRetain) {
                    newSpans.add(span);
                }
            }
            for (Span span : newSpans) {
                List<JaegerAttribute> newTags = new ArrayList<>();
                for (JaegerAttribute attribute : span.getTags()) {
                    if (RETAIN_TAGS.contains(attribute.getKey())) {
                        newTags.add(attribute);
                    }
                }
                span.setTags(newTags);
                List<JaegerAttribute> newProcessTags = new ArrayList<>();
                JaegerProcess process = span.getProcess();
                List<JaegerAttribute> processTags = process.getTags();
                for (JaegerAttribute processTag : processTags) {
                    if (RETAIN_PROCESS_TAGS.contains(processTag.getKey())) {
                        newProcessTags.add(processTag);
                    }
                }
                process.setTags(newProcessTags);
            }
            return newSpans;
        }
        return spans;
    }

    private boolean hasException(Span span) {
        // Determine if the span contains any errors based on the actual situation
        for (JaegerAttribute tag : span.getTags()) {
            if ("error".equals(tag.getKey())) {
                return true;


            }
        }
        return false;
    }

    private void sortChildren(TraceTreeNode node) {
        if (node.getChildren() != null) {
            node.getChildren().sort(Comparator.comparingLong(n -> n.getSpan().getStartTime()));
            for (TraceTreeNode child : node.getChildren()) {
                sortChildren(child);
            }
        }
    }

    private void findSlowNode(TraceTreeNode root, List<TraceTreeNode> slowNodes, long rootDuration) {
        // Find nodes with duration greater than 80% of the parent node's duration.
        // If none found, look for 50%, then 30%, 20%, and finally 10%.
        if (!root.getChildren().isEmpty()) {
            for (TraceTreeNode childNode : root.getChildren()) {
                if (findByPercent(childNode, slowNodes, rootDuration, 0.8)) {

                    findSlowNode(childNode, slowNodes, childNode.getSpan().getDuration());
                } else if (findByPercent(childNode, slowNodes, rootDuration, 0.5)) {
                    findSlowNode(childNode, slowNodes, childNode.getSpan().getDuration());
                } else if (findByPercent(childNode, slowNodes, rootDuration, 0.3)) {
                    findSlowNode(childNode, slowNodes, childNode.getSpan().getDuration());
                } else if (findByPercent(childNode, slowNodes, rootDuration, 0.2)) {
                    findSlowNode(childNode, slowNodes, childNode.getSpan().getDuration());
                } else if (findByPercent(childNode, slowNodes, rootDuration, 0.1)) {
                    findSlowNode(childNode, slowNodes, childNode.getSpan().getDuration());
                }
            }
        }
    }

    private boolean findByPercent(TraceTreeNode parent, List<TraceTreeNode> slowNodes, long rootDuration, double percent) {
        boolean result = false;
        for (TraceTreeNode childNode : parent.getChildren()) {
            if (childNode.getSpan().getDuration() > rootDuration * percent) {
                result = true;
                slowNodes.add(childNode);
            }
        }
        return result;
    }

    private void findErrorTargetNode(TraceTreeNode node, List<TraceTreeNode> errorNodes) {
        if (hasException(node.getSpan())) {
            errorNodes.add(node);
        }

        if (node.getChildren() != null) {
            for (TraceTreeNode child : node.getChildren()) {
                findErrorTargetNode(child, errorNodes);
            }
        }
    }

    /**
     * Add the specified number of child nodes of the targetNode to the result.
     * First, add the first-level child nodes; if the quantity is not sufficient,
     * then add the second-level child nodes. If the number of child nodes exceeds afterNumber,
     * or if there are no more child nodes, the method returns.
     *
     * @param result      List to store the resulting Span objects
     * @param targetNode
     * @param afterNumber
     */
    private void addNChild(List<Span> result, TraceTreeNode targetNode, int afterNumber, int currentChildCount) {
        List<TraceTreeNode> children = targetNode.getChildren();
        if (!children.isEmpty()) {
            List<TraceTreeNode> childs = new ArrayList<>();
            for (TraceTreeNode treeNode : children) {
                result.add(treeNode.getSpan());
                currentChildCount++;
                if (currentChildCount >= afterNumber) {
                    return;
                }
                if (!treeNode.getChildren().isEmpty()) {
                    childs.add(treeNode);
                }
            }
            // If the number of first-level child nodes is less than afterNumber, continue adding second-level child nodes
            for (TraceTreeNode secondChild : childs) {
                addNChild(result, secondChild, afterNumber, currentChildCount);
            }
        }
    }

    private TraceIdQueryVo buildTraceIdQueryVo(TraceQueryParam param) {

        TraceIdQueryVo vo = new TraceIdQueryVo();
        vo.setTraceId(param.getTraceId());
        long startTime = param.getTimeStamp() - QUERY_TIME_RANGE;
        long endTime = param.getTimeStamp() + QUERY_TIME_RANGE;
        vo.setStartTime(startTime);
        vo.setEndTime(endTime);
        return vo;
    }
}