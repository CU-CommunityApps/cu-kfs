package edu.cornell.kfs.sys.cache;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.cornell.kfs.sys.util.CuResourceUtils;
import io.lettuce.core.RedisClient;
import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.push.PushMessage;
import io.lettuce.core.codec.StringCodec;

public class CuRedisConnectionManager implements Closeable {

    private static final Logger LOG = LogManager.getLogger();

    private final RedisClient redisClient;
    private final Runnable connectionChangeListener;
    private final Consumer<List<String>> redisKeyInvalidationListener;
    private final StringCodec stringCodec;
    private final AtomicMarkableReference<StatefulRedisConnection<String, String>> sharedConnection;
    private final AtomicReference<StatefulRedisConnection<String, String>> connectionChangeTracker;
    private final ExecutorService redisTaskThreadPool;
    private final ScheduledExecutorService trackedConnectionValidationProcess;
    private final ScheduledFuture<?> scheduledValidationProcess;

    public CuRedisConnectionManager(RedisClient redisClient, Runnable connectionChangeListener,
            Consumer<List<String>> redisKeyInvalidationListener) {
        this.redisClient = redisClient;
        this.connectionChangeListener = connectionChangeListener;
        this.redisKeyInvalidationListener = redisKeyInvalidationListener;
        this.stringCodec = new StringCodec(StandardCharsets.UTF_8);
        this.sharedConnection = new AtomicMarkableReference<>(null, true);
        this.connectionChangeTracker = new AtomicReference<>(null);
        this.redisTaskThreadPool = Executors.newCachedThreadPool();
        this.trackedConnectionValidationProcess = Executors.newSingleThreadScheduledExecutor();
        this.scheduledValidationProcess = trackedConnectionValidationProcess.scheduleAtFixedRate(
                this::handleChangeOfSharedConnectionIfNecessary, 0L, 5L, TimeUnit.SECONDS);
    }

    public void executeRedisTaskAsynchronously(Consumer<StatefulRedisConnection<String, String>> redisTask) {
        redisTaskThreadPool.execute(() -> executeRedisTask(redisTask));
    }

    public void executeRedisTask(Consumer<StatefulRedisConnection<String, String>> redisTask) {
        StatefulRedisConnection<String, String> redisConnection = getSharedConnection();
        redisTask.accept(redisConnection);
    }

    public <T> void executeRedisTaskAsynchronously(
            BiConsumer<StatefulRedisConnection<String, String>, T> redisTask, T extraTaskArg) {
        redisTaskThreadPool.execute(() -> executeRedisTask(redisTask, extraTaskArg));
    }

    public <T> void executeRedisTask(
            BiConsumer<StatefulRedisConnection<String, String>, T> redisTask, T extraTaskArg) {
        StatefulRedisConnection<String, String> redisConnection = getSharedConnection();
        redisTask.accept(redisConnection, extraTaskArg);
    }

    private void handleChangeOfSharedConnectionIfNecessary() {
        StatefulRedisConnection<String, String> currentConnection = getSharedConnectionIfPossible();
        StatefulRedisConnection<String, String> oldConnection = connectionChangeTracker.getAndSet(currentConnection);
        if (oldConnection != currentConnection) {
            CuResourceUtils.closeQuietly(oldConnection);
            connectionChangeListener.run();
        }
    }

    private StatefulRedisConnection<String, String> getSharedConnection() {
        StatefulRedisConnection<String, String> redisConnection = getSharedConnectionIfPossible();
        if (redisConnection == null) {
            throw new RuntimeException("Could not retrieve or initialize a shared connection");
        }
        return redisConnection;
    }

    private StatefulRedisConnection<String, String> getSharedConnectionIfPossible() {
        boolean[] managerActive = new boolean[1];
        StatefulRedisConnection<String, String> redisConnection = sharedConnection.get(managerActive);
        if (!managerActive[0]) {
            throw new IllegalStateException("Connection Manager has already been closed");
        } else if (!isConnectionValid(redisConnection)) {
            return getOrCreateSharedConnection(redisConnection);
        }
        return redisConnection;
    }

    private StatefulRedisConnection<String, String> getOrCreateSharedConnection(
            StatefulRedisConnection<String, String> oldConnection) {
        synchronized (this) {
            StatefulRedisConnection<String, String> currentConnection = null;
            boolean[] managerActive = new boolean[1];
            try {
                currentConnection = sharedConnection.get(managerActive);
                if (!managerActive[0]) {
                    throw new IllegalStateException("Connection Manager has already been closed");
                } else if (!isConnectionValid(currentConnection)) {
                    CuResourceUtils.closeQuietly(oldConnection);
                    CuResourceUtils.closeQuietly(currentConnection);
                    currentConnection = getNewConnection();
                    if (!sharedConnection.compareAndSet(oldConnection, currentConnection, true, true)) {
                        throw new IllegalStateException(
                                "Could not update shared connection reference; this should NEVER happen!");
                    }
                    LOG.info("getOrCreateSharedConnection, Successfully created a new shared Redis connection");
                }
                return currentConnection;
            } catch (RuntimeException e) {
                CuResourceUtils.closeQuietly(oldConnection);
                CuResourceUtils.closeQuietly(currentConnection);
                LOG.error("getOrCreateSharedConnection, Unexpected exception occurred while preparing "
                        + "Redis connection; will forcibly close potentially-initialized connection if necessary", e);
                if (!managerActive[0] && e instanceof IllegalStateException) {
                    throw e;
                }
                return null;
            }
        }
    }

    private StatefulRedisConnection<String, String> getNewConnection() {
        StatefulRedisConnection<String, String> redisConnection = null;
        try {
            redisConnection = redisClient.connect(stringCodec);
            if (!isConnectionValid(redisConnection)) {
                throw new RuntimeException(
                        "Newly-created Redis connection could not perform a successful ping() operation");
            }
            redisConnection.addListener(this::handleMessageFromRedis);
            TrackingArgs trackingArgs = TrackingArgs.Builder.enabled().bcast().noloop();
            redisConnection.sync().clientTracking(trackingArgs);
            return redisConnection;
        } catch (Exception e) {
            CuResourceUtils.closeQuietly(redisConnection);
            LOG.error("getNewConnection, Unexpected exception occurred while preparing Redis connection; "
                    + "will forcibly close potentially-initialized connection if necessary", e);
            return null;
        }
    }

    public boolean isConnectionValid(StatefulRedisConnection<String, String> redisConnection) {
        if (redisConnection == null) {
            LOG.warn("isConnectionValid, Connection does not exist; caller should generate a new one");
            return false;
        }
        try {
            String pingResponse = redisConnection.sync().ping();
            boolean result = StringUtils.equalsIgnoreCase(CuCacheConstants.REDIS_DEFAULT_PING_RESPONSE, pingResponse);
            if (!result) {
                LOG.error("isConnectionValid, Unexpected PING response was received from Redis: " + pingResponse);
            }
            return result;
        } catch (Exception e) {
            LOG.error("isConnectionValid, Unexpected exception while validating connection", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private void handleMessageFromRedis(PushMessage pushMessage) {
        if (StringUtils.equalsIgnoreCase(CuCacheConstants.REDIS_MESSAGE_TYPE_INVALIDATE, pushMessage.getType())) {
            List<Object> content = pushMessage.getContent(stringCodec::decodeValue);
            if (CollectionUtils.size(content) >= 2) {
                List<String> invalidatedKeys = (List<String>) content.get(1);
                if (CollectionUtils.isNotEmpty(invalidatedKeys)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("handleMessageFromRedis, Handling invalidation of the following Redis keys: "
                                + invalidatedKeys);
                    }
                    redisKeyInvalidationListener.accept(invalidatedKeys);
                }
            }
        }
    }

    @Override
    public void close() throws IOException {
        shutDownConnectionValidationProcessQuietly();
        shutDownThreadPoolQuietly();
        shutDownRedisClientAndConnectionsQuietly();
    }

    private void shutDownConnectionValidationProcessQuietly() {
        try {
            scheduledValidationProcess.cancel(false);
        } catch (Exception e) {
            LOG.error("shutDownConnectionValidationProcessQuietly, Unexpected exception occurred while shutting down "
                    + "the periodic validation of the Redis connection", e);
        }
        shutDownExecutorServiceQuietly(trackedConnectionValidationProcess);
    }

    private void shutDownThreadPoolQuietly() {
        shutDownExecutorServiceQuietly(redisTaskThreadPool);
    }

    private void shutDownExecutorServiceQuietly(ExecutorService executorService) {
        try {
            if (!executorService.isShutdown()) {
                try {
                    executorService.awaitTermination(5000L, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    LOG.error("shutDownExecutorServiceQuietly, Thread was interrupted "
                            + "during ExecutorService shutdown", e);
                }
                if (!executorService.isShutdown()) {
                    executorService.shutdownNow();
                }
            }
        } catch (Exception e) {
            LOG.error("shutDownExecutorServiceQuietly, Unexpected exception occurred while "
                    + "shutting down ExecutorService", e);
        }
    }

    private void shutDownRedisClientAndConnectionsQuietly() {
        synchronized (this) {
            try {
                StatefulRedisConnection<String, String> redisConnection = sharedConnection.getReference();
                CuResourceUtils.closeQuietly(redisConnection);
                sharedConnection.set(null, false);
                
                StatefulRedisConnection<String, String> oldRedisConnection = connectionChangeTracker.get();
                CuResourceUtils.closeQuietly(oldRedisConnection);
                connectionChangeTracker.set(null);
                
                redisClient.shutdown();
            } catch (Exception e) {
                LOG.error("shutDownRedisClientAndConnectionsQuietly, Unexpected exception occurred "
                        + "during Redis client/connection shutdown", e);
            }
        }
    }

}
