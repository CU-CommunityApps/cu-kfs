package edu.cornell.kfs.sys.util;

import java.util.LinkedList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Helper class that allows for storing a particular object while performing an operation, and where
 * the stored value only exists for the duration of the operation and is then automatically cleaned up.
 * This class is especially helpful for implementing a short-lived cache.
 * 
 * Each thread has its own separate stored object, so instances of this class are thread-safe as long as
 * the given Supplier and Consumer do not perform non-thread-safe tasks. In addition, each thread has
 * its own stack of values, so the operation can call the helper wrapping method again
 * and have the sub-operation use its own sub-value.
 * 
 * The temporary object will get created and managed by the performTaskWithNewLocalObject() method.
 * To access the temporary object within the lambda code (or descendant code) that gets passed
 * to the method noted above, call the getRequiredLocalObject() method.
 * 
 * This setup is similar to that from the financials GlobalVariables class and its helper
 * doInNewGlobalVariables() method, except that this class has tighter control and visibility over
 * its items and also has more flexible exception handling.
 */
public final class StackedThreadLocalObject<E> {
    private static final Logger LOG = LogManager.getLogger();

    private final ThreadLocal<LinkedList<E>> objectStacks;
    private final Supplier<? extends E> objectCreator;
    private final Consumer<? super E> objectCleanupTask;
    private final String missingObjectMessage;

    public static <V> StackedThreadLocalObject<V> withLifecycleTasksAndMissingObjectMessage(
            Supplier<? extends V> objectCreator, Consumer<? super V> objectCleanupTask,
            String missingObjectMessage) {
        return new StackedThreadLocalObject<>(objectCreator, objectCleanupTask, missingObjectMessage);
    }

    private StackedThreadLocalObject(Supplier<? extends E> objectCreator, Consumer<? super E> objectCleanupTask,
            String missingObjectMessage) {
        this.objectStacks = ThreadLocal.withInitial(LinkedList::new);
        this.objectCreator = Objects.requireNonNull(objectCreator);
        this.objectCleanupTask = Objects.requireNonNull(objectCleanupTask);
        this.missingObjectMessage = requireNonBlankMessage(missingObjectMessage);
    }

    private String requireNonBlankMessage(String message) {
        if (StringUtils.isBlank(message)) {
            throw new IllegalArgumentException("Missing-object message cannot be blank");
        }
        return message;
    }

    public final <R, T extends Throwable> R performTaskWithNewLocalObject(
            CallableForThrowType<R, T> callable) throws T {
        E localObject = null;
        boolean objectAddedToStack = false;
        
        try {
            localObject = Objects.requireNonNull(objectCreator.get(), "The new object cannot be null");
            getLocalObjectStack().addFirst(localObject);
            objectAddedToStack = true;
            return callable.call();
        } catch (Throwable t) {
            if (objectAddedToStack) {
                LOG.error("performTaskWithNewLocalObject: The operation encountered an error", t);
            } else {
                LOG.error("performTaskWithNewLocalObject: An error was thrown during setup", t);
            }
            throw t;
        } finally {
            try {
                if (objectAddedToStack) {
                    getLocalObjectStack().removeFirst();
                }
                if (localObject != null) {
                    objectCleanupTask.accept(localObject);
                }
            } catch (RuntimeException e) {
                LOG.error("performTaskWithNewLocalObject: An error was thrown during cleanup", e);
                throw e;
            }
        }
    }

    public final E getRequiredLocalObject() {
        E localObject = getLocalObjectStack().peekFirst();
        if (localObject == null) {
            throw new IllegalStateException(missingObjectMessage);
        }
        return localObject;
    }

    private LinkedList<E> getLocalObjectStack() {
        return objectStacks.get();
    }

}
