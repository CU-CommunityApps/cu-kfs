package edu.cornell.kfs.vnd.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.SpringXmlBeanFactoryMethod;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.vnd.CuVendorTestConstants.VendorSpringBeans;
import edu.cornell.kfs.vnd.batch.VendorEmployeeComparisonResultCsv;
import edu.cornell.kfs.vnd.batch.service.VendorEmployeeComparisonService;
import edu.cornell.kfs.vnd.batch.service.impl.annotation.VendorComparisonResult;
import edu.cornell.kfs.vnd.batch.service.impl.annotation.VendorComparisonResultRow;
import edu.cornell.kfs.vnd.batch.service.impl.fixture.VendorComparisonResultRowFixture;
import edu.cornell.kfs.vnd.dataaccess.CuVendorDao;

@Execution(ExecutionMode.SAME_THREAD)
@CreateTestDirectories(
        baseDirectory = VendorEmployeeComparisonServiceResultFileTest.TEST_VND_DIRECTORY,
        subDirectories = {
                VendorEmployeeComparisonServiceResultFileTest.TEST_VND_REPORTS_DIRECTORY,
                VendorEmployeeComparisonServiceResultFileTest.TEST_VND_EMPL_RESULTS_DIRECTORY
        }
)
public class VendorEmployeeComparisonServiceResultFileTest {

    private static final String TEST_VND_DIRECTORY = "test/vnd_empl_results/";
    private static final String TEST_VND_REPORTS_DIRECTORY = TEST_VND_DIRECTORY + "reports/vnd/";
    private static final String TEST_VND_STAGING_DIRECTORY = TEST_VND_DIRECTORY + "staging/vnd/";
    private static final String TEST_VND_EMPL_RESULTS_DIRECTORY =
            TEST_VND_STAGING_DIRECTORY + "emplCompareWorkday/result/";

    private static final String VND_EMPL_RESULTS_CSV_FILENAME_PATTERN = "empl_result_20240615_1230{0}.csv";
    private static final String VND_EMPL_RESULTS_DONE_FILENAME_PATTERN = "empl_result_20240615_1230{0}.done";

    private static final String BASE_TEST_REPORT_FILE_PATH =
            "classpath:edu/cornell/kfs/vnd/batch/service/impl/empl-compare-report/";

    @RegisterExtension
    TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/vnd/batch/service/impl/cu-spring-vnd-empl-comparison-result-test.xml");

    private VendorEmployeeComparisonService vendorEmployeeComparisonService;
    private Map<String, File> resultFileAndReportFilePairs;

    @BeforeEach
    void setUp() throws Exception {
        resultFileAndReportFilePairs = new HashMap<>();
        vendorEmployeeComparisonService = springContextExtension.getBean(
                VendorSpringBeans.VENDOR_EMPLOYEE_COMPARISON_SERVICE, VendorEmployeeComparisonService.class);
    }

    @SpringXmlBeanFactoryMethod
    public BiConsumer<String, File> buildTestReportFileTracker() {
        return this::trackResultFileAndReportFilePair;
    }

    private void trackResultFileAndReportFilePair(final String resultFile, final File reportFile) {
        assertFalse(resultFileAndReportFilePairs.containsKey(resultFile),
                "Unexpected attempt to track the following result file twice: " + resultFile);
        resultFileAndReportFilePairs.put(resultFile, reportFile);
    }

    @SpringXmlBeanFactoryMethod
    public CuVendorDao buildMockVendorDao() {
        return Mockito.mock(CuVendorDao.class);
    }

    @SpringXmlBeanFactoryMethod
    public DateTimeService buildTestDateTimeService() {
        return new TestDateTimeServiceImpl();
    }

    @SpringXmlBeanFactoryMethod
    public ParameterService buildMockParameterService() {
        return Mockito.mock(ParameterService.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        vendorEmployeeComparisonService = null;
        resultFileAndReportFilePairs = null;
    }



    private void createTestCsvFile(final VendorComparisonResult testCaseFixture) throws IOException {
        final String simpleFileName = generateFileName(testCaseFixture, VND_EMPL_RESULTS_CSV_FILENAME_PATTERN);
        final File csvFile = new File(TEST_VND_EMPL_RESULTS_DIRECTORY + simpleFileName);
        try (
                final FileOutputStream fileStream = new FileOutputStream(csvFile);
                final OutputStreamWriter streamWriter = new OutputStreamWriter(fileStream, StandardCharsets.UTF_8);
                final BufferedWriter fileWriter = new BufferedWriter(streamWriter);
        ) {
            if (testCaseFixture.writeCsvHeaderLine()) {
                if (StringUtils.isNotBlank(testCaseFixture.csvHeaderLineOverride())) {
                    fileWriter.write(testCaseFixture.csvHeaderLineOverride());
                } else {
                    final String csvHeader = Arrays.stream(VendorEmployeeComparisonResultCsv.values())
                            .map(VendorEmployeeComparisonResultCsv::name)
                            .collect(Collectors.joining(CUKFSConstants.COMMA_WITH_QUOTES,
                                    CUKFSConstants.DOUBLE_QUOTE, CUKFSConstants.DOUBLE_QUOTE));
                    fileWriter.write(csvHeader);
                }
                fileWriter.write(KFSConstants.NEWLINE);
            }

            for (final VendorComparisonResultRowFixture rowFixture : testCaseFixture.csvDataRows()) {
                final VendorComparisonResultRow rowFixtureData = FixtureUtils.getAnnotationBasedFixture(
                        rowFixture, VendorComparisonResultRow.class);
                final String csvRow = VendorComparisonResultRow.Converters.toCsvRow(rowFixtureData);
                fileWriter.write(csvRow);
                fileWriter.write(KFSConstants.NEWLINE);
            }

            fileWriter.flush();
        } 
    }

    private void createTestDoneFile(final VendorComparisonResult testCaseFixture) throws IOException {
        final String simpleFileName = generateFileName(testCaseFixture, VND_EMPL_RESULTS_DONE_FILENAME_PATTERN);
        final File doneFile = new File(TEST_VND_EMPL_RESULTS_DIRECTORY + simpleFileName);
        FileUtils.touch(doneFile);
    }

    private String generateFileName(final VendorComparisonResult testCaseFixture, final String fileNamePattern) {
        final String indexedValue = StringUtils.leftPad(
                String.valueOf(testCaseFixture.index()), 2, '0');
        return MessageFormat.format(fileNamePattern, indexedValue);
    }



    enum LocalTestCase {
        @VendorComparisonResult(
                index = 1,
                csvDataRows = {
                        VendorComparisonResultRowFixture.JOHN_DOE,
                },
                expectedReportFile = "rpt-single-active-row.txt"
        )
        FILE_WITH_SINGLE_ACTIVE_ROW;
    }



    @Test
    void doSomething() throws Exception {
        final LocalTestCase enumFixture = LocalTestCase.FILE_WITH_SINGLE_ACTIVE_ROW;
        final VendorComparisonResult testCaseFixture = FixtureUtils.getAnnotationBasedFixture(
                enumFixture, VendorComparisonResult.class);
        createTestCsvFile(testCaseFixture);
        createTestDoneFile(testCaseFixture);
        assertCsvFileProcessingSucceeds();
        assertOnlyCsvFilesRemainInResultsDirectory(testCaseFixture);
        assertReportFilesHaveExpectedContent(testCaseFixture);
    }

    private void assertCsvFileProcessingSucceeds() throws Exception {
        final boolean csvProcessingResult = vendorEmployeeComparisonService.processResultsOfVendorEmployeeComparison();
        assertTrue(csvProcessingResult, "The results CSV file should have been processed successfully");
    }

    private void assertReportFilesHaveExpectedContent(final VendorComparisonResult... fixtures) throws Exception {
        final Map<String, VendorComparisonResult> fixturesExpectingSuccess = Stream.of(fixtures)
                .filter(fixture -> fixture.expectingSuccess())
                .collect(Collectors.toUnmodifiableMap(
                        fixture -> generateFileName(fixture, VND_EMPL_RESULTS_CSV_FILENAME_PATTERN),
                        fixture -> fixture));

        final File reportFileDirectory = new File(TEST_VND_REPORTS_DIRECTORY);
        final File[] reportFiles = reportFileDirectory.listFiles();
        assertNotNull(reportFiles, "Unable to search for files in the reports file directory");
        assertEquals(fixturesExpectingSuccess.size(), reportFiles.length,
                "Wrong number of report files were generated");
        assertEquals(fixturesExpectingSuccess.size(), resultFileAndReportFilePairs.size(),
                "Wrong number of generated report files were recorded");

        final Set<String> reportFileNames = Arrays.stream(reportFiles)
                .map(reportFile -> reportFile.getName())
                .collect(Collectors.toUnmodifiableSet());

        for (final String csvFileName : fixturesExpectingSuccess.keySet()) {
            final VendorComparisonResult fixture = fixturesExpectingSuccess.get(csvFileName);
            final File reportFile = resultFileAndReportFilePairs.get(csvFileName);
            assertNotNull(reportFile, "Could not find report file corresponding to CSV file: " + csvFileName);
            assertTrue(reportFileNames.contains(reportFile.getName()), "CSV file " + csvFileName
                    + " has a matching report that's not in the expected reports directory: " + reportFile.getName());
            assertReportFileHasExpectedContent(fixture, reportFile);
        }
    }

    private void assertReportFileHasExpectedContent(final VendorComparisonResult fixture, final File reportFile)
            throws Exception {
        final String expectedReportContent = getExpectedReportFileContents(fixture);
        final String actualReportContent = FileUtils.readFileToString(reportFile, StandardCharsets.UTF_8);
        final String actualReportContentForCompare = getFileContentWithoutInitialPageMarkerLines(actualReportContent);
        assertEquals(expectedReportContent, actualReportContentForCompare, "Wrong report file contents");
    }

    private String getExpectedReportFileContents(final VendorComparisonResult fixture) throws IOException {
        final String reportFilePath = BASE_TEST_REPORT_FILE_PATH + fixture.expectedReportFile();
        try (
                final InputStream reportContent = CuCoreUtilities.getResourceAsStream(reportFilePath);
        ) {
            return IOUtils.toString(reportContent, StandardCharsets.UTF_8);
        }
    }

    private String getFileContentWithoutInitialPageMarkerLines(final String originalContent) {
        int lineFeedIndex = StringUtils.indexOf(originalContent, KFSConstants.NEWLINE);
        lineFeedIndex = StringUtils.indexOf(originalContent, KFSConstants.NEWLINE, lineFeedIndex + 1);
        if (lineFeedIndex < 0) {
            return originalContent;
        }
        return StringUtils.substring(originalContent, lineFeedIndex + 1);
    }

    private void assertOnlyCsvFilesRemainInResultsDirectory(final VendorComparisonResult... fixtures) {
        assertOnlyOneFilePerFixtureRemainsInResultsDirectory(VND_EMPL_RESULTS_CSV_FILENAME_PATTERN, fixtures);
    }

    private void assertOnlyDoneFilesRemainInResultsDirectory(final VendorComparisonResult... fixtures) {
        assertOnlyOneFilePerFixtureRemainsInResultsDirectory(VND_EMPL_RESULTS_DONE_FILENAME_PATTERN, fixtures);
    }

    private void assertOnlyOneFilePerFixtureRemainsInResultsDirectory(
            final String fileNamePattern, final VendorComparisonResult... fixtures) {
        final File resultFileDirectory = new File(TEST_VND_EMPL_RESULTS_DIRECTORY);
        final File[] actualFiles = resultFileDirectory.listFiles();
        assertNotNull(actualFiles, "Unable to search for files in the results file directory");
        assertEquals(fixtures.length, actualFiles.length, "Wrong number of remaining files in results file directory");

        final Set<String> expectedFileNames = Stream.of(fixtures)
                .map(fixture -> generateFileName(fixture, fileNamePattern))
                .collect(Collectors.toUnmodifiableSet());

        for (final File actualFile : actualFiles) {
            final String actualFileName = actualFile.getName();
            assertTrue(expectedFileNames.contains(actualFileName),
                    "Found an unexpected file in the results directory: " + actualFileName);
        }
    }

}
