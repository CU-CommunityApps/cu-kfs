package edu.cornell.kfs.sys.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

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
    
    private BatchFile allowedFile;
    private BatchFile conditionallyPreventedFile;

    @BeforeEach
    void setUp() throws Exception {
        Logger.getLogger(CuBatchFileAdminAuthorizationServiceImpl.class.getName()).setLevel(Level.DEBUG);
        cuBatchFileAdminAuthorizationServiceImpl = new CuBatchFileAdminAuthorizationServiceImpl();
        allowedFile = buildMockBatchFile(new File(TEST_BATCH_BASE_DIRECTORY  + "allowDownload/test.txt"));
        conditionallyPreventedFile =  buildMockBatchFile(new File(TEST_BATCH_BASE_DIRECTORY + CONDITIONALLY_PREVENT_DIRECTORY_NAME + "/test.txt"));
    }
    
    private BatchFile buildMockBatchFile(File file) {
        BatchFile batchFile = Mockito.mock(BatchFile.class);
        Mockito.when(batchFile.getFileName()).thenReturn(file.getAbsolutePath());
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
        cuBatchFileAdminAuthorizationServiceImpl.setPreventDownloadDirectories(CONDITIONALLY_PREVENT_DIRECTORY_NAME + ",fooDirectory");
        verifyDownloadPreventedResults(false, true);
    }
    
    @Test
    void testIsDownloadOfFilePreventedTwoDirectoriesNotRelatedToTestFiles() {
        cuBatchFileAdminAuthorizationServiceImpl.setPreventDownloadDirectories("fooDirectory,barDirectory");
        verifyDownloadPreventedResults(false, false);
    }
    
    private void verifyDownloadPreventedResults(boolean expectedAllowedResults, boolean expectedConditionalResults) {
        boolean actualAllowedResults = cuBatchFileAdminAuthorizationServiceImpl.isDownloadOfFilePrevented(allowedFile);
        boolean actualConditionalResults = cuBatchFileAdminAuthorizationServiceImpl.isDownloadOfFilePrevented(conditionallyPreventedFile);
        assertEquals(expectedAllowedResults, actualAllowedResults);
        assertEquals(expectedConditionalResults, actualConditionalResults);
    }

}
