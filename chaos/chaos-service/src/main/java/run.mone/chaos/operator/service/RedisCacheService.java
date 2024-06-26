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
package run.mone.chaos.operator.service;


import com.xiaomi.youpin.docean.anno.Service;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;
import run.mone.chaos.operator.common.Config;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class RedisCacheService {

    private JedisPool pool = null;

    private JedisCluster jedisCluster = null;

    // init nacos
    public void init() {
        log.info("Init RedisCacheService");
        String redisAddress = Config.ins().get("redis.address", "");
        String redisPWD = Config.ins().get("redis.password", "");
        String redisCluster = Config.ins().get("redis.cluster", "no");
        log.info("Redis address: {}, Password: {},redisCluster:{}", redisAddress, redisPWD, redisCluster);

        if (StringUtils.isEmpty(redisAddress)) {
            throw new IllegalArgumentException("Redis address is null");
        }

        String maxActive = Config.ins().get("redis.maxActive", "24");
        String maxWait = Config.ins().get("redis.maxWait", "1000");
        String maxIdle = Config.ins().get("redis.maxIdle", "8");
        String redisTimeout = Config.ins().get("redis.timeout", "2000");

        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxIdle(Integer.parseInt(maxIdle));
        config.setMinIdle(0);
        config.setMaxTotal(Integer.parseInt(maxActive));
        config.setMaxWaitMillis(Integer.parseInt(maxWait));
        log.info("Chaos Redis config: {}", config.toString());

        try {
            if ("yes".equalsIgnoreCase(redisCluster)) {
                // Cluster mode
                String[] addrArr = redisAddress.split(",");
                Set<HostAndPort> jedisClusterNodes = new HashSet<>();
                for (String addr : addrArr) {
                    String[] infos = addr.split(":");
                    if (infos.length != 2) {
                        throw new IllegalArgumentException("Redis address is in an invalid format");
                    }
                    jedisClusterNodes.add(new HostAndPort(infos[0], Integer.parseInt(infos[1])));
                }
                if (StringUtils.isEmpty(redisPWD)) {
                    jedisCluster = new JedisCluster(jedisClusterNodes, 3000, Integer.parseInt(redisTimeout), 3, config);
                } else {
                    jedisCluster = new JedisCluster(jedisClusterNodes, 3000, Integer.parseInt(redisTimeout), 3, redisPWD, config);
                }
            } else {
                // Single node mode
                String[] infos = redisAddress.split(":");
                if (infos.length != 2) {
                    throw new IllegalArgumentException("Redis address is in an invalid format");
                }
                if (StringUtils.isEmpty(redisPWD)) {
                    throw new IllegalArgumentException("Redis password is null");
                }
                pool = new JedisPool(config, infos[0], Integer.parseInt(infos[1]), Integer.parseInt(redisTimeout), redisPWD);
            }
        } catch (Exception e) {
            log.error("Chaos Redis initialization error: {}", e.getMessage());
        }
    }


    /*
     *  key 锁
     *  expireTime 毫秒超时时间
     * */
    public boolean lock(String key, long expireTime) {
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                SetParams params = new SetParams();
                params.px(expireTime);
                params.nx();
                String opRes = jedis.set(key, "1", params);
                return opRes.equals("OK");
            }
        }

        if (jedisCluster != null) {
            SetParams params = new SetParams();
            params.px(expireTime);
            params.nx();
            String opRes = jedisCluster.set(key, "1", params);
            return opRes.equals("OK");
        }
        throw new RuntimeException("redisPool is null");
    }

    /*
     *  key 锁
     *  注意：未在进行的状态才可以加锁
     * */
    public boolean lockWithoutTime(String key) {
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                SetParams params = new SetParams();
                params.nx();
                String opRes = jedis.set(key, "1", params);
                if (StringUtils.isEmpty(opRes)) {
                    return false;
                }
                return opRes.equals("OK");
            }
        }


        if (jedisCluster != null) {
            SetParams params = new SetParams();
            params.nx();
            String opRes = jedisCluster.set(key, "1", params);
            if (StringUtils.isEmpty(opRes)) {
                return false;
            }
            return opRes.equals("OK");
        }

        throw new RuntimeException("redisPool is null");
    }

    public boolean lockWithoutTime(String key,String value) {
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                SetParams params = new SetParams();
                params.nx();
                String opRes = jedis.set(key, value, params);
                if (StringUtils.isEmpty(opRes)) {
                    return false;
                }
                return opRes.equals("OK");
            }
        }


        if (jedisCluster != null) {
            SetParams params = new SetParams();
            params.nx();
            String opRes = jedisCluster.set(key, value, params);
            if (StringUtils.isEmpty(opRes)) {
                return false;
            }
            return opRes.equals("OK");
        }

        throw new RuntimeException("redisPool is null");
    }

    public boolean set(String key,String value) {
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                SetParams params = new SetParams();
                params.nx();
                String opRes = jedis.set(key, value, params);
                if (StringUtils.isEmpty(opRes)) {
                    return false;
                }
                return opRes.equals("OK");
            }
        }


        if (jedisCluster != null) {
            SetParams params = new SetParams();
            params.nx();
            String opRes = jedisCluster.set(key, value, params);
            if (StringUtils.isEmpty(opRes)) {
                return false;
            }
            return opRes.equals("OK");
        }

        throw new RuntimeException("redisPool is null");
    }

    /*
     *  key 锁
     * */
    public boolean unlock(String key) {
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                Long del = jedis.del(key);
                return del == 1;
            }
        }

        if (jedisCluster != null) {
            Long del = jedisCluster.del(key);
            return del == 1;
        }

        throw new RuntimeException("redisPool is null");
    }

    // 未开始、已恢复状态可以加锁，进行中不可加
    public boolean lockByStatus(String key, long expireTime) {
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                SetParams params = new SetParams();
                params.px(expireTime);
                params.nx();
                String opRes = jedis.set(key, "1", params);
                return opRes.equals("OK");
            }
        }
        throw new RuntimeException("redisPool is null");
    }

    // recover完成后，释放锁，只有恢复状态的锁可以释放
    public boolean unlockByStatus(String key) {
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                Long del = jedis.del(key);
                return del == 1;
            }
        }
        throw new RuntimeException("redisPool is null");
    }

    public String get(String key) {
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                return jedis.get(key);
            }
        }

        if (jedisCluster != null) {
            return jedisCluster.get(key);
        }
        throw new RuntimeException("redisPool is null");
    }

    public boolean del(String key) {
        if (pool != null) {
            try (Jedis jedis = pool.getResource()) {
                Long del = jedis.del(key);
                return del == 1;
            }
        }

        if (jedisCluster != null) {
            Long del = jedisCluster.del(key);
            return del == 1;
        }

        throw new RuntimeException("redisPool is null");
    }

}
