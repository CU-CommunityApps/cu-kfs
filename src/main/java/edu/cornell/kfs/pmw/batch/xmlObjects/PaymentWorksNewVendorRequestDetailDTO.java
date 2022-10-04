package edu.cornell.kfs.pmw.batch.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksCustomFieldsDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksRequestingCompanyDTO;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksNewVendorRequestDetailDTO {

    private String id;
    private String request_status;
    @XmlElement(name = "custom_fields")
    private PaymentWorksCustomFieldsDTO custom_fields;
    private PaymentWorksRequestingCompanyDTO requesting_company;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequest_status() {
        return request_status;
    }

    public void setRequest_status(String request_status) {
        this.request_status = request_status;
    }

    public PaymentWorksRequestingCompanyDTO getRequesting_company() {
        return requesting_company;
    }

    public void setRequesting_company(PaymentWorksRequestingCompanyDTO requesting_company) {
        this.requesting_company = requesting_company;
    }

    public PaymentWorksCustomFieldsDTO getCustom_fields() {
        return custom_fields;
    }

    public void setCustom_fields(PaymentWorksCustomFieldsDTO custom_fields) {
        this.custom_fields = custom_fields;
    }

}
