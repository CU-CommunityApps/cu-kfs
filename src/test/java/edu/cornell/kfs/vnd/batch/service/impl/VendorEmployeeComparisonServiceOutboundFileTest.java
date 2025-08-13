package edu.cornell.kfs.vnd.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.impl.datetime.DateTimeServiceImpl;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSConstants.FileExtensions;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.SpringXmlTestBeanFactoryMethod;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.vnd.CuVendorTestConstants.VendorSpringBeans;
import edu.cornell.kfs.vnd.batch.VendorEmployeeComparisonCsv;
import edu.cornell.kfs.vnd.batch.VendorEmployeeComparisonResultCsvInputFileType;
import edu.cornell.kfs.vnd.batch.service.VendorEmployeeComparisonReportService;
import edu.cornell.kfs.vnd.batch.service.impl.annotation.VendorComparisonRow;
import edu.cornell.kfs.vnd.batch.service.impl.annotation.VendorComparisonRows;
import edu.cornell.kfs.vnd.businessobject.VendorWithTaxId;
import edu.cornell.kfs.vnd.dataaccess.CuVendorDao;

@Execution(ExecutionMode.SAME_THREAD)
@CreateTestDirectories(
        baseDirectory = VendorEmployeeComparisonServiceOutboundFileTest.TEST_VND_DIRECTORY,
        subDirectories = {
                VendorEmployeeComparisonServiceOutboundFileTest.TEST_VND_REPORTS_DIRECTORY,
                VendorEmployeeComparisonServiceOutboundFileTest.TEST_VND_OUTBOUND_FILE_EXPORT_DIRECTORY,
                VendorEmployeeComparisonServiceOutboundFileTest.TEST_VND_OUTBOUND_FILE_CREATION_DIRECTORY
        }
)
public class VendorEmployeeComparisonServiceOutboundFileTest {

    static final String TEST_VND_DIRECTORY = "test/vnd_compare_out/";
    static final String TEST_VND_REPORTS_DIRECTORY = TEST_VND_DIRECTORY + "reports/vnd/";
    static final String TEST_VND_STAGING_DIRECTORY = TEST_VND_DIRECTORY + "staging/vnd/";
    static final String TEST_VND_OUTBOUND_FILE_EXPORT_DIRECTORY =
            TEST_VND_STAGING_DIRECTORY + "emplCompareWorkday/outbound/";
    static final String TEST_VND_OUTBOUND_FILE_CREATION_DIRECTORY =
            TEST_VND_OUTBOUND_FILE_EXPORT_DIRECTORY + "being-written/";

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/vnd/batch/service/impl/cu-spring-vnd-empl-comparison-outbound-test.xml");

    private static VendorComparisonRow[] sourceDataRows;
    private static boolean forceExceptionWithinDaoMethodCall;
    private static AtomicBoolean streamFromDaoWasClosed;

    private VendorEmployeeComparisonServiceImpl vendorEmployeeComparisonService;

    @BeforeEach
    void setUp() throws Exception {
        sourceDataRows = new VendorComparisonRow[0];
        forceExceptionWithinDaoMethodCall = false;
        streamFromDaoWasClosed = new AtomicBoolean(false);
        vendorEmployeeComparisonService = springContextExtension.getBean(
                VendorSpringBeans.VENDOR_EMPLOYEE_COMPARISON_SERVICE, VendorEmployeeComparisonServiceImpl.class);
    }

    private void initializeVendorQuerySourceData(final VendorComparisonRows testCaseFixture) {
        sourceDataRows = testCaseFixture.value();
    }

    @SpringXmlTestBeanFactoryMethod
    public static CuVendorDao buildMockVendorDao() {
        final CuVendorDao vendorDao = Mockito.mock(CuVendorDao.class);
        Mockito.when(vendorDao.getPotentialEmployeeVendorsAsCloseableStream())
                .then(invocation -> getMockDaoSearchResults());
        return vendorDao;
    }

    private static Stream<VendorWithTaxId> getMockDaoSearchResults() {
        if (forceExceptionWithinDaoMethodCall) {
            throw new RuntimeException("Forcibly throwing exception for "
                    + "getPotentialEmployeeVendorsAsCloseableStream() method call");
        }
        return Arrays.stream(sourceDataRows)
                .onClose(() -> recordClosingOfStream())
                .map(VendorComparisonRow.Converters::toVendorWithTaxId);
    }

    private static void recordClosingOfStream() {
        streamFromDaoWasClosed.set(true);
    }

    @SpringXmlTestBeanFactoryMethod
    public static DateTimeService buildTestDateTimeService() {
        return new DateTimeServiceImpl();
    }

    @SpringXmlTestBeanFactoryMethod
    public static VendorEmployeeComparisonResultCsvInputFileType buildMockVendorEmployeeComparisonResultFileType() {
        return Mockito.mock(VendorEmployeeComparisonResultCsvInputFileType.class);
    }

    @SpringXmlTestBeanFactoryMethod
    public static VendorEmployeeComparisonReportService buildMockVendorEmployeeComparisonReportService() {
        return Mockito.mock(VendorEmployeeComparisonReportService.class);
    }

    @SpringXmlTestBeanFactoryMethod
    public static BatchInputFileService buildMockBatchInputFileService() {
        return Mockito.mock(BatchInputFileService.class);
    }

    @AfterEach
    void shutDown() throws Exception {
        vendorEmployeeComparisonService = null;
        streamFromDaoWasClosed = null;
        sourceDataRows = null;
    }



    enum LocalTestCase {
        @VendorComparisonRows({})
        EMPTY_SOURCE,

        @VendorComparisonRows({
            @VendorComparisonRow(vendorId = "12345-0", taxId = "xxxxx1111")
        })
        SINGLE_VENDOR,

        @VendorComparisonRows({
            @VendorComparisonRow(vendorId = "12345-0", taxId = "xxxxx1111"),
            @VendorComparisonRow(vendorId = "44444-0", taxId = "xxxxx2222"),
            @VendorComparisonRow(vendorId = "78787-0", taxId = "xxxxx9876")
        })
        MULTIPLE_VENDORS,

        @VendorComparisonRows({
            @VendorComparisonRow(vendorId = "12345-0", taxId = "xxxxx1111", forceException = true)
        })
        SINGLE_VENDOR_FORCE_EXCEPTION,

        @VendorComparisonRows({
            @VendorComparisonRow(vendorId = "12345-0", taxId = "xxxxx1111"),
            @VendorComparisonRow(vendorId = "44444-0", taxId = "xxxxx2222", forceException = true),
            @VendorComparisonRow(vendorId = "78787-0", taxId = "xxxxx9876")
        })
        MULTIPLE_VENDORS_FORCE_EXCEPTION;

        public Arguments toNamedAnnotationFixtureArgument() {
            return FixtureUtils.createNamedAnnotationFixtureArgument(this, VendorComparisonRows.class);
        }
    }

    static Stream<Arguments> allTestCases() {
        return Arrays.stream(LocalTestCase.values())
                .map(LocalTestCase::toNamedAnnotationFixtureArgument);
    }

    static Stream<Arguments> standardTestCases() {
        return Stream.of(
                LocalTestCase.EMPTY_SOURCE,
                LocalTestCase.SINGLE_VENDOR,
                LocalTestCase.MULTIPLE_VENDORS
        ).map(LocalTestCase::toNamedAnnotationFixtureArgument);
    }

    static Stream<Arguments> exceptionTestCases() {
        return Stream.of(
                LocalTestCase.SINGLE_VENDOR_FORCE_EXCEPTION,
                LocalTestCase.MULTIPLE_VENDORS_FORCE_EXCEPTION
        ).map(LocalTestCase::toNamedAnnotationFixtureArgument);
    }



    @ParameterizedTest
    @MethodSource("standardTestCases")
    void testSuccessfulCsvFilePreparation(final VendorComparisonRows testCaseFixture) throws Exception {
        initializeVendorQuerySourceData(testCaseFixture);
        assertCsvFilePreparationSucceeds(testCaseFixture);
    }

    @ParameterizedTest
    @MethodSource("exceptionTestCases")
    void testCsvFilePreparationFailureWhileReadingSourceDataFromStream(
            final VendorComparisonRows testCaseFixture) throws Exception {
        initializeVendorQuerySourceData(testCaseFixture);
        assertCsvFilePreparationFails();
    }

    @ParameterizedTest
    @MethodSource("allTestCases")
    void testCsvFilePreparationFailureWhileCreatingSourceDataStream(
            final VendorComparisonRows testCaseFixture) throws Exception {
        initializeVendorQuerySourceData(testCaseFixture);
        forceExceptionWithinDaoMethodCall = true;
        assertCsvFilePreparationFails();
    }

    private void assertCsvFilePreparationSucceeds(final VendorComparisonRows testCaseFixture) throws Exception {
        vendorEmployeeComparisonService.generateFileContainingPotentialVendorEmployees();

        assertSourceDataStreamHasExpectedClosingState();
        if (isCsvFileExpectedToBeNonEmpty(testCaseFixture)) {
            assertCsvFileHasExpectedContent(testCaseFixture);
        } else {
            assertCsvFileIsEmptyAndUnstagedOrIsNotPresent();
        }
    }

    private void assertCsvFilePreparationFails() throws Exception {
        assertThrows(RuntimeException.class,
                () -> vendorEmployeeComparisonService.generateFileContainingPotentialVendorEmployees(),
                "The CSV file generation process should have encountered an exception");

        assertSourceDataStreamHasExpectedClosingState();
        final Optional<File> unstagedCsvFile = assertAndGetUnstagedCsvFileIfPresent();
        if (forceExceptionWithinDaoMethodCall) {
            assertTrue(unstagedCsvFile.isEmpty(),
                    "The application exception should have been thrown prior to generating the CSV file");
        }
    }

    private void assertSourceDataStreamHasExpectedClosingState() throws Exception {
        if (forceExceptionWithinDaoMethodCall) {
            assertFalse(streamFromDaoWasClosed.get(), "The application exception should have been thrown prior to "
                    + "the point where a closeable stream would even be available for closing");
        } else {
            assertTrue(streamFromDaoWasClosed.get(), "The vendor source data stream should have been closed");
        }
    }

    private boolean isCsvFileExpectedToBeNonEmpty(final VendorComparisonRows testCaseFixture) {
        final VendorComparisonRow[] expectedRows = testCaseFixture.value();
        return expectedRows.length > 0;
    }

    private void assertCsvFileHasExpectedContent(final VendorComparisonRows testCaseFixture) throws Exception {
        final File csvFile = assertAndGetStagedCsvFile();
        try (
                final LineIterator csvLineIterator = FileUtils.lineIterator(csvFile, StandardCharsets.UTF_8.name())
        ) {
            assertTrue(csvLineIterator.hasNext(), "The CSV file should not have been empty");
            final String expectedHeaderRow = getExpectedCsvHeaderRow();
            final String actualHeaderRow = csvLineIterator.next();
            assertEquals(expectedHeaderRow, actualHeaderRow, "Wrong CSV header row content");
            assertCsvFileHasExpectedDataRows(testCaseFixture, csvLineIterator);
        }
    }

    private void assertCsvFileHasExpectedDataRows(
            final VendorComparisonRows testCaseFixture, final LineIterator csvLineIterator) throws Exception {
        final VendorComparisonRow[] expectedRows = testCaseFixture.value();
        int dataLineIndex = 0;

        while (csvLineIterator.hasNext()) {
            assertTrue(dataLineIndex < expectedRows.length,
                    "The CSV file had more than the expected " + expectedRows.length + " data rows");
            final String expectedRow = VendorComparisonRow.Converters.toCsvRow(expectedRows[dataLineIndex]);
            final String actualRow = csvLineIterator.next();
            assertEquals(expectedRow, actualRow, "Wrong CSV data on file line " + (dataLineIndex + 2));
            dataLineIndex++;
        }

        assertEquals(expectedRows.length, dataLineIndex, "Wrong number of data rows in CSV file");
    }

    private File assertAndGetStagedCsvFile() {
        final File csvCreationDirectory = new File(TEST_VND_OUTBOUND_FILE_CREATION_DIRECTORY);
        final File csvExportDirectory = new File(TEST_VND_OUTBOUND_FILE_EXPORT_DIRECTORY);

        final Collection<File> creationDirectoryCsvFiles = FileUtils.listFiles(csvCreationDirectory, null, false);
        assertTrue(CollectionUtils.isEmpty(creationDirectoryCsvFiles),
                "The directory for temporarily storing the generated CSV file should have been empty");

        final Collection<File> exportDirectoryCsvFiles = FileUtils.listFiles(csvExportDirectory, null, false);
        assertEquals(1, CollectionUtils.size(exportDirectoryCsvFiles),
                "Wrong number of files in CSV export directory");

        final File csvFile = exportDirectoryCsvFiles.iterator().next();
        assertNotNull(csvFile, "The generated CSV file should have been present in the export directory");
        assertTrue(StringUtils.endsWithIgnoreCase(csvFile.getAbsolutePath(), FileExtensions.CSV),
                "The generated file should have been a .csv file");
        return csvFile;
    }

    private void assertCsvFileIsEmptyAndUnstagedOrIsNotPresent() throws Exception {
        final Optional<File> unstagedCsvFile = assertAndGetUnstagedCsvFileIfPresent();
        if (unstagedCsvFile.isPresent()) {
            final File csvFile = unstagedCsvFile.get();
            final String fileContents = FileUtils.readFileToString(csvFile, StandardCharsets.UTF_8);
            assertTrue(StringUtils.isBlank(fileContents), "The unstaged CSV file should have been empty");
        }
    }

    private Optional<File> assertAndGetUnstagedCsvFileIfPresent() {
        final File csvCreationDirectory = new File(TEST_VND_OUTBOUND_FILE_CREATION_DIRECTORY);
        final File csvExportDirectory = new File(TEST_VND_OUTBOUND_FILE_EXPORT_DIRECTORY);

        final Collection<File> exportDirectoryCsvFiles = FileUtils.listFiles(csvExportDirectory, null, false);
        assertTrue(CollectionUtils.isEmpty(exportDirectoryCsvFiles),
                "The directory for storing the exportable CSV files should have been empty");

        final Collection<File> creationDirectoryCsvFiles = FileUtils.listFiles(csvCreationDirectory, null, false);
        final int numCsvFiles = CollectionUtils.size(creationDirectoryCsvFiles);
        if (numCsvFiles != 0) {
            assertTrue(numCsvFiles == 1, "There should have been 0 or 1 unstaged CSV files, "
                    + "but the actual file count was: " + numCsvFiles);
            final File csvFile = creationDirectoryCsvFiles.iterator().next();
            assertNotNull(csvFile, "The unstaged CSV file should have been present in the 'being-written' directory");
            assertTrue(StringUtils.endsWithIgnoreCase(csvFile.getAbsolutePath(), FileExtensions.CSV),
                    "The unstaged file should have been a .csv file");
            return Optional.of(csvFile);
        } else {
            return Optional.empty();
        }
    }

    private String getExpectedCsvHeaderRow() {
        return Arrays.stream(VendorEmployeeComparisonCsv.values())
                .map(VendorEmployeeComparisonCsv::getHeaderLabel)
                .collect(Collectors.joining(
                        CUKFSConstants.COMMA_WITH_QUOTES, CUKFSConstants.DOUBLE_QUOTE, CUKFSConstants.DOUBLE_QUOTE));
    }

}
