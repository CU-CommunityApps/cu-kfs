package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.gl.GeneralLedgerConstants.BatchFileSystem;
import org.kuali.kfs.kns.datadictionary.control.SelectControlDefinition;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.datadictionary.mask.MaskFormatterLiteral;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.sys.service.impl.EmailServiceImpl;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.coa.businessobject.options.CuCheckingSavingsValuesFinder;
import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpParameterConstants;
import edu.cornell.kfs.pdp.CUPdpPropertyConstants;
import edu.cornell.kfs.pdp.CUPdpTestConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractCsv;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractCsvInputFileType;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractFileResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractStep;
import edu.cornell.kfs.pdp.batch.fixture.ACHBankFixture;
import edu.cornell.kfs.pdp.batch.fixture.ACHFileFixture;
import edu.cornell.kfs.pdp.batch.fixture.ACHPersonPayeeFixture;
import edu.cornell.kfs.pdp.batch.fixture.ACHRowFixture;
import edu.cornell.kfs.pdp.batch.fixture.ACHUpdateFixture;
import edu.cornell.kfs.pdp.batch.fixture.PayeeACHAccountFixture;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractReportService;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;
import edu.cornell.kfs.pdp.businessobject.options.TestPayeeAchIdTypeValuesFinder;
import edu.cornell.kfs.pdp.document.CuPayeeACHAccountMaintainableImpl;
import edu.cornell.kfs.pdp.service.CuAchService;
import edu.cornell.kfs.pdp.service.impl.CuAchServiceImpl;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.MockPersonUtil;


@SuppressWarnings("deprecation")
public class PayeeACHAccountExtractServiceImplTest {

    private static final String ACH_SOURCE_FILE_PATH = "src/test/resources/edu/cornell/kfs/pdp/batch/service/fixture";
    private static final String ACH_TESTING_FILE_PATH = "test/pdp/payeeACHAccountExtract";
    private static final String ACH_TESTING_DIRECTORY = ACH_TESTING_FILE_PATH + "/";
    private static final String FILE_PATH_FORMAT = "{0}/{1}{2}";
    private static final String LITERAL_MASK_VALUE = "********";
    private static final Integer DOCUMENT_DESCRIPTION_MAX_LENGTH = Integer.valueOf(40);
    private static final String BANK_ACCOUNT_NUMBER_MAX_LENGTH_ERROR_MESSAGE = "Bank account number is too long";
    private static final String BUSINESS_RULES_FAILURE_MESSAGE = "Business rule validation failed";
    private static final String GLOBAL_ERROR_FORMAT = "{0}";
    private static final int BANK_ACCOUNT_NUMBER_MAX_LENGTH_FOR_ROUTING_VALIDATION = 17;
    private static final int INITIAL_DOCUMENT_ID = 1000;
    private static final long INITIAL_ACH_ACCOUNT_ID = 5000L;

    private static final String UNRESOLVED_EMAIL_STRING = "Payment $ \\n\\ for [payeeIdentifierTypeCode] of [payeeIdNumber]"
            + " will go to [bankAccountTypeCode] account at [bankRouting.bankName].[bankAccountNumber]";
    private static final String EMAIL_STRING_AS_FORMAT = "Payment $ \n\\ for {0} of {1} will go to {2} account at {3}.";
    private static final String PERSONAL_SAVINGS_ACCOUNT_TYPE_LABEL = "Personal Savings";

    private TestPayeeACHAccountExtractService payeeACHAccountExtractService;
    private AtomicInteger documentIdCounter;
    private AtomicLong achAccountIdCounter;

    @Before
    public void setUp() throws Exception {
        documentIdCounter = new AtomicInteger(INITIAL_DOCUMENT_ID);
        achAccountIdCounter = new AtomicLong(INITIAL_ACH_ACCOUNT_ID);
        
        payeeACHAccountExtractService = new TestPayeeACHAccountExtractService();
        payeeACHAccountExtractService.setBatchInputFileService(new BatchInputFileServiceImpl());
        payeeACHAccountExtractService.setBatchInputFileTypes(
                Collections.singletonList(createACHBatchInputFileType()));
        payeeACHAccountExtractService.setParameterService(createMockParameterService());
        payeeACHAccountExtractService.setPersonService(createMockPersonService());
        payeeACHAccountExtractService.setAchService(createAchService());
        payeeACHAccountExtractService.setAchBankService(createMockAchBankService());
        payeeACHAccountExtractService.setBusinessObjectService(createMockBusinessObjectService());
        payeeACHAccountExtractService.setPayeeACHAccountExtractReportService(createMockPayeeACHAccountExtractReportService());
        payeeACHAccountExtractService.setDateTimeService(createMockDateTimeService());
        payeeACHAccountExtractService.setPayeeACHAccountDocumentService(buildPayeeACHAccountDocumentServiceImpl());
    }

    @After
    public void cleanUp() throws Exception {
        removeTestFilesAndDirectories();
    }

    @Test
    public void testLoadValidFileWithSingleDataRow() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_SINGLE_SUCCESSFUL_LINE);
    }

    @Test
    public void testLoadFileWithInvalidFormat() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_BAD_HEADERS);
    }

    @Test
    public void testLoadFileWithoutAccountRows() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_HEADER_ROW_ONLY);
    }

    @Test
    public void testLoadFileWithAllInvalidRows() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_ONLY_VALIDATION_FAILURE_LINES);
    }

    @Test
    public void testLoadFileWithSomeInvalidRows() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_GOOD_AND_BAD_LINES);
    }

    @Test
    public void testLoadFileWithLineMatchingExistingAccounts() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_SINGLE_MATCHING_LINE);
    }

    @Test
    public void testLoadFileWithMultipleValidRows() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_MULTIPLE_SUCCESSFUL_LINES);
    }

    @Test
    public void testLoadFileWithLinesPartiallyOrFullyMatchingExistingAccounts() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_PARTIAL_AND_FULL_MATCHES);
    }

    @Test
    public void testLoadFileWithValidRowsPlusOneRulesFailureRow() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_GOOD_LINES_AND_BAD_RULES_FAILURE_LINE);
    }

    @Test
    public void testLoadMultipleFiles() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_BAD_HEADERS,
                ACHFileFixture.FILE_WITH_SINGLE_SUCCESSFUL_LINE, ACHFileFixture.FILE_WITH_GOOD_AND_BAD_LINES,
                ACHFileFixture.FILE_WITH_SINGLE_MATCHING_LINE, ACHFileFixture.FILE_WITH_PARTIAL_AND_FULL_MATCHES);
    }

    @Test
    public void testEmailBodyPlaceholderResolution() throws Exception {
        // NOTE: We expect that all potentially-sensitive placeholders except bank name will be replaced with empty text.
        TestPayeeAchIdTypeValuesFinder payeeIdTypeKeyValues = new TestPayeeAchIdTypeValuesFinder();
        PayeeACHAccount achAccount = PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW.toPayeeACHAccount();
        String expectedPayeeIdTypeLabel = payeeIdTypeKeyValues.getKeyLabel(achAccount.getPayeeIdentifierTypeCode());
        String expectedBody = MessageFormat.format(EMAIL_STRING_AS_FORMAT, expectedPayeeIdTypeLabel,
                achAccount.getPayeeIdNumber(), PERSONAL_SAVINGS_ACCOUNT_TYPE_LABEL, achAccount.getBankRouting().getBankName());
        PayeeACHAccountDocumentServiceImpl payeeDocumentService = (PayeeACHAccountDocumentServiceImpl) payeeACHAccountExtractService.payeeACHAccountDocumentService;
        String actualBody = payeeDocumentService.getResolvedEmailBody(achAccount, UNRESOLVED_EMAIL_STRING);
        
        assertEquals("Email body placeholders and special characters were not resolved properly", expectedBody, actualBody);
    }

    private void assertACHAccountExtractionHasCorrectResults(ACHFileFixture... fixtures) throws Exception {
        List<ACHFileFixture> expectedResults = Arrays.asList(fixtures);
        String[] fileNames = Arrays.stream(fixtures)
                .map(ACHFileFixture::getBaseFileName)
                .toArray(String[]::new);
        
        copyInputFilesAndGenerateDoneFiles(fileNames);
        processACHBatchDetailsInNewUserSession();
        
        assertFilesHaveExpectedResults(expectedResults, payeeACHAccountExtractService.getFileResults());
        assertDoneFilesWereDeleted(fileNames);
    }

    private boolean processACHBatchDetailsInNewUserSession() throws Exception {
        Person systemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
        UserSession systemUserSession = MockPersonUtil.createMockUserSession(systemUser);
        return GlobalVariables.doInNewGlobalVariables(systemUserSession, payeeACHAccountExtractService::processACHBatchDetails);
    }

    private void assertFilesHaveExpectedResults(List<ACHFileFixture> expectedResults, Map<String, ACHFileResult> actualResults) throws Exception {
        assertEquals("Wrong number of processed files", expectedResults.size(), actualResults.size());
        for (int i = 0; i < expectedResults.size(); i++) {
            ACHFileFixture expectedResult = expectedResults.get(i);
            ACHFileResult actualResult = actualResults.get(expectedResult.baseFileName);
            assertFileHasExpectedResults(i, expectedResult, actualResult);
        }
    }

    private void assertFileHasExpectedResults(int index, ACHFileFixture expectedResult, ACHFileResult actualResult) throws Exception {
        assertEquals("Wrong file-processable state at index " + index, expectedResult.processableFile, actualResult.isProcessableFile());
        if (expectedResult.processableFile) {
            List<ACHRowFixture> expectedRows = expectedResult.rowResults;
            List<ACHRowResult> actualRows = actualResult.getRowResults();
            assertEquals("Wrong number of processed file rows", expectedRows.size(), actualRows.size());
            
            for (int i = 0; i < expectedRows.size(); i++) {
                ACHRowFixture expectedRow = expectedRows.get(i);
                ACHRowResult actualRow = actualRows.get(i);
                assertRowHasExpectedUpdates(i, expectedRow, actualRow);
            }
        }
    }

    private void assertRowHasExpectedUpdates(int index, ACHRowFixture expectedRow, ACHRowResult actualRow) throws Exception {
        assertEquals("Wrong row validation result at index " + index, expectedRow.validRow, actualRow.isValidRow());
        if (expectedRow.validRow) {
            assertAccountHasExpectedUpdates(
                    PayeeIdTypeCodes.EMPLOYEE, expectedRow.employeeUpdateResult, actualRow.getDocumentForEmployeeAccountUpdate());
            assertAccountHasExpectedUpdates(
                    PayeeIdTypeCodes.ENTITY, expectedRow.entityUpdateResult, actualRow.getDocumentForEntityAccountUpdate());
        }
    }

    private void assertAccountHasExpectedUpdates(String payeeType, ACHUpdateFixture expectedUpdate, MaintenanceDocument document) throws Exception {
        if (expectedUpdate == ACHUpdateFixture.NO_UPDATE) {
           assertTrue("There should not have been an update to the account for payee type " + payeeType, ObjectUtils.isNull(document));
           return;
        }
        
        assertTrue("There should have been an update to the account for payee type " + payeeType, ObjectUtils.isNotNull(document));
        
        FinancialSystemMaintainable newMaintainable = (FinancialSystemMaintainable) document.getNewMaintainableObject();
        assertTrue("New maintainable should not be null", ObjectUtils.isNotNull(newMaintainable));
        assertEquals("Wrong data object class", PayeeACHAccount.class, newMaintainable.getDataObjectClass());
        assertEquals("Wrong maintenance action", expectedUpdate.getExpectedMaintenanceAction(), newMaintainable.getMaintenanceAction());
        assertPayeeACHAccountIsCorrect(expectedUpdate.getExpectedMaintenanceAction(), expectedUpdate.newAccount, newMaintainable);
        
        if (StringUtils.equals(KFSConstants.MAINTENANCE_EDIT_ACTION, expectedUpdate.getExpectedMaintenanceAction())) {
            FinancialSystemMaintainable oldMaintainable = (FinancialSystemMaintainable) document.getOldMaintainableObject();
            assertTrue("Old maintainable should not be null", ObjectUtils.isNotNull(oldMaintainable));
            assertEquals("Wrong data object class on old maintainable", PayeeACHAccount.class, oldMaintainable.getDataObjectClass());
            assertPayeeACHAccountIsCorrect(expectedUpdate.getExpectedMaintenanceAction(), expectedUpdate.oldAccount, oldMaintainable);
        }
    }

    private void assertPayeeACHAccountIsCorrect(
            String expectedMaintenanceAction, PayeeACHAccountFixture expectedAccountFixture, FinancialSystemMaintainable maintainable) throws Exception {
        PayeeACHAccount expectedAccount = expectedAccountFixture.toPayeeACHAccount();
        PayeeACHAccount actualAccount = (PayeeACHAccount) maintainable.getDataObject();
        
        assertTrue("ACH account should not be null", ObjectUtils.isNotNull(actualAccount));
        assertTrue("ACH account generated ID should not be null", ObjectUtils.isNotNull(actualAccount.getAchAccountGeneratedIdentifier()));
        if (StringUtils.equals(KFSConstants.MAINTENANCE_EDIT_ACTION, expectedMaintenanceAction)) {
            assertEquals("ACH account generated ID should not have changed",
                    expectedAccount.getAchAccountGeneratedIdentifier(), actualAccount.getAchAccountGeneratedIdentifier());
        }
        assertEquals("Wrong payee ID type code", expectedAccount.getPayeeIdentifierTypeCode(), actualAccount.getPayeeIdentifierTypeCode());
        assertEquals("Wrong payee ID number", expectedAccount.getPayeeIdNumber(), actualAccount.getPayeeIdNumber());
        assertEquals("Wrong transaction type", expectedAccount.getAchTransactionType(), actualAccount.getAchTransactionType());
        assertEquals("Wrong bank routing number", expectedAccount.getBankRoutingNumber(), actualAccount.getBankRoutingNumber());
        assertEquals("Wrong bank account type", expectedAccount.getBankAccountTypeCode(), actualAccount.getBankAccountTypeCode());
        assertEquals("Wrong bank account number", expectedAccount.getBankAccountNumber(), actualAccount.getBankAccountNumber());
        assertEquals("Wrong active flag status", expectedAccount.isActive(), actualAccount.isActive());
    }

    private void assertDoneFilesWereDeleted(String... inputFileNames) throws Exception {
        for (String inputFileName : inputFileNames) {
            File doneFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_TESTING_FILE_PATH, inputFileName, BatchFileSystem.DONE_FILE_EXTENSION));
            assertFalse("There should not be a .done file for " + inputFileName, doneFile.exists());
        }
    }

    protected void copyInputFilesAndGenerateDoneFiles(String... inputFileNames) throws IOException {
        for (String inputFileName : inputFileNames) {
            File sourceFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_SOURCE_FILE_PATH, inputFileName, CUPdpTestConstants.CSV_FILE_EXTENSION));
            File destFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_TESTING_FILE_PATH, inputFileName, CUPdpTestConstants.CSV_FILE_EXTENSION));
            File doneFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_TESTING_FILE_PATH, inputFileName, BatchFileSystem.DONE_FILE_EXTENSION));
            FileUtils.copyFile(sourceFile, destFile);
            doneFile.createNewFile();
        }
    }

    protected void removeTestFilesAndDirectories() throws IOException {
        File achDirectory = new File(ACH_TESTING_DIRECTORY);
        if (achDirectory.exists() && achDirectory.isDirectory()) {
            for (File achFile : achDirectory.listFiles()) {
                achFile.delete();
            }
            achDirectory.delete();
            
            // Delete parent directories.
            int slashIndex = ACH_TESTING_DIRECTORY.lastIndexOf('/', ACH_TESTING_DIRECTORY.lastIndexOf('/') - 1);
            while (slashIndex != -1) {
                File tempDirectory = new File(ACH_TESTING_DIRECTORY.substring(0, slashIndex + 1));
                tempDirectory.delete();
                slashIndex = ACH_TESTING_DIRECTORY.lastIndexOf('/', slashIndex - 1);
            }
        }
    }

    private PayeeACHAccountExtractCsvInputFileType createACHBatchInputFileType() {
        PayeeACHAccountExtractCsvInputFileType fileType = new PayeeACHAccountExtractCsvInputFileType();
        fileType.setDirectoryPath(ACH_TESTING_FILE_PATH);
        fileType.setFileExtension(
                StringUtils.substringAfter(CUPdpTestConstants.CSV_FILE_EXTENSION, CUKFSConstants.DELIMITER));
        fileType.setCsvEnumClass(PayeeACHAccountExtractCsv.class);
        return fileType;
    }

    private ParameterService createMockParameterService() throws Exception {
        ParameterService parameterService = mock(ParameterService.class);
        Stream<Pair<String, String>> parameterMappings = Stream.of(
                Pair.of(CUPdpParameterConstants.ACH_PERSONAL_CHECKING_TRANSACTION_CODE, CUPdpTestConstants.PERSONAL_CHECKING_CODE),
                Pair.of(CUPdpParameterConstants.ACH_PERSONAL_SAVINGS_TRANSACTION_CODE, CUPdpTestConstants.PERSONAL_SAVINGS_CODE),
                Pair.of(CUPdpParameterConstants.ACH_DIRECT_DEPOSIT_TRANSACTION_TYPE, CUPdpTestConstants.DIRECT_DEPOSIT_TYPE),
                Pair.of(CUPdpParameterConstants.GENERATED_PAYEE_ACH_ACCOUNT_DOC_NOTE_TEXT, CUPdpTestConstants.GENERATED_PAAT_NOTE_TEXT),
                Pair.of(CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT, CUPdpTestConstants.NEW_ACCOUNT_EMAIL_SUBJECT),
                Pair.of(CUPdpParameterConstants.NEW_PAYEE_ACH_ACCOUNT_EMAIL_BODY, CUPdpTestConstants.NEW_ACCOUNT_EMAIL_BODY),
                Pair.of(CUPdpParameterConstants.UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_SUBJECT, CUPdpTestConstants.UPDATED_ACCOUNT_EMAIL_SUBJECT),
                Pair.of(CUPdpParameterConstants.UPDATED_PAYEE_ACH_ACCOUNT_EMAIL_BODY, CUPdpTestConstants.UPDATED_ACCOUNT_EMAIL_BODY));
        
        parameterMappings.forEach((mapping) -> {
            when(parameterService.getParameterValueAsString(PayeeACHAccountExtractStep.class, mapping.getKey()))
                    .thenReturn(mapping.getValue());
        });
        
        when(parameterService.getParameterValueAsString(
                KFSConstants.CoreModuleNamespaces.PDP, KfsParameterConstants.BATCH_COMPONENT, KFSConstants.FROM_EMAIL_ADDRESS_PARAM_NM))
                .thenReturn(CUPdpTestConstants.ACH_EMAIL_FROM_ADDRESS);
        
        return parameterService;
    }

    private DocumentService createMockDocumentService() throws Exception {
        DocumentService mockDocumentService = mock(DocumentService.class);
        
        when(mockDocumentService.getNewDocument(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_MAINT_DOC_TYPE))
                .then(this::createMockPAATDocument);
        
        when(mockDocumentService.routeDocument(any(Document.class), any(), any()))
                .then(this::recordAndReturnDocumentIfValid);
        
        return mockDocumentService;
    }

    private Document recordAndReturnDocumentIfValid(InvocationOnMock invocation) throws Exception {
        Document document = invocation.getArgument(0);
        validateBankAccountMaxLength(document);
        if (GlobalVariables.getMessageMap().hasErrors()) {
            throw new ValidationException(BUSINESS_RULES_FAILURE_MESSAGE);
        }
        setBankObjectOnPayeeACHAccountIfAbsent(document);
        payeeACHAccountExtractService.addRoutedDocument((MaintenanceDocument) document);
        return document;
    }

    private void validateBankAccountMaxLength(Document document) {
        MaintenanceDocument paatDocument = (MaintenanceDocument) document;
        PayeeACHAccount newAccount = (PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject();
        if (StringUtils.length(newAccount.getBankAccountNumber()) > BANK_ACCOUNT_NUMBER_MAX_LENGTH_FOR_ROUTING_VALIDATION) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.BANK_ACCOUNT_NUMBER, KFSConstants.GLOBAL_ERRORS,
                    BANK_ACCOUNT_NUMBER_MAX_LENGTH_ERROR_MESSAGE);
        }
    }

    private void setBankObjectOnPayeeACHAccountIfAbsent(Document document) {
        MaintenanceDocument paatDocument = (MaintenanceDocument) document;
        PayeeACHAccount achAccount = (PayeeACHAccount) paatDocument.getNewMaintainableObject().getDataObject();
        if (ObjectUtils.isNull(achAccount.getBankRouting())) {
            ACHBankFixture.findBankByRoutingNumber(achAccount.getBankRoutingNumber())
                    .ifPresent((bankFixture) -> achAccount.setBankRouting(bankFixture.toACHBank()));
        }
    }

    private MaintenanceDocument createMockPAATDocument(InvocationOnMock invocation) throws Exception {
        MaintenanceDocument paatDocument = mock(FinancialSystemMaintenanceDocument.class, CALLS_REAL_METHODS);
        
        paatDocument.setDocumentHeader(new FinancialSystemDocumentHeader());
        paatDocument.setAdHocRoutePersons(new ArrayList<>());
        paatDocument.setAdHocRouteWorkgroups(new ArrayList<>());
        paatDocument.setNotes(new ArrayList<>());
        paatDocument.setOldMaintainableObject(createNewMaintainableForPAAT());
        paatDocument.setNewMaintainableObject(createNewMaintainableForPAAT());
        
        int nextId = documentIdCounter.incrementAndGet();
        String documentNumber = String.valueOf(nextId);
        paatDocument.setDocumentNumber(documentNumber);
        paatDocument.getDocumentHeader().setDocumentNumber(documentNumber);
        
        return paatDocument;
    }

    private FinancialSystemMaintainable createNewMaintainableForPAAT() {
        FinancialSystemMaintainable maintainable = new CuPayeeACHAccountMaintainableImpl();
        maintainable.setDataObjectClass(PayeeACHAccount.class);
        maintainable.setDataObject(new PayeeACHAccount());
        return maintainable;
    }

    private DataDictionaryService createMockDataDictionaryService() throws Exception {
        final String PAYEE_ACH_ACCOUNT_CLASSNAME = PayeeACHAccount.class.getName();
        final String DOCUMENT_HEADER_CLASSNAME = DocumentHeader.class.getName();
        /*
         * Only a few specific attribute definitions should be masked or have values finders; the rest
         * should be plain. Also, we only care about the max lengths of a few specific properties.
         */
        DataDictionaryService ddService = mock(DataDictionaryService.class);
        AttributeDefinition maskedAttribute = createMaskedAttributeDefinition();
        AttributeDefinition unmaskedAttribute = createUnmaskedAttributeDefinition();
        AttributeDefinition payeeIdTypeAttribute = createAttributeDefinitionWithValuesFinder(
                new TestPayeeAchIdTypeValuesFinder(), false);
        AttributeDefinition bankAccountTypeAttribute = createAttributeDefinitionWithValuesFinder(
                new CuCheckingSavingsValuesFinder(), true);
        
        when(ddService.getAttributeDefinition(PAYEE_ACH_ACCOUNT_CLASSNAME, PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE))
                .thenReturn(payeeIdTypeAttribute);
        when(ddService.getAttributeDefinition(PAYEE_ACH_ACCOUNT_CLASSNAME, PdpPropertyConstants.PAYEE_ID_NUMBER))
                .thenReturn(unmaskedAttribute);
        when(ddService.getAttributeDefinition(PAYEE_ACH_ACCOUNT_CLASSNAME, CUPdpPropertyConstants.BANK_ACCOUNT_TYPE_CODE))
                .thenReturn(bankAccountTypeAttribute);
        when(ddService.getAttributeDefinition(PAYEE_ACH_ACCOUNT_CLASSNAME, CUPdpPropertyConstants.PAYEE_ACH_BANK_NAME))
                .thenReturn(maskedAttribute);
        when(ddService.getAttributeDefinition(PAYEE_ACH_ACCOUNT_CLASSNAME, KFSPropertyConstants.BANK_ACCOUNT_NUMBER))
                .thenReturn(maskedAttribute);
        
        when(ddService.getAttributeMaxLength(DOCUMENT_HEADER_CLASSNAME, KRADPropertyConstants.DOCUMENT_DESCRIPTION))
                .thenReturn(DOCUMENT_DESCRIPTION_MAX_LENGTH);
        
        return ddService;
    }

    private AttributeDefinition createMaskedAttributeDefinition() {
        // We only care about the attribute being masked, not about any other setup like property name and max length.
        AttributeDefinition maskedDefinition = new AttributeDefinition();
        AttributeSecurity maskedSecurity = new AttributeSecurity();
        MaskFormatterLiteral literalMask = new MaskFormatterLiteral();
        maskedSecurity.setMask(true);
        literalMask.setLiteral(LITERAL_MASK_VALUE);
        maskedSecurity.setMaskFormatter(literalMask);
        maskedDefinition.setAttributeSecurity(maskedSecurity);
        return maskedDefinition;
    }

    private AttributeDefinition createUnmaskedAttributeDefinition() {
        // We only care about the attribute being unmasked, not about any other setup like property name and max length.
        return new AttributeDefinition();
    }

    private AttributeDefinition createAttributeDefinitionWithValuesFinder(KeyValuesFinder valuesFinder, boolean includeKeyInLabel) {
        // We only care about the values finder and include-key-in-label flag, not about any other setup like property name and max length.
        AttributeDefinition attrDefinition = new AttributeDefinition();
        SelectControlDefinition controlDefinition = new SelectControlDefinition();
        controlDefinition.setValuesFinder(valuesFinder);
        controlDefinition.setIncludeKeyInLabel(Boolean.valueOf(includeKeyInLabel));
        attrDefinition.setControl(controlDefinition);
        return attrDefinition;
    }

    private PersonService createMockPersonService() throws Exception {
        PersonService personService = mock(PersonService.class);
        Stream<ACHPersonPayeeFixture> fixtures = Stream.of(
                ACHPersonPayeeFixture.JOHN_DOE, ACHPersonPayeeFixture.JANE_DOE, ACHPersonPayeeFixture.ROBERT_SMITH,
                ACHPersonPayeeFixture.MARY_SMITH, ACHPersonPayeeFixture.KFS_SYSTEM_USER);
        
        fixtures.forEach((fixture) -> {
            when(personService.getPersonByPrincipalName(fixture.principalName))
                    .thenReturn(fixture.toPerson());
        });
        
        return personService;
    }

    private SequenceAccessorService createMockSequenceAccessorService() throws Exception {
        SequenceAccessorService sequenceAccessorService = mock(SequenceAccessorService.class);
        
        when(sequenceAccessorService.getNextAvailableSequenceNumber(PdpConstants.ACH_ACCOUNT_IDENTIFIER_SEQUENCE_NAME))
                .then((invocation) -> Long.valueOf(achAccountIdCounter.incrementAndGet()));
        
        return sequenceAccessorService;
    }

    private CuAchService createAchService() throws Exception {
        CuAchServiceImpl achService = new CuAchServiceImpl();
        achService.setBusinessObjectService(createMockBusinessObjectService());
        return achService;
    }

    private BusinessObjectService createMockBusinessObjectService() throws Exception {
        BusinessObjectService businessObjectService = mock(BusinessObjectService.class);
        
        Stream<PayeeACHAccountFixture> achFixtures = Stream.of(
                PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD, PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD);
        
        achFixtures.forEach((achFixture) -> {
            Map<String, Object> mapForMatching = createPropertiesMapForMatching(achFixture);
            when(businessObjectService.findMatching(PayeeACHAccount.class, mapForMatching))
                    .thenReturn(Collections.singletonList(achFixture.toPayeeACHAccount()));
        });
        
        return businessObjectService;
    }
    
    private DateTimeService createMockDateTimeService() {
        DateTimeService dateTimeService = mock(DateTimeService.class);
        Date currentDate = new Date(Calendar.getInstance().getTimeInMillis());
        when(dateTimeService.getCurrentSqlDate()).thenReturn(currentDate);
        return dateTimeService;
    }
    
    private PayeeACHAccountDocumentServiceImpl buildPayeeACHAccountDocumentServiceImpl() throws Exception {
        PayeeACHAccountDocumentServiceImpl payeeACHAccountDocumentService = Mockito.spy(new PayeeACHAccountDocumentServiceImpl());
        Mockito.doNothing().when(payeeACHAccountDocumentService).addNote(Mockito.any(), Mockito.anyString());
        
        payeeACHAccountDocumentService.setConfigurationService(createMockConfigurationService());
        payeeACHAccountDocumentService.setDataDictionaryService(createMockDataDictionaryService());
        payeeACHAccountDocumentService.setDocumentService(createMockDocumentService());
        payeeACHAccountDocumentService.setEmailService(new TestEmailService());
        payeeACHAccountDocumentService.setParameterService(createMockParameterService());
        payeeACHAccountDocumentService.setPersonService(createMockPersonService());
        payeeACHAccountDocumentService.setSequenceAccessorService(createMockSequenceAccessorService());
        return payeeACHAccountDocumentService;
    }

    private Map<String, Object> createPropertiesMapForMatching(PayeeACHAccountFixture achFixture) {
        Map<String, Object> propertiesMap = new HashMap<>();
        propertiesMap.put(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, achFixture.payeeIdentifierTypeCode);
        propertiesMap.put(PdpPropertyConstants.ACH_TRANSACTION_TYPE, achFixture.achTransactionType);
        propertiesMap.put(PdpPropertyConstants.PAYEE_ID_NUMBER, achFixture.getPayeeIdNumber());
        return propertiesMap;
    }

    private AchBankService createMockAchBankService() throws Exception {
        AchBankService achBankService = mock(AchBankService.class);
        Stream<ACHBankFixture> banks = Stream.of(ACHBankFixture.FIRST_BANK, ACHBankFixture.SECOND_BANK);
        
        banks.forEach((bankFixture) -> {
            when(achBankService.getByPrimaryId(bankFixture.bankRoutingNumber))
                    .thenReturn(bankFixture.toACHBank());
        });
        
        return achBankService;
    }

    private ConfigurationService createMockConfigurationService() throws Exception {
        ConfigurationService configurationService = mock(ConfigurationService.class);
        
        when(configurationService.getPropertyValueAsString(KFSConstants.GLOBAL_ERRORS))
                .thenReturn(GLOBAL_ERROR_FORMAT);
        
        return configurationService;
    }
    

    private PayeeACHAccountExtractReportService createMockPayeeACHAccountExtractReportService() {              
        return mock(PayeeACHAccountExtractReportService.class);
    }

    private static class TestEmailService extends EmailServiceImpl {
        @Override
        protected String getMode() {
            return MODE_LOG;
        }
    }

    private static class TestPayeeACHAccountExtractService extends PayeeACHAccountExtractServiceImpl {
        private Map<String, ACHFileResult> fileResults = new HashMap<>();
        private ACHFileResult currentFileResult;
        private ACHRowResult currentRowResult;
        
        @Override
        protected PayeeACHAccountExtractFileResult loadACHBatchDetailFile(String inputFileName, BatchInputFileType batchInputFileType) {
            currentFileResult = new ACHFileResult();
            
            try {
                return super.loadACHBatchDetailFile(inputFileName, batchInputFileType);
            } catch (Exception e) {
                currentFileResult.markAsUnprocessable();
                throw e;
            } finally {
            		if (CollectionUtils.isEmpty(currentFileResult.getRowResults())) {
            			currentFileResult.markAsUnprocessable();
            		}
                fileResults.put(generateFileResultKey(inputFileName), currentFileResult);
                currentFileResult = null;
            }
        }
        
        private String generateFileResultKey(String inputFileName) {
            String key = inputFileName;
            if (StringUtils.contains(key, CUPdpTestConstants.BACKSLASH)) {
                key = StringUtils.substringAfterLast(inputFileName, CUPdpTestConstants.BACKSLASH);
            }
            if (StringUtils.contains(key, CUKFSConstants.SLASH)) {
                key = StringUtils.substringAfterLast(key, CUKFSConstants.SLASH);
            }
            key = StringUtils.substringBeforeLast(key, CUKFSConstants.DELIMITER);
            return key;
        }
        
        @Override
        protected String processACHBatchDetail(PayeeACHAccountExtractDetail achDetail) {
            currentRowResult = new ACHRowResult();
            
            try {
                String failureMessage = super.processACHBatchDetail(achDetail);
                if (StringUtils.isNotBlank(failureMessage)) {
                    currentRowResult.markRowAsInvalid();
                }
                return failureMessage;
            } catch (Exception e) {
                currentRowResult.markRowAsInvalid();
                throw e;
            } finally {
                currentFileResult.addRowResult(currentRowResult);
                currentRowResult = null;
            }
        }
        
        public void addRoutedDocument(MaintenanceDocument routedDocument) {
            FinancialSystemMaintainable newMaintainable = (FinancialSystemMaintainable) routedDocument.getNewMaintainableObject();
            PayeeACHAccount achAccount = (PayeeACHAccount) newMaintainable.getDataObject();
            
            if (StringUtils.equals(PayeeIdTypeCodes.EMPLOYEE, achAccount.getPayeeIdentifierTypeCode())) {
                currentRowResult.setDocumentForEmployeeAccountUpdate(routedDocument);
            } else if (StringUtils.equals(PayeeIdTypeCodes.ENTITY, achAccount.getPayeeIdentifierTypeCode())) {
                currentRowResult.setDocumentForEntityAccountUpdate(routedDocument);
            } else {
                throw new IllegalStateException("Unexpected payee ID type: " + achAccount.getPayeeIdentifierTypeCode());
            }
        }
        
        public Map<String, ACHFileResult> getFileResults() {
            return fileResults;
        }
    }

    private static class ACHFileResult {
        private boolean processableFile;
        private List<ACHRowResult> rowResults;
        
        public ACHFileResult() {
            this.processableFile = true;
            this.rowResults = new ArrayList<>();
        }
        
        public void markAsUnprocessable() {
            processableFile = false;
        }

        public boolean isProcessableFile() {
            return processableFile;
        }
        
        public void addRowResult(ACHRowResult rowResult) {
            rowResults.add(rowResult);
        }
        
        public List<ACHRowResult> getRowResults() {
            return rowResults;
        }
    }

    private static class ACHRowResult {
        private MaintenanceDocument documentForEmployeeAccountUpdate;
        private MaintenanceDocument documentForEntityAccountUpdate;
        private boolean validRow;

        public ACHRowResult() {
            validRow = true;
        }

        public MaintenanceDocument getDocumentForEmployeeAccountUpdate() {
            return documentForEmployeeAccountUpdate;
        }

        public void setDocumentForEmployeeAccountUpdate(MaintenanceDocument documentForEmployeeAccountUpdate) {
            this.documentForEmployeeAccountUpdate = documentForEmployeeAccountUpdate;
        }

        public MaintenanceDocument getDocumentForEntityAccountUpdate() {
            return documentForEntityAccountUpdate;
        }

        public void setDocumentForEntityAccountUpdate(MaintenanceDocument documentForEntityAccountUpdate) {
            this.documentForEntityAccountUpdate = documentForEntityAccountUpdate;
        }

        public boolean isValidRow() {
            return validRow;
        }

        public void markRowAsInvalid() {
            this.validRow = false;
        }
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailNoClean() {
        String bankAccountNumber = "12345";
        validateCleanPayeeACHAccountExtractDetailBankAccountNumber(bankAccountNumber, bankAccountNumber);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailCleanDashes() {
        String bankAccountNumber = "1-2---3-4-5----6-";
        String expectedBankAccount = "123456";
        validateCleanPayeeACHAccountExtractDetailBankAccountNumber(bankAccountNumber, expectedBankAccount);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailCleanDashesWithLetters() {
        String bankAccountNumber = "A1-2---3-4-5---6-";
        String expectedBankAccount = "A123456";
        validateCleanPayeeACHAccountExtractDetailBankAccountNumber(bankAccountNumber, expectedBankAccount);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithLetters() {
        String bankAccountNumber = "A12345";
        validateCleanPayeeACHAccountExtractDetailBankAccountNumber(bankAccountNumber, bankAccountNumber);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithNull() {
        validateCleanPayeeACHAccountExtractDetailBankAccountNumber(null, null);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithEmptyString() {
        validateCleanPayeeACHAccountExtractDetailBankAccountNumber(StringUtils.EMPTY, StringUtils.EMPTY);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithSpaces() {
        validateCleanPayeeACHAccountExtractDetailBankAccountNumber(" 1 23  4 5 6 ", "123456");
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithSpacesAndDashes() {
        validateCleanPayeeACHAccountExtractDetailBankAccountNumber(" 1 2----3  4 - 5 6-7 ", "1234567");
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithSpacesAndDashesAndLetters() {
        validateCleanPayeeACHAccountExtractDetailBankAccountNumber("A 1 2----3  4 - 5 6-7 B ", "A1234567B");
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithJustSpaces() {
        validateCleanPayeeACHAccountExtractDetailBankAccountNumber("   ", StringUtils.EMPTY);
    }

    @Test
    public void testBlankBankName() {
        validateCleanPayeeACHAccountExtractDetailBankName("   ", StringUtils.EMPTY);
    }

    @Test
    public void testStandardBankName() {
        validateCleanPayeeACHAccountExtractDetailBankName("Example Bank", "Example Bank");
    }

    @Test
    public void testLongBankName() {
        validateCleanPayeeACHAccountExtractDetailBankName("Tompkins Community Bank and Sons forty characters test", "Tompkins Community Bank and Sons forty c");
    }

    private void validateCleanPayeeACHAccountExtractDetailBankAccountNumber(String bankAccountNumber, String expectedCleanedBankAccountNumber) {
        PayeeACHAccountExtractDetail detail = new PayeeACHAccountExtractDetail();
        detail.setBankAccountNumber(bankAccountNumber);
        payeeACHAccountExtractService.cleanPayeeACHAccountExtractDetail(detail);
        
        assertEquals(expectedCleanedBankAccountNumber, detail.getBankAccountNumber());
    }

    private void validateCleanPayeeACHAccountExtractDetailBankName(String bankName, String expectedCleanedBankName) {
        PayeeACHAccountExtractDetail detail = new PayeeACHAccountExtractDetail();
        detail.setBankName(bankName);
        payeeACHAccountExtractService.cleanPayeeACHAccountExtractDetail(detail);

        assertEquals(expectedCleanedBankName, detail.getBankName());
    }

}
