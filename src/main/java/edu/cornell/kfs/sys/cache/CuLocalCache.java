package edu.cornell.kfs.sys.cache;

import java.util.List;

import org.springframework.cache.Cache;

public interface CuLocalCache extends Cache {

    String getKeyPrefix();

    void evictFromLocalCacheOnly(Object key);

    void evictFromLocalCacheOnly(List<?> keys);

    void clearLocalCacheOnly();

}
