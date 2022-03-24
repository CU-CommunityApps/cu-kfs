package edu.cornell.kfs.sys.cache;

import java.util.Collection;

import org.springframework.cache.Cache;

public interface CuLocalCache extends Cache {

    String getKeyPrefixForRemoteKeyStorage();

    void evictFromLocalCacheOnly(Object key);

    void evictFromLocalCacheOnly(Collection<?> keys);

    void clearLocalCacheOnly();

}
