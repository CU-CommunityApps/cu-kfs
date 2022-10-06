package edu.cornell.kfs.pmw.batch.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksCorpAddressDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksRemittanceAddressesDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksTaxClassificationDTO;

@XmlRootElement(name = "requesting_company")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksRequestingCompanyDTO {

    private String id;
    private String legal_name;
    private String desc;
    private String name;
    private String legal_last_name;
    private String legal_first_name;
    private String url;
    private String tin;
    private String tin_type;
    private String tin_name_validation_status;
    private String tax_country;
    private String w8_w9;
    private String telephone;
    private String duns;
    private String corporate_email;
    private PaymentWorksCorpAddressDTO corp_address;
    private PaymentWorksRemittanceAddressesDTO remittance_addresses;
    private PaymentWorksTaxClassificationDTO tax_classification;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLegal_name() {
        return legal_name;
    }

    public void setLegal_name(String legal_name) {
        this.legal_name = legal_name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getW8_w9() {
        return w8_w9;
    }

    public void setW8_w9(String w8_w9) {
        this.w8_w9 = w8_w9;
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

    public PaymentWorksCorpAddressDTO getCorp_address() {
        return corp_address;
    }

    public void setCorp_address(PaymentWorksCorpAddressDTO corp_address) {
        this.corp_address = corp_address;
    }

    public PaymentWorksRemittanceAddressesDTO getRemittance_addresses() {
        return remittance_addresses;
    }

    public void setRemittance_addresses(PaymentWorksRemittanceAddressesDTO remittance_addresses) {
        this.remittance_addresses = remittance_addresses;
    }

    public PaymentWorksTaxClassificationDTO getTax_classification() {
        return tax_classification;
    }

    public void setTax_classification(PaymentWorksTaxClassificationDTO tax_classification) {
        this.tax_classification = tax_classification;
    }

    public String getTin_type() {
        return tin_type;
    }

    public void setTin_type(String tin_type) {
        this.tin_type = tin_type;
    }

    public String getTax_country() {
        return tax_country;
    }

    public void setTax_country(String tax_country) {
        this.tax_country = tax_country;
    }

    public String getLegal_last_name() {
        return legal_last_name;
    }

    public void setLegal_last_name(String legal_last_name) {
        this.legal_last_name = legal_last_name;
    }

    public String getLegal_first_name() {
        return legal_first_name;
    }

    public void setLegal_first_name(String legal_first_name) {
        this.legal_first_name = legal_first_name;
    }

    public String getTin_name_validation_status() {
        return tin_name_validation_status;
    }

    public void setTin_name_validation_status(String tin_name_validation_status) {
        this.tin_name_validation_status = tin_name_validation_status;
    }

    public String getCorporate_email() {
        return corporate_email;
    }

    public void setCorporate_email(String corporate_email) {
        this.corporate_email = corporate_email;
    }

}
