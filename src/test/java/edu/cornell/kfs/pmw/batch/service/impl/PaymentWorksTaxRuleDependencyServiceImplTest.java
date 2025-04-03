package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.TaxRule;
import edu.cornell.kfs.pmw.batch.PaymentWorksConstants.VendorOwnershipTypeCodes;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;

class PaymentWorksTaxRuleDependencyServiceImplTest {
    private PaymentWorksTaxRuleDependencyServiceImpl taxRuleService;

    @BeforeEach
    void setUp() throws Exception {
        Configurator.setLevel(PaymentWorksTaxRuleDependencyServiceImpl.class.getName(), Level.DEBUG);
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
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.FOREIGN_TIN.getPmwCodeAsString());
        TaxRule actualResults = taxRuleService.determineTaxRuleToUseForDataPopulation(pmwVendor, "CA");
        assertEquals(TaxRule.FOREIGN_INDIVIDUAL, actualResults);
    }
    
    @Test
    void testDetermineTaxRuleToUseForDataPopulationForeignIndividualUsTaxPayerId() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setSupplierCategory("Foreign Individual");
        pmwVendor.setRequestingCompanyTinType(PaymentWorksConstants.PaymentWorksTinType.SSN.getPmwCodeAsString());
        TaxRule actualResults = taxRuleService.determineTaxRuleToUseForDataPopulation(pmwVendor, "CA");
        assertEquals(TaxRule.FOREIGN_INDIVIDUAL_US_TAX_PAYER, actualResults);
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
        Date actualDate = taxRuleService.buildSqlDateFromString("2020-07-01");
        Date expectedDate = PaymentWorksVendorSupplierDiversityServiceImplTest.createSqlDate(2020, 7, 1);
        
        assertEquals(expectedDate, actualDate);
    }
    
    @Test
    void testPopulateOwnershipCodeLLC_C_Corp() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.LLC_TAXED_AS_C_CORPORATION_TAX_CLASSIFICATION_INDICATOR);
        VendorHeader vendorHeader = new VendorHeader();
        taxRuleService.populateOwnershipCode(pmwVendor, TaxRule.NOT_INDIVIDUAL_US, vendorHeader);
        assertEquals(VendorOwnershipTypeCodes.C_CORPORATION, vendorHeader.getVendorOwnershipCode());
    }
    
    @Test
    void testPopulateOwnershipCodeLLC_S_Corp() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxClassificationCode(PaymentWorksConstants.LLC_TAXED_AS_S_CORPORATION_TAX_CLASSIFICATION_INDICATOR);
        VendorHeader vendorHeader = new VendorHeader();
        taxRuleService.populateOwnershipCode(pmwVendor, TaxRule.NOT_INDIVIDUAL_US, vendorHeader);
        assertEquals(VendorOwnershipTypeCodes.S_CORPORATION, vendorHeader.getVendorOwnershipCode());
    }

}
