package edu.cornell.kfs.fp;

public class CuFPTestConstants {
    public static final String BUSINESS_RULE_VALIDATION_DESCRIPTION_INDICATOR = "FAIL BUSINESS RULES";
    public static final String TEST_VALIDATION_ERROR_KEY = "test.validation.error";
    public static final String TEST_VALIDATION_ERROR_MESSAGE = "The document has invalid data.";
    public static final String TEST_ATTACHMENT_DOWNLOAD_FAILURE_MESSAGE = "Failed to download attachment {0}";
    public static final String GENERIC_ERROR_MESSAGE = "Unexpected XML processing error";
    public static final String GENERIC_NUMERIC_ERROR_MESSAGE = "Invalid number";
    public static final String XML_ADAPTER_ERROR_MESSAGE = "Error at line {0}: {1}";
    public static final String EXCEPTION_MESSAGE_REGEX
            = "^(?<exceptionClassname>([\\w$]+\\.)+[\\w$]+(Exception|Error|Throwable))((: ?)(?<detailMessage>.+))?$";
    public static final String TEST_CREDENTIAL_GROUP_CODE = "TESTGRP";
    public static final String AWS_CREDENTIAL_GROUP_CODE = "AWS-Bill";
    public static final String BUDGET_ADJUSTMENT_DOC_TYPE = "BA";
    public static final String YEAR_END_BUDGET_ADJUSTMENT_DOC_TYPE = "YEBA";
    public static final String YEAR_END_TRANSFER_OF_FUNDS_DOC_TYPE = "YETF";
    public static final String DISBURSEMENT_VOUCHER_DOC_TYPE = "DV";
    public static final String AUXILIARY_VOUCHER_DOC_TYPE = "AV";
    public static final int FY_2016 = 2016;
    public static final int FY_2018 = 2018;
    public static final String DATE_02_21_2019 = "02/21/2019";
    public static final String AV_VALIDATION_MESSAGE_KEY_PREFIX = "error.create.accounting.document.av.";
    public static final String ACCOUNT_NUMBER_LABEL = "Account Number";

    public static final String UOM_EACH = "EA";
    public static final String UOM_DOZEN = "DZ";

    public static final String TEST_CREATE_ACCOUNT_DOCUMENT_INVALID_DATA = "Detected invalid data value {0} for element: {1}";
    public static final String TEST_CREATE_ACCOUNT_DOCUMENT_NULL_BLANK_DATA = "Detected null or blank data for element: {0}";
    public static final String TEST_CREATE_ACCOUNT_DOCUMENT_PAYEE_MISMATCH = "The payee name of the vendor is {0} and the payee name entered in the xml is {1}.  We will use the payee name from the vendor.  Please use the correct payee name.";

    public static final class TestEmails {
        public static final String KFS_GL_FP_AT_CORNELL_DOT_EDU = "kfs-gl_fp@cornell.edu";
        public static final String MOCK_TEST_DEVS_AT_CORNELL_DOT_EDU = "mock-test-devs@cornell.edu";
        public static final String MOCK_TEST_FUNC_LEADS_AT_CORNELL_DOT_EDU = "mock-test-func-leads@cornell.edu";
    }
}
