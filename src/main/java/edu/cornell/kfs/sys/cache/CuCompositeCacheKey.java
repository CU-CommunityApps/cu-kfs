package edu.cornell.kfs.sys.cache;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kuali.kfs.sys.KFSConstants;

public final class CuCompositeCacheKey {

    public static final CuCompositeCacheKey EMPTY = new CuCompositeCacheKey(null, null);

    private final String cacheName;
    private final String localCacheKey;

    public CuCompositeCacheKey(String cacheName, String localCacheKey) {
        this.cacheName = StringUtils.defaultIfBlank(cacheName, KFSConstants.EMPTY_STRING);
        this.localCacheKey = StringUtils.defaultIfBlank(localCacheKey, KFSConstants.EMPTY_STRING);
    }

    public String getCacheName() {
        return cacheName;
    }

    public String getLocalCacheKey() {
        return localCacheKey;
    }

    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof CuCompositeCacheKey)) {
            return false;
        }
        CuCompositeCacheKey otherCacheKey = (CuCompositeCacheKey) otherObject;
        return new EqualsBuilder().append(cacheName, otherCacheKey.cacheName)
                .append(localCacheKey, otherCacheKey.localCacheKey)
                .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(cacheName).append(localCacheKey).build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("cacheName", cacheName)
                .append("localCacheKey", localCacheKey)
                .build();
    }

    public String toRedisCacheKey() {
        return buildRedisCacheKeyInternal(cacheName, localCacheKey);
    }

    public static String buildRedisCacheKey(String cacheName, String localCacheKey) {
        return buildRedisCacheKeyInternal(StringUtils.defaultIfBlank(cacheName, KFSConstants.EMPTY_STRING),
                StringUtils.defaultIfBlank(localCacheKey, KFSConstants.EMPTY_STRING));
    }

    private static String buildRedisCacheKeyInternal(String cacheName, String localCacheKey) {
        if (StringUtils.isBlank(cacheName) || StringUtils.isBlank(localCacheKey)) {
            throw new IllegalArgumentException("Cache name and/or local key cannot be blank");
        }
        return StringUtils.join(CuCacheConstants.REDIS_KEY_PREFIX_START_CHARS, cacheName,
                CuCacheConstants.REDIS_KEY_PREFIX_END_CHARS, localCacheKey);
    }

    public static CuCompositeCacheKey fromRedisCacheKey(String redisCacheKey) {
        int prefixEndCharsIndex = StringUtils.indexOf(redisCacheKey, CuCacheConstants.REDIS_KEY_PREFIX_END_CHARS);
        if (prefixEndCharsIndex >= 0
                && StringUtils.startsWith(redisCacheKey, CuCacheConstants.REDIS_KEY_PREFIX_START_CHARS)) {
            String cacheName = StringUtils.substring(redisCacheKey,
                    CuCacheConstants.REDIS_KEY_PREFIX_START_CHARS.length(), prefixEndCharsIndex);
            String localCacheKey = StringUtils.substring(redisCacheKey,
                    prefixEndCharsIndex + CuCacheConstants.REDIS_KEY_PREFIX_END_CHARS.length());
            return new CuCompositeCacheKey(cacheName, localCacheKey);
        } else {
            return new CuCompositeCacheKey(null, redisCacheKey);
        }
    }

}
