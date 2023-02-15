package edu.cornell.kfs.module.purap.jaggaer.xml;

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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "erpNumber", "oldERPNumber", "sqIntegrationNumber", "thirdPartyRefNumber", "name",
        "active", "associatedAddress", "email", "isoCurrencyCode", "contactName", "purpose", "accountId",
        "accountHolderName", "accountType", "countryCode", "bankAccount", "flexFields" })
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

    public ERPNumber getERPNumber() {
        return erpNumber;
    }

    public void setERPNumber(ERPNumber value) {
        this.erpNumber = value;
    }

    public String getOldERPNumber() {
        return oldERPNumber;
    }

    public void setOldERPNumber(String value) {
        this.oldERPNumber = value;
    }

    public SQIntegrationNumber getSQIntegrationNumber() {
        return sqIntegrationNumber;
    }

    public void setSQIntegrationNumber(SQIntegrationNumber value) {
        this.sqIntegrationNumber = value;
    }

    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    public void setThirdPartyRefNumber(ThirdPartyRefNumber value) {
        this.thirdPartyRefNumber = value;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name value) {
        this.name = value;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active value) {
        this.active = value;
    }

    public List<AssociatedAddress> getAssociatedAddress() {
        if (associatedAddress == null) {
            associatedAddress = new ArrayList<AssociatedAddress>();
        }
        return this.associatedAddress;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email value) {
        this.email = value;
    }

    public IsoCurrencyCode getIsoCurrencyCode() {
        return isoCurrencyCode;
    }

    public void setIsoCurrencyCode(IsoCurrencyCode value) {
        this.isoCurrencyCode = value;
    }

    public ContactName getContactName() {
        return contactName;
    }

    public void setContactName(ContactName value) {
        this.contactName = value;
    }

    public Purpose getPurpose() {
        return purpose;
    }

    public void setPurpose(Purpose value) {
        this.purpose = value;
    }

    public AccountId getAccountId() {
        return accountId;
    }

    public void setAccountId(AccountId value) {
        this.accountId = value;
    }

    public AccountHolderName getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(AccountHolderName value) {
        this.accountHolderName = value;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType value) {
        this.accountType = value;
    }

    public CountryCode getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(CountryCode value) {
        this.countryCode = value;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount value) {
        this.bankAccount = value;
    }

    public FlexFields getFlexFields() {
        return flexFields;
    }

    public void setFlexFields(FlexFields value) {
        this.flexFields = value;
    }

}
