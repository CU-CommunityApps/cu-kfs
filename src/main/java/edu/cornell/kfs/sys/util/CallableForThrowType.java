package edu.cornell.kfs.sys.util;

/**
 * Helper functional interface that is similar to java.util.concurrent.Callable in base Java,
 * except that it allows for specifying what type of Throwable it can throw.
 * 
 * TODO: This interface is nearly the same as the FailableCallable interface
 * from commons-lang3 version 3.9 and later. Once we upgrade to commons-lang3 3.9+,
 * remove this interface and replace its usage with FailableCallable instead.
 */
@FunctionalInterface
public interface CallableForThrowType<R, T extends Throwable> {
    R call() throws T;
}
