package edu.cornell.kfs.concur.batch.businessobject;

import java.util.List;

import edu.cornell.kfs.concur.businessobjects.ValidationResult;

public class ConcurRequestExtractRequestDetailLineValidationResult extends ValidationResult {
    private boolean cashAdvanceLine;
    private boolean cashAdvanceUsedInExpenseReport;
    private boolean validCashAdvanceLine;

    public ConcurRequestExtractRequestDetailLineValidationResult() {
        super();
        this.cashAdvanceLine = false;
        this.cashAdvanceUsedInExpenseReport = false;
        this.validCashAdvanceLine = false;
    }

    public boolean isCashAdvanceLine() {
        return cashAdvanceLine;
    }

    public boolean isNotCashAdvanceLine() {
        return !cashAdvanceLine;
    }

    public void setCashAdvanceLine(boolean cashAdvanceLine) {
        this.cashAdvanceLine = cashAdvanceLine;
    }

    public boolean isCashAdvanceUsedInExpenseReport() {
        return cashAdvanceUsedInExpenseReport;
    }

    public boolean isCashAdvanceNotUsedInExpenseReport() {
        return !cashAdvanceUsedInExpenseReport;
    }

    public void setCashAdvanceUsedInExpenseReport(boolean cashAdvanceUsedInExpenseReport) {
        this.cashAdvanceUsedInExpenseReport = cashAdvanceUsedInExpenseReport;
    }

    public boolean isValidCashAdvanceLine() {
        return validCashAdvanceLine;
    }

    public boolean isNotValidCashAdvanceLine() {
        return !validCashAdvanceLine;
    }

    public void setValidCashAdvanceLine(boolean validCashAdvanceLine) {
        this.validCashAdvanceLine = validCashAdvanceLine;
    }

}
