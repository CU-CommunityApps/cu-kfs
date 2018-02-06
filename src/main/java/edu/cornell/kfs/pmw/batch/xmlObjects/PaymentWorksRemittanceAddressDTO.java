package edu.cornell.kfs.pmw.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import edu.cornell.kfs.pmw.batch.xmlObjects.PaymentWorksBankAccountDTO;

@XmlRootElement(name = "list-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksRemittanceAddressDTO {

    private PaymentWorksAddressBaseDTO remitAddress;
    
    @XmlElement(name = "bank_account")
    private PaymentWorksBankAccountDTO bankAccount;

    public PaymentWorksBankAccountDTO getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(PaymentWorksBankAccountDTO bankAccount) {
        this.bankAccount = bankAccount;
    }

    public PaymentWorksAddressBaseDTO getRemitAddress() {
        return remitAddress;
    }

    public void setRemitAddress(PaymentWorksAddressBaseDTO remitAddress) {
        this.remitAddress = remitAddress;
    }

}
