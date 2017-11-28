package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.sys.businessobject.SourceAccountingLine;

/**
 * Test-only source accounting line that avoids SpringContext.getBean() calls in the "setAccountNumber" setter.
 */
public class TestSourceAccountingLine extends SourceAccountingLine {
    private static final long serialVersionUID = 1L;

    @Override
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

}
