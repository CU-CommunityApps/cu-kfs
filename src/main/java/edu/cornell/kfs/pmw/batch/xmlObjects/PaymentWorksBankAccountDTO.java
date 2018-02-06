package edu.cornell.kfs.pmw.batch.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class PaymentWorksBankAccountDTO {

    @XmlElement(name = "id")
    private String paymentWorksBankAccountId;
    
    @XmlElement(name = "bank_name")
    private String bankName;
    
    @XmlElement(name = "bank_acct_num")
    private String bankAccountNumber;
    
    @XmlElement(name = "validation_file")
    private String validationFile;
    
    @XmlElement(name = "ach_email")
    private String achEmail;
    
    @XmlElement(name = "routing_num")
    private String routingNumber;
    
    @XmlElement(name = "acct")
    private String account;
    
    @XmlElement(name = "bank_acct_alias")
    private String bankAccountAlias;
    
    @XmlElement(name = "bank_acct_type")
    private String bankAccountType;
    
    @XmlElement(name = "authorized")
    private String authorized;
    
    @XmlElement(name = "acct_company")
    private String accountCompany;
    
    @XmlElement(name = "swift_code")
    private String swiftCode;
    
    @XmlElement(name = "name_on_acct")
    private String nameOnAccount;
    
    @XmlElement(name = "bank_address")
    private PaymentWorksAddressBaseDTO bankAddress;
    

    public String getPaymentWorksBankAccountId() {
        return paymentWorksBankAccountId;
    }

    public void setPaymentWorksBankAccountId(String paymentWorksBankAccountId) {
        this.paymentWorksBankAccountId = paymentWorksBankAccountId;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getValidationFile() {
        return validationFile;
    }

    public void setValidationFile(String validationFile) {
        this.validationFile = validationFile;
    }

    public String getAchEmail() {
        return achEmail;
    }

    public void setAchEmail(String achEmail) {
        this.achEmail = achEmail;
    }

    public String getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getBankAccountAlias() {
        return bankAccountAlias;
    }

    public void setBankAccountAlias(String bankAccountAlias) {
        this.bankAccountAlias = bankAccountAlias;
    }

    public String getBankAccountType() {
        return bankAccountType;
    }

    public void setBankAccountType(String bankAccountType) {
        this.bankAccountType = bankAccountType;
    }

    public String getAuthorized() {
        return authorized;
    }

    public void setAuthorized(String authorized) {
        this.authorized = authorized;
    }

    public String getAccountCompany() {
        return accountCompany;
    }

    public void setAccountCompany(String accountCompany) {
        this.accountCompany = accountCompany;
    }

    public String getSwiftCode() {
        return swiftCode;
    }

    public void setSwiftCode(String swiftCode) {
        this.swiftCode = swiftCode;
    }

    public String getNameOnAccount() {
        return nameOnAccount;
    }

    public void setNameOnAccount(String nameOnAccount) {
        this.nameOnAccount = nameOnAccount;
    }

    public PaymentWorksAddressBaseDTO getBankAddress() {
        return bankAddress;
    }

    public void setBankAddress(PaymentWorksAddressBaseDTO bankAddress) {
        this.bankAddress = bankAddress;
    }

}
