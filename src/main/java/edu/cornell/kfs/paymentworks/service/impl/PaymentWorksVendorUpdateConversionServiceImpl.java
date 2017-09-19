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

import java.util.ArrayList;
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
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.paymentworks.PaymentWorksConstants;
import edu.cornell.kfs.paymentworks.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.paymentworks.service.PaymentWorksUtilityService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorService;
import edu.cornell.kfs.paymentworks.service.PaymentWorksVendorUpdateConversionService;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorNumberDTO;
import edu.cornell.kfs.paymentworks.xmlObjects.PaymentWorksVendorUpdatesDTO;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class PaymentWorksVendorUpdateConversionServiceImpl implements PaymentWorksVendorUpdateConversionService {

    protected PaymentWorksUtilityService paymentWorksUtilityService;
    protected VendorService vendorService;

    @Override
    public PaymentWorksVendor createPaymentWorksVendorUpdate(PaymentWorksVendorUpdatesDTO paymentWorksVendorUpdatesDTO) {

        PaymentWorksVendor paymentWorksVendor = new PaymentWorksVendor();

        Map<String, String> fieldChanges = paymentWorksUtilityService.convertFieldArrayToMap(paymentWorksVendorUpdatesDTO.getField_changes());
        Map<String, String> fieldChangesFrom = paymentWorksUtilityService.convertFieldArrayToMapFromValues(paymentWorksVendorUpdatesDTO.getField_changes());

        // Requesting company info
        if (StringUtils.equals(paymentWorksVendorUpdatesDTO.getGroup_name(), PaymentWorksConstants.VendorUpdateGroups.COMPANY)) {

            paymentWorksVendor.setRequestingCompanyName(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("Company Name (DBA)"), "requestingCompanyName"));
            paymentWorksVendor.setRequestingCompanyNameOldValue(paymentWorksUtilityService.trimFieldToMax(fieldChangesFrom.get("Company Name (DBA)"), "requestingCompanyOldValue"));
            paymentWorksVendor.setRequestingCompanyTin(fieldChanges.get("Tax ID"));
            paymentWorksVendor.setRequestingCompanyTinType(fieldChanges.get("Tax ID Type"));
            paymentWorksVendor.setRequestingCompanyTaxCountry(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("Tax Country"), "requestingCompanyTaxCountry"));
            paymentWorksVendor.setRequestingCompanyTelephone(fieldChanges.get("Telephone"));
            paymentWorksVendor.setRequestingCompanyTelephoneOldValue(fieldChangesFrom.get("Telephone"));
            paymentWorksVendor.setRequestingCompanyDesc(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("Description"), "requestingCompanyDesc"));
            paymentWorksVendor.setRequestingCompanyLegalName(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("Legal Name"), "requestingCompanyLegalName"));
            paymentWorksVendor.setRequestingCompanyDuns(fieldChanges.get("D-U-N-S"));
            paymentWorksVendor.setRequestingCompanyTaxClassificationCode(getTaxClassificationCode(fieldChanges.get("Tax Classification")));
            paymentWorksVendor.setRequestingCompanyW8W9(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("W8/W9"), "requestingCompanyW8W9"));
            paymentWorksVendor.setRequestingCompanyUrl( paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("URL"), "requestingCompanyUrl"));
        }

        // remittance address
        if (StringUtils.equals(paymentWorksVendorUpdatesDTO.getGroup_name(), PaymentWorksConstants.VendorUpdateGroups.REMIT_ADDRESS)) {

            paymentWorksVendor.setRemittanceAddressStreet1(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("Street1"), "remittanceAddressStreet1"));
            paymentWorksVendor.setRemittanceAddressStreet2(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("Street2"), "remittanceAddressStreet2"));
            paymentWorksVendor.setRemittanceAddressCity(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("City"), "remittanceAddressCity"));
            paymentWorksVendor.setRemittanceAddressState(fieldChanges.get("State"));
            paymentWorksVendor.setRemittanceAddressCountry(fieldChanges.get("Country"));
            paymentWorksVendor.setRemittanceAddressZipCode(fieldChanges.get("Zipcode"));
        }

        // corp address
        if (StringUtils.equals(paymentWorksVendorUpdatesDTO.getGroup_name(), PaymentWorksConstants.VendorUpdateGroups.CORP_ADDRESS)) {

            paymentWorksVendor.setCorpAddressStreet1(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("Street1"), "corpAddressStreet1"));
            paymentWorksVendor.setCorpAddressStreet2(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("Street2"), "corpAddressStreet2"));
            paymentWorksVendor.setCorpAddressCity(paymentWorksUtilityService.trimFieldToMax(fieldChanges.get("City"), "corpAddressCity"));
            paymentWorksVendor.setCorpAddressState(fieldChanges.get("State"));
            paymentWorksVendor.setCorpAddressCountry(fieldChanges.get("Country"));
            paymentWorksVendor.setCorpAddressZipCode(fieldChanges.get("Zipcode"));
        }

        // other
        paymentWorksVendor.setVendorRequestId(paymentWorksVendorUpdatesDTO.getId());
        paymentWorksVendor.setGroupName(paymentWorksVendorUpdatesDTO.getGroup_name());
        paymentWorksVendor.setVendorName(paymentWorksUtilityService.trimFieldToMax(paymentWorksVendorUpdatesDTO.getVendor_name(), "vendorName"));

        // vendor numbers
        if (ObjectUtils.isNotNull(paymentWorksVendorUpdatesDTO.getVendor_nums().getVendorNumbers())
                && !paymentWorksVendorUpdatesDTO.getVendor_nums().getVendorNumbers().isEmpty()) {

            StringBuffer vendorNumberStr = new StringBuffer("");

            for (PaymentWorksVendorNumberDTO vendorNumber : paymentWorksVendorUpdatesDTO.getVendor_nums().getVendorNumbers()) {

                for (String siteCode : vendorNumber.getSite_codes().getSite_codes()) {
                    vendorNumberStr.append(vendorNumber.getVendor_num());
                    vendorNumberStr.append("-");
                    vendorNumberStr.append(siteCode);
                    vendorNumberStr.append(",");
                }
            }

            if (vendorNumberStr.length() > 0) {
                paymentWorksVendor.setVendorNumberList(vendorNumberStr.toString().substring(0, vendorNumberStr.length() - 1));
            }
        }

        return paymentWorksVendor;
    }

    @Override
    public boolean duplicateFieldsOnVendor(VendorDetail vendorDetail, PaymentWorksVendor paymentWorksVendor) {
        boolean isDuplicate = false;

        // Requesting company info
        if (StringUtils.equals(paymentWorksVendor.getGroupName(), PaymentWorksConstants.VendorUpdateGroups.COMPANY)) {

            boolean taxableIndicator = true;
            boolean ownershipCodesMatch = true;
            // boolean holdCodeMatch = true;
            boolean tinTypeMatch = true;

            // check if the hold code has changed due to country changing
            if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTaxCountry())) {
                if (!StringUtils.equals(paymentWorksVendor.getRequestingCompanyTaxCountry(), "US")) {

                    // holdCodeMatch =
                    // StringUtils.equals(((VendorDetailExtension)vendorDetail.getExtension()).getHoldCode(),
                    // "NRA");
                }
            }

            // check taxable indicator, basing it on classification code and
            // ownership category code (similar to other type of business)
            if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTaxClassificationCode())) {
                taxableIndicator = isTaxable(paymentWorksVendor.getRequestingCompanyTaxClassificationCode(),
                        vendorDetail.getVendorHeader().getVendorOwnershipCategoryCode());

                ownershipCodesMatch = StringUtils.equals(vendorDetail.getVendorHeader().getVendorOwnershipCode(),
                        PaymentWorksConstants.OwnershipTaxClassification.fromTaxClassification(paymentWorksVendor.getRequestingCompanyTaxClassificationCode()).ownershipCode);
            }

            // check the tin type, translate first
            if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTinType())) {
                tinTypeMatch = StringUtils.equals(PaymentWorksConstants.TinType.fromTinCode(paymentWorksVendor.getRequestingCompanyTinType()).taxTypeCode,
                        vendorDetail.getVendorHeader().getVendorTaxTypeCode());
            }

            if (existingVendorAlias(vendorDetail, paymentWorksVendor)
                    && isEqualsIgnoreNull(paymentWorksVendor.getRequestingCompanyTin(), vendorDetail.getVendorHeader().getVendorTaxNumber())
                    && existingVendorPhoneNumber(vendorDetail, paymentWorksVendor)
                    && isEqualsIgnoreNull(paymentWorksVendor.getRequestingCompanyLegalName(), vendorDetail.getVendorName())
                    && isEqualsIgnoreNull(paymentWorksVendor.getRequestingCompanyDuns(), vendorDetail.getVendorDunsNumber())
                    && (vendorDetail.isTaxableIndicator() == taxableIndicator) &&
                    // ownershipCodesMatch && holdCodeMatch && tinTypeMatch &&
                    ownershipCodesMatch && tinTypeMatch
                    && isEqualsIgnoreNull(paymentWorksVendor.getRequestingCompanyUrl(), vendorDetail.getVendorUrlAddress())
                    && isEqualsIgnoreNull(paymentWorksVendor.getRequestingCompanyTaxCountry(), vendorDetail.getVendorHeader().getVendorCorpCitizenCode())) {

                isDuplicate = true;
            }
        }

        // remittance address
        if (StringUtils.equals(paymentWorksVendor.getGroupName(), PaymentWorksConstants.VendorUpdateGroups.REMIT_ADDRESS)) {

            VendorAddress address = getVendorService().getVendorDefaultAddress(vendorDetail.getVendorAddresses(), VendorConstants.AddressTypes.REMIT, null);

            if (ObjectUtils.isNotNull(address)) {

                boolean stateMatch;

                if (StringUtils.equalsIgnoreCase(StringUtils.defaultString(paymentWorksVendor.getRemittanceAddressCountry(), address.getVendorCountryCode()), "US")) {
                    stateMatch = isEqualsIgnoreNull(paymentWorksVendor.getRemittanceAddressState(), address.getVendorStateCode());
                } else {
                    stateMatch = isEqualsIgnoreNull(paymentWorksVendor.getRemittanceAddressState(), address.getVendorAddressInternationalProvinceName());
                }

                if (isEqualsIgnoreNull(paymentWorksVendor.getRemittanceAddressStreet1(), address.getVendorLine1Address())
                        && isEqualsIgnoreNull(paymentWorksVendor.getRemittanceAddressStreet2(), address.getVendorLine2Address())
                        && isEqualsIgnoreNull(paymentWorksVendor.getRemittanceAddressCity(), address.getVendorCityName())
                        && isEqualsIgnoreNull(paymentWorksVendor.getRemittanceAddressCountry(), address.getVendorCountryCode())
                        && isEqualsIgnoreNull(paymentWorksVendor.getRemittanceAddressZipCode(), address.getVendorZipCode())
                        && stateMatch) {
                    isDuplicate = true;
                }
            }
        }

        // corp address
        if (StringUtils.equals(paymentWorksVendor.getGroupName(), PaymentWorksConstants.VendorUpdateGroups.CORP_ADDRESS)) {

            VendorAddress address = getVendorService().getVendorDefaultAddress(vendorDetail.getVendorAddresses(),
                    VendorConstants.AddressTypes.PURCHASE_ORDER, null);

            if (ObjectUtils.isNotNull(address)) {

                boolean stateMatch;
                // boolean holdCodeMatch = true;

                if (StringUtils.isNotBlank(paymentWorksVendor.getCorpAddressState())) {
                    if (StringUtils.equalsIgnoreCase("EN", vendorDetail.getVendorHeader().getVendorOwnershipCategoryCode())
                            && !StringUtils.equalsIgnoreCase("CT", paymentWorksVendor.getCorpAddressState())) {

                        // holdCodeMatch =
                        // StringUtils.equalsIgnoreCase("A&E-TAX",
                        // ((VendorDetailExtension)vendorDetail.getExtension()).getHoldCode());
                    }
                }

                if (StringUtils.equalsIgnoreCase(StringUtils.defaultString(paymentWorksVendor.getCorpAddressCountry(), address.getVendorCountryCode()), "US")) {
                    stateMatch = isEqualsIgnoreNull(paymentWorksVendor.getCorpAddressState(), address.getVendorStateCode());
                } else {
                    stateMatch = isEqualsIgnoreNull(paymentWorksVendor.getCorpAddressState(), address.getVendorAddressInternationalProvinceName());
                }

                if (isEqualsIgnoreNull(paymentWorksVendor.getCorpAddressStreet1(), address.getVendorLine1Address())
                        && isEqualsIgnoreNull(paymentWorksVendor.getCorpAddressStreet2(), address.getVendorLine2Address())
                        && isEqualsIgnoreNull(paymentWorksVendor.getCorpAddressCity(), address.getVendorCityName())
                        && isEqualsIgnoreNull(paymentWorksVendor.getCorpAddressCountry(), address.getVendorCountryCode())
                        && isEqualsIgnoreNull(paymentWorksVendor.getCorpAddressZipCode(), address.getVendorZipCode()) &&
                        // stateMatch && holdCodeMatch){
                        stateMatch) {

                    isDuplicate = true;
                }
            }
        }

        return isDuplicate;
    }

    @Override
    public VendorDetail createVendorDetailForEdit(VendorDetail newVendorDetail, VendorDetail oldVendorDetail, PaymentWorksVendor paymentWorksVendor) {

        // Requesting company info
        if (StringUtils.equals(paymentWorksVendor.getGroupName(), PaymentWorksConstants.VendorUpdateGroups.COMPANY)) {

            newVendorDetail.setVendorAliases(getVendorAliasesForVendorUpdate(newVendorDetail, oldVendorDetail, paymentWorksVendor));
            newVendorDetail.getVendorHeader().setVendorTaxNumber(StringUtils.defaultIfEmpty(paymentWorksVendor.getRequestingCompanyTin(), newVendorDetail.getVendorHeader().getVendorTaxNumber()));
            newVendorDetail.setVendorPhoneNumbers(getVendorPhoneNumbersForVendorUpdate(newVendorDetail, oldVendorDetail, paymentWorksVendor));
            newVendorDetail.setVendorName(StringUtils.defaultIfEmpty(paymentWorksVendor.getRequestingCompanyLegalName(),newVendorDetail.getVendorName()));
            newVendorDetail.setVendorDunsNumber(StringUtils.defaultIfEmpty(paymentWorksVendor.getRequestingCompanyDuns(), newVendorDetail.getVendorDunsNumber()));

            VendorHeader vendorHeader = (VendorHeader) ObjectUtils.deepCopy(newVendorDetail.getVendorHeader());

            if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTaxClassificationCode())) {

                newVendorDetail.setTaxableIndicator(isTaxable(paymentWorksVendor.getRequestingCompanyTaxClassificationCode(),
                                vendorHeader.getVendorOwnershipCategoryCode()));

                // set ownership code
                vendorHeader.setVendorOwnershipCode(PaymentWorksConstants.OwnershipTaxClassification.fromTaxClassification(
                                paymentWorksVendor.getRequestingCompanyTaxClassificationCode()).ownershipCode);
            }

            if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTinType())) {
                vendorHeader.setVendorTaxTypeCode(PaymentWorksConstants.TinType.fromTinCode(paymentWorksVendor.getRequestingCompanyTinType()).taxTypeCode);
            }

            vendorHeader.setVendorCorpCitizenCode(StringUtils.defaultIfEmpty(paymentWorksVendor.getRequestingCompanyTaxCountry(),
                            newVendorDetail.getVendorHeader().getVendorCorpCitizenCode()));

            // set foreign indicator
            if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTaxCountry())) {
                if (StringUtils.equals(paymentWorksVendor.getRequestingCompanyTaxCountry(), "US")) {
                    vendorHeader.setVendorForeignIndicator(false);
                } else {
                    vendorHeader.setVendorForeignIndicator(true);
                }
            }

            newVendorDetail.setVendorHeader(vendorHeader);

            // set hold code to NRA if incoming country is not US
            if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTaxCountry())) {
                if (!StringUtils.equals(paymentWorksVendor.getRequestingCompanyTaxCountry(), "US")) {
                    VendorDetailExtension vde = (VendorDetailExtension) ObjectUtils.deepCopy(newVendorDetail.getExtension());
                    // vde.setHoldCode("NRA");
                    newVendorDetail.setExtension(vde);
                }
            }

            newVendorDetail.setVendorUrlAddress(StringUtils.defaultIfEmpty(paymentWorksVendor.getRequestingCompanyUrl(),
                    newVendorDetail.getVendorUrlAddress()));
        }

        // remittance address
        if (StringUtils.equals(paymentWorksVendor.getGroupName(), PaymentWorksConstants.VendorUpdateGroups.REMIT_ADDRESS)) {

            VendorAddress address = getVendorService().getVendorDefaultAddress(newVendorDetail.getVendorAddresses(), VendorConstants.AddressTypes.REMIT, null);
            VendorAddress newAddress = new VendorAddress();
            List<VendorAddress> vendorAddresses = (List<VendorAddress>) ObjectUtils.deepCopy(new ArrayList<VendorAddress>(newVendorDetail.getVendorAddresses()));

            if (ObjectUtils.isNotNull(address)) {
                // copy for new address
                newAddress = (VendorAddress) ObjectUtils.deepCopy(address);

                // inactivate address
                for (VendorAddress addr : vendorAddresses) {
                    if (addr.getVendorAddressGeneratedIdentifier().equals(address.getVendorAddressGeneratedIdentifier())) {
                        addr.setActive(false);
                        addr.setVendorDefaultAddressIndicator(false);
                    }
                }

                // clear keys
                newAddress.setVendorAddressGeneratedIdentifier(null);
                newAddress.setObjectId(null);
                newAddress.setVersionNumber(null);
            }

            // add new address
            newAddress.setNewCollectionRecord(true);
            newAddress.setActive(true);
            newAddress.setVendorDefaultAddressIndicator(true);
            newAddress.setVendorAddressTypeCode(VendorConstants.AddressTypes.REMIT);

            String addressLine2 = "";
            if (StringUtils.isNotEmpty(paymentWorksVendor.getRemittanceAddressStreet1())) {
                addressLine2 = StringUtils.defaultIfEmpty(paymentWorksVendor.getRemittanceAddressStreet2(), "");
            } else {
                addressLine2 = StringUtils.defaultIfEmpty(paymentWorksVendor.getRemittanceAddressStreet2(), newAddress.getVendorLine2Address());
            }

            newAddress.setVendorLine1Address(StringUtils.defaultIfEmpty(paymentWorksVendor.getRemittanceAddressStreet1(), newAddress.getVendorLine1Address()));
            newAddress.setVendorLine2Address(StringUtils.defaultIfEmpty(paymentWorksVendor.getRemittanceAddressStreet2(), addressLine2));
            newAddress.setVendorCityName(StringUtils.defaultIfEmpty(paymentWorksVendor.getRemittanceAddressCity(), newAddress.getVendorCityName()));
            newAddress.setVendorCountryCode(StringUtils.defaultIfEmpty(paymentWorksVendor.getRemittanceAddressCountry(), newAddress.getVendorCountryCode()));

            if (StringUtils.equalsIgnoreCase(newAddress.getVendorCountryCode(), "US")) {
                newAddress.setVendorStateCode(StringUtils.defaultIfEmpty(paymentWorksVendor.getRemittanceAddressState(), newAddress.getVendorStateCode()));
                newAddress.setVendorAddressInternationalProvinceName("");
            } else {
                newAddress.setVendorAddressInternationalProvinceName(StringUtils.defaultIfEmpty(paymentWorksVendor.getRemittanceAddressState(), newAddress.getVendorAddressInternationalProvinceName()));
                newAddress.setVendorStateCode("");
            }

            newAddress.setVendorZipCode(StringUtils.defaultIfEmpty(paymentWorksVendor.getRemittanceAddressZipCode(), newAddress.getVendorZipCode()));

            vendorAddresses.add(newAddress);
            newVendorDetail.setVendorAddresses(vendorAddresses);
        }

        // corp address
        if (StringUtils.equals(paymentWorksVendor.getGroupName(), PaymentWorksConstants.VendorUpdateGroups.CORP_ADDRESS)) {

            VendorAddress address = getVendorService().getVendorDefaultAddress(newVendorDetail.getVendorAddresses(), VendorConstants.AddressTypes.PURCHASE_ORDER, null);
            VendorAddress newAddress = new VendorAddress();
            List<VendorAddress> vendorAddresses = (List<VendorAddress>) ObjectUtils.deepCopy(new ArrayList<VendorAddress>(newVendorDetail.getVendorAddresses()));

            if (ObjectUtils.isNotNull(address)) {
                // copy for new address
                newAddress = (VendorAddress) ObjectUtils.deepCopy(address);

                // inactivate address
                for (VendorAddress addr : vendorAddresses) {
                    if (addr.getVendorAddressGeneratedIdentifier().equals(address.getVendorAddressGeneratedIdentifier())) {
                        addr.setActive(false);
                        addr.setVendorDefaultAddressIndicator(false);
                    }
                }

                // clear keys
                newAddress.setVendorAddressGeneratedIdentifier(null);
                newAddress.setObjectId(null);
                newAddress.setVersionNumber(null);
            }

            // add new address
            newAddress.setNewCollectionRecord(true);
            newAddress.setActive(true);
            newAddress.setVendorDefaultAddressIndicator(true);
            newAddress.setVendorAddressTypeCode(VendorConstants.AddressTypes.PURCHASE_ORDER);

            String addressLine2 = "";
            if (StringUtils.isNotEmpty(paymentWorksVendor.getCorpAddressStreet1())) {
                addressLine2 = StringUtils.defaultIfEmpty(paymentWorksVendor.getCorpAddressStreet2(), "");
            } else {
                addressLine2 = StringUtils.defaultIfEmpty(paymentWorksVendor.getCorpAddressStreet2(), newAddress.getVendorLine2Address());
            }

            newAddress.setVendorLine1Address(StringUtils.defaultIfEmpty(paymentWorksVendor.getCorpAddressStreet1(), newAddress.getVendorLine1Address()));
            newAddress.setVendorLine2Address(StringUtils.defaultIfEmpty(paymentWorksVendor.getCorpAddressStreet2(), addressLine2));
            newAddress.setVendorCityName(StringUtils.defaultIfEmpty(paymentWorksVendor.getCorpAddressCity(), newAddress.getVendorCityName()));
            newAddress.setVendorCountryCode(StringUtils.defaultIfEmpty(paymentWorksVendor.getCorpAddressCountry(), newAddress.getVendorCountryCode()));

            if (StringUtils.equalsIgnoreCase(newAddress.getVendorCountryCode(), "US")) {
                newAddress.setVendorStateCode(StringUtils.defaultIfEmpty(paymentWorksVendor.getCorpAddressState(), newAddress.getVendorStateCode()));
                newAddress.setVendorAddressInternationalProvinceName("");
            } else {
                newAddress.setVendorAddressInternationalProvinceName(StringUtils.defaultIfEmpty(paymentWorksVendor.getCorpAddressState(),
                                newAddress.getVendorAddressInternationalProvinceName()));
                newAddress.setVendorStateCode("");
            }

            newAddress.setVendorZipCode(StringUtils.defaultIfEmpty(paymentWorksVendor.getCorpAddressZipCode(), newAddress.getVendorZipCode()));

            // set hold code to A&E-TAX if incoming state is not CT and
            // ownership category is entertainment (EN)
            if (StringUtils.isNotBlank(paymentWorksVendor.getCorpAddressState())) {
                if (StringUtils.equalsIgnoreCase("EN", newVendorDetail.getVendorHeader().getVendorOwnershipCategoryCode())
                        && !StringUtils.equalsIgnoreCase("CT", paymentWorksVendor.getCorpAddressState())) {

                    VendorDetailExtension vde = (VendorDetailExtension) ObjectUtils.deepCopy(newVendorDetail.getExtension());
                    // vde.setHoldCode("A&E-TAX");
                    newVendorDetail.setExtension(vde);
                }
            }

            vendorAddresses.add(newAddress);
            newVendorDetail.setVendorAddresses(vendorAddresses);
        }

        return newVendorDetail;
    }

    /**
     * Updates a vendor alias list with PaymentWorks data
     *
     * @param vendorDetail
     * @param paymentWorksVendor
     * @return
     */
    protected List<VendorAlias> getVendorAliasesForVendorUpdate(VendorDetail vendorDetail, VendorDetail oldVendorDetail, PaymentWorksVendor paymentWorksVendor) {

        List<VendorAlias> aliasList = (List<VendorAlias>) ObjectUtils.deepCopy(new ArrayList<VendorAlias>(vendorDetail.getVendorAliases()));
        boolean existingValueFound = false;

        if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyName())) {

            for (VendorAlias alias : aliasList) {
                if (StringUtils.equals(alias.getVendorAliasName(), paymentWorksVendor.getRequestingCompanyName())) {
                    alias.setActive(true);
                    existingValueFound = true;
                }
            }

            if (ObjectUtils.isNull(aliasList)) {
                aliasList = new ArrayList<VendorAlias>();
            }

            if (!existingValueFound) {
                VendorAlias alias = new VendorAlias();
                alias.setVendorAliasName(paymentWorksVendor.getRequestingCompanyName());
                alias.setActive(true);
                alias.setNewCollectionRecord(true);
                aliasList.add(alias);

                // add null to old vendor detail
                if (ObjectUtils.isNotNull(oldVendorDetail)) {
                    VendorAlias aliasEmpty = new VendorAlias();
                    aliasEmpty.setNewCollectionRecord(true);
                    oldVendorDetail.getVendorAliases().add(aliasEmpty);
                }
            }
        }

        return aliasList;
    }

    /**
     * Updates a vendor phone number list with PaymentWorks data
     *
     * @param vendorDetail
     * @param paymentWorksVendor
     * @return
     */
    protected List<VendorPhoneNumber> getVendorPhoneNumbersForVendorUpdate(VendorDetail vendorDetail, VendorDetail oldVendorDetail, PaymentWorksVendor paymentWorksVendor) {

        List<VendorPhoneNumber> vendorPhoneNumbers = (List<VendorPhoneNumber>) ObjectUtils.deepCopy(new ArrayList<VendorPhoneNumber>(vendorDetail.getVendorPhoneNumbers()));
        boolean existingValueFound = false;

        if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTelephone())) {
            // deactivate old phone
            for (VendorPhoneNumber phone : vendorPhoneNumbers) {
                if (StringUtils.equals(phone.getVendorPhoneNumber(), paymentWorksVendor.getRequestingCompanyTelephoneOldValue())) {
                    phone.setActive(false);
                }

                if (StringUtils.equals(phone.getVendorPhoneNumber(), paymentWorksVendor.getRequestingCompanyTelephone())) {
                    phone.setActive(true);
                    existingValueFound = true;
                }
            }

            if (ObjectUtils.isNull(vendorPhoneNumbers)) {
                vendorPhoneNumbers = new ArrayList<VendorPhoneNumber>();
            }

            if (!existingValueFound) {
                VendorPhoneNumber phoneNumber = new VendorPhoneNumber();
                phoneNumber.setVendorPhoneNumber(paymentWorksVendor.getRequestingCompanyTelephone());
                phoneNumber.setVendorPhoneTypeCode(PaymentWorksConstants.VENDOR_PHONE_TYPE_CODE_PHONE);
                phoneNumber.setActive(true);
                phoneNumber.setNewCollectionRecord(true);
                vendorPhoneNumbers.add(phoneNumber);

                // add null to old vendor detail
                if (ObjectUtils.isNotNull(oldVendorDetail)) {
                    VendorPhoneNumber phoneNumberEmpty = new VendorPhoneNumber();
                    phoneNumberEmpty.setNewCollectionRecord(true);
                    oldVendorDetail.getVendorPhoneNumbers().add(phoneNumberEmpty);
                }
            }
        }

        return vendorPhoneNumbers;
    }

    protected boolean existingVendorAlias(VendorDetail vendorDetail, PaymentWorksVendor paymentWorksVendor) {

        List<VendorAlias> aliasList = vendorDetail.getVendorAliases();
        boolean existingValueFound = false;

        if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyName())) {
            for (VendorAlias alias : aliasList) {
                if (StringUtils.equals(alias.getVendorAliasName(), paymentWorksVendor.getRequestingCompanyName())
                        && alias.isActive()) {
                    existingValueFound = true;
                    break;
                }
            }
        } else {
            // no value so plays no role in comparison
            existingValueFound = true;
        }

        return existingValueFound;
    }

    protected boolean existingVendorPhoneNumber(VendorDetail vendorDetail, PaymentWorksVendor paymentWorksVendor) {

        List<VendorPhoneNumber> vendorPhoneNumbers = vendorDetail.getVendorPhoneNumbers();
        boolean existingValueFound = false;

        if (StringUtils.isNotBlank(paymentWorksVendor.getRequestingCompanyTelephone())) {
            for (VendorPhoneNumber phone : vendorPhoneNumbers) {
                if (StringUtils.equals(phone.getVendorPhoneNumber(), paymentWorksVendor.getRequestingCompanyTelephone())
                        && phone.isActive()) {
                    existingValueFound = true;
                    break;
                }
            }
        } else {
            // no value so plays no role in comparison
            existingValueFound = true;
        }

        return existingValueFound;
    }

    protected String getTaxClassificationCode(String taxClassificationName) {

        String taxClassificationCode = taxClassificationName;

        if (StringUtils.equals(taxClassificationName, "Individual/sole proprietor/single-member LLC")) {
            taxClassificationCode = "0";
        } else if (StringUtils.equals(taxClassificationName, "C Corporation")) {
            taxClassificationCode = "1";
        } else if (StringUtils.equals(taxClassificationName, "S Corporation")) {
            taxClassificationCode = "2";
        } else if (StringUtils.equals(taxClassificationName, "Partnership")) {
            taxClassificationCode = "3";
        } else if (StringUtils.equals(taxClassificationName, "Trust/Estate")) {
            taxClassificationCode = "4";
        } else if (StringUtils.equals(taxClassificationName, "LLC taxed as C Corporation")) {
            taxClassificationCode = "5";
        } else if (StringUtils.equals(taxClassificationName, "LLC taxed as S Corporation")) {
            taxClassificationCode = "6";
        } else if (StringUtils.equals(taxClassificationName, "LLC taxed as Partnership")) {
            taxClassificationCode = "7";
        } else if (StringUtils.equals(taxClassificationName, "Other")) {
            taxClassificationCode = "8";
        }

        return taxClassificationCode;

    }

    protected boolean isTaxable(String classificationCode, String ownershipCategoryCode) {
        boolean taxableIndicator = true;

        if ((StringUtils.equals(classificationCode, "1") || StringUtils.equals(classificationCode, "2"))
                && (StringUtils.equals(ownershipCategoryCode, "US") || StringUtils.equals(ownershipCategoryCode, "SE") || StringUtils.isBlank(ownershipCategoryCode))) {
            taxableIndicator = false;
        }

        return taxableIndicator;
    }

    /**
     * Looks at equality but returns true when first value is null
     *
     * @return
     */
    protected boolean isEqualsIgnoreNull(String compareStrNull, String compareStr2) {
        boolean match = true;

        if (StringUtils.isNotBlank(compareStrNull)) {
            match = StringUtils.equals(compareStrNull, compareStr2);
        }

        return match;
    }

    public PaymentWorksUtilityService getPaymentWorksUtilityService() {
        return paymentWorksUtilityService;
    }

    public void setPaymentWorksUtilityService(PaymentWorksUtilityService paymentWorksUtilityService) {
        this.paymentWorksUtilityService = paymentWorksUtilityService;
    }

    public VendorService getVendorService() {
        return vendorService;
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

}
