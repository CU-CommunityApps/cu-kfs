package edu.cornell.kfs.pmw.batch.service;

import java.text.SimpleDateFormat;
import java.util.Locale;

public interface PaymentWorksDataTransformationService {
    
    static final SimpleDateFormat PROCESSING_TIMESTAMP_SQL_FORMATTER = new SimpleDateFormat("dd-MMM-yy", Locale.US);
    static final SimpleDateFormat PROCESSING_TIMESTAMP_REPORT_FORMATTER = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    String convertPmwTinTypeCodeToPmwTinTypeText(String pmwTinTypeCodeToConvert);
    
}
