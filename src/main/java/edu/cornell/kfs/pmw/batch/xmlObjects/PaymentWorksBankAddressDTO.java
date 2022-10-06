package edu.cornell.kfs.pmw.batch.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksAddressBaseDTO;

@XmlRootElement(name = "bank_address")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksBankAddressDTO extends PaymentWorksAddressBaseDTO {
}
