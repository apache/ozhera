package com.xiaomi.mone.monitor.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author gaoxihui
 * @date 2021/9/14 3:17 下午
 */
@Data
public class Triple<T extends Object> implements Serializable {

    T value;
    String label;
    String enLabel;

    public Triple(T value, String label, String enLabel){
        this.label = label;
        this.enLabel = enLabel;
        this.value = value;
    }
}
