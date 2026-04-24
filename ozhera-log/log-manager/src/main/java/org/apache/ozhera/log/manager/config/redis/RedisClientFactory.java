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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.ozhera.log.common.Config;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class RedisClientFactory {
    private static final ConcurrentHashMap<String, JedisCluster> jedisClusterMap =
            new ConcurrentHashMap<>();

    /**
     * init when application starts
     *
     * <p>if we have more redis pool config, use zk storing these configs and change this init method
     */
    public static void init() {
        RedisCachePool redisCachePool = getRedisCachePool();
        if (jedisClusterMap.containsKey(redisCachePool.getName())) {
            return;
        }
        JedisCluster jedisCluster = createJedisCluster(redisCachePool);
        jedisClusterMap.put(redisCachePool.getName(), jedisCluster);
    }

    public static JedisCluster getJedisCluster() {
        return getJedisCluster(Config.ins().get("redis.pool.name", ""));
    }

    public static JedisCluster getJedisCluster(String redisPoolName) {
        JedisCluster jedisCluster = jedisClusterMap.get(redisPoolName);
        if (jedisCluster == null) {
//            throw new IllegalArgumentException(String.format("redis pool {} not config", redisPoolName));
        }
        return jedisCluster;
    }

    /**
     * create jedis cluster
     *
     * @param redisCachePool
     * @return
     */
    private static JedisCluster createJedisCluster(RedisCachePool redisCachePool) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisCachePool.getMaxTotal());
        poolConfig.setMaxIdle(redisCachePool.getMaxIdle());
        poolConfig.setMinIdle(redisCachePool.getMinIdle());
        poolConfig.setMaxWaitMillis(redisCachePool.getMaxWaitMillis());
        poolConfig.setMinEvictableIdleTimeMillis(redisCachePool.getMinEvictableIdleTimeMillis());
        poolConfig.setBlockWhenExhausted(true);
        poolConfig.setTestWhileIdle(false);
        poolConfig.setTestOnReturn(false);
        Set<HostAndPort> hostAndPortSet = new HashSet<>();
        for (Node node : redisCachePool.getNodes()) {
            hostAndPortSet.add(new HostAndPort(node.getHostname(), node.getPort()));
        }

        JedisCluster jedisCluster;
        if (StringUtils.isNotEmpty(redisCachePool.getPassword())) {
            jedisCluster =
                    new JedisCluster(
                            hostAndPortSet,
                            redisCachePool.getConnTimeout(),
                            redisCachePool.getSocketTimeout(),
                            redisCachePool.getMaxAttempts(),
                            redisCachePool.getPassword(),
                            poolConfig);
        } else {
            jedisCluster =
                    new JedisCluster(
                            hostAndPortSet,
                            redisCachePool.getSocketTimeout(),
                            redisCachePool.getMaxAttempts(),
                            poolConfig);
        }
        log.info("init redis cluster success, poolName:{}", redisCachePool.getName());
        return jedisCluster;
    }

    private static RedisCachePoolImpl getRedisCachePool() {
        String poolName = Config.ins().get("redis.pool.name", "");
        String maxTotalStr = Config.ins().get("redis.pool.max.total", "50");
        String maxIdleStr = Config.ins().get("redis.pool.max.idle", "30");
        String minIdleStr = Config.ins().get("redis.pool.min.idle", "10");
        String maxWaitMillisStr = Config.ins().get("redis.pool.max.wait", "1000");
        String minEvictableIdleTimeMillisStr =
                Config.ins().get("redis.pool.min.evictable.idle.time", "600000");
        String connTimeoutStr = Config.ins().get("redis.connection.timeout", "3000");
        String socketTimeoutStr = Config.ins().get("redis.socket.timeout", "1000");
        String maxAttemptsStr = Config.ins().get("redis.max.attempts", "2");
        String addressesStr = Config.ins().get("redis.addresses", "");
        Validate.notBlank(
                addressesStr,
                String.format("redis.addresses cannot be blank,redis.pool.name=%s", poolName));
        String[] addresses = addressesStr.split(",");
        String password = Config.ins().get("redis.password", "");

        if (StringUtils.isAnyEmpty(poolName, addressesStr, password)) {
            log.error("Failed to init redis cluster");
        }


        RedisCachePoolImpl pool =
                new RedisCachePoolImpl(
                        poolName,
                        Integer.parseInt(maxTotalStr),
                        Integer.parseInt(maxIdleStr),
                        Integer.parseInt(minIdleStr),
                        Long.parseLong(maxWaitMillisStr),
                        Integer.parseInt(connTimeoutStr),
                        Integer.parseInt(socketTimeoutStr),
                        Long.parseLong(minEvictableIdleTimeMillisStr),
                        Integer.parseInt(maxAttemptsStr),
                        password);
        for (String address : addresses) {
            pool.addNode(parseNode(address));
        }

        return pool;
    }

    private static Node parseNode(String address) {
        String[] ss1 = address.split(":");
        String hostname = ss1[0];
        int port = Integer.parseInt(ss1[1]);
        return new NodeImpl().setHostname(hostname).setPort(port);
    }
}
