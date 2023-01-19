package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PayeeACHAccountDocumentServiceImplTest {
    
    private PayeeACHAccountDocumentServiceImpl payeeACHAccountDocumentService;
    
    @Before
    public void setUp() throws Exception {
        payeeACHAccountDocumentService = new PayeeACHAccountDocumentServiceImpl();
    }
    
    @After
    public void tearDown() throws Exception {
        payeeACHAccountDocumentService = null;
    }
    
    @Test
    public void testGoodAccountTypeToStandardEntryClassConversion() {
        validateGoodAccountTypeToStandardEntryClassConversion("22PPD");
        validateGoodAccountTypeToStandardEntryClassConversion("32PPD");
    }
    
    @Test
    public void ard() {
        validateBadAccountTypeToStandardEntryClassConversion(null);
        validateBadAccountTypeToStandardEntryClassConversion("");
        validateBadAccountTypeToStandardEntryClassConversion("PD");
        validateBadAccountTypeToStandardEntryClassConversion("ABC");
        validateBadAccountTypeToStandardEntryClassConversion("22PP");
        validateBadAccountTypeToStandardEntryClassConversion("32XXX");
    }
    
    private void validateGoodAccountTypeToStandardEntryClassConversion(String codeToValidate) {
        payeeACHAccountDocumentService.determineStandardEntryClass(codeToValidate);
        assertTrue(true);
    }
    
    private void validateBadAccountTypeToStandardEntryClassConversion(String codeToValidate) {
        try {
            payeeACHAccountDocumentService.determineStandardEntryClass(codeToValidate);
            assertTrue(false);
        } catch (IllegalArgumentException iae) {
            assertTrue(true);
        }
    }
    
}
