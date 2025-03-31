package edu.cornell.kfs.sys.batch.service.impl;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.service.impl.CacheServiceImpl;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheManager;

public class CuCacheServiceImpl extends CacheServiceImpl {
    private static final Logger LOG = LogManager.getLogger();

    protected CacheManager cacheManager;

    @Override
    public void clearSystemCaches() {
        LOG.debug("clearSystemCaches, cache manager class {}", cacheManager.getClass());
        RedisCacheManager redisManager = (RedisCacheManager) cacheManager;
        for (final String cacheName : redisManager.getCacheNames()) {
            
            Optional<String> configurationPrefix = ((RedisCache) redisManager.getCache(cacheName)).getCacheConfiguration().getKeyPrefix();
            String cachePreFix = ((RedisCache) redisManager.getCache(cacheName)).getCacheConfiguration().getKeyPrefixFor(cacheName);
         
            LOG.debug("clearSystemCaches, attempting to clear cache for '{}', with configuration prefix '{}' and cachePrefix '{}'", cacheName, configurationPrefix, cachePreFix);
            cacheManager.getCache(cacheName).clear();
        }
    }
    
    public void setCacheManager(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        super.setCacheManager(cacheManager);
    }
}
