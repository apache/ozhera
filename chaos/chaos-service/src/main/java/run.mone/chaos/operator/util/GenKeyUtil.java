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

/**
 * @author zhangxiaowei6
 * @Date 2024/4/15 14:23
 */


public class GenKeyUtil {

    public static String genRedisLockKey(String projectId, String pipelineId) {
        return "mione-chaos-lock-" + projectId + "-" + pipelineId;
    }

    public static String genRecoverRedisLockKey(String projectId, String pipelineId) {
        return "mione-chaos-lock-recover" + projectId + "-" + pipelineId;
    }

    public static String genBotCronKey(String mongoId) {
        return "mione-chaos-bot-cron-" + mongoId;
    }

    public static String genBotAlarmKey(String projectName, int projectId, String type, int pipelineId) {
        return "mione-chaos-bot-alarm-" + projectName + "-" + projectId + "-" + pipelineId + "-" + type;
    }
}
