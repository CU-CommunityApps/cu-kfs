package edu.cornell.kfs.pmw.batch.xmlObjects;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksNewVendorRequestsDTO;

@XmlRootElement(name = "results")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksNewVendorRequestsDTO {
    
    @XmlElement(name = "list-item")
    private List<PaymentWorksNewVendorRequestDTO> pmwNewVendorRequests;

    public List<PaymentWorksNewVendorRequestDTO> getPmwNewVendorRequests() {
        return pmwNewVendorRequests;
    }

    public void setPmwNewVendorRequests(List<PaymentWorksNewVendorRequestDTO> pmwNewVendorRequests) {
        this.pmwNewVendorRequests = pmwNewVendorRequests;
    }

}
