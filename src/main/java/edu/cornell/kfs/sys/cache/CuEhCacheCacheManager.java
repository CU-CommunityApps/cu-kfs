package edu.cornell.kfs.sys.cache;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.cache.ehcache.EhCacheCacheManager;

import edu.cornell.kfs.sys.util.CuRedisUtils;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.event.NotificationScope;
import net.sf.ehcache.event.RegisteredEventListeners;

public class CuEhCacheCacheManager extends EhCacheCacheManager implements RedisEventListener, DisposableBean {

    private static final Logger LOG = LogManager.getLogger();

    private CacheManager ehcacheManager;
    private RedisEventListenerLazyInitProxy redisEventListenerProxy;
    private EhcacheEventListenerForUpdatingRedis ehcacheEventListener;
    private Set<String> cacheNames;
    private Set<String> cachesIgnoringRedisEvents;
    private Set<String> cachesToClearOnRedisConnectionChange;
    private long defaultTimeToLive;

    private Set<String> redisAwareCacheNames;

    @Override
    public void afterPropertiesSet() {
        Objects.requireNonNull(ehcacheManager, "Internal Ehcache Manager cannot be null");
        Objects.requireNonNull(redisEventListenerProxy, "redisEventListenerProxy cannot be null");
        Objects.requireNonNull(ehcacheEventListener, "ehcacheEventListener cannot be null");
        Objects.requireNonNull(cacheNames, "cacheNames cannot be null");
        Objects.requireNonNull(cachesIgnoringRedisEvents, "cachesIgnoringRedisEvents cannot be null");
        Objects.requireNonNull(cachesToClearOnRedisConnectionChange,
                "cachesToClearOnRedisConnectionChange cannot be null");
        if (defaultTimeToLive <= 0) {
            throw new IllegalStateException("defaultTimeToLive cannot be non-positive");
        }
        
        if (!cacheNames.containsAll(cachesIgnoringRedisEvents)) {
            LOG.error("afterPropertiesSet, The following Redis-ignored caches were not in the cacheNames Set: "
                    + findCacheNamesNotIncludedInMainSet(cacheNames, cachesIgnoringRedisEvents));
            throw new IllegalStateException("cachesIgnoringRedisEvents references caches not found in cacheNames");
        } else if (!cacheNames.containsAll(cachesToClearOnRedisConnectionChange)) {
            LOG.error("afterPropertiesSet, The following on-connection-change caches were not in the cacheNames Set: "
                    + findCacheNamesNotIncludedInMainSet(cacheNames, cachesToClearOnRedisConnectionChange));
            throw new IllegalStateException(
                    "cachesToClearOnRedisConnectionChange references caches not found in cacheNames");
        }
        
        super.afterPropertiesSet();
        
        this.redisAwareCacheNames = cacheNames.stream()
                .filter(cacheName -> !cachesIgnoringRedisEvents.contains(cacheName))
                .collect(Collectors.toUnmodifiableSet());
        
        redisAwareCacheNames.stream()
                .map(this::getLocalCache)
                .peek(this::overrideTimeToLiveOnRedisAwareCacheIfNecessary)
                .forEach(this::addEventListenerToLocalCache);
        
        redisEventListenerProxy.setActualEventListener(this);
    }

    private Set<String> findCacheNamesNotIncludedInMainSet(Set<String> namesSet, Set<String> namesSubset) {
        return namesSubset.stream()
                .filter(cacheName -> !namesSet.contains(cacheName))
                .collect(Collectors.toUnmodifiableSet());
    }

    private Ehcache getLocalCache(String cacheName) {
        Ehcache localCache = ehcacheManager.getEhcache(cacheName);
        if (localCache == null) {
            throw new IllegalStateException("Could not find named cache: " + cacheName);
        }
        return localCache;
    }

    private void overrideTimeToLiveOnRedisAwareCacheIfNecessary(Ehcache localCache) {
        CacheConfiguration cacheConfiguration = localCache.getCacheConfiguration();
        long cacheTimeToLive = cacheConfiguration.getTimeToLiveSeconds();
        if (cacheTimeToLive <= 0) {
            LOG.warn("overrideTimeToLiveOnRedisAwareCacheIfNecessary, Cache " + localCache.getName()
                    + " is Redis-aware but has an infinite/undefined time-to-live; overriding time-to-live to "
                    + defaultTimeToLive + " seconds. Please consider updating this cache's EhCache configuration.");
            cacheConfiguration.setTimeToLiveSeconds(defaultTimeToLive);
        }
    }

    private void addEventListenerToLocalCache(Ehcache localCache) {
        RegisteredEventListeners notificationService = localCache.getCacheEventNotificationService();
        notificationService.registerListener(ehcacheEventListener, NotificationScope.LOCAL);
    }

    @Override
    public void notifyConnectionEstablished() {
        clearMarkedCachesForConnectionChange();
    }

    @Override
    public void notifyConnectionClosed() {
        clearMarkedCachesForConnectionChange();
    }

    private void clearMarkedCachesForConnectionChange() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearMarkedCachesForConnectionChange, Fully clearing the following local caches: "
                    + cachesToClearOnRedisConnectionChange);
        }
        cachesToClearOnRedisConnectionChange.stream()
                .map(this::getLocalCache)
                .forEach(localCache -> localCache.removeAll(true));
    }

    @Override
    public void notifyCacheKeysInvalidated(List<String> keys) {
        if (keys.size() == 1) {
            removeKeyFromCache(keys.get(0));
        } else {
            removeKeysFromCache(keys);
        }
    }

    private void removeKeyFromCache(String redisCacheKey) {
        CuRedisCacheKeyWrapper wrappedKey = CuRedisUtils.wrapRedisCacheKey(redisCacheKey);
        if (shouldRemoveKeyFromCache(wrappedKey)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("removeKeyFromCache, Removing the following entry from the local cache: "
                        + wrappedKey.getCacheName() + " --- " + wrappedKey.getLocalCacheKey());
            }
            getLocalCache(wrappedKey.getCacheName())
                    .remove(wrappedKey.getLocalCacheKey(), true);
        }
    }

    private boolean shouldRemoveKeyFromCache(CuRedisCacheKeyWrapper wrappedKey) {
        if (!wrappedKey.hasExpectedKeyFormat()) {
            LOG.warn("shouldRemoveKeyFromCache, Redis invalidated a cache key with an unexpected format: "
                    + wrappedKey.getFullCacheKey());
            return false;
        } else if (!redisAwareCacheNames.contains(wrappedKey.getCacheName())) {
            LOG.warn("shouldRemoveKeyFromCache, Redis invalidated a cache key with an unexpected cache name prefix: "
                    + wrappedKey.getFullCacheKey());
            return false;
        } else {
            return true;
        }
    }

    private void removeKeysFromCache(List<String> redisCacheKeys) {
        redisCacheKeys.stream()
                .map(CuRedisUtils::wrapRedisCacheKey)
                .filter(this::shouldRemoveKeyFromCache)
                .collect(Collectors.groupingBy(CuRedisCacheKeyWrapper::getCacheName,
                        Collectors.mapping(CuRedisCacheKeyWrapper::getLocalCacheKey, Collectors.toUnmodifiableList())))
                .forEach(this::removeKeysFromCache);
    }

    private void removeKeysFromCache(String cacheName, List<String> localCacheKeys) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeKeysFromCache, Removing the following entries from the local cache. Cache name: "
                    + cacheName + ", Cache keys: " + localCacheKeys);
        }
        getLocalCache(cacheName).removeAll(localCacheKeys, true);
    }

    @Override
    public void destroy() throws Exception {
        if (redisEventListenerProxy != null) {
            redisEventListenerProxy.destroy();
        }
        if (ehcacheEventListener != null) {
            ehcacheEventListener.dispose();
        }
        if (ehcacheManager != null) {
            ehcacheManager.shutdown();
        }
    }

    @Override
    public void setCacheManager(CacheManager cacheManager) {
        super.setCacheManager(cacheManager);
        this.ehcacheManager = cacheManager;
    }

    public void setRedisEventListenerProxy(RedisEventListenerLazyInitProxy redisEventListenerProxy) {
        this.redisEventListenerProxy = redisEventListenerProxy;
    }

    public void setEhcacheEventListener(EhcacheEventListenerForUpdatingRedis ehcacheEventListener) {
        this.ehcacheEventListener = ehcacheEventListener;
    }

    public void setCacheNames(Collection<String> cacheNames) {
        this.cacheNames = Set.copyOf(cacheNames);
    }

    public void setCachesIgnoringRedisEvents(Collection<String> cachesIgnoringRedisEvents) {
        this.cachesIgnoringRedisEvents = Set.copyOf(cachesIgnoringRedisEvents);
    }

    public void setCachesToClearOnRedisConnectionChange(Collection<String> cachesToClearOnRedisConnectionChange) {
        this.cachesToClearOnRedisConnectionChange = Set.copyOf(cachesToClearOnRedisConnectionChange);
    }

    public void setDefaultTimeToLive(long defaultTimeToLive) {
        this.defaultTimeToLive = defaultTimeToLive;
    }

}
