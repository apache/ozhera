package com.xiaomi.mone.app.service.mq.model;

import com.google.gson.JsonObject;
import com.xiaomi.mone.app.model.HeraAppBaseInfo;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author gaoxihui
 * @date 2022/2/21 2:38 下午
 */

@ToString
@Data
public class HeraAppMessage implements Serializable {

    private String id;

    private Integer iamTreeId;

    private Integer iamTreeType;

    private String appName;

    private String appCname;

    private String owner;

    private Integer platformType;

    private Integer bindType;

    private String appLanguage;

    private Integer appType;

    private JsonObject envMapping;

    private List<String> joinedMembers;

    private Integer delete;

    private Map<String,String> env;

    public HeraAppBaseInfo baseInfo(){

        HeraAppBaseInfo heraAppBaseInfo = new HeraAppBaseInfo();

        heraAppBaseInfo.setBindId(String.valueOf(this.getId()));
        heraAppBaseInfo.setBindType(this.getBindType());
        heraAppBaseInfo.setAppName(this.getAppName());
        heraAppBaseInfo.setAppCname(this.getAppCname());

        if(env != null && StringUtils.isNotBlank(env.get("appLanguage"))){
            heraAppBaseInfo.setAppLanguage(env.get("appLanguage"));
        }else{
            heraAppBaseInfo.setAppLanguage(this.getAppLanguage());
        }

        heraAppBaseInfo.setPlatformType(this.getPlatformType());
        heraAppBaseInfo.setAppType(this.getAppType());
        heraAppBaseInfo.setEnvsMap(this.getEnvMapping() == null ? "" : this.getEnvMapping().toString());

        heraAppBaseInfo.setIamTreeId(this.getIamTreeId());
        heraAppBaseInfo.setIamTreeType(this.getIamTreeType());

        return heraAppBaseInfo;
    }

}
