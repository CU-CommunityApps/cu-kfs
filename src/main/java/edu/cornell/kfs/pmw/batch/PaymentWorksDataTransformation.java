package edu.cornell.kfs.pmw.batch;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksTinType;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;

public final class PaymentWorksDataTransformation {

    private static final SimpleDateFormat PROCESSING_TIMESTAMP_REPORT_FORMATTER = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    private PaymentWorksDataTransformation() {
        throw new UnsupportedOperationException("do not call");
    }

    public static String convertPmwTinTypeCodeToPmwTinTypeText(String pmwTinTypeCodeToConvert) {
        String returnValue = KFSConstants.EMPTY_STRING;
        List<PaymentWorksTinType> matchingValues = Arrays.asList(PaymentWorksConstants.PaymentWorksTinType.values())
                                                         .stream()
                                                         .filter(tinType -> tinType.getPmwCodeAsString().equalsIgnoreCase(pmwTinTypeCodeToConvert))
                                                         .collect(Collectors.toList());
        returnValue = matchingValues.size() == 1 ? matchingValues.get(0).getPmwText() : pmwTinTypeCodeToConvert;
        return returnValue;
    }

    public static String formatReportSubmissionTimeStamp(Timestamp reportSubmissionTimeStamp) {
        if (ObjectUtils.isNotNull(reportSubmissionTimeStamp)) {
            return PROCESSING_TIMESTAMP_REPORT_FORMATTER.format(reportSubmissionTimeStamp);
        } else {
            return KFSConstants.EMPTY_STRING;
        }
    }

    public static String formatVendorNumber(PaymentWorksVendor pmwVendor) {
        return formatVendorNumber(pmwVendor.getKfsVendorHeaderGeneratedIdentifier(), pmwVendor.getKfsVendorDetailAssignedIdentifier());
    }

    public static String formatReportVendorNumber(PaymentWorksBatchReportVendorItem reportItem) {
        return formatVendorNumber(reportItem.getKfsVendorHeaderGeneratedIdentifier(), reportItem.getKfsVendorDetailAssignedIdentifier());
    }

    public static String formatReportVendorLegalName(PaymentWorksBatchReportVendorItem reportItem) {
        return formatVendorName(
                reportItem.getPmwVendorLegelName(), reportItem.getPmwVendorLegelFirstName(), reportItem.getPmwVendorLegelLastName());
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
