package edu.cornell.kfs.sys.cache;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionProvider.TargetAware;

import io.lettuce.core.RedisURI;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.push.PushMessage;
import io.lettuce.core.codec.StringCodec;

public class ClientTrackingLettuceConnectionProvider implements LettuceConnectionProvider, TargetAware,
        DisposableBean {

    private static final Logger LOG = LogManager.getLogger();

    private final LettuceConnectionProvider actualConnectionProvider;
    private final RedisEventListener eventListener;
    private final StringCodec stringCodec;

    public ClientTrackingLettuceConnectionProvider(LettuceConnectionProvider actualConnectionProvider,
            RedisEventListener eventListener) {
        Objects.requireNonNull(actualConnectionProvider, "actualConnectionProvider cannot be null");
        Objects.requireNonNull(eventListener, "eventListener cannot be null");
        this.actualConnectionProvider = actualConnectionProvider;
        this.eventListener = eventListener;
        this.stringCodec = new StringCodec(StandardCharsets.UTF_8);
    }

    @Override
    public <T extends StatefulConnection<?, ?>> T getConnection(Class<T> connectionType) {
        T connection = actualConnectionProvider.getConnection(connectionType);
        enableClientTrackingForNewConnection(connection);
        return connection;
    }

    @Override
    public <T extends StatefulConnection<?, ?>> CompletionStage<T> getConnectionAsync(Class<T> connectionType) {
        return actualConnectionProvider.getConnectionAsync(connectionType)
                .whenComplete(this::enableClientTrackingForNewConnectionAsynchronously);
    }

    @Override
    public void release(StatefulConnection<?, ?> connection) {
        actualConnectionProvider.release(connection);
        notifyConnectionClosedQuietly();
    }

    @Override
    public CompletableFuture<Void> releaseAsync(StatefulConnection<?, ?> connection) {
        return actualConnectionProvider.releaseAsync(connection)
                .whenComplete(this::notifyConnectionClosedQuietlyAndAsynchronously);
    }

    @Override
    public <T extends StatefulConnection<?, ?>> T getConnection(Class<T> connectionType, RedisURI redisURI) {
        if (!(actualConnectionProvider instanceof TargetAware)) {
            throw new UnsupportedOperationException("Underlying connection provider is not actually target-aware");
        }
        T connection = ((TargetAware) actualConnectionProvider).getConnection(connectionType, redisURI);
        enableClientTrackingForNewConnection(connection);
        return connection;
    }

    @Override
    public <T extends StatefulConnection<?, ?>> CompletionStage<T> getConnectionAsync(Class<T> connectionType,
            RedisURI redisURI) {
        if (!(actualConnectionProvider instanceof TargetAware)) {
            throw new UnsupportedOperationException("Underlying connection provider is not actually target-aware");
        }
        return ((TargetAware) actualConnectionProvider).getConnectionAsync(connectionType, redisURI)
                .whenComplete(this::enableClientTrackingForNewConnectionAsynchronously);
    }

    private <T extends StatefulConnection<?, ?>> void enableClientTrackingForNewConnectionAsynchronously(
            T connection, Throwable error) {
        if (error != null) {
            LOG.error("enableClientTrackingForNewConnectionAsynchronously, Connection initialization failed", error);
            return;
        }
        enableClientTrackingForNewConnection(connection);
    }

    private <T extends StatefulConnection<?, ?>> void enableClientTrackingForNewConnection(T connection) {
        boolean setupSuccessful = false;
        try {
            if (!(connection instanceof StatefulRedisConnection)) {
                throw new RuntimeException("Connection is not an instance of StatefulRedisConnection; "
                        + "if the needed functionality is now available in StatefulConnection instead, then please "
                        + "update the appropriate code");
            }
            StatefulRedisConnection<?, ?> redisConnection = (StatefulRedisConnection<?, ?>) connection;
            TrackingArgs argsEnabledAndBcastAndNoloop = TrackingArgs.Builder.enabled().bcast().noloop();
            redisConnection.addListener(this::handleRedisMessage);
            redisConnection.sync().clientTracking(argsEnabledAndBcastAndNoloop);
            LOG.info("enableClientTrackingForNewConnection, Successfully enabled client tracking "
                    + "on a new Redis connection");
            notifyConnectionEstablishedQuietly();
            setupSuccessful = true;
        } catch (Exception e) {
            setupSuccessful = false;
            LOG.error("enableClientTrackingForNewConnection, Could not initialize client tracking", e);
            throw e;
        } finally {
            if (!setupSuccessful) {
                releaseConnectionQuietlyForClientTrackingFailure(connection);
            }
        }
    }

    private void releaseConnectionQuietlyForClientTrackingFailure(StatefulConnection<?, ?> connection) {
        try {
            actualConnectionProvider.release(connection);
        } catch (Exception e) {
            LOG.error("releaseConnectionQuietlyForClientTrackingFailure, Could not close/release connection", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void handleRedisMessage(PushMessage message) {
        if (StringUtils.equalsIgnoreCase(CuCacheConstants.REDIS_MESSAGE_TYPE_INVALIDATE, message.getType())) {
            List<Object> content = message.getContent(stringCodec::decodeValue);
            if (CollectionUtils.size(content) >= 2) {
                List<String> invalidatedKeys = (List<String>) content.get(1);
                if (CollectionUtils.isNotEmpty(invalidatedKeys)) {
                    notifyCacheKeysInvalidatedQuietly(invalidatedKeys);
                }
            }
        }
    }

    private void notifyConnectionEstablishedQuietly() {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("notifyConnectionEstablishedQuietly, A new Redis connection has been established");
            }
            eventListener.notifyConnectionEstablished();
        } catch (Exception e) {
            LOG.error("notifyConnectionEstablishedQuietly, Error encountered while invoking listener", e);
        }
    }

    private void notifyConnectionClosedQuietlyAndAsynchronously(Void ignoredResult, Throwable error) {
        if (error != null) {
            LOG.error("notifyConnectionClosedQuietlyAndAsynchronously, Close/Release of connection failed", error);
        } else {
            notifyConnectionClosedQuietly();
        }
    }

    private void notifyConnectionClosedQuietly() {
        try {
            LOG.info("notifyConnectionClosedQuietly, A Redis connection has been closed/released");
            eventListener.notifyConnectionClosed();
        } catch (Exception e) {
            LOG.error("notifyConnectionClosedQuietly, Error encountered while invoking listener", e);
        }
    }

    private void notifyCacheKeysInvalidatedQuietly(List<String> keys) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("notifyCacheKeysInvalidatedQuietly, Redis invalidated the following keys: " + keys);
            }
            eventListener.notifyCacheKeysInvalidated(keys);
        } catch (Exception e) {
            LOG.error("notifyCacheKeysInvalidatedQuietly, Error encountered while invoking listener", e);
        }
    }

    @Override
    public void destroy() throws Exception {
        if (actualConnectionProvider instanceof DisposableBean) {
            ((DisposableBean) actualConnectionProvider).destroy();
        }
    }

}
