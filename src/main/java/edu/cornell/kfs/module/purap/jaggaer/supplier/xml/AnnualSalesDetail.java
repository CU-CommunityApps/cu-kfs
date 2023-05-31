package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "annualSalesYear", "annualSalesAmount", "isoCurrencyCode" })
@XmlRootElement(name = "AnnualSales")
public class AnnualSalesDetail {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "AnnualSalesYear")
    private JaggaerBasicValue annualSalesYear;
    @XmlElement(name = "AnnualSalesAmount")
    private Amount annualSalesAmount;
    @XmlElement(name = "IsoCurrencyCode")
    private IsoCurrencyCode isoCurrencyCode;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public JaggaerBasicValue getAnnualSalesYear() {
        return annualSalesYear;
    }

    public void setAnnualSalesYear(JaggaerBasicValue annualSalesYear) {
        this.annualSalesYear = annualSalesYear;
    }

    public Amount getAnnualSalesAmount() {
        return annualSalesAmount;
    }

    public void setAnnualSalesAmount(Amount annualSalesAmount) {
        this.annualSalesAmount = annualSalesAmount;
    }

    public IsoCurrencyCode getIsoCurrencyCode() {
        return isoCurrencyCode;
    }

    public void setIsoCurrencyCode(IsoCurrencyCode isoCurrencyCode) {
        this.isoCurrencyCode = isoCurrencyCode;
    }

}
