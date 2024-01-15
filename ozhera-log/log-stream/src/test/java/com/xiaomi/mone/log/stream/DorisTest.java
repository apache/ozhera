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
//package com.xiaomi.mone.log.stream;
//
//import lombok.extern.slf4j.Slf4j;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.sql.*;
//import java.time.Instant;
//
///**
// * @author wtt
// * @version 1.0
// * @description
// * @date 2023/11/7 10:25
// */
//@Slf4j
//public class DorisTest {
//
//    private String driverUrl = "org.mariadb.jdbc.Driver";
//    private String url = "jdbc:mariadb://127.0.0.1:9030/demo";
//    private String user = "root";
//    private String password = "";
//    private String insertSQL = "INSERT INTO log_doris_test (date, message,timestamp) VALUES (?, ?,?)";
//    private Connection connection;
//    private PreparedStatement preparedStatement;
//    private Statement statement;
//
//    @Before
//    public void init() throws SQLException, ClassNotFoundException {
//        Class.forName(driverUrl);
//        connection = DriverManager.getConnection(url, user, password);
//        // 创建一个Statement对象
//        statement = connection.createStatement();
//
//        preparedStatement = connection.prepareStatement(insertSQL);
//    }
//
//    @Test
//    public void addData() throws SQLException {
//        Long date = Instant.now().toEpochMilli();
//        for (int i = 0; i < 1000; i++) {
//            preparedStatement.setInt(1, i);
//            preparedStatement.setString(2, "2023-11-07 02:27:33,464|INFO ||Thread-6|com.xiaomi.data.push.rpc.RpcClient|?|serverIp:127.0.0.1 serverPort:9899");
//            preparedStatement.setLong(3, date);
//            int rowsInserted = preparedStatement.executeUpdate();
//            System.out.println("result:" + rowsInserted);
//        }
//    }
//
//    @Test
//    public void queryData() throws SQLException {
//        // Execute SQL query
//        String query = "SELECT * FROM log_doris_test";
//        ResultSet resultSet = statement.executeQuery(query);
//
//        ResultSetMetaData metaData = resultSet.getMetaData();
//
//        int columnCount = metaData.getColumnCount();
//        // Process query results
//        while (resultSet.next()) {
//            for (int i = 1; i <= columnCount; i++) {
//                // Get column names
//                String columnName = metaData.getColumnName(i);
//
//                // Get column value
//                Object columnValue = resultSet.getObject(i);
//
//                // Here you can operate with column names and column values
//                System.out.println(columnName + ": " + columnValue);
//            }
//            System.out.println("------");
//        }
//    }
//
//    /**
//     * Table creation test
//     *
//     * @throws SQLException
//     */
//    @Test
//    public void createTable() throws SQLException {
//
//        String createTableSQL = "CREATE TABLE example_tbl3\n" +
//                "(\n" +
//                "    `user_id` LARGEINT NOT NULL COMMENT \"user id\",\n" +
//                "    `date` DATE NOT NULL COMMENT \"\",\n" +
//                "    `city` VARCHAR(20) COMMENT \"\",\n" +
//                "    `age` SMALLINT COMMENT \"\",\n" +
//                "    `sex` TINYINT COMMENT \"\",\n" +
//                "    `last_visit_date` DATETIME REPLACE DEFAULT \"1970-01-01 00:00:00\" COMMENT \"\",\n" +
//                "    `cost` BIGINT SUM DEFAULT \"0\" COMMENT \"\",\n" +
//                "    `max_dwell_time` INT MAX DEFAULT \"0\" COMMENT \"\",\n" +
//                "    `min_dwell_time` INT MIN DEFAULT \"99999\" COMMENT \"\"\n" +
//                ")\n" +
//                "AGGREGATE KEY(`user_id`, `date`, `city`, `age`, `sex`)\n" +
//                "DISTRIBUTED BY HASH(`user_id`) BUCKETS 1\n" +
//                "PROPERTIES (\n" +
//                "    \"replication_allocation\" = \"tag.location.default: 1\"\n" +
//                ");";
//
//        statement.execute(createTableSQL);
//        log.info("Table created successfully.");
//    }
//
//    @Test
//    public void deleteTable() throws SQLException {
//        String tableName = "example_tbl2";
//        // construct SQL statements and delete tables
//        String sql = "DROP TABLE " + tableName;
//
//        // execute SQL statement to delete table
//        statement.executeUpdate(sql);
//
//        log.info("Table " + tableName + " deleted successfully.");
//
//    }
//
//    @After
//    public void shutDown() throws SQLException {
//        preparedStatement.close();
//        connection.close();
//    }
//}
