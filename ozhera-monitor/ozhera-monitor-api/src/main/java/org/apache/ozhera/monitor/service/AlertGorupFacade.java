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
package org.apache.ozhera.monitor.service;


import org.apache.ozhera.monitor.service.bo.AlertGroupQryInfo;

import java.util.List;

public interface AlertGorupFacade {

    List<AlertGroupQryInfo> query(String account, String likeName);

    List<AlertGroupQryInfo> queryByIds(String account, List<Long> ids);
}
