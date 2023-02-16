package edu.cornell.kfs.krad.antivirus.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.krad.CUKRADConstants.ClamAVResponses;
import edu.cornell.kfs.krad.CUKRADTestConstants;
import edu.cornell.kfs.krad.antivirus.service.ScanResult.Status;

@Execution(ExecutionMode.SAME_THREAD)
public class ClamAVAntiVirusServiceImplTest {

    private static final String BASE_TEST_FILE_PATH = "classpath:edu/cornell/kfs/krad/service/fixture/";
    private static final String GOOD_TEST_FILE = "good.txt";
    private static final String MOCK_VIRUS_TEST_FILE = "virus.txt";
    private static final String GOOD_LARGE_TEST_FILE = "good_large_file.txt";
    private static final String ERROR_LARGE_TEST_FILE = "error_large_file.txt";
    private static final String MOCK_VIRUS_LARGE_TEST_FILE = "virus_large_file.txt";

    private MockClamAVEndpoint mockClamAV;
    private ClamAVAntiVirusServiceImpl antiVirusService;

    @BeforeEach
    void setUp() throws Exception {
        mockClamAV = new MockClamAVEndpoint();
        antiVirusService = new ClamAVAntiVirusServiceImpl();
        antiVirusService.setTimeout(CUKRADTestConstants.TEST_SOCKET_TIMEOUT);
        antiVirusService.setHost(mockClamAV.getHostAddress());
        antiVirusService.setPort(mockClamAV.getPort());
    }

    @AfterEach
    void shutDown() throws Exception {
        antiVirusService = null;
        IOUtils.closeQuietly(mockClamAV);
        mockClamAV = null;
    }

    @Test
    void testPing() throws Exception {
        assertTrue("The ping() call should have succeeded", antiVirusService.ping());
    }

    @Test
    void testStats() throws Exception {
        String actualStats = antiVirusService.stats();
        assertEquals("Wrong mock stats response", CUKRADTestConstants.TEST_STATS_OUTPUT, actualStats);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        GOOD_TEST_FILE,
        GOOD_LARGE_TEST_FILE
    })
    void testScanGoodFiles(String fileName) throws Exception {
        ClamAVScanResult result = runScan(fileName);
        assertScanPassed(result);
    }

    @Test
    void testScanGoodFileFromByteArray() throws Exception {
        ClamAVScanResult result = runScanWithPreLoadedFile(GOOD_TEST_FILE);
        assertScanPassed(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        MOCK_VIRUS_TEST_FILE,
        MOCK_VIRUS_LARGE_TEST_FILE
    })
    void testScanMockVirusedFiles(String fileName) throws Exception {
        ClamAVScanResult result = runScan(fileName);
        assertScanFailedDueToMockVirus(result);
    }

    @Test
    void testScanFileExceedingMockSizeLimit() throws Exception {
        ClamAVScanResult result = runScan(ERROR_LARGE_TEST_FILE);
        assertScanExceededFileSizeLimit(result);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        GOOD_TEST_FILE,
        GOOD_LARGE_TEST_FILE,
        MOCK_VIRUS_TEST_FILE,
        MOCK_VIRUS_LARGE_TEST_FILE,
        ERROR_LARGE_TEST_FILE
    })
    void testMockServerErrorWhileScanningFiles(String fileName) throws Exception {
        mockClamAV.setForceTempFileError(true);
        ClamAVScanResult result = runScan(fileName);
        assertScanEncounteredTempFileError(result);
    }

    private ClamAVScanResult runScan(String fileName) throws Exception {
        try (
            InputStream fileStream = CuCoreUtilities.getResourceAsStream(BASE_TEST_FILE_PATH + fileName);
        ) {
            return antiVirusService.scan(fileStream);
        }
    }

    private ClamAVScanResult runScanWithPreLoadedFile(String fileName) throws Exception {
        try (
            InputStream fileStream = CuCoreUtilities.getResourceAsStream(BASE_TEST_FILE_PATH + fileName);
        ) {
            byte[] fileBytes = IOUtils.toByteArray(fileStream);
            return antiVirusService.scan(fileBytes);
        }
    }

    private void assertScanPassed(ClamAVScanResult result) throws Exception {
        assertEquals("Wrong result status", Status.PASSED, result.getStatus());
        assertEquals("Wrong result string", ClamAVResponses.RESPONSE_OK, result.getResult());
        assertTrue("Signature should have been blank", StringUtils.isBlank(result.getSignature()));
    }

    private void assertScanFailedDueToMockVirus(ClamAVScanResult result) throws Exception {
        assertEquals("Wrong result status", Status.FAILED, result.getStatus());
        assertEquals("Wrong signature", CUKRADTestConstants.MOCK_VIRUS_MESSAGE, result.getSignature());
        assertTrue("Result string did not start with the expected prefix",
                StringUtils.startsWith(result.getResult(), ClamAVResponses.STREAM_PREFIX));
        assertTrue("Result string did not end with the expected suffix",
                StringUtils.endsWith(result.getResult(), ClamAVResponses.FOUND_SUFFIX));
    }

    private void assertScanExceededFileSizeLimit(ClamAVScanResult result) throws Exception {
        assertScanEncounteredError(ClamAVResponses.RESPONSE_SIZE_EXCEEDED, result);
    }

    private void assertScanEncounteredTempFileError(ClamAVScanResult result) throws Exception {
        assertScanEncounteredError(ClamAVResponses.RESPONSE_ERROR_WRITING_FILE, result);
    }

    private void assertScanEncounteredError(
            String potentialExpectedResultMessage, ClamAVScanResult result) throws Exception {
        assertEquals("Wrong result status", Status.ERROR, result.getStatus());
        assertTrue("Signature should have been blank", StringUtils.isBlank(result.getSignature()));
        if (StringUtils.isNotBlank(result.getResult())) {
            assertEquals("Wrong result string", potentialExpectedResultMessage, result.getResult());
        } else {
            assertNotNull("An exception should have been recorded if the endpoint response could not be retrieved",
                    result.getException());
        }
    }

}
