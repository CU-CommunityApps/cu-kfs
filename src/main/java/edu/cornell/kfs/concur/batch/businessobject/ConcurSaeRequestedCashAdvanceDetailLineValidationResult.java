package edu.cornell.kfs.concur.batch.businessobject;

import edu.cornell.kfs.concur.businessobjects.ValidationResult;

public class ConcurSaeRequestedCashAdvanceDetailLineValidationResult extends ValidationResult {
    private boolean cashAdvanceLine;
    private boolean cashAdvanceApprovedOrApplied;
    private boolean clonedCashAdvance;
    private boolean cashAdvanceUsedInExpenseReport;
    private boolean duplicatedCashAdvanceLine;
    private boolean validAddressWhenCheckPaymentForCashAdvance;
    private boolean cashAdvanceAccountingDataValid;
    private boolean validCashAdvanceLine;

    public ConcurSaeRequestedCashAdvanceDetailLineValidationResult() {
        super();
        this.cashAdvanceLine = false;
        this.cashAdvanceApprovedOrApplied = false;
        this.clonedCashAdvance = true;
        this.cashAdvanceUsedInExpenseReport = true;
        this.duplicatedCashAdvanceLine = true;
        this.validAddressWhenCheckPaymentForCashAdvance = false;
        this.cashAdvanceAccountingDataValid = false;
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

    public boolean isCashAdvanceApprovedOrApplied() {
        return cashAdvanceApprovedOrApplied;
    }

    public void setCashAdvanceApprovedOrApplied(boolean cashAdvanceApprovedOrApplied) {
        this.cashAdvanceApprovedOrApplied = cashAdvanceApprovedOrApplied;
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

    public boolean isDuplicatedCashAdvanceLine() {
        return duplicatedCashAdvanceLine;
    }

    public boolean isNotDuplicatedCashAdvanceLine() {
        return !duplicatedCashAdvanceLine;
    }

    public void setDuplicatedCashAdvanceLine(boolean duplicatedCashAdvanceLine) {
        this.duplicatedCashAdvanceLine = duplicatedCashAdvanceLine;
    }

    public boolean isValidAddressWhenCheckPaymentForCashAdvance() {
        return validAddressWhenCheckPaymentForCashAdvance;
    }
    
    public boolean isNotValidAddressWhenCheckPaymentForCashAdvance() {
        return !validAddressWhenCheckPaymentForCashAdvance;
    }

    public void setValidAddressWhenCheckPaymentForCashAdvance(boolean validAddressWhenCheckPaymentForCashAdvance) {
        this.validAddressWhenCheckPaymentForCashAdvance = validAddressWhenCheckPaymentForCashAdvance;
    }

    public boolean isCashAdvanceAccountingDataValid() {
        return cashAdvanceAccountingDataValid;
    }
    
    public boolean isCashAdvanceAccountingDataNotValid() {
        return !cashAdvanceAccountingDataValid;
    }

    public void setCashAdvanceAccountingDataValid(boolean cashAdvanceAccountingDataValid) {
        this.cashAdvanceAccountingDataValid = cashAdvanceAccountingDataValid;
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

    public boolean isClonedCashAdvance() {
        return clonedCashAdvance;
    }
    
    public boolean isNotClonedCashAdvance() {
        return !clonedCashAdvance;
    }

    public void setClonedCashAdvance(boolean clonedCashAdvance) {
        this.clonedCashAdvance = clonedCashAdvance;
    }

}
