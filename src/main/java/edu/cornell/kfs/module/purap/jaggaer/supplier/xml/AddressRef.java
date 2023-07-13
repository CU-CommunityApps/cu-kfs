package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "erpNumber", "sqIntegrationNumber", "thirdPartyRefNumber" })
@XmlRootElement(name = "AddressRef")
public class AddressRef implements JaggaerRef {

    @XmlElement(name = "ERPNumber")
    private ErpNumber erpNumber;
    @XmlElement(name = "SQIntegrationNumber")
    private SQIntegrationNumber sqIntegrationNumber;
    @XmlElement(name = "ThirdPartyRefNumber")
    private ThirdPartyRefNumber thirdPartyRefNumber;

    public ErpNumber getErpNumber() {
        return erpNumber;
    }

    @Override
    public void setErpNumber(ErpNumber erpNumber) {
        this.erpNumber = erpNumber;
    }

    public SQIntegrationNumber getSqIntegrationNumber() {
        return sqIntegrationNumber;
    }

    @Override
    public void setSqIntegrationNumber(SQIntegrationNumber sqIntegrationNumber) {
        this.sqIntegrationNumber = sqIntegrationNumber;
    }

    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    @Override
    public void setThirdPartyRefNumber(ThirdPartyRefNumber thirdPartyRefNumber) {
        this.thirdPartyRefNumber = thirdPartyRefNumber;
    }

}
