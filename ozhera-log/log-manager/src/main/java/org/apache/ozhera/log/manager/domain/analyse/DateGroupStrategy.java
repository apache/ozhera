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
package org.apache.ozhera.log.manager.domain.analyse;

import org.apache.ozhera.log.manager.model.bo.CalcuAggrParam;
import org.apache.ozhera.log.manager.model.dto.LogAnalyseDataDTO;
import org.apache.ozhera.log.manager.model.dto.LogAnalyseDataDateDTO;
import com.xiaomi.youpin.docean.anno.Service;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.LongBounds;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;

import java.util.*;

@Service
public class DateGroupStrategy implements AggrCalcuStrategy {

    @Override
    public AggregationBuilder getAggr(CalcuAggrParam param) {
        // The timestamp parameter is taken to the second bit to offset the bucketing error
        Long startTime = param.getStartTime() / 1000 * 1000;
        Long endTime = param.getEndTime() / 1000 * 1000;

        int interval = (int) Math.ceil((endTime - startTime) / 1000 / Double.parseDouble(param.getGraphParam()));
        String offset = startTime % (interval * 1000) + "ms";
        // Time binning aggregation
        DateHistogramAggregationBuilder dateGroupAggs = AggregationBuilders
                .dateHistogram("dateAggs")
                .field("timestamp")
                .minDocCount(0)
                .offset(offset)
                .format("yyyy-MM-dd HH:mm:ss")
                .timeZone(TimeZone.getTimeZone("GMT+8").toZoneId())
                .fixedInterval(new DateHistogramInterval(interval + "s"))
                .extendedBounds(new LongBounds(startTime, endTime - 1000));
        // Field aggregation
        AggregationBuilder topAggs = AggregationBuilders
                .terms("fieldAggs")
                .size(4)
                .field(param.getBead())
                .executionHint("map");
        // Combine aggregations
        dateGroupAggs.subAggregation(topAggs);

        return dateGroupAggs;
    }

    @Override
    public LogAnalyseDataDTO formatRes(SearchResponse response) {
        if (response == null || response.getAggregations() == null) {
            return null;
        }
        Histogram dateAggs = response.getAggregations().get("dateAggs");
        if (dateAggs == null) {
            return null;
        }
        // Solve the term and data do not correspond - the first traversal, encapsulated into intermediate data
        Map<String, Map<String, String>> ferryMap = new LinkedHashMap<>();
        Map<String, String> ferryTermMap;
        Set<String> termKeySet = new HashSet<>();
        for (int i = 0; i < dateAggs.getBuckets().size(); i++) {
            Histogram.Bucket bucket = dateAggs.getBuckets().get(i);
            ParsedStringTerms filedAggs = bucket.getAggregations().get("fieldAggs");
            if (filedAggs == null) {
                return null;
            }
            ferryTermMap = new HashMap<>();
            for (int j = 0; j < filedAggs.getBuckets().size(); j++) {
                Terms.Bucket filedAggsBucket = filedAggs.getBuckets().get(j);
                ferryTermMap.put(filedAggsBucket.getKeyAsString(), String.valueOf(filedAggsBucket.getDocCount()));
                // Collect all the keys
                termKeySet.add(filedAggsBucket.getKeyAsString());
            }
            ferryMap.put(bucket.getKeyAsString(), ferryTermMap);
        }
        // Solve the term and data do not correspond - secondary traversal, encapsulated into dto
        List<List<String>> date = new ArrayList<>();
        List<String> ferryDate;
        for (Map.Entry<String, Map<String, String>> entry : ferryMap.entrySet()) {
            ferryDate = new ArrayList<>();
            ferryDate.add(entry.getKey());
            for (String term : termKeySet) {
                ferryDate.add(entry.getValue().containsKey(term) ? entry.getValue().get(term) : "0");
            }
            date.add(ferryDate);
        }
        return new LogAnalyseDataDateDTO(date, termKeySet);
    }

}
