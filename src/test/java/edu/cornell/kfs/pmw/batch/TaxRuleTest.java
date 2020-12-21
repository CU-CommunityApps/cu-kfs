package edu.cornell.kfs.pmw.batch;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

public class TaxRuleTest {

    @Test
    void testIndividualSSN() {
        TaxRule rule = TaxRule.INDIVIDUAL_US_SSN;
        assertEquals(PaymentWorksConstants.PaymentWorksTinType.SSN.getKfsTaxTypeCodeAsString(), rule.taxTypeCode);
        assertEquals(PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.kfsVendorOwnershipTypeCode, 
                rule.ownershipTypeCode);
        assertTrue(rule.populateW9Attributes);
        assertTrue(rule.populateFirstLastLegalName);
        assertFalse(rule.populateBusinessLegalName);
        assertDefaultForeignValuesForUSVendor(rule);
    }
    
    @Test
    void testIndividualEIN() {
        TaxRule rule = TaxRule.INDIVIDUAL_US_EIN;
        assertEquals(PaymentWorksConstants.PaymentWorksTinType.FEIN.getKfsTaxTypeCodeAsString(), rule.taxTypeCode);
        assertEquals(PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.kfsVendorOwnershipTypeCode, 
                rule.ownershipTypeCode);
        assertTrue(rule.populateW9Attributes);
        assertFalse(rule.populateFirstLastLegalName);
        assertTrue(rule.populateBusinessLegalName);
        assertDefaultForeignValuesForUSVendor(rule);
    }
    
    @Test
    void testNotIndividualUs() {
        TaxRule rule = TaxRule.NOT_INDIVIDUAL_US;
        assertEquals(PaymentWorksConstants.PaymentWorksTinType.FEIN.getKfsTaxTypeCodeAsString(), rule.taxTypeCode);
        assertEquals(StringUtils.EMPTY, rule.ownershipTypeCode);
        assertTrue(rule.populateW9Attributes);
        assertFalse(rule.populateFirstLastLegalName);
        assertTrue(rule.populateBusinessLegalName);
        assertDefaultForeignValuesForUSVendor(rule);
    }
    
    void assertDefaultForeignValuesForUSVendor(TaxRule rule) {
        assertFalse(rule.isForeign);
        assertFalse(rule.populateDateOfBirth);
        assertEquals(StringUtils.EMPTY, rule.w8TypeCode);
        assertFalse(rule.populateForeignSSN);
    }
    
    @Test
    void testForeignIndividualUSTaxPayer() {
        TaxRule rule = TaxRule.FOREIGN_INDIVIDUAL_US_TAX_PAYER;
        assertEquals(PaymentWorksConstants.PaymentWorksTinType.SSN.getKfsTaxTypeCodeAsString(), rule.taxTypeCode);
        assertTrue(rule.populateForeignSSN);
        assertStandardForeignIndividualValues(rule);
    }
    
    @Test
    void testForeignIndividual() {
        TaxRule rule = TaxRule.FOREIGN_INDIVIDUAL;
        assertEquals(PaymentWorksConstants.PaymentWorksTinType.FOREIGN_TIN.getKfsTaxTypeCodeAsString(), rule.taxTypeCode);
        assertFalse(rule.populateForeignSSN);
        assertStandardForeignIndividualValues(rule);
    }

    protected void assertStandardForeignIndividualValues(TaxRule rule) {
        assertEquals(PaymentWorksConstants.PaymentWorksTaxClassification.INDIVIDUAL_SOLE_PROPRIETOR.kfsVendorOwnershipTypeCode, 
                rule.ownershipTypeCode);
        assertFalse(rule.populateW9Attributes);
        assertTrue(rule.populateFirstLastLegalName);
        assertFalse(rule.populateBusinessLegalName);
        assertTrue(rule.isForeign);
        assertTrue(rule.populateDateOfBirth);
        assertEquals(PaymentWorksConstants.W8TypeCodes.BN, rule.w8TypeCode);
    }
    
    @Test
    void testForeignEntity() {
        TaxRule rule = TaxRule.FOREIGN_ENTITY;
        assertEquals(PaymentWorksConstants.PaymentWorksTinType.FOREIGN_TIN.getKfsTaxTypeCodeAsString(), rule.taxTypeCode);
        assertEquals(PaymentWorksConstants.PaymentWorksTaxClassification.C_CORPORATION.kfsVendorOwnershipTypeCode, 
                rule.ownershipTypeCode);
        assertFalse(rule.populateW9Attributes);
        assertFalse(rule.populateFirstLastLegalName);
        assertTrue(rule.populateBusinessLegalName);
        assertTrue(rule.isForeign);
        assertFalse(rule.populateDateOfBirth);
        assertEquals(PaymentWorksConstants.W8TypeCodes.BE, rule.w8TypeCode);
        assertFalse(rule.populateForeignSSN);
    }

}
