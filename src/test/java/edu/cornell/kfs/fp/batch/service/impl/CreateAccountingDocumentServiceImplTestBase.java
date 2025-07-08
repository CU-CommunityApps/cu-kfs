package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.runner.RunWith;
import org.kuali.kfs.coa.service.AccountingPeriodService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.resourceloader.ResourceLoaderException;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.document.PreEncumbranceDocument;
import org.kuali.kfs.fp.service.FiscalYearFunctionControlService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.CuFPTestConstants.TestEmails;
import edu.cornell.kfs.fp.batch.AccountingXmlDocumentInputFileType;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.AccountingXmlDocumentDownloadAttachmentService;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentReportService;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentValidationService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentClassMappingUtils;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentEntryFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;
import edu.cornell.kfs.fp.businessobject.CreateAccountingDocumentFileEntry;
import edu.cornell.kfs.fp.document.CuDistributionOfIncomeAndExpenseDocument;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherDefaultDueDateService;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherPayeeService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.businessobject.WebServiceCredential;
import edu.cornell.kfs.sys.businessobject.fixture.WebServiceCredentialFixture;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.util.GlobalResourceLoaderUtils;
import edu.cornell.kfs.sys.util.MockDocumentUtils;
import edu.cornell.kfs.sys.util.MockDocumentUtils.TestAdHocRoutePerson;
import edu.cornell.kfs.sys.util.MockDocumentUtils.TestNote;
import edu.cornell.kfs.sys.util.MockPersonUtil;
import edu.cornell.kfs.sys.util.fixture.TestUserFixture;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

@RunWith(MockitoJUnitRunner.class)
public abstract class CreateAccountingDocumentServiceImplTestBase {

    private static final String SOURCE_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml";
    private static final String TARGET_TEST_FILE_PATH = "test/fp/accountingXmlDocument";
    private static final String FULL_FILE_PATH_FORMAT = "%s/%s%s";
    private static final int DOCUMENT_NUMBER_START = 1000;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HH_mm_ss_S, Locale.US);

    protected TestCreateAccountingDocumentServiceImpl createAccountingDocumentService;
    private List<AccountingDocument> routedAccountingDocuments;
    private List<String> creationOrderedBaseFileNames;
    private Map<String, CreateAccountingDocumentFileEntry> fileEntries;
    private long nextFileEntryId = 1000L;
    
    protected ConfigurationService configurationService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;

    public void setUp() throws Exception {
        configurationService = buildMockConfigurationService();
        dateTimeService = buildMockDateTimeService();
        parameterService = buildParameterService();
        routedAccountingDocuments = new ArrayList<>();
        creationOrderedBaseFileNames = new ArrayList<>();
        fileEntries = new HashMap<>();
        createTargetTestDirectory();
    }
    
    public void setupBasicCreateAccountingDocumentServices() throws Exception {
        createAccountingDocumentService.setAccountingDocumentBatchInputFileType(buildAccountingXmlDocumentInputFileType(dateTimeService));
        createAccountingDocumentService.setBatchInputFileService(new BatchInputFileServiceImpl());
        createAccountingDocumentService.setFileStorageService(buildFileStorageService());
        createAccountingDocumentService.setConfigurationService(configurationService);
        createAccountingDocumentService.setDocumentService(buildMockDocumentService());
        createAccountingDocumentService.setCreateAccountingDocumentReportService(new TestCreateAccountingDocumentReportService());
        createAccountingDocumentService.setParameterService(parameterService);
        createAccountingDocumentService.setCreateAccountingDocumentValidationService(buildCreateAccountingDocumentValidationService(configurationService));
        createAccountingDocumentService.setBusinessObjectService(buildMockBusinessObjectService());
        createAccountingDocumentService.setDateTimeService(dateTimeService);
    }

    @After
    public void tearDown() throws Exception {
        deleteTargetTestDirectory();
    }
    
    protected void assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture... fixtures) {
        boolean actualResults = createAccountingDocumentService.createAccountingDocumentsFromXml();
        assertDocumentsWereCreatedAndRoutedCorrectly(actualResults, fixtures);
        assertDoneFilesWereDeleted();
    }

    private void assertDocumentsWereCreatedAndRoutedCorrectly(boolean actualResults, AccountingXmlDocumentListWrapperFixture... fixtures) {
        Map<String, AccountingXmlDocumentListWrapperFixture> fileNameToFixtureMap = buildFileNameToFixtureMap(fixtures);
        
        AccountingXmlDocumentEntryFixture[] processingOrderedDocumentFixtures = createAccountingDocumentService.getProcessingOrderedBaseFileNames()
                .stream()
                .map(fileNameToFixtureMap::get)
                .flatMap((fixture) -> fixture.documents.stream())
                .toArray(AccountingXmlDocumentEntryFixture[]::new);
        
        assertDocumentsWereCreatedAndRoutedCorrectly(processingOrderedDocumentFixtures);
        
        assertActualResultsAreExpected(actualResults, fileNameToFixtureMap);
    }
    
    private void assertActualResultsAreExpected(boolean actualResults, Map<String, AccountingXmlDocumentListWrapperFixture> fileNameToFixtureMap) {
        boolean expectedResult = true;
        for (AccountingXmlDocumentListWrapperFixture xmlFixture : fileNameToFixtureMap.values()) {
            expectedResult &= xmlFixture.expectedResults;
        }
        assertEquals("The expected result should equal the actual result", expectedResult, actualResults);
    }

    private Map<String, AccountingXmlDocumentListWrapperFixture> buildFileNameToFixtureMap(
            AccountingXmlDocumentListWrapperFixture... fixtures) {
        Map<String, AccountingXmlDocumentListWrapperFixture> fileNameToFixtureMap = new HashMap<>();
        for (int i = 0; i < fixtures.length; i++) {
            String baseFileName = creationOrderedBaseFileNames.get(i);
            fileNameToFixtureMap.put(baseFileName, fixtures[i]);
        }
        return fileNameToFixtureMap;
    }

    private void assertDocumentsWereCreatedAndRoutedCorrectly(AccountingXmlDocumentEntryFixture... fixtures) {
        List<AccountingXmlDocumentEntryFixture> expectedRoutableFixtures = new ArrayList<>(fixtures.length);
        List<AccountingDocument> expectedAccountingDocuments = new ArrayList<>(fixtures.length);
        buildExpectedDocumentsAndAppendToLists(expectedRoutableFixtures::add, expectedAccountingDocuments::add, fixtures);
        
        assertEquals("Wrong number of routed documents", expectedAccountingDocuments.size(), routedAccountingDocuments.size());
        
        for (int i = 0; i < expectedAccountingDocuments.size(); i++) {
            AccountingXmlDocumentEntryFixture fixture = expectedRoutableFixtures.get(i);
            Class<? extends AccountingDocument> expectedDocumentClass = AccountingDocumentClassMappingUtils
                    .getDocumentClassByDocumentType(fixture.documentTypeCode);
            assertAccountingDocumentIsCorrect(expectedDocumentClass, expectedAccountingDocuments.get(i), routedAccountingDocuments.get(i));
        }
    }

    private void buildExpectedDocumentsAndAppendToLists(Consumer<AccountingXmlDocumentEntryFixture> fixtureListAppender,
            Consumer<AccountingDocument> documentListAppender, AccountingXmlDocumentEntryFixture... fixtures) {
        MutableInt idCounter = new MutableInt(DOCUMENT_NUMBER_START);
        
        Stream.of(fixtures)
                .filter(this::isDocumentExpectedToReachInitiationPoint)
                .map((fixture) -> buildDocumentIdToFixtureMapping(idCounter, fixture))
                .filter((mapping) -> isDocumentExpectedToPassBusinessRulesValidation(mapping.getValue()))
                .peek((mapping) -> fixtureListAppender.accept(mapping.getValue()))
                .map(this::buildExpectedDocument)
                .forEach(documentListAppender);
    }

    private Map.Entry<String, AccountingXmlDocumentEntryFixture> buildDocumentIdToFixtureMapping(
            MutableInt idCounter, AccountingXmlDocumentEntryFixture fixture) {
        idCounter.increment();
        return new AbstractMap.SimpleImmutableEntry<>(idCounter.toString(), fixture);
    }

    private AccountingDocument buildExpectedDocument(Map.Entry<String, AccountingXmlDocumentEntryFixture> docIdToFixtureMapping) {
        AccountingXmlDocumentEntryFixture fixture = docIdToFixtureMapping.getValue();
        return fixture.toAccountingDocument(docIdToFixtureMapping.getKey());
    }

    @SuppressWarnings("unchecked")
    protected void assertAccountingDocumentIsCorrect(
            Class<? extends AccountingDocument> documentClass, AccountingDocument expectedDocument, AccountingDocument actualDocument) {
        assertTrue("Document was not of the expected type of " + documentClass.getName(), documentClass.isAssignableFrom(actualDocument.getClass()));
        assertEquals("Wrong document number", expectedDocument.getDocumentNumber(), actualDocument.getDocumentNumber());
        
        if (expectedDocument instanceof PreEncumbranceDocument) {
            PreEncumbranceDocument expectedPeDocument = (PreEncumbranceDocument) expectedDocument;
            PreEncumbranceDocument actualPeDocument = (PreEncumbranceDocument) actualDocument;
            assertEquals("wrong reversal date", expectedPeDocument.getReversalDate(), actualPeDocument.getReversalDate());
        }
        
        assertHeaderIsCorrect(expectedDocument.getDocumentHeader(),
                actualDocument.getDocumentHeader());
        assertObjectListIsCorrect("source accounting lines",
                expectedDocument.getSourceAccountingLines(), actualDocument.getSourceAccountingLines(), this::assertAccountingLineIsCorrect);
        assertObjectListIsCorrect("target accounting lines",
                expectedDocument.getTargetAccountingLines(), actualDocument.getTargetAccountingLines(), this::assertAccountingLineIsCorrect);
        assertObjectListIsCorrect("notes",
                expectedDocument.getNotes(), actualDocument.getNotes(), this::assertNoteIsCorrect);
        assertObjectListIsCorrect("ad hoc persons",
                expectedDocument.getAdHocRoutePersons(), actualDocument.getAdHocRoutePersons(), this::assertAdHocPersonIsCorrect);
    }

    protected <T> void assertObjectListIsCorrect(String listLabel, List<? extends T> expectedObjects, List<? extends T> actualObjects,
            BiConsumer<? super T, ? super T> objectValidator) {
        assertEquals("Wrong number of " + listLabel, expectedObjects.size(), actualObjects.size());
        for (int i = 0; i < expectedObjects.size(); i++) {
            objectValidator.accept(expectedObjects.get(i), actualObjects.get(i));
        }
    }

    private void assertHeaderIsCorrect(DocumentHeader expectedHeader, DocumentHeader actualHeader) {
        assertEquals("Wrong document number", expectedHeader.getDocumentNumber(), actualHeader.getDocumentNumber());
        assertEquals("Wrong document description", expectedHeader.getDocumentDescription(), actualHeader.getDocumentDescription());
        assertEquals("Wrong document explanation", expectedHeader.getExplanation(), actualHeader.getExplanation());
        assertEquals("Wrong org document number", expectedHeader.getOrganizationDocumentNumber(), actualHeader.getOrganizationDocumentNumber());
    }

    protected void assertAccountingLineIsCorrect(AccountingLine expectedLine, AccountingLine actualLine) {
        assertEquals("Wrong accounting line implementation class", expectedLine.getClass(), actualLine.getClass());
        assertEquals("Wrong document number", expectedLine.getDocumentNumber(), actualLine.getDocumentNumber());
        assertEquals("Wrong chart code", expectedLine.getChartOfAccountsCode(), actualLine.getChartOfAccountsCode());
        assertEquals("Wrong account number", expectedLine.getAccountNumber(), actualLine.getAccountNumber());
        assertEquals("Wrong sub-account number", expectedLine.getSubAccountNumber(), actualLine.getSubAccountNumber());
        assertEquals("Wrong object code", expectedLine.getFinancialObjectCode(), actualLine.getFinancialObjectCode());
        assertEquals("Wrong sub-object code", expectedLine.getFinancialSubObjectCode(), actualLine.getFinancialSubObjectCode());
        assertEquals("Wrong project code", expectedLine.getProjectCode(), actualLine.getProjectCode());
        assertEquals("Wrong org ref ID", expectedLine.getOrganizationReferenceId(), actualLine.getOrganizationReferenceId());
        assertEquals("Wrong line description", expectedLine.getFinancialDocumentLineDescription(), actualLine.getFinancialDocumentLineDescription());
        assertEquals("Wrong line amount", expectedLine.getAmount(), actualLine.getAmount());
        assertEquals("wrong reference number", expectedLine.getReferenceNumber(), actualLine.getReferenceNumber());
        if (StringUtils.isNotBlank(expectedLine.getDebitCreditCode())) {
            assertEquals("Wrong debit/credit code", expectedLine.getDebitCreditCode(), actualLine.getDebitCreditCode());
        } else {
            assertTrue("Line should not have a debit/credit code set up", StringUtils.isBlank(actualLine.getDebitCreditCode()));
        }
    }

    private void assertNoteIsCorrect(Note expectedNote, Note actualNote) {
        assertEquals("Wrong note type", expectedNote.getNoteTypeCode(), actualNote.getNoteTypeCode());
        assertEquals("Wrong note text", expectedNote.getNoteText(), actualNote.getNoteText());
        if (expectedNote.getAttachment() == null) {
            assertNull("Note should not have had an attachment", actualNote.getAttachment());
        } else {
            Attachment expectedAttachment = expectedNote.getAttachment();
            Attachment actualAttachment = actualNote.getAttachment();
            assertNotNull("Note should have had an attachment", actualAttachment);
            assertEquals("Wrong attachment file name", expectedAttachment.getAttachmentFileName(), actualAttachment.getAttachmentFileName());
        }
    }

    private void assertAdHocPersonIsCorrect(AdHocRoutePerson expectedAdHocPerson, AdHocRoutePerson actualAdHocPerson) {
        assertEquals("Wrong document number", expectedAdHocPerson.getdocumentNumber(), actualAdHocPerson.getdocumentNumber());
        assertEquals("Wrong recipient netID", expectedAdHocPerson.getId(), actualAdHocPerson.getId());
        assertEquals("Wrong action requested", expectedAdHocPerson.getActionRequested(), actualAdHocPerson.getActionRequested());
    }

    private void createTargetTestDirectory() throws IOException {
        File accountingXmlDocumentTestDirectory = new File(TARGET_TEST_FILE_PATH);
        FileUtils.forceMkdir(accountingXmlDocumentTestDirectory);
    }

    protected void copyTestFilesAndCreateDoneFiles(String... baseFileNames) throws IOException {
        for (String baseFileName : baseFileNames) {
            File sourceFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, SOURCE_TEST_FILE_PATH, baseFileName, CuFPConstants.XML_FILE_EXTENSION));
            File targetFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName, CuFPConstants.XML_FILE_EXTENSION));
            File doneFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName,
                            GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION));
            FileUtils.copyFile(sourceFile, targetFile);
            doneFile.createNewFile();
            creationOrderedBaseFileNames.add(baseFileName);
        }
    }

    protected void renameTestAndDoneFileToUppercase(String baseFileName) throws IOException {
        String[] extensions = {
            CuFPConstants.XML_FILE_EXTENSION,
            GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION
        };
        for (String extension : extensions) {
            File oldFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName, extension));
            File tempFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName, extension + "1"));
            File newFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH,
                            StringUtils.upperCase(baseFileName, Locale.US), extension));
            FileUtils.moveFile(oldFile, tempFile);
            FileUtils.moveFile(tempFile, newFile);
        }
        int orderedNamesIndex = creationOrderedBaseFileNames.indexOf(baseFileName);
        if (orderedNamesIndex == -1) {
            throw new IllegalStateException("fileName was not recorded during prior setup: " + baseFileName);
        }
        creationOrderedBaseFileNames.set(orderedNamesIndex, StringUtils.upperCase(baseFileName, Locale.US));
    }

    private void assertDoneFilesWereDeleted() {
        for (String baseFileName : creationOrderedBaseFileNames) {
            File doneFile = new File(
                    String.format(FULL_FILE_PATH_FORMAT, TARGET_TEST_FILE_PATH, baseFileName,
                            GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION));
            assertFalse("The file '" + baseFileName + GeneralLedgerConstants.BatchFileSystem.DONE_FILE_EXTENSION
                    + "' should have been deleted", doneFile.exists());
        }
    }

    private void deleteTargetTestDirectory() throws IOException {
        File accountingXmlDocumentTestDirectory = new File(TARGET_TEST_FILE_PATH);
        if (accountingXmlDocumentTestDirectory.exists() && accountingXmlDocumentTestDirectory.isDirectory()) {
            FileUtils.forceDelete(accountingXmlDocumentTestDirectory.getAbsoluteFile());
        }
    }

    private AccountingXmlDocumentInputFileType buildAccountingXmlDocumentInputFileType(
            final DateTimeService dateTimeService) throws Exception {
        final AccountingXmlDocumentInputFileType inputFileType = new AccountingXmlDocumentInputFileType();
        inputFileType.setOutputClass(AccountingXmlDocumentListWrapper.class);
        inputFileType.setDateTimeService(dateTimeService);
        inputFileType.setFileNamePrefix("accountingXmlDocument_");
        inputFileType.setFileExtension(StringUtils.substringAfter(CuFPConstants.XML_FILE_EXTENSION, KFSConstants.DELIMITER));
        inputFileType.setDirectoryPath(TARGET_TEST_FILE_PATH);
        inputFileType.setSchemaLocation(CuFPConstants.ACCOUNTING_XML_DOCUMENT_XSD_LOCATION);
        return inputFileType;
    }

    private DateTimeService buildMockDateTimeService() throws Exception {
        DateTimeService dateTimeService = Mockito.mock(DateTimeService.class);
        Mockito.when(dateTimeService.toDateTimeStringForFilename(Mockito.any())).then(this::formatDate);
        Mockito.when(dateTimeService.getCurrentDate()).then(this::buildNewDate);
        Mockito.when(dateTimeService.getCurrentSqlDate())
                .then(this::buildStaticSqlDate);
        Mockito.when(dateTimeService.getCurrentTimestamp())
                .then(this::buildStaticTimestamp);
        return dateTimeService;
    }
    
    private String formatDate(InvocationOnMock invocation) {
        Date date = invocation.getArgument(0);
        return DATE_FORMAT.format(date);
    }
    
    private Date buildNewDate(InvocationOnMock invocation) {
        return new Date();
    }

    private java.sql.Date buildStaticSqlDate(InvocationOnMock invocation) {
        DateTime dateTime = StringToJavaDateAdapter.parseToDateTime(CuFPTestConstants.DATE_02_21_2019);
        return new java.sql.Date(dateTime.getMillis());
    }

    private Timestamp buildStaticTimestamp(InvocationOnMock invocation) {
        DateTime dateTime = StringToJavaDateAdapter.parseToDateTime(CuFPTestConstants.DATE_02_21_2019);
        return new Timestamp(dateTime.getMillis());
    }

    private FileStorageService buildFileStorageService() throws Exception {
        FileSystemFileStorageServiceImpl fileStorageService = new FileSystemFileStorageServiceImpl();
        fileStorageService.setPathPrefix(KFSConstants.DELIMITER);
        return fileStorageService;
    }

    protected ConfigurationService buildMockConfigurationService() throws Exception {
        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        Mockito.when(configurationService.getPropertyValueAsString(CuFPTestConstants.TEST_VALIDATION_ERROR_KEY))
            .thenReturn(CuFPTestConstants.TEST_VALIDATION_ERROR_MESSAGE);
        Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_ATTACHMENT_DOWNLOAD))
            .thenReturn(CuFPTestConstants.TEST_ATTACHMENT_DOWNLOAD_FAILURE_MESSAGE);
        Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_GENERIC_ERROR))
                .thenReturn(CuFPTestConstants.GENERIC_ERROR_MESSAGE);
        Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_GENERIC_NUMERIC_ERROR))
                .thenReturn(CuFPTestConstants.GENERIC_NUMERIC_ERROR_MESSAGE);
        Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_XML_ADAPTER_ERROR))
                .thenReturn(CuFPTestConstants.XML_ADAPTER_ERROR_MESSAGE);
        Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.VALIDATION_CREATE_ACCOUNTING_DOCUMENT_EXCEPTION_MESSAGE_REGEX))
                .thenReturn(CuFPTestConstants.EXCEPTION_MESSAGE_REGEX);
        Mockito.when(configurationService.getPropertyValueAsString(CuFPKeyConstants.CREATE_ACCOUNTING_DOCUMENT_PAYEE_NAME_NOT_SAME_AS_VENDOR))
        .thenReturn(CuFPTestConstants.TEST_CREATE_ACCOUNT_DOCUMENT_PAYEE_MISMATCH);
        return configurationService;
    }

    protected PersonService buildMockPersonService() throws Exception {
        PersonService personService = Mockito.mock(PersonService.class);
        Person systemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
        Mockito.when(personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER)).thenReturn(systemUser);
        Person testUser = MockPersonUtil.createMockPerson(TestUserFixture.TEST_USER);
        Mockito.when(personService.getPersonByEmployeeId(TestUserFixture.TEST_USER.employeeId)).thenReturn(testUser);
        return personService;
    }

    private DocumentService buildMockDocumentService() throws Exception {
        DocumentService documentService = Mockito.mock(DocumentService.class);
        Mockito.when(documentService.routeDocument(Mockito.any(), Mockito.any(), Mockito.any())).then(this::recordAndReturnDocumentIfValid);
        return documentService;
    }
    
    private Document recordAndReturnDocumentIfValid(InvocationOnMock invocation) {
        Document document = invocation.getArgument(0);
        if (!documentPassesBusinessRules(document)) {
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, CuFPTestConstants.TEST_VALIDATION_ERROR_KEY);
            throw new ValidationException("Simulated business rule validation failure");
        }
        routedAccountingDocuments.add((AccountingDocument) document);
        return document;
    }

    private ParameterService buildParameterService() {
        String namespace = KFSConstants.CoreModuleNamespaces.FINANCIAL;
        String component = CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCOUNTING_DOCUMENT_SERVICE_COMPONENT_NAME;
        ParameterService parameterService = Mockito.mock(ParameterService.class);
        Mockito.when(parameterService.getParameterValueAsString(namespace, component,
                CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCT_DOC_REPORT_EMAIL_ADDRESS))
                .thenReturn(TestEmails.KFS_GL_FP_AT_CORNELL_DOT_EDU);
        Mockito.when(parameterService.getParameterValuesAsString(namespace, component,
                CuFPParameterConstants.CreateAccountingDocumentService.DUPLICATE_FILE_REPORT_EMAIL_ADDRESSES))
                .thenReturn(List.of(TestEmails.MOCK_TEST_DEVS_AT_CORNELL_DOT_EDU,
                        TestEmails.MOCK_TEST_FUNC_LEADS_AT_CORNELL_DOT_EDU));
        Mockito.when(parameterService.getParameterValueAsBoolean(namespace, component,
                CuFPParameterConstants.CreateAccountingDocumentService.DUPLICATE_FILE_CHECK_IND))
                .thenReturn(Boolean.TRUE);
        return parameterService;
    }

    private CreateAccountingDocumentValidationService buildCreateAccountingDocumentValidationService(
            ConfigurationService configurationService) throws Exception {
        CreateAccountingDocumentValidationServiceImpl validationService = new CreateAccountingDocumentValidationServiceImpl();
        validationService.setConfigurationService(configurationService);
        validationService.afterPropertiesSet();
        return validationService;
    }

    private BusinessObjectService buildMockBusinessObjectService() {
        BusinessObjectService businessObjectService = Mockito.mock(BusinessObjectService.class);
        Mockito.when(businessObjectService
                .findMatching(Mockito.eq(CreateAccountingDocumentFileEntry.class), Mockito.anyMap()))
                .then(this::getExistingFileEntry);
        Mockito.when(businessObjectService.save(Mockito.any(CreateAccountingDocumentFileEntry.class)))
                .then(this::saveFileEntry);
        return businessObjectService;
    }

    private Collection<CreateAccountingDocumentFileEntry> getExistingFileEntry(InvocationOnMock invocation) {
        Map<?, ?> criteria = (Map<?, ?>) invocation.getArgument(1);
        String fileName = (String) criteria.get(KFSPropertyConstants.FILE_NAME);
        CreateAccountingDocumentFileEntry fileEntry = fileEntries.get(fileName);
        return (fileEntry != null)
                ? List.of((CreateAccountingDocumentFileEntry) ObjectUtils.deepCopy(fileEntry))
                : List.of();
    }

    private CreateAccountingDocumentFileEntry saveFileEntry(InvocationOnMock invocation) {
        CreateAccountingDocumentFileEntry fileEntry = invocation.getArgument(0);
        fileEntry.setFileId(++nextFileEntryId);
        fileEntries.put(fileEntry.getFileName(), fileEntry);
        return (CreateAccountingDocumentFileEntry) ObjectUtils.deepCopy(fileEntry);
    }

    protected void overwriteFileEntry(String fileName, AccountingXmlDocumentListWrapperFixture fileFixture) {
        CreateAccountingDocumentFileEntry fileEntry = new CreateAccountingDocumentFileEntry();
        fileEntry.setFileId(++nextFileEntryId);
        fileEntry.setFileName(fileName);
        fileEntry.setFileCreatedDate(new Timestamp(fileFixture.getCreateDateAsDateTime().getMillis()));
        fileEntry.setFileProcessedDate(dateTimeService.getCurrentTimestamp());
        fileEntry.setReportEmailAddress(fileFixture.reportEmail);
        fileEntry.setFileOverview(fileFixture.overview);
        fileEntry.setDocumentCount(fileFixture.documents.size());
        fileEntries.put(fileName, fileEntry);
    }

    private boolean documentPassesBusinessRules(Document document) {
        return !StringUtils.equalsIgnoreCase(
                CuFPTestConstants.BUSINESS_RULE_VALIDATION_DESCRIPTION_INDICATOR, document.getDocumentHeader().getDocumentDescription());
    }

    private boolean isDocumentExpectedToReachInitiationPoint(AccountingXmlDocumentEntryFixture fixture) {
        return !AccountingXmlDocumentEntryFixture.BAD_CONVERSION_DOCUMENT_PLACEHOLDER.equals(fixture);
    }

    private boolean isDocumentExpectedToPassBusinessRulesValidation(AccountingXmlDocumentEntryFixture fixture) {
        return !AccountingXmlDocumentEntryFixture.BAD_RULES_DOCUMENT_PLACEHOLDER.equals(fixture);
    }

    protected TestAccountingXmlDocumentDownloadAttachmentService buildAccountingXmlDocumentDownloadAttachmentService() throws Exception {
        TestAccountingXmlDocumentDownloadAttachmentService downloadAttachmentService = new TestAccountingXmlDocumentDownloadAttachmentService();
        downloadAttachmentService.setAttachmentService(buildMockAttachmentService());
        downloadAttachmentService.setWebServiceCredentialService(buildMockWebServiceCredentialService());
        downloadAttachmentService.setClient(buildMockClient());
        return downloadAttachmentService;
    }

    private AttachmentService buildMockAttachmentService() throws Exception {
        AttachmentService attachmentService = Mockito.mock(AttachmentService.class);
        Mockito.when(attachmentService.createAttachment(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.any(), Mockito.any())).then(this::buildSimpleAttachment);
        return attachmentService;
    }
    
    private Attachment buildSimpleAttachment(InvocationOnMock invocation) {
        String fileName = invocation.getArgument(1);
        Attachment attachment = new Attachment();
        attachment.setAttachmentFileName(fileName);
        return attachment;
    }

    private WebServiceCredentialService buildMockWebServiceCredentialService() {
        WebServiceCredentialService credentialService = Mockito.mock(WebServiceCredentialService.class);
        Mockito.when(credentialService.getWebServiceCredentialsByGroupCode(Mockito.anyString())).then(this::findCredentialsFromFixture);
        return credentialService;
    }
    
    private Collection<WebServiceCredential> findCredentialsFromFixture(InvocationOnMock invocation) {
        String groupCode = invocation.getArgument(0);
        return WebServiceCredentialFixture.getCredentialsByCredentialGroupCode(groupCode);
    }
    
    protected UniversityDateService buildMockUniversityDateService() {
        UniversityDateService dateService = Mockito.mock(UniversityDateService.class);
        Mockito.when(dateService.getCurrentFiscalYear()).thenReturn(2019);
        return dateService;
    }

    private Client buildMockClient() {
        Client client = Mockito.mock(Client.class);
        Mockito.when(client.target(Mockito.any(URI.class))).then(this::buildMockWebTarget);
        return client;
    }

    private WebTarget buildMockWebTarget(InvocationOnMock invocation) {
        WebTarget target = Mockito.mock(WebTarget.class);
        Mockito.when(target.request()).then(this::buildMockInvocationBuilder);
        return target;
    }

    private Invocation.Builder buildMockInvocationBuilder(InvocationOnMock invocation) {
        Invocation.Builder builder = Mockito.mock(Invocation.Builder.class);
        Mockito.when(builder.header(Mockito.anyString(), Mockito.any())).thenReturn(builder);
        Mockito.when(builder.buildGet()).then(this::buildMockInvocation);
        return builder;
    }
    
    private Invocation buildMockInvocation(InvocationOnMock invocationOnMock) {
        Invocation invocation = Mockito.mock(Invocation.class);
        Mockito.when(invocation.invoke()).then(this::buildMockResponse);
        return invocation;
    }

    private Response buildMockResponse(InvocationOnMock invocation) {
        Response response = Mockito.mock(Response.class);
        Mockito.when(response.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        Mockito.when(response.readEntity(InputStream.class)).then(this::buildSingleByteInputStream);
        return response;
    }

    private InputStream buildSingleByteInputStream(InvocationOnMock invocation) {
        return new ByteArrayInputStream(new byte[] {1});
    }

    protected static class TestCreateAccountingDocumentServiceImpl extends CreateAccountingDocumentServiceImpl {
        private Map<String, AccountingDocumentGenerator<?>> documentGeneratorsByBeanName;
        private PersonService personService;
        protected AccountingXmlDocumentDownloadAttachmentService downloadAttachmentService;
        private ConfigurationService configurationService;
        protected FiscalYearFunctionControlService fiscalYearFunctionControlService;
        protected UniversityDateService universityDateService;
        protected AccountingPeriodService accountingPeriodService;
        protected DateTimeService dateTimeService;
        protected CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService;
        protected CuDisbursementVoucherPayeeService cuDisbursementVoucherPayeeService;
        protected VendorService vendorService;
        
        private int nextDocumentNumber;
        private List<String> processingOrderedBaseFileNames;
        private boolean failToCreateDocument;
        
        public TestCreateAccountingDocumentServiceImpl(
                PersonService personService, AccountingXmlDocumentDownloadAttachmentService downloadAttachmentService,
                ConfigurationService configurationService, UniversityDateService universityDateService,
                DateTimeService dateTimeService) {
            this.personService = personService;
            this.downloadAttachmentService = downloadAttachmentService;
            this.configurationService = configurationService;
            this.universityDateService = universityDateService;
            this.dateTimeService = dateTimeService;
            this.nextDocumentNumber = DOCUMENT_NUMBER_START;
            this.processingOrderedBaseFileNames = new ArrayList<>();
            this.failToCreateDocument = false;
        }

        public void setFailToCreateDocument(boolean failToCreateDocument) {
            this.failToCreateDocument = failToCreateDocument;
        }

        public void initializeDocumentGeneratorsFromMappings(AccountingDocumentMapping... documentMappings) {
            this.documentGeneratorsByBeanName = Arrays.stream(documentMappings)
                    .map(AccountingDocumentMapping::getGeneratorConstructor)
                    .map(this::buildAccountingDocumentGenerator)
                    .collect(Collectors.toMap(
                            this::buildGeneratorBeanName, Function.identity()));
        }

        protected AccountingDocumentGenerator<?> buildAccountingDocumentGenerator(
                BiFunction<Supplier<Note>, Supplier<AdHocRoutePerson>, AccountingDocumentGeneratorBase<?>> generatorConstructor) {
            AccountingDocumentGeneratorBase<?> accountingDocumentGenerator = generatorConstructor.apply(
                    MockDocumentUtils::buildMockNote, MockDocumentUtils::buildMockAdHocRoutePerson);
            accountingDocumentGenerator.setPersonService(personService);
            accountingDocumentGenerator.setAccountingXmlDocumentDownloadAttachmentService(downloadAttachmentService);
            accountingDocumentGenerator.setConfigurationService(configurationService);
            return accountingDocumentGenerator;
        }

        protected String buildGeneratorBeanName(AccountingDocumentGenerator<?> documentGenerator) {
            return CuFPConstants.ACCOUNTING_DOCUMENT_GENERATOR_BEAN_PREFIX
                    + AccountingDocumentClassMappingUtils.getDocumentTypeByDocumentClass(documentGenerator.getDocumentClass());
        }

        public List<String> getProcessingOrderedBaseFileNames() {
            return processingOrderedBaseFileNames;
        }

        @Override
        public boolean createAccountingDocumentsFromXml() {
            Person systemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
            UserSession systemUserSession = MockPersonUtil.createMockUserSession(systemUser);

            boolean result = false;
            try {
                result = GlobalVariables.doInNewGlobalVariables(systemUserSession, () -> {
                    return GlobalResourceLoaderUtils.doWithResourceRetrievalDelegatedToKradResourceLoaderUtil(
                            () -> super.createAccountingDocumentsFromXml());
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return  result;
        }

        @Override
        protected void processAccountingDocumentFromXml(String fileName, CreateAccountingDocumentLogReport logReport) {
            processingOrderedBaseFileNames.add(convertToBaseFileName(fileName));
            super.processAccountingDocumentFromXml(fileName, logReport);
        }

        private String convertToBaseFileName(String fileName) {
            String fileNameWithoutPath = new File(fileName).getName();
            return StringUtils.substringBefore(fileNameWithoutPath, KFSConstants.DELIMITER);
        }

        @Override
        protected AccountingDocumentGenerator<?> findDocumentGenerator(String beanName) {
            if (StringUtils.isBlank(beanName)) {
                throw new IllegalStateException("Document generator bean name should not have been blank");
            } else {
                return documentGeneratorsByBeanName.computeIfAbsent(beanName, this::throwExceptionIfGeneratorIsNotFound);
            }
        }

        protected AccountingDocumentGenerator<?> throwExceptionIfGeneratorIsNotFound(String beanName) {
            throw new IllegalStateException("Unrecognized document generator bean name: " + beanName);
        }

        @Override
        protected Document getNewDocument(Class<? extends Document> documentClass) {
            if (failToCreateDocument) {
                throw new ResourceLoaderException("Emulate problem getting services");
            }
            if (documentClass == null) {
                throw new IllegalStateException("Document class should not have been null");
            } else if (generatorDoesNotExistForDocumentClass(documentClass)) {
                throw new IllegalStateException("Unexpected accounting document class: " + documentClass.getName());
            }
            
            @SuppressWarnings("unchecked")
            Class<? extends AccountingDocument> accountingDocumentClass = (Class<? extends AccountingDocument>) documentClass;
            Document document = MockDocumentUtils.buildMockAccountingDocument(accountingDocumentClass);
            String documentNumber = String.valueOf(++nextDocumentNumber);
            document.setDocumentNumber(documentNumber);
            document.getDocumentHeader().setDocumentNumber(documentNumber);
            
            return document;
        }

        protected boolean generatorDoesNotExistForDocumentClass(Class<? extends Document> documentClass) {
            return documentGeneratorsByBeanName.values().stream()
                    .noneMatch((generator) -> generator.getDocumentClass().equals(documentClass));
        }

        @Override
        protected String buildValidationErrorMessage(ValidationException validationException) {
            String validationErrorMessage = super.buildValidationErrorMessage(validationException);
            assertFalse("The error-message-building process should not have encountered an unexpected exception",
                    StringUtils.equals(CuFPConstants.ALTERNATE_BASE_VALIDATION_ERROR_MESSAGE, validationErrorMessage));
            return validationErrorMessage;
        }
        
    }
    
    protected static class TestAccountingXmlDocumentDownloadAttachmentService extends AccountingXmlDocumentDownloadAttachmentServiceImpl {
        private Client mockClient;
        private boolean forceUseOfRealClientToTestAttachmentUrls;

        @Override
        protected Client getClient() {
            return mockClient;
        }

        public void setClient(Client client) {
            this.mockClient = client;
        }
        
        @Override
        protected Invocation buildClientRequest(String url, Collection<WebServiceCredential> creds) throws URISyntaxException {
            if (forceUseOfRealClientToTestAttachmentUrls) {
                return buildClienRequesttWithRealClientObject(url);
            } else {
                return super.buildClientRequest(url, creds);
            }
        }

        private Invocation buildClienRequesttWithRealClientObject(String url) throws URISyntaxException {
            URI uri = new URI(url);
            Builder builder = super.getClient().target(uri).request();
            return builder.buildGet();
        }

        public void setForceUseOfRealClientToTestAttachmentUrls(boolean forceUseOfRealClientToTestAttachmentUrls) {
            this.forceUseOfRealClientToTestAttachmentUrls = forceUseOfRealClientToTestAttachmentUrls;
        }
        
    }
    
    private class TestCreateAccountingDocumentReportService implements CreateAccountingDocumentReportService {

        @Override
        public void generateReport(CreateAccountingDocumentReportItem reportItem) {
        }

        @Override
        public void sendReportEmail(String fromAddress, List<String> toAddresses) {
        }
        
    }
    
}
