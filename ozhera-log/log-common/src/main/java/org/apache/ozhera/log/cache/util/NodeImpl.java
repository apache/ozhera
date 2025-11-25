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


package org.apache.ozhera.log.cache.util;

public class NodeImpl implements Node {


    private String hostname;
    private int port;

    public NodeImpl() {
    }

    public String getHostname() {
        return this.hostname;
    }

    public NodeImpl setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public int getPort() {
        return this.port;
    }

    public NodeImpl setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Node) {
            Node anNode = (Node) obj;
            return this.hostname.equals(anNode.getHostname()) && this.port == anNode.getPort();
        } else {
            return false;
        }
    }

    public String toString() {
        return this.hostname + ":" + this.port;
    }

    public int hashCode() {
        throw new UnsupportedOperationException("hashCode not designed");
    }
}
