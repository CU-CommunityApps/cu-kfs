package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class CuEscapeHandlerTest {

    static Stream<Arguments> characterEscapingScenarios() {
        return convertStringPairsToActualArguments(
                "", "",
                " ", " ",
                "256", "256",
                "John Doe", "John Doe",
                "John Garc\u00EDa", "John Garc\u00EDa",
                "This is a test!", "This is a test!",
                "JSP (Java Server Pages)" , "JSP (Java Server Pages)",
                "J & K Company", "J &amp; K Company",
                "Payment for someone's travel reimbursement", "Payment for someone&apos;s travel reimbursement",
                "<li> is for list items", "&lt;li&gt; is for list items",
                "The \"best\" deal", "The &quot;best&quot; deal",
                "abcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyz",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZ", "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                "0123456789", "0123456789",
                "`~!@#$%^&*()-_=+[{]}\\|;:'\",<.>/?", "`~!@#$%^&amp;*()-_=+[{]}\\|;:&apos;&quot;,&lt;.&gt;/?"
        );
    }

    private static Stream<Arguments> convertStringPairsToActualArguments(String... flattenedPairs) {
        if (flattenedPairs.length % 2 != 0) {
            throw new IllegalArgumentException("Cannot pass in an odd number of items; the array is expected "
                    + "to contain pairs of strings");
        }
        Stream.Builder<Arguments> args = Stream.builder();
        for (int i = 0; i < flattenedPairs.length; i += 2) {
            String originalStringValue = flattenedPairs[i];
            String expectedEscapedValue = flattenedPairs[i + 1];
            args.add(Arguments.of(originalStringValue, expectedEscapedValue, true));
            args.add(Arguments.of(originalStringValue, expectedEscapedValue, false));
        }
        return args.build();
    }

    @ParameterizedTest
    @MethodSource("characterEscapingScenarios")
    void testEscapeCharacters(String originalStringValue, String expectedEscapedValue, boolean isAttribute) {
        CuEscapeHandler escapeHandler = new CuEscapeHandler();
        String actualEscapedValue = escapeHandler.escape(originalStringValue.toCharArray(), isAttribute);
        assertEquals(expectedEscapedValue, actualEscapedValue, "Wrong string-escaping result");
    }

    @ParameterizedTest
    @ValueSource(booleans = {
            true,
            false
    })
    void testCannotEscapeNullInput(boolean isAttribute) {
        CuEscapeHandler escapeHandler = new CuEscapeHandler();
        assertThrows(RuntimeException.class, () -> escapeHandler.escape(null, isAttribute),
                "The escape handler should have failed to process a null char array");
    }

}
