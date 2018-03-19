package edu.cornell.kfs.fp.batch.xml.fixture;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import static edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils.defaultToEmptyStringIfBlank;

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
    ACCT_R504707_OBJ_2643_AMOUNT_100_04("WX", "R504707", "35799", "2643", "987", "JX-111999", "333GGG", "This is the target line!", 100.04),

    ACCT_1658328_OBJ_6600_AMOUNT_1("IT", "1658328", null, "6600", null, null, null, null, 1.00),
    ACCT_1658328_OBJ_6600_AMOUNT_2("IT", "1658328", null, "6600", null, null, null, null, 2.00),
    ACCT_165835X_OBJ_6600_AMOUNT_3("IT", "165835X", null, "6600", null, null, null, null, 3.00),
    ACCT_R583805_SA_70170_OBJ_6600_AMOUNT_4("IT", "R583805", "70170", "6600", null, null, null, null, 4.00),
    ACCT_R583805_OBJ_6600_AMOUNT_5("IT", "R583805", null, "6600", null, null, null, null, 5.00),
    ACCT_R589966_OBJ_1000_AMOUNT_6("IT", "R589966", "NONCA", "1000", null, "EB-PLGIFT", "AEH56", null, 6.00),
    ACCT_1023715_OBJ_4020_SO109_AMOUNT_7("IT", "1023715", "97601", "4020", "109", null, "AEH56", null, 7.00),
    ACCT_1023715_OBJ_4020_AMOUNT_8("IT", "1023715", "97601", "4020", null, null, "AEH56", null, 8.00),
    ACCT_R589966_OBJ_1000_AMOUNT_9("IT", "R589966", null, "1000", null, null, "AEH56", null, 9.00),
    ACCT_J801000_SA_SHAN_OBJ_6600_AMOUNT_10("CS", "J801000", "SHAN", "6600", null, null, null, null, 10.00),
    ACCT_J801000_OBJ_6600_AMOUNT_11("CS", "J801000", null, "6600", null, null, null, null, 11.00),
    ACCT_NONE_OBJ_6600_AMOUNT_12("IT", "Internal", null, "6600", null, null, null, null, 12.00),
    ACCT_1023715_OBJ_4020_AMOUNT_13_INVALID("IT", "IT*1023715*97601*4020*109**AEH56*BAR", null, "6600", null, null, null, null, 13.00);

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

    public <T extends AccountingLine> T toAccountingLineBo(Class<T> accountingLineClass, String documentNumber) {
        try {
            T accountingLine = accountingLineClass.newInstance();
            accountingLine.setDocumentNumber(documentNumber);
            accountingLine.setChartOfAccountsCode(chartCode);
            accountingLine.setAccountNumber(accountNumber);
            accountingLine.setSubAccountNumber(subAccountNumber);
            accountingLine.setFinancialObjectCode(objectCode);
            accountingLine.setFinancialSubObjectCode(subObjectCode);
            accountingLine.setProjectCode(projectCode);
            accountingLine.setOrganizationReferenceId(orgRefId);
            accountingLine.setFinancialDocumentLineDescription(lineDescription);
            accountingLine.setAmount(amount);
            return accountingLine;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

}
