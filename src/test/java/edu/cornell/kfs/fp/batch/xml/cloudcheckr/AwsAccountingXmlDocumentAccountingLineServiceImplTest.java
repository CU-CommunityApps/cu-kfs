package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import org.apache.commons.lang.ObjectUtils;
import org.junit.After;
import org.junit.Before;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture.GroupLevelFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentAccountingLineFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.DefaultKfsAccountForAwsFixture;

@ConfigureContext(session = org.kuali.kfs.sys.fixture.UserNameFixture.ccs1)
public class AwsAccountingXmlDocumentAccountingLineServiceImplTest extends KualiTestBase {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AwsAccountingXmlDocumentAccountingLineServiceImplTest.class);

    private AwsAccountingXmlDocumentAccountingLineService awsAccountingXmlDocumentAccountingLineService;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.awsAccountingXmlDocumentAccountingLineService = SpringContext.getBean(AwsAccountingXmlDocumentAccountingLineService.class);
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

    public void testVerifyInvalidStarDelimitedAccountStringUsesValidDefaultStarDelimitedAccountString() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_J801000_COST_13,        //todo: confirm requirements
        DefaultKfsAccountForAwsFixture.AWS_PQR_KFS_J801000,
        AccountingXmlDocumentAccountingLineFixture.ACCT_J801000_OBJ_6600_AMOUNT_13_INVALID);
    }

    public void testVerifyInvalidStarDelimitedAccountStringUsesInvalidDefaultAsAccount() {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_1023715_COST_14,
                DefaultKfsAccountForAwsFixture.AWS_STU_KFS_1023715_INVALID,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1023715_OBJ_4020_AMOUNT_14_INVALID);
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
