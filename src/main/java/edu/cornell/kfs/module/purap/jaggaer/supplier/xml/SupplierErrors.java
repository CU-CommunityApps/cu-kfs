
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "supplierRef",
    "errorMessage",
    "addressErrors",
    "contactErrors",
    "locationErrors",
    "accountsPayableErrors",
    "customElementErrors"
})
@XmlRootElement(name = "SupplierErrors")
public class SupplierErrors {

    @XmlElement(name = "SupplierRef", required = true)
    protected SupplierRef supplierRef;
    @XmlElement(name = "ErrorMessage")
    protected List<ErrorMessage> errorMessage;
    @XmlElement(name = "AddressErrors")
    protected List<AddressErrors> addressErrors;
    @XmlElement(name = "ContactErrors")
    protected List<ContactErrors> contactErrors;
    @XmlElement(name = "LocationErrors")
    protected List<LocationErrors> locationErrors;
    @XmlElement(name = "AccountsPayableErrors")
    protected List<AccountsPayableErrors> accountsPayableErrors;
    @XmlElement(name = "CustomElementErrors")
    protected List<CustomElementErrors> customElementErrors;

    /**
     * Gets the value of the supplierRef property.
     * 
     * @return
     *     possible object is
     *     {@link SupplierRef }
     *     
     */
    public SupplierRef getSupplierRef() {
        return supplierRef;
    }

    /**
     * Sets the value of the supplierRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link SupplierRef }
     *     
     */
    public void setSupplierRef(SupplierRef value) {
        this.supplierRef = value;
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

    /**
     * Gets the value of the addressErrors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the addressErrors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAddressErrors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AddressErrors }
     * 
     * 
     */
    public List<AddressErrors> getAddressErrors() {
        if (addressErrors == null) {
            addressErrors = new ArrayList<AddressErrors>();
        }
        return this.addressErrors;
    }

    /**
     * Gets the value of the contactErrors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contactErrors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContactErrors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ContactErrors }
     * 
     * 
     */
    public List<ContactErrors> getContactErrors() {
        if (contactErrors == null) {
            contactErrors = new ArrayList<ContactErrors>();
        }
        return this.contactErrors;
    }

    /**
     * Gets the value of the locationErrors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the locationErrors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLocationErrors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LocationErrors }
     * 
     * 
     */
    public List<LocationErrors> getLocationErrors() {
        if (locationErrors == null) {
            locationErrors = new ArrayList<LocationErrors>();
        }
        return this.locationErrors;
    }

    /**
     * Gets the value of the accountsPayableErrors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the accountsPayableErrors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAccountsPayableErrors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AccountsPayableErrors }
     * 
     * 
     */
    public List<AccountsPayableErrors> getAccountsPayableErrors() {
        if (accountsPayableErrors == null) {
            accountsPayableErrors = new ArrayList<AccountsPayableErrors>();
        }
        return this.accountsPayableErrors;
    }

    /**
     * Gets the value of the customElementErrors property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the customElementErrors property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustomElementErrors().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link CustomElementErrors }
     * 
     * 
     */
    public List<CustomElementErrors> getCustomElementErrors() {
        if (customElementErrors == null) {
            customElementErrors = new ArrayList<CustomElementErrors>();
        }
        return this.customElementErrors;
    }

}
