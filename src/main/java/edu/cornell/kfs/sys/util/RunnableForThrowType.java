package edu.cornell.kfs.sys.util;

/**
 * Helper functional interface that is similar to java.lang.Runnable in base Java,
 * except that it allows for specifying what type of Throwable it can throw.
 * 
 * TODO: This interface is nearly the same as the FailableRunnable interface
 * from commons-lang3 version 3.9 and later. Once we upgrade to commons-lang3 3.9+,
 * remove this interface and replace its usage with FailableRunnable instead.
 */
@FunctionalInterface
public interface RunnableForThrowType<T extends Throwable> {
    void run() throws T;
}
