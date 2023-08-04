package com.rsmart.kuali.kfs.cr.batch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.CrTestConstants;
import com.rsmart.kuali.kfs.cr.CrTestConstants.TestParamValues;
import com.rsmart.kuali.kfs.cr.batch.fixture.CheckReconciliationFixture;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;

public class CheckReconciliationImportStepTest {

    private static final String TEST_CR_BATCH_DIRECTORY = "test/cr/";

    private CheckReconciliationImportStep crImportBatchStep;
    private Map<String, String> parameters;
    private Map<CheckReconciliationFixture, CheckReconciliation> storedCheckReconRows;

    @BeforeEach
    void setUp() throws Exception {
        createTestBatchDirectory();
        parameters = new HashMap<>();
    }

    @AfterEach
    void tearDown() throws Exception {
        deleteTestBatchDirectory();
    }

    private void createTestBatchDirectory() throws IOException {
        File testBatchDirectory = new File(TEST_CR_BATCH_DIRECTORY);
        FileUtils.forceMkdir(testBatchDirectory);
    }

    private void deleteTestBatchDirectory() throws IOException {
        File testBatchDirectory = new File(TEST_CR_BATCH_DIRECTORY);
        if (testBatchDirectory.exists() && testBatchDirectory.isDirectory()) {
            FileUtils.forceDelete(testBatchDirectory.getAbsoluteFile());
        }
    }

    private Map<String, String> initializeParameters() {
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

        parms.put(createJpmcParmName(CRConstants.ACCOUNT_NUM), TestParamValues.ACCOUNT_111_2345);
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

}
