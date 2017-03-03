package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.Calendar;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;
import edu.cornell.kfs.concur.batch.fixture.ConcurRequestExtractFileFixture;

public class ConcurRequestExtractFileValidationServiceImplTest {
    private ConcurRequestExtractFileValidationServiceImpl concurRequestExtractFileValidationService;
    
    @Before
    public void setUp() throws Exception {
        Logger.getLogger(ConcurRequestExtractFileValidationServiceImpl.class).setLevel(Level.DEBUG);
        concurRequestExtractFileValidationService = new ConcurRequestExtractFileValidationServiceImpl();
    }
    
    @After
    public void tearDown() throws Exception {
        concurRequestExtractFileValidationService = null;
    }
    
    @Test
    public void testHeaderAmountMatches() {
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.GOOD_FILE.createConcurRequestExtractFile();
        assertTrue("Expected Result: Header amount SHOULD match sum of row amounts from file.", concurRequestExtractFileValidationService.fileApprovedAmountsMatchHeaderApprovedAmount(testFile));
    }
    
    @Test
    public void testHeaderAmountDoesNotMatch() {
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.BAD_REQUEST_AMOUNT_FILE.createConcurRequestExtractFile();
        assertFalse("Expected Result: Header amount should NOT match sum of row amounts from file.", concurRequestExtractFileValidationService.fileApprovedAmountsMatchHeaderApprovedAmount(testFile));
    }

    @Test
    public void testHeaderRowCountMatches() {
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.GOOD_FILE.createConcurRequestExtractFile();
        assertTrue("ExpectedResult: Header row count should MATCH file row count.", concurRequestExtractFileValidationService.fileRowCountMatchesHeaderRowCount(testFile));
    }

    @Test
    public void testHeaderRowCountDoesNotMatch() {
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.BAD_FILE_COUNT_FILE.createConcurRequestExtractFile();
        assertFalse("Expected Result: Header row count should NOT match file row count.", concurRequestExtractFileValidationService.fileRowCountMatchesHeaderRowCount(testFile));
    }

}
