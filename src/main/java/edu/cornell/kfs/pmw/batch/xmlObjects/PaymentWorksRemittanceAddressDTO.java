package edu.cornell.kfs.pmw.batch.xmlObjects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

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
