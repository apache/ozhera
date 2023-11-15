package com.xiaomi.mone.log.manager.service.extension.store;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xiaomi.mone.log.common.Constant;
import com.xiaomi.mone.log.manager.common.exception.MilogManageException;
import com.xiaomi.mone.log.manager.mapper.MilogEsClusterMapper;
import com.xiaomi.mone.log.manager.mapper.MilogEsIndexMapper;
import com.xiaomi.mone.log.manager.model.dto.LogStorageData;
import com.xiaomi.mone.log.manager.model.pojo.MilogEsClusterDO;
import com.xiaomi.mone.log.manager.model.pojo.MilogEsIndexDO;
import com.xiaomi.youpin.docean.Ioc;
import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/11/10 14:29
 */
@Service
@Slf4j
public class DorisLogStorageService implements LogStorageService {

    private static final String ALTER_TABLE_PREFIX = "ALTER TABLE %s ";
    private static final String DROP_COLUMN_FORMAT = "DROP COLUMN %s";
    private static final String ADD_COLUMN_FORMAT = "ADD COLUMN %s %s";
    private static final String CHANGE_COLUMN_FORMAT = "MODIFY COLUMN %s %s";
    private static final String DELETE_TABLE_FORMAT = "DROP TABLE IF EXISTS %s";


    @Resource
    private MilogEsClusterMapper milogEsClusterMapper;

    @Resource
    private MilogEsIndexMapper esIndexMapper;

    @Override
    public boolean createTable(LogStorageData storageData) {
        MilogEsClusterDO esClusterDO = milogEsClusterMapper.selectById(storageData.getClusterId());
        if (null == esClusterDO) {
            throw new MilogManageException("doris data config not exist");
        }
        DataSource dataSource = Ioc.ins().getBean(Constant.LOG_STORAGE_SERV_BEAN_PRE + storageData.getClusterId());

        String tableName = buildTableName(storageData.getClusterId(), storageData.getStoreId());

        try (Statement statement = dataSource.getConnection().createStatement()) {
            String createTableGrammar = buildCreateTableGrammar(tableName, storageData.getKeys(), storageData.getColumnTypes());
            statement.execute(createTableGrammar);

        } // Automatically closes statement
        catch (SQLException e) {
            throw new MilogManageException("createTable error:" + e.getMessage());
        }
        addLogStorageTable(storageData, tableName);
        return true;
    }

    private void addLogStorageTable(LogStorageData storageData, String tableName) {
        MilogEsIndexDO logStorageData = new MilogEsIndexDO();
        logStorageData.setClusterId(storageData.getClusterId());
        logStorageData.setLogType(storageData.getLogType());
        logStorageData.setIndexName(tableName);
        esIndexMapper.insert(logStorageData);
    }

    private String buildCreateTableGrammar(String tableName, String keys, String columnTypes) {
        String CREATE_TABLE_TEMPLATE = "CREATE TABLE %s (%s) DISTRIBUTED BY HASH(`%s`) BUCKETS 1 PROPERTIES\n" +
                "(\n" +
                "\"replication_num\" = \"1\"\n" +
                ");";

        Map<String, String> fieldMap = buildFieldMap(keys, columnTypes);
        StringJoiner columnDefinitions = new StringJoiner(", ");
        fieldMap.forEach((key, value) ->
                columnDefinitions.add(String.format("`%s` %s", key, value))
        );

        return String.format(
                CREATE_TABLE_TEMPLATE,
                tableName,
                columnDefinitions,
                "timestamp"
        );
    }

    @Override
    public boolean updateTable(LogStorageData storageData) {
        String tableName = buildTableName(storageData.getClusterId(), storageData.getStoreId());
        if (noChanges(storageData)) {
            return false;
        }
        DataSource dataSource = Ioc.ins().getBean(Constant.LOG_STORAGE_SERV_BEAN_PRE + storageData.getClusterId());
        try {
            Connection connection = dataSource.getConnection();
            deleteColumns(connection, tableName, storageData.getKeys(), storageData.getUpdateKeys());
            addColumns(connection, tableName, storageData.getKeys(), storageData.getUpdateKeys(), storageData.getUpdateColumnTypes());
            changeColumns(connection, tableName, storageData.getKeys(), storageData.getColumnTypes(), storageData.getUpdateKeys(), storageData.getUpdateColumnTypes());
        } catch (Exception e) {
            throw new MilogManageException("updateTable error:" + e.getMessage());
        }
        updateLogStorageTable(storageData, tableName);
        return true;
    }

    private void updateLogStorageTable(LogStorageData storageData, String tableName) {
        LambdaQueryWrapper<MilogEsIndexDO> lambdaQueryWrapper = Wrappers.<MilogEsIndexDO>lambdaQuery()
                .eq(MilogEsIndexDO::getClusterId, storageData.getClusterId())
                .eq(MilogEsIndexDO::getIndexName, tableName);
        List<MilogEsIndexDO> logEsIndexDOS = esIndexMapper.selectList(lambdaQueryWrapper);
        for (MilogEsIndexDO logEsIndexDO : logEsIndexDOS) {
            if (!Objects.equals(storageData.getLogType(), logEsIndexDO.getLogType())) {
                logEsIndexDO.setLogType(storageData.getLogType());
                esIndexMapper.updateById(logEsIndexDO);
            }
        }
    }

    private boolean noChanges(LogStorageData storageData) {
        if (StringUtils.isEmpty(storageData.getUpdateKeys()) || StringUtils.isEmpty(storageData.getUpdateColumnTypes())) {
            return true;
        }
        return StringUtils.equals(storageData.getKeys(), storageData.getUpdateKeys()) &&
                StringUtils.equals(storageData.getColumnTypes(), storageData.getUpdateColumnTypes());
    }

    private void deleteColumns(Connection connection, String tableName, String currentKeys, String updateKeys) throws
            Exception {
        List<String> cleanKeyList = getCleanKeyList(currentKeys);
        List<String> updateCleanKeyList = getCleanKeyList(updateKeys);
        List<String> deleteKeys = cleanKeyList.stream().filter(key -> !updateCleanKeyList.contains(key)).collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(deleteKeys)) {
            String delColumns = deleteKeys.stream().map(key -> String.format(DROP_COLUMN_FORMAT, key))
                    .collect(Collectors.joining(","));
            executeAlterTable(connection, tableName, delColumns);
        }
    }

    private void addColumns(Connection connection, String tableName, String keys, String updateKeys, String updateColumnTypes) throws Exception {
        List<String> cleanKeyList = getCleanKeyList(updateKeys);
        List<String> addKeys = cleanKeyList.stream().filter(key -> !getCleanKeyList(keys).contains(key))
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(addKeys)) {
            Map<String, String> fieldMap = buildFieldMap(updateKeys, updateColumnTypes);
            String addColumns = addKeys.stream()
                    .map(key -> String.format(ADD_COLUMN_FORMAT, key, fieldMap.get(key)))
                    .collect(Collectors.joining(","));
            executeAlterTable(connection, tableName, addColumns);
        }
    }

    private void changeColumns(Connection connection, String tableName, String currentKeys, String currentColumnTypes, String updateKeys, String updateColumnTypes) throws Exception {
        Map<String, String> originFieldMap = buildFieldMap(currentKeys, currentColumnTypes);
        Map<String, String> currentFieldMap = buildFieldMap(updateKeys, updateColumnTypes);

        List<String> updateFields = currentFieldMap.entrySet().stream()
                .filter(entry -> !StringUtils.equals(entry.getValue(), originFieldMap.get(entry.getKey())))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(updateFields)) {
            String changeColumns = updateFields.stream()
                    .map(key -> String.format(CHANGE_COLUMN_FORMAT, key, currentFieldMap.get(key)))
                    .collect(Collectors.joining(","));
            executeAlterTable(connection, tableName, changeColumns);
        }
    }

    private void executeAlterTable(Connection connection, String tableName, String columnStatements) throws
            Exception {
        String sql = String.format(ALTER_TABLE_PREFIX + "%s;", tableName, columnStatements);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } finally {
            log.error("executeAlterTable error,sql:{}", sql);
        }
    }

    private List<String> getCleanKeyList(String keys) {
        return Arrays.stream(keys.split(","))
                .map(s -> s.split(":")[0])
                .collect(Collectors.toList());
    }


    @Override
    public boolean deleteTable(LogStorageData storageData) {
        String tableName = buildTableName(storageData.getClusterId(), storageData.getStoreId());
        DataSource dataSource = Ioc.ins().getBean(Constant.LOG_STORAGE_SERV_BEAN_PRE + storageData.getClusterId());

        try (Statement statement = dataSource.getConnection().createStatement()) {

            String createTableGrammar = String.format(DELETE_TABLE_FORMAT, tableName);
            statement.execute(createTableGrammar);

        } // Automatically closes statement
        catch (SQLException e) {
            throw new MilogManageException("deleteTable error:" + e.getMessage());
        }
        deleteLogStorageTable(storageData, tableName);
        return true;
    }

    private void deleteLogStorageTable(LogStorageData storageData, String tableName) {
        LambdaQueryWrapper<MilogEsIndexDO> lambdaQueryWrapper = Wrappers.<MilogEsIndexDO>lambdaQuery()
                .eq(MilogEsIndexDO::getLogType, storageData.getLogType())
                .eq(MilogEsIndexDO::getClusterId, storageData.getClusterId())
                .eq(MilogEsIndexDO::getIndexName, tableName);
        esIndexMapper.delete(lambdaQueryWrapper);
    }
}
