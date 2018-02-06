package edu.cornell.kfs.pmw.batch.xmlObjects;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

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
