package edu.cornell.kfs.sys.util;

import java.util.Locale;

public final class CemiUtils {

    public static final String convertToBooleanValueForFileExtract(final boolean value) {
        return Boolean.valueOf(value)
                .toString()
                .toUpperCase(Locale.US);
    }

}
