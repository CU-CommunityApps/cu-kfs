/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.businessobject;

import java.sql.Date;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;

public class PaymentWorksVendor extends PersistableBusinessObjectBase {
	private static final long serialVersionUID = -4431128959859612352L;
	
	private String requestingCompanyId;
	private String requestingCompanyTin;
	private String requestingCompanyTinType;
	private String requestingCompanyTaxCountry;
	private String requestingCompanyLegalName;
	private String requestingCompanyLegalFirstName;
	private String requestingCompanyLegalLastName;
	private String requestingCompanyName;
	private String requestingCompanyNameOldValue;
	private String requestingCompanyDesc;
	private String requestingCompanyTelephone;
	private String requestingCompanyTelephoneOldValue;
	private String requestingCompanyDuns;
	private String requestingCompanyTaxClassificationName;
	private String requestingCompanyTaxClassificationCode;
	private String requestingCompanyUrl;
	private String requestingCompanyW8W9;
	private String requestingCompanyCorporateEmail;

	private String remittanceAddressId;
	private String remittanceAddressName;
	private String remittanceAddressStreet1;
	private String remittanceAddressStreet2;
	private String remittanceAddressCity;
	private String remittanceAddressState;
	private String remittanceAddressCountry;
	private String remittanceAddressZipCode;
	private boolean remittanceAddressValidated;
	private String remittanceAddressCompanyId;

	private String corpAddressId;
	private String corpAddressName;
	private String corpAddressStreet1;
	private String corpAddressStreet2;
	private String corpAddressCity;
	private String corpAddressState;
	private String corpAddressCountry;
	private String corpAddressZipCode;
	private boolean corpAddressValidated;
	private String corpAddressCompanyId;

	private String bankAcctId;
	private String bankAcctBankName;
	private String bankAcctRoutingNumber;
	private String bankAcctBankAccountNumber;
	private String bankAcctBankValidationFile;
	private String bankAcctAchEmail;
	private String bankAcctType;
	private String bankAcctAuthorized;
	private String bankAcctCompanyId;
	private String bankAcctSwiftCode;
	private String bankAcctNameOnAccount;

	private String bankAddressId;
	private String bankAddressName;
	private String bankAddressStreet1;
	private String bankAddressStreet2;
	private String bankAddressCity;
	private String bankAddressState;
	private String bankAddressCountry;
	private String bankAddressZipCode;
	private boolean bankAddressValidated;
	private String bankAddressCompanyId;

	//below not reviewed yet
	private String vendorRequestId; // primary key
	private String requestStatus;
	private String transactionType; // NV,VU,ACH
	private String groupName;
	private String processStatus;
	private String vendorName;
	private Timestamp processTimestamp;
	private String documentNumber;
	private Integer vendorHeaderGeneratedIdentifier;
	private Integer vendorDetailAssignedIdentifier;
	private String documentNumberList;
	private String vendorNumberList;

	private transient boolean customFieldConversionErrors = false;
	
	//custom fields
	private String initiatorNetId;
	private String vendorType;
	
	private String vendorInformationContactName;
	private String vendorInformationPhoneNumber;
	private String vendorInformationPhoneExtension;
	private String vendorInformationEmail;
	
	private boolean diverseBusiness;
	private String diversityClassifications;
	private String minorityStatus;
	private String mbeCertificationExpirationDate;
	private String womanOwned;
	private String wbeCertificationExpirationDate;
	private String disabledVeteran;
	private String veteranCertificationExpirationDate;

	private boolean conflictOfInterest;
	private String conflictOfInterestRelationshipToEmployee;
	private String conflictOfInterestEmployeeName;
	private String conflictOfInterestEmployeePhoneNumber;

	private boolean acceptCreditCards;

	private String servicesProvided;
	private String emailAddress;
	private boolean invoicing;
	private String eInvoiceContactName;
	private String eInvoiceContactPhoneNumber;
	private String eInvoicePhoneExtension;
	private String eInvoiceEmail;
	private String poAttention;
	private String poTransmissionMethod;
	private String poAddress1;
	private String poAddress2;
	private String poCity;
	private String poState;
	private String poPostalCode;
	private String poCountry;
	private String poFaxNumber;
	private String poCountryName;
	private String taxCountry;
	private String insuranceCertificate;
	private String insuranceContactName;
	private String insuranceContactPhoneNumber;
	private String insuranceContactPhoneExtension;
	private String insuranceContactEmail;
	private String salesContactName;
	private String salesContactPhoneNumber;
	private String salesContactPhoneExtension;
	private String salesContactEmail;
	private String accountsReceivableContactName;
	private String accountsReceivableContactPhone;
	private String accountsReceivableContactPhoneExtension;
	private String accountsReceivableContactEmail;
	private String state;
	private String australianProvince;
	private String stateProvince;
	private String canadianProvince;
	private boolean seperateLegalEntityProvidingServices;
	private boolean cornellProvidedTrainingOrEquipmentRequired;
	private boolean informalMarketing;
	private boolean currentlyPaidThroughPayroll;
	private boolean everPaidThroughPayroll;
	private boolean servicesProvidedWithoutInsurance;

	public PaymentWorksVendor() {
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

	public String getRequestingCompanyName() {
		return requestingCompanyName;
	}

	public void setRequestingCompanyName(String requestingCompanyName) {
		this.requestingCompanyName = requestingCompanyName;
	}

	public String getRequestingCompanyNameOldValue() {
		return requestingCompanyNameOldValue;
	}

	public void setRequestingCompanyNameOldValue(String requestingCompanyNameOldValue) {
		this.requestingCompanyNameOldValue = requestingCompanyNameOldValue;
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

	public String getRequestingCompanyTelephoneOldValue() {
		return requestingCompanyTelephoneOldValue;
	}

	public void setRequestingCompanyTelephoneOldValue(String requestingCompanyTelephoneOldValue) {
		this.requestingCompanyTelephoneOldValue = requestingCompanyTelephoneOldValue;
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

	public String getRequestingCompanyTaxClassificationCode() {
		return requestingCompanyTaxClassificationCode;
	}

	public void setRequestingCompanyTaxClassificationCode(String requestingCompanyTaxClassificationCode) {
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

	public String getRemittanceAddressId() {
		return remittanceAddressId;
	}

	public void setRemittanceAddressId(String remittanceAddressId) {
		this.remittanceAddressId = remittanceAddressId;
	}

	public String getRemittanceAddressName() {
		return remittanceAddressName;
	}

	public void setRemittanceAddressName(String remittanceAddressName) {
		this.remittanceAddressName = remittanceAddressName;
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

	public String getCorpAddressId() {
		return corpAddressId;
	}

	public void setCorpAddressId(String corpAddressId) {
		this.corpAddressId = corpAddressId;
	}

	public String getCorpAddressName() {
		return corpAddressName;
	}

	public void setCorpAddressName(String corpAddressName) {
		this.corpAddressName = corpAddressName;
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

	public String getVendorRequestId() {
		return vendorRequestId;
	}

	public void setVendorRequestId(String vendorRequestId) {
		this.vendorRequestId = vendorRequestId;
	}

	public String getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getProcessStatus() {
		return processStatus;
	}

	public void setProcessStatus(String processStatus) {
		this.processStatus = processStatus;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

	public Timestamp getProcessTimestamp() {
		return processTimestamp;
	}

	public void setProcessTimestamp(Timestamp processTimestamp) {
		this.processTimestamp = processTimestamp;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public Integer getVendorHeaderGeneratedIdentifier() {
		return vendorHeaderGeneratedIdentifier;
	}

	public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
		this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
	}

	public Integer getVendorDetailAssignedIdentifier() {
		return vendorDetailAssignedIdentifier;
	}

	public void setVendorDetailAssignedIdentifier(Integer vendorDetailAssignedIdentifier) {
		this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
	}

	public String getDocumentNumberList() {
		return documentNumberList;
	}

	public void setDocumentNumberList(String documentNumberList) {
		this.documentNumberList = documentNumberList;
	}

	public String getVendorNumberList() {
		return vendorNumberList;
	}

	public void setVendorNumberList(String vendorNumberList) {
		this.vendorNumberList = vendorNumberList;
	}

	public boolean isCustomFieldConversionErrors() {
		return customFieldConversionErrors;
	}

	public void setCustomFieldConversionErrors(boolean customFieldConversionErrors) {
		this.customFieldConversionErrors = customFieldConversionErrors;
	}

	public String getServicesProvided() {
		return servicesProvided;
	}

	public void setServicesProvided(String servicesProvided) {
		this.servicesProvided = servicesProvided;
	}

	public String getVendorType() {
		return vendorType;
	}

	public void setVendorType(String vendorType) {
		this.vendorType = vendorType;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getInitiatorNetId() {
		return initiatorNetId;
	}

	public void setInitiatorNetId(String initiatorNetId) {
		this.initiatorNetId = initiatorNetId;
	}

	public boolean isInvoicing() {
		return invoicing;
	}

	public void setInvoicing(boolean invoicing) {
		this.invoicing = invoicing;
	}

	public String geteInvoiceContactName() {
		return eInvoiceContactName;
	}

	public void seteInvoiceContactName(String eInvoiceContactName) {
		this.eInvoiceContactName = eInvoiceContactName;
	}

	public String geteInvoiceContactPhoneNumber() {
		return eInvoiceContactPhoneNumber;
	}

	public void seteInvoiceContactPhoneNumber(String eInvoiceContactPhoneNumber) {
		this.eInvoiceContactPhoneNumber = eInvoiceContactPhoneNumber;
	}

	public String geteInvoicePhoneExtension() {
		return eInvoicePhoneExtension;
	}

	public void seteInvoicePhoneExtension(String eInvoicePhoneExtension) {
		this.eInvoicePhoneExtension = eInvoicePhoneExtension;
	}

	public String geteInvoiceEmail() {
		return eInvoiceEmail;
	}

	public void seteInvoiceEmail(String eInvoiceEmail) {
		this.eInvoiceEmail = eInvoiceEmail;
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

	public String getPoAttention() {
		return poAttention;
	}

	public void setPoAttention(String poAttention) {
		this.poAttention = poAttention;
	}

	public String getPoTransmissionMethod() {
		return poTransmissionMethod;
	}

	public void setPoTransmissionMethod(String poTransmissionMethod) {
		this.poTransmissionMethod = poTransmissionMethod;
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

	public String getPoState() {
		return poState;
	}

	public void setPoState(String poState) {
		this.poState = poState;
	}

	public String getPoPostalCode() {
		return poPostalCode;
	}

	public void setPoPostalCode(String poPostalCode) {
		this.poPostalCode = poPostalCode;
	}

	public String getPoCountry() {
		return poCountry;
	}

	public void setPoCountry(String poCountry) {
		this.poCountry = poCountry;
	}

	public String getPoFaxNumber() {
		return poFaxNumber;
	}

	public void setPoFaxNumber(String poFaxNumber) {
		this.poFaxNumber = poFaxNumber;
	}

	public String getPoCountryName() {
		return poCountryName;
	}

	public void setPoCountryName(String poCountryName) {
		this.poCountryName = poCountryName;
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

	public String getInsuranceCertificate() {
		return insuranceCertificate;
	}

	public void setInsuranceCertificate(String insuranceCertificate) {
		this.insuranceCertificate = insuranceCertificate;
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

	public String getVeteranCertificationExpirationDate() {
		return veteranCertificationExpirationDate;
	}

	public void setVeteranCertificationExpirationDate(String veteranCertificationExpirationDate) {
		this.veteranCertificationExpirationDate = veteranCertificationExpirationDate;
	}

	public String getMbeCertificationExpirationDate() {
		return mbeCertificationExpirationDate;
	}

	public void setMbeCertificationExpirationDate(String mbeCertificationExpirationDate) {
		this.mbeCertificationExpirationDate = mbeCertificationExpirationDate;
	}

	public String getWomanOwned() {
		return womanOwned;
	}

	public void setWomanOwned(String womanOwned) {
		this.womanOwned = womanOwned;
	}

	public String getWbeCertificationExpirationDate() {
		return wbeCertificationExpirationDate;
	}

	public void setWbeCertificationExpirationDate(String wbeCertificationExpirationDate) {
		this.wbeCertificationExpirationDate = wbeCertificationExpirationDate;
	}

	public String getDisabledVeteran() {
		return disabledVeteran;
	}

	public void setDisabledVeteran(String disabledVeteran) {
		this.disabledVeteran = disabledVeteran;
	}

	public String getMinorityStatus() {
		return minorityStatus;
	}

	public void setMinorityStatus(String minorityStatus) {
		this.minorityStatus = minorityStatus;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getAustralianProvince() {
		return australianProvince;
	}

	public void setAustralianProvince(String australianProvince) {
		this.australianProvince = australianProvince;
	}

	public String getStateProvince() {
		return stateProvince;
	}

	public void setStateProvince(String stateProvince) {
		this.stateProvince = stateProvince;
	}

	public String getCanadianProvince() {
		return canadianProvince;
	}

	public void setCanadianProvince(String canadianProvince) {
		this.canadianProvince = canadianProvince;
	}

	public boolean isSeperateLegalEntityProvidingServices() {
		return seperateLegalEntityProvidingServices;
	}

	public void setSeperateLegalEntityProvidingServices(boolean seperateLegalEntityProvidingServices) {
		this.seperateLegalEntityProvidingServices = seperateLegalEntityProvidingServices;
	}

	public boolean isCornellProvidedTrainingOrEquipmentRequired() {
		return cornellProvidedTrainingOrEquipmentRequired;
	}

	public void setCornellProvidedTrainingOrEquipmentRequired(boolean cornellProvidedTrainingOrEquipmentRequired) {
		this.cornellProvidedTrainingOrEquipmentRequired = cornellProvidedTrainingOrEquipmentRequired;
	}

	public boolean isInformalMarketing() {
		return informalMarketing;
	}

	public void setInformalMarketing(boolean informalMarketing) {
		this.informalMarketing = informalMarketing;
	}

	public boolean isDiverseBusiness() {
		return diverseBusiness;
	}

	public void setDiverseBusiness(boolean diverseBusiness) {
		this.diverseBusiness = diverseBusiness;
	}

	public boolean isCurrentlyPaidThroughPayroll() {
		return currentlyPaidThroughPayroll;
	}

	public void setCurrentlyPaidThroughPayroll(boolean currentlyPaidThroughPayroll) {
		this.currentlyPaidThroughPayroll = currentlyPaidThroughPayroll;
	}

	public boolean isEverPaidThroughPayroll() {
		return everPaidThroughPayroll;
	}

	public void setEverPaidThroughPayroll(boolean everPaidThroughPayroll) {
		this.everPaidThroughPayroll = everPaidThroughPayroll;
	}

	public boolean isAcceptCreditCards() {
		return acceptCreditCards;
	}

	public void setAcceptCreditCards(boolean acceptCreditCards) {
		this.acceptCreditCards = acceptCreditCards;
	}

	public boolean isServicesProvidedWithoutInsurance() {
		return servicesProvidedWithoutInsurance;
	}

	public void setServicesProvidedWithoutInsurance(boolean servicesProvidedWithoutInsurance) {
		this.servicesProvidedWithoutInsurance = servicesProvidedWithoutInsurance;
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

	public String getRequestingCompanyCorporateEmail() {
	    return requestingCompanyCorporateEmail;
	}

	public void setRequestingCompanyCorporateEmail(String requestingCompanyCorporateEmail) {
	    this.requestingCompanyCorporateEmail = requestingCompanyCorporateEmail;
	}

	public boolean isRemittanceAddressValidated() {
	    return remittanceAddressValidated;
	}

	public void setRemittanceAddressValidated(boolean remittanceAddressValidated) {
	    this.remittanceAddressValidated = remittanceAddressValidated;
	}

	public String getRemittanceAddressCompanyId() {
	    return remittanceAddressCompanyId;
	}

	public void setRemittanceAddressCompanyId(String remittanceAddressCompanyId) {
	    this.remittanceAddressCompanyId = remittanceAddressCompanyId;
	}

	public boolean isCorpAddressValidated() {
	    return corpAddressValidated;
	}

	public void setCorpAddressValidated(boolean corpAddressValidated) {
	    this.corpAddressValidated = corpAddressValidated;
	}

	public String getCorpAddressCompanyId() {
	    return corpAddressCompanyId;
	}

	public void setCorpAddressCompanyId(String corpAddressCompanyId) {
	    this.corpAddressCompanyId = corpAddressCompanyId;
	}

	public String getBankAcctId() {
	    return bankAcctId;
	}

	public void setBankAcctId(String bankAcctId) {
	    this.bankAcctId = bankAcctId;
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

	public String getBankAcctCompanyId() {
	    return bankAcctCompanyId;
	}

	public void setBankAcctCompanyId(String bankAcctCompanyId) {
	    this.bankAcctCompanyId = bankAcctCompanyId;
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

	public String getBankAddressId() {
	    return bankAddressId;
	}

	public void setBankAddressId(String bankAddressId) {
	    this.bankAddressId = bankAddressId;
	}

	public String getBankAddressName() {
	    return bankAddressName;
	}

	public void setBankAddressName(String bankAddressName) {
	    this.bankAddressName = bankAddressName;
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

	public String getBankAddressCompanyId() {
	    return bankAddressCompanyId;
	}

	public void setBankAddressCompanyId(String bankAddressCompanyId) {
	    this.bankAddressCompanyId = bankAddressCompanyId;
	}

	public String getDiversityClassifications() {
	    return diversityClassifications;
	}

	public void setDiversityClassifications(String diversityClassifications) {
	    this.diversityClassifications = diversityClassifications;
	}

	public String getTaxCountry() {
	    return taxCountry;
	}

	public void setTaxCountry(String taxCountry) {
	    this.taxCountry = taxCountry;
	}

	public String toString() {
	    StringBuilder sb = new StringBuilder("PaymentWorksVendor::").append(KFSConstants.NEWLINE);
	    sb.append("requestingCompanyId").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyId).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyLegalName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyLegalName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyTin").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER)
	        .append((StringUtils.isNotBlank(requestingCompanyTin) ? PaymentWorksConstants.OUTPUT_RESTRICTED_DATA_PRESENT : KFSConstants.EMPTY_STRING)).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyTinType").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyTinType).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyTaxCountry").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyTaxCountry).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyLegalName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyLegalName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyLegalFirstName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyLegalFirstName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyLegalLastName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyLegalLastName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyNameOldValue").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyNameOldValue).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyDesc").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyDesc).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyTelephone").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyTelephone).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyTelephoneOldValue").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyTelephoneOldValue).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyDuns").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyDuns).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyTaxClassificationName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyTaxClassificationName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyTaxClassificationCode").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyTaxClassificationCode).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyUrl").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyUrl).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyW8W9").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER)
	        .append((StringUtils.isNotBlank(requestingCompanyW8W9) ? PaymentWorksConstants.OUTPUT_RESTRICTED_DATA_PRESENT : KFSConstants.EMPTY_STRING)).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("requestingCompanyCorporateEmail").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(requestingCompanyCorporateEmail).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);

	    sb.append("remittanceAddressId").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(remittanceAddressId).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("remittanceAddressName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(remittanceAddressName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("remittanceAddressStreet1").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(remittanceAddressStreet1).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("remittanceAddressStreet2").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(remittanceAddressStreet2).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("remittanceAddressCity").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(remittanceAddressCity).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("remittanceAddressState").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(remittanceAddressState).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("remittanceAddressCountry").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(remittanceAddressCountry).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("remittanceAddressZipCode").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(remittanceAddressZipCode).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("remittanceAddressValidated").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER)
	        .append((remittanceAddressValidated ? KFSConstants.Booleans.TRUE : KFSConstants.Booleans.FALSE)).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("remittanceAddressCompanyId").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(remittanceAddressCompanyId).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);

	    sb.append("corpAddressId").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(corpAddressId).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("corpAddressName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(corpAddressName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("corpAddressStreet1").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(corpAddressStreet1).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("corpAddressStreet2").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(corpAddressStreet2).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("corpAddressCity").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(corpAddressCity).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("corpAddressState").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(corpAddressState).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("corpAddressCountry").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(corpAddressCountry).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("corpAddressZipCode").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(corpAddressZipCode).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("corpAddressValidated").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER)
	        .append((corpAddressValidated ? KFSConstants.Booleans.TRUE : KFSConstants.Booleans.FALSE)).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("corpAddressCompanyId").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(corpAddressCompanyId).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);

	    sb.append("bankAcctId").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAcctId).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAcctBankName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAcctBankName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAcctRoutingNumber").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER)
	        .append((StringUtils.isNotBlank(bankAcctRoutingNumber) ? PaymentWorksConstants.OUTPUT_RESTRICTED_DATA_PRESENT : KFSConstants.EMPTY_STRING)).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAcctBankAccountNumber").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER)
	        .append((StringUtils.isNotBlank(bankAcctBankAccountNumber) ? PaymentWorksConstants.OUTPUT_RESTRICTED_DATA_PRESENT : KFSConstants.EMPTY_STRING)).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAcctBankValidationFile").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER)
	        .append((StringUtils.isNotBlank(bankAcctBankValidationFile) ? PaymentWorksConstants.OUTPUT_RESTRICTED_DATA_PRESENT : KFSConstants.EMPTY_STRING)).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAcctAchEmail").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAcctAchEmail).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAcctType").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAcctType).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAcctAuthorized").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAcctAuthorized).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAcctCompanyId").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAcctCompanyId).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAcctSwiftCode").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAcctSwiftCode).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAcctNameOnAccount").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAcctNameOnAccount).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);

	    sb.append("bankAddressId").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAddressId).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAddressName").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAddressName).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAddressStreet1").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAddressStreet1).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAddressStreet2").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAddressStreet2).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAddressCity").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAddressCity).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAddressState").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAddressState).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAddressCountry").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAddressCountry).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAddressZipCode").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAddressZipCode).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAddressValidated").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER)
	        .append((bankAddressValidated ? KFSConstants.Booleans.TRUE : KFSConstants.Booleans.FALSE)).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
	    sb.append("bankAddressCompanyId").append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_BEGIN_DELIMITER).append(bankAddressCompanyId).append(PaymentWorksConstants.OUTPUT_ATTRIBUTE_END_DELIMITER);
        return sb.toString();
    }
}
