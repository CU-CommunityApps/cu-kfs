package edu.cornell.kfs.vnd.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
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

import edu.cornell.kfs.sys.CUKFSConstants;
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

    private static final String VND_EMPL_RESULTS_CSV_FILENAME = "empl_result_20240615_123045.csv";
    private static final String VND_EMPL_RESULTS_DONE_FILENAME = "empl_result_20240615_123045.done";

    private static final String BASE_TEST_REPORT_FILE_PATH =
            "classpath:edu/cornell/kfs/vnd/batch/service/impl/empl-compare-report/";

    private static final ZonedDateTime MOCK_CURRENT_DATE = ZonedDateTime.of(
            2024, 5, 17, 10, 30, 0, 0, ZoneId.of(CUKFSConstants.TIME_ZONE_US_EASTERN));

    @RegisterExtension
    TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/vnd/batch/service/impl/cu-spring-vnd-empl-comparison-result-test.xml");

    private VendorEmployeeComparisonService vendorEmployeeComparisonService;
    private VendorComparisonResult testCaseFixture;

    @BeforeEach
    void setUp() throws Exception {
        vendorEmployeeComparisonService = springContextExtension.getBean(
                VendorSpringBeans.VENDOR_EMPLOYEE_COMPARISON_SERVICE, VendorEmployeeComparisonService.class);
    }

    private void initializeTestCaseFixture(final LocalTestCase testCase) {
        final VendorComparisonResult fixture = FixtureUtils.getAnnotationBasedFixture(
                testCase, VendorComparisonResult.class);
        testCaseFixture = fixture;
    }

    @SpringXmlBeanFactoryMethod
    public CuVendorDao buildMockVendorDao() {
        return Mockito.mock(CuVendorDao.class);
    }

    @SpringXmlBeanFactoryMethod
    public DateTimeService buildMockDateTimeService() {
        final DateTimeService dateTimeService = Mockito.mock(DateTimeService.class);
        Mockito.when(dateTimeService.getCurrentDate())
                .then(invocation -> Date.from(MOCK_CURRENT_DATE.toInstant()));
        return dateTimeService;
    }

    @SpringXmlBeanFactoryMethod
    public ParameterService buildMockParameterService() {
        return Mockito.mock(ParameterService.class);
    }

    private void assertTestCaseFixtureWasInitialized() {
        assertNotNull(testCaseFixture, "The test case's metadata was not properly initialized");
    }

    @AfterEach
    void tearDown() throws Exception {
        testCaseFixture = null;
        vendorEmployeeComparisonService = null;
    }



    private void createTestCsvFile() throws IOException {
        assertTestCaseFixtureWasInitialized();
        final File csvFile = new File(TEST_VND_EMPL_RESULTS_DIRECTORY + VND_EMPL_RESULTS_CSV_FILENAME);
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

    private void createTestDoneFile() throws IOException {
        final File doneFile = new File(TEST_VND_EMPL_RESULTS_DIRECTORY + VND_EMPL_RESULTS_DONE_FILENAME);
        FileUtils.touch(doneFile);
    }

    
    
    enum LocalTestCase {
        @VendorComparisonResult(
                csvDataRows = {
                        VendorComparisonResultRowFixture.JOHN_DOE,
                },
                expectedReportFile = ""
        )
        FILE_WITH_SINGLE_ACTIVE_ROW;
    }



    @Test
    void doSomething() throws Exception {
        initializeTestCaseFixture(LocalTestCase.FILE_WITH_SINGLE_ACTIVE_ROW);
        createTestCsvFile();
        createTestDoneFile();
        assertCsvFileProcessingSucceeds();
    }

    private void assertCsvFileProcessingSucceeds() throws Exception {
        assertTestCaseFixtureWasInitialized();
        final boolean csvProcessingResult = vendorEmployeeComparisonService.processResultsOfVendorEmployeeComparison();
        assertTrue(csvProcessingResult, "The results CSV file should have been processed successfully");
    }

}
