/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package run.mone.chaos.operator.constant;

import lombok.Getter;
import run.mone.chaos.operator.bo.*;
import run.mone.chaos.operator.bo.http.HttpBO;
import run.mone.chaos.operator.dao.domain.*;
import run.mone.chaos.operator.service.TaskBaseService;
import run.mone.chaos.operator.service.impl.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public enum TaskEnum {

    un_know(-99, "unknow", null, null, null),
    stress(1, "stress", StressBO.class, StressPO.class, StressServiceImpl.class),
    network(2, "network", NetworkBO.class, NetworkPO.class, NetWorkServiceImpl.class),
    jvm(3, "jvm", JvmBO.class, JvmPO.class, JvmServiceImpl.class),
    pod(4, "pod", PodBO.class, PodPO.class, PodChaosServiceImpl.class),
    http(5, "http", HttpBO.class, HttpPO.class, HttpServiceImpl.class),
    io(6, "io", IOBO.class, IOPO.class, IoServiceImpl.class),
    time(7, "time", TimeBO.class, TimePO.class, TimeServiceImpl.class),
    dubbo(8, "dubbo", JvmBO.class, JvmPO.class, JvmServiceImpl.class),
    ;
    private int type;
    private String typeName;

    @Getter
    private final Class<? extends PipelineBO> boClass;

    @Getter
    private final Class poClass;


    @Getter
    private final Class<? extends TaskBaseService> serviceClass;

    TaskEnum(int type, String typeName, Class<? extends PipelineBO> bo, Class poClass, Class<? extends TaskBaseService> serviceClass) {
        this.type = type;
        this.typeName = typeName;
        this.poClass = poClass;
        this.boClass = bo;
        this.serviceClass = serviceClass;
    }


    public int type() {
        return this.type;
    }

    public String typeName() {
        return this.typeName;
    }


    public static TaskEnum fromType(Integer type) {
        for (TaskEnum routeType : TaskEnum.values()) {
            if (routeType.type == type) {
                return routeType;
            }
        }
        return un_know;
    }

    public static Map<String, Integer> toMap() {
        Map<String, Integer> map = new LinkedHashMap<>();
        TaskEnum[] values = values();
        Arrays.stream(values).forEach(i -> map.put(i.typeName(), i.type()));
        return map;
    }

}
