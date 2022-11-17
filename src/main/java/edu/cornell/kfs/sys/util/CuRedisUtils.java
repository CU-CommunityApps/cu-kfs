package edu.cornell.kfs.sys.util;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.cache.CuCacheConstants;
import edu.cornell.kfs.sys.cache.CuRedisCacheKeyWrapper;

public final class CuRedisUtils {

    public static String buildRedisCacheKey(String cacheName, String localCacheKey) {
        if (StringUtils.isBlank(cacheName) || StringUtils.isBlank(localCacheKey)) {
            throw new IllegalArgumentException("Cache name and/or local key cannot be blank");
        } else if (!StringUtils.isAlphanumeric(cacheName)) {
            throw new IllegalArgumentException("Cache name was not alphanumeric");
        }
        return StringUtils.join(CuCacheConstants.REDIS_KEY_PREFIX_START_CHARS, cacheName,
                CuCacheConstants.REDIS_KEY_PREFIX_END_CHARS, localCacheKey);
    }

    public static CuRedisCacheKeyWrapper wrapRedisCacheKey(String redisCacheKey) {
        String cacheName = KFSConstants.EMPTY_STRING;
        String localCacheKey = KFSConstants.EMPTY_STRING;
        
        int prefixEndCharsIndex = StringUtils.indexOf(redisCacheKey, CuCacheConstants.REDIS_KEY_PREFIX_END_CHARS);
        if (prefixEndCharsIndex >= 0
                && StringUtils.startsWith(redisCacheKey, CuCacheConstants.REDIS_KEY_PREFIX_START_CHARS)) {
            cacheName = StringUtils.substring(redisCacheKey,
                    CuCacheConstants.REDIS_KEY_PREFIX_START_CHARS.length(), prefixEndCharsIndex);
            localCacheKey = StringUtils.substring(redisCacheKey,
                    prefixEndCharsIndex + CuCacheConstants.REDIS_KEY_PREFIX_END_CHARS.length());
            if (!StringUtils.isAlphanumeric(cacheName)) {
                cacheName = KFSConstants.EMPTY_STRING;
            }
        }
        
        return new CuRedisCacheKeyWrapper(redisCacheKey, cacheName, localCacheKey);
    }

}
