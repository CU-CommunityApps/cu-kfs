package edu.cornell.kfs.module.purap.jaggaer.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supplierRef", "errorMessage", "addressErrors", "contactErrors", "locationErrors",
        "accountsPayableErrors", "customElementErrors" })
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

    public SupplierRef getSupplierRef() {
        return supplierRef;
    }

    public void setSupplierRef(SupplierRef value) {
        this.supplierRef = value;
    }

    public List<ErrorMessage> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new ArrayList<ErrorMessage>();
        }
        return this.errorMessage;
    }

    public List<AddressErrors> getAddressErrors() {
        if (addressErrors == null) {
            addressErrors = new ArrayList<AddressErrors>();
        }
        return this.addressErrors;
    }

    public List<ContactErrors> getContactErrors() {
        if (contactErrors == null) {
            contactErrors = new ArrayList<ContactErrors>();
        }
        return this.contactErrors;
    }

    public List<LocationErrors> getLocationErrors() {
        if (locationErrors == null) {
            locationErrors = new ArrayList<LocationErrors>();
        }
        return this.locationErrors;
    }

    public List<AccountsPayableErrors> getAccountsPayableErrors() {
        if (accountsPayableErrors == null) {
            accountsPayableErrors = new ArrayList<AccountsPayableErrors>();
        }
        return this.accountsPayableErrors;
    }

    public List<CustomElementErrors> getCustomElementErrors() {
        if (customElementErrors == null) {
            customElementErrors = new ArrayList<CustomElementErrors>();
        }
        return this.customElementErrors;
    }

}
