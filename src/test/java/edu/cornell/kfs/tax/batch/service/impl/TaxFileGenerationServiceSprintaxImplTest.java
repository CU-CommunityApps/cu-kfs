package edu.cornell.kfs.tax.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.coreservice.api.parameter.EvaluationOperator;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.dataaccess.TestDataHelperDao;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import edu.cornell.kfs.sys.util.CuMockBuilder;
import edu.cornell.kfs.sys.util.GlobalResourceLoaderUtils;
import edu.cornell.kfs.sys.util.SpringXmlTestBeanFactoryMethod;
import edu.cornell.kfs.sys.util.TestDateUtils;
import edu.cornell.kfs.sys.util.TestFileUtils;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.Tax1042SParameterNames;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.CuTaxTestConstants.TaxSpringBeans;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxBatchConfig.Mode;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.TestTransactionDetailCsvInputFileType;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;
import edu.cornell.kfs.tax.batch.util.TestTaxSqlUtils;
import edu.cornell.kfs.tax.batch.util.TestTaxSqlUtils.TableNames;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.businessobject.TransactionDetail.TransactionDetailField;
import edu.cornell.kfs.tax.service.TransactionOverrideService;
import edu.cornell.kfs.tax.util.TaxParameterUtils;

@Execution(ExecutionMode.SAME_THREAD)
@CreateTestDirectories(
        baseDirectory = TaxFileGenerationServiceSprintaxImplTest.TEST_TAX_DIRECTORY,
        subDirectories = {
                TaxFileGenerationServiceSprintaxImplTest.TEST_TAX_STAGING_DIRECTORY,
                TaxFileGenerationServiceSprintaxImplTest.TEST_TAX_SPRINTAX_DIRECTORY
        }
)
public class TaxFileGenerationServiceSprintaxImplTest {

    static final String TEST_TAX_DIRECTORY = "test/tax_sprintax_file_print/";
    static final String TEST_TAX_STAGING_DIRECTORY = TEST_TAX_DIRECTORY + "staging/tax/";
    static final String TEST_TAX_SPRINTAX_DIRECTORY = TEST_TAX_STAGING_DIRECTORY + "sprintax/";

    private static final String TEST_CASE_BASE_DIRECTORY = "classpath:edu/cornell/kfs/tax/batch/sprintax-file-test/";

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/tax/batch/cu-spring-tax-sprintax-file-printing-test.xml");

    private TaxFileGenerationServiceSprintaxImpl taxFileGenerationService;
    private TestDataHelperDao testDataHelperDao;
    private TestTransactionDetailCsvInputFileType testTransactionDetailCsvInputFileType;
    private AtomicReference<LocalTestCase> testCaseHolder;
    // private PlatformTransactionManager transactionManager;
    // private TransactionStatus transactionStatus;

    @BeforeAll
    static void performFirstTimeInitialization() throws Exception {
        final TestDataHelperDao helperDao = getTestDataHelperDao();
        TestTaxSqlUtils.createTransactionDetailTable(helperDao);
        TestTaxSqlUtils.createAbridgedVendorHeaderTable(helperDao);
        TestTaxSqlUtils.createAbridgedVendorDetailTable(helperDao);
        TestTaxSqlUtils.createAbridgedVendorAddressTable(helperDao);
        TestTaxSqlUtils.createAbridgedNoteTable(helperDao);
        TestTaxSqlUtils.createAbridgedDocumentHeaderTableForNoteLinkingOnly(helperDao);
    }

    private static TestDataHelperDao getTestDataHelperDao() {
        return springContextExtension.getBean("testDataHelperDao", TestDataHelperDao.class);
    }

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        taxFileGenerationService = (TaxFileGenerationServiceSprintaxImpl) springContextExtension.getBean(
                TaxSpringBeans.TAX_FILE_GENERATION_SERVICE_FOR_1042S, TaxFileGenerationService.class);
        testTransactionDetailCsvInputFileType = springContextExtension.getBean(
                TaxSpringBeans.TEST_TRANSACTION_DETAIL_CSV_INPUT_FILE_TYPE, TestTransactionDetailCsvInputFileType.class);
        testCaseHolder = (AtomicReference<LocalTestCase>) springContextExtension.getBean(
                TaxSpringBeans.TEST_CASE_HOLDER, AtomicReference.class);
        testDataHelperDao = getTestDataHelperDao();
        // transactionManager = springContextExtension.getBean("transactionManager", PlatformTransactionManager.class);

        // final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        // transactionDefinition.setTimeout(3600);
        // transactionStatus = transactionManager.getTransaction(transactionDefinition);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (testDataHelperDao != null) {
            resetDatabaseTables();
        }
        // if (transactionStatus != null && transactionManager != null) {
        //     transactionManager.rollback(transactionStatus);
        // }
        // transactionStatus = null;
        // transactionManager = null;
        testDataHelperDao = null;
        testCaseHolder = null;
        testTransactionDetailCsvInputFileType = null;
        taxFileGenerationService = null;
    }

    private void resetDatabaseTables() throws Exception {
        testDataHelperDao.disconnectTablesFromCsvFiles(List.of(
                TableNames.TX_TRANSACTION_DETAIL_T,
                TableNames.PUR_VNDR_HDR_T,
                TableNames.PUR_VNDR_DTL_T,
                TableNames.PUR_VNDR_ADDR_T,
                TableNames.KRNS_NTE_T,
                TableNames.FS_DOC_HEADER_T
        ));
    }

    @SpringXmlTestBeanFactoryMethod
    public static AtomicReference<LocalTestCase> buildTestCaseHolder() {
        return new AtomicReference<>();
    }

    @SpringXmlTestBeanFactoryMethod
    public static TransactionOverrideService buildMockTransactionOverrideService(
            final AtomicReference<LocalTestCase> testCaseHolder) {
        return new CuMockBuilder<>(TransactionOverrideService.class)
                .build();
    }

    @SpringXmlTestBeanFactoryMethod
    public static ParameterService buildMockParameterService() {
        final ParameterService service = TaxParameterUtils.createUpdatableMockParameterServiceForTaxProcessing();
        final Map<String, String> parameterMappings = getTaxParameterMappings();
        final Set<String> parametersWithDisallowEvals = getTaxParametersWithDisallowEvals();

        for (Map.Entry<String, String> parameterMapping : parameterMappings.entrySet()) {
            final String parameterName = parameterMapping.getKey();
            final String parameterValue = parameterMapping.getValue();
            final EvaluationOperator evaluationOperator = parametersWithDisallowEvals.contains(parameterName)
                    ? EvaluationOperator.DISALLOW : EvaluationOperator.ALLOW;
            final Parameter parameter = TaxParameterUtils.create1042SParameter(parameterName, parameterValue,
                    evaluationOperator);
            service.updateParameter(parameter);
        }

        return service;
    }

    private static Map<String, String> getTaxParameterMappings() {
        return Map.ofEntries(
                Map.entry(Tax1042SParameterNames.CHAPTER3_EXEMPTION_CODES, "NotExempt=00;TaxTreaty=04;ForeignSource=03"),
                Map.entry(Tax1042SParameterNames.CHAPTER4_DEFAULT_EXEMPTION_CODE, "15"),
                Map.entry(Tax1042SParameterNames.CHAPTER4_STATUS_CODES_TO_CHAPTER4_EXEMPTION_CODES, "18=16"),
                Map.entry(get1042SParameterName(TaxCommonParameterNames.DATES_TO_PROCESS_PARAMETER_SUFFIX), "2024"),
                Map.entry(get1042SParameterName(TaxCommonParameterNames.EXCLUDE_BY_OWNERSHIP_TYPE_PARAMETER_SUFFIX), ""),
                Map.entry(get1042SParameterName(TaxCommonParameterNames.EXCLUDE_BY_VENDOR_TYPE_PARAMETER_SUFFIX), ""),
                Map.entry(Tax1042SParameterNames.EXCLUDED_DOC_NOTE_TEXT, "void"),
                Map.entry(Tax1042SParameterNames.EXCLUDED_INCOME_CODE, "##"),
                Map.entry(Tax1042SParameterNames.EXCLUDED_INCOME_CODE_SUB_TYPE, "#"),
                Map.entry(Tax1042SParameterNames.EXCLUDED_PAYMENT_REASON_CODE, ""),
                Map.entry(Tax1042SParameterNames.FEDERAL_TAX_WITHHELD_INCLUDED_OBJECT_CODE_CHART_ACCOUNT, "2999=IT-G555555"),
                Map.entry(Tax1042SParameterNames.FEDERAL_TAX_WITHHELD_VALID_OBJECT_CODES, "2999"),
                Map.entry(Tax1042SParameterNames.INCOME_CLASS_CODE_DENOTING_ROYALTIES, ""),
                Map.entry(Tax1042SParameterNames.INCOME_CLASS_CODE_TO_IRS_INCOME_CODE, "A=23;F=16;I=17;R=12;T=19;Z=42"),
                Map.entry(Tax1042SParameterNames.INCOME_CLASS_CODE_TO_IRS_INCOME_CODE_SUB_TYPE, "A=N;F=L;I=N;N=N;R=C;T=N;Z=N"),
                Map.entry(Tax1042SParameterNames.INCOME_CLASS_CODE_VALID_OBJECT_CODES, "A=6730;F=2;I=6730;R=4;T=;Z="),
                Map.entry(Tax1042SParameterNames.NON_REPORTABLE_INCOME_CODE, "XX"),
                Map.entry(Tax1042SParameterNames.OTHER_INCOME_EXCLUDED_PMT_LN1_ADDR, "Petty Cash"),
                Map.entry(Tax1042SParameterNames.PDP_EXCLUDED_DOC_TYPES, "APPP;APQQ"),
                Map.entry(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_AND_DV_CHK_STUB_TEXT, ""),
                Map.entry(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT, ""),
                Map.entry(get1042SParameterName(TaxCommonParameterNames.SOLE_PROPRIETOR_OWNER_CODE_PARAMETER_SUFFIX), "ID"),
                Map.entry(Tax1042SParameterNames.STATE_INCOME_TAX_WITHHELD_INCLUDED_OBJECT_CODE_CHART_ACCOUNT, ""),
                Map.entry(Tax1042SParameterNames.STATE_INCOME_TAX_WITHHELD_VALID_OBJECT_CODES, ""),
                Map.entry(Tax1042SParameterNames.STATE_NAME, "NY"),
                Map.entry(Tax1042SParameterNames.VENDOR_OWNERSHIP_TO_CHAPTER3_STATUS_CODE, "CP=15;ET=17;PT=08;IO=19;GV=19;ID=16;NP=20;SC=XX")
        );
    }

    private static Set<String> getTaxParametersWithDisallowEvals() {
        return Set.of(Tax1042SParameterNames.ROYALTIES_INCLUDED_OBJECT_CODE_CHART_ACCOUNT);
    }

    private static String get1042SParameterName(final String parameterNameSuffix) {
        return StringUtils.join(CUTaxConstants.TAX_TYPE_1042S, CUKFSConstants.UNDERSCORE, parameterNameSuffix);
    }

    enum LocalTestCase {

        BASIC_SINGLE_ROW_TEST("single-row-case-source-data.csv", "single-row-case-expected-transactions.csv",
                "single-row-case-expected-demographic-file.csv", "single-row-case-expected-payment-file.csv")
        /*BASIC_SINGLE_ROW_TEST2("single-row-case-source-data.csv", "single-row-case-expected-transactions.csv",
                "single-row-case-expected-demographic-file.csv", "single-row-case-expected-payment-file.csv"),
        BASIC_SINGLE_ROW_TEST3("single-row-case-source-data.csv", "single-row-case-expected-transactions.csv",
                "single-row-case-expected-demographic-file.csv", "single-row-case-expected-payment-file.csv"),
        BASIC_SINGLE_ROW_TEST4("single-row-case-source-data.csv", "single-row-case-expected-transactions.csv",
                "single-row-case-expected-demographic-file.csv", "single-row-case-expected-payment-file.csv")*/
        ;

        private final String sourceFileName;
        private final String expectedTransactionDataFileName;
        private final String expectedDemographicFileName;
        private final String expectedPaymentFileName;

        private LocalTestCase(final String sourceFileName, final String expectedTransactionDataFileName,
                final String expectedDemographicFileName, final String expectedPaymentFileName) {
            this.sourceFileName = sourceFileName;
            this.expectedTransactionDataFileName = expectedTransactionDataFileName;
            this.expectedDemographicFileName = expectedDemographicFileName;
            this.expectedPaymentFileName = expectedPaymentFileName;
        }

    }

    @ParameterizedTest
    @EnumSource
    void testSuccessfulGenerationOfSprintaxFiles(final LocalTestCase testCase) throws Exception {
        populateDatabaseTables(testCase);
        final TaxBatchConfig batchConfig = buildTaxBatchConfigFor1042S();
        final TaxStatistics statistics = GlobalResourceLoaderUtils
                .doWithResourceRetrievalDelegatedToKradResourceLoaderUtil(
                        () -> taxFileGenerationService.generateFiles(batchConfig));
        testDataHelperDao.forciblyCommitTransaction();

        assertNotNull(statistics, "statistics object from 1042-S generation process should not have been null");
        assertTransactionDetailsWereUpdatedAsExpected(testCase);
        assertDemographicFileDataIsCorrect(testCase);
        assertPaymentFileDataIsCorrect(testCase);
    }

    private void populateDatabaseTables(final LocalTestCase testCase) throws Exception {
        final String fullSourceFilePath = TEST_CASE_BASE_DIRECTORY + testCase.sourceFileName;
        testDataHelperDao.splitAndConnectCsvFileToDatabase(fullSourceFilePath);
        testDataHelperDao.forciblyCommitTransaction();
    }

    private TaxBatchConfig buildTaxBatchConfigFor1042S() {
        return new TaxBatchConfig(Mode.CREATE_TAX_FILES, CUTaxConstants.TAX_TYPE_1042S, 2024,
                TestDateUtils.toUtilDate("2025-03-01T14:30:45"),
                TestDateUtils.toSqlDate("2024-01-01"), TestDateUtils.toSqlDate("2024-12-31"));
    }

    private void assertTransactionDetailsWereUpdatedAsExpected(final LocalTestCase testCase) throws Exception {
        final String fileWithExpectedTransactionDetails =
                TEST_CASE_BASE_DIRECTORY + testCase.expectedTransactionDataFileName;
        final String fileWithActualTransactionDetails = testDataHelperDao.generateFileNameForTableDerivedFromCsvChunk(
                testCase.sourceFileName, TableNames.TX_TRANSACTION_DETAIL_T);
        final List<TransactionDetail> expectedDetails = getTransactionDetailsFromCsvFile(
                fileWithExpectedTransactionDetails);
        final List<TransactionDetail> actualDetails = getTransactionDetailsFromCsvFile(
                fileWithActualTransactionDetails);
        decryptTaxIdsOnTransactionDetails(actualDetails);
        assertTransactionDetailsMatch(expectedDetails, actualDetails);
    }

    private void assertTransactionDetailsMatch(final List<TransactionDetail> expectedDetails,
            final List<TransactionDetail> actualDetails) {
        assertEquals(expectedDetails.size(), actualDetails.size(), "Wrong number of persisted transaction details");

        final Map<String, TransactionDetail> expectedDetailsMap = expectedDetails.stream()
                .collect(Collectors.toUnmodifiableMap(TransactionDetail::getTransactionDetailId, Function.identity()));
        final Set<String> encounteredDetails = new HashSet<>();

        for (final TransactionDetail actualDetail : actualDetails) {
            assertTrue(StringUtils.isNotBlank(actualDetail.getTransactionDetailId()),
                    "Found a transaction detail with a blank ID");
            assertTrue(encounteredDetails.add(actualDetail.getTransactionDetailId()),
                    "Unexpected duplicate transaction detail: " + actualDetail.getTransactionDetailId());

            final TransactionDetail expectedDetail = expectedDetailsMap.get(actualDetail.getTransactionDetailId());
            assertNotNull(expectedDetail, "Unexpected transaction detail: " + actualDetail.getTransactionDetailId());
            assertEquals(expectedDetail, actualDetail, "Wrong transaction detail data");
        }
    }

    @SuppressWarnings("unchecked")
    private List<TransactionDetail> getTransactionDetailsFromCsvFile(final String filePath) throws Exception {
        final String fileContents = TestFileUtils.getFileContentsWithoutFirstLineAndSubsequentBlankLines(filePath);
        final String fieldNameHeader = createTransactionDetailFieldNameHeader();
        final String contentsToParse = StringUtils.join(fieldNameHeader, KFSConstants.NEWLINE, fileContents);
        final byte[] byteContents = contentsToParse.getBytes(StandardCharsets.UTF_8);
        return (List<TransactionDetail>) testTransactionDetailCsvInputFileType.parse(byteContents);
    }

    private String createTransactionDetailFieldNameHeader() {
        return Arrays.stream(TransactionDetailField.values())
                .map(TransactionDetailField::getFieldName)
                .collect(Collectors.joining(KFSConstants.COMMA));
    }

    private void decryptTaxIdsOnTransactionDetails(final List<TransactionDetail> transactionDetails) {
        for (final TransactionDetail transactionDetail : transactionDetails) {
            final String decryptedTaxNumber = testDataHelperDao.decrypt(transactionDetail.getVendorTaxNumber());
            transactionDetail.setVendorTaxNumber(decryptedTaxNumber);
        }
    }

    private void assertDemographicFileDataIsCorrect(final LocalTestCase testCase) throws Exception {
        final String expectedDemographicFilePath = TEST_CASE_BASE_DIRECTORY + testCase.expectedDemographicFileName;
        final String actualDemographicFileName = findSprintaxStagingFileNameWithPrefix(
                CUTaxConstants.Sprintax.DEMOGRAPHIC_OUTPUT_FILE_PREFIX);
        final String actualDemographicFilePath = TEST_TAX_SPRINTAX_DIRECTORY + actualDemographicFileName;
        TestFileUtils.assertFileContentsMatch(expectedDemographicFilePath, actualDemographicFilePath);
    }

    private void assertPaymentFileDataIsCorrect(final LocalTestCase testCase) throws Exception {
        final String expectedPaymentFilePath = TEST_CASE_BASE_DIRECTORY + testCase.expectedPaymentFileName;
        final String actualPaymentFileName = findSprintaxStagingFileNameWithPrefix(
                CUTaxConstants.Sprintax.PAYMENTS_OUTPUT_FILE_PREFIX);
        final String actualPaymentFilePath = TEST_TAX_SPRINTAX_DIRECTORY + actualPaymentFileName;
        TestFileUtils.assertFileContentsMatch(expectedPaymentFilePath, actualPaymentFilePath);
    }

    private String findSprintaxStagingFileNameWithPrefix(final String fileNamePrefix) {
        final File sprintaxStagingDirectory = new File(TEST_TAX_SPRINTAX_DIRECTORY);
        final String[] filesWithPrefix = sprintaxStagingDirectory.list(
                (directory, name) -> StringUtils.startsWith(name, fileNamePrefix));
        assertEquals(1, filesWithPrefix.length, "Wrong number of files with prefix " + fileNamePrefix);
        System.out.println(filesWithPrefix[0]);
        return filesWithPrefix[0];
    }

}
