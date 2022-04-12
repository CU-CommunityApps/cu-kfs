package edu.cornell.kfs.sys.cache;

import java.util.List;

public interface RedisEventListener {

    void notifyConnectionEstablished();

    void notifyConnectionClosed();

    void notifyCacheKeysInvalidated(List<String> keys);

}
