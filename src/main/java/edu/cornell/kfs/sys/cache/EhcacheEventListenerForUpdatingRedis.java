package edu.cornell.kfs.sys.cache;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.KeyScanOptions;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;

import edu.cornell.kfs.sys.util.CuRedisUtils;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

public class EhcacheEventListenerForUpdatingRedis implements CacheEventListener, DisposableBean {

    private static final Logger LOG = LogManager.getLogger();

    private static final int KEY_BATCH_SIZE = 50;

    private final RedisTemplate<String, String> redisTemplate;
    private final ExecutorService redisExecutor;

    public EhcacheEventListenerForUpdatingRedis(RedisTemplate<String, String> redisTemplate) {
        Objects.requireNonNull(redisTemplate, "redisTemplate cannot be null");
        int poolSize = Runtime.getRuntime().availableProcessors();
        this.redisTemplate = redisTemplate;
        this.redisExecutor = new ThreadPoolExecutor(poolSize, poolSize, 0L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
    }

    @Override
    public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
        addCacheKeyPlaceholderInRedisIfAbsent(cache, element);
    }

    @Override
    public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
        addCacheKeyPlaceholderInRedisIfAbsent(cache, element);
    }

    private void addCacheKeyPlaceholderInRedisIfAbsent(Ehcache cache, Element element) {
        String cacheName = cache.getName();
        String localCacheKey = Objects.toString(element.getObjectKey());
        long timeToLive = determineTimeToLiveForCache(cache);
        
        redisExecutor.execute(() -> {
            String redisCacheKey = CuRedisUtils.buildRedisCacheKey(cacheName, localCacheKey);
            if (LOG.isDebugEnabled()) {
                LOG.debug("addCacheKeyPlaceholderInRedisIfAbsent, Adding placeholder value in redis for key: "
                        + redisCacheKey);
            }
            redisTemplate.opsForValue().setIfAbsent(redisCacheKey, KFSConstants.ACTIVE_INDICATOR,
                    timeToLive, TimeUnit.SECONDS);
        });
    }

    private long determineTimeToLiveForCache(Ehcache cache) {
        long timeToLive = cache.getCacheConfiguration().getTimeToLiveSeconds();
        if (timeToLive <= 0) {
            throw new IllegalStateException("Redis-aware cache " + cache.getName()
                    + " has an infinite/undefined time-to-live but this class requires a finite time-to-live value; "
                    + "this should NEVER happen!");
        }
        return timeToLive;
    }

    @Override
    public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
        removeCacheKeyPlaceholderFromRedis(cache, element);
    }

    @Override
    public void notifyElementExpired(Ehcache cache, Element element) {
        removeCacheKeyPlaceholderFromRedis(cache, element);
    }

    @Override
    public void notifyElementEvicted(Ehcache cache, Element element) {
        removeCacheKeyPlaceholderFromRedis(cache, element);
    }

    private void removeCacheKeyPlaceholderFromRedis(Ehcache cache, Element element) {
        String cacheName = cache.getName();
        String localCacheKey = Objects.toString(element.getObjectKey());
        
        redisExecutor.execute(() -> {
            String redisCacheKey = CuRedisUtils.buildRedisCacheKey(cacheName, localCacheKey);
            if (LOG.isDebugEnabled()) {
                LOG.debug("removeCacheKeyPlaceholderFromRedis, Deleting placeholder value in redis for key: "
                        + redisCacheKey);
            }
            redisTemplate.delete(redisCacheKey);
        });
    }

    @Override
    public void notifyRemoveAll(Ehcache cache) {
        String cacheName = cache.getName();
        redisExecutor.execute(() -> removeAllCacheKeysInRedisForNamedCache(cacheName));
    }

    private void removeAllCacheKeysInRedisForNamedCache(String cacheName) {
        List<String> keysToDelete = getAllKeysInRedisForNamedCache(cacheName);
        if (LOG.isDebugEnabled()) {
            LOG.debug("removeAllCacheKeysInRedisForNamedCache, Deleting all placeholder values for keys: "
                    + keysToDelete);
        }
        
        for (int startIndex = 0; startIndex < keysToDelete.size(); startIndex += KEY_BATCH_SIZE) {
            int endIndex = Math.min(startIndex + KEY_BATCH_SIZE, keysToDelete.size());
            List<String> keysSubList = keysToDelete.subList(startIndex, endIndex);
            redisTemplate.delete(keysSubList);
        }
    }

    private List<String> getAllKeysInRedisForNamedCache(String cacheName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getAllKeysInRedisForNamedCache, Searching for all keys related to cache name: " + cacheName);
        }
        
        return redisTemplate.execute((RedisConnection connection) -> {
            ScanOptions scanOptions = buildScanOptionsForBulkKeySearch(cacheName);
            Stream.Builder<String> keys = Stream.builder();
            try (
                Cursor<byte[]> keyCursor = connection.scan(scanOptions);
            ) {
                while (keyCursor.hasNext()) {
                    byte[] key = keyCursor.next();
                    keys.add(new String(key, StandardCharsets.UTF_8));
                }
            }
            return keys.build()
                    .distinct()
                    .collect(Collectors.toUnmodifiableList());
        });
    }

    private ScanOptions buildScanOptionsForBulkKeySearch(String cacheName) {
        String keyPattern = buildKeyPrefixSearchPattern(cacheName);
        return KeyScanOptions.scanOptions(DataType.STRING)
                .count(KEY_BATCH_SIZE)
                .match(keyPattern)
                .build();
    }

    private String buildKeyPrefixSearchPattern(String cacheName) {
        return StringUtils.join(CuCacheConstants.REDIS_KEY_PREFIX_START_CHARS, cacheName,
                CuCacheConstants.REDIS_KEY_PREFIX_END_CHARS, KFSConstants.WILDCARD_CHARACTER);
    }

    @Override
    public void destroy() throws Exception {
        dispose();
    }

    @Override
    public void dispose() {
        redisExecutor.shutdown();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("This listener does not support cloning");
    }

}
