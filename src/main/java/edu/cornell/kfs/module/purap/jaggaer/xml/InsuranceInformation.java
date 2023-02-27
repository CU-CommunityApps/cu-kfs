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
    protected JaggaerBasicValue policyNumber;
    @XmlElement(name = "InsuranceLimit")
    protected JaggaerBasicValue insuranceLimit;
    @XmlElement(name = "ExpirationDate")
    protected JaggaerBasicValue expirationDate;
    @XmlElement(name = "InsuranceProvider")
    protected JaggaerBasicValue insuranceProvider;
    @XmlElement(name = "Agent")
    protected JaggaerBasicValue agent;
    @XmlElement(name = "InsuranceProviderPhone")
    protected InsuranceProviderPhone insuranceProviderPhone;
    @XmlElement(name = "InsuranceCertificate")
    protected InsuranceCertificate insuranceCertificate;
    @XmlElement(name = "OtherTypeName")
    protected JaggaerBasicValue otherTypeName;

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

    public JaggaerBasicValue getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(JaggaerBasicValue value) {
        this.policyNumber = value;
    }

    public JaggaerBasicValue getInsuranceLimit() {
        return insuranceLimit;
    }

    public void setInsuranceLimit(JaggaerBasicValue value) {
        this.insuranceLimit = value;
    }

    public JaggaerBasicValue getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(JaggaerBasicValue value) {
        this.expirationDate = value;
    }

    public JaggaerBasicValue getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(JaggaerBasicValue value) {
        this.insuranceProvider = value;
    }

    public JaggaerBasicValue getAgent() {
        return agent;
    }

    public void setAgent(JaggaerBasicValue value) {
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

    public JaggaerBasicValue getOtherTypeName() {
        return otherTypeName;
    }

    public void setOtherTypeName(JaggaerBasicValue value) {
        this.otherTypeName = value;
    }

}
