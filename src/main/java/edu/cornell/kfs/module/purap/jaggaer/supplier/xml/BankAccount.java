package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "bankName", "accountHoldersName", "accountNumberType", "routingNumber",
        "bankAccountNumber", "ibanBankAccountNumber", "directDepositFormat", "bankIdentifierCode",
        "internationalRoutingCode", "isoCountryCode", "addressLine1", "addressLine2", "addressLine3", "city", "state",
        "postalCode" })
@XmlRootElement(name = "BankAccount")
public class BankAccount {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "BankName")
    protected JaggaerBasicValue bankName;
    @XmlElement(name = "AccountHoldersName")
    protected JaggaerBasicValue accountHoldersName;
    @XmlElement(name = "AccountNumberType")
    protected JaggaerBasicValue accountNumberType;
    @XmlElement(name = "RoutingNumber")
    protected JaggaerBasicValue routingNumber;
    @XmlElement(name = "BankAccountNumber")
    protected JaggaerBasicValue bankAccountNumber;
    @XmlElement(name = "IbanBankAccountNumber")
    protected JaggaerBasicValue ibanBankAccountNumber;
    @XmlElement(name = "DirectDepositFormat")
    protected JaggaerBasicValue directDepositFormat;
    @XmlElement(name = "BankIdentifierCode")
    protected JaggaerBasicValue bankIdentifierCode;
    @XmlElement(name = "InternationalRoutingCode")
    protected JaggaerBasicValue internationalRoutingCode;
    @XmlElement(name = "IsoCountryCode")
    protected IsoCountryCode isoCountryCode;
    @XmlElement(name = "AddressLine1")
    protected AddressLine addressLine1;
    @XmlElement(name = "AddressLine2")
    protected AddressLine addressLine2;
    @XmlElement(name = "AddressLine3")
    protected AddressLine addressLine3;
    @XmlElement(name = "City")
    protected City city;
    @XmlElement(name = "State")
    protected State state;
    @XmlElement(name = "PostalCode")
    protected PostalCode postalCode;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String isChanged) {
        this.isChanged = isChanged;
    }

    public JaggaerBasicValue getBankName() {
        return bankName;
    }

    public void setBankName(JaggaerBasicValue bankName) {
        this.bankName = bankName;
    }

    public JaggaerBasicValue getAccountHoldersName() {
        return accountHoldersName;
    }

    public void setAccountHoldersName(JaggaerBasicValue accountHoldersName) {
        this.accountHoldersName = accountHoldersName;
    }

    public JaggaerBasicValue getAccountNumberType() {
        return accountNumberType;
    }

    public void setAccountNumberType(JaggaerBasicValue accountNumberType) {
        this.accountNumberType = accountNumberType;
    }

    public JaggaerBasicValue getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(JaggaerBasicValue routingNumber) {
        this.routingNumber = routingNumber;
    }

    public JaggaerBasicValue getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(JaggaerBasicValue bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public JaggaerBasicValue getIbanBankAccountNumber() {
        return ibanBankAccountNumber;
    }

    public void setIbanBankAccountNumber(JaggaerBasicValue ibanBankAccountNumber) {
        this.ibanBankAccountNumber = ibanBankAccountNumber;
    }

    public JaggaerBasicValue getDirectDepositFormat() {
        return directDepositFormat;
    }

    public void setDirectDepositFormat(JaggaerBasicValue directDepositFormat) {
        this.directDepositFormat = directDepositFormat;
    }

    public JaggaerBasicValue getBankIdentifierCode() {
        return bankIdentifierCode;
    }

    public void setBankIdentifierCode(JaggaerBasicValue bankIdentifierCode) {
        this.bankIdentifierCode = bankIdentifierCode;
    }

    public JaggaerBasicValue getInternationalRoutingCode() {
        return internationalRoutingCode;
    }

    public void setInternationalRoutingCode(JaggaerBasicValue internationalRoutingCode) {
        this.internationalRoutingCode = internationalRoutingCode;
    }

    public IsoCountryCode getIsoCountryCode() {
        return isoCountryCode;
    }

    public void setIsoCountryCode(IsoCountryCode isoCountryCode) {
        this.isoCountryCode = isoCountryCode;
    }

    public AddressLine getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(AddressLine addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public AddressLine getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(AddressLine addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public AddressLine getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(AddressLine addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public PostalCode getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(PostalCode postalCode) {
        this.postalCode = postalCode;
    }

}
