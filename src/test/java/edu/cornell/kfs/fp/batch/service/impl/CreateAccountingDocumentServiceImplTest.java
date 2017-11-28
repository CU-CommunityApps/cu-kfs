package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentClassMappingUtils;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentEntryFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;
import edu.cornell.kfs.fp.document.CuDistributionOfIncomeAndExpenseDocument;
import edu.cornell.kfs.sys.batch.JAXBXmlBatchInputFileTypeBase;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.MockDocumentUtils;

public class CreateAccountingDocumentServiceImplTest {

    private static final String SOURCE_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml";
    private static final String TARGET_TEST_FILE_PATH = "test/fp/accountingXmlDocument";
    private static final String FULL_FILE_PATH_FORMAT = "%s/%s%s";
    private static final int DOCUMENT_NUMBER_START = 1000;

    @SuppressWarnings("deprecation")
    private static final String DI_GENERATOR_BEAN_NAME = CuFPConstants.ACCOUNTING_DOCUMENT_GENERATOR_BEAN_PREFIX
            + KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE;

    private TestCreateAccountingDocumentServiceImpl createAccountingDocumentService;
    private List<AccountingDocument> routedAccountingDocuments;
    private List<String> creationOrderedBaseFileNames;

    @Before
    public void setUp() throws Exception {
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl();
        createAccountingDocumentService.setAccountingDocumentBatchInputFileType(buildAccountingXmlDocumentInputFileType());
        createAccountingDocumentService.setBatchInputFileService(new BatchInputFileServiceImpl());
        createAccountingDocumentService.setDocumentService(buildMockDocumentService());
        
        routedAccountingDocuments = new ArrayList<>();
        creationOrderedBaseFileNames = new ArrayList<>();
        createTargetTestDirectory();
    }

    @After
    public void tearDown() throws Exception {
        deleteTargetTestDirectory();
    }

    @Test
    public void testLoadSingleFileWithSingleDIDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-di-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithZeroDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("empty-document-list-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.EMPTY_DOCUMENT_LIST_TEST);
    }

    @Test
    public void testLoadMultipleFilesWithDIDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-di-document-test", "multi-di-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture.SINGLE_DI_DOCUMENT_TEST,
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocumentsPlusDocumentWithInvalidDocType() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-plus-invalid-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_INVALID_SECOND_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocumentsPlusDocumentWithRulesFailure() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_BAD_RULES_FIRST_DOCUMENT_TEST);
    }

    private void assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture... fixtures) {
        createAccountingDocumentService.createAccountingDocumentsFromXml();
        assertDocumentsWereCreatedAndRoutedCorrectly(fixtures);
    }

    private void assertDocumentsWereCreatedAndRoutedCorrectly(AccountingXmlDocumentListWrapperFixture... fixtures) {
        Map<String, AccountingXmlDocumentListWrapperFixture> fileNameToFixtureMap = buildFileNameToFixtureMap(fixtures);
        
        AccountingXmlDocumentEntryFixture[] processingOrderedDocumentFixtures = createAccountingDocumentService.getProcessingOrderedBaseFileNames()
                .stream()
                .map(fileNameToFixtureMap::get)
                .flatMap((fixture) -> fixture.documents.stream())
                .toArray(AccountingXmlDocumentEntryFixture[]::new);
        
        assertDocumentsWereCreatedAndRoutedCorrectly(processingOrderedDocumentFixtures);
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
        List<AccountingDocument> expectedAccountingDocuments = buildExpectedDocumentsList(fixtures);
        assertEquals("Wrong number of routed documents", expectedAccountingDocuments.size(), routedAccountingDocuments.size());
        for (int i = 0; i < expectedAccountingDocuments.size(); i++) {
            AccountingXmlDocumentEntryFixture fixture = fixtures[i];
            Class<? extends AccountingDocument> expectedDocumentClass = AccountingDocumentClassMappingUtils
                    .getDocumentClassByDocumentType(fixture.documentTypeCode);
            assertAccountingDocumentIsCorrect(expectedDocumentClass, expectedAccountingDocuments.get(i), routedAccountingDocuments.get(i));
        }
    }

    private List<AccountingDocument> buildExpectedDocumentsList(AccountingXmlDocumentEntryFixture... fixtures) {
        MutableInt nextDocumentNumber = new MutableInt(DOCUMENT_NUMBER_START);
        Supplier<String> docNumberSupplier = () -> {
             nextDocumentNumber.increment();
             return nextDocumentNumber.toString();
        };
        
        return Stream.of(fixtures)
                .map((documentFixture) -> documentFixture.toAccountingDocument(docNumberSupplier.get()))
                .filter(this::documentPassesBusinessRules)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @SuppressWarnings("unchecked")
    private void assertAccountingDocumentIsCorrect(
            Class<? extends AccountingDocument> documentClass, AccountingDocument expectedDocument, AccountingDocument actualDocument) {
        assertTrue("Document was not of the expected type of " + documentClass.getName(), documentClass.isAssignableFrom(actualDocument.getClass()));
        assertEquals("Wrong document number", expectedDocument.getDocumentNumber(), actualDocument.getDocumentNumber());
        
        assertHeaderIsCorrect((FinancialSystemDocumentHeader) expectedDocument.getDocumentHeader(),
                (FinancialSystemDocumentHeader) actualDocument.getDocumentHeader());
        assertObjectListIsCorrect("source accounting lines",
                expectedDocument.getSourceAccountingLines(), actualDocument.getSourceAccountingLines(), this::assertAccountingLineIsCorrect);
        assertObjectListIsCorrect("target accounting lines",
                expectedDocument.getTargetAccountingLines(), actualDocument.getTargetAccountingLines(), this::assertAccountingLineIsCorrect);
        assertObjectListIsCorrect("notes",
                expectedDocument.getNotes(), actualDocument.getNotes(), this::assertNoteIsCorrect);
        assertObjectListIsCorrect("ad hoc persons",
                expectedDocument.getAdHocRoutePersons(), actualDocument.getAdHocRoutePersons(), this::assertAdHocPersonIsCorrect);
    }

    private <T> void assertObjectListIsCorrect(String listLabel, List<? extends T> expectedObjects, List<? extends T> actualObjects,
            BiConsumer<? super T, ? super T> objectValidator) {
        assertEquals("Wrong number of " + listLabel, expectedObjects.size(), actualObjects.size());
        for (int i = 0; i < expectedObjects.size(); i++) {
            objectValidator.accept(expectedObjects.get(i), actualObjects.get(i));
        }
    }

    private void assertHeaderIsCorrect(FinancialSystemDocumentHeader expectedHeader, FinancialSystemDocumentHeader actualHeader) {
        assertEquals("Wrong document number", expectedHeader.getDocumentNumber(), actualHeader.getDocumentNumber());
        assertEquals("Wrong document description", expectedHeader.getDocumentDescription(), actualHeader.getDocumentDescription());
        assertEquals("Wrong document explanation", expectedHeader.getExplanation(), actualHeader.getExplanation());
        assertEquals("Wrong org document number", expectedHeader.getOrganizationDocumentNumber(), actualHeader.getOrganizationDocumentNumber());
    }

    private void assertAccountingLineIsCorrect(AccountingLine expectedLine, AccountingLine actualLine) {
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
    }

    private void assertNoteIsCorrect(Note expectedNote, Note actualNote) {
        assertEquals("Wrong note type", expectedNote.getNoteTypeCode(), actualNote.getNoteTypeCode());
        assertEquals("Wrong note text", expectedNote.getNoteText(), actualNote.getNoteText());
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

    private void copyTestFilesAndCreateDoneFiles(String... baseFileNames) throws IOException {
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

    private void deleteTargetTestDirectory() throws IOException {
        File accountingXmlDocumentTestDirectory = new File(TARGET_TEST_FILE_PATH);
        if (accountingXmlDocumentTestDirectory.exists() && accountingXmlDocumentTestDirectory.isDirectory()) {
            FileUtils.forceDelete(accountingXmlDocumentTestDirectory.getAbsoluteFile());
        }
    }

    private JAXBXmlBatchInputFileTypeBase buildAccountingXmlDocumentInputFileType() throws Exception {
        JAXBXmlBatchInputFileTypeBase inputFileType = new JAXBXmlBatchInputFileTypeBase();
        inputFileType.setDateTimeService(buildMockDateTimeService());
        inputFileType.setMarshalService(new CUMarshalServiceImpl());
        inputFileType.setPojoClass(AccountingXmlDocumentListWrapper.class);
        inputFileType.setFileTypeIdentifier("accountingXmlDocumentFileType");
        inputFileType.setFileNamePrefix("accountingXmlDocument_");
        inputFileType.setTitleKey("accountingXmlDocument");
        inputFileType.setFileExtension(StringUtils.substringAfter(CuFPConstants.XML_FILE_EXTENSION, "."));
        inputFileType.setDirectoryPath(TARGET_TEST_FILE_PATH);
        return inputFileType;
    }

    private DateTimeService buildMockDateTimeService() throws Exception {
        DateTimeService dateTimeService = EasyMock.createMock(DateTimeService.class);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HH-mm-ss-S");
        
        Capture<Date> dateArg = EasyMock.newCapture();
        EasyMock.expect(dateTimeService.toDateTimeStringForFilename(EasyMock.capture(dateArg)))
                .andStubAnswer(() -> dateFormat.format(dateArg.getValue()));
        EasyMock.expect(dateTimeService.getCurrentDate())
                .andStubAnswer(Date::new);
        
        EasyMock.replay(dateTimeService);
        return dateTimeService;
    }

    private DocumentService buildMockDocumentService() throws Exception {
        DocumentService documentService = EasyMock.createMock(DocumentService.class);
        
        Capture<Document> documentArg = EasyMock.newCapture();
        EasyMock.expect(
                documentService.routeDocument(
                        EasyMock.capture(documentArg), EasyMock.anyObject(), EasyMock.anyObject()))
                .andStubAnswer(() -> recordAndReturnDocument(documentArg.getValue()));
        
        EasyMock.replay(documentService);
        return documentService;
    }

    private Document recordAndReturnDocument(Document document) {
        if (!documentPassesBusinessRules(document)) {
            throw new ValidationException("Simulated business rule validation failure");
        }
        routedAccountingDocuments.add((AccountingDocument) document);
        return document;
    }

    private boolean documentPassesBusinessRules(Document document) {
        return !StringUtils.equalsIgnoreCase(
                CuFPTestConstants.BUSINESS_RULE_VALIDATION_DESCRIPTION_INDICATOR, document.getDocumentHeader().getDocumentDescription());
    }

    private static class TestCreateAccountingDocumentServiceImpl extends CreateAccountingDocumentServiceImpl {
        private CuDistributionOfIncomeAndExpenseDocumentGenerator diGenerator;
        private int nextDocumentNumber;
        private List<String> processingOrderedBaseFileNames;

        public TestCreateAccountingDocumentServiceImpl() {
            diGenerator = new CuDistributionOfIncomeAndExpenseDocumentGenerator(
                    MockDocumentUtils::buildMockNote, MockDocumentUtils::buildMockAdHocRoutePerson);
            nextDocumentNumber = DOCUMENT_NUMBER_START;
            processingOrderedBaseFileNames = new ArrayList<>();
        }

        public List<String> getProcessingOrderedBaseFileNames() {
            return processingOrderedBaseFileNames;
        }

        @Override
        protected void processAccountingDocumentFromXml(String fileName) {
            processingOrderedBaseFileNames.add(convertToBaseFileName(fileName));
            super.processAccountingDocumentFromXml(fileName);
        }

        private String convertToBaseFileName(String fileName) {
            String fileNameWithoutPath = new File(fileName).getName();
            return StringUtils.substringBefore(fileNameWithoutPath, KFSConstants.DELIMITER);
        }

        @Override
        protected AccountingDocumentGenerator<? extends AccountingDocument> findDocumentGenerator(String beanName) {
            if (StringUtils.isBlank(beanName)) {
                throw new IllegalStateException("Document generator bean name should not have been blank");
            }
            
            switch (beanName) {
                case DI_GENERATOR_BEAN_NAME :
                    return diGenerator;
                default :
                    throw new IllegalStateException("Unrecognized document generator bean name: " + beanName);
            }
        }

        @Override
        protected Document getNewDocument(Class<? extends Document> documentClass) {
            if (documentClass == null) {
                throw new IllegalStateException("Document class should not have been null");
            } else if (!CuDistributionOfIncomeAndExpenseDocument.class.equals(documentClass)) {
                throw new IllegalStateException("Unexpected accounting document class: " + documentClass.getName());
            }
            
            @SuppressWarnings("unchecked")
            Class<? extends AccountingDocument> accountingDocumentClass = (Class<? extends AccountingDocument>) documentClass;
            Document document = MockDocumentUtils.buildMockAccountingDocument(accountingDocumentClass);
            String documentNumber = String.valueOf(++nextDocumentNumber);
            document.setDocumentNumber(documentNumber);
            document.getDocumentHeader().setDocumentNumber(documentNumber);
            
            if (document instanceof AccountingDocument) {
                AccountingDocument accountingDocument = (AccountingDocument) document;
                accountingDocument.setSourceAccountingLines(new ArrayList<>());
                accountingDocument.setTargetAccountingLines(new ArrayList<>());
                accountingDocument.setNextSourceLineNumber(Integer.valueOf(1));
                accountingDocument.setNextTargetLineNumber(Integer.valueOf(1));
            }
            
            return document;
        }
    }

}
