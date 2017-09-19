package edu.cornell.kfs.paymentworks.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksNewVendorsRootDTO {
    private Integer count;
    private String next;
    private String previous;

    @XmlElement(name = "results")
    private PaymentWorksNewVendorsResultsDTO newVendors;

    public PaymentWorksNewVendorsResultsDTO getNewVendors() {
        return newVendors;
    }

    public void setNewVendors(PaymentWorksNewVendorsResultsDTO newVendor) {
        this.newVendors = newVendors;
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
