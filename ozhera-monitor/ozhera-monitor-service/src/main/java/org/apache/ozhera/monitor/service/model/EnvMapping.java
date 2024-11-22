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

package org.apache.ozhera.monitor.service.model;

import com.google.gson.Gson;
import lombok.Data;
import lombok.ToString;

import java.util.Arrays;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2022/2/18 2:19 PM
 */
@Data
@ToString
public class EnvMapping {
    List<Area> areas;

    public static void main(String[] args) {
        Env e1 = new Env("dev");
//        Env e2 = new Env("Staging");
        Env e3 = new Env("online");

        Region r1 = new Region();
        r1.setName("beijing1");
        r1.setEnvs(Arrays.asList(e1,e3));
        Region r2 = new Region();
        r2.setName("beijing2");
        r2.setEnvs(Arrays.asList(e1,e3));

        Region r3 = new Region();
        r3.setName("e1");
        r3.setEnvs(Arrays.asList(e1,e3));
        Region r4 = new Region();
        r4.setName("e2");
        r4.setEnvs(Arrays.asList(e1,e3));

        Area area = new Area();
        area.setName("china");
        area.setCname("中国大陆");
        area.setRegions(Arrays.asList(r1,r2));

        Area areaE = new Area();
        areaE.setName("Europe");
        areaE.setCname("欧洲");
        areaE.setRegions(Arrays.asList(r3));


        EnvMapping em = new EnvMapping();
        em.setAreas(Arrays.asList(area,areaE));

        System.out.println(new Gson().toJson(em));

    }
}
