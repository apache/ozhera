package io.opentelemetry.instrumentation.api.db;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RedisCommandUtil {
    // 不进行span导出的命令
    public static final String REDIS_EXCLUDE_COMMAND = "PING|AUTH";

    //The operation quantity of these commands is too large, it needs to be skipped.
    private static final Set<String> SKIP_END_NAME = Stream.of("hget", "mget", "mset", "hmget", "hgetall").collect(Collectors.toSet());

    //If it's in skipName and there are no errors, skip it directly.(issue #16)
    public static boolean skipEnd(String operationName, @Nullable Throwable error) {
        return (null != operationName) && (SKIP_END_NAME.contains(operationName.toLowerCase()) && (null == error));
    }
}
