package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "taxableByDefault", "tax1Active", "tax1", "tax2Active", "tax2", "taxShipping",
        "taxHandling" })
@XmlRootElement(name = "TaxInfo")
public class TaxInfo {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "TaxableByDefault")
    protected JaggaerBasicValue taxableByDefault;
    @XmlElement(name = "Tax1Active")
    protected JaggaerBasicValue tax1Active;
    @XmlElement(name = "Tax1")
    protected JaggaerBasicValue tax1;
    @XmlElement(name = "Tax2Active")
    protected JaggaerBasicValue tax2Active;
    @XmlElement(name = "Tax2")
    protected JaggaerBasicValue tax2;
    @XmlElement(name = "TaxShipping")
    protected JaggaerBasicValue taxShipping;
    @XmlElement(name = "TaxHandling")
    protected JaggaerBasicValue taxHandling;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public JaggaerBasicValue getTaxableByDefault() {
        return taxableByDefault;
    }

    public void setTaxableByDefault(JaggaerBasicValue value) {
        this.taxableByDefault = value;
    }

    public JaggaerBasicValue getTax1Active() {
        return tax1Active;
    }

    public void setTax1Active(JaggaerBasicValue value) {
        this.tax1Active = value;
    }

    public JaggaerBasicValue getTax1() {
        return tax1;
    }

    public void setTax1(JaggaerBasicValue value) {
        this.tax1 = value;
    }

    public JaggaerBasicValue getTax2Active() {
        return tax2Active;
    }

    public void setTax2Active(JaggaerBasicValue value) {
        this.tax2Active = value;
    }

    public JaggaerBasicValue getTax2() {
        return tax2;
    }

    public void setTax2(JaggaerBasicValue value) {
        this.tax2 = value;
    }

    public JaggaerBasicValue getTaxShipping() {
        return taxShipping;
    }

    public void setTaxShipping(JaggaerBasicValue value) {
        this.taxShipping = value;
    }

    public JaggaerBasicValue getTaxHandling() {
        return taxHandling;
    }

    public void setTaxHandling(JaggaerBasicValue value) {
        this.taxHandling = value;
    }

}
