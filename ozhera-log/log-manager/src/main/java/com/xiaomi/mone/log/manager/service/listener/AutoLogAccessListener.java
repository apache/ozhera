package com.xiaomi.mone.log.manager.service.listener;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.google.gson.reflect.TypeToken;
import com.xiaomi.mone.app.api.response.AppBaseInfo;
import com.xiaomi.mone.log.api.enums.OperateEnum;
import com.xiaomi.mone.log.api.enums.ProjectTypeEnum;
import com.xiaomi.mone.log.manager.dao.MilogLogTailDao;
import com.xiaomi.mone.log.manager.dao.MilogLogstoreDao;
import com.xiaomi.mone.log.manager.dao.MilogSpaceDao;
import com.xiaomi.mone.log.manager.model.bo.AutoAccessLogParam;
import com.xiaomi.mone.log.manager.model.bo.LogTailParam;
import com.xiaomi.mone.log.manager.model.pojo.MilogLogStoreDO;
import com.xiaomi.mone.log.manager.model.pojo.MilogLogTailDo;
import com.xiaomi.mone.log.manager.service.HeraAppService;
import com.xiaomi.mone.log.manager.service.extension.tail.TailExtensionService;
import com.xiaomi.mone.log.manager.service.extension.tail.TailExtensionServiceFactory;
import com.xiaomi.mone.log.manager.service.impl.LogTailServiceImpl;
import com.xiaomi.mone.log.parse.LogParserFactory;
import com.xiaomi.youpin.docean.anno.Component;
import com.xiaomi.youpin.docean.plugin.config.anno.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Resource;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.xiaomi.mone.log.common.Constant.*;

/**
 *
 * @description
 * @version 1.0
 * @author wtt
 * @date 2024/7/10 10:45
 *
 */
@Component
@Slf4j
public class AutoLogAccessListener {
    @Resource
    private MilogSpaceDao spaceDao;

    @Resource
    private MilogLogstoreDao logStoreDao;

    @Resource
    private MilogLogTailDao logTailDao;

    @Resource
    private LogTailServiceImpl logTailService;

    private TailExtensionService tailExtensionService;

    @Resource
    private HeraAppService heraAppService;

    @Value("$defaultNacosAddres")
    private String nacosAddress;

    private final String ACCESS_LOG_DATA_ID_KEY = "auto_access_log_key";

    private ConfigService configService;

    public void init() throws NacosException {
        freshAccessLogList();
    }

    private void freshAccessLogList() throws NacosException {
        if (null == configService) {
            tailExtensionService = TailExtensionServiceFactory.getTailExtensionService();
            this.configService = initConfigService();
            startListening();
        }
    }

    private ConfigService initConfigService() throws NacosException {
        Properties properties = new Properties();
        properties.setProperty("serverAddr", nacosAddress);
        return NacosFactory.createConfigService(properties);
    }

    private void startListening() throws NacosException {
        configService.addListener(ACCESS_LOG_DATA_ID_KEY, DEFAULT_GROUP_ID, new Listener() {
            @Override
            public void receiveConfigInfo(String configInfo) {
                autoLogAccess(configInfo);
            }

            @Override
            public Executor getExecutor() {
                return null;
            }
        });
    }

    private void autoLogAccess(String dataContent) {
        if (StringUtils.isNotEmpty(dataContent)) {
            List<AutoAccessLogParam> accessLogParamList = GSON.fromJson(dataContent, new TypeToken<List<AutoAccessLogParam>>() {
            }.getType());
            if (CollectionUtils.isNotEmpty(accessLogParamList)) {
                accessLogParamList.forEach(this::processLogParam);
            }
        }
    }

    private void processLogParam(AutoAccessLogParam logParam) {
        if (!isValidLogParam(logParam)) {
            log.warn("AutoLogAccess, data param valid error, data:{}", GSON.toJson(logParam));
            return;
        }

        if (!isValidSpace(logParam.getSpaceId())) {
            log.warn("AutoLogAccess, space not exist:{}", logParam.getSpaceId());
            return;
        }

        MilogLogStoreDO logStoreDO = logStoreDao.queryById(logParam.getStoreId());
        if (logStoreDO == null) {
            log.warn("AutoLogAccess, store not exist:{}", logParam.getStoreId());
            return;
        }

        AppBaseInfo appBaseInfo = heraAppService.queryByAppId(logParam.getAppId(), ProjectTypeEnum.MIONE_TYPE.getCode());
        if (appBaseInfo == null) {
            log.warn("AppBaseInfo not exist, appId:{}, appName:{}", logParam.getAppId(), logParam.getAppName());
            appBaseInfo = buildAppBaseInfo(logParam);
        }

        String tailName = generateTailName(logParam.getAppName(), logParam.getEnvName(), logParam.getEnvId());
        if (!logTailExists(logParam, Long.valueOf(appBaseInfo.getId()), tailName)) {
            addLogTail(logParam, logStoreDO, appBaseInfo, tailName);
        } else {
            log.info("AutoLogAccess, tail already exist, tailName:{}", tailName);
        }
    }

    @NotNull
    private static AppBaseInfo buildAppBaseInfo(AutoAccessLogParam logParam) {
        AppBaseInfo appBaseInfo;
        appBaseInfo = new AppBaseInfo();
        appBaseInfo.setId((int) (logParam.getAppId() * 100));
        appBaseInfo.setAppName(logParam.getAppName());
        appBaseInfo.setBindId(logParam.getAppId().toString());
        return appBaseInfo;
    }

    private boolean isValidLogParam(AutoAccessLogParam logParam) {
        return logParam.getSpaceId() != null && logParam.getAppId() != null &&
                logParam.getStoreId() != null && logParam.getLogPath() != null &&
                logParam.getEnvId() != null && CollectionUtils.isNotEmpty(logParam.getIps());
    }

    private boolean isValidSpace(Long spaceId) {
        return spaceDao.queryById(spaceId) != null;
    }

    private boolean logTailExists(AutoAccessLogParam logParam, Long heraAppId, String tailName) {
        List<MilogLogTailDo> logTailDos = logTailDao.queryByCondition(logParam.getSpaceId(), logParam.getStoreId(), tailName,
                heraAppId, logParam.getEnvId(), logParam.getLogPath());
        return CollectionUtils.isNotEmpty(logTailDos);
    }

    private void addLogTail(AutoAccessLogParam logParam, MilogLogStoreDO logStoreDO, AppBaseInfo appBaseInfo, String tailName) {
        LogTailParam logTailParam = buildLogTailParam(logParam.getSpaceId(), logStoreDO, logParam, appBaseInfo, tailName, logParam.getIps());
        MilogLogTailDo logTailDo = logTailService.buildLogTailDo(logTailParam, logStoreDO, appBaseInfo, DEFAULT_OPERATOR);
        logTailDao.add(logTailDo);

        String topicName = buildTopicName(logParam, logTailDo.getTail());
        tailExtensionService.defaultBindingAppTailConfigRel(logTailDo.getId(), logTailDo.getMilogAppId(), logStoreDO.getMqResourceId(), topicName, null);
        logTailService.sengMessageToStream(logTailDo, OperateEnum.ADD_OPERATE.getCode());

        CompletableFuture.runAsync(() -> logTailService.sengMessageToAgent(Long.valueOf(appBaseInfo.getId()), logTailDo));
    }

    private String buildTopicName(AutoAccessLogParam logParam, String tailName) {
        return String.format("%s-%s", logParam.getAppId(), tailName);
    }

    private String generateTailName(String appName, String envName, Long envId) {
        return String.format("%s-%s-%s", appName, envName, envId);
    }

    private LogTailParam buildLogTailParam(Long spaceId, MilogLogStoreDO logStoreDO, AutoAccessLogParam logParam,
                                           AppBaseInfo appBaseInfo, String tailName, List<String> ips) {
        return LogTailParam.builder()
                .spaceId(spaceId)
                .storeId(logStoreDO.getId())
                .appId(logParam.getAppId())
                .milogAppId(Long.valueOf(appBaseInfo.getId()))
                .envId(logParam.getEnvId())
                .envName(logParam.getEnvName())
                .tail(tailName)
                .parseType(LogParserFactory.LogParserEnum.SEPARATOR_PARSE.getCode())
                .parseScript(DEFAULT_TAIL_SEPARATOR)
                .logPath(logParam.getLogPath())
                .ips(ips)
                .valueList(DEFAULT_VALUE_LIST)
                .appType(ProjectTypeEnum.MIONE_TYPE.getCode())
                .deployWay(ProjectTypeEnum.MIONE_TYPE.getCode())
                .build();
    }
}
