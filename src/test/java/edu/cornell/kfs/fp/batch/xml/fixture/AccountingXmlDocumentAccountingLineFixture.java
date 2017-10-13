package edu.cornell.kfs.fp.batch.xml.fixture;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;

public enum AccountingXmlDocumentAccountingLineFixture {
    TEST_ACCOUNT1(null, null, null, null, null, null, null, null, 0);

    public final String chartCode;
    public final String accountNumber;
    public final String subAccountNumber;
    public final String objectCode;
    public final String subObjectCode;
    public final String projectCode;
    public final String orgRefId;
    public final String lineDescription;
    public final KualiDecimal amount;

    private AccountingXmlDocumentAccountingLineFixture(String chartCode, String accountNumber, String subAccountNumber,
            String objectCode, String subObjectCode, String projectCode, String orgRefId,
            String lineDescription, double amount) {
        this.chartCode = chartCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = subAccountNumber;
        this.objectCode = objectCode;
        this.subObjectCode = subObjectCode;
        this.projectCode = projectCode;
        this.orgRefId = orgRefId;
        this.lineDescription = lineDescription;
        this.amount = new KualiDecimal(amount);
    }

    public AccountingXmlDocumentAccountingLine toAccountingLinePojo() {
        AccountingXmlDocumentAccountingLine accountingLine = new AccountingXmlDocumentAccountingLine();
        accountingLine.setChartCode(chartCode);
        accountingLine.setAccountNumber(accountNumber);
        accountingLine.setSubAccountNumber(subAccountNumber);
        accountingLine.setObjectCode(objectCode);
        accountingLine.setSubObjectCode(subObjectCode);
        accountingLine.setProjectCode(projectCode);
        accountingLine.setOrgRefId(orgRefId);
        accountingLine.setLineDescription(lineDescription);
        accountingLine.setAmount(amount);
        return accountingLine;
    }

}
