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
/**
 * @author wtt
 * @date 2025/11/21 15:04
 * @version 1.0
 *
 * 日志过滤器链，这个过滤器使用了pipeline模式,参考了tomcat的过滤器链,通过SPI机制,
 * 将日志过滤器加入到过滤器链中，并实现日志过滤器接口，实现日志过滤逻辑
 */
package org.apache.ozhera.log.agent.channel.pipeline;