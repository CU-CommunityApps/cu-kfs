package edu.cornell.kfs.vnd.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.vnd.batch.service.impl.annotation.VendorComparisonRow;
import edu.cornell.kfs.vnd.batch.service.impl.annotation.VendorComparisonRows;
import edu.cornell.kfs.vnd.businessobject.VendorWithSSN;
import edu.cornell.kfs.vnd.dataaccess.CuVendorDao;

@Execution(ExecutionMode.SAME_THREAD)
public class VendorEmployeeComparisonServiceOutboundFileTest {

    private static final String TEST_VND_DIRECTORY = "test/vnd_compare_out/";
    private static final String TEST_VND_STAGING_DIRECTORY = TEST_VND_DIRECTORY + "staging/vnd/";
    private static final String TEST_VND_OUTBOUND_FILE_EXPORT_DIRECTORY =
            TEST_VND_STAGING_DIRECTORY + "emplCompareWorkday/outbound/";
    private static final String TEST_VND_OUTBOUND_FILE_CREATION_DIRECTORY =
            TEST_VND_OUTBOUND_FILE_EXPORT_DIRECTORY + "being-written/";

    private static final ZonedDateTime MOCK_CURRENT_DATE = ZonedDateTime.of(
            2024, 5, 17, 10, 30, 0, 0, ZoneId.of("US/Eastern"));

    private VendorEmployeeComparisonServiceImpl vendorEmployeeComparisonService;
    private VendorComparisonRows testCaseFixture;
    private boolean forceExceptionWithinDaoMethodCall;
    private AtomicBoolean streamFromDaoWasClosed;

    @BeforeEach
    void setUp() throws Exception {
        createTestBatchDirectories();
        vendorEmployeeComparisonService = buildVendorEmployeeComparisonService();
        forceExceptionWithinDaoMethodCall = false;
        streamFromDaoWasClosed = new AtomicBoolean(false);
    }

    private void initializeTestCaseFixture(LocalTestCase testCase) {
        final VendorComparisonRows fixture = FixtureUtils.getAnnotationBasedFixture(
                testCase, VendorComparisonRows.class);
        testCaseFixture = fixture;
    }

    private void createTestBatchDirectories() throws IOException {
        final File testBatchDirectory = new File(TEST_VND_OUTBOUND_FILE_CREATION_DIRECTORY);
        FileUtils.forceMkdir(testBatchDirectory);
    }

    private VendorEmployeeComparisonServiceImpl buildVendorEmployeeComparisonService() {
        final VendorEmployeeComparisonServiceImpl service = new VendorEmployeeComparisonServiceImpl();
        service.setCsvEmployeeComparisonFileCreationDirectory(TEST_VND_OUTBOUND_FILE_CREATION_DIRECTORY);
        service.setCsvEmployeeComparisonFileExportDirectory(TEST_VND_OUTBOUND_FILE_EXPORT_DIRECTORY);
        service.setVendorDao(buildMockVendorDao());
        service.setBatchInputFileService(Mockito.mock(BatchInputFileService.class));
        service.setVendorEmployeeComparisonResultFileType(Mockito.mock(BatchInputFileType.class));
        service.setDateTimeService(buildMockDateTimeService());
        return service;
    }

    private CuVendorDao buildMockVendorDao() {
        final CuVendorDao vendorDao = Mockito.mock(CuVendorDao.class);
        Mockito.when(vendorDao.getPotentialEmployeeVendorsAsCloseableStream())
                .then(invocation -> getMockDaoSearchResults());
        return vendorDao;
    }

    private Stream<VendorWithSSN> getMockDaoSearchResults() {
        assertNotNull(testCaseFixture, "The test case's metadata was not properly initialized");
        if (forceExceptionWithinDaoMethodCall) {
            throw new RuntimeException("Forcibly throwing exception for "
                    + "getPotentialEmployeeVendorsAsCloseableStream() method call");
        }
        return Arrays.stream(testCaseFixture.value())
                .onClose(this::recordClosingOfStream)
                .map(VendorComparisonRow.Converters::toVendorWithSSN);
    }

    private void recordClosingOfStream() {
        streamFromDaoWasClosed.set(true);
    }

    private DateTimeService buildMockDateTimeService() {
        final DateTimeService dateTimeService = Mockito.mock(DateTimeService.class);
        Mockito.when(dateTimeService.getCurrentDate())
                .then(invocation -> Date.from(MOCK_CURRENT_DATE.toInstant()));
        return dateTimeService;
    }

    @AfterEach
    void shutDown() throws Exception {
        streamFromDaoWasClosed = null;
        testCaseFixture = null;
        vendorEmployeeComparisonService = null;
        deleteTestBatchDirectories();
    }

    private void deleteTestBatchDirectories() throws IOException {
        final File testBatchDirectory = new File(TEST_VND_DIRECTORY);
        if (testBatchDirectory.exists() && testBatchDirectory.isDirectory()) {
            FileUtils.forceDelete(testBatchDirectory.getAbsoluteFile());
        }
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
            @VendorComparisonRow(vendorId = "12345-1", taxId = "xxxxx1111"),
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
            @VendorComparisonRow(vendorId = "12345-1", taxId = "xxxxx1111"),
            @VendorComparisonRow(vendorId = "44444-0", taxId = "xxxxx2222", forceException = true),
            @VendorComparisonRow(vendorId = "78787-0", taxId = "xxxxx9876")
        })
        MULTIPLE_VENDORS_FORCE_EXCEPTION;
    }

    static Stream<LocalTestCase> standardTestCases() {
        return Stream.of(
                LocalTestCase.EMPTY_SOURCE,
                LocalTestCase.SINGLE_VENDOR,
                LocalTestCase.MULTIPLE_VENDORS
        );
    }

    static Stream<LocalTestCase> exceptionTestCases() {
        return Stream.of(
                LocalTestCase.SINGLE_VENDOR_FORCE_EXCEPTION,
                LocalTestCase.MULTIPLE_VENDORS_FORCE_EXCEPTION
        );
    }



    @ParameterizedTest
    @MethodSource("standardTestCases")
    void testSuccessfulCsvFilePreparation(LocalTestCase testCase) throws Exception {
        initializeTestCaseFixture(testCase);
    }

    private void assertCsvFileHasExpectedContent() {
        
    }

}
