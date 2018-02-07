package edu.cornell.kfs.pmw.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksAddressBaseDTO;

@XmlRootElement(name = "corp_address")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksCorpAddressDTO extends PaymentWorksAddressBaseDTO {
}
