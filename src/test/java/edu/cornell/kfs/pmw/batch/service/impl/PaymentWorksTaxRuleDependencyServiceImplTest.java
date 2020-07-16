package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.TaxRule;
import edu.cornell.kfs.pmw.batch.businessobject.KfsVendorDataWrapper;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

class PaymentWorksTaxRuleDependencyServiceImplTest {
    private static final Logger LOG = LogManager.getLogger(PaymentWorksTaxRuleDependencyServiceImplTest.class);
    
    private PaymentWorksTaxRuleDependencyServiceImpl taxRuleService;

    @BeforeEach
    void setUp() throws Exception {
        taxRuleService = new PaymentWorksTaxRuleDependencyServiceImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        taxRuleService = null;
    }

    @Test
    void testDetermineTaxRuleToUseForDataPopulationOther() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        TaxRule actualResults = taxRuleService.determineTaxRuleToUseForDataPopulation(pmwVendor, KFSConstants.COUNTRY_CODE_UNITED_STATES);
        assertEquals(TaxRule.OTHER, actualResults);
    }
    
    @Test
    void testDetermineTaxRuleToUseForDataPopulationIndividualUsSSN() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.SSN.getPmwCodeAsString());
        TaxRule actualResults = taxRuleService.determineTaxRuleToUseForDataPopulation(pmwVendor, KFSConstants.COUNTRY_CODE_UNITED_STATES);
        assertEquals(TaxRule.INDIVIDUAL_US_SSN, actualResults);
    }
    
    @Test
    void testDetermineTaxRuleToUseForDataPopulationIndividualUsEIN() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.INDIVIDUAL_SOLE_PROPRIETOR_TAX_CLASSIFICATION_INDICATOR);
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.FEIN.getPmwCodeAsString());
        TaxRule actualResults = taxRuleService.determineTaxRuleToUseForDataPopulation(pmwVendor, KFSConstants.COUNTRY_CODE_UNITED_STATES);
        assertEquals(TaxRule.INDIVIDUAL_US_EIN, actualResults);
    }
    
    @Test
    void testDetermineTaxRuleToUseForDataPopulationNotIndividualUs() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.C_CORPORATION_TAX_CLASSIFICATION_INDICATOR);
        TaxRule actualResults = taxRuleService.determineTaxRuleToUseForDataPopulation(pmwVendor, KFSConstants.COUNTRY_CODE_UNITED_STATES);
        assertEquals(TaxRule.NOT_INDIVIDUAL_US, actualResults);
    }
    
    @Test
    void testDetermineTaxRuleToUseForDataPopulationForeignIndividual() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setSupplierCategory("Foreign Individual");
        TaxRule actualResults = taxRuleService.determineTaxRuleToUseForDataPopulation(pmwVendor, "CA");
        assertEquals(TaxRule.FOREIGN_INDIVIDUAL, actualResults);
    }
    
    @Test
    void testDetermineTaxRuleToUseForDataPopulationForeignEntity() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setSupplierCategory("Foreign Entity");
        TaxRule actualResults = taxRuleService.determineTaxRuleToUseForDataPopulation(pmwVendor, "CA");
        assertEquals(TaxRule.FOREIGN_ENTITY, actualResults);
    }
    
    @Test
    void testBuildDateFromString() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setW8SignedDate("2020-07-01");
        Date actualDate = taxRuleService.buildDateFromString(pmwVendor);
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2020, Calendar.JULY, 01, 0, 0);
        Date expectedDate = new Date(cal.getTimeInMillis());
        
        assertEquals(expectedDate, actualDate);
        
    }
    
    @Test
    void testTaxRuleToString() {
        for (TaxRule rule : TaxRule.values()) {
            LOG.info("testTaxRuleToString, " + rule.toString());
            
        }
    }

}
