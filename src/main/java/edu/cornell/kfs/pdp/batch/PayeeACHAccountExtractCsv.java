package edu.cornell.kfs.pdp.batch;

/**
 * Enum defining the column value types from Payee ACH Account Extract .csv files.
 * These are the literal header names expected to be in the .csv file,
 * including the trailing underscore in the "BALANCE_ACCT_" header name.
 */
public enum PayeeACHAccountExtractCsv {
    EMPL_ID,
    NET_ID,
    LAST_NAME,
    FIRST_NAME,
    PAYMENT_TYPE,
    BALANCE_ACCT_,
    COMPLETE_DT,
    BANK_NAME,
    ROUTING_NO,
    ACCOUNT_NO,
    ACCOUNT_TYPE;
}
