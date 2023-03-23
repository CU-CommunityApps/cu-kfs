package edu.cornell.kfs.sys.util;

import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;

public final class CuAssertions {

    private CuAssertions() {
        throw new UnsupportedOperationException("Do not call");
    }

    public static void assertStringEquals(String expected, String actual) {
        Assertions.assertEquals(
                defaultToNullIfBlank(expected), defaultToNullIfBlank(actual));
    }

    public static void assertStringEquals(String expected, String actual, String message) {
        Assertions.assertEquals(
                defaultToNullIfBlank(expected), defaultToNullIfBlank(actual), message);
    }

    public static void assertStringEquals(String expected, String actual, Supplier<String> messageSupplier) {
        Assertions.assertEquals(
                defaultToNullIfBlank(expected), defaultToNullIfBlank(actual), messageSupplier);
    }

    private static String defaultToNullIfBlank(String value) {
        return StringUtils.defaultIfBlank(value, null);
    }

}
