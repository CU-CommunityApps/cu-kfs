package edu.cornell.kfs.fp.batch.xml.fixture;

import static edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils.defaultToEmptyStringIfBlank;

import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

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
    ACCT_R504707_OBJ_2643_AMOUNT_100_04("WX", "R504707", "35799", "2643", "987", "JX-111999", "333GGG", "This is the target line!", 100.04),
    ACCT_G254700_OBJ_4020_AMOUNT_100_INCOME1("IT", "G254700", null, "4020", null, null, null, "Income Line 1", 100.00),
    ACCT_G263700_OBJ_1280_AMOUNT_50_INCOME2("IT", "G263700", null, "1280", null, null, null, "Income Line 2", 50.00),
    ACCT_G254710_OBJ_4020_AMOUNT_100_EXPENSE1("IT", "G254710", null, "4020", null, null, null, "Expense Line 1", 100.00),
    ACCT_G263600_OBJ_1280_AMOUNT_50_EXPENSE2("IT", "G263600", null, "1280", null, null, null, "Expense Line 2", 50.00),
    ACCT_G254700_OBJ_4020_AMOUNT_1000_INCOME1(ACCT_G254700_OBJ_4020_AMOUNT_100_INCOME1, 1000.00),
    ACCT_G263700_OBJ_1280_AMOUNT_500_INCOME2(ACCT_G263700_OBJ_1280_AMOUNT_50_INCOME2, 500.00),
    ACCT_G254710_OBJ_4020_AMOUNT_1000_EXPENSE1(ACCT_G254710_OBJ_4020_AMOUNT_100_EXPENSE1, 1000.00),
    ACCT_G263600_OBJ_1280_AMOUNT_500_EXPENSE2(ACCT_G263600_OBJ_1280_AMOUNT_50_EXPENSE2, 500.00),
    
    ACCT_S524000_SUB_24100_OBJ_8070_SUB_900_AMT_100_FROM("IT", "S524000", "24100", "8070", "900", null, null, "From Line", 100),
    ACCT_S343717_OBJ_7070_AMT_100_TO("IT", "S343717", null, "7070", null, null, null, "To Line", 100),
    ACCT_S524000_SUB_24100_OBJ_8070_SUB_900_AMT_1000_FROM("IT", "S524000", "24100", "8070", "900", null, null, "From Line", 1000),
    ACCT_S343717_OBJ_7070_AMT_1000_TO("IT", "S343717", null, "7070", null, null, null, "To Line", 1000),

    ACCT_1433000_OBJ_4480_AMOUNT_40("IT", "1433000", null, "4480", null, null, null, null, 40.00),
    ACCT_1433000_OBJ_5390_AMOUNT_40("IT", "1433000", null, "5390", null, null, null, null, 40.00),
    ACCT_C200222_OBJ_4480_AMOUNT_40("IT", "C200222", null, "4480", null, null, null, null, 40.00),
    ACCT_C200222_OBJ_5390_AMOUNT_40("IT", "C200222", null, "5390", null, null, null, null, 40.00),

    ACCT_1433000_OBJ_4480_AMOUNT_40_BASE_0_NO_MONTHS(ACCT_1433000_OBJ_4480_AMOUNT_40, 40.00,
            BudgetAdjustmentAccountDataFixture.BASE_0_NO_MONTHS),
    ACCT_1433000_OBJ_5390_AMOUNT_40_BASE_0_MONTH03_40(ACCT_1433000_OBJ_5390_AMOUNT_40, 40.00,
            BudgetAdjustmentAccountDataFixture.BASE_0_MONTH03_40),
    ACCT_C200222_OBJ_4480_AMOUNT_40_BASE_0_NO_MONTHS(ACCT_C200222_OBJ_4480_AMOUNT_40, 40.00,
            BudgetAdjustmentAccountDataFixture.BASE_0_NO_MONTHS),
    ACCT_C200222_OBJ_5390_AMOUNT_40_NO_BASE_MONTH03_40(ACCT_C200222_OBJ_5390_AMOUNT_40, 40.00,
            BudgetAdjustmentAccountDataFixture.NO_BASE_MONTH03_40),

    ACCT_1433000_OBJ_4480_AMOUNT_40_BASE_10_MONTH03_40(ACCT_1433000_OBJ_4480_AMOUNT_40, 40.00,
            BudgetAdjustmentAccountDataFixture.BASE_10_MONTH03_40),
    ACCT_1433000_OBJ_5390_AMOUNT_40_BASE_10_MONTH03_40(ACCT_1433000_OBJ_5390_AMOUNT_40, 40.00,
            BudgetAdjustmentAccountDataFixture.BASE_10_MONTH03_40),
    ACCT_C200222_OBJ_4480_AMOUNT_40_BASE_10_MONTH03_40(ACCT_C200222_OBJ_4480_AMOUNT_40, 40.00,
            BudgetAdjustmentAccountDataFixture.BASE_10_MONTH03_40),
    ACCT_C200222_OBJ_5390_AMOUNT_40_BASE_10_MONTH03_40(ACCT_C200222_OBJ_5390_AMOUNT_40, 40.00,
            BudgetAdjustmentAccountDataFixture.BASE_10_MONTH03_40),

    ACCT_1433000_OBJ_4480_AMOUNT_52_BASE_0_MONTH01_37_MONTH08_15(ACCT_1433000_OBJ_4480_AMOUNT_40, 52.00,
            BudgetAdjustmentAccountDataFixture.BASE_0_MONTH01_37_MONTH08_15),
    ACCT_1433000_OBJ_5390_AMOUNT_52_BASE_0_ALL_MONTHS_4_OR_5(ACCT_1433000_OBJ_5390_AMOUNT_40, 52.00,
            BudgetAdjustmentAccountDataFixture.BASE_0_ALL_MONTHS_4_OR_5),
    ACCT_C200222_OBJ_4480_AMOUNT_52_BASE_0_MONTH01_37_MONTH08_15(ACCT_C200222_OBJ_4480_AMOUNT_40, 52.00,
            BudgetAdjustmentAccountDataFixture.BASE_0_MONTH01_37_MONTH08_15),
    ACCT_C200222_OBJ_5390_AMOUNT_52_BASE_0_ALL_MONTHS_4_OR_5(ACCT_C200222_OBJ_5390_AMOUNT_40, 52.00,
            BudgetAdjustmentAccountDataFixture.BASE_0_ALL_MONTHS_4_OR_5),

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
    ACCT_INTERNAL_OBJ_6600_AMOUNT_12("IT", "Internal", null, "6600", null, null, null, null, 12.00),
    CHART_CS_ACCT_J801000_OBJ_4020_AMOUNT_50_VALID("CS", "J801000", null, "6600", null, null, null, null, 50.00),
    ACCT_1023715_OBJ_4020_AMOUNT_13_INVALID("IT", "IT*1023715*97601*4020*109**AEH56*BAR", null, "6600", null, null, null, null, 13.00),
    CHART_IT_ACCT_R583805_70170_OBJ_6600_AMOUNT_12_VALID("IT", "R583805", "70170", "6600", null, null, null, null, 12.00),
    CHART_IT_ACCT_R589966_NONCA_OBJ_1000_AMOUNT_12_VALID("IT", "R589966", "NONCA", "1000", null, "EB-PLGIFT", "AEH56", null, 12.00),
    CHART_IT_ACCT_R589966_NONCA_OBJ_1000_10X_AMOUNT_12_VALID("IT", "R589966", "NONCA", "1000", "10X", "EB-PLGIFT", "AEH56", null, 12.00),
    ACCT_1433000_OBJ_4480_DEBIT_55("IT", "1433000", null, "4480", null, null, null, null, 55.00, 0),
    ACCT_C200222_OBJ_5390_CREDIT_55("IT", "C200222", null, "5390", null, null, null, null, 0, 55.00),
    ACCT_1003163_OBJ_6900_AMT_50_ENCUM_LINE("IT", "1003163", null, "6900", null, null, null, "Encumbrance Line", 50),
    ACCT_1533039_OBJ_6900_AMT_50_REF_NUMBER("IT", "1533039", null, "6900", null, null, null, null, 50, "RefNumber");

    public final String chartCode;
    public final String accountNumber;
    public final String subAccountNumber;
    public final String objectCode;
    public final String subObjectCode;
    public final String projectCode;
    public final String orgRefId;
    public final String lineDescription;
    public final KualiDecimal amount;
    public final KualiDecimal debitAmount;
    public final KualiDecimal creditAmount;
    public final BudgetAdjustmentAccountDataFixture budgetAdjustmentData;
    public final String referenceNumber;

    private AccountingXmlDocumentAccountingLineFixture(
            AccountingXmlDocumentAccountingLineFixture baseFixture, double newAmount) {
        this(baseFixture.chartCode, baseFixture.accountNumber, baseFixture.subAccountNumber,
                baseFixture.objectCode, baseFixture.subObjectCode, baseFixture.projectCode, baseFixture.orgRefId,
                baseFixture.lineDescription, newAmount);
    }

    private AccountingXmlDocumentAccountingLineFixture(AccountingXmlDocumentAccountingLineFixture baseFixture,
            double newAmount, BudgetAdjustmentAccountDataFixture newBudgetAdjustmentData) {
        this(baseFixture.chartCode, baseFixture.accountNumber, baseFixture.subAccountNumber,
                baseFixture.objectCode, baseFixture.subObjectCode, baseFixture.projectCode, baseFixture.orgRefId,
                baseFixture.lineDescription, newAmount, 0, 0, newBudgetAdjustmentData, null);
    }

    private AccountingXmlDocumentAccountingLineFixture(String chartCode, String accountNumber, String subAccountNumber,
            String objectCode, String subObjectCode, String projectCode, String orgRefId,
            String lineDescription, double amount) {
        this(chartCode, accountNumber, subAccountNumber, objectCode, subObjectCode, projectCode, orgRefId,
                lineDescription, amount, 0, 0, null, null);
    }
    
    private AccountingXmlDocumentAccountingLineFixture(String chartCode, String accountNumber, String subAccountNumber,
            String objectCode, String subObjectCode, String projectCode, String orgRefId,
            String lineDescription, double amount, String referenceNumber) {
        this(chartCode, accountNumber, subAccountNumber, objectCode, subObjectCode, projectCode, orgRefId,
                lineDescription, amount, 0, 0, null, referenceNumber);
    }

    private AccountingXmlDocumentAccountingLineFixture(String chartCode, String accountNumber, String subAccountNumber,
            String objectCode, String subObjectCode, String projectCode, String orgRefId,
            String lineDescription, double debitAmount, double creditAmount) {
        this(chartCode, accountNumber, subAccountNumber, objectCode, subObjectCode, projectCode, orgRefId,
                lineDescription, 0, debitAmount, creditAmount, null, null);
    }

    private AccountingXmlDocumentAccountingLineFixture(String chartCode, String accountNumber, String subAccountNumber,
            String objectCode, String subObjectCode, String projectCode, String orgRefId,
            String lineDescription, double amount, double debitAmount, double creditAmount, 
            BudgetAdjustmentAccountDataFixture budgetAdjustmentData, String referenceNumber) {
        this.chartCode = chartCode;
        this.accountNumber = accountNumber;
        this.subAccountNumber = defaultToEmptyStringIfBlank(subAccountNumber);
        this.objectCode = objectCode;
        this.subObjectCode = defaultToEmptyStringIfBlank(subObjectCode);
        this.projectCode = defaultToEmptyStringIfBlank(projectCode);
        this.orgRefId = defaultToEmptyStringIfBlank(orgRefId);
        this.lineDescription = defaultToEmptyStringIfBlank(lineDescription);
        this.amount = new KualiDecimal(amount);
        this.debitAmount = new KualiDecimal(debitAmount);
        this.creditAmount = new KualiDecimal(creditAmount);
        this.budgetAdjustmentData = budgetAdjustmentData;
        this.referenceNumber = referenceNumber;
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
        
        if (!KualiDecimal.ZERO.equals(debitAmount)) {
            accountingLine.setDebitAmount(debitAmount);
        } else if (!KualiDecimal.ZERO.equals(creditAmount)) {
            accountingLine.setCreditAmount(creditAmount);
        } else {
            accountingLine.setAmount(amount);
        }
        
        if (budgetAdjustmentData != null) {
            budgetAdjustmentData.configureAccountingLineXmlPojo(accountingLine);
        }
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
            setAccountingLineAmountAndDebitCreditCode(accountingLine);
            if (accountingLine instanceof BudgetAdjustmentAccountingLine) {
                BudgetAdjustmentAccountingLine budgetAdjustmentLine = (BudgetAdjustmentAccountingLine) accountingLine;
                budgetAdjustmentLine.setCurrentBudgetAdjustmentAmount(amount);
                if (budgetAdjustmentData != null) {
                    budgetAdjustmentData.configureBudgetAdjustmentAccountingLine(budgetAdjustmentLine);
                }
            }
            accountingLine.setReferenceNumber(referenceNumber);
            return accountingLine;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    private <T extends AccountingLine> void setAccountingLineAmountAndDebitCreditCode(T accountingLine) {
        if (!KualiDecimal.ZERO.equals(debitAmount)) {
            accountingLine.setAmount(debitAmount);
            accountingLine.setDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
        } else if (!KualiDecimal.ZERO.equals(creditAmount)) {
            accountingLine.setAmount(creditAmount);
            accountingLine.setDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
        } else {
            accountingLine.setAmount(amount);
        }
    }

}
