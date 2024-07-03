/*
 *  Copyright 2020 Xiaomi
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package run.mone.chaos.operator.config;

import com.google.common.collect.Sets;
import com.xiaomi.hera.trace.context.TraceIdUtil;
import com.xiaomi.mone.log.common.Config;
import com.xiaomi.mone.tpc.login.filter.DoceanReqUserFilter;
import com.xiaomi.mone.tpc.login.util.ConstUtil;
import com.xiaomi.mone.tpc.login.vo.AuthUserVo;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.aop.AopContext;
import com.xiaomi.youpin.docean.aop.EnhanceInterceptor;
import com.xiaomi.youpin.docean.mvc.ContextHolder;
import com.xiaomi.youpin.docean.mvc.MvcContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import run.mone.chaos.operator.common.context.MoneUserContext;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class FilterConfiguration extends EnhanceInterceptor {

    @Resource
    private DoceanReqUserFilter doceanReqUserFilter;

    public FilterConfiguration() {
        doceanReqUserFilter = new DoceanReqUserFilter();
        Config config = Config.ins();
        Map<String, String> map = new HashMap<>();
        map.put(ConstUtil.devMode, config.get("tpc.devMode", "false"));
        map.put(ConstUtil.innerAuth, "true");
        //map.put(ConstUtil.authTokenUrl, config.get("auth_token_url", "http://127.0.0.1:8998/login/token/parse"));
        map.put(ConstUtil.ignoreUrl, "/chaosApiTask/*");
        //map.put(ConstUtil.loginUrl, config.get("tpc_login_url", ""));
       // map.put(ConstUtil.logoutUrl, config.get("tpc_logout_url", ""));
        map.put(ConstUtil.PUBLIC_KEY_FILTER_INIT_PARAM_KEY, config.get("aegis.sdk.public.key", ""));
        doceanReqUserFilter.init(map);
    }

    private static final Integer MAX_LENGTH = 3000;


    private String filterUrls = "";//((Config)Ioc.ins().getBean(Config.class)).get("filter_urls", "");

    private static final Set<String> TPC_HEADERS_URLS = Sets.newHashSet();

    private List<String> filterUrlList;

    {
        filterUrlList = Arrays.stream(filterUrls.split(",")).distinct().collect(Collectors.toList());
        //TPC_HEADERS_URLS.add(SPACE_PAGE_URL);
    }

    @Override
    public void before(AopContext aopContext, Method method, Object[] args) {
        /**
         * User information will not be available in the context
         */
        if (filterUrlList.contains(method.getName())) {
            return;
        }
        MvcContext mvcContext = ContextHolder.getContext().get();
        try {
            saveUserInfoThreadLocal(mvcContext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveUserInfoThreadLocal(MvcContext mvcContext) throws Exception {
        AuthUserVo userVo = (AuthUserVo) mvcContext.session().getAttribute(ConstUtil.TPC_USER);
        if (null == userVo && !doceanReqUserFilter.doFilter(mvcContext)) {
            throw new Exception("please go to login");
        }
        userVo = (AuthUserVo) mvcContext.session().getAttribute(ConstUtil.TPC_USER);
        MoneUserContext.setCurrentUser(null);
    }

    @Override
    public Object after(AopContext context, Method method, Object res) {
        solveResHeaders();
        clearThreadLocal();
        return super.after(context, method, res);
    }

    private void solveResHeaders() {
        MvcContext mvcContext = ContextHolder.getContext().get();
        mvcContext.getResHeaders().put("traceId", TraceIdUtil.traceId() == null ? StringUtils.EMPTY : TraceIdUtil.traceId());
        if (TPC_HEADERS_URLS.contains(mvcContext.getPath())) {
            mvcContext.getResHeaders().put("tpc_home_url_head", ((Config)Ioc.ins().getBean(Config.class)).get("tpc_home_url_head", "https://127.0.0.1"));
        }
    }

    @Override
    public void exception(AopContext context, Method method, Throwable ex) {
        log.error("data exception,", ex);
        clearThreadLocal();
        super.exception(context, method, ex);
    }

    private void clearThreadLocal() {
        MoneUserContext.clear();
    }

}
