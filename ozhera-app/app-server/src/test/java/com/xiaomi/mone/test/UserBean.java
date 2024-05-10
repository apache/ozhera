package com.xiaomi.mone.test;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 *
 * @description
 * @version 1.0
 * @author wtt
 * @date 2024/4/28 10:10
 *
 */
@Data
public class UserBean {

    @NotBlank
    private String userName;

    @Min(value = 18, message = "age最小值为18")
    private Integer age;
}
