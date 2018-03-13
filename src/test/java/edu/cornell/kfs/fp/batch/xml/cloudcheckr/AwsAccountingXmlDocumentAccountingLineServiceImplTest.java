package edu.cornell.kfs.fp.batch.xml.cloudcheckr;

import edu.cornell.kfs.fp.batch.service.impl.AwsAccountingXmlDocumentAccountingLineServiceImpl;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;
import edu.cornell.kfs.fp.batch.service.AwsAccountingXmlDocumentAccountingLineService;
import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.List;

import edu.cornell.kfs.sys.service.CUMarshalService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;

public class AwsAccountingXmlDocumentAccountingLineServiceImplTest {

    private static final String BASE_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml/cloudcheckr/";
    private static final String BASIC_CORNELL_TEST = "cloudcheckr_basic_cornell_test.xml";
    private static final String NONE_DEFAULT_TEST_FILENAME = "cloudcheckr_none_example_test.xml";
    private static final String ACCOUNT_ONLY_DEFAULT_TEST_FILENAME = "cloudcheckr_account_only_example_test.xml";
    private static final String ACCOUNT_DASH_SUBACCOUNT_TEST_FILENAME = "cloudcheckr_account_dash_subaccount_example_test.xml";
    private static final String ACCOUNT_STAR_DELIMITED_MIN_TEST_FILENAME = "cloudcheckr_account_star_delimited_min_example_test.xml";
    private static final String ACCOUNT_STAR_DELIMITED_MAX_TEST_FILENAME = "cloudcheckr_account_star_delimited_max_example_test.xml";

    private CUMarshalService marshalService;
    private AwsAccountingXmlDocumentAccountingLineService awsXmlDocumentAccountingLineService;

    @Before
    public void setUp() throws Exception {
        this.marshalService = new CUMarshalServiceImpl();
        this.awsXmlDocumentAccountingLineService = new AwsAccountingXmlDocumentAccountingLineServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        this.marshalService = null;
        this.awsXmlDocumentAccountingLineService = null;
    }

    @Test
    public void verifyAccountOnlyInValueUsesDefaultValuesTest() throws JAXBException {
        File cornellBasicTestFile = new File(BASE_TEST_FILE_PATH + ACCOUNT_ONLY_DEFAULT_TEST_FILENAME);
        CloudCheckrWrapper actualCloudCheckerWrapper = marshalService.unmarshalFile(cornellBasicTestFile, CloudCheckrWrapper.class);
        List<GroupLevel> costsByGroupList = actualCloudCheckerWrapper.getCostsByGroup();
        assertTrue(costsByGroupList.size() == 1);
        GroupLevel groupLevelAccount = costsByGroupList.get(0);
        assertTrue(StringUtils.equals("Account", groupLevelAccount.getGroupName()));
        List<GroupLevel> groupLevelCostCenterList = groupLevelAccount.getNextLevel();
        assertTrue(groupLevelCostCenterList.size() == 4);
        GroupLevel groupLevelCostCenter = groupLevelCostCenterList.get(0);
        assertTrue(StringUtils.equals("Cost Center", groupLevelCostCenter.getGroupName()));

        AccountingXmlDocumentAccountingLine xmlAccountingLine =
                awsXmlDocumentAccountingLineService.createAccountingXmlDocumentAccountingLine(groupLevelCostCenter, null);

        assertTrue(StringUtils.equals(xmlAccountingLine.getChartCode(), "IT"));
        assertTrue(StringUtils.equals(xmlAccountingLine.getObjectCode(), "6600"));
        assertTrue(StringUtils.equals(xmlAccountingLine.getAccountNumber(), "U353901"));
    }
}
