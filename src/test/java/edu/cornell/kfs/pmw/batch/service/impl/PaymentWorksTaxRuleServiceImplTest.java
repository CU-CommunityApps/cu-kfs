package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.Ignore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

class PaymentWorksTaxRuleServiceImplTest {
    
    private PaymentWorksVendorToKfsVendorDetailConversionServiceImpl vendorDetailService;
    private PaymentWorksTaxRuleServiceImpl taxRuleService;
    
    private static final String US_COUNTRY =  KFSConstants.COUNTRY_CODE_UNITED_STATES;
    private static final String CANADA_COUNTRY = "CA";

    @BeforeEach
    void setUp() throws Exception {
        vendorDetailService = new PaymentWorksVendorToKfsVendorDetailConversionServiceImpl();
        taxRuleService = new PaymentWorksTaxRuleServiceImpl();
        Configurator.setLevel(PaymentWorksTaxRuleServiceImpl.class.getName(), Level.DEBUG);
    }

    @AfterEach
    void tearDown() throws Exception {
        vendorDetailService = null;
        taxRuleService = null;
    }

    @Test
    void testDetermineIndividualUSSSN() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.SSN.getPmwCodeAsString());
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_US_SSN, pmwVendor, US_COUNTRY);
    }
    
    @Test
    void testDetermineIndividualUSEIN() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.FEIN.getPmwCodeAsString());
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_US_EIN, pmwVendor, US_COUNTRY);
    }
    
    @Test
    void testDetermineNotIndivudual() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.C_CORPORATION_TAX_CLASSIFICATION_INDICATOR);
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_US, pmwVendor, US_COUNTRY);
    }
    
    @Test
    void testDetermineNotUSSolePropietorSSN() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.SSN.getPmwCodeAsString());
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_INDIVIDUAL, pmwVendor, CANADA_COUNTRY);
    }
    
    @Test
    void testDetermineNotUSSolePropietorITIN() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.ITIN.getPmwCodeAsString());
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_INDIVIDUAL, pmwVendor, CANADA_COUNTRY);
    }  

    @Ignore
    void testDetermineNotUSSolePropietorSSNTaxOther() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.SSN.getPmwCodeAsString());
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_OTHER, pmwVendor, CANADA_COUNTRY);
    }
    
    @Ignore
    void testDetermineNotUSSolePropietorITINaxOther() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.ITIN.getPmwCodeAsString());
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_SSN_OR_ITIN_TAX_CLASS_OTHER, pmwVendor, CANADA_COUNTRY);
    }
    
    @Test
    void testDetermineNoUSForeignTaxClassIndividual() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.FOREIGN_TIN.getPmwCodeAsString());
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_INDIVIDUAL, pmwVendor, CANADA_COUNTRY);
    }
    
    @Ignore
    void testDetermineNoUSForeignTaxClassOther() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.FOREIGN_TIN.getPmwCodeAsString());
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.INDIVIDUAL_NOT_US_FOREIGN_TAX_CLASS_OTHER, pmwVendor, CANADA_COUNTRY);
    }
    
    @Test
    void testDetermineNotIndivdualUSEIN() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.C_CORPORATION_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.FEIN.getPmwCodeAsString());
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_NOT_US_EIN, pmwVendor, CANADA_COUNTRY);
    }
    
    @Test
    void testDetermineNotIndivdualUSForeign() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.C_CORPORATION_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.FOREIGN_TIN.getPmwCodeAsString());
        
        validateValues(PaymentWorksConstants.PaymentWorksNewVendorTaxBusinessRule.NOT_INDIVIDUAL_NOT_US_FOREIGN, pmwVendor, CANADA_COUNTRY);
    }
    
    private void validateValues(int expectedResults, PaymentWorksVendor pmwVendor, String countryCode) {
        int actualResultsTaxRuleService = taxRuleService.determineTaxRuleToUseForDataPopulation(pmwVendor , countryCode);
        int actualResultsVendorDetailService = vendorDetailService.determineTaxRuleToUseForDataPopulation(pmwVendor, countryCode);
        assertEquals("actual tax rule results not as expected", expectedResults, actualResultsTaxRuleService);
        assertEquals("actual vendor detail results not as expected", expectedResults, actualResultsVendorDetailService);
    }

}
