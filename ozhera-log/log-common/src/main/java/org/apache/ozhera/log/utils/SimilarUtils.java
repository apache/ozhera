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
package org.apache.ozhera.log.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/2/16 13:03
 */
public class SimilarUtils {

    public SimilarUtils() {
    }

    public static String findHighestSimilarityStr(String baseStr, List<String> strList) {
        String strR = "";
        if (1 == strList.size()) {
            strR = strList.get(0);
        } else {
            for (String s : strList) {
                if (baseStr.contains(s)) {
                    strR = s;
                }
            }
        }
        if (StringUtils.isEmpty(strR)) {
            // find similar fileName
            strR = strList.stream().sorted((o1, o2) ->
                    Double.compare(computeSimilarity(baseStr, o2), computeSimilarity(baseStr, o1)))
                    .findFirst()
                    .get();
        }
        return strR;
    }

    public static double computeSimilarity(String str1, String str2) {
        int levenshteinDistance = computeLevenshteinDistance(str1, str2);
        int maxLen = Math.max(str1.length(), str2.length());
        return (1 - ((double) levenshteinDistance / maxLen)) * 100;
    }

    private static int computeLevenshteinDistance(String str1, String str2) {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++)
            distance[i][0] = i;
        for (int j = 1; j <= str2.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= str1.length(); i++)
            for (int j = 1; j <= str2.length(); j++)
                distance[i][j] = minimum(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1] + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0 : 1));

        return distance[str1.length()][str2.length()];
    }

    private static int minimum(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

}
