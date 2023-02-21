
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "contactName", "externalId", "baselineExternalId", "thirdPartyRefNumber",
        "baselineThirdPartyRefNumber", "active" })
@XmlRootElement(name = "ContactHeader")
public class ContactHeader {

    @XmlElement(name = "ContactName")
    protected ContactName contactName;
    @XmlElement(name = "ExternalId")
    protected ExternalId externalId;
    @XmlElement(name = "BaselineExternalId")
    protected BaselineExternalId baselineExternalId;
    @XmlElement(name = "ThirdPartyRefNumber")
    protected ThirdPartyRefNumber thirdPartyRefNumber;
    @XmlElement(name = "BaselineThirdPartyRefNumber")
    protected BaselineThirdPartyRefNumber baselineThirdPartyRefNumber;
    @XmlElement(name = "Active")
    protected Active active;

    public ContactName getContactName() {
        return contactName;
    }

    public void setContactName(ContactName value) {
        this.contactName = value;
    }

    public ExternalId getExternalId() {
        return externalId;
    }

    public void setExternalId(ExternalId value) {
        this.externalId = value;
    }

    public BaselineExternalId getBaselineExternalId() {
        return baselineExternalId;
    }

    public void setBaselineExternalId(BaselineExternalId value) {
        this.baselineExternalId = value;
    }

    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    public void setThirdPartyRefNumber(ThirdPartyRefNumber value) {
        this.thirdPartyRefNumber = value;
    }

    public BaselineThirdPartyRefNumber getBaselineThirdPartyRefNumber() {
        return baselineThirdPartyRefNumber;
    }

    public void setBaselineThirdPartyRefNumber(BaselineThirdPartyRefNumber value) {
        this.baselineThirdPartyRefNumber = value;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active value) {
        this.active = value;
    }

}
