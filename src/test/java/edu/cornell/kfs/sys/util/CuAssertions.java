package edu.cornell.kfs.sys.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

public final class CuAssertions {

    private CuAssertions() {
        throw new UnsupportedOperationException("Do not call");
    }

    public static <T, R> void assertListEquals(List<T> expected, List<R> actual,
            BiConsumer<T, R> listElementAssertion, String listLabel) {
        int expectedSize = CollectionUtils.size(expected);
        assertEquals(expectedSize, CollectionUtils.size(actual), "Wrong number of elements for " + listLabel);
        for (int i = 0; i < expectedSize; i++) {
            T expectedElement = expected.get(i);
            R actualElement = actual.get(i);
            listElementAssertion.accept(expectedElement, actualElement);
        }
    }

    public static void assertDateEquals(ChronoZonedDateTime<?> expected, Date actual, String message) {
        if (expected == null) {
            assertEquals(null, actual, message);
            return;
        }
        Instant expectedInstant = expected.toInstant();
        Instant actualInstant = Instant.ofEpochMilli(actual.getTime());
        assertEquals(expectedInstant, actualInstant, message);
    }

    public static void assertDateEquals(DateTime expected, Date actual, String message) {
        if (expected == null) {
            assertEquals(null, actual, message);
            return;
        }
        DateTime actualAsDateTime = new DateTime(actual.getTime());
        assertEquals(expected, actualAsDateTime, message);
    }

    public static void assertStringEquals(String expected, String actual, String message) {
        assertEquals(defaultToNullIfBlank(expected), defaultToNullIfBlank(actual), message);
    }

    private static String defaultToNullIfBlank(String value) {
        return StringUtils.defaultIfBlank(value, null);
    }

}
