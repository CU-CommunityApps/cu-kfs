
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
@XmlType(name = "", propOrder = {
    "taxableByDefault",
    "tax1Active",
    "tax1",
    "tax2Active",
    "tax2",
    "taxShipping",
    "taxHandling"
})
@XmlRootElement(name = "TaxInfo")
public class TaxInfo {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "TaxableByDefault")
    protected TaxableByDefault taxableByDefault;
    @XmlElement(name = "Tax1Active")
    protected Tax1Active tax1Active;
    @XmlElement(name = "Tax1")
    protected Tax1 tax1;
    @XmlElement(name = "Tax2Active")
    protected Tax2Active tax2Active;
    @XmlElement(name = "Tax2")
    protected Tax2 tax2;
    @XmlElement(name = "TaxShipping")
    protected TaxShipping taxShipping;
    @XmlElement(name = "TaxHandling")
    protected TaxHandling taxHandling;

    
    public String getIsChanged() {
        return isChanged;
    }

    
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    
    public TaxableByDefault getTaxableByDefault() {
        return taxableByDefault;
    }

    
    public void setTaxableByDefault(TaxableByDefault value) {
        this.taxableByDefault = value;
    }

    
    public Tax1Active getTax1Active() {
        return tax1Active;
    }

    
    public void setTax1Active(Tax1Active value) {
        this.tax1Active = value;
    }

    
    public Tax1 getTax1() {
        return tax1;
    }

    
    public void setTax1(Tax1 value) {
        this.tax1 = value;
    }

    
    public Tax2Active getTax2Active() {
        return tax2Active;
    }

    
    public void setTax2Active(Tax2Active value) {
        this.tax2Active = value;
    }

    
    public Tax2 getTax2() {
        return tax2;
    }

    
    public void setTax2(Tax2 value) {
        this.tax2 = value;
    }

    
    public TaxShipping getTaxShipping() {
        return taxShipping;
    }

    
    public void setTaxShipping(TaxShipping value) {
        this.taxShipping = value;
    }

    
    public TaxHandling getTaxHandling() {
        return taxHandling;
    }

    
    public void setTaxHandling(TaxHandling value) {
        this.taxHandling = value;
    }

}
