package edu.cornell.kfs.vnd.util;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public final class CuVendorUtils {

    public static String formatVendorNumber(
            final Integer vendorHeaderGeneratedIdentifier, final Integer vendorDetailAssignedIdentifier) {
        if (vendorHeaderGeneratedIdentifier != null && vendorDetailAssignedIdentifier != null) {
            return StringUtils.join(vendorHeaderGeneratedIdentifier,
                    KFSConstants.DASH, vendorDetailAssignedIdentifier);
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

}
