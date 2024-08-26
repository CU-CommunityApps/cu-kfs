package edu.cornell.kfs.fp.businessobject;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;

public class DisbursementVoucherWireTransferExtendedAttribute extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {
    
    protected String documentNumber;
    protected String bankStreetAddress;
    protected String bankProvince;
    protected String bankSWIFTCode;
    protected String bankIBAN;
    protected String sortOrTransitCode;
    protected String correspondentBankName;
    protected String correspondentBankAddress;
    protected String correspondentBankRoutingNumber;
    protected String correspondentBankAccountNumber;
    protected String correspondentBankSwiftCode;
    
    public DisbursementVoucherWireTransferExtendedAttribute() {
        super();
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
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

    public String getBankSWIFTCode() {
        return bankSWIFTCode;
    }

    public void setBankSWIFTCode(String bankSWIFTCode) {
        this.bankSWIFTCode = bankSWIFTCode;
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
