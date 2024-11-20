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
package org.apache.ozhera.monitor.bo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2022/4/1 11:20 上午
 */
public enum AppType {

    businessType(0, "businessType"),
    hostType(1, "hostType"),
    serverless(2, "serverless"),
    mesh(3, "businessType"),
    ;
    private Integer code;
    private String message;

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    AppType(Integer code, String message){
        this.code = code;
        this.message = message;
    }

    public static AppType getEnum(Integer code){
        if(code == null){
            return null;
        }
        AppType[] values = AppType.values();
        for(AppType value : values){
            if(value.getCode().equals(code)){
                return value;
            }
        }
        return null;
    }

    public static List<Pair> getCodeDescList(){
        List <Pair> list = new ArrayList<>();
        AppType[] values = AppType.values();
        for(AppType value : values){
            Pair pair = new Pair(value.getCode(),value.getMessage());
            list.add(pair);
        }
        return list;
    }

}
