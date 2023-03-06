
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


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "bankName",
    "accountHoldersName",
    "accountNumberType",
    "routingNumber",
    "bankAccountNumber",
    "ibanBankAccountNumber",
    "directDepositFormat",
    "bankIdentifierCode",
    "internationalRoutingCode",
    "isoCountryCode",
    "addressLine1",
    "addressLine2",
    "addressLine3",
    "city",
    "state",
    "postalCode"
})
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

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Gets the value of the isChanged property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIsChanged() {
        return isChanged;
    }

    /**
     * Sets the value of the isChanged property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIsChanged(String value) {
        this.isChanged = value;
    }

    /**
     * Gets the value of the bankName property.
     * 
     * @return
     *     possible object is
     *     {@link BankName }
     *     
     */
    public BankName getBankName() {
        return bankName;
    }

    /**
     * Sets the value of the bankName property.
     * 
     * @param value
     *     allowed object is
     *     {@link BankName }
     *     
     */
    public void setBankName(BankName value) {
        this.bankName = value;
    }

    /**
     * Gets the value of the accountHoldersName property.
     * 
     * @return
     *     possible object is
     *     {@link AccountHoldersName }
     *     
     */
    public AccountHoldersName getAccountHoldersName() {
        return accountHoldersName;
    }

    /**
     * Sets the value of the accountHoldersName property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccountHoldersName }
     *     
     */
    public void setAccountHoldersName(AccountHoldersName value) {
        this.accountHoldersName = value;
    }

    /**
     * Gets the value of the accountNumberType property.
     * 
     * @return
     *     possible object is
     *     {@link AccountNumberType }
     *     
     */
    public AccountNumberType getAccountNumberType() {
        return accountNumberType;
    }

    /**
     * Sets the value of the accountNumberType property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccountNumberType }
     *     
     */
    public void setAccountNumberType(AccountNumberType value) {
        this.accountNumberType = value;
    }

    /**
     * Gets the value of the routingNumber property.
     * 
     * @return
     *     possible object is
     *     {@link RoutingNumber }
     *     
     */
    public RoutingNumber getRoutingNumber() {
        return routingNumber;
    }

    /**
     * Sets the value of the routingNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link RoutingNumber }
     *     
     */
    public void setRoutingNumber(RoutingNumber value) {
        this.routingNumber = value;
    }

    /**
     * Gets the value of the bankAccountNumber property.
     * 
     * @return
     *     possible object is
     *     {@link BankAccountNumber }
     *     
     */
    public BankAccountNumber getBankAccountNumber() {
        return bankAccountNumber;
    }

    /**
     * Sets the value of the bankAccountNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link BankAccountNumber }
     *     
     */
    public void setBankAccountNumber(BankAccountNumber value) {
        this.bankAccountNumber = value;
    }

    /**
     * Gets the value of the ibanBankAccountNumber property.
     * 
     * @return
     *     possible object is
     *     {@link IbanBankAccountNumber }
     *     
     */
    public IbanBankAccountNumber getIbanBankAccountNumber() {
        return ibanBankAccountNumber;
    }

    /**
     * Sets the value of the ibanBankAccountNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link IbanBankAccountNumber }
     *     
     */
    public void setIbanBankAccountNumber(IbanBankAccountNumber value) {
        this.ibanBankAccountNumber = value;
    }

    /**
     * Gets the value of the directDepositFormat property.
     * 
     * @return
     *     possible object is
     *     {@link DirectDepositFormat }
     *     
     */
    public DirectDepositFormat getDirectDepositFormat() {
        return directDepositFormat;
    }

    /**
     * Sets the value of the directDepositFormat property.
     * 
     * @param value
     *     allowed object is
     *     {@link DirectDepositFormat }
     *     
     */
    public void setDirectDepositFormat(DirectDepositFormat value) {
        this.directDepositFormat = value;
    }

    /**
     * Gets the value of the bankIdentifierCode property.
     * 
     * @return
     *     possible object is
     *     {@link BankIdentifierCode }
     *     
     */
    public BankIdentifierCode getBankIdentifierCode() {
        return bankIdentifierCode;
    }

    /**
     * Sets the value of the bankIdentifierCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link BankIdentifierCode }
     *     
     */
    public void setBankIdentifierCode(BankIdentifierCode value) {
        this.bankIdentifierCode = value;
    }

    /**
     * Gets the value of the internationalRoutingCode property.
     * 
     * @return
     *     possible object is
     *     {@link InternationalRoutingCode }
     *     
     */
    public InternationalRoutingCode getInternationalRoutingCode() {
        return internationalRoutingCode;
    }

    /**
     * Sets the value of the internationalRoutingCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link InternationalRoutingCode }
     *     
     */
    public void setInternationalRoutingCode(InternationalRoutingCode value) {
        this.internationalRoutingCode = value;
    }

    /**
     * Gets the value of the isoCountryCode property.
     * 
     * @return
     *     possible object is
     *     {@link IsoCountryCode }
     *     
     */
    public IsoCountryCode getIsoCountryCode() {
        return isoCountryCode;
    }

    /**
     * Sets the value of the isoCountryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link IsoCountryCode }
     *     
     */
    public void setIsoCountryCode(IsoCountryCode value) {
        this.isoCountryCode = value;
    }

    /**
     * Gets the value of the addressLine1 property.
     * 
     * @return
     *     possible object is
     *     {@link AddressLine1 }
     *     
     */
    public AddressLine1 getAddressLine1() {
        return addressLine1;
    }

    /**
     * Sets the value of the addressLine1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressLine1 }
     *     
     */
    public void setAddressLine1(AddressLine1 value) {
        this.addressLine1 = value;
    }

    /**
     * Gets the value of the addressLine2 property.
     * 
     * @return
     *     possible object is
     *     {@link AddressLine2 }
     *     
     */
    public AddressLine2 getAddressLine2() {
        return addressLine2;
    }

    /**
     * Sets the value of the addressLine2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressLine2 }
     *     
     */
    public void setAddressLine2(AddressLine2 value) {
        this.addressLine2 = value;
    }

    /**
     * Gets the value of the addressLine3 property.
     * 
     * @return
     *     possible object is
     *     {@link AddressLine3 }
     *     
     */
    public AddressLine3 getAddressLine3() {
        return addressLine3;
    }

    /**
     * Sets the value of the addressLine3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link AddressLine3 }
     *     
     */
    public void setAddressLine3(AddressLine3 value) {
        this.addressLine3 = value;
    }

    /**
     * Gets the value of the city property.
     * 
     * @return
     *     possible object is
     *     {@link City }
     *     
     */
    public City getCity() {
        return city;
    }

    /**
     * Sets the value of the city property.
     * 
     * @param value
     *     allowed object is
     *     {@link City }
     *     
     */
    public void setCity(City value) {
        this.city = value;
    }

    /**
     * Gets the value of the state property.
     * 
     * @return
     *     possible object is
     *     {@link State }
     *     
     */
    public State getState() {
        return state;
    }

    /**
     * Sets the value of the state property.
     * 
     * @param value
     *     allowed object is
     *     {@link State }
     *     
     */
    public void setState(State value) {
        this.state = value;
    }

    /**
     * Gets the value of the postalCode property.
     * 
     * @return
     *     possible object is
     *     {@link PostalCode }
     *     
     */
    public PostalCode getPostalCode() {
        return postalCode;
    }

    /**
     * Sets the value of the postalCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link PostalCode }
     *     
     */
    public void setPostalCode(PostalCode value) {
        this.postalCode = value;
    }

}
