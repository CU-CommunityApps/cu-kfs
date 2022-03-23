package edu.cornell.kfs.sys.cache;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import io.lettuce.core.KeyScanArgs;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import net.sf.ehcache.Ehcache;

public class CuRedisAwareLocalCache extends CuDefaultLocalCache {

    private static final Logger LOG = LogManager.getLogger();

    private static final int KEY_DELETE_BATCH_SIZE = 50;
    private static final long KEY_DELETE_AWAIT_SECONDS = 60L;

    private final CuRedisConnectionManager connectionManager;
    private final long expirationSeconds;

    public CuRedisAwareLocalCache(String cacheName, Ehcache localCache, CuRedisConnectionManager connectionManager,
            long expirationSeconds) {
        super(cacheName, localCache);
        Objects.requireNonNull(connectionManager, "connectionManager cannot be null");
        this.connectionManager = connectionManager;
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    protected void notifyOfNewCacheEntryQuietly(Object key) {
        try {
            connectionManager.executeRedisTaskAsynchronously(this::addExpirableKeyPlaceholderToRedisIfAbsent, key);
        } catch (Exception e) {
            LOG.error("notifyOfNewCacheEntryQuietly, Could not schedule call to update Redis cache entry", e);
        }
    }

    private void addExpirableKeyPlaceholderToRedisIfAbsent(
            StatefulRedisConnection<String, String> redisConnection, Object key) {
        String redisKey = convertToRedisKey(key);
        SetArgs argsNotExistsAndExpireAfterDuration = SetArgs.Builder.nx().ex(expirationSeconds);
        if (LOG.isDebugEnabled()) {
            LOG.debug("addExpirableKeyPlaceholderToRedisIfAbsent, Adding placeholder for key: " + redisKey);
        }
        redisConnection.sync().set(redisKey, KFSConstants.ACTIVE_INDICATOR, argsNotExistsAndExpireAfterDuration);
    }

    @Override
    protected void notifyOfDeletedCacheEntryQuietly(Object key) {
        try {
            connectionManager.executeRedisTaskAsynchronously(this::deleteKeyPlaceholderFromRedisIfPresent, key);
        } catch (Exception e) {
            LOG.error("notifyOfDeletedCacheEntryQuietly, Could not schedule call to delete Redis cache entry", e);
        }
    }

    private void deleteKeyPlaceholderFromRedisIfPresent(
            StatefulRedisConnection<String, String> redisConnection, Object key) {
        String redisKey = convertToRedisKey(key);
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteKeyPlaceholderFromRedisIfPresent, Deleting placeholder for key: " + redisKey);
        }
        redisConnection.sync().del(redisKey);
    }

    @Override
    protected void notifyOfClearedCacheQuietly() {
        try {
            connectionManager.executeRedisTaskAsynchronously(this::deleteKeyPlaceholdersInRedisForCurrentCache);
        } catch (Exception e) {
            LOG.error("notifyOfClearedCacheQuietly, Could not schedule call to clear Redis cache", e);
        }
    }

    private void deleteKeyPlaceholdersInRedisForCurrentCache(StatefulRedisConnection<String, String> redisConnection) {
        String[] keysToDelete = getKeysToDelete(redisConnection.sync());
        long startTimeMillis = 0L;
        if (LOG.isDebugEnabled()) {
            LOG.debug("deleteKeyPlaceholdersInRedisForCurrentCache, Deleting placeholders for keys: "
                    + Arrays.toString(keysToDelete));
            startTimeMillis = System.currentTimeMillis();
        }
        
        RedisAsyncCommands<String, String> asyncCommands = redisConnection.async();
        Stream.Builder<RedisFuture<?>> asyncCalls = Stream.builder();
        for (int startIndex = 0; startIndex < keysToDelete.length; startIndex += KEY_DELETE_BATCH_SIZE) {
            int endIndex = Math.min(startIndex + KEY_DELETE_BATCH_SIZE, keysToDelete.length);
            String[] keysSubArray = Arrays.copyOfRange(keysToDelete, startIndex, endIndex);
            asyncCalls.add(asyncCommands.del(keysSubArray));
        }
        
        RedisFuture<?>[] triggeredCalls = asyncCalls.build().toArray(RedisFuture[]::new);
        if (LettuceFutures.awaitAll(KEY_DELETE_AWAIT_SECONDS, TimeUnit.SECONDS, triggeredCalls)) {
            if (LOG.isDebugEnabled()) {
                long elapsedTimeMillis = System.currentTimeMillis() - startTimeMillis;
                LOG.debug("deleteKeyPlaceholdersInRedisForCurrentCache, Successfully deleted " + keysToDelete.length
                        + " keys in " + elapsedTimeMillis + " milliseconds");
            }
        } else {
            LOG.warn("deleteKeyPlaceholdersInRedisForCurrentCache, Operation for deleting " + keysToDelete.length
                    + " keys took longer than " + KEY_DELETE_AWAIT_SECONDS
                    + " seconds; the bulk key deletion may still be in progress.");
        }
    }

    private String[] getKeysToDelete(RedisCommands<String, String> syncCommands) {
        Stream.Builder<String> keysToDelete = Stream.builder();
        KeyScanArgs scanArgs = KeyScanArgs.Builder
                .matches(keyPrefix + KFSConstants.WILDCARD_CHARACTER)
                .limit(KEY_DELETE_BATCH_SIZE);
        KeyScanCursor<String> cursor = null;
        
        do {
            cursor = (cursor != null) ? syncCommands.scan(cursor, scanArgs) : syncCommands.scan(scanArgs);
            List<String> keys = cursor.getKeys();
            if (CollectionUtils.isNotEmpty(keys)) {
                for (String key : keys) {
                    keysToDelete.add(key);
                }
            }
        } while (!cursor.isFinished());
        
        return keysToDelete.build()
                .distinct()
                .toArray(String[]::new);
    }

    private String convertToRedisKey(Object key) {
        return keyPrefix + Objects.toString(key);
    }

}
