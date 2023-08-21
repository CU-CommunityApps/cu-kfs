package com.rsmart.kuali.kfs.cr.batch.fixture;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import com.rsmart.kuali.kfs.cr.CRConstants;
import com.rsmart.kuali.kfs.cr.CrTestConstants;
import com.rsmart.kuali.kfs.cr.CrTestConstants.TestParamValues;
import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;

import edu.cornell.kfs.sys.CUKFSConstants;

public enum CheckReconciliationFixture {

    CHECK_999_DISB_STAL_925("20180509", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 99999999, 92.5),
    CHECK_123_DISB_STAL_29252("20180809", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 12345678, 292.52),
    CHECK_199_DISB_STAL_925("20180722", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 19999999, 2123.19),
    CHECK_399_DISB_VOID_951("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.VOIDED, 39999999, 9.51),
    CHECK_111_DISB_CLRD_123("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.CLEARED, 11111111, 1.23),
    CHECK_211_DISB_STOP_123("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.STOP, 21111111, 1.23),
    CHECK_311_DISB_STAL_123("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.STALE, 31111111, 1.23),
    CHECK_411_DISB_EXCP_123("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.EXCP, 41111111, 1.23),
    CHECK_511_DISB_CDIS_321("20180712", CrTestConstants.MELLON_BANK_CODE, CRConstants.CANCELLED, 51111111, 3.21),

    CHECK_50012233_DISB_ISSD_5607("20230726", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 50012233, 56.07,
            CRConstants.CLEARED, "20230811"),
    CHECK_50012234_DISB_ISSD_22222("20230727", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 50012234, 222.22,
            CRConstants.CLEARED, "20230811"),
    CHECK_50012237_DISB_VOID_12500("20991231", CrTestConstants.MELLON_BANK_CODE, CRConstants.VOIDED, 50012237, 125),
    CHECK_55555555_DISB_ISSD_22222("20230727", CrTestConstants.MELLON_BANK_CODE, CRConstants.ISSUED, 55555555, 222.22,
            CRConstants.EXCP, "20230811"),
    CHECK_15566_NDWR_ISSD_76500("20230801", CrTestConstants.JPMC_BANK_CODE, CRConstants.ISSUED, 15566, 765,
            CRConstants.CLEARED, "20230810"),
    CHECK_15567_NDWR_ISSD_1995("20230801", CrTestConstants.JPMC_BANK_CODE, CRConstants.ISSUED, 15567, 19.95,
            CRConstants.CLEARED, "20230810"),
    CHECK_15568_NDWR_ISSD_2202455("20991231", CrTestConstants.JPMC_BANK_CODE, CRConstants.ISSUED, 15568, 22024.55,
            CRConstants.CLEARED, "20230810"),
    CHECK_15569_NDWR_VOID_18875("20991231", CrTestConstants.JPMC_BANK_CODE, CRConstants.VOIDED, 15569, 188.75),
    CHECK_88888_NDWR_ISSD_1995("20230801", CrTestConstants.JPMC_BANK_CODE, CRConstants.ISSUED, 88888, 19.95,
            CRConstants.EXCP, "20230810");

    static final DateTimeFormatter DATE_FORMATTER_yyyyMMdd = DateTimeFormatter.ofPattern(
            CUKFSConstants.DATE_FORMAT_yyyyMMdd, Locale.US);

    public final String checkIssuedDate;
    public final String bankCode;
    public final String checkStatus;
    public final KualiInteger checkNumber;
    public final KualiDecimal checkTotalAmount;
    public final String newCheckStatus;
    public final String newStatusChangeDate;

    private CheckReconciliationFixture(String checkIssuedDate, String bankCode, String checkStatus, int checkNumber, double checkTotalAmount) {
        this(checkIssuedDate, bankCode, checkStatus, checkNumber, checkTotalAmount, checkStatus, checkIssuedDate);
    }

    CheckReconciliationFixture(String checkIssuedDate, String bankCode, String checkStatus,
            int checkNumber, double checkTotalAmount, String newCheckStatus, String newStatusChangeDate) {
        this.checkIssuedDate = checkIssuedDate;
        this.bankCode = bankCode;
        this.checkStatus = checkStatus;
        this.checkNumber = new KualiInteger(checkNumber);
        this.checkTotalAmount = new KualiDecimal(checkTotalAmount);
        this.newCheckStatus = newCheckStatus;
        this.newStatusChangeDate = newStatusChangeDate;
    }

    public Date getParsedCheckIssuedDate() {
        LocalDate localDate = LocalDate.parse(checkIssuedDate, DATE_FORMATTER_yyyyMMdd);
        return Date.valueOf(localDate);
    }

    public Date getParsedStatusChangeDate() {
        LocalDate localDate = LocalDate.parse(newStatusChangeDate, DATE_FORMATTER_yyyyMMdd);
        return Date.valueOf(localDate);
    }

    public String getExpectedBankAccountNumber() {
        switch (bankCode) {
            case CrTestConstants.MELLON_BANK_CODE :
                return TestParamValues.ACCOUNT_111_2345;
            case CrTestConstants.JPMC_BANK_CODE :
                return TestParamValues.ACCOUNT_888_7777;
            default :
                throw new IllegalStateException("Unexpected bank code: " + bankCode);
        }
    }

    public CheckReconciliation toCheckReconciliationUsingCurrentStatus() {
        CheckReconciliation checkReconciliation = toCheckReconciliation();
        checkReconciliation.setStatusChangeDate(checkReconciliation.getCheckDate());
        return checkReconciliation;
    }

    private CheckReconciliation toCheckReconciliation() {
        CheckReconciliation checkReconciliation = new CheckReconciliation();
        checkReconciliation.setId(ordinal());
        checkReconciliation.setBankCode(bankCode);
        checkReconciliation.setBankAccountNumber(getExpectedBankAccountNumber());
        checkReconciliation.setCheckNumber(checkNumber);
        checkReconciliation.setCheckDate(getParsedCheckIssuedDate());
        checkReconciliation.setStatus(checkStatus);
        checkReconciliation.setAmount(checkTotalAmount);
        return checkReconciliation;
    }

}
