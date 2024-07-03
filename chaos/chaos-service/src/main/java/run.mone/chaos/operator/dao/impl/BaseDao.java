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
package run.mone.chaos.operator.dao.impl;

import com.xiaomi.youpin.docean.Ioc;
import dev.morphia.Datastore;
import dev.morphia.Key;
import dev.morphia.query.*;
import dev.morphia.query.internal.MorphiaCursor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.util.CollectionUtils;
import run.mone.chaos.operator.dao.domain.BaseDomain;
import run.mone.chaos.operator.dto.page.PageData;

import java.util.*;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/13 19:03
 */
@Slf4j
public abstract class BaseDao {

    protected Datastore datastore = Ioc.ins().getBean(Datastore.class);

    public Object insert(BaseDomain domain) {
        domain.insertInit();
        Key<BaseDomain> result = datastore.save(domain);
        log.info("BaseDao.insert={}", result);
        return result.getId();
    }

    public <T extends BaseDomain> int update(Object id, Map<String, Object> kvMap, Class<T> clazz) {
        if (kvMap.isEmpty()) {
            return 0;
        }
        Query<T> query = datastore.createQuery(clazz).field("_id").equal(id);
        UpdateOperations<T> updateOperations = datastore.createUpdateOperations(clazz);
        kvMap.forEach(updateOperations::set);
        updateOperations.set("updateTime", System.currentTimeMillis());
        if (!kvMap.containsKey("updateUser")) {
            updateOperations.set("updateUser", "unknown");
        }
        UpdateResults updateRes = datastore.update(query, updateOperations);
        log.info("BaseDao.update={}", updateRes);
        return updateRes.getUpdatedCount();
    }

    public <T extends BaseDomain> T getById(Object id, Class<T> clazz) {
        if (id == null) {
            return null;
        }
        Query<T> query = datastore.createQuery(clazz).field("_id").equal(new ObjectId(String.valueOf(id)));
        T t = query.get();
        log.info("BaseDao.getById id={}, clazzName={}, result={}", id, clazz.getName(), t);
        return t;
    }

    public <T extends BaseDomain> List<T> queryListById(Collection<Object> ids, Class<T> clazz) {
        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }
        List<Object> idArr = new ArrayList<>(ids);
        Query<T> query = datastore.createQuery(clazz).field("id").in(idArr);
        try (MorphiaCursor<T> tMorphiaCursor = query.find()) {
            return tMorphiaCursor.toList();
        }
    }

    public <T extends BaseDomain> long getTotal(Query<T> query, Class<T> clazz) {
        return query.count();
    }

    public <T extends BaseDomain> PageData<T> getListByPage(PageData<T> pageData, Class<T> clazz) {
        Query<T> query = datastore.createQuery(clazz);
        long all = query.count();
        return getPageData(pageData, query, all);
    }

    private <T extends BaseDomain> PageData<T> getPageData(PageData<T> pageData, Query<T> query, long all) {
        try (MorphiaCursor<T> morphiaCursor = query.order("-updateTime").find(new FindOptions().skip(pageData.getPage() > 0 ? (pageData.getPage() - 1) * pageData.getPageSize() : 0).limit(pageData.getPageSize()))) {
            List<T> list = morphiaCursor.toList();
            pageData.setList(list);
            pageData.setTotal(query.count());
            return pageData;
        }
    }

    public <T extends BaseDomain> PageData<T> getListByMap(Map<String, Object> kvMap, PageData<T> pageData, Class<T> clazz) {
        Query<T> query = datastore.createQuery(clazz);
        long all = query.countAll();
        kvMap.forEach((k, v) -> {
            if (v instanceof Collection) {
                query.field(k).in((Collection) v);
            } else {
                query.field(k).equal(v);
            }
        });
        return getPageData(pageData, query, all);
    }


}
