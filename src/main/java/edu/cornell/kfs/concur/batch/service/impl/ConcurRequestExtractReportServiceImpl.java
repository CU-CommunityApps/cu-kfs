package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurRequestExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class ConcurRequestExtractReportServiceImpl implements ConcurRequestExtractReportService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractReportServiceImpl.class);

    protected ReportWriterService reportWriterService;
    protected EmailService emailService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConfigurationService configurationService;

    protected String fileNamePrefixFirstPart;
    protected String fileNamePrefixSecondPart;
    protected String reportTitle;
    protected String reportConcurFileNameLabel;
    protected String summarySubTitle;
    protected String cashAdvancesProcessedInPdpLabel;
    protected String cashAdvancesBypassedRelatedToExpenseReportLabel;
    protected String recordsBypassedTravelRequestOnlyLabel;
    protected String duplicateCashAdvanceRequestsLabel;
    protected String clonedCashAdvanceRequestsLabel;
    protected String cashAdvancesNotProcessedValidationErrorsLabel;
    protected String totalsForFileLabel;
    protected String reportValidationErrorsSubTitle;
    protected String recordsNotSentToPdpSubLabel;

    public String getRecordsNotSentToPdpSubLabel() {
        return recordsNotSentToPdpSubLabel;
    }

    public void setRecordsNotSentToPdpSubLabel(String recordsNotSentToPdpSubLabel) {
        this.recordsNotSentToPdpSubLabel = recordsNotSentToPdpSubLabel;
    }
    
    @Override
    public File generateReport(ConcurRequestExtractBatchReportData reportData) {
        LOG.debug("generateReport: entered");
        initializeReportTitleAndFileName(reportData);
        if (!writeHeaderValidationErrors(reportData)) {
            writeSummarySubReport(reportData);
            writeValidationErrorSubReport(reportData);
        }
        finalizeReport();
        return getReportWriterService().getReportFile();
    }
    
    @Override
    public void sendEmailThatNoFileWasProcesed() {
        String body = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_NO_REPORT_EMAIL_BODY);
        String subject = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_REQUEST_EXTRACT_NO_REPORT_EMAIL_SUBJECT);
        sendEmail(subject, body);
    }
    
    @Override
    public void sendResultsEmail(ConcurRequestExtractBatchReportData reportData, File reportFile) {
        String body = readReportFileToString(reportFile);
        String subject = buildEmailSubject(reportData);
        sendEmail(subject, body);
    }
    
    private void sendEmail(String subject, String body) {
        String toAddress = getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_REPORT_EMAIL_TO_ADDRESS);
        String fromAddress = getConcurBatchUtilityService().getConcurParameterValue(ConcurParameterConstants.CONCUR_REPORT_EMAIL_FROM_ADDRESS);
        List<String> toAddressList = new ArrayList<>();
        toAddressList.add(toAddress);
        
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(fromAddress);
        message.setSubject(subject);
        message.getToAddresses().addAll(toAddressList);
        message.setMessage(body);
        
        boolean htmlMessage = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmail, from address: " + fromAddress + "  to address: " + toAddress);
            LOG.debug("sendEmail, the email subject: " + subject);
            LOG.debug("sendEmail, the email budy: " + body);
        }
        getEmailService().sendMessage(message, htmlMessage);
    }
    
    protected String readReportFileToString(File reportFile) {
        String contents = getConcurBatchUtilityService().getFileContents(reportFile.getAbsolutePath());
        if (StringUtils.isEmpty(contents)) {
            LOG.error("readReportFileToString, could not read report file into String");
            contents = "Could not read the report file.";
        }
        return contents;
    }
    
    protected String buildEmailSubject(ConcurRequestExtractBatchReportData reportData) {
        StringBuilder sb = new StringBuilder("The request extract file ");
        sb.append(reportData.getConcurFileName()).append(" has been processed.");
        if(!reportData.getHeaderValidationErrors().isEmpty()) {
            sb.append("  There are header validation errors.");
        }
        if(!reportData.getValidationErrorFileLines().isEmpty()) {
            sb.append("  There are line level validation errors.");
        }
        return sb.toString();
    }
    
    protected void initializeReportTitleAndFileName(ConcurRequestExtractBatchReportData  reportData) {
        LOG.debug("initializeReportTitleAndFileName, entered for Concur data file name:" + reportData.getConcurFileName());
        String concurFileName = convertConcurFileNameToDefaultWhenNotProvided(reportData.getConcurFileName());
        getReportWriterService().setFileNamePrefix(buildConcurReportFileNamePrefix(concurFileName, getFileNamePrefixFirstPart(), getFileNamePrefixSecondPart()));
        getReportWriterService().setTitle(getReportTitle());
        getReportWriterService().initialize();
        getReportWriterService().writeNewLines(2);
        getReportWriterService().writeFormattedMessageLine(getReportConcurFileNameLabel() + concurFileName);
        getReportWriterService().writeNewLines(2);
    }

    protected boolean writeHeaderValidationErrors(ConcurRequestExtractBatchReportData reportData) {
        boolean headerValidationFailed = false;
        if (CollectionUtils.isNotEmpty(reportData.getHeaderValidationErrors())) {
            LOG.debug("writeHeaderValidationErrors, detected header validation errors");
            headerValidationFailed = true;
            getReportWriterService().writeFormattedMessageLine("--------------------------------------------------------");
            getReportWriterService().writeFormattedMessageLine("The following header row validation errors were detected");
            getReportWriterService().writeFormattedMessageLine("--------------------------------------------------------");
            for (String errorString : reportData.getHeaderValidationErrors()) {
                getReportWriterService().writeFormattedMessageLine(errorString);
            }
            getReportWriterService().writeNewLines(2);
        }
        else {
            LOG.debug("writeHeaderValidationErrors, NO header validation errors detected");
        }
        return headerValidationFailed;
    }

    protected void writeSummarySubReport(ConcurRequestExtractBatchReportData reportData) {
        LOG.debug("writeSummarySubReport, entered");
        ensureSummaryLabelsHaveValues(reportData);
        getReportWriterService().writeSubTitle(getSummarySubTitle());
        getReportWriterService().writeNewLines(1);

        String rowFormat = "%54s %20d %20s";
        String hdrRowFormat = "%54s %20s %20s";
        Object[] headerArgs = { "                                                     ", "Record Count", "Dollar Amount" };
        Object[] headerBreak = { "----------------------------------------------------", "------------", "-------------" };
        Object[] totalsBreak = { "====================================================", "============", "=============" };
        getReportWriterService().setNewPage(false);
        getReportWriterService().writeNewLines(1);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerArgs);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);

        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getCashAdvancesProcessedInPdp().getItemLabel(), reportData.getCashAdvancesProcessedInPdp().getRecordCount(), reportData.getCashAdvancesProcessedInPdp().getDollarAmount().toString());
        getReportWriterService().writeNewLines(1);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, totalsBreak);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, getRecordsNotSentToPdpSubLabel(), KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING);
        getReportWriterService().writeNewLines(1);
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getCashAdvancesBypassedRelatedToExpenseReport().getItemLabel(), reportData.getCashAdvancesBypassedRelatedToExpenseReport().getRecordCount(), reportData.getCashAdvancesBypassedRelatedToExpenseReport().getDollarAmount().toString());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getRecordsBypassedTravelRequestOnly().getItemLabel(), reportData.getRecordsBypassedTravelRequestOnly().getRecordCount(), reportData.getRecordsBypassedTravelRequestOnly().getDollarAmount().toString());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getDuplicateCashAdvanceRequests().getItemLabel(), reportData.getDuplicateCashAdvanceRequests().getRecordCount(), reportData.getDuplicateCashAdvanceRequests().getDollarAmount().toString());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getClonedCashAdvanceRequests().getItemLabel(), reportData.getClonedCashAdvanceRequests().getRecordCount(), reportData.getClonedCashAdvanceRequests().getDollarAmount().toString());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getCashAdvancesNotProcessedValidationErrors().getItemLabel(), reportData.getCashAdvancesNotProcessedValidationErrors().getRecordCount(), reportData.getCashAdvancesNotProcessedValidationErrors().getDollarAmount().toString());
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, totalsBreak);
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getTotalsForFile().getItemLabel(), reportData.getTotalsForFile().getRecordCount(), reportData.getTotalsForFile().getDollarAmount().toString());
        getReportWriterService().writeNewLines(4);
    }

    protected void writeValidationErrorSubReport(ConcurRequestExtractBatchReportData reportData) {
        LOG.debug("writeValidationErrorSubReport, entered");
        String rowFormat = "%-24s %-24s %-36s %-36s %-14s";
        String hdrRowFormat = "%-24s %-24s %-36s %-36s %-14s";
        Object[] headerArgs = { "Report ID", "Employee ID", "Last Name", "First Name", "Middle Initial" };
        Object[] headerBreak = { "---------", "-----------", "---------", "----------", "--------------" };

        if (CollectionUtils.isEmpty(reportData.getValidationErrorFileLines())) {
            getReportWriterService().setNewPage(false);
            getReportWriterService().writeSubTitle(getReportValidationErrorsSubTitle());
            getReportWriterService().writeNewLines(1);
            getReportWriterService().writeFormattedMessageLine(ConcurConstants.RequestExtractReport.NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE);
        }
        else {
            getReportWriterService().writeSubTitle(getReportValidationErrorsSubTitle());
            getReportWriterService().writeNewLines(1);
            getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);
            getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerArgs);
            getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);

            for(ConcurBatchReportLineValidationErrorItem errorItem : reportData.getValidationErrorFileLines()) {
                getReportWriterService().writeFormattedMessageLine(rowFormat, errorItem.getReportId(), errorItem.getEmployeeId(), errorItem.getLastName(), errorItem.getFirstName(), errorItem.getMiddleInitial());
                getReportWriterService().writeNewLines(1);
                writeErrorItemMessages(errorItem.getItemErrorResults());
                getReportWriterService().writeNewLines(2);
            }
        }
    }

    protected void finalizeReport() {
        LOG.debug("finalizeReport, entered");
        getReportWriterService().writeNewLines(3);
        getReportWriterService().writeFormattedMessageLine(ConcurConstants.RequestExtractReport.END_OF_REPORT_MESSAGE);
        getReportWriterService().destroy();
    }
    
    private String buildConcurReportFileNamePrefix(String concurFileName, String prefixFirstPart, String prefixSecondPart) {
        return (prefixFirstPart + (StringUtils.substringBeforeLast(concurFileName, ConcurConstants.FILE_EXTENSION_DELIMITTER)) + prefixSecondPart);
    }

    private String convertConcurFileNameToDefaultWhenNotProvided(String concurFileName) {
        if (StringUtils.isEmpty(concurFileName)) {
            return ConcurConstants.RequestExtractReport.UNKNOWN_REQUEST_EXTRACT_FILENAME;
        }
        else {
            return concurFileName;
        }
    }

    private void writeErrorItemMessages(List<String> errorMessages) {
        LOG.debug("writeErrorItemMessages, entered");
        if (CollectionUtils.isEmpty(errorMessages)) {
            getReportWriterService().writeFormattedMessageLine(ConcurConstants.RequestExtractReport.NO_VALIDATION_ERROR_MESSAGES_TO_OUTPUT);
        }
        else {
            for (String errorMessage : errorMessages) {
                getReportWriterService().writeFormattedMessageLine(errorMessage);
            }
        }
    }

    private void ensureSummaryLabelsHaveValues (ConcurRequestExtractBatchReportData reportData) {
        if (StringUtils.isEmpty(reportData.getCashAdvancesProcessedInPdp().getItemLabel())) {
            reportData.getCashAdvancesProcessedInPdp().setItemLabel(getCashAdvancesProcessedInPdpLabel());
        }
        if (StringUtils.isEmpty(reportData.getCashAdvancesBypassedRelatedToExpenseReport().getItemLabel())) {
            reportData.getCashAdvancesBypassedRelatedToExpenseReport().setItemLabel(getCashAdvancesBypassedRelatedToExpenseReportLabel());
        }
        if (StringUtils.isEmpty(reportData.getRecordsBypassedTravelRequestOnly().getItemLabel())) {
            reportData.getRecordsBypassedTravelRequestOnly().setItemLabel(getRecordsBypassedTravelRequestOnlyLabel());
        }
        if (StringUtils.isEmpty(reportData.getDuplicateCashAdvanceRequests().getItemLabel())) {
            reportData.getDuplicateCashAdvanceRequests().setItemLabel(getDuplicateCashAdvanceRequestsLabel());
        }
        if (StringUtils.isEmpty(reportData.getClonedCashAdvanceRequests().getItemLabel())) {
            reportData.getClonedCashAdvanceRequests().setItemLabel(getClonedCashAdvanceRequestsLabel());
        }
        if (StringUtils.isEmpty(reportData.getCashAdvancesNotProcessedValidationErrors().getItemLabel())) {
            reportData.getCashAdvancesNotProcessedValidationErrors().setItemLabel(getCashAdvancesNotProcessedValidationErrorsLabel());
        }
        if (StringUtils.isEmpty(reportData.getTotalsForFile().getItemLabel())) {
            reportData.getTotalsForFile().setItemLabel(getTotalsForFileLabel());
        }
    }

    public String getFileNamePrefixFirstPart() {
        return fileNamePrefixFirstPart;
    }

    public void setFileNamePrefixFirstPart(String fileNamePrefixFirstPart) {
        this.fileNamePrefixFirstPart = fileNamePrefixFirstPart;
    }

    public String getFileNamePrefixSecondPart() {
        return fileNamePrefixSecondPart;
    }

    public void setFileNamePrefixSecondPart(String fileNamePrefixSecondPart) {
        this.fileNamePrefixSecondPart = fileNamePrefixSecondPart;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getReportConcurFileNameLabel() {
        return reportConcurFileNameLabel;
    }

    public void setReportConcurFileNameLabel(String reportConcurFileNameLabel) {
        this.reportConcurFileNameLabel = reportConcurFileNameLabel;
    }

    public String getSummarySubTitle() {
        return summarySubTitle;
    }

    public void setSummarySubTitle(String summarySubTitle) {
        this.summarySubTitle = summarySubTitle;
    }

    public String getCashAdvancesProcessedInPdpLabel() {
        return cashAdvancesProcessedInPdpLabel;
    }

    public void setCashAdvancesProcessedInPdpLabel(String cashAdvancesProcessedInPdpLabel) {
        this.cashAdvancesProcessedInPdpLabel = cashAdvancesProcessedInPdpLabel;
    }

    public String getCashAdvancesBypassedRelatedToExpenseReportLabel() {
        return cashAdvancesBypassedRelatedToExpenseReportLabel;
    }

    public void setCashAdvancesBypassedRelatedToExpenseReportLabel(String cashAdvancesBypassedRelatedToExpenseReportLabel) {
        this.cashAdvancesBypassedRelatedToExpenseReportLabel = cashAdvancesBypassedRelatedToExpenseReportLabel;
    }

    public String getRecordsBypassedTravelRequestOnlyLabel() {
        return recordsBypassedTravelRequestOnlyLabel;
    }

    public void setRecordsBypassedTravelRequestOnlyLabel(String recordsBypassedTravelRequestOnlyLabel) {
        this.recordsBypassedTravelRequestOnlyLabel = recordsBypassedTravelRequestOnlyLabel;
    }

    public String getDuplicateCashAdvanceRequestsLabel() {
        return duplicateCashAdvanceRequestsLabel;
    }

    public void setDuplicateCashAdvanceRequestsLabel(String duplicateCashAdvanceRequestsLabel) {
        this.duplicateCashAdvanceRequestsLabel = duplicateCashAdvanceRequestsLabel;
    }

    public String getClonedCashAdvanceRequestsLabel() {
        return clonedCashAdvanceRequestsLabel;
    }

    public void setClonedCashAdvanceRequestsLabel(String clonedCashAdvanceRequestsLabel) {
        this.clonedCashAdvanceRequestsLabel = clonedCashAdvanceRequestsLabel;
    }

    public String getCashAdvancesNotProcessedValidationErrorsLabel() {
        return cashAdvancesNotProcessedValidationErrorsLabel;
    }

    public void setCashAdvancesNotProcessedValidationErrorsLabel(String cashAdvancesNotProcessedValidationErrorsLabel) {
        this.cashAdvancesNotProcessedValidationErrorsLabel = cashAdvancesNotProcessedValidationErrorsLabel;
    }

    public String getTotalsForFileLabel() {
        return totalsForFileLabel;
    }

    public void setTotalsForFileLabel(String totalsForFileLabel) {
        this.totalsForFileLabel = totalsForFileLabel;
    }

    public String getReportValidationErrorsSubTitle() {
        return reportValidationErrorsSubTitle;
    }

    public void setReportValidationErrorsSubTitle(String reportValidationErrorsSubTitle) {
        this.reportValidationErrorsSubTitle = reportValidationErrorsSubTitle;
    }
    
    public ReportWriterService getReportWriterService() {
        return reportWriterService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public ConcurBatchUtilityService getConcurBatchUtilityService() {
        return concurBatchUtilityService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
