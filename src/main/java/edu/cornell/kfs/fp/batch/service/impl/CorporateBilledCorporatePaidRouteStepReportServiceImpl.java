package edu.cornell.kfs.fp.batch.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.batch.service.CorporateBilledCorporatePaidRouteStepReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class CorporateBilledCorporatePaidRouteStepReportServiceImpl implements CorporateBilledCorporatePaidRouteStepReportService {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CorporateBilledCorporatePaidRouteStepReportServiceImpl.class);
    
    protected ConfigurationService configurationService;
    protected ReportWriterService reportWriterService;

    @Override
    public void createReport(int totalCBCPSavedDocumentCount, List<String> successfullyRoutedDocuments, Map<String, ValidationException> documentErrors) {
        LOG.info("reportDocumentErrors, number of errors: " + documentErrors.size());
        reportWriterService.initialize();
        buildHeaderSection(totalCBCPSavedDocumentCount, successfullyRoutedDocuments.size(), documentErrors.size());
        buildErrorSection(documentErrors);
        reportWriterService.destroy();

    }
    
    protected void buildHeaderSection(int totalCbcpCount, int totalRoutedCount, int totalErrorCount) {
        reportWriterService.writeSubTitle("******* CBCP Job Summary *******");
        reportWriterService.writeFormattedMessageLine("Total number of CBCP documents in saved status: " + totalCbcpCount);
        reportWriterService.writeFormattedMessageLine("Total number of CBCP documents routed: " + totalRoutedCount);
        reportWriterService.writeFormattedMessageLine("Total number of CBCP documents that failed to route: " + totalErrorCount);
        reportWriterService.writeNewLines(1);
    }

    protected void buildErrorSection(Map<String, ValidationException> documentErrors) {
        reportWriterService.writeSubTitle("******* Documents that failed to route *******");
        if (documentErrors.size() > 0) {
            for (String documentNumber : documentErrors.keySet()) {
                ValidationException error = documentErrors.get(documentNumber);
                LOG.info("reportDocumentErrors: document number: " + documentNumber + " error: " + error);
                reportWriterService.writeNewLines(1);
                reportWriterService.writeFormattedMessageLine("Document number " + documentNumber + " had the following wrror: " + buildValidationErrorMessage(error));
            } 
        } else {
            reportWriterService.writeNewLines(1);
            reportWriterService.writeFormattedMessageLine("No documents in erorr");
        }
    }
    
    protected String buildValidationErrorMessage(ValidationException validationException) {
        try {
            Map<String, AutoPopulatingList<ErrorMessage>> errorMessages = GlobalVariables.getMessageMap().getErrorMessages();
            return errorMessages.values().stream()
                    .flatMap(List::stream)
                    .map(this::buildValidationErrorMessageForSingleError)
                    .collect(Collectors.joining(
                            KFSConstants.NEWLINE, validationException.getMessage() + KFSConstants.NEWLINE, KFSConstants.NEWLINE));
        } catch (RuntimeException e) {
            LOG.error("buildValidationErrorMessage: Could not build validation error message", e);
            return CuFPConstants.ALTERNATE_BASE_VALIDATION_ERROR_MESSAGE;
        }
    }

    protected String buildValidationErrorMessageForSingleError(ErrorMessage errorMessage) {
        String errorMessageString = configurationService.getPropertyValueAsString(errorMessage.getErrorKey());
        if (StringUtils.isBlank(errorMessageString)) {
            throw new RuntimeException("Cannot find error message for key: " + errorMessage.getErrorKey());
        }
        
        Object[] messageParameters = (Object[]) errorMessage.getMessageParameters();
        if (messageParameters != null && messageParameters.length > 0) {
            return MessageFormat.format(errorMessageString, messageParameters);
        } else {
            return errorMessageString;
        }
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

}
