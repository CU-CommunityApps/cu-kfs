package edu.cornell.kfs.tax.util;

import java.text.MessageFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import edu.cornell.kfs.tax.CUTaxConstants;

public final class TaxUtils {

    public static String build1099BoxNumberMappingKey(String formType, String boxNumber) {
        String convertedFormType = StringUtils.defaultIfBlank(formType, CUTaxConstants.NULL_1099_MAPPING);
        String convertedBoxNumber = StringUtils.defaultIfBlank(boxNumber, CUTaxConstants.NULL_1099_MAPPING);
        String boxNumberMappingKey = MessageFormat.format(CUTaxConstants.TAX_1099_BOX_MAPPING_KEY_FORMAT,
                convertedFormType, convertedBoxNumber);
        return StringUtils.upperCase(boxNumberMappingKey, Locale.US);
    }

    public static boolean is1099BoxNumberMappingKeyFormattedProperly(String boxNumberMappingKey) {
        return StringUtils.isNotBlank(boxNumberMappingKey)
                && CUTaxConstants.TAX_1099_BOX_MAPPING_KEY_PATTERN.matcher(boxNumberMappingKey).matches();
    }

    private TaxUtils() {
        throw new UnsupportedOperationException("do not call");
    }

}
