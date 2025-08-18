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
package org.apache.ozhera.log.manager.dao;

import org.apache.ozhera.log.manager.model.pojo.LogStreamBalanceConfig;
import com.xiaomi.youpin.docean.anno.Service;
import org.nutz.dao.Cnd;
import org.nutz.dao.impl.NutDao;

import javax.annotation.Resource;
import java.util.List;

import static org.apache.ozhera.log.common.Constant.EQUAL_OPERATE;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/10/16 15:48
 */
@Service
public class LogStreamBalanceConfigDao {

    @Resource
    private NutDao dao;

    public List<LogStreamBalanceConfig> queryConfigEnabled() {
        return dao.query(LogStreamBalanceConfig.class, Cnd.where("status", EQUAL_OPERATE, 0));
    }

}
