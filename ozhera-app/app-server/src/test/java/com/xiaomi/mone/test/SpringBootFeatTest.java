package com.xiaomi.mone.test;

import com.xiaomi.mone.app.AppBootstrap;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
        validator(validator);
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
