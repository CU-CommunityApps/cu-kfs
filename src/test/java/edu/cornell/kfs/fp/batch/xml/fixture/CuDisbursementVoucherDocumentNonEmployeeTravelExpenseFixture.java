package edu.cornell.kfs.fp.batch.xml.fixture;

import org.kuali.rice.core.api.util.type.KualiDecimal;

public enum CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture {
    DELTA("A", "DELTA", 55.55),
    OTHER_LODGING("L", "Other - Lodging", 1555.55),
    PREPAID_AVIS("PR", "AVIS", 23.55),
    PREPAID_OTHER("PO", "PREPAID OTHER", 230.55);
    
    public final String expenseType;
    public final String companyName;
    public final KualiDecimal amount;
    
    private CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture(String expenseType, String companyName, double amount) {
        this.expenseType = expenseType;
        this.companyName = companyName;
        this.amount = new KualiDecimal(amount);
    }
}
