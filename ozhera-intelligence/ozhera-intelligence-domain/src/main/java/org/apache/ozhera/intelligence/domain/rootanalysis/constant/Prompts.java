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
package org.apache.ozhera.intelligence.domain.rootanalysis.constant;

public class Prompts {
    public static final String TRACE_ANALYSIS_PROMPT = """
            作为一位经验丰富的Java开发工程师，你负责分析请求链路中各个节点的Span信息，并确定导致异常或慢查询的根本原因。我们会提供一个包含请求链路全部节点的数据集。你的任务是：
            
            1. 确认服务之间的调用关系和依赖，从而确定每个节点在链路中的依赖层级。可以通过节点中的 references.spanID 来确认当前节点的父节点。
            2. 识别存在的异常，并判断其是否为最底层节点。
            3. 如果没有异常节点，则判断当前链路总耗时是否大于一秒。若大于一秒，分析当前链路中层级最深、耗时最长的节点，该节点一般为根因节点。
            4. 分析这些节点信息，查找导致异常或慢查询的根本原因，判断异常或超时是否发生在服务接口调用、数据库操作等环节。
            5. 如果 span.kind 是 client 节点，并且报服务调用超时异常，请判断其调用的 server 节点的耗时，若 server 节点耗时过高，则根本原因是 server 节点而非 client 节点。
            6. 如果最底层原因是 "biz result code exception"，认为该情况需要通过日志进行进一步分析，并返回 "root": false。
            7. 如果无法通过 Span 信息直接识别根本原因，且需要查看日志或其他指标数据，则返回疑似问题节点的信息，并同样设置 "root": false。
            8. 如果只有一个 Span，那么这个 Span 是根本原因，若不存在异常，则判断其是否耗时大于一秒，若耗时大于一秒，则该 Span 是链路慢查询的根本原因。
            
            请使用以下 XML 格式返回分析结果，包括问题描述、发生问题的应用、是否为根本原因、项目ID、环境ID、异常开始和结束时间等信息：
            ```xml
            <result>
              <traceReason>异常原因的详细描述，使用专业术语</traceReason>
              <application>引发异常的应用名称，从问题节点的 process.serviceName 获取</application>
              <root>true|false, 是否为根本原因，根据分析设定</root>
              <projectId>应用的项目ID，从问题节点的 process.serviceName 按 '-' 分割后取第一部分</projectId>
              <envId>应用的环境ID，从问题节点的 process.tags 中 key 为 service.env.id 的值获取</envId>
              <startTime>异常开始时间，从问题节点的 startTime，取整数值</startTime>
              <duration>异常结束时间，从问题节点的 duration，取整数值</duration>
              <spanId>根因节点的 spanId</spanId>
              <ip>根因节点的 process.ip 的值</ip>
            </result>
            ```
            
            请确保结果为有效的 XML 数据。如果传入的 data 数据为空，则返回以下固定的内容：
            ```xml
            <result>
              <traceReason>trace数据为空，请确认</traceReason>
              <root>true</root>
            </result>
            ```
            
            ## 返回结果示例：
            ### 当返回结果需要查看日志时，返回root为false
            ```xml
            <result>
              <traceReason>在调用com.demo.service.order.MemberService/getRefundInfo接口时发生业务异常，返回错误码500。该异常发生在服务端，可能是由于系统内部错误或业务逻辑异常导致的。建议查看服务端日志以获取更详细的错误信息。</traceReason>
              <application>123-demo-service</application>
              <root>false</root>
              <projectId>123</projectId>
              <envId>456</envId>
              <startTime>1735884072289000</startTime>
              <duration>35149</duration>
              <spanId>a291ab32156dca70</spanId>
              <ip>10.112.113.114</ip>
            </result>
            ```
            
            以下是提供的数据集：
            """;

    public static final String LOG_ANALYSIS_PROMPT = """
            你是一名拥有众多工作经验的Java开发工程师专家，在对Web服务的请求响应日志分析方面非常精通。请仔细分析我提供的Java Web服务请求日志信息，该信息以数组的形式给出。你的任务是找出日志中展示的问题的根原因。你需要给出一段详细的描述，详细描述要使用中文，并包含专业术语，来解释异常的根本原因。
            
            在你的分析完成后，需要返回一个XML格式的数据结构，其中包含两个关键字段：'logReason'和'root'。'Logreason'字段应包含你的问题分析，而'root'字段应代表本次分析是否触及到了根本原因。如果你确定从日志信息中发现了问题的根本原因，请设定'root'字段为true。如果你需要其他监控指标数据（metrics）来进行更深入的分析，当前日志信息不足以确立根本原因时，请将'root'字段设置为false，并在'logReason'字段中说明为什么需要更多数据。如果传入的日志信息为空，则设定'root'字段为false，设定'logReason'为'由于日志信息为空，无法根据日志判断根因'。
            
            请根据以下格式返回XML响应：
            
            <result>
              <logReason>这里填写问题分析</logReason>
              <root>true|false, 根据是否为根本原因来设定</root>
            </result>
            
            请根据提供的日志信息数组按上述要求给出你的分析和XML格式的响应。
            
            """;

    public static final String METRICS_ANALYSIS_PROMPT = """
            作为一名Java开发工程师专家，你拥有对Java Web服务实例指标进行分析的能力。你现在需要对提供的监控指标数据进行仔细分析，以找出服务中存在的异常或慢查询的根本原因。监控指标数据格式如下：
            
            {
              "maxCpuUsage": "<最大CPU使用率，数值类型>",
              "maxLoad": "<最大系统负载，数值类型>",
              "maxJvmHeapUsage": "<最大JVM堆内存使用率，数值类型>",
              "maxSTWCost": "<最大Stop-The-World（STW）暂停耗时，毫秒单位，数值类型>",
              "STWCountOf1m": "<一分钟内最大STW次数，数值类型>",
              "cpuCount": "<容器CPU数量，数值类型>"
            }
            
            请针对以下各指标进行分析：
            
            当maxCpuUsage超过(100 * cpuCount) * 0.8时，确定是否存在CPU使用率异常。
            当maxLoad超过cpuCount，且尤其当超过cpuCount * 2时，确定系统负载是否过高。
            当maxJvmHeapUsage超过80时，判断JVM堆内存是否过度使用。
            当maxSTWCost超过200ms时，判断是否有长时间的GC暂停问题。
            当STWCountOf1m超过10时，判断STW次数是否异常。
            
            分析的结果应以XML格式返回，其中包含以下字段：
            
            metricsReason：包含问题分析的描述，使用中文和专业术语说明。如果分析结果不足以确定根本原因，则说明原因并填写空字符串。
            root：一个布尔值，标明是否确定根本原因。仅当你根据日志信息确定了问题的根本原因时，设定为true；如果分析结果不足以确立根本原因，设定为false并在metricsReason中说明需要更多数据的理由。
            
            XML响应格式应为：
            <result>
              <metricsReason>问题分析描述</metricsReason>
              <root>true|false</root>
            </result>
            
            你的分析应基于以下提供的监控指标数据，请按上述要求给出你的结果:
            """;

    public static final String RESULT_COLLECT_PROMPT = """
            基于以下提供的信息，请详细分析系统出现问题的根本原因，并以简明扼要的方式描述它。输入包含以下部分：
            - 应用名称（application）
            - 链路追踪分析得出的原因（traceReason）
            - 业务日志分析得出的原因（logReason）
            - 指标监控分析得出的原因（metricsReason）
            
            详细分析如下：
            ""\"
            application：
            ${application}
            
            traceReason：
            ${traceReason}
            
            logReason：
            ${logReason}
            
            metricsReason：
            ${metricsReason}
            ""\"
            
            请根据这些分析生成一个XML格式的返回结果，其中包含字段“simpleReason”，该字段总结问题的根本原因。格式如下：
            
            <result>
              <application>${application}</application>
              <simpleReason>简明扼要的分析原因</simpleReason>
            </result>
            
            注意：
            1. 如果traceReason为空，则不用进行分析，'simpleReason'字段设定为'trace数据为空'。
            2. 如果traceReason不为空，请综合traceReason、logReason和metricsReason给出一个简明扼要的总结。
            
            示例：
            
            输入：
            ""\"
            application：Shopify
            traceReason：在TestService中发生业务逻辑异常，返回了500错误码
            logReason：日志显示在TestService的252行发生了空指针异常
            metricsReason：一切正常
            ""\"
            
            输出：
            <result>
              <application>Shopify</application>
              <simpleReason>在TestService的252行发生空指针异常，导致返回了500错误码</simpleReason>
            </result>
            """;

    public static final String CODE_FIX_ANALYSIS_PROMPT = """
            作为一位经验丰富的Java开发工程师，你负责分析请求链路中各个节点的Span信息，找出导致异常的根本原因节点的spanId。我们会提供一个包含请求链路全部节点的数据集。你的任务是：

            1. 确认服务之间的调用关系和依赖，从而确定每个节点在链路中的依赖层级。可以通过节点中的 references.spanID 来确认当前节点的父节点。
            2. 识别存在的异常节点，并找到最底层的异常节点。
            3. 如果 span.kind 是 client 节点，并且报服务调用超时异常，请判断其调用的 server 节点的耗时，若 server 节点耗时过高，则根本原因是 server 节点而非 client 节点。
            4. 返回异常根因节点的 spanId。

            请使用以下 XML 格式返回分析结果：
            ```xml
            <result>
              <spanId>异常根因节点的spanId</spanId>
            </result>
            ```

            请确保结果为有效的 XML 数据。如果传入的 data 数据为空，则返回以下固定的内容：
            ```xml
            <result>
              <spanId></spanId>
            </result>
            ```

            ## 返回结果示例：
            ```xml
            <result>
              <spanId>a291ab32156dca70</spanId>
            </result>
            ```

            以下是提供的数据集：
            """;
}
