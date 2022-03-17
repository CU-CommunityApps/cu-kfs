package edu.cornell.kfs.sys.cache;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import edu.cornell.kfs.sys.util.CuResourceUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;

public class CuRedisConnectionFactory
        implements PooledObjectFactory<StatefulRedisConnection<String, byte[]>>, InitializingBean, Closeable {

    private static final Logger LOG = LogManager.getLogger();

    private static final String DEFAULT_REDIS_PING_RESPONSE = "PONG";

    private RedisClient redisClient;

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(redisClient, "redisClient cannot be null");
    }

    @Override
    public void close() throws IOException {
        try {
            redisClient.shutdown();
        } catch (Exception e) {
            LOG.error("close, Unexpected exception occurred during Redis client shutdown", e);
        }
    }

    @Override
    public PooledObject<StatefulRedisConnection<String, byte[]>> makeObject() throws Exception {
        StatefulRedisConnection<String, byte[]> redisConnection = null;
        try {
            RedisCodec<String, byte[]> codec = RedisCodec.of(
                    new StringCodec(StandardCharsets.UTF_8), new ByteArrayCodec());
            redisConnection = redisClient.connect(codec);
            return new DefaultPooledObject<>(redisConnection);
        } catch (Exception e) {
            LOG.error("makeObject, Unexpected exception occurred while preparing Redis connection; "
                    + "will forcibly close potentially-initialized connection if necessary", e);
            CuResourceUtils.closeQuietly(redisConnection);
            throw e;
        }
    }

    @Override
    public void destroyObject(PooledObject<StatefulRedisConnection<String, byte[]>> p) throws Exception {
        try {
            StatefulRedisConnection<String, byte[]> redisConnection = p.getObject();
            CuResourceUtils.closeQuietly(redisConnection);
        } catch (Exception e) {
            LOG.error("destroyObject, Unexpected exception occurred during destroy; exception will be ignored", e);
        }
    }

    @Override
    public boolean validateObject(PooledObject<StatefulRedisConnection<String, byte[]>> p) {
        return validateActualConnectionObject(p.getObject());
    }

    public boolean validateActualConnectionObject(StatefulRedisConnection<String, byte[]> redisConnection) {
        try {
            String pingResponse = redisConnection.sync().ping();
            return StringUtils.equalsIgnoreCase(DEFAULT_REDIS_PING_RESPONSE, pingResponse);
        } catch (Exception e) {
            LOG.error("validateObject, Unexpected exception while validating object; will remove it from pool", e);
            return false;
        }
    }

    @Override
    public void activateObject(PooledObject<StatefulRedisConnection<String, byte[]>> p) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("activateObject, No-op reinitialization of pooled connection");
        }
    }

    @Override
    public void passivateObject(PooledObject<StatefulRedisConnection<String, byte[]>> p) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("passivateObject, No-op uninitialization of pooled connection");
        }
    }

    public void setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

}
