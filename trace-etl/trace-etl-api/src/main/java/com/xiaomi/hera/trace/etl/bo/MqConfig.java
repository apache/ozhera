package com.xiaomi.hera.trace.etl.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author goodjava@qq.com
 * @date 2023/9/19 17:36
 */
@Data
public class MqConfig implements Serializable {


    private String group;

    private String nameSerAddr;

    private String topicName;


}
