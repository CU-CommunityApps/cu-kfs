package edu.cornell.kfs.module.purap.businessobject.xml;

import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "chartOfAccountsCode", "accountNumber", "financialObjectCode", "subAccountNumber",
        "financialSubObjectCode", "projectCode", "organizationReferenceId", "amountOrPercent", "useAmountOrPercent" })
@XmlRootElement(name = "account")
public class Account {

    private String chartOfAccountsCode;
    private String accountNumber;
    private String financialObjectCode;
    private String subAccountNumber;
    private String financialSubObjectCode;
    private String projectCode;
    private String organizationReferenceId;
    private BigDecimal amountOrPercent;
    @XmlSchemaType(name = "string")
    private String useAmountOrPercent;

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    public void setFinancialObjectCode(String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public String getFinancialSubObjectCode() {
        return financialSubObjectCode;
    }

    public void setFinancialSubObjectCode(String financialSubObjectCode) {
        this.financialSubObjectCode = financialSubObjectCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getOrganizationReferenceId() {
        return organizationReferenceId;
    }

    public void setOrganizationReferenceId(String organizationReferenceId) {
        this.organizationReferenceId = organizationReferenceId;
    }

    public BigDecimal getAmountOrPercent() {
        return amountOrPercent;
    }

    public void setAmountOrPercent(BigDecimal amountOrPercent) {
        this.amountOrPercent = amountOrPercent;
    }

    public String getUseAmountOrPercent() {
        return useAmountOrPercent;
    }

    public void setUseAmountOrPercent(String useAmountOrPercent) {
        this.useAmountOrPercent = useAmountOrPercent;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }
}
