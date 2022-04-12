package edu.cornell.kfs.sys.cache;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.DisposableBean;

public final class RedisEventListenerLazyInitProxy implements RedisEventListener, DisposableBean {

    private final AtomicReference<RedisEventListener> lazyInitializedListener;

    public RedisEventListenerLazyInitProxy() {
        this.lazyInitializedListener = new AtomicReference<>(NoOpListener.INSTANCE);
    }

    public void setActualEventListener(RedisEventListener actualEventListener) {
        if (!lazyInitializedListener.compareAndSet(NoOpListener.INSTANCE, actualEventListener)) {
            throw new IllegalStateException("The actual event listener has already been configured for this proxy");
        }
    }

    @Override
    public void notifyConnectionEstablished() {
        lazyInitializedListener.get().notifyConnectionEstablished();
    }

    @Override
    public void notifyConnectionClosed() {
        lazyInitializedListener.get().notifyConnectionClosed();
    }

    @Override
    public void notifyCacheKeysInvalidated(List<String> keys) {
        lazyInitializedListener.get().notifyCacheKeysInvalidated(keys);
    }

    @Override
    public void destroy() throws Exception {
        lazyInitializedListener.set(NoOpListener.INSTANCE);
    }

    private static final class NoOpListener implements RedisEventListener {
        private static final NoOpListener INSTANCE = new NoOpListener();

        @Override
        public void notifyConnectionEstablished() {}

        @Override
        public void notifyConnectionClosed() {}

        @Override
        public void notifyCacheKeysInvalidated(List<String> keys) {}
    }

}
