package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

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
    private String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "PolicyNumber")
    private JaggaerBasicValue policyNumber;
    @XmlElement(name = "InsuranceLimit")
    private JaggaerBasicValue insuranceLimit;
    @XmlElement(name = "ExpirationDate")
    private JaggaerBasicValue expirationDate;
    @XmlElement(name = "InsuranceProvider")
    private JaggaerBasicValue insuranceProvider;
    @XmlElement(name = "Agent")
    private JaggaerBasicValue agent;
    @XmlElement(name = "InsuranceProviderPhone")
    private InsuranceProviderPhone insuranceProviderPhone;
    @XmlElement(name = "InsuranceCertificate")
    private InsuranceCertificate insuranceCertificate;
    @XmlElement(name = "OtherTypeName")
    private JaggaerBasicValue otherTypeName;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public JaggaerBasicValue getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(JaggaerBasicValue policyNumber) {
        this.policyNumber = policyNumber;
    }

    public JaggaerBasicValue getInsuranceLimit() {
        return insuranceLimit;
    }

    public void setInsuranceLimit(JaggaerBasicValue insuranceLimit) {
        this.insuranceLimit = insuranceLimit;
    }

    public JaggaerBasicValue getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(JaggaerBasicValue expirationDate) {
        this.expirationDate = expirationDate;
    }

    public JaggaerBasicValue getInsuranceProvider() {
        return insuranceProvider;
    }

    public void setInsuranceProvider(JaggaerBasicValue insuranceProvider) {
        this.insuranceProvider = insuranceProvider;
    }

    public JaggaerBasicValue getAgent() {
        return agent;
    }

    public void setAgent(JaggaerBasicValue agent) {
        this.agent = agent;
    }

    public InsuranceProviderPhone getInsuranceProviderPhone() {
        return insuranceProviderPhone;
    }

    public void setInsuranceProviderPhone(InsuranceProviderPhone insuranceProviderPhone) {
        this.insuranceProviderPhone = insuranceProviderPhone;
    }

    public InsuranceCertificate getInsuranceCertificate() {
        return insuranceCertificate;
    }

    public void setInsuranceCertificate(InsuranceCertificate insuranceCertificate) {
        this.insuranceCertificate = insuranceCertificate;
    }

    public JaggaerBasicValue getOtherTypeName() {
        return otherTypeName;
    }

    public void setOtherTypeName(JaggaerBasicValue otherTypeName) {
        this.otherTypeName = otherTypeName;
    }

}
