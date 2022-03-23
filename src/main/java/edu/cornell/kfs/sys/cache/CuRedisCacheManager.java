package edu.cornell.kfs.sys.cache;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import edu.cornell.kfs.sys.CUKFSConstants;
import io.lettuce.core.RedisClient;
import net.sf.ehcache.Ehcache;

public class CuRedisCacheManager implements CacheManager, InitializingBean, Closeable {

    private static final Logger LOG = LogManager.getLogger();

    private Set<String> cacheNames;
    private Map<String, Long> cacheExpirations;
    private Long defaultExpiration;
    private Set<String> localCachesToClearOnListenerReset;
    private Set<String> cachesIgnoringRedisEvents;
    private net.sf.ehcache.CacheManager localCacheManager;
    private RedisClient redisClient;

    private CuRedisConnectionManager connectionManager;
    private Map<String, CuLocalCache> caches;
    private Map<String, CuLocalCache> cachesMappedByKeyPrefix;

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(cacheNames, "cacheNames cannot be null");
        Objects.requireNonNull(cacheExpirations, "cacheExpirations cannot be null");
        Objects.requireNonNull(defaultExpiration, "defaultExpiration cannot be null");
        Objects.requireNonNull(localCachesToClearOnListenerReset, "localCachesToClearOnListenerReset cannot be null");
        Objects.requireNonNull(cachesIgnoringRedisEvents, "cachesIgnoringRedisEvents cannot be null");
        Objects.requireNonNull(localCacheManager, "localCacheManager cannot be null");
        Objects.requireNonNull(redisClient, "redisClient cannot be null");
        
        this.connectionManager = new CuRedisConnectionManager(redisClient,
                this::clearMarkedCachesOnSharedConnectionChange, this::handleInvalidatedRedisKeys);
        this.caches = buildLocalCaches();
        this.cachesMappedByKeyPrefix = buildMappingsOfKeyPrefixesToCaches(caches.values());
    }

    private Map<String, CuLocalCache> buildLocalCaches() {
        return cacheNames.stream()
                .collect(Collectors.toUnmodifiableMap(
                        cacheName -> cacheName, this::buildLocalCache));
    }

    private CuLocalCache buildLocalCache(String cacheName) {
        Ehcache localCache = localCacheManager.getCache(cacheName);
        if (localCache == null) {
            throw new IllegalStateException("Could not find local cache with name: " + cacheName);
        }
        
        if (cachesIgnoringRedisEvents.contains(cacheName)) {
            return new CuDefaultLocalCache(cacheName, localCache);
        } else {
            Long expirationMillis = cacheExpirations.getOrDefault(cacheName, defaultExpiration);
            return new CuRedisAwareLocalCache(cacheName, localCache, connectionManager, expirationMillis);
        }
    }

    private Map<String, CuLocalCache> buildMappingsOfKeyPrefixesToCaches(Collection<CuLocalCache> caches) {
        return caches.stream()
                .filter(cache -> !cachesIgnoringRedisEvents.contains(cache.getName()))
                .collect(Collectors.toUnmodifiableMap(CuLocalCache::getKeyPrefix, cache -> cache));
    }

    private void clearMarkedCachesOnSharedConnectionChange() {
        for (String cacheName : localCachesToClearOnListenerReset) {
            caches.get(cacheName).clearLocalCacheOnly();
        }
    }

    private void handleInvalidatedRedisKeys(List<String> invalidatedKeys) {
        if (invalidatedKeys.size() == 1) {
            evictFromLocalCache(invalidatedKeys.get(0));
        } else {
            evictFromLocalCache(invalidatedKeys);
        }
    }

    private void evictFromLocalCache(String redisKey) {
        String keyPrefix = getPrefixForRedisKey(redisKey);
        CuLocalCache cache = cachesMappedByKeyPrefix.get(keyPrefix);
        if (cache != null) {
            String keyWithoutPrefix = getKeyWithoutPrefix(redisKey);
            if (LOG.isDebugEnabled()) {
                LOG.debug("evictFromLocalCache, Evicting key '" + keyWithoutPrefix + "' with prefix: " + keyPrefix);
            }
            cache.evictFromLocalCacheOnly(keyWithoutPrefix);
        } else {
            LOG.warn("evictFromLocalCache, Could not find Redis-aware cache for evicting key: " + redisKey);
        }
    }

    private void evictFromLocalCache(List<String> redisKeys) {
        Map<String, List<String>> partitionedAndConvertedKeys = redisKeys.stream()
                .collect(Collectors.groupingBy(this::getPrefixForRedisKey,
                        Collectors.mapping(this::getKeyWithoutPrefix, Collectors.toUnmodifiableList())));
        partitionedAndConvertedKeys.forEach(this::evictFromLocalCache);
    }

    private void evictFromLocalCache(String keyPrefix, List<String> potentiallyConvertedKeys) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("evictFromLocalCache, Evicting cache for prefix '" + keyPrefix
                    + "' and potentially-converted keys: " + potentiallyConvertedKeys);
        }
        if (StringUtils.isBlank(keyPrefix)) {
            LOG.warn("evictFromLocalCache, Could not evict " + potentiallyConvertedKeys.size()
                    + " keys that were missing an appropriate prefix");
            return;
        }
        
        CuLocalCache cache = cachesMappedByKeyPrefix.get(keyPrefix);
        if (cache != null) {
            cache.evictFromLocalCacheOnly(potentiallyConvertedKeys);
        } else {
            LOG.warn("evictFromLocalCache, Could not evict " + potentiallyConvertedKeys.size()
                    + " keys that did not have a Redis-aware cache associated with prefix: " + keyPrefix);
        }
    }

    private String getPrefixForRedisKey(String redisKey) {
        int indexOfLastCharInPrefix = StringUtils.indexOf(redisKey, CUKFSConstants.VERTICAL_LINE);
        if (indexOfLastCharInPrefix < 0) {
            return KFSConstants.EMPTY_STRING;
        }
        return StringUtils.substring(redisKey, 0, indexOfLastCharInPrefix + 1);
    }

    private String getKeyWithoutPrefix(String redisKey) {
        String keyWithoutPrefix = StringUtils.substringAfter(redisKey, CUKFSConstants.VERTICAL_LINE);
        return StringUtils.isNotBlank(keyWithoutPrefix) ? keyWithoutPrefix : StringUtils.defaultString(redisKey);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void close() throws IOException {
        IOUtils.closeQuietly(connectionManager);
        if (redisClient != null) {
            try {
                redisClient.shutdown();
            } catch (Exception e) {
                LOG.error("close, Unexpected exception occurred during Redis client shutdown", e);
            }
        }
        if (localCacheManager != null) {
            try {
                localCacheManager.shutdown();
            } catch (Exception e) {
                LOG.error("close, Unexpected exception occurred during EhCache Manager shutdown", e);
            }
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

    public void setCachesIgnoringRedisEvents(Collection<String> cachesIgnoringRedisEvents) {
        this.cachesIgnoringRedisEvents = Set.copyOf(cachesIgnoringRedisEvents);
    }

    public void setLocalCacheManager(net.sf.ehcache.CacheManager localCacheManager) {
        this.localCacheManager = localCacheManager;
    }

    public void setRedisClient(RedisClient redisClient) {
        this.redisClient = redisClient;
    }

}
