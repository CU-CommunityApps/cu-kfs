package edu.cornell.kfs.fp.batch.service;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.krad.exception.ValidationException;

public interface CorporateBilledCorporatePaidRouteStepReportService {
    
    void createReport(int totalCBCPSavedDocumentCount, List<String> successfullyRoutedDocuments, Map<String, String> documentErrors);
    
    String buildValidationErrorMessage(ValidationException validationException);
}
