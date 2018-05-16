package edu.cornell.kfs.fp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.fp.businessobject.FiscalYearFunctionControl;
import org.kuali.kfs.fp.businessobject.InternalBillingItem;
import org.kuali.kfs.fp.document.InternalBillingDocument;
import org.kuali.kfs.fp.service.FiscalYearFunctionControlService;
import org.kuali.kfs.fp.service.impl.FiscalYearFunctionControlServiceImpl;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.AttachmentService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.CuFPParameterConstants;
import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.CreateAccountingDocumentReportItem;
import edu.cornell.kfs.fp.batch.service.AccountingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.AccountingXmlDocumentDownloadAttachmentService;
import edu.cornell.kfs.fp.batch.service.CreateAccountingDocumentReportService;
import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentListWrapper;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentClassMappingUtils;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingDocumentMapping;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentEntryFixture;
import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentListWrapperFixture;
import edu.cornell.kfs.sys.batch.JAXBXmlBatchInputFileTypeBase;
import edu.cornell.kfs.sys.businessobject.fixture.WebServiceCredentialFixture;
import edu.cornell.kfs.sys.service.WebServiceCredentialService;
import edu.cornell.kfs.sys.service.impl.CUMarshalServiceImpl;
import edu.cornell.kfs.sys.util.MockDocumentUtils;
import edu.cornell.kfs.sys.util.MockObjectUtils;
import edu.cornell.kfs.sys.util.MockPersonUtil;

public class CreateAccountingDocumentServiceImplTest {

    private static final String SOURCE_TEST_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/xml";
    private static final String TARGET_TEST_FILE_PATH = "test/fp/accountingXmlDocument";
    private static final String FULL_FILE_PATH_FORMAT = "%s/%s%s";
    private static final int DOCUMENT_NUMBER_START = 1000;

    private TestCreateAccountingDocumentServiceImpl createAccountingDocumentService;
    private List<AccountingDocument> routedAccountingDocuments;
    private List<String> creationOrderedBaseFileNames;

    @Before
    public void setUp() throws Exception {
        ConfigurationService configurationService = buildMockConfigurationService();
        createAccountingDocumentService = new TestCreateAccountingDocumentServiceImpl(
                buildMockPersonService(), buildAccountingXmlDocumentDownloadAttachmentService(),
                configurationService, buildMockFiscalYearFunctionControlService());
        createAccountingDocumentService.initializeDocumentGeneratorsFromMappings(
                AccountingDocumentMapping.DI_DOCUMENT, AccountingDocumentMapping.IB_DOCUMENT, AccountingDocumentMapping.TF_DOCUMENT,
                AccountingDocumentMapping.BA_DOCUMENT);
        createAccountingDocumentService.setAccountingDocumentBatchInputFileType(buildAccountingXmlDocumentInputFileType());
        createAccountingDocumentService.setBatchInputFileService(new BatchInputFileServiceImpl());
        createAccountingDocumentService.setFileStorageService(buildFileStorageService());
        createAccountingDocumentService.setConfigurationService(configurationService);
        createAccountingDocumentService.setDocumentService(buildMockDocumentService());
        createAccountingDocumentService.setCreateAccountingDocumentReportService(new TestCreateAccountingDocumentReportService());
        createAccountingDocumentService.setParameterService(buildParameterService());
        
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
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_BAD_CONVERSION_SECOND_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocumentsPlusDocumentWithRulesFailure() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_BAD_RULES_FIRST_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDIDocumentsPlusDocumentsWithBadAttachments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-di-plus-bad-attachments-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DI_DOCUMENT_WITH_BAD_ATTACHMENTS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleIBDocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ib-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_IB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleIBDocumentLackingItems() throws Exception {
        copyTestFilesAndCreateDoneFiles("ib-without-items-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_IB_DOCUMENT_NO_ITEMS_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleIBDocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ib-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_IB_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleIBDocumentsPlusDocumentWithRulesFailure() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ib-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_IB_DOCUMENT_WITH_BAD_RULES_THIRD_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleDocumentTypes() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-doc-types-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_DOCUMENT_TYPES_TEST);
    }

    @Test
    public void testLoadSingleFileWithDIDocumentContainingIBItemsToIgnore() throws Exception {
        copyTestFilesAndCreateDoneFiles("di-with-ib-items-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.DI_WITH_IB_ITEMS_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocument() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentLackingBAAccountProperties() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-no-base-or-months-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_NO_BASEAMOUNT_OR_MONTHS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentUsingNonZeroBaseAmounts() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-nonzero-base-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_NONZERO_BASEAMOUNT_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithSingleBADocumentUsingMultipleMonthAmounts() throws Exception {
        copyTestFilesAndCreateDoneFiles("single-ba-multi-months-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.SINGLE_BA_MULTI_MONTHS_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleBADocuments() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ba-document-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_BA_DOCUMENT_TEST);
    }

    @Test
    public void testLoadSingleFileWithMultipleBADocumentsPlusDocumentsWithRulesFailures() throws Exception {
        copyTestFilesAndCreateDoneFiles("multi-ba-plus-bad-rules-doc-test");
        assertDocumentsAreGeneratedCorrectlyByBatchProcess(
                AccountingXmlDocumentListWrapperFixture.MULTI_BA_DOCUMENT_WITH_SOME_BAD_RULES_DOCUMENTS_TEST);
    }

    private void assertDocumentsAreGeneratedCorrectlyByBatchProcess(AccountingXmlDocumentListWrapperFixture... fixtures) {
        createAccountingDocumentService.createAccountingDocumentsFromXml();
        assertDocumentsWereCreatedAndRoutedCorrectly(fixtures);
        assertDoneFilesWereDeleted();
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
        
        if (InternalBillingDocument.class.isAssignableFrom(documentClass)) {
            assertObjectListIsCorrect("items",
                    ((InternalBillingDocument) expectedDocument).getItems(), ((InternalBillingDocument) actualDocument).getItems(),
                    this::assertInternalBillingItemIsCorrect);
        }
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
        if (expectedLine instanceof BudgetAdjustmentAccountingLine) {
            assertBudgetAdjustmentAccountingLinePropertiesAreCorrect(
                    (BudgetAdjustmentAccountingLine) expectedLine, (BudgetAdjustmentAccountingLine) actualLine);
        }
    }

    private void assertBudgetAdjustmentAccountingLinePropertiesAreCorrect(
            BudgetAdjustmentAccountingLine expectedLine, BudgetAdjustmentAccountingLine actualLine) {
        assertEquals("Wrong base amount", expectedLine.getBaseBudgetAdjustmentAmount(), actualLine.getBaseBudgetAdjustmentAmount());
        assertEquals("Wrong current amount", expectedLine.getCurrentBudgetAdjustmentAmount(), actualLine.getCurrentBudgetAdjustmentAmount());
        assertEquals("Wrong month 01 amount",
                expectedLine.getFinancialDocumentMonth1LineAmount(), actualLine.getFinancialDocumentMonth1LineAmount());
        assertEquals("Wrong month 02 amount",
                expectedLine.getFinancialDocumentMonth2LineAmount(), actualLine.getFinancialDocumentMonth2LineAmount());
        assertEquals("Wrong month 03 amount",
                expectedLine.getFinancialDocumentMonth3LineAmount(), actualLine.getFinancialDocumentMonth3LineAmount());
        assertEquals("Wrong month 04 amount",
                expectedLine.getFinancialDocumentMonth4LineAmount(), actualLine.getFinancialDocumentMonth4LineAmount());
        assertEquals("Wrong month 05 amount",
                expectedLine.getFinancialDocumentMonth5LineAmount(), actualLine.getFinancialDocumentMonth5LineAmount());
        assertEquals("Wrong month 06 amount",
                expectedLine.getFinancialDocumentMonth6LineAmount(), actualLine.getFinancialDocumentMonth6LineAmount());
        assertEquals("Wrong month 07 amount",
                expectedLine.getFinancialDocumentMonth7LineAmount(), actualLine.getFinancialDocumentMonth7LineAmount());
        assertEquals("Wrong month 08 amount",
                expectedLine.getFinancialDocumentMonth8LineAmount(), actualLine.getFinancialDocumentMonth8LineAmount());
        assertEquals("Wrong month 09 amount",
                expectedLine.getFinancialDocumentMonth9LineAmount(), actualLine.getFinancialDocumentMonth9LineAmount());
        assertEquals("Wrong month 10 amount",
                expectedLine.getFinancialDocumentMonth10LineAmount(), actualLine.getFinancialDocumentMonth10LineAmount());
        assertEquals("Wrong month 11 amount",
                expectedLine.getFinancialDocumentMonth11LineAmount(), actualLine.getFinancialDocumentMonth11LineAmount());
        assertEquals("Wrong month 12 amount",
                expectedLine.getFinancialDocumentMonth12LineAmount(), actualLine.getFinancialDocumentMonth12LineAmount());
    }

    private void assertInternalBillingItemIsCorrect(InternalBillingItem expectedItem, InternalBillingItem actualItem) {
        assertEquals("Wrong document number", expectedItem.getDocumentNumber(), actualItem.getDocumentNumber());
        assertEquals("Wrong item sequence number", expectedItem.getItemSequenceId(), actualItem.getItemSequenceId());
        assertEquals("Wrong service date", expectedItem.getItemServiceDate(), actualItem.getItemServiceDate());
        assertEquals("Wrong stock number", expectedItem.getItemStockNumber(), actualItem.getItemStockNumber());
        assertEquals("Wrong item description", expectedItem.getItemStockDescription(), actualItem.getItemStockDescription());
        assertEquals("Wrong item quantity", expectedItem.getItemQuantity(), actualItem.getItemQuantity());
        assertEquals("Wrong unit of measure", expectedItem.getUnitOfMeasureCode(), actualItem.getUnitOfMeasureCode());
        assertEquals("Wrong item cost", expectedItem.getItemUnitAmount(), actualItem.getItemUnitAmount());
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

    private JAXBXmlBatchInputFileTypeBase buildAccountingXmlDocumentInputFileType() throws Exception {
        JAXBXmlBatchInputFileTypeBase inputFileType = new JAXBXmlBatchInputFileTypeBase();
        inputFileType.setDateTimeService(buildMockDateTimeService());
        inputFileType.setCuMarshalService(new CUMarshalServiceImpl());
        inputFileType.setPojoClass(AccountingXmlDocumentListWrapper.class);
        inputFileType.setFileTypeIdentifier("accountingXmlDocumentFileType");
        inputFileType.setFileNamePrefix("accountingXmlDocument_");
        inputFileType.setTitleKey("accountingXmlDocument");
        inputFileType.setFileExtension(StringUtils.substringAfter(CuFPConstants.XML_FILE_EXTENSION, KFSConstants.DELIMITER));
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

    private FileStorageService buildFileStorageService() throws Exception {
        FileSystemFileStorageServiceImpl fileStorageService = new FileSystemFileStorageServiceImpl();
        fileStorageService.setPathPrefix(KFSConstants.DELIMITER);
        return fileStorageService;
    }

    private ConfigurationService buildMockConfigurationService() throws Exception {
        ConfigurationService configurationService = EasyMock.createMock(ConfigurationService.class);
        
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPTestConstants.TEST_VALIDATION_ERROR_KEY))
                .andStubReturn(CuFPTestConstants.TEST_VALIDATION_ERROR_MESSAGE);
        EasyMock.expect(configurationService.getPropertyValueAsString(CuFPKeyConstants.ERROR_CREATE_ACCOUNTING_DOCUMENT_ATTACHMENT_DOWNLOAD))
                .andStubReturn(CuFPTestConstants.TEST_ATTACHMENT_DOWNLOAD_FAILURE_MESSAGE);
        
        EasyMock.replay(configurationService);
        return configurationService;
    }

    private PersonService buildMockPersonService() throws Exception {
        PersonService personService = EasyMock.createMock(PersonService.class);
        
        Person systemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
        EasyMock.expect(personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER))
                .andStubReturn(systemUser);
        
        EasyMock.replay(personService);
        return personService;
    }

    private DocumentService buildMockDocumentService() throws Exception {
        DocumentService documentService = EasyMock.createMock(DocumentService.class);
        
        Capture<Document> documentArg = EasyMock.newCapture();
        EasyMock.expect(
                documentService.routeDocument(
                        EasyMock.capture(documentArg), EasyMock.anyObject(), EasyMock.anyObject()))
                .andStubAnswer(() -> recordAndReturnDocumentIfValid(documentArg.getValue()));
        
        EasyMock.replay(documentService);
        return documentService;
    }
    
    private ParameterService buildParameterService() {
        ParameterService parameterService = EasyMock.createMock(ParameterService.class);
        
        EasyMock.expect(
                parameterService.getParameterValueAsString(KFSConstants.ParameterNamespaces.FINANCIAL, 
                CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCOUNTING_DOCUMENT_SERVICE_COMPONENT_NAME, 
                CuFPParameterConstants.CreateAccountingDocumentService.CREATE_ACCT_DOC_REPORT_EMAIL_ADDRESS))
        .andStubAnswer(() -> "kfs-gl_fp@cornell.edu");
        
        EasyMock.replay(parameterService);
        
        return parameterService;
    }

    private Document recordAndReturnDocumentIfValid(Document document) {
        if (!documentPassesBusinessRules(document)) {
            GlobalVariables.getMessageMap().putError(KRADConstants.GLOBAL_ERRORS, CuFPTestConstants.TEST_VALIDATION_ERROR_KEY);
            throw new ValidationException("Simulated business rule validation failure");
        }
        routedAccountingDocuments.add((AccountingDocument) document);
        return document;
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

    private TestAccountingXmlDocumentDownloadAttachmentService buildAccountingXmlDocumentDownloadAttachmentService() throws Exception {
        TestAccountingXmlDocumentDownloadAttachmentService downloadAttachmentService = new TestAccountingXmlDocumentDownloadAttachmentService();
        downloadAttachmentService.setAttachmentService(buildMockAttachmentService());
        downloadAttachmentService.setWebServiceCredentialService(buildMockWebServiceCredentialService());
        downloadAttachmentService.setClient(buildMockClient());
        return downloadAttachmentService;
    }

    private AttachmentService buildMockAttachmentService() throws Exception {
        return MockObjectUtils.buildMockObjectWithExceptionProneSetup(AttachmentService.class, (attachmentService) -> {
            Capture<String> fileNameArg = EasyMock.newCapture();
            EasyMock.expect(
                    attachmentService.createAttachment(
                            EasyMock.anyObject(), EasyMock.capture(fileNameArg), EasyMock.anyObject(),
                            EasyMock.anyInt(), EasyMock.anyObject(), EasyMock.anyObject()))
                    .andStubAnswer(() -> buildSimpleAttachment(fileNameArg.getValue()));
        });
    }

    private Attachment buildSimpleAttachment(String fileName) {
        Attachment attachment = new Attachment();
        attachment.setAttachmentFileName(fileName);
        return attachment;
    }

    private WebServiceCredentialService buildMockWebServiceCredentialService() {
        return MockObjectUtils.buildMockObject(WebServiceCredentialService.class, (webServiceCredentialService) -> {
            Capture<String> groupCodeArg = EasyMock.newCapture();
            EasyMock.expect(
                    webServiceCredentialService.getWebServiceCredentialsByGroupCode(EasyMock.capture(groupCodeArg)))
                    .andStubAnswer(() -> WebServiceCredentialFixture.getCredentialsByCredentialGroupCode(groupCodeArg.getValue()));
        });
    }

    private FiscalYearFunctionControlService buildMockFiscalYearFunctionControlService() {
        List<FiscalYearFunctionControl> allowedBudgetAdjustmentYears = IntStream.of(CuFPTestConstants.FY_2016, CuFPTestConstants.FY_2018)
                .mapToObj(this::buildFunctionControlAllowingBudgetAdjustment)
                .collect(Collectors.toCollection(ArrayList::new));
        
        FiscalYearFunctionControlService fyService = mock(FiscalYearFunctionControlService.class);
        when(fyService.getBudgetAdjustmentAllowedYears())
                .thenReturn(allowedBudgetAdjustmentYears);
        
        return fyService;
    }

    private FiscalYearFunctionControl buildFunctionControlAllowingBudgetAdjustment(int fiscalYear) {
        FiscalYearFunctionControl functionControl = new FiscalYearFunctionControl();
        functionControl.setUniversityFiscalYear(Integer.valueOf(fiscalYear));
        functionControl.setFinancialSystemFunctionControlCode(FiscalYearFunctionControlServiceImpl.FY_FUNCTION_CONTROL_BA_ALLOWED);
        functionControl.setFinancialSystemFunctionActiveIndicator(true);
        return functionControl;
    }

    private Client buildMockClient() {
        return MockObjectUtils.buildMockObject(Client.class, (client) -> {
            EasyMock.expect(client.target(EasyMock.isA(URI.class)))
                    .andStubAnswer(this::buildMockWebTarget);
        });
    }

    private WebTarget buildMockWebTarget() {
        return MockObjectUtils.buildMockObject(WebTarget.class, (webTarget) -> {
            EasyMock.expect(webTarget.request())
                    .andStubAnswer(this::buildMockInvocationBuilder);
        });
    }

    private Invocation.Builder buildMockInvocationBuilder() {
        return MockObjectUtils.buildMockObject(Invocation.Builder.class, (invocationBuilder) -> {
            EasyMock.expect(invocationBuilder.header(EasyMock.anyObject(), EasyMock.anyObject()))
                    .andStubReturn(invocationBuilder);
            EasyMock.expect(invocationBuilder.buildGet())
                    .andStubAnswer(this::buildMockInvocation);
        });
    }

    private Invocation buildMockInvocation() {
        return MockObjectUtils.buildMockObject(Invocation.class, (invocation) -> {
            EasyMock.expect(invocation.invoke())
                    .andStubAnswer(this::buildMockResponse);
        });
    }

    private Response buildMockResponse() {
        return MockObjectUtils.buildMockObject(Response.class, (response) -> {
            EasyMock.expect(response.getStatus())
                    .andStubReturn(Response.Status.OK.getStatusCode());
            EasyMock.expect(response.readEntity(InputStream.class))
                    .andStubAnswer(this::buildSingleByteInputStream);
        });
    }

    private InputStream buildSingleByteInputStream() {
        return new ByteArrayInputStream(new byte[] {1});
    }

    private static class TestCreateAccountingDocumentServiceImpl extends CreateAccountingDocumentServiceImpl {
        private Map<String, AccountingDocumentGenerator<?>> documentGeneratorsByBeanName;
        private PersonService personService;
        private AccountingXmlDocumentDownloadAttachmentService downloadAttachmentService;
        private ConfigurationService configurationService;
        private FiscalYearFunctionControlService fiscalYearFunctionControlService;
        private int nextDocumentNumber;
        private List<String> processingOrderedBaseFileNames;

        public TestCreateAccountingDocumentServiceImpl(
                PersonService personService, AccountingXmlDocumentDownloadAttachmentService downloadAttachmentService,
                ConfigurationService configurationService, FiscalYearFunctionControlService fiscalYearFunctionControlService) {
            this.personService = personService;
            this.downloadAttachmentService = downloadAttachmentService;
            this.configurationService = configurationService;
            this.fiscalYearFunctionControlService = fiscalYearFunctionControlService;
            this.nextDocumentNumber = DOCUMENT_NUMBER_START;
            this.processingOrderedBaseFileNames = new ArrayList<>();
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
            if (accountingDocumentGenerator instanceof CuBudgetAdjustmentDocumentGenerator) {
                CuBudgetAdjustmentDocumentGenerator baGenerator = (CuBudgetAdjustmentDocumentGenerator) accountingDocumentGenerator;
                baGenerator.setFiscalYearFunctionControlService(fiscalYearFunctionControlService);
            }
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
        public void createAccountingDocumentsFromXml() {
            Person systemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
            UserSession systemUserSession = MockPersonUtil.createMockUserSession(systemUser);
            
            try {
                GlobalVariables.doInNewGlobalVariables(systemUserSession, () -> {
                    super.createAccountingDocumentsFromXml();
                    return null;
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
    
    private static class TestAccountingXmlDocumentDownloadAttachmentService extends AccountingXmlDocumentDownloadAttachmentServiceImpl {

        private Client mockClient;

        @Override
        protected Client getClient() {
            return mockClient;
        }

        public void setClient(Client client) {
            this.mockClient = client;
        }
        
    }
    
    private class TestCreateAccountingDocumentReportService implements CreateAccountingDocumentReportService {

        @Override
        public void generateReport(CreateAccountingDocumentReportItem reportItem) {
        }

        @Override
        public void sendReportEmail(String toAddress, String fromAddress) {
        }
        
    }

}
