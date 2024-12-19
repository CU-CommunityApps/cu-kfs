package edu.cornell.kfs.concur.services;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.ConcurKeyConstants;
import edu.cornell.kfs.concur.ConcurUtils;
import edu.cornell.kfs.concur.businessobjects.ConcurAccountInfo;

public class ConcurAccountValidationTestConstants {
    private static final String VALID_CHART = "VALIDCHART";
    private static final String BAD_CHART = "BADCHART";
    private static final String VALID_ACCT_NBR = "VALIDACCT";
    private static final String BAD_ACCT_NBR = "BADACCT";
    private static final String INACTIVE_ACCT_NBR = "INACTIVEACCT";
    private static final String CLOSED_ACCT_NBR = "CLOSEDACCT";

    private static final String VALID_OBJ_CD = "VALIDOBJ";
    private static final String BAD_OBJ_CD = "BADOBJ";
    private static final String INACTIVE_OBJ_CD = "INACTIVEOBJ";

    public static final String VALID_PROJECT_CODE = "VALIDPROJECT";
    public static final String BAD_PROJECT_CODE = "BADPROJECT";
    public static final String INACTIVE_PROJECT_CODE = "INACTIVEPROJECT";

    private static final String VALID_SUB_ACCT = "VALIDSUBACCT";
    private static final String BAD_SUB_ACCT = "BADSUBACCT";
    private static final String INACTIVE_SUB_ACCT = "INACTIVESUBACCT";

    private static final String VALID_SUB_OBJECT = "VALIDSUBOBJ";
    private static final String BAD_SUB_OBJECT = "BADSUBOBJ";
    private static final String INACTIVE_SUB_OBJECT = "INACTIVESUBOBJ";
    
    private static final String VALID_ACCOUNT_DETAIL_MESSAGE = "Account VALIDCHART-VALIDACCT, subFundCode, HEFC higherEdFunctionCode";

    public enum AccountEnum {
        VALID(VALID_CHART, VALID_ACCT_NBR, true, false, "subFundCode", "higherEdFunctionCode"),
        INACTIVE(VALID_CHART, INACTIVE_ACCT_NBR, false, true), CLOSED(VALID_CHART, CLOSED_ACCT_NBR, false, true),
        BAD(VALID_CHART, BAD_ACCT_NBR, false, false), NULL(VALID_CHART, null, false, false);

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
        NULL_SUB_ACCOUNT(VALID_CHART, VALID_ACCT_NBR, null, true),
        BAD_ACCOUNT(VALID_CHART, BAD_ACCT_NBR, VALID_SUB_ACCT, false),
        BAD_SUB_ACCOUNT(VALID_CHART, VALID_ACCT_NBR, BAD_SUB_ACCT, false),
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
        VALID(VALID_CHART, VALID_OBJ_CD, true), INACTIVE(VALID_CHART, INACTIVE_OBJ_CD, false),
        NULL(VALID_CHART, null, false), BAD(VALID_CHART, BAD_OBJ_CD, false);

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
        BAD_OBJ(VALID_CHART, VALID_ACCT_NBR, BAD_OBJ_CD, VALID_SUB_OBJECT, false),
        BAD_SUB_OBJ(VALID_CHART, VALID_ACCT_NBR, VALID_OBJ_CD, BAD_SUB_OBJECT, false),
        NULL_SUB_OBJ(VALID_CHART, VALID_ACCT_NBR, VALID_OBJ_CD, null, false),
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

    public enum ConcurAccountInfoEnum {
        FULL_ACCOUNT_INFO(VALID_CHART, VALID_ACCT_NBR, VALID_SUB_ACCT, VALID_OBJ_CD, VALID_SUB_OBJECT,
                VALID_PROJECT_CODE, true, buildMessages(), buildMessages(VALID_ACCOUNT_DETAIL_MESSAGE)),
        NULL_CHART(null, VALID_ACCT_NBR, VALID_SUB_ACCT, VALID_OBJ_CD, VALID_SUB_OBJECT, VALID_PROJECT_CODE, false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                        ConcurConstants.AccountingStringFieldNames.CHART))),
        NULL_ACCOUNT(VALID_CHART, null, VALID_SUB_ACCT, VALID_OBJ_CD, VALID_SUB_OBJECT, VALID_PROJECT_CODE, false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                        ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER))),
        NULL_OBJECT(VALID_CHART, VALID_ACCT_NBR, VALID_SUB_ACCT, null, VALID_SUB_OBJECT, VALID_PROJECT_CODE, false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                        ConcurConstants.AccountingStringFieldNames.OBJECT_CODE))),
        NULL_CHART_ACCOUNT_OBJECT(null, null, VALID_SUB_ACCT, null, VALID_SUB_OBJECT, VALID_PROJECT_CODE, false,
                buildMessages(
                        buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                ConcurConstants.AccountingStringFieldNames.CHART),
                        buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER),
                        buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                ConcurConstants.AccountingStringFieldNames.OBJECT_CODE))),
        NULL_CHART_OBJECT(null, VALID_ACCT_NBR, VALID_SUB_ACCT, null, VALID_SUB_OBJECT, VALID_PROJECT_CODE, false,
                buildMessages(
                        buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                ConcurConstants.AccountingStringFieldNames.CHART),
                        buildFormattedMessage(KFSKeyConstants.ERROR_REQUIRED,
                                ConcurConstants.AccountingStringFieldNames.OBJECT_CODE))),
        BAD_CHART_CODE(BAD_CHART, VALID_ACCT_NBR, VALID_SUB_ACCT, VALID_OBJ_CD, VALID_SUB_OBJECT, VALID_PROJECT_CODE,
                false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                        ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, BAD_CHART, VALID_ACCT_NBR))),
        BAD_ACCOUNT(VALID_CHART, BAD_ACCT_NBR, VALID_SUB_ACCT, VALID_OBJ_CD, VALID_SUB_OBJECT, VALID_PROJECT_CODE,
                false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                        ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, VALID_CHART, BAD_ACCT_NBR))),
        INACTIVE_ACCOUNT(VALID_CHART, INACTIVE_ACCT_NBR, VALID_SUB_ACCT, VALID_OBJ_CD, VALID_SUB_OBJECT,
                VALID_PROJECT_CODE, false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_INACTIVE,
                        ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, VALID_CHART, INACTIVE_ACCT_NBR))),
        BAD_SUBACCOUNT(VALID_CHART, VALID_ACCT_NBR, BAD_SUB_ACCT, VALID_OBJ_CD, VALID_SUB_OBJECT, VALID_PROJECT_CODE,
                false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                        ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, VALID_CHART, VALID_ACCT_NBR,
                        BAD_SUB_ACCT))),
        BAD_OBJECT(VALID_CHART, VALID_ACCT_NBR, VALID_SUB_ACCT, BAD_OBJ_CD, VALID_SUB_OBJECT, VALID_PROJECT_CODE, false,
                buildMessages(
                        buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.OBJECT_CODE, VALID_CHART, BAD_OBJ_CD),
                        buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, VALID_CHART, VALID_ACCT_NBR,
                                BAD_OBJ_CD, VALID_SUB_OBJECT))),
        INACTIVE_OBJECT(VALID_CHART, VALID_ACCT_NBR, VALID_SUB_ACCT, INACTIVE_OBJ_CD, VALID_SUB_OBJECT,
                VALID_PROJECT_CODE, false,
                buildMessages(
                        buildFormattedMessage(KFSKeyConstants.ERROR_INACTIVE,
                                ConcurConstants.AccountingStringFieldNames.OBJECT_CODE, VALID_CHART, INACTIVE_OBJ_CD),
                        buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, VALID_CHART, VALID_ACCT_NBR,
                                INACTIVE_OBJ_CD, VALID_SUB_OBJECT))),
        BAD_SUBOBJECT(VALID_CHART, VALID_ACCT_NBR, VALID_SUB_ACCT, VALID_OBJ_CD, BAD_SUB_OBJECT, VALID_PROJECT_CODE,
                false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                        ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, VALID_CHART, VALID_ACCT_NBR,
                        VALID_OBJ_CD, BAD_SUB_OBJECT))),
        INACTIVE_SUBOBJECT(VALID_CHART, VALID_ACCT_NBR, VALID_SUB_ACCT, VALID_OBJ_CD, INACTIVE_SUB_OBJECT,
                VALID_PROJECT_CODE, false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_INACTIVE,
                        ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, VALID_CHART, VALID_ACCT_NBR,
                        VALID_OBJ_CD, INACTIVE_SUB_OBJECT))),
        BAD_PROJECT(VALID_CHART, VALID_ACCT_NBR, VALID_SUB_ACCT, VALID_OBJ_CD, VALID_SUB_OBJECT, BAD_PROJECT_CODE,
                false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                        ConcurConstants.AccountingStringFieldNames.PROJECT_CODE, BAD_PROJECT_CODE))),
        INACTIVE_PROJECT(VALID_CHART, VALID_ACCT_NBR, VALID_SUB_ACCT, VALID_OBJ_CD, VALID_SUB_OBJECT,
                INACTIVE_PROJECT_CODE, false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_INACTIVE,
                        ConcurConstants.AccountingStringFieldNames.PROJECT_CODE, INACTIVE_PROJECT_CODE))),
        BAD_ACCOUNT_SUBACCOUNT_OBJECT_SUBOBJECT_PROJECT(VALID_CHART, BAD_ACCT_NBR, BAD_SUB_ACCT, BAD_OBJ_CD,
                BAD_SUB_OBJECT, BAD_PROJECT_CODE, false,
                buildMessages(buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                        ConcurConstants.AccountingStringFieldNames.ACCOUNT_NUMBER, VALID_CHART, BAD_ACCT_NBR))),
        BAD_SUBACCOUNT_OBJECT_SUBOBJECT_PROJECT(VALID_CHART, VALID_ACCT_NBR, BAD_SUB_ACCT, BAD_OBJ_CD, BAD_SUB_OBJECT,
                BAD_PROJECT_CODE, false,
                buildMessages(
                        buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.OBJECT_CODE, VALID_CHART, BAD_OBJ_CD),
                        buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.SUB_ACCOUNT_NUMBER, VALID_CHART,
                                VALID_ACCT_NBR, BAD_SUB_ACCT),
                        buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.SUB_OBJECT_CODE, VALID_CHART, VALID_ACCT_NBR,
                                BAD_OBJ_CD, BAD_SUB_OBJECT),
                        buildFormattedMessage(KFSKeyConstants.ERROR_EXISTENCE,
                                ConcurConstants.AccountingStringFieldNames.PROJECT_CODE, BAD_PROJECT_CODE))),
        MINIMUM_ACCOUNT_INFO_NULLS(VALID_CHART, VALID_ACCT_NBR, null, VALID_OBJ_CD, null, null, true, buildMessages(), 
                buildMessages(VALID_ACCOUNT_DETAIL_MESSAGE)),
        MINIMUM_ACCOUNT_INFO_EMPTY(VALID_CHART, VALID_ACCT_NBR, StringUtils.EMPTY, VALID_OBJ_CD, StringUtils.EMPTY,
                StringUtils.EMPTY, true, buildMessages(), buildMessages(VALID_ACCOUNT_DETAIL_MESSAGE));

        public final String chart;
        public final String account;
        public final String subAccount;
        public final String object;
        public final String subObject;
        public final String project;
        public final boolean validationExpectation;
        public final List<String> expectedErrorMessages;
        public final List<String> expectedDetailMessages;

        private ConcurAccountInfoEnum(String chart, String account, String subAccount, String object, String subObject,
                String project, boolean validationExpectation, List<String> expectedErrorMessages) {
            this(chart, account, subAccount, object, subObject, project, validationExpectation, expectedErrorMessages, new ArrayList<String>());
        }
        
        private ConcurAccountInfoEnum(String chart, String account, String subAccount, String object, String subObject,
                String project, boolean validationExpectation, List<String> expectedErrorMessages, List<String> expectedDetailMessages) {
            this.chart = chart;
            this.account = account;
            this.subAccount = subAccount;
            this.object = object;
            this.subObject = subObject;
            this.project = project;
            this.validationExpectation = validationExpectation;
            this.expectedErrorMessages = expectedErrorMessages;
            this.expectedDetailMessages = expectedDetailMessages;
        }

        public ConcurAccountInfo toConcurAccountInfo() {
            ConcurAccountInfo accountInfo = new ConcurAccountInfo(chart, account, subAccount, object, subObject,
                    project);
            return accountInfo;
        }
    }

    private static String buildFormattedMessage(String errorMessageProperty, String label) {
        return MessageFormat.format(buildMockConfigurationService().getPropertyValueAsString(errorMessageProperty),
                label);
    }

    public static String buildFormattedMessage(String errorMessageProperty, String label, String... values) {
        String formattedLabel = ConcurUtils.formatStringForErrorMessage(label, values);
        return buildFormattedMessage(errorMessageProperty, formattedLabel);
    }

    public static List<String> buildMessages(String... messages) {
        List<String> messageList = new ArrayList<>();
        for (String message : messages) {
            messageList.add(message);
        }
        return messageList;
    }

    public static ConfigurationService buildMockConfigurationService() {
        ConfigurationService service = Mockito.mock(ConfigurationService.class);
        Mockito.when(
                service.getPropertyValueAsString(ConcurKeyConstants.MESSAGE_CONCUR_EVENT_NOTIFICATION_ACCOUNT_DETAIL))
                .thenReturn("Account {0}-{1}, {2}, HEFC {3}");
        Mockito.when(service.getPropertyValueAsString(KFSKeyConstants.ERROR_REQUIRED))
                .thenReturn("{0} is a required field.");
        Mockito.when(service.getPropertyValueAsString(KFSKeyConstants.ERROR_EXISTENCE))
                .thenReturn("The specified {0} does not exist.");
        Mockito.when(service.getPropertyValueAsString(KFSKeyConstants.ERROR_INACTIVE))
                .thenReturn("The specified {0} is inactive.");
        return service;
    }

}
