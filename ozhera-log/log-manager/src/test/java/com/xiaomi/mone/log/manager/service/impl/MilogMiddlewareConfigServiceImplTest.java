/*
 * Copyright (C) 2020 Xiaomi Corporation
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
package com.xiaomi.mone.log.manager.service.impl;

import com.xiaomi.mone.log.api.model.bo.MiLogResource;
import static com.xiaomi.mone.log.manager.common.utils.ManagerUtil.getConfigFromNanos;
import com.xiaomi.youpin.docean.Ioc;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author: songyutong1
 * @date: 2024/08/22/15:15
 */
public class MilogMiddlewareConfigServiceImplTest {

//    @Test
//    public void testCheckEsAddressPortOperate() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//        getConfigFromNanos();
//        Ioc.ins().init("com.xiaomi");
//        MilogMiddlewareConfigServiceImpl milogMiddlewareConfigService = Ioc.ins().getBean(MilogMiddlewareConfigServiceImpl.class);
//        Method method = milogMiddlewareConfigService.getClass().getDeclaredMethod("checkEsAddressPortOperate", MiLogResource.class);
//        method.setAccessible(true);
//
//        MiLogResource miLogResource = new MiLogResource();
//        miLogResource.setServiceUrl("localhost");
//        method.invoke(milogMiddlewareConfigService, miLogResource);
//        Assert.assertEquals("localhost:80", miLogResource.getServiceUrl());
//
//        miLogResource.setServiceUrl("localhost:80");
//        method.invoke(milogMiddlewareConfigService, miLogResource);
//        Assert.assertEquals("localhost:80", miLogResource.getServiceUrl());
//
//        miLogResource.setServiceUrl("localhost:");
//        method.invoke(milogMiddlewareConfigService, miLogResource);
//        Assert.assertEquals("localhost:80", miLogResource.getServiceUrl());
//    }

}
