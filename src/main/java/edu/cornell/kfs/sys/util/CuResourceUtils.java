package edu.cornell.kfs.sys.util;

public final class CuResourceUtils {

    public static void closeQuietly(AutoCloseable autoCloseable) {
        try {
            autoCloseable.close();
        } catch (Exception e) {
        }
    }

}
