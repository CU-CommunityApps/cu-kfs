package edu.cornell.kfs.sys.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.batch.BatchFile;
import org.mockito.Mockito;

class CuBatchFileAdminAuthorizationServiceImplTest {
    private CuBatchFileAdminAuthorizationServiceImpl cuBatchFileAdminAuthorizationServiceImpl;
    private static final String TEST_BATCH_BASE_DIRECTORY = "src/test/resources/edu/cornell/kfs/sys/batch/service/impl/CuBatchFileAdminAuthorizationServiceImplTest/";
    private static final String CONDITIONALLY_PREVENT_DIRECTORY_NAME = "conditionallyPreventDownload";
    private static final String ALLOWED_DIRECTORY_NAME = "allowDownload";
    private static final String ALLOW_FILE_NAME = "allow_test.txt";
    private static final String PREVENT_FILE_NAME = "prevent_test.txt";
    
    private BatchFile allowedFile;
    private BatchFile conditionallyPreventedFile;

    @BeforeEach
    void setUp() throws Exception {
        Logger.getLogger(CuBatchFileAdminAuthorizationServiceImpl.class.getName()).setLevel(Level.DEBUG);
        cuBatchFileAdminAuthorizationServiceImpl = new CuBatchFileAdminAuthorizationServiceImpl();
        allowedFile = buildMockBatchFile(TEST_BATCH_BASE_DIRECTORY + ALLOWED_DIRECTORY_NAME, ALLOW_FILE_NAME);
        conditionallyPreventedFile = buildMockBatchFile(TEST_BATCH_BASE_DIRECTORY + CONDITIONALLY_PREVENT_DIRECTORY_NAME, PREVENT_FILE_NAME);
    }
    
    private BatchFile buildMockBatchFile(String patch, String fileName) {
        BatchFile batchFile = Mockito.mock(BatchFile.class);
        Mockito.when(batchFile.getFileName()).thenReturn(fileName);
        Mockito.when(batchFile.getPath()).thenReturn(patch);
        return batchFile;
    }

    @AfterEach
    void tearDown() {
        cuBatchFileAdminAuthorizationServiceImpl = null;
        allowedFile = null;
        conditionallyPreventedFile = null;
    }

    @Test
    void testIsDownloadOfFilePreventedNullDirectories() {
        cuBatchFileAdminAuthorizationServiceImpl.setPreventDownloadDirectories(null);
        verifyDownloadPreventedResults(false, false);
    }
    
    @Test
    void testIsDownloadOfFilePreventedEmptyString() {
        cuBatchFileAdminAuthorizationServiceImpl.setPreventDownloadDirectories(StringUtils.EMPTY);
        verifyDownloadPreventedResults(false, false);
    }
    
    @Test
    void testIsDownloadOfFilePreventedOneDirectory() {
        cuBatchFileAdminAuthorizationServiceImpl.setPreventDownloadDirectories(CONDITIONALLY_PREVENT_DIRECTORY_NAME);
        verifyDownloadPreventedResults(false, true);
    }
    
    @Test
    void testIsDownloadOfFilePreventedTwoDirectories() {
        cuBatchFileAdminAuthorizationServiceImpl.setPreventDownloadDirectories("fooDirectory/," + CONDITIONALLY_PREVENT_DIRECTORY_NAME);
        verifyDownloadPreventedResults(false, true);
    }
    
    @Test
    void testIsDownloadOfFilePreventedTwoDirectoriesNotRelatedToTestFiles() {
        cuBatchFileAdminAuthorizationServiceImpl.setPreventDownloadDirectories("fooDirectory/,barDirectory/");
        verifyDownloadPreventedResults(false, false);
    }
    
    private void verifyDownloadPreventedResults(boolean expectedAllowedResults, boolean expectedConditionalResults) {
        boolean actualAllowedResults = cuBatchFileAdminAuthorizationServiceImpl.isDownloadOfFilePrevented(allowedFile);
        boolean actualConditionalResults = cuBatchFileAdminAuthorizationServiceImpl.isDownloadOfFilePrevented(conditionallyPreventedFile);
        assertEquals(expectedAllowedResults, actualAllowedResults);
        assertEquals(expectedConditionalResults, actualConditionalResults);
    }

}
