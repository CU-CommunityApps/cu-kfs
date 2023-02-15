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
    protected BankName bankName;
    @XmlElement(name = "AccountHoldersName")
    protected AccountHoldersName accountHoldersName;
    @XmlElement(name = "AccountNumberType")
    protected AccountNumberType accountNumberType;
    @XmlElement(name = "RoutingNumber")
    protected RoutingNumber routingNumber;
    @XmlElement(name = "BankAccountNumber")
    protected BankAccountNumber bankAccountNumber;
    @XmlElement(name = "IbanBankAccountNumber")
    protected IbanBankAccountNumber ibanBankAccountNumber;
    @XmlElement(name = "DirectDepositFormat")
    protected DirectDepositFormat directDepositFormat;
    @XmlElement(name = "BankIdentifierCode")
    protected BankIdentifierCode bankIdentifierCode;
    @XmlElement(name = "InternationalRoutingCode")
    protected InternationalRoutingCode internationalRoutingCode;
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

    public BankName getBankName() {
        return bankName;
    }

    public void setBankName(BankName value) {
        this.bankName = value;
    }

    public AccountHoldersName getAccountHoldersName() {
        return accountHoldersName;
    }

    public void setAccountHoldersName(AccountHoldersName value) {
        this.accountHoldersName = value;
    }

    public AccountNumberType getAccountNumberType() {
        return accountNumberType;
    }

    public void setAccountNumberType(AccountNumberType value) {
        this.accountNumberType = value;
    }

    public RoutingNumber getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(RoutingNumber value) {
        this.routingNumber = value;
    }

    public BankAccountNumber getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(BankAccountNumber value) {
        this.bankAccountNumber = value;
    }

    public IbanBankAccountNumber getIbanBankAccountNumber() {
        return ibanBankAccountNumber;
    }

    public void setIbanBankAccountNumber(IbanBankAccountNumber value) {
        this.ibanBankAccountNumber = value;
    }

    public DirectDepositFormat getDirectDepositFormat() {
        return directDepositFormat;
    }

    public void setDirectDepositFormat(DirectDepositFormat value) {
        this.directDepositFormat = value;
    }

    public BankIdentifierCode getBankIdentifierCode() {
        return bankIdentifierCode;
    }

    public void setBankIdentifierCode(BankIdentifierCode value) {
        this.bankIdentifierCode = value;
    }

    public InternationalRoutingCode getInternationalRoutingCode() {
        return internationalRoutingCode;
    }

    public void setInternationalRoutingCode(InternationalRoutingCode value) {
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
