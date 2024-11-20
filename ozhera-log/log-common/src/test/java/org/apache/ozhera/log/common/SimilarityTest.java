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
package org.apache.ozhera.log.common;

import cn.hutool.core.date.DateUtil;
import org.apache.ozhera.log.utils.SimilarUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/2/16 12:48
 */
public class SimilarityTest {

    @Test
    public void test2() {
        System.out.println(DateUtil.parse("2022/01/23 23:23:34").getTime());
    }

    @Test
    public void testFindHighestSimilarityStr() {
        String baseStr = "appserver.20230912.log";
        List<String> strList = Arrays.asList("appserver.log", "appserver_warn.log", "error.log", "appserver_error.log");
        String expected = "appserver.log";
        String actual = SimilarUtils.findHighestSimilarityStr(baseStr, strList);
        assertEquals(expected, actual);
    }


}
