package edu.cornell.kfs.concur.batch.report;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.KFSConstants;

public class ConcurStandardAccountingExtractBatchReportData implements ConcurEmailableReportData {
    
    private String concurFileName;
    private List<String> headerValidationErrors;
    private List<ConcurBatchReportRemovedCharactersWarningItem> linesWithRemovedCharacters;
    private ConcurBatchReportSummaryItem reimbursementsInExpenseReport;
    private ConcurBatchReportSummaryItem cashAdvancesRelatedToExpenseReports;
    private ConcurBatchReportSummaryItem expensesPaidOnCorporateCard;
    private ConcurBatchReportSummaryItem transactionsBypassed;
    private ConcurBatchReportSummaryItem pdpRecordsProcessed;
    private ConcurBatchReportSummaryItem cashAdvanceRequestsBypassed;
    private List<ConcurBatchReportLineValidationErrorItem> validationErrorFileLines;
    private List<ConcurBatchReportMissingObjectCodeItem> pendingClientObjectCodeLines; 

    public ConcurStandardAccountingExtractBatchReportData() {
        this.concurFileName = KFSConstants.EMPTY_STRING;
        this.headerValidationErrors = new ArrayList<String>();
        this.linesWithRemovedCharacters = new ArrayList<>();
        this.reimbursementsInExpenseReport = new ConcurBatchReportSummaryItem();
        this.cashAdvancesRelatedToExpenseReports = new ConcurBatchReportSummaryItem();
        this.expensesPaidOnCorporateCard = new ConcurBatchReportSummaryItem();
        this.transactionsBypassed = new ConcurBatchReportSummaryItem();
        this.pdpRecordsProcessed = new ConcurBatchReportSummaryItem();
        this.cashAdvanceRequestsBypassed = new ConcurBatchReportSummaryItem();
        this.validationErrorFileLines = new ArrayList<ConcurBatchReportLineValidationErrorItem>();
        this.pendingClientObjectCodeLines = new ArrayList<ConcurBatchReportMissingObjectCodeItem>();
    }
    
    public ConcurStandardAccountingExtractBatchReportData(String concurFileName,
            List<String> headerValidationErrors,
            List<ConcurBatchReportRemovedCharactersWarningItem> linesWithRemovedCharacters,
            ConcurBatchReportSummaryItem reimbursementsInExpenseReport,
            ConcurBatchReportSummaryItem cashAdvancesRelatedToExpenseReports,
            ConcurBatchReportSummaryItem expensesPaidOnCorporateCard,
            ConcurBatchReportSummaryItem transactionsBypassed,
            ConcurBatchReportSummaryItem pdpRecordsProcessed,
            ConcurBatchReportSummaryItem cashAdvanceRequestsBypassed,
            List<ConcurBatchReportLineValidationErrorItem> validationErrorFileLines,
            List<ConcurBatchReportMissingObjectCodeItem> pendingClientObjectCodeLines) {
        this.headerValidationErrors = headerValidationErrors;
        this.linesWithRemovedCharacters = linesWithRemovedCharacters;
        this.concurFileName = concurFileName;
        this.reimbursementsInExpenseReport = reimbursementsInExpenseReport;
        this.cashAdvancesRelatedToExpenseReports = cashAdvancesRelatedToExpenseReports;
        this.expensesPaidOnCorporateCard = expensesPaidOnCorporateCard;
        this.transactionsBypassed = transactionsBypassed;
        this.pdpRecordsProcessed = pdpRecordsProcessed;
        this.cashAdvanceRequestsBypassed = cashAdvanceRequestsBypassed;
        this.validationErrorFileLines = validationErrorFileLines;
        this.pendingClientObjectCodeLines = pendingClientObjectCodeLines;
    }
    
    @Override
    public String getConcurFileName() {
        return concurFileName;
    }

    public void setConcurFileName(String concurFileName) {
        this.concurFileName = concurFileName;
    }
    
    @Override
    public List<String> getHeaderValidationErrors() {
        return headerValidationErrors;
    }

    public void setHeaderValidationErrors(List<String> headerValidationErrors) {
        this.headerValidationErrors = headerValidationErrors;
    }

    public void addHeaderValidationError(String headerValidationError) {
        if (headerValidationErrors == null) {
            headerValidationErrors = new ArrayList<String>();
        }
        this.headerValidationErrors.add(headerValidationError);
    }

    public List<ConcurBatchReportRemovedCharactersWarningItem> getLinesWithRemovedCharacters() {
        return linesWithRemovedCharacters;
    }

    public void setLinesWithRemovedCharacters(
            List<ConcurBatchReportRemovedCharactersWarningItem> linesWithRemovedCharacters) {
        this.linesWithRemovedCharacters = linesWithRemovedCharacters;
    }

    public void addLineWithRemovedCharacters(ConcurBatchReportRemovedCharactersWarningItem lineWithRemovedCharacters) {
        if (linesWithRemovedCharacters == null) {
            linesWithRemovedCharacters = new ArrayList<>();
        }
        linesWithRemovedCharacters.add(lineWithRemovedCharacters);
    }

    public ConcurBatchReportSummaryItem getReimbursementsInExpenseReport() {
        return reimbursementsInExpenseReport;
    }

    public void setReimbursementsInExpenseReport(ConcurBatchReportSummaryItem reimbursementsInExpenseReport) {
        this.reimbursementsInExpenseReport = reimbursementsInExpenseReport;
    }

    public ConcurBatchReportSummaryItem getCashAdvancesRelatedToExpenseReports() {
        return cashAdvancesRelatedToExpenseReports;
    }

    public void setCashAdvancesRelatedToExpenseReports(ConcurBatchReportSummaryItem cashAdvancesRelatedToExpenseReports) {
        this.cashAdvancesRelatedToExpenseReports = cashAdvancesRelatedToExpenseReports;
    }

    public ConcurBatchReportSummaryItem getExpensesPaidOnCorporateCard() {
        return expensesPaidOnCorporateCard;
    }

    public void setExpensesPaidOnCorporateCard(ConcurBatchReportSummaryItem expensesPaidOnCorporateCard) {
        this.expensesPaidOnCorporateCard = expensesPaidOnCorporateCard;
    }

    public ConcurBatchReportSummaryItem getTransactionsBypassed() {
        return transactionsBypassed;
    }

    public void setTransactionsBypassed(ConcurBatchReportSummaryItem transactionsBypassed) {
        this.transactionsBypassed = transactionsBypassed;
    }

    public ConcurBatchReportSummaryItem getPdpRecordsProcessed() {
        return pdpRecordsProcessed;
    }

    public void setPdpRecordsProcessed(ConcurBatchReportSummaryItem pdpRecordsProcessed) {
        this.pdpRecordsProcessed = pdpRecordsProcessed;
    }

    public ConcurBatchReportSummaryItem getCashAdvanceRequestsBypassed() {
        return cashAdvanceRequestsBypassed;
    }

    public void setCashAdvanceRequestsBypassed(ConcurBatchReportSummaryItem cashAdvanceRequestsBypassed) {
        this.cashAdvanceRequestsBypassed = cashAdvanceRequestsBypassed;
    }

    @Override
    public List<ConcurBatchReportLineValidationErrorItem> getValidationErrorFileLines() {
        return this.validationErrorFileLines;
    }

    public void setValidationErrorFileLines(List<ConcurBatchReportLineValidationErrorItem> validationErrorFileLines) {
        this.validationErrorFileLines = validationErrorFileLines;
    }

    public void addValidationErrorFileLine(ConcurBatchReportLineValidationErrorItem validationErrorFileLine) {
        if (validationErrorFileLines == null) {
            validationErrorFileLines = new ArrayList<ConcurBatchReportLineValidationErrorItem>();
        }
        this.validationErrorFileLines.add(validationErrorFileLine);
    }

    public List<ConcurBatchReportMissingObjectCodeItem> getPendingClientObjectCodeLines() {
        return pendingClientObjectCodeLines;
    }

    public void setPendingClientObjectCodeLines(List<ConcurBatchReportMissingObjectCodeItem> pendingClientObjectCodeLines) {
        this.pendingClientObjectCodeLines = pendingClientObjectCodeLines;
    }

    public void addPendingClientObjectCodeLine(ConcurBatchReportMissingObjectCodeItem pendingClientObjectCodeLine) {
        if (pendingClientObjectCodeLines == null) {
            pendingClientObjectCodeLines = new ArrayList<ConcurBatchReportMissingObjectCodeItem>();
        }
        this.pendingClientObjectCodeLines.add(pendingClientObjectCodeLine);
    }
    
    @Override
    public String getReportTypeName() {
        return "standard accounting extract trip reimbursement";
    }

}
