package edu.cornell.kfs.pmw.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksCustomFieldsDTO;
import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksRequestingCompanyDTO;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksNewVendorRequestDetailDTO {

    @XmlElement(name = "id")
    private String paymentWorksVendorId;
    
    @XmlElement(name = "request_status")
    private String requestStatus;
    
    @XmlElement(name = "custom_fields")
    private PaymentWorksCustomFieldsDTO customFields;
    
    @XmlElement(name = "requesting_company")
    private PaymentWorksRequestingCompanyDTO requestingCompany;

    public String getPaymentWorksVendorId() {
        return paymentWorksVendorId;
    }

    public void setPaymentWorksVendorId(String paymentWorksVendorId) {
        this.paymentWorksVendorId = paymentWorksVendorId;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    public PaymentWorksCustomFieldsDTO getCustomFields() {
        return customFields;
    }

    public void setCustomFields(PaymentWorksCustomFieldsDTO customFields) {
        this.customFields = customFields;
    }

    public PaymentWorksRequestingCompanyDTO getRequestingCompany() {
        return requestingCompany;
    }

    public void setRequestingCompany(PaymentWorksRequestingCompanyDTO requestingCompany) {
        this.requestingCompany = requestingCompany;
    }

}
