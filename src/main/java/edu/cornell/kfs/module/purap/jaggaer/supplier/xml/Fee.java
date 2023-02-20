
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "feeType", "percentage", "amount", "feeScope" })
@XmlRootElement(name = "Fee")
public class Fee {

    @XmlElement(name = "FeeType")
    protected FeeType feeType;
    @XmlElement(name = "Percentage")
    protected Percentage percentage;
    @XmlElement(name = "Amount")
    protected Amount amount;
    @XmlElement(name = "FeeScope")
    protected FeeScope feeScope;

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
