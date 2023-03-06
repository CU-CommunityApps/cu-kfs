
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 */
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

    /**
     * Gets the value of the accountsPayableRef property.
     * 
     * @return
     *     possible object is
     *     {@link AccountsPayableRef }
     *     
     */
    public AccountsPayableRef getAccountsPayableRef() {
        return accountsPayableRef;
    }

    /**
     * Sets the value of the accountsPayableRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccountsPayableRef }
     *     
     */
    public void setAccountsPayableRef(AccountsPayableRef value) {
        this.accountsPayableRef = value;
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the errorMessage property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getErrorMessage().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ErrorMessage }
     * 
     * 
     */
    public List<ErrorMessage> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new ArrayList<ErrorMessage>();
        }
        return this.errorMessage;
    }

}
