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

-- trace-etl

-- ----------------------------
-- Table structure for hera_trace_etl_config
-- ----------------------------
CREATE TABLE "hera_trace_etl_config"
(
    "id"                        SERIAL PRIMARY KEY,
    "base_info_id"              INTEGER DEFAULT NULL,
    "exclude_method"            VARCHAR(255) DEFAULT NULL,
    "exclude_httpserver_method" VARCHAR(255) DEFAULT NULL,
    "exclude_thread"            VARCHAR(255) DEFAULT NULL,
    "exclude_sql"               VARCHAR(255) DEFAULT NULL,
    "exclude_http_url"          VARCHAR(255) DEFAULT NULL,
    "exclude_ua"                VARCHAR(255) DEFAULT NULL,
    "http_slow_threshold"       INTEGER DEFAULT NULL,
    "dubbo_slow_threshold"      INTEGER DEFAULT NULL,
    "mysql_slow_threshold"      INTEGER DEFAULT NULL,
    "trace_filter"              INTEGER DEFAULT NULL,
    "trace_duration_threshold"  INTEGER DEFAULT NULL,
    "trace_debug_flag"          VARCHAR(255) DEFAULT NULL,
    "http_status_error"         VARCHAR(255) DEFAULT NULL,
    "exception_error"           VARCHAR(512) DEFAULT NULL,
    "grpc_code_error"           VARCHAR(255) DEFAULT NULL,
    "status"                    VARCHAR(2) DEFAULT '1',
    "create_time"               TIMESTAMP DEFAULT NULL,
    "update_time"               TIMESTAMP DEFAULT NULL,
    "create_user"               VARCHAR(32) DEFAULT NULL,
    "update_user"               VARCHAR(32) DEFAULT NULL,
    "bind_id"                   VARCHAR(50) DEFAULT NULL,
    "app_name"                  VARCHAR(255) DEFAULT NULL
);
COMMENT ON COLUMN "hera_trace_etl_config"."base_info_id" IS 'hera_base_info id';
COMMENT ON COLUMN "hera_trace_etl_config"."exclude_method" IS 'Application operation filtering';
COMMENT ON COLUMN "hera_trace_etl_config"."exclude_httpserver_method" IS 'Application operations filtered by the httpServer end.';
COMMENT ON COLUMN "hera_trace_etl_config"."exclude_thread" IS 'Thread name filtering';
COMMENT ON COLUMN "hera_trace_etl_config"."exclude_sql" IS 'SQL filtering';
COMMENT ON COLUMN "hera_trace_etl_config"."exclude_http_url" IS 'URL filtering';
COMMENT ON COLUMN "hera_trace_etl_config"."exclude_ua" IS 'User-agent filtering';
COMMENT ON COLUMN "hera_trace_etl_config"."http_slow_threshold" IS 'http slow query threshold';
COMMENT ON COLUMN "hera_trace_etl_config"."dubbo_slow_threshold" IS 'dubbo slow query threshold';
COMMENT ON COLUMN "hera_trace_etl_config"."mysql_slow_threshold" IS 'mysql slow query threshold';
COMMENT ON COLUMN "hera_trace_etl_config"."trace_filter" IS 'The percentage of trace to be stored in ES.';
COMMENT ON COLUMN "hera_trace_etl_config"."trace_duration_threshold" IS 'The time threshold for storing trace in Elasticsearch.';
COMMENT ON COLUMN "hera_trace_etl_config"."trace_debug_flag" IS 'The debug flag of trace is stored in ES, corresponding to the key of heraContext.';
COMMENT ON COLUMN "hera_trace_etl_config"."http_status_error" IS 'Which HTTP status codes are not displayed in the exception list?';
COMMENT ON COLUMN "hera_trace_etl_config"."exception_error" IS 'Which exceptions are not considered abnormal requests?';
COMMENT ON COLUMN "hera_trace_etl_config"."grpc_code_error" IS 'Which grpc_code is not considered an abnormal request?';
COMMENT ON COLUMN "hera_trace_etl_config"."status" IS 'Is valid 0 invalid, 1 valid.';
COMMENT ON COLUMN "hera_trace_etl_config"."create_user" IS 'Creator';
COMMENT ON COLUMN "hera_trace_etl_config"."update_user" IS 'Editor';
COMMENT ON COLUMN "hera_trace_etl_config"."bind_id" IS 'project id';
COMMENT ON COLUMN "hera_trace_etl_config"."app_name" IS 'project name';

-- ----------------------------
-- Table structure for hera_meta_data
-- ----------------------------
CREATE TABLE "hera_meta_data"
(
    "id"            SERIAL PRIMARY KEY,
    "type"          VARCHAR(10) DEFAULT NULL,
    "meta_id"       INTEGER DEFAULT NULL,
    "meta_name"     VARCHAR(255) DEFAULT NULL,
    "env_id"        INTEGER DEFAULT NULL,
    "env_name"      VARCHAR(255) DEFAULT NULL,
    "host"          VARCHAR(255) DEFAULT NULL,
    "port"          JSON DEFAULT NULL,
    "dubbo_service_meta" TEXT DEFAULT NULL,
    "create_time"   TIMESTAMP DEFAULT NULL,
    "update_time"   TIMESTAMP DEFAULT NULL,
    "create_by"     VARCHAR(125) DEFAULT NULL,
    "update_by"     VARCHAR(125) DEFAULT NULL
);
COMMENT ON COLUMN "hera_meta_data"."type" IS 'Data types include APP, MYSQL, REDIS, ES, MQ, etc.';
COMMENT ON COLUMN "hera_meta_data"."meta_id" IS 'Metadata id, such as appId.';
COMMENT ON COLUMN "hera_meta_data"."meta_name" IS 'The name of the metadata, such as appName.';
COMMENT ON COLUMN "hera_meta_data"."env_id" IS 'Environment ID';
COMMENT ON COLUMN "hera_meta_data"."env_name" IS 'Environment name';
COMMENT ON COLUMN "hera_meta_data"."host" IS 'The instance corresponding to the metadata could be an IP, a domain name, or a host name.';
COMMENT ON COLUMN "hera_meta_data"."port" IS 'Port exposed by metadata';
COMMENT ON COLUMN "hera_meta_data"."dubbo_service_meta" IS 'dubbo Service information includes ServiceName, Group, and version.';

-- ----------------------------
-- Table structure for alert_group
-- ----------------------------
CREATE TABLE "alert_group"
(
    "id"          SERIAL PRIMARY KEY,
    "name"        VARCHAR(64) NOT NULL,
    "desc"        VARCHAR(256) DEFAULT NULL,
    "chat_id"     VARCHAR(125) DEFAULT NULL,
    "creater"     VARCHAR(64) DEFAULT NULL,
    "create_time" TIMESTAMP DEFAULT NULL,
    "update_time" TIMESTAMP DEFAULT NULL,
    "rel_id"      BIGINT DEFAULT 0,
    "type"        VARCHAR(32) DEFAULT 'alert',
    "deleted"     INTEGER DEFAULT 0
);
COMMENT ON COLUMN "alert_group"."name" IS 'name';
COMMENT ON COLUMN "alert_group"."desc" IS 'comment';
COMMENT ON COLUMN "alert_group"."chat_id" IS 'feishu ID';
COMMENT ON COLUMN "alert_group"."creater" IS 'creator';
COMMENT ON COLUMN "alert_group"."create_time" IS 'create time';
COMMENT ON COLUMN "alert_group"."update_time" IS 'update time';
COMMENT ON COLUMN "alert_group"."rel_id" IS 'relation ID';
COMMENT ON COLUMN "alert_group"."type" IS 'Alarm type';
COMMENT ON COLUMN "alert_group"."deleted" IS '0 normal, 1 delete';

-- ----------------------------
-- Table structure for alert_group_member
-- ----------------------------
CREATE TABLE "alert_group_member"
(
    "id"             SERIAL PRIMARY KEY,
    "member_id"      BIGINT DEFAULT 0,
    "alert_group_id" BIGINT DEFAULT 0,
    "creater"        VARCHAR(64) DEFAULT NULL,
    "create_time"    TIMESTAMP DEFAULT NULL,
    "update_time"    TIMESTAMP DEFAULT NULL,
    "member"         VARCHAR(64) DEFAULT '',
    "deleted"        INTEGER DEFAULT 0
);
COMMENT ON COLUMN "alert_group_member"."member_id" IS 'member ID';
COMMENT ON COLUMN "alert_group_member"."alert_group_id" IS 'Alarm group ID';
COMMENT ON COLUMN "alert_group_member"."creater" IS 'creator';
COMMENT ON COLUMN "alert_group_member"."create_time" IS 'create time';
COMMENT ON COLUMN "alert_group_member"."update_time" IS 'update time';
COMMENT ON COLUMN "alert_group_member"."member" IS 'user';
COMMENT ON COLUMN "alert_group_member"."deleted" IS '0 normal, 1 delete';

-- ----------------------------
-- Table structure for app_alarm_rule
-- ----------------------------
CREATE TABLE "app_alarm_rule"
(
    "id"          SERIAL PRIMARY KEY,
    "alarm_id"    INTEGER DEFAULT NULL,
    "alert"       VARCHAR(255) NOT NULL,
    "cname"       VARCHAR(255) DEFAULT NULL,
    "metric_type" INTEGER DEFAULT NULL,
    "expr"        TEXT DEFAULT NULL,
    "for_time"    VARCHAR(50) NOT NULL,
    "labels"      TEXT DEFAULT NULL,
    "annotations" VARCHAR(255) DEFAULT NULL,
    "rule_group"  VARCHAR(50) DEFAULT NULL,
    "priority"    VARCHAR(20) DEFAULT NULL,
    "alert_team"  TEXT DEFAULT NULL,
    "env"         VARCHAR(100) DEFAULT NULL,
    "op"          VARCHAR(5) DEFAULT NULL,
    "value"       NUMERIC(255, 2) DEFAULT NULL,
    "data_count"  INTEGER DEFAULT NULL,
    "send_interval" VARCHAR(20) DEFAULT NULL,
    "project_id"  INTEGER DEFAULT NULL,
    "strategy_id" INTEGER DEFAULT 0,
    "iam_id"      INTEGER DEFAULT NULL,
    "template_id" INTEGER DEFAULT NULL,
    "rule_type"   INTEGER DEFAULT NULL,
    "rule_status" INTEGER DEFAULT NULL,
    "remark"      VARCHAR(255) DEFAULT NULL,
    "creater"     VARCHAR(64) DEFAULT NULL,
    "status"      INTEGER DEFAULT NULL,
    "create_time" TIMESTAMP DEFAULT NULL,
    "update_time" TIMESTAMP DEFAULT NULL
);
COMMENT ON COLUMN "app_alarm_rule"."alarm_id" IS 'Alarm ID, corresponding to the alarm ID of the Prometheus alarm interface.';
COMMENT ON COLUMN "app_alarm_rule"."alert" IS 'Police name';
COMMENT ON COLUMN "app_alarm_rule"."cname" IS 'Alias for reporting a crime';
COMMENT ON COLUMN "app_alarm_rule"."metric_type" IS 'Indicator type 0 preset indicator 1 user-defined indicator.';
COMMENT ON COLUMN "app_alarm_rule"."expr" IS 'Expression';
COMMENT ON COLUMN "app_alarm_rule"."for_time" IS 'Duration';
COMMENT ON COLUMN "app_alarm_rule"."labels" IS 'label';
COMMENT ON COLUMN "app_alarm_rule"."annotations" IS 'Alarm description information';
COMMENT ON COLUMN "app_alarm_rule"."rule_group" IS 'rule-group';
COMMENT ON COLUMN "app_alarm_rule"."priority" IS 'Alarm level';
COMMENT ON COLUMN "app_alarm_rule"."alert_team" IS 'Alarm group JSON';
COMMENT ON COLUMN "app_alarm_rule"."env" IS 'env';
COMMENT ON COLUMN "app_alarm_rule"."op" IS 'Operator';
COMMENT ON COLUMN "app_alarm_rule"."value" IS 'Threshold';
COMMENT ON COLUMN "app_alarm_rule"."data_count" IS 'Number of data points recently';
COMMENT ON COLUMN "app_alarm_rule"."send_interval" IS 'Alarm sending interval';
COMMENT ON COLUMN "app_alarm_rule"."project_id" IS 'Project ID';
COMMENT ON COLUMN "app_alarm_rule"."strategy_id" IS 'Strategy ID';
COMMENT ON COLUMN "app_alarm_rule"."iam_id" IS 'iamId';
COMMENT ON COLUMN "app_alarm_rule"."template_id" IS 'Template ID';
COMMENT ON COLUMN "app_alarm_rule"."rule_type" IS 'Rule type: 0 template rule, 1 application configuration rule.';
COMMENT ON COLUMN "app_alarm_rule"."rule_status" IS '0 Active 1 Pause';
COMMENT ON COLUMN "app_alarm_rule"."remark" IS 'Note';
COMMENT ON COLUMN "app_alarm_rule"."creater" IS 'creator';
COMMENT ON COLUMN "app_alarm_rule"."status" IS 'Status 0 valid 1 deleted';

-- ----------------------------
-- Table structure for app_alarm_rule_template
-- ----------------------------
CREATE TABLE "app_alarm_rule_template"
(
    "id"            SERIAL PRIMARY KEY,
    "name"          VARCHAR(255) NOT NULL,
    "type"          INTEGER NOT NULL,
    "remark"        VARCHAR(255) DEFAULT NULL,
    "creater"       VARCHAR(64) DEFAULT NULL,
    "status"        INTEGER DEFAULT NULL,
    "create_time"   TIMESTAMP DEFAULT NULL,
    "update_time"   TIMESTAMP DEFAULT NULL,
    "strategy_type" INTEGER DEFAULT 0
);
COMMENT ON COLUMN "app_alarm_rule_template"."name" IS 'name';
COMMENT ON COLUMN "app_alarm_rule_template"."type" IS 'type 0 system 1 user';
COMMENT ON COLUMN "app_alarm_rule_template"."remark" IS 'remark';
COMMENT ON COLUMN "app_alarm_rule_template"."creater" IS 'creator';
COMMENT ON COLUMN "app_alarm_rule_template"."status" IS 'status：0 Effective 1 Deleted';
COMMENT ON COLUMN "app_alarm_rule_template"."strategy_type" IS 'strategy_type';


-- ----------------------------
-- Table structure for app_alarm_strategy
-- ----------------------------
CREATE TABLE "app_alarm_strategy"
(
    "id"              SERIAL PRIMARY KEY,
    "iamId"           INTEGER DEFAULT 0,
    "appId"           INTEGER NOT NULL,
    "appName"         VARCHAR(100) DEFAULT NULL,
    "strategy_type"   INTEGER DEFAULT NULL,
    "strategy_name"   VARCHAR(100) DEFAULT NULL,
    "desc"            VARCHAR(255) DEFAULT NULL,
    "creater"         VARCHAR(64) DEFAULT NULL,
    "create_time"     TIMESTAMP DEFAULT NULL,
    "update_time"     TIMESTAMP DEFAULT NULL,
    "status"          SMALLINT NOT NULL DEFAULT 0,
    "alert_team"      TEXT DEFAULT NULL,
    "group3"          VARCHAR(32) DEFAULT '',
    "group4"          VARCHAR(32) DEFAULT '',
    "group5"          VARCHAR(32) DEFAULT '',
    "envs"            TEXT DEFAULT NULL,
    "alert_members"   TEXT DEFAULT NULL,
    "at_members"      TEXT DEFAULT NULL,
    "services"        TEXT DEFAULT NULL
);
COMMENT ON COLUMN "app_alarm_strategy"."appName" IS 'appName';
COMMENT ON COLUMN "app_alarm_strategy"."strategy_type" IS 'strategy_type';
COMMENT ON COLUMN "app_alarm_strategy"."strategy_name" IS 'strategy_name';
COMMENT ON COLUMN "app_alarm_strategy"."desc" IS 'desc';
COMMENT ON COLUMN "app_alarm_strategy"."status" IS 'status';
COMMENT ON COLUMN "app_alarm_strategy"."alert_team" IS 'alert_team';
COMMENT ON COLUMN "app_alarm_strategy"."group3" IS 'group3';
COMMENT ON COLUMN "app_alarm_strategy"."group4" IS 'group4';
COMMENT ON COLUMN "app_alarm_strategy"."group5" IS 'group5';
COMMENT ON COLUMN "app_alarm_strategy"."envs" IS 'envs';
COMMENT ON COLUMN "app_alarm_strategy"."alert_members" IS 'alert_members';
COMMENT ON COLUMN "app_alarm_strategy"."at_members" IS 'at_members';

-- ----------------------------
-- Table structure for app_capacity_auto_adjust
-- ----------------------------
CREATE TABLE "app_capacity_auto_adjust"
(
    "id"              SERIAL PRIMARY KEY,
    "app_id"          INTEGER NOT NULL,
    "pipeline_id"     INTEGER NOT NULL,
    "container"       VARCHAR(255) DEFAULT NULL,
    "status"          SMALLINT DEFAULT NULL,
    "min_instance"    INTEGER DEFAULT NULL,
    "max_instance"    INTEGER DEFAULT NULL,
    "auto_capacity"   SMALLINT DEFAULT NULL,
    "depend_on"       SMALLINT DEFAULT NULL,
    "create_time"     TIMESTAMP DEFAULT NULL,
    "update_time"     TIMESTAMP DEFAULT NULL
);
COMMENT ON COLUMN "app_capacity_auto_adjust"."pipeline_id" IS 'pipeline id';
COMMENT ON COLUMN "app_capacity_auto_adjust"."container" IS 'container name';
COMMENT ON COLUMN "app_capacity_auto_adjust"."status" IS '0 Available，1 Not available.';
COMMENT ON COLUMN "app_capacity_auto_adjust"."min_instance" IS 'min instance';
COMMENT ON COLUMN "app_capacity_auto_adjust"."max_instance" IS 'max instance';
COMMENT ON COLUMN "app_capacity_auto_adjust"."auto_capacity" IS 'auto capacity 1 yes 0 no';
COMMENT ON COLUMN "app_capacity_auto_adjust"."depend_on" IS 'depend_on 0 cpu 1 memory 2 both depend_on';
COMMENT ON COLUMN "app_capacity_auto_adjust"."create_time" IS 'create time';
COMMENT ON COLUMN "app_capacity_auto_adjust"."update_time" IS 'update time';
CREATE UNIQUE INDEX "unique-pipleline" ON "app_capacity_auto_adjust" ("app_id", "pipeline_id");

-- ----------------------------
-- Table structure for app_capacity_auto_adjust_record
-- ----------------------------
CREATE TABLE "app_capacity_auto_adjust_record"
(
    "id"              SERIAL PRIMARY KEY,
    "container"       VARCHAR(255) DEFAULT NULL,
    "name_space"      VARCHAR(255) DEFAULT NULL,
    "replicas"        INTEGER DEFAULT NULL,
    "set_replicas"    INTEGER DEFAULT NULL,
    "env_id"          INTEGER DEFAULT NULL,
    "status"          SMALLINT DEFAULT NULL,
    "time"            BIGINT DEFAULT NULL,
    "create_time"     TIMESTAMP DEFAULT NULL,
    "update_time"     TIMESTAMP DEFAULT NULL
);
COMMENT ON COLUMN "app_capacity_auto_adjust_record"."container" IS 'container';
COMMENT ON COLUMN "app_capacity_auto_adjust_record"."name_space" IS 'name_space';
COMMENT ON COLUMN "app_capacity_auto_adjust_record"."replicas" IS 'replicas';
COMMENT ON COLUMN "app_capacity_auto_adjust_record"."set_replicas" IS 'set_replicas';
COMMENT ON COLUMN "app_capacity_auto_adjust_record"."env_id" IS 'env_id';
COMMENT ON COLUMN "app_capacity_auto_adjust_record"."status" IS 'status';
COMMENT ON COLUMN "app_capacity_auto_adjust_record"."time" IS 'time';

-- ----------------------------
-- Table structure for app_grafana_mapping
-- ----------------------------
CREATE TABLE "app_grafana_mapping"
(
    "id"              SERIAL PRIMARY KEY,
    "app_name"        VARCHAR(100) NOT NULL,
    "mione_env"       VARCHAR(20) DEFAULT NULL,
    "grafana_url"     VARCHAR(200) NOT NULL,
    "create_time"     TIMESTAMP DEFAULT NULL,
    "update_time"     TIMESTAMP DEFAULT NULL
);
CREATE INDEX "appNameIndex" ON "app_grafana_mapping" ("app_name");

-- ----------------------------
-- Table structure for app_monitor
-- ----------------------------
CREATE TABLE "app_monitor"
(
    "id"              SERIAL PRIMARY KEY,
    "project_id"      INTEGER DEFAULT NULL,
    "iam_tree_id"     INTEGER DEFAULT NULL,
    "iam_tree_type"   SMALLINT DEFAULT NULL,
    "project_name"    VARCHAR(255) DEFAULT NULL,
    "app_source"      INTEGER DEFAULT 0,
    "owner"           VARCHAR(128) DEFAULT NULL,
    "care_user"       VARCHAR(30) DEFAULT NULL,
    "alarm_level"     INTEGER DEFAULT NULL,
    "total_alarm"     INTEGER DEFAULT NULL,
    "exception_num"   INTEGER DEFAULT NULL,
    "slow_query_num"  INTEGER DEFAULT NULL,
    "status"          INTEGER DEFAULT NULL,
    "base_info_id"    INTEGER DEFAULT NULL,
    "create_time"     TIMESTAMP DEFAULT NULL,
    "update_time"     TIMESTAMP DEFAULT NULL
);
COMMENT ON COLUMN "app_monitor"."project_id" IS 'project_id';
COMMENT ON COLUMN "app_monitor"."iam_tree_id" IS 'iamTreeId';
COMMENT ON COLUMN "app_monitor"."iam_tree_type" IS 'iam type，0:TPC、1:IAM';
COMMENT ON COLUMN "app_monitor"."project_name" IS 'project_name';
COMMENT ON COLUMN "app_monitor"."app_source" IS 'app_source 0-opensource';
COMMENT ON COLUMN "app_monitor"."owner" IS 'owner';
COMMENT ON COLUMN "app_monitor"."care_user" IS 'care_user';
COMMENT ON COLUMN "app_monitor"."alarm_level" IS 'alarm_level';
COMMENT ON COLUMN "app_monitor"."total_alarm" IS 'total_alarm';
COMMENT ON COLUMN "app_monitor"."exception_num" IS 'exception_num';
COMMENT ON COLUMN "app_monitor"."slow_query_num" IS 'slow_query_num';
COMMENT ON COLUMN "app_monitor"."status" IS 'status 0 Effective 1 deleted';
COMMENT ON COLUMN "app_monitor"."base_info_id" IS 'base_info_id';

-- ----------------------------
-- Table structure for app_quality_market
-- ----------------------------
CREATE TABLE "app_quality_market"
(
    "id"              SERIAL PRIMARY KEY,
    "market_name"     VARCHAR(255) NOT NULL DEFAULT '',
    "creator"         VARCHAR(100) DEFAULT '',
    "service_list"    TEXT DEFAULT NULL,
    "last_updater"    VARCHAR(100) DEFAULT '',
    "remark"          VARCHAR(255) DEFAULT '',
    "create_time"     TIMESTAMP DEFAULT NULL,
    "update_time"     TIMESTAMP DEFAULT NULL
);
COMMENT ON COLUMN "app_quality_market"."market_name" IS 'market_name';
COMMENT ON COLUMN "app_quality_market"."creator" IS 'creator';
COMMENT ON COLUMN "app_quality_market"."service_list" IS 'Multiple applications are separated by semicolons.';
COMMENT ON COLUMN "app_quality_market"."last_updater" IS 'last_updater';
COMMENT ON COLUMN "app_quality_market"."remark" IS 'remark';
COMMENT ON COLUMN "app_quality_market"."create_time" IS 'create_time';
COMMENT ON COLUMN "app_quality_market"."update_time" IS 'update_time';
CREATE INDEX "key_market_name" ON "app_quality_market" ("market_name");
CREATE INDEX "key_creator" ON "app_quality_market" ("creator");


-- ----------------------------
-- Table structure for app_scrape_job
-- ----------------------------
CREATE TABLE "app_scrape_job"
(
    "id"          SERIAL PRIMARY KEY,
    "iam_id"      INT NOT NULL,
    "user"        VARCHAR(64) NOT NULL DEFAULT '',
    "job_json"    TEXT DEFAULT NULL,
    "message"     VARCHAR(255) NOT NULL DEFAULT '',
    "data"        VARCHAR(255) DEFAULT '',
    "job_name"    VARCHAR(64) DEFAULT NULL,
    "status"      INT NOT NULL DEFAULT 0,
    "job_desc"    VARCHAR(255) DEFAULT '',
    "create_time" TIMESTAMP NOT NULL,
    "update_time" TIMESTAMP DEFAULT NULL
);

COMMENT ON COLUMN "app_scrape_job"."id" IS 'id';
COMMENT ON COLUMN "app_scrape_job"."iam_id" IS 'iam id';
COMMENT ON COLUMN "app_scrape_job"."user" IS 'user';
COMMENT ON COLUMN "app_scrape_job"."job_json" IS 'job_json';
COMMENT ON COLUMN "app_scrape_job"."message" IS 'message';
COMMENT ON COLUMN "app_scrape_job"."data" IS 'Success is the fetch ID returned by the request.';
COMMENT ON COLUMN "app_scrape_job"."job_name" IS 'The name of the job being fetched';
COMMENT ON COLUMN "app_scrape_job"."status" IS 'Job status: 0 - creation failed, 1 - creation successful, 2 - deleted.';
COMMENT ON COLUMN "app_scrape_job"."job_desc" IS 'job_desc';
COMMENT ON COLUMN "app_scrape_job"."create_time" IS 'create_time';
COMMENT ON COLUMN "app_scrape_job"."update_time" IS 'update_time';

-- ----------------------------
-- Table structure for app_service_market
-- ----------------------------
CREATE TABLE "app_service_market"
(
    "id"           SERIAL PRIMARY KEY,
    "market_name"  VARCHAR(150) NOT NULL DEFAULT '',
    "belong_team"  VARCHAR(150) NOT NULL DEFAULT '',
    "creator"      VARCHAR(50) DEFAULT '',
    "service_list" TEXT,
    "last_updater" VARCHAR(50) DEFAULT '',
    "remark"       VARCHAR(255) DEFAULT '',
    "service_type" INT NOT NULL DEFAULT 0,
    "create_time"  TIMESTAMP DEFAULT NULL,
    "update_time"  TIMESTAMP DEFAULT NULL
);

COMMENT ON COLUMN "app_service_market"."id" IS 'id';
COMMENT ON COLUMN "app_service_market"."market_name" IS 'market_name';
COMMENT ON COLUMN "app_service_market"."belong_team" IS 'belong_team';
COMMENT ON COLUMN "app_service_market"."creator" IS 'creator';
COMMENT ON COLUMN "app_service_market"."service_list" IS 'Multiple applications are separated by semicolons.';
COMMENT ON COLUMN "app_service_market"."last_updater" IS 'last_updater';
COMMENT ON COLUMN "app_service_market"."remark" IS 'remark';
COMMENT ON COLUMN "app_service_market"."service_type" IS 'service_type';
COMMENT ON COLUMN "app_service_market"."create_time" IS 'create_time';
COMMENT ON COLUMN "app_service_market"."update_time" IS 'update_time';

-- Indexes for app_service_market
CREATE INDEX "idx_market_name" ON "app_service_market" ("market_name");
CREATE INDEX "idx_creator" ON "app_service_market" ("creator");
CREATE INDEX "idx_belong_team" ON "app_service_market" ("belong_team");

-- ----------------------------
-- Table structure for app_tesla_alarm_rule
-- ----------------------------
CREATE TABLE "app_tesla_alarm_rule"
(
    "id"          SERIAL PRIMARY KEY,
    "name"        VARCHAR(100) DEFAULT NULL,
    "tesla_group" VARCHAR(100) NOT NULL,
    "alert_type"  VARCHAR(50) DEFAULT NULL,
    "exper"       TEXT DEFAULT NULL,
    "op"          VARCHAR(2) DEFAULT NULL,
    "value"       FLOAT DEFAULT NULL,
    "duration"    VARCHAR(20) DEFAULT NULL,
    "remark"      VARCHAR(255) DEFAULT NULL,
    "type"        INT DEFAULT NULL,
    "status"      INT DEFAULT NULL,
    "creater"     VARCHAR(64) DEFAULT NULL,
    "create_time" TIMESTAMP DEFAULT NULL,
    "update_time" TIMESTAMP DEFAULT NULL
);

COMMENT ON COLUMN "app_tesla_alarm_rule"."id" IS 'id';
COMMENT ON COLUMN "app_tesla_alarm_rule"."name" IS 'name';
COMMENT ON COLUMN "app_tesla_alarm_rule"."tesla_group" IS 'tesla_group';
COMMENT ON COLUMN "app_tesla_alarm_rule"."alert_type" IS 'alert_type';
COMMENT ON COLUMN "app_tesla_alarm_rule"."exper" IS 'exper';
COMMENT ON COLUMN "app_tesla_alarm_rule"."op" IS 'operation';
COMMENT ON COLUMN "app_tesla_alarm_rule"."value" IS 'Threshold';
COMMENT ON COLUMN "app_tesla_alarm_rule"."duration" IS 'duration';
COMMENT ON COLUMN "app_tesla_alarm_rule"."remark" IS 'remark';
COMMENT ON COLUMN "app_tesla_alarm_rule"."type" IS 'type';
COMMENT ON COLUMN "app_tesla_alarm_rule"."status" IS 'status';
COMMENT ON COLUMN "app_tesla_alarm_rule"."creater" IS 'creator';
COMMENT ON COLUMN "app_tesla_alarm_rule"."create_time" IS 'create_time';
COMMENT ON COLUMN "app_tesla_alarm_rule"."update_time" IS 'update_time';

-- ----------------------------
-- Table structure for app_tesla_feishu_mapping
-- ----------------------------
CREATE TABLE "app_tesla_feishu_mapping"
(
    "id"              SERIAL PRIMARY KEY,
    "tesla_group"     VARCHAR(50) NOT NULL,
    "feishu_group_id" VARCHAR(50) NOT NULL,
    "remark"          VARCHAR(255) DEFAULT NULL,
    "creater"         VARCHAR(64) DEFAULT NULL,
    "status"          INT DEFAULT NULL,
    "create_time"     TIMESTAMP DEFAULT NULL,
    "update_time"     TIMESTAMP DEFAULT NULL
);

COMMENT ON COLUMN "app_tesla_feishu_mapping"."id" IS 'id';
COMMENT ON COLUMN "app_tesla_feishu_mapping"."tesla_group" IS 'tesla_group';
COMMENT ON COLUMN "app_tesla_feishu_mapping"."feishu_group_id" IS 'feishu_group_id';
COMMENT ON COLUMN "app_tesla_feishu_mapping"."remark" IS 'remark';
COMMENT ON COLUMN "app_tesla_feishu_mapping"."creater" IS 'creator';
COMMENT ON COLUMN "app_tesla_feishu_mapping"."status" IS 'status';
COMMENT ON COLUMN "app_tesla_feishu_mapping"."create_time" IS 'create_time';
COMMENT ON COLUMN "app_tesla_feishu_mapping"."update_time" IS 'update_time';

-- ----------------------------
-- Table structure for hera_app_base_info
-- ----------------------------
CREATE TABLE "hera_app_base_info"
(
    "id"            SERIAL PRIMARY KEY,
    "bind_id"       VARCHAR(50) NOT NULL,
    "bind_type"     INT NOT NULL,
    "app_name"      VARCHAR(255) NOT NULL,
    "app_cname"     VARCHAR(255) DEFAULT NULL,
    "app_type"      INT NOT NULL,
    "app_language"  VARCHAR(30) DEFAULT NULL,
    "platform_type" INT NOT NULL,
    "app_sign_id"   VARCHAR(60) DEFAULT NULL,
    "iam_tree_id"   INT DEFAULT NULL,
    "iam_tree_type" INT NOT NULL,
    "envs_map"      TEXT DEFAULT NULL,
    "auto_capacity" INT DEFAULT NULL,
    "status"        INT DEFAULT NULL,
    "create_time"   TIMESTAMP DEFAULT NULL,
    "update_time"   TIMESTAMP DEFAULT NULL
);

COMMENT ON COLUMN "hera_app_base_info"."id" IS 'id';
COMMENT ON COLUMN "hera_app_base_info"."bind_id" IS 'bind id';
COMMENT ON COLUMN "hera_app_base_info"."bind_type" IS 'bind_type(0 appId 1 iamTreeId)';
COMMENT ON COLUMN "hera_app_base_info"."app_name" IS 'app_name';
COMMENT ON COLUMN "hera_app_base_info"."app_cname" IS 'app_cname';
COMMENT ON COLUMN "hera_app_base_info"."app_type" IS 'Application Type - Associated Indicator Monitoring Template（0 Business Application 1 rate limited or exceeded quota）';
COMMENT ON COLUMN "hera_app_base_info"."app_language" IS 'language type';
COMMENT ON COLUMN "hera_app_base_info"."platform_type" IS 'platform type';
COMMENT ON COLUMN "hera_app_base_info"."app_sign_id" IS 'app sign id';
COMMENT ON COLUMN "hera_app_base_info"."iam_tree_id" IS 'iam_tree_id(The alarm interface is necessary.)';
COMMENT ON COLUMN "hera_app_base_info"."iam_tree_type" IS 'iam type，0:TPC、1:IAM';
COMMENT ON COLUMN "hera_app_base_info"."envs_map" IS 'envs list';
COMMENT ON COLUMN "hera_app_base_info"."auto_capacity" IS 'auto capacity 1 yes，0 no';
COMMENT ON COLUMN "hera_app_base_info"."status" IS 'status';
COMMENT ON COLUMN "hera_app_base_info"."create_time" IS 'create_time';
COMMENT ON COLUMN "hera_app_base_info"."update_time" IS 'update_time';

-- Indexes for hera_app_base_info
CREATE UNIQUE INDEX "idx_unique_bind" ON "hera_app_base_info" ("bind_id", "platform_type");
CREATE INDEX "idx_app_name" ON "hera_app_base_info" ("app_name");

-- ----------------------------
-- Table structure for hera_app_excess_info
-- ----------------------------
CREATE TABLE "hera_app_excess_info"
(
    "id"          SERIAL PRIMARY KEY,
    "app_base_id" BIGINT DEFAULT NULL,
    "tree_ids"    JSON DEFAULT NULL,
    "node_ips"    JSON DEFAULT NULL,
    "managers"    JSON DEFAULT NULL,
    "create_time" TIMESTAMP DEFAULT NULL,
    "update_time" TIMESTAMP DEFAULT NULL
);

COMMENT ON COLUMN "hera_app_excess_info"."id" IS 'id';
COMMENT ON COLUMN "hera_app_excess_info"."app_base_id" IS 'app_base_id';
COMMENT ON COLUMN "hera_app_excess_info"."tree_ids" IS 'tree_ids';
COMMENT ON COLUMN "hera_app_excess_info"."node_ips" IS 'node_ips';
COMMENT ON COLUMN "hera_app_excess_info"."managers" IS 'managers';
COMMENT ON COLUMN "hera_app_excess_info"."create_time" IS 'create_time';
COMMENT ON COLUMN "hera_app_excess_info"."update_time" IS 'update_time';

-- Index for hera_app_excess_info
CREATE UNIQUE INDEX "app_base_id_index" ON "hera_app_excess_info" ("app_base_id");


-- ----------------------------
-- Table structure for hera_app_env
-- ----------------------------
CREATE TABLE "hera_app_env" (
                                "id" BIGSERIAL PRIMARY KEY, -- 使用 BIGSERIAL 来替代 SERIAL 并自动生成 id
                                "hera_app_id" bigint NOT NULL,
                                "app_id" bigint NOT NULL,
                                "app_name" varchar(100) DEFAULT NULL,
                                "env_id" bigint DEFAULT NULL,
                                "env_name" varchar(100) DEFAULT NULL,
                                "ip_list" json DEFAULT NULL,
                                "ctime" bigint NOT NULL,
                                "creator" varchar(50) DEFAULT NULL,
                                "utime" bigint DEFAULT NULL,
                                "updater" varchar(50) DEFAULT NULL
);

COMMENT ON COLUMN "hera_app_env"."id" IS 'ID';
COMMENT ON COLUMN "hera_app_env"."hera_app_id" IS 'hera_app_base_info table id';
COMMENT ON COLUMN "hera_app_env"."app_id" IS 'app_id';
COMMENT ON COLUMN "hera_app_env"."app_name" IS 'app_name';
COMMENT ON COLUMN "hera_app_env"."env_id" IS 'env_id (Comes from synchronous information)';
COMMENT ON COLUMN "hera_app_env"."env_name" IS 'env_name';
COMMENT ON COLUMN "hera_app_env"."ip_list" IS 'ip_list (The information stored here is all final)';
COMMENT ON COLUMN "hera_app_env"."ctime" IS 'create time (Millisecond timestamp)';
COMMENT ON COLUMN "hera_app_env"."creator" IS 'creator';
COMMENT ON COLUMN "hera_app_env"."utime" IS 'update time (Millisecond timestamp)';
COMMENT ON COLUMN "hera_app_env"."updater" IS 'updater';

-- ----------------------------
-- Table structure for hera_app_role
-- ----------------------------
CREATE TABLE "hera_app_role" (
                                 "id" SERIAL PRIMARY KEY, -- 使用 SERIAL 来替代 int
                                 "app_id" varchar(50) NOT NULL,
                                 "app_platform" int NOT NULL,
                                 "user" varchar(80) NOT NULL,
                                 "role" int NOT NULL,
                                 "status" int NOT NULL,
                                 "create_time" timestamp DEFAULT NULL,
                                 "update_time" timestamp DEFAULT NULL
);

COMMENT ON COLUMN "hera_app_role"."id" IS 'ID';
COMMENT ON COLUMN "hera_app_role"."app_id" IS 'app_id';
COMMENT ON COLUMN "hera_app_role"."app_platform" IS 'app_platform';
COMMENT ON COLUMN "hera_app_role"."user" IS 'user';
COMMENT ON COLUMN "hera_app_role"."role" IS 'role';
COMMENT ON COLUMN "hera_app_role"."status" IS 'status';
COMMENT ON COLUMN "hera_app_role"."create_time" IS 'create_time';
COMMENT ON COLUMN "hera_app_role"."update_time" IS 'update_time';

-- Indexes for hera_app_role
CREATE INDEX "idx_app_role" ON "hera_app_role" ("app_id", "app_platform");
CREATE INDEX "idx_app_role_user" ON "hera_app_role" ("user");

-- ----------------------------
-- Table structure for hera_oper_log
-- ----------------------------
CREATE TABLE "hera_oper_log" (
                                 "id" BIGSERIAL PRIMARY KEY, -- 使用 BIGSERIAL 来替代 SERIAL
                                 "oper_name" varchar(64) NOT NULL,
                                 "log_type" int DEFAULT 0,
                                 "before_parent_id" bigint DEFAULT 0,
                                 "module_name" varchar(64) DEFAULT '',
                                 "interface_name" varchar(64) DEFAULT '',
                                 "interface_url" varchar(128) DEFAULT '',
                                 "action" varchar(32) DEFAULT '',
                                 "before_data" text DEFAULT NULL,
                                 "after_data" text DEFAULT NULL,
                                 "create_time" timestamp DEFAULT NULL,
                                 "update_time" timestamp DEFAULT NULL,
                                 "data_type" int DEFAULT 0,
                                 "after_parent_id" bigint DEFAULT 0,
                                 "result_desc" varchar(128) DEFAULT ''
);

COMMENT ON COLUMN "hera_oper_log"."id" IS 'ID';
COMMENT ON COLUMN "hera_oper_log"."oper_name" IS 'operator';
COMMENT ON COLUMN "hera_oper_log"."log_type" IS 'log_type (0 Overview, 1 Detail)';
COMMENT ON COLUMN "hera_oper_log"."before_parent_id" IS 'before_parent_id';
COMMENT ON COLUMN "hera_oper_log"."module_name" IS 'module_name';
COMMENT ON COLUMN "hera_oper_log"."interface_name" IS 'interface_name';
COMMENT ON COLUMN "hera_oper_log"."interface_url" IS 'interface_url';
COMMENT ON COLUMN "hera_oper_log"."action" IS 'action';
COMMENT ON COLUMN "hera_oper_log"."before_data" IS 'before_data';
COMMENT ON COLUMN "hera_oper_log"."after_data" IS 'after_data';
COMMENT ON COLUMN "hera_oper_log"."create_time" IS 'create_time';
COMMENT ON COLUMN "hera_oper_log"."update_time" IS 'update_time';
COMMENT ON COLUMN "hera_oper_log"."data_type" IS 'data_type (0 Unknown, 1 Strategy, 2 Rules)';
COMMENT ON COLUMN "hera_oper_log"."after_parent_id" IS 'after_parent_id';
COMMENT ON COLUMN "hera_oper_log"."result_desc" IS 'result_desc';

-- Indexes for hera_oper_log
CREATE INDEX "idx_before_parent_id" ON "hera_oper_log" ("before_parent_id");
CREATE INDEX "idx_oper_name" ON "hera_oper_log" ("oper_name");
CREATE INDEX "idx_after_parent_id" ON "hera_oper_log" ("after_parent_id");

-- ----------------------------
-- Table structure for rules
-- ----------------------------
CREATE TABLE "rules" (
                         "rule_id" SERIAL PRIMARY KEY, -- 使用 SERIAL
                         "rule_name" varchar(255) DEFAULT '',
                         "rule_fn" varchar(255) DEFAULT '',
                         "rule_interval" int DEFAULT NULL,
                         "rule_alert" varchar(255) DEFAULT '',
                         "rule_expr" text DEFAULT NULL,
                         "rule_for" varchar(255) DEFAULT '',
                         "rule_labels" varchar(255) DEFAULT '',
                         "rule_annotations" text DEFAULT NULL,
                         "principal" varchar(255) DEFAULT NULL,
                         "create_time" date DEFAULT NULL,
                         "update_time" date DEFAULT NULL
);

COMMENT ON COLUMN "rules"."rule_id" IS 'rule_id';
COMMENT ON COLUMN "rules"."rule_name" IS 'rule_name';
COMMENT ON COLUMN "rules"."rule_fn" IS 'type';
COMMENT ON COLUMN "rules"."rule_interval" IS 'rule_interval';
COMMENT ON COLUMN "rules"."rule_alert" IS 'rule_alert name';
COMMENT ON COLUMN "rules"."rule_expr" IS 'rule_expr';
COMMENT ON COLUMN "rules"."rule_for" IS 'duration';
COMMENT ON COLUMN "rules"."rule_labels" IS 'Rule dimension information';
COMMENT ON COLUMN "rules"."rule_annotations" IS 'Description of rules';
COMMENT ON COLUMN "rules"."principal" IS 'Comma separated prefixes of the person in charge email.';
COMMENT ON COLUMN "rules"."create_time" IS 'create_time';
COMMENT ON COLUMN "rules"."update_time" IS 'update_time';

-- Unique Index for rules
CREATE UNIQUE INDEX "unique_key" ON "rules" ("rule_alert");

-- ----------------------------
-- Table structure for rule_promql_template
-- ----------------------------
CREATE TABLE "rule_promql_template" (
                                        "id" SERIAL PRIMARY KEY, -- 使用 SERIAL
                                        "name" varchar(255) NOT NULL,
                                        "promql" varchar(512) DEFAULT NULL,
                                        "type" int NOT NULL,
                                        "remark" varchar(255) DEFAULT NULL,
                                        "creater" varchar(64) DEFAULT '',
                                        "status" int DEFAULT NULL,
                                        "create_time" timestamp DEFAULT NULL,
                                        "update_time" timestamp DEFAULT NULL
);

COMMENT ON COLUMN "rule_promql_template"."id" IS 'ID';
COMMENT ON COLUMN "rule_promql_template"."name" IS 'Template Name';
COMMENT ON COLUMN "rule_promql_template"."promql" IS 'promql';
COMMENT ON COLUMN "rule_promql_template"."type" IS 'type (0 system, 1 user)';
COMMENT ON COLUMN "rule_promql_template"."remark" IS 'remark';
COMMENT ON COLUMN "rule_promql_template"."creater" IS 'creator';
COMMENT ON COLUMN "rule_promql_template"."status" IS 'status: 0 Effective';
COMMENT ON COLUMN "rule_promql_template"."create_time" IS 'create_time';
COMMENT ON COLUMN "rule_promql_template"."update_time" IS 'update_time';

-- Index for rule_promql_template
CREATE INDEX "idx_creater" ON "rule_promql_template" ("creater");

-- ----------------------------
-- Table structure for app_monitor_config
-- ----------------------------
CREATE TABLE "app_monitor_config" (
                                      "id" serial PRIMARY KEY,
                                      "project_id" int NOT NULL,
                                      "config_type" int NOT NULL,
                                      "config_name" varchar(50) NOT NULL,
                                      "value" varchar(255) NOT NULL,
                                      "status" int NOT NULL,
                                      "create_time" timestamp DEFAULT NULL,
                                      "update_time" timestamp DEFAULT NULL
);

COMMENT ON COLUMN "app_monitor_config"."id" IS 'ID';
COMMENT ON COLUMN "app_monitor_config"."project_id" IS 'id';
COMMENT ON COLUMN "app_monitor_config"."config_type" IS 'config_type (0 Slow query time)';
COMMENT ON COLUMN "app_monitor_config"."config_name" IS 'config_name';
COMMENT ON COLUMN "app_monitor_config"."value" IS 'value';
COMMENT ON COLUMN "app_monitor_config"."status" IS 'status';
COMMENT ON COLUMN "app_monitor_config"."create_time" IS 'create_time';
COMMENT ON COLUMN "app_monitor_config"."update_time" IS 'update_time';

-- ----------------------------
-- Table structure for mione_grafana_template
-- ----------------------------
CREATE TABLE "mione_grafana_template" (
                                          "id" serial PRIMARY KEY,
                                          "name" varchar(64) NOT NULL DEFAULT '',
                                          "template" text DEFAULT NULL,
                                          "platform" int DEFAULT NULL,
                                          "language" int DEFAULT NULL,
                                          "app_type" int DEFAULT NULL,
                                          "panel_id_list" text DEFAULT NULL,
                                          "url_param" text DEFAULT NULL,
                                          "create_time" timestamp DEFAULT NULL,
                                          "update_time" timestamp DEFAULT NULL,
                                          "deleted" boolean DEFAULT FALSE
);

COMMENT ON COLUMN "mione_grafana_template"."id" IS 'id';
COMMENT ON COLUMN "mione_grafana_template"."name" IS 'Template name';
COMMENT ON COLUMN "mione_grafana_template"."template" IS 'template json';
COMMENT ON COLUMN "mione_grafana_template"."platform" IS 'platform';
COMMENT ON COLUMN "mione_grafana_template"."language" IS 'language';
COMMENT ON COLUMN "mione_grafana_template"."app_type" IS 'app_type';
COMMENT ON COLUMN "mione_grafana_template"."panel_id_list" IS 'panel_id_list';
COMMENT ON COLUMN "mione_grafana_template"."url_param" IS 'url_param';
COMMENT ON COLUMN "mione_grafana_template"."create_time" IS 'create_time';
COMMENT ON COLUMN "mione_grafana_template"."update_time" IS 'update_time';
COMMENT ON COLUMN "mione_grafana_template"."deleted" IS '0 Not deleted, 1 deleted';

-- Unique Index for mione_grafana_template
CREATE UNIQUE INDEX "uq_name" ON "mione_grafana_template" ("name");

-- ----------------------------
-- Table structure for milog_analyse_dashboard
-- ----------------------------
CREATE TABLE "milog_analyse_dashboard" (
                                           "id" serial PRIMARY KEY,
                                           "name" varchar(255) DEFAULT NULL,
                                           "store_id" bigint DEFAULT NULL,
                                           "space_id" bigint DEFAULT NULL,
                                           "creator" varchar(255) DEFAULT NULL,
                                           "updater" varchar(255) DEFAULT NULL,
                                           "create_time" bigint DEFAULT NULL,
                                           "update_time" bigint DEFAULT NULL
);

COMMENT ON COLUMN "milog_analyse_dashboard"."id" IS 'id';
COMMENT ON COLUMN "milog_analyse_dashboard"."name" IS 'name';
COMMENT ON COLUMN "milog_analyse_dashboard"."store_id" IS 'store_id';
COMMENT ON COLUMN "milog_analyse_dashboard"."space_id" IS 'space_id';
COMMENT ON COLUMN "milog_analyse_dashboard"."creator" IS 'creator';
COMMENT ON COLUMN "milog_analyse_dashboard"."updater" IS 'updater';
COMMENT ON COLUMN "milog_analyse_dashboard"."create_time" IS 'create_time';
COMMENT ON COLUMN "milog_analyse_dashboard"."update_time" IS 'update_time';

-- ----------------------------
-- Table structure for milog_analyse_dashboard_graph_ref
-- ----------------------------
CREATE TABLE "milog_analyse_dashboard_graph_ref" (
                                                     "id" serial PRIMARY KEY,
                                                     "dashboard_id" bigint DEFAULT NULL,
                                                     "graph_id" bigint DEFAULT NULL,
                                                     "point" json DEFAULT NULL,
                                                     "private_name" varchar(255) DEFAULT NULL
);

COMMENT ON COLUMN "milog_analyse_dashboard_graph_ref"."id" IS 'id';
COMMENT ON COLUMN "milog_analyse_dashboard_graph_ref"."dashboard_id" IS 'dashboard_id';
COMMENT ON COLUMN "milog_analyse_dashboard_graph_ref"."graph_id" IS 'graph_id';
COMMENT ON COLUMN "milog_analyse_dashboard_graph_ref"."point" IS 'point';
COMMENT ON COLUMN "milog_analyse_dashboard_graph_ref"."private_name" IS 'private_name';

-- ----------------------------
-- Table structure for milog_analyse_graph
-- ----------------------------
CREATE TABLE "milog_analyse_graph" (
                                       "id" serial PRIMARY KEY,
                                       "name" varchar(255) DEFAULT NULL,
                                       "field_name" varchar(255) DEFAULT NULL,
                                       "space_id" bigint DEFAULT NULL,
                                       "store_id" bigint DEFAULT NULL,
                                       "graph_type" int DEFAULT NULL,
                                       "graph_param" varchar(1000) DEFAULT NULL,
                                       "updater" varchar(255) DEFAULT NULL,
                                       "creator" varchar(255) DEFAULT NULL,
                                       "create_time" bigint DEFAULT NULL,
                                       "update_time" bigint DEFAULT NULL
);

COMMENT ON COLUMN "milog_analyse_graph"."id" IS 'id';
COMMENT ON COLUMN "milog_analyse_graph"."name" IS 'name';
COMMENT ON COLUMN "milog_analyse_graph"."field_name" IS 'field_name';
COMMENT ON COLUMN "milog_analyse_graph"."space_id" IS 'space_id';
COMMENT ON COLUMN "milog_analyse_graph"."store_id" IS 'store_id';
COMMENT ON COLUMN "milog_analyse_graph"."graph_type" IS 'graph_type';
COMMENT ON COLUMN "milog_analyse_graph"."graph_param" IS 'graph_param';
COMMENT ON COLUMN "milog_analyse_graph"."updater" IS 'updater';
COMMENT ON COLUMN "milog_analyse_graph"."creator" IS 'creator';
COMMENT ON COLUMN "milog_analyse_graph"."create_time" IS 'create_time';
COMMENT ON COLUMN "milog_analyse_graph"."update_time" IS 'update_time';

-- ----------------------------
-- Table structure for milog_analyse_graph_type
-- ----------------------------
CREATE TABLE "milog_analyse_graph_type" (
                                            "id" serial PRIMARY KEY,
                                            "name" varchar(255) DEFAULT NULL,
                                            "type" int DEFAULT NULL,
                                            "calculate" varchar(255) DEFAULT NULL,
                                            "classify" varchar(50) DEFAULT NULL
);

COMMENT ON COLUMN "milog_analyse_graph_type"."id" IS 'id';
COMMENT ON COLUMN "milog_analyse_graph_type"."name" IS 'name';
COMMENT ON COLUMN "milog_analyse_graph_type"."type" IS 'type';
COMMENT ON COLUMN "milog_analyse_graph_type"."calculate" IS 'calculate';
COMMENT ON COLUMN "milog_analyse_graph_type"."classify" IS 'classify';

-- ----------------------------
-- Table structure for milog_app_middleware_rel
-- ----------------------------
CREATE TABLE "milog_app_middleware_rel" (
                                            "id" bigserial PRIMARY KEY,
                                            "milog_app_id" bigint NOT NULL,
                                            "middleware_id" bigint NOT NULL,
                                            "tail_id" bigint NOT NULL,
                                            "config" json DEFAULT NULL,
                                            "ctime" bigint NOT NULL,
                                            "utime" bigint NOT NULL,
                                            "creator" varchar(50) NOT NULL,
                                            "updater" varchar(50) NOT NULL
);

COMMENT ON COLUMN "milog_app_middleware_rel"."id" IS 'id';
COMMENT ON COLUMN "milog_app_middleware_rel"."milog_app_id" IS 'milog app table id';
COMMENT ON COLUMN "milog_app_middleware_rel"."middleware_id" IS 'middleware ID';
COMMENT ON COLUMN "milog_app_middleware_rel"."tail_id" IS 'tailId';
COMMENT ON COLUMN "milog_app_middleware_rel"."config" IS 'config, json style';
COMMENT ON COLUMN "milog_app_middleware_rel"."ctime" IS 'create time';
COMMENT ON COLUMN "milog_app_middleware_rel"."utime" IS 'update time';
COMMENT ON COLUMN "milog_app_middleware_rel"."creator" IS 'creator';
COMMENT ON COLUMN "milog_app_middleware_rel"."updater" IS 'updater';


-- ----------------------------
-- Table structure for milog_es_cluster
-- ----------------------------
CREATE TABLE "milog_es_cluster" (
                                    "id" bigserial PRIMARY KEY,
                                    "log_storage_type1" varchar(50) DEFAULT 'elasticsearch',
                                    "tag" varchar(255) DEFAULT NULL,
                                    "name" varchar(255) DEFAULT NULL,
                                    "region" varchar(255) DEFAULT NULL,
                                    "cluster_name" varchar(255) DEFAULT NULL,
                                    "addr" varchar(255) DEFAULT NULL,
                                    "user" varchar(255) DEFAULT NULL,
                                    "pwd" varchar(255) DEFAULT NULL,
                                    "token" varchar(255) DEFAULT NULL,
                                    "dt_catalog" varchar(255) DEFAULT NULL,
                                    "dt_database" varchar(255) DEFAULT NULL,
                                    "area" varchar(255) DEFAULT NULL,
                                    "ctime" bigint DEFAULT NULL,
                                    "utime" bigint DEFAULT NULL,
                                    "creator" varchar(50) DEFAULT NULL,
                                    "updater" varchar(50) DEFAULT NULL,
                                    "labels" json DEFAULT NULL,
                                    "con_way" varchar(50) DEFAULT NULL,
                                    "log_storage_type" varchar(50) DEFAULT 'elasticsearch',
                                    "is_default" smallint DEFAULT 0
);

COMMENT ON COLUMN "milog_es_cluster"."id" IS 'id';
COMMENT ON COLUMN "milog_es_cluster"."log_storage_type1" IS 'Log storage type';
COMMENT ON COLUMN "milog_es_cluster"."tag" IS 'cluster type';
COMMENT ON COLUMN "milog_es_cluster"."name" IS 'cluster name';
COMMENT ON COLUMN "milog_es_cluster"."region" IS 'region';
COMMENT ON COLUMN "milog_es_cluster"."cluster_name" IS 'cluster_name';
COMMENT ON COLUMN "milog_es_cluster"."addr" IS 'ES addr';
COMMENT ON COLUMN "milog_es_cluster"."user" IS 'ES user';
COMMENT ON COLUMN "milog_es_cluster"."pwd" IS 'ES pwd';
COMMENT ON COLUMN "milog_es_cluster"."token" IS 'token';
COMMENT ON COLUMN "milog_es_cluster"."dt_catalog" IS 'dt_catalog';
COMMENT ON COLUMN "milog_es_cluster"."dt_database" IS 'dt_database';
COMMENT ON COLUMN "milog_es_cluster"."area" IS 'area';
COMMENT ON COLUMN "milog_es_cluster"."ctime" IS 'ctime';
COMMENT ON COLUMN "milog_es_cluster"."utime" IS 'utime';
COMMENT ON COLUMN "milog_es_cluster"."creator" IS 'creator';
COMMENT ON COLUMN "milog_es_cluster"."updater" IS 'updater';
COMMENT ON COLUMN "milog_es_cluster"."labels" IS 'labels';
COMMENT ON COLUMN "milog_es_cluster"."con_way" IS 'connect way: pwd, token';
COMMENT ON COLUMN "milog_es_cluster"."log_storage_type" IS 'log storage type';
COMMENT ON COLUMN "milog_es_cluster"."is_default" IS 'is_default';

-- ----------------------------
-- Table structure for milog_es_index
-- ----------------------------
CREATE TABLE "milog_es_index" (
                                  "id" bigserial PRIMARY KEY,
                                  "cluster_id" bigint DEFAULT NULL,
                                  "log_type" int DEFAULT NULL,
                                  "index_name" varchar(255) DEFAULT NULL
);

COMMENT ON COLUMN "milog_es_index"."id" IS 'id';
COMMENT ON COLUMN "milog_es_index"."cluster_id" IS 'cluster_id';
COMMENT ON COLUMN "milog_es_index"."log_type" IS 'log_type';
COMMENT ON COLUMN "milog_es_index"."index_name" IS 'es index_name';

-- ----------------------------
-- Table structure for milog_log_count
-- ----------------------------
CREATE TABLE "milog_log_count" (
                                   "id" bigserial PRIMARY KEY,
                                   "tail_id" bigint DEFAULT NULL,
                                   "es_index" varchar(255) DEFAULT NULL,
                                   "day" date DEFAULT NULL,
                                   "number" bigint DEFAULT NULL
);

COMMENT ON COLUMN "milog_log_count"."id" IS 'id';
COMMENT ON COLUMN "milog_log_count"."tail_id" IS 'tail ID';
COMMENT ON COLUMN "milog_log_count"."es_index" IS 'es index name';
COMMENT ON COLUMN "milog_log_count"."day" IS 'log data start yyyy-MM-dd';
COMMENT ON COLUMN "milog_log_count"."number" IS 'number';

-- ----------------------------
-- Table structure for milog_log_num_alert
-- ----------------------------
CREATE TABLE "milog_log_num_alert" (
                                       "id" bigserial PRIMARY KEY,
                                       "day" date DEFAULT NULL,
                                       "number" bigint DEFAULT NULL,
                                       "app_id" bigint DEFAULT NULL,
                                       "app_name" varchar(255) DEFAULT NULL,
                                       "alert_user" varchar(5000) DEFAULT NULL,
                                       "ctime" bigint DEFAULT NULL
);

COMMENT ON COLUMN "milog_log_num_alert"."id" IS 'id';
COMMENT ON COLUMN "milog_log_num_alert"."day" IS 'day';
COMMENT ON COLUMN "milog_log_num_alert"."number" IS 'number';
COMMENT ON COLUMN "milog_log_num_alert"."app_id" IS 'app_id';
COMMENT ON COLUMN "milog_log_num_alert"."app_name" IS 'app_name';
COMMENT ON COLUMN "milog_log_num_alert"."alert_user" IS 'alert_user';
COMMENT ON COLUMN "milog_log_num_alert"."ctime" IS 'ctime';

-- Index for milog_log_num_alert
CREATE INDEX "day_app_id_idx" ON "milog_log_num_alert" ("day", "app_id");

-- ----------------------------
-- Table structure for milog_log_search_save
-- ----------------------------
CREATE TABLE "milog_log_search_save" (
                                         "id" bigserial PRIMARY KEY,
                                         "name" varchar(255) DEFAULT NULL,
                                         "space_id" int DEFAULT NULL,
                                         "store_id" bigint DEFAULT NULL,
                                         "tail_id" varchar(250) DEFAULT NULL,
                                         "query_text" varchar(2000) DEFAULT NULL,
                                         "is_fix_time" int DEFAULT NULL,
                                         "start_time" bigint DEFAULT NULL,
                                         "end_time" bigint DEFAULT NULL,
                                         "common" varchar(255) DEFAULT NULL,
                                         "sort" bigint DEFAULT NULL,
                                         "order_num" bigint DEFAULT NULL,
                                         "creator" varchar(255) DEFAULT NULL,
                                         "updater" varchar(255) DEFAULT NULL,
                                         "create_time" bigint DEFAULT NULL,
                                         "update_time" bigint DEFAULT NULL
);

COMMENT ON COLUMN "milog_log_search_save"."id" IS 'Id';
COMMENT ON COLUMN "milog_log_search_save"."name" IS 'name';
COMMENT ON COLUMN "milog_log_search_save"."space_id" IS 'space_id';
COMMENT ON COLUMN "milog_log_search_save"."store_id" IS 'store_id';
COMMENT ON COLUMN "milog_log_search_save"."tail_id" IS 'tail_id';
COMMENT ON COLUMN "milog_log_search_save"."query_text" IS 'query_text';
COMMENT ON COLUMN "milog_log_search_save"."is_fix_time" IS '1-Saved the time parameter, 0-Not saved';
COMMENT ON COLUMN "milog_log_search_save"."start_time" IS 'start_time';
COMMENT ON COLUMN "milog_log_search_save"."end_time" IS 'end_time';
COMMENT ON COLUMN "milog_log_search_save"."common" IS 'common';
COMMENT ON COLUMN "milog_log_search_save"."sort" IS 'type 1-search, 2-tail, 3-store';
COMMENT ON COLUMN "milog_log_search_save"."order_num" IS 'sort';
COMMENT ON COLUMN "milog_log_search_save"."creator" IS 'creator';
COMMENT ON COLUMN "milog_log_search_save"."updater" IS 'updater';
COMMENT ON COLUMN "milog_log_search_save"."create_time" IS 'create_time';
COMMENT ON COLUMN "milog_log_search_save"."update_time" IS 'update_time';

-- ----------------------------
-- Table structure for milog_log_template
-- ----------------------------
CREATE TABLE "milog_log_template" (
                                      "id" bigserial PRIMARY KEY,
                                      "ctime" bigint DEFAULT NULL,
                                      "utime" bigint DEFAULT NULL,
                                      "template_name" varchar(255) NOT NULL,
                                      "type" int DEFAULT NULL,
                                      "support_area" varchar(255) DEFAULT NULL,
                                      "order_col" int DEFAULT NULL,
                                      "supported_consume" smallint NOT NULL DEFAULT 1
);

COMMENT ON COLUMN "milog_log_template"."id" IS 'id';
COMMENT ON COLUMN "milog_log_template"."ctime" IS 'ctime';
COMMENT ON COLUMN "milog_log_template"."utime" IS 'utime';
COMMENT ON COLUMN "milog_log_template"."template_name" IS 'template_name';
COMMENT ON COLUMN "milog_log_template"."type" IS 'template type 0-Custom log, 1-app, 2-nginx';
COMMENT ON COLUMN "milog_log_template"."support_area" IS 'support_area';
COMMENT ON COLUMN "milog_log_template"."order_col" IS 'sort';
COMMENT ON COLUMN "milog_log_template"."supported_consume" IS 'Whether to support consumption, default support is 1';

-- ----------------------------
-- 表结构：milog_log_template_detail
-- ----------------------------
CREATE TABLE "milog_log_template_detail"
(
    "id"              BIGSERIAL NOT NULL,
    "ctime"           bigint NULL DEFAULT NULL,
    "utime"           bigint NULL DEFAULT NULL,
    "template_id"     varchar(20) NULL DEFAULT NULL,
    "properties_key"  varchar(255) NULL DEFAULT NULL,
    "properties_type" varchar(255) NULL DEFAULT NULL,
    PRIMARY KEY ("id")
);

COMMENT ON COLUMN "milog_log_template_detail"."id" IS 'id';
COMMENT ON COLUMN "milog_log_template_detail"."ctime" IS 'ctime';
COMMENT ON COLUMN "milog_log_template_detail"."utime" IS 'utime';
COMMENT ON COLUMN "milog_log_template_detail"."template_id" IS 'template_id';
COMMENT ON COLUMN "milog_log_template_detail"."properties_key" IS 'properties_key；1-Required,2-Suggestion,3-Hidden';
COMMENT ON COLUMN "milog_log_template_detail"."properties_type" IS 'properties_type';

-- ----------------------------
-- 表结构：milog_logstail
-- ----------------------------
CREATE TABLE "milog_logstail"
(
    "id"                BIGSERIAL NOT NULL,
    "ctime"             bigint NULL DEFAULT NULL,
    "utime"             bigint NULL DEFAULT NULL,
    "creator"           varchar(80) NULL DEFAULT NULL,
    "updater"           varchar(80) NULL DEFAULT NULL,
    "space_id"          bigint NULL DEFAULT NULL,
    "store_id"          bigint NULL DEFAULT NULL,
    "tail"              varchar(255) NULL DEFAULT NULL,
    "milog_app_id"      bigint NULL DEFAULT NULL,
    "app_id"            bigint NULL DEFAULT NULL,
    "app_name"          varchar(128) NULL DEFAULT NULL,
    "app_type"          smallint NULL DEFAULT NULL,
    "machine_type"      smallint NULL DEFAULT NULL,
    "env_id"            int NULL DEFAULT NULL,
    "env_name"          varchar(128) NULL DEFAULT NULL,
    "parse_type"        int NULL DEFAULT NULL,
    "parse_script"      text NULL,
    "log_path"          varchar(1024) NULL DEFAULT NULL,
    "log_split_express" varchar(255) NULL DEFAULT NULL,
    "value_list"        varchar(1024) NULL DEFAULT NULL,
    "ips"               json NULL,
    "motor_rooms"       json NULL,
    "filter"            json NULL,
    "en_es_index"       json NULL,
    "deploy_way"        int NULL DEFAULT NULL,
    "deploy_space"      varchar(255) NULL DEFAULT NULL,
    "first_line_reg"    varchar(255) NULL DEFAULT NULL,
    "collection_ready"  boolean DEFAULT TRUE,
    PRIMARY KEY ("id")
);

COMMENT ON COLUMN "milog_logstail"."id" IS 'id';
COMMENT ON COLUMN "milog_logstail"."ctime" IS 'ctime';
COMMENT ON COLUMN "milog_logstail"."utime" IS 'utime';
COMMENT ON COLUMN "milog_logstail"."creator" IS 'creator';
COMMENT ON COLUMN "milog_logstail"."updater" IS 'updater';
COMMENT ON COLUMN "milog_logstail"."space_id" IS 'spaceId';
COMMENT ON COLUMN "milog_logstail"."store_id" IS 'storeId';
COMMENT ON COLUMN "milog_logstail"."tail" IS 'app alias name';
COMMENT ON COLUMN "milog_logstail"."milog_app_id" IS 'milog table id';
COMMENT ON COLUMN "milog_logstail"."app_id" IS 'app id';
COMMENT ON COLUMN "milog_logstail"."app_name" IS 'app_name';
COMMENT ON COLUMN "milog_logstail"."app_type" IS '0.mione 1.mis';
COMMENT ON COLUMN "milog_logstail"."machine_type" IS 'mis app machine type 0.container 1.physical machine';
COMMENT ON COLUMN "milog_logstail"."env_id" IS 'env_id';
COMMENT ON COLUMN "milog_logstail"."env_name" IS 'env_name';
COMMENT ON COLUMN "milog_logstail"."parse_type" IS 'parse_type: 1:Service application log, 2.Separator, 3: One line, 4: Multiple lines, 5: customize';
COMMENT ON COLUMN "milog_logstail"."parse_script" IS 'For delimiters, this field specifies the delimiter, for custom, this field specifies the log reading script.';
COMMENT ON COLUMN "milog_logstail"."log_path" IS 'Comma-separated, multiple log file paths, e.g. /home/work/log/xxx/server.log';
COMMENT ON COLUMN "milog_logstail"."log_split_express" IS 'Log split expression';
COMMENT ON COLUMN "milog_logstail"."value_list" IS 'Value list, separated by commas.';
COMMENT ON COLUMN "milog_logstail"."ips" IS 'ip list';
COMMENT ON COLUMN "milog_logstail"."motor_rooms" IS 'mis Application server room information';
COMMENT ON COLUMN "milog_logstail"."filter" IS 'filter config';
COMMENT ON COLUMN "milog_logstail"."en_es_index" IS 'mis Application index configuration';
COMMENT ON COLUMN "milog_logstail"."deploy_way" IS 'deploy way: 1-mione, 2-miline, 3-k8s';
COMMENT ON COLUMN "milog_logstail"."deploy_space" IS 'matrix service deployment space';
COMMENT ON COLUMN "milog_logstail"."first_line_reg" IS 'Regular expression at the beginning of a line';
COMMENT ON COLUMN "milog_logstail"."collection_ready" IS 'Any non-zero value is true';

-- ----------------------------
-- 表结构：milog_logstore
-- ----------------------------
CREATE TABLE "milog_logstore"
(
    "id"               BIGSERIAL NOT NULL,
    "ctime"            bigint NULL DEFAULT NULL,
    "utime"            bigint NULL DEFAULT NULL,
    "space_id"         bigint NOT NULL,
    "logstoreName"     varchar(255) NOT NULL,
    "store_period"     int NULL DEFAULT NULL,
    "shard_cnt"        int NULL DEFAULT NULL,
    "key_list"         varchar(1024) NULL DEFAULT NULL,
    "column_type_list" varchar(1024) NULL DEFAULT NULL,
    "log_type"         varchar(11) NULL DEFAULT NULL,
    "es_index"         varchar(255) NULL DEFAULT NULL,
    "es_cluster_id"    bigint NULL DEFAULT NULL,
    "machine_room"     varchar(50) NULL DEFAULT NULL,
    "creator"          varchar(50) NULL DEFAULT NULL,
    "updater"          varchar(50) NULL DEFAULT NULL,
    "mq_resource_id"   bigint NULL DEFAULT NULL,
    "is_matrix_app"    int NULL DEFAULT 0,
    PRIMARY KEY ("id")
);

COMMENT ON COLUMN "milog_logstore"."id" IS 'id';
COMMENT ON COLUMN "milog_logstore"."ctime" IS 'ctime';
COMMENT ON COLUMN "milog_logstore"."utime" IS 'utime';
COMMENT ON COLUMN "milog_logstore"."space_id" IS 'spaceId';
COMMENT ON COLUMN "milog_logstore"."logstoreName" IS 'log store Name';
COMMENT ON COLUMN "milog_logstore"."store_period" IS 'store_period:1-3-5-7';
COMMENT ON COLUMN "milog_logstore"."shard_cnt" IS 'Number of storage shards';
COMMENT ON COLUMN "milog_logstore"."key_list" IS 'key list, Multiple separated by commas';
COMMENT ON COLUMN "milog_logstore"."column_type_list" IS 'column type, Multiple separated by commas';
COMMENT ON COLUMN "milog_logstore"."log_type" IS '1:app,2:ngx..';
COMMENT ON COLUMN "milog_logstore"."es_index" IS 'es index:milog_logstoreName';
COMMENT ON COLUMN "milog_logstore"."es_cluster_id" IS 'es cluster id';
COMMENT ON COLUMN "milog_logstore"."machine_room" IS 'machine info';
COMMENT ON COLUMN "milog_logstore"."creator" IS 'creator';
COMMENT ON COLUMN "milog_logstore"."updater" IS 'updater';
COMMENT ON COLUMN "milog_logstore"."mq_resource_id" IS 'resource mq Id';
COMMENT ON COLUMN "milog_logstore"."is_matrix_app" IS 'is matrix app: 0=false，1=true';

-- ----------------------------
-- 表结构：milog_middleware_config
-- ----------------------------
CREATE TABLE "milog_middleware_config"
(
    "id" BIGSERIAL NOT NULL,
    "type" smallint NOT NULL,
    "region_en" varchar(20) DEFAULT NULL,
    "alias" varchar(255) DEFAULT NULL,
    "name_server" varchar(255) DEFAULT NULL,
    "service_url" varchar(255) DEFAULT NULL,
    "ak" varchar(255) DEFAULT NULL,
    "sk" varchar(255) DEFAULT NULL,
    "broker_name" varchar(255) DEFAULT NULL,
    "token" varchar(255) DEFAULT NULL,
    "dt_catalog" varchar(255) DEFAULT NULL,
    "dt_database" varchar(255) DEFAULT NULL,
    "authorization" text DEFAULT NULL,
    "org_id" varchar(50) DEFAULT NULL,
    "team_id" varchar(50) DEFAULT NULL,
    "is_default" smallint DEFAULT 0,
    "ctime" bigint NOT NULL,
    "utime" bigint NOT NULL,
    "creator" varchar(50) NOT NULL,
    "updater" varchar(50) NOT NULL,
    "labels" json DEFAULT NULL,
    PRIMARY KEY ("id")
);

COMMENT ON COLUMN "milog_middleware_config"."id" IS 'id';
COMMENT ON COLUMN "milog_middleware_config"."type" IS 'config type 1. rocketmq 2.talos';
COMMENT ON COLUMN "milog_middleware_config"."region_en" IS 'region';
COMMENT ON COLUMN "milog_middleware_config"."alias" IS 'alias';
COMMENT ON COLUMN "milog_middleware_config"."name_server" IS 'nameServer addr';
COMMENT ON COLUMN "milog_middleware_config"."service_url" IS 'domain';
COMMENT ON COLUMN "milog_middleware_config"."ak" IS 'ak';
COMMENT ON COLUMN "milog_middleware_config"."sk" IS 'sk';
COMMENT ON COLUMN "milog_middleware_config"."broker_name" IS 'rocketmq addr';
COMMENT ON COLUMN "milog_middleware_config"."token" IS 'token';
COMMENT ON COLUMN "milog_middleware_config"."dt_catalog" IS 'dt catalog';
COMMENT ON COLUMN "milog_middleware_config"."dt_database" IS 'dt database';
COMMENT ON COLUMN "milog_middleware_config"."authorization" IS 'middleware authorization info';
COMMENT ON COLUMN "milog_middleware_config"."org_id" IS 'organization id';
COMMENT ON COLUMN "milog_middleware_config"."team_id" IS 'team id';
COMMENT ON COLUMN "milog_middleware_config"."is_default" IS '0 not default, 1 default';
COMMENT ON COLUMN "milog_middleware_config"."ctime" IS 'ctime';
COMMENT ON COLUMN "milog_middleware_config"."utime" IS 'utime';
COMMENT ON COLUMN "milog_middleware_config"."creator" IS 'creator';
COMMENT ON COLUMN "milog_middleware_config"."updater" IS 'updater';
COMMENT ON COLUMN "milog_middleware_config"."labels" IS 'labels';

-- ----------------------------
-- 表结构：milog_region_zone
-- ----------------------------
CREATE TABLE "milog_region_zone"
(
    "id"             BIGSERIAL NOT NULL,
    "region_name_en" varchar(255) DEFAULT NULL,
    "region_name_cn" varchar(255) DEFAULT NULL,
    "zone_name_en"   varchar(50) DEFAULT NULL,
    "zone_name_cn"   varchar(255) DEFAULT NULL,
    "ctime"          bigint NULL DEFAULT NULL,
    "utime"          bigint NULL DEFAULT NULL,
    "creator"        varchar(50) DEFAULT NULL,
    "updater"        varchar(50) DEFAULT NULL,
    PRIMARY KEY ("id")
);

COMMENT ON COLUMN "milog_region_zone"."id" IS 'id';
COMMENT ON COLUMN "milog_region_zone"."region_name_en" IS 'region name en';
COMMENT ON COLUMN "milog_region_zone"."region_name_cn" IS 'region_name_cn';
COMMENT ON COLUMN "milog_region_zone"."zone_name_en" IS 'zone_name_en';
COMMENT ON COLUMN "milog_region_zone"."zone_name_cn" IS 'zone_name_cn';
COMMENT ON COLUMN "milog_region_zone"."ctime" IS 'ctime';
COMMENT ON COLUMN "milog_region_zone"."utime" IS 'utime';
COMMENT ON COLUMN "milog_region_zone"."creator" IS 'creator';
COMMENT ON COLUMN "milog_region_zone"."updater" IS 'updater';


-- ----------------------------
-- Table structure for milog_space
-- ----------------------------
CREATE TABLE "milog_space"
(
    "id"             BIGSERIAL NOT NULL,
    "ctime"          BIGINT NULL DEFAULT NULL,
    "utime"          BIGINT NULL DEFAULT NULL,
    "tenant_id"      INT NULL DEFAULT NULL,
    "space_name"     VARCHAR(255) NULL DEFAULT NULL,
    "source"         VARCHAR(20) NULL DEFAULT NULL,
    "creator"        VARCHAR(255) NULL DEFAULT NULL,
    "dept_id"        VARCHAR(255) NULL DEFAULT NULL,
    "updater"        VARCHAR(50) NULL DEFAULT NULL,
    "description"    VARCHAR(255) NULL DEFAULT NULL,
    "create_dept_id" VARCHAR(50) NULL DEFAULT NULL,
    "perm_dept_id"   VARCHAR(2000) NULL DEFAULT NULL,
    PRIMARY KEY ("id")
);

COMMENT ON COLUMN "milog_space"."ctime" IS 'ctime';
COMMENT ON COLUMN "milog_space"."utime" IS 'utime';
COMMENT ON COLUMN "milog_space"."tenant_id" IS 'tenant_id';
COMMENT ON COLUMN "milog_space"."space_name" IS 'space_name';
COMMENT ON COLUMN "milog_space"."source" IS 'source: opensource';
COMMENT ON COLUMN "milog_space"."creator" IS 'creator';
COMMENT ON COLUMN "milog_space"."dept_id" IS 'Department of the creator location';
COMMENT ON COLUMN "milog_space"."updater" IS 'updater';
COMMENT ON COLUMN "milog_space"."description" IS 'description';
COMMENT ON COLUMN "milog_space"."create_dept_id" IS 'create_dept_id';
COMMENT ON COLUMN "milog_space"."perm_dept_id" IS 'perm_dept_id';

-- ----------------------------
-- Table structure for milog_store_space_auth
-- ----------------------------
CREATE TABLE "milog_store_space_auth"
(
    "id"       BIGSERIAL NOT NULL,
    "store_id" BIGINT NOT NULL,
    "space_id" BIGINT NOT NULL,
    "ctime"    BIGINT NOT NULL,
    "utime"    BIGINT NULL DEFAULT NULL,
    "creator"  VARCHAR(100) NOT NULL,
    "updater"  VARCHAR(100) NULL DEFAULT NULL,
    PRIMARY KEY ("id")
);

-- ----------------------------
-- Table structure for prometheus_alert
-- ----------------------------
CREATE TABLE "prometheus_alert"
(
    "id"              SERIAL NOT NULL,
    "name"            VARCHAR(255) NOT NULL,
    "cname"           VARCHAR(255) NOT NULL,
    "expr"            VARCHAR(4096) NOT NULL,
    "labels"          VARCHAR(4096) NOT NULL,
    "alert_for"       VARCHAR(20) NOT NULL,
    "env"             VARCHAR(100) DEFAULT NULL,
    "enabled"         SMALLINT NOT NULL DEFAULT 0,
    "priority"        SMALLINT NOT NULL,
    "created_by"      VARCHAR(255) NOT NULL,
    "created_time"    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_time"    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted_by"      VARCHAR(255) NOT NULL,
    "deleted_time"    TIMESTAMP NULL DEFAULT NULL,
    "prom_cluster"    VARCHAR(100) DEFAULT 'public',
    "status"          VARCHAR(32) NOT NULL DEFAULT 'pending',
    "instances"       VARCHAR(255) DEFAULT '',
    "thresholds_op"   VARCHAR(8) DEFAULT NULL,
    "thresholds"      TEXT,
    "type"            INT DEFAULT NULL,
    "alert_member"    VARCHAR(1024) NOT NULL DEFAULT '',
    "alert_at_people" VARCHAR(1024) NOT NULL,
    "annotations"     VARCHAR(4096) NOT NULL DEFAULT '',
    "alert_group"     VARCHAR(255) DEFAULT '',
    PRIMARY KEY ("id"),
    CONSTRAINT "uniq_name" UNIQUE ("cname")
);

COMMENT ON COLUMN "prometheus_alert"."id" IS 'alert id';
COMMENT ON COLUMN "prometheus_alert"."name" IS 'alert name';
COMMENT ON COLUMN "prometheus_alert"."cname" IS 'alert cname';
COMMENT ON COLUMN "prometheus_alert"."expr" IS 'expr';
COMMENT ON COLUMN "prometheus_alert"."labels" IS 'labels';
COMMENT ON COLUMN "prometheus_alert"."alert_for" IS 'for';
COMMENT ON COLUMN "prometheus_alert"."env" IS 'config environment';
COMMENT ON COLUMN "prometheus_alert"."enabled" IS 'enabled';
COMMENT ON COLUMN "prometheus_alert"."priority" IS 'priority';
COMMENT ON COLUMN "prometheus_alert"."created_by" IS 'creator';
COMMENT ON COLUMN "prometheus_alert"."created_time" IS 'created time';
COMMENT ON COLUMN "prometheus_alert"."updated_time" IS 'updated time';
COMMENT ON COLUMN "prometheus_alert"."deleted_by" IS 'delete user';
COMMENT ON COLUMN "prometheus_alert"."deleted_time" IS 'deleted time';
COMMENT ON COLUMN "prometheus_alert"."prom_cluster" IS 'prometheus cluster name';
COMMENT ON COLUMN "prometheus_alert"."status" IS 'Was the configuration successfully sent: pending, success';
COMMENT ON COLUMN "prometheus_alert"."instances" IS 'Instances where the configuration takes effect, separated by commas';
COMMENT ON COLUMN "prometheus_alert"."thresholds_op" IS 'Multiple threshold operators, supporting "or" or "and"';
COMMENT ON COLUMN "prometheus_alert"."thresholds" IS 'Alarm threshold array';
COMMENT ON COLUMN "prometheus_alert"."type" IS 'Mode, simple mode is 0, complex mode is 1';
COMMENT ON COLUMN "prometheus_alert"."alert_member" IS 'alert_member';
COMMENT ON COLUMN "prometheus_alert"."alert_at_people" IS 'alert at people';
COMMENT ON COLUMN "prometheus_alert"."annotations" IS 'annotations';
COMMENT ON COLUMN "prometheus_alert"."alert_group" IS 'group';

-- ----------------------------
-- Table structure for scrape_config
-- ----------------------------
CREATE TABLE "scrape_config"
(
    "id"           SERIAL NOT NULL,
    "prom_cluster" VARCHAR(100) NOT NULL DEFAULT 'public',
    "region"       VARCHAR(128) NOT NULL DEFAULT '',
    "zone"         VARCHAR(128) NOT NULL DEFAULT '',
    "env"          VARCHAR(100) NOT NULL DEFAULT '',
    "status"       VARCHAR(32) NOT NULL DEFAULT 'pending',
    "instances"    VARCHAR(255) DEFAULT '',
    "job_name"     VARCHAR(255) NOT NULL DEFAULT '',
    "body"         TEXT NOT NULL,
    "created_by"   VARCHAR(100) NOT NULL DEFAULT '',
    "created_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "deleted_by"   VARCHAR(100) DEFAULT NULL,
    "deleted_time" TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "unq_job_name" UNIQUE ("job_name", "deleted_by")
);

COMMENT ON COLUMN "scrape_config"."id" IS 'id';
COMMENT ON COLUMN "scrape_config"."prom_cluster" IS 'prometheus cluster name';
COMMENT ON COLUMN "scrape_config"."region" IS 'region';
COMMENT ON COLUMN "scrape_config"."zone" IS 'zone';
COMMENT ON COLUMN "scrape_config"."env" IS 'config env: staging, preview, production';
COMMENT ON COLUMN "scrape_config"."status" IS 'Current status of the task';
COMMENT ON COLUMN "scrape_config"."instances" IS 'Collection tasks';
COMMENT ON COLUMN "scrape_config"."job_name" IS 'Collection task name';
COMMENT ON COLUMN "scrape_config"."body" IS 'scrape_config structure JSON string';
COMMENT ON COLUMN "scrape_config"."created_by" IS 'created_by';
COMMENT ON COLUMN "scrape_config"."created_time" IS 'created_time';
COMMENT ON COLUMN "scrape_config"."updated_time" IS 'updated_time';
COMMENT ON COLUMN "scrape_config"."deleted_by" IS 'deleted_by';
COMMENT ON COLUMN "scrape_config"."deleted_time" IS 'deleted_time';

-- ----------------------------
-- Table structure for silence
-- ----------------------------
CREATE TABLE "silence"
(
    "id"           SERIAL NOT NULL,
    "uuid"         VARCHAR(100) NOT NULL,
    "comment"      VARCHAR(255) NOT NULL,
    "start_time"   TIMESTAMP NOT NULL,
    "end_time"     TIMESTAMP NOT NULL,
    "created_by"   VARCHAR(255) NOT NULL,
    "created_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_time" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "prom_cluster" VARCHAR(100) DEFAULT 'public',
    "status"       VARCHAR(32) NOT NULL DEFAULT 'pending',
    "alert_id"     VARCHAR(255) NOT NULL DEFAULT '0',
    PRIMARY KEY ("id")
);

COMMENT ON COLUMN "silence"."id" IS 'silence id';
COMMENT ON COLUMN "silence"."uuid" IS 'silence uuid';
COMMENT ON COLUMN "silence"."comment" IS 'creator';
COMMENT ON COLUMN "silence"."start_time" IS 'silence start time';
COMMENT ON COLUMN "silence"."end_time" IS 'silence end time';
COMMENT ON COLUMN "silence"."created_by" IS 'creator';
COMMENT ON COLUMN "silence"."created_time" IS 'created time';
COMMENT ON COLUMN "silence"."updated_time" IS 'updated time';
COMMENT ON COLUMN "silence"."prom_cluster" IS 'prometheus cluster name';
COMMENT ON COLUMN "silence"."status" IS 'Was the configuration successfully deployed';
COMMENT ON COLUMN "silence"."alert_id" IS 'alert id';

-- ----------------------------
-- Table structure for silence_matcher
-- ----------------------------
CREATE TABLE "silence_matcher"
(
    "silence_id" INT NOT NULL,
    "name"       VARCHAR(255) NOT NULL,
    "value"      VARCHAR(255) NOT NULL,
    "is_regex"   SMALLINT NOT NULL,
    "is_equal"   SMALLINT NOT NULL
);

COMMENT ON COLUMN "silence_matcher"."silence_id" IS 'silence id';
COMMENT ON COLUMN "silence_matcher"."name" IS 'name';
COMMENT ON COLUMN "silence_matcher"."value" IS 'value';
COMMENT ON COLUMN "silence_matcher"."is_regex" IS 'if is regex matcher';
COMMENT ON COLUMN "silence_matcher"."is_equal" IS 'if is equal matcher';
