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

import java.sql.Timestamp;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class PaymentWorksVendor extends PersistableBusinessObjectBase {

	// requesting company
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

	// custom fields
	private String typeOfBusiness;
	private boolean studentIndicator;
	private boolean stateOfConnecticutEmployeeIndicator;
	private String currentFormerStateOfConnecticutEmployee;
	private boolean immediateFamilyIndicator;
	private boolean smallOrMinorityOwnedBusinessIndicator;
	private String poFaxNumber;
	private String achEmailAddress;
	private String uconnContactEmailAddress;

	// remittance address
	private String remittanceAddressId;
	private String remittanceAddressName;
	private String remittanceAddressStreet1;
	private String remittanceAddressStreet2;
	private String remittanceAddressCity;
	private String remittanceAddressState;
	private String remittanceAddressCountry;
	private String remittanceAddressZipCode;

	// Corp Address
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

	private transient boolean sendToPaymentWorks;

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

	public String getTypeOfBusiness() {
		return typeOfBusiness;
	}

	public void setTypeOfBusiness(String typeOfBusiness) {
		this.typeOfBusiness = typeOfBusiness;
	}

	public String getPoFaxNumber() {
		return poFaxNumber;
	}

	public void setPoFaxNumber(String poFaxNumber) {
		this.poFaxNumber = poFaxNumber;
	}

	public String getAchEmailAddress() {
		return achEmailAddress;
	}

	public void setAchEmailAddress(String achEmailAddress) {
		this.achEmailAddress = achEmailAddress;
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

	public String getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}

	public Timestamp getProcessTimestamp() {
		return processTimestamp;
	}

	public void setProcessTimestamp(Timestamp processTimestamp) {
		this.processTimestamp = processTimestamp;
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

	public String getCurrentFormerStateOfConnecticutEmployee() {
		return currentFormerStateOfConnecticutEmployee;
	}

	public void setCurrentFormerStateOfConnecticutEmployee(String currentFormerStateOfConnecticutEmployee) {
		this.currentFormerStateOfConnecticutEmployee = currentFormerStateOfConnecticutEmployee;
	}

	public String getUconnContactEmailAddress() {
		return uconnContactEmailAddress;
	}

	public void setUconnContactEmailAddress(String uconnContactEmailAddress) {
		this.uconnContactEmailAddress = uconnContactEmailAddress;
	}

	public String getVendorRequestId() {
		return vendorRequestId;
	}

	public void setVendorRequestId(String vendorRequestId) {
		this.vendorRequestId = vendorRequestId;
	}

	public String getDocumentNumber() {
		return documentNumber;
	}

	public void setDocumentNumber(String documentNumber) {
		this.documentNumber = documentNumber;
	}

	public boolean isStudentIndicator() {
		return studentIndicator;
	}

	public void setStudentIndicator(boolean studentIndicator) {
		this.studentIndicator = studentIndicator;
	}

	public boolean isStateOfConnecticutEmployeeIndicator() {
		return stateOfConnecticutEmployeeIndicator;
	}

	public void setStateOfConnecticutEmployeeIndicator(boolean stateOfConnecticutEmployeeIndicator) {
		this.stateOfConnecticutEmployeeIndicator = stateOfConnecticutEmployeeIndicator;
	}

	public boolean isImmediateFamilyIndicator() {
		return immediateFamilyIndicator;
	}

	public void setImmediateFamilyIndicator(boolean immediateFamilyIndicator) {
		this.immediateFamilyIndicator = immediateFamilyIndicator;
	}

	public boolean isSmallOrMinorityOwnedBusinessIndicator() {
		return smallOrMinorityOwnedBusinessIndicator;
	}

	public void setSmallOrMinorityOwnedBusinessIndicator(boolean smallOrMinorityOwnedBusinessIndicator) {
		this.smallOrMinorityOwnedBusinessIndicator = smallOrMinorityOwnedBusinessIndicator;
	}

	public String getProcessStatus() {
		return processStatus;
	}

	public void setProcessStatus(String processStatus) {
		this.processStatus = processStatus;
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

	public String getRequestingCompanyNameOldValue() {
		return requestingCompanyNameOldValue;
	}

	public void setRequestingCompanyNameOldValue(String requestingCompanyNameOldValue) {
		this.requestingCompanyNameOldValue = requestingCompanyNameOldValue;
	}

	public String getRequestingCompanyTelephoneOldValue() {
		return requestingCompanyTelephoneOldValue;
	}

	public void setRequestingCompanyTelephoneOldValue(String requestingCompanyTelephoneOldValue) {
		this.requestingCompanyTelephoneOldValue = requestingCompanyTelephoneOldValue;
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

	public boolean isSendToPaymentWorks() {
		return sendToPaymentWorks;
	}

	public void setSendToPaymentWorks(boolean sendToPaymentWorks) {
		this.sendToPaymentWorks = sendToPaymentWorks;
	}

	public String getVendorName() {
		return vendorName;
	}

	public void setVendorName(String vendorName) {
		this.vendorName = vendorName;
	}

}
