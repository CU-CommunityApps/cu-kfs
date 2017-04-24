package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportSummaryItem;
import edu.cornell.kfs.concur.batch.report.ConcurRequestExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class ConcurRequestExtractReportServiceImpl implements ConcurRequestExtractReportService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractReportServiceImpl.class);

    protected ReportWriterService reportWriterService;

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
        String cashAdvancesProcessedInPdpLabel = (StringUtils.isEmpty(reportData.getCashAdvancesProcessedInPdp().getItemLabel())) ?
                getCashAdvancesProcessedInPdpLabel() : reportData.getCashAdvancesProcessedInPdp().getItemLabel();

        String cashAdvancesBypassedRelatedToExpenseReportLabel = (StringUtils.isEmpty(reportData.getCashAdvancesBypassedRelatedToExpenseReport().getItemLabel())) ?
                getCashAdvancesBypassedRelatedToExpenseReportLabel() : reportData.getCashAdvancesBypassedRelatedToExpenseReport().getItemLabel();

        String recordsBypassedTravelRequestOnlyLabel = (StringUtils.isEmpty(reportData.getRecordsBypassedTravelRequestOnly().getItemLabel())) ?
                getRecordsBypassedTravelRequestOnlyLabel() : reportData.getRecordsBypassedTravelRequestOnly().getItemLabel();

        String duplicateCashAdvanceRequestsLabel = (StringUtils.isEmpty(reportData.getDuplicateCashAdvanceRequests().getItemLabel())) ?
                getDuplicateCashAdvanceRequestsLabel() : reportData.getDuplicateCashAdvanceRequests().getItemLabel();

        String clonedCashAdvanceRequestsLabel = (StringUtils.isEmpty(reportData.getClonedCashAdvanceRequests().getItemLabel())) ?
                getClonedCashAdvanceRequestsLabel() : reportData.getClonedCashAdvanceRequests().getItemLabel();       

        String cashAdvancesNotProcessedValidationErrorsLabel = (StringUtils.isEmpty(reportData.getCashAdvancesNotProcessedValidationErrors().getItemLabel())) ?
                getCashAdvancesNotProcessedValidationErrorsLabel() : reportData.getCashAdvancesNotProcessedValidationErrors().getItemLabel();  

        String totalsForFileLabel = (StringUtils.isEmpty(reportData.getTotalsForFile().getItemLabel())) ?
                getTotalsForFileLabel() : reportData.getTotalsForFile().getItemLabel();

        getReportWriterService().writeSubTitle(this.getSummarySubTitle());
        getReportWriterService().writeNewLines(1);

        String rowFormat = "%54s %20d %20s";
        String hdrRowFormat = "%54s %20s %20s";
        Object[] headerArgs = { "                        Totals                      ", "Record Count", "Dollar Amount" };
        Object[] headerBreak = { "----------------------------------------------------", "------------", "-------------" };
        Object[] totalsBreak = { "====================================================", "============", "=============" };
        getReportWriterService().setNewPage(false);
        getReportWriterService().writeNewLines(1);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerArgs);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);

        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                cashAdvancesProcessedInPdpLabel, reportData.getCashAdvancesProcessedInPdp().getRecordCount(),
                reportData.getCashAdvancesProcessedInPdp().getDollarAmount().toString());

        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                cashAdvancesBypassedRelatedToExpenseReportLabel, reportData.getCashAdvancesBypassedRelatedToExpenseReport().getRecordCount(),
                reportData.getCashAdvancesBypassedRelatedToExpenseReport().getDollarAmount().toString());

        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                recordsBypassedTravelRequestOnlyLabel, reportData.getRecordsBypassedTravelRequestOnly().getRecordCount(),
                reportData.getRecordsBypassedTravelRequestOnly().getDollarAmount().toString());

        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                duplicateCashAdvanceRequestsLabel, reportData.getDuplicateCashAdvanceRequests().getRecordCount(),
                reportData.getDuplicateCashAdvanceRequests().getDollarAmount().toString());
        
        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                clonedCashAdvanceRequestsLabel, reportData.getClonedCashAdvanceRequests().getRecordCount(),
                reportData.getClonedCashAdvanceRequests().getDollarAmount().toString());
        
        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                cashAdvancesNotProcessedValidationErrorsLabel, reportData.getCashAdvancesNotProcessedValidationErrors().getRecordCount(),
                reportData.getCashAdvancesNotProcessedValidationErrors().getDollarAmount().toString());

        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, totalsBreak);
        
        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                totalsForFileLabel, reportData.getTotalsForFile().getRecordCount(),
                reportData.getTotalsForFile().getDollarAmount().toString());

        getReportWriterService().pageBreak();
    }

    protected void writeValidationErrorSubReport(ConcurRequestExtractBatchReportData reportData) {
        LOG.debug("writeValidationErrorSubReport, entered");
        String rowFormat = "%-24s %-24s %-36s %-36s %-14s";
        String hdrRowFormat = "%-24s %-24s %-36s %-36s %-14s";
        Object[] headerArgs = { "Report ID", "Employee ID", "Last Name", "First Name", "Middle Initial" };
        Object[] headerBreak = { "---------", "-----------", "---------", "----------", "--------------" };

        boolean firstLine = true;

        if (CollectionUtils.isEmpty(reportData.getValidationErrorFileLines())) {
            getReportWriterService().setNewPage(false);
            getReportWriterService().writeSubTitle(getReportValidationErrorsSubTitle());
            getReportWriterService().writeNewLines(1);
            getReportWriterService().writeFormattedMessageLine(ConcurConstants.RequestExtractReport.NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE);
        }
        else {
            for(ConcurBatchReportLineValidationErrorItem errorItem : reportData.getValidationErrorFileLines()) {
                if (getReportWriterService().isNewPage() || firstLine) {
                    firstLine = false;
                    getReportWriterService().setNewPage(false);
                    getReportWriterService().writeSubTitle(getReportValidationErrorsSubTitle());
                    getReportWriterService().writeNewLines(1);
                }
                getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);
                getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerArgs);
                getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);
                getReportWriterService().writeFormattedMessageLine(rowFormat, errorItem.getReportId(), errorItem.getEmployeeId(), errorItem.getLastName(), errorItem.getFirstName(), errorItem.getMiddleInitial());
                getReportWriterService().writeNewLines(1);
                writeErrorItemMessages(errorItem.getItemErrorResults());
                getReportWriterService().writeNewLines(2);
            }
        }
        getReportWriterService().pageBreak();
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

    public ReportWriterService getReportWriterService() {
        return reportWriterService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
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

}
