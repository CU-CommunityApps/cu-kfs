package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.kns.datadictionary.control.SelectControlDefinition;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.service.impl.DataDictionaryServiceImpl;
import org.kuali.kfs.krad.bo.DocumentHeader;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.datadictionary.AttributeDefinition;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.datadictionary.mask.MaskFormatterLiteral;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.service.impl.DocumentServiceImpl;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.businessobject.ACHBank;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.document.PayeeACHAccountMaintainableImpl;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.pdp.service.AchService;
import org.kuali.kfs.pdp.service.impl.AchBankServiceImpl;
import org.kuali.kfs.pdp.service.impl.AchServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.impl.BatchInputFileServiceImpl;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.kfs.sys.service.impl.EmailServiceImpl;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.impl.identity.PersonServiceImpl;
import org.kuali.rice.krad.bo.BusinessObject;

import edu.cornell.kfs.coa.businessobject.options.CUCheckingSavingsValuesFinder;
import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.CUPdpTestConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractCsv;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractCsvInputFileType;
import edu.cornell.kfs.pdp.batch.fixture.ACHBankFixture;
import edu.cornell.kfs.pdp.batch.fixture.ACHFileFixture;
import edu.cornell.kfs.pdp.batch.fixture.ACHPersonPayeeFixture;
import edu.cornell.kfs.pdp.batch.fixture.ACHRowFixture;
import edu.cornell.kfs.pdp.batch.fixture.ACHUpdateFixture;
import edu.cornell.kfs.pdp.batch.fixture.PayeeACHAccountFixture;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;
import edu.cornell.kfs.sys.service.mock.MockParameterServiceImpl;

@SuppressWarnings("deprecation")
public class PayeeACHAccountExtractServiceImplTest {

    private static final String BANK_ACCOUNT_NUMBER = "bankAccountNumber";
    private static final String BANK_NAME = "bankRouting.bankName";
    private static final String BANK_ACCOUNT_TYPE_CODE = "bankAccountTypeCode";
    private static final Integer DOCUMENT_DESCRIPTION_MAX_LENGTH = Integer.valueOf(40);

    private static final String ACH_SOURCE_FILE_PATH = "src/test/resources/edu/cornell/kfs/pdp/batch/service/fixture";
    private static final String ACH_TESTING_FILE_PATH = "test/pdp/payeeACHAccountExtract";
    private static final String ACH_TESTING_DIRECTORY = ACH_TESTING_FILE_PATH + "/";
    private static final String FILE_PATH_FORMAT = "{0}/{1}.{2}";
    private static final String ACH_CSV_FILE_EXTENSION = "csv";
    private static final String ACH_DONE_FILE_EXTENSION = "done";

    private static final String UNRESOLVED_EMAIL_STRING = "Payment $ \\n\\ for [payeeIdentifierTypeCode] of [payeeIdNumber]"
            + " will go to [bankAccountTypeCode] account at [bankRouting.bankName].[bankAccountNumber]";
    private static final String EMAIL_STRING_AS_FORMAT = "Payment $ \n\\ for {0} of {1} will go to {2} account at {3}.";
    private static final String PERSONAL_SAVINGS_ACCOUNT_TYPE_LABEL = "Personal Savings";

    private TestPayeeACHAccountExtractService payeeACHAccountExtractService;

    @Before
    public void setUp() throws Exception {
        payeeACHAccountExtractService = new TestPayeeACHAccountExtractService();
        payeeACHAccountExtractService.setBatchInputFileService(new BatchInputFileServiceImpl());
        payeeACHAccountExtractService.setBatchInputFileTypes(
                Collections.singletonList(createACHBatchInputFileType()));
        payeeACHAccountExtractService.setParameterService(new MockParameterServiceImpl());
        payeeACHAccountExtractService.setEmailService(new EmailServiceImpl());
        payeeACHAccountExtractService.setDocumentService(createMockDocumentService());
        payeeACHAccountExtractService.setDataDictionaryService(createMockDataDictionaryService());
        payeeACHAccountExtractService.setPersonService(createMockPersonService());
        payeeACHAccountExtractService.setSequenceAccessorService(new MockSequenceAccessorService());
        payeeACHAccountExtractService.setAchService(createMockAchService());
        payeeACHAccountExtractService.setAchBankService(createMockAchBankService());
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
    public void testLoadMultipleFiles() throws Exception {
        assertACHAccountExtractionHasCorrectResults(ACHFileFixture.FILE_WITH_BAD_HEADERS,
                ACHFileFixture.FILE_WITH_SINGLE_SUCCESSFUL_LINE, ACHFileFixture.FILE_WITH_GOOD_AND_BAD_LINES,
                ACHFileFixture.FILE_WITH_SINGLE_MATCHING_LINE, ACHFileFixture.FILE_WITH_PARTIAL_AND_FULL_MATCHES);
    }

    @Test
    public void testEmailBodyPlaceholderResolution() throws Exception {
        // NOTE: We expect that all potentially-sensitive placeholders except bank name will be replaced with empty text.
        PayeeACHAccount achAccount = PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW.toPayeeACHAccount();
        String expectedBody = MessageFormat.format(EMAIL_STRING_AS_FORMAT, achAccount.getPayeeIdentifierTypeCode(),
                achAccount.getPayeeIdNumber(), PERSONAL_SAVINGS_ACCOUNT_TYPE_LABEL, achAccount.getBankRouting().getBankName());
        String actualBody = payeeACHAccountExtractService.getResolvedEmailBody(achAccount, UNRESOLVED_EMAIL_STRING);
        
        assertEquals("Email body placeholders and special characters were not resolved properly", expectedBody, actualBody);
    }

    private void assertACHAccountExtractionHasCorrectResults(ACHFileFixture... fixtures) throws Exception {
        List<ACHFileFixture> expectedResults = Arrays.asList(fixtures);
        String[] fileNames = Arrays.stream(fixtures)
                .map(ACHFileFixture::getBaseFileName)
                .toArray(String[]::new);
        
        copyInputFilesAndGenerateDoneFiles(fileNames);
        payeeACHAccountExtractService.processACHBatchDetails();
        
        assertFilesHaveExpectedResults(expectedResults, payeeACHAccountExtractService.getFileResults());
        assertDoneFilesWereDeleted(fileNames);
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
        assertPayeeACHAccountIsCorrect(expectedUpdate.newAccount, newMaintainable);
        
        if (StringUtils.equals(KFSConstants.MAINTENANCE_EDIT_ACTION, expectedUpdate.getExpectedMaintenanceAction())) {
            FinancialSystemMaintainable oldMaintainable = (FinancialSystemMaintainable) document.getOldMaintainableObject();
            assertTrue("Old maintainable should not be null", ObjectUtils.isNotNull(oldMaintainable));
            assertEquals("Wrong data object class on old maintainable", PayeeACHAccount.class, oldMaintainable.getDataObjectClass());
            assertPayeeACHAccountIsCorrect(expectedUpdate.oldAccount, oldMaintainable);
        }
    }

    private void assertPayeeACHAccountIsCorrect(
            PayeeACHAccountFixture expectedAccountFixture, FinancialSystemMaintainable maintainable) throws Exception {
        PayeeACHAccount expectedAccount = expectedAccountFixture.toPayeeACHAccount();
        PayeeACHAccount actualAccount = (PayeeACHAccount) maintainable.getDataObject();
        
        assertTrue("ACH account should not be null", ObjectUtils.isNotNull(actualAccount));
        assertEquals("Wrong payee ID type code", expectedAccount.getPayeeIdentifierTypeCode(), actualAccount.getPayeeIdentifierTypeCode());
        assertEquals("Wrong payee ID number", expectedAccount.getPayeeIdNumber(), actualAccount.getPayeeIdNumber());
        assertEquals("Wrong transaction type", expectedAccount.getAchTransactionType(), actualAccount.getAchTransactionType());
        assertEquals("Wrong bank routing number", expectedAccount.getBankRoutingNumber(), actualAccount.getBankRoutingNumber());
        assertEquals("Wrong bank account type", expectedAccount.getBankAccountTypeCode(), actualAccount.getBankAccountTypeCode());
        assertEquals("Wrong bank account number", expectedAccount.getBankAccountNumber(), actualAccount.getBankAccountNumber());
    }

    private void assertDoneFilesWereDeleted(String... inputFileNames) throws Exception {
        for (String inputFileName : inputFileNames) {
            File doneFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_TESTING_FILE_PATH, inputFileName, ACH_DONE_FILE_EXTENSION));
            assertFalse("There should not be a .done file for " + inputFileName, doneFile.exists());
        }
    }

    protected void copyInputFilesAndGenerateDoneFiles(String... inputFileNames) throws IOException {
        for (String inputFileName : inputFileNames) {
            File sourceFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_SOURCE_FILE_PATH, inputFileName, ACH_CSV_FILE_EXTENSION));
            File destFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_TESTING_FILE_PATH, inputFileName, ACH_CSV_FILE_EXTENSION));
            File doneFile = new File(MessageFormat.format(FILE_PATH_FORMAT, ACH_TESTING_FILE_PATH, inputFileName, ACH_DONE_FILE_EXTENSION));
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
        fileType.setFileExtension(ACH_CSV_FILE_EXTENSION);
        fileType.setCsvEnumClass(PayeeACHAccountExtractCsv.class);
        return fileType;
    }

    private DocumentService createMockDocumentService() throws Exception {
        DocumentService mockDocumentService = EasyMock.createMock(DocumentServiceImpl.class);
        
        EasyMock.expect(mockDocumentService.getNewDocument(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_MAINT_DOC_TYPE))
                .andStubAnswer(this::createMockPAATDocument);
        
        Capture<Document> documentArg = EasyMock.newCapture();
        EasyMock.expect(
                mockDocumentService.routeDocument(EasyMock.capture(documentArg), EasyMock.anyObject(), EasyMock.anyObject()))
                .andStubAnswer(() -> recordAndReturnDocument(documentArg.getValue()));
        
        EasyMock.replay(mockDocumentService);
        return mockDocumentService;
    }

    private Document recordAndReturnDocument(Document document) {
        payeeACHAccountExtractService.addRoutedDocument((MaintenanceDocument) document);
        return document;
    }

    private MaintenanceDocument createMockPAATDocument() throws Exception {
        MaintenanceDocument paatDocument = EasyMock.partialMockBuilder(FinancialSystemMaintenanceDocument.class)
                .createNiceMock();
        EasyMock.replay(paatDocument);
        
        paatDocument.setDocumentHeader(new FinancialSystemDocumentHeader());
        paatDocument.setAdHocRoutePersons(new ArrayList<>());
        paatDocument.setAdHocRouteWorkgroups(new ArrayList<>());
        paatDocument.setNotes(new ArrayList<>());
        paatDocument.setOldMaintainableObject(createNewMaintainableForPAAT());
        paatDocument.setNewMaintainableObject(createNewMaintainableForPAAT());
        
        return paatDocument;
    }

    private FinancialSystemMaintainable createNewMaintainableForPAAT() {
        FinancialSystemMaintainable maintainable = new PayeeACHAccountMaintainableImpl();
        maintainable.setBoClass(PayeeACHAccount.class);
        maintainable.setDataObject(new PayeeACHAccount());
        return maintainable;
    }

    private DataDictionaryService createMockDataDictionaryService() throws Exception {
        /*
         * Only a few specific attribute definitions should be masked or have values finders; the rest
         * should be plain. Also, we only care about the max lengths of a few specific properties.
         */
        DataDictionaryService ddService = EasyMock.createMock(DataDictionaryServiceImpl.class);
        
        EasyMock.expect(ddService.getAttributeDefinition(EasyMock.eq(PayeeACHAccount.class.getName()),
                EasyMock.or(EasyMock.eq(BANK_ACCOUNT_NUMBER), EasyMock.eq(BANK_NAME)))).andStubReturn(createMaskedAttributeDefinition());
        
        EasyMock.expect(ddService.getAttributeDefinition(PayeeACHAccount.class.getName(), BANK_ACCOUNT_TYPE_CODE)).andStubReturn(
                createAttributeDefinitionWithValuesFinder(CUCheckingSavingsValuesFinder.class.getName(), true));
        
        EasyMock.expect(ddService.getAttributeDefinition(EasyMock.eq(PayeeACHAccount.class.getName()), EasyMock.and(
                EasyMock.not(EasyMock.eq(BANK_ACCOUNT_TYPE_CODE)), EasyMock.and(
                        EasyMock.not(EasyMock.eq(BANK_ACCOUNT_NUMBER)), EasyMock.not(EasyMock.eq(BANK_NAME)))
                ))).andStubReturn(createUnmaskedAttributeDefinition());
        
        EasyMock.expect(ddService.getAttributeMaxLength(
                DocumentHeader.class.getName(), KRADPropertyConstants.DOCUMENT_DESCRIPTION)).andStubReturn(DOCUMENT_DESCRIPTION_MAX_LENGTH);
        
        EasyMock.replay(ddService);
        return ddService;
    }

    private AttributeDefinition createMaskedAttributeDefinition() {
        // We only care about the attribute being masked, not about any other setup like property name and max length.
        AttributeDefinition maskedDefinition = new AttributeDefinition();
        AttributeSecurity maskedSecurity = new AttributeSecurity();
        MaskFormatterLiteral literalMask = new MaskFormatterLiteral();
        maskedSecurity.setMask(true);
        literalMask.setLiteral("********");
        maskedSecurity.setMaskFormatter(literalMask);
        maskedDefinition.setAttributeSecurity(maskedSecurity);
        return maskedDefinition;
    }

    private AttributeDefinition createUnmaskedAttributeDefinition() {
        // We only care about the attribute being unmasked, not about any other setup like property name and max length.
        return new AttributeDefinition();
    }

    private AttributeDefinition createAttributeDefinitionWithValuesFinder(String valuesFinderClass, boolean includeKeyInLabel) {
        // We only care about the values finder and include-key-in-label flag, not about any other setup like property name and max length.
        AttributeDefinition attrDefinition = new AttributeDefinition();
        SelectControlDefinition controlDefinition = new SelectControlDefinition();
        controlDefinition.setValuesFinderClass(valuesFinderClass);
        controlDefinition.setIncludeKeyInLabel(Boolean.valueOf(includeKeyInLabel));
        attrDefinition.setControl(controlDefinition);
        return attrDefinition;
    }

    private PersonService createMockPersonService() throws Exception {
        Map<String, Person> personMap = Stream
                .of(ACHPersonPayeeFixture.JOHN_DOE, ACHPersonPayeeFixture.JANE_DOE, ACHPersonPayeeFixture.ROBERT_SMITH,
                        ACHPersonPayeeFixture.MARY_SMITH, ACHPersonPayeeFixture.KFS_SYSTEM_USER)
                .collect(Collectors.toMap(
                        ACHPersonPayeeFixture::getPrincipalName, ACHPersonPayeeFixture::toPerson));
        
        Capture<String> principalNameArg = EasyMock.newCapture();
        PersonService personService = EasyMock.createMock(PersonServiceImpl.class);
        EasyMock.expect(
                personService.getPersonByPrincipalName(EasyMock.capture(principalNameArg)))
                .andStubAnswer(() -> personMap.get(principalNameArg.getValue()));
        EasyMock.replay(personService);
        
        return personService;
    }

    private AchService createMockAchService() throws Exception {
        Map<String, Map<String, PayeeACHAccount>> achAccountsMap = buildNestedMapOfACHAccountsByTypeAndId(
                PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD, PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD,
                PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD);
        
        AchService achService = EasyMock.createMock(AchServiceImpl.class);
        Capture<String> payeeIdTypeCodeArg = EasyMock.newCapture();
        Capture<String> payeeIdNumberArg = EasyMock.newCapture();
        
        IAnswer<PayeeACHAccount> achInformationMethodHandler = () -> {
            Map<String, PayeeACHAccount> achAccountsForType = achAccountsMap.get(payeeIdTypeCodeArg.getValue());
            if (achAccountsForType != null) {
                return achAccountsForType.get(payeeIdNumberArg.getValue());
            } else {
                return null;
            }
        };
        
        EasyMock.expect(
                achService.getAchInformation(EasyMock.capture(payeeIdTypeCodeArg),
                        EasyMock.capture(payeeIdNumberArg), EasyMock.eq(CUPdpTestConstants.DIRECT_DEPOSIT_TYPE)))
                .andStubAnswer(achInformationMethodHandler);
        
        EasyMock.replay(achService);
        return achService;
    }

    private Map<String, Map<String, PayeeACHAccount>> buildNestedMapOfACHAccountsByTypeAndId(PayeeACHAccountFixture... fixtures) {
        return Arrays.stream(fixtures)
                .collect(Collectors.groupingBy(PayeeACHAccountFixture::getPayeeIdentifierTypeCode,
                        Collectors.toMap(PayeeACHAccountFixture::getPayeeIdNumber, PayeeACHAccountFixture::toPayeeACHAccount)));
    }

    private AchBankService createMockAchBankService() throws Exception {
        Map<String, ACHBank> banksMap = Stream.of(ACHBankFixture.FIRST_BANK, ACHBankFixture.SECOND_BANK)
                .collect(Collectors.toMap(ACHBankFixture::getBankRoutingNumber, ACHBankFixture::toACHBank));
        
        AchBankService achBankService = EasyMock.createMock(AchBankServiceImpl.class);
        Capture<String> bankRoutingNumberArg = EasyMock.newCapture();
        
        EasyMock.expect(
                achBankService.getByPrimaryId(EasyMock.capture(bankRoutingNumberArg)))
                .andStubAnswer(() -> banksMap.get(bankRoutingNumberArg.getValue()));
        
        EasyMock.replay(achBankService);
        return achBankService;
    }

    private static class MockSequenceAccessorService implements SequenceAccessorService {
        private long currentValue;

        @Override
        public Long getNextAvailableSequenceNumber(String sequenceName, Class<? extends BusinessObject> clazz) {
            return getNextAvailableSequenceNumber(sequenceName);
        }

        @Override
        public Long getNextAvailableSequenceNumber(String sequenceName) {
            if (!PdpConstants.ACH_ACCOUNT_IDENTIFIER_SEQUENCE_NAME.equals(sequenceName)) {
                throw new RuntimeException("Unexpected sequence name: " + sequenceName);
            }
            currentValue++;
            return Long.valueOf(currentValue);
        }
        
    }

    private static class TestPayeeACHAccountExtractService extends PayeeACHAccountExtractServiceImpl {
        private Map<String, ACHFileResult> fileResults = new HashMap<>();
        private ACHFileResult currentFileResult;
        private ACHRowResult currentRowResult;
        
        @Override
        protected List<String> loadACHBatchDetailFile(String inputFileName, BatchInputFileType batchInputFileType) {
            currentFileResult = new ACHFileResult();
            
            try {
                return super.loadACHBatchDetailFile(inputFileName, batchInputFileType);
            } catch (Exception e) {
                currentFileResult.markAsUnprocessable();
                throw e;
            } finally {
                fileResults.put(generateFileResultKey(inputFileName), currentFileResult);
                currentFileResult = null;
            }
        }
        
        private String generateFileResultKey(String inputFileName) {
            String key = inputFileName;
            if (StringUtils.contains(key, "\\")) {
                key = StringUtils.substringAfterLast(inputFileName, "\\");
            }
            if (StringUtils.contains(key, "/")) {
                key = StringUtils.substringAfterLast(key, "/");
            }
            key = StringUtils.substringBeforeLast(key, ".");
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
        
        @Override
        protected Note createEmptyNote() {
            Note note = EasyMock.partialMockBuilder(Note.class)
                    .addMockedMethod("setNotePostedTimestampToCurrent")
                    .createNiceMock();
            
            note.setNotePostedTimestampToCurrent();
            EasyMock.expectLastCall().anyTimes();
            
            EasyMock.replay(note);
            return note;
        }
        
        // Increase visibility to "public" for testing convenience.
        @Override
        public String getResolvedEmailBody(PayeeACHAccount achAccount, String emailBody) {
            return super.getResolvedEmailBody(achAccount, emailBody);
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

}
