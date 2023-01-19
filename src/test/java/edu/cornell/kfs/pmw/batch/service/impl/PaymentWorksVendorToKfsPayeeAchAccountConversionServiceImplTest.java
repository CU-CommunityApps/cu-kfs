package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.coa.businessobject.options.CuCheckingSavingsValuesFinder;

public class PaymentWorksVendorToKfsPayeeAchAccountConversionServiceImplTest {
        
        private PaymentWorksVendorToKfsPayeeAchAccountConversionServiceImpl PaymentWorksVendorToKfsPayeeAchAccountConversionService;
        
        @Before
        public void setUp() throws Exception {
            PaymentWorksVendorToKfsPayeeAchAccountConversionService = new PaymentWorksVendorToKfsPayeeAchAccountConversionServiceImpl();
        }
        
        @After
        public void tearDown() throws Exception {
            PaymentWorksVendorToKfsPayeeAchAccountConversionService = null;
        }
        
        @Test
        public void testGoodAccountTypeToStandardEntryClassConversion() {
            validateGoodAccountTypeToStandardEntryClassConversion(CuCheckingSavingsValuesFinder.BankAccountTypes.CORPORATE_CHECKING);
            validateGoodAccountTypeToStandardEntryClassConversion(CuCheckingSavingsValuesFinder.BankAccountTypes.CORPORATE_SAVINGS);
            validateGoodAccountTypeToStandardEntryClassConversion(CuCheckingSavingsValuesFinder.BankAccountTypes.PERSONAL_CHECKING);
            validateGoodAccountTypeToStandardEntryClassConversion(CuCheckingSavingsValuesFinder.BankAccountTypes.PERSONAL_SAVINGS);
        }
        
        @Test
        public void testBadAccountTypeToStandardEntryClassConversion() {
            validateBadAccountTypeToStandardEntryClassConversion(null);
            validateBadAccountTypeToStandardEntryClassConversion("");
            validateBadAccountTypeToStandardEntryClassConversion("PD");
            validateBadAccountTypeToStandardEntryClassConversion("ABC");
            validateBadAccountTypeToStandardEntryClassConversion("22PP");
            validateBadAccountTypeToStandardEntryClassConversion("32XXX");
        }
        
        private void validateGoodAccountTypeToStandardEntryClassConversion(String codeToValidate) {
            PaymentWorksVendorToKfsPayeeAchAccountConversionService.determineStandardEntryClass(codeToValidate);
            assertTrue(true);
        }
        
        private void validateBadAccountTypeToStandardEntryClassConversion(String codeToValidate) {
            try {
                PaymentWorksVendorToKfsPayeeAchAccountConversionService.determineStandardEntryClass(codeToValidate);
                assertTrue(false);
            } catch (IllegalArgumentException iae) {
                assertTrue(true);
            }
        }

}
