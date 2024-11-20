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

package org.apache.ozhera.monitor.test;

import org.apache.ozhera.monitor.utils.FreeMarkerUtil;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaoxihui
 * @date 2021/7/7 8:42 下午
 */
public class TestUtils {


    @Test
    public void testFreeMarker() {

        Map<String, Object> map = new HashMap<>();
        map.put("size", 0);
        map.put("gte_val", "1497283200000");
        map.put("lte_val", "1497928996980");
        map.put("min_val", "1497283200000");
        map.put("max_val", "1497928996980");
        map.put("interval", "21526566ms");

        try {
            //获取工程路径
            String content = FreeMarkerUtil.getContent("/", "eventflow.ftl", map);
            System.out.println("返回的json" + "\n" + content + "\n");
        } catch (IOException | TemplateException e) {
            e.printStackTrace();
        }
    }
}
