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
package run.mone.chaos.operator.service;

import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import run.mone.chaos.operator.util.GenKeyUtil;

import javax.annotation.Resource;

/**
 * @author zhangxiaowei6
 * @Date 2024/4/18 14:58
 */
@Slf4j
@Service
public class LockService {

    @Resource
    private RedisCacheService cache;
    public boolean chaosLock(Integer projectId, Integer pipelineId,String redisValue) {
        String key = GenKeyUtil.genRedisLockKey(String.valueOf(projectId), String.valueOf(pipelineId));
        boolean res = cache.lockWithoutTime(key,redisValue);
        log.info("ChaosTaskController.chaosLock key: {} ,res: {},redisValue: {}", key, res,redisValue);
        return res;
    }

    public void chaosUnLock(Integer projectId, Integer pipelineId) {
        String key = GenKeyUtil.genRedisLockKey(String.valueOf(projectId), String.valueOf(pipelineId));
        boolean unlock = cache.unlock(key);
        log.info("ChaosTaskController.chaosUnLock res: {}", unlock);
    }

    public boolean chaosRecoverLock(Integer projectId, Integer pipelineId) {
        String key = GenKeyUtil.genRecoverRedisLockKey(String.valueOf(projectId), String.valueOf(pipelineId));
        boolean res = cache.lock(key, 60*1000);
        log.info("ChaosTaskController.chaosRecoverLock key: {} ,res: {}", key, res);
        return res;
    }

    public void chaosRecoverUnLock(Integer projectId, Integer pipelineId) {
        String key = GenKeyUtil.genRecoverRedisLockKey(String.valueOf(projectId), String.valueOf(pipelineId));
        boolean unlock = cache.unlock(key);
        log.info("ChaosTaskController.chaosRecoverUnLock res: {}", unlock);
    }

    public String getReentrantLockValue(int projectId,int pipelineId) {
        String key = GenKeyUtil.genRedisLockKey(String.valueOf(projectId), String.valueOf(pipelineId));
        return cache.get(key);
    }
}
