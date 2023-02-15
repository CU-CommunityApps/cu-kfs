package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "policyNumber", "insuranceLimit", "expirationDate", "insuranceProvider", "agent",
        "insuranceProviderPhone", "insuranceCertificate", "otherTypeName" })
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

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public PolicyNumber getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(PolicyNumber value) {
        this.policyNumber = value;
    }

    public InsuranceLimit getInsuranceLimit() {
        return insuranceLimit;
    }

    public void setInsuranceLimit(InsuranceLimit value) {
        this.insuranceLimit = value;
    }

    public ExpirationDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(ExpirationDate value) {
        this.expirationDate = value;
    }

    public InsuranceProvider getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(InsuranceProvider value) {
        this.insuranceProvider = value;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent value) {
        this.agent = value;
    }

    public InsuranceProviderPhone getInsuranceProviderPhone() {
        return insuranceProviderPhone;
    }

    public void setInsuranceProviderPhone(InsuranceProviderPhone value) {
        this.insuranceProviderPhone = value;
    }

    public InsuranceCertificate getInsuranceCertificate() {
        return insuranceCertificate;
    }

    public void setInsuranceCertificate(InsuranceCertificate value) {
        this.insuranceCertificate = value;
    }

    public OtherTypeName getOtherTypeName() {
        return otherTypeName;
    }

    public void setOtherTypeName(OtherTypeName value) {
        this.otherTypeName = value;
    }

}
