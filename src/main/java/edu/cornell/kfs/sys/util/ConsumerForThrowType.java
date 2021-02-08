package edu.cornell.kfs.sys.util;

/**
 * Helper functional interface that is similar to java.util.function.Consumer in base Java,
 * except that it allows for specifying what type of Throwable it can throw.
 * 
 * TODO: This interface is nearly the same as the FailableConsumer interface
 * from commons-lang3 version 3.9 and later. Once we upgrade to commons-lang3 3.9+,
 * remove this interface and replace its usage with FailableConsumer instead.
 */
@FunctionalInterface
public interface ConsumerForThrowType<R, T extends Throwable> {
    void accept(R value) throws T;
}
