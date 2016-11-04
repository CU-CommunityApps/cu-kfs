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
package edu.cornell.kfs.paymentworks.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorAlias;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCorpAddressDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDetailDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksRemittanceAddressDTO;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class PaymentWorksNewVendorConversionUtil {

	/**
	 * converts PaymentWorks vendor into kfs vendor detail
	 *
	 * @param paymentWorksVendor
	 * @return
	 */
	public VendorDetail createVendorDetail(PaymentWorksVendor paymentWorksVendor) {

		VendorDetail vendorDetail = new VendorDetail();
		VendorDetailExtension vendorDetailExtension = new VendorDetailExtension();

		// set header
		vendorDetail.setVendorHeader(createVendorHeader(paymentWorksVendor));

		// set addresses
		List<VendorAddress> vendorAddresses = new ArrayList<VendorAddress>();
		vendorAddresses.add(createCorpAddress(paymentWorksVendor));
		vendorAddresses.add(createRemittanceAddress(paymentWorksVendor));
		vendorDetail.setVendorAddresses(vendorAddresses);

		// set detail
		vendorDetail.setVendorParentIndicator(true);
		vendorDetail.setVendorName(paymentWorksVendor.getRequestingCompanyLegalName());
		vendorDetail.setVendorPaymentTermsCode("00N45");

		if ((StringUtils.equals(paymentWorksVendor.getRequestingCompanyTaxClassificationCode(), "1")
				|| StringUtils.equals(paymentWorksVendor.getRequestingCompanyTaxClassificationCode(), "2"))
				&& StringUtils.equals(paymentWorksVendor.getTypeOfBusiness(), "Other")) {
			vendorDetail.setTaxableIndicator(false);
		} else {
			vendorDetail.setTaxableIndicator(true);
		}

		if (!StringUtils.equals(paymentWorksVendor.getRequestingCompanyTaxCountry(), "US")) {
			// vendorDetailExtension.setHoldCode("NRA");
		} else if (StringUtils.equals(paymentWorksVendor.getTypeOfBusiness(), "Entertainment/Public Speaker")
				&& !StringUtils.equalsIgnoreCase("CT", paymentWorksVendor.getCorpAddressState())) {
			// vendorDetailExtension.setHoldCode("A&E-TAX");
		} else if (paymentWorksVendor.isStateOfConnecticutEmployeeIndicator()
				|| paymentWorksVendor.isStudentIndicator()) {
			// vendorDetailExtension.setHoldCode("REIMONLY");
		}

		vendorDetail.setExtension(vendorDetailExtension);

		vendorDetail.setVendorDunsNumber(paymentWorksVendor.getRequestingCompanyDuns());
		vendorDetail.setVendorUrlAddress(paymentWorksVendor.getRequestingCompanyUrl());
		vendorDetail.setActiveIndicator(true);

		// add vendor alias
		VendorAlias vendorAlias = new VendorAlias();
		vendorAlias.setVendorAliasName(paymentWorksVendor.getRequestingCompanyName());
		vendorAlias.setActive(true);

		List<VendorAlias> vendorAliasList = new ArrayList<VendorAlias>();
		vendorAliasList.add(vendorAlias);
		vendorDetail.setVendorAliases(vendorAliasList);

		// add phone number
		if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTelephone())) {
			VendorPhoneNumber vendorPhoneNumber = new VendorPhoneNumber();
			vendorPhoneNumber.setVendorPhoneNumber(paymentWorksVendor.getRequestingCompanyTelephone());
			vendorPhoneNumber.setVendorPhoneTypeCode("PH");
			vendorPhoneNumber.setActive(true);

			List<VendorPhoneNumber> vendorPhoneNumberList = new ArrayList<VendorPhoneNumber>();
			vendorPhoneNumberList.add(vendorPhoneNumber);
			vendorDetail.setVendorPhoneNumbers(vendorPhoneNumberList);
		}

		return vendorDetail;
	}

	protected VendorHeader createVendorHeader(PaymentWorksVendor paymentWorksVendor) {

		VendorHeader vendorHeader = new VendorHeader();

		vendorHeader.setVendorTypeCode("PO");

		if (StringUtils.equals(paymentWorksVendor.getRequestingCompanyTaxCountry(), "US")) {
			vendorHeader.setVendorForeignIndicator(false);
		} else {
			vendorHeader.setVendorForeignIndicator(true);
		}

		vendorHeader.setVendorTaxNumber(paymentWorksVendor.getRequestingCompanyTin());

		vendorHeader.setVendorTaxTypeCode(getTaxTypeCode(paymentWorksVendor.getRequestingCompanyTinType()));

		// set ownership code
		vendorHeader.setVendorOwnershipCode(
				getOwnershipCode(paymentWorksVendor.getRequestingCompanyTaxClassificationCode()));

		if (paymentWorksVendor.isStudentIndicator()
				&& StringUtils.equals(paymentWorksVendor.getTypeOfBusiness(), "Other")) {
			vendorHeader.setVendorOwnershipCategoryCode("US");
		} else if (paymentWorksVendor.isStateOfConnecticutEmployeeIndicator()
				&& StringUtils.equals(paymentWorksVendor.getTypeOfBusiness(), "Other")) {
			vendorHeader.setVendorOwnershipCategoryCode("SE");
		} else if (StringUtils.equals(paymentWorksVendor.getTypeOfBusiness(), "Attorney Fees")) {
			vendorHeader.setVendorOwnershipCategoryCode("LE");
		} else if (StringUtils.equals(paymentWorksVendor.getTypeOfBusiness(), "Medical Services")) {
			vendorHeader.setVendorOwnershipCategoryCode("ME");
		} else if (StringUtils.equals(paymentWorksVendor.getTypeOfBusiness(), "Entertainment/Public Speaker")) {
			vendorHeader.setVendorOwnershipCategoryCode("EN");
		} else {
			vendorHeader.setVendorOwnershipCategoryCode("");
		}

		vendorHeader.setVendorCorpCitizenCode(paymentWorksVendor.getRequestingCompanyTaxCountry());

		return vendorHeader;
	}

	protected VendorAddress createCorpAddress(PaymentWorksVendor paymentWorksVendor) {

		VendorAddress vendorAddress = new VendorAddress();

		vendorAddress.setVendorAddressTypeCode(VendorConstants.AddressTypes.PURCHASE_ORDER);
		vendorAddress.setVendorAttentionName("");
		vendorAddress.setVendorBusinessToBusinessUrlAddress("");
		vendorAddress.setVendorLine1Address(paymentWorksVendor.getCorpAddressStreet1());
		vendorAddress.setVendorLine2Address(paymentWorksVendor.getCorpAddressStreet2());
		vendorAddress.setVendorCityName(paymentWorksVendor.getCorpAddressCity());

		if (StringUtils.equalsIgnoreCase(paymentWorksVendor.getCorpAddressCountry(), "US")) {
			vendorAddress.setVendorStateCode(paymentWorksVendor.getCorpAddressState());
		} else {
			vendorAddress.setVendorAddressInternationalProvinceName(paymentWorksVendor.getCorpAddressState());
		}

		vendorAddress.setVendorZipCode(paymentWorksVendor.getCorpAddressZipCode());
		vendorAddress.setVendorCountryCode(paymentWorksVendor.getCorpAddressCountry());

		if (StringUtils.isNotBlank(paymentWorksVendor.getPoFaxNumber())) {
			vendorAddress.setVendorFaxNumber(paymentWorksVendor.getPoFaxNumber());
		}

		if (StringUtils.isNotBlank(paymentWorksVendor.getAchEmailAddress())) {
			vendorAddress.setVendorAddressEmailAddress(paymentWorksVendor.getAchEmailAddress());
		}

		vendorAddress.setVendorDefaultAddressIndicator(true);
		vendorAddress.setActive(true);

		return vendorAddress;
	}

	protected VendorAddress createRemittanceAddress(PaymentWorksVendor paymentWorksVendor) {
		VendorAddress vendorAddress = new VendorAddress();

		vendorAddress.setVendorAddressTypeCode(VendorConstants.AddressTypes.REMIT);
		vendorAddress.setVendorAttentionName("");
		vendorAddress.setVendorBusinessToBusinessUrlAddress("");
		vendorAddress.setVendorLine1Address(paymentWorksVendor.getRemittanceAddressStreet1());
		vendorAddress.setVendorLine2Address(paymentWorksVendor.getRemittanceAddressStreet2());
		vendorAddress.setVendorCityName(paymentWorksVendor.getRemittanceAddressCity());

		if (StringUtils.equalsIgnoreCase(paymentWorksVendor.getRemittanceAddressCountry(), "US")) {
			vendorAddress.setVendorStateCode(paymentWorksVendor.getRemittanceAddressState());
		} else {
			vendorAddress.setVendorAddressInternationalProvinceName(paymentWorksVendor.getRemittanceAddressState());
		}

		vendorAddress.setVendorZipCode(paymentWorksVendor.getRemittanceAddressZipCode());
		vendorAddress.setVendorCountryCode(paymentWorksVendor.getRemittanceAddressCountry());
		vendorAddress.setVendorDefaultAddressIndicator(true);
		vendorAddress.setActive(true);

		return vendorAddress;
	}

	public PaymentWorksVendor createPaymentWorksNewVendor(VendorDetail vendorDetail, String documentNumber) {

		PaymentWorksVendor paymentWorksVendor = new PaymentWorksVendor();

		String sequence = SpringContext.getBean(SequenceAccessorService.class)
				.getNextAvailableSequenceNumber(PaymentWorksConstants.PAYMENT_WORKS_VENDOR_SEQ).toString();

		paymentWorksVendor.setVendorRequestId("KFS" + sequence);
		paymentWorksVendor.setDocumentNumber(documentNumber);
		paymentWorksVendor.setVendorDetailAssignedIdentifier(vendorDetail.getVendorDetailAssignedIdentifier());
		paymentWorksVendor.setVendorHeaderGeneratedIdentifier(vendorDetail.getVendorHeaderGeneratedIdentifier());
		paymentWorksVendor.setRequestingCompanyLegalName(vendorDetail.getVendorName());
		paymentWorksVendor.setRequestingCompanyTin(vendorDetail.getVendorHeader().getVendorTaxNumber());
		// paymentWorksVendor.setUconnContactEmailAddress();

		// set flag indicating if we will send this to PaymentWorks
		if (vendorDetail.isActiveIndicator()
				&& !(StringUtils.equals(vendorDetail.getVendorHeader().getVendorOwnershipCategoryCode(), "US")
						|| StringUtils.equals(vendorDetail.getVendorHeader().getVendorOwnershipCategoryCode(), "NU")
						|| StringUtils.equals(vendorDetail.getVendorHeader().getVendorOwnershipCategoryCode(), "SE")
						|| StringUtils.equals(vendorDetail.getVendorHeader().getVendorOwnershipCategoryCode(), "UE")
						|| StringUtils.equals(vendorDetail.getVendorHeader().getVendorOwnershipCategoryCode(), "AE"))) {

			paymentWorksVendor.setSendToPaymentWorks(true);
		} else {
			paymentWorksVendor.setSendToPaymentWorks(false);
		}

		// set address
		VendorAddress poAddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(
				vendorDetail.getVendorHeaderGeneratedIdentifier(), vendorDetail.getVendorDetailAssignedIdentifier(),
				VendorConstants.AddressTypes.PURCHASE_ORDER, null);

		if (ObjectUtils.isNotNull(poAddress)) {
			paymentWorksVendor.setCorpAddressStreet1(poAddress.getVendorLine1Address());
			paymentWorksVendor.setCorpAddressStreet2(poAddress.getVendorLine2Address());
			paymentWorksVendor.setCorpAddressCity(poAddress.getVendorCityName());

			if (StringUtils.equalsIgnoreCase(poAddress.getVendorCountryCode(), "US")) {
				paymentWorksVendor.setCorpAddressState(poAddress.getVendorStateCode());
			} else {
				paymentWorksVendor.setCorpAddressState(poAddress.getVendorAddressInternationalProvinceName());
			}

			paymentWorksVendor.setCorpAddressZipCode(poAddress.getVendorZipCode());
			paymentWorksVendor.setCorpAddressCountry(poAddress.getVendorCountryCode());
		}

		VendorAddress remitAddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(
				vendorDetail.getVendorHeaderGeneratedIdentifier(), vendorDetail.getVendorDetailAssignedIdentifier(),
				VendorConstants.AddressTypes.REMIT, null);

		if (ObjectUtils.isNotNull(remitAddress)) {
			paymentWorksVendor.setRemittanceAddressStreet1(remitAddress.getVendorLine1Address());
			paymentWorksVendor.setRemittanceAddressStreet2(remitAddress.getVendorLine2Address());
			paymentWorksVendor.setRemittanceAddressCity(remitAddress.getVendorCityName());

			if (StringUtils.equalsIgnoreCase(remitAddress.getVendorCountryCode(), "US")) {
				paymentWorksVendor.setRemittanceAddressState(remitAddress.getVendorStateCode());
			} else {
				paymentWorksVendor.setRemittanceAddressState(remitAddress.getVendorAddressInternationalProvinceName());
			}

			paymentWorksVendor.setRemittanceAddressZipCode(remitAddress.getVendorZipCode());
			paymentWorksVendor.setRemittanceAddressCountry(remitAddress.getVendorCountryCode());
		}

		return paymentWorksVendor;
	}

	public PaymentWorksVendor createPaymentWorksNewVendor(
			PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO) {

		PaymentWorksVendor paymentWorksNewVendor = new PaymentWorksVendor();
		PaymentWorksUtil paymentWorksUtil = new PaymentWorksUtil();

		// Requesting company info
		paymentWorksNewVendor.setRequestingCompanyId(paymentWorksNewVendorDetailDTO.getRequesting_company().getId());
		paymentWorksNewVendor.setRequestingCompanyTin(paymentWorksNewVendorDetailDTO.getRequesting_company().getTin());
		paymentWorksNewVendor
				.setRequestingCompanyTinType(paymentWorksNewVendorDetailDTO.getRequesting_company().getTin_type());
		paymentWorksNewVendor.setRequestingCompanyTaxCountry(
				paymentWorksNewVendorDetailDTO.getRequesting_company().getTax_country());
		// KPS-316 restrict field size to db column length
		paymentWorksNewVendor.setRequestingCompanyLegalName(paymentWorksUtil.trimFieldToMax(
				paymentWorksNewVendorDetailDTO.getRequesting_company().getLegal_name(), "requestingCompanyLegalName"));
		paymentWorksNewVendor.setRequestingCompanyName(paymentWorksUtil.trimFieldToMax(
				paymentWorksNewVendorDetailDTO.getRequesting_company().getName(), "requestingCompanyName"));
		paymentWorksNewVendor.setRequestingCompanyDesc(paymentWorksUtil.trimFieldToMax(
				paymentWorksNewVendorDetailDTO.getRequesting_company().getDesc(), "requestingCompanyDesc"));
		String companyPhone = paymentWorksUtil.trimFieldToMax(
				paymentWorksNewVendorDetailDTO.getRequesting_company().getTelephone(), "requestingCompanyTelephone");
		paymentWorksNewVendor.setRequestingCompanyTelephone(paymentWorksUtil.convertPhoneNumber(companyPhone));
		// end KPS-316 company info
		paymentWorksNewVendor
				.setRequestingCompanyDuns(paymentWorksNewVendorDetailDTO.getRequesting_company().getDuns());
		paymentWorksNewVendor.setRequestingCompanyTaxClassificationName(
				paymentWorksNewVendorDetailDTO.getRequesting_company().getTax_classification().getName());
		paymentWorksNewVendor.setRequestingCompanyTaxClassificationCode(
				paymentWorksNewVendorDetailDTO.getRequesting_company().getTax_classification().getCode());
		paymentWorksNewVendor.setRequestingCompanyUrl(paymentWorksUtil.trimFieldToMax(
				paymentWorksNewVendorDetailDTO.getRequesting_company().getUrl(), "requestingCompanyUrl"));
		paymentWorksNewVendor.setRequestingCompanyW8W9(paymentWorksUtil.trimFieldToMax(
				paymentWorksNewVendorDetailDTO.getRequesting_company().getW8_w9(), "requestingCompanyW8W9"));

		// custom fields
		Map<String, String> customFieldsMap = paymentWorksUtil
				.convertFieldArrayToMap(paymentWorksNewVendorDetailDTO.getCustom_fields());

		paymentWorksNewVendor.setTypeOfBusiness(customFieldsMap.get("Type of Business"));
		paymentWorksNewVendor.setStudentIndicator(BooleanUtils.toBoolean(customFieldsMap.get("UConn Student")));
		paymentWorksNewVendor.setStateOfConnecticutEmployeeIndicator(
				BooleanUtils.toBoolean(customFieldsMap.get("State of Connecticut Employee")));
		paymentWorksNewVendor.setCurrentFormerStateOfConnecticutEmployee(
				customFieldsMap.get("Current/former State of Connecticut Employee"));
		paymentWorksNewVendor
				.setImmediateFamilyIndicator(BooleanUtils.toBoolean(customFieldsMap.get("Immediate family")));
		paymentWorksNewVendor.setSmallOrMinorityOwnedBusinessIndicator(
				BooleanUtils.toBoolean(customFieldsMap.get("Small/Minority owned business")));
		// KPS-316 restrict field size to db column length
		String poFaxNbr = paymentWorksUtil.trimFieldToMax(customFieldsMap.get("PO Fax #"), "poFaxNumber");
		paymentWorksNewVendor.setPoFaxNumber(paymentWorksUtil.convertPhoneNumber(poFaxNbr));
		// end kps-316 custom fields
		String achEmailAddress = customFieldsMap.get("ACH - email address");
		String uconnContactEmailAddress = customFieldsMap.get("Email of UConn Contact");

		// Remove +xxx from email address (gmail alias, not supported by KFS)
		if (StringUtils.isNotEmpty(achEmailAddress)) {
			achEmailAddress = achEmailAddress.replaceAll("(\\+)(.)*(?=@)", "");
		}

		if (StringUtils.isNotEmpty(uconnContactEmailAddress)) {
			uconnContactEmailAddress = uconnContactEmailAddress.replaceAll("(\\+)(.)*(?=@)", "");
		}

		paymentWorksNewVendor.setAchEmailAddress(paymentWorksUtil.trimFieldToMax(achEmailAddress, "achEmailAddress"));
		paymentWorksNewVendor.setUconnContactEmailAddress(
				paymentWorksUtil.trimFieldToMax(uconnContactEmailAddress, "uconnContactEmailAddress"));

		// remittance address
		if (ObjectUtils.isNotNull(paymentWorksNewVendorDetailDTO.getRequesting_company().getRemittance_addresses())
				&& ObjectUtils.isNotNull(paymentWorksNewVendorDetailDTO.getRequesting_company()
						.getRemittance_addresses().getRemittance_address())
				&& paymentWorksNewVendorDetailDTO.getRequesting_company().getRemittance_addresses()
						.getRemittance_address().size() > 0) {

			// should only be 1 address
			PaymentWorksRemittanceAddressDTO remittanceAddress = paymentWorksNewVendorDetailDTO.getRequesting_company()
					.getRemittance_addresses().getRemittance_address().get(0);

			paymentWorksNewVendor.setRemittanceAddressId(remittanceAddress.getId());
			paymentWorksNewVendor.setRemittanceAddressName(
					paymentWorksUtil.trimFieldToMax(remittanceAddress.getName(), "remittanceAddressName"));
			paymentWorksNewVendor.setRemittanceAddressStreet1(
					paymentWorksUtil.trimFieldToMax(remittanceAddress.getStreet1(), "remittanceAddressStreet1"));
			paymentWorksNewVendor.setRemittanceAddressStreet2(
					paymentWorksUtil.trimFieldToMax(remittanceAddress.getStreet2(), "remittanceAddressStreet2"));
			paymentWorksNewVendor.setRemittanceAddressCity(
					paymentWorksUtil.trimFieldToMax(remittanceAddress.getCity(), "remittanceAddressCity"));
			paymentWorksNewVendor.setRemittanceAddressState(remittanceAddress.getState());
			paymentWorksNewVendor.setRemittanceAddressCountry(remittanceAddress.getCountry());
			paymentWorksNewVendor.setRemittanceAddressZipCode(remittanceAddress.getZipcode());
		}

		// corp address
		if (ObjectUtils.isNotNull(paymentWorksNewVendorDetailDTO.getRequesting_company().getCorp_address())) {

			PaymentWorksCorpAddressDTO corpAddress = paymentWorksNewVendorDetailDTO.getRequesting_company()
					.getCorp_address();

			paymentWorksNewVendor.setCorpAddressId(corpAddress.getId());
			paymentWorksNewVendor
					.setCorpAddressName(paymentWorksUtil.trimFieldToMax(corpAddress.getName(), "corpAddressName"));
			paymentWorksNewVendor.setCorpAddressStreet1(
					paymentWorksUtil.trimFieldToMax(corpAddress.getStreet1(), "corpAddressStreet1"));
			paymentWorksNewVendor.setCorpAddressStreet2(
					paymentWorksUtil.trimFieldToMax(corpAddress.getStreet2(), "corpAddressStreet2"));
			paymentWorksNewVendor
					.setCorpAddressCity(paymentWorksUtil.trimFieldToMax(corpAddress.getCity(), "corpAddressCity"));
			paymentWorksNewVendor.setCorpAddressState(corpAddress.getState());
			paymentWorksNewVendor.setCorpAddressCountry(corpAddress.getCountry());
			paymentWorksNewVendor.setCorpAddressZipCode(corpAddress.getZipcode());
		}

		paymentWorksNewVendor.setVendorRequestId(paymentWorksNewVendorDetailDTO.getId());

		return paymentWorksNewVendor;
	}

	protected PaymentWorksVendor createPaymentWorksNewVendorFromDetail(PaymentWorksVendor vendor) {

		VendorDetail vendorDetail = SpringContext.getBean(VendorService.class).getVendorDetail(
				vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
		PaymentWorksVendor newVendor = null;

		if (ObjectUtils.isNotNull(vendorDetail)) {
			newVendor = new PaymentWorksNewVendorConversionUtil().createPaymentWorksNewVendor(vendorDetail,
					vendor.getDocumentNumber());

			newVendor.setVendorRequestId(vendor.getVendorRequestId());
			newVendor.setObjectId(vendor.getObjectId());
			newVendor.setVersionNumber(vendor.getVersionNumber());
			newVendor.setUconnContactEmailAddress(vendor.getUconnContactEmailAddress());
			newVendor.setAchEmailAddress(vendor.getAchEmailAddress());
		}

		return newVendor;
	}

	/**
	 * Translates corp status from PaymentWorks to KFS ownership code
	 *
	 * @param corpStatus
	 * @return
	 */
	protected String getOwnershipCode(String taxClassificationCode) {

		String ownershipCode;

		if (StringUtils.equals(taxClassificationCode, "0")) {
			ownershipCode = "ID";
		} else if (StringUtils.equals(taxClassificationCode, "1")) {
			ownershipCode = "CP";
		} else if (StringUtils.equals(taxClassificationCode, "2")) {
			ownershipCode = "CP";
		} else if (StringUtils.equals(taxClassificationCode, "3")) {
			ownershipCode = "PA";
		} else if (StringUtils.equals(taxClassificationCode, "4")) {
			ownershipCode = "TE";
		} else if (StringUtils.equals(taxClassificationCode, "5")) {
			ownershipCode = "CP";
		} else if (StringUtils.equals(taxClassificationCode, "6")) {
			ownershipCode = "CP";
		} else if (StringUtils.equals(taxClassificationCode, "7")) {
			ownershipCode = "PA";
		} else if (StringUtils.equals(taxClassificationCode, "8")) {
			ownershipCode = "PS";
		} else {
			ownershipCode = "UN";
		}

		return ownershipCode;
	}

	public String getTaxTypeCode(String tinTypeCode) {
		String taxTypeCode = "";

		if (StringUtils.equals(tinTypeCode, "1") || StringUtils.equals(tinTypeCode, "3")) {
			taxTypeCode = "FEIN";
		} else if (StringUtils.equals(tinTypeCode, "0") || StringUtils.equals(tinTypeCode, "2")) {
			taxTypeCode = "SSN";
		}

		return taxTypeCode;
	}

}
