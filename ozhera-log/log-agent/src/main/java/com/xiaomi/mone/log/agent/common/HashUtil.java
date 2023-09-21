package com.xiaomi.mone.log.agent.common;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;

/**
 * @author wtt
 * @version 1.0
 * @description
 * @date 2023/9/21 16:07
 */
public class HashUtil {

    private HashUtil() {
    }

    public static int consistentHash(String partitionKey, int buckets) {
        HashCode hasCode = Hashing.murmur3_32().hashString(partitionKey, StandardCharsets.UTF_8);
        return Hashing.consistentHash(hasCode, buckets);
    }
}
