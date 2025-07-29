package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.CollectorBatch;
import org.kuali.kfs.gl.batch.CollectorBatchHeaderFieldUtil;
import org.kuali.kfs.gl.batch.CollectorBatchTrailerRecordFieldUtil;
import org.kuali.kfs.gl.businessobject.OriginEntryFieldUtil;
import org.kuali.kfs.kns.lookup.LookupableHelperService;
import org.kuali.kfs.sys.businessobject.service.SearchService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.BusinessObjectStringParserFieldUtils;
import org.kuali.kfs.sys.businessobject.service.impl.BatchFileSearchService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.impl.datetime.DateTimeServiceImpl;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.concur.batch.businessobject.BusinessObjectFlatFileSerializerFieldUtils;
import edu.cornell.kfs.concur.batch.businessobject.CollectorBatchHeaderSerializerFieldUtil;
import edu.cornell.kfs.concur.batch.businessobject.CollectorBatchTrailerRecordSerializerFieldUtil;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.OriginEntrySerializerFieldUtil;
import edu.cornell.kfs.concur.batch.fixture.ConcurCollectorBatchFixture;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.BusinessObjectFlatFileSerializerService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractCreateCollectorFileService;

@SuppressWarnings("deprecation")
public class ConcurStandardAccountingExtractCreateCollectorFileServiceImplTest {

    protected static final String GET_FIELD_LENGTH_MAP_METHOD = "getFieldLengthMap";
    protected static final String EXPECTED_RESULTS_DIRECTORY_PATH = "src/test/resources/edu/cornell/kfs/concur/batch/service/impl/fixture/";
    protected static final String BASE_TEST_DIRECTORY = "test";
    protected static final String COLLECTOR_OUTPUT_DIRECTORY_PATH = BASE_TEST_DIRECTORY + "/gl/collectorFlatFile/";

    protected ConcurStandardAccountingExtractCreateCollectorFileService collectorFileService;

    @Before
    public void setUp() throws Exception {
        buildCollectorOutputDirectory();
        collectorFileService = buildCollectorFileService();
    }

    @After
    public void tearDown() throws Exception {
        collectorFileService = null;
        removeTestFilesAndDirectories();
    }

    @Test
    public void testGenerateFileWithMinimalLineItems() throws Exception {
        assertCollectorFileIsGeneratedCorrectly("merging-test.data", ConcurCollectorBatchFixture.MERGING_TEST);
    }

    @Test
    public void testBatchSequenceNumberIncrementsWhenGeneratingMultipleFilesOnSameDay() throws Exception {
        assertCollectorFileIsGeneratedCorrectly("fiscal-year-test1.data", ConcurCollectorBatchFixture.FISCAL_YEAR_TEST1);
        assertCollectorFileIsGeneratedCorrectly("fiscal-year-test2.data", ConcurCollectorBatchFixture.FISCAL_YEAR_TEST2);
        assertCollectorFileIsGeneratedCorrectly("fiscal-year-test3.data", ConcurCollectorBatchFixture.FISCAL_YEAR_TEST3);
    }

    @Test
    public void testCollectorFileNotGeneratedWhenBuilderFails() throws Exception {
        assertCollectorFileIsNotGenerated("NON_EXISTENT_FIXTURE");
    }

    protected void assertCollectorFileIsGeneratedCorrectly(String expectedResultsFileName,
            ConcurCollectorBatchFixture fixture) throws Exception {
        ConcurStandardAccountingExtractBatchReportData reportData = new ConcurStandardAccountingExtractBatchReportData();
        ConcurStandardAccountingExtractFile saeFileContents = new ConcurStandardAccountingExtractFile();
        saeFileContents.setOriginalFileName(fixture.name() + CuGeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION);
        
        String collectorFileName = collectorFileService.buildCollectorFile(saeFileContents, reportData);
        assertTrue("Collector file did not get created successfully", StringUtils.isNotBlank(collectorFileName));
        
        assertGeneratedCollectorFileHasCorrectName(saeFileContents.getOriginalFileName(), collectorFileName);
        
        String expectedResultsFilePath = EXPECTED_RESULTS_DIRECTORY_PATH + expectedResultsFileName;
        String actualResultsFilePath = COLLECTOR_OUTPUT_DIRECTORY_PATH + collectorFileName;
        assertFileContentsAreCorrect(expectedResultsFilePath, actualResultsFilePath);
    }

    protected void assertGeneratedCollectorFileHasCorrectName(String originalFileName, String collectorFileName) throws Exception {
        assertEquals("File name has too many extension delimiters",
                StringUtils.countMatches(originalFileName, KFSConstants.DELIMITER),
                StringUtils.countMatches(collectorFileName, KFSConstants.DELIMITER));
        assertFalse("Did not find a file extension after the delimiter", StringUtils.endsWith(collectorFileName, KFSConstants.DELIMITER));
        
        String collectorFilePrefix = StringUtils.left(collectorFileName, ConcurConstants.COLLECTOR_CONCUR_OUTPUT_FILE_NAME_PREFIX.length());
        String collectorFileExtension = KFSConstants.DELIMITER + StringUtils.substringAfterLast(collectorFileName, KFSConstants.DELIMITER);
        assertEquals("Collector file has the wrong prefix", ConcurConstants.COLLECTOR_CONCUR_OUTPUT_FILE_NAME_PREFIX, collectorFilePrefix);
        assertEquals("Collector file has the wrong extension", GeneralLedgerConstants.BatchFileSystem.EXTENSION, collectorFileExtension);
        
        String originalFileNameWithoutExtension = StringUtils.substringBeforeLast(originalFileName, KFSConstants.DELIMITER);
        assertEquals("Collector file should have had the original SAE file's name embedded within it", originalFileNameWithoutExtension,
                StringUtils.substringBetween(collectorFileName, collectorFilePrefix, collectorFileExtension));
    }

    protected void assertFileContentsAreCorrect(String expectedResultsFilePath, String actualResultsFilePath) throws Exception {
        FileReader expectedFileContents = null;
        BufferedReader expectedContents = null;
        FileReader actualFileContents = null;
        BufferedReader actualContents = null;
        
        try {
            expectedFileContents = new FileReader(expectedResultsFilePath);
            expectedContents = new BufferedReader(expectedFileContents);
            actualFileContents = new FileReader(actualResultsFilePath);
            actualContents = new BufferedReader(actualFileContents);
            
            int lineCount = 0;
            String expectedLine = expectedContents.readLine();
            String actualLine = actualContents.readLine();
            
            while (expectedLine != null) {
                lineCount++;
                assertNotNull("Collector file had only " + (lineCount - 1) + " lines, but should have had more than that", actualLine);
                assertEquals("Wrong Collector file content at line " + lineCount, expectedLine, actualLine);
                expectedLine = expectedContents.readLine();
                actualLine = actualContents.readLine();
            }
            
            assertNull("Collector file should have had only " + lineCount + " lines, but actually had more than that", actualLine);
            
        } finally {
            IOUtils.closeQuietly(actualContents);
            IOUtils.closeQuietly(actualFileContents);
            IOUtils.closeQuietly(expectedContents);
            IOUtils.closeQuietly(expectedFileContents);
        }
    }

    protected void assertCollectorFileIsNotGenerated(String fixtureName) throws Exception {
        ConcurStandardAccountingExtractBatchReportData reportData = new ConcurStandardAccountingExtractBatchReportData();
        ConcurStandardAccountingExtractFile saeFileContents = new ConcurStandardAccountingExtractFile();
        saeFileContents.setOriginalFileName(fixtureName + CuGeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION);
        
        String collectorFilePath = collectorFileService.buildCollectorFile(saeFileContents, reportData);
        assertTrue("A Collector file should not have been created", StringUtils.isBlank(collectorFilePath));
    }

    protected void buildCollectorOutputDirectory() throws IOException {
        File collectorTestDirectory = new File(COLLECTOR_OUTPUT_DIRECTORY_PATH);
        FileUtils.forceMkdir(collectorTestDirectory);
    }

    protected void removeTestFilesAndDirectories() throws IOException {
        File collectorTestDirectory = new File(COLLECTOR_OUTPUT_DIRECTORY_PATH);
        if (collectorTestDirectory.exists() && collectorTestDirectory.isDirectory()) {
            FileUtils.forceDelete(collectorTestDirectory.getAbsoluteFile());
        }
    }

    protected ConcurStandardAccountingExtractCreateCollectorFileService buildCollectorFileService() throws Exception {
        DateTimeService dateTimeService = buildDateTimeService();
        TestConcurStandardAccountingExtractCreateCollectorFileServiceImpl collectorFileServiceImpl =
                new TestConcurStandardAccountingExtractCreateCollectorFileServiceImpl();
        
        collectorFileServiceImpl.setDateTimeService(dateTimeService);
        collectorFileServiceImpl.setBatchFileLookupableHelperService(buildBatchFileLookupableHelperService(dateTimeService));
        collectorFileServiceImpl.setCollectorBatchBuilder(buildMockBatchBuilder());
        collectorFileServiceImpl.setStagingDirectoryPath(BASE_TEST_DIRECTORY);
        collectorFileServiceImpl.setCollectorDirectoryPath(COLLECTOR_OUTPUT_DIRECTORY_PATH);
        collectorFileServiceImpl.setCollectorFlatFileSerializerService(buildCollectorFlatFileSerializerService(dateTimeService));
        
        return collectorFileServiceImpl;
    }

    protected DateTimeService buildDateTimeService() {
        return new TestDateTimeServiceImpl();
    }

    protected SearchService buildBatchFileLookupableHelperService(DateTimeService dateTimeService) {
    	SearchService lookupableHelperServiceImpl = new TestBatchFileLookupSearchServiceImpl();
        return lookupableHelperServiceImpl;
    }

    protected ConcurStandardAccountingExtractCollectorBatchBuilder buildMockBatchBuilder() {
        ConcurStandardAccountingExtractCollectorBatchBuilder batchBuilder = mock(ConcurStandardAccountingExtractCollectorBatchBuilder.class);
        when(batchBuilder.buildCollectorBatchFromStandardAccountingExtract(
                anyInt(), any(ConcurStandardAccountingExtractFile.class), any(ConcurStandardAccountingExtractBatchReportData.class)))
                .thenAnswer(this::buildFixtureBasedCollectorBatch);
        return batchBuilder;
    }

    protected CollectorBatch buildFixtureBasedCollectorBatch(InvocationOnMock invocation) {
        Integer sequenceNumber = invocation.getArgument(0);
        ConcurStandardAccountingExtractFile saeFileContents = invocation.getArgument(1);
        String fixtureConstantName = StringUtils.substringBeforeLast(saeFileContents.getOriginalFileName(), KFSConstants.DELIMITER);
        ConcurCollectorBatchFixture fixture;
        
        try {
            fixture = ConcurCollectorBatchFixture.valueOf(fixtureConstantName);
        } catch (IllegalArgumentException | NullPointerException e) {
            return null;
        }
        
        CollectorBatch collectorBatch = fixture.toCollectorBatch();
        collectorBatch.setBatchSequenceNumber(sequenceNumber);
        return collectorBatch;
    }

    protected <T> T buildMockObject(Class<T> objectClass, Consumer<T> objectConfigurer) {
        T mockObject = mock(objectClass);
        objectConfigurer.accept(mockObject);
        return mockObject;
    }

    protected BusinessObjectFlatFileSerializerService buildCollectorFlatFileSerializerService(
            DateTimeService dateTimeService) throws Exception {
        CollectorFlatFileSerializerServiceImpl serializerServiceImpl = new CollectorFlatFileSerializerServiceImpl();
        
        serializerServiceImpl.setHeaderSerializerUtils(buildBatchHeaderSerializerFieldUtil(dateTimeService));
        serializerServiceImpl.setLineItemSerializerUtils(buildOriginEntrySerializerFieldUtil(dateTimeService));
        serializerServiceImpl.setFooterSerializerUtils(buildBatchTrailerRecordSerializerFieldUtil());
        
        return serializerServiceImpl;
    }

    protected CollectorBatchHeaderSerializerFieldUtil buildBatchHeaderSerializerFieldUtil(
            DateTimeService dateTimeService) throws Exception {
        return buildSerializerFieldUtils(CollectorBatchHeaderSerializerFieldUtil.class, buildMockBatchHeaderFieldUtil(),
                (serializerFieldUtils) -> serializerFieldUtils.setDateTimeService(dateTimeService));
    }

    protected OriginEntrySerializerFieldUtil buildOriginEntrySerializerFieldUtil(
            DateTimeService dateTimeService) throws Exception {
        return buildSerializerFieldUtils(OriginEntrySerializerFieldUtil.class, buildMockOriginEntryFieldUtil(),
                (serializerFieldUtils) -> serializerFieldUtils.setDateTimeService(dateTimeService));
    }

    protected CollectorBatchTrailerRecordSerializerFieldUtil buildBatchTrailerRecordSerializerFieldUtil() throws Exception {
        return buildSerializerFieldUtils(CollectorBatchTrailerRecordSerializerFieldUtil.class,
                buildMockBatchTrailerRecordFieldUtil(), (serializerFieldUtils) -> {});
    }

    protected <T extends BusinessObjectFlatFileSerializerFieldUtils> T buildSerializerFieldUtils(
            Class<T> serializerUtilsClass, BusinessObjectStringParserFieldUtils parserFieldUtils,
            Consumer<T> utilsConfigurer) throws Exception {
        T serializerFieldUtils = serializerUtilsClass.newInstance();
        serializerFieldUtils.setParserFieldUtils(parserFieldUtils);
        utilsConfigurer.accept(serializerFieldUtils);
        return serializerFieldUtils;
    }

    protected CollectorBatchHeaderFieldUtil buildMockBatchHeaderFieldUtil() {
        Map<String,Integer> fieldLengthMap = new HashMap<>();
        fieldLengthMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, 4);
        fieldLengthMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.ORGANIZATION_CODE, 4);
        fieldLengthMap.put(KFSPropertyConstants.TRANSMISSION_DATE, 15);
        fieldLengthMap.put(KFSPropertyConstants.COLLECTOR_BATCH_RECORD_TYPE, 2);
        fieldLengthMap.put(KFSPropertyConstants.BATCH_SEQUENCE_NUMBER, 1);
        fieldLengthMap.put(KFSPropertyConstants.KUALI_USER_PERSON_EMAIL_ADDRESS, 40);
        fieldLengthMap.put(KFSPropertyConstants.COLLECTOR_BATCH_PERSON_USER_ID, 30);
        fieldLengthMap.put(KFSPropertyConstants.DEPARTMENT_NAME, 30);
        fieldLengthMap.put(KFSPropertyConstants.MAILING_ADDRESS, 30);
        fieldLengthMap.put(KFSPropertyConstants.CAMPUS_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.PHONE_NUMBER, 10);
        
        return buildMockParserFieldUtils(new CollectorBatchHeaderFieldUtil(), fieldLengthMap);
    }

    protected OriginEntryFieldUtil buildMockOriginEntryFieldUtil() {
        Map<String,Integer> fieldLengthMap = new HashMap<>();
        fieldLengthMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, 4);
        fieldLengthMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.ACCOUNT_NUMBER, 7);
        fieldLengthMap.put(KFSPropertyConstants.SUB_ACCOUNT_NUMBER, 5);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, 4);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_SUB_OBJECT_CODE, 3);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_OBJECT_TYPE_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_DOCUMENT_TYPE_CODE, 4);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_SYSTEM_ORIGINATION_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.DOCUMENT_NUMBER, 14);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_ENTRY_SEQUENCE_NUMBER, 5);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_DESC, 40);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_LEDGER_ENTRY_AMOUNT, 21);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_DEBIT_CREDIT_CODE, 1);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_DATE, 10);
        fieldLengthMap.put(KFSPropertyConstants.ORGANIZATION_DOCUMENT_NUMBER, 10);
        fieldLengthMap.put(KFSPropertyConstants.PROJECT_CODE, 10);
        fieldLengthMap.put(KFSPropertyConstants.ORGANIZATION_REFERENCE_ID, 8);
        fieldLengthMap.put(KFSPropertyConstants.REFERENCE_FIN_DOCUMENT_TYPE_CODE, 4);
        fieldLengthMap.put(KFSPropertyConstants.FIN_SYSTEM_REF_ORIGINATION_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_DOCUMENT_REFERENCE_NBR, 14);
        fieldLengthMap.put(KFSPropertyConstants.FINANCIAL_DOCUMENT_REVERSAL_DATE, 10);
        fieldLengthMap.put(KFSPropertyConstants.TRANSACTION_ENCUMBRANCE_UPDT_CD, 1);
        
        return buildMockParserFieldUtils(new OriginEntryFieldUtil(), fieldLengthMap);
    }

    protected CollectorBatchTrailerRecordFieldUtil buildMockBatchTrailerRecordFieldUtil() {
        Map<String,Integer> fieldLengthMap = new HashMap<>();
        fieldLengthMap.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, 4);
        fieldLengthMap.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, 2);
        fieldLengthMap.put(KFSPropertyConstants.ORGANIZATION_CODE, 4);
        fieldLengthMap.put(KFSPropertyConstants.TRANSMISSION_DATE, 15);
        fieldLengthMap.put(KFSPropertyConstants.COLLECTOR_BATCH_RECORD_TYPE, 2);
        fieldLengthMap.put(KFSPropertyConstants.TRAILER_RECORD_FIRST_EMPTY_FIELD, 19);
        fieldLengthMap.put(KFSPropertyConstants.TOTAL_RECORDS, 5);
        fieldLengthMap.put(KFSPropertyConstants.TRAILER_RECORD_SECOND_EMPTY_FIELD, 42);
        fieldLengthMap.put(KFSPropertyConstants.TOTAL_AMOUNT, 19);
        
        return buildMockParserFieldUtils(new CollectorBatchTrailerRecordFieldUtil(), fieldLengthMap);
    }

    /**
     * The BusinessObjectStringParserFieldUtils.getFieldLengthMap() method relies on a private method
     * to initialize the field length map, which in turn makes calls to SpringContext.getBean()
     * to retrieve the DataDictionaryService. To allow for micro-testing of such classes
     * without introducing several new subclasses, this method will create a partial mock
     * of the class so that the "getFieldLengthMap" method will return a pre-defined Map instead.
     */
    protected <T extends BusinessObjectStringParserFieldUtils> T buildMockParserFieldUtils(
            T fieldUtilsImpl, Map<String,Integer> fieldLengthMap) {
        T fieldUtilsSpy = spy(fieldUtilsImpl);
        doReturn(fieldLengthMap)
                .when(fieldUtilsSpy).getFieldLengthMap();
        return fieldUtilsSpy;
    }

    /**
     * Custom collector file creation service that allows for configuring a single CollectorBatch builder directly,
     * which will always be used for the SAE-to-CollectorBatch processing.
     */
    public static class TestConcurStandardAccountingExtractCreateCollectorFileServiceImpl
            extends ConcurStandardAccountingExtractCreateCollectorFileServiceImpl {
        protected ConcurStandardAccountingExtractCollectorBatchBuilder collectorBatchBuilder;
        
        public void setCollectorBatchBuilder(ConcurStandardAccountingExtractCollectorBatchBuilder collectorBatchBuilder) {
            this.collectorBatchBuilder = collectorBatchBuilder;
        }
        
        @Override
        protected ConcurStandardAccountingExtractCollectorBatchBuilder createBatchBuilder() {
            return collectorBatchBuilder;
        }
    }
    
    /**
     * Custom batch file lookupable class that uses a pre-defined List of root directories.
     */
    public static class TestBatchFileLookupSearchServiceImpl extends BatchFileSearchService {
        private static final long serialVersionUID = 1L;

        @Override
        protected List<File> getDirectoriesToSearch(List<String> selectedPaths) {
            return Collections.singletonList(new File(COLLECTOR_OUTPUT_DIRECTORY_PATH).getAbsoluteFile());
        }
    }


    /**
     * Custom DateTimeService class that is configured to automatically handle certain Concur date formats.
     */
    public static class TestDateTimeServiceImpl extends DateTimeServiceImpl {
        
        public TestDateTimeServiceImpl() {
        }
    }

}
