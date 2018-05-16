package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.fp.businessobject.BudgetAdjustmentSourceAccountingLine;

/**
 * Test-only BA source accounting line that avoids SpringContext.getBean() calls in the "setAccountNumber" setter.
 */
public class TestBudgetAdjustmentSourceAccountingLine extends BudgetAdjustmentSourceAccountingLine {
    private static final long serialVersionUID = 1L;

    @Override
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

}
