package com.rsmart.kuali.kfs.cr.batch.service.impl;

import com.rsmart.kuali.kfs.cr.CrTestConstants;
import com.rsmart.kuali.kfs.cr.batch.fixture.CheckReconciliationFixture;
import com.rsmart.kuali.kfs.cr.batch.fixture.StaleCheckFileFixture;
import com.rsmart.kuali.kfs.cr.batch.fixture.StaleCheckRowFixture;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.dataaccess.CheckReconciliationDao;
import com.rsmart.kuali.kfs.cr.batch.StaleCheckExtractCsvFields;
import com.rsmart.kuali.kfs.cr.batch.StaleCheckExtractCsvInputFileType;
import edu.cornell.kfs.pdp.CUPdpTestConstants;
import com.rsmart.kuali.kfs.cr.businessobject.StaleCheckBatchRow;
import edu.cornell.kfs.sys.CUKFSConstants;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.gl.GeneralLedgerConstants.BatchFileSystem;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.impl.datetime.DateTimeServiceImpl;
import org.mockito.AdditionalAnswers;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class StaleCheckExtractServiceImplTest {

    private static final String STALE_CHECK_SOURCE_FILE_PATH = "src/test/resources/com/rsmart/kuali/kfs/cr/batch/service/fixture";
    private static final String STALE_CHECK_TESTING_FILE_PATH = "test/cr/staleCheckExtract";
    private static final String STALE_CHECK_TESTING_DIRECTORY = STALE_CHECK_TESTING_FILE_PATH + "/";
    private static final String FILE_PATH_FORMAT = "{0}/{1}{2}";

    private TestStaleCheckExtractService staleCheckExtractService;

    @Before
    public void setUp() throws Exception {
        staleCheckExtractService = new TestStaleCheckExtractService();
        staleCheckExtractService.setBatchInputFileService(new BatchInputFileServiceImpl());
        staleCheckExtractService.setBatchInputFileTypes(Collections.singletonList(createStaleCheckBatchInputFileType()));
        staleCheckExtractService.setBusinessObjectService(createMockBusinessObjectService());
        staleCheckExtractService.setCheckReconciliationDao(createMockCheckReconciliationDao());
        staleCheckExtractService.setDateTimeService(createMockDateTimeService());
    }

    @After
    public void cleanUp() {
        removeTestFilesAndDirectories();
    }

    @Test
    public void testLoadValidFileWithSingleDataRow() throws Exception {
        assertStaleCheckExtractionHasCorrectResults(StaleCheckFileFixture.FILE_WITH_SINGLE_SUCCESSFUL_LINE);
    }

    @Test
    public void testLoadFileWithInvalidFormat() throws Exception {
        assertStaleCheckExtractionHasCorrectResults(StaleCheckFileFixture.FILE_WITH_BAD_HEADERS);
    }

    @Test
    public void testLoadFileWithoutDataRows() throws Exception {
        assertStaleCheckExtractionHasCorrectResults(StaleCheckFileFixture.FILE_WITH_HEADER_ROW_ONLY);
    }

    @Test
    public void testLoadFileWithSomeInvalidRows() throws Exception {
        assertStaleCheckExtractionHasCorrectResults(StaleCheckFileFixture.FILE_MIXED_LINES);
    }

    @Test
    public void testLoadFileWithMultipleValidRows() throws Exception {
        assertStaleCheckExtractionHasCorrectResults(StaleCheckFileFixture.FILE_MULTIPLE_VALID_LINES);
    }

    @Test
    public void testLoadFileWithMultipleInvalidStatusRows() throws Exception {
        assertStaleCheckExtractionHasCorrectResults(StaleCheckFileFixture.FILE_MULTIPLE_INVALID_STATUS_LINES);
    }

    private void assertStaleCheckExtractionHasCorrectResults(StaleCheckFileFixture... fixtures) throws Exception {
        List<StaleCheckFileFixture> expectedResults = Arrays.asList(fixtures);
        String[] fileNames = Arrays.stream(fixtures)
                .map(StaleCheckFileFixture::getBaseFileName)
                .toArray(String[]::new);

        copyInputFilesAndGenerateDoneFiles(fileNames);
        staleCheckExtractService.processStaleCheckBatchFiles();

        Map<String, StaleCheckExtractServiceImplTest.StaleCheckFileResult> actualResults = staleCheckExtractService.getFileResults();
        assertFilesHaveExpectedResults(expectedResults, actualResults);
        assertDoneFilesWereDeleted(fileNames);
    }

    private void assertFilesHaveExpectedResults(List<StaleCheckFileFixture> expectedResults, Map<String, StaleCheckFileResult> actualResults) {
        assertEquals("Wrong number of processed files", expectedResults.size(), actualResults.size());
        for (int i = 0; i < expectedResults.size(); i++) {
            StaleCheckFileFixture expectedResult = expectedResults.get(i);
            StaleCheckFileResult actualResult = actualResults.get(expectedResult.baseFileName);
            assertFileHasExpectedResults(i, expectedResult, actualResult);
        }
    }

    private void assertFileHasExpectedResults(int index, StaleCheckFileFixture expectedResult, StaleCheckFileResult actualResult) {
        assertEquals("Wrong file-processable state at index " + index, expectedResult.processableFile, actualResult.isProcessableFile());
        if (expectedResult.processableFile) {
            List<StaleCheckRowFixture> expectedRows = expectedResult.staleCheckRows;
            List<StaleCheckRowResult> actualRows = actualResult.getRowResults();
            assertEquals("Wrong number of processed file rows", expectedRows.size(), actualRows.size());

            for (int i = 0; i < expectedRows.size(); i++) {
                StaleCheckRowFixture expectedStaleCheckRow = expectedRows.get(i);
                StaleCheckRowResult actualStaleCheckRowResult = actualRows.get(i);

                verifyParsedRowMatchesExpectedResult(expectedStaleCheckRow, actualStaleCheckRowResult);
            }
        }
    }

    private void verifyParsedRowMatchesExpectedResult(StaleCheckRowFixture expectedRow, StaleCheckRowResult actualStaleCheckRowResult) {
        assertEquals("Unexpected Stale Check Row Result", expectedRow.valid, actualStaleCheckRowResult.validRow);
        if (expectedRow.valid) {
            assertEquals("Unexpected Check Number", expectedRow.checkNumber, actualStaleCheckRowResult.staleCheckBatchRow.getCheckNumber());
            assertEquals("Unexpected Check Status", expectedRow.checkStatus, actualStaleCheckRowResult.staleCheckBatchRow.getCheckStatus());
            assertEquals("Unexpected Bank Code", expectedRow.bankCode, actualStaleCheckRowResult.staleCheckBatchRow.getBankCode());
            assertEquals("Unexpected Check Issued Date", expectedRow.checkIssuedDate, actualStaleCheckRowResult.staleCheckBatchRow.getCheckIssuedDate());
            assertEquals("Unexpected Check Total Amount", expectedRow.checkTotalAmount, actualStaleCheckRowResult.staleCheckBatchRow.getCheckTotalAmount());
        }
    }

    private void assertDoneFilesWereDeleted(String... inputFileNames) {
        for (String inputFileName : inputFileNames) {
            File doneFile = new File(MessageFormat.format(FILE_PATH_FORMAT, STALE_CHECK_TESTING_FILE_PATH, inputFileName, BatchFileSystem.DONE_FILE_EXTENSION));
            assertFalse("There should not be a .done file for " + inputFileName, doneFile.exists());
        }
    }

    protected void copyInputFilesAndGenerateDoneFiles(String... inputFileNames) throws IOException {
        for (String inputFileName : inputFileNames) {
            File sourceFile = new File(MessageFormat.format(FILE_PATH_FORMAT, STALE_CHECK_SOURCE_FILE_PATH, inputFileName, CUPdpTestConstants.CSV_FILE_EXTENSION));
            File destFile = new File(MessageFormat.format(FILE_PATH_FORMAT, STALE_CHECK_TESTING_FILE_PATH, inputFileName, CUPdpTestConstants.CSV_FILE_EXTENSION));
            File doneFile = new File(MessageFormat.format(FILE_PATH_FORMAT, STALE_CHECK_TESTING_FILE_PATH, inputFileName, BatchFileSystem.DONE_FILE_EXTENSION));
            FileUtils.copyFile(sourceFile, destFile);
            doneFile.createNewFile();
        }
    }

    protected void removeTestFilesAndDirectories() {
        File staleExtractDirectory = new File(STALE_CHECK_TESTING_DIRECTORY);
        if (staleExtractDirectory.exists() && staleExtractDirectory.isDirectory()) {
            for (File staleCheckFile : staleExtractDirectory.listFiles()) {
                staleCheckFile.delete();
            }
            staleExtractDirectory.delete();

            int slashIndex = STALE_CHECK_TESTING_DIRECTORY.lastIndexOf('/', STALE_CHECK_TESTING_DIRECTORY.lastIndexOf('/') - 1);
            while (slashIndex != -1) {
                File tempDirectory = new File(STALE_CHECK_TESTING_DIRECTORY.substring(0, slashIndex + 1));
                tempDirectory.delete();
                slashIndex = STALE_CHECK_TESTING_DIRECTORY.lastIndexOf('/', slashIndex - 1);
            }
        }
    }

    private StaleCheckExtractCsvInputFileType createStaleCheckBatchInputFileType() {
        StaleCheckExtractCsvInputFileType fileType = new StaleCheckExtractCsvInputFileType();
        fileType.setDirectoryPath(STALE_CHECK_TESTING_FILE_PATH);
        fileType.setFileExtension(
                StringUtils.substringAfter(CUPdpTestConstants.CSV_FILE_EXTENSION, CUKFSConstants.DELIMITER));
        fileType.setCsvEnumClass(StaleCheckExtractCsvFields.class);
        return fileType;
    }

    private static class TestStaleCheckExtractService extends StaleCheckExtractServiceImpl {
        private Map<String, StaleCheckFileResult> fileResults = new HashMap<>();
        private StaleCheckFileResult currentFileResult;
        private StaleCheckRowResult currentRowResult;

        @Override
        protected List<String> loadStaleCheckBatchFile(String inputFileName, BatchInputFileType batchInputFileType) {
            currentFileResult = new StaleCheckFileResult();

            try {
                return super.loadStaleCheckBatchFile(inputFileName, batchInputFileType);
            } catch (Exception e) {
                currentFileResult.markAsUnprocessable();
                throw e;
            } finally {
            		if (CollectionUtils.isEmpty(currentFileResult.getRowResults())) {
            			currentFileResult.markAsUnprocessable();
            		}
                fileResults.put(generateFileResultKey(inputFileName), currentFileResult);
                currentFileResult = null;
            }
        }

        private String generateFileResultKey(String inputFileName) {
            String key = inputFileName;
            if (StringUtils.contains(key, CUPdpTestConstants.BACKSLASH)) {
                key = StringUtils.substringAfterLast(inputFileName, CUPdpTestConstants.BACKSLASH);
            }
            if (StringUtils.contains(key, CUKFSConstants.SLASH)) {
                key = StringUtils.substringAfterLast(key, CUKFSConstants.SLASH);
            }
            key = StringUtils.substringBeforeLast(key, CUKFSConstants.DELIMITER);
            return key;
        }

        @Override
        protected String processStaleCheckBatchRow(StaleCheckBatchRow staleCheckRow) {
            currentRowResult = new StaleCheckRowResult(staleCheckRow);

            try {
                String failureMessage = super.processStaleCheckBatchRow(staleCheckRow);
                if (StringUtils.isNotBlank(failureMessage)) {
                    currentRowResult.markRowAsInvalid();
                }
                return failureMessage;
            } catch (Exception e) {
                currentRowResult.markRowAsInvalid();
                throw e;
            } finally {
                currentFileResult.addRowResult(currentRowResult);
                currentRowResult = null;
            }
        }

        public Map<String, StaleCheckExtractServiceImplTest.StaleCheckFileResult> getFileResults() {
            return fileResults;
        }
    }

    public static class StaleCheckFileResult {
        private boolean processableFile;
        private List<StaleCheckRowResult> rowResults;

        public StaleCheckFileResult() {
            this.processableFile = true;
            this.rowResults = new ArrayList<>();
        }

        public void markAsUnprocessable() {
            processableFile = false;
        }

        public boolean isProcessableFile() {
            return processableFile;
        }

        public void addRowResult(StaleCheckRowResult rowResult) {
            rowResults.add(rowResult);
        }

        public List<StaleCheckRowResult> getRowResults() {
            return rowResults;
        }
    }

    private static class StaleCheckRowResult {
        public StaleCheckBatchRow staleCheckBatchRow;
        public boolean validRow;

        public StaleCheckRowResult(StaleCheckBatchRow staleCheckBatchRow) {
            this.staleCheckBatchRow = staleCheckBatchRow;
            validRow = true;
        }

        public void markRowAsInvalid() {
            this.validRow = false;
        }
    }

    private static BusinessObjectService createMockBusinessObjectService() {
        BusinessObjectService businessObjectService = mock(BusinessObjectService.class);
        when(businessObjectService.save(any(CheckReconciliation.class))).then(AdditionalAnswers.returnsFirstArg());
        return businessObjectService;
    }

    private static DateTimeService createMockDateTimeService() {
        DateTimeService dateTimeService = mock(DateTimeServiceImpl.class);
        java.sql.Date currentSqlDate = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
        when(dateTimeService.getCurrentSqlDate()).thenReturn(currentSqlDate);
        return dateTimeService;
    }

    private static CheckReconciliationDao createMockCheckReconciliationDao() {
        CheckReconciliationDao checkReconciliationDao = mock(CheckReconciliationDao.class);
        CheckReconciliation mockedCheckReconciliation = createMockCheckReconciliation(CheckReconciliationFixture.CHECK_999_JPCD_STAL_925);
        when(checkReconciliationDao.findByCheckNumber("99999999", CrTestConstants.JPMC_BANK_CODE)).thenReturn(mockedCheckReconciliation);

        mockedCheckReconciliation = createMockCheckReconciliation(CheckReconciliationFixture.CHECK_123_JPCD_STAL_29252);
        when(checkReconciliationDao.findByCheckNumber("12345678", CrTestConstants.JPMC_BANK_CODE)).thenReturn(mockedCheckReconciliation);

        mockedCheckReconciliation = createMockCheckReconciliation(CheckReconciliationFixture.CHECK_199_JPCD_STAL_925);
        when(checkReconciliationDao.findByCheckNumber("19999999", CrTestConstants.JPMC_BANK_CODE)).thenReturn(mockedCheckReconciliation);

        mockedCheckReconciliation = createMockCheckReconciliation(CheckReconciliationFixture.CHECK_399_JPCD_VOID_951);
        when(checkReconciliationDao.findByCheckNumber("39999999", CrTestConstants.JPMC_BANK_CODE)).thenReturn(mockedCheckReconciliation);

        mockedCheckReconciliation = createMockCheckReconciliation(CheckReconciliationFixture.CHECK_111_JPCD_CLRD_123);
        when(checkReconciliationDao.findByCheckNumber("11111111", CrTestConstants.JPMC_BANK_CODE)).thenReturn(mockedCheckReconciliation);

        mockedCheckReconciliation = createMockCheckReconciliation(CheckReconciliationFixture.CHECK_211_JPCD_STOP_123);
        when(checkReconciliationDao.findByCheckNumber("21111111", CrTestConstants.JPMC_BANK_CODE)).thenReturn(mockedCheckReconciliation);

        mockedCheckReconciliation = createMockCheckReconciliation(CheckReconciliationFixture.CHECK_311_JPCD_STAL_123);
        when(checkReconciliationDao.findByCheckNumber("31111111", CrTestConstants.JPMC_BANK_CODE)).thenReturn(mockedCheckReconciliation);

        mockedCheckReconciliation = createMockCheckReconciliation(CheckReconciliationFixture.CHECK_411_JPCD_EXCP_123);
        when(checkReconciliationDao.findByCheckNumber("41111111", CrTestConstants.JPMC_BANK_CODE)).thenReturn(mockedCheckReconciliation);

        mockedCheckReconciliation = createMockCheckReconciliation(CheckReconciliationFixture.CHECK_511_JPCD_CDIS_321);
        when(checkReconciliationDao.findByCheckNumber("51111111", CrTestConstants.JPMC_BANK_CODE)).thenReturn(mockedCheckReconciliation);
        return checkReconciliationDao;
    }

    private static CheckReconciliation createMockCheckReconciliation(CheckReconciliationFixture checkReconciliationFixture) {
        CheckReconciliation checkReconciliation = mock(CheckReconciliation.class);

        when(checkReconciliation.getStatus()).thenReturn(checkReconciliationFixture.checkStatus);
        when(checkReconciliation.getCheckNumber()).thenReturn(checkReconciliationFixture.checkNumber);
        when(checkReconciliation.getBankCode()).thenReturn(checkReconciliationFixture.bankCode);
        when(checkReconciliation.getAmount()).thenReturn(checkReconciliationFixture.checkTotalAmount);

        return checkReconciliation;
    }

}
