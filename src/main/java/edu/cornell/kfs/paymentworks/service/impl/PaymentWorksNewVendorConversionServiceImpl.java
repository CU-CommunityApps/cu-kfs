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
package edu.cornell.kfs.paymentworks.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorAlias;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorPhoneNumber;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.rice.core.web.format.FormatException;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksFieldMapping;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksNewVendorConversionService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksBankAccountDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksBankAddressDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksCorpAddressDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksNewVendorDetailDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksRemittanceAddressDTO;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class PaymentWorksNewVendorConversionServiceImpl implements PaymentWorksNewVendorConversionService {
	
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PaymentWorksNewVendorConversionServiceImpl.class);
	
	protected SequenceAccessorService sequenceAccessorService;
	protected BusinessObjectService businessObjectService;
	protected VendorService vendorService;
	protected PaymentWorksUtilityService paymentWorksUtilityService;
	
	@Override
	public VendorDetail createVendorDetail(PaymentWorksVendor paymentWorksVendor) {
		VendorDetail vendorDetail = new VendorDetail();
		vendorDetail.setVendorHeader(createVendorHeader(paymentWorksVendor));
		vendorDetail.setVendorAddresses(buildVendorAddresses(paymentWorksVendor));

		vendorDetail.setVendorParentIndicator(true);
		vendorDetail.setVendorName(paymentWorksVendor.getRequestingCompanyLegalName());
		vendorDetail.setVendorPaymentTermsCode(PaymentWorksConstants.VENDOR_PAYMENT_TERMS_CODE_DEFAULT);
		
		vendorDetail.setTaxableIndicator(isVendorTaxable(paymentWorksVendor));

		vendorDetail.setExtension(buildVendorDetailExtension());

		vendorDetail.setVendorDunsNumber(paymentWorksVendor.getRequestingCompanyDuns());
		vendorDetail.setVendorUrlAddress(paymentWorksVendor.getRequestingCompanyUrl());
		vendorDetail.setActiveIndicator(true);

		vendorDetail.setVendorAliases(buldVendorAliasList(paymentWorksVendor));

		if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTelephone())) {
			vendorDetail.setVendorPhoneNumbers(buildVendorPhoneNumberList(paymentWorksVendor.getRequestingCompanyTelephone()));
		}

		return vendorDetail;
	}
	
	protected VendorHeader createVendorHeader(PaymentWorksVendor paymentWorksVendor) {
		VendorHeader vendorHeader = new VendorHeader();
		vendorHeader.setVendorTypeCode(KFSConstants.FinancialDocumentTypeCodes.PURCHASE_ORDER);
		vendorHeader.setVendorForeignIndicator(!isUnitedStatesVendor(paymentWorksVendor));
		vendorHeader.setVendorTaxNumber(paymentWorksVendor.getRequestingCompanyTin());
		vendorHeader.setVendorTaxTypeCode(PaymentWorksConstants.TinType.fromTinCode(
				paymentWorksVendor.getRequestingCompanyTinType()).taxTypeCode);
		vendorHeader.setVendorOwnershipCode(PaymentWorksConstants.OwnershipTaxClassification.fromTaxClassification(
				paymentWorksVendor.getRequestingCompanyTaxClassificationCode()).ownershipCode);
		vendorHeader.setVendorCorpCitizenCode(paymentWorksVendor.getRequestingCompanyTaxCountry());
		return vendorHeader;
	}
	
	protected List<VendorAddress> buildVendorAddresses(PaymentWorksVendor paymentWorksVendor) {
		List<VendorAddress> vendorAddresses = new ArrayList<VendorAddress>();
		vendorAddresses.add(createCorpAddress(paymentWorksVendor));
		vendorAddresses.add(createRemittanceAddress(paymentWorksVendor));
		return vendorAddresses;
	}
	
	protected VendorAddress createCorpAddress(PaymentWorksVendor paymentWorksVendor) {

		VendorAddress vendorAddress = new VendorAddress();

		vendorAddress.setVendorAddressTypeCode(VendorConstants.AddressTypes.PURCHASE_ORDER);
		vendorAddress.setVendorAttentionName("");
		vendorAddress.setVendorBusinessToBusinessUrlAddress("");
		vendorAddress.setVendorLine1Address(paymentWorksVendor.getCorpAddressStreet1());
		vendorAddress.setVendorLine2Address(paymentWorksVendor.getCorpAddressStreet2());
		vendorAddress.setVendorCityName(paymentWorksVendor.getCorpAddressCity());
		vendorAddress.setVendorZipCode(paymentWorksVendor.getCorpAddressZipCode());
		vendorAddress.setVendorCountryCode(paymentWorksVendor.getCorpAddressCountry());
		
		if (StringUtils.equalsIgnoreCase(paymentWorksVendor.getCorpAddressCountry(), "US")) {
			vendorAddress.setVendorStateCode(paymentWorksVendor.getCorpAddressState());
		} else {
			vendorAddress.setVendorAddressInternationalProvinceName(paymentWorksVendor.getCorpAddressState());
		}
		
		
		if (StringUtils.isNotBlank(paymentWorksVendor.getPoFaxNumber())) {
			vendorAddress.setVendorFaxNumber(paymentWorksVendor.getPoFaxNumber());
		}
		
		vendorAddress.setVendorDefaultAddressIndicator(true);
		vendorAddress.setActive(true);
		vendorAddress.setExtension(buildCuVendorAddressExtension(vendorAddress));
		return vendorAddress;
	}
	
	protected CuVendorAddressExtension buildCuVendorAddressExtension(VendorAddress address) {
		CuVendorAddressExtension extension = new CuVendorAddressExtension();
		if (StringUtils.isNotBlank(address.getVendorFaxNumber())) {
			extension.setPurchaseOrderTransmissionMethodCode(PurapConstants.POTransmissionMethods.FAX);
		} else if (StringUtils.isNotBlank(address.getVendorAddressEmailAddress())) {
			extension.setPurchaseOrderTransmissionMethodCode(CUPurapConstants.POTransmissionMethods.EMAIL);
		} else if (isValidManualTransmission(address)) {
			extension.setPurchaseOrderTransmissionMethodCode(CUPurapConstants.POTransmissionMethods.MANUAL);
		} else {
			LOG.error("Unable to calculate the purchase order transmission method code.");
		}
		return extension;
	}
	
	protected boolean isValidManualTransmission(VendorAddress address) {
		return StringUtils.isNotBlank(address.getVendorCountryCode())
				&& StringUtils.isNotBlank(address.getVendorCountryCode())
				&& StringUtils.isNotBlank(address.getVendorStateCode())
				&& StringUtils.isNotBlank(address.getVendorZipCode())
				&& StringUtils.isNotBlank(address.getVendorLine1Address())
				&& StringUtils.isNotBlank(address.getVendorCityName());
	}
	
	protected VendorAddress createRemittanceAddress(PaymentWorksVendor paymentWorksVendor) {
		VendorAddress vendorAddress = new VendorAddress();

		vendorAddress.setVendorAddressTypeCode(VendorConstants.AddressTypes.REMIT);
		vendorAddress.setVendorAttentionName(StringUtils.EMPTY);
		vendorAddress.setVendorBusinessToBusinessUrlAddress(StringUtils.EMPTY);
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
	
	protected boolean isVendorTaxable(PaymentWorksVendor paymentWorksVendor) {
		boolean isNonTaxable = (StringUtils.equals(paymentWorksVendor.getRequestingCompanyTaxClassificationCode(), "1")
				|| StringUtils.equals(paymentWorksVendor.getRequestingCompanyTaxClassificationCode(), "2"))
				&& StringUtils.equals(paymentWorksVendor.getVendorType(), "Other");
		return !isNonTaxable;
	}
	
	protected VendorDetailExtension buildVendorDetailExtension() {
		VendorDetailExtension vendorDetailExtension = new VendorDetailExtension();
		vendorDetailExtension.setDefaultB2BPaymentMethodCode(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK);
		return vendorDetailExtension;
	}
	
	protected List<VendorAlias> buldVendorAliasList(PaymentWorksVendor paymentWorksVendor) {
		VendorAlias vendorAlias = new VendorAlias();
		vendorAlias.setVendorAliasName(paymentWorksVendor.getRequestingCompanyName());
		vendorAlias.setActive(true);
		List<VendorAlias> vendorAliasList = new ArrayList<VendorAlias>();
		vendorAliasList.add(vendorAlias);
		return vendorAliasList;
	}

	protected List<VendorPhoneNumber> buildVendorPhoneNumberList(String phoneNumber) {
		VendorPhoneNumber vendorPhoneNumber = new VendorPhoneNumber();
		vendorPhoneNumber.setVendorPhoneNumber(phoneNumber);
		vendorPhoneNumber.setVendorPhoneTypeCode(PaymentWorksConstants.VENDOR_PHONE_TYPE_CODE_PHONE);
		vendorPhoneNumber.setActive(true);

		List<VendorPhoneNumber> vendorPhoneNumberList = new ArrayList<VendorPhoneNumber>();
		vendorPhoneNumberList.add(vendorPhoneNumber);
		return vendorPhoneNumberList;
	}

	protected boolean isUnitedStatesVendor(PaymentWorksVendor paymentWorksVendor) {
		return StringUtils.equals(paymentWorksVendor.getRequestingCompanyTaxCountry(), "US");
	}
	
	@Override
	public PaymentWorksVendor createPaymentWorksVendor(VendorDetail vendorDetail, String documentNumber) {
		PaymentWorksVendor paymentWorksVendor = new PaymentWorksVendor();

		String sequence = getSequenceAccessorService().getNextAvailableSequenceNumber(PaymentWorksConstants.PAYMENT_WORKS_VENDOR_SEQUENCE_NAME).toString();
		paymentWorksVendor.setVendorRequestId(PaymentWorksConstants.VENDOR_REQUEST_ID_KFS_TEMP_ID_STARTER + sequence);
		
		paymentWorksVendor.setDocumentNumber(documentNumber);
		paymentWorksVendor.setVendorDetailAssignedIdentifier(vendorDetail.getVendorDetailAssignedIdentifier());
		paymentWorksVendor.setVendorHeaderGeneratedIdentifier(vendorDetail.getVendorHeaderGeneratedIdentifier());
		paymentWorksVendor.setRequestingCompanyLegalName(vendorDetail.getVendorName());
		paymentWorksVendor.setRequestingCompanyTin(vendorDetail.getVendorHeader().getVendorTaxNumber());

		setPOAddressOnPaymentWorksVendor(vendorDetail, paymentWorksVendor);

		setRemitAddressOnPaymentWorksVendor(vendorDetail, paymentWorksVendor);

		return paymentWorksVendor;
	}

	protected void setRemitAddressOnPaymentWorksVendor(VendorDetail vendorDetail,
			PaymentWorksVendor paymentWorksVendor) {
		VendorAddress remitAddress = getVendorService().getVendorDefaultAddress(
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
	}

	protected void setPOAddressOnPaymentWorksVendor(VendorDetail vendorDetail, PaymentWorksVendor paymentWorksVendor) {
		VendorAddress poAddress = getVendorService().getVendorDefaultAddress(
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
	}

	@Override
	public PaymentWorksVendor createPaymentWorksVendor(PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO) {
		PaymentWorksVendor paymentWorksNewVendor = new PaymentWorksVendor();
		paymentWorksNewVendor.setVendorRequestId(paymentWorksNewVendorDetailDTO.getId());
		populateNewVendorRequestingCompanyAttributes(paymentWorksNewVendor, paymentWorksNewVendorDetailDTO);
		populateNewVendorRemittanceAddressAttributes(paymentWorksNewVendor, paymentWorksNewVendorDetailDTO);
		populateNewVendorCorporateAddressAttributes(paymentWorksNewVendor, paymentWorksNewVendorDetailDTO);
		extractCustomFields(paymentWorksNewVendorDetailDTO, paymentWorksNewVendor);
		return paymentWorksNewVendor;
	}

	private void populateNewVendorRequestingCompanyAttributes(PaymentWorksVendor paymentWorksNewVendor, PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO) {
		paymentWorksNewVendor.setRequestingCompanyId(paymentWorksNewVendorDetailDTO.getRequesting_company().getId());
		paymentWorksNewVendor.setRequestingCompanyLegalName(paymentWorksNewVendorDetailDTO.getRequesting_company().getLegal_name());
		paymentWorksNewVendor.setRequestingCompanyDesc(paymentWorksNewVendorDetailDTO.getRequesting_company().getDesc());
		paymentWorksNewVendor.setRequestingCompanyName(paymentWorksNewVendorDetailDTO.getRequesting_company().getName());
		paymentWorksNewVendor.setRequestingCompanyLegalLastName(paymentWorksNewVendorDetailDTO.getRequesting_company().getLegal_last_name());
		paymentWorksNewVendor.setRequestingCompanyLegalFirstName(paymentWorksNewVendorDetailDTO.getRequesting_company().getLegal_first_name());
		paymentWorksNewVendor.setRequestingCompanyUrl(paymentWorksNewVendorDetailDTO.getRequesting_company().getUrl());
		paymentWorksNewVendor.setRequestingCompanyTin(paymentWorksNewVendorDetailDTO.getRequesting_company().getTin());
		paymentWorksNewVendor.setRequestingCompanyTinType(paymentWorksNewVendorDetailDTO.getRequesting_company().getTin_type());
		paymentWorksNewVendor.setRequestingCompanyTaxCountry(paymentWorksNewVendorDetailDTO.getRequesting_company().getTax_country());
		paymentWorksNewVendor.setRequestingCompanyW8W9(paymentWorksNewVendorDetailDTO.getRequesting_company().getW8_w9());
		paymentWorksNewVendor.setRequestingCompanyTelephone(paymentWorksNewVendorDetailDTO.getRequesting_company().getTelephone());
		paymentWorksNewVendor.setRequestingCompanyDuns(paymentWorksNewVendorDetailDTO.getRequesting_company().getDuns());
		paymentWorksNewVendor.setRequestingCompanyCorporateEmail(paymentWorksNewVendorDetailDTO.getRequesting_company().getCorporate_email());
		paymentWorksNewVendor.setRequestingCompanyTaxClassificationName(paymentWorksNewVendorDetailDTO.getRequesting_company().getTax_classification().getName());
		paymentWorksNewVendor.setRequestingCompanyTaxClassificationCode(paymentWorksNewVendorDetailDTO.getRequesting_company().getTax_classification().getCode());
	}

	private void populateNewVendorRemittanceAddressAttributes(PaymentWorksVendor paymentWorksNewVendor, PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO) {
        if (singleRemittanceAddressExists(paymentWorksNewVendorDetailDTO)) {
            PaymentWorksRemittanceAddressDTO paymentWorksRemittanceAddressDTO = paymentWorksNewVendorDetailDTO.getRequesting_company().getRemittance_addresses().getRemittance_address().get(0);

            paymentWorksNewVendor.setRemittanceAddressId(paymentWorksRemittanceAddressDTO.getId());
            paymentWorksNewVendor.setRemittanceAddressName(paymentWorksRemittanceAddressDTO.getName());
            paymentWorksNewVendor.setRemittanceAddressStreet1(paymentWorksRemittanceAddressDTO.getStreet1());
            paymentWorksNewVendor.setRemittanceAddressStreet2(paymentWorksRemittanceAddressDTO.getStreet2());
            paymentWorksNewVendor.setRemittanceAddressCity(paymentWorksRemittanceAddressDTO.getCity());
            paymentWorksNewVendor.setRemittanceAddressState(paymentWorksRemittanceAddressDTO.getState());
            paymentWorksNewVendor.setRemittanceAddressCountry(paymentWorksRemittanceAddressDTO.getCountry());
            paymentWorksNewVendor.setRemittanceAddressZipCode(paymentWorksRemittanceAddressDTO.getZipcode());
            paymentWorksNewVendor.setRemittanceAddressValidated(paymentWorksRemittanceAddressDTO.getValidated());

            populateNewVendorBankAccountAttributes(paymentWorksNewVendor, paymentWorksRemittanceAddressDTO);
        }
	}

	private void populateNewVendorBankAccountAttributes(PaymentWorksVendor paymentWorksNewVendor, PaymentWorksRemittanceAddressDTO paymentWorksRemittanceAddressDTO) {
        if (bankAccountDataExists(paymentWorksRemittanceAddressDTO)) {
            PaymentWorksBankAccountDTO paymentWorksBankAccountDTO = paymentWorksRemittanceAddressDTO.getBank_acct();

            paymentWorksNewVendor.setBankAcctId(paymentWorksBankAccountDTO.getId());
            paymentWorksNewVendor.setBankAcctBankName(paymentWorksBankAccountDTO.getBank_name());
            paymentWorksNewVendor.setBankAcctBankAccountNumber(paymentWorksBankAccountDTO.getBank_acct_num());
            paymentWorksNewVendor.setBankAcctBankValidationFile(paymentWorksBankAccountDTO.getValidation_file());
            paymentWorksNewVendor.setBankAcctAchEmail(paymentWorksBankAccountDTO.getAch_email());
            paymentWorksNewVendor.setBankAcctRoutingNumber(paymentWorksBankAccountDTO.getRouting_num());
            paymentWorksNewVendor.setBankAcctType(paymentWorksBankAccountDTO.getBank_acct_type());
            paymentWorksNewVendor.setBankAcctAuthorized(paymentWorksBankAccountDTO.getAuthorized());
            paymentWorksNewVendor.setBankAcctCompanyId(paymentWorksBankAccountDTO.getAcct_company());
            paymentWorksNewVendor.setBankAcctSwiftCode(paymentWorksBankAccountDTO.getSwift_code());
            paymentWorksNewVendor.setBankAcctNameOnAccount(paymentWorksBankAccountDTO.getName_on_acct());

            populateNewVendorBankAddressAttributes(paymentWorksNewVendor, paymentWorksBankAccountDTO);
        }
	}

	private void populateNewVendorBankAddressAttributes(PaymentWorksVendor paymentWorksNewVendor, PaymentWorksBankAccountDTO paymentWorksBankAccountDTO) {
        if (bankAddressExists(paymentWorksBankAccountDTO)) {
            PaymentWorksBankAddressDTO paymentWorksBankAddressDTO = paymentWorksBankAccountDTO.getBank_address();

            paymentWorksNewVendor.setBankAddressId(paymentWorksBankAddressDTO.getId());
            paymentWorksNewVendor.setBankAddressName(paymentWorksBankAddressDTO.getName());
            paymentWorksNewVendor.setBankAddressStreet1(paymentWorksBankAddressDTO.getStreet1());
            paymentWorksNewVendor.setBankAddressStreet2(paymentWorksBankAddressDTO.getStreet2());
            paymentWorksNewVendor.setBankAddressCity(paymentWorksBankAddressDTO.getCity());
            paymentWorksNewVendor.setBankAddressState(paymentWorksBankAddressDTO.getState());
            paymentWorksNewVendor.setBankAddressCountry(paymentWorksBankAddressDTO.getCountry());
            paymentWorksNewVendor.setBankAddressZipCode(paymentWorksBankAddressDTO.getZipcode());
            paymentWorksNewVendor.setBankAddressValidated(paymentWorksBankAddressDTO.getValidated());
        }
	}

	private void populateNewVendorCorporateAddressAttributes(PaymentWorksVendor paymentWorksNewVendor, PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO) {
        if (corporateAddressExists(paymentWorksNewVendorDetailDTO)) {
            PaymentWorksCorpAddressDTO corpAddress = paymentWorksNewVendorDetailDTO.getRequesting_company().getCorp_address();

            paymentWorksNewVendor.setCorpAddressId(corpAddress.getId());
            paymentWorksNewVendor.setCorpAddressName(corpAddress.getName());
            paymentWorksNewVendor.setCorpAddressStreet1(corpAddress.getStreet1());
            paymentWorksNewVendor.setCorpAddressStreet2(corpAddress.getStreet2());
            paymentWorksNewVendor.setCorpAddressCity(corpAddress.getCity());
            paymentWorksNewVendor.setCorpAddressState(corpAddress.getState());
            paymentWorksNewVendor.setCorpAddressCountry(corpAddress.getCountry());
            paymentWorksNewVendor.setCorpAddressZipCode(corpAddress.getZipcode());
            paymentWorksNewVendor.setCorpAddressValidated(corpAddress.getValidated());
        }
	}

    private boolean singleRemittanceAddressExists(PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO) {
        if (ObjectUtils.isNotNull(paymentWorksNewVendorDetailDTO.getRequesting_company().getRemittance_addresses()) &&
            ObjectUtils.isNotNull(paymentWorksNewVendorDetailDTO.getRequesting_company().getRemittance_addresses().getRemittance_address()) &&
            paymentWorksNewVendorDetailDTO.getRequesting_company().getRemittance_addresses().getRemittance_address().size() == 1) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean corporateAddressExists(PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO) {
        if (ObjectUtils.isNotNull(paymentWorksNewVendorDetailDTO.getRequesting_company().getCorp_address())) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean bankAccountDataExists(PaymentWorksRemittanceAddressDTO paymentWorksRemittanceAddressDTO) {
        if (ObjectUtils.isNotNull(paymentWorksRemittanceAddressDTO.getBank_acct())) {
            return true;
        }
        else {
            return false;
        }
    }

    private boolean bankAddressExists(PaymentWorksBankAccountDTO paymentWorksBankAccountDTO) {
        if (ObjectUtils.isNotNull(paymentWorksBankAccountDTO.getBank_address())) {
            return true;
        }
        else {
            return false;
        }
    }

	protected void extractCustomFields(PaymentWorksNewVendorDetailDTO paymentWorksNewVendorDetailDTO, PaymentWorksVendor paymentWorksNewVendor) {
		Map<String, String> customFieldsMap = getPaymentWorksUtilityService().convertFieldArrayToMap(paymentWorksNewVendorDetailDTO.getCustom_fields());
		for (String payamentWorkCustomFieldName : customFieldsMap.keySet()) {
			String customFieldValueFromPaymentWorks = customFieldsMap.get(payamentWorkCustomFieldName);
			
			PaymentWorksFieldMapping fieldMapping = findPaymentWorksFieldMapping(payamentWorkCustomFieldName);
			
			if (ObjectUtils.isNotNull(fieldMapping)) {
				Object propertyValueForSetter = convertStringValueToObjectForPropertySetting(customFieldsMap.get(payamentWorkCustomFieldName));
				if (ObjectUtils.isNotNull(propertyValueForSetter)) {
					try {
						ObjectUtils.setObjectProperty(paymentWorksNewVendor, fieldMapping.getKfsFieldName(), propertyValueForSetter);
						if (LOG.isDebugEnabled()) {
							LOG.debug("extractCustomFields setting KFS field " + fieldMapping.getKfsFieldName() +
							          "' for PaymentWorks field '" + payamentWorkCustomFieldName +
							          " and is of object type " + propertyValueForSetter.getClass());
						}
					} catch (FormatException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
						paymentWorksNewVendor.setCustomFieldConversionErrors(true);
						LOG.error("extractCustomFields Unable to set KFS field '" + fieldMapping.getKfsFieldName() +
						          "' for PaymentWorks field '" + payamentWorkCustomFieldName +
						          "'  Due to the error: " +  e.getMessage(), e);
					}
				}
			} else {
				paymentWorksNewVendor.setCustomFieldConversionErrors(true);
				LOG.error("extractCustomFields, Unable to find a KFS field for the PaymentWorks field of '" + payamentWorkCustomFieldName + "'");
			}
		}
	}

	protected PaymentWorksFieldMapping findPaymentWorksFieldMapping(String payamentWorkCustomFieldName) {
		Map fieldValues = new HashMap();
		fieldValues.put(PaymentWorksConstants.PaymentWorksFieldMappingDatabaseFieldNames.PAYMENT_WORKS_FIELD_LABEL, StringUtils.trim(payamentWorkCustomFieldName));
		Collection mappings = getBusinessObjectService().findMatching(PaymentWorksFieldMapping.class, fieldValues);
		if (!mappings.isEmpty()) {
			return (PaymentWorksFieldMapping)mappings.iterator().next();
		}
		return null;
	}
	
	protected Object convertStringValueToObjectForPropertySetting(String value) {
		if (StringUtils.equalsIgnoreCase(value, "YES") || StringUtils.equalsIgnoreCase(value, "NO")) {
			return StringUtils.equalsIgnoreCase(value, "YES");
		} else if (StringUtils.isBlank(value)) {
			return null;
		} else {
			return value;
		}
	}
	
	@Override
	public PaymentWorksVendor createPaymentWorksVendorFromDetail(PaymentWorksVendor vendor) {

		VendorDetail vendorDetail = getVendorService().getVendorDetail(
				vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier());
		PaymentWorksVendor newVendor = null;

		if (ObjectUtils.isNotNull(vendorDetail)) {
			newVendor = createPaymentWorksVendor(vendorDetail, vendor.getDocumentNumber());

			newVendor.setVendorRequestId(vendor.getVendorRequestId());
			newVendor.setObjectId(vendor.getObjectId());
			newVendor.setVersionNumber(vendor.getVersionNumber());
		}

		return newVendor;
	}

	public SequenceAccessorService getSequenceAccessorService() {
		return sequenceAccessorService;
	}

	public void setSequenceAccessorService(SequenceAccessorService sequenceAccessorService) {
		this.sequenceAccessorService = sequenceAccessorService;
	}

	public VendorService getVendorService() {
		return vendorService;
	}

	public void setVendorService(VendorService vendorService) {
		this.vendorService = vendorService;
	}

	public BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}

	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

	public PaymentWorksUtilityService getPaymentWorksUtilityService() {
		return paymentWorksUtilityService;
	}

	public void setPaymentWorksUtilityService(PaymentWorksUtilityService paymentWorksUtilityService) {
		this.paymentWorksUtilityService = paymentWorksUtilityService;
	}

}
