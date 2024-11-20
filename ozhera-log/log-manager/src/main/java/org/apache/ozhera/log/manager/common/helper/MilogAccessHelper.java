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
package org.apache.ozhera.log.manager.common.helper;

import cn.hutool.core.util.ReflectUtil;
import com.google.common.collect.Lists;
import org.apache.ozhera.log.manager.model.bo.AccessMilogParam;
import org.apache.ozhera.log.manager.model.pojo.MilogLogTailDo;
import com.xiaomi.youpin.docean.anno.Component;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.ozhera.log.common.Constant.SYMBOL_COMMA;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/4/19 14:29
 */
@Component
public class MilogAccessHelper {


    public String validParam(AccessMilogParam milogParam) {
        List<String> builder = Lists.newArrayList();
        Field[] fields = ReflectUtil.getFields(milogParam.getClass());
        for (Field field : fields) {
            Object fieldValue = ReflectUtil.getFieldValue(milogParam, field);
            if (null == fieldValue) {
                builder.add(String.format("%s cannot be empty", field.getName()));
            }
        }
        return builder.stream().collect(Collectors.joining(SYMBOL_COMMA));
    }

    public boolean compareSame(MilogLogTailDo newParam, MilogLogTailDo oldParam) {
        Field[] newFields = ReflectUtil.getFieldsDirectly(newParam.getClass(), false);
        Field[] oldFields = ReflectUtil.getFieldsDirectly(oldParam.getClass(), false);
        for (int i = 0; i < newFields.length; i++) {
            Object newFieldValue = ReflectUtil.getFieldValue(newParam, newFields[i]);
            Object oldFieldValue = ReflectUtil.getFieldValue(oldParam, oldFields[i]);
            if (!Objects.equals(newFieldValue, oldFieldValue)) {
                return false;
            }
        }
        return true;
    }
}
