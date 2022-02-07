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
    public static final String AUXILIARY_VOUCHER_COMPONENT = "AuxiliaryVoucher";
    public static final String DOCUMENT_VALIDATION_PARAMETER_TYPE = "VALID";
    public static final String AV_VALIDATION_MESSAGE_KEY_PREFIX = "error.create.accounting.document.av.";

    public static final String UOM_EACH = "EA";
    public static final String UOM_DOZEN = "DZ";

    public static final String TEST_VALIDATION_GROUP_LEVEL_TYPE_INVALID_ERROR_MESSAGE = "Invalid Group Level Type, expected Cost Center Group Level.";
    public static final String TEST_VALIDATION_CHART_NOT_FOUND_ERROR_MESSAGE = "Could not find Chart %s.";
    public static final String TEST_VALIDATION_CHART_INACTIVE_ERROR_MESSAGE = "Invalid Chart %s is not active.";
    public static final String TEST_VALIDATION_ACCOUNT_NUMBER_BLANK_ERROR_MESSAGE = "Account Number cannot be blank.";
    public static final String TEST_VALIDATION_ACCOUNT_NOT_FOUND_ERROR_MESSAGE = "Could not find Account (%s, %s).";
    public static final String TEST_VALIDATION_ACCOUNT_CLOSED_ERROR_MESSAGE = "Invalid Account (%s, %s) is closed.";
    public static final String TEST_VALIDATION_ACCOUNT_EXPIRED_ERROR_MESSAGE = "Invalid Account (%s, %s) is expired.";
    public static final String TEST_VALIDATION_OBJECT_CODE_NOT_FOUND_ERROR_MESSAGE = "Could not find Object Code (%s, %s).";
    public static final String TEST_VALIDATION_OBJECT_CODE_INACTIVE_ERROR_MESSAGE = "Invalid Object Code (%s, %s) is not active.";
    public static final String TEST_VALIDATION_SUB_ACCOUNT_NOT_FOUND_ERROR_MESSAGE = "Could not find Sub-Account (%s, %s, %s).";
    public static final String TEST_VALIDATION_SUB_ACCOUNT_INACTIVE_ERROR_MESSAGE = "Invalid Sub-Account (%s, %s, %s) is not active.";
    public static final String TEST_VALIDATION_SUB_OBJECT_NOT_FOUND_ERROR_MESSAGE = "Could not find Sub-Object (%s, %s, %s, %s).";
    public static final String TEST_VALIDATION_SUB_OBJECT_INACTIVE_ERROR_MESSAGE = "Invalid Sub-Object (%s, %s, %s, %s) is not active.";
    public static final String TEST_VALIDATION_PROJECT_NOT_FOUND_ERROR_MESSAGE = "Could not find Project Code %s.";
    public static final String TEST_VALIDATION_PROJECT_INACTIVE_ERROR_MESSAGE = "Invalid Project Code %s is not active.";
    public static final String TEST_VALIDATION_OBJ_REF_ID_TOO_LONG_ERROR_MESSAGE = "Organization Reference ID %s cannot be more than 8 characters in length.";

    public static final String TEST_AWS_BILLING_DEFAULT_CHART_CODE = "IT";
    public static final String TEST_AWS_BILLING_CHART_CODE_CS = "CS";
    public static final String TEST_AWS_DEFAULT_OBJ_CODE = "6600";
    public static final String TEST_ACCOUNT_NUMBER_1658328 = "1658328";
    public static final String TEST_ACCOUNT_NUMBER_R583805 = "R583805";
    public static final String TEST_INVALID_STAR_ACCOUNT_STRING = "IT*1023715*97601*4020*109**AEH56*foo";
    public static final String TEST_ACCOUNT_NUMBER_R589966 = "R589966";
    public static final String TEST_ACCOUNT_NUMBER_J80100X = "J80100X";
    public static final String TEST_ACCOUNT_NUMBER_165833X = "165833X";
    public static final String TEST_ACCOUNT_NUMBER_1023715 = "1023715";
    public static final String TEST_ACCOUNT_NUMBER_J801000 = "J801000";
    public static final String TEST_SUB_ACCOUNT_NUMBER_70170 = "70170";
    public static final String TEST_SUB_ACCOUNT_NUMBER_NONCA = "NONCA";
    public static final String TEST_SUB_ACCOUNT_NUMBER_NONCX = "NONCX";
    public static final String TEST_SUB_ACCOUNT_NUMBER_533X = "533X";
    public static final String TEST_SUB_ACCOUNT_NUMBER_97601 = "97601";
    public static final String TEST_SUB_ACCOUNT_NUMBER_SHAN = "SHAN";
    public static final String TEST_OBJ_CODE_1000 = "1000";
    public static final String TEST_OBJ_CODE_4020 = "4020";
    public static final String TEST_SUB_OBJ_CODE_109 = "109";
    public static final String TEST_SUB_OBJ_CODE_10X = "10X";
    public static final String TEST_PROJECT_CODE_EB_PLGIFX = "EB-PLGIFX";
    public static final String TEST_PROJECT_CODE_EB_PLGIFT = "EB-PLGIFT";
    
    public static final String TEST_CREATE_ACCOUNT_DOCUMENT_INVALID_DATA = "Detected invalid data value {0} for element: {1}";
    public static final String TEST_CREATE_ACCOUNT_DOCUMENT_NULL_BLANK_DATA = "Detected null or blank data for element: {0}";
    
    public static final String TEST_CREATE_ACCOUNTING_DOCUMENT_PAYEE_MISMATCH = "The payee name of the vendor is {0} and the payee name entered in the xml is {1}.  We will use the payee name from the vendor.  Please use the correct payee name.";
}
