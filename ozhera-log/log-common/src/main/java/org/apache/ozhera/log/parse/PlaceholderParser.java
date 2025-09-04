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
 *
 * 支持：
 * 1. %s 普通字段
 * 2. %s[n@sep] 合并 n 个 token
 * 3. %s[-] 丢弃字段
 * 4. 只在相邻两个 %s 之间的方括号可跳过，里面内容可提取
 *
 * @author wtt
 * @version 1.0
 * @description
 * @date 2025/4/7 15:36
 */
public class PlaceholderParser extends AbstractLogParser {
    private final List<String> fieldNames;
    private final List<ScriptPart> parts;

    public PlaceholderParser(LogParserData parserData) {
        super(parserData);
        String parseScript = parserData.getParseScript();
        this.fieldNames = valueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
        this.parts = splitScript(parseScript);
    }

    /** 拆分脚本为占位符和静态部分 */
    private List<ScriptPart> splitScript(String script) {
        List<ScriptPart> list = new ArrayList<>();
        int idx = 0;
        while (idx < script.length()) {
            int next = script.indexOf("%s", idx);
            if (next == -1) {
                list.add(new ScriptPart(script.substring(idx), false, null));
                break;
            }
            if (next > idx) {
                list.add(new ScriptPart(script.substring(idx, next), false, null));
            }
            // 检查 modifier
            String modifier = null;
            if (next + 2 < script.length() && script.charAt(next + 2) == '[') {
                int end = script.indexOf(']', next + 2);
                if (end != -1) {
                    modifier = script.substring(next + 3, end);
                    next = end;
                }
            }
            list.add(new ScriptPart("%s", true, modifier));
            idx = next + 2;
        }
        return list;
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
        return new ArrayList<>(parse(logData).values().stream().map(Object::toString).toList());
    }


    public Map<String, Object> parse(String logLine) {
        Map<String, Object> result = new LinkedHashMap<>();
        String remaining = logLine;
        int fieldIndex = 0;

        try {
            for (int i = 0; i < parts.size(); i++) {
                ScriptPart part = parts.get(i);
                if (part.isPlaceholder) {
                    // 检查下一个占位符之间是否有方括号
                    String bracketValue = null;
                    if (i + 1 < parts.size() && parts.get(i + 1).isPlaceholder) {
                        // 尝试提取 [] 内的内容
                        if (!remaining.isEmpty() && remaining.charAt(0) == '[') {
                            int endBracket = remaining.indexOf(']');
                            if (endBracket != -1) {
                                bracketValue = remaining.substring(1, endBracket);
                                remaining = remaining.substring(endBracket + 1);
                            }
                        }
                    }

                    String fieldValue;

                    // n@sep 处理
                    if (part.modifier != null && part.modifier.matches("\\d+@.*")) {
                        String[] arr = part.modifier.split("@", 2);
                        int count = Integer.parseInt(arr[0]);
                        String sep = arr[1];
                        List<String> tokens = new ArrayList<>();
                        String temp = remaining;
                        for (int j = 0; j < count; j++) {
                            int idxSep = temp.indexOf(sep);
                            if (idxSep == -1) {
                                tokens.add(temp);
                                temp = "";
                                break;
                            } else {
                                tokens.add(temp.substring(0, idxSep));
                                temp = temp.substring(idxSep + sep.length());
                            }
                        }
                        fieldValue = String.join(sep, tokens).trim();
                        remaining = temp;
                    } else {
                        // 普通 %s
                        String nextStatic = null;
                        for (int j = i + 1; j < parts.size(); j++) {
                            if (!parts.get(j).isPlaceholder) {
                                nextStatic = parts.get(j).text;
                                break;
                            }
                        }
                        int endPos = nextStatic != null ? remaining.indexOf(nextStatic) : remaining.length();
                        if (endPos == -1) {
                            endPos = remaining.length();
                        }
                        fieldValue = remaining.substring(0, endPos).trim();
                        remaining = remaining.substring(endPos);
                    }

                    // 如果有方括号值，可以组合或存入 Map
                    if (bracketValue != null && !bracketValue.isEmpty()) {
                        fieldValue = bracketValue;
                    }

                    // 丢弃字段
                    if (!"-".equals(part.modifier) && fieldIndex < fieldNames.size()) {
                        result.put(fieldNames.get(fieldIndex++), fieldValue);
                    }

                } else {
                    // 静态部分严格匹配
                    if (!remaining.startsWith(part.text)) {
                        return Collections.emptyMap();
                    }
                    remaining = remaining.substring(part.text.length());
                }
            }
        } catch (Exception e) {
            return Collections.emptyMap();
        }

        return result;
    }

    private static class ScriptPart {
        String text;
        boolean isPlaceholder;
        String modifier;

        public ScriptPart(String text, boolean isPlaceholder, String modifier) {
            this.text = text;
            this.isPlaceholder = isPlaceholder;
            this.modifier = modifier;
        }
    }
}
