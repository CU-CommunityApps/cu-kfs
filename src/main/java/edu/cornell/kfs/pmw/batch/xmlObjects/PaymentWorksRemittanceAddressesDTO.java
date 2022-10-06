package edu.cornell.kfs.pmw.batch.xmlObjects;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksRemittanceAddressDTO;

@XmlRootElement(name = "remittance_addresses")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksRemittanceAddressesDTO {

    @XmlElement(name = "list-item")
    private List<PaymentWorksRemittanceAddressDTO> remittance_address;

    public List<PaymentWorksRemittanceAddressDTO> getRemittance_address() {
        return remittance_address;
    }

    public void setRemittance_address(List<PaymentWorksRemittanceAddressDTO> remittance_address) {
        this.remittance_address = remittance_address;
    }

}
