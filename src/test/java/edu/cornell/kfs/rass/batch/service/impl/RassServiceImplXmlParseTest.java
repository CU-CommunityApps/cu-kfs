package edu.cornell.kfs.rass.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.gl.GeneralLedgerConstants;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.rass.RassConstants.RassParseResultCode;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlDocumentWrapperFixture;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.LoadSpringFile;
import edu.cornell.kfs.sys.util.SpringEnabledMicroTestBase;

@LoadSpringFile("edu/cornell/kfs/rass/batch/cu-spring-rass-service-test.xml")
public class RassServiceImplXmlParseTest extends SpringEnabledMicroTestBase {

    private static final String SOURCE_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/rass";
    private static final String TARGET_TEST_FILE_PATH = "test/rass";
    private static final String FULL_FILE_PATH_FORMAT = "%s/%s%s";

    private static final String RASS_EXAMPLE = "rass_example";
    private static final String RASS_AWARDS_ONLY = "rass_awards_only";
    private static final String RASS_AGENCIES_ONLY = "rass_agencies_only";
    private static final String RASS_EMPTY = "rass_empty";
    private static final String RASS_BAD_FORMAT = "rass_bad_format";

    private TestRassServiceImpl rassService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        createTargetTestDirectory();
        rassService = springContext.getBean(RassTestConstants.RASS_SERVICE_BEAN_NAME, TestRassServiceImpl.class);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        deleteTargetTestDirectory();
    }

    @Test
    public void testLoadFileWithAwardsAndAgencies() throws Exception {
        assertRassXmlFilesParseAsExpected(
                fileNames(RASS_EXAMPLE),
                expectedResults(
                        result(RASS_EXAMPLE, RassParseResultCode.SUCCESS, RassXmlDocumentWrapperFixture.RASS_EXAMPLE)));
    }

    @Test
    public void testLoadFileWithNeitherAgenciesNorAwards() throws Exception {
        assertRassXmlFilesParseAsExpected(
                fileNames(RASS_EMPTY),
                expectedResults(
                        result(RASS_EMPTY, RassParseResultCode.SUCCESS, RassXmlDocumentWrapperFixture.RASS_EMPTY_FILE)));
    }

    @Test
    public void testLoadFileOnlyContainingAwards() throws Exception {
        assertRassXmlFilesParseAsExpected(
                fileNames(RASS_AWARDS_ONLY),
                expectedResults(
                        result(RASS_AWARDS_ONLY, RassParseResultCode.SUCCESS, RassXmlDocumentWrapperFixture.RASS_AWARDS_ONLY)));
    }

    @Test
    public void testLoadFileOnlyContainingAgencies() throws Exception {
        assertRassXmlFilesParseAsExpected(
                fileNames(RASS_AGENCIES_ONLY),
                expectedResults(
                        result(RASS_AGENCIES_ONLY, RassParseResultCode.SUCCESS, RassXmlDocumentWrapperFixture.RASS_AGENCIES_ONLY)));
    }

    @Test
    public void testLoadBadlyFormattedFile() throws Exception {
        assertRassXmlFilesParseAsExpected(
                fileNames(RASS_BAD_FORMAT),
                expectedResults(
                        result(RASS_BAD_FORMAT, RassParseResultCode.ERROR)));
    }

    @Test
    public void testLoadMultipleFiles() throws Exception {
        assertRassXmlFilesParseAsExpected(
                fileNames(RASS_EXAMPLE, RASS_BAD_FORMAT, RASS_AWARDS_ONLY),
                expectedResults(
                        result(RASS_EXAMPLE, RassParseResultCode.SUCCESS, RassXmlDocumentWrapperFixture.RASS_EXAMPLE),
                        result(RASS_BAD_FORMAT, RassParseResultCode.ERROR),
                        result(RASS_AWARDS_ONLY, RassParseResultCode.SUCCESS, RassXmlDocumentWrapperFixture.RASS_AWARDS_ONLY)));
    }

    private void assertRassXmlFilesParseAsExpected(
            List<String> baseFileNames, List<RassXmlFileParseResult> expectedResults) throws Exception {
        copyTestFilesAndCreateDoneFiles(baseFileNames);
        List<RassXmlFileParseResult> actualResults = rassService.readXML();
        assertRassParseResultsAreCorrect(expectedResults, actualResults);
        assertDoneFilesWereDeleted(baseFileNames);
    }

    private void assertRassParseResultsAreCorrect(
            List<RassXmlFileParseResult> expectedResults, List<RassXmlFileParseResult> actualResults) {
        assertEquals("Wrong number of parsing results", expectedResults.size(), actualResults.size());
        
        Map<String, RassXmlFileParseResult> actualResultsByFileName = actualResults.stream()
                .collect(Collectors.toMap(this::getFileNameFromResultUsingSlashes, Function.identity()));
        
        for (RassXmlFileParseResult expectedResult : expectedResults) {
            String expectedFileName = expectedResult.getRassXmlFileName();
            RassXmlFileParseResult actualResult = actualResultsByFileName.get(expectedFileName);
            assertNotNull("A result object should have been present for file " + expectedFileName, actualResult);
            assertEquals("Wrong parse result code for file " + expectedFileName,
                    expectedResult.getResultCode(), actualResult.getResultCode());
            if (expectedResult.hasParsedDocumentWrapper()) {
                assertTrue("Parsed content should have been present for file " + expectedFileName, actualResult.hasParsedDocumentWrapper());
                assertEquals("Wrong parsed contents for file " + expectedFileName,
                        expectedResult.getParsedDocumentWrapper(), actualResult.getParsedDocumentWrapper());
            } else {
                assertFalse("Parsed data should not have been present for file " + expectedFileName, actualResult.hasParsedDocumentWrapper());
            }
        }
    }

    private void createTargetTestDirectory() throws IOException {
        File rassXmlTestDirectory = new File(TARGET_TEST_FILE_PATH);
        FileUtils.forceMkdir(rassXmlTestDirectory);
    }

    private void copyTestFilesAndCreateDoneFiles(List<String> baseFileNames) throws IOException {
        for (String baseFileName : baseFileNames) {
            File sourceFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, SOURCE_TEST_FILE_PATH, baseFileName, CuFPConstants.XML_FILE_EXTENSION));
            File targetFile = new File(
                    buildTargetFileName(baseFileName));
            File doneFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName,
                            GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION));
            FileUtils.copyFile(sourceFile, targetFile);
            doneFile.createNewFile();
        }
    }

    private void assertDoneFilesWereDeleted(List<String> baseFileNames) {
        for (String baseFileName : baseFileNames) {
            File doneFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName,
                            GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION));
            assertFalse("The file '" + baseFileName + GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION
                    + "' should have been deleted", doneFile.exists());
        }
    }

    private void deleteTargetTestDirectory() throws IOException {
        File rassXmlTestDirectory = new File(TARGET_TEST_FILE_PATH);
        if (rassXmlTestDirectory.exists() && rassXmlTestDirectory.isDirectory()) {
            FileUtils.forceDelete(rassXmlTestDirectory.getAbsoluteFile());
        }
    }

    private String buildTargetFileName(String baseFileName) {
        return String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName, CuFPConstants.XML_FILE_EXTENSION);
    }

    private String getFileNameFromResultUsingSlashes(RassXmlFileParseResult parseResult) {
        return StringUtils.replace(parseResult.getRassXmlFileName(), CUKFSConstants.BACKSLASH, CUKFSConstants.SLASH);
    }

    private List<String> fileNames(String... baseFileNames) {
        return Arrays.asList(baseFileNames);
    }

    private List<RassXmlFileParseResult> expectedResults(RassXmlFileParseResult... expectedResults) {
        return Arrays.asList(expectedResults);
    }

    private RassXmlFileParseResult result(String baseFileName, RassParseResultCode resultCode) {
        return new RassXmlFileParseResult(
                buildTargetFileName(baseFileName), resultCode, Optional.empty());
    }

    private RassXmlFileParseResult result(String baseFileName, RassParseResultCode resultCode, RassXmlDocumentWrapperFixture xmlFixture) {
        return new RassXmlFileParseResult(
                buildTargetFileName(baseFileName), resultCode, Optional.of(xmlFixture.toRassXmlDocumentWrapper()));
    }

}
