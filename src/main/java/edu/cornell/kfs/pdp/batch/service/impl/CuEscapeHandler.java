package edu.cornell.kfs.pdp.batch.service.impl;

import java.util.Map;

import com.prowidesoftware.swift.model.mx.EscapeHandler;

/*
 * CU-specific EscapeHandler implementation that is similar to Prowide's DefaultEscapeHandler,
 * but with some important differences:
 * 
 * # Quotes will always be escaped, not just those within XML attributes. Also, this class
 *       fixes a bug in earlier pw-iso20022 versions that could cause duplication of quotes.
 * 
 * # Apostrophes will also be escaped.
 * 
 * # Characters outside the US-ASCII range will *not* be escaped.
 * 
 * This class's logic has been derived from the following Prowide file:
 * 
 * https://github.com/prowide/prowide-iso20022/blob/SRU2021-9.2.7/iso20022-core/
 *         src/main/java/com/prowidesoftware/swift/model/mx/DefaultEscapeHandler.java
 */
public class CuEscapeHandler implements EscapeHandler {

    private static final Map<Character, String> ESCAPED_CHARS = Map.ofEntries(
            Map.entry('&', "&amp;"),
            Map.entry('<', "&lt;"),
            Map.entry('>', "&gt;"),
            Map.entry('"', "&quot;"),
            Map.entry('\'', "&apos;")
    );

    @Override
    public String escape(char[] arr, boolean isAttribute) {
        StringBuilder escapedContent = new StringBuilder(arr.length);
        for (char currentChar : arr) {
            String explicitlyEscapedValue = ESCAPED_CHARS.get(currentChar);
            if (explicitlyEscapedValue != null) {
                escapedContent.append(explicitlyEscapedValue);
            } else {
                escapedContent.append(currentChar);
            }
        }
        return escapedContent.toString();
    }

}
