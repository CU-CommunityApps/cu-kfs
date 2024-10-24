package edu.cornell.kfs.pmw.batch.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cornell.kfs.vnd.businessobject.options.EinvoiceIndicatorValuesFinder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.PaymentMethod;
import org.kuali.kfs.sys.businessobject.State;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.SupplierDiversity;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.web.format.FormatException;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksTaxRuleDependencyService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorSupplierDiversityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorToKfsVendorDetailConversionService;
import edu.cornell.kfs.vnd.CUVendorConstants;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class PaymentWorksVendorToKfsVendorDetailConversionServiceImpl implements PaymentWorksVendorToKfsVendorDetailConversionService {
	private static final Logger LOG = LogManager.getLogger();
    
    protected ConfigurationService configurationService;
    protected DateTimeService dateTimeService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected BusinessObjectService businessObjectService;
    protected PaymentWorksVendorSupplierDiversityService paymentWorksVendorSupplierDiversityService;
    protected PaymentWorksTaxRuleDependencyService paymentWorksTaxRuleDependencyService;
            
    @Override
    public KfsVendorDataWrapper createKfsVendorDetailFromPmwVendor(PaymentWorksVendor pmwVendor,
                                                                   Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap,
                                                                   Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap) {
        KfsVendorDataWrapper kfsVendorDataWrapper = createVendorDetail(pmwVendor, paymentWorksIsoToFipsCountryMap, paymentWorksToKfsDiversityMap);
        return kfsVendorDataWrapper;
    }

    protected KfsVendorDataWrapper createVendorDetail(PaymentWorksVendor pmwVendor, 
                                                      Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap, 
                                                      Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap) {
        
        KfsVendorDataWrapper kfsVendorDataWrapper = paymentWorksTaxRuleDependencyService.buildKfsVendorDataWrapper(pmwVendor, paymentWorksIsoToFipsCountryMap);
        
        if (ObjectUtils.isNotNull(kfsVendorDataWrapper.getVendorDetail())) {
            kfsVendorDataWrapper.getVendorDetail().getVendorHeader().setVendorTypeCode(determineKfsVendorTypeCodeBasedOnPmwVendorType(pmwVendor.getVendorType()));
            kfsVendorDataWrapper.getVendorDetail().getVendorHeader().setVendorSupplierDiversities(buildVendorDiversities(pmwVendor, paymentWorksToKfsDiversityMap));
            kfsVendorDataWrapper.getVendorDetail().setVendorDunsNumber(pmwVendor.getRequestingCompanyDuns());
            kfsVendorDataWrapper.getVendorDetail().setActiveIndicator(true);
            kfsVendorDataWrapper.getVendorDetail().setVendorUrlAddress(pmwVendor.getRequestingCompanyUrl());
            kfsVendorDataWrapper.getVendorDetail().setVendorAddresses(buildVendorAddresses(pmwVendor, paymentWorksIsoToFipsCountryMap));
            kfsVendorDataWrapper.getVendorDetail().setVendorContacts(buildVendorContacts(pmwVendor));
            kfsVendorDataWrapper.getVendorDetail().setDefaultPaymentMethodCode(buildDefaultKFSPaymentMethodCode(pmwVendor, paymentWorksIsoToFipsCountryMap));
            kfsVendorDataWrapper.getVendorDetail().setDefaultPaymentMethod(buildVendorDefaultPaymentMethod(kfsVendorDataWrapper.getVendorDetail().getDefaultPaymentMethodCode()));
            kfsVendorDataWrapper.getVendorDetail().setExtension(buildVendorDetailExtension(pmwVendor));
            kfsVendorDataWrapper.getVendorDetail().setVendorParentIndicator(true);
            if (paymentWorksVendorIsPurchaseOrderVendor(pmwVendor)) {
                kfsVendorDataWrapper.getVendorDetail().setVendorPaymentTermsCode(PaymentWorksConstants.KFSVendorMaintenaceDocumentConstants.KFSPoVendorConstants.PAYMENT_TERMS_NET_60_DAYS_CODE);
                kfsVendorDataWrapper.getVendorDetail().setVendorShippingTitleCode(PaymentWorksConstants.KFSVendorMaintenaceDocumentConstants.KFSPoVendorConstants.VENDOR_SHIPPING_TITLE_DESTINATION_CODE);
                kfsVendorDataWrapper.getVendorDetail().setVendorShippingPaymentTermsCode(PaymentWorksConstants.KFSVendorMaintenaceDocumentConstants.KFSPoVendorConstants.VENDOR_SHIPPING_PAYMENT_TERMS_PREPAID_AND_ADD_CODE);
            }
            kfsVendorDataWrapper = buildRemainingVendorNotes(pmwVendor, kfsVendorDataWrapper);
            kfsVendorDataWrapper.getVendorDetail().setBoNotes(kfsVendorDataWrapper.getVendorNotes());
        }
        return kfsVendorDataWrapper;
    }
    
    private boolean paymentWorksVendorIsPurchaseOrderVendor(PaymentWorksVendor pmwVendor) {
        return StringUtils.equals(determineKfsVendorTypeCodeBasedOnPmwVendorType(pmwVendor.getVendorType()), VendorConstants.VendorTypes.PURCHASE_ORDER);
    }
    
    private List<VendorAddress> buildVendorAddresses(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        List<VendorAddress> allVendorAddresses = new ArrayList<VendorAddress>();
        allVendorAddresses.add(buildTaxAddressFromIsoCountryData(pmwVendor, paymentWorksIsoToFipsCountryMap));
        allVendorAddresses.add(buildRemitAddressFromIsoCountryData(pmwVendor, paymentWorksIsoToFipsCountryMap));
        if (paymentWorksVendorIsPurchaseOrderVendor(pmwVendor)) {
            allVendorAddresses.add(buildPurchaseOrderAddressFromFipsData(pmwVendor, paymentWorksIsoToFipsCountryMap));
        }
        return allVendorAddresses;
    }

    private VendorAddress buildTaxAddressFromIsoCountryData(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        VendorAddress taxAddress = buildBaseAddress(CUVendorConstants.CUAddressTypes.TAX, 
                                                    pmwVendor.getCorpAddressStreet1(),pmwVendor.getCorpAddressStreet2(),
                                                    pmwVendor.getCorpAddressCity(), pmwVendor.getCorpAddressZipCode(), 
                                                    paymentWorksTaxRuleDependencyService.convertIsoCountryCodeToFipsCountryCode(
                                                            pmwVendor.getCorpAddressCountry(), paymentWorksIsoToFipsCountryMap));
        taxAddress = assignStateCodeOrProvinceBasedOnFipsCountryCode(taxAddress, pmwVendor.getCorpAddressState());
        return taxAddress;
    }

    private VendorAddress buildRemitAddressFromIsoCountryData(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        VendorAddress remitAddress = buildBaseAddress(VendorConstants.AddressTypes.REMIT,
                                                      pmwVendor.getRemittanceAddressStreet1(), pmwVendor.getRemittanceAddressStreet2(), 
                                                      pmwVendor.getRemittanceAddressCity(), pmwVendor.getRemittanceAddressZipCode(), 
                                                      paymentWorksTaxRuleDependencyService.convertIsoCountryCodeToFipsCountryCode(
                                                              pmwVendor.getRemittanceAddressCountry(), paymentWorksIsoToFipsCountryMap));
        remitAddress.setVendorDefaultAddressIndicator(true);
        remitAddress = assignStateCodeOrProvinceBasedOnFipsCountryCode(remitAddress, pmwVendor.getRemittanceAddressState());
        return (remitAddress);
    }

    private VendorAddress buildPurchaseOrderAddressFromFipsData(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        String fipsCountryCode = buildPOFipsCountryCode(pmwVendor, paymentWorksIsoToFipsCountryMap);
        
        VendorAddress poAddress = buildBaseAddress(VendorConstants.AddressTypes.PURCHASE_ORDER,
                                                   pmwVendor.getPoAddress1(), pmwVendor.getPoAddress2(),
                                                   pmwVendor.getPoCity(), pmwVendor.getPoPostalCode(),
                                                   fipsCountryCode);
        poAddress = assignPoStateOrProvinceBasedOnCountryCode(poAddress, pmwVendor);
        poAddress = assignMethodOfPoTransmission(poAddress, pmwVendor);
        poAddress.setVendorAttentionName(pmwVendor.getPoAttention());
        poAddress.setVendorDefaultAddressIndicator(true);
        return (poAddress);
    }

    protected String buildPOFipsCountryCode(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        String fipsCountryCode = StringUtils.EMPTY;
        PaymentWorksPurchaseOrderCountryFipsOption option = PaymentWorksPurchaseOrderCountryFipsOption.findPaymentWorksPurchaseOrderCountryFipsOption(pmwVendor.getPoCountryUsCanadaAustraliaOther());
        if (LOG.isDebugEnabled()) {
            LOG.debug("buildPOFipsCountryCode, FIPS country code option: " + option.toString());
        }
        if (StringUtils.isNotBlank(option.fipsCountryCode)) {
            fipsCountryCode = option.fipsCountryCode;
        } else {
            try {
                fipsCountryCode = paymentWorksTaxRuleDependencyService.convertIsoCountryCodeToFipsCountryCode(
                        pmwVendor.getPoCountry(), paymentWorksIsoToFipsCountryMap);
            } catch (NullPointerException npe) {
                LOG.error("buildPOFipsCountryCode, had an error converting '" + pmwVendor.getPoCountry() + "' to a FIPS code.", npe);
                fipsCountryCode = StringUtils.EMPTY;
            }
        }
        if (StringUtils.isBlank(fipsCountryCode)) {
            LOG.error("buildPOFipsCountryCode, unable to find FIPS country code for country code " + pmwVendor.getPoCountry());
        }
        return fipsCountryCode;
    }

    private VendorAddress assignMethodOfPoTransmission(VendorAddress poAddress, PaymentWorksVendor pmwVendor) {
        CuVendorAddressExtension poAddressExtension = new CuVendorAddressExtension();

        if (StringUtils.equalsIgnoreCase(pmwVendor.getPoTransmissionMethod(), PaymentWorksConstants.PaymentWorksMethodOfPoTransmission.EMAIL.getPmwPoTransmissionMethodAsText())) {
            poAddressExtension.setPurchaseOrderTransmissionMethodCode(PaymentWorksConstants.PaymentWorksMethodOfPoTransmission.EMAIL.getKfsPoTransmissionMethodCode());
            poAddress.setVendorAddressEmailAddress(pmwVendor.getPoEmailAddress());
        }
        else if (StringUtils.equalsIgnoreCase(pmwVendor.getPoTransmissionMethod(), PaymentWorksConstants.PaymentWorksMethodOfPoTransmission.FAX.getPmwPoTransmissionMethodAsText())) {
            poAddressExtension.setPurchaseOrderTransmissionMethodCode(PaymentWorksConstants.PaymentWorksMethodOfPoTransmission.FAX.getKfsPoTransmissionMethodCode());
            setVendorFaxNumberValue(poAddress, pmwVendor.getPoFaxNumber());           
        }
        else if (StringUtils.equalsIgnoreCase(pmwVendor.getPoTransmissionMethod(), PaymentWorksConstants.PaymentWorksMethodOfPoTransmission.US_MAIL.getPmwPoTransmissionMethodAsText())) {
            poAddressExtension.setPurchaseOrderTransmissionMethodCode(PaymentWorksConstants.PaymentWorksMethodOfPoTransmission.US_MAIL.getKfsPoTransmissionMethodCode());
        }
        poAddress.setExtension(poAddressExtension);
        return poAddress;
    }
    
    private void setVendorFaxNumberValue(VendorAddress vendorAddress, String vendorFaxNumber) {
        try {
            Class type = ObjectUtils.easyGetPropertyType(vendorAddress, VendorPropertyConstants.VENDOR_FAX_NUMBER);
            ObjectUtils.setObjectProperty(vendorAddress, VendorPropertyConstants.VENDOR_FAX_NUMBER, type, vendorFaxNumber);
        } catch (FormatException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            vendorAddress.setVendorFaxNumber(null);
            LOG.error("setVendorFaxNumberValue: Vendor Fax Number cannot be set due to exception: " + e.getMessage(), e);
        }
    }

    private VendorAddress buildBaseAddress(String addressType, String line1, String line2, String city, String zip, String fipsCountryCode) {
        VendorAddress baseAddress = new VendorAddress();
        baseAddress.setVendorAddressTypeCode(addressType);
        baseAddress.setVendorCountryCode(fipsCountryCode);
        baseAddress.setVendorLine1Address(line1);
        baseAddress.setVendorLine2Address(line2);
        baseAddress.setVendorCityName(city);
        baseAddress.setVendorZipCode(zip);
        baseAddress.setActive(true);
        return baseAddress;
    }

    private VendorAddress assignPoStateOrProvinceBasedOnCountryCode(VendorAddress baseAddress, PaymentWorksVendor pmwVendor) {
        if (isUnitedStatesFipsCountryCode(baseAddress.getVendorCountryCode())){
            baseAddress = assignStateCodeOrProvinceBasedOnFipsCountryCode(baseAddress, convertFipsUsStateNameToFipsUsStateCode(pmwVendor.getPoUsState()));
        }
        else if (isCanadaFipsCountryCode(baseAddress.getVendorCountryCode())){
            baseAddress = assignStateCodeOrProvinceBasedOnFipsCountryCode(baseAddress, pmwVendor.getPoCanadianProvince());
        }
        else if (isAustraliaFipsCountryCode(baseAddress.getVendorCountryCode())){
            baseAddress = assignStateCodeOrProvinceBasedOnFipsCountryCode(baseAddress, pmwVendor.getPoAustralianProvince());
        }
        else {
            baseAddress = assignStateCodeOrProvinceBasedOnFipsCountryCode(baseAddress, pmwVendor.getPoStateProvince());
        }
        return baseAddress;
    }
    
    private VendorAddress assignStateCodeOrProvinceBasedOnFipsCountryCode(VendorAddress baseAddress, String stateProvince) {
        if (isUnitedStatesFipsCountryCode(baseAddress.getVendorCountryCode())){
            baseAddress.setVendorStateCode(stateProvince);
        }
        else {
            baseAddress.setVendorAddressInternationalProvinceName(stateProvince);
        }
        return baseAddress;
    }
    
    private String convertFipsUsStateNameToFipsUsStateCode(String fipsStateName) {
        Map<String, Object> fieldValues = new HashMap<>();
        fieldValues.put("active", true);
        fieldValues.put("countryCode", KFSConstants.COUNTRY_CODE_UNITED_STATES);

        Collection<State>  allStatesForUsFipsCountryCode = businessObjectService.findMatching(State.class, fieldValues);
        List<State> matchingStates = allStatesForUsFipsCountryCode.stream()
                                                               .filter(stateItem -> stateItem.getName().equalsIgnoreCase(fipsStateName))
                                                               .filter(stateItem -> stateItem.isActive())
                                                               .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        LOG.info("convertFipsUsStateNameToFipsUsStateCode: matchingStates = " + matchingStates.toString());
        if (matchingStates.size() == 1) {
            LOG.info("convertFipsUsStateNameToFipsUsStateCode: stateCode being returned = " + matchingStates.get(0).getCode());
            return matchingStates.get(0).getCode();
        }
        else {
            LOG.info("convertFipsUsStateNameToFipsUsStateCode: fipsStateName being returned, could not find stateCode = " + fipsStateName);
            return fipsStateName;
        }
    }

    private List<VendorContact> buildVendorContacts(PaymentWorksVendor pmwVendor) {
        List<VendorContact> vendorContacts = new ArrayList<VendorContact>();

        vendorContacts.add(buildVendorInformationFormContact(pmwVendor));
        
        if (paymentWorksVendorIsPurchaseOrderVendor(pmwVendor)) {
            if (shouldCreateContact(pmwVendor.getInsuranceContactName())) {
                vendorContacts.add(buildInsuranceContact(pmwVendor));
            }
            if (shouldCreateContact(pmwVendor.getSalesContactName())) {
                vendorContacts.add(buildSalesContact(pmwVendor));
            }
            if (shouldCreateContact(pmwVendor.getAccountsReceivableContactName())) {
                vendorContacts.add(buildAccountsReceivableContact(pmwVendor));
            }
        }
        return vendorContacts;
    }
    
    protected boolean shouldCreateContact(String contactName) {
        return StringUtils.isNotBlank(contactName);
    }
    
    private VendorContact buildVendorInformationFormContact(PaymentWorksVendor pmwVendor) {
        return buildContact(PaymentWorksConstants.KFSVendorContactTypes.VENDOR_INFORMATION_FORM,
                            PaymentWorksConstants.KFSVendorContactPhoneTypes.VENDOR_INFORMATION,
                            pmwVendor.getVendorInformationContactName(), pmwVendor.getVendorInformationEmail(),
                            pmwVendor.getVendorInformationPhoneNumber(), pmwVendor.getVendorInformationPhoneExtension());
    }

    private VendorContact buildInsuranceContact(PaymentWorksVendor pmwVendor) {
        return buildContact(PaymentWorksConstants.KFSVendorContactTypes.INSURANCE,
                            PaymentWorksConstants.KFSVendorContactPhoneTypes.INSURANCE,
                            pmwVendor.getInsuranceContactName(), pmwVendor.getInsuranceContactEmail(),
                            pmwVendor.getInsuranceContactPhoneNumber(), pmwVendor.getInsuranceContactPhoneExtension());
    }

    private VendorContact buildSalesContact(PaymentWorksVendor pmwVendor) {
        return buildContact(PaymentWorksConstants.KFSVendorContactTypes.SALES,
                            PaymentWorksConstants.KFSVendorContactPhoneTypes.SALES,
                            pmwVendor.getSalesContactName(), pmwVendor.getSalesContactEmail(),
                            pmwVendor.getSalesContactPhoneNumber(), pmwVendor.getSalesContactPhoneExtension());
    }

    private VendorContact buildAccountsReceivableContact(PaymentWorksVendor pmwVendor) {
        return buildContact(PaymentWorksConstants.KFSVendorContactTypes.ACCOUNTS_RECEIVABLE,
                            PaymentWorksConstants.KFSVendorContactPhoneTypes.ACCOUNTS_RECEIVABLE_PHONE,
                            pmwVendor.getAccountsReceivableContactName(), pmwVendor.getAccountsReceivableContactEmail(),
                            pmwVendor.getAccountsReceivableContactPhone(), pmwVendor.getAccountsReceivableContactPhoneExtension());
    }

    protected VendorContact buildContact(String contactType, String contactPhoneType, String contactName, String contactEmailAddress, String contactPhoneNumber, String contactPhoneExtension) {
        List<VendorContactPhoneNumber> vendorContactPhoneNumbers = new ArrayList<VendorContactPhoneNumber>();
        if (StringUtils.isNotBlank(contactPhoneNumber)) {
            LOG.debug("buildContact, there is a phone number, so add a VendorContactPhoneNumber");
            vendorContactPhoneNumbers.add(buildContactPhoneNumber(contactPhoneType, contactPhoneNumber, contactPhoneExtension));
        }
        VendorContact contact = new VendorContact();
        contact.setVendorContactPhoneNumbers(vendorContactPhoneNumbers);
        contact.setVendorContactTypeCode(contactType);
        contact.setVendorContactName(truncateValueToMaxLength(contactName, CUVendorConstants.MAX_VENDOR_CONTACT_NAME_LENGTH));
        contact.setVendorContactEmailAddress(contactEmailAddress);
        contact.setActive(true);
        return contact;
    }
    
    private VendorContactPhoneNumber buildContactPhoneNumber(String contactPhoneType, String contactPhoneNumber, String contactPhoneExtension) {
        VendorContactPhoneNumber vendorContactPhoneNumber = new VendorContactPhoneNumber();
        vendorContactPhoneNumber.setVendorPhoneTypeCode(contactPhoneType);
        vendorContactPhoneNumber.setVendorPhoneNumber(contactPhoneNumber);
        if (StringUtils.isNotBlank(contactPhoneExtension)) {
            vendorContactPhoneNumber.setVendorPhoneExtensionNumber(contactPhoneExtension);
        }
        vendorContactPhoneNumber.setActive(true);
        return vendorContactPhoneNumber;
    }
    
    private boolean isCanadaFipsCountryCode (String countryCode) {
        return (StringUtils.equalsIgnoreCase(countryCode, PaymentWorksConstants.FIPS_COUNTRY_CODE_CANADA));
    }

    private boolean isAustraliaFipsCountryCode (String countryCode) {
        return (StringUtils.equalsIgnoreCase(countryCode, PaymentWorksConstants.FIPS_COUNTRY_CODE_AUSTRALIA));
    }

    private boolean isUnitedStatesFipsCountryCode (String countryCode) {
        return (StringUtils.isNotBlank(countryCode) &&
                StringUtils.equalsIgnoreCase(countryCode, KFSConstants.COUNTRY_CODE_UNITED_STATES));
    }

    private String determineKfsVendorTypeCodeBasedOnPmwVendorType(String pmwVendorType) {
        return (StringUtils.equalsIgnoreCase(StringUtils.trim(pmwVendorType), PaymentWorksConstants.PaymentWorksVendorType.PURCHASE_ORDER) ? VendorConstants.VendorTypes.PURCHASE_ORDER : VendorConstants.VendorTypes.DISBURSEMENT_VOUCHER);
    }
    
    protected static String truncateValueToMaxLength(String inputValue, int maxLength) {
        if (StringUtils.length(inputValue) <= maxLength) {
            return inputValue;
        } else {
            return inputValue.substring(0, maxLength);
        }
    }

    protected static String truncateLegalFirstNameToMaximumAllowedLengthWhenFormattedWithLegalLastName(String legalLastName, String legalFirstName, String delim, int maxLength) {
        int maxAllowedLengthOfFirstName = maxLength - delim.length() - legalLastName.length();
        
        if (maxAllowedLengthOfFirstName <= 0)
            return KFSConstants.EMPTY_STRING;
        else 
            return truncateValueToMaxLength(legalFirstName, maxAllowedLengthOfFirstName);
    }
    
    protected VendorDetailExtension buildVendorDetailExtension(PaymentWorksVendor pmwVendor) {
        VendorDetailExtension vendorDetailExtension = new VendorDetailExtension();
        vendorDetailExtension.setPaymentWorksOriginatingIndicator(true);
        vendorDetailExtension.setPaymentWorksLastActivityTimestamp(dateTimeService.getCurrentTimestamp());
        vendorDetailExtension.setEinvoiceVendorIndicator(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.NONE.code);
        return vendorDetailExtension;
    }
    
    protected String buildDefaultKFSPaymentMethodCode(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        String vendorCountryCode = paymentWorksTaxRuleDependencyService.convertIsoCountryCodeToFipsCountryCode(
                pmwVendor.getRequestingCompanyTaxCountry(), paymentWorksIsoToFipsCountryMap);
        if (isUnitedStatesFipsCountryCode(vendorCountryCode)) {
            LOG.info("buildKFSPaymentMethodCode, Domestic Vendor, returning payment method code value of {}", KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK);
            return KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK;
        } else {
            if (StringUtils.equalsIgnoreCase(PaymentWorksConstants.PaymentWorksPaymentMethods.WIRE, pmwVendor.getPaymentMethod())) {
                LOG.info("buildKFSPaymentMethodCode, Foreign Vendor, returning payment method code value of {}", KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE);
                return KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE;
            } else if (StringUtils.equalsIgnoreCase(PaymentWorksConstants.PaymentWorksPaymentMethods.CHECK, pmwVendor.getPaymentMethod()) ||
                    StringUtils.equalsIgnoreCase(PaymentWorksConstants.PaymentWorksPaymentMethods.ACH, pmwVendor.getPaymentMethod())) {
                LOG.info("buildKFSPaymentMethodCode, Foreign Vendor, returning payment method code value of {}", KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK);
                return KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK;
            } else {
                throw new IllegalArgumentException("buildKFSPaymentMethodCode, Invalid PaymentWorks payment method code: " + pmwVendor.getPaymentMethod());
            }
        }
    }
    
    protected PaymentMethod buildVendorDefaultPaymentMethod(String paymentMethodCodeValue) {
        PaymentMethod paymentMethodReferenceObject= businessObjectService.findBySinglePrimaryKey(PaymentMethod.class, paymentMethodCodeValue);
        if (ObjectUtils.isNotNull(paymentMethodReferenceObject)) {
            LOG.info("buildVendorDefaultPaymentMethod, Foreign Vendor, return payment method to {}", KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK);
            return paymentMethodReferenceObject;
        } else {
            throw new IllegalArgumentException("buildVendorDefaultPaymentMethod, Could not find Payment Method object for code : " + paymentMethodCodeValue);
        }
    }
    
    private List<VendorSupplierDiversity> buildVendorDiversities(PaymentWorksVendor pmwVendor, Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap) {
        List<VendorSupplierDiversity> kfsVendorSupplierDiversities = new ArrayList<VendorSupplierDiversity>();
        if (isDiverseBusiness(pmwVendor)) {
            kfsVendorSupplierDiversities.addAll(paymentWorksVendorSupplierDiversityService.buildSupplierDiversityListFromPaymentWorksVendor(pmwVendor));
        }
        return kfsVendorSupplierDiversities;
    }
    
    protected boolean isDiverseBusiness(PaymentWorksVendor pmwVendor) {
        return pmwVendor.isDiverseBusiness() || 
                StringUtils.isNotBlank(pmwVendor.getFederalDiversityClassifications()) || 
                StringUtils.isNotBlank(pmwVendor.getStateDiversityClassifications());
    }
    
    private KfsVendorDataWrapper buildRemainingVendorNotes(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        kfsVendorDataWrapper = createInitiatorNote(pmwVendor, kfsVendorDataWrapper);
        kfsVendorDataWrapper = createVendorTypeBusinessPurposeNote(pmwVendor, kfsVendorDataWrapper);
        kfsVendorDataWrapper = createConflictOfInterestNote(pmwVendor, kfsVendorDataWrapper);
        kfsVendorDataWrapper = createComplianceScreeningNote(pmwVendor, kfsVendorDataWrapper);
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper createInitiatorNote(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        StringBuilder sbText = new StringBuilder(configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.NOTES_INITIATOR_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getInitiatorNetId());
        kfsVendorDataWrapper = paymentWorksBatchUtilityService.createNoteRecordingAnyErrors(kfsVendorDataWrapper, sbText.toString(), PaymentWorksConstants.ErrorDescriptorForBadKfsNote.INITIATOR.getNoteDescriptionString());
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper createVendorTypeBusinessPurposeNote(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        StringBuilder sbText = new StringBuilder(configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_PAYMENT_REASON_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getVendorType());
        kfsVendorDataWrapper = paymentWorksBatchUtilityService.createNoteRecordingAnyErrors(kfsVendorDataWrapper, sbText.toString(), PaymentWorksConstants.ErrorDescriptorForBadKfsNote.BUSINESS_PURPOSE.getNoteDescriptionString());
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper createComplianceScreeningNote(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        String noteText = configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_COMPLIANCE_SCREENING_MESSAGE);
        kfsVendorDataWrapper = paymentWorksBatchUtilityService.createNoteRecordingAnyErrors(kfsVendorDataWrapper, noteText, PaymentWorksConstants.ErrorDescriptorForBadKfsNote.COMPLIANCE_SCREENING.getNoteDescriptionString());
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper createConflictOfInterestNote(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        if (pmwVendor.isConflictOfInterest()) {
            StringBuilder sbText = new StringBuilder(configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_MESSAGE)).append(KFSConstants.BLANK_SPACE).append(System.lineSeparator());
            sbText.append(configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_EMPLOYEE_NAME_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getConflictOfInterestEmployeeName()).append(System.lineSeparator());
            sbText.append(configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_PHONE_NUMBER_LABEL)).append(pmwVendor.getConflictOfInterestEmployeePhoneNumber()).append(KFSConstants.BLANK_SPACE).append(System.lineSeparator());
            sbText.append(configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_RELATIONSHIP_LABEL)).append(pmwVendor.getConflictOfInterestRelationshipToEmployee()).append(KFSConstants.BLANK_SPACE).append(System.lineSeparator());
            kfsVendorDataWrapper = paymentWorksBatchUtilityService.createNoteRecordingAnyErrors(kfsVendorDataWrapper, sbText.toString(), PaymentWorksConstants.ErrorDescriptorForBadKfsNote.CONFLICT_OF_INTEREST.getNoteDescriptionString());
        }
        return kfsVendorDataWrapper;
    }
    
    protected String findPoCountryToUse(PaymentWorksVendor pmwVendor) {
        String poCountryUsCanadaAustraliaOther = pmwVendor.getPoCountryUsCanadaAustraliaOther();
        String poCountryToUse;
        String poCountry = pmwVendor.getPoCountry();
        if (StringUtils.equalsIgnoreCase(poCountryUsCanadaAustraliaOther, PaymentWorksConstants.PO_ADDRESS_COUNTRY_OTHER) ||
                StringUtils.isBlank(poCountryUsCanadaAustraliaOther)) {
            poCountryToUse = poCountry;
        } else {
            poCountryToUse = poCountryUsCanadaAustraliaOther;
        }
        return poCountryToUse;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setPaymentWorksVendorSupplierDiversityService(PaymentWorksVendorSupplierDiversityService paymentWorksVendorSupplierDiversityService) {
        this.paymentWorksVendorSupplierDiversityService = paymentWorksVendorSupplierDiversityService;
    }

    public void setPaymentWorksTaxRuleDependencyService(PaymentWorksTaxRuleDependencyService paymentWorksTaxRuleDependencyService) {
        this.paymentWorksTaxRuleDependencyService = paymentWorksTaxRuleDependencyService;
    }

}
