package edu.cornell.kfs.pmw.batch.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksTaxClassification;
import edu.cornell.kfs.pmw.batch.TaxRule;
import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksTaxRuleDependencyService;

public class PaymentWorksTaxRuleDependencyServiceImpl implements PaymentWorksTaxRuleDependencyService {
    protected DateTimeService dateTimeService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected ConfigurationService configurationService;
    

    @Override
    public KfsVendorDataWrapper populateTaxRuleDependentAttributes(PaymentWorksVendor pmwVendor,
            Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        KfsVendorDataWrapper vendorDataWrapper = new KfsVendorDataWrapper();
        addVendorHeaderToKfsVendorDataWrapper(pmwVendor, paymentWorksIsoToFipsCountryMap, vendorDataWrapper);
        return vendorDataWrapper;
    }

    protected void addVendorHeaderToKfsVendorDataWrapper(PaymentWorksVendor pmwVendor,
            Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap, KfsVendorDataWrapper vendorDataWrapper) {
        String vendorFipsCountryCode = convertIsoCountryCodeToFipsCountryCode(pmwVendor.getRequestingCompanyTaxCountry(), paymentWorksIsoToFipsCountryMap);
        TaxRule taxRule = determineTaxRuleToUseForDataPopulation(pmwVendor, vendorFipsCountryCode);
        
        VendorHeader vendorHeader = new VendorHeader();
        vendorHeader.setVendorForeignIndicator(!isUnitedStatesFipsCountryCode(vendorFipsCountryCode));
        vendorHeader.setVendorTaxNumber(pmwVendor.getRequestingCompanyTin());
        vendorHeader.setVendorTaxTypeCode(taxRule.taxTypeCode);
        if (StringUtils.isNotBlank(taxRule.ownershipTypeCode)) {
            vendorHeader.setVendorOwnershipCode(taxRule.ownershipTypeCode);
        } else {
            PaymentWorksTaxClassification classfication = PaymentWorksTaxClassification.findPaymentWorksTaxClassification(
                    pmwVendor.getRequestingCompanyTaxClassificationCode());
            vendorHeader.setVendorOwnershipCode(classfication.translationToKfsOwnershipTypeCode);
        }
        vendorHeader.setVendorCorpCitizenCode(convertIsoCountryCodeToFipsCountryCode(pmwVendor.getRequestingCompanyTaxCountry(), paymentWorksIsoToFipsCountryMap));
        vendorDataWrapper.getVendorDetail().setVendorHeader(vendorHeader);
        
        if (taxRule.populateW9Attributes) {
            populateW9Attributes(vendorDataWrapper, pmwVendor);
        }
        
        if (taxRule.populateFirstLastLegalName) {
            populateFirstLastLegalName(pmwVendor, vendorDataWrapper);
        }
        
        if (taxRule.populateBusinessLegalName) {
            populateBusinessLegalName(pmwVendor, vendorDataWrapper);
        }
    }
    
    private TaxRule determineTaxRuleToUseForDataPopulation(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        if (isIndividualUsSsn(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return TaxRule.INDIVIDUAL_US_SSN;
        }  else if (isIndividualUsEin(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return TaxRule.INDIVIDUAL_US_EIN;
        } else if (isNotIndividualUs(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            return TaxRule.NOT_INDIVIDUAL_US;
        } else {
            throw new IllegalArgumentException();
        }
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
    
    private void populateW9Attributes(KfsVendorDataWrapper kfsVendorDataWrapper, PaymentWorksVendor pmwVendor) {
        kfsVendorDataWrapper.getVendorDetail().getVendorHeader().setVendorW9ReceivedIndicator(new Boolean(true));
        kfsVendorDataWrapper.getVendorDetail().getVendorHeader().setVendorW9SignedDate(dateTimeService.getCurrentSqlDate());
        kfsVendorDataWrapper = paymentWorksBatchUtilityService.createNoteRecordingAnyErrors(kfsVendorDataWrapper, 
                configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_W9_URL_EXISTS_MESSAGE), 
                PaymentWorksConstants.ErrorDescriptorForBadKfsNote.W9.getNoteDescriptionString());
    }
    
    private void populateFirstLastLegalName(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        kfsVendorDataWrapper.getVendorDetail().setVendorFirstLastNameIndicator(true);
        String vendorLastName = truncateValueToMaxLength(pmwVendor.getRequestingCompanyLegalLastName(), VendorConstants.MAX_VENDOR_NAME_LENGTH);
        kfsVendorDataWrapper.getVendorDetail().setVendorLastName(vendorLastName);
        kfsVendorDataWrapper.getVendorDetail().setVendorFirstName(truncateLegalFirstNameToMaximumAllowedLengthWhenFormattedWithLegalLastName(vendorLastName, pmwVendor.getRequestingCompanyLegalFirstName(), VendorConstants.NAME_DELIM, VendorConstants.MAX_VENDOR_NAME_LENGTH));
    }
    
    private KfsVendorDataWrapper populateBusinessLegalName(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper kfsVendorDataWrapper) {
        kfsVendorDataWrapper.getVendorDetail().setVendorName(truncateValueToMaxLength(pmwVendor.getRequestingCompanyLegalName(), VendorConstants.MAX_VENDOR_NAME_LENGTH));
        kfsVendorDataWrapper.getVendorDetail().setVendorFirstLastNameIndicator(false);
        kfsVendorDataWrapper.getVendorDetail().setVendorFirstName(KFSConstants.EMPTY_STRING);
        kfsVendorDataWrapper.getVendorDetail().setVendorLastName(KFSConstants.EMPTY_STRING);
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
    
    
    private boolean isUnitedStatesFipsCountryCode (String countryCode) {
        return StringUtils.equalsIgnoreCase(countryCode, KFSConstants.COUNTRY_CODE_UNITED_STATES);
    }
    
    private String convertIsoCountryCodeToFipsCountryCode(String isoCountryCode, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        return (paymentWorksIsoToFipsCountryMap.get(isoCountryCode).get(0)).getFipsCountryCode();
    }
    
    private boolean isPmwVendorIndividualSolePropriatorSingleMemberLlc(PaymentWorksVendor pmwVendor) {
        return (ObjectUtils.isNotNull(pmwVendor.getRequestingCompanyTaxClassificationCode()) && 
                pmwVendor.getRequestingCompanyTaxClassificationCode().intValue() == PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
    }
    
    private boolean isTinTypeSsn(String tinTypeCode) {
        return (StringUtils.isNotBlank(tinTypeCode) &&
                StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.SSN.getPmwCodeAsString()));
    }
    
    private boolean isTinTypeEin(String tinTypeCode) {
        return (StringUtils.isNotBlank(tinTypeCode) &&
                StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.FEIN.getPmwCodeAsString()));
    }
    
    private boolean isNotPmwVendorIndividualSolePropriatorSingleMemberLlc(PaymentWorksVendor pmwVendor) {
        return (ObjectUtils.isNotNull(pmwVendor.getRequestingCompanyTaxClassificationCode()) && 
               !(pmwVendor.getRequestingCompanyTaxClassificationCode().intValue() == PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR));
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
    
}
