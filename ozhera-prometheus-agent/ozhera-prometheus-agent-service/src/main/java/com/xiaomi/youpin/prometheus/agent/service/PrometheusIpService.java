package com.xiaomi.youpin.prometheus.agent.service;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.xiaomi.data.push.client.HttpClientV2;
import com.xiaomi.youpin.prometheus.agent.domain.Ips;
import com.xiaomi.youpin.prometheus.agent.service.api.PrometheusIpServiceExtension;
import com.xiaomi.youpin.prometheus.agent.service.dto.heraApp.GetAllPodIpRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class PrometheusIpService {

    @Autowired
    private PrometheusIpServiceExtension prometheusIpServiceExtension;

    @NacosValue(value = "${hera.app.addr}", autoRefreshed = true)
    private String heraAppAddr;

    @NacosValue(value = "${golang.runtime.default.port}", autoRefreshed = true)
    private String goRuntimePort;

    private static final Gson gson = new Gson();

    public List<Ips> getByType(String type) {
        return prometheusIpServiceExtension.getByType(type);
    }

    public Set<String> getIpsByAppName(String name) {
        return prometheusIpServiceExtension.getIpsByAppName(name);
    }

    public Set<String> getEtcdHosts() {
        return prometheusIpServiceExtension.getEtcdHosts();
    }


    public List<Ips> getK8sNodeIp(String type) {
        return prometheusIpServiceExtension.getK8sNodeIp(type);
    }

    public List<Ips> getHeraAppPodIp() {
        log.info("getHeraAppPodIp begin heraAppAddr:{},goRuntimePort: {}", heraAppAddr, goRuntimePort);
        List<Ips> res = new ArrayList<>();
        String url = heraAppAddr + "/hera/app/env/non/probe/ips";
        log.info("getHeraAppPodIp url:{}", url);
        Map<String, String> headers = new HashMap(1);
        headers.put("Content-Type", "application/json; charset=utf-8");
        try {
            String heraAppRes = HttpClientV2.get(url, headers, 10000);
            GetAllPodIpRes getAllPodIpRes = gson.fromJson(heraAppRes, GetAllPodIpRes.class);
            log.info("getHeraAppPodIp heraAppRes : {} ", heraAppRes);
            if (getAllPodIpRes == null || getAllPodIpRes.getCode() != 0) {
                return res;
            }
            Ips ips = new Ips();
            List<String> ipStr = new ArrayList<>();
            getAllPodIpRes.getData().forEach(it -> {
                ipStr.add(it + ":" + goRuntimePort);
            });
            ips.setTargets(ipStr);
            res.add(ips);
            return res;
        } catch (Exception e) {
            log.error("getHeraAppPodIp error:{}", e);
            return res;
        }
    }

}
