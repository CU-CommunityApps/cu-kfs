package edu.cornell.kfs.fp.businessobject.fixture;

import org.apache.commons.lang3.StringUtils;
import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.sys.CUKFSKeyConstants;

public enum ScheduledSourceAccountingLineFixture {
    
    GOOD_TWELEVE_MONTHS(1200.00, 100.00, "12", 0, StringUtils.EMPTY),
    GOOD_ELEVEN_MONTHS(1100.00, 100.00, "11", 0, StringUtils.EMPTY),
    GOOD_ROUND_UP(51956.76, 4329.73, "12", 0, StringUtils.EMPTY),
    ERROR_TOO_MANY_OCCURANCES(1200.00, 100.00, "13", 1, CUKFSKeyConstants.ERROR_RCDV_TOO_MANY_RECURRENCES),
    ERROR_SUM_LESS_THAN_RECURRANCE(1200.00, 10.01, "12", 1, CUKFSKeyConstants.ERROR_RCDV_RECURRENCE_SUM_LESS_THAN_TANSACTION);
    
    
    public final String chart;
    public final String account;
    public final String objectCode;
    public final String scheduleType;
    public final int rowId;
    public final double amount;
    public final double partialAmount;
    public final String transactionCount;
    public final int errorCount;
    public final String errorKey;
    
    private ScheduledSourceAccountingLineFixture(String chart, String account, String objectCode, String scheduleType,
            int rowId, double amount, double partialAmount, String transactionCount, int errorCount, String errorKey) {
        this.chart = chart;
        this.account = account;
        this.objectCode = objectCode;
        this.scheduleType = scheduleType;
        this.rowId = rowId;
        this.amount = amount;
        this.partialAmount = partialAmount;
        this.transactionCount = transactionCount;
        this.errorCount = errorCount;
        this.errorKey = errorKey;
    }
    
    private ScheduledSourceAccountingLineFixture(double amount, double partialAmount, String transactionCount, int errorCount, String errorKey) {
        this ("IT", "G254700", "1400", CuFPConstants.ScheduledSourceAccountingLineConstants.ScheduleTypes.MONTHLY.name, 
                1, amount, partialAmount, transactionCount, errorCount, errorKey);
    }
    
}
