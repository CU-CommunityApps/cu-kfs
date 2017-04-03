package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.service.ReportWriterService;
import edu.cornell.kfs.sys.service.impl.ReportWriterTextServiceImpl;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportMissingObjectCodeItem;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportSummaryItem;
import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractReportService;
import edu.cornell.kfs.paymentworks.batch.report.SupplierUploadSummary;
import edu.cornell.kfs.paymentworks.batch.report.SupplierUploadSummaryLine;

public class ConcurStandardAccountingExtractReportServiceImpl implements ConcurStandardAccountingExtractReportService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractReportServiceImpl.class);

    protected ReportWriterService reportWriterService;

    protected String fileNamePrefixFirstPart;
    protected String fileNamePrefixSecondPart;
    protected String reportTitle;
    protected String summarySubTitle;
    protected String reportConcurFileNameLabel;
    protected String reimbursementsInExpenseReportLabel;
    protected String cashAdvancesRelatedToExpenseReportsLabel;
    protected String expensesPaidOnCorporateCardLabel;
    protected String transactionsBypassedLabel;
    protected String pdpRecordsProcessedLabel;
    protected String reportValidationErrorsSubTitle;
    protected String reportMissingObjectCodesSubTitle;
    
    public File generateReport(ConcurStandardAccountingExtractBatchReportData reportData) {
        initializeReportTitleAndFileName(reportData);
        writeSummarySubReport(reportData);
        writeValidationErrorSubReport(reportData);
        writeMissingObjectCodesSubReport(reportData);
        finalizeReport();
        return getReportWriterService().getReportFile();
    }
    
    protected void initializeReportTitleAndFileName(ConcurStandardAccountingExtractBatchReportData reportData) {
        String concurFileName = reportData.getConcurFileName();
        if (StringUtils.isEmpty(concurFileName)) {
            concurFileName = ConcurConstants.StandardAccountingExtractReport.UNKNOWN_SAE_FILENAME;
        }
        getReportWriterService().setFileNamePrefix((getFileNamePrefixFirstPart() + concurFileName + getFileNamePrefixSecondPart()));
        getReportWriterService().setTitle(getReportTitle());
        getReportWriterService().initialize();
        getReportWriterService().writeNewLines(2);
        getReportWriterService().writeFormattedMessageLine(getReportConcurFileNameLabel() + concurFileName);
        getReportWriterService().writeNewLines(2);
    }
    
    protected void writeSummarySubReport(ConcurStandardAccountingExtractBatchReportData reportData) {
        String reimbursementsInExpenseReportLabel = (StringUtils.isEmpty(reportData.getReimbursementsInExpenseReport().getItemLabel())) ?
                getReimbursementsInExpenseReportLabel() : reportData.getReimbursementsInExpenseReport().getItemLabel();

        String cashAdvancesRelatedToExpenseReportsLabel = (StringUtils.isEmpty(reportData.getCashAdvancesRelatedToExpenseReports().getItemLabel())) ?
                getCashAdvancesRelatedToExpenseReportsLabel() : reportData.getCashAdvancesRelatedToExpenseReports().getItemLabel();

        String expensesPaidOnCorporateCardLabel = (StringUtils.isEmpty(reportData.getExpensesPaidOnCorporateCard().getItemLabel())) ?
                getExpensesPaidOnCorporateCardLabel() : reportData.getExpensesPaidOnCorporateCard().getItemLabel();

        String transactionsBypassedLabel = (StringUtils.isEmpty(reportData.getTransactionsBypassed().getItemLabel())) ?
                getTransactionsBypassedLabel() : reportData.getTransactionsBypassed().getItemLabel();

        String pdpRecordsProcessedLabel = (StringUtils.isEmpty(reportData.getPdpRecordsProcessed().getItemLabel())) ?
                getPdpRecordsProcessedLabel() : reportData.getPdpRecordsProcessed().getItemLabel();

        getReportWriterService().writeSubTitle(this.getSummarySubTitle());
        getReportWriterService().writeNewLines(2);

        String rowFormat = "%44s %20d %20s";
        String hdrRowFormat = "%44s %20s %20s";
        Object[] headerArgs = { "Totals", "Record Count", "Dollar Amount" };
        Object[] headerBreak = { "------------------------------------------", "------------", "-------------" };
        getReportWriterService().setNewPage(false);
        getReportWriterService().writeNewLines(1);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerArgs);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);

        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                reimbursementsInExpenseReportLabel, reportData.getReimbursementsInExpenseReport().getRecordCount(),
                reportData.getReimbursementsInExpenseReport().getDollarAmount().toString());

        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                cashAdvancesRelatedToExpenseReportsLabel, reportData.getCashAdvancesRelatedToExpenseReports().getRecordCount(),
                reportData.getCashAdvancesRelatedToExpenseReports().getDollarAmount().toString());

        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                expensesPaidOnCorporateCardLabel, reportData.getExpensesPaidOnCorporateCard().getRecordCount(),
                reportData.getExpensesPaidOnCorporateCard().getDollarAmount().toString());

        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                transactionsBypassedLabel, reportData.getTransactionsBypassed().getRecordCount(),
                reportData.getTransactionsBypassed().getDollarAmount().toString());

        getReportWriterService().writeFormattedMessageLine(rowFormat, 
                pdpRecordsProcessedLabel, reportData.getPdpRecordsProcessed().getRecordCount(),
                reportData.getPdpRecordsProcessed().getDollarAmount().toString());

        getReportWriterService().pageBreak();
    }

    protected void writeValidationErrorSubReport(ConcurStandardAccountingExtractBatchReportData reportData) {
        String rowFormat = "%-24s %-24s %-36s %-36s %-14s";
        String hdrRowFormat = "%-24s %-24s %-36s %-36s %-14s";
        Object[] headerArgs = { "Report ID", "Employee ID", "Last Name", "First Name", "Middle Initial" };
        Object[] headerBreak = { "---------", "-----------", "---------", "----------", "--------------" };

        boolean firstLine = true;

        if (CollectionUtils.isEmpty(reportData.getValidationErrorFileLines())) {
            getReportWriterService().setNewPage(false);
            getReportWriterService().writeSubTitle(getReportValidationErrorsSubTitle());
            getReportWriterService().writeNewLines(1);
            getReportWriterService().writeFormattedMessageLine(ConcurConstants.StandardAccountingExtractReport.NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE);
        }
        else {
            for(ConcurBatchReportLineValidationErrorItem errorItem : reportData.getValidationErrorFileLines()){
                if (getReportWriterService().isNewPage() || firstLine) {
                    firstLine = false;
                    getReportWriterService().setNewPage(false);
                    getReportWriterService().writeSubTitle(getReportValidationErrorsSubTitle());
                    getReportWriterService().writeNewLines(1);
                    getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerArgs);
                    getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);
                }
                getReportWriterService().writeFormattedMessageLine(rowFormat, errorItem.getReportId(), errorItem.getEmployeeId(), errorItem.getLastName(), errorItem.getFirstName(), errorItem.getMiddleInitial());
            }
        }
        getReportWriterService().pageBreak();
    }

    protected void writeMissingObjectCodesSubReport(ConcurStandardAccountingExtractBatchReportData reportData) {
        String rowFormat = "%-20s %-20s %-20s %-20s %-20s %20s";
        String hdrRowFormat = "%-20s %-20s %-20s %-20s %-20s %20s";
        Object[] headerArgs = { "Report ID", "Employee ID", "Last Name", "First Name", "Policy Name", "Expense Type Name"};
        Object[] headerBreak = { "---------", "-----------", "---------", "----------", "-----------", "-----------------"};

        boolean firstLine = true;

        if (CollectionUtils.isEmpty(reportData.getValidationErrorFileLines())) {
            getReportWriterService().setNewPage(false);
            getReportWriterService().writeSubTitle(getReportMissingObjectCodesSubTitle());
            getReportWriterService().writeNewLines(1);
            getReportWriterService().writeFormattedMessageLine(ConcurConstants.StandardAccountingExtractReport.NO_RECORDS_MISSING_OBJECT_CODES_MESSAGE);
        }
        else {
            for(ConcurBatchReportMissingObjectCodeItem errorItem : reportData.getPendingClientObjectCodeLines()){
                if (getReportWriterService().isNewPage() || firstLine) {
                    firstLine = false;
                    getReportWriterService().setNewPage(false);
                    getReportWriterService().writeSubTitle(getReportMissingObjectCodesSubTitle());
                    getReportWriterService().writeNewLines(1);
                    getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerArgs);
                    getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);
                }
                getReportWriterService().writeFormattedMessageLine(rowFormat, errorItem.getReportId(), errorItem.getEmployeeId(), errorItem.getLastName(), errorItem.getFirstName(), errorItem.getMiddleInitial());
            }
        }
    }

    protected void finalizeReport() {
        getReportWriterService().writeNewLines(3);
        getReportWriterService().writeFormattedMessageLine(ConcurConstants.StandardAccountingExtractReport.END_OF_REPORT_MESSAGE);
        getReportWriterService().destroy();
    }

    public ReportWriterService getReportWriterService() {
        return reportWriterService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public String getFileNamePrefixFirstPart() {
        if (StringUtils.isEmpty(fileNamePrefixFirstPart)) {
            setFileNamePrefixFirstPart(ConcurConstants.StandardAccountingExtractReport.PREFIX_FIRST_PART_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return fileNamePrefixFirstPart;
    }

    public void setFileNamePrefixFirstPart(String fileNamePrefixFirstPart) {
        this.fileNamePrefixFirstPart = fileNamePrefixFirstPart;
    }

    public String getFileNamePrefixSecondPart() {
        if (StringUtils.isEmpty(fileNamePrefixSecondPart)) {
            setFileNamePrefixFirstPart(ConcurConstants.StandardAccountingExtractReport.PREFIX_SECOND_PART_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return fileNamePrefixSecondPart;
    }

    public void setFileNamePrefixSecondPart(String fileNamePrefixSecondPart) {
        this.fileNamePrefixSecondPart = fileNamePrefixSecondPart;
    }

    public String getReportTitle() {
        if (StringUtils.isEmpty(reportTitle)) {
            setReportTitle(ConcurConstants.StandardAccountingExtractReport.SAE_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return reportTitle;
    }

    public void setReportTitle(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    public String getSummarySubTitle() {
        if (StringUtils.isEmpty(summarySubTitle)) {
            setSummarySubTitle(ConcurConstants.StandardAccountingExtractReport.SAE_SUMMARY_REPORT_SUB_TITLE_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return summarySubTitle;
    }

    public void setSummarySubTitle(String summarySubTitle) {
        this.summarySubTitle = summarySubTitle;
    }

    public String getReportConcurFileNameLabel() {
        if (StringUtils.isEmpty(reportConcurFileNameLabel)) {
            setReportConcurFileNameLabel(ConcurConstants.StandardAccountingExtractReport.SAE_REPORT_CONCUR_FILE_NAME_LABEL_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return reportConcurFileNameLabel;
    }

    public void setReportConcurFileNameLabel(String reportConcurFileNameLabel) {
        this.reportConcurFileNameLabel = reportConcurFileNameLabel;
    }

    public String getReimbursementsInExpenseReportLabel() {
        if (StringUtils.isEmpty(reimbursementsInExpenseReportLabel)) {
            setReimbursementsInExpenseReportLabel(ConcurConstants.StandardAccountingExtractReport.REIMBURSEMENTS_IN_EXPENSE_REPORT_LABEL_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return reimbursementsInExpenseReportLabel;
    }

    public void setReimbursementsInExpenseReportLabel(String reimbursementsInExpenseReportLabel) {
        this.reimbursementsInExpenseReportLabel = reimbursementsInExpenseReportLabel;
    }

    public String getCashAdvancesRelatedToExpenseReportsLabel() {
        if (StringUtils.isEmpty(cashAdvancesRelatedToExpenseReportsLabel)) {
            setCashAdvancesRelatedToExpenseReportsLabel(ConcurConstants.StandardAccountingExtractReport.CASH_ADVANCE_RELATED_TO_EXPENSE_REPORT_LABEL_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return cashAdvancesRelatedToExpenseReportsLabel;
    }

    public void setCashAdvancesRelatedToExpenseReportsLabel(String cashAdvancesRelatedToExpenseReportsLabel) {
        this.cashAdvancesRelatedToExpenseReportsLabel = cashAdvancesRelatedToExpenseReportsLabel;
    }

    public String getExpensesPaidOnCorporateCardLabel() {
        if (StringUtils.isEmpty(expensesPaidOnCorporateCardLabel)) {
            setExpensesPaidOnCorporateCardLabel(ConcurConstants.StandardAccountingExtractReport.EXPENSES_PAID_ON_CORPORATE_CARD_LABEL_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return expensesPaidOnCorporateCardLabel;
    }

    public void setExpensesPaidOnCorporateCardLabel(String expensesPaidOnCorporateCardLabel) {
        this.expensesPaidOnCorporateCardLabel = expensesPaidOnCorporateCardLabel;
    }

    public String getTransactionsBypassedLabel() {
        if (StringUtils.isEmpty(transactionsBypassedLabel)) {
            setTransactionsBypassedLabel(ConcurConstants.StandardAccountingExtractReport.TRANSACTIONS_BYPASSED_LABEL_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return transactionsBypassedLabel;
    }

    public void setTransactionsBypassedLabel(String transactionsBypassedLabel) {
        this.transactionsBypassedLabel = transactionsBypassedLabel;
    }

    public String getPdpRecordsProcessedLabel() {
        if (StringUtils.isEmpty(pdpRecordsProcessedLabel)) {
            setPdpRecordsProcessedLabel(ConcurConstants.StandardAccountingExtractReport.PDP_RECORDS_PROCESSED_LABEL_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return pdpRecordsProcessedLabel;
    }

    public void setPdpRecordsProcessedLabel(String pdpRecordsProcessedLabel) {
        this.pdpRecordsProcessedLabel = pdpRecordsProcessedLabel;
    }

    public String getReportValidationErrorsSubTitle() {
        if (StringUtils.isEmpty(reportValidationErrorsSubTitle)) {
            setReportValidationErrorsSubTitle(ConcurConstants.StandardAccountingExtractReport.SAE_VALIDATION_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return reportValidationErrorsSubTitle;
    }

    public void setReportValidationErrorsSubTitle(String reportValidationErrorsSubTitle) {
        this.reportValidationErrorsSubTitle = reportValidationErrorsSubTitle;
    }

    public String getReportMissingObjectCodesSubTitle() {
        if (StringUtils.isEmpty(reportMissingObjectCodesSubTitle)) {
            setReportMissingObjectCodesSubTitle(ConcurConstants.StandardAccountingExtractReport.SAE_MISSING_OBJECT_CODES_SUB_REPORT_TITLE_NOT_SET_IN_CONFIGURATION_FILE);
        }
        return reportMissingObjectCodesSubTitle;
    }

    public void setReportMissingObjectCodesSubTitle(String reportMissingObjectCodesSubTitle) {
        this.reportMissingObjectCodesSubTitle = reportMissingObjectCodesSubTitle;
    }

}
