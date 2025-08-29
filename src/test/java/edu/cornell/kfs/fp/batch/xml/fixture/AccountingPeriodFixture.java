package edu.cornell.kfs.fp.batch.xml.fixture;

import java.sql.Date;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum AccountingPeriodFixture {
    JUNE_2018(2018, KFSConstants.MONTH12, "06/30/2018", "07/06/2018", false),
    CLOSING_2018(2018, KFSConstants.MONTH13, "06/30/2018", "07/06/2018", true),
    ANNUAL_BAL_2018(2018, KFSConstants.PERIOD_CODE_ANNUAL_BALANCE, "06/30/2018", true),
    JULY_2018(2019, KFSConstants.MONTH1, "07/31/2018", "08/06/2018", true),
    AUG_2018(2019, KFSConstants.MONTH2, "08/31/2018", "09/06/2018", true),
    SEPT_2018(2019, KFSConstants.MONTH3, "09/30/2018", "10/06/2018", true),
    OCT_2018(2019, KFSConstants.MONTH4, "10/31/2018", "11/06/2018", true),
    NOV_2018(2019, KFSConstants.MONTH5, "11/30/2018", "12/06/2018", true),
    DEC_2018(2019, KFSConstants.MONTH6, "12/31/2018", "01/06/2019", true),
    JAN_2019(2019, KFSConstants.MONTH7, "01/31/2019", "02/06/2019", true),
    FEB_2019(2019, KFSConstants.MONTH8, "02/28/2019", "03/06/2019", true),
    MAR_2019(2019, KFSConstants.MONTH9, "03/31/2019", "04/06/2019", true),
    APR_2019(2019, KFSConstants.MONTH10, "04/30/2019", "05/06/2019", true),
    MAY_2019(2019, KFSConstants.MONTH11, "05/31/2019", "06/06/2019", true),
    JUNE_2019(2019, KFSConstants.MONTH12, "06/30/2019", "07/06/2019", true),
    CLOSING_2019(2019, KFSConstants.MONTH13, "06/30/2019", "07/06/2019", true),
    ANNUAL_BAL_2019(2019, KFSConstants.PERIOD_CODE_ANNUAL_BALANCE, "06/30/2019", true),
    BEG_BAL_2019(2019, KFSConstants.PERIOD_CODE_BEGINNING_BALANCE, "06/30/2019", true),
    CG_BEG_BAL_2019(2019, KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE, "06/30/2019", true);

    public final Integer universityFiscalYear;
    public final String universityFiscalPeriodCode;
    public final String universityFiscalPeriodEndDate;
    public final String adjustmentAccrualVoucherDefaultReversalDate;
    public final boolean active;

    AccountingPeriodFixture(Integer universityFiscalYear, String universityFiscalPeriodCode,
            String universityFiscalPeriodEndDate, boolean active) {
        this(universityFiscalYear, universityFiscalPeriodCode, universityFiscalPeriodEndDate, KFSConstants.EMPTY_STRING, active);
    }

    AccountingPeriodFixture(Integer universityFiscalYear, String universityFiscalPeriodCode,
            String universityFiscalPeriodEndDate, String adjustmentAccrualVoucherDefaultReversalDate, boolean active) {
        this.universityFiscalYear = universityFiscalYear;
        this.universityFiscalPeriodCode = universityFiscalPeriodCode;
        this.universityFiscalPeriodEndDate = universityFiscalPeriodEndDate;
        this.adjustmentAccrualVoucherDefaultReversalDate = adjustmentAccrualVoucherDefaultReversalDate;
        this.active = active;
    }

    public AccountingPeriod toAccountingPeriod() {
        AccountingPeriod accountingPeriod = new AccountingPeriod();
        
        accountingPeriod.setUniversityFiscalYear(universityFiscalYear);
        accountingPeriod.setUniversityFiscalPeriodCode(universityFiscalPeriodCode);
        accountingPeriod.setUniversityFiscalPeriodName(getUniversityFiscalPeriodName());
        accountingPeriod.setUniversityFiscalPeriodEndDate(buildSqlDate(universityFiscalPeriodEndDate));
        if (StringUtils.isNotBlank(adjustmentAccrualVoucherDefaultReversalDate)) {
            accountingPeriod.setAdjustmentAccrualVoucherDefaultReversalDate(getReversalSqlDate());
        }
        accountingPeriod.setActive(active);
        
        return accountingPeriod;
    }

    public String getUniversityFiscalPeriodName() {
        String fiscalYearString = universityFiscalYear.toString();
        String previousYearString = String.valueOf(universityFiscalYear - 1);
        
        switch (universityFiscalPeriodCode) {
            case KFSConstants.MONTH1 :
                return "JULY " + previousYearString;
            case KFSConstants.MONTH2 :
                return "AUG. " + previousYearString;
            case KFSConstants.MONTH3 :
                return "SEPT. " + previousYearString;
            case KFSConstants.MONTH4 :
                return "OCT. " + previousYearString;
            case KFSConstants.MONTH5 :
                return "NOV. " + previousYearString;
            case KFSConstants.MONTH6 :
                return "DEC. " + previousYearString;
            case KFSConstants.MONTH7 :
                return "JAN. " + fiscalYearString;
            case KFSConstants.MONTH8 :
                return "FEB. " + fiscalYearString;
            case KFSConstants.MONTH9 :
                return "MAR. " + fiscalYearString;
            case KFSConstants.MONTH10 :
                return "APR. " + fiscalYearString;
            case KFSConstants.MONTH11 :
                return "MAY " + fiscalYearString;
            case KFSConstants.MONTH12 :
                return "JUNE " + fiscalYearString;
            case KFSConstants.MONTH13 :
                return "CLOSING " + StringUtils.right(fiscalYearString, 2);
            case KFSConstants.PERIOD_CODE_ANNUAL_BALANCE :
                return "ANNUAL BAL";
            case KFSConstants.PERIOD_CODE_BEGINNING_BALANCE :
                return "BEG BAL " + StringUtils.right(fiscalYearString, 2);
            case KFSConstants.PERIOD_CODE_CG_BEGINNING_BALANCE :
                return "CG BEG BAL";
            default :
                throw new IllegalStateException("Invalid period code: " + universityFiscalPeriodCode);
        }
    }

    public Date getReversalSqlDate() {
        return buildSqlDate(adjustmentAccrualVoucherDefaultReversalDate);
    }

    private Date buildSqlDate(String dateString) {
        DateTime parsedDate = StringToJavaDateAdapter.parseToDateTime(dateString);
        return new Date(parsedDate.getMillis());
    }

    public static Optional<AccountingPeriodFixture> findAccountingPeriod(Integer fiscalYear, String periodCode) {
        return Arrays.stream(AccountingPeriodFixture.values())
                .filter(fixture -> fixture.universityFiscalYear.equals(fiscalYear)
                        && StringUtils.equals(fixture.universityFiscalPeriodCode, periodCode))
                .findFirst();
    }

    public static Stream<AccountingPeriodFixture> findOpenAccountingPeriodsAsStream() {
        return Arrays.stream(AccountingPeriodFixture.values())
                .filter(fixture -> fixture.active)
                .sorted(new AccountingPeriodFixtureComparator());
    }

    private static final class AccountingPeriodFixtureComparator implements Comparator<AccountingPeriodFixture> {

        @Override
        public int compare(AccountingPeriodFixture fixture1, AccountingPeriodFixture fixture2) {
            int result = fixture1.universityFiscalYear.compareTo(fixture2.universityFiscalYear);
            if (result == 0) {
                result = fixture1.universityFiscalPeriodCode.compareTo(fixture2.universityFiscalPeriodCode);
            }
            return result;
        }
        
    }

}
