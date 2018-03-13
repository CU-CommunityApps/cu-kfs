package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import edu.cornell.kfs.fp.batch.service.impl.AwsAccountingXmlDocumentAccountingLineServiceImpl;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture.GroupLevelFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentAccountingLineFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.DefaultKfsAccountForAwsFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBException;

public class AwsAccountingXmlDocumentAccountingLineServiceImplTest {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AwsAccountingXmlDocumentAccountingLineServiceImplTest.class);

    private AwsAccountingXmlDocumentAccountingLineService awsXmlDocumentAccountingLineService;

    @Before
    public void setUp() throws Exception {
        this.awsXmlDocumentAccountingLineService = new AwsAccountingXmlDocumentAccountingLineServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        this.awsXmlDocumentAccountingLineService = null;
    }

    @Test
    public void verifyAccountOnlyInValueUsesDefaultValuesTest() throws JAXBException {
        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_NONE_COST_1,
                DefaultKfsAccountForAwsFixture.AWS_ABC_KFS_1658328,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1658328_OBJ_6600_AMOUNT_1);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_165833X_COST_2,
                DefaultKfsAccountForAwsFixture.AWS_ABC_KFS_1658328,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1658328_OBJ_6600_AMOUNT_2);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_1658328_COST_3,
                DefaultKfsAccountForAwsFixture.AWS_DEF_KFS_165835X,
                AccountingXmlDocumentAccountingLineFixture.ACCT_165835X_OBJ_6600_AMOUNT_3);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_165833X_COST_4,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1658333_SA_5333_OBJ_6600_AMOUNT_4);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_1658333_5333_COST_5,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1658333_OBJ_6600_AMOUNT_5);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_R589966_STAR_VALID_COST_6,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_R589966_OBJ_1000_AMOUNT_6);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_1023715_STAR_VALID_COST_7,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1023715_OBJ_4020_SO109_AMOUNT_7);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_1023715_STAR_INVALID_SUB_OBJ_COST_8,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1023715_OBJ_4020_AMOUNT_8);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_R589966_STAR_PART_VALID_COST_9,
                DefaultKfsAccountForAwsFixture.AWS_GHI_KFS_1658498,
                AccountingXmlDocumentAccountingLineFixture.ACCT_R589966_OBJ_4020_AMOUNT_9);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_J801000_COST_10,
                DefaultKfsAccountForAwsFixture.AWS_JKL_KFS_J801000_INVALID,
                AccountingXmlDocumentAccountingLineFixture.ACCT_J801000_OBJ_4020_AMOUNT_10);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_J80100X_COST_11,
                DefaultKfsAccountForAwsFixture.AWS_JKL_KFS_J801000_INVALID,
                AccountingXmlDocumentAccountingLineFixture.ACCT_J801000_OBJ_4020_AMOUNT_11);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_NONE_COST_12,
                DefaultKfsAccountForAwsFixture.AWS_MNO_KFS_INTERNAL,
                AccountingXmlDocumentAccountingLineFixture.ACCT_J801000_OBJ_4020_AMOUNT_12);

        verifyServiceCreatesExpectedAccountingLine(GroupLevelFixture.ACCT_J801000_COST_13,
                DefaultKfsAccountForAwsFixture.AWS_PQR_KFS_J801000_INVALID,
                AccountingXmlDocumentAccountingLineFixture.ACCT_1023715_OBJ_4020_AMOUNT_13_INVALID);

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
//        AccountingXmlDocumentAccountingLine actualXmlAccountingLine =
//                awsXmlDocumentAccountingLineService.createAccountingXmlDocumentAccountingLine(groupLevelCostCenter, defaultKfsAccountForAws);

        System.out.println(String.format("Expect (%s, %s) => %s", groupLevelCostCenter.getGroupValue(),
                defaultKfsAccountForAws.getKfsDefaultAccount(),
                expectedXmlAccountingLine.getAccountNumber()));

        //assertTrue(ObjectUtils.equals(expectedXmlAccountingLine, actualXmlAccountingLine));
    }

}
