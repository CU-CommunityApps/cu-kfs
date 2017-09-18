package edu.cornell.kfs.paymentworks.xmlObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "results")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksNewVendorsResultsDTO {
    @XmlElement(name = "list-item")
    private List<PaymentWorksNewVendorDTO> newVendorList;

    public List<PaymentWorksNewVendorDTO> getNewVendorList() {
        return newVendorList;
    }

    public void setNewVendorList(List<PaymentWorksNewVendorDTO> newVendorList) {
        this.newVendorList = newVendorList;
    }

}
