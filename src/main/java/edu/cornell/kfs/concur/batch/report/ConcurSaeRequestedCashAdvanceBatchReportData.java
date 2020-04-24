package edu.cornell.kfs.concur.batch.report;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.batch.report.ConcurBatchReportSummaryItem;

public class ConcurSaeRequestedCashAdvanceBatchReportData implements ConcurEmailableReportData {
    
    private String concurFileName;
    private List<String> headerValidationErrors;
    private ConcurBatchReportSummaryItem cashAdvancesProcessedInPdp;
    private ConcurBatchReportSummaryItem cashAdvancesBypassedRelatedToExpenseReport;
    private ConcurBatchReportSummaryItem recordsBypassedNotCashAdvances;
    private ConcurBatchReportSummaryItem duplicateCashAdvanceRequests;
    private ConcurBatchReportSummaryItem clonedCashAdvanceRequests;
    private ConcurBatchReportSummaryItem cashAdvancesNotProcessedValidationErrors;
    private ConcurBatchReportSummaryItem totalsForFile;
    private List<ConcurBatchReportLineValidationErrorItem> validationErrorFileLines;
    
    public ConcurSaeRequestedCashAdvanceBatchReportData () {
        this.concurFileName = KFSConstants.EMPTY_STRING;
        this.headerValidationErrors = new ArrayList<String>();
        this.cashAdvancesProcessedInPdp = new ConcurBatchReportSummaryItem();
        this.cashAdvancesBypassedRelatedToExpenseReport = new ConcurBatchReportSummaryItem();
        this.recordsBypassedNotCashAdvances = new ConcurBatchReportSummaryItem();
        this.duplicateCashAdvanceRequests = new ConcurBatchReportSummaryItem();
        this.clonedCashAdvanceRequests = new ConcurBatchReportSummaryItem();
        this.cashAdvancesNotProcessedValidationErrors = new ConcurBatchReportSummaryItem();
        this.totalsForFile = new ConcurBatchReportSummaryItem();
        this.validationErrorFileLines = new ArrayList<ConcurBatchReportLineValidationErrorItem>();
    }
   
    public ConcurSaeRequestedCashAdvanceBatchReportData (String concurFileName,
            List<String> headerValidationErrors,
            ConcurBatchReportSummaryItem cashAdvancesProcessedInPdp,
            ConcurBatchReportSummaryItem cashAdvancesBypassedRelatedToExpenseReport,
            ConcurBatchReportSummaryItem recordsBypassedNotCashAdvances,
            ConcurBatchReportSummaryItem duplicateCashAdvanceRequests,
            ConcurBatchReportSummaryItem clonedCashAdvanceRequests,
            ConcurBatchReportSummaryItem cashAdvancesNotProcessedValidationErrors,
            ConcurBatchReportSummaryItem totalsForFile,
            List<ConcurBatchReportLineValidationErrorItem> validationErrorFileLines) {
        this.concurFileName = concurFileName;
        this.headerValidationErrors = headerValidationErrors;
        this.cashAdvancesProcessedInPdp = cashAdvancesProcessedInPdp;
        this.cashAdvancesBypassedRelatedToExpenseReport = cashAdvancesBypassedRelatedToExpenseReport;
        this.recordsBypassedNotCashAdvances = recordsBypassedNotCashAdvances;
        this.duplicateCashAdvanceRequests = duplicateCashAdvanceRequests;
        this.clonedCashAdvanceRequests = clonedCashAdvanceRequests;
        this.cashAdvancesNotProcessedValidationErrors = cashAdvancesNotProcessedValidationErrors;
        this.totalsForFile = totalsForFile;
        this.validationErrorFileLines = validationErrorFileLines;
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

    public ConcurBatchReportSummaryItem getCashAdvancesProcessedInPdp() {
        return cashAdvancesProcessedInPdp;
    }

    public void setCashAdvancesProcessedInPdp(ConcurBatchReportSummaryItem cashAdvancesProcessedInPdp) {
        this.cashAdvancesProcessedInPdp = cashAdvancesProcessedInPdp;
    }

    public ConcurBatchReportSummaryItem getCashAdvancesBypassedRelatedToExpenseReport() {
        return cashAdvancesBypassedRelatedToExpenseReport;
    }

    public void setCashAdvancesBypassedRelatedToExpenseReport(
            ConcurBatchReportSummaryItem cashAdvancesBypassedRelatedToExpenseReport) {
        this.cashAdvancesBypassedRelatedToExpenseReport = cashAdvancesBypassedRelatedToExpenseReport;
    }

    public ConcurBatchReportSummaryItem getRecordsBypassedNotCashAdvances() {
        return recordsBypassedNotCashAdvances;
    }

    public void setRecordsBypassedNotCashAdvances(ConcurBatchReportSummaryItem recordsBypassedNotCashAdvances) {
        this.recordsBypassedNotCashAdvances = recordsBypassedNotCashAdvances;
    }

    public ConcurBatchReportSummaryItem getDuplicateCashAdvanceRequests() {
        return duplicateCashAdvanceRequests;
    }

    public void setDuplicateCashAdvanceRequests(ConcurBatchReportSummaryItem duplicateCashAdvanceRequests) {
        this.duplicateCashAdvanceRequests = duplicateCashAdvanceRequests;
    }

    public ConcurBatchReportSummaryItem getClonedCashAdvanceRequests() {
        return clonedCashAdvanceRequests;
    }

    public void setClonedCashAdvanceRequests(ConcurBatchReportSummaryItem clonedCashAdvanceRequests) {
        this.clonedCashAdvanceRequests = clonedCashAdvanceRequests;
    }

    public ConcurBatchReportSummaryItem getCashAdvancesNotProcessedValidationErrors() {
        return cashAdvancesNotProcessedValidationErrors;
    }

    public void setCashAdvancesNotProcessedValidationErrors(
            ConcurBatchReportSummaryItem cashAdvancesNotProcessedValidationErrors) {
        this.cashAdvancesNotProcessedValidationErrors = cashAdvancesNotProcessedValidationErrors;
    }

    public ConcurBatchReportSummaryItem getTotalsForFile() {
        return totalsForFile;
    }

    public void setTotalsForFile(ConcurBatchReportSummaryItem totalsForFile) {
        this.totalsForFile = totalsForFile;
    }
    
    @Override
    public List<ConcurBatchReportLineValidationErrorItem> getValidationErrorFileLines() {
        return validationErrorFileLines;
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
    
    @Override
    public String getReportTypeName() {
        return "sae requested cash advances";
    }

}
