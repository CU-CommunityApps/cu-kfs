package edu.cornell.kfs.pmw.batch.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestsDTO;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksNewVendorRequestsRootDTO {
    
    private Integer count;
    private String next;
    private String previous;

    @XmlElement(name = "results")
    private PaymentWorksNewVendorRequestsDTO pmwNewVendorRequestsDTO;

    public PaymentWorksNewVendorRequestsDTO getPmwNewVendorRequestsDTO() {
        return pmwNewVendorRequestsDTO;
    }

    public void setPmwNewVendorRequestsDTO(PaymentWorksNewVendorRequestsDTO pmwNewVendorRequestsDTO) {
        this.pmwNewVendorRequestsDTO = pmwNewVendorRequestsDTO;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }
}
