package edu.cornell.kfs.concur.services;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;

public interface ConcurAccountValidationTestConstants {
    public static final String VALID_CHART = "VALIDCHART";
    public static final String BAD_CHART = "BADCHART";
    public static final String VALID_ACCT_NBR = "VALIDACCT";
    public static final String BAD_ACCT_NBR = "BADACCT";
    public static final String INACTIVE_ACCT_NBR = "INACTIVEACCT";
    public static final String CLOSED_ACCT_NBR = "CLOSEDACCT";

    public static final String VALID_OBJ_CD = "VALIDOBJ";
    public static final String BAD_OBJ_CD = "BADOBJ";
    public static final String INACTIVE_OBJ_CD = "INACTIVEOBJ";

    public static final String VALID_PROJECT_CODE = "VALIDPROJECT";
    public static final String BAD_PROJECT_CODE = "BADPROJECT";
    public static final String INACTIVE_PROJECT_CODE = "INACTIVEPROJECT";

    public static final String VALID_SUB_ACCT = "VALIDSUBACCT";
    public static final String BAD_SUB_ACCT = "BADSUBACCT";
    public static final String INACTIVE_SUB_ACCT = "INACTIVESUBACCT";

    public static final String VALID_SUB_OBJECT = "VALIDSUBOBJ";
    public static final String BAD_SUB_OBJECT = "BADSUBOBJ";
    public static final String INACTIVE_SUB_OBJECT = "INACTIVESUBOBJ";
    
    public enum AccountEnum {
        VALID(VALID_CHART, VALID_ACCT_NBR, true, false, "subFundCode", "higherEdFunctionCode"),
        INACTIVE(VALID_CHART, INACTIVE_ACCT_NBR, false, true),
        CLOSED(VALID_CHART, CLOSED_ACCT_NBR, false, true);
        
        public final String chart;
        public final String account;
        public final boolean active;
        public final boolean closed;
        public final String subFundGroupCode;
        public final String higherEdFunctionCode;
        
        private AccountEnum(String chart, String account, boolean active, boolean closed) {
            this (chart, account, active, closed, StringUtils.EMPTY, StringUtils.EMPTY);
        }
        
        private AccountEnum(String chart, String account, boolean active, boolean closed, String subFundGroupCode, String higherEdFunctionCode) {
            this.chart = chart;
            this.account = account;
            this.active = active;
            this.closed = closed;
            this.subFundGroupCode = subFundGroupCode;
            this.higherEdFunctionCode = higherEdFunctionCode;
        }
        
        public Account toAccountBo() {
            Account accountBo = new Account();
            accountBo.setChartOfAccountsCode(chart);
            accountBo.setAccountNumber(account);
            accountBo.setActive(active);
            accountBo.setClosed(closed);
            accountBo.setSubFundGroupCode(subFundGroupCode);
            accountBo.setFinancialHigherEdFunctionCd(higherEdFunctionCode);
            return accountBo;
        }
        
    }

}
