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
package org.apache.ozhera.log.manager.service;

import org.apache.ozhera.log.manager.common.context.MoneUserContext;
import org.apache.ozhera.log.manager.domain.Tpc;
import com.xiaomi.mone.tpc.common.enums.UserTypeEnum;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import com.xiaomi.youpin.docean.Ioc;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/2/13 15:50
 */
@Slf4j
public class TpcTest {
    private Tpc tpc;

    @Before
    public void buildBean() {
        Ioc.ins().init("com.xiaomi");
        tpc = Ioc.ins().getBean(Tpc.class);
        //Simulate user login
        AuthUserVo authUserVo = new AuthUserVo();
        authUserVo.setAccount("zhangsan29");
        authUserVo.setUserType(UserTypeEnum.CAS_TYPE.getCode());
        MoneUserContext.setCurrentUser(authUserVo, true);
    }

}
