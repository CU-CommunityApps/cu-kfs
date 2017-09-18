package edu.cornell.kfs.paymentworks.xmlObjects;

import javax.xml.bind.annotation.XmlElement;

public class PaymentWorksNewVendorUpdateVendorStatus {

    private Integer id;
    @XmlElement(name = "request_status")
    private Integer request_status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRequest_status() {
        return request_status;
    }

    public void setRequest_status(Integer requestStatus) {
        this.request_status = requestStatus;
    }

}
