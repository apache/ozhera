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
package org.apache.ozhera.log.agent.channel;

import org.apache.ozhera.log.agent.input.Input;
import org.apache.ozhera.log.agent.output.Output;
import org.apache.ozhera.log.api.enums.OperateEnum;
import org.apache.ozhera.log.api.model.meta.FilterConf;
import org.apache.ozhera.log.api.model.meta.LogPattern;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author shanwb
 * @date 2021-07-20
 */
@Data
public class ChannelDefine implements Serializable {

    private Long channelId;

    private String tailName;

    private Long appId;

    private String appName;

    private Input input;

    private Output output;

    private OperateEnum operateEnum;

    private List<String> ips;
    /**
     * Relationship between IP and directory
     */
    private List<LogPattern.IPRel> ipDirectoryRel;

    /**
     * filter and script configuration
     */
    private List<FilterConf> filters;

    /**
     * Individual configuration data, default full configuration under this machine.
     */
    private Boolean singleMetaData;

    private String podType;
    /**
     * The log collection in the directory that needs to be deleted when a machine goes offline only has a value when a machine of a certain application goes offline.
     */
    private String delDirectory;

    private List<String> filterLogLevelList;

}
