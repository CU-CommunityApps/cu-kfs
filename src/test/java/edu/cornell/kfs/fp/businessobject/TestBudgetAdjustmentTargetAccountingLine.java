package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.fp.businessobject.BudgetAdjustmentTargetAccountingLine;

/**
 * Test-only BA target accounting line that avoids SpringContext.getBean() calls in the "setAccountNumber" setter.
 */
public class TestBudgetAdjustmentTargetAccountingLine extends BudgetAdjustmentTargetAccountingLine {
    private static final long serialVersionUID = 1L;

    @Override
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

}
