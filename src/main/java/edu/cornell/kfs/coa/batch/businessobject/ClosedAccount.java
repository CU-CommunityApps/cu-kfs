package edu.cornell.kfs.coa.batch.businessobject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public class ClosedAccount {
    
    private String chart;
    private String accountNumber;
    private String subAccountNumber;
    private String accountClosedIndicator;
    private Date accountClosedDate;
    
    public ClosedAccount() {
        this.chart = null;
        this.accountNumber = null;
        this.subAccountNumber = null;
        this.accountClosedIndicator = null;
        this.accountClosedDate = null;
    }
    
    public String getChart() {
        return chart;
    }
    
    public void setChart(String chart) {
        this.chart = chart;
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    public String getSubAccountNumber() {
        return subAccountNumber;
    }
    
    public void setSubAccountNumber(String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }
    
    public String getAccountClosedIndicator() {
        return accountClosedIndicator;
    }
    
    public void setAccountClosedIndicator(String accountClosedIndicator) {
        this.accountClosedIndicator = accountClosedIndicator;
    }

    public Date getAccountClosedDate() {
        return accountClosedDate;
    }

    public void setAccountClosedDate(Date accountClosedDate) {
        this.accountClosedDate = accountClosedDate;
    }
    
    public String toCsvString() {
        SimpleDateFormat sdf = new SimpleDateFormat(KFSConstants.MONTH_DAY_YEAR_DATE_FORMAT, Locale.US);
        StringBuilder sb = new StringBuilder();
        sb.append(chart).append(KFSConstants.COMMA);
        sb.append(accountNumber).append(KFSConstants.COMMA);
        sb.append((StringUtils.isNotBlank(subAccountNumber) ? subAccountNumber : KFSConstants.EMPTY_STRING)).append(KFSConstants.COMMA);
        sb.append(accountClosedIndicator).append(KFSConstants.COMMA);
        sb.append(sdf.format(accountClosedDate));
        return sb.toString();
    }

}
