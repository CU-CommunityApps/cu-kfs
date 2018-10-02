package com.rsmart.kuali.kfs.cr.batch.fixture;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.CrTestConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;

public enum CheckReconciliationFixture {

    CHECK_999_DISB_STAL_925("20180509", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 99999999, 92.5),
    CHECK_123_DISB_STAL_29252("20180809", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 12345678, 292.52),
    CHECK_199_DISB_STAL_925("20180722", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 19999999, 2123.19),
    CHECK_399_DISB_VOID_951("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.VOIDED, 39999999, 9.51),
    CHECK_111_DISB_CLRD_123("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.CLEARED, 11111111, 1.23),
    CHECK_211_DISB_STOP_123("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.STOP, 21111111, 1.23),
    CHECK_311_DISB_STAL_123("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.STALE, 31111111, 1.23),
    CHECK_411_DISB_EXCP_123("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.EXCP, 41111111, 1.23),
    CHECK_511_DISB_CDIS_321("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.CANCELLED, 51111111, 3.21);

    public final String checkIssuedDate;
    public final String bankCode;
    public final String checkStatus;
    public final KualiInteger checkNumber;
    public final KualiDecimal checkTotalAmount;

    CheckReconciliationFixture(String checkIssuedDate, String bankCode, String checkStatus, int checkNumber, double checkTotalAmount) {
        this.checkIssuedDate = checkIssuedDate;
        this.bankCode = bankCode;
        this.checkStatus = checkStatus;
        this.checkNumber = new KualiInteger(checkNumber);
        this.checkTotalAmount = new KualiDecimal(checkTotalAmount);
    }

}
