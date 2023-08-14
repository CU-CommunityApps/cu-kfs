package com.rsmart.kuali.kfs.cr.batch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
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

import edu.cornell.kfs.sys.CUKFSConstants;

public class CheckReconciliationImportStepTest {

    private static final String TEST_CR_BATCH_DIRECTORY = "test/cr_import/";
    private static final String TEST_BASE_REPORTS_DIRECTORY = TEST_CR_BATCH_DIRECTORY + CUKFSConstants.REPORTS_DIR;
    private static final String TEST_BASE_STAGING_DIRECTORY = TEST_CR_BATCH_DIRECTORY + CUKFSConstants.STAGING_DIR;
    private static final String TEST_CR_REPORTS_DIRECTORY = TEST_BASE_REPORTS_DIRECTORY + "/cr/";
    private static final String TEST_CR_ARCHIVE_DIRECTORY = TEST_BASE_STAGING_DIRECTORY + "/cr/archive/";
    private static final String TEST_CR_UPLOAD_DIRECTORY = TEST_BASE_STAGING_DIRECTORY + "/cr/upload/";

    private CheckReconciliationImportStep crImportStep;
    private Map<String, String> parameters;
    private Map<String, CheckReconciliation> currentCheckReconRows;
    private Map<String, CheckReconciliation> updatedCheckReconRows;
    private List<Bank> banks;

    @BeforeEach
    void setUp() throws Exception {
        createTestBatchDirectories();
        parameters = createInitialTestParameterMappings();
        currentCheckReconRows = createInitialCheckReconciliationMappings();
        updatedCheckReconRows = new HashMap<>();
        banks = createBanks();
        
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

    private void deleteTestBatchDirectories() throws IOException {
        File testBatchDirectory = new File(TEST_CR_BATCH_DIRECTORY);
        if (testBatchDirectory.exists() && testBatchDirectory.isDirectory()) {
            FileUtils.forceDelete(testBatchDirectory.getAbsoluteFile());
        }
    }

    private Map<String, String> createInitialTestParameterMappings() {
        Map<String, String> parms = new HashMap<>();
        parms.put(CRConstants.PARAMETER_PREFIX, KFSConstants.EMPTY_STRING);

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
        return fixtures.map(CheckReconciliationFixture::toCheckReconciliationBeforeUpdate)
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
        Map<String, String> criteria = invocation.getArgument(1);
        String checkReconKey = createCheckReconMapKey(criteria.get(KFSPropertyConstants.BANK_ACCOUNT_NUMBER),
                criteria.get(KFSPropertyConstants.CHECK_NUMBER));
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

    private void assertCheckReconciliationImportRunsProperly(
            List<CheckReconciliationFixture> expectedCheckReconUpdates) throws Exception {
        Date currentDate = new Date();
        crImportStep.execute(CrTestConstants.CR_IMPORT_JOB_NAME, currentDate);
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
        }
    }

}
