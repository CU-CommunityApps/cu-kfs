package edu.cornell.kfs.pmw.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksAddressBaseDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksRemittanceAddressesDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksTaxClassificationDTO;

@XmlRootElement(name = "requesting_company")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksRequestingCompanyDTO {

    private String id;
    
    @XmlElement(name = "legal_name")
    private String companyLegalName;
    
    @XmlElement(name = "desc")
    private String description;
    
    @XmlElement(name = "name")
    private String companyName;
    
    @XmlElement(name = "legal_last_name")
    private String legalLastName;
    
    @XmlElement(name = "legal_first_name")
    private String legalFirstName;
    
    private String url;
    
    private String tin;
    
    @XmlElement(name = "tin_type")
    private String tinType;
    
    @XmlElement(name = "tin_name_validation_status")
    private String tinNameValidationStatus;
    
    @XmlElement(name = "tax_country")
    private String taxCountry;
    
    @XmlElement(name = "w8_w9")
    private String w8w9Url;
    
    private String telephone;
    
    private String duns;
    
    @XmlElement(name = "corporate_email")
    private String corporateEmail;
    
    @XmlElement(name = "corp_address")
    private PaymentWorksAddressBaseDTO corporateAddress;
    
    private PaymentWorksRemittanceAddressesDTO remittanceAddresses;
    
    private PaymentWorksTaxClassificationDTO taxClassification;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String companyLegalName) {
        this.companyLegalName = companyLegalName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getLegalLastName() {
        return legalLastName;
    }

    public void setLegalLastName(String legalLastName) {
        this.legalLastName = legalLastName;
    }

    public String getLegalFirstName() {
        return legalFirstName;
    }

    public void setLegalFirstName(String legalFirstName) {
        this.legalFirstName = legalFirstName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTin() {
        return tin;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public String getTinType() {
        return tinType;
    }

    public void setTinType(String tinType) {
        this.tinType = tinType;
    }

    public String getTinNameValidationStatus() {
        return tinNameValidationStatus;
    }

    public void setTinNameValidationStatus(String tinNameValidationStatus) {
        this.tinNameValidationStatus = tinNameValidationStatus;
    }

    public String getTaxCountry() {
        return taxCountry;
    }

    public void setTaxCountry(String taxCountry) {
        this.taxCountry = taxCountry;
    }

    public String getW8w9Url() {
        return w8w9Url;
    }

    public void setW8w9Url(String w8w9Url) {
        this.w8w9Url = w8w9Url;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getDuns() {
        return duns;
    }

    public void setDuns(String duns) {
        this.duns = duns;
    }

    public String getCorporateEmail() {
        return corporateEmail;
    }

    public void setCorporateEmail(String corporateEmail) {
        this.corporateEmail = corporateEmail;
    }

    public PaymentWorksAddressBaseDTO getCorporateAddress() {
        return corporateAddress;
    }

    public void setCorporateAddress(PaymentWorksAddressBaseDTO corporateAddress) {
        this.corporateAddress = corporateAddress;
    }

    public PaymentWorksRemittanceAddressesDTO getRemittanceAddresses() {
        return remittanceAddresses;
    }

    public void setRemittanceAddresses(PaymentWorksRemittanceAddressesDTO remittanceAddresses) {
        this.remittanceAddresses = remittanceAddresses;
    }

    public PaymentWorksTaxClassificationDTO getTaxClassification() {
        return taxClassification;
    }

    public void setTaxClassification(PaymentWorksTaxClassificationDTO taxClassification) {
        this.taxClassification = taxClassification;
    }

}
