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
package org.apache.ozhera.app.auth;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.client.config.utils.MD5;
import org.apache.ozhera.app.common.Result;
import org.apache.ozhera.app.enums.CommonError;
import org.apache.ozhera.app.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.concurrent.locks.ReentrantLock;


@Slf4j
@Service
public class AuthorizationService {

    @NacosValue(value = "${hera.auth.user}",autoRefreshed = true)
    private String userName;

    @NacosValue(value = "${hera.auth.pwd}",autoRefreshed = true)
    private String passWord;

    @NacosValue(value = "${hera.auth.secret}",autoRefreshed = true)
    private String secret;

    @Autowired
    RedisService redisService;

    private static int tokenAccessSequence = 0;
    private static int tokenTimeLimit = 10 * 60;

    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final ReentrantLock lock = new ReentrantLock();

    public Result fetchToken(String user, String sign, Long timestamp){

        log.info("fetchToken user : {} , timestamp : {}",user,df.format(timestamp));

        if(StringUtils.isBlank(user) || StringUtils.isBlank(sign) || timestamp == null){
            log.error("fetchToken param error! user : {}, sign : {}, timestamp :{}",user,sign,timestamp);
            return Result.fail(CommonError.ParamsError);
        }

        Long currentTime = System.currentTimeMillis();
        if(currentTime - timestamp < 0 || currentTime - timestamp > (1000 * 30)){
            log.error("fetchToken param time expired! currentTimeStamp : {}, param timestamp :{}",currentTime,timestamp);
            return Result.fail(CommonError.ParamsError);
        }

        if (!userName.equals(user)) {
            log.info("fetchToken param user error! user : {} ", user);
            return Result.fail(CommonError.NO_AUTHORIZATION);
        }

        String md5Pwd = MD5.getInstance().getMD5String(passWord);
        StringBuilder secretPwdBuffer = new StringBuilder();
        secretPwdBuffer.append(userName).append(md5Pwd).append(timestamp);

        String signSource = MD5.getInstance().getMD5String(secretPwdBuffer.toString());
        if(!signSource.equals(sign)){
            log.info("fetchToken param sign error! user : {} , sign : {}", user, sign);
            return Result.fail(CommonError.NO_AUTHORIZATION);
        }

        return Result.success(generateToken(userName));

    }

    private String generateToken(String userName){

        StringBuilder builder = new StringBuilder();
        builder.append("token:").append(userName).append(":").append(System.currentTimeMillis()).append(":").append(secret);
        String tokenString = MD5.getInstance().getMD5String(builder.toString());

        try {
            redisService.set(tokenString,String.valueOf(tokenAccessSequence),tokenTimeLimit);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
        return tokenString;
    }

    public Result checkAuthorization(String token){

        if(StringUtils.isBlank(token)){
            log.error("checkAuthorization error!token is null!");
            return Result.fail(CommonError.NO_TOKEN);
        }

        String result = null;
        try {
            result = redisService.get(token);
        } catch (Exception e) {
            log.error("checkAuthorization redis exception!" + e.getMessage(),e);
            return Result.fail(CommonError.UnknownError);
        }

        if(StringUtils.isBlank(result)){
            log.error("checkAuthorization token is error or expired!");
            return Result.fail(CommonError.INVALID_TOKEN);
        }

        return Result.success();

    }

    public Integer checkAuthorizationWithSeq(String token,Integer accessSeq){

        lock.lock();
        try {
            if (StringUtils.isBlank(token)) {
                log.error("checkAuthorization error!token is null!");
                return null;
            }

            if (redisService.get(token) == null) {
                log.error("checkAuthorization token is error or expired!");
                return null;
            }

            String accessNumStr = redisService.get(token);
            Integer accessNum = Integer.valueOf(accessNumStr);

            if (accessSeq <= accessNum) {
                log.error("token accessSeq has expired!accessSeq:{}, currentNum:{}", accessSeq, accessNum);
                return null;
            }

            if (accessSeq - accessNum > 1) {
                log.error("token accessSeq error!accessSeq:{}, currentNum:{}", accessSeq, accessNum);
                return null;
            }

            long ttl = redisService.ttl(token);

            redisService.set(token, String.valueOf(accessSeq), ttl);

            return accessSeq + 1;
        } finally {
            lock.unlock();
        }
    }
}
