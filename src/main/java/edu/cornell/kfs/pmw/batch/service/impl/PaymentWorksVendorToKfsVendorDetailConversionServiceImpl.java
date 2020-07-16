package edu.cornell.kfs.pmw.batch.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.State;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.SupplierDiversity;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.businessobject.VendorSupplierDiversity;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.core.web.format.FormatException;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksFormModeService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksTaxRuleDependencyService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorSupplierDiversityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksVendorToKfsVendorDetailConversionService;
import edu.cornell.kfs.vnd.CUVendorConstants;
import edu.cornell.kfs.vnd.businessobject.CuVendorAddressExtension;
import edu.cornell.kfs.vnd.businessobject.CuVendorSupplierDiversityExtension;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

public class PaymentWorksVendorToKfsVendorDetailConversionServiceImpl implements PaymentWorksVendorToKfsVendorDetailConversionService {
	private static final Logger LOG = LogManager.getLogger(PaymentWorksVendorToKfsVendorDetailConversionServiceImpl.class);
    
    protected ConfigurationService configurationService;
    protected DateTimeService dateTimeService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected BusinessObjectService businessObjectService;
    protected PaymentWorksFormModeService paymentWorksFormModeService;
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
        
        KfsVendorDataWrapper kfsVendorDataWrapper = null;
        if (paymentWorksFormModeService.shouldUseLegacyFormProcessingMode()) {
            kfsVendorDataWrapper = populateTaxRuleDependentAttributes(pmwVendor, paymentWorksIsoToFipsCountryMap);
        } else if (paymentWorksFormModeService.shouldUseForeignFormProcessingMode()) {
            kfsVendorDataWrapper = paymentWorksTaxRuleDependencyService.populateTaxRuleDependentAttributes(pmwVendor, paymentWorksIsoToFipsCountryMap);
        }
        if (ObjectUtils.isNotNull(kfsVendorDataWrapper.getVendorDetail())) {
            kfsVendorDataWrapper.getVendorDetail().getVendorHeader().setVendorTypeCode(determineKfsVendorTypeCodeBasedOnPmwVendorType(pmwVendor.getVendorType()));
            kfsVendorDataWrapper.getVendorDetail().getVendorHeader().setVendorSupplierDiversities(buildVendorDiversities(pmwVendor, paymentWorksToKfsDiversityMap));
            kfsVendorDataWrapper.getVendorDetail().setVendorDunsNumber(pmwVendor.getRequestingCompanyDuns());
            if (paymentWorksFormModeService.shouldUseLegacyFormProcessingMode()) {
                kfsVendorDataWrapper.getVendorDetail().setVendorCreditCardIndicator(new Boolean(pmwVendor.isAcceptCreditCards()));
            }
            kfsVendorDataWrapper.getVendorDetail().setActiveIndicator(true);
            kfsVendorDataWrapper.getVendorDetail().setVendorUrlAddress(pmwVendor.getRequestingCompanyUrl());
            kfsVendorDataWrapper.getVendorDetail().setVendorAddresses(buildVendorAddresses(pmwVendor, paymentWorksIsoToFipsCountryMap));
            kfsVendorDataWrapper.getVendorDetail().setVendorContacts(buildVendorContacts(pmwVendor));
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
    
    private KfsVendorDataWrapper populateTaxRuleDependentAttributes(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        KfsVendorDataWrapper kfsVendorDataWrapper = new KfsVendorDataWrapper();

        //Foreign PO Vendors could have US tax but a Foreign PO address could have been entered, thus if-statement needed.
        //Foreign Vendors would have foreign tax (either for vendor type of DV or PO) with all of those cases being dealt with in case-statement.
        if (paymentWorksVendorIsPurchaseOrderVendor(pmwVendor)
            && isPurchaseOrderCountryNotUnitedStatesFipsCountryCodeOption(pmwVendor)) {
            kfsVendorDataWrapper = populateDataForTaxRuleForeignVendorProcessingNotAutomaticYet(kfsVendorDataWrapper);
        } else {
            switch (determineTaxRuleToUseForDataPopulation(pmwVendor, convertIsoCountryCodeToFipsCountryCode(pmwVendor.getRequestingCompanyTaxCountry(), paymentWorksIsoToFipsCountryMap))) {
                case PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_US_SSN:
                {    kfsVendorDataWrapper = populateDataForTaxRuleIndividualUsSsn(kfsVendorDataWrapper, pmwVendor, paymentWorksIsoToFipsCountryMap);
                }
                break;

                case PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_US_EIN:
                {    kfsVendorDataWrapper = populateDataForTaxRuleIndividualUsEin(kfsVendorDataWrapper, pmwVendor, paymentWorksIsoToFipsCountryMap);
                }
                break;

                case PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_US:
                {    kfsVendorDataWrapper = populateDataForTaxRuleNotIndividualUs(kfsVendorDataWrapper, pmwVendor, paymentWorksIsoToFipsCountryMap);
                }
                break;

                case PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_INDIVIDUAL:
                {    kfsVendorDataWrapper = populateDataForTaxRuleForeignVendorProcessingNotAutomaticYet(kfsVendorDataWrapper);
                }
                break;

                case PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_OTHER:
                {    kfsVendorDataWrapper = populateDataForTaxRuleForeignVendorProcessingNotAutomaticYet(kfsVendorDataWrapper);
                }
                break;

                case PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_INDIVIDUAL:
                {    kfsVendorDataWrapper = populateDataForTaxRuleForeignVendorProcessingNotAutomaticYet(kfsVendorDataWrapper);
                }
                break;

                case PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_OTHER:
                {    kfsVendorDataWrapper = populateDataForTaxRuleForeignVendorProcessingNotAutomaticYet(kfsVendorDataWrapper);
                }
                break;

                case PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_NOT_US_EIN:
                {    kfsVendorDataWrapper = populateDataForTaxRuleForeignVendorProcessingNotAutomaticYet(kfsVendorDataWrapper);
                }
                break;

                case PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_NOT_US_FOREIGN:
                {    kfsVendorDataWrapper = populateDataForTaxRuleForeignVendorProcessingNotAutomaticYet(kfsVendorDataWrapper);
                }
                break;

                case PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.COULD_NOT_DETERMINE_TAX_RULE_TO_USE:
                default:
                {    kfsVendorDataWrapper = populateDataForTaxRuleCouldNotDetermineTaxRuleToUse(kfsVendorDataWrapper);
                }
                break;
            }
        }
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper populateDataForTaxRuleIndividualUsSsn(KfsVendorDataWrapper kfsVendorDataWrapper, PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        VendorHeader vendorHeader = new VendorHeader();
        vendorHeader.setVendorForeignIndicator(new Boolean(false));
        vendorHeader.setVendorTaxNumber(pmwVendor.getRequestingCompanyTin());
        vendorHeader.setVendorTaxTypeCode(PaymentWorksConstants.PaymentWorksTinType.SSN.getKfsTaxTypeCodeAsString());
        vendorHeader.setVendorOwnershipCode(PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.getTranslationToKfsOwnershipTypeCode());
        vendorHeader.setVendorCorpCitizenCode(convertIsoCountryCodeToFipsCountryCode(pmwVendor.getRequestingCompanyTaxCountry(), paymentWorksIsoToFipsCountryMap)); 
        kfsVendorDataWrapper.getVendorDetail().setVendorHeader(vendorHeader);
        kfsVendorDataWrapper = populateW9Attributes(kfsVendorDataWrapper, pmwVendor);
        kfsVendorDataWrapper = populateFirstLastLegalName(pmwVendor, kfsVendorDataWrapper);
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper populateDataForTaxRuleIndividualUsEin(KfsVendorDataWrapper kfsVendorDataWrapper, PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        VendorHeader vendorHeader = new VendorHeader();
        vendorHeader.setVendorForeignIndicator(new Boolean(false));
        vendorHeader.setVendorTaxNumber(pmwVendor.getRequestingCompanyTin());
        vendorHeader.setVendorTaxTypeCode(PaymentWorksConstants.PaymentWorksTinType.FEIN.getKfsTaxTypeCodeAsString());
        vendorHeader.setVendorOwnershipCode(PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.getTranslationToKfsOwnershipTypeCode());
        vendorHeader.setVendorCorpCitizenCode(convertIsoCountryCodeToFipsCountryCode(pmwVendor.getRequestingCompanyTaxCountry(), paymentWorksIsoToFipsCountryMap));
        kfsVendorDataWrapper.getVendorDetail().setVendorHeader(vendorHeader);
        kfsVendorDataWrapper = populateW9Attributes(kfsVendorDataWrapper, pmwVendor);
        kfsVendorDataWrapper = populateBusinessLegalName(pmwVendor, kfsVendorDataWrapper);
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper populateDataForTaxRuleNotIndividualUs(KfsVendorDataWrapper kfsVendorDataWrapper, PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        VendorHeader vendorHeader = new VendorHeader();
        vendorHeader.setVendorForeignIndicator(new Boolean(false));
        vendorHeader.setVendorTaxNumber(pmwVendor.getRequestingCompanyTin());
        vendorHeader.setVendorTaxTypeCode(PaymentWorksConstants.PaymentWorksTinType.FEIN.getKfsTaxTypeCodeAsString());
        vendorHeader.setVendorOwnershipCode(determineKfsOwnershipTypeCodeFromPmwTaxClassificationCode(pmwVendor));
        vendorHeader.setVendorCorpCitizenCode(convertIsoCountryCodeToFipsCountryCode(pmwVendor.getRequestingCompanyTaxCountry(), paymentWorksIsoToFipsCountryMap));
        kfsVendorDataWrapper.getVendorDetail().setVendorHeader(vendorHeader);
        kfsVendorDataWrapper = populateW9Attributes(kfsVendorDataWrapper, pmwVendor);
        kfsVendorDataWrapper = populateBusinessLegalName(pmwVendor, kfsVendorDataWrapper);
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper populateDataForTaxRuleCouldNotDetermineTaxRuleToUse(KfsVendorDataWrapper kfsVendorDataWrapper) {
        kfsVendorDataWrapper.getErrorMessages().add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.COULD_NOT_DETERMINE_TAX_BUSINESS_RULE_TO_USE));
        kfsVendorDataWrapper.setVendorDetail(null);
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper populateDataForTaxRuleForeignVendorProcessingNotAutomaticYet(KfsVendorDataWrapper kfsVendorDataWrapper) {
        kfsVendorDataWrapper.getErrorMessages().add(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.FOREIGN_VENDOR_PROCESSING_NOT_AUTOMATIC_YET));
        kfsVendorDataWrapper.setVendorDetail(null);
        return kfsVendorDataWrapper;
    }
    
    private String convertIsoCountryCodeToFipsCountryCode(String isoCountryCode, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        return (paymentWorksIsoToFipsCountryMap.get(isoCountryCode).get(0)).getFipsCountryCode();
    }

    private List<VendorAddress> buildVendorAddresses(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        List<VendorAddress> allVendorAddresses = new ArrayList<VendorAddress>();
        allVendorAddresses.add(buildTaxAddressFromIsoCountryData(pmwVendor, paymentWorksIsoToFipsCountryMap));
        allVendorAddresses.add(buildRemitAddressFromIsoCountryData(pmwVendor, paymentWorksIsoToFipsCountryMap));
        if (paymentWorksVendorIsPurchaseOrderVendor(pmwVendor)) {
            allVendorAddresses.add(buildPurchaseOrderAddressFromFipsData(pmwVendor));
        }
        return allVendorAddresses;
    }

    private VendorAddress buildTaxAddressFromIsoCountryData(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        VendorAddress taxAddress = buildBaseAddress(CUVendorConstants.CUAddressTypes.TAX, 
                                                    pmwVendor.getCorpAddressStreet1(),pmwVendor.getCorpAddressStreet2(),
                                                    pmwVendor.getCorpAddressCity(), pmwVendor.getCorpAddressZipCode(), 
                                                    convertIsoCountryCodeToFipsCountryCode(pmwVendor.getCorpAddressCountry(), paymentWorksIsoToFipsCountryMap));
        taxAddress = assignStateCodeOrProvinceBasedOnFipsCountryCode(taxAddress, pmwVendor.getCorpAddressState());
        return taxAddress;
    }

    private VendorAddress buildRemitAddressFromIsoCountryData(PaymentWorksVendor pmwVendor, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        VendorAddress remitAddress = buildBaseAddress(VendorConstants.AddressTypes.REMIT,
                                                      pmwVendor.getRemittanceAddressStreet1(), pmwVendor.getRemittanceAddressStreet2(), 
                                                      pmwVendor.getRemittanceAddressCity(), pmwVendor.getRemittanceAddressZipCode(), 
                                                      convertIsoCountryCodeToFipsCountryCode(pmwVendor.getRemittanceAddressCountry(), paymentWorksIsoToFipsCountryMap));
        remitAddress.setVendorDefaultAddressIndicator(true);
        remitAddress = assignStateCodeOrProvinceBasedOnFipsCountryCode(remitAddress, pmwVendor.getRemittanceAddressState());
        return (remitAddress);
    }

    private String convertFipsPoCountryOptionToFipsCountryCode(String poCountryOption) {
        if (StringUtils.isBlank(poCountryOption)) {
            return KFSConstants.EMPTY_STRING;
        }
        else if (StringUtils.equalsIgnoreCase(poCountryOption, PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.getPmwCountryOptionAsString())) {
            return PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.getFipsCountryCode();
        }
        else if (StringUtils.equalsIgnoreCase(poCountryOption, PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.getPmwCountryOptionAsString())) {
            return PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.getFipsCountryCode();
        }
        else if (StringUtils.equalsIgnoreCase(poCountryOption, PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.AUSTRALIA.getPmwCountryOptionAsString())) {
            return PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.AUSTRALIA.getFipsCountryCode();
        }
        else if (StringUtils.equalsIgnoreCase(poCountryOption, PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.OTHER.getPmwCountryOptionAsString())) {
            return PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.OTHER.getFipsCountryCode();
        }
        return KFSConstants.EMPTY_STRING;
    }

    private VendorAddress buildPurchaseOrderAddressFromFipsData(PaymentWorksVendor pmwVendor) {
        VendorAddress poAddress = buildBaseAddress(VendorConstants.AddressTypes.PURCHASE_ORDER,
                                                   pmwVendor.getPoAddress1(), pmwVendor.getPoAddress2(),
                                                   pmwVendor.getPoCity(), pmwVendor.getPoPostalCode(),
                                                   convertFipsPoCountryOptionToFipsCountryCode(findPoCountryToUse(pmwVendor)));
        poAddress = assignPoStateOrProvinceBasedOnCountryCode(poAddress, pmwVendor);
        poAddress = assignMethodOfPoTransmission(poAddress, pmwVendor);
        poAddress.setVendorAttentionName(pmwVendor.getPoAttention());
        poAddress.setVendorDefaultAddressIndicator(true);
        return (poAddress);
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
            
            if (paymentWorksFormModeService.shouldUseLegacyFormProcessingMode() && pmwVendor.isInvoicing()) {
                vendorContacts.add(buildEinvoicingContact(pmwVendor));
            }
        }
        return vendorContacts;
    }
    
    protected boolean shouldCreateContact(String contactName) {
        return paymentWorksFormModeService.shouldUseLegacyFormProcessingMode() || StringUtils.isNotBlank(contactName);
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

    private VendorContact buildEinvoicingContact(PaymentWorksVendor pmwVendor) {
        return buildContact(PaymentWorksConstants.KFSVendorContactTypes.E_INVOICING,
                            PaymentWorksConstants.KFSVendorContactPhoneTypes.E_INVOICING,
                            pmwVendor.geteInvoiceContactName(), pmwVendor.geteInvoiceEmail(),
                            pmwVendor.geteInvoiceContactPhoneNumber(), pmwVendor.geteInvoicePhoneExtension());
    }

    private VendorContact buildContact(String contactType, String contactPhoneType, String contactName, String contactEmailAddress, String contactPhoneNumber, String contactPhoneExtension) {
        List<VendorContactPhoneNumber> vendorContactPhoneNumbers = new ArrayList<VendorContactPhoneNumber>();
        vendorContactPhoneNumbers.add(buildContactPhoneNumber(contactPhoneType, contactPhoneNumber, contactPhoneExtension));
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
    
    private int determineTaxRuleToUseForDataPopulation(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        if (isIndividualUsSsn(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_US_SSN;
        }
        else if (isIndividualUsEin(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_US_EIN;
        }
        else if (isNotIndividualUs(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_US;
        }
        else if (isIndividualNotUsSsnOrItinTaxClassificationIndividual(pmwVendor, pmwVendorFipsTaxCountryCode)){
            return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_INDIVIDUAL;
        }
        else if (isIndividualNotUsSsnOrItinTaxClassificationOther(pmwVendor, pmwVendorFipsTaxCountryCode)) {
           return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_OTHER;
        }
        else if (isIndividualNotUsForeignTaxClassificationIndividual(pmwVendor, pmwVendorFipsTaxCountryCode)) {
           return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_INDIVIDUAL;
        }
        else if (isIndividualNotUsForeignTaxClassificationOther(pmwVendor, pmwVendorFipsTaxCountryCode)) {
           return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_OTHER;
        }
        else if (isNotIndividualNotUsEin(pmwVendor, pmwVendorFipsTaxCountryCode)) {
           return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_NOT_US_EIN;
        }
        else if (isNotIndividualNotUsForeign(pmwVendor, pmwVendorFipsTaxCountryCode)) {
          return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_NOT_US_FOREIGN;
        }
        return PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.COULD_NOT_DETERMINE_TAX_RULE_TO_USE;
    }
    
    private boolean isIndividualUsSsn(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && 
                isUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode) &&
                isTinTypeSsn(pmwVendor.getRequestingCompanyTinType()));
    }
    
    private boolean isIndividualUsEin(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && 
                isUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode) &&
                isTinTypeEin(pmwVendor.getRequestingCompanyTinType()));
    }
    
    private boolean isNotIndividualUs(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isNotPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && 
                isUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode));
    }
    
    private boolean isIndividualNotUsSsnOrItinTaxClassificationIndividual(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && 
                isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode) &&
                (isTinTypeSsn(pmwVendor.getRequestingCompanyTinType()) | isTinTypeItin(pmwVendor.getRequestingCompanyTinType())) &&
                isTaxClassificationIndividualSolePropriatorSingleMemberLlc(pmwVendor.getRequestingCompanyTaxClassificationCode()));
    }
    
    private boolean isIndividualNotUsSsnOrItinTaxClassificationOther(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && 
                isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode) &&
                (isTinTypeSsn(pmwVendor.getRequestingCompanyTinType()) | isTinTypeItin(pmwVendor.getRequestingCompanyTinType())) &&
                isTaxClassificationOther(pmwVendor.getRequestingCompanyTaxClassificationCode()));
    }
    
    private boolean isIndividualNotUsForeignTaxClassificationIndividual(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && 
                isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode) &&
                isTinTypeForeignTin(pmwVendor.getRequestingCompanyTinType()) &&
                isTaxClassificationIndividualSolePropriatorSingleMemberLlc(pmwVendor.getRequestingCompanyTaxClassificationCode()));
    }
    
    private boolean isIndividualNotUsForeignTaxClassificationOther(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && 
                isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode) &&
                isTinTypeForeignTin(pmwVendor.getRequestingCompanyTinType()) &&
                isTaxClassificationOther(pmwVendor.getRequestingCompanyTaxClassificationCode()));
    }
    
    private boolean isNotIndividualNotUsEin(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isNotPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && 
                isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode) &&
                isTinTypeEin(pmwVendor.getRequestingCompanyTinType()));
    }
    
    private boolean isNotIndividualNotUsForeign(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return (isNotPmwVendorIndividualSolePropriatorSingleMemberLlc(pmwVendor) && 
                isNotUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode) &&
                isTinTypeForeignTin(pmwVendor.getRequestingCompanyTinType()));
    }

    /**
     * PMW does not return the value for the yes/no radio button "For tax purposes are you an individual, sole proprietor or single-member LLC?".
     * This radio button yes/no value must be interpreted from the tax classification construct sent back in the requesting company construct on the vendor.
     *    Yes ==> Tax Classification will be 0="individual, sole proprietor or single-member LLC",
     *    No ==> Tax Classification is any other value.
     */
    private boolean isPmwVendorIndividualSolePropriatorSingleMemberLlc(PaymentWorksVendor pmwVendor) {
        return (ObjectUtils.isNotNull(pmwVendor.getRequestingCompanyTaxClassificationCode()) && 
                pmwVendor.getRequestingCompanyTaxClassificationCode().intValue() == PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR) ? true : false;
    }
    
    private boolean isNotPmwVendorIndividualSolePropriatorSingleMemberLlc(PaymentWorksVendor pmwVendor) {
        return (ObjectUtils.isNotNull(pmwVendor.getRequestingCompanyTaxClassificationCode()) && 
               !(pmwVendor.getRequestingCompanyTaxClassificationCode().intValue() == PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR)) ? true : false;
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
    
    private boolean isNotUnitedStatesFipsCountryCode (String countryCode) {
        return (StringUtils.isNotBlank(countryCode) &&
                !StringUtils.equalsIgnoreCase(countryCode, KFSConstants.COUNTRY_CODE_UNITED_STATES));
    }
    
    private boolean isPurchaseOrderCountryNotUnitedStatesFipsCountryCodeOption(PaymentWorksVendor pmwVendor) {
        String poCountryToUse = findPoCountryToUse(pmwVendor);
        return (StringUtils.isNotBlank(poCountryToUse)
                && !StringUtils.equalsIgnoreCase(poCountryToUse, PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.getPmwCountryOptionAsString()));
    }

    private boolean isTinTypeSsn(String tinTypeCode) {
        return (StringUtils.isNotBlank(tinTypeCode) &&
                StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.SSN.getPmwCodeAsString()));
    }
    
    private boolean isTinTypeEin(String tinTypeCode) {
        return (StringUtils.isNotBlank(tinTypeCode) &&
                StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.FEIN.getPmwCodeAsString()));
    }
    
    private boolean isTinTypeItin(String tinTypeCode) {
        return (StringUtils.isNotBlank(tinTypeCode) &&
                StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.ITIN.getPmwCodeAsString()));
    }
    
    private boolean isTinTypeForeignTin(String tinTypeCode) {
        return (StringUtils.isNotBlank(tinTypeCode) &&
                StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.FOREIGN_TIN.getPmwCodeAsString()));
    }
    
    private boolean isTaxClassificationIndividualSolePropriatorSingleMemberLlc(Integer taxClassificationCode) {
        return (ObjectUtils.isNotNull(taxClassificationCode) &&
                taxClassificationCode.intValue() == PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
    }
    
    private boolean isTaxClassificationOther(Integer taxClassificationCode) {
        return (ObjectUtils.isNotNull(taxClassificationCode) &&
                taxClassificationCode.intValue() == PaymentWorksConstants.OTHER_TAX_CLASSIFICATION_INDICATOR);
    }

    private String determineKfsVendorTypeCodeBasedOnPmwVendorType(String pmwVendorType) {
        return (StringUtils.equalsIgnoreCase(StringUtils.trim(pmwVendorType), PaymentWorksConstants.PaymentWorksVendorType.PURCHASE_ORDER) ? VendorConstants.VendorTypes.PURCHASE_ORDER : VendorConstants.VendorTypes.DISBURSEMENT_VOUCHER);
    }

    private String determineKfsOwnershipTypeCodeFromPmwTaxClassificationCode(PaymentWorksVendor pmwVendor) {
        String kfsOwnsershipTypeCode = KFSConstants.EMPTY_STRING;
        
        switch (pmwVendor.getRequestingCompanyTaxClassificationCode().intValue()) {
            case PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR : 
                kfsOwnsershipTypeCode = PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.getTranslationToKfsOwnershipTypeCode();
                break;
            case PaymentWorksConstants.C_CORPORATION_TAX_CLASSIFICATION_INDICATOR:
                kfsOwnsershipTypeCode = PaymentWorksConstants.PaymentWorksTaxClassification.C_CORPORATION.getTranslationToKfsOwnershipTypeCode();
                break;
            case PaymentWorksConstants.S_CORPORATION_TAX_CLASSIFICATION_INDICATOR :
                kfsOwnsershipTypeCode = PaymentWorksConstants.PaymentWorksTaxClassification.S_CORPORATION.getTranslationToKfsOwnershipTypeCode();
                break;
            case PaymentWorksConstants.PARTNERSHIP_TAX_CLASSIFICATION_INDICATOR : 
                kfsOwnsershipTypeCode = PaymentWorksConstants.PaymentWorksTaxClassification.PARTNERSHIP.getTranslationToKfsOwnershipTypeCode();
                break;
            case PaymentWorksConstants.TRUST_ESTATE_TAX_CLASSIFICATION_INDICATOR : 
                kfsOwnsershipTypeCode = PaymentWorksConstants.PaymentWorksTaxClassification.TRUST_ESTATE.getTranslationToKfsOwnershipTypeCode();
                break;
            case PaymentWorksConstants.LLC_TAXED_AS_C_CORPORATION_TAX_CLASSIFICATION_INDICATOR : 
                kfsOwnsershipTypeCode = PaymentWorksConstants.PaymentWorksTaxClassification.LLC_TAXED_AS_C_CORPORATION.getTranslationToKfsOwnershipTypeCode();
                break;
            case PaymentWorksConstants.LLC_TAXED_AS_S_CORPORATION_TAX_CLASSIFICATION_INDICATOR : 
                kfsOwnsershipTypeCode = PaymentWorksConstants.PaymentWorksTaxClassification.LLC_TAXED_AS_S_CORPORATION.getTranslationToKfsOwnershipTypeCode();
                break;
            case PaymentWorksConstants.LLC_TAXED_AS_PARTNERSHIP_TAX_CLASSIFICATION_INDICATOR: 
                kfsOwnsershipTypeCode = PaymentWorksConstants.PaymentWorksTaxClassification.LLC_TAXED_AS_PARTNERSHIP.getTranslationToKfsOwnershipTypeCode();
                break;
            case PaymentWorksConstants.OTHER_TAX_CLASSIFICATION_INDICATOR: 
                kfsOwnsershipTypeCode = PaymentWorksConstants.PaymentWorksTaxClassification.OTHER.getTranslationToKfsOwnershipTypeCode();
                break;
            default: break;
        }
        return kfsOwnsershipTypeCode;
    }

    private KfsVendorDataWrapper populateBusinessLegalName(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        kfsVendorDataWrapper.getVendorDetail().setVendorName(truncateValueToMaxLength(pmwVendor.getRequestingCompanyLegalName(), VendorConstants.MAX_VENDOR_NAME_LENGTH));
        kfsVendorDataWrapper.getVendorDetail().setVendorFirstLastNameIndicator(false);
        kfsVendorDataWrapper.getVendorDetail().setVendorFirstName(KFSConstants.EMPTY_STRING);
        kfsVendorDataWrapper.getVendorDetail().setVendorLastName(KFSConstants.EMPTY_STRING);
        return kfsVendorDataWrapper;
    }

    private KfsVendorDataWrapper populateFirstLastLegalName(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        kfsVendorDataWrapper.getVendorDetail().setVendorFirstLastNameIndicator(true);
        String vendorLastName = truncateValueToMaxLength(pmwVendor.getRequestingCompanyLegalLastName(), VendorConstants.MAX_VENDOR_NAME_LENGTH);
        kfsVendorDataWrapper.getVendorDetail().setVendorLastName(vendorLastName);
        kfsVendorDataWrapper.getVendorDetail().setVendorFirstName(truncateLegalFirstNameToMaximumAllowedLengthWhenFormattedWithLegalLastName(vendorLastName, pmwVendor.getRequestingCompanyLegalFirstName(), VendorConstants.NAME_DELIM, VendorConstants.MAX_VENDOR_NAME_LENGTH));
        return kfsVendorDataWrapper;
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

    private KfsVendorDataWrapper populateW9Attributes(KfsVendorDataWrapper kfsVendorDataWrapper, PaymentWorksVendor pmwVendor) {
        kfsVendorDataWrapper.getVendorDetail().getVendorHeader().setVendorW9ReceivedIndicator(new Boolean(true));
        kfsVendorDataWrapper.getVendorDetail().getVendorHeader().setVendorW9SignedDate(getDateTimeService().getCurrentSqlDate());
        kfsVendorDataWrapper = getPaymentWorksBatchUtilityService().createNoteRecordingAnyErrors(kfsVendorDataWrapper, 
                getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_W9_URL_EXISTS_MESSAGE), 
                PaymentWorksConstants.ErrorDescriptorForBadKfsNote.W9.getNoteDescriptionString());
        return kfsVendorDataWrapper;
    }
    
    private VendorDetailExtension buildVendorDetailExtension(PaymentWorksVendor pmwVendor) {
        VendorDetailExtension vendorDetailExtension = new VendorDetailExtension();
        vendorDetailExtension.setDefaultB2BPaymentMethodCode(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK);
        vendorDetailExtension.setPaymentWorksOriginatingIndicator(true);
        vendorDetailExtension.setPaymentWorksLastActivityTimestamp(getDateTimeService().getCurrentTimestamp());
        return vendorDetailExtension;
    }
    
    private List<VendorSupplierDiversity> buildVendorDiversities(PaymentWorksVendor pmwVendor, Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap) {
        List<VendorSupplierDiversity> kfsVendorSupplierDiversities = new ArrayList<VendorSupplierDiversity>();
        if (pmwVendor.isDiverseBusiness()) {
            if (paymentWorksFormModeService.shouldUseLegacyFormProcessingMode()) {
                List<VendorSupplierDiversity> kfsVendorSupplierDiversitiesForCheckBoxes = new ArrayList<VendorSupplierDiversity>();
                kfsVendorSupplierDiversitiesForCheckBoxes = buildVendorDiversitiesFromPmwFormCheckboxes(pmwVendor, paymentWorksToKfsDiversityMap, kfsVendorSupplierDiversitiesForCheckBoxes);
                if (!kfsVendorSupplierDiversitiesForCheckBoxes.isEmpty()){
                    kfsVendorSupplierDiversities.addAll(kfsVendorSupplierDiversitiesForCheckBoxes);
                }
                List<VendorSupplierDiversity> kfsVendorSupplierDiversitiesForDropDowns = new ArrayList<VendorSupplierDiversity>();
                kfsVendorSupplierDiversitiesForDropDowns = buildVendorDiversitiesFromPmwFormDropDownLists(pmwVendor, paymentWorksToKfsDiversityMap, kfsVendorSupplierDiversitiesForDropDowns);
                if (!kfsVendorSupplierDiversitiesForDropDowns.isEmpty()){
                    kfsVendorSupplierDiversities.addAll(kfsVendorSupplierDiversitiesForDropDowns);
                }
            } else if (paymentWorksFormModeService.shouldUseForeignFormProcessingMode()) {
                kfsVendorSupplierDiversities.addAll(paymentWorksVendorSupplierDiversityService.buildSuppplierDivsersityListFromPaymentWorksVendor(pmwVendor));
            }
        }
        return kfsVendorSupplierDiversities;
    }
    
    private List<VendorSupplierDiversity> buildVendorDiversitiesFromPmwFormDropDownLists(PaymentWorksVendor pmwVendor, Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap, List<VendorSupplierDiversity> kfsVendorSupplierDiversities) {
        VendorSupplierDiversity minority = createVendorSupplierDiversityForDropDownData(pmwVendor.getMinorityStatus(), 
                pmwVendor.getMbeCertificationExpirationDate(), paymentWorksToKfsDiversityMap);
        if (ObjectUtils.isNotNull(minority)) {
            kfsVendorSupplierDiversities.add(minority);
        }
        
        VendorSupplierDiversity womanOwned = createVendorSupplierDiversityForDropDownData(pmwVendor.getWomanOwned(), 
                pmwVendor.getWbeCertificationExpirationDate(), paymentWorksToKfsDiversityMap);
        if (ObjectUtils.isNotNull(womanOwned)) {
            kfsVendorSupplierDiversities.add(womanOwned);
        }
        
        VendorSupplierDiversity veteran = createVendorSupplierDiversityForDropDownData(pmwVendor.getDisabledVeteran(), 
                pmwVendor.getVeteranCertificationExpirationDate(), paymentWorksToKfsDiversityMap);
        if (ObjectUtils.isNotNull(veteran)) {
            kfsVendorSupplierDiversities.add(veteran);
        }
        return kfsVendorSupplierDiversities;
    }
    
    private List<VendorSupplierDiversity> buildVendorDiversitiesFromNewPmwFormDropDownLists(PaymentWorksVendor pmwVendor, Map<String, 
            SupplierDiversity> paymentWorksToKfsDiversityMap) {
        //This should be implemented using the fields on the new PMW form.
        LOG.info("buildVendorDiversitiesFromPmwFormDropDownLists, " + PaymentWorksConstants.FOREIGN_FORM_PROCESSING_NOT_IMPLEMENTED_LOG_MESSAGE);
        List<VendorSupplierDiversity> supplierDiversities = new ArrayList<VendorSupplierDiversity>();
        return supplierDiversities;
    }

    private VendorSupplierDiversity createVendorSupplierDiversityForDropDownData (String pmwDiversityStatusDescription, String pmwDiversityCertifiedExpirationDate, Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap) {
        VendorSupplierDiversity kfsVendorVendorSupplierDiversity = null;
        if (ObjectUtils.isNotNull(pmwDiversityStatusDescription) && paymentWorksToKfsDiversityMap.containsKey(pmwDiversityStatusDescription)) {
            SupplierDiversity supplierDiversityFromMap = paymentWorksToKfsDiversityMap.get(pmwDiversityStatusDescription);
            java.sql.Date expirationDate = null;
            if (StringUtils.containsIgnoreCase(pmwDiversityStatusDescription, PaymentWorksConstants.DIVERSITY_EXPIRATION_DATE_CERTIFIED) && (ObjectUtils.isNotNull(pmwDiversityCertifiedExpirationDate))) {
                try {
                    SimpleDateFormat userEnteredDateFormat = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT);
                    java.util.Date userEnteredDate = userEnteredDateFormat.parse(pmwDiversityCertifiedExpirationDate);
                    expirationDate = new java.sql.Date(userEnteredDate.getTime());
                }
                catch (ParseException pe) {
                    LOG.info("createVendorSupplierDiversityForDropDownData: ParseException caught while attempting to create VendorSupplierDiversity object for PaymentWorks diveristy " +
                              pmwDiversityStatusDescription + " with PaymentWorks user entered certified expiration date value of " + pmwDiversityCertifiedExpirationDate +
                              ". Setting diversity expiration date to one year from today.");
                    expirationDate = addOneYearToDate(getDateTimeService().getCurrentDate());
                }
            }
            else {
                expirationDate = addOneYearToDate(getDateTimeService().getCurrentDate());
            }
            kfsVendorVendorSupplierDiversity = buildVendorSupplierDiversity(supplierDiversityFromMap.getVendorSupplierDiversityCode(), expirationDate);
        }
        return kfsVendorVendorSupplierDiversity;
    }
    
    private List<VendorSupplierDiversity> buildVendorDiversitiesFromPmwFormCheckboxes(PaymentWorksVendor pmwVendor, Map<String, SupplierDiversity> paymentWorksToKfsDiversityMap, List<VendorSupplierDiversity> kfsVendorSupplierDiversities) {
        if (StringUtils.isNotBlank(pmwVendor.getDiversityClassifications())) {
            ArrayList<String> pmwDiversities = new ArrayList<String>(Arrays.asList(pmwVendor.getDiversityClassifications().split("\\s*,\\s*")));
            if (!pmwDiversities.isEmpty()) {
                 for (String pmwDiversity : pmwDiversities) {
                     if (paymentWorksToKfsDiversityMap.containsKey(pmwDiversity)) {
                         SupplierDiversity supplierDiversityFromMap = paymentWorksToKfsDiversityMap.get(pmwDiversity);
                         kfsVendorSupplierDiversities.add(buildVendorSupplierDiversity(supplierDiversityFromMap.
                                 getVendorSupplierDiversityCode(), addOneYearToDate(getDateTimeService().getCurrentDate())));
                     } else {
                         LOG.info("buildVendorDiversities:: PaymentWorks Vendor : " + pmwVendor.getRequestingCompanyLegalName() + 
                                  "  Diversity Value : " + pmwDiversity + " does not have corresponding KFS SupplierDiversity defined."); 
                     }
                 }
            }
        }
        return kfsVendorSupplierDiversities;
    }
    
    private VendorSupplierDiversity buildVendorSupplierDiversity(String vendorSupplierDiversityCode, java.sql.Date vendorSupplierDiversityExpirationDate) {
        VendorSupplierDiversity vendorSupplierDiversity = new VendorSupplierDiversity();
        vendorSupplierDiversity.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
        vendorSupplierDiversity.setActive(true);
        CuVendorSupplierDiversityExtension diversityExtension = new CuVendorSupplierDiversityExtension();
        diversityExtension.setVendorSupplierDiversityCode(vendorSupplierDiversityCode);
        diversityExtension.setVendorSupplierDiversityExpirationDate(vendorSupplierDiversityExpirationDate);
        vendorSupplierDiversity.setExtension(diversityExtension);
        return vendorSupplierDiversity;
    }
    
    protected java.sql.Date addOneYearToDate(java.util.Date inDate) {
        GregorianCalendar calendarDateWithYearAdded = new GregorianCalendar();
        calendarDateWithYearAdded.clear();

        calendarDateWithYearAdded.setTimeInMillis(inDate.getTime());
        calendarDateWithYearAdded.add(GregorianCalendar.YEAR, 1);
        java.util.Date yearAddedToUtilDateType = new java.util.Date(calendarDateWithYearAdded.getTimeInMillis());
        return new java.sql.Date(yearAddedToUtilDateType.getTime());
    }
    
    private KfsVendorDataWrapper buildRemainingVendorNotes(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        if (paymentWorksFormModeService.shouldUseLegacyFormProcessingMode()) {
            kfsVendorDataWrapper = createGoodsAndServicesNote(pmwVendor, kfsVendorDataWrapper);
        }
        kfsVendorDataWrapper = createInitiatorNote(pmwVendor, kfsVendorDataWrapper);
        kfsVendorDataWrapper = createVendorTypeBusinessPurposeNote(pmwVendor, kfsVendorDataWrapper);
        kfsVendorDataWrapper = createConflictOfInterestNote(pmwVendor, kfsVendorDataWrapper);
        if (paymentWorksFormModeService.shouldUseLegacyFormProcessingMode()) {
            kfsVendorDataWrapper = createInsuranceCertificateNote(pmwVendor, kfsVendorDataWrapper);
        }
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper createGoodsAndServicesNote(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        StringBuilder sbText = new StringBuilder(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_PROVIDED_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getServicesProvided()).append(System.lineSeparator());
        if (servicesAreBeingProvided(pmwVendor)) {
            sbText = populateAnswersToServiceQuestions(sbText, pmwVendor);
        }
        kfsVendorDataWrapper = getPaymentWorksBatchUtilityService().createNoteRecordingAnyErrors(kfsVendorDataWrapper, sbText.toString(), PaymentWorksConstants.ErrorDescriptorForBadKfsNote.GOODS_AND_SERVICES.getNoteDescriptionString());
        return kfsVendorDataWrapper;
    }
    
    private boolean servicesAreBeingProvided(PaymentWorksVendor pmwVendor) {
        return (StringUtils.isNotBlank(pmwVendor.getServicesProvided())
                && (StringUtils.equalsIgnoreCase(pmwVendor.getServicesProvided(), PaymentWorksConstants.PaymentWorksGoodsVsServicesOptions.SERVICES.getOptionValueAsString())
                    || StringUtils.equalsIgnoreCase(pmwVendor.getServicesProvided(), PaymentWorksConstants.PaymentWorksGoodsVsServicesOptions.GOODS_WITH_SERVICES.getOptionValueAsString())));
    }

    private StringBuilder populateAnswersToServiceQuestions(StringBuilder sbText, PaymentWorksVendor pmwVendor) {
        sbText = appendServicesAnswerToText(sbText, PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_CURRENT_PAYROLL_PAID_LABEL, pmwVendor.isCurrentlyPaidThroughPayroll());
        sbText = appendServicesAnswerToText(sbText, PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_EVER_PAYROLL_PAID_LABEL, pmwVendor.isEverPaidThroughPayroll());
        sbText = appendServicesAnswerToText(sbText, PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_NOT_SOLE_PROPRIETOR_LABEL, pmwVendor.isSeperateLegalEntityProvidingServices());
        sbText = appendServicesAnswerToText(sbText, PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_RECEIVING_EQUIPMENT_TRAINING_LABEL, pmwVendor.isCornellProvidedTrainingOrEquipmentRequired());
        sbText = appendServicesAnswerToText(sbText, PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_NO_MARKETING_LABEL, pmwVendor.isInformalMarketing());
        sbText = appendServicesAnswerToText(sbText, PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_NO_INSURANCE_LABEL, pmwVendor.isServicesProvidedWithoutInsurance());
        return sbText;
    }

    private StringBuilder appendServicesAnswerToText(StringBuilder sbText, String serviceQuestionLabel, boolean isAnswerAffimative) {
        return(sbText.append(getConfigurationService().getPropertyValueAsString(serviceQuestionLabel)).append(KFSConstants.BLANK_SPACE).append((isAnswerAffimative ? KFSConstants.OptionLabels.YES : KFSConstants.OptionLabels.NO)).append(System.lineSeparator()).append(System.lineSeparator()));
    }
    
    private KfsVendorDataWrapper createInitiatorNote(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        StringBuilder sbText = new StringBuilder(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NOTES_INITIATOR_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getInitiatorNetId());
        kfsVendorDataWrapper = getPaymentWorksBatchUtilityService().createNoteRecordingAnyErrors(kfsVendorDataWrapper, sbText.toString(), PaymentWorksConstants.ErrorDescriptorForBadKfsNote.INITIATOR.getNoteDescriptionString());
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper createVendorTypeBusinessPurposeNote(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        StringBuilder sbText = new StringBuilder(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_PAYMENT_REASON_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getVendorType());
        kfsVendorDataWrapper = getPaymentWorksBatchUtilityService().createNoteRecordingAnyErrors(kfsVendorDataWrapper, sbText.toString(), PaymentWorksConstants.ErrorDescriptorForBadKfsNote.BUSINESS_PURPOSE.getNoteDescriptionString());
        return kfsVendorDataWrapper;
    }
    
    private KfsVendorDataWrapper createConflictOfInterestNote(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        if (pmwVendor.isConflictOfInterest()) {
            StringBuilder sbText = new StringBuilder(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_MESSAGE)).append(KFSConstants.BLANK_SPACE).append(System.lineSeparator());
            sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_EMPLOYEE_NAME_LABEL)).append(KFSConstants.BLANK_SPACE).append(pmwVendor.getConflictOfInterestEmployeeName()).append(System.lineSeparator());
            sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_PHONE_NUMBER_LABEL)).append(pmwVendor.getConflictOfInterestEmployeePhoneNumber()).append(KFSConstants.BLANK_SPACE).append(System.lineSeparator());
            sbText.append(getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_RELATIONSHIP_LABEL)).append(pmwVendor.getConflictOfInterestRelationshipToEmployee()).append(KFSConstants.BLANK_SPACE).append(System.lineSeparator());
            kfsVendorDataWrapper = getPaymentWorksBatchUtilityService().createNoteRecordingAnyErrors(kfsVendorDataWrapper, sbText.toString(), PaymentWorksConstants.ErrorDescriptorForBadKfsNote.CONFLICT_OF_INTEREST.getNoteDescriptionString());
        }
        return kfsVendorDataWrapper;
    }

    private KfsVendorDataWrapper createInsuranceCertificateNote(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        if (paymentWorksVendorIsPurchaseOrderVendor(pmwVendor) && StringUtils.isNotBlank(pmwVendor.getInsuranceCertificate())) {
            kfsVendorDataWrapper = getPaymentWorksBatchUtilityService().createNoteRecordingAnyErrors(kfsVendorDataWrapper,
                    getConfigurationService().getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_INSURANCE_CERTIFICATE_URL_EXISTS_MESSAGE),
                    PaymentWorksConstants.ErrorDescriptorForBadKfsNote.INSURANCE_CERTIFICATE.getNoteDescriptionString());
        }
        return kfsVendorDataWrapper;
    }
    
    protected String findPoCountryToUse(PaymentWorksVendor pmwVendor) {
        String poCountryToUse = pmwVendor.getPoCountry();
        if (paymentWorksFormModeService.shouldUseForeignFormProcessingMode()) {
            if (!StringUtils.equalsIgnoreCase(PaymentWorksConstants.PO_COUNTRY_US_CANADA_AUSTRALIA_OTHER_VALUE_OTHER, 
                    pmwVendor.getPoCountryUsCanadaAustraliaOther())) {
                poCountryToUse = pmwVendor.getPoCountryUsCanadaAustraliaOther();
            }
        } 
        return poCountryToUse;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public PaymentWorksBatchUtilityService getPaymentWorksBatchUtilityService() {
        return paymentWorksBatchUtilityService;
    }

    public void setPaymentWorksBatchUtilityService(PaymentWorksBatchUtilityService paymentWorksBatchUtilityService) {
        this.paymentWorksBatchUtilityService = paymentWorksBatchUtilityService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setPaymentWorksFormModeService(PaymentWorksFormModeService paymentWorksFormModeService) {
        this.paymentWorksFormModeService = paymentWorksFormModeService;
    }

    public void setPaymentWorksVendorSupplierDiversityService(PaymentWorksVendorSupplierDiversityService paymentWorksVendorSupplierDiversityService) {
        this.paymentWorksVendorSupplierDiversityService = paymentWorksVendorSupplierDiversityService;
    }

    public void setPaymentWorksTaxRuleDependencyService(PaymentWorksTaxRuleDependencyService paymentWorksTaxRuleDependencyService) {
        this.paymentWorksTaxRuleDependencyService = paymentWorksTaxRuleDependencyService;
    }

}
