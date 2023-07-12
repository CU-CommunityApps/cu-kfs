package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "supplierRef", "errorMessage", "addressErrors", "contactErrors", "locationErrors",
        "accountsPayableErrors", "customElementErrors" })
@XmlRootElement(name = "SupplierErrors")
public class SupplierErrors {

    @XmlElement(name = "SupplierRef", required = true)
    private SupplierRef supplierRef;
    @XmlElement(name = "ErrorMessage")
    private List<ErrorMessage> errorMessage;
    @XmlElement(name = "AddressErrors")
    private List<AddressErrors> addressErrors;
    @XmlElement(name = "ContactErrors")
    private List<ContactErrors> contactErrors;
    @XmlElement(name = "LocationErrors")
    private List<LocationErrors> locationErrors;
    @XmlElement(name = "AccountsPayableErrors")
    private List<AccountsPayableErrors> accountsPayableErrors;
    @XmlElement(name = "CustomElementErrors")
    private List<CustomElementErrors> customElementErrors;

    public SupplierRef getSupplierRef() {
        return supplierRef;
    }

    public void setSupplierRef(SupplierRef supplierRef) {
        this.supplierRef = supplierRef;
    }

    public List<ErrorMessage> getErrorMessage() {
        if (errorMessage == null) {
            errorMessage = new ArrayList<ErrorMessage>();
        }
        return errorMessage;
    }

    public List<AddressErrors> getAddressErrors() {
        if (addressErrors == null) {
            addressErrors = new ArrayList<AddressErrors>();
        }
        return addressErrors;
    }

    public List<ContactErrors> getContactErrors() {
        if (contactErrors == null) {
            contactErrors = new ArrayList<ContactErrors>();
        }
        return contactErrors;
    }

    public List<LocationErrors> getLocationErrors() {
        if (locationErrors == null) {
            locationErrors = new ArrayList<LocationErrors>();
        }
        return locationErrors;
    }

    public List<AccountsPayableErrors> getAccountsPayableErrors() {
        if (accountsPayableErrors == null) {
            accountsPayableErrors = new ArrayList<AccountsPayableErrors>();
        }
        return accountsPayableErrors;
    }

    public List<CustomElementErrors> getCustomElementErrors() {
        if (customElementErrors == null) {
            customElementErrors = new ArrayList<CustomElementErrors>();
        }
        return customElementErrors;
    }

}
