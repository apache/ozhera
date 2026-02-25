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

package org.apache.ozhera.log.manager.config.redis;



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RedisCachePoolImpl implements RedisCachePool{

    private static Comparator<Node> nodeComparator =
            new Comparator<Node>() {
                @Override
                public int compare(Node n1, Node n2) {
                    int cmp = n1.getHostname().compareTo(n2.getHostname());
                    if (cmp != 0) {
                        return cmp;
                    }
                    if (n1.getPort() < n2.getPort()) {
                        return -1;
                    } else if (n1.getPort() > n2.getPort()) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            };

    private String name;
    private int maxTotal;
    private int maxIdle;
    private int minIdle;
    private int connTimeout;
    private int socketTimeout;
    private long maxWaitMillis;
    private int maxAttempts;
    private String password;
    private long minEvictableIdleTimeMillis;
    private List<Node> nodes = new ArrayList<>();

    public RedisCachePoolImpl() {
    }

    public RedisCachePoolImpl(
            String name,
            int maxTotal,
            int maxIdle,
            int minIdle,
            long maxWaitMillis,
            int connTimeout,
            int socketTimeout,
            long minEvictableIdleTimeMillis,
            int maxAttempts,
            String password) {
        this.name = name;
        this.maxTotal = maxTotal;
        this.maxIdle = maxIdle;
        this.minIdle = minIdle;
        this.maxWaitMillis = maxWaitMillis;
        this.connTimeout = connTimeout;
        this.socketTimeout = socketTimeout;
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        this.maxAttempts = maxAttempts;
        this.password = password;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    @Override
    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    @Override
    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public long getMaxWaitMillis() {
        return maxWaitMillis;
    }

    public void setMaxWaitMillis(long maxWaitMillis) {
        this.maxWaitMillis = maxWaitMillis;
    }

    @Override
    public int getConnTimeout() {
        return connTimeout;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    @Override
    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    @Override
    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * add a node to pool
     *
     * @param node
     */
    public void addNode(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }
        for (Node n : nodes) {
            // hostname和port都一样就不能添加进来
            // 因为不考虑权重，所以这里不能用equals来判断
            if (n.getHostname().equals(node.getHostname()) && n.getPort() == node.getPort()) {
                throw new IllegalArgumentException(
                        "Duplicated Node: " + node.toString() + ", currently in List is: " + n.toString());
            }
        }
        nodes.add(node);
    }

    /**
     * Compare whether the two groups of nodes are the same. As long as the data are the same, it doesn't matter about the order.
     *
     * @param nodes1
     * @param nodes2
     * @return
     */
    private boolean compareNodes(List<Node> nodes1, List<Node> nodes2) {
        if (nodes1.size() == nodes2.size()) {
            Node[] type = new Node[]{};
            Node[] nodea1 = nodes1.toArray(type);
            Node[] nodea2 = nodes2.toArray(type);
            Arrays.sort(nodea1, nodeComparator);
            Arrays.sort(nodea2, nodeComparator);
            return Arrays.equals(nodea1, nodea2);
        } else {
            return false;
        }
    }

    public int hashCode() {
        throw new UnsupportedOperationException("hashCode not designed");
    }

    public boolean equals(Object anObject) {
        if (anObject != null && anObject instanceof CachePool) {
            RedisCachePool anPool = (RedisCachePool) anObject;
            if (this.name.equals(anPool.getName())) {
                return this.getMaxTotal() == anPool.getMaxTotal()
                        && this.getMaxIdle() == anPool.getMaxIdle()
                        && this.getMinIdle() == anPool.getMinIdle()
                        && this.getMaxWaitMillis() == anPool.getMaxWaitMillis()
                        && this.getConnTimeout() == anPool.getConnTimeout()
                        && this.getSocketTimeout() == anPool.getSocketTimeout()
                        && this.getMinEvictableIdleTimeMillis() == anPool.getMinEvictableIdleTimeMillis()
                        && this.getMaxAttempts() == anPool.getMaxAttempts()
                        && this.getPassword().equals(anPool.getPassword())
                        && compareNodes(this.nodes, anPool.getNodes());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "RedisCachePoolImpl{"
                + "name='"
                + name
                + '\''
                + ", maxTotal="
                + maxTotal
                + ", maxIdle="
                + maxIdle
                + ", minIdle="
                + minIdle
                + ", maxWaitMillis="
                + maxWaitMillis
                + ", connTimeout="
                + connTimeout
                + ", socketTimeout="
                + socketTimeout
                + ", minEvictableIdleTimeMillis="
                + minEvictableIdleTimeMillis
                + ", maxAttempts="
                + maxAttempts
                + ", nodes="
                + nodes
                + '}';
    }
}
