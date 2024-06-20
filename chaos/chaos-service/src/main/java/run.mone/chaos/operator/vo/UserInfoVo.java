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
package run.mone.chaos.operator.vo;

import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import lombok.Data;

@Data
public class UserInfoVo {

    private String fullAccount;
    private int userType;
    private String username;//用户账号
    private String name;//名称
    private String displayName;//展示名称
    private String email;//邮箱
    private String id;//id
    private String avatar;//头像图片
    private String tenant;
    private boolean admin;  //是否admin


    public static UserInfoVo convert(AuthUserVo userVo) {
        UserInfoVo userInfo = new UserInfoVo();
        userInfo.setDisplayName(userVo.getName());
        userInfo.setEmail(userVo.getEmail());
        userInfo.setName(userVo.getName());
        userInfo.setUsername(userVo.getAccount());
        userInfo.setFullAccount(userVo.genFullAccount());
        userInfo.setAvatar(userVo.getAvatarUrl());
        userInfo.setUserType(userVo.getUserType());
        return userInfo;
    }


}
