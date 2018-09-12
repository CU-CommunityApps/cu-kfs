package com.rsmart.kuali.kfs.cr.batch.fixture;

import com.rsmart.kuali.kfs.cr.CRConstants;
import org.kuali.kfs.sys.KFSConstants;
import edu.cornell.kfs.pdp.CUPdpTestConstants;

public enum StaleCheckRowFixture {

    CHECK_999_DISB_STAL_925_VALID("20180509", CUPdpTestConstants.MELLON_BANK_CODE, CRConstants.STALE, "99999999", "92.5", true),
    CHECK_999_DISB_BLANK_INVALID("20180509", CUPdpTestConstants.MELLON_BANK_CODE, KFSConstants.EMPTY_STRING,
            KFSConstants.EMPTY_STRING, KFSConstants.EMPTY_STRING, false),
    CHECK_123_DISB_STAL_29252_VALID("20180809", CUPdpTestConstants.MELLON_BANK_CODE, CRConstants.STALE, "12345678", "292.52", true),
    CHECK_123_BLANK_STAL_19223_INVALID("20180609", KFSConstants.EMPTY_STRING, CRConstants.STALE, "82345678", "192.23", false),
    CHECK_199_DISB_STAL_212319_VALID("20180712", CUPdpTestConstants.MELLON_BANK_CODE, CRConstants.STALE, "19999999", "2,123.19", true);

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
