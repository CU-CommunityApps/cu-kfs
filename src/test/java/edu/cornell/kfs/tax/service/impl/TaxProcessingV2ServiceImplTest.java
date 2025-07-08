package edu.cornell.kfs.tax.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;
import edu.cornell.kfs.sys.util.CuMockBuilder;
import edu.cornell.kfs.sys.util.FixtureUtils;
import edu.cornell.kfs.sys.util.SpringXmlTestBeanFactoryMethod;
import edu.cornell.kfs.sys.util.TestDateUtils;
import edu.cornell.kfs.sys.util.TestSpringContextExtension;
import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.CuTaxTestConstants.TaxSpringBeans;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.TaxStatistics;
import edu.cornell.kfs.tax.batch.service.TaxFileGenerationService;
import edu.cornell.kfs.tax.dataaccess.TaxProcessingDao;
import edu.cornell.kfs.tax.dataaccess.impl.TaxStatType;
import edu.cornell.kfs.tax.fixture.TaxConfigTestCase;
import edu.cornell.kfs.tax.util.TaxParameterUtils;
import edu.cornell.kfs.tax.util.TaxUtils;

@Execution(ExecutionMode.SAME_THREAD)
public class TaxProcessingV2ServiceImplTest {

    private static final LocalDate TEST_DATE_2025_01_10 = LocalDate.of(2025, 1, 10);

    @RegisterExtension
    static TestSpringContextExtension springContextExtension = TestSpringContextExtension.forClassPathSpringXmlFile(
            "edu/cornell/kfs/tax/batch/cu-spring-tax-processing-v2-service-test.xml");

    private TaxProcessingV2ServiceImpl taxProcessingV2Service;
    private ParameterService mockParameterService;
    private AtomicReference<TaxBatchConfig> taxConfigHolderFor1042S;
    private AtomicReference<TaxBatchConfig> taxConfigHolderForTransactionPrinting;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        taxProcessingV2Service = springContextExtension.getBean(
                TaxSpringBeans.TAX_PROCESSING_V2_SERVICE, TaxProcessingV2ServiceImpl.class);
        mockParameterService = springContextExtension.getBean(
                TaxSpringBeans.BASE_PARAMETER_SERVICE, ParameterService.class);
        taxConfigHolderFor1042S = springContextExtension.getBean(
                TaxSpringBeans.TEST_TAX_CONFIG_HOLDER_FOR_1042S, AtomicReference.class);
        taxConfigHolderForTransactionPrinting = springContextExtension.getBean(
                TaxSpringBeans.TEST_TAX_CONFIG_HOLDER_FOR_TRANSACTION_LIST_PRINTING, AtomicReference.class);
        taxConfigHolderFor1042S.set(null);
        taxConfigHolderForTransactionPrinting.set(null);
    }

    @AfterEach
    void shutDown() throws Exception {
        if (taxConfigHolderForTransactionPrinting != null) {
            taxConfigHolderForTransactionPrinting.set(null);
        }
        if (taxConfigHolderFor1042S != null) {
            taxConfigHolderFor1042S.set(null);
        }
        taxConfigHolderForTransactionPrinting = null;
        taxConfigHolderFor1042S = null;
        mockParameterService = null;
        taxProcessingV2Service = null;
    }



    @SpringXmlTestBeanFactoryMethod
    public static ParameterService buildMockParameterService() {
        final ParameterService service = TaxParameterUtils.createUpdatableMockParameterServiceForTaxProcessing();
        service.createParameter(generate1042SDatesToProcessParameter(KFSConstants.EMPTY_STRING));
        return service;
    }

    private static Parameter generate1042SDatesToProcessParameter(final String value) {
        return TaxParameterUtils.create1042SParameter(
                CUTaxConstants.TAX_TYPE_1042S + TaxCommonParameterNames.DATES_TO_PROCESS_PARAMETER_SUFFIX, value);
    }

    @SpringXmlTestBeanFactoryMethod
    public static AtomicReference<TaxBatchConfig> buildTaxConfigHolder() {
        return new AtomicReference<>();
    }

    @SpringXmlTestBeanFactoryMethod
    public static TaxFileGenerationService buildMockTaxFileGenerationService(
            final AtomicReference<TaxBatchConfig> configHolder) throws Exception {
        return new CuMockBuilder<>(TaxFileGenerationService.class)
                .withAnswer(
                        service -> service.generateFiles(Mockito.any()),
                        invocation -> handleTaxFileGenerationServiceCall(invocation, configHolder))
                .build();
    }

    private static TaxStatistics handleTaxFileGenerationServiceCall(
            final InvocationOnMock invocation, final AtomicReference<TaxBatchConfig> configHolder) {
        final TaxBatchConfig config = invocation.getArgument(0);
        configHolder.set(config);
        return (config.getMode() == TaxBatchConfig.Mode.CREATE_TAX_FILES)
                ? TaxUtils.generateBaseStatisticsFor1042S()
                : new TaxStatistics(TaxStatType.NUM_TRANSACTION_ROWS);
    }

    @SpringXmlTestBeanFactoryMethod
    public static TaxProcessingDao buildMockTaxProcessingDao() {
        return Mockito.mock(TaxProcessingDao.class);
    }

    @SpringXmlTestBeanFactoryMethod
    public static DateTimeService buildSpiedTestDateTimeService() throws Exception {
        final TestDateTimeServiceImpl dateTimeService = new TestDateTimeServiceImpl();
        dateTimeService.afterPropertiesSet();
        return new CuMockBuilder<>(dateTimeService)
                .withReturn(TestDateTimeServiceImpl::getLocalDateNow, TEST_DATE_2025_01_10)
                .build();
    }



    enum LocalTestCase {
        @TaxConfigTestCase(
                datesToProcessSetting = CUTaxConstants.YEAR_TO_DATE,
                taxType = CUTaxConstants.TAX_TYPE_1042S,
                reportYear = 2025,
                processingStartDate = "2025-01-12T10:55:34",
                taxDateRangeStart = "2025-01-01",
                taxDateRangeEnd = "2025-12-31")
        CONFIG_1042S_YEAR_TO_DATE,

        @TaxConfigTestCase(
                datesToProcessSetting = CUTaxConstants.PREVIOUS_YEAR_TO_DATE,
                taxType = CUTaxConstants.TAX_TYPE_1042S,
                reportYear = 2024,
                processingStartDate = "2025-02-15T22:55:34",
                taxDateRangeStart = "2024-01-01",
                taxDateRangeEnd = "2024-12-31")
        CONFIG_1042S_PREVIOUS_YEAR_TO_DATE,

        @TaxConfigTestCase(
                datesToProcessSetting = "2021",
                taxType = CUTaxConstants.TAX_TYPE_1042S,
                reportYear = 2021,
                processingStartDate = "2024-01-03T01:23:45",
                taxDateRangeStart = "2021-01-01",
                taxDateRangeEnd = "2021-12-31")
        CONFIG_1042S_EXPLICIT_YEAR,

        @TaxConfigTestCase(
                datesToProcessSetting = "02/07/2024;10/13/2024",
                taxType = CUTaxConstants.TAX_TYPE_1042S,
                reportYear = 2024,
                processingStartDate = "2025-01-09T01:23:45",
                taxDateRangeStart = "2024-02-07",
                taxDateRangeEnd = "2024-10-13")
        CONFIG_1042S_EXPLICIT_DATE_RANGE;

        public Arguments toNamedAnnotationFixtureArgument() {
            return FixtureUtils.createNamedAnnotationFixtureArgument(this, TaxConfigTestCase.class);
        }
    }

    static Stream<Arguments> taxBatchConfigurationsFor1042S() {
        return Arrays.stream(LocalTestCase.values())
                .map(LocalTestCase::toNamedAnnotationFixtureArgument);
    } 



    @ParameterizedTest
    @MethodSource("taxBatchConfigurationsFor1042S")
    void testTaxBatchConfigObjectSetupFor1042S(final TaxConfigTestCase testCase) throws Exception {
        assertTaxConfigHoldersAreInDefaultState();
        update1042SDatesToProcessParameter(testCase.datesToProcessSetting());

        final LocalDateTime testStartDate = TestDateUtils.toLocalDateTime(testCase.processingStartDate());
        final TaxBatchConfig expectedConfig = TaxConfigTestCase.Utils.toTaxBatchConfig(testCase);
        final TaxBatchConfig expectedPrintingConfig = TaxConfigTestCase.Utils.toTaxBatchConfig(
                testCase, TaxBatchConfig.Mode.CREATE_TRANSACTION_LIST_FILE);

        taxProcessingV2Service.performTaxProcessingFor1042S(testStartDate);

        final TaxBatchConfig actualConfig = taxConfigHolderFor1042S.get();
        final TaxBatchConfig actualPrintingConfig = taxConfigHolderForTransactionPrinting.get();

        assertNotNull(actualConfig, "A new TaxBatchConfig 1042-S reference should have been created and processed");
        assertNotNull(actualPrintingConfig,
                "A new TaxBatchConfig transaction-printing reference should have been created and processed");
        assertTaxBatchConfigWasPreparedProperly(expectedConfig, actualConfig);
        assertTaxBatchConfigWasPreparedProperly(expectedPrintingConfig, actualPrintingConfig);
    }

    private void assertTaxConfigHoldersAreInDefaultState() {
        assertNull(taxConfigHolderFor1042S.get(), "The TaxBatchConfig 1042-S reference should have been null");
        assertNull(taxConfigHolderForTransactionPrinting.get(),
                "The TaxBatchConfig transaction-printing reference should have been null");
    }

    private void update1042SDatesToProcessParameter(final String value) {
        final Parameter parameter = generate1042SDatesToProcessParameter(value);
        mockParameterService.updateParameter(parameter);
        final String retrievedValue = mockParameterService.getParameterValueAsString(
                parameter.getNamespaceCode(), parameter.getComponentCode(), parameter.getName());
        assertEquals(value, retrievedValue, "The 1042-S dates-to-process parameter did not get overridden properly");
    }

    private void assertTaxBatchConfigWasPreparedProperly(final TaxBatchConfig expectedConfig,
            final TaxBatchConfig actualConfig) {
        assertEquals(expectedConfig.getMode(), actualConfig.getMode(), "Wrong tax processing mode");
        assertEquals(expectedConfig.getTaxType(), actualConfig.getTaxType(), "Wrong tax type");
        assertEquals(expectedConfig.getReportYear(), actualConfig.getReportYear(), "Wrong report year");
        assertEquals(expectedConfig.getProcessingStartDate(), actualConfig.getProcessingStartDate(),
                "Wrong processing start date");
        assertEquals(expectedConfig.getStartDate(), actualConfig.getStartDate(), "Wrong start date");
        assertEquals(expectedConfig.getEndDate(), actualConfig.getEndDate(), "Wrong end date");
    }



    @ParameterizedTest(name = "[{index}] ''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {
            KFSConstants.BLANK_SPACE,
            "Twenty Twenty-Five",
            "Year-To-Date",
            "2024.5",
            "02/29/2023;03/15/2023",
            "01/01/2024;02/02/2024;03/03/2024",
            "11/01/2024;01/31/2025",
            "-150",
            "1865",
            "3",
            "98765"
    })
    void testInvalidDatesToProcessSettingsFor1042S(final String datesToProcessSetting) throws Exception {
        final LocalDateTime testStartDate = TestDateUtils.toLocalDateTime("2025-01-12T10:55:34");
        assertTaxConfigHoldersAreInDefaultState();
        update1042SDatesToProcessParameter(datesToProcessSetting);

        assertThrows(RuntimeException.class, () -> taxProcessingV2Service.performTaxProcessingFor1042S(testStartDate),
                "The processing should have failed as the result of an invalid dates-to-process parameter value");
        assertTaxConfigHoldersAreInDefaultState();
    }

}
