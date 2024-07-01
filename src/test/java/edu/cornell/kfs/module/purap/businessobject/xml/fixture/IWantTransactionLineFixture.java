package edu.cornell.kfs.module.purap.businessobject.xml.fixture;

import java.math.BigDecimal;

import edu.cornell.kfs.module.purap.businessobject.xml.IWantTransactionLineXml;
import edu.cornell.kfs.module.purap.businessobject.xml.IWantXmlConstants.IWantAmountOrPercentTypeXml;

public enum IWantTransactionLineFixture {

    TRANSACTION_LINE_TEST("chart", "account", "object code", "sub account", "sub object code", "project code",
            "reference id", BigDecimal.valueOf(666.66), IWantAmountOrPercentTypeXml.A);

    public final String chartOfAccountsCode;
    public final String accountNumber;
    public final String financialObjectCode;
    public final String subAccountNumber;
    public final String financialSubObjectCode;
    public final String projectCode;
    public final String organizationReferenceId;
    public final BigDecimal amountOrPercent;
    public final IWantAmountOrPercentTypeXml useAmountOrPercent;

    private IWantTransactionLineFixture(String chartOfAccountsCode, String accountNumber, String financialObjectCode,
            String subAccountNumber, String financialSubObjectCode, String projectCode, String organizationReferenceId,
            BigDecimal amountOrPercent, IWantAmountOrPercentTypeXml useAmountOrPercent) {
        this.chartOfAccountsCode = chartOfAccountsCode;
        this.accountNumber = accountNumber;
        this.financialObjectCode = financialObjectCode;
        this.subAccountNumber = subAccountNumber;
        this.financialSubObjectCode = financialSubObjectCode;
        this.projectCode = projectCode;
        this.organizationReferenceId = organizationReferenceId;
        this.amountOrPercent = amountOrPercent;
        this.useAmountOrPercent = useAmountOrPercent;
    }

    public IWantTransactionLineXml toIWantTransactionLineXml() {
        IWantTransactionLineXml line = new IWantTransactionLineXml();
        line.setAccountNumber(accountNumber);
        line.setAmountOrPercent(amountOrPercent);
        line.setChartOfAccountsCode(chartOfAccountsCode);
        line.setFinancialObjectCode(financialObjectCode);
        line.setFinancialSubObjectCode(financialSubObjectCode);
        line.setOrganizationReferenceId(organizationReferenceId);
        line.setProjectCode(projectCode);
        line.setSubAccountNumber(subAccountNumber);
        line.setUseAmountOrPercent(useAmountOrPercent);
        return line;
    }

}
