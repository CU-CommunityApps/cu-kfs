package edu.cornell.kfs.sys.cache;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.cache.Cache;
import org.springframework.cache.support.NullValue;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import io.lettuce.core.KeyScanArgs;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.sync.RedisCommands;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

public class CuRedisCache implements Cache {

    private static final String KEY_PREFIX_FORMAT = "'{'{0}'}|'";
    private static final int KEY_DELETE_BATCH_SIZE = 50;
    private static final Class<?> NULL_CLASS_TO_SKIP_TYPE_CHECK = null;

    private final String cacheName;
    private final Ehcache localCache;
    private final CuRedisConnectionHandler connectionHandler;
    private final int expirationSeconds;
    private final String keyPrefix;
    private final RedisSerializer<Object> valueSerializer;

    public CuRedisCache(String cacheName, Ehcache localCache, CuRedisConnectionHandler connectionHandler,
            int expirationSeconds) {
        this.cacheName = cacheName;
        this.localCache = localCache;
        this.connectionHandler = connectionHandler;
        this.expirationSeconds = expirationSeconds;
        this.keyPrefix = MessageFormat.format(KEY_PREFIX_FORMAT, cacheName);
        this.valueSerializer = new JdkSerializationRedisSerializer();
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }

    @Override
    public String getName() {
        return cacheName;
    }

    @Override
    public Object getNativeCache() {
        return connectionHandler;
    }

    @Override
    public ValueWrapper get(Object key) {
        Element cacheElement = getEntryFromLocalCacheOrElse(
                (String) key, NULL_CLASS_TO_SKIP_TYPE_CHECK, this::getValueFromRedisIfPresent);
        return convertElementToValueWrapperIfPresent(cacheElement);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Class<T> type) {
        Element cacheElement = getEntryFromLocalCacheOrElse((String) key, type, this::getValueFromRedisIfPresent);
        return (T) getValueFromElementIfPresent(cacheElement);
    }

    private Element getValueFromRedisIfPresent(String key, Class<?> type) {
        return connectionHandler.doRedisAction(redisConnection -> {
            String redisKey = convertToRedisKey(key);
            byte[] valueAsBytes = redisConnection.sync().get(redisKey);
            Object value = (valueAsBytes != null) ? valueSerializer.deserialize(valueAsBytes) : null;
            if (value != null) {
                if (type != null) {
                    type.cast(value);
                }
                return putInLocalCache(key, value);
            } else {
                return null;
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Element cacheElement = getEntryFromLocalCacheOrElse((String) key, valueLoader,
                this::getValueFromRedisOrInitializeFromLoader);
        return (T) getValueFromElementIfPresent(cacheElement);
    }

    private Element getValueFromRedisOrInitializeFromLoader(String key, Callable<?> valueLoader) {
        return connectionHandler.doRedisAction(redisConnection -> {
            String redisKey = convertToRedisKey(key);
            byte[] valueAsBytes = redisConnection.sync().get(redisKey);
            Object cacheValue;
            if (valueAsBytes != null) {
                cacheValue = valueSerializer.deserialize(valueAsBytes);
            } else {
                Object loadedValue = getValueFromLoader(key, valueLoader);
                cacheValue = convertNullValueIfNecessary(loadedValue);
                valueAsBytes = valueSerializer.serialize(cacheValue);
                SetArgs argsExpireAfter = SetArgs.Builder.ex(expirationSeconds);
                redisConnection.sync().set(redisKey, valueAsBytes, argsExpireAfter);
            }
            return putInLocalCache(key, cacheValue);
        });
    }

    private <T> T getValueFromLoader(String key, Callable<T> valueLoader) {
        try {
            return valueLoader.call();
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        }
    }

    @Override
    public void put(Object key, Object value) {
        potentiallyUpdateCacheEntry((String) key, value, false, this::setValueLocallyAndInRedis);
    }

    private Element setValueLocallyAndInRedis(String key, Object value) {
        return connectionHandler.doRedisAction(redisConnection -> {
            String redisKey = convertToRedisKey(key);
            Object nullSafeValue = convertNullValueIfNecessary(value);
            byte[] valueAsBytes = valueSerializer.serialize(nullSafeValue);
            SetArgs argsExpireAfterDuration = SetArgs.Builder.ex(expirationSeconds);
            redisConnection.sync().set(redisKey, valueAsBytes, argsExpireAfterDuration);
            return putInLocalCache(key, nullSafeValue);
        });
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        Element cacheElement = getEntryFromLocalCacheOrElse((String) key, value, this::setValueInRedisIfAbsent);
        return convertElementToValueWrapperIfPresent(cacheElement);
    }

    private Element setValueInRedisIfAbsent(String key, Object value) {
        return connectionHandler.doRedisAction(redisConnection -> {
            String redisKey = convertToRedisKey(key);
            Object nullSafeValue = convertNullValueIfNecessary(value);
            byte[] valueAsBytes = valueSerializer.serialize(nullSafeValue);
            SetArgs argsNotExistsAndExpireAfterDuration = SetArgs.Builder.nx().ex(expirationSeconds);
            byte[] existingValueAsBytes = redisConnection.sync().setGet(
                    redisKey, valueAsBytes, argsNotExistsAndExpireAfterDuration);
            
            boolean redisHasExistingValue = (existingValueAsBytes != null);
            Object valueForLocalCache = redisHasExistingValue
                    ? valueSerializer.deserialize(existingValueAsBytes) : nullSafeValue;
            Element newEntry = putInLocalCache(key, valueForLocalCache);
            return redisHasExistingValue ? newEntry : null;
        });
    }

    @Override
    public void evict(Object key) {
        evict((String) key, true);
    }

    public void evictFromLocalCacheOnly(Object key) {
        evict((String) key, false);
    }

    private void evict(String key, boolean evictFromRedis) {
        localCache.acquireWriteLockOnKey(key);
        try {
            localCache.remove(key);
            if (evictFromRedis) {
                deleteEntryInRedis(key);
            }
        } finally {
            localCache.releaseWriteLockOnKey(key);
        }
    }

    private void deleteEntryInRedis(String key) {
        connectionHandler.doRedisAction(redisConnection -> {
            String redisKey = convertToRedisKey(key);
            redisConnection.sync().del(redisKey);
            return null;
        });
    }

    @Override
    public void clear() {
        localCache.removeAll();
        deleteEntriesInRedisForCurrentCache();
    }

    public void clearLocalCacheOnly() {
        localCache.removeAll();
    }

    private void deleteEntriesInRedisForCurrentCache() {
        connectionHandler.doRedisAction(redisConnection -> {
            RedisCommands<String, byte[]> syncCommands = redisConnection.sync();
            List<String> keys = getKeysToDelete(syncCommands);
            for (int startIndex = 0; startIndex < keys.size(); startIndex += KEY_DELETE_BATCH_SIZE) {
                int endIndex = Math.min(startIndex + KEY_DELETE_BATCH_SIZE, keys.size());
                List<String> keysSubList = keys.subList(startIndex, endIndex);
                String[] keysArray = keysSubList.toArray(String[]::new);
                syncCommands.del(keysArray);
            }
            return null;
        });
    }

    private List<String> getKeysToDelete(RedisCommands<String, byte[]> syncCommands) {
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
                .collect(Collectors.toUnmodifiableList());
    }

    private <T> Element getEntryFromLocalCacheOrElse(String key, T extraArg,
            BiFunction<String, T, Element> cacheProcessor) {
        localCache.acquireReadLockOnKey(key);
        try {
            Element cacheElement = localCache.get(key);
            if (cacheElement != null) {
                return cacheElement;
            }
        } finally {
            localCache.releaseReadLockOnKey(key);
        }
        return potentiallyUpdateCacheEntry(key, extraArg, true, cacheProcessor);
    }

    private <T> Element potentiallyUpdateCacheEntry(String key, T extraArg, boolean checkForLocalValueFirst,
            BiFunction<String, T, Element> cacheProcessor) {
        localCache.acquireWriteLockOnKey(key);
        try {
            if (checkForLocalValueFirst) {
                Element cacheElement = localCache.get(key);
                if (cacheElement != null) {
                    return cacheElement;
                }
            }
            return cacheProcessor.apply(key, extraArg);
        } finally {
            localCache.releaseWriteLockOnKey(key);
        }
    }

    private String convertToRedisKey(String key) {
        String stringKey = (String) key;
        if (StringUtils.isBlank(stringKey)) {
            throw new IllegalArgumentException("Blank keys are not supported");
        }
        return keyPrefix + stringKey;
    }

    private Element putInLocalCache(String key, Object value) {
        Element cacheEntry = new Element(key, value, 0, expirationSeconds);
        localCache.put(cacheEntry);
        return cacheEntry;
    }

    private Object getValueFromElementIfPresent(Element cacheElement) {
        return (cacheElement != null) ? convertBackToNullIfNecessary(cacheElement.getObjectValue()) : null;
    }

    private ValueWrapper convertElementToValueWrapperIfPresent(Element cacheElement) {
        if (cacheElement != null) {
            Object value = convertBackToNullIfNecessary(cacheElement.getObjectValue());
            return new SimpleValueWrapper(value);
        } else {
            return null;
        }
    }

    private Object convertNullValueIfNecessary(Object value) {
        return (value != null) ? value : NullValue.INSTANCE;
    }

    private Object convertBackToNullIfNecessary(Object value) {
        return (value instanceof NullValue) ? null : value;
    }

}
