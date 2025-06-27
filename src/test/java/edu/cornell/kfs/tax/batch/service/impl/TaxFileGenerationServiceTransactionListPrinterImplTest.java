package edu.cornell.kfs.tax.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableSupplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import edu.cornell.kfs.sys.util.CuMockBuilder;
import edu.cornell.kfs.sys.util.GlobalResourceLoaderUtils;
import edu.cornell.kfs.sys.util.SpringXmlTestBeanFactoryMethod;
import edu.cornell.kfs.sys.util.TestDateUtils;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CuTaxTestConstants.TaxSpringBeans;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxBatchConfig.Mode;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.TestTransactionDetailCsvInputFileType;
import edu.cornell.kfs.tax.batch.dataaccess.TaxDtoRowMapper;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailHandler;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailProcessorDao;
import edu.cornell.kfs.tax.batch.dataaccess.impl.TaxDtoRowMapperTestImpl;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

@Execution(ExecutionMode.SAME_THREAD)
@CreateTestDirectories(
        baseDirectory = TaxFileGenerationServiceTransactionListPrinterImplTest.TEST_TAX_DIRECTORY,
        subDirectories = {
                TaxFileGenerationServiceTransactionListPrinterImplTest.TEST_TAX_STAGING_DIRECTORY,
                TaxFileGenerationServiceTransactionListPrinterImplTest.TEST_TAX_TRANSACTIONS_DIRECTORY,
                TaxFileGenerationServiceTransactionListPrinterImplTest.TEST_TAX_TRANSACTIONS_CSV_DIRECTORY
        }
)
public class TaxFileGenerationServiceTransactionListPrinterImplTest {

    static final String TEST_TAX_DIRECTORY = "test/tax_transaction_list_print/";
    static final String TEST_TAX_STAGING_DIRECTORY = TEST_TAX_DIRECTORY + "staging/tax/";
    static final String TEST_TAX_TRANSACTIONS_DIRECTORY = TEST_TAX_STAGING_DIRECTORY + "transactions/";
    static final String TEST_TAX_TRANSACTIONS_CSV_DIRECTORY = TEST_TAX_STAGING_DIRECTORY + "transactions_csv/";

    private static final String BASE_TRANSACTION_FILES_DIRECTORY =
            "classpath:edu/cornell/kfs/tax/batch/transaction-list-printing-test/";
    private static final String MASKED_TRANSACTION_FILE_SUFFIX = "-masked" + CUKFSConstants.TEXT_FILE_EXTENSION;

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/tax/batch/cu-spring-tax-transaction-list-printing-test.xml");

    private TaxFileGenerationServiceTransactionListPrinterImpl taxFileGenerationService;
    private AtomicReference<LocalTestCase> testCaseHolder;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        taxFileGenerationService = springContextExtension.getBean(
                TaxSpringBeans.TAX_FILE_GENERATION_SERVICE_FOR_TRANSACTION_LIST_PRINTING,
                TaxFileGenerationServiceTransactionListPrinterImpl.class);
        testCaseHolder = springContextExtension.getBean(
                TaxSpringBeans.TEST_CASE_HOLDER, AtomicReference.class);

        taxFileGenerationService.setMaskSensitiveData(false);
        testCaseHolder.set(null);
    }

    @AfterEach
    void tearDown() throws Exception {
        testCaseHolder = null;
        taxFileGenerationService = null;
    }



    @SpringXmlTestBeanFactoryMethod
    public static AtomicReference<LocalTestCase> buildTestCaseHolder() {
        return new AtomicReference<>();
    }

    @SpringXmlTestBeanFactoryMethod
    public static TransactionDetailProcessorDao buildMockTransactionDetailProcessorDao(
            final TestTransactionDetailCsvInputFileType transactionDetailCsvFileType,
            final AtomicReference<LocalTestCase> testCaseHolder) throws Exception {
        return new CuMockBuilder<>(TransactionDetailProcessorDao.class)
                .withAnswer(
                        dao -> dao.processTransactionDetails(Mockito.any(), Mockito.any()),
                        invocation -> processPredefinedTransactionDetails(
                                invocation, transactionDetailCsvFileType, testCaseHolder))
                .build();
    }

    private static TaxStatistics processPredefinedTransactionDetails(final InvocationOnMock invocation,
            final TestTransactionDetailCsvInputFileType transactionDetailCsvFileType,
            final AtomicReference<LocalTestCase> testCaseHolder) throws Exception {
        final TaxBatchConfig config = invocation.getArgument(0);
        final TransactionDetailHandler handler = invocation.getArgument(1);
        Validate.notNull(config, "config should not have been null");
        Validate.notNull(handler, "handler should not have been null");

        final List<TransactionDetail> transactionDetails = buildTransactionDetailsFromCsvData(
                transactionDetailCsvFileType, testCaseHolder);
        final TaxDtoRowMapper<TransactionDetail> rowMapper =
                new TaxDtoRowMapperTestImpl<>(transactionDetails);

        return GlobalResourceLoaderUtils.doWithResourceRetrievalDelegatedToKradResourceLoaderUtil(
                () -> handler.performProcessing(config, rowMapper));
    }

    @SuppressWarnings("unchecked")
    private static List<TransactionDetail> buildTransactionDetailsFromCsvData(
            final TestTransactionDetailCsvInputFileType transactionDetailCsvFileType,
            final AtomicReference<LocalTestCase> testCaseHolder) throws Exception {
        final LocalTestCase testCase = testCaseHolder.get();
        Validate.validState(testCase != null, "Test case enum constant was not passed to the AtomicReference");

        final String filePath = BASE_TRANSACTION_FILES_DIRECTORY + testCase.sourceDataFile;
        try (final InputStream fileStream = CuCoreUtilities.getResourceAsStream(filePath)) {
            final byte[] fileContents = IOUtils.toByteArray(fileStream);
            return (List<TransactionDetail>) transactionDetailCsvFileType.parse(fileContents);
        }

    }



    enum LocalTestCase {

        EMPTY_DATA("empty-rows-source.csv", "empty-rows-target.txt"),
        SINGLE_1042S_ROW("single-1042s-row-source.csv", "single-1042s-row-target.txt"),
        MULTI_1042S_ROWS("multi-1042s-rows-source.csv", "multi-1042s-rows-target.txt");

        private final String sourceDataFile;
        private final String expectedResultFile;

        private LocalTestCase(final String sourceDataFile, final String expectedResultFile) {
            this.sourceDataFile = sourceDataFile;
            this.expectedResultFile = expectedResultFile;
        }

        private String getExpectedResultFileNameForMaskedResults() {
            final String nameWithoutExtension = StringUtils.substringBeforeLast(
                    expectedResultFile, CUKFSConstants.TEXT_FILE_EXTENSION);
            return nameWithoutExtension + MASKED_TRANSACTION_FILE_SUFFIX;
        }

    }

    @ParameterizedTest
    @EnumSource(LocalTestCase.class)
    void testTransactionRowPrinting(final LocalTestCase testCase) throws Exception {
        final TaxBatchConfig config = buildTaxBatchConfigFor1042S();
        testCaseHolder.set(testCase);

        taxFileGenerationService.generateFiles(config);
        assertTransactionDetailsWerePrintedCorrectly(testCase.expectedResultFile);
    }

    @ParameterizedTest
    @EnumSource(LocalTestCase.class)
    void testMaskedTransactionRowPrinting(final LocalTestCase testCase) throws Exception {
        final TaxBatchConfig config = buildTaxBatchConfigFor1042S();
        testCaseHolder.set(testCase);
        taxFileGenerationService.setMaskSensitiveData(true);

        taxFileGenerationService.generateFiles(config);
        assertTransactionDetailsWerePrintedCorrectly(testCase.getExpectedResultFileNameForMaskedResults());
    }

    private TaxBatchConfig buildTaxBatchConfigFor1042S() {
        return new TaxBatchConfig(Mode.CREATE_TRANSACTION_LIST_FILE, CUTaxConstants.TAX_TYPE_1042S, 2025,
                TestDateUtils.toLocalDateTime("2025-03-01T14:30:45"),
                TestDateUtils.toSqlDate("2024-01-01"), TestDateUtils.toSqlDate("2024-12-31"));
    }

    private void assertTransactionDetailsWerePrintedCorrectly(final String expectedResultFileName) throws Exception {
        final String expectedResultFilePath = BASE_TRANSACTION_FILES_DIRECTORY + expectedResultFileName;
        final File generatedFile = getGeneratedTransactionPrintingFile();
        final String expectedFileData = getFileContents(
                () -> CuCoreUtilities.getResourceAsStream(expectedResultFilePath));
        final String actualFileData = getFileContents(() -> new FileInputStream(generatedFile));
        assertEquals(expectedFileData, actualFileData, "Wrong file contents");
    }

    private File getGeneratedTransactionPrintingFile() {
        final File transactionsDirectory = new File(TEST_TAX_TRANSACTIONS_DIRECTORY);
        final Collection<File> files = FileUtils.listFiles(transactionsDirectory, null, false);
        assertEquals(1, files.size(), "Wrong number of files found in destination directory");
        return files.iterator().next();
    }

    private String getFileContents(final FailableSupplier<InputStream, IOException> inputStreamGenerator)
            throws IOException {
        try (final InputStream inputStream = inputStreamGenerator.get()) {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }

}
