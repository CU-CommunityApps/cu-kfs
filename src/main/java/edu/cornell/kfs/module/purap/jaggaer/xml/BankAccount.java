//
package edu.cornell.kfs.module.purap.jaggaer.xml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.CollapsedStringAdapter;
import jakarta.xml.bind.annotation.adapters.NormalizedStringAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 */
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
    protected AddressLine1 addressLine1;
    @XmlElement(name = "AddressLine2")
    protected AddressLine2 addressLine2;
    @XmlElement(name = "AddressLine3")
    protected AddressLine3 addressLine3;
    @XmlElement(name = "City")
    protected City city;
    @XmlElement(name = "State")
    protected State state;
    @XmlElement(name = "PostalCode")
    protected PostalCode postalCode;

    public String getType() {
        return type;
    }

    public void setType(String value) {
        this.type = value;
    }

    public String getIsChanged() {
        return isChanged;
    }

    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    public JaggaerBasicValue getBankName() {
        return bankName;
    }

    public void setBankName(JaggaerBasicValue value) {
        this.bankName = value;
    }

    public JaggaerBasicValue getAccountHoldersName() {
        return accountHoldersName;
    }

    public void setAccountHoldersName(JaggaerBasicValue value) {
        this.accountHoldersName = value;
    }

    public JaggaerBasicValue getAccountNumberType() {
        return accountNumberType;
    }

    public void setAccountNumberType(JaggaerBasicValue value) {
        this.accountNumberType = value;
    }

    public JaggaerBasicValue getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(JaggaerBasicValue value) {
        this.routingNumber = value;
    }

    public JaggaerBasicValue getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(JaggaerBasicValue value) {
        this.bankAccountNumber = value;
    }

    public JaggaerBasicValue getIbanBankAccountNumber() {
        return ibanBankAccountNumber;
    }

    public void setIbanBankAccountNumber(JaggaerBasicValue value) {
        this.ibanBankAccountNumber = value;
    }

    public JaggaerBasicValue getDirectDepositFormat() {
        return directDepositFormat;
    }

    public void setDirectDepositFormat(JaggaerBasicValue value) {
        this.directDepositFormat = value;
    }

    public JaggaerBasicValue getBankIdentifierCode() {
        return bankIdentifierCode;
    }

    public void setBankIdentifierCode(JaggaerBasicValue value) {
        this.bankIdentifierCode = value;
    }

    public JaggaerBasicValue getInternationalRoutingCode() {
        return internationalRoutingCode;
    }

    public void setInternationalRoutingCode(JaggaerBasicValue value) {
        this.internationalRoutingCode = value;
    }

    public IsoCountryCode getIsoCountryCode() {
        return isoCountryCode;
    }

    public void setIsoCountryCode(IsoCountryCode value) {
        this.isoCountryCode = value;
    }

    public AddressLine1 getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(AddressLine1 value) {
        this.addressLine1 = value;
    }

    public AddressLine2 getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(AddressLine2 value) {
        this.addressLine2 = value;
    }

    public AddressLine3 getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(AddressLine3 value) {
        this.addressLine3 = value;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City value) {
        this.city = value;
    }

    public State getState() {
        return state;
    }

    public void setState(State value) {
        this.state = value;
    }

    public PostalCode getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(PostalCode value) {
        this.postalCode = value;
    }

}
