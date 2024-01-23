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
package com.xiaomi.mone.log.manager.service.impl;

import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.google.common.collect.Lists;
import com.xiaomi.mone.log.api.enums.LogStorageTypeEnum;
import com.xiaomi.mone.log.manager.domain.EsCluster;
import com.xiaomi.mone.log.manager.mapper.MilogEsClusterMapper;
import com.xiaomi.mone.log.manager.model.pojo.MilogEsClusterDO;
import com.xiaomi.mone.log.manager.service.EsIndexOperateService;
import com.xiaomi.mone.log.utils.DateUtils;
import com.xiaomi.youpin.docean.anno.Service;
import com.xiaomi.youpin.docean.plugin.es.EsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.cluster.metadata.AliasMetadata;
import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;
import org.elasticsearch.cluster.metadata.Template;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

import static com.xiaomi.mone.log.common.Constant.GSON;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2024/1/19 17:11
 */
@Slf4j
@Service
public class EsIndexOperateServiceImpl implements EsIndexOperateService {

    @Resource
    private MilogEsClusterMapper esClusterMapper;

    @Resource
    private EsCluster esCluster;

    private List<String> containKeys = Lists.newArrayList("mione", "zgq");


    @Override
    public void createIndexPre() {
        /**
         * 1. First query the es information in the table
         * 2. Query query index template
         * 3.Modify the alias of the current index template
         * 4. Query whether the index with sequence exists, create it if it does not exist
         */
        List<MilogEsClusterDO> esClusterDOS = esClusterMapper.selectAll();
        for (MilogEsClusterDO esClusterDO : esClusterDOS) {
            try {
                LogStorageTypeEnum storageTypeEnum = LogStorageTypeEnum.queryByName(esClusterDO.getLogStorageType());
                if (LogStorageTypeEnum.ELASTICSEARCH == storageTypeEnum) {
                    EsService esService = esCluster.getEsService(esClusterDO.getId());
                    if (esService != null) {
                        List<String> indexTemplates = getIndexTemplate(esService);
                        String today = DateUtils.getTime();
                        String tomorrow = DateUtils.getTime(1);
                        for (String indexTemplateName : indexTemplates) {
                            log.info("index template name:{}", indexTemplateName);
                            if (!indexTemplateName.startsWith(".") &&
                                    containKeys.stream().anyMatch(indexTemplateName::contains)) {
                                createOrCheckIndex(esService, indexTemplateName, today);
                                createOrCheckIndex(esService, indexTemplateName, tomorrow);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("createIndexPre,error,param:{}", GSON.toJson(esClusterDO), e);

            }
        }
    }

    /**
     * The problem of not being able to obtain templates when compatible with high and low versions
     *
     * @param esService
     * @return
     * @throws IOException
     */
    private List<String> getIndexTemplate(EsService esService) throws IOException {
        List<String> templateNames = new ArrayList<>();
        try {
            RestHighLevelClient client = esService.getEsClient().getEsOriginalClient();
            for (String containKey : containKeys) {

                GetComposableIndexTemplateRequest templateRequest = new GetComposableIndexTemplateRequest(String.format("*%s*", containKey));
                GetComposableIndexTemplatesResponse indexTemplatesResponse = client.indices()
                        .getIndexTemplate(templateRequest, RequestOptions.DEFAULT);
                Map<String, ComposableIndexTemplate> indexTemplates = indexTemplatesResponse.getIndexTemplates();

                for (Map.Entry<String, ComposableIndexTemplate> mapEntry : indexTemplates.entrySet()) {
                    String name = mapEntry.getKey();
                    ComposableIndexTemplate value = mapEntry.getValue();
                    Template template = value.template();

                    String today = DateUtils.getTime();
                    String newRolloverAlias = String.format("%s-%s", name, today);
                    if (!templateNames.contains(name)) {
                        templateNames.add(name);
                        if (null != template) {
                            Settings settings = template.settings();
                            String rolloverAlias = settings.get("index.lifecycle.rollover_alias");

                            if (!StringUtils.equals(rolloverAlias, newRolloverAlias)) {
                                updateTemplate(template, newRolloverAlias, name, client);
                            }
                        } else {
                            updateTemplate(null, newRolloverAlias, name, client);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("getIndexTemplate (_index_template error,use _template command)", e);
            GetIndexTemplatesRequest indexTemplatesRequest = new GetIndexTemplatesRequest();
            List<IndexTemplateMetadata> indexTemplates = esService.getIndexTemplates(indexTemplatesRequest);

            for (IndexTemplateMetadata template : indexTemplates) {
                if (containKeys.stream().anyMatch(data -> template.name().contains(data)) && !templateNames.contains(template.name())) {
                    templateNames.add(template.name());
                    Settings settings = template.settings();
                    String rolloverAlias = settings.get("index.lifecycle.rollover_alias");
                    if (rolloverAlias != null) {
                        String name = template.name();
                        String today = DateUtils.getTime();
                        String newRolloverAlias = String.format("%s-%s", name, today);
                        if (!StringUtils.equals(rolloverAlias, newRolloverAlias)) {
                            updateTemplate(esService, template);
                        }
                    } else {
                        updateTemplate(esService, template);
                    }
                }
            }
        }
        return templateNames;
    }

    private static void updateTemplate(Template template, String newRolloverAlias, String name, RestHighLevelClient client) throws IOException {
        // Get existing settings
        Settings.Builder updatedSettings = Settings.builder().put(template.settings());

        // Add or update index.lifecycle.rollover_alias
        updatedSettings.put("index.lifecycle.rollover_alias", newRolloverAlias);

        // Update rollover alias
        IndexTemplateMetadata.Builder updatedTemplateBuilder = IndexTemplateMetadata.builder(name);
        updatedTemplateBuilder.settings(Settings.builder()
                .put(template.settings())
                .put("index.lifecycle.rollover_alias", newRolloverAlias));

        Template newTemplate = new Template(updatedSettings.build(), template.mappings(), template.aliases());

        PutComposableIndexTemplateRequest request = new PutComposableIndexTemplateRequest()
                .name(name);
        ComposableIndexTemplate composableIndexTemplate =
                new ComposableIndexTemplate(Arrays.asList(name + "-*"), newTemplate, null, null, null, null);
        request.indexTemplate(composableIndexTemplate);

        // Send request to update template
        client.indices().putIndexTemplate(request, RequestOptions.DEFAULT);
        log.info("update ComposableIndexTemplate:{} success,", name);
    }


    private void updateTemplate(EsService esService, IndexTemplateMetadata template) throws IOException {
        String name = template.name();
        String today = DateUtils.getTime();
        String newRolloverAlias = String.format("%s-%s", name, today);
        // Get existing settings
        Settings.Builder updatedSettings = Settings.builder().put(template.settings());

        // Add or update index.lifecycle.rollover_alias
        updatedSettings.put("index.lifecycle.rollover_alias", newRolloverAlias);

        // create a new put index template request instance
        PutIndexTemplateRequest putRequest = new PutIndexTemplateRequest(template.name());

        // copy the contents of Index Template Metadata to Put Index Template Request
        putRequest.patterns(template.patterns());
        putRequest.order(template.order());
        putRequest.settings(updatedSettings.build());
        putRequest.mapping(template.mappings().getSourceAsMap());
        putRequest.version(template.version());

        // set aliases
        Iterator<ObjectObjectCursor<String, AliasMetadata>> iterator = template.aliases().iterator();
        while (iterator.hasNext()) {
            ObjectObjectCursor<String, AliasMetadata> aliasEntry = iterator.next();
            AliasMetadata aliasMetadata = aliasEntry.value;
            Alias alias = new Alias(aliasEntry.key);
            alias.routing(aliasMetadata.getIndexRouting());
            alias.isHidden(aliasMetadata.isHidden());
            if (aliasMetadata.writeIndex() != null) {
                alias.writeIndex(aliasMetadata.writeIndex());
            }
            alias.searchRouting(aliasMetadata.searchRouting());
            putRequest.alias(alias);
        }

        esService.getEsClient().getEsOriginalClient().indices().putTemplate(putRequest, RequestOptions.DEFAULT);
    }

    private void createOrCheckIndex(EsService esService, String templateName, String dateSuffix) throws IOException {
        String indexCreateName = String.format("%s-%s-000001", templateName, dateSuffix);
        GetIndexRequest todayIndexRequest = new GetIndexRequest(indexCreateName);
        try {
            boolean dateIndexExists = esService.getEsClient().getEsOriginalClient().indices().exists(todayIndexRequest, RequestOptions.DEFAULT);

            if (!dateIndexExists) {
                CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexCreateName);
                Alias alias = new Alias(String.format("%s-%s", templateName, dateSuffix));
                alias.writeIndex(true);
                createIndexRequest.alias(alias);

                createIndexRequest.source("{\n" +
                        "  \"settings\": {\n" +
                        "    \"index.lifecycle.rollover_alias\": \"" + String.format("%s-%s", templateName, dateSuffix) + "\"\n" +
                        "  }\n" +
                        "}", XContentType.JSON);

                esService.createIndex(createIndexRequest);
                log.error("index:{} create success", indexCreateName);
            }
        } catch (Exception e) {
            log.error("createOrCheckIndex error, index:{}", indexCreateName, e);
        }
    }

}
