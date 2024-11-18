package edu.cornell.kfs.concur.services;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;

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
        INACTIVE(VALID_CHART, INACTIVE_ACCT_NBR, false, true), CLOSED(VALID_CHART, CLOSED_ACCT_NBR, false, true);

        public final String chart;
        public final String account;
        public final boolean active;
        public final boolean closed;
        public final String subFundGroupCode;
        public final String higherEdFunctionCode;

        private AccountEnum(String chart, String account, boolean active, boolean closed) {
            this(chart, account, active, closed, StringUtils.EMPTY, StringUtils.EMPTY);
        }

        private AccountEnum(String chart, String account, boolean active, boolean closed, String subFundGroupCode,
                String higherEdFunctionCode) {
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

    public enum SubAccountEnum {
        VALID(VALID_CHART, VALID_ACCT_NBR, VALID_SUB_ACCT, true),
        INACTIVE(VALID_CHART, VALID_ACCT_NBR, INACTIVE_SUB_ACCT, false);

        public final String chart;
        public final String account;
        public final String subAccount;
        public final boolean active;

        private SubAccountEnum(String chart, String account, String subAccount, boolean active) {
            this.chart = chart;
            this.account = account;
            this.subAccount = subAccount;
            this.active = active;
        }

        public SubAccount toSubAccount() {
            SubAccount subAccountBo = new SubAccount();
            subAccountBo.setChartOfAccountsCode(chart);
            subAccountBo.setAccountNumber(account);
            subAccountBo.setSubAccountNumber(subAccount);
            subAccountBo.setActive(active);
            return subAccountBo;
        }
    }

    public enum ObjectCodeEnum {
        VALID(VALID_CHART, VALID_OBJ_CD, true), INACTIVE(VALID_CHART, INACTIVE_OBJ_CD, false);

        public final String chart;
        public final String objectCode;
        public final boolean active;

        private ObjectCodeEnum(String chart, String objectCode, boolean active) {
            this.chart = chart;
            this.objectCode = objectCode;
            this.active = active;
        }

        public ObjectCode toObjectCode() {
            ObjectCode objectCodeBo = new ObjectCode();
            objectCodeBo.setChartOfAccountsCode(chart);
            objectCodeBo.setFinancialObjectCode(objectCode);
            objectCodeBo.setActive(active);
            return objectCodeBo;
        }
    }

    public enum SubObjectCodeEnum {
        VALID(VALID_CHART, VALID_ACCT_NBR, VALID_OBJ_CD, VALID_SUB_OBJECT, true),
        INACTIVE(VALID_CHART, VALID_ACCT_NBR, VALID_OBJ_CD, INACTIVE_SUB_OBJECT, false);

        public final String chart;
        public final String account;
        public final String objectCode;
        public final String subObjectCode;
        public final boolean active;

        private SubObjectCodeEnum(String chart, String account, String objectCode, String subObjectCode,
                boolean active) {
            this.chart = chart;
            this.account = account;
            this.objectCode = objectCode;
            this.subObjectCode = subObjectCode;
            this.active = active;
        }

        public SubObjectCode toSubObjectCode() {
            SubObjectCode subObjectCodeBo = new SubObjectCode();
            subObjectCodeBo.setChartOfAccountsCode(chart);
            subObjectCodeBo.setAccountNumber(account);
            subObjectCodeBo.setFinancialObjectCode(objectCode);
            subObjectCodeBo.setFinancialSubObjectCode(subObjectCode);
            subObjectCodeBo.setActive(active);
            return subObjectCodeBo;
        }
    }

    public enum ProjectCodeEnum {
        VALID(VALID_PROJECT_CODE, true), INACTIVE(INACTIVE_PROJECT_CODE, false);

        public final String projectCode;
        public final boolean active;

        private ProjectCodeEnum(String projectCode, boolean active) {
            this.projectCode = projectCode;
            this.active = active;
        }

        public ProjectCode toProjectCode() {
            ProjectCode project = new ProjectCode();
            project.setCode(projectCode);
            project.setActive(active);
            return project;
        }
    }

}
