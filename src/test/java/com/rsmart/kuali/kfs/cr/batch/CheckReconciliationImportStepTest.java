package com.rsmart.kuali.kfs.cr.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.pdp.service.PaymentDetailService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.Bank;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.CrTestConstants;
import com.rsmart.kuali.kfs.cr.CrTestConstants.TestParamValues;
import com.rsmart.kuali.kfs.cr.batch.fixture.CheckReconciliationFixture;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;
import com.rsmart.kuali.kfs.cr.document.service.GlTransactionService;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;

@Execution(ExecutionMode.SAME_THREAD)
public class CheckReconciliationImportStepTest {

    private static final String TEST_CR_RESOURCES_DIRECTORY =
            "classpath:com/rsmart/kuali/kfs/cr/batch/service/fixture/";
    private static final String TEST_CR_BATCH_DIRECTORY = "test/cr_import/";
    private static final String TEST_BASE_REPORTS_DIRECTORY = TEST_CR_BATCH_DIRECTORY + CUKFSConstants.REPORTS_DIR;
    private static final String TEST_BASE_STAGING_DIRECTORY = TEST_CR_BATCH_DIRECTORY + CUKFSConstants.STAGING_DIR;
    private static final String TEST_CR_REPORTS_DIRECTORY = TEST_BASE_REPORTS_DIRECTORY + "/cr/";
    private static final String TEST_CR_ARCHIVE_DIRECTORY = TEST_BASE_STAGING_DIRECTORY + "/cr/archive/";
    private static final String TEST_CR_UPLOAD_DIRECTORY = TEST_BASE_STAGING_DIRECTORY + "/cr/upload/";

    private static final String MELLON_SUCCESS_FILE = "mellon_check_success.txt";
    private static final String MELLON_BAD_FILE = "mellon_check_bad.txt";
    private static final String JPMC_SUCCESS_FILE = "jpmc_check_success.txt";
    private static final String JPMC_BAD_FILE = "jpmc_check_bad.txt";

    private static final String MELLON_FILE_PREFIX = "mellon_";
    private static final String JPMC_FILE_PREFIX = "jpmc_";

    private enum CrImportResult {
        SUCCESS,
        FAILURE,
        EXCEPTION
    }

    private CheckReconciliationImportStep crImportStep;
    private Map<String, String> parameters;
    private Map<String, CheckReconciliation> currentCheckReconRows;
    private Map<String, CheckReconciliation> updatedCheckReconRows;
    private List<Bank> banks;
    private List<String> prefixMappings;

    @BeforeEach
    void setUp() throws Exception {
        createTestBatchDirectories();
        parameters = createInitialTestParameterMappings();
        currentCheckReconRows = createInitialCheckReconciliationMappings();
        updatedCheckReconRows = new HashMap<>();
        banks = createBanks();
        prefixMappings = new ArrayList<>();
        setPrefixMappings(Map.entry(MELLON_FILE_PREFIX, KFSConstants.EMPTY_STRING),
                Map.entry(JPMC_FILE_PREFIX, CrTestConstants.JPMC_PARAM_PREFIX));
        
        crImportStep = new CheckReconciliationImportStep();
        crImportStep.setParameterService(createMockParameterService());
        crImportStep.setKualiConfigurationService(createMockConfigurationService());
        crImportStep.setBusinessObjectService(createMockBusinessObjectService());
        crImportStep.setPaymentDetailService(Mockito.mock(PaymentDetailService.class));
        crImportStep.setGlTransactionService(createMockGlTransactionService());
    }

    @AfterEach
    void tearDown() throws Exception {
        crImportStep = null;
        prefixMappings = null;
        banks = null;
        updatedCheckReconRows = null;
        currentCheckReconRows = null;
        parameters = null;
        deleteTestBatchDirectories();
    }

    private void createTestBatchDirectories() throws IOException {
        String[] directoryPaths = {
            TEST_CR_BATCH_DIRECTORY,
            TEST_CR_REPORTS_DIRECTORY,
            TEST_CR_ARCHIVE_DIRECTORY,
            TEST_CR_UPLOAD_DIRECTORY
        };
        for (String directoryPath : directoryPaths) {
            File testBatchDirectory = new File(directoryPath);
            FileUtils.forceMkdir(testBatchDirectory);
        }
    }

    private void copyFilesToCrUploadDirectory(List<String> fileNames) throws IOException {
        for (String fileName : fileNames) {
            String sourceFilePath = TEST_CR_RESOURCES_DIRECTORY + fileName;
            File targetFilePath = new File(TEST_CR_UPLOAD_DIRECTORY + fileName);
            try (InputStream fileStream = CuCoreUtilities.getResourceAsStream(sourceFilePath)) {
                FileUtils.copyToFile(fileStream, targetFilePath);
            }
        }
    }

    private void deleteTestBatchDirectories() throws IOException {
        File testBatchDirectory = new File(TEST_CR_BATCH_DIRECTORY);
        if (testBatchDirectory.exists() && testBatchDirectory.isDirectory()) {
            FileUtils.forceDelete(testBatchDirectory.getAbsoluteFile());
        }
    }

    private Map<String, String> createInitialTestParameterMappings() {
        Map<String, String> parms = new HashMap<>();

        parms.put(CRConstants.ACCOUNT_NUM, TestParamValues.ACCOUNT_111_2345);
        parms.put(CRConstants.ACCOUNT_NUM_COL, TestParamValues.UNUSED_COL_0);
        parms.put(CRConstants.ACCOUNT_NUM_HEADER_IND, KRADConstants.NO_INDICATOR_VALUE);
        parms.put(CRConstants.AMOUNT_DECIMAL_IND, KRADConstants.YES_INDICATOR_VALUE);
        parms.put(CRConstants.AMOUNT_COL, TestParamValues.MELLON_CHK_AMT_COL);
        parms.put(CRConstants.CHECK_DATE_COL, TestParamValues.MELLON_CHK_DT_COL);
        parms.put(CRConstants.CHECK_DATE_FORMAT, TestParamValues.MELLON_DATE_FORMAT);
        parms.put(CRConstants.CHECK_NUM_COL, TestParamValues.MELLON_CHK_NB_COL);
        parms.put(CRConstants.CLEARING_ACCOUNT, TestParamValues.CLEARING_ACCOUNT_G987654);
        parms.put(CRConstants.CLEARING_COA, TestParamValues.CLEARING_CHART_IT);
        parms.put(CRConstants.CLEARING_OBJECT_CODE, TestParamValues.CLEARING_OBJECT_5555);
        parms.put(CRConstants.CHECK_FILE_COLUMNS, TestParamValues.MELLON_COL_LENGTHS);
        parms.put(CRConstants.CHECK_FILE_DELIMETER, KFSConstants.COMMA);
        parms.put(CRConstants.CHECK_FILE_FOOTER_COLUMNS, TestParamValues.NOT_APPLICABLE);
        parms.put(CRConstants.CHECK_FILE_FOOTER, KRADConstants.NO_INDICATOR_VALUE);
        parms.put(CRConstants.CHECK_FILE_HEADER_COLUMNS, TestParamValues.MELLON_HEADER_COL_LENGTHS);
        parms.put(CRConstants.CHECK_FILE_HEADER, KRADConstants.YES_INDICATOR_VALUE);
        parms.put(CRConstants.CHECK_FILE_TYPE, CRConstants.FIXED);
        parms.put(CRConstants.ISSUE_DATE_COL, TestParamValues.MELLON_ISSD_DT_COL);
        parms.put(CRConstants.BNK_CD_NOT_FOUND, CrTestConstants.MELLON_BANK_CODE);
        parms.put(CRConstants.PAYEE_ID_COL, TestParamValues.MELLON_PAYEE_ID_COL);
        parms.put(CRConstants.PAYEE_NAME_COL, TestParamValues.MELLON_PAYEE_NAME_COL);
        parms.put(CRConstants.SRC_NOT_FOUND, TestParamValues.SOURCE_FOR_NOT_FOUND_B);
        parms.put(CRConstants.CLRD_STATUS, TestParamValues.MELLON_CLRD_CODES);
        parms.put(CRConstants.CNCL_STATUS, TestParamValues.MELLON_CNCL_CODES);
        parms.put(CRConstants.STATUS_COL, TestParamValues.MELLON_STATUS_COL);
        parms.put(CRConstants.ISSD_STATUS, TestParamValues.MELLON_ISSD_CODES);
        parms.put(CRConstants.STAL_STATUS, TestParamValues.NOT_APPLICABLE);
        parms.put(CRConstants.STOP_STATUS, TestParamValues.MELLON_STOP_CODES);
        parms.put(CRConstants.VOID_STATUS, TestParamValues.MELLON_VOID_CODES);

        parms.put(createJpmcParmName(CRConstants.ACCOUNT_NUM), TestParamValues.ACCOUNT_888_7777);
        parms.put(createJpmcParmName(CRConstants.ACCOUNT_NUM_COL), TestParamValues.UNUSED_COL_0);
        parms.put(createJpmcParmName(CRConstants.ACCOUNT_NUM_HEADER_IND), KRADConstants.NO_INDICATOR_VALUE);
        parms.put(createJpmcParmName(CRConstants.AMOUNT_DECIMAL_IND), KRADConstants.YES_INDICATOR_VALUE);
        parms.put(createJpmcParmName(CRConstants.AMOUNT_COL), TestParamValues.JPMC_CHK_AMT_COL);
        parms.put(createJpmcParmName(CRConstants.CHECK_DATE_COL), TestParamValues.JPMC_CHK_DT_COL);
        parms.put(createJpmcParmName(CRConstants.CHECK_DATE_FORMAT), TestParamValues.JPMC_DATE_FORMAT);
        parms.put(createJpmcParmName(CRConstants.CHECK_NUM_COL), TestParamValues.JPMC_CHK_NB_COL);
        parms.put(createJpmcParmName(CRConstants.CLEARING_ACCOUNT), TestParamValues.CLEARING_ACCOUNT_G987654);
        parms.put(createJpmcParmName(CRConstants.CLEARING_COA), TestParamValues.CLEARING_CHART_IT);
        parms.put(createJpmcParmName(CRConstants.CLEARING_OBJECT_CODE), TestParamValues.CLEARING_OBJECT_5555);
        parms.put(createJpmcParmName(CRConstants.CHECK_FILE_COLUMNS), TestParamValues.JPMC_COL_LENGTHS);
        parms.put(createJpmcParmName(CRConstants.CHECK_FILE_DELIMETER), KFSConstants.COMMA);
        parms.put(createJpmcParmName(CRConstants.CHECK_FILE_FOOTER_COLUMNS), TestParamValues.NOT_APPLICABLE);
        parms.put(createJpmcParmName(CRConstants.CHECK_FILE_FOOTER), KRADConstants.NO_INDICATOR_VALUE);
        parms.put(createJpmcParmName(CRConstants.CHECK_FILE_HEADER_COLUMNS), TestParamValues.NOT_APPLICABLE);
        parms.put(createJpmcParmName(CRConstants.CHECK_FILE_HEADER), KRADConstants.NO_INDICATOR_VALUE);
        parms.put(createJpmcParmName(CRConstants.CHECK_FILE_TYPE), CRConstants.FIXED);
        parms.put(createJpmcParmName(CRConstants.ISSUE_DATE_COL), TestParamValues.JPMC_ISSD_DT_COL);
        parms.put(createJpmcParmName(CRConstants.BNK_CD_NOT_FOUND), CrTestConstants.JPMC_BANK_CODE);
        parms.put(createJpmcParmName(CRConstants.PAYEE_ID_COL), TestParamValues.JPMC_PAYEE_ID_COL);
        parms.put(createJpmcParmName(CRConstants.PAYEE_NAME_COL), TestParamValues.JPMC_PAYEE_NAME_COL);
        parms.put(createJpmcParmName(CRConstants.SRC_NOT_FOUND), TestParamValues.SOURCE_FOR_NOT_FOUND_B);
        parms.put(createJpmcParmName(CRConstants.CLRD_STATUS), TestParamValues.JPMC_CLRD_CODES);
        parms.put(createJpmcParmName(CRConstants.CNCL_STATUS), TestParamValues.JPMC_CNCL_CODES);
        parms.put(createJpmcParmName(CRConstants.STATUS_COL), TestParamValues.JPMC_STATUS_COL);
        parms.put(createJpmcParmName(CRConstants.ISSD_STATUS), TestParamValues.JPMC_ISSD_CODES);
        parms.put(createJpmcParmName(CRConstants.STAL_STATUS), TestParamValues.NOT_APPLICABLE);
        parms.put(createJpmcParmName(CRConstants.STOP_STATUS), TestParamValues.JPMC_STOP_CODES);
        parms.put(createJpmcParmName(CRConstants.VOID_STATUS), TestParamValues.JPMC_VOID_CODES);

        return parms;
    }

    @SafeVarargs
    private void setPrefixMappings(Map.Entry<String, String>... mappings) {
        setPrefixMappings(List.of(mappings));
    }

    private void setPrefixMappings(List<Map.Entry<String, String>> mappings) {
        prefixMappings.clear();
        for (Map.Entry<String, String> mapping : mappings) {
            String mappingString = mapping.getKey() + CUKFSConstants.EQUALS_SIGN + mapping.getValue();
            prefixMappings.add(mappingString);
        }
    }

    private String createJpmcParmName(String baseParameterName) {
        return CrTestConstants.JPMC_PARAM_PREFIX + baseParameterName;
    }

    private ParameterService createMockParameterService() {
        ParameterService parameterService = Mockito.mock(ParameterService.class);

        Mockito.when(parameterService.getParameterValueAsString(
                        Mockito.eq(CheckReconciliationImportStep.class), Mockito.anyString()))
                .then(invocation -> parameters.get(invocation.getArgument(1)));

        Mockito.when(parameterService.getParameterValueAsBoolean(
                        Mockito.eq(CheckReconciliationImportStep.class), Mockito.anyString()))
                .then(invocation -> StringUtils.equalsIgnoreCase(
                        parameters.get(invocation.getArgument(1)), KRADConstants.YES_INDICATOR_VALUE));

        Mockito.when(parameterService.getParameterValuesAsString(
                        CheckReconciliationImportStep.class, CRConstants.PARAMETER_PREFIX_MAPPINGS))
                .then(invocation -> List.copyOf(prefixMappings));

        return parameterService;
    }

    private ConfigurationService createMockConfigurationService() {
        Map<String, String> mockProperties = Map.ofEntries(
                Map.entry(KFSConstants.REPORTS_DIRECTORY_KEY, TEST_BASE_REPORTS_DIRECTORY),
                Map.entry(com.rsmart.kuali.kfs.sys.KFSConstants.STAGING_DIRECTORY_KEY, TEST_BASE_STAGING_DIRECTORY)
        );

        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        for (Map.Entry<String, String> mockProperty : mockProperties.entrySet()) {
            Mockito.when(configurationService.getPropertyValueAsString(mockProperty.getKey()))
                    .thenReturn(mockProperty.getValue());
        }
        return configurationService;
    }

    private Map<String, CheckReconciliation> createInitialCheckReconciliationMappings() {
        Stream<CheckReconciliationFixture> fixtures = Stream.of(
                CheckReconciliationFixture.CHECK_50012233_DISB_ISSD_5607,
                CheckReconciliationFixture.CHECK_50012234_DISB_ISSD_22222,
                CheckReconciliationFixture.CHECK_50012237_DISB_VOID_12500,
                CheckReconciliationFixture.CHECK_15566_NDWR_ISSD_76500,
                CheckReconciliationFixture.CHECK_15567_NDWR_ISSD_1995,
                CheckReconciliationFixture.CHECK_15568_NDWR_ISSD_2202455,
                CheckReconciliationFixture.CHECK_15569_NDWR_VOID_18875
        );
        return fixtures.map(CheckReconciliationFixture::toCheckReconciliationUsingCurrentStatus)
                .collect(Collectors.toMap(
                        this::createCheckReconMapKey,
                        Function.identity(),
                        (checkRecon1, checkRecon2) -> checkRecon2,
                        HashMap::new));
    }

    private String createCheckReconMapKey(CheckReconciliation checkReconciliation) {
        return createCheckReconMapKey(checkReconciliation.getBankAccountNumber(),
                checkReconciliation.getCheckNumber());
    }

    private String createCheckReconMapKey(String bankAccountNumber, KualiInteger checkNumber) {
        return createCheckReconMapKey(bankAccountNumber, checkNumber.toString());
    }

    private String createCheckReconMapKey(String bankAccountNumber, String checkNumber) {
        return bankAccountNumber + CUKFSConstants.COLON + checkNumber;
    }

    private List<Bank> createBanks() {
        return List.of(
                createBank(CrTestConstants.MELLON_BANK_CODE, TestParamValues.ACCOUNT_111_2345),
                createBank(CrTestConstants.JPMC_BANK_CODE, TestParamValues.ACCOUNT_888_7777));
    }

    private Bank createBank(String bankCode, String bankAccountNumber) {
        Bank bank = new Bank();
        bank.setBankCode(bankCode);
        bank.setBankAccountNumber(bankAccountNumber);
        return bank;
    }

    private BusinessObjectService createMockBusinessObjectService() {
        BusinessObjectService businessObjectService = Mockito.mock(BusinessObjectService.class);
        
        Mockito.when(businessObjectService.findAll(Bank.class))
                .thenReturn(banks);
        
        Mockito.when(businessObjectService.findMatching(
                        Mockito.eq(CheckReconciliation.class), Mockito.anyMap()))
                .then(this::findMatchingCheckRecon);
        
        Mockito.when(businessObjectService.save(Mockito.any(CheckReconciliation.class)))
                .then(this::saveCheckRecon);
        
        return businessObjectService;
    }

    private List<CheckReconciliation> findMatchingCheckRecon(InvocationOnMock invocation) {
        Map<String, Object> criteria = invocation.getArgument(1);
        String checkReconKey = createCheckReconMapKey(
                (String) criteria.get(KFSPropertyConstants.BANK_ACCOUNT_NUMBER),
                (KualiInteger) criteria.get(KFSPropertyConstants.CHECK_NUMBER));
        CheckReconciliation checkRecon = currentCheckReconRows.get(checkReconKey);
        return (checkRecon != null) ? List.of(checkRecon) : List.of();
    }

    private CheckReconciliation saveCheckRecon(InvocationOnMock invocation) {
        CheckReconciliation checkRecon = invocation.getArgument(0);
        String checkReconKey = createCheckReconMapKey(checkRecon.getBankAccountNumber(), checkRecon.getCheckNumber());
        currentCheckReconRows.put(checkReconKey, checkRecon);
        updatedCheckReconRows.put(checkReconKey, checkRecon);
        return checkRecon;
    }

    private GlTransactionService createMockGlTransactionService() {
        GlTransactionService glTransactionService = Mockito.mock(GlTransactionService.class);
        
        Mockito.when(glTransactionService.getNewCheckReconciliations(Mockito.any()))
                .thenReturn(List.of());
        
        Mockito.when(glTransactionService.getCanceledChecks())
                .thenReturn(List.of());
        
        Mockito.when(glTransactionService.getAllPaymentGroupForSearchCriteria(Mockito.any(), Mockito.any()))
                .thenReturn(List.of());
        
        return glTransactionService;
    }

    static Stream<Arguments> successfulCheckReconImports() {
        return Stream.of(
                Arguments.of(files(MELLON_SUCCESS_FILE), List.of(
                        CheckReconciliationFixture.CHECK_50012233_DISB_ISSD_5607,
                        CheckReconciliationFixture.CHECK_50012234_DISB_ISSD_22222,
                        CheckReconciliationFixture.CHECK_55555555_DISB_ISSD_22222
                )),
                Arguments.of(files(JPMC_SUCCESS_FILE), List.of(
                        CheckReconciliationFixture.CHECK_15566_NDWR_ISSD_76500,
                        CheckReconciliationFixture.CHECK_15567_NDWR_ISSD_1995,
                        CheckReconciliationFixture.CHECK_15568_NDWR_ISSD_2202455,
                        CheckReconciliationFixture.CHECK_88888_NDWR_ISSD_1995
                )),
                Arguments.of(files(MELLON_SUCCESS_FILE, JPMC_SUCCESS_FILE), List.of(
                        CheckReconciliationFixture.CHECK_50012233_DISB_ISSD_5607,
                        CheckReconciliationFixture.CHECK_50012234_DISB_ISSD_22222,
                        CheckReconciliationFixture.CHECK_55555555_DISB_ISSD_22222,
                        CheckReconciliationFixture.CHECK_15566_NDWR_ISSD_76500,
                        CheckReconciliationFixture.CHECK_15567_NDWR_ISSD_1995,
                        CheckReconciliationFixture.CHECK_15568_NDWR_ISSD_2202455,
                        CheckReconciliationFixture.CHECK_88888_NDWR_ISSD_1995
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("successfulCheckReconImports")
    void testNormalExecutionOfCheckReconciliationImport(List<String> testFileNames,
            List<CheckReconciliationFixture> expectedCheckReconUpdates) throws Exception {
        copyFilesToCrUploadDirectory(testFileNames);
        assertCheckReconciliationImportHasExpectedResult(CrImportResult.SUCCESS, expectedCheckReconUpdates);
    }

    @Test
    void testExecutionOfCheckReconImportWithoutAnyInputFiles() throws Exception {
        assertCheckReconciliationImportHasExpectedResult(CrImportResult.SUCCESS, List.of());
    }

    static Stream<Arguments> invalidDataImports() {
        return Stream.of(
                Arguments.of(files(MELLON_BAD_FILE), List.of(
                        CheckReconciliationFixture.CHECK_50012233_DISB_ISSD_5607
                )),
                Arguments.of(files(JPMC_BAD_FILE), List.of()),
                Arguments.of(files(MELLON_BAD_FILE, JPMC_BAD_FILE), List.of(
                        CheckReconciliationFixture.CHECK_50012233_DISB_ISSD_5607
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDataImports")
    void testCheckReconciliationImportWithInvalidDataFiles(List<String> testFileNames,
            List<CheckReconciliationFixture> expectedCheckReconUpdates) throws Exception {
        copyFilesToCrUploadDirectory(testFileNames);
        assertCheckReconciliationImportHasExpectedResult(CrImportResult.FAILURE, expectedCheckReconUpdates);
    }

    static Stream<Arguments> checkReconImportsWithMismatchedPrefixes() {
        return Stream.of(
                files(MELLON_SUCCESS_FILE),
                files(JPMC_SUCCESS_FILE),
                files(MELLON_SUCCESS_FILE, JPMC_SUCCESS_FILE)
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("checkReconImportsWithMismatchedPrefixes")
    void testExecutionFailsWhenParamPrefixesAreMismatched(List<String> testFileNames) throws Exception {
        setPrefixMappings(Map.entry(MELLON_FILE_PREFIX, CrTestConstants.JPMC_PARAM_PREFIX),
                Map.entry(JPMC_FILE_PREFIX, KFSConstants.EMPTY_STRING));
        copyFilesToCrUploadDirectory(testFileNames);
        assertCheckReconciliationImportHasExpectedResult(CrImportResult.FAILURE, List.of());
    }

    static Stream<Arguments> checkReconImportsWithInvalidParameterPrefixes() {
        List<String> mellonFile = files(MELLON_SUCCESS_FILE);
        List<String> jpmcFile = files(JPMC_SUCCESS_FILE);
        List<String> mellonAndJpmcFiles = files(MELLON_SUCCESS_FILE, JPMC_SUCCESS_FILE);
        return Stream.of(
                Arguments.of("a", mellonFile),
                Arguments.of("a", jpmcFile),
                Arguments.of("a", mellonAndJpmcFiles),
                Arguments.of("Dummy", mellonFile),
                Arguments.of("Dummy", jpmcFile),
                Arguments.of("Dummy", mellonAndJpmcFiles),
                Arguments.of("BANK1_", mellonFile),
                Arguments.of("BANK1_", jpmcFile),
                Arguments.of("BANK1_", mellonAndJpmcFiles)
        );
    }

    @ParameterizedTest
    @MethodSource("checkReconImportsWithInvalidParameterPrefixes")
    void testExecutionFailsWhenParamPrefixIsInvalid(String paramPrefix, List<String> testFileNames) throws Exception {
        setPrefixMappings(Map.entry(MELLON_FILE_PREFIX, paramPrefix),
                Map.entry(JPMC_FILE_PREFIX, paramPrefix));
        copyFilesToCrUploadDirectory(testFileNames);
        assertCheckReconciliationImportHasExpectedResult(CrImportResult.EXCEPTION, List.of());
    }

    static Stream<Arguments> unmatchableFilePrefixes() {
        return Stream.of(
                List.of(),
                List.of(Map.entry("bny_", KFSConstants.EMPTY_STRING)),
                List.of(Map.entry("chase_", CrTestConstants.JPMC_PARAM_PREFIX)),
                List.of(Map.entry("bny_", KFSConstants.EMPTY_STRING),
                        Map.entry("chase_", CrTestConstants.JPMC_PARAM_PREFIX))
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("unmatchableFilePrefixes")
    void testFilesAreIgnoredWhenNoFilePrefixMatchExists(List<Map.Entry<String, String>> mappings) throws Exception {
        setPrefixMappings(mappings);
        copyFilesToCrUploadDirectory(List.of(MELLON_SUCCESS_FILE, JPMC_SUCCESS_FILE));
        assertCheckReconciliationImportHasExpectedResult(CrImportResult.FAILURE, List.of());
    }

    static Stream<Arguments> conflictingFilePrefixes() {
        return Stream.of(
                List.of(
                        Map.entry(MELLON_FILE_PREFIX, KFSConstants.EMPTY_STRING),
                        Map.entry(MELLON_FILE_PREFIX, CrTestConstants.JPMC_PARAM_PREFIX)
                ),
                List.of(
                        Map.entry(JPMC_FILE_PREFIX, KFSConstants.EMPTY_STRING),
                        Map.entry(JPMC_FILE_PREFIX, CrTestConstants.JPMC_PARAM_PREFIX)
                ),
                List.of(
                        Map.entry(MELLON_FILE_PREFIX + "bny_", KFSConstants.EMPTY_STRING),
                        Map.entry(MELLON_FILE_PREFIX, CrTestConstants.JPMC_PARAM_PREFIX)
                ),
                List.of(
                        Map.entry(JPMC_FILE_PREFIX, KFSConstants.EMPTY_STRING),
                        Map.entry(JPMC_FILE_PREFIX + "chase_", CrTestConstants.JPMC_PARAM_PREFIX)
                )
        ).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("conflictingFilePrefixes")
    void testCannotHaveDuplicateFilePrefixes(List<Map.Entry<String, String>> mappings) throws Exception {
        setPrefixMappings(mappings);
        copyFilesToCrUploadDirectory(List.of(MELLON_SUCCESS_FILE, JPMC_SUCCESS_FILE));
        assertCheckReconciliationImportHasExpectedResult(CrImportResult.EXCEPTION, List.of());
    }

    private void assertCheckReconciliationImportHasExpectedResult(CrImportResult expectedResult,
            List<CheckReconciliationFixture> expectedCheckReconUpdates) throws Exception {
        Date currentDate = new Date();
        switch (expectedResult) {
            case SUCCESS :
                assertTrue(crImportStep.execute(CrTestConstants.CR_IMPORT_JOB_NAME, currentDate),
                        "The Check Reconciliation Import Step should have succeeded");
                break;

            case FAILURE :
                assertFalse(crImportStep.execute(CrTestConstants.CR_IMPORT_JOB_NAME, currentDate),
                        "The Check Reconciliation Import Step should have failed due to a caught exception");
                break;

            case EXCEPTION :
                assertThrows(RuntimeException.class,
                        () -> crImportStep.execute(CrTestConstants.CR_IMPORT_JOB_NAME, currentDate),
                        "The Check Reconciliation Import Step should have failed due to an uncaught exception");
                break;

            default :
                throw new IllegalArgumentException("Invalid CR Import Result expectation: " + expectedResult);
        }
        assertCheckReconciliationImportHadExpectedUpdates(expectedCheckReconUpdates);
    }

    private void assertCheckReconciliationImportHadExpectedUpdates(
            List<CheckReconciliationFixture> expectedCheckReconUpdates) {
        assertEquals(expectedCheckReconUpdates.size(), updatedCheckReconRows.size(),
                "Wrong number of Check Reconciliations were updated");
        Set<String> encounteredCheckRecons = new HashSet<>();
        for (CheckReconciliationFixture checkReconFixture : expectedCheckReconUpdates) {
            String checkReconKey = createCheckReconMapKey(
                    checkReconFixture.getExpectedBankAccountNumber(), checkReconFixture.checkNumber);
            assertTrue(encounteredCheckRecons.add(checkReconKey),
                    "Erroneously expected the following Check Recon entry twice: " + checkReconKey);
            CheckReconciliation checkRecon = updatedCheckReconRows.get(checkReconKey);
            assertNotNull(checkRecon, "The following Check Recon should have been updated: " + checkReconKey);
            
            assertEquals(checkReconFixture.bankCode, checkRecon.getBankCode(), "Wrong bank code");
            assertEquals(checkReconFixture.getExpectedBankAccountNumber(), checkRecon.getBankAccountNumber(),
                    "Wrong bank account number");
            assertEquals(checkReconFixture.checkNumber, checkRecon.getCheckNumber(), "Wrong check number");
            assertEquals(checkReconFixture.checkTotalAmount, checkRecon.getAmount(), "Wrong check amount");
            assertEquals(checkReconFixture.getParsedCheckIssuedDate(), checkRecon.getCheckDate(), "Wrong issue date");
            assertEquals(checkReconFixture.newCheckStatus, checkRecon.getStatus(), "Wrong check status");
            assertEquals(checkReconFixture.getParsedStatusChangeDate(), checkRecon.getStatusChangeDate(),
                    "Wrong status change date");
            assertTrue(checkRecon.isActive());
        }
    }

    private static List<String> files(String... fileNames) {
        return List.of(fileNames);
    }

}
