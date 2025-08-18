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

package org.apache.ozhera.prometheus.agent.service.prometheus;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.ozhera.prometheus.agent.Commons;
import org.apache.ozhera.prometheus.agent.Impl.RuleAlertDao;
import org.apache.ozhera.prometheus.agent.entity.RuleAlertEntity;
import org.apache.ozhera.prometheus.agent.enums.ErrorCode;
import org.apache.ozhera.prometheus.agent.enums.RuleAlertStatusEnum;
import org.apache.ozhera.prometheus.agent.param.alert.RuleAlertParam;
import org.apache.ozhera.prometheus.agent.result.Result;
import org.apache.ozhera.prometheus.agent.result.alertManager.AlertManagerFireResult;
import org.apache.ozhera.prometheus.agent.service.alarmContact.DingAlertContact;
import org.apache.ozhera.prometheus.agent.service.alarmContact.FeishuAlertContact;
import org.apache.ozhera.prometheus.agent.service.alarmContact.MailAlertContact;
import org.apache.ozhera.prometheus.agent.vo.PageDataVo;
import lombok.extern.slf4j.Slf4j;
import org.nutz.lang.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RuleAlertService {
    @Autowired
    RuleAlertDao dao;

    @Autowired
    FeishuAlertContact feishuAlertContact;

    @Autowired
    MailAlertContact mailAlertContact;

    @Autowired(required = false)
    DingAlertContact dingAlertContact;

    @NacosValue(value = "${hera.alertmanager.url}", autoRefreshed = true)
    private String silenceUrl;
    @NacosValue(value = "${hera.alert.type}", autoRefreshed = true)
    private String alertTYPE;

    public static final Gson gson = new Gson();

    public Result CreateRuleAlert(RuleAlertParam param) {
        log.info("RuleAlertService.CreateRuleAlert  param : {}", gson.toJson(param));
        try {
            if (param.getPromCluster() == null) {
                param.setPromCluster(Commons.DEFAULT_ALERT_PROM_CLUSTER);
            }
            RuleAlertEntity ruleAlertEntity = new RuleAlertEntity();
            ruleAlertEntity.setName(param.getAlert() + "-" + param.getCname());
            ruleAlertEntity.setCname(param.getCname());
            ruleAlertEntity.setExpr(param.getExpr());
            ruleAlertEntity.setLabels(transLabel2String(param.getLabels()));
            ruleAlertEntity.setAnnotation(transAnnotation2String(param.getAnnotations()));
            ruleAlertEntity.setAlertFor(param.getForTime());
            ruleAlertEntity.setEnv(Strings.join(",", param.getEnv()));
            ruleAlertEntity.setEnabled(param.getEnabled() == null ? 1 : param.getEnabled());
            ruleAlertEntity.setPriority(transPriority2Integer(param.getPriority()));
            ruleAlertEntity.setCreatedBy(String.join(",", param.getAlert_member()));
            ruleAlertEntity.setCreatedTime(new Date());
            ruleAlertEntity.setUpdatedTime(new Date());
            ruleAlertEntity.setDeletedBy("");
            ruleAlertEntity.setPromCluster(param.getPromCluster());
            ruleAlertEntity.setStatus(RuleAlertStatusEnum.PENDING.getDesc());
            ruleAlertEntity.setType("0");
            if (param.getAlert_member() != null) {
                ruleAlertEntity.setAlertMember(Strings.join(",", param.getAlert_member()));
            }
            ruleAlertEntity.setAlertAtPeople(Strings.join(",", param.getAlert_at_people()));
            ruleAlertEntity.setAlert_group(param.getGroup() == null ? "example" : param.getGroup());

            log.info("RuleAlertService.CreateRuleAlert ruleAlertEntity:{}", gson.toJson(ruleAlertEntity));
            Long id = dao.CreateRuleAlert(ruleAlertEntity);
            log.info("RuleAlertService.CreateRuleAlert  res : {}", id);
            return Result.success(id);
        } catch (Exception e) {
            log.error("RuleAlertService.CreateRuleAlert  fail, name: {},message: {}", param.getAlert(), e.getMessage());
            return Result.fail(ErrorCode.unknownError, e.getMessage());
        }
    }

    public Result UpdateRuleAlert(String id, RuleAlertParam param) {
        log.info("RuleAlertService.UpdateRuleAlert  param : {}", gson.toJson(param));

        // Incremental replacement, allowing fields to be modified: cname、expr、for、labels、annotations、priority、env、alertMember
        try {
            RuleAlertEntity data = dao.GetRuleAlert(id);

            if (param.getCname() != null) {
                data.setCname(param.getCname());
            }
            if (param.getExpr() != null) {
                data.setExpr(param.getExpr());
            }
            if (param.getForTime() != null) {
                data.setAlertFor(param.getForTime());
            }
            if (param.getLabels() != null) {
                data.setLabels(transLabel2String(param.getLabels()));
            }
            if (param.getAnnotations() != null) {
                data.setAnnotation(transAnnotation2String(param.getAnnotations()));
            }
            if (param.getPriority() != null) {
                data.setPriority(transPriority2Integer(param.getPriority()));
            }
            if (param.getEnv() != null) {
                data.setEnv(Strings.join(",", param.getEnv()));
            }
            if (param.getAlert_member() != null) {
                data.setAlertMember(Strings.join(",", param.getAlert_member()));
            }
            if (param.getAlert_at_people() != null) {
                data.setAlertAtPeople(Strings.join(",", param.getAlert_at_people()));
            }
            data.setUpdatedTime(new Date());
            log.info("RuleAlertService.UpdateRuleAlert data : {}", gson.toJson(data));
            String res = dao.UpdateRuleAlert(id, data);
            return Result.success(res);
        } catch (Exception e) {
            log.error("RuleAlertService.UpdateRuleAlert fail param : {}", gson.toJson(param));
            return Result.fail(ErrorCode.unknownError, e.getMessage());
        }
    }

    public Result DeleteRuleAlert(String id) {
        log.info("RuleAlertService.DeleteRuleAlert id : {}", id);

        try {
            int res = dao.DeleteRuleAlert(id);
            if (res != 1) {
                return Result.fail(ErrorCode.OperationFailed);
            }
            log.info("RuleAlertService.DeleteRuleAlert res : {}", res);
            return Result.success(res);
        } catch (Exception e) {
            log.error("RuleAlertService.DeleteRuleAlert fail, id : {}", id);
            return Result.fail(ErrorCode.unknownError, e.getMessage());
        }
    }

    public Result GetRuleAlert(String id) {
        log.info("RuleAlertService.GetRuleAlert id : {}", id);
        RuleAlertEntity ruleAlertEntity = dao.GetRuleAlert(id);
        log.info("RuleAlertService.GetRuleAlert res : {}", gson.toJson(ruleAlertEntity));
        return Result.success(ruleAlertEntity);
    }

    public Result GetRuleAlertList(Integer pageSize, Integer pageNo) {
        log.info("RuleAlertService.GetRuleAlertList pageSize : {} pageNo : {}", pageSize, pageNo);
        List<RuleAlertEntity> lists = dao.GetRuleAlertList(pageSize, pageNo);
        PageDataVo<RuleAlertEntity> pdo = new PageDataVo<RuleAlertEntity>();
        pdo.setPageNo(pageNo);
        pdo.setPageSize(pageSize);
        pdo.setTotal(dao.CountRuleAlert());
        pdo.setList(lists);
        log.info("RuleAlertService.GetRuleAlertList count : {}", pdo.getTotal());
        return Result.success(pdo);
    }

    public Result EnabledRuleAlert(String id, String enabled) {
        log.info("RuleAlertService.EnabledRuleAlert id : {} enabled : {}", id, enabled);
        try {
            RuleAlertEntity ruleAlertEntity = dao.GetRuleAlert(id);
            if (ruleAlertEntity == null) {
                return Result.fail(ErrorCode.NO_DATA_FOUND);
            }
            ruleAlertEntity.setEnabled(Integer.parseInt(enabled));
            String res = dao.UpdateRuleAlert(id, ruleAlertEntity);
            return Result.success(res);
        } catch (Exception e) {
            log.error("RuleAlertService.EnabledRuleAlert fail id:{}", id);
            return Result.fail(ErrorCode.unknownError, e.getMessage());
        }
    }

    //TODO: Create different Feishu cards through different templates
    //TODO: Construct alarm notifications through different types, such as Feishu and email
    public Result SendAlert(String body) {
        JsonObject jsonObject = gson.fromJson(body, JsonObject.class);
        //log.info("SendAlert jsonObject:{}", gson.toJson(jsonObject));
        AlertManagerFireResult fireResult = gson.fromJson(body, AlertManagerFireResult.class);
        //Build alarm triggers by type.
        switch (alertTYPE) {
            case "feishu":
                feishuAlertContact.Reach(fireResult);
                break;
            case "mail":
                mailAlertContact.Reach(fireResult);
                break;
            case "dingding":
                dingAlertContact.Reach(fireResult);
                break;
            default:
                feishuAlertContact.Reach(fireResult);
        }
        return Result.success("发送告警");
    }

    //TODO: Temporary method provided to alertManagerClient, which needs to be refactored in the future.
    public List<RuleAlertEntity> GetAllRuleAlertList() {
        log.info("RuleAlertService.GetAllRuleAlertList");
        List<RuleAlertEntity> list = dao.GetAllRuleAlertList();
        return list;
    }

    public List<RuleAlertEntity> GetAllCloudRuleAlertList(String status) {
        log.info("RuleAlertService.GetAllCloudRuleAlertList");
        List<RuleAlertEntity> list = dao.GetAllCloudRuleAlertList(status);
        return list;
    }

    public void UpdateRuleAlertDeleteToDone(String alertName) {
        log.info("ScrapeJobService.UpdateRuleAlertDeleteToDone  status : {}", RuleAlertStatusEnum.DONE.getDesc());
        int affectRow = dao.UpdateRuleAlertDeleteToDone(alertName);
        log.info("ScrapeJobService.UpdateRuleAlertDeleteToDone affectRow num:{}", affectRow);
    }

    // convert labelmap to string
    private String transLabel2String(Map<String, String> labels) {
        String res = "";
        try {
            for (Map.Entry<String, String> entry : labels.entrySet()) {
                res += entry.getKey() + "=" + entry.getValue() + ",";
            }
            return res.substring(0, res.length() - 1);
        } catch (Exception e) {
            log.error("transLabel2String error:{}", e.getMessage());
            return "";
        }
    }

    // convert the annotationMap to a json string
    private String transAnnotation2String(Map<String, String> annotations) {
        return gson.toJson(annotations);
//        String res = "";
//        for (Map.Entry<String, String> entry : annotations.entrySet()) {
//            res += entry.getKey() + "=" + entry.getValue() + ",";
//        }
//        return res.substring(0, res.length() - 1);
    }

    private int transPriority2Integer(String priority) {
        try {
            String[] ps = priority.split("P");
            if (ps.length == 2) {
                return Integer.parseInt(ps[1]);
            } else {
                // default P2
                return 2;
            }
        } catch (Exception e) {
            log.error("transPriority2Integer error:{}", e.getMessage());
            return 2;
        }
    }


}