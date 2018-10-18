package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurParameterConstants;
import edu.cornell.kfs.concur.batch.service.impl.fixture.EmailFileFixture;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class ConcurBatchUtilityServiceImplTest {
    ConcurBatchUtilityServiceImpl utilityService;

    @Before
    public void setUp() throws Exception {
        utilityService = new ConcurBatchUtilityServiceImpl();
        FileSystemFileStorageServiceImpl fileService = new FileSystemFileStorageServiceImpl();
        fileService.setPathPrefix(StringUtils.EMPTY);
        utilityService.setFileStorageService(fileService);
        utilityService.setParameterService(createMockParameterService());
    }

    private ParameterService createMockParameterService() {
        ParameterService parameterService = mock(ParameterService.class);
        
        String testValidTravelerStatuses = StringUtils.joinWith(CUKFSConstants.SEMICOLON,
                ConcurConstants.EMPLOYEE_STATUS_CODE, ConcurConstants.NON_EMPLOYEE_STATUS_CODE);
        when(parameterService.getParameterValueAsString(CUKFSConstants.ParameterNamespaces.CONCUR,
                CUKFSParameterKeyConstants.ALL_COMPONENTS, ConcurParameterConstants.CONCUR_VALID_TRAVELER_STATUSES_FOR_PDP_EMPLOYEE_PROCESSING))
                .thenReturn(testValidTravelerStatuses);
        
        return parameterService;
    }

    @After
    public void tearDown() throws Exception {
        utilityService = null;
    }

    @Test
    public void getFileContents_nonFile() {
        String actual = utilityService.getFileContents("foo");
        String expected = StringUtils.EMPTY;
        assertEquals("A null file should have no contents", expected, actual);
    }
    
    @Test
    public void getFileContents_emptyFile() {
        String actual = utilityService.getFileContents(EmailFileFixture.EMPTY_FILE.fullFilePath);
        String expected = EmailFileFixture.EMPTY_FILE.fileContents;
        assertEquals("An empty file should have no contents", expected, actual);
    }
    
    @Test
    public void getFileContents_simpleContentsFile() {
        String actual = utilityService.getFileContents(EmailFileFixture.SIMPLE_FILE.fullFilePath);
        assertEquals("The simple contents file should have formatted values", EmailFileFixture.SIMPLE_FILE.fileContents, actual);
    }

    @Test
    public void testTravelerStatusCheck() throws Exception {
        assertTravelerStatusIsValid(ConcurConstants.EMPLOYEE_STATUS_CODE);
        assertTravelerStatusIsValid(ConcurConstants.NON_EMPLOYEE_STATUS_CODE);
        assertTravelerStatusIsValid(ConcurConstants.EMPLOYEE_STATUS_CODE.toLowerCase());
        assertTravelerStatusIsValid(ConcurConstants.NON_EMPLOYEE_STATUS_CODE.toLowerCase());
        assertTravelerStatusIsInvalid(null);
        assertTravelerStatusIsInvalid(KFSConstants.EMPTY_STRING);
        assertTravelerStatusIsInvalid(KFSConstants.BLANK_SPACE);
        assertTravelerStatusIsInvalid("OtherValue");
    }

    private void assertTravelerStatusIsValid(String status) {
        assertTrue("The status '" + status + "' should have been seen as valid",
                utilityService.isValidTravelerStatusForProcessingAsPDPEmployeeType(status));
    }

    private void assertTravelerStatusIsInvalid(String status) {
        assertFalse("The status '" + status + "' should have been seen as invalid",
                utilityService.isValidTravelerStatusForProcessingAsPDPEmployeeType(status));
    }

}
