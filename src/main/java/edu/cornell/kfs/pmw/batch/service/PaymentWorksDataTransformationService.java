package edu.cornell.kfs.pmw.batch.service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;

public interface PaymentWorksDataTransformationService {
    
    static final SimpleDateFormat PROCESSING_TIMESTAMP_REPORT_FORMATTER = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    String convertPmwTinTypeCodeToPmwTinTypeText(String pmwTinTypeCodeToConvert);

    String formatReportSubmissionTimeStamp(Timestamp reportSubmissionTimeStamp);

    String formatReportVendorNumber(PaymentWorksBatchReportVendorItem reportItem);

    String formatReportVendorLegalName(PaymentWorksBatchReportVendorItem reportItem);
}
