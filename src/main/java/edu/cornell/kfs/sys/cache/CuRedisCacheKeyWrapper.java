package edu.cornell.kfs.sys.cache;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kuali.kfs.sys.KFSConstants;

public final class CuRedisCacheKeyWrapper {

    private final String fullCacheKey;
    private final String cacheName;
    private final String localCacheKey;

    public CuRedisCacheKeyWrapper(String fullCacheKey, String cacheName, String localCacheKey) {
        this.fullCacheKey = StringUtils.defaultIfBlank(fullCacheKey, KFSConstants.EMPTY_STRING);
        this.cacheName = StringUtils.defaultIfBlank(cacheName, KFSConstants.EMPTY_STRING);
        this.localCacheKey = StringUtils.defaultIfBlank(localCacheKey, KFSConstants.EMPTY_STRING);
    }

    public String getFullCacheKey() {
        return fullCacheKey;
    }

    public String getCacheName() {
        return cacheName;
    }

    public String getLocalCacheKey() {
        return localCacheKey;
    }

    public boolean hasExpectedKeyFormat() {
        return StringUtils.isNotBlank(cacheName) && StringUtils.isNotBlank(localCacheKey);
    }

    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof CuRedisCacheKeyWrapper)) {
            return false;
        }
        CuRedisCacheKeyWrapper otherWrapper = (CuRedisCacheKeyWrapper) otherObject;
        return new EqualsBuilder()
                .append(fullCacheKey, otherWrapper.fullCacheKey)
                .append(cacheName, otherWrapper.cacheName)
                .append(localCacheKey, otherWrapper.localCacheKey)
                .build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(fullCacheKey)
                .append(cacheName)
                .append(localCacheKey)
                .build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("fullCacheKey", fullCacheKey)
                .append("cacheName", cacheName)
                .append("localCacheKey", localCacheKey)
                .build();
    }

}
