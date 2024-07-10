package edu.cornell.kfs.fp.batch.xml;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import edu.cornell.kfs.sys.xmladapters.KualiDecimalXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.KualiIntegerXmlAdapter;
import edu.cornell.kfs.sys.xmladapters.TrimmedStringXmlAdapter;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Accounting", namespace = StringUtils.EMPTY)
public class AccountingXmlDocumentAccountingLine {

    @XmlElement(name = "coa_cd", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(TrimmedStringXmlAdapter.class)
    protected String chartCode;

    @XmlElement(name = "account_nbr", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(TrimmedStringXmlAdapter.class)
    protected String accountNumber;

    @XmlElement(name = "sub_account_nbr", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(TrimmedStringXmlAdapter.class)
    protected String subAccountNumber;

    @XmlElement(name = "object_cd", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(TrimmedStringXmlAdapter.class)
    protected String objectCode;

    @XmlElement(name = "sub_object_cd", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(TrimmedStringXmlAdapter.class)
    protected String subObjectCode;

    @XmlElement(name = "project", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(TrimmedStringXmlAdapter.class)
    protected String projectCode;

    @XmlElement(name = "org_ref_id", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(TrimmedStringXmlAdapter.class)
    protected String orgRefId;

    @XmlElement(name = "line_description", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(TrimmedStringXmlAdapter.class)
    protected String lineDescription;

    @XmlElement(name = "amount", namespace = StringUtils.EMPTY, required = true)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal amount;

    @XmlElement(name = "base_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiIntegerXmlAdapter.class)
    protected KualiInteger baseAmount;

    @XmlElement(name = "month_01_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month01Amount;

    @XmlElement(name = "month_02_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month02Amount;

    @XmlElement(name = "month_03_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month03Amount;

    @XmlElement(name = "month_04_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month04Amount;

    @XmlElement(name = "month_05_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month05Amount;

    @XmlElement(name = "month_06_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month06Amount;

    @XmlElement(name = "month_07_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month07Amount;

    @XmlElement(name = "month_08_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month08Amount;

    @XmlElement(name = "month_09_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month09Amount;

    @XmlElement(name = "month_10_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month10Amount;

    @XmlElement(name = "month_11_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month11Amount;

    @XmlElement(name = "month_12_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal month12Amount;

    @XmlElement(name = "debit_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal debitAmount;

    @XmlElement(name = "credit_amount", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(KualiDecimalXmlAdapter.class)
    protected KualiDecimal creditAmount;

    @XmlElement(name = "reference_number", namespace = StringUtils.EMPTY, required = false)
    @XmlJavaTypeAdapter(TrimmedStringXmlAdapter.class)
    protected String referenceNumber;

    public String getChartCode() {
        return chartCode;
    }

    public void setChartCode(String chartCode) {
        this.chartCode = chartCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }

    public String getSubObjectCode() {
        return subObjectCode;
    }

    public void setSubObjectCode(String subObjectCode) {
        this.subObjectCode = subObjectCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getOrgRefId() {
        return orgRefId;
    }

    public void setOrgRefId(String orgRefId) {
        this.orgRefId = orgRefId;
    }

    public String getLineDescription() {
        return lineDescription;
    }

    public void setLineDescription(String lineDescription) {
        this.lineDescription = lineDescription;
    }

    public KualiDecimal getAmount() {
        return amount;
    }

    public void setAmount(KualiDecimal amount) {
        this.amount = amount;
    }

    public KualiInteger getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(KualiInteger baseAmount) {
        this.baseAmount = baseAmount;
    }

    public KualiDecimal getMonth01Amount() {
        return month01Amount;
    }

    public void setMonth01Amount(KualiDecimal month01Amount) {
        this.month01Amount = month01Amount;
    }

    public KualiDecimal getMonth02Amount() {
        return month02Amount;
    }

    public void setMonth02Amount(KualiDecimal month02Amount) {
        this.month02Amount = month02Amount;
    }

    public KualiDecimal getMonth03Amount() {
        return month03Amount;
    }

    public void setMonth03Amount(KualiDecimal month03Amount) {
        this.month03Amount = month03Amount;
    }

    public KualiDecimal getMonth04Amount() {
        return month04Amount;
    }

    public void setMonth04Amount(KualiDecimal month04Amount) {
        this.month04Amount = month04Amount;
    }

    public KualiDecimal getMonth05Amount() {
        return month05Amount;
    }

    public void setMonth05Amount(KualiDecimal month05Amount) {
        this.month05Amount = month05Amount;
    }

    public KualiDecimal getMonth06Amount() {
        return month06Amount;
    }

    public void setMonth06Amount(KualiDecimal month06Amount) {
        this.month06Amount = month06Amount;
    }

    public KualiDecimal getMonth07Amount() {
        return month07Amount;
    }

    public void setMonth07Amount(KualiDecimal month07Amount) {
        this.month07Amount = month07Amount;
    }

    public KualiDecimal getMonth08Amount() {
        return month08Amount;
    }

    public void setMonth08Amount(KualiDecimal month08Amount) {
        this.month08Amount = month08Amount;
    }

    public KualiDecimal getMonth09Amount() {
        return month09Amount;
    }

    public void setMonth09Amount(KualiDecimal month09Amount) {
        this.month09Amount = month09Amount;
    }

    public KualiDecimal getMonth10Amount() {
        return month10Amount;
    }

    public void setMonth10Amount(KualiDecimal month10Amount) {
        this.month10Amount = month10Amount;
    }

    public KualiDecimal getMonth11Amount() {
        return month11Amount;
    }

    public void setMonth11Amount(KualiDecimal month11Amount) {
        this.month11Amount = month11Amount;
    }

    public KualiDecimal getMonth12Amount() {
        return month12Amount;
    }

    public void setMonth12Amount(KualiDecimal month12Amount) {
        this.month12Amount = month12Amount;
    }

    public KualiDecimal getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(KualiDecimal debitAmount) {
        this.debitAmount = debitAmount;
    }

    public KualiDecimal getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(KualiDecimal creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(obj, this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
