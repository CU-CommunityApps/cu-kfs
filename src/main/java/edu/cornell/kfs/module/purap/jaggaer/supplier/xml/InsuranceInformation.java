
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "policyNumber",
    "insuranceLimit",
    "expirationDate",
    "insuranceProvider",
    "agent",
    "insuranceProviderPhone",
    "insuranceCertificate",
    "otherTypeName"
})
@XmlRootElement(name = "InsuranceInformation")
public class InsuranceInformation {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "PolicyNumber")
    protected PolicyNumber policyNumber;
    @XmlElement(name = "InsuranceLimit")
    protected InsuranceLimit insuranceLimit;
    @XmlElement(name = "ExpirationDate")
    protected ExpirationDate expirationDate;
    @XmlElement(name = "InsuranceProvider")
    protected InsuranceProvider insuranceProvider;
    @XmlElement(name = "Agent")
    protected Agent agent;
    @XmlElement(name = "InsuranceProviderPhone")
    protected InsuranceProviderPhone insuranceProviderPhone;
    @XmlElement(name = "InsuranceCertificate")
    protected InsuranceCertificate insuranceCertificate;
    @XmlElement(name = "OtherTypeName")
    protected OtherTypeName otherTypeName;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

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
     * Gets the value of the policyNumber property.
     * 
     * @return
     *     possible object is
     *     {@link PolicyNumber }
     *     
     */
    public PolicyNumber getPolicyNumber() {
        return policyNumber;
    }

    /**
     * Sets the value of the policyNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link PolicyNumber }
     *     
     */
    public void setPolicyNumber(PolicyNumber value) {
        this.policyNumber = value;
    }

    /**
     * Gets the value of the insuranceLimit property.
     * 
     * @return
     *     possible object is
     *     {@link InsuranceLimit }
     *     
     */
    public InsuranceLimit getInsuranceLimit() {
        return insuranceLimit;
    }

    /**
     * Sets the value of the insuranceLimit property.
     * 
     * @param value
     *     allowed object is
     *     {@link InsuranceLimit }
     *     
     */
    public void setInsuranceLimit(InsuranceLimit value) {
        this.insuranceLimit = value;
    }

    /**
     * Gets the value of the expirationDate property.
     * 
     * @return
     *     possible object is
     *     {@link ExpirationDate }
     *     
     */
    public ExpirationDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the value of the expirationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExpirationDate }
     *     
     */
    public void setExpirationDate(ExpirationDate value) {
        this.expirationDate = value;
    }

    /**
     * Gets the value of the insuranceProvider property.
     * 
     * @return
     *     possible object is
     *     {@link InsuranceProvider }
     *     
     */
    public InsuranceProvider getInsuranceProvider() {
        return insuranceProvider;
    }

    /**
     * Sets the value of the insuranceProvider property.
     * 
     * @param value
     *     allowed object is
     *     {@link InsuranceProvider }
     *     
     */
    public void setInsuranceProvider(InsuranceProvider value) {
        this.insuranceProvider = value;
    }

    /**
     * Gets the value of the agent property.
     * 
     * @return
     *     possible object is
     *     {@link Agent }
     *     
     */
    public Agent getAgent() {
        return agent;
    }

    /**
     * Sets the value of the agent property.
     * 
     * @param value
     *     allowed object is
     *     {@link Agent }
     *     
     */
    public void setAgent(Agent value) {
        this.agent = value;
    }

    /**
     * Gets the value of the insuranceProviderPhone property.
     * 
     * @return
     *     possible object is
     *     {@link InsuranceProviderPhone }
     *     
     */
    public InsuranceProviderPhone getInsuranceProviderPhone() {
        return insuranceProviderPhone;
    }

    /**
     * Sets the value of the insuranceProviderPhone property.
     * 
     * @param value
     *     allowed object is
     *     {@link InsuranceProviderPhone }
     *     
     */
    public void setInsuranceProviderPhone(InsuranceProviderPhone value) {
        this.insuranceProviderPhone = value;
    }

    /**
     * Gets the value of the insuranceCertificate property.
     * 
     * @return
     *     possible object is
     *     {@link InsuranceCertificate }
     *     
     */
    public InsuranceCertificate getInsuranceCertificate() {
        return insuranceCertificate;
    }

    /**
     * Sets the value of the insuranceCertificate property.
     * 
     * @param value
     *     allowed object is
     *     {@link InsuranceCertificate }
     *     
     */
    public void setInsuranceCertificate(InsuranceCertificate value) {
        this.insuranceCertificate = value;
    }

    /**
     * Gets the value of the otherTypeName property.
     * 
     * @return
     *     possible object is
     *     {@link OtherTypeName }
     *     
     */
    public OtherTypeName getOtherTypeName() {
        return otherTypeName;
    }

    /**
     * Sets the value of the otherTypeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link OtherTypeName }
     *     
     */
    public void setOtherTypeName(OtherTypeName value) {
        this.otherTypeName = value;
    }

}
