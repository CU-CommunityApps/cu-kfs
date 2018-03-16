package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import edu.cornell.kfs.coa.fixture.ChartFixture;
import edu.cornell.kfs.coa.fixture.SubObjectCodeFixture;
import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;
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
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.SubObjectCode;
import org.kuali.kfs.coa.service.ChartService;
import org.kuali.kfs.coa.service.SubObjectCodeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;

@ConfigureContext(session = org.kuali.kfs.sys.fixture.UserNameFixture.ccs1)
public class AwsAccountingXmlDocumentAccountingLineServiceImplTest extends KualiTestBase {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AwsAccountingXmlDocumentAccountingLineServiceImplTest.class);

    private AwsAccountingXmlDocumentAccountingLineServiceImpl awsAccountingXmlDocumentAccountingLineService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.awsAccountingXmlDocumentAccountingLineService = (AwsAccountingXmlDocumentAccountingLineServiceImpl) SpringContext.getBean(AwsAccountingXmlDocumentAccountingLineService.class);
        this.awsAccountingXmlDocumentAccountingLineService.setConfigurationService(buildMockConfigurationService());
        this.awsAccountingXmlDocumentAccountingLineService.setParameterService(buildMockParameterService());
        this.awsAccountingXmlDocumentAccountingLineService.setChartService(buildMockChartService());
        this.awsAccountingXmlDocumentAccountingLineService.setSubObjectCodeService(buildMockSubObjectCodeService());
    }


    private ChartService buildMockChartService() {
        ChartService chartService = EasyMock.createMock(ChartService.class);

        Chart chartIt = createMockChart(ChartFixture.CHART_IT);
        EasyMock.expect(chartService.getByPrimaryId(CuFPTestConstants.TEST_VALIDATION_AWS_BILLING_DEFAULT_CHART_CODE))
                .andStubReturn(chartIt);

        Chart chartCs = createMockChart(ChartFixture.CHART_CS);
        EasyMock.expect(chartService.getByPrimaryId(CuFPTestConstants.TEST_VALIDATION_AWS_BILLING_CHART_CODE_CS))
                .andStubReturn(chartCs);

        EasyMock.replay(chartService);
        return chartService;
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

    public static Chart createMockChart(ChartFixture chartFixture) {
        Chart chart = EasyMock.createMock(Chart.class);
        EasyMock.expect(chart.isActive()).andStubReturn(chartFixture.active);
        EasyMock.replay(chart);
        return chart;
    }

    public static SubObjectCode createMockSubObjectCode(SubObjectCodeFixture subObjectCodeFixture) {
        SubObjectCode subObjectCode = EasyMock.createMock(SubObjectCode.class);
        EasyMock.expect(subObjectCode.isActive()).andStubReturn(subObjectCodeFixture.active);
        EasyMock.replay(subObjectCode);
        return subObjectCode;
    }

    private ParameterService buildMockParameterService() {
        ParameterService parameterService = EasyMock.createMock(ParameterService.class);

        EasyMock.expect(parameterService.getParameterValueAsString(KFSConstants.CoreModuleNamespaces.FINANCIAL,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_COMPENT_NAME,
                CuFPConstants.AmazonWebServiceBillingConstants.AWS_CHART_CODE_PROPERTY_NAME))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_AWS_BILLING_DEFAULT_CHART_CODE);
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
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_ACCOUNT_INACTIVE))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_ACCOUNT_INACTIVE_ERROR_MESSAGE);
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
        super.tearDown();
    }

    public void testVerifyNoneUsesDefaultAccountString() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_NONE_COST_1,
                DefaultKfsAccountForAwsFixture.AWS_ABC_KFS_1658328,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1658328_OBJ_6600_AMOUNT_1);
    }

    public void testVerifyInvalidAccountUsesDefaultAccountString() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_165833X_COST_2,
                DefaultKfsAccountForAwsFixture.AWS_ABC_KFS_1658328,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1658328_OBJ_6600_AMOUNT_2);
    }

    public void testVerifyInvalidAccountUsesDefaultInvalidAccountString() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_165833X_COST_3,
                DefaultKfsAccountForAwsFixture.AWS_DEF_KFS_165835X,
                AccountingXmlDocumentAccountingLineFixture.ACCT_165835X_OBJ_6600_AMOUNT_3);
    }

    public void testVerifyValidAccountHyphenSubAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_R583805_70710_COST_4,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_R583805_SA_70170_OBJ_6600_AMOUNT_4);
    }

    public void testVerifyValidAccountHyphenInvalidSubAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_R583805_533X_COST_5,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_R583805_OBJ_6600_AMOUNT_5);
    }

    public void testVerifyValidStarDelimitedAccountStringWithProject() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_R589966_STAR_VALID_COST_6,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_R589966_OBJ_1000_AMOUNT_6);
    }

    public void testVerifyValidStarDelimitedAccountStringWithSubObject() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_1023715_STAR_VALID_COST_7,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1023715_OBJ_4020_SO109_AMOUNT_7);
    }

    public void testVerifyValidStarDelimitedAccountStringWithInvalidSubObject() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_1023715_STAR_INVALID_SUB_OBJ_COST_8,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1023715_OBJ_4020_AMOUNT_8);
    }

    public void testVerifyValidStarDelimitedAccountStringWithInvalidSubAccountProject() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_R589966_STAR_PART_VALID_COST_9,
            DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
            AccountingXmlDocumentAccountingLineFixture.ACCT_R589966_OBJ_1000_AMOUNT_9);
    }

    public void testVerifyValidStarDelimitedAccountStringWithValidSubAccount(){
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_J801000_COST_10,
                DefaultKfsAccountForAwsFixture.AWS_JKL_KFS_J801000,
                AccountingXmlDocumentAccountingLineFixture.ACCT_J801000_SA_SHAN_OBJ_6600_AMOUNT_10);
    }

    public void testVerifyInvalidAccountNumberUsesValidStarDelimitedDefaultAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_J80100X_COST_11,
                DefaultKfsAccountForAwsFixture.AWS_JKL_KFS_J801000,
                AccountingXmlDocumentAccountingLineFixture.ACCT_J801000_OBJ_6600_AMOUNT_11);
    }

    public void testVerifyInternalDefaultAccountUsesCostCenterValueAsAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_NONE_COST_12,
                DefaultKfsAccountForAwsFixture.AWS_MNO_KFS_INTERNAL,
                AccountingXmlDocumentAccountingLineFixture.ACCT_NONE_OBJ_6600_AMOUNT_12);
    }

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

        assertTrue(ObjectUtils.equals(expectedXmlAccountingLine, actualXmlAccountingLine));
    }

}
