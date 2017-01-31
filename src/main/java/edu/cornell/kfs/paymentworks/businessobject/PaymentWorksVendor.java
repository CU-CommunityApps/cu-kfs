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

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class PaymentWorksVendor extends PersistableBusinessObjectBase {
	private static final long serialVersionUID = -4431128959859612352L;
	
	private String requestingCompanyId;
	private String requestingCompanyTin;
	private String requestingCompanyTinType;
	private String requestingCompanyTaxCountry;
	private String requestingCompanyLegalName;
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
	
	private String remittanceAddressId;
	private String remittanceAddressName;
	private String remittanceAddressStreet1;
	private String remittanceAddressStreet2;
	private String remittanceAddressCity;
	private String remittanceAddressState;
	private String remittanceAddressCountry;
	private String remittanceAddressZipCode;

	private String corpAddressId;
	private String corpAddressName;
	private String corpAddressStreet1;
	private String corpAddressStreet2;
	private String corpAddressCity;
	private String corpAddressState;
	private String corpAddressCountry;
	private String corpAddressZipCode;

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
	private String businessPurpose;
	private String servicesProvided;
	private String vendorType;
	private String emailAddress;
	private String initiatorNetId;
	
	private boolean invoicing;
	private String eInvoiceContactName;
	private String eInvoiceContactPhoneNumber;
	private String eInvoicePhoneExtension;
	private String eInvoiceEmail;
	
	private String vendorInformationContact;
	private String vendorInformationContactName;
	private String vendorInformationPhoneNumber;
	private String vendorInformationPhoneExtension;
	private String vendorInformationEmail;
	
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
	
	private boolean conflictOfInterest;
	private String conflictOfInterestRelationshipToEmployee;
	private String conflictOfInterestEmployeeName;
	private String conflictOfInterestEmployeePhoneNumber;
	
	private String insurance;
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
	
	private String diversityClassification;
	private Date veteranCertificationExpirationDate;
	private Date mbeCertificationExpirationDate;
	private String womanOwned;
	private Date wbeCertificationExpirationDate;
	private String disabledVeteran;
	private String minorityStatus;
	
	private String state;
	private String australianProvince;
	private String stateProvince;
	private String canadianProvince;
	
	private boolean seperateLegalEntityProvidingServices;
	private boolean cornellProvidedTrainingOrEquipmentRequired;
	private boolean informalMarketing;
	private boolean diverseBusiness;
	private boolean currentlyPaidThroughPayroll;
	private boolean everPaidThroughPayroll;
	private boolean acceptCreditCards;
	private boolean servicesProvidedWithoutInsurance;

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

	public String getBusinessPurpose() {
		return businessPurpose;
	}

	public void setBusinessPurpose(String businessPurpose) {
		this.businessPurpose = businessPurpose;
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

	public String getVendorInformationContact() {
		return vendorInformationContact;
	}

	public void setVendorInformationContact(String vendorInformationContact) {
		this.vendorInformationContact = vendorInformationContact;
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

	public String getInsurance() {
		return insurance;
	}

	public void setInsurance(String inusrance) {
		this.insurance = insurance;
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

	public String getDiversityClassification() {
		return diversityClassification;
	}

	public void setDiversityClassification(String diversityClassification) {
		this.diversityClassification = diversityClassification;
	}

	public Date getVeteranCertificationExpirationDate() {
		return veteranCertificationExpirationDate;
	}

	public void setVeteranCertificationExpirationDate(Date veteranCertificationExpirationDate) {
		this.veteranCertificationExpirationDate = veteranCertificationExpirationDate;
	}

	public Date getMbeCertificationExpirationDate() {
		return mbeCertificationExpirationDate;
	}

	public void setMbeCertificationExpirationDate(Date mbeCertificationExpirationDate) {
		this.mbeCertificationExpirationDate = mbeCertificationExpirationDate;
	}

	public String getWomanOwned() {
		return womanOwned;
	}

	public void setWomanOwned(String womanOwned) {
		this.womanOwned = womanOwned;
	}

	public Date getWbeCertificationExpirationDate() {
		return wbeCertificationExpirationDate;
	}

	public void setWbeCertificationExpirationDate(Date wbeCertificationExpirationDate) {
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

}
