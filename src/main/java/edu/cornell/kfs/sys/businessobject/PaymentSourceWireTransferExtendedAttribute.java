package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;

public class PaymentSourceWireTransferExtendedAttribute extends PersistableBusinessObjectBase implements PersistableBusinessObjectExtension {
    
    private String documentNumber;
    private String payeeAccountTypeCode;
    private String bankStreetAddress;
    private String bankProvince;
    private String bankSwiftCode;
    private String bankIBAN;
    private String sortOrTransitCode;
    private String correspondentBankName;
    private String correspondentBankAddress;
    private String correspondentBankRoutingNumber;
    private String correspondentBankAccountNumber;
    private String correspondentBankSwiftCode;
    
    public PaymentSourceWireTransferExtendedAttribute() {
    }
    
    public String getDocumentNumber() {
        return documentNumber;
    }
    
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }
    
    public String getPayeeAccountTypeCode() {
        return payeeAccountTypeCode;
    }
    
    public void setPayeeAccountTypeCode(String payeeAccountTypeCode) {
        this.payeeAccountTypeCode = payeeAccountTypeCode;
    }
    
    public String getBankStreetAddress() {
        return bankStreetAddress;
    }
    
    public void setBankStreetAddress(String bankStreetAddress) {
        this.bankStreetAddress = bankStreetAddress;
    }
    
    public String getBankProvince() {
        return bankProvince;
    }
    
    public void setBankProvince(String bankProvince) {
        this.bankProvince = bankProvince;
    }
    
    public String getBankSwiftCode() {
        return bankSwiftCode;
    }
    
    public void setBankSwiftCode(String bankSwiftCode) {
        this.bankSwiftCode = bankSwiftCode;
    }
    
    public String getBankIBAN() {
        return bankIBAN;
    }
    
    public void setBankIBAN(String bankIBAN) {
        this.bankIBAN = bankIBAN;
    }
    
    public String getSortOrTransitCode() {
        return sortOrTransitCode;
    }

    public void setSortOrTransitCode(String sortOrTransitCode) {
        this.sortOrTransitCode = sortOrTransitCode;
    }

    public String getCorrespondentBankName() {
        return correspondentBankName;
    }
    
    public void setCorrespondentBankName(String correspondentBankName) {
        this.correspondentBankName = correspondentBankName;
    }
    
    public String getCorrespondentBankAddress() {
        return correspondentBankAddress;
    }
    
    public void setCorrespondentBankAddress(String correspondentBankAddress) {
        this.correspondentBankAddress = correspondentBankAddress;
    }
    
    public String getCorrespondentBankRoutingNumber() {
        return correspondentBankRoutingNumber;
    }
    
    public void setCorrespondentBankRoutingNumber(String correspondentBankRoutingNumber) {
        this.correspondentBankRoutingNumber = correspondentBankRoutingNumber;
    }
    
    public String getCorrespondentBankAccountNumber() {
        return correspondentBankAccountNumber;
    }
    
    public void setCorrespondentBankAccountNumber(String correspondentBankAccountNumber) {
        this.correspondentBankAccountNumber = correspondentBankAccountNumber;
    }
    
    public String getCorrespondentBankSwiftCode() {
        return correspondentBankSwiftCode;
    }
    
    public void setCorrespondentBankSwiftCode(String correspondentBankSwiftCode) {
        this.correspondentBankSwiftCode = correspondentBankSwiftCode;
    }
}
