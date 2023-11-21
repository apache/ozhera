package com.xiaomi.mone.log.manager.service.extension.store;

import com.google.common.collect.Lists;
import com.xiaomi.mone.log.manager.model.dto.LogStorageData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/10 14:26
 */
public interface LogStorageService {

    String DORIS_TABLE_PREFIX = "hera_log_doris_table";

    boolean createTable(LogStorageData logStorageData);

    boolean updateTable(LogStorageData logStorageData);

    boolean deleteTable(LogStorageData logStorageData);


    default String buildTableName(Long clusterId, Long storeId) {
        return String.format("%s_%s_%s", DORIS_TABLE_PREFIX, clusterId, storeId);
    }

    String BIGINT = "BIGINT";
    String VARCHAR = "VARCHAR(1024)";
    String VARCHAR_LARGE = "VARCHAR(65533)";
    List<String> Large_Field_Properties = Lists.newArrayList("message", "logsource");


    default Map<String, String> buildFieldMap(String keys, String columnTypes) {

        List<String> cleanKeyList = Arrays.stream(keys.split(","))
                .map(s -> s.split(":")[0])
                .collect(Collectors.toList());

        List<String> columnTypeList = Arrays.asList(columnTypes.split(","));

        return IntStream.range(0, cleanKeyList.size())
                .boxed()
                .collect(Collectors.toMap(
                        i -> cleanKeyList.get(i),
                        i -> mapFieldType(columnTypeList.get(i).toLowerCase(), cleanKeyList.get(i))
                ));
    }

    default String mapFieldType(String fieldType, String field) {
        if (Large_Field_Properties.contains(field)) {
            return VARCHAR_LARGE;
        }
        switch (fieldType) {
            case "date":
            case "long":
                return BIGINT;
            case "keyword":
            case "text":
            case "ip":
                return VARCHAR;
            default:
                return fieldType;
        }
    }

}
