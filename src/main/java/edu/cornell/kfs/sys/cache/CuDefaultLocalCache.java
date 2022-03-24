package edu.cornell.kfs.sys.cache;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.cache.ehcache.EhCacheCache;

import net.sf.ehcache.Ehcache;

public class CuDefaultLocalCache extends EhCacheCache implements CuLocalCache {

    protected final String cacheName;
    protected final Ehcache localCache;

    public CuDefaultLocalCache(String cacheName, Ehcache localCache) {
        super(localCache);
        if (StringUtils.isBlank(cacheName)) {
            throw new IllegalArgumentException("cacheName cannot be blank");
        } else if (!StringUtils.equals(cacheName, localCache.getName())) {
            throw new IllegalArgumentException("cacheName does not match up with name derived from localCache");
        }
        this.cacheName = cacheName;
        this.localCache = localCache;
    }

    @Override
    public String getKeyPrefixForRemoteKeyStorage() {
        return KFSConstants.EMPTY_STRING;
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Callable<T> wrappedLoader = () -> getFromLoaderAndSendNotification(key, valueLoader);
        return super.get(key, wrappedLoader);
    }

    protected <T> T getFromLoaderAndSendNotification(Object key, Callable<T> valueLoader) throws Exception {
        T loadedValue = valueLoader.call();
        notifyOfNewCacheEntryQuietly(key);
        return loadedValue;
    }

    @Override
    public void put(Object key, Object value) {
        super.put(key, value);
        notifyOfNewCacheEntryQuietly(key);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        ValueWrapper existingValue = super.putIfAbsent(key, value);
        if (existingValue == null) {
            notifyOfNewCacheEntryQuietly(key);
        }
        return existingValue;
    }

    @Override
    public void evict(Object key) {
        super.evict(key);
        notifyOfDeletedCacheEntryQuietly(key);
    }

    @Override
    public void evictFromLocalCacheOnly(Object key) {
        super.evict(key);
    }

    @Override
    public void evictFromLocalCacheOnly(Collection<?> keys) {
        localCache.removeAll(keys);
    }

    @Override
    public void clear() {
        super.clear();
        notifyOfClearedCacheQuietly();
    }

    @Override
    public void clearLocalCacheOnly() {
        super.clear();
    }

    protected void notifyOfNewCacheEntryQuietly(Object key) {
        
    }

    protected void notifyOfDeletedCacheEntryQuietly(Object key) {
        
    }

    protected void notifyOfClearedCacheQuietly() {
        
    }

}
