package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.mockito.Mockito;

import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItemDetail;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentEntry;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperPojoFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentEntryPojoFixture;

public class CreateAccountingDocumentValidationServiceImplTest {

    private TestCreateAccountingDocumentValidationServiceImpl createAccountingDocumentValidationService;
    private static String LABEL_FOR_TESTING_RESULTS = "Expected results should equal actual results: ";

    @Before
    public void setUp() throws Exception {
        createAccountingDocumentValidationService = new TestCreateAccountingDocumentValidationServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGoodHeaderEmailIsValid() {
        AccountingXmlDocumentListWrapper dataToBeTested = AccountingXmlDocumentListWrapperPojoFixture.GOOD_HEADER_DATA.toAccountingXmlDocumentListWrappePojo();
        CreateAccountingDocumentReportItem reportItem = new CreateAccountingDocumentReportItem("testGoodHeaderEmailIsValid");
        boolean actualResults = createAccountingDocumentValidationService.isValidXmlFileHeaderData(dataToBeTested, reportItem);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentListWrapperPojoFixture.GOOD_HEADER_DATA.expectedResults, actualResults);
    }
    
    @Test
    public void testBadHeaderEmailIsInvalid() {
        AccountingXmlDocumentListWrapper dataToBeTested = AccountingXmlDocumentListWrapperPojoFixture.BAD_EMAIL_IN_HEADER.toAccountingXmlDocumentListWrappePojo();
        CreateAccountingDocumentReportItem reportItem = new CreateAccountingDocumentReportItem("testBadHeaderEmailIsInvalid");
        boolean actualResults = createAccountingDocumentValidationService.isValidXmlFileHeaderData(dataToBeTested, reportItem);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentListWrapperPojoFixture.BAD_EMAIL_IN_HEADER.expectedResults, actualResults);
    }
    
    @Test
    public void testBadHeaderOverviewIsInvalid() {
        AccountingXmlDocumentListWrapper dataToBeTested = AccountingXmlDocumentListWrapperPojoFixture.BAD_OVERVIEW_IN_HEADER.toAccountingXmlDocumentListWrappePojo();
        CreateAccountingDocumentReportItem reportItem = new CreateAccountingDocumentReportItem("testBadHeaderOverviewIsInvalid");
        boolean actualResults = createAccountingDocumentValidationService.isValidXmlFileHeaderData(dataToBeTested, reportItem);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentListWrapperPojoFixture.BAD_OVERVIEW_IN_HEADER.expectedResults, actualResults);
    }
    
    @Test
    public void testBadHeaderEmailOverviewIsInvalid() {
        AccountingXmlDocumentListWrapper dataToBeTested = AccountingXmlDocumentListWrapperPojoFixture.BAD_EMAIL_OVERVIEW_IN_HEADER.toAccountingXmlDocumentListWrappePojo();
        CreateAccountingDocumentReportItem reportItem = new CreateAccountingDocumentReportItem("testBadHeaderEmailOverviewIsInvalid");
        boolean actualResults = createAccountingDocumentValidationService.isValidXmlFileHeaderData(dataToBeTested, reportItem);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentListWrapperPojoFixture.BAD_EMAIL_OVERVIEW_IN_HEADER.expectedResults, actualResults);
    }
    
    @Test
    public void testAllRequiredDocumentDataIsValid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.GOOD_DATA_ALL_ITEMS.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.GOOD_DATA_ALL_ITEMS.expectedResults, actualResults);
    }
    
    @Test
    public void testIndexDocumentDataIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_TEST.expectedResults, actualResults);
    }
    
    @Test
    public void testDocumentTypeIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_DOCUMENT_TYPE_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_DOCUMENT_TYPE_TEST.expectedResults, actualResults);
    }
    
    @Test
    public void testDocumentExplanationIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_EXPLANATION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_EXPLANATION_TEST.expectedResults, actualResults);
    }
    
    @Test
    public void testDocumentDescriptionIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_DESCRIPTION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_DESCRIPTION_TEST.expectedResults, actualResults);
    }

    private static class TestCreateAccountingDocumentValidationServiceImpl extends CreateAccountingDocumentValidationServiceImpl {
        public TestCreateAccountingDocumentValidationServiceImpl() {
            this.configurationService = buildMockConfigurationService();
        }
        
        private ConfigurationService buildMockConfigurationService() {
            ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
            Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_INVALID_DATA_FOR_ELEMENT))
                .thenReturn(CuFPTestConstants.TEST_CREATE_ACCOUNT_DOCUMENT_INVALID_DATA);
            Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.REPORT_CREATE_ACCOUNTING_DOCUMENT_NULL_BLANK_DATA_ELEMENT))
                .thenReturn(CuFPTestConstants.TEST_CREATE_ACCOUNT_DOCUMENT_NULL_BLANK_DATA);
            return configurationService;
        }
    }
    

}
