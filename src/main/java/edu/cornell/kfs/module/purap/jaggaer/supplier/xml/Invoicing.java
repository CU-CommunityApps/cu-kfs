
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "terms", "numberWheel", "invoiceQtyDifference", "invoiceCostDifference",
        "notifyUserAtTimeOfInvoiceCreation", "invoiceQtyDifferenceAbove", "invoiceQtyDifferenceBelow",
        "invoiceExtendedPriceDifferenceAbove", "invoiceExtendedPriceDifferenceBelow", "invoiceUnitPriceDifferenceAbove",
        "invoiceUnitPriceDifferenceBelow" })
@XmlRootElement(name = "Invoicing")
public class Invoicing {

    @XmlElement(name = "Terms")
    protected Terms terms;
    @XmlElement(name = "NumberWheel")
    protected NumberWheel numberWheel;
    @XmlElement(name = "InvoiceQtyDifference")
    protected InvoiceQtyDifference invoiceQtyDifference;
    @XmlElement(name = "InvoiceCostDifference")
    protected InvoiceCostDifference invoiceCostDifference;
    @XmlElement(name = "NotifyUserAtTimeOfInvoiceCreation")
    protected NotifyUserAtTimeOfInvoiceCreation notifyUserAtTimeOfInvoiceCreation;
    @XmlElement(name = "InvoiceQtyDifferenceAbove")
    protected InvoiceQtyDifferenceAbove invoiceQtyDifferenceAbove;
    @XmlElement(name = "InvoiceQtyDifferenceBelow")
    protected InvoiceQtyDifferenceBelow invoiceQtyDifferenceBelow;
    @XmlElement(name = "InvoiceExtendedPriceDifferenceAbove")
    protected InvoiceExtendedPriceDifferenceAbove invoiceExtendedPriceDifferenceAbove;
    @XmlElement(name = "InvoiceExtendedPriceDifferenceBelow")
    protected InvoiceExtendedPriceDifferenceBelow invoiceExtendedPriceDifferenceBelow;
    @XmlElement(name = "InvoiceUnitPriceDifferenceAbove")
    protected InvoiceUnitPriceDifferenceAbove invoiceUnitPriceDifferenceAbove;
    @XmlElement(name = "InvoiceUnitPriceDifferenceBelow")
    protected InvoiceUnitPriceDifferenceBelow invoiceUnitPriceDifferenceBelow;

    public Terms getTerms() {
        return terms;
    }

    public void setTerms(Terms value) {
        this.terms = value;
    }

    public NumberWheel getNumberWheel() {
        return numberWheel;
    }

    public void setNumberWheel(NumberWheel value) {
        this.numberWheel = value;
    }

    public InvoiceQtyDifference getInvoiceQtyDifference() {
        return invoiceQtyDifference;
    }

    public void setInvoiceQtyDifference(InvoiceQtyDifference value) {
        this.invoiceQtyDifference = value;
    }

    public InvoiceCostDifference getInvoiceCostDifference() {
        return invoiceCostDifference;
    }

    public void setInvoiceCostDifference(InvoiceCostDifference value) {
        this.invoiceCostDifference = value;
    }

    public NotifyUserAtTimeOfInvoiceCreation getNotifyUserAtTimeOfInvoiceCreation() {
        return notifyUserAtTimeOfInvoiceCreation;
    }

    public void setNotifyUserAtTimeOfInvoiceCreation(NotifyUserAtTimeOfInvoiceCreation value) {
        this.notifyUserAtTimeOfInvoiceCreation = value;
    }

    public InvoiceQtyDifferenceAbove getInvoiceQtyDifferenceAbove() {
        return invoiceQtyDifferenceAbove;
    }

    public void setInvoiceQtyDifferenceAbove(InvoiceQtyDifferenceAbove value) {
        this.invoiceQtyDifferenceAbove = value;
    }

    public InvoiceQtyDifferenceBelow getInvoiceQtyDifferenceBelow() {
        return invoiceQtyDifferenceBelow;
    }

    public void setInvoiceQtyDifferenceBelow(InvoiceQtyDifferenceBelow value) {
        this.invoiceQtyDifferenceBelow = value;
    }

    public InvoiceExtendedPriceDifferenceAbove getInvoiceExtendedPriceDifferenceAbove() {
        return invoiceExtendedPriceDifferenceAbove;
    }

    public void setInvoiceExtendedPriceDifferenceAbove(InvoiceExtendedPriceDifferenceAbove value) {
        this.invoiceExtendedPriceDifferenceAbove = value;
    }

    public InvoiceExtendedPriceDifferenceBelow getInvoiceExtendedPriceDifferenceBelow() {
        return invoiceExtendedPriceDifferenceBelow;
    }

    public void setInvoiceExtendedPriceDifferenceBelow(InvoiceExtendedPriceDifferenceBelow value) {
        this.invoiceExtendedPriceDifferenceBelow = value;
    }

    public InvoiceUnitPriceDifferenceAbove getInvoiceUnitPriceDifferenceAbove() {
        return invoiceUnitPriceDifferenceAbove;
    }

    public void setInvoiceUnitPriceDifferenceAbove(InvoiceUnitPriceDifferenceAbove value) {
        this.invoiceUnitPriceDifferenceAbove = value;
    }

    public InvoiceUnitPriceDifferenceBelow getInvoiceUnitPriceDifferenceBelow() {
        return invoiceUnitPriceDifferenceBelow;
    }

    public void setInvoiceUnitPriceDifferenceBelow(InvoiceUnitPriceDifferenceBelow value) {
        this.invoiceUnitPriceDifferenceBelow = value;
    }

}
