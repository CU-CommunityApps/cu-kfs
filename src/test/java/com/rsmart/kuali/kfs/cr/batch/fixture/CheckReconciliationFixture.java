package com.rsmart.kuali.kfs.cr.batch.fixture;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.CrTestConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;

public enum CheckReconciliationFixture {

    CHECK_999_DISB_STAL_925("20180509", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 99999999, 92.5),
    CHECK_123_DISB_STAL_29252("20180809", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 12345678, 292.52),
    CHECK_199_DISB_STAL_925("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 19999999, 2123.19);

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
