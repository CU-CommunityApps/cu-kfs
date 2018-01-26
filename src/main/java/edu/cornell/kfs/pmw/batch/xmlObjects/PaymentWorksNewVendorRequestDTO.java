package edu.cornell.kfs.pmw.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "list-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksNewVendorRequestDTO {
    
    private String id;
    private String request_status;

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

}