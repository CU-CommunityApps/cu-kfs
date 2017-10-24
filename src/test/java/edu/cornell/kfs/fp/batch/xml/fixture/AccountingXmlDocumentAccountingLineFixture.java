package edu.cornell.kfs.fp.batch.xml.fixture;

import static edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils.defaultToEmptyStringIfBlank;

import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;

public enum AccountingXmlDocumentAccountingLineFixture {
    ACCT_R504700_OBJ_2640_AMOUNT_100("IT", "R504700", null, "2640", null, null, null, null, 100.00),
    ACCT_1000718_OBJ_4000_AMOUNT_50("IT", "1000718", null, "4000", null, null, null, null, 50.00),
    ACCT_R504706_OBJ_2640_AMOUNT_100("IT", "R504706", null, "2640", null, null, null, null, 100.00),
    ACCT_1000710_OBJ_4000_AMOUNT_50("IT", "1000710", null, "4000", null, null, null, null, 50.00),
    ACCT_R504700_OBJ_2640_AMOUNT_1000(ACCT_R504700_OBJ_2640_AMOUNT_100, 1000.00),
    ACCT_1000718_OBJ_4000_AMOUNT_500(ACCT_1000718_OBJ_4000_AMOUNT_50, 500.00),
    ACCT_R504706_OBJ_2640_AMOUNT_1000(ACCT_R504706_OBJ_2640_AMOUNT_100, 1000.00),
    ACCT_1000710_OBJ_4000_AMOUNT_500(ACCT_1000710_OBJ_4000_AMOUNT_50, 500.00),
    ACCT_R504701_OBJ_2641_AMOUNT_100_04("IT", "R504701", "13579", "2641", "888", "ZZ-654321", "642CBA", "The source line", 100.04),
    ACCT_R504707_OBJ_2643_AMOUNT_100_04("WX", "R504707", "35799", "2643", "987", "JX-111999", "333GGG", "This is the target line!", 100.04);

    public final String chartCode;
    public final String accountNumber;
    public final String subAccountNumber;
    public final String objectCode;
    public final String subObjectCode;
    public final String projectCode;
    public final String orgRefId;
    public final String lineDescription;
    public final KualiDecimal amount;

    private AccountingXmlDocumentAccountingLineFixture(
            AccountingXmlDocumentAccountingLineFixture baseFixture, double newAmount) {
        this(baseFixture.chartCode, baseFixture.accountNumber, baseFixture.subAccountNumber,
                baseFixture.objectCode, baseFixture.subObjectCode, baseFixture.projectCode, baseFixture.orgRefId,
                baseFixture.lineDescription, newAmount);
    }

    private AccountingXmlDocumentAccountingLineFixture(String chartCode, String accountNumber, String subAccountNumber,
            String objectCode, String subObjectCode, String projectCode, String orgRefId,
            String lineDescription, double amount) {
        this.chartCode = chartCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = defaultToEmptyStringIfBlank(subAccountNumber);
        this.objectCode = objectCode;
        this.subObjectCode = defaultToEmptyStringIfBlank(subObjectCode);
        this.projectCode = defaultToEmptyStringIfBlank(projectCode);
        this.orgRefId = defaultToEmptyStringIfBlank(orgRefId);
        this.lineDescription = defaultToEmptyStringIfBlank(lineDescription);
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
