package edu.cornell.kfs.pmw.batch.businessobject;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksDataTransformation;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.PurgeableBusinessObjectInterface;

public class PaymentWorksVendor extends PersistableBusinessObjectBase implements Serializable, PurgeableBusinessObjectInterface {
    private static final long serialVersionUID = -6784832598701451681L;
    
    private Integer id;
    private String pmwVendorRequestId;
    private String pmwRequestStatus;
    private String pmwTransactionType;
    
    private String kfsVendorProcessingStatus;
    private String kfsVendorDocumentNumber;
    private Integer kfsVendorHeaderGeneratedIdentifier;
    private Integer kfsVendorDetailAssignedIdentifier;
    
    private String kfsAchProcessingStatus;
    private String kfsAchDocumentNumber;
    private Timestamp processTimestamp;

    private String supplierUploadStatus;

    private String requestingCompanyId;
    private String requestingCompanyTin;
    private String requestingCompanyTinType;
    private String requestingCompanyTaxCountry;
    private String requestingCompanyLegalName;
    private String requestingCompanyLegalFirstName;
    private String requestingCompanyLegalLastName;
    private String requestingCompanyName;
    // instance field added as OpenCSV is expecting it to be present, even if the
    // library relies upon the getter/setter methods to access its value
    private String requestingCompanyLegalNameForProcessing;
    private String requestingCompanyDesc;
    private String requestingCompanyTelephone;
    private String requestingCompanyDuns;
    private String requestingCompanyTaxClassificationName;
    private Integer requestingCompanyTaxClassificationCode;
    private String requestingCompanyUrl;
    private String requestingCompanyW8W9;
    private String requestingCompanyCorporateEmail;

    private String remittanceAddressStreet1;
    private String remittanceAddressStreet2;
    private String remittanceAddressCity;
    private String remittanceAddressState;
    private String remittanceAddressCountry;
    private String remittanceAddressZipCode;
    private boolean remittanceAddressValidated;

    private String corpAddressStreet1;
    private String corpAddressStreet2;
    private String corpAddressCity;
    private String corpAddressState;
    private String corpAddressCountry;
    private String corpAddressZipCode;
    private boolean corpAddressValidated;

    private String bankAcctBankName;
    private String bankAcctRoutingNumber;
    private String bankAcctBankAccountNumber;
    private String bankAcctBankValidationFile;
    private String bankAcctAchEmail;
    private String bankAcctType;
    private String bankAcctAuthorized;
    private String bankAcctSwiftCode;
    private String bankAcctNameOnAccount;

    private String bankAddressStreet1;
    private String bankAddressStreet2;
    private String bankAddressCity;
    private String bankAddressState;
    private String bankAddressCountry;
    private String bankAddressZipCode;
    private boolean bankAddressValidated;

    private boolean customFieldConversionErrors = false;

    // custom fields
    private String initiatorNetId;
    private String vendorType;

    private String vendorInformationContactName;
    private String vendorInformationPhoneNumber;
    private String vendorInformationPhoneExtension;
    private String vendorInformationEmail;

    private boolean conflictOfInterest;
    private String conflictOfInterestRelationshipToEmployee;
    private String conflictOfInterestEmployeeName;
    private String conflictOfInterestEmployeePhoneNumber;

    private String insuranceContactName;
    private String insuranceContactPhoneNumber;
    private String insuranceContactPhoneExtension;
    private String insuranceContactEmail;
    
    private String poCountryLegacy;
    private String poUsState;
    private String poAustralianProvince;
    private String poCanadianProvince;
    private String poCountryName;
    private String poStateProvince;
    private String poAddress1;
    private String poAddress2;
    private String poCity;
    private String poPostalCode;
    private String poAttention;

    private String salesContactName;
    private String salesContactPhoneNumber;
    private String salesContactPhoneExtension;
    private String salesContactEmail;
    
    private String accountsReceivableContactName;
    private String accountsReceivableContactPhone;
    private String accountsReceivableContactPhoneExtension;
    private String accountsReceivableContactEmail;
    
    private String poTransmissionMethod;
    private String poFaxNumber;
    private String poEmailAddress;
    
    /*
     * new fields
     */
    private String supplierCategory;
    private String federalDivsersityCertificates;
    private String paymentMethod;
    private boolean discountedPaymentTerms;
    private String w8SignedDate;
    private String chapter3StatusCode;
    private String chapter4StatusCode;
    private String giinCode;
    private String dateOfBirth;
    private String newYorkCertfiedBusiness;
    private String newYorkDiversityCertificates;
    private String stateDiversityClassifications;
    private String federalDiversityClassifications;
    private String poCountryUsCanadaAustraliaOther;
    private String poCountry;
    private boolean diverseBusiness;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRequestingCompanyId() {
        return requestingCompanyId;
    }

    public void setRequestingCompanyId(String requestingCompanyId) {
        this.requestingCompanyId = requestingCompanyId;
    }

    public String getRequestingCompanyTin() {
        return requestingCompanyTin;
    }

    public void setRequestingCompanyTin(String requestingCompanyTin) {
        this.requestingCompanyTin = requestingCompanyTin;
    }

    public String getRequestingCompanyTinType() {
        return requestingCompanyTinType;
    }

    public void setRequestingCompanyTinType(String requestingCompanyTinType) {
        this.requestingCompanyTinType = requestingCompanyTinType;
    }

    public String getRequestingCompanyTaxCountry() {
        return requestingCompanyTaxCountry;
    }

    public void setRequestingCompanyTaxCountry(String requestingCompanyTaxCountry) {
        this.requestingCompanyTaxCountry = requestingCompanyTaxCountry;
    }

    public String getRequestingCompanyLegalName() {
        return requestingCompanyLegalName;
    }

    public void setRequestingCompanyLegalName(String requestingCompanyLegalName) {
        this.requestingCompanyLegalName = requestingCompanyLegalName;
    }

    public String getRequestingCompanyLegalFirstName() {
        return requestingCompanyLegalFirstName;
    }

    public void setRequestingCompanyLegalFirstName(String requestingCompanyLegalFirstName) {
        this.requestingCompanyLegalFirstName = requestingCompanyLegalFirstName;
    }

    public String getRequestingCompanyLegalLastName() {
        return requestingCompanyLegalLastName;
    }

    public void setRequestingCompanyLegalLastName(String requestingCompanyLegalLastName) {
        this.requestingCompanyLegalLastName = requestingCompanyLegalLastName;
    }

    public String getRequestingCompanyLegalNameForProcessing() {
        return PaymentWorksDataTransformation.formatVendorName(
                requestingCompanyLegalName, requestingCompanyLegalFirstName, requestingCompanyLegalLastName);
    }

    public String getRequestingCompanyName() {
        return requestingCompanyName;
    }

    public void setRequestingCompanyName(String requestingCompanyName) {
        this.requestingCompanyName = requestingCompanyName;
    }

    public String getRequestingCompanyDesc() {
        return requestingCompanyDesc;
    }

    public void setRequestingCompanyDesc(String requestingCompanyDesc) {
        this.requestingCompanyDesc = requestingCompanyDesc;
    }

    public String getRequestingCompanyTelephone() {
        return requestingCompanyTelephone;
    }

    public void setRequestingCompanyTelephone(String requestingCompanyTelephone) {
        this.requestingCompanyTelephone = requestingCompanyTelephone;
    }

    public String getRequestingCompanyDuns() {
        return requestingCompanyDuns;
    }

    public void setRequestingCompanyDuns(String requestingCompanyDuns) {
        this.requestingCompanyDuns = requestingCompanyDuns;
    }

    public String getRequestingCompanyTaxClassificationName() {
        return requestingCompanyTaxClassificationName;
    }

    public void setRequestingCompanyTaxClassificationName(String requestingCompanyTaxClassificationName) {
        this.requestingCompanyTaxClassificationName = requestingCompanyTaxClassificationName;
    }

    public Integer getRequestingCompanyTaxClassificationCode() {
        return requestingCompanyTaxClassificationCode;
    }

    public void setRequestingCompanyTaxClassificationCode(Integer requestingCompanyTaxClassificationCode) {
        this.requestingCompanyTaxClassificationCode = requestingCompanyTaxClassificationCode;
    }

    public String getRequestingCompanyUrl() {
        return requestingCompanyUrl;
    }

    public void setRequestingCompanyUrl(String requestingCompanyUrl) {
        this.requestingCompanyUrl = requestingCompanyUrl;
    }

    public String getRequestingCompanyW8W9() {
        return requestingCompanyW8W9;
    }

    public void setRequestingCompanyW8W9(String requestingCompanyW8W9) {
        this.requestingCompanyW8W9 = requestingCompanyW8W9;
    }

    public String getRequestingCompanyCorporateEmail() {
        return requestingCompanyCorporateEmail;
    }

    public void setRequestingCompanyCorporateEmail(String requestingCompanyCorporateEmail) {
        this.requestingCompanyCorporateEmail = requestingCompanyCorporateEmail;
    }

    public String getRemittanceAddressStreet1() {
        return remittanceAddressStreet1;
    }

    public void setRemittanceAddressStreet1(String remittanceAddressStreet1) {
        this.remittanceAddressStreet1 = remittanceAddressStreet1;
    }

    public String getRemittanceAddressStreet2() {
        return remittanceAddressStreet2;
    }

    public void setRemittanceAddressStreet2(String remittanceAddressStreet2) {
        this.remittanceAddressStreet2 = remittanceAddressStreet2;
    }

    public String getRemittanceAddressCity() {
        return remittanceAddressCity;
    }

    public void setRemittanceAddressCity(String remittanceAddressCity) {
        this.remittanceAddressCity = remittanceAddressCity;
    }

    public String getRemittanceAddressState() {
        return remittanceAddressState;
    }

    public void setRemittanceAddressState(String remittanceAddressState) {
        this.remittanceAddressState = remittanceAddressState;
    }

    public String getRemittanceAddressCountry() {
        return remittanceAddressCountry;
    }

    public void setRemittanceAddressCountry(String remittanceAddressCountry) {
        this.remittanceAddressCountry = remittanceAddressCountry;
    }

    public String getRemittanceAddressZipCode() {
        return remittanceAddressZipCode;
    }

    public void setRemittanceAddressZipCode(String remittanceAddressZipCode) {
        this.remittanceAddressZipCode = remittanceAddressZipCode;
    }

    public boolean isRemittanceAddressValidated() {
        return remittanceAddressValidated;
    }

    public void setRemittanceAddressValidated(boolean remittanceAddressValidated) {
        this.remittanceAddressValidated = remittanceAddressValidated;
    }

    public String getCorpAddressStreet1() {
        return corpAddressStreet1;
    }

    public void setCorpAddressStreet1(String corpAddressStreet1) {
        this.corpAddressStreet1 = corpAddressStreet1;
    }

    public String getCorpAddressStreet2() {
        return corpAddressStreet2;
    }

    public void setCorpAddressStreet2(String corpAddressStreet2) {
        this.corpAddressStreet2 = corpAddressStreet2;
    }

    public String getCorpAddressCity() {
        return corpAddressCity;
    }

    public void setCorpAddressCity(String corpAddressCity) {
        this.corpAddressCity = corpAddressCity;
    }

    public String getCorpAddressState() {
        return corpAddressState;
    }

    public void setCorpAddressState(String corpAddressState) {
        this.corpAddressState = corpAddressState;
    }

    public String getCorpAddressCountry() {
        return corpAddressCountry;
    }

    public void setCorpAddressCountry(String corpAddressCountry) {
        this.corpAddressCountry = corpAddressCountry;
    }

    public String getCorpAddressZipCode() {
        return corpAddressZipCode;
    }

    public void setCorpAddressZipCode(String corpAddressZipCode) {
        this.corpAddressZipCode = corpAddressZipCode;
    }

    public boolean isCorpAddressValidated() {
        return corpAddressValidated;
    }

    public void setCorpAddressValidated(boolean corpAddressValidated) {
        this.corpAddressValidated = corpAddressValidated;
    }

    public String getBankAcctBankName() {
        return bankAcctBankName;
    }

    public void setBankAcctBankName(String bankAcctBankName) {
        this.bankAcctBankName = bankAcctBankName;
    }

    public String getBankAcctRoutingNumber() {
        return bankAcctRoutingNumber;
    }

    public void setBankAcctRoutingNumber(String bankAcctRoutingNumber) {
        this.bankAcctRoutingNumber = bankAcctRoutingNumber;
    }

    public String getBankAcctBankAccountNumber() {
        return bankAcctBankAccountNumber;
    }

    public void setBankAcctBankAccountNumber(String bankAcctBankAccountNumber) {
        this.bankAcctBankAccountNumber = bankAcctBankAccountNumber;
    }

    public String getBankAcctBankValidationFile() {
        return bankAcctBankValidationFile;
    }

    public void setBankAcctBankValidationFile(String bankAcctBankValidationFile) {
        this.bankAcctBankValidationFile = bankAcctBankValidationFile;
    }

    public String getBankAcctAchEmail() {
        return bankAcctAchEmail;
    }

    public void setBankAcctAchEmail(String bankAcctAchEmail) {
        this.bankAcctAchEmail = bankAcctAchEmail;
    }

    public String getBankAcctType() {
        return bankAcctType;
    }

    public void setBankAcctType(String bankAcctType) {
        this.bankAcctType = bankAcctType;
    }

    public String getBankAcctAuthorized() {
        return bankAcctAuthorized;
    }

    public void setBankAcctAuthorized(String bankAcctAuthorized) {
        this.bankAcctAuthorized = bankAcctAuthorized;
    }

    public String getBankAcctSwiftCode() {
        return bankAcctSwiftCode;
    }

    public void setBankAcctSwiftCode(String bankAcctSwiftCode) {
        this.bankAcctSwiftCode = bankAcctSwiftCode;
    }

    public String getBankAcctNameOnAccount() {
        return bankAcctNameOnAccount;
    }

    public void setBankAcctNameOnAccount(String bankAcctNameOnAccount) {
        this.bankAcctNameOnAccount = bankAcctNameOnAccount;
    }

    public String getBankAddressStreet1() {
        return bankAddressStreet1;
    }

    public void setBankAddressStreet1(String bankAddressStreet1) {
        this.bankAddressStreet1 = bankAddressStreet1;
    }

    public String getBankAddressStreet2() {
        return bankAddressStreet2;
    }

    public void setBankAddressStreet2(String bankAddressStreet2) {
        this.bankAddressStreet2 = bankAddressStreet2;
    }

    public String getBankAddressCity() {
        return bankAddressCity;
    }

    public void setBankAddressCity(String bankAddressCity) {
        this.bankAddressCity = bankAddressCity;
    }

    public String getBankAddressState() {
        return bankAddressState;
    }

    public void setBankAddressState(String bankAddressState) {
        this.bankAddressState = bankAddressState;
    }

    public String getBankAddressCountry() {
        return bankAddressCountry;
    }

    public void setBankAddressCountry(String bankAddressCountry) {
        this.bankAddressCountry = bankAddressCountry;
    }

    public String getBankAddressZipCode() {
        return bankAddressZipCode;
    }

    public void setBankAddressZipCode(String bankAddressZipCode) {
        this.bankAddressZipCode = bankAddressZipCode;
    }

    public boolean isBankAddressValidated() {
        return bankAddressValidated;
    }

    public void setBankAddressValidated(boolean bankAddressValidated) {
        this.bankAddressValidated = bankAddressValidated;
    }

    public String getPmwVendorRequestId() {
        return pmwVendorRequestId;
    }

    public void setPmwVendorRequestId(String pmwVendorRequestId) {
        this.pmwVendorRequestId = pmwVendorRequestId;
    }

    public String getPmwRequestStatus() {
        return pmwRequestStatus;
    }

    public void setPmwRequestStatus(String pmwRequestStatus) {
        this.pmwRequestStatus = pmwRequestStatus;
    }

    public String getPmwTransactionType() {
        return pmwTransactionType;
    }

    public void setPmwTransactionType(String pmwTransactionType) {
        this.pmwTransactionType = pmwTransactionType;
    }

    public String getKfsVendorProcessingStatus() {
        return kfsVendorProcessingStatus;
    }

    public void setKfsVendorProcessingStatus(String kfsVendorProcessingStatus) {
        this.kfsVendorProcessingStatus = kfsVendorProcessingStatus;
    }

    public Timestamp getProcessTimestamp() {
        return processTimestamp;
    }

    public void setProcessTimestamp(Timestamp processTimestamp) {
        this.processTimestamp = processTimestamp;
    }

    public String getSupplierUploadStatus() {
        return supplierUploadStatus;
    }

    public void setSupplierUploadStatus(String supplierUploadStatus) {
        this.supplierUploadStatus = supplierUploadStatus;
    }

    public String getKfsVendorDocumentNumber() {
        return kfsVendorDocumentNumber;
    }

    public void setKfsVendorDocumentNumber(String kfsVendorDocumentNumber) {
        this.kfsVendorDocumentNumber = kfsVendorDocumentNumber;
    }

    public String getKfsAchProcessingStatus() {
        return kfsAchProcessingStatus;
    }

    public void setKfsAchProcessingStatus(String kfsAchProcessingStatus) {
        this.kfsAchProcessingStatus = kfsAchProcessingStatus;
    }

    public String getKfsAchDocumentNumber() {
        return kfsAchDocumentNumber;
    }

    public void setKfsAchDocumentNumber(String kfsAchDocumentNumber) {
        this.kfsAchDocumentNumber = kfsAchDocumentNumber;
    }

    public Integer getKfsVendorHeaderGeneratedIdentifier() {
        return kfsVendorHeaderGeneratedIdentifier;
    }

    public void setKfsVendorHeaderGeneratedIdentifier(Integer kfsVendorHeaderGeneratedIdentifier) {
        this.kfsVendorHeaderGeneratedIdentifier = kfsVendorHeaderGeneratedIdentifier;
    }

    public Integer getKfsVendorDetailAssignedIdentifier() {
        return kfsVendorDetailAssignedIdentifier;
    }

    public void setKfsVendorDetailAssignedIdentifier(Integer kfsVendorDetailAssignedIdentifier) {
        this.kfsVendorDetailAssignedIdentifier = kfsVendorDetailAssignedIdentifier;
    }

    public String getKfsVendorNumber() {
        if (kfsVendorHeaderGeneratedIdentifier != null && kfsVendorDetailAssignedIdentifier != null) {
            return StringUtils.join(kfsVendorHeaderGeneratedIdentifier,
                    KFSConstants.DASH, kfsVendorDetailAssignedIdentifier);
        } else {
            return null;
        }
    }

    public void setKfsVendorNumber(final String kfsVendorNumber) {
        // Ignore
    }

    public boolean isCustomFieldConversionErrors() {
        return customFieldConversionErrors;
    }
    

    public void setCustomFieldConversionErrors(boolean customFieldConversionErrors) {
        this.customFieldConversionErrors = customFieldConversionErrors;
    }

    public String getInitiatorNetId() {
        return initiatorNetId;
    }

    public void setInitiatorNetId(String initiatorNetId) {
        this.initiatorNetId = initiatorNetId;
    }

    public String getVendorType() {
        return vendorType;
    }

    public void setVendorType(String vendorType) {
        this.vendorType = vendorType;
    }

    public String getVendorInformationContactName() {
        return vendorInformationContactName;
    }

    public void setVendorInformationContactName(String vendorInformationContactName) {
        this.vendorInformationContactName = vendorInformationContactName;
    }

    public String getVendorInformationPhoneNumber() {
        return vendorInformationPhoneNumber;
    }

    public void setVendorInformationPhoneNumber(String vendorInformationPhoneNumber) {
        this.vendorInformationPhoneNumber = vendorInformationPhoneNumber;
    }

    public String getVendorInformationPhoneExtension() {
        return vendorInformationPhoneExtension;
    }

    public void setVendorInformationPhoneExtension(String vendorInformationPhoneExtension) {
        this.vendorInformationPhoneExtension = vendorInformationPhoneExtension;
    }

    public String getVendorInformationEmail() {
        return vendorInformationEmail;
    }

    public void setVendorInformationEmail(String vendorInformationEmail) {
        this.vendorInformationEmail = vendorInformationEmail;
    }

    public boolean isDiverseBusiness() {
        return diverseBusiness;
    }

    public void setDiverseBusiness(boolean diverseBusiness) {
        this.diverseBusiness = diverseBusiness;
    }
    
    public boolean isConflictOfInterest() {
        return conflictOfInterest;
    }

    public void setConflictOfInterest(boolean conflictOfInterest) {
        this.conflictOfInterest = conflictOfInterest;
    }

    public String getConflictOfInterestRelationshipToEmployee() {
        return conflictOfInterestRelationshipToEmployee;
    }

    public void setConflictOfInterestRelationshipToEmployee(String conflictOfInterestRelationshipToEmployee) {
        this.conflictOfInterestRelationshipToEmployee = conflictOfInterestRelationshipToEmployee;
    }

    public String getConflictOfInterestEmployeeName() {
        return conflictOfInterestEmployeeName;
    }

    public void setConflictOfInterestEmployeeName(String conflictOfInterestEmployeeName) {
        this.conflictOfInterestEmployeeName = conflictOfInterestEmployeeName;
    }

    public String getConflictOfInterestEmployeePhoneNumber() {
        return conflictOfInterestEmployeePhoneNumber;
    }

    public void setConflictOfInterestEmployeePhoneNumber(String conflictOfInterestEmployeePhoneNumber) {
        this.conflictOfInterestEmployeePhoneNumber = conflictOfInterestEmployeePhoneNumber;
    }

    public String getInsuranceContactName() {
        return insuranceContactName;
    }

    public void setInsuranceContactName(String insuranceContactName) {
        this.insuranceContactName = insuranceContactName;
    }

    public String getInsuranceContactPhoneNumber() {
        return insuranceContactPhoneNumber;
    }

    public void setInsuranceContactPhoneNumber(String insuranceContactPhoneNumber) {
        this.insuranceContactPhoneNumber = insuranceContactPhoneNumber;
    }

    public String getInsuranceContactPhoneExtension() {
        return insuranceContactPhoneExtension;
    }

    public void setInsuranceContactPhoneExtension(String insuranceContactPhoneExtension) {
        this.insuranceContactPhoneExtension = insuranceContactPhoneExtension;
    }

    public String getInsuranceContactEmail() {
        return insuranceContactEmail;
    }

    public void setInsuranceContactEmail(String insuranceContactEmail) {
        this.insuranceContactEmail = insuranceContactEmail;
    }

    public String getPoCountryLegacy() {
        return poCountryLegacy;
    }

    public void setPoCountryLegacy(String poCountryLegacy) {
        this.poCountryLegacy = poCountryLegacy;
    }

    public String getPoUsState() {
        return poUsState;
    }

    public void setPoUsState(String poUsState) {
        this.poUsState = poUsState;
    }

    public String getPoAustralianProvince() {
        return poAustralianProvince;
    }

    public void setPoAustralianProvince(String poAustralianProvince) {
        this.poAustralianProvince = poAustralianProvince;
    }

    public String getPoCanadianProvince() {
        return poCanadianProvince;
    }

    public void setPoCanadianProvince(String poCanadianProvince) {
        this.poCanadianProvince = poCanadianProvince;
    }

    public String getPoCountryName() {
        return poCountryName;
    }

    public void setPoCountryName(String poCountryName) {
        this.poCountryName = poCountryName;
    }

    public String getPoStateProvince() {
        return poStateProvince;
    }

    public void setPoStateProvince(String poStateProvince) {
        this.poStateProvince = poStateProvince;
    }

    public String getPoAddress1() {
        return poAddress1;
    }

    public void setPoAddress1(String poAddress1) {
        this.poAddress1 = poAddress1;
    }

    public String getPoAddress2() {
        return poAddress2;
    }

    public void setPoAddress2(String poAddress2) {
        this.poAddress2 = poAddress2;
    }

    public String getPoCity() {
        return poCity;
    }

    public void setPoCity(String poCity) {
        this.poCity = poCity;
    }

    public String getPoPostalCode() {
        return poPostalCode;
    }

    public void setPoPostalCode(String poPostalCode) {
        this.poPostalCode = poPostalCode;
    }

    public String getPoAttention() {
        return poAttention;
    }

    public void setPoAttention(String poAttention) {
        this.poAttention = poAttention;
    }

    public String getSalesContactName() {
        return salesContactName;
    }

    public void setSalesContactName(String salesContactName) {
        this.salesContactName = salesContactName;
    }

    public String getSalesContactPhoneNumber() {
        return salesContactPhoneNumber;
    }

    public void setSalesContactPhoneNumber(String salesContactPhoneNumber) {
        this.salesContactPhoneNumber = salesContactPhoneNumber;
    }

    public String getSalesContactPhoneExtension() {
        return salesContactPhoneExtension;
    }

    public void setSalesContactPhoneExtension(String salesContactPhoneExtension) {
        this.salesContactPhoneExtension = salesContactPhoneExtension;
    }

    public String getSalesContactEmail() {
        return salesContactEmail;
    }

    public void setSalesContactEmail(String salesContactEmail) {
        this.salesContactEmail = salesContactEmail;
    }

    public String getAccountsReceivableContactName() {
        return accountsReceivableContactName;
    }

    public void setAccountsReceivableContactName(String accountsReceivableContactName) {
        this.accountsReceivableContactName = accountsReceivableContactName;
    }

    public String getAccountsReceivableContactPhone() {
        return accountsReceivableContactPhone;
    }

    public void setAccountsReceivableContactPhone(String accountsReceivableContactPhone) {
        this.accountsReceivableContactPhone = accountsReceivableContactPhone;
    }

    public String getAccountsReceivableContactPhoneExtension() {
        return accountsReceivableContactPhoneExtension;
    }

    public void setAccountsReceivableContactPhoneExtension(String accountsReceivableContactPhoneExtension) {
        this.accountsReceivableContactPhoneExtension = accountsReceivableContactPhoneExtension;
    }

    public String getAccountsReceivableContactEmail() {
        return accountsReceivableContactEmail;
    }

    public void setAccountsReceivableContactEmail(String accountsReceivableContactEmail) {
        this.accountsReceivableContactEmail = accountsReceivableContactEmail;
    }

    public String getPoTransmissionMethod() {
        return poTransmissionMethod;
    }

    public void setPoTransmissionMethod(String poTransmissionMethod) {
        this.poTransmissionMethod = poTransmissionMethod;
    }

    public String getPoFaxNumber() {
        return poFaxNumber;
    }

    public void setPoFaxNumber(String poFaxNumber) {
        this.poFaxNumber = poFaxNumber;
    }

    public String getPoEmailAddress() {
        return poEmailAddress;
    }

    public void setPoEmailAddress(String poEmailAddress) {
        this.poEmailAddress = poEmailAddress;
    }
    
    public String getSupplierCategory() {
        return supplierCategory;
    }

    public void setSupplierCategory(String supplierCategory) {
        this.supplierCategory = supplierCategory;
    }

    public String getFederalDivsersityCertificates() {
        return federalDivsersityCertificates;
    }

    public void setFederalDivsersityCertificates(String federalDivsersityCertificates) {
        this.federalDivsersityCertificates = federalDivsersityCertificates;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getW8SignedDate() {
        return w8SignedDate;
    }

    public void setW8SignedDate(String w8SignedDate) {
        this.w8SignedDate = w8SignedDate;
    }

    public String getChapter3StatusCode() {
        return chapter3StatusCode;
    }

    public void setChapter3StatusCode(String chapter3StatusCode) {
        this.chapter3StatusCode = chapter3StatusCode;
    }

    public String getChapter4StatusCode() {
        return chapter4StatusCode;
    }

    public void setChapter4StatusCode(String chapter4StatusCode) {
        this.chapter4StatusCode = chapter4StatusCode;
    }

    public String getGiinCode() {
        return giinCode;
    }

    public void setGiinCode(String giinCode) {
        this.giinCode = giinCode;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getNewYorkCertfiedBusiness() {
        return newYorkCertfiedBusiness;
    }

    public void setNewYorkCertfiedBusiness(String newYorkCertfiedBusiness) {
        this.newYorkCertfiedBusiness = newYorkCertfiedBusiness;
    }

    public boolean isDiscountedPaymentTerms() {
        return discountedPaymentTerms;
    }

    public void setDiscountedPaymentTerms(boolean discountedPaymentTerms) {
        this.discountedPaymentTerms = discountedPaymentTerms;
    }

    public String getNewYorkDiversityCertificates() {
        return newYorkDiversityCertificates;
    }

    public void setNewYorkDiversityCertificates(String newYorkDiversityCertificates) {
        this.newYorkDiversityCertificates = newYorkDiversityCertificates;
    }

    public String getFederalDiversityClassifications() {
        return federalDiversityClassifications;
    }

    public void setFederalDiversityClassifications(String federalDiversityClassifications) {
        this.federalDiversityClassifications = federalDiversityClassifications;
    }

    public String getStateDiversityClassifications() {
        return stateDiversityClassifications;
    }

    public void setStateDiversityClassifications(String stateDiversityClassifications) {
        this.stateDiversityClassifications = stateDiversityClassifications;
    }

    public String getPoCountryUsCanadaAustraliaOther() {
        return poCountryUsCanadaAustraliaOther;
    }

    public void setPoCountryUsCanadaAustraliaOther(String poCountryUsCanadaAustraliaOther) {
        this.poCountryUsCanadaAustraliaOther = poCountryUsCanadaAustraliaOther;
    }

    public String getPoCountry() {
        return poCountry;
    }

    public void setPoCountry(String poCountry) {
        this.poCountry = poCountry;
    }

    @Override
    public String toString() {
            ReflectionToStringBuilder builder = new ReflectionToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
            
            builder.setExcludeFieldNames(PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_TIN, 
                    PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_W8_W9,
                    PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_ROUTING_NUMBER,
                    PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_BANK_ACCOUNT_NUMBER,
                    PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_BANK_VALIDATION_FILE);
            
            builder.append(PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_TIN, 
                    buildRestrictedFieldPrintableValue(requestingCompanyTin));
            builder.append(PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_W8_W9, 
                    buildRestrictedFieldPrintableValue(requestingCompanyW8W9));
            builder.append(PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_ROUTING_NUMBER, 
                    buildRestrictedFieldPrintableValue(bankAcctBankAccountNumber));
            builder.append(PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_BANK_ACCOUNT_NUMBER, 
                    buildRestrictedFieldPrintableValue(bankAcctBankAccountNumber));
            builder.append(PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_BANK_VALIDATION_FILE, 
                    buildRestrictedFieldPrintableValue(bankAcctBankValidationFile));
            
            return builder.build();
    }
    
    private String buildRestrictedFieldPrintableValue(String fieldValue) {
        if (StringUtils.isNotEmpty(fieldValue)) {
            return PaymentWorksConstants.OUTPUT_RESTRICTED_DATA_PRESENT;
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public String buildObjectSpecificPurgeableRecordData() {
        StringBuilder sb = new StringBuilder();
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.PMW_VENDOR_REQUEST_ID, pmwVendorRequestId);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.KFS_VENDOR_PROCESSING_STATUS, kfsVendorProcessingStatus);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.KFS_VENDOR_DOCUMENT_NUMBER, kfsVendorDocumentNumber);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.KFS_ACH_PROCESSING_STATUS, kfsAchProcessingStatus);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.KFS_ACH_DOCUMENT_NUMBER, kfsAchDocumentNumber);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.SUPPLIER_UPLOAD_STATUS, supplierUploadStatus);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_LEGAL_NAME, requestingCompanyLegalName);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_LEGAL_FIRST_NAME, requestingCompanyLegalFirstName);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_LEGAL_LAST_NAME, requestingCompanyLegalLastName);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_NAME, requestingCompanyName);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.VENDOR_TYPE, vendorType);
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.INITIATOR_NETID, initiatorNetId);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_mm_dd_yyyy_hh_mm_ss_am, Locale.US); 
        addToStringBuilder(sb, PaymentWorksConstants.PaymentWorksVendorFieldName.PROCESS_TIMESTAMP, dateFormat.format(processTimestamp));
        
        return sb.toString();
    }
    
    private void addToStringBuilder(StringBuilder sb, String fieldName, String fieldValue) {
        sb.append(fieldName).append(CUKFSConstants.EQUALS_SIGN).append(fieldValue).append(KFSConstants.NEWLINE);
    }

}
