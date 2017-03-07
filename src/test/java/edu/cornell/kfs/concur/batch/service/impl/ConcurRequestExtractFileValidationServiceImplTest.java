package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    public void testHeaderAmountAndHeaderRowCountsMatch() {
        List<ConcurRequestExtractFile> testFiles = ConcurRequestExtractFileFixture.GOOD_FILE.createConcurRequestExtractFiles();
        assertTrue("Expected Result: Header amount SHOULD match sum of row amounts from file.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFiles));
    }
    
    @Test
    public void testHeaderAmountDoesNotMatch() {
        List<ConcurRequestExtractFile> testFiles = ConcurRequestExtractFileFixture.BAD_REQUEST_AMOUNT_FILE.createConcurRequestExtractFiles();
        assertFalse("Expected Result: Header amount should NOT match sum of row amounts from file.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFiles));
    }

    @Test
    public void testHeaderRowCountDoesNotMatch() {
        List<ConcurRequestExtractFile> testFiles = ConcurRequestExtractFileFixture.BAD_FILE_COUNT_FILE.createConcurRequestExtractFiles();
        assertFalse("Expected Result: Header row count should NOT match file row count.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFiles));
    }

}
