package edu.cornell.kfs.module.purap.businessobject.xml;

import java.math.BigDecimal;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "chartOfAccountsCode", "accountNumber", "financialObjectCode", "subAccountNumber",
        "financialSubObjectCode", "projectCode", "organizationReferenceId", "amountOrPercent", "useAmountOrPercent" })
@XmlRootElement(name = "account", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
public class Account {

    @XmlElement(name = "chartOfAccountsCode", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String chartOfAccountsCode;

    @XmlElement(name = "accountNumber", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String accountNumber;

    @XmlElement(name = "financialObjectCode", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String financialObjectCode;

    @XmlElement(name = "subAccountNumber", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String subAccountNumber;

    @XmlElement(name = "financialSubObjectCode", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String financialSubObjectCode;

    @XmlElement(name = "projectCode", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String projectCode;

    @XmlElement(name = "organizationReferenceId", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private String organizationReferenceId;

    @XmlElement(name = "amountOrPercent", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    private BigDecimal amountOrPercent;

    @XmlElement(name = "useAmountOrPercent", namespace = "http://www.kuali.org/kfs/purap/iWantDocument")
    @XmlSchemaType(name = "string")
    private AmountOrPercentType useAmountOrPercent;

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

    public AmountOrPercentType getUseAmountOrPercent() {
        return useAmountOrPercent;
    }

    public void setUseAmountOrPercent(AmountOrPercentType useAmountOrPercent) {
        this.useAmountOrPercent = useAmountOrPercent;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }

}
