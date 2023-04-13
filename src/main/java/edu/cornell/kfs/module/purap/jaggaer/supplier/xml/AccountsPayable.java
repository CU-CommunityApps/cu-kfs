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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "erpNumber", "oldERPNumber", "sqIntegrationNumber", "thirdPartyRefNumber", "name",
        "active", "associatedAddress", "email", "isoCurrencyCode", "contactName", "purpose", "accountId",
        "accountHolderName", "accountType", "countryCode", "bankAccount", "flexFields" })
@XmlRootElement(name = "AccountsPayable")
public class AccountsPayable {

    @XmlAttribute(name = "type", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    private String type;
    @XmlAttribute(name = "isChanged")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    private String isChanged;
    @XmlElement(name = "ERPNumber")
    private ERPNumber erpNumber;
    @XmlElement(name = "OldERPNumber")
    private String oldERPNumber;
    @XmlElement(name = "SQIntegrationNumber")
    private SQIntegrationNumber sqIntegrationNumber;
    @XmlElement(name = "ThirdPartyRefNumber")
    private ThirdPartyRefNumber thirdPartyRefNumber;
    @XmlElement(name = "Name")
    private Name name;
    @XmlElement(name = "Active")
    private Active active;
    @XmlElement(name = "AssociatedAddress")
    private List<AssociatedAddress> associatedAddress;
    @XmlElement(name = "Email")
    private Email email;
    @XmlElement(name = "IsoCurrencyCode")
    private IsoCurrencyCode isoCurrencyCode;
    @XmlElement(name = "ContactName")
    private JaggaerBasicValue contactName;
    @XmlElement(name = "Purpose")
    private JaggaerBasicValue purpose;
    @XmlElement(name = "AccountId")
    private JaggaerBasicValue accountId;
    @XmlElement(name = "AccountHolderName")
    private JaggaerBasicValue accountHolderName;
    @XmlElement(name = "AccountType")
    private JaggaerBasicValue accountType;
    @XmlElement(name = "CountryCode")
    private JaggaerBasicValue countryCode;
    @XmlElement(name = "BankAccount")
    private BankAccount bankAccount;
    @XmlElement(name = "FlexFields")
    private FlexFields flexFields;

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

    public ERPNumber getErpNumber() {
        return erpNumber;
    }

    public void setErpNumber(ERPNumber erpNumber) {
        this.erpNumber = erpNumber;
    }

    public String getOldERPNumber() {
        return oldERPNumber;
    }

    public void setOldERPNumber(String oldERPNumber) {
        this.oldERPNumber = oldERPNumber;
    }

    public SQIntegrationNumber getSqIntegrationNumber() {
        return sqIntegrationNumber;
    }

    public void setSqIntegrationNumber(SQIntegrationNumber sqIntegrationNumber) {
        this.sqIntegrationNumber = sqIntegrationNumber;
    }

    public ThirdPartyRefNumber getThirdPartyRefNumber() {
        return thirdPartyRefNumber;
    }

    public void setThirdPartyRefNumber(ThirdPartyRefNumber thirdPartyRefNumber) {
        this.thirdPartyRefNumber = thirdPartyRefNumber;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Active getActive() {
        return active;
    }

    public void setActive(Active active) {
        this.active = active;
    }

    public List<AssociatedAddress> getAssociatedAddress() {
        if (associatedAddress == null) {
            associatedAddress = new ArrayList<AssociatedAddress>();
        }
        return associatedAddress;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public IsoCurrencyCode getIsoCurrencyCode() {
        return isoCurrencyCode;
    }

    public void setIsoCurrencyCode(IsoCurrencyCode isoCurrencyCode) {
        this.isoCurrencyCode = isoCurrencyCode;
    }

    public JaggaerBasicValue getContactName() {
        return contactName;
    }

    public void setContactName(JaggaerBasicValue contactName) {
        this.contactName = contactName;
    }

    public JaggaerBasicValue getPurpose() {
        return purpose;
    }

    public void setPurpose(JaggaerBasicValue purpose) {
        this.purpose = purpose;
    }

    public JaggaerBasicValue getAccountId() {
        return accountId;
    }

    public void setAccountId(JaggaerBasicValue accountId) {
        this.accountId = accountId;
    }

    public JaggaerBasicValue getAccountHolderName() {
        return accountHolderName;
    }

    public void setAccountHolderName(JaggaerBasicValue accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public JaggaerBasicValue getAccountType() {
        return accountType;
    }

    public void setAccountType(JaggaerBasicValue accountType) {
        this.accountType = accountType;
    }

    public JaggaerBasicValue getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(JaggaerBasicValue countryCode) {
        this.countryCode = countryCode;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(BankAccount bankAccount) {
        this.bankAccount = bankAccount;
    }

    public FlexFields getFlexFields() {
        return flexFields;
    }

    public void setFlexFields(FlexFields flexFields) {
        this.flexFields = flexFields;
    }

}
