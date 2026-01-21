package edu.cornell.kfs.rass.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.RassConstants.RassObjectGroupingUpdateResultCode;
import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;
import edu.cornell.kfs.rass.RassConstants.RassParseResultCode;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.RassBatchJobReport;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResult;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResultGrouping;
import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.RassXmlFileProcessingResult;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.LoadSpringFile;
import edu.cornell.kfs.sys.util.SpringEnabledMicroTestBase;

@LoadSpringFile("edu/cornell/kfs/rass/batch/cu-spring-rass-report-test.xml")
public class RassReportServiceImplTest extends SpringEnabledMicroTestBase {

    private static final String EXPECTED_RESULTS_DIRECTORY_PATH = "src/test/resources/edu/cornell/kfs/rass/reports/";
    private static final String TEST_REPORTS_DIRECTORY_PATH = "test/rass/";

    private static final String EMPTY_INPUT_FILE = "empty-input";
    private static final String SINGLE_ITEMS_FILE = "single-items";
    private static final String AGENCY_ERRORS_FILE = "agency-errors";
    private static final String MIXED_ITEMS_FILE = "mixed-items";
    private static final String ERROR_REPORT_SINGLE_ERROR_FILE = "errors-for-single-file";
    private static final String ERROR_REPORT_MULTI_ERRORS_FILE = "errors-for-multi-files";
    private static final String BAD_FILE1 = "bad-file1";
    private static final String ANOTHER_BAD_FILE = "another-bad-file";

    private static final String AGENCY_12457 = "12457";
    private static final String AGENCY_66660 = "66660";
    private static final String AGENCY_66661 = "66661";
    private static final String AGENCY_70005 = "70005";
    private static final String AGENCY_70023 = "70023";
    private static final String AGENCY_88855 = "88855";
    private static final String AGENCY_89898 = "89898";
    private static final String AGENCY_89899 = "89899";
    private static final String PROPOSAL_98765 = "98765";
    private static final String PROPOSAL_44556 = "44556";
    private static final String PROPOSAL_44567 = "44567";
    private static final String PROPOSAL_45599 = "45599";
    private static final String PROPOSAL_35791 = "35791";
    private static final String PROPOSAL_38333 = "38333";
    private static final String DOCUMENT_1501 = "1501";
    private static final String DOCUMENT_1502 = "1502";
    private static final String DOCUMENT_3101 = "3101";
    private static final String DOCUMENT_3102 = "3102";
    private static final String DOCUMENT_3103 = "3103";
    private static final String DOCUMENT_3104 = "3104";
    private static final String DOCUMENT_3105 = "3105";
    private static final String DOCUMENT_3106 = "3106";
    private static final String DOCUMENT_3107 = "3107";
    private static final String DOCUMENT_3108 = "3108";
    private static final String DOCUMENT_3109 = "3109";
    private static final String DOCUMENT_3110 = "3110";

    private RassReportServiceImpl rassReportService;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        rassReportService = springContext.getBean(
                RassTestConstants.RASS_REPORT_SERVICE_BEAN_NAME, RassReportServiceImpl.class);
        buildReportOutputDirectory();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        rassReportService = null;
        removeGeneratedReportFilesAndDirectories();
    }

    private void buildReportOutputDirectory() throws Exception {
        File rassReportOutputDirectory = getReportsDirectoryAsFile();
        FileUtils.forceMkdir(rassReportOutputDirectory);
    }

    private void removeGeneratedReportFilesAndDirectories() throws Exception {
        File rassReportOutputDirectory = getReportsDirectoryAsFile();
        if (rassReportOutputDirectory.exists() && rassReportOutputDirectory.isDirectory()) {
            FileUtils.forceDelete(rassReportOutputDirectory.getAbsoluteFile());
        }
    }

    private File getReportsDirectoryAsFile() {
        return new File(TEST_REPORTS_DIRECTORY_PATH);
    }

    @Test
    public void testEmptyReport() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(),
                rassReport(fileParseResults(), fileProcessingResults()));
    }

    @Test
    public void testReportWithSingleFileForEmptyResults() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(EMPTY_INPUT_FILE),
                rassReport(
                        fileParseResults(
                                fileParseResult(EMPTY_INPUT_FILE, RassParseResultCode.SUCCESS)),
                        fileProcessingResults(
                                emptyProcessingResult(EMPTY_INPUT_FILE))
                ));
    }

    @Test
    public void testReportWithSingleFileForSimpleResults() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(SINGLE_ITEMS_FILE),
                rassReport(
                        fileParseResults(
                                fileParseResult(SINGLE_ITEMS_FILE, RassParseResultCode.SUCCESS)),
                        fileProcessingResults(
                                singleItemsProcessingResult(SINGLE_ITEMS_FILE))
                ));
    }

    @Test
    public void testReportWithSingleFileForAgencyErrorResults() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(AGENCY_ERRORS_FILE),
                rassReport(
                        fileParseResults(
                                fileParseResult(AGENCY_ERRORS_FILE, RassParseResultCode.SUCCESS)),
                        fileProcessingResults(
                                agencyErrorsProcessingResult(AGENCY_ERRORS_FILE))
                ));
    }

    @Test
    public void testReportWithSingleFileForComplexResults() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(MIXED_ITEMS_FILE),
                rassReport(
                        fileParseResults(
                                fileParseResult(MIXED_ITEMS_FILE, RassParseResultCode.SUCCESS)),
                        fileProcessingResults(
                                mixedItemsProcessingResult(MIXED_ITEMS_FILE))
                ));
    }

    @Test
    public void testMultipleSuccessfulFiles() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(SINGLE_ITEMS_FILE, EMPTY_INPUT_FILE, MIXED_ITEMS_FILE),
                rassReport(
                        fileParseResults(
                                fileParseResult(SINGLE_ITEMS_FILE, RassParseResultCode.SUCCESS),
                                fileParseResult(EMPTY_INPUT_FILE, RassParseResultCode.SUCCESS),
                                fileParseResult(MIXED_ITEMS_FILE, RassParseResultCode.SUCCESS)),
                        fileProcessingResults(
                                singleItemsProcessingResult(SINGLE_ITEMS_FILE),
                                emptyProcessingResult(EMPTY_INPUT_FILE),
                                mixedItemsProcessingResult(MIXED_ITEMS_FILE))
                ));
    }

    @Test
    public void testErrorReportForSingleFailedFile() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(ERROR_REPORT_SINGLE_ERROR_FILE),
                rassReport(
                        fileParseResults(
                                fileParseResult(BAD_FILE1, RassParseResultCode.ERROR)),
                        fileProcessingResults()
                ));
    }

    @Test
    public void testErrorReportForMultipleFailedFiles() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(ERROR_REPORT_MULTI_ERRORS_FILE),
                rassReport(
                        fileParseResults(
                                fileParseResult(BAD_FILE1, RassParseResultCode.ERROR),
                                fileParseResult(ANOTHER_BAD_FILE, RassParseResultCode.ERROR)),
                        fileProcessingResults()
                ));
    }

    @Test
    public void testReportsForMixOfSuccessfulAndErrorFiles() throws Exception {
        assertReportServiceGeneratesCorrectReportOutput(
                expectedFiles(SINGLE_ITEMS_FILE, AGENCY_ERRORS_FILE, MIXED_ITEMS_FILE, ERROR_REPORT_MULTI_ERRORS_FILE),
                rassReport(
                        fileParseResults(
                                fileParseResult(SINGLE_ITEMS_FILE, RassParseResultCode.SUCCESS),
                                fileParseResult(BAD_FILE1, RassParseResultCode.ERROR),
                                fileParseResult(AGENCY_ERRORS_FILE, RassParseResultCode.SUCCESS),
                                fileParseResult(MIXED_ITEMS_FILE, RassParseResultCode.SUCCESS),
                                fileParseResult(ANOTHER_BAD_FILE, RassParseResultCode.ERROR)),
                        fileProcessingResults(
                                singleItemsProcessingResult(SINGLE_ITEMS_FILE),
                                agencyErrorsProcessingResult(AGENCY_ERRORS_FILE),
                                mixedItemsProcessingResult(MIXED_ITEMS_FILE))
                ));
    }

    private void assertReportServiceGeneratesCorrectReportOutput(
            List<String> expectedOutputFiles, RassBatchJobReport rassBatchJobReport) throws Exception {
        rassReportService.writeBatchJobReports(rassBatchJobReport);
        Collection<File> actualOutputFiles = FileUtils.listFiles(getReportsDirectoryAsFile(), null, false);
        assertEquals("Wrong number of report files generated", expectedOutputFiles.size(), actualOutputFiles.size());
        
        for (String expectedOutputFile : expectedOutputFiles) {
            File expectedFile = new File(EXPECTED_RESULTS_DIRECTORY_PATH + expectedOutputFile);
            File actualFile = findOutputFile(expectedOutputFile, actualOutputFiles);
            assertTrue("Could not find expected-results file " + expectedOutputFile, expectedFile.exists());
            
            String expectedContent = FileUtils.readFileToString(expectedFile, StandardCharsets.UTF_8);
            String actualContent = getContentWithoutInitialPageMarkerLines(
                    FileUtils.readFileToString(actualFile, StandardCharsets.UTF_8));
            assertEquals("Wrong file contents for " + actualFile.getName(), expectedContent, actualContent);
        }
    }

    private File findOutputFile(String expectedOutputFile, Collection<File> actualOutputFiles) {
        String expectedOutputFileNamePrefixFragment = getOutputFileNamePrefixFragment(expectedOutputFile);
        String expectedFilePrefix = MessageFormat.format(
                rassReportService.getReportFileNamePrefixFormat(), expectedOutputFileNamePrefixFragment);
        File[] matchingFiles = actualOutputFiles.stream()
                .filter(file -> StringUtils.startsWith(file.getName(), expectedFilePrefix))
                .toArray(File[]::new);
        assertEquals("Wrong number of output files found for " + expectedOutputFile, 1, matchingFiles.length);
        return matchingFiles[0];
    }

    private String getOutputFileNamePrefixFragment(String expectedOutputFile) {
        if (StringUtils.startsWith(expectedOutputFile, RassConstants.RASS_PARSE_ERRORS_BASE_FILENAME)) {
            return RassConstants.RASS_PARSE_ERRORS_BASE_FILENAME;
        } else {
            return StringUtils.substringBeforeLast(expectedOutputFile, KFSConstants.DELIMITER);
        }
    }

    private String addTextExtensionToBaseFileName(String fileName) {
        return fileName + CuGeneralLedgerConstants.BatchFileSystem.TEXT_EXTENSION;
    }

    private String addXmlExtensionToBaseFileName(String fileName) {
        return fileName + CUKFSConstants.XML_FILE_EXTENSION;
    }

    private String getContentWithoutInitialPageMarkerLines(String fileContent) {
        String result = StringUtils.substringAfter(fileContent, KFSConstants.NEWLINE);
        result = StringUtils.substringAfter(result, KFSConstants.NEWLINE);
        return result;
    }

    private List<String> expectedFiles(String... expectedFiles) {
        return Stream.of(expectedFiles)
                .map(this::addTextExtensionToBaseFileName)
                .collect(Collectors.toList());
    }

    private RassBatchJobReport rassReport(List<RassXmlFileParseResult> fileParseResults,
            Map<String, RassXmlFileProcessingResult> fileProcessingResults) {
        return new RassBatchJobReport(fileParseResults, fileProcessingResults);
    }

    private List<RassXmlFileParseResult> fileParseResults(RassXmlFileParseResult... fileParseResults) {
        return Arrays.asList(fileParseResults);
    }

    private RassXmlFileParseResult fileParseResult(String fileName, RassParseResultCode resultCode) {
        String xmlFileName = addXmlExtensionToBaseFileName(fileName);
        Optional<RassXmlDocumentWrapper> dummyParsedContent = RassParseResultCode.SUCCESS.equals(resultCode)
                ? Optional.of(new RassXmlDocumentWrapper()) : Optional.empty();
        return new RassXmlFileParseResult(xmlFileName, resultCode, dummyParsedContent);
    }

    private Map<String, RassXmlFileProcessingResult> fileProcessingResults(
            RassXmlFileProcessingResult... fileProcessingResults) {
        return Stream.of(fileProcessingResults)
                .collect(Collectors.toMap(RassXmlFileProcessingResult::getRassXmlFileName, Function.identity()));
    }

    private RassXmlFileProcessingResult emptyProcessingResult(String fileName) {
        return fileProcessingResult(
                fileName, emptyAgencyResults(), emptyProposalResults(), emptyAwardResults());
    }

    private RassXmlFileProcessingResult singleItemsProcessingResult(String fileName) {
        return fileProcessingResult(
                fileName,
                agencyResults(
                        RassObjectGroupingUpdateResultCode.SUCCESS,
                        agencySuccess(AGENCY_12457, DOCUMENT_1501, RassObjectUpdateResultCode.SUCCESS_NEW)),
                proposalResults(
                        RassObjectGroupingUpdateResultCode.SUCCESS,
                        proposalSkip(PROPOSAL_98765)),
                awardResults(
                        RassObjectGroupingUpdateResultCode.SUCCESS,
                        awardSuccess(PROPOSAL_98765, DOCUMENT_1502, RassObjectUpdateResultCode.SUCCESS_EDIT))
                );
    }

    private RassXmlFileProcessingResult agencyErrorsProcessingResult(String fileName) {
        return fileProcessingResult(
                fileName,
                agencyResults(
                        RassObjectGroupingUpdateResultCode.ERROR,
                        agencyError(AGENCY_66660, "Invalid Agency Type"),
                        agencyError(AGENCY_66661, "Cannot proceed with processing object because an update failure "
                                + "was detected for Agency with ID 66660")),
                emptyProposalResults(),
                emptyAwardResults());
    }

    private RassXmlFileProcessingResult mixedItemsProcessingResult(String fileName) {
        return fileProcessingResult(
                fileName,
                agencyResults(
                        RassObjectGroupingUpdateResultCode.SUCCESS,
                        agencySuccess(AGENCY_66660, DOCUMENT_3101, RassObjectUpdateResultCode.SUCCESS_NEW),
                        agencySkip(AGENCY_70005),
                        agencySkip(AGENCY_70023),
                        agencySuccess(AGENCY_12457, DOCUMENT_3102, RassObjectUpdateResultCode.SUCCESS_EDIT),
                        agencySuccess(AGENCY_88855, DOCUMENT_3103, RassObjectUpdateResultCode.SUCCESS_NEW),
                        agencySuccess(AGENCY_89898, DOCUMENT_3104, RassObjectUpdateResultCode.SUCCESS_NEW),
                        agencySuccess(AGENCY_89899, DOCUMENT_3105, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                proposalResults(
                        RassObjectGroupingUpdateResultCode.ERROR,
                        proposalSkip(PROPOSAL_98765),
                        proposalError(PROPOSAL_44556, "Invalid Purpose Code"),
                        proposalSuccess(PROPOSAL_44567, DOCUMENT_3106, RassObjectUpdateResultCode.SUCCESS_NEW),
                        proposalSuccess(PROPOSAL_45599, DOCUMENT_3107, RassObjectUpdateResultCode.SUCCESS_NEW),
                        proposalSuccess(PROPOSAL_35791, DOCUMENT_3108, RassObjectUpdateResultCode.SUCCESS_EDIT),
                        proposalSkip(PROPOSAL_38333)),
                awardResults(
                        RassObjectGroupingUpdateResultCode.ERROR,
                        awardError(PROPOSAL_98765, "Project Title is a required field"),
                        awardError(PROPOSAL_44556, "Cannot proceed with processing object because an update failure "
                                + "was detected for Proposal with ID 44556"),
                        awardSuccess(PROPOSAL_44567, DOCUMENT_3109, RassObjectUpdateResultCode.SUCCESS_NEW),
                        awardError(PROPOSAL_45599, "Invalid Grant Description"),
                        awardSkip(PROPOSAL_35791),
                        awardSuccess(PROPOSAL_38333, DOCUMENT_3110, RassObjectUpdateResultCode.SUCCESS_EDIT))
                );
    }

    private RassXmlFileProcessingResult fileProcessingResult(
            String fileName, RassBusinessObjectUpdateResultGrouping<Agency> agencyResults,
            RassBusinessObjectUpdateResultGrouping<Proposal> proposalResults,
            RassBusinessObjectUpdateResultGrouping<Award> awardResults) {
        String xmlFileName = addXmlExtensionToBaseFileName(fileName);
        return new RassXmlFileProcessingResult(xmlFileName, agencyResults, awardResults);
    }

    private RassBusinessObjectUpdateResultGrouping<Agency> emptyAgencyResults() {
        return agencyResults(RassObjectGroupingUpdateResultCode.SUCCESS);
    }

    @SafeVarargs
    private final RassBusinessObjectUpdateResultGrouping<Agency> agencyResults(
            RassObjectGroupingUpdateResultCode resultCode, RassBusinessObjectUpdateResult<Agency>... agencyResults) {
        return new RassBusinessObjectUpdateResultGrouping<>(Agency.class, Arrays.asList(agencyResults), resultCode);
    }

    private RassBusinessObjectUpdateResult<Agency> agencySuccess(
            String agencyNumber, String documentId, RassObjectUpdateResultCode resultCode) {
        return new RassBusinessObjectUpdateResult<>(
                Agency.class, agencyNumber, documentId, resultCode, StringUtils.EMPTY);
    }

    private RassBusinessObjectUpdateResult<Agency> agencySkip(String agencyNumber) {
        return new RassBusinessObjectUpdateResult<>(Agency.class, agencyNumber, RassObjectUpdateResultCode.SKIPPED);
    }

    private RassBusinessObjectUpdateResult<Agency> agencyError(String agencyNumber, String errorMessage) {
        return new RassBusinessObjectUpdateResult<>(
                Agency.class, agencyNumber, RassObjectUpdateResultCode.ERROR, errorMessage);
    }

    private RassBusinessObjectUpdateResultGrouping<Proposal> emptyProposalResults() {
        return proposalResults(RassObjectGroupingUpdateResultCode.SUCCESS);
    }

    @SafeVarargs
    private final RassBusinessObjectUpdateResultGrouping<Proposal> proposalResults(
            RassObjectGroupingUpdateResultCode resultCode,
            RassBusinessObjectUpdateResult<Proposal>... proposalResults) {
        return new RassBusinessObjectUpdateResultGrouping<>(
                Proposal.class, Arrays.asList(proposalResults), resultCode);
    }

    private RassBusinessObjectUpdateResult<Proposal> proposalSuccess(
            String proposalNumber, String documentId, RassObjectUpdateResultCode resultCode) {
        return new RassBusinessObjectUpdateResult<>(
                Proposal.class, proposalNumber, documentId, resultCode, StringUtils.EMPTY);
    }

    private RassBusinessObjectUpdateResult<Proposal> proposalSkip(String proposalNumber) {
        return new RassBusinessObjectUpdateResult<>(
                Proposal.class, proposalNumber, RassObjectUpdateResultCode.SKIPPED);
    }

    private RassBusinessObjectUpdateResult<Proposal> proposalError(String proposalNumber, String errorMessage) {
        return new RassBusinessObjectUpdateResult<>(
                Proposal.class, proposalNumber, RassObjectUpdateResultCode.ERROR, errorMessage);
    }

    private RassBusinessObjectUpdateResultGrouping<Award> emptyAwardResults() {
        return awardResults(RassObjectGroupingUpdateResultCode.SUCCESS);
    }

    @SafeVarargs
    private final RassBusinessObjectUpdateResultGrouping<Award> awardResults(
            RassObjectGroupingUpdateResultCode resultCode, RassBusinessObjectUpdateResult<Award>... awardResults) {
        return new RassBusinessObjectUpdateResultGrouping<>(Award.class, Arrays.asList(awardResults), resultCode);
    }

    private RassBusinessObjectUpdateResult<Award> awardSuccess(
            String proposalNumber, String documentId, RassObjectUpdateResultCode resultCode) {
        return new RassBusinessObjectUpdateResult<>(
                Award.class, proposalNumber, documentId, resultCode, StringUtils.EMPTY);
    }

    private RassBusinessObjectUpdateResult<Award> awardSkip(String proposalNumber) {
        return new RassBusinessObjectUpdateResult<>(Award.class, proposalNumber, RassObjectUpdateResultCode.SKIPPED);
    }

    private RassBusinessObjectUpdateResult<Award> awardError(String proposalNumber, String errorMessage) {
        return new RassBusinessObjectUpdateResult<>(
                Award.class, proposalNumber, RassObjectUpdateResultCode.ERROR, errorMessage);
    }

}
