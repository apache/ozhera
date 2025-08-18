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
package org.apache.ozhera.log.parse;

import java.util.*;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2025/4/7 15:36
 */
public class PlaceholderParser extends AbstractLogParser {
    private final String parseScript;
    private final List<String> fieldNames;
    private final List<String> staticParts;

    public PlaceholderParser(LogParserData parserData) {
        super(parserData);
        parseScript = parserData.getParseScript();
        fieldNames = valueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
        this.staticParts = splitAndFormat();
    }

    private List<String> splitAndFormat() {
        List<String> parts = new ArrayList<>();
        String[] tokens = parseScript.split("%s", -1);
        for (String token : tokens) {
            if (!token.isEmpty()) {
                parts.add(token);
            }
        }
        return parts;
    }

    @Override
    public Map<String, Object> doParse(String logData, String ip, Long lineNum, Long collectStamp, String fileName) {
        return parse(logData);
    }

    @Override
    public Map<String, Object> doParseSimple(String logData, Long collectStamp) {
        return parse(logData);
    }

    @Override
    public List<String> parseLogData(String logData) throws Exception {
        List<String> result = new ArrayList<>();
        String remaining = logData;
        int staticPartIndex = 0;

        try {
            if (!staticParts.isEmpty() && remaining.startsWith(staticParts.get(0))) {
                remaining = remaining.substring(staticParts.get(0).length());
                staticPartIndex++;
            }

            while (staticPartIndex < staticParts.size()) {
                String nextStatic = staticParts.get(staticPartIndex);
                int endPos = remaining.indexOf(nextStatic);

                if (endPos == -1) {
                    if (!remaining.trim().isEmpty()) {
                        result.add(remaining.trim());
                    }
                    break;
                }

                String fieldValue = remaining.substring(0, endPos).trim();
                if (!fieldValue.isEmpty()) {
                    result.add(fieldValue);
                }

                remaining = remaining.substring(endPos + nextStatic.length());
                staticPartIndex++;
            }

            if (!remaining.trim().isEmpty()) {
                result.add(remaining.trim());
            }

        } catch (Exception ignored) {
        }
        return result;
    }


    public Map<String, Object> parse(String logLine) {
        Map<String, Object> result = new LinkedHashMap<>();
        String remaining = logLine;
        int fieldIndex = 0;
        int staticPartIndex = 0;

        try {
            if (!staticParts.isEmpty() && remaining.startsWith(staticParts.get(0))) {
                remaining = remaining.substring(staticParts.get(0).length());
                staticPartIndex++;
            }

            while (fieldIndex < fieldNames.size()) {
                String nextStatic = staticPartIndex < staticParts.size() ? staticParts.get(staticPartIndex) : null;
                int endPos = nextStatic != null ? remaining.indexOf(nextStatic) : remaining.length();

                if (endPos == -1) return result;

                String fieldValue = remaining.substring(0, endPos).trim();
                result.put(fieldNames.get(fieldIndex++), fieldValue);
                remaining = remaining.substring(endPos);

                if (nextStatic != null) {
                    if (!remaining.startsWith(nextStatic)) {
                        return Collections.emptyMap();
                    }
                    remaining = remaining.substring(nextStatic.length());
                    staticPartIndex++;
                }
            }

            return result;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }
}
