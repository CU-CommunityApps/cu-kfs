package edu.cornell.kfs.sys.cache;

public final class CuCacheConstants {

    public static final String KEY_PREFIX_FORMAT = "'<<'{0}'>>|'";
    public static final String KEY_PREFIX_END_CHARS = ">>|";

    public static final String REDIS_DEFAULT_PING_RESPONSE = "PONG";
    public static final String REDIS_MESSAGE_TYPE_INVALIDATE = "invalidate";
    public static final int REDIS_KEY_DELETE_BATCH_SIZE = 50;
    public static final long REDIS_KEY_DELETE_AWAIT_SECONDS = 60L;

}
