
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
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
    "erpNumber",
    "oldERPNumber",
    "sqIntegrationNumber",
    "thirdPartyRefNumber",
    "name",
    "active",
    "associatedAddress",
    "email",
    "isoCurrencyCode",
    "contactName",
    "purpose",
    "accountId",
    "accountHolderName",
    "accountType",
    "countryCode",
    "bankAccount",
    "flexFields"
})
@XmlRootElement(name = "AccountsPayable")
public class AccountsPayable {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String isChanged;
    @XmlElement(name = "ERPNumber")
    protected ERPNumber erpNumber;
    @XmlElement(name = "OldERPNumber")
    protected String oldERPNumber;
    @XmlElement(name = "SQIntegrationNumber")
    protected SQIntegrationNumber sqIntegrationNumber;
    @XmlElement(name = "ThirdPartyRefNumber")
    protected ThirdPartyRefNumber thirdPartyRefNumber;
    @XmlElement(name = "Name")
    protected Name name;
    @XmlElement(name = "Active")
    protected Active active;
    @XmlElement(name = "AssociatedAddress")
    protected List<AssociatedAddress> associatedAddress;
    @XmlElement(name = "Email")
    protected Email email;
    @XmlElement(name = "IsoCurrencyCode")
    protected IsoCurrencyCode isoCurrencyCode;
    @XmlElement(name = "ContactName")
    protected ContactName contactName;
    @XmlElement(name = "Purpose")
    protected Purpose purpose;
    @XmlElement(name = "AccountId")
    protected AccountId accountId;
    @XmlElement(name = "AccountHolderName")
    protected AccountHolderName accountHolderName;
    @XmlElement(name = "AccountType")
    protected AccountType accountType;
    @XmlElement(name = "CountryCode")
    protected CountryCode countryCode;
    @XmlElement(name = "BankAccount")
    protected BankAccount bankAccount;
    @XmlElement(name = "FlexFields")
    protected FlexFields flexFields;

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
     * Gets the value of the erpNumber property.
     * 
     * @return
     *     possible object is
     *     {@link ERPNumber }
     *     
     */
    public ERPNumber getERPNumber() {
        return erpNumber;
    }

    /**
     * Sets the value of the erpNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERPNumber }
     *     
     */
    public void setERPNumber(ERPNumber value) {
        this.erpNumber = value;
    }

    /**
     * Gets the value of the oldERPNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOldERPNumber() {
        return oldERPNumber;
    }

    /**
     * Sets the value of the oldERPNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOldERPNumber(String value) {
        this.oldERPNumber = value;
    }

    /**
     * Gets the value of the sqIntegrationNumber property.
     * 
     * @return
     *     possible object is
     *     {@link SQIntegrationNumber }
     *     
     */
    public SQIntegrationNumber getSQIntegrationNumber() {
        return sqIntegrationNumber;
    }

    /**
     * Sets the value of the sqIntegrationNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link SQIntegrationNumber }
     *     
     */
    public void setSQIntegrationNumber(SQIntegrationNumber value) {
        this.sqIntegrationNumber = value;
    }

    /**
     * Gets the value of the thirdPartyRefNumber property.
     * 
     * @return
     *     possible object is
     *     {@link ThirdPartyRefNumber }
     *     
     */
    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    /**
     * Sets the value of the thirdPartyRefNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link ThirdPartyRefNumber }
     *     
     */
    public void setThirdPartyRefNumber(ThirdPartyRefNumber value) {
        this.thirdPartyRefNumber = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link Name }
     *     
     */
    public Name getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link Name }
     *     
     */
    public void setName(Name value) {
        this.name = value;
    }

    /**
     * Gets the value of the active property.
     * 
     * @return
     *     possible object is
     *     {@link Active }
     *     
     */
    public Active getActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     * 
     * @param value
     *     allowed object is
     *     {@link Active }
     *     
     */
    public void setActive(Active value) {
        this.active = value;
    }

    /**
     * Gets the value of the associatedAddress property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the associatedAddress property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAssociatedAddress().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AssociatedAddress }
     * 
     * 
     */
    public List<AssociatedAddress> getAssociatedAddress() {
        if (associatedAddress == null) {
            associatedAddress = new ArrayList<AssociatedAddress>();
        }
        return this.associatedAddress;
    }

    /**
     * Gets the value of the email property.
     * 
     * @return
     *     possible object is
     *     {@link Email }
     *     
     */
    public Email getEmail() {
        return email;
    }

    /**
     * Sets the value of the email property.
     * 
     * @param value
     *     allowed object is
     *     {@link Email }
     *     
     */
    public void setEmail(Email value) {
        this.email = value;
    }

    /**
     * Gets the value of the isoCurrencyCode property.
     * 
     * @return
     *     possible object is
     *     {@link IsoCurrencyCode }
     *     
     */
    public IsoCurrencyCode getIsoCurrencyCode() {
        return isoCurrencyCode;
    }

    /**
     * Sets the value of the isoCurrencyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link IsoCurrencyCode }
     *     
     */
    public void setIsoCurrencyCode(IsoCurrencyCode value) {
        this.isoCurrencyCode = value;
    }

    /**
     * Gets the value of the contactName property.
     * 
     * @return
     *     possible object is
     *     {@link ContactName }
     *     
     */
    public ContactName getContactName() {
        return contactName;
    }

    /**
     * Sets the value of the contactName property.
     * 
     * @param value
     *     allowed object is
     *     {@link ContactName }
     *     
     */
    public void setContactName(ContactName value) {
        this.contactName = value;
    }

    /**
     * Gets the value of the purpose property.
     * 
     * @return
     *     possible object is
     *     {@link Purpose }
     *     
     */
    public Purpose getPurpose() {
        return purpose;
    }

    /**
     * Sets the value of the purpose property.
     * 
     * @param value
     *     allowed object is
     *     {@link Purpose }
     *     
     */
    public void setPurpose(Purpose value) {
        this.purpose = value;
    }

    /**
     * Gets the value of the accountId property.
     * 
     * @return
     *     possible object is
     *     {@link AccountId }
     *     
     */
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Sets the value of the accountId property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccountId }
     *     
     */
    public void setAccountId(AccountId value) {
        this.accountId = value;
    }

    /**
     * Gets the value of the accountHolderName property.
     * 
     * @return
     *     possible object is
     *     {@link AccountHolderName }
     *     
     */
    public AccountHolderName getAccountHolderName() {
        return accountHolderName;
    }

    /**
     * Sets the value of the accountHolderName property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccountHolderName }
     *     
     */
    public void setAccountHolderName(AccountHolderName value) {
        this.accountHolderName = value;
    }

    /**
     * Gets the value of the accountType property.
     * 
     * @return
     *     possible object is
     *     {@link AccountType }
     *     
     */
    public AccountType getAccountType() {
        return accountType;
    }

    /**
     * Sets the value of the accountType property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccountType }
     *     
     */
    public void setAccountType(AccountType value) {
        this.accountType = value;
    }

    /**
     * Gets the value of the countryCode property.
     * 
     * @return
     *     possible object is
     *     {@link CountryCode }
     *     
     */
    public CountryCode getCountryCode() {
        return countryCode;
    }

    /**
     * Sets the value of the countryCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link CountryCode }
     *     
     */
    public void setCountryCode(CountryCode value) {
        this.countryCode = value;
    }

    /**
     * Gets the value of the bankAccount property.
     * 
     * @return
     *     possible object is
     *     {@link BankAccount }
     *     
     */
    public BankAccount getBankAccount() {
        return bankAccount;
    }

    /**
     * Sets the value of the bankAccount property.
     * 
     * @param value
     *     allowed object is
     *     {@link BankAccount }
     *     
     */
    public void setBankAccount(BankAccount value) {
        this.bankAccount = value;
    }

    /**
     * Gets the value of the flexFields property.
     * 
     * @return
     *     possible object is
     *     {@link FlexFields }
     *     
     */
    public FlexFields getFlexFields() {
        return flexFields;
    }

    /**
     * Sets the value of the flexFields property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlexFields }
     *     
     */
    public void setFlexFields(FlexFields value) {
        this.flexFields = value;
    }

}
