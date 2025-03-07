package edu.cornell.kfs.tax.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import edu.cornell.kfs.tax.CUTaxConstants;

public final class TaxUtils {

    public static String build1099BoxNumberMappingKey(String formType, String boxNumber) {
        String convertedFormType = StringUtils.defaultIfBlank(formType, CUTaxConstants.TAX_1099_UNKNOWN_FORM_TYPE);
        String convertedBoxNumber = StringUtils.defaultIfBlank(boxNumber, CUTaxConstants.TAX_1099_UNKNOWN_BOX_KEY);
        String boxNumberMappingKey = MessageFormat.format(CUTaxConstants.TAX_1099_BOX_MAPPING_KEY_FORMAT,
                convertedFormType, convertedBoxNumber);
        return StringUtils.upperCase(boxNumberMappingKey, Locale.US);
    }

    public static Pair<String, String> build1099FormTypeAndBoxNumberPair(String boxNumberMappingKey) {
        if (StringUtils.isBlank(boxNumberMappingKey)) {
            return CUTaxConstants.TAX_1099_UNKNOWN_BOX_COMPOSITE_KEY;
        }
        String upperCaseKey = StringUtils.upperCase(boxNumberMappingKey, Locale.US);
        Matcher keyMatcher = CUTaxConstants.TAX_1099_BOX_MAPPING_KEY_PATTERN.matcher(upperCaseKey);
        if (keyMatcher.matches()) {
            String formType = keyMatcher.group(1);
            String boxNumber = keyMatcher.group(2);
            return Pair.of(formType, boxNumber);
        } else {
            throw new IllegalArgumentException("Box number mapping key is malformed: " + boxNumberMappingKey);
        }
    }

    public static boolean is1099BoxNumberMappingKeyFormattedProperly(String boxNumberMappingKey) {
        return StringUtils.isNotBlank(boxNumberMappingKey)
                && CUTaxConstants.TAX_1099_BOX_MAPPING_KEY_PATTERN.matcher(boxNumberMappingKey).matches();
    }

    public static java.util.Date copyDate(final java.util.Date value) {
        return new java.util.Date(value.getTime());
    }

    public static java.sql.Date copyDate(final java.sql.Date value) {
        return new java.sql.Date(value.getTime());
    }

    private TaxUtils() {
        throw new UnsupportedOperationException("do not call");
    }

}
