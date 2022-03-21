package edu.cornell.kfs.sys.cache;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import io.lettuce.core.TrackingArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.push.PushMessage;
import io.lettuce.core.codec.StringCodec;
import net.sf.ehcache.Ehcache;

public class CuRedisCacheManager implements CacheManager, InitializingBean, Closeable {

    private static final Logger LOG = LogManager.getLogger();

    private static final String VERTICAL_LINE = "|";
    private static final String MESSAGE_TYPE_INVALIDATE = "invalidate";

    private Set<String> cacheNames;
    private Map<String, Long> cacheExpirations;
    private Long defaultExpiration;
    private Set<String> localCachesToClearOnListenerReset;
    private net.sf.ehcache.CacheManager localCacheManager;
    private CuRedisConnectionFactory redisConnectionFactory;
    private GenericObjectPoolConfig poolConfig;

    private Map<String, CuRedisCache> caches;
    private Map<String, CuRedisCache> cachesMappedByKeyPrefix;
    private StringCodec stringHandler;
    private GenericObjectPool<StatefulRedisConnection<String, byte[]>> connectionPool;
    private volatile StatefulRedisConnection<String, byte[]> trackedConnection;
    private Object trackedConnectionLock;
    private ScheduledExecutorService trackedConnectionValidationProcess;

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(cacheNames, "cacheNames cannot be null");
        Objects.requireNonNull(cacheExpirations, "cacheExpirations cannot be null");
        Objects.requireNonNull(defaultExpiration, "defaultExpiration cannot be null");
        Objects.requireNonNull(localCachesToClearOnListenerReset, "localCachesToClearOnListenerReset cannot be null");
        Objects.requireNonNull(localCacheManager, "localCacheManager cannot be null");
        Objects.requireNonNull(redisConnectionFactory, "redisConnectionFactory cannot be null");
        Objects.requireNonNull(poolConfig, "poolConfig cannot be null");
        
        this.caches = buildRedisCaches();
        this.cachesMappedByKeyPrefix = buildMappingsOfKeyPrefixesToCaches(caches.values());
        this.stringHandler = new StringCodec(StandardCharsets.UTF_8);
        this.connectionPool = new GenericObjectPool<>(redisConnectionFactory, poolConfig);
        this.trackedConnection = null;
        this.trackedConnectionLock = new Object();
        this.trackedConnectionValidationProcess = Executors.newSingleThreadScheduledExecutor();
        trackedConnectionValidationProcess.scheduleAtFixedRate(
                this::refreshTrackedRedisConnectionIfNecessary, 0L, 5L, TimeUnit.SECONDS);
    }

    private Map<String, CuRedisCache> buildRedisCaches() {
        return cacheNames.stream()
                .collect(Collectors.toUnmodifiableMap(
                        cacheName -> cacheName, this::buildRedisCache));
    }

    private CuRedisCache buildRedisCache(String cacheName) {
        Ehcache localCache = localCacheManager.getCache(cacheName);
        if (localCache == null) {
            throw new IllegalStateException("Could not find local cache with name: " + cacheName);
        }
        Long expirationMillis = cacheExpirations.getOrDefault(cacheName, defaultExpiration);
        return new CuRedisCache(
                cacheName, localCache, this::handleRedisActionWithPooledConnection, expirationMillis.intValue());
    }

    private Map<String, CuRedisCache> buildMappingsOfKeyPrefixesToCaches(Collection<CuRedisCache> caches) {
        return caches.stream()
                .collect(Collectors.toUnmodifiableMap(CuRedisCache::getKeyPrefix, cache -> cache));
    }

    private <T> T handleRedisActionWithPooledConnection(
            Function<StatefulRedisConnection<String, byte[]>, T> redisAction) {
        StatefulRedisConnection<String, byte[]> redisConnection = null;
        try {
            redisConnection = connectionPool.borrowObject();
            return redisAction.apply(redisConnection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            connectionPool.returnObject(redisConnection);
        }
    }

    private void refreshTrackedRedisConnectionIfNecessary() {
        synchronized (trackedConnectionLock) {
            trackedConnection = replaceTrackedRedisConnectionIfNecessary(trackedConnection);
        }
    }

    private StatefulRedisConnection<String, byte[]> replaceTrackedRedisConnectionIfNecessary(
            StatefulRedisConnection<String, byte[]> oldConnection) {
        if (oldConnection != null) {
            if (redisConnectionFactory.validateActualConnectionObject(oldConnection)) {
                return oldConnection;
            } else {
                invalidateOrReturnConnectionToPool(oldConnection);
            }
        }
        
        StatefulRedisConnection<String, byte[]> newConnection = null;
        try {
            newConnection = connectionPool.borrowObject();
            newConnection.addListener(this::handleMessageFromRedis);
            TrackingArgs trackingArgs = TrackingArgs.Builder.enabled().bcast();
            newConnection.sync().clientTracking(trackingArgs);
            return newConnection;
        } catch (Exception e) {
            LOG.error("replaceTrackedRedisConnectionIfNecessary, Could not prepare new Redis connection", e);
            if (newConnection != null) {
                invalidateOrReturnConnectionToPool(newConnection);
            }
            return null;
        } finally {
            if (oldConnection != null) {
                for (String cacheName : localCachesToClearOnListenerReset) {
                    caches.get(cacheName).clearLocalCacheOnly();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void handleMessageFromRedis(PushMessage pushMessage) {
        if (StringUtils.equalsIgnoreCase(MESSAGE_TYPE_INVALIDATE, pushMessage.getType())) {
            List<Object> content = pushMessage.getContent(stringHandler::decodeValue);
            if (content.size() >= 2) {
                List<String> invalidatedKeys = (List<String>) content.get(1);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("handleMessageFromRedis, Evicting the following keys: " + invalidatedKeys);
                }
                for (String invalidatedKey : invalidatedKeys) {
                    evictFromLocalCache(invalidatedKey);
                }
            }
        }
    }

    private void evictFromLocalCache(String key) {
        String keyPrefix = StringUtils.substring(key, 0, StringUtils.indexOf(key, VERTICAL_LINE) + 1);
        CuRedisCache cache = cachesMappedByKeyPrefix.get(keyPrefix);
        if (cache != null) {
            String keyWithoutPrefix = StringUtils.substringAfter(key, VERTICAL_LINE);
            cache.evictFromLocalCacheOnly(keyWithoutPrefix);
        }
    }

    private void invalidateOrReturnConnectionToPool(StatefulRedisConnection<String, byte[]> redisConnection) {
        try {
            connectionPool.invalidateObject(redisConnection);
        } catch (Exception e) {
            LOG.error("invalidateOrReturnConnectionToPool, Could not invalidate connection; "
                    + "will perform a standard return of the connection to the pool instead", e);
            connectionPool.returnObject(redisConnection);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void close() throws IOException {
        if (trackedConnectionValidationProcess != null && !trackedConnectionValidationProcess.isShutdown()) {
            try {
                trackedConnectionValidationProcess.awaitTermination(500L, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                LOG.error("close, Thread was interrupted during ExecutorService shutdown", e);
            }
            if (!trackedConnectionValidationProcess.isShutdown()) {
                trackedConnectionValidationProcess.shutdownNow();
            }
        }
        
        if (connectionPool != null && !connectionPool.isClosed()) {
            if (trackedConnection != null && trackedConnectionLock != null) {
                synchronized (trackedConnectionLock) {
                    StatefulRedisConnection<String, byte[]> redisConnection = trackedConnection;
                    if (redisConnection != null) {
                        invalidateOrReturnConnectionToPool(redisConnection);
                    }
                }
            }
            IOUtils.closeQuietly(connectionPool);
        }
        
        if (redisConnectionFactory != null) {
            IOUtils.closeQuietly(redisConnectionFactory);
        }
    }

    @Override
    public Cache getCache(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Cache name cannot be blank");
        }
        return caches.get(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return cacheNames;
    }

    public void setCacheNames(Collection<String> cacheNames) {
        this.cacheNames = Set.copyOf(cacheNames);
    }

    public void setCacheExpirations(Map<String, Long> cacheExpirations) {
        this.cacheExpirations = Map.copyOf(cacheExpirations);
    }

    public void setDefaultExpiration(Long defaultExpiration) {
        this.defaultExpiration = defaultExpiration;
    }

    public void setLocalCachesToClearOnListenerReset(Collection<String> localCachesToClearOnListenerReset) {
        this.localCachesToClearOnListenerReset = Set.copyOf(localCachesToClearOnListenerReset);
    }

    public void setLocalCacheManager(net.sf.ehcache.CacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    public void setRedisConnectionFactory(CuRedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

}
