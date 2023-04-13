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
@XmlType(name = "", propOrder = { "feeType", "percentage", "amount", "feeScope" })
@XmlRootElement(name = "Fee")
public class Fee {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "FeeType")
    private JaggaerBasicValue feeType;
    @XmlElement(name = "Percentage")
    private JaggaerBasicValue percentage;
    @XmlElement(name = "Amount")
    private Amount amount;
    @XmlElement(name = "FeeScope")
    private JaggaerBasicValue feeScope;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public JaggaerBasicValue getFeeType() {
        return feeType;
    }

    public void setFeeType(JaggaerBasicValue feeType) {
        this.feeType = feeType;
    }

    public JaggaerBasicValue getPercentage() {
        return percentage;
    }

    public void setPercentage(JaggaerBasicValue percentage) {
        this.percentage = percentage;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public JaggaerBasicValue getFeeScope() {
        return feeScope;
    }

    public void setFeeScope(JaggaerBasicValue feeScope) {
        this.feeScope = feeScope;
    }

}
