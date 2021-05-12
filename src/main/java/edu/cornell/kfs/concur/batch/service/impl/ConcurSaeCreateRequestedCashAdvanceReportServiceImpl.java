package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurSaeRequestedCashAdvanceBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurReportEmailService;
import edu.cornell.kfs.concur.batch.service.ConcurSaeCreateRequestedCashAdvanceReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class ConcurSaeCreateRequestedCashAdvanceReportServiceImpl implements ConcurSaeCreateRequestedCashAdvanceReportService {
    private static final Logger LOG = LogManager.getLogger(ConcurSaeCreateRequestedCashAdvanceReportServiceImpl.class);

    protected ReportWriterService reportWriterService;
    protected ConfigurationService configurationService;
    protected ConcurReportEmailService concurReportEmailService;

    protected String fileNamePrefixFirstPart;
    protected String fileNamePrefixSecondPart;
    protected String reportTitle;
    protected String reportConcurFileNameLabel;
    protected String summarySubTitle;
    protected String cashAdvancesProcessedInPdpLabel;
    protected String cashAdvancesBypassedRelatedToExpenseReportLabel;
    protected String recordsBypassedNotCashAdvanceLabel;
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
    public File generateReport(ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
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
        String body = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_CASH_ADVANCES_NO_REPORT_EMAIL_BODY);
        String subject = getConfigurationService().getPropertyValueAsString(ConcurKeyConstants.CONCUR_SAE_REQUESTED_CASH_ADVANCES_NO_REPORT_EMAIL_SUBJECT);
        getConcurReportEmailService().sendEmail(subject, body);
    }
    
    @Override
    public void sendResultsEmail(ConcurSaeRequestedCashAdvanceBatchReportData reportData, File reportFile) {
        getConcurReportEmailService().sendResultsEmail(reportData, reportFile);
    }
    
    protected void initializeReportTitleAndFileName(ConcurSaeRequestedCashAdvanceBatchReportData  reportData) {
        LOG.debug("initializeReportTitleAndFileName, entered for Concur data file name:" + reportData.getConcurFileName());
        String concurFileName = convertConcurFileNameToDefaultWhenNotProvided(reportData.getConcurFileName());
        getReportWriterService().setFileNamePrefix(buildConcurReportFileNamePrefix(concurFileName, getFileNamePrefixFirstPart(), getFileNamePrefixSecondPart()));
        getReportWriterService().setTitle(getReportTitle());
        getReportWriterService().initialize();
        getReportWriterService().writeNewLines(2);
        getReportWriterService().writeFormattedMessageLine(getReportConcurFileNameLabel() + concurFileName);
        getReportWriterService().writeNewLines(2);
    }

    protected boolean writeHeaderValidationErrors(ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
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

    protected void writeSummarySubReport(ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
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
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getRecordsBypassedNotCashAdvances().getItemLabel(), reportData.getRecordsBypassedNotCashAdvances().getRecordCount(), reportData.getRecordsBypassedNotCashAdvances().getDollarAmount().toString());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getDuplicateCashAdvanceRequests().getItemLabel(), reportData.getDuplicateCashAdvanceRequests().getRecordCount(), reportData.getDuplicateCashAdvanceRequests().getDollarAmount().toString());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getClonedCashAdvanceRequests().getItemLabel(), reportData.getClonedCashAdvanceRequests().getRecordCount(), reportData.getClonedCashAdvanceRequests().getDollarAmount().toString());
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getCashAdvancesNotProcessedValidationErrors().getItemLabel(), reportData.getCashAdvancesNotProcessedValidationErrors().getRecordCount(), reportData.getCashAdvancesNotProcessedValidationErrors().getDollarAmount().toString());
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, totalsBreak);
        getReportWriterService().writeFormattedMessageLine(rowFormat, reportData.getTotalsForFile().getItemLabel(), reportData.getTotalsForFile().getRecordCount(), reportData.getTotalsForFile().getDollarAmount().toString());
        getReportWriterService().writeNewLines(4);
    }

    protected void writeValidationErrorSubReport(ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
        LOG.debug("writeValidationErrorSubReport, entered");
        String rowFormat = "%-24s %-24s %-36s %-36s %-14s";
        String hdrRowFormat = "%-24s %-24s %-36s %-36s %-14s";
        Object[] headerArgs = { "Cash Advance Key", "Employee ID", "Last Name", "First Name", "Middle Initial" };
        Object[] headerBreak = { "----------------", "-----------", "---------", "----------", "--------------" };

        if (CollectionUtils.isEmpty(reportData.getValidationErrorFileLines())) {
            getReportWriterService().setNewPage(false);
            getReportWriterService().writeSubTitle(getReportValidationErrorsSubTitle());
            getReportWriterService().writeNewLines(1);
            getReportWriterService().writeFormattedMessageLine(ConcurConstants.SaeRequestedCashAdvancesExtractReport.NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE);
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
        getReportWriterService().writeFormattedMessageLine(ConcurConstants.SaeRequestedCashAdvancesExtractReport.END_OF_REPORT_MESSAGE);
        getReportWriterService().destroy();
    }
    
    private String buildConcurReportFileNamePrefix(String concurFileName, String prefixFirstPart, String prefixSecondPart) {
        return (prefixFirstPart + (StringUtils.substringBeforeLast(concurFileName, ConcurConstants.FILE_EXTENSION_DELIMITTER)) + prefixSecondPart);
    }

    private String convertConcurFileNameToDefaultWhenNotProvided(String concurFileName) {
        if (StringUtils.isEmpty(concurFileName)) {
            return ConcurConstants.SaeRequestedCashAdvancesExtractReport.UNKNOWN_SAE_FILENAME;
        }
        else {
            return concurFileName;
        }
    }

    private void writeErrorItemMessages(List<String> errorMessages) {
        LOG.debug("writeErrorItemMessages, entered");
        if (CollectionUtils.isEmpty(errorMessages)) {
            getReportWriterService().writeFormattedMessageLine(ConcurConstants.SaeRequestedCashAdvancesExtractReport.NO_VALIDATION_ERROR_MESSAGES_TO_OUTPUT);
        }
        else {
            for (String errorMessage : errorMessages) {
                getReportWriterService().writeFormattedMessageLine(errorMessage);
            }
        }
    }

    private void ensureSummaryLabelsHaveValues (ConcurSaeRequestedCashAdvanceBatchReportData reportData) {
        if (StringUtils.isEmpty(reportData.getCashAdvancesProcessedInPdp().getItemLabel())) {
            reportData.getCashAdvancesProcessedInPdp().setItemLabel(getCashAdvancesProcessedInPdpLabel());
        }
        if (StringUtils.isEmpty(reportData.getCashAdvancesBypassedRelatedToExpenseReport().getItemLabel())) {
            reportData.getCashAdvancesBypassedRelatedToExpenseReport().setItemLabel(getCashAdvancesBypassedRelatedToExpenseReportLabel());
        }
        if (StringUtils.isEmpty(reportData.getRecordsBypassedNotCashAdvances().getItemLabel())) {
            reportData.getRecordsBypassedNotCashAdvances().setItemLabel(this.getRecordsBypassedNotCashAdvanceLabel());
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

    public String getRecordsBypassedNotCashAdvanceLabel() {
        return recordsBypassedNotCashAdvanceLabel;
    }

    public void setRecordsBypassedNotCashAdvanceLabel(String recordsBypassedNotCashAdvanceLabel) {
        this.recordsBypassedNotCashAdvanceLabel = recordsBypassedNotCashAdvanceLabel;
    }

    public void setCashAdvancesBypassedRelatedToExpenseReportLabel(String cashAdvancesBypassedRelatedToExpenseReportLabel) {
        this.cashAdvancesBypassedRelatedToExpenseReportLabel = cashAdvancesBypassedRelatedToExpenseReportLabel;
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

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public ConcurReportEmailService getConcurReportEmailService() {
        return concurReportEmailService;
    }

    public void setConcurReportEmailService(ConcurReportEmailService concurReportEmailService) {
        this.concurReportEmailService = concurReportEmailService;
    }

}
