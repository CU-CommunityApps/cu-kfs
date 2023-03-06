
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "active",
    "discount",
    "days",
    "net",
    "customPaymentTerm",
    "fob",
    "standardPaymentTermsCode",
    "termsType",
    "daysAfter"
})
@XmlRootElement(name = "PaymentTerms")
public class PaymentTerms {

    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "Discount")
    protected Discount discount;
    @XmlElement(name = "Days")
    protected Days days;
    @XmlElement(name = "Net")
    protected Net net;
    @XmlElement(name = "CustomPaymentTerm")
    protected CustomPaymentTerm customPaymentTerm;
    @XmlElement(name = "FOB")
    protected FOB fob;
    @XmlElement(name = "StandardPaymentTermsCode")
    protected StandardPaymentTermsCode standardPaymentTermsCode;
    @XmlElement(name = "TermsType")
    protected TermsType termsType;
    @XmlElement(name = "DaysAfter")
    protected DaysAfter daysAfter;

    /**
     * Gets the value of the isChanged property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsChanged() {
        return isChanged;
    }

    /**
     * Sets the value of the isChanged property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    /**
     * Gets the value of the active property.
     * 
     * @return
     *     possible object is
     *     {@link Active }
     *     
     */
    public Active getActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Active }
     *     
     */
    public void setActive(Active value) {
        this.active = value;
    }

    /**
     * Gets the value of the discount property.
     * 
     * @return
     *     possible object is
     *     {@link Discount }
     *     
     */
    public Discount getDiscount() {
        return discount;
    }

    /**
     * Sets the value of the discount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Discount }
     *     
     */
    public void setDiscount(Discount value) {
        this.discount = value;
    }

    /**
     * Gets the value of the days property.
     * 
     * @return
     *     possible object is
     *     {@link Days }
     *     
     */
    public Days getDays() {
        return days;
    }

    /**
     * Sets the value of the days property.
     * 
     * @param value
     *     allowed object is
     *     {@link Days }
     *     
     */
    public void setDays(Days value) {
        this.days = value;
    }

    /**
     * Gets the value of the net property.
     * 
     * @return
     *     possible object is
     *     {@link Net }
     *     
     */
    public Net getNet() {
        return net;
    }

    /**
     * Sets the value of the net property.
     * 
     * @param value
     *     allowed object is
     *     {@link Net }
     *     
     */
    public void setNet(Net value) {
        this.net = value;
    }

    /**
     * Gets the value of the customPaymentTerm property.
     * 
     * @return
     *     possible object is
     *     {@link CustomPaymentTerm }
     *     
     */
    public CustomPaymentTerm getCustomPaymentTerm() {
        return customPaymentTerm;
    }

    /**
     * Sets the value of the customPaymentTerm property.
     * 
     * @param value
     *     allowed object is
     *     {@link CustomPaymentTerm }
     *     
     */
    public void setCustomPaymentTerm(CustomPaymentTerm value) {
        this.customPaymentTerm = value;
    }

    /**
     * Gets the value of the fob property.
     * 
     * @return
     *     possible object is
     *     {@link FOB }
     *     
     */
    public FOB getFOB() {
        return fob;
    }

    /**
     * Sets the value of the fob property.
     * 
     * @param value
     *     allowed object is
     *     {@link FOB }
     *     
     */
    public void setFOB(FOB value) {
        this.fob = value;
    }

    /**
     * Gets the value of the standardPaymentTermsCode property.
     * 
     * @return
     *     possible object is
     *     {@link StandardPaymentTermsCode }
     *     
     */
    public StandardPaymentTermsCode getStandardPaymentTermsCode() {
        return standardPaymentTermsCode;
    }

    /**
     * Sets the value of the standardPaymentTermsCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link StandardPaymentTermsCode }
     *     
     */
    public void setStandardPaymentTermsCode(StandardPaymentTermsCode value) {
        this.standardPaymentTermsCode = value;
    }

    /**
     * Gets the value of the termsType property.
     * 
     * @return
     *     possible object is
     *     {@link TermsType }
     *     
     */
    public TermsType getTermsType() {
        return termsType;
    }

    /**
     * Sets the value of the termsType property.
     * 
     * @param value
     *     allowed object is
     *     {@link TermsType }
     *     
     */
    public void setTermsType(TermsType value) {
        this.termsType = value;
    }

    /**
     * Gets the value of the daysAfter property.
     * 
     * @return
     *     possible object is
     *     {@link DaysAfter }
     *     
     */
    public DaysAfter getDaysAfter() {
        return daysAfter;
    }

    /**
     * Sets the value of the daysAfter property.
     * 
     * @param value
     *     allowed object is
     *     {@link DaysAfter }
     *     
     */
    public void setDaysAfter(DaysAfter value) {
        this.daysAfter = value;
    }

}
