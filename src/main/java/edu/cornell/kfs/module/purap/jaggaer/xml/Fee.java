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
@XmlType(name = "", propOrder = { "feeType", "percentage", "amount", "feeScope" })
@XmlRootElement(name = "Fee")
public class Fee {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "FeeType")
    protected FeeType feeType;
    @XmlElement(name = "Percentage")
    protected Percentage percentage;
    @XmlElement(name = "Amount")
    protected Amount amount;
    @XmlElement(name = "FeeScope")
    protected FeeScope feeScope;

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public FeeType getFeeType() {
        return feeType;
    }

    public void setFeeType(FeeType value) {
        this.feeType = value;
    }

    public Percentage getPercentage() {
        return percentage;
    }

    public void setPercentage(Percentage value) {
        this.percentage = value;
    }

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount value) {
        this.amount = value;
    }

    public FeeScope getFeeScope() {
        return feeScope;
    }
    
    public void setFeeScope(FeeScope value) {
        this.feeScope = value;
    }

}
