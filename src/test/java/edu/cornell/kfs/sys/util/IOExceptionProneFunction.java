package edu.cornell.kfs.sys.util;

import java.io.IOException;

@FunctionalInterface
public interface IOExceptionProneFunction<T, R> {

    R apply(T inputValue) throws IOException;

}
