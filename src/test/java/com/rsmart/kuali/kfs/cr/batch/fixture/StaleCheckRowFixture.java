package com.rsmart.kuali.kfs.cr.batch.fixture;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.CrTestConstants;
import org.kuali.kfs.sys.KFSConstants;

public enum StaleCheckRowFixture {

    CHECK_999_JPCD_STAL_925_VALID("20180509", CrTestConstants.JPMC_BANK_CODE, CRConstants.STALE, "99999999", "92.5", true),
    CHECK_999_JPCD_BLANK_INVALID("20180509", CrTestConstants.JPMC_BANK_CODE, KFSConstants.EMPTY_STRING,
            KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, false),
    CHECK_123_JPCD_STAL_29252_VALID("20180809", CrTestConstants.JPMC_BANK_CODE, CRConstants.STALE, "12345678", "292.52", true),
    CHECK_123_BLANK_STAL_19223_INVALID("20180609", KFSConstants.EMPTY_STRING, CRConstants.STALE, "82345678", "192.23", false),
    CHECK_199_JPCD_STAL_212319_VALID("20180712", CrTestConstants.JPMC_BANK_CODE, CRConstants.STALE, "19999999", "2,123.19", true),
    CHECK_399_JPCD_VOID_951("20180712", CrTestConstants.JPMC_BANK_CODE, CRConstants.VOIDED, "39999999", "9.51", false),
    CHECK_111_JPCD_CLRD_123("20180712", CrTestConstants.JPMC_BANK_CODE, CRConstants.CLEARED, "11111111", "1.23", false),
    CHECK_211_JPCD_STOP_123("20180712", CrTestConstants.JPMC_BANK_CODE, CRConstants.STOP, "21111111", "1.23", false),
    CHECK_311_JPCD_STAL_123("20180712", CrTestConstants.JPMC_BANK_CODE, CRConstants.STALE, "31111111", "1.23", false),
    CHECK_411_JPCD_EXCP_123("20180712", CrTestConstants.JPMC_BANK_CODE, CRConstants.EXCP, "41111111", "1.23", false),
    CHECK_511_JPCD_CDIS_321("20180712", CrTestConstants.JPMC_BANK_CODE, CRConstants.CANCELLED, "51111111", "3.21", false);

    public final String checkIssuedDate;
    public final String bankCode;
    public final String checkStatus;
    public final String checkNumber;
    public final String checkTotalAmount;
    public final boolean valid;

    StaleCheckRowFixture(String checkIssuedDate, String bankCode, String checkStatus, String checkNumber, String checkTotalAmount, boolean valid) {
        this.checkIssuedDate = checkIssuedDate;
        this.bankCode = bankCode;
        this.checkStatus = checkStatus;
        this.checkNumber = checkNumber;
        this.checkTotalAmount = checkTotalAmount;
        this.valid = valid;
    }

}
