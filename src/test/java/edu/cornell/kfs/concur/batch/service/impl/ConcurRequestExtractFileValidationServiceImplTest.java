package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestEntryDetailFileLine;
import edu.cornell.kfs.concur.batch.fixture.ConcurParameterConstantsFixture;
import edu.cornell.kfs.concur.batch.fixture.ConcurRequestExtractFileFixture;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class ConcurRequestExtractFileValidationServiceImplTest {
    private ConcurRequestExtractFileValidationServiceImpl concurRequestExtractFileValidationService;
    private ConcurParameterConstantsFixture concurParameterConstantsFixture;
    private ConcurBatchUtilityServiceImpl concurBatchUtilityService;
    
    @Before
    public void setUp() throws Exception {
        Logger.getLogger(ConcurRequestExtractFileValidationServiceImpl.class).setLevel(Level.DEBUG);
        concurRequestExtractFileValidationService = new ConcurRequestExtractFileValidationServiceImpl();
        concurBatchUtilityService = new TestableConcurBatchUtilityServiceImpl();
        concurRequestExtractFileValidationService.setConcurBatchUtilityService(concurBatchUtilityService);
        concurParameterConstantsFixture = new ConcurParameterConstantsFixture();
    }
    
    @After
    public void tearDown() throws Exception {
        concurRequestExtractFileValidationService = null;
        concurBatchUtilityService = null;
        concurParameterConstantsFixture = null;
    }
    
    @Test
    public void testHeaderAmountAndHeaderRowCountsMatch() {
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.GOOD_FILE.createConcurRequestExtractFile();
        assertTrue("Expected Result: Header amount SHOULD match sum of row amounts from file.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFile));
    }
    
    @Test
    public void testHeaderAmountDoesNotMatch() {
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.BAD_REQUEST_AMOUNT_FILE.createConcurRequestExtractFile();
        assertFalse("Expected Result: Header amount should NOT match sum of row amounts from file.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFile));
    }

    @Test
    public void testHeaderRowCountDoesNotMatch() {
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.BAD_FILE_COUNT_FILE.createConcurRequestExtractFile();
        assertFalse("Expected Result: Header row count should NOT match file row count.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFile));
    }

    @Test
    public void testFileContainsBadEmployeeGroupId() {
        ConcurRequestExtractFile testFile = ConcurRequestExtractFileFixture.BAD_EMPLOYEE_GROUP_ID_FILE.createConcurRequestExtractFile();
        assertFalse("Expected Result: Request Detail row contains BAD Employee Group Id.", concurRequestExtractFileValidationService.requestExtractHeaderRowValidatesToFileContents(testFile));
    }

    private class TestableConcurBatchUtilityServiceImpl extends ConcurBatchUtilityServiceImpl {
        @Override
        public String getConcurParameterValue(String parameterName) {
            String parameterValue = concurParameterConstantsFixture.getValueForConcurParameter(parameterName);
            if (StringUtils.isEmpty(parameterValue)) {
                return parameterName;
            }
            else {
                return parameterValue;
            }
        }
    }

}
