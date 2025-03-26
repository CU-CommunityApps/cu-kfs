package edu.cornell.kfs.sys.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.service.impl.CacheServiceImpl;
import org.springframework.cache.CacheManager;

public class CuCacheServiceImpl extends CacheServiceImpl {
    private static final Logger LOG = LogManager.getLogger();

    protected CacheManager cacheManager;

    @Override
    public void clearSystemCaches() {
        LOG.debug("clearSystemCaches, cache manager class {}", cacheManager.getClass());
        for (final String cacheName : cacheManager.getCacheNames()) {
            LOG.debug("clearSystemCaches, attempting to clear cache for {}", cacheName);
            cacheManager.getCache(cacheName).clear();
        }
    }
    
    public void setCacheManager(final CacheManager cacheManager) {
        this.cacheManager = cacheManager;
        super.setCacheManager(cacheManager);
    }
}
