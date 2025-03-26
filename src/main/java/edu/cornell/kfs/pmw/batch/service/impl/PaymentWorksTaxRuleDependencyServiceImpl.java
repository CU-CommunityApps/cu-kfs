package edu.cornell.kfs.pmw.batch.service.impl;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.TaxRule;
import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksBatchUtilityService;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksTaxRuleDependencyService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class PaymentWorksTaxRuleDependencyServiceImpl implements PaymentWorksTaxRuleDependencyService {
    private static final Logger LOG = LogManager.getLogger();
    
    protected DateTimeService dateTimeService;
    protected PaymentWorksBatchUtilityService paymentWorksBatchUtilityService;
    protected ConfigurationService configurationService;
    
    protected static final DateTimeFormatter DATE_FORMATTER_FOR_SQL_DATE = 
            DateTimeFormatter.ofPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd, Locale.US);

    @Override
    public KfsVendorDataWrapper buildKfsVendorDataWrapper(PaymentWorksVendor pmwVendor,
            Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        LOG.info("buildKfsVendorDataWrapper, entering");
        KfsVendorDataWrapper vendorDataWrapper = new KfsVendorDataWrapper();
        VendorHeader vendorHeader = vendorDataWrapper.getVendorDetail().getVendorHeader();
        
        String vendorFipsCountryCode = convertIsoCountryCodeToFipsCountryCode(pmwVendor.getRequestingCompanyTaxCountry(), paymentWorksIsoToFipsCountryMap);
        vendorHeader.setVendorForeignIndicator(!isUnitedStatesFipsCountryCode(vendorFipsCountryCode));
        vendorHeader.setVendorCorpCitizenCode(vendorFipsCountryCode);
        
        TaxRule taxRule = determineTaxRuleToUseForDataPopulation(pmwVendor, vendorFipsCountryCode);
        LOG.info("buildKfsVendorDataWrapper, vendor request " + pmwVendor.getPmwVendorRequestId() + " tax rule" + taxRule);
        
        populateTaxRuleDependentAttributes(pmwVendor, vendorDataWrapper, taxRule);
        return vendorDataWrapper;
    }
    
    protected TaxRule determineTaxRuleToUseForDataPopulation(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        TaxRule taxRule = TaxRule.OTHER;
        if (isIndividualUsSsn(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            taxRule = TaxRule.INDIVIDUAL_US_SSN;
        }  else if (isIndividualUsEin(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            taxRule = TaxRule.INDIVIDUAL_US_EIN;
        } else if (isNotIndividualUs(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            taxRule = TaxRule.NOT_INDIVIDUAL_US;
        } else if (isForeignIndividualWithSSN(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            taxRule = TaxRule.FOREIGN_INDIVIDUAL_US_TAX_PAYER;
        } else if (isForeignIndividual(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            taxRule = TaxRule.FOREIGN_INDIVIDUAL;
        } else if (isForeignEntity(pmwVendor, pmwVendorFipsTaxCountryCode)) {
            taxRule = TaxRule.FOREIGN_ENTITY;
        } else {
            LOG.error("determineTaxRuleToUseForDataPopulation, unknown tax rule for request " + pmwVendor.getId());
        }
        return taxRule;
    }
    
    protected void populateTaxRuleDependentAttributes(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper vendorDataWrapper, TaxRule taxRule) {
        VendorDetail vendorDetail = vendorDataWrapper.getVendorDetail();
        VendorHeader vendorHeader = vendorDetail.getVendorHeader();
        if (taxRule.isForeign) {
            poulateForeignVendorValues(pmwVendor, vendorDataWrapper, taxRule);
        } else {
            vendorHeader.setVendorTaxNumber(pmwVendor.getRequestingCompanyTin());
            vendorHeader.setVendorTaxTypeCode(taxRule.taxTypeCode);
        }
        
        populateOwnershipCode(pmwVendor, taxRule, vendorHeader);
        
        if (taxRule.populateW9Attributes) {
            populateW9Attributes(vendorDataWrapper, pmwVendor);
        }
        
        if (taxRule.populateFirstLastLegalName) {
            populateFirstLastLegalName(pmwVendor, vendorDetail);
        }
        
        if (taxRule.populateBusinessLegalName) {
            populateBusinessLegalName(pmwVendor, vendorDetail);
        }
    }
    
    protected void poulateForeignVendorValues(PaymentWorksVendor pmwVendor, KfsVendorDataWrapper vendorDataWrapper, TaxRule taxRule) {
        VendorHeader vendorHeader = vendorDataWrapper.getVendorDetail().getVendorHeader();
        if (StringUtils.isNotBlank(pmwVendor.getRequestingCompanyTin())) {
            if (taxRule.populateForeignSSN) {
                vendorHeader.setVendorTaxNumber(pmwVendor.getRequestingCompanyTin());
                vendorHeader.setVendorForeignTaxId(StringUtils.EMPTY);
                vendorHeader.setVendorTaxTypeCode(taxRule.taxTypeCode);
            } else {
                vendorHeader.setVendorForeignTaxId(pmwVendor.getRequestingCompanyTin());
                vendorHeader.setVendorTaxNumber(StringUtils.EMPTY);
            }
        }
        
        vendorHeader.setVendorChapter3StatusCode(pmwVendor.getChapter3StatusCode());
        vendorHeader.setVendorChapter4StatusCode(pmwVendor.getChapter4StatusCode());
        vendorHeader.setVendorGIIN(pmwVendor.getGiinCode());
        populateW8Attributes(vendorDataWrapper, pmwVendor, taxRule);
        if (taxRule.populateDateOfBirth && StringUtils.isNotBlank(pmwVendor.getDateOfBirth())) {
            Date dob = buildSqlDateFromString(pmwVendor.getDateOfBirth());
            vendorHeader.setVendorDOB(dob);
        }
    }
    
    protected void populateOwnershipCode(PaymentWorksVendor pmwVendor, TaxRule taxRule, VendorHeader vendorHeader) {
        if (StringUtils.isNotBlank(taxRule.ownershipTypeCode)) {
            vendorHeader.setVendorOwnershipCode(taxRule.ownershipTypeCode);
        } else {
            PaymentWorksConstants.PaymentWorksTaxClassification classfication = PaymentWorksConstants.PaymentWorksTaxClassification.
                    findPaymentWorksTaxClassification(pmwVendor.getRequestingCompanyTaxClassificationCode());
            vendorHeader.setVendorOwnershipCode(classfication.kfsVendorOwnershipTypeCode);
        }
    }
    
    private void populateW9Attributes(KfsVendorDataWrapper kfsVendorDataWrapper, PaymentWorksVendor pmwVendor) {
        kfsVendorDataWrapper.getVendorDetail().getVendorHeader().setVendorW9ReceivedIndicator(new Boolean(true));
        kfsVendorDataWrapper.getVendorDetail().getVendorHeader().setVendorW9SignedDate(dateTimeService.getCurrentSqlDate());
        paymentWorksBatchUtilityService.createNoteRecordingAnyErrors(kfsVendorDataWrapper, 
                configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_W9_URL_EXISTS_MESSAGE), 
                PaymentWorksConstants.ErrorDescriptorForBadKfsNote.W9.getNoteDescriptionString());
    }
    
    private void populateFirstLastLegalName(PaymentWorksVendor pmwVendor, VendorDetail vendorDetail) {
        vendorDetail.setVendorFirstLastNameIndicator(true);
        String vendorLastName = truncateValueToMaxLength(pmwVendor.getRequestingCompanyLegalLastName(), VendorConstants.MAX_VENDOR_NAME_LENGTH);
        vendorDetail.setVendorLastName(vendorLastName);
        vendorDetail.setVendorFirstName(truncateLegalFirstNameToMaximumAllowedLengthWhenFormattedWithLegalLastName(vendorLastName, pmwVendor.getRequestingCompanyLegalFirstName(), VendorConstants.NAME_DELIM, VendorConstants.MAX_VENDOR_NAME_LENGTH));
    }
    
    private void populateBusinessLegalName(PaymentWorksVendor pmwVendor, VendorDetail vendorDetail) {
        vendorDetail.setVendorName(truncateValueToMaxLength(pmwVendor.getRequestingCompanyLegalName(), VendorConstants.MAX_VENDOR_NAME_LENGTH));
        vendorDetail.setVendorFirstLastNameIndicator(false);
        vendorDetail.setVendorFirstName(KFSConstants.EMPTY_STRING);
        vendorDetail.setVendorLastName(KFSConstants.EMPTY_STRING);
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
    
    private boolean isForeignIndividualWithSSN(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return  isForeignIndividual(pmwVendor, pmwVendorFipsTaxCountryCode) 
                && StringUtils.equalsAnyIgnoreCase(pmwVendor.getRequestingCompanyTinType(), 
                        PaymentWorksConstants.PaymentWorksTinType.SSN.getPmwCodeAsString());
    }
    
    private boolean isForeignIndividual(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return  !isUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode) && 
                StringUtils.containsIgnoreCase(pmwVendor.getSupplierCategory(), PaymentWorksConstants.SUPPLIER_CATEGORY_INDIVIDUAL);
    }
    
    private boolean isForeignEntity(PaymentWorksVendor pmwVendor, String pmwVendorFipsTaxCountryCode) {
        return !isUnitedStatesFipsCountryCode(pmwVendorFipsTaxCountryCode) &&
                StringUtils.containsIgnoreCase(pmwVendor.getSupplierCategory(), PaymentWorksConstants.SUPPLIER_CATEGORY_ENTITY);
    }
    
    private void populateW8Attributes(KfsVendorDataWrapper kfsVendorDataWrapper, PaymentWorksVendor pmwVendor, TaxRule taxRule) {
        VendorHeader vendorHeader = kfsVendorDataWrapper.getVendorDetail().getVendorHeader();
        if (StringUtils.isNotBlank(pmwVendor.getRequestingCompanyW8W9())) {
            LOG.debug("populateW8Attributes, setting W8 values");
            vendorHeader.setVendorW8BenReceivedIndicator(true);
            vendorHeader.setVendorW8TypeCode(taxRule.w8TypeCode);
            if (StringUtils.isNotBlank(pmwVendor.getW8SignedDate())) {
                vendorHeader.setVendorW8SignedDate(buildSqlDateFromString(pmwVendor.getW8SignedDate()));
            }
            paymentWorksBatchUtilityService.createNoteRecordingAnyErrors(kfsVendorDataWrapper, 
                    configurationService.getPropertyValueAsString(PaymentWorksKeyConstants.NEW_VENDOR_PVEN_NOTES_W8_URL_EXISTS_MESSAGE), 
                    PaymentWorksConstants.ErrorDescriptorForBadKfsNote.W8.getNoteDescriptionString());
        } else {
            LOG.error("populateW8Attributes, NOT setting W8 values");
            vendorHeader.setVendorW8BenReceivedIndicator(false);
        }
    }
    
    protected Date buildSqlDateFromString(String  dateString) {
        LocalDate dateStringAsLocalDate = LocalDate.parse(dateString, DATE_FORMATTER_FOR_SQL_DATE);
        Date sqlDateBuiltFromDateString = Date.valueOf(dateStringAsLocalDate);
        if (LOG.isDebugEnabled()) {
            LOG.debug("buildSqlDateFromString, dateString: {}  dateStringAsLocalDate: {}  sqlDateBuiltFromDateString: {}", dateString , dateStringAsLocalDate, sqlDateBuiltFromDateString);
        }
        return sqlDateBuiltFromDateString;
    }
    
    protected String truncateValueToMaxLength(String inputValue, int maxLength) {
        return StringUtils.substring(inputValue, 0, maxLength);
    }
    
    protected String truncateLegalFirstNameToMaximumAllowedLengthWhenFormattedWithLegalLastName(String legalLastName, String legalFirstName, String delim, int maxLength) {
        int maxAllowedLengthOfFirstName = maxLength - delim.length() - legalLastName.length();
        
        if (maxAllowedLengthOfFirstName <= 0)
            return KFSConstants.EMPTY_STRING;
        else 
            return truncateValueToMaxLength(legalFirstName, maxAllowedLengthOfFirstName);
    }
    
    @Override
    public String convertIsoCountryCodeToFipsCountryCode(String isoCountryCode, Map<String, List<PaymentWorksIsoFipsCountryItem>> paymentWorksIsoToFipsCountryMap) {
        String fipsCountryCode = StringUtils.EMPTY;
        if (paymentWorksIsoToFipsCountryMap.containsKey(isoCountryCode)) {
            if (paymentWorksIsoToFipsCountryMap.get(isoCountryCode).size() == 1) {
                fipsCountryCode = paymentWorksIsoToFipsCountryMap.get(isoCountryCode).get(0).getFipsCountryCode();
            } else {
                LOG.error("convertIsoCountryCodeToFipsCountryCode, more than one FIPS country for ISO country " + isoCountryCode);
            }
        } else {
            LOG.error("convertIsoCountryCodeToFipsCountryCode, no FIPS country for ISO country " + isoCountryCode);
        }
        return fipsCountryCode;
    }
    
    private boolean isUnitedStatesFipsCountryCode (String countryCode) {
        return StringUtils.equalsIgnoreCase(countryCode, KFSConstants.COUNTRY_CODE_UNITED_STATES);
    }
    
    private boolean isPmwVendorIndividualSolePropriatorSingleMemberLlc(PaymentWorksVendor pmwVendor) {
        return (ObjectUtils.isNotNull(pmwVendor.getRequestingCompanyTaxClassificationCode()) && 
                pmwVendor.getRequestingCompanyTaxClassificationCode().intValue() == PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
    }
    
    private boolean isTinTypeSsn(String tinTypeCode) {
        return StringUtils.equalsIgnoreCase(tinTypeCode, PaymentWorksConstants.PaymentWorksTinType.SSN.getPmwCodeAsString());
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
