
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;



@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "accountsPayableRef",
    "errorMessage"
})
@XmlRootElement(name = "AccountsPayableErrors")
public class AccountsPayableErrors {

    @XmlElement(name = "AccountsPayableRef", required = true)
    protected AccountsPayableRef accountsPayableRef;
    @XmlElement(name = "ErrorMessage")
    protected List<ErrorMessage> errorMessage;

    
    public AccountsPayableRef getAccountsPayableRef() {
        return accountsPayableRef;
    }

    
    public void setAccountsPayableRef(AccountsPayableRef value) {
        this.accountsPayableRef = value;
    }

    
    public List<ErrorMessage> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new ArrayList<ErrorMessage>();
        }
        return this.errorMessage;
    }

}
