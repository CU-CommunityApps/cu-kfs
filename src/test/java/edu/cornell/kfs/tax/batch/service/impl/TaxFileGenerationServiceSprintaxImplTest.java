package edu.cornell.kfs.tax.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.sys.dataaccess.TestDataHelperDao;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import edu.cornell.kfs.sys.util.CuMockBuilder;
import edu.cornell.kfs.sys.util.GlobalResourceLoaderUtils;
import edu.cornell.kfs.sys.util.SpringXmlTestBeanFactoryMethod;
import edu.cornell.kfs.sys.util.TestDateUtils;
import edu.cornell.kfs.sys.util.TestFileUtils;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CuTaxTestConstants.TaxSpringBeans;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxBatchConfig.Mode;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;
import edu.cornell.kfs.tax.batch.util.TestTaxSqlUtils;
import edu.cornell.kfs.tax.batch.util.TestTaxSqlUtils.ColumnNames;
import edu.cornell.kfs.tax.batch.util.TestTaxSqlUtils.TableNames;
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
    private AtomicReference<LocalTestCase> testCaseHolder;
    // private PlatformTransactionManager transactionManager;
    // private TransactionStatus transactionStatus;

    @BeforeAll
    static void performFirstTimeInitialization() throws Exception {
        final TestDataHelperDao helperDao = getTestDataHelperDao();
        TestTaxSqlUtils.createTransactionDetailTable(helperDao);
        TestTaxSqlUtils.createTransactionDetailTableForLoadingCsvData(helperDao);
        TestTaxSqlUtils.createAbridgedVendorHeaderTable(helperDao);
        TestTaxSqlUtils.createAbridgedVendorHeaderTableForLoadingCsvData(helperDao);
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
        taxFileGenerationService = null;
    }

    private void resetDatabaseTables() throws Exception {
        testDataHelperDao.disconnectTablesFromCsvFiles(List.of(
                TableNames.TX_TRANSACTION_DETAIL_T_CSV,
                TableNames.PUR_VNDR_HDR_T_CSV,
                TableNames.PUR_VNDR_DTL_T,
                TableNames.PUR_VNDR_ADDR_T,
                TableNames.KRNS_NTE_T,
                TableNames.FS_DOC_HEADER_T
        ));
        testDataHelperDao.truncateTables(List.of(
                TableNames.TX_TRANSACTION_DETAIL_T,
                TableNames.PUR_VNDR_HDR_T
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
        return service;
    }

    enum LocalTestCase {

        BASIC_SINGLE_ROW_TEST("single-row-case-source-data.csv", "single-row-case-expected-transactions.csv",
                "single-row-case-expected-demographic-file.csv", "single-row-case-expected-payment-file.csv");
        /*BASIC_SINGLE_ROW_TEST2("single-row-case-source-data.csv"),
        BASIC_SINGLE_ROW_TEST3("single-row-case-source-data.csv"),
        BASIC_SINGLE_ROW_TEST4("single-row-case-source-data.csv"),
        BASIC_SINGLE_ROW_TEST5("single-row-case-source-data.csv");*/

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
    void testSomething(final LocalTestCase testCase) throws Exception {
        populateDatabaseTables(testCase);
        final TaxBatchConfig batchConfig = buildTaxBatchConfigFor1042S();
        final TaxStatistics statistics = GlobalResourceLoaderUtils
                .doWithResourceRetrievalDelegatedToKradResourceLoaderUtil(
                        () -> taxFileGenerationService.generateFiles(batchConfig));
        assertNotNull(statistics, "statistics object from 1042-S generation process should not have been null");
        testDataHelperDao.forciblyCommitTransaction();
        assertTransactionDetailsWereUpdatedAsExpected(testCase);
    }

    private void populateDatabaseTables(final LocalTestCase testCase) throws Exception {
        final String fullSourceFilePath = TEST_CASE_BASE_DIRECTORY + testCase.sourceFileName;
        testDataHelperDao.splitAndConnectCsvFileToDatabase(fullSourceFilePath);
        testDataHelperDao.forciblyCommitTransaction();
        testDataHelperDao.forciblyEncryptColumns(
                TableNames.TX_TRANSACTION_DETAIL_T, List.of(ColumnNames.VENDOR_TAX_NBR));
        testDataHelperDao.forciblyEncryptColumns(
                TableNames.PUR_VNDR_HDR_T, List.of(ColumnNames.VNDR_US_TAX_NBR));
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
        TestFileUtils.assertFileContentsMatch(fileWithExpectedTransactionDetails, fileWithActualTransactionDetails);
    }

}
