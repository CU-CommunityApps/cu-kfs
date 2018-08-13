package edu.cornell.kfs.pmw.batch;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;

public final class PaymentWorksUtils {

    private PaymentWorksUtils() {
        throw new UnsupportedOperationException("do not call");
    }

    /**
     * Concatenates the vendorLastName and a delimiter and the vendorFirstName fields in a similar manner to the
     * KualiCo base code PRIVATE method org.kuali.kfs.vnd.document.VendorMaintainableImpl.setVendorName which 
     * formats the business object name values just prior to saving the data to the database.
     */
    public static String formatVendorName(String vendorFullName, String vendorFirstName, String vendorLastName) {
        if (StringUtils.isNotBlank(vendorFullName)) {
            return vendorFullName;
        } else {
            return vendorLastName + VendorConstants.NAME_DELIM + vendorFirstName;
        }
    }

    public static String formatVendorNumber(Integer vendorHeaderGeneratedIdentifier, Integer vendorDetailAssignedIdentifier) {
        if (ObjectUtils.isNotNull(vendorHeaderGeneratedIdentifier) || ObjectUtils.isNotNull(vendorDetailAssignedIdentifier)) {
            return defaultToEmptyIfNull(vendorHeaderGeneratedIdentifier)
                    + KFSConstants.DASH + defaultToEmptyIfNull(vendorDetailAssignedIdentifier);
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

    private static String defaultToEmptyIfNull(Integer value) {
        return ObjectUtils.isNotNull(value) ? value.toString() : KFSConstants.EMPTY_STRING;
    }

}
