package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportMissingObjectCodeItem;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class ConcurStandardAccountingExtractReportServiceImpl implements ConcurStandardAccountingExtractReportService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractReportServiceImpl.class);

    protected static final String REPORT_ID_HEADER = "Report ID";
    protected static final String REPORT_ID_HEADER_BREAK = "---------";
    protected static final String BLANK_REPORT_ID_SUBSTITUTE = "[blank value]";

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
        LOG.debug("generateReport, entered");
        initializeReportTitleAndFileName(reportData);
        if (!writeHeaderValidationErrors(reportData)) {
            writeSummarySubReport(reportData);
            writeValidationErrorSubReport(reportData);
            writeMissingObjectCodesSubReport(reportData);
        }
        finalizeReport();
        return getReportWriterService().getReportFile();
    }
    
    protected void initializeReportTitleAndFileName(ConcurStandardAccountingExtractBatchReportData  reportData) {
        LOG.debug("initializeReportTitleAndFileName, entered for Concur data file name:" + reportData.getConcurFileName());
        String concurFileName = convertConcurFileNameToDefaultWhenNotProvided(reportData.getConcurFileName());
        getReportWriterService().setFileNamePrefix(buildConcurReportFileNamePrefix(concurFileName, getFileNamePrefixFirstPart(), getFileNamePrefixSecondPart()));
        getReportWriterService().setTitle(getReportTitle());
        getReportWriterService().initialize();
        getReportWriterService().writeNewLines(2);
        getReportWriterService().writeFormattedMessageLine(getReportConcurFileNameLabel() + concurFileName);
        getReportWriterService().writeNewLines(2);
    }

    protected boolean writeHeaderValidationErrors(ConcurStandardAccountingExtractBatchReportData reportData) {
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

    protected void writeSummarySubReport(ConcurStandardAccountingExtractBatchReportData reportData) {
        LOG.debug("writeSummarySubReport, entered");
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
        getReportWriterService().writeNewLines(1);

        String rowFormat = "%44s %20d %20s";
        String hdrRowFormat = "%44s %20s %20s";
        Object[] headerArgs = { "Totals", "Record Count", "Dollar Amount" };
        Object[] headerBreak = { "------------------------------------------", "------------", "-------------" };
        getReportWriterService().setNewPage(false);
        getReportWriterService().writeNewLines(1);
        getReportWriterService().writeFormattedMessageLine(hdrRowFormat, headerBreak);
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
        LOG.debug("writeValidationErrorSubReport, entered");

        if (CollectionUtils.isEmpty(reportData.getValidationErrorFileLines())) {
            writeErrorSubReportWithNoLineItems(
                    getReportValidationErrorsSubTitle(), ConcurConstants.StandardAccountingExtractReport.NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE);
        } else {
            String rowFormat = "%-15s %-24s %-36s %-36s %-14s %-10s %-14s %-18s %-11s %-15s %-12s %-10s %-23s";
            Object[] headerArgs = getValidationErrorItemHeaders();
            Object[] headerBreak = buildHeaderBreaksFromHeaders(headerArgs);
            
            Consumer<ConcurBatchReportLineValidationErrorItem> errorItemWriter = (errorItem) -> {
                writeErrorItemHeader(rowFormat, headerArgs, headerBreak);
                writeValidationErrorItemFields(rowFormat, errorItem);
                writeErrorResultsForErrorItem(errorItem);
            };
            
            writeErrorSubReport(reportData.getValidationErrorFileLines(), getReportValidationErrorsSubTitle(), errorItemWriter);
        }
        getReportWriterService().pageBreak();
    }

    protected void writeMissingObjectCodesSubReport(ConcurStandardAccountingExtractBatchReportData reportData) {
        LOG.debug("writeMissingObjectCodesSubReport, entered");

        if (CollectionUtils.isEmpty(reportData.getPendingClientObjectCodeLines())) {
            writeErrorSubReportWithNoLineItems(
                    getReportMissingObjectCodesSubTitle(), ConcurConstants.StandardAccountingExtractReport.NO_RECORDS_MISSING_OBJECT_CODES_MESSAGE);
        } else {
            String rowFormat = "%-15s %-24s %-36s %-36s %-14s %-10s %-14s %-18s %-11s %-15s %-12s %-10s %-23s %-64s %-64s";
            Object[] headerArgs = getMissingObjectCodeItemHeaders();
            Object[] headerBreak = buildHeaderBreaksFromHeaders(headerArgs);
            
            Consumer<ConcurBatchReportMissingObjectCodeItem> errorItemWriter = (errorItem) -> {
                writeErrorItemHeader(rowFormat, headerArgs, headerBreak);
                writeMissingObjectCodeItemFields(rowFormat, errorItem);
                writeErrorResultsForErrorItem(errorItem);
            };
            
            writeErrorSubReport(reportData.getPendingClientObjectCodeLines(), getReportMissingObjectCodesSubTitle(), errorItemWriter);
        }
    }

    protected Object[] getValidationErrorItemHeaders() {
        return new Object[] {
            "Sequence Number", "Employee ID", "Last Name", "First Name", "Middle Initial", "Chart Code", "Account Number", "Sub-Account Number",
            "Object Code", "Sub-Object Code", "Project Code", "Org Ref ID", "Line Amount"
        };
    }

    protected Object[] getMissingObjectCodeItemHeaders() {
        return ArrayUtils.addAll(getValidationErrorItemHeaders(), new Object[] {"Policy Name", "Expense Type Name"});
    }

    protected Object[] buildHeaderBreaksFromHeaders(Object... headers) {
        return Arrays.stream(headers)
                .map(this::buildHeaderBreakFromHeader)
                .toArray(Object[]::new);
    }

    protected String buildHeaderBreakFromHeader(Object header) {
        return StringUtils.repeat(KFSConstants.DASH, header.toString().length());
    }

    protected void writeErrorSubReportWithNoLineItems(String subTitle, String message) {
        writeSubTitleForErrorSubReport(subTitle);
        getReportWriterService().writeFormattedMessageLine(message);
    }

    protected <T extends ConcurBatchReportLineValidationErrorItem> void writeErrorSubReport(
            List<T> errorItems, String subTitle, Consumer<T> errorItemWriter) {
        boolean firstLine = true;
        Map<String, List<T>> groupedErrors = groupErrorItemsByReportId(errorItems);
        
        for (Map.Entry<String, List<T>> grouping : groupedErrors.entrySet()) {
            String reportId = grouping.getKey();
            List<T> errorItemSubGroup = grouping.getValue();
            if (!firstLine) {
                writeReportIdHeader(reportId);
            }
            
            for (T errorItem : errorItemSubGroup) {
                if (getReportWriterService().isNewPage() || firstLine) {
                    writeSubTitleForErrorSubReport(subTitle);
                    if (firstLine) {
                        writeReportIdHeader(reportId);
                        firstLine = false;
                    }
                }
                errorItemWriter.accept(errorItem);
            }
        }
    }

    protected <T extends ConcurBatchReportLineValidationErrorItem> Map<String, List<T>> groupErrorItemsByReportId(List<T> errorItems) {
        return errorItems.stream()
                .collect(Collectors.groupingBy(
                        this::getNullSafeReportIdFromItem, LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));
    }

    protected String getNullSafeReportIdFromItem(ConcurBatchReportLineValidationErrorItem errorItem) {
        return StringUtils.defaultIfBlank(errorItem.getReportId(), BLANK_REPORT_ID_SUBSTITUTE);
    }

    protected void writeSubTitleForErrorSubReport(String subTitle) {
        getReportWriterService().setNewPage(false);
        getReportWriterService().writeSubTitle(subTitle);
        getReportWriterService().writeNewLines(1);
    }

    protected void writeReportIdHeader(String reportId) {
        getReportWriterService().writeFormattedMessageLine(REPORT_ID_HEADER_BREAK);
        getReportWriterService().writeFormattedMessageLine(REPORT_ID_HEADER);
        getReportWriterService().writeFormattedMessageLine(REPORT_ID_HEADER_BREAK);
        getReportWriterService().writeFormattedMessageLine(reportId);
        getReportWriterService().writeNewLines(1);
    }

    protected void writeErrorItemHeader(String rowFormat, Object[] headerArgs, Object[] headerBreakArgs) {
        getReportWriterService().writeFormattedMessageLine(rowFormat, headerBreakArgs);
        getReportWriterService().writeFormattedMessageLine(rowFormat, headerArgs);
        getReportWriterService().writeFormattedMessageLine(rowFormat, headerBreakArgs);
    }

    protected void writeValidationErrorItemFields(String rowFormat, ConcurBatchReportLineValidationErrorItem errorItem) {
        getReportWriterService().writeFormattedMessageLine(rowFormat, errorItem.getLineId(), errorItem.getEmployeeId(), errorItem.getLastName(),
                errorItem.getFirstName(), errorItem.getMiddleInitial(), errorItem.getChartOfAccountsCode(), errorItem.getAccountNumber(),
                errorItem.getSubAccountNumber(), errorItem.getObjectCode(), errorItem.getSubObjectCode(), errorItem.getProjectCode(),
                errorItem.getOrgRefId(), errorItem.getLineAmount());
    }

    protected void writeMissingObjectCodeItemFields(String rowFormat, ConcurBatchReportMissingObjectCodeItem errorItem) {
        getReportWriterService().writeFormattedMessageLine(rowFormat, errorItem.getLineId(), errorItem.getEmployeeId(), errorItem.getLastName(),
                errorItem.getFirstName(), errorItem.getMiddleInitial(), errorItem.getChartOfAccountsCode(), errorItem.getAccountNumber(),
                errorItem.getSubAccountNumber(), errorItem.getObjectCode(), errorItem.getSubObjectCode(), errorItem.getProjectCode(),
                errorItem.getOrgRefId(), errorItem.getLineAmount(), errorItem.getPolicyName(), errorItem.getExpenseTypeName());
    }

    protected void writeErrorResultsForErrorItem(ConcurBatchReportLineValidationErrorItem errorItem) {
        getReportWriterService().writeNewLines(1);
        writeErrorItemMessages(errorItem.getItemErrorResults());
        getReportWriterService().writeNewLines(1);
    }

    private void writeErrorItemMessages(List<String> errorMessages) {
        LOG.debug("writeErrorItemMessages, entered");
        if (CollectionUtils.isEmpty(errorMessages)) {
            getReportWriterService().writeFormattedMessageLine(ConcurConstants.StandardAccountingExtractReport.NO_VALIDATION_ERROR_MESSAGES_TO_OUTPUT);
        }
        else {
            for (String errorMessage : errorMessages) {
                getReportWriterService().writeFormattedMessageLine(errorMessage);
            }
        }
    }

    protected void finalizeReport() {
        LOG.debug("finalizeReport, entered");
        getReportWriterService().writeNewLines(3);
        getReportWriterService().writeFormattedMessageLine(ConcurConstants.StandardAccountingExtractReport.END_OF_REPORT_MESSAGE);
        getReportWriterService().destroy();
    }

    private String convertConcurFileNameToDefaultWhenNotProvided(String concurFileName) {
        if (StringUtils.isEmpty(concurFileName)) {
            return ConcurConstants.StandardAccountingExtractReport.UNKNOWN_SAE_FILENAME;
        }
        else {
            return concurFileName;
        }
    }

    private String buildConcurReportFileNamePrefix(String concurFileName, String prefixFirstPart, String prefixSecondPart) {
        return (prefixFirstPart + (StringUtils.substringBeforeLast(concurFileName, ConcurConstants.FILE_EXTENSION_DELIMITTER)) + prefixSecondPart);
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
