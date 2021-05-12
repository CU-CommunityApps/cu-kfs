package edu.cornell.kfs.fp.batch.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.batch.service.CorporateBilledCorporatePaidRouteStepReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class CorporateBilledCorporatePaidRouteStepReportServiceImpl implements CorporateBilledCorporatePaidRouteStepReportService {
	private static final Logger LOG = LogManager.getLogger(CorporateBilledCorporatePaidRouteStepReportServiceImpl.class);
    
    protected ConfigurationService configurationService;
    protected ReportWriterService reportWriterService;
    protected EmailService emailService;
    protected ConcurBatchUtilityService concurBatchUtilityService;

    @Override
    public void createReport(int totalCBCPSavedDocumentCount, List<String> successfullyRoutedDocuments, Map<String, String> documentErrors) {
        LOG.info("reportDocumentErrors, number of errors: " + documentErrors.size());
        reportWriterService.initialize();
        buildHeaderSection(totalCBCPSavedDocumentCount, successfullyRoutedDocuments.size(), documentErrors.size());
        buildErrorSection(documentErrors);
        reportWriterService.destroy();
    }
    
    protected void buildHeaderSection(int totalCbcpCount, int totalRoutedCount, int totalErrorCount) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.MESSAGE_CBCP_ROUTE_STEP_REPORT_SUPMMARY_SUBHEADER));
        String suummaryDetailFormat = configurationService.getPropertyValueAsString(CuFPKeyConstants.MESSAGE_CBCP_ROUTE_STEP_REPORT_SUPMMARY_DETAIL_LINE);
        reportWriterService.writeFormattedMessageLine(MessageFormat.format(suummaryDetailFormat, "in saved status", totalCbcpCount));
        reportWriterService.writeFormattedMessageLine(MessageFormat.format(suummaryDetailFormat, "routed", totalRoutedCount));
        reportWriterService.writeFormattedMessageLine(MessageFormat.format(suummaryDetailFormat, "failed to route", totalErrorCount));
        reportWriterService.writeNewLines(3);
    }

    protected void buildErrorSection(Map<String, String> documentErrors) {
        reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                CuFPKeyConstants.MESSAGE_CBCP_ROUTE_STEP_REPORT_ERROR_SUBHEADER));
        if (documentErrors.size() > 0) {
            String errorDetailFormat = configurationService.getPropertyValueAsString(CuFPKeyConstants.MESSAGE_CBCP_ROUTE_STEP_REPORT_ERROR_DETAIL_LINE);
            for (String documentNumber : documentErrors.keySet()) {
                String errorMessage = documentErrors.get(documentNumber);
                LOG.info("buildErrorSection, document " + documentNumber + " failed to route due to: " + errorMessage);
                reportWriterService.writeFormattedMessageLine(MessageFormat.format(errorDetailFormat, documentNumber, errorMessage));
            } 
        } else {
            reportWriterService.writeNewLines(1);
            reportWriterService.writeSubTitle(configurationService.getPropertyValueAsString(
                    CuFPKeyConstants.MESSAGE_CBCP_ROUTE_STEP_REPORT_NO_ERRORS));
        }
    }
    
    @Override
    public String buildValidationErrorMessage(ValidationException validationException) {
        try {
            Map<String, AutoPopulatingList<ErrorMessage>> errorMessages = GlobalVariables.getMessageMap().getErrorMessages();
            return errorMessages.values().stream().flatMap(List::stream)
                    .map(this::buildValidationErrorMessageForSingleError)
                    .collect(Collectors.joining(KFSConstants.NEWLINE,
                            validationException.getMessage() + KFSConstants.NEWLINE, KFSConstants.NEWLINE));
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
        
        String[] messageParameters = errorMessage.getMessageParameters();
        if (ArrayUtils.isNotEmpty(messageParameters)) {
            return MessageFormat.format(errorMessageString, messageParameters);
        } else {
            return errorMessageString;
        }
    }
    
    @Override
    public void sendReportEmail(String toAddress, String fromAddress) {
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(fromAddress);
        String subject = reportWriterService.getTitle();
        message.setSubject(subject);
        message.getToAddresses().add(toAddress);
        String body = concurBatchUtilityService.getFileContents(reportWriterService.getReportFile().getAbsolutePath());
        message.setMessage(body);
        
        boolean htmlMessage = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmail, from address: " + fromAddress + "  to address: " + toAddress);
            LOG.debug("sendEmail, the email subject: " + subject);
            LOG.debug("sendEmail, the email budy: " + body);
        }
        try {
            emailService.sendMessage(message, htmlMessage);
        } catch (Exception e) {
            LOG.error("sendEmail, the email could not be sent", e);
        }
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }
}
