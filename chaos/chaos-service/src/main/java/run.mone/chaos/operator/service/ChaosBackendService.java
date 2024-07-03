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

import com.google.gson.Gson;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.mvc.ContextHolder;
import com.xiaomi.youpin.docean.mvc.MvcResult;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.ObjectUtils;
import run.mone.chaos.operator.bo.restartPodListBo;
import run.mone.chaos.operator.common.Config;
import run.mone.chaos.operator.dao.domain.ChaosTask;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhangxiaowei6
 */
@Slf4j
@Service
public class ChaosBackendService {

    @Resource
    private LockService lockService;

    @Resource
    private ChaosTaskService chaosTaskService;

    @Resource
    private ChaosMetricsService chaosMetricsService;

    @Resource(name = "podClient")
    private MixedOperation<Pod, PodList, io.fabric8.kubernetes.client.dsl.Resource<Pod>> podClient;

    private final String NACOS_ADMIN_KEY = "admin";

    private final String NACOS_ADMIN_DEFAULT = "zhangxiaowei6,caobaoyu,zhangping17,gaoyulin";

    private String CHAOS_ENV;

    private final Gson gson = new Gson();

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(120))
            .readTimeout(Duration.ofSeconds(120))
            .build();

    public void init() {
        log.info("begin init backendService!");
        CHAOS_ENV = Config.ins().get("chaos.env", "staging");
    }

    public MvcResult<String> restartPodList(restartPodListBo bo) {
        MvcResult<String> mvcResult = new MvcResult<>();
        try {
            AuthUserVo userVo = ((AuthUserVo) ContextHolder.getContext().get()
                    .getSession().getAttribute("TPC_USER"));
            if (!getAdminList().contains(userVo.getAccount())) {
                mvcResult.setCode(401);
                mvcResult.setData("权限不足!");
                return mvcResult;
            }
            // 开始重启给定的pod
            log.info("ChaosBackendService.restart param :{}", bo);
            List<Pod> pods = podClient.inAnyNamespace().withLabel("pipeline-id", String.valueOf(bo.getPipelineId())).list().getItems();
            pods.stream().filter(pod -> bo.getPodIpList().contains(pod.getStatus().getPodIP())).forEach(pod -> podClient.delete(pod));
            mvcResult.setData("ok");
            return mvcResult;
        } catch (Exception e) {
            log.error("ChaosBackendService.restartPodList error:{}", e);
            mvcResult.setCode(500);
            mvcResult.setData("error!");
            return mvcResult;
        }
    }

    public MvcResult<String> deleteCacheKey(String id) {
        MvcResult<String> mvcResult = new MvcResult<>();
        AuthUserVo userVo = ((AuthUserVo) ContextHolder.getContext().get()
                .getSession().getAttribute("TPC_USER"));
        if (!getAdminList().contains(userVo.getAccount())) {
            mvcResult.setCode(401);
            mvcResult.setData("权限不足!");
            return mvcResult;
        }
        ChaosTask chaosTaskById = chaosTaskService.getChaosTaskById(id);
        if (ObjectUtils.isEmpty(chaosTaskById)) {
            mvcResult.setCode(500);
            mvcResult.setData("实验不存在!");
            return mvcResult;
        }

        // 执行实验锁解除
        lockService.chaosUnLock(chaosTaskById.getProjectId(), chaosTaskById.getPipelineId());
        mvcResult.setCode(0);
        mvcResult.setData("ok");
        return mvcResult;
    }

    public MvcResult<List<String>> getAdmin() {
        MvcResult<List<String>> mvcResult = new MvcResult<>();
        mvcResult.setData(getAdminList());
        return mvcResult;
    }

    public MvcResult<String> getAdminStr() {
        MvcResult<String> res = new MvcResult<>();
        res.setData(Config.ins().get(NACOS_ADMIN_KEY, NACOS_ADMIN_DEFAULT));
        return res;
    }

    public MvcResult<String> getServiceAvailabilityUrl() {
        String chaosServiceAvailabilityUrl = chaosMetricsService.getChaosServiceAvailabilityUrl();
        MvcResult<String> mvcResult = new MvcResult<>();
        mvcResult.setData(chaosServiceAvailabilityUrl);
        return mvcResult;
    }

    public MvcResult<String> getReentrantLockValue(int projectId, int pipelineId) {
        log.info("ChaosBackendService.getReentrantLockValue projectId:{},pipelineId:{}", projectId, pipelineId);
        MvcResult<String> res = new MvcResult<>();
        try {
            String reentrantLockValue = lockService.getReentrantLockValue(projectId, pipelineId);
            log.info("ChaosBackendService.getReentrantLockValue reentrantLockValue:{}", reentrantLockValue);
            res.setData(reentrantLockValue);
            return res;
        } catch (Exception e) {
            log.error("ChaosBackendService.getReentrantLockValue error:{}", e);
            res.setCode(500);
            res.setMessage(e.getMessage());
            return res;
        }
    }

    public boolean isAdmin(String userName) {
        return getAdminList().contains(userName);
    }

    private List<String> getAdminList() {
        try {
            String adminStr = Config.ins().get(NACOS_ADMIN_KEY, NACOS_ADMIN_DEFAULT);
            return Arrays.stream(adminStr.split(",")).toList();
        } catch (Exception e) {
            log.error("ChaosBackendService.getAdminList error:{},return default:{}", e, NACOS_ADMIN_DEFAULT);
            return Arrays.stream(NACOS_ADMIN_DEFAULT.split(",")).toList();
        }
    }

}
