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
@XmlType(name = "", propOrder = { "amount", "isoCurrencyCode" })
@XmlRootElement(name = "SupplierCapital")
public class SupplierCapital {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "Amount", required = true)
    protected Amount amount;
    @XmlElement(name = "IsoCurrencyCode", required = true)
    protected IsoCurrencyCode isoCurrencyCode;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public IsoCurrencyCode getIsoCurrencyCode() {
        return isoCurrencyCode;
    }

    public void setIsoCurrencyCode(IsoCurrencyCode isoCurrencyCode) {
        this.isoCurrencyCode = isoCurrencyCode;
    }

}
