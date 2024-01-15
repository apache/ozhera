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
package com.xiaomi.mone.log.manager.service.extension.store;

import com.xiaomi.mone.log.manager.model.dto.LogStorageData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/10 14:28
 */
public class EsLogStorageService implements LogStorageService {

    @Override
    public boolean createTable(LogStorageData logStorageData) {
        return false;
    }

    @Override
    public boolean updateTable(LogStorageData logStorageData) {
        return false;
    }

    @Override
    public boolean deleteTable(LogStorageData logStorageData) {
        return false;
    }

    @Override
    public List<String> getColumnList(Long clusterId, String tableName) {
        return new ArrayList<>();
    }
}
