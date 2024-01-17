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
package com.xiaomi.mone.app.response;

import com.google.gson.Gson;
import com.xiaomi.mone.app.common.Result;
import com.xiaomi.mone.app.response.anno.OriginalResponse;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2022/10/29 13:36
 */
@ControllerAdvice
public class AppResponseAdvice implements ResponseBodyAdvice<Object> {

    private static Gson gson = new Gson();

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        if (ErrorController.class.isAssignableFrom(methodParameter.getExecutable().getDeclaringClass())) {
            return false;
        }
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        /**
         * return original response by this annotation
         */
        if (methodParameter.hasMethodAnnotation(OriginalResponse.class)) {
            return body;
        }
        if (body instanceof Result) {
            return body;
        } else if (body instanceof String) {
            try {
                return gson.toJson(Result.success(body));
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(e.getMessage());
            }
        }
        return Result.success(body);
    }
}
