package edu.cornell.kfs.pmw.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "list-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksNewVendorRequestDTO {
    
    @XmlElement(name = "id")
    private String paymentWorksVendorId;
    
    @XmlElement(name = "request_status")
    private String requestStatus;

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

}
