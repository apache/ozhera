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
package com.xiaomi.mone.test;

import com.xiaomi.mone.app.api.model.HeraMetaDataPortModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

@Slf4j
public class HeraMetaDataTest {

    @Test
    public void testField(){
        HeraMetaDataPortModel portModel = new HeraMetaDataPortModel();
        portModel.setThriftPort(10030);

        Class<? extends HeraMetaDataPortModel> aClass = portModel.getClass();
        Field[] declaredFields = aClass.getDeclaredFields();
        for(Field field : declaredFields){
            field.setAccessible(true);
            try {
                int o = (int) field.get(portModel);
                if(o > 0){
                    System.out.println("get port : "+o);
                    return;
                }
            } catch (Exception e) {
                log.error("Hera meta data Consumer getAvailablePort error : ",e);
            }
        }
        System.out.println("no get port");
    }
}
