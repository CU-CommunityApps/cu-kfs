package edu.cornell.kfs.module.purap.iwant.xml;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.module.purap.businessobject.BatchIWantAccount;
import edu.cornell.kfs.sys.xmladapters.KualiDecimalNullPossibleXmlAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "chartOfAccountsCode", "accountNumber", "financialObjectCode", "subAccountNumber",
        "financialSubObjectCode", "projectCode", "organizationReferenceId", "amountOrPercent", "useAmountOrPercent" })
@XmlRootElement(name = "account", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
public class IWantTransactionLineXml {

    @XmlElement(name = "chartOfAccountsCode", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String chartOfAccountsCode;

    @XmlElement(name = "accountNumber", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String accountNumber;

    @XmlElement(name = "financialObjectCode", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String financialObjectCode;

    @XmlElement(name = "subAccountNumber", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String subAccountNumber;

    @XmlElement(name = "financialSubObjectCode", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String financialSubObjectCode;

    @XmlElement(name = "projectCode", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String projectCode;

    @XmlElement(name = "organizationReferenceId", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    private String organizationReferenceId;

    @XmlElement(name = "amountOrPercent", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    @XmlJavaTypeAdapter(KualiDecimalNullPossibleXmlAdapter.class)
    private KualiDecimal amountOrPercent;

    @XmlElement(name = "useAmountOrPercent", namespace = IWantXmlConstants.IWANT_DOCUMENT_NAMESPACE)
    @XmlSchemaType(name = "string")
    private IWantXmlConstants.IWantAmountOrPercentTypeXml useAmountOrPercent;

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

    public KualiDecimal getAmountOrPercent() {
        return amountOrPercent;
    }

    public void setAmountOrPercent(KualiDecimal amountOrPercent) {
        this.amountOrPercent = amountOrPercent;
    }

    public IWantXmlConstants.IWantAmountOrPercentTypeXml getUseAmountOrPercent() {
        return useAmountOrPercent;
    }

    public void setUseAmountOrPercent(IWantXmlConstants.IWantAmountOrPercentTypeXml useAmountOrPercent) {
        this.useAmountOrPercent = useAmountOrPercent;
    }
    
    public BatchIWantAccount toBatchIWantAccount() {
        BatchIWantAccount account = new BatchIWantAccount();
        account.setChartOfAccountsCode(chartOfAccountsCode);
        account.setAccountNumber(accountNumber);
        account.setFinancialObjectCode(financialObjectCode);
        account.setSubAccountNumber(subAccountNumber);
        account.setFinancialSubObjectCode(financialSubObjectCode);
        account.setProjectCode(projectCode);
        account.setOrganizationReferenceId(organizationReferenceId);
        account.setAmountOrPercent(String.valueOf(amountOrPercent));
        account.setUseAmountOrPercent(useAmountOrPercent.value());
        return account;
    }

    @Override
    public String toString() {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        return builder.build();
    }

}
