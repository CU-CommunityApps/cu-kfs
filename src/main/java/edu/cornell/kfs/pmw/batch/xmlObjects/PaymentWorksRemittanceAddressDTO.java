package edu.cornell.kfs.pmw.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksBankAccountDTO;

@XmlRootElement(name = "list-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksRemittanceAddressDTO extends PaymentWorksAddressBaseDTO {

    private PaymentWorksBankAccountDTO bank_acct;

    public PaymentWorksBankAccountDTO getBank_acct() {
        return bank_acct;
    }

    public void setBank_acct(PaymentWorksBankAccountDTO bank_acct) {
        this.bank_acct = bank_acct;
    }

}
