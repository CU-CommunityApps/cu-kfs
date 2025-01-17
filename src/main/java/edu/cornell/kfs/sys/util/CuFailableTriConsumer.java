package edu.cornell.kfs.sys.util;

/**
 * Apache Commons Lang3 doesn't appear to have a "failable" variant of its TriConsumer interface,
 * so a custom one has been added here for convenience.
 */
@FunctionalInterface
public interface CuFailableTriConsumer<T, U, V, E extends Throwable> {

    void accept(final T firstArg, final U secondArg, final V thirdArg) throws E;

}
