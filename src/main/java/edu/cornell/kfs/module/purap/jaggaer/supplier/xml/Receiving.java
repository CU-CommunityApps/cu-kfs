
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "numberWheel", "receiptQtyDifference", "receiptCostDifference",
        "notifyUserAtTimeOfReceiptCreation", "receiptQtyDifferenceAbove", "receiptQtyDifferenceBelow",
        "receiptCostDifferenceAbove", "receiptCostDifferenceBelow" })
@XmlRootElement(name = "Receiving")
public class Receiving {

    @XmlElement(name = "NumberWheel")
    protected NumberWheel numberWheel;
    @XmlElement(name = "ReceiptQtyDifference")
    protected ReceiptQtyDifference receiptQtyDifference;
    @XmlElement(name = "ReceiptCostDifference")
    protected ReceiptCostDifference receiptCostDifference;
    @XmlElement(name = "NotifyUserAtTimeOfReceiptCreation")
    protected NotifyUserAtTimeOfReceiptCreation notifyUserAtTimeOfReceiptCreation;
    @XmlElement(name = "ReceiptQtyDifferenceAbove")
    protected ReceiptQtyDifferenceAbove receiptQtyDifferenceAbove;
    @XmlElement(name = "ReceiptQtyDifferenceBelow")
    protected ReceiptQtyDifferenceBelow receiptQtyDifferenceBelow;
    @XmlElement(name = "ReceiptCostDifferenceAbove")
    protected ReceiptCostDifferenceAbove receiptCostDifferenceAbove;
    @XmlElement(name = "ReceiptCostDifferenceBelow")
    protected ReceiptCostDifferenceBelow receiptCostDifferenceBelow;

    public NumberWheel getNumberWheel() {
        return numberWheel;
    }

    public void setNumberWheel(NumberWheel value) {
        this.numberWheel = value;
    }

    public ReceiptQtyDifference getReceiptQtyDifference() {
        return receiptQtyDifference;
    }

    public void setReceiptQtyDifference(ReceiptQtyDifference value) {
        this.receiptQtyDifference = value;
    }

    public ReceiptCostDifference getReceiptCostDifference() {
        return receiptCostDifference;
    }

    public void setReceiptCostDifference(ReceiptCostDifference value) {
        this.receiptCostDifference = value;
    }

    public NotifyUserAtTimeOfReceiptCreation getNotifyUserAtTimeOfReceiptCreation() {
        return notifyUserAtTimeOfReceiptCreation;
    }

    public void setNotifyUserAtTimeOfReceiptCreation(NotifyUserAtTimeOfReceiptCreation value) {
        this.notifyUserAtTimeOfReceiptCreation = value;
    }

    public ReceiptQtyDifferenceAbove getReceiptQtyDifferenceAbove() {
        return receiptQtyDifferenceAbove;
    }

    public void setReceiptQtyDifferenceAbove(ReceiptQtyDifferenceAbove value) {
        this.receiptQtyDifferenceAbove = value;
    }

    public ReceiptQtyDifferenceBelow getReceiptQtyDifferenceBelow() {
        return receiptQtyDifferenceBelow;
    }

    public void setReceiptQtyDifferenceBelow(ReceiptQtyDifferenceBelow value) {
        this.receiptQtyDifferenceBelow = value;
    }

    public ReceiptCostDifferenceAbove getReceiptCostDifferenceAbove() {
        return receiptCostDifferenceAbove;
    }

    public void setReceiptCostDifferenceAbove(ReceiptCostDifferenceAbove value) {
        this.receiptCostDifferenceAbove = value;
    }

    public ReceiptCostDifferenceBelow getReceiptCostDifferenceBelow() {
        return receiptCostDifferenceBelow;
    }

    public void setReceiptCostDifferenceBelow(ReceiptCostDifferenceBelow value) {
        this.receiptCostDifferenceBelow = value;
    }

}
