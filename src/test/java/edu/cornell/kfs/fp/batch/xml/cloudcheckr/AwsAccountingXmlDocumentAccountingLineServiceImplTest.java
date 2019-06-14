package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import edu.cornell.kfs.coa.fixture.AwsAccountFixture;
import edu.cornell.kfs.coa.fixture.ChartFixture;
import edu.cornell.kfs.coa.fixture.ObjectCodeFixture;
import edu.cornell.kfs.coa.fixture.ProjectCodeFixture;
import edu.cornell.kfs.coa.fixture.SubAccountFixture;
import edu.cornell.kfs.coa.fixture.SubObjectCodeFixture;
import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.service.impl.AwsAccountingXmlDocumentAccountingLineServiceImpl;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture.GroupLevelFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentAccountingLineFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.DefaultKfsAccountForAwsFixture;
import org.apache.commons.lang.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.ObjectCodeService;
import org.kuali.kfs.coa.service.ProjectCodeService;
import org.kuali.kfs.coa.service.SubAccountService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AwsAccountingXmlDocumentAccountingLineServiceImplTest {
	private static final Logger LOG = LogManager.getLogger(AwsAccountingXmlDocumentAccountingLineServiceImplTest.class);

    private AwsAccountingXmlDocumentAccountingLineServiceImpl awsAccountingXmlDocumentAccountingLineService;

    @Before
    public void setUp() {
        this.awsAccountingXmlDocumentAccountingLineService = new AwsAccountingXmlDocumentAccountingLineServiceImpl();
        this.awsAccountingXmlDocumentAccountingLineService.setConfigurationService(buildMockConfigurationService());
        this.awsAccountingXmlDocumentAccountingLineService.setParameterService(buildMockParameterService());
        this.awsAccountingXmlDocumentAccountingLineService.setChartService(buildMockChartService());
        this.awsAccountingXmlDocumentAccountingLineService.setAccountService(buildMockAccountService());
        this.awsAccountingXmlDocumentAccountingLineService.setObjectCodeService(buildMockObjectCodeService());
        this.awsAccountingXmlDocumentAccountingLineService.setSubObjectCodeService(buildMockSubObjectCodeService());
        this.awsAccountingXmlDocumentAccountingLineService.setSubAccountService(buildMockSubAccountService());
        this.awsAccountingXmlDocumentAccountingLineService.setProjectCodeService(buildMockProjectCodeService());
    }

    @After
    public void tearDown() {
        this.awsAccountingXmlDocumentAccountingLineService = null;
    }

    @Test
    public void testVerifyNoneUsesDefaultAccountString() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_NONE_COST_1,
                DefaultKfsAccountForAwsFixture.AWS_ABC_KFS_1658328,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1658328_OBJ_6600_AMOUNT_1);
    }

    @Test
    public void testVerifyInvalidAccountUsesDefaultAccountString() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_165833X_COST_2,
                DefaultKfsAccountForAwsFixture.AWS_ABC_KFS_1658328,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1658328_OBJ_6600_AMOUNT_2);
    }

    @Test
    public void testVerifyInvalidAccountUsesDefaultInvalidAccountString() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_165833X_COST_3,
                DefaultKfsAccountForAwsFixture.AWS_DEF_KFS_165835X,
                AccountingXmlDocumentAccountingLineFixture.ACCT_165835X_OBJ_6600_AMOUNT_3);
    }

    @Test
    public void testVerifyValidAccountHyphenSubAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_R583805_70710_COST_4,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_R583805_SA_70170_OBJ_6600_AMOUNT_4);
    }

    @Test
    public void testVerifyValidAccountHyphenInvalidSubAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_R583805_533X_COST_5,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_R583805_OBJ_6600_AMOUNT_5);
    }

    @Test
    public void testVerifyValidStarDelimitedAccountStringWithProject() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_R589966_STAR_VALID_COST_6,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_R589966_OBJ_1000_AMOUNT_6);
    }

    @Test
    public void testVerifyValidStarDelimitedAccountStringWithSubObject() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_1023715_STAR_VALID_COST_7,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1023715_OBJ_4020_SO109_AMOUNT_7);
    }

    @Test
    public void testVerifyValidStarDelimitedAccountStringWithInvalidSubObject() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_1023715_STAR_INVALID_SUB_OBJ_COST_8,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1023715_OBJ_4020_AMOUNT_8);
    }

    @Test
    public void testVerifyValidStarDelimitedAccountStringWithInvalidSubAccountProject() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_R589966_STAR_PART_VALID_COST_9,
            DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
            AccountingXmlDocumentAccountingLineFixture.ACCT_R589966_OBJ_1000_AMOUNT_9);
    }

    @Test
    public void testVerifyValidStarDelimitedAccountStringWithValidSubAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_J801000_COST_10,
                DefaultKfsAccountForAwsFixture.AWS_JKL_KFS_J801000,
                AccountingXmlDocumentAccountingLineFixture.ACCT_J801000_SA_SHAN_OBJ_6600_AMOUNT_10);
    }

    @Test
    public void testVerifyInvalidAccountNumberUsesValidStarDelimitedDefaultAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_J80100X_COST_11,
                DefaultKfsAccountForAwsFixture.AWS_JKL_KFS_J801000,
                AccountingXmlDocumentAccountingLineFixture.ACCT_J801000_OBJ_6600_AMOUNT_11);
    }

    @Test
    public void testVerifyInternalDefaultAccountUsesCostCenterValueAsAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_NONE_COST_12,
                DefaultKfsAccountForAwsFixture.AWS_MNO_KFS_INTERNAL,
                AccountingXmlDocumentAccountingLineFixture.ACCT_INTERNAL_OBJ_6600_AMOUNT_12);
    }

    @Test
    public void testVerifyInvalidStarDelimitedAccountStringUsesInvalidDefaultAsAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_1023715_COST_13,
                DefaultKfsAccountForAwsFixture.AWS_STU_KFS_1023715_INVALID,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1023715_OBJ_4020_AMOUNT_13_INVALID);
    }

    private void verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture groupLevelCostCenterFixture,
                                                       DefaultKfsAccountForAwsFixture defaultKfsAccountForAwsFixture,
                                                       AccountingXmlDocumentAccountingLineFixture expectedXmlAccountingLineFixture) {
        GroupLevel groupLevelCostCenter = groupLevelCostCenterFixture.toGroupLevel();
        DefaultKfsAccountForAws defaultKfsAccountForAws = defaultKfsAccountForAwsFixture.toDefaultKfsAccountForAwsPojo();
        AccountingXmlDocumentAccountingLine expectedXmlAccountingLine = expectedXmlAccountingLineFixture.toAccountingLinePojo();
        AccountingXmlDocumentAccountingLine actualXmlAccountingLine =
                awsAccountingXmlDocumentAccountingLineService.createAccountingXmlDocumentAccountingLine(groupLevelCostCenter, defaultKfsAccountForAws);

        LOG.info(String.format("Expect (%s, %s) => (%s, %s, %s)", groupLevelCostCenter.getGroupValue(),
                defaultKfsAccountForAws.getKfsDefaultAccount(), expectedXmlAccountingLine.getChartCode(),
                expectedXmlAccountingLine.getAccountNumber(), expectedXmlAccountingLine.getObjectCode()));

        LOG.info(String.format("Actual (%s, %s) => (%s, %s, %s)", groupLevelCostCenter.getGroupValue(),
                defaultKfsAccountForAws.getKfsDefaultAccount(), actualXmlAccountingLine.getChartCode(),
                actualXmlAccountingLine.getAccountNumber(), actualXmlAccountingLine.getObjectCode()));

        assert(ObjectUtils.equals(expectedXmlAccountingLine, actualXmlAccountingLine));
    }

    private ChartService buildMockChartService() {
        ChartService chartService = mock(ChartService.class);

        Chart expectedChart = createMockChart(ChartFixture.CHART_IT);
        when(chartService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE))
                .thenReturn(expectedChart);

        expectedChart = createMockChart(ChartFixture.CHART_CS);
        when(chartService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_CHART_CODE_CS))
                .thenReturn(expectedChart);

        return chartService;
    }

    private AccountService buildMockAccountService() {
        AccountService accountService = mock(AccountService.class);

        Account mockedAccount = createMockAccount(AwsAccountFixture.ACCOUNT_1658328);
        when(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_1658328))
                .thenReturn(mockedAccount);

        when(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_165833X))
                .thenReturn(null);

        mockedAccount = createMockAccount(AwsAccountFixture.ACCOUNT_R583805);
        when(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_R583805))
                .thenReturn(mockedAccount);

        mockedAccount = createMockAccount(AwsAccountFixture.ACCOUNT_R589966);
        when(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_R589966))
                .thenReturn(mockedAccount);

        mockedAccount = createMockAccount(AwsAccountFixture.ACCOUNT_R589966);
        when(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_1023715))
                .thenReturn(mockedAccount);

        mockedAccount = createMockAccount(AwsAccountFixture.ACCOUNT_CS_J801000);
        when(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_CHART_CODE_CS, CuFPTestConstants.TEST_ACCOUNT_NUMBER_J801000))
                .thenReturn(mockedAccount);

        when(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_J80100X))
                .thenReturn(null);

        when(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_INVALID_STAR_ACCOUNT_STRING))
                .thenReturn(null);

        return accountService;
    }

    private SubAccountService buildMockSubAccountService() {
        SubAccountService subAccountService = mock(SubAccountService.class);

        SubAccount mockedSubAccount = createMockSubAccount(SubAccountFixture.SA_70170);
        when(subAccountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE,
                CuFPTestConstants.TEST_ACCOUNT_NUMBER_R583805,
                CuFPTestConstants.TEST_SUB_ACCOUNT_NUMBER_70170))
                .thenReturn(mockedSubAccount);

        mockedSubAccount = createMockSubAccount(SubAccountFixture.SA_NONCA);
        when(subAccountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE,
                CuFPTestConstants.TEST_ACCOUNT_NUMBER_R589966,
                CuFPTestConstants.TEST_SUB_ACCOUNT_NUMBER_NONCA))
                .thenReturn(mockedSubAccount);

        when(subAccountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE,
                CuFPTestConstants.TEST_ACCOUNT_NUMBER_R589966,
                CuFPTestConstants.TEST_SUB_ACCOUNT_NUMBER_NONCX))
                .thenReturn(null);

        mockedSubAccount = createMockSubAccount(SubAccountFixture.SA_97601);
        when(subAccountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE,
                CuFPTestConstants.TEST_ACCOUNT_NUMBER_1023715,
                CuFPTestConstants.TEST_SUB_ACCOUNT_NUMBER_97601))
                .thenReturn(mockedSubAccount);

        mockedSubAccount = createMockSubAccount(SubAccountFixture.SA_SHAN);
        when(subAccountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_CHART_CODE_CS,
                CuFPTestConstants.TEST_ACCOUNT_NUMBER_J801000,
                CuFPTestConstants.TEST_SUB_ACCOUNT_NUMBER_SHAN))
                .thenReturn(mockedSubAccount);

        when(subAccountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE,
                CuFPTestConstants.TEST_ACCOUNT_NUMBER_R583805,
                CuFPTestConstants.TEST_SUB_ACCOUNT_NUMBER_533X))
                .thenReturn(null);

        return subAccountService;
    }

    private ObjectCodeService buildMockObjectCodeService() {
        ObjectCodeService objectCodeService = mock(ObjectCodeService.class);

        ObjectCode mockedObjectCode = createMockObjectCode(ObjectCodeFixture.OC_IT_6600);
        when(objectCodeService.getByPrimaryIdForCurrentYear(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_AWS_DEFAULT_OBJ_CODE))
                .thenReturn(mockedObjectCode);

        mockedObjectCode = createMockObjectCode(ObjectCodeFixture.OC_IT_4020);
        when(objectCodeService.getByPrimaryIdForCurrentYear(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_OBJ_CODE_4020))
                .thenReturn(mockedObjectCode);

        mockedObjectCode = createMockObjectCode(ObjectCodeFixture.OC_IT_1000);
        when(objectCodeService.getByPrimaryIdForCurrentYear(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_OBJ_CODE_1000))
                .thenReturn(mockedObjectCode);

        mockedObjectCode = createMockObjectCode(ObjectCodeFixture.OC_CS_6600);
        when(objectCodeService.getByPrimaryIdForCurrentYear(CuFPTestConstants.TEST_AWS_BILLING_CHART_CODE_CS, CuFPTestConstants.TEST_AWS_DEFAULT_OBJ_CODE))
                .thenReturn(mockedObjectCode);

        return objectCodeService;
    }

    private SubObjectCodeService buildMockSubObjectCodeService() {
        SubObjectCodeService subObjectCodeService = mock(SubObjectCodeService.class);

        SubObjectCode mockedSubObjectCode = createMockSubObjectCode(SubObjectCodeFixture.SO_109);
        when(subObjectCodeService.getByPrimaryIdForCurrentYear(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_1023715,
                CuFPTestConstants.TEST_OBJ_CODE_4020, CuFPTestConstants.TEST_SUB_OBJ_CODE_109))
                .thenReturn(mockedSubObjectCode);

        when(subObjectCodeService.getByPrimaryIdForCurrentYear(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, CuFPTestConstants.TEST_ACCOUNT_NUMBER_1023715,
                CuFPTestConstants.TEST_OBJ_CODE_4020, CuFPTestConstants.TEST_SUB_OBJ_CODE_10X))
                .thenReturn(null);

        return subObjectCodeService;
    }

    private ProjectCodeService buildMockProjectCodeService() {
        ProjectCodeService projectCodeService = mock(ProjectCodeService.class);

        when(projectCodeService.getByPrimaryId(CuFPTestConstants.TEST_PROJECT_CODE_EB_PLGIFX))
                .thenReturn(null);

        ProjectCode mockedProjectCode = createMockProjectCode(ProjectCodeFixture.PC_EB_PLGIFT);
        when(projectCodeService.getByPrimaryId(CuFPTestConstants.TEST_PROJECT_CODE_EB_PLGIFT))
                .thenReturn(mockedProjectCode);

        return projectCodeService;
    }

    private ParameterService buildMockParameterService() {
        ParameterService parameterService = mock(ParameterService.class);

        when(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPONENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_CHART_CODE_PARAMETER_NAME))
                .thenReturn(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE);
        when(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPONENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_OBJECT_CODE_PARAMETER_NAME))
                .thenReturn(CuFPTestConstants.TEST_AWS_DEFAULT_OBJ_CODE);


        return parameterService;
    }

    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService configurationService = mock(ConfigurationService.class);

        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_INVALID_GROUP_LEVEL_TYPE))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_GROUP_LEVEL_TYPE_INVALID_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CHART_NOT_FOUND))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_CHART_NOT_FOUND_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CHART_INACTIVE))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_CHART_INACTIVE_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_NUMBER_BLANK))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_ACCOUNT_NUMBER_BLANK_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_NOT_FOUND))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_ACCOUNT_NOT_FOUND_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_CLOSED))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_ACCOUNT_CLOSED_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_EXPIRED))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_ACCOUNT_EXPIRED_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_OBJECT_CODE_NOT_FOUND_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_INACTIVE))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_OBJECT_CODE_INACTIVE_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_ACCOUNT_NOT_FOUND))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_SUB_ACCOUNT_NOT_FOUND_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_ACCOUNT_INACTIVE))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_SUB_ACCOUNT_INACTIVE_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_OBJECT_NOT_FOUND))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_SUB_OBJECT_NOT_FOUND_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_OBJECT_INACTIVE))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_SUB_OBJECT_INACTIVE_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_PROJECT_CODE_NOT_FOUND))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_PROJECT_NOT_FOUND_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_PROJECT_CODE_INACTIVE))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_PROJECT_INACTIVE_ERROR_MESSAGE);
        when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ORG_REF_ID_TOO_LONG))
                .thenReturn(CuFPTestConstants.TEST_VALIDATION_OBJ_REF_ID_TOO_LONG_ERROR_MESSAGE);


        return configurationService;
    }

    private static Chart createMockChart(ChartFixture chartFixture) {
        Chart chart = mock(Chart.class);
        when(chart.isActive()).thenReturn(chartFixture.active);

        return chart;
    }

    private static Account createMockAccount(AwsAccountFixture accountFixture) {
        Account account = mock(Account.class);
        when(account.isClosed()).thenReturn(!accountFixture.active);
        when(account.isExpired()).thenReturn(accountFixture.expired);

        return account;
    }

    private static SubAccount createMockSubAccount(SubAccountFixture subAccountFixture) {
        SubAccount subAccount = mock(SubAccount.class);
        when(subAccount.isActive()).thenReturn(subAccountFixture.active);

        return subAccount;
    }

    private static ObjectCode createMockObjectCode(ObjectCodeFixture objectCodeFixture) {
        ObjectCode objectCode = mock(ObjectCode.class);
        when(objectCode.isActive()).thenReturn(objectCodeFixture.active);

        return objectCode;
    }

    private static SubObjectCode createMockSubObjectCode(SubObjectCodeFixture subObjectCodeFixture) {
        SubObjectCode subObjectCode = mock(SubObjectCode.class);
        when(subObjectCode.isActive()).thenReturn(subObjectCodeFixture.active);

        return subObjectCode;
    }

    private static ProjectCode createMockProjectCode(ProjectCodeFixture projectCodeFixture) {
        ProjectCode projectCode = mock(ProjectCode.class);
        when(projectCode.isActive()).thenReturn(projectCodeFixture.active);

        return projectCode;
    }

}
