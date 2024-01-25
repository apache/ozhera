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
package run.mone.trace.etl.extension.es;

import com.alibaba.fastjson.JSONObject;
import com.xiaomi.mone.es.EsProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Slf4j
public class EsTraceUtil {

    @Autowired
    private EsProcessor esProcessor;

    @Resource(name = "errorEsProcessor")
    private EsProcessor errorEsProcessor;

    public void insertBulk(String index,String json){
        try {
            JSONObject jsonObject = JSONObject.parseObject(json);
            esProcessor.bulkInsert(index, jsonObject);
        }catch (Exception e){
            log.error("Insert jaeger es data exception:",e);
        }
    }

    public void insertBulk(String index,Map jsonMap){
        try {
            esProcessor.bulkInsert(index, jsonMap);
        }catch (Exception e){
            log.error("Insert jaeger es data exception:",e);
        }
    }

    public void insertErrorBulk(String index,Map jsonMap){
        try {
            errorEsProcessor.bulkInsert(index, jsonMap);
        }catch (Exception e){
            log.error("insert error es exception：",e);
        }
    }
}
