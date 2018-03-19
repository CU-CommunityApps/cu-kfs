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
import org.easymock.EasyMock;
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

public class AwsAccountingXmlDocumentAccountingLineServiceImplTest {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AwsAccountingXmlDocumentAccountingLineServiceImplTest.class);

    private AwsAccountingXmlDocumentAccountingLineServiceImpl awsAccountingXmlDocumentAccountingLineService;

    @Before
    public void setUp() throws Exception {
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

    private ChartService buildMockChartService() {
        ChartService chartService = EasyMock.createMock(ChartService.class);

        EasyMock.expect(chartService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE))
                .andStubReturn(createMockChart(ChartFixture.CHART_IT));

        EasyMock.expect(chartService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_CHART_CODE_CS))
                .andStubReturn(createMockChart(ChartFixture.CHART_CS));

        EasyMock.replay(chartService);
        return chartService;
    }

    private AccountService buildMockAccountService() {
        AccountService accountService = EasyMock.createMock(AccountService.class);

        EasyMock.expect(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, "1658328"))
                .andStubReturn(createMockAccount(AwsAccountFixture.ACCOUNT_1658328));

        EasyMock.expect(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, "165833X"))
                .andStubReturn(null);

        EasyMock.expect(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, "R583805"))
                .andStubReturn(createMockAccount(AwsAccountFixture.ACCOUNT_R583805));

        EasyMock.expect(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, "R589966"))
                .andStubReturn(createMockAccount(AwsAccountFixture.ACCOUNT_R589966));

        EasyMock.expect(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, "1023715"))
                .andStubReturn(createMockAccount(AwsAccountFixture.ACCOUNT_1023715));

        EasyMock.expect(accountService.getByPrimaryId("CS", "J801000"))
                .andStubReturn(createMockAccount(AwsAccountFixture.ACCOUNT_CS_J801000));

        EasyMock.expect(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, "J80100X"))
                .andStubReturn(null);

        EasyMock.expect(accountService.getByPrimaryId(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE, "IT*1023715*97601*4020*109**AEH56*foo"))
                .andStubReturn(null);

        EasyMock.replay(accountService);
        return accountService;
    }

    private SubAccountService buildMockSubAccountService() {
        SubAccountService subAccountService = EasyMock.createMock(SubAccountService.class);

        EasyMock.expect(subAccountService.getByPrimaryId("IT", "R583805", "70170"))
                .andStubReturn(createMockSubAccount(SubAccountFixture.SA_70170));

        EasyMock.expect(subAccountService.getByPrimaryId("IT", "R589966", "NONCA"))
                .andStubReturn(createMockSubAccount(SubAccountFixture.SA_NONCA));

        EasyMock.expect(subAccountService.getByPrimaryId("IT", "R589966", "NONCX"))
                .andStubReturn(null);

        EasyMock.expect(subAccountService.getByPrimaryId("IT", "1023715", "97601"))
                .andStubReturn(createMockSubAccount(SubAccountFixture.SA_97601));

        EasyMock.expect(subAccountService.getByPrimaryId("CS", "J801000", "SHAN"))
                .andStubReturn(createMockSubAccount(SubAccountFixture.SA_SHAN));

        EasyMock.expect(subAccountService.getByPrimaryId("IT", "R583805", "533X"))
                .andStubReturn(null);

        EasyMock.replay(subAccountService);
        return subAccountService;
    }

    private ObjectCodeService buildMockObjectCodeService() {
        ObjectCodeService objectCodeService = EasyMock.createMock(ObjectCodeService.class);

        EasyMock.expect(objectCodeService.getByPrimaryIdForCurrentYear("IT", "6600"))
                .andStubReturn(createMockObjectCode(ObjectCodeFixture.OC_IT_6600));

        EasyMock.expect(objectCodeService.getByPrimaryIdForCurrentYear("IT", "4020"))
                .andStubReturn(createMockObjectCode(ObjectCodeFixture.OC_IT_4020));

        EasyMock.expect(objectCodeService.getByPrimaryIdForCurrentYear("IT", "1000"))
                .andStubReturn(createMockObjectCode(ObjectCodeFixture.OC_IT_1000));

        EasyMock.expect(objectCodeService.getByPrimaryIdForCurrentYear("CS", "6600"))
                .andStubReturn(createMockObjectCode(ObjectCodeFixture.OC_CS_6600));

        EasyMock.replay(objectCodeService);
        return objectCodeService;
    }

    private SubObjectCodeService buildMockSubObjectCodeService() {
        SubObjectCodeService subObjectCodeService = EasyMock.createMock(SubObjectCodeService.class);

        SubObjectCode subObjectCode = createMockSubObjectCode(SubObjectCodeFixture.SO_109);
        EasyMock.expect(subObjectCodeService.getByPrimaryIdForCurrentYear("IT", "1023715", "4020", "109"))
                .andStubReturn(subObjectCode);

        EasyMock.expect(subObjectCodeService.getByPrimaryIdForCurrentYear("IT", "1023715", "4020", "10X"))
                .andStubReturn(null);

        EasyMock.replay(subObjectCodeService);
        return subObjectCodeService;
    }

    private ProjectCodeService buildMockProjectCodeService() {
        ProjectCodeService projectCodeService = EasyMock.createMock(ProjectCodeService.class);

        EasyMock.expect(projectCodeService.getByPrimaryId("EB-PLGIFX"))
                .andStubReturn(null);

        EasyMock.expect(projectCodeService.getByPrimaryId("EB-PLGIFT"))
                .andStubReturn(createMockProjectCode(ProjectCodeFixture.PC_EB_PLGIFT));

        EasyMock.replay(projectCodeService);
        return projectCodeService;
    }

    public static Chart createMockChart(ChartFixture chartFixture) {
        Chart chart = EasyMock.createMock(Chart.class);
        EasyMock.expect(chart.isActive()).andStubReturn(chartFixture.active);
        EasyMock.replay(chart);
        return chart;
    }

    public static Account createMockAccount(AwsAccountFixture accountFixture) {
        Account account = EasyMock.createMock(Account.class);
        EasyMock.expect(account.isClosed()).andStubReturn(!accountFixture.active);
        EasyMock.expect(account.isExpired()).andStubReturn(accountFixture.expired);
        EasyMock.replay(account);
        return account;
    }

    public static SubAccount createMockSubAccount(SubAccountFixture subAccountFixture) {
        SubAccount subAccount = EasyMock.createMock(SubAccount.class);
        EasyMock.expect(subAccount.isActive()).andStubReturn(subAccountFixture.active);
        EasyMock.replay(subAccount);
        return subAccount;
    }

    public static ObjectCode createMockObjectCode(ObjectCodeFixture objectCodeFixture) {
        ObjectCode objectCode = EasyMock.createMock(ObjectCode.class);
        EasyMock.expect(objectCode.isActive()).andStubReturn(objectCodeFixture.active);
        EasyMock.replay(objectCode);
        return objectCode;
    }

    public static SubObjectCode createMockSubObjectCode(SubObjectCodeFixture subObjectCodeFixture) {
        SubObjectCode subObjectCode = EasyMock.createMock(SubObjectCode.class);
        EasyMock.expect(subObjectCode.isActive()).andStubReturn(subObjectCodeFixture.active);
        EasyMock.replay(subObjectCode);
        return subObjectCode;
    }

    public static ProjectCode createMockProjectCode(ProjectCodeFixture projectCodeFixture) {
        ProjectCode projectCode = EasyMock.createMock(ProjectCode.class);
        EasyMock.expect(projectCode.isActive()).andStubReturn(projectCodeFixture.active);
        EasyMock.replay(projectCode);
        return projectCode;
    }

    private ParameterService buildMockParameterService() {
        ParameterService parameterService = EasyMock.createMock(ParameterService.class);

        EasyMock.expect(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_CHART_CODE_PROPERTY_NAME))
                .andStubReturn(CuFPTestConstants.TEST_AWS_BILLING_DEFAULT_CHART_CODE);
        EasyMock.expect(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_OBJECT_CODE_PROPERTY_NAME))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_AWS_BILLING_DEFAULT_OBJECT_CODE);

        EasyMock.replay(parameterService);
        return parameterService;
    }

    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService configurationService = EasyMock.createMock(ConfigurationService.class);

        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_INVALID_GROUP_LEVEL_TYPE))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_GROUP_LEVEL_TYPE_INVALID_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CHART_NOT_FOUND))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_CHART_NOT_FOUND_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CHART_INACTIVE))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_CHART_INACTIVE_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_NUMBER_BLANK))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_ACCOUNT_NUMBER_BLANK_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_NOT_FOUND))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_ACCOUNT_NOT_FOUND_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_CLOSED))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_ACCOUNT_CLOSED_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_EXPIRED))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_ACCOUNT_EXPIRED_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_NOT_FOUND))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_OBJECT_CODE_NOT_FOUND_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_OBJECT_CODE_INACTIVE))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_OBJECT_CODE_INACTIVE_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_ACCOUNT_NOT_FOUND))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_SUB_ACCOUNT_NOT_FOUND_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_ACCOUNT_INACTIVE))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_SUB_ACCOUNT_INACTIVE_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_OBJECT_NOT_FOUND))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_SUB_OBJECT_NOT_FOUND_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_SUB_OBJECT_INACTIVE))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_SUB_OBJECT_INACTIVE_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_PROJECT_CODE_NOT_FOUND))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_PROJECT_NOT_FOUND_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_PROJECT_CODE_INACTIVE))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_PROJECT_INACTIVE_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ORG_REF_ID_TOO_LONG))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_OBJ_REF_ID_TOO_LONG_ERROR_MESSAGE);

        EasyMock.replay(configurationService);
        return configurationService;
    }

    @After
    public void tearDown() throws Exception {
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
    public void testVerifyValidStarDelimitedAccountStringWithValidSubAccount(){
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
                AccountingXmlDocumentAccountingLineFixture.ACCT_NONE_OBJ_6600_AMOUNT_12);
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

}
