package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supplierRef", "errorMessages", "addressErrors", "contactErrors", "locationErrors",
        "accountsPayableErrors", "customElementErrors" })
@XmlRootElement(name = "SupplierErrors")
public class SupplierError {

    @XmlElement(name = "SupplierRef", required = true)
    private SupplierRef supplierRef;
    @XmlElement(name = "ErrorMessage")
    private List<ErrorMessage> errorMessages;
    @XmlElement(name = "AddressErrors")
    private List<AddressError> addressErrors;
    @XmlElement(name = "ContactErrors")
    private List<ContactError> contactErrors;
    @XmlElement(name = "LocationErrors")
    private List<LocationError> locationErrors;
    @XmlElement(name = "AccountsPayableErrors")
    private List<AccountsPayableError> accountsPayableErrors;
    @XmlElement(name = "CustomElementErrors")
    private List<CustomElementError> customElementErrors;

    public SupplierRef getSupplierRef() {
        return supplierRef;
    }

    public void setSupplierRef(SupplierRef supplierRef) {
        this.supplierRef = supplierRef;
    }

    public List<ErrorMessage> getErrorMessages() {
        if (errorMessages == null) {
            errorMessages = new ArrayList<ErrorMessage>();
        }
        return errorMessages;
    }

    public List<AddressError> getAddressErrors() {
        if (addressErrors == null) {
            addressErrors = new ArrayList<AddressError>();
        }
        return addressErrors;
    }

    public List<ContactError> getContactErrors() {
        if (contactErrors == null) {
            contactErrors = new ArrayList<ContactError>();
        }
        return contactErrors;
    }

    public List<LocationError> getLocationErrors() {
        if (locationErrors == null) {
            locationErrors = new ArrayList<LocationError>();
        }
        return locationErrors;
    }

    public List<AccountsPayableError> getAccountsPayableErrors() {
        if (accountsPayableErrors == null) {
            accountsPayableErrors = new ArrayList<AccountsPayableError>();
        }
        return accountsPayableErrors;
    }

    public List<CustomElementError> getCustomElementErrors() {
        if (customElementErrors == null) {
            customElementErrors = new ArrayList<CustomElementError>();
        }
        return customElementErrors;
    }

}
