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
package run.mone.chaos.operator.controller;

import com.xiaomi.youpin.docean.anno.Controller;
import com.xiaomi.youpin.docean.anno.RequestMapping;
import com.xiaomi.youpin.infra.rpc.Result;
import org.apache.commons.lang3.ObjectUtils;
import run.mone.chaos.operator.service.UserService;
import run.mone.chaos.operator.vo.UserInfoVo;

import javax.annotation.Resource;

/**
 * @author caobaoyu
 * @description:
 * @date 2024-04-14 14:28
 */
@Controller
public class UserController {

    @Resource
    private UserService userService;

    @RequestMapping(path = "/user/getUserInfo")
    public Result<UserInfoVo> userInfoVo() {
        UserInfoVo userInfo = userService.getUserInfo();
        if (ObjectUtils.isEmpty(userInfo)) {
            throw new RuntimeException("user not exist");
        }
        return Result.success(userInfo);
    }

}
