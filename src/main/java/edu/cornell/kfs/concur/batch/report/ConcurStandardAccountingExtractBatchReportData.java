package edu.cornell.kfs.concur.batch.report;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.batch.report.ConcurBatchReportHeaderValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportLineValidationErrorItem;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportMissingObjectCodeItem;
import edu.cornell.kfs.concur.batch.report.ConcurBatchReportSummaryItem;

public class ConcurStandardAccountingExtractBatchReportData {
    
    private String concurFileName;
    private ConcurBatchReportHeaderValidationErrorItem headerValidationErrors;
    private ConcurBatchReportSummaryItem reimbursementsInExpenseReport;
    private ConcurBatchReportSummaryItem cashAdvancesRelatedToExpenseReports;
    private ConcurBatchReportSummaryItem expensesPaidOnCorporateCard;
    private ConcurBatchReportSummaryItem transactionsBypassed;
    private ConcurBatchReportSummaryItem pdpRecordsProcessed;
    private List<ConcurBatchReportLineValidationErrorItem> validationErrorFileLines;
    private List<ConcurBatchReportMissingObjectCodeItem> pendingClientObjectCodeLines; 
    
    public ConcurStandardAccountingExtractBatchReportData() {
        this.concurFileName = KFSConstants.EMPTY_STRING;
        this.reimbursementsInExpenseReport = new ConcurBatchReportSummaryItem();
        this.cashAdvancesRelatedToExpenseReports = new ConcurBatchReportSummaryItem();
        this.expensesPaidOnCorporateCard = new ConcurBatchReportSummaryItem();
        this.transactionsBypassed = new ConcurBatchReportSummaryItem();
        this.pdpRecordsProcessed = new ConcurBatchReportSummaryItem();
        this.validationErrorFileLines = new ArrayList<ConcurBatchReportLineValidationErrorItem>();
        this.pendingClientObjectCodeLines = new ArrayList<ConcurBatchReportMissingObjectCodeItem>();
    }
    
    public ConcurStandardAccountingExtractBatchReportData(String concurFileName,
            ConcurBatchReportHeaderValidationErrorItem headerValidationErrors,
            ConcurBatchReportSummaryItem reimbursementsInExpenseReport,
            ConcurBatchReportSummaryItem cashAdvancesRelatedToExpenseReports,
            ConcurBatchReportSummaryItem expensesPaidOnCorporateCard,
            ConcurBatchReportSummaryItem transactionsBypassed,
            ConcurBatchReportSummaryItem pdpRecordsProcessed,
            List<ConcurBatchReportLineValidationErrorItem> validationErrorFileLines,
            List<ConcurBatchReportMissingObjectCodeItem> pendingClientObjectCodeLines) {
        this.headerValidationErrors = headerValidationErrors;
        this.concurFileName = concurFileName;
        this.reimbursementsInExpenseReport = reimbursementsInExpenseReport;
        this.cashAdvancesRelatedToExpenseReports = cashAdvancesRelatedToExpenseReports;
        this.expensesPaidOnCorporateCard = expensesPaidOnCorporateCard;
        this.transactionsBypassed = transactionsBypassed;
        this.pdpRecordsProcessed = pdpRecordsProcessed;
        this.validationErrorFileLines = validationErrorFileLines;
        this.pendingClientObjectCodeLines = pendingClientObjectCodeLines;
    }
    
    public String getConcurFileName() {
        return concurFileName;
    }

    public void setConcurFileName(String concurFileName) {
        this.concurFileName = concurFileName;
    }

    public ConcurBatchReportHeaderValidationErrorItem getHeaderValidationErrors() {
        return headerValidationErrors;
    }

    public void setHeaderValidationErrors(ConcurBatchReportHeaderValidationErrorItem headerValidationErrors) {
        this.headerValidationErrors = headerValidationErrors;
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

}
