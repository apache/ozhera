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
package org.apache.ozhera.app.test;

import org.junit.jupiter.api.Test;

import javax.annotation.Resource;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 *
 * @description
 * @version 1.0
 * @author wtt
 * @date 2024/4/28 10:02
 *
 */
//@SpringBootTest(classes = AppBootstrap.class)
public class SpringBootFeatTest {

    @Resource
    private Validator validator;

    @Test
    public void validatorSpringTest() {
//        validator(validator);
    }

    private void validator(Validator validator) {

        UserBean userBean = new UserBean();
        userBean.setUserName("");
        userBean.setAge(17);

        Set<ConstraintViolation<UserBean>> constraintViolations = validator.validate(userBean);
        System.out.println("validate 校验对象属性：");
        System.out.println(constraintViolations);
        System.out.println();

        constraintViolations = validator.validateProperty(userBean, "age");
        System.out.println("validate 校验对象属性age：");
        System.out.println(constraintViolations);
        System.out.println();

        constraintViolations = validator.validateValue(UserBean.class, "age", 15);
        System.out.println("validate 校验对象属性age：");
        System.out.println(constraintViolations);
    }

    @Test
    public void validatorJavaTest() {
        Validator validator1 = ValidationUtils.getValidator();
        validator(validator1);
    }

    public static class ValidationUtils {

        public static Validator getValidator() {
            ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
            return validatorFactory.getValidator();
        }
    }

}
