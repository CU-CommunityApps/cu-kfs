package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.sys.businessobject.TargetAccountingLine;

/**
 * Test-only target accounting line that avoids SpringContext.getBean() calls in the "setAccountNumber" setter.
 */
public class TestTargetAccountingLine extends TargetAccountingLine {
    private static final long serialVersionUID = 1L;

    @Override
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

}
