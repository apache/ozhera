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
package run.mone.chaos.operator.util;

import io.fabric8.kubernetes.api.model.Pod;
import org.apache.commons.lang3.StringUtils;
import run.mone.chaos.operator.constant.CmdConstant;

/**
 * @author zhangxiaowei6
 * @Date 2023/12/6 10:48
 */


public class AnnotationUtil {

    //生成pod的annotation，用于记录podFailure过程的原始信息

    public static String GenKeyForImage(String podName, String containerName, boolean isInit) {
        String doing = "";
        if (isInit) {
            doing = String.format(CmdConstant.POD_FAILURE_ANNOTATION, containerName, podName, "-init");
        } else {
            doing = String.format(CmdConstant.POD_FAILURE_ANNOTATION, containerName, podName, "-normal");
        }

        //超过63 直接用containerName记录
        if (StringUtils.length(doing) > 63) {
            doing = containerName;
        }

        return doing;
    }
}
