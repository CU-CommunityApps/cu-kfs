package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "accountsPayableRef", "errorMessages" })
@XmlRootElement(name = "AccountsPayableErrors")
public class AccountsPayableError {
    @XmlElement(name = "AccountsPayableRef", required = true)
    private AccountsPayableRef accountsPayableRef;
    @XmlElement(name = "ErrorMessage")
    private List<ErrorMessage> errorMessages;

    public AccountsPayableRef getAccountsPayableRef() {
        return accountsPayableRef;
    }

    public void setAccountsPayableRef(AccountsPayableRef accountsPayableRef) {
        this.accountsPayableRef = accountsPayableRef;
    }

    public List<ErrorMessage> getErrorMessages() {
        if (errorMessages == null) {
            errorMessages = new ArrayList<ErrorMessage>();
        }
        return errorMessages;
    }
}
