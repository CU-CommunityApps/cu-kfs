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
    private static final String LABEL_FOR_TESTING_RESULTS = "Expected results should equal actual results: ";

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
    
    @Test
    public void testIndexDocumentTypeIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DOCUMENT_TYPE_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DOCUMENT_TYPE_TEST.expectedResults, actualResults);
    }
    
    @Test
    public void testIndexDescriptionIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DESCRIPTION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DESCRIPTION_TEST.expectedResults, actualResults);
    }
    @Test
    public void testIndexExplanationIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_EXPLANATION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_EXPLANATION_TEST.expectedResults, actualResults);
    }
    @Test
    public void testDocumentTypeDescriptionIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_DOCUMENT_TYPE_DESCRIPTION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_DOCUMENT_TYPE_DESCRIPTION_TEST.expectedResults, actualResults);
    }
    @Test
    public void testDocumentTypeExplanationIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_DOCUMENT_TYPE_EXPLANATION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_DOCUMENT_TYPE_EXPLANATION_TEST.expectedResults, actualResults);
    }
    @Test
    public void testDescriptionExplanationIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_DESCRIPTION_EXPLANATION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_DESCRIPTION_EXPLANATION_TEST.expectedResults, actualResults);
    }
    @Test
    public void testIndexDocumentTypeDescriptionIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DOCUMENT_TYPE_DESCRIPTION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DOCUMENT_TYPE_DESCRIPTION_TEST.expectedResults, actualResults);
    }
    @Test
    public void testIndexDocumentTypeExplanationIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DOCUMENT_TYPE_EXPLANATION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DOCUMENT_TYPE_EXPLANATION_TEST.expectedResults, actualResults);
    }
    @Test
    public void testIndexDescriptionExplanationIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DESCRIPTION_EXPLANATION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DESCRIPTION_EXPLANATION_TEST.expectedResults, actualResults);
    }
    @Test
    public void testDocumentTypeDescriptionExplanationIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_DOCUMENT_TYPE_DESCRIPTION_EXPLANATION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_DOCUMENT_TYPE_DESCRIPTION_EXPLANATION_TEST.expectedResults, actualResults);
    }
    @Test
    public void testIndexDocumentTypeDescriptionExplanationIsInvalid() {
        AccountingXmlDocumentEntry dataToBeTested = AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DOCUMENT_TYPE_DESCRIPTION_EXPLANATION_TEST.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(dataToBeTested, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, AccountingXmlDocumentEntryPojoFixture.BAD_INDEX_DOCUMENT_TYPE_DESCRIPTION_EXPLANATION_TEST.expectedResults, actualResults);
    }

    @Test
    public void testDocumentIsInvalidDueToXmlAdapterErrorOnNumericField() {
        assertResultAndMessageAreCorrectForDocumentWithXmlAdapterValidationErrors(
                AccountingXmlDocumentEntryPojoFixture.BAD_XML_NUMERIC_ERROR_TEST,
                "\n\nError at line 25: Invalid number");
    }

    @Test
    public void testDocumentIsInvalidDueToDescriptiveXmlAdapterErrorOnNumericField() {
        assertResultAndMessageAreCorrectForDocumentWithXmlAdapterValidationErrors(
                AccountingXmlDocumentEntryPojoFixture.BAD_XML_DESCRIPTIVE_NUMERIC_ERROR_TEST,
                "\n\nError at line 25: Commas are not allowed in numbers");
    }

    @Test
    public void testDocumentIsInvalidDueToXmlAdapterErrorOnDateField() {
        assertResultAndMessageAreCorrectForDocumentWithXmlAdapterValidationErrors(
                AccountingXmlDocumentEntryPojoFixture.BAD_XML_DATE_ERROR_TEST,
                "\n\nError at line 66: Cannot parse \"02/31/2018\": Value 31 for dayOfMonth must be in the range [1,28]");
    }

    @Test
    public void testDocumentIsInvalidDueToXmlAdapterErrorWithoutDescriptiveMessage() {
        assertResultAndMessageAreCorrectForDocumentWithXmlAdapterValidationErrors(
                AccountingXmlDocumentEntryPojoFixture.BAD_XML_GENERIC_ERROR_TEST,
                "\n\nError at line 4: Unexpected XML processing error");
    }

    @Test
    public void testDocumentIsInvalidDueToXmlAdapterErrorWithoutClassnameInMessage() {
        assertResultAndMessageAreCorrectForDocumentWithXmlAdapterValidationErrors(
                AccountingXmlDocumentEntryPojoFixture.BAD_XML_ERROR_MESSAGE_WITHOUT_CLASSNAME_TEST,
                "\n\nError at line 103: Account number does not reference an actual KFS account");
    }

    @Test
    public void testDocumentIsInvalidDueToXmlAdapterErrorWithoutLineNumberReference() {
        assertResultAndMessageAreCorrectForDocumentWithXmlAdapterValidationErrors(
                AccountingXmlDocumentEntryPojoFixture.BAD_XML_ERROR_WITHOUT_LINE_NUMBER_TEST,
                "\n\nCommas are not allowed in numbers");
    }

    @Test
    public void testDocumentIsInvalidDueToXmlAdapterErrorWithoutLineNumberOrMessage() {
        assertResultAndMessageAreCorrectForDocumentWithXmlAdapterValidationErrors(
                AccountingXmlDocumentEntryPojoFixture.BAD_XML_ERROR_WITHOUT_LINE_OR_MESSAGE_TEST,
                "\n\nUnexpected XML processing error");
    }

    @Test
    public void testDocumentIsInvalidDueToMultipleXmlAdapterErrors() {
        assertResultAndMessageAreCorrectForDocumentWithXmlAdapterValidationErrors(
                AccountingXmlDocumentEntryPojoFixture.BAD_XML_MULTIPLE_ADAPTER_ERRORS_TEST,
                "\n\nError at line 25: Invalid number"
                        + "\n\nError at line 66: Cannot parse \"02/31/2018\": Value 31 for dayOfMonth must be in the range [1,28]");
    }

    @Test
    public void testDocumentIsInvalidDueToXmlAdapterErrorAndRequiredDataError() {
        assertResultAndMessageAreCorrectForDocumentWithXmlAdapterValidationErrors(
                AccountingXmlDocumentEntryPojoFixture.BAD_XML_ADAPTER_AND_REQUIRED_DATA_ERRORS_TEST,
                "\n\nDetected null or blank data for element: Explanation"
                        + "\n\nError at line 25: Commas are not allowed in numbers");
    }

    private void assertResultAndMessageAreCorrectForDocumentWithXmlAdapterValidationErrors(
            AccountingXmlDocumentEntryPojoFixture fixture, String expectedMessage) {
        AccountingXmlDocumentEntry documentEntry = fixture.toDocumentEntryPojo();
        CreateAccountingDocumentReportItemDetail reportItemDetail = new CreateAccountingDocumentReportItemDetail();
        boolean actualResults = createAccountingDocumentValidationService.isAllRequiredDataValid(
                documentEntry, reportItemDetail);
        assertEquals(LABEL_FOR_TESTING_RESULTS, fixture.expectedResults, actualResults);
        assertEquals("Wrong error message for result", expectedMessage, reportItemDetail.getErrorMessage());
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
            Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_GENERIC_ERROR))
                    .thenReturn(CuFPTestConstants.GENERIC_ERROR_MESSAGE);
            Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_GENERIC_NUMERIC_ERROR))
                    .thenReturn(CuFPTestConstants.GENERIC_NUMERIC_ERROR_MESSAGE);
            Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_XML_ADAPTER_ERROR))
                    .thenReturn(CuFPTestConstants.XML_ADAPTER_ERROR_MESSAGE);
            return configurationService;
        }
    }
    

}
