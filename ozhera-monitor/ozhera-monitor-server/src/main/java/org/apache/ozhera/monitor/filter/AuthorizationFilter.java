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

package org.apache.ozhera.monitor.filter;

import org.apache.ozhera.monitor.service.user.LocalUser;
import org.apache.ozhera.monitor.service.user.MoneUserDetailService;
import org.apache.ozhera.monitor.service.user.UseDetailInfo;
import com.xiaomi.mone.tpc.login.enums.UserTypeEnum;
import com.xiaomi.mone.tpc.login.util.UserUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.util.CollectionUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author gaoxihui
 * @date 2021/11/8 9:07 AM
 */
@Order(5)
@WebFilter(filterName = "authorizationFilter", urlPatterns = "/*")
public class AuthorizationFilter implements Filter {

    public final static String ADMIN_OP_URI_PREFIX = "/api-manual/";
    @Autowired
    MoneUserDetailService moneUserDetailService;

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        AuthUserVo userVo = UserUtil.getUser();
        if (userVo == null || !UserTypeEnum.CAS_TYPE.getCode().equals(userVo.getUserType())) {
            chain.doFilter(request,response);
            return;
        }
        List<String> blackList = moneUserDetailService.getBlackList();
        if (blackList.contains(userVo.getAccount())) {
            noAuthResponse(response);
            return;
        }
        String url = ((HttpServletRequest)request).getRequestURI();
        if(url.indexOf(ADMIN_OP_URI_PREFIX) >= 0){
            List<String> adminUserList = moneUserDetailService.getAdminUserList();
            if(!adminUserList.contains(userVo.getAccount())){
                noAuthResponse(response);
                return;
            }
        }
        UseDetailInfo detailInfo = moneUserDetailService.queryUser(userVo.getCasUid());
        if (detailInfo != null) {
            List<String> deptBlackList = moneUserDetailService.getDeptBlackList();
            if (deptBlackList.contains(detailInfo.getDeptDescr())) {
                List<String> whiteList = moneUserDetailService.getWhiteList();
                if(!CollectionUtils.isEmpty(whiteList) && whiteList.contains(detailInfo.getUserName())){
                    try {
                        LocalUser.set(detailInfo);
                        chain.doFilter(request, response);
                    } finally {
                        LocalUser.clear();
                    }
                }else{
                    noAuthResponse(response);
                    return;
                }
            }else{
                try {
                    LocalUser.set(detailInfo);
                    chain.doFilter(request, response);
                } finally {
                    LocalUser.clear();
                }
            }
        } else {
            chain.doFilter(request, response);
        }

    }

    private void noAuthResponse(ServletResponse response) throws IOException {
        HttpServletResponse httpServletResponse = (HttpServletResponse)response;
        httpServletResponse.setStatus(401);
    }

}
