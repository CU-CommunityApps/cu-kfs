package edu.cornell.kfs.sys.cache;

import java.util.function.Function;

import io.lettuce.core.api.StatefulRedisConnection;

@FunctionalInterface
public interface CuRedisConnectionHandler {
    <T> T doRedisAction(Function<StatefulRedisConnection<String, byte[]>, T> operation);
}
