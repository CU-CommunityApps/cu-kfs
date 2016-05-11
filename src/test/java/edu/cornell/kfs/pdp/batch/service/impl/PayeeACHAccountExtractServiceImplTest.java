package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
import org.kuali.kfs.sys.document.FinancialSystemMaintainable;
import org.kuali.kfs.sys.service.impl.DevelopmentMailServiceImpl;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.impl.identity.PersonImpl;
import org.kuali.rice.kim.impl.identity.PersonServiceImpl;
import org.kuali.rice.kns.datadictionary.control.SelectControlDefinition;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.kns.document.MaintenanceDocumentBase;
import org.kuali.rice.kns.service.impl.DataDictionaryServiceImpl;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.bo.DocumentHeader;
import org.kuali.rice.krad.bo.Note;
import org.kuali.rice.krad.datadictionary.AttributeDefinition;
import org.kuali.rice.krad.datadictionary.AttributeSecurity;
import org.kuali.rice.krad.datadictionary.mask.MaskFormatterLiteral;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.DataDictionaryService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.SequenceAccessorService;
import org.kuali.rice.krad.service.impl.DocumentServiceImpl;
import org.kuali.rice.krad.util.KRADPropertyConstants;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.options.CUCheckingSavingsValuesFinder;
import edu.cornell.kfs.pdp.CUPdpConstants;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractCsv;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractCsvInputFileType;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;
import edu.cornell.kfs.sys.service.mock.MockParameterServiceImpl;

@SuppressWarnings("deprecation")
public class PayeeACHAccountExtractServiceImplTest {

    private static final String BANK_ACCOUNT_NUMBER = "bankAccountNumber";
    private static final String BANK_NAME = "bankRouting.bankName";
    private static final String BANK_ACCOUNT_TYPE_CODE = "bankAccountTypeCode";
    private static final Integer DOCUMENT_DESCRIPTION_MAX_LENGTH = Integer.valueOf(40);

    private static final String ACH_SOURCE_FILE_PATH = "src/test/java/edu/cornell/kfs/pdp/batch/service/fixture";
    private static final String ACH_TESTING_FILE_PATH = "test/pdp/payeeACHAccountExtract";
    private static final String ACH_TESTING_DIRECTORY = ACH_TESTING_FILE_PATH + "/";
    private static final String FILE_PATH_FORMAT = "{0}/{1}.{2}";
    private static final String ACH_CSV_FILE_EXTENSION = "csv";
    private static final String ACH_DONE_FILE_EXTENSION = "done";

    private static final String BAD_HEADERS_FILE = "pdp_ach_test_badHeaders";
    private static final String BAD_LINES_FILE = "pdp_ach_test_badLines";
    private static final String GOOD_LINES_FILE = "pdp_ach_test_good";
    private static final String MIX_GOOD_BAD_LINES_FILE = "pdp_ach_test_mixGoodBadLines";

    private static final String KFS_DIRECT_DEPOSIT_TYPE = "PRAP";
    private static final String COMPLETED_DATE_FROM_TEST_FILES = "01/01/2016";

    private static final String TEST_PRINCIPALID = "1234567";
    private static final String TEST_PRINCIPALNAME = "jad987";
    private static final String TEST_ENTITYID = "2345678";
    private static final String TEST_NAME = "Doe, John A.";
    private static final String TEST_EMPLOYEEID = "1122333";
    private static final String TEST_EMAIL = "jad987@someplace.edu";
    private static final String TEST_BANK_ROUTING_NUMBER = "111000999";
    private static final String TEST_BANK_NAME = "First Bank";
    private static final String TEST_BANK_ACCOUNT_TYPE = CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_CHECKING_ACCOUNT_TYPE;
    private static final String TEST_ACCOUNT_NUMBER = "44333222111";

    private static final String TEST_ALT_PRINCIPALID = "9876543";
    private static final String TEST_ALT_PRINCIPALNAME = "jd8888";
    private static final String TEST_ALT_ENTITYID = "8765432";
    private static final String TEST_ALT_NAME = "Doe, Jane";
    private static final String TEST_ALT_EMPLOYEEID = "4455666";
    private static final String TEST_ALT_EMAIL = "jd8888@someplace.edu";
    private static final String TEST_ALT_BANK_ROUTING_NUMBER = "666777888";
    private static final String TEST_ALT_BANK_NAME = "Second Bank";
    private static final String TEST_ALT_BANK_ACCOUNT_TYPE = CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_SAVINGS_ACCOUNT_TYPE;
    private static final String TEST_ALT_ACCOUNT_NUMBER = "66555444333";

    private static final String UNRESOLVED_EMAIL_STRING
            = "Payment $ \\n\\ for [payeeIdentifierTypeCode] of [payeeIdNumber] will go to [bankAccountTypeCode] account at [bankRouting.bankName].[bankAccountNumber]";
    private static final String EMAIL_STRING_AS_FORMAT = "Payment $ \n\\ for {0} of {1} will go to {2} account at {3}.";
    private static final String PERSONAL_SAVINGS_ACCOUNT_TYPE_LABEL = "Personal Savings";

    private TestPayeeACHAccountExtractService payeeACHAccountExtractService;
    
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PayeeACHAccountExtractServiceImplTest.class);

    @Before
    public void setUp() throws Exception {
        payeeACHAccountExtractService = new TestPayeeACHAccountExtractService();
        payeeACHAccountExtractService.setBatchInputFileService(new BatchInputFileServiceImpl());
        payeeACHAccountExtractService.setBatchInputFileTypes(Collections.<BatchInputFileType>singletonList(getBatchInputFileType()));
        payeeACHAccountExtractService.setParameterService(new MockParameterServiceImpl());
        payeeACHAccountExtractService.setMailService(new DevelopmentMailServiceImpl());
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
    public void testLoadValidFile() throws Exception {
        // Load only the "good" file, which we expect to completely succeed.
        List<PayeeACHAccountExtractDetail> expectedSuccessfulDetails = Arrays.asList(
                getExpectedExtractDetailForTestAccount());
        
        copyInputFilesAndGenerateDoneFiles(GOOD_LINES_FILE);
        payeeACHAccountExtractService.processACHBatchDetails();
        assertFileAndRowLoadingResultsAreCorrect(
                new AchFileResults(1, 0, 0), getExpectedResultsForGoodFile(), expectedSuccessfulDetails);
        assertDoneFilesWereDeleted(GOOD_LINES_FILE);
    }

    @Test
    public void testLoadFileWithInvalidFormat() throws Exception {
        // Load only the badly-formatted file, which we expect to fail altogether.
        copyInputFilesAndGenerateDoneFiles(BAD_HEADERS_FILE);
        payeeACHAccountExtractService.processACHBatchDetails();
        assertFileAndRowLoadingResultsAreCorrect(
                new AchFileResults(0, 0, 1), new AchRowResults(), Collections.<PayeeACHAccountExtractDetail>emptyList());
        assertDoneFilesWereDeleted(BAD_HEADERS_FILE);
    }

    @Test
    public void testLoadFileWithAllInvalidRows() throws Exception {
        // Load only the rows-with-bad-data file, which we expect to load but to have all of its data lines rejected (due to validation problems).
        copyInputFilesAndGenerateDoneFiles(BAD_LINES_FILE);
        payeeACHAccountExtractService.processACHBatchDetails();
        assertFileAndRowLoadingResultsAreCorrect(
                new AchFileResults(0, 1, 0), getExpectedResultsForBadLinesFile(), Collections.<PayeeACHAccountExtractDetail>emptyList());
        assertDoneFilesWereDeleted(BAD_LINES_FILE);
    }

    @Test
    public void testLoadFileWithSomeInvalidRows() throws Exception {
        // Load only the some-good-and-bad-rows file, which we expect to load but to have some of its data lines rejected (due to validation problems).
        copyInputFilesAndGenerateDoneFiles(MIX_GOOD_BAD_LINES_FILE);
        payeeACHAccountExtractService.processACHBatchDetails();
        assertFileAndRowLoadingResultsAreCorrect(
                new AchFileResults(0, 1, 0), getExpectedResultsForMixedGoodBadLinesFile(), Collections.<PayeeACHAccountExtractDetail>emptyList());
        assertDoneFilesWereDeleted(MIX_GOOD_BAD_LINES_FILE);
    }

    @Test
    public void testLoadMultipleFiles() throws Exception {
        // Load the badly-formatted file, the all-invalid-data file, and the some-invalid-data file.
        AchRowResults expectedRowResults = AchRowResults.createSummarizedResults(getExpectedResultsForBadLinesFile(), getExpectedResultsForMixedGoodBadLinesFile());
        copyInputFilesAndGenerateDoneFiles(BAD_HEADERS_FILE, BAD_LINES_FILE, MIX_GOOD_BAD_LINES_FILE);
        payeeACHAccountExtractService.processACHBatchDetails();
        assertFileAndRowLoadingResultsAreCorrect(
                new AchFileResults(0, 2, 1), expectedRowResults, Collections.<PayeeACHAccountExtractDetail>emptyList());
        assertDoneFilesWereDeleted(BAD_HEADERS_FILE);
        assertDoneFilesWereDeleted(BAD_LINES_FILE);
        assertDoneFilesWereDeleted(MIX_GOOD_BAD_LINES_FILE);
    }

    @Test
    public void testEmailBodyPlaceholderResolution() throws Exception {
        // NOTE: We expect that all potentially-sensitive placeholders except bank name will be replaced with empty text.
        PayeeACHAccount achAccount = createEmployeePayeeACHAccountForAlternateUser();
        String expectedBody = MessageFormat.format(EMAIL_STRING_AS_FORMAT, achAccount.getPayeeIdentifierTypeCode(),
                achAccount.getPayeeIdNumber(), PERSONAL_SAVINGS_ACCOUNT_TYPE_LABEL, achAccount.getBankRouting().getBankName());
        String actualBody = payeeACHAccountExtractService.getResolvedEmailBody(achAccount, UNRESOLVED_EMAIL_STRING);
        
        assertEquals("Email body placeholders and special characters were not resolved properly", expectedBody, actualBody);
    }

    private void assertFileAndRowLoadingResultsAreCorrect(AchFileResults expectedFileResults,
            AchRowResults expectedRowResults, List<PayeeACHAccountExtractDetail> expectedSuccessfulDetails) throws Exception {
        AchFileResults actualFileResults = payeeACHAccountExtractService.getFileResults();
        AchRowResults actualRowResults = payeeACHAccountExtractService.getRowResults();
        List<PayeeACHAccountExtractDetail> actualSuccessfulDetails = payeeACHAccountExtractService.getSuccessfulDetails();
        
        assertEquals("Wrong number of completely successful files", expectedFileResults.getNumSuccessfulFiles(), actualFileResults.getNumSuccessfulFiles());
        assertEquals("Wrong number of files with bad data rows", expectedFileResults.getNumFilesWithBadRows(), actualFileResults.getNumFilesWithBadRows());
        assertEquals("Wrong number of files that could not be processed", expectedFileResults.getNumBadFiles(), actualFileResults.getNumBadFiles());
        assertRowProcessingResultsAreCorrect(expectedRowResults, actualRowResults);
        assertExtractedDetailsAreCorrect(expectedSuccessfulDetails, actualSuccessfulDetails);
    }

    private void assertRowProcessingResultsAreCorrect(
            AchRowResults expectedRowResults, AchRowResults actualRowResults) throws Exception {
        assertEquals("Wrong number of invalid file rows", expectedRowResults.getNumBadRows(), actualRowResults.getNumBadRows());
        assertPayeeResultsAreCorrect(expectedRowResults.getEmployeeRowResults(), actualRowResults.getEmployeeRowResults(), "Employee");
        assertPayeeResultsAreCorrect(expectedRowResults.getEntityRowResults(), actualRowResults.getEntityRowResults(), "Entity");
    }

    private void assertPayeeResultsAreCorrect(AchSuccessfulRowResults expectedResults,
            AchSuccessfulRowResults actualResults, String payeeTypeLabel) throws Exception {
        assertEquals(payeeTypeLabel + " types have wrong number of new rows", expectedResults.getNumNewRows(), actualResults.getNumNewRows());
        assertEquals(payeeTypeLabel + " types have wrong number of existing rows", expectedResults.getNumExistingRows(), actualResults.getNumExistingRows());
    }

    private void assertExtractedDetailsAreCorrect(List<PayeeACHAccountExtractDetail> expectedSuccessfulDetails,
            List<PayeeACHAccountExtractDetail> actualSuccessfulDetails) throws Exception {
        assertEquals("Wrong number of payee details processed", expectedSuccessfulDetails.size(), actualSuccessfulDetails.size());
        for (int i = 0; i < expectedSuccessfulDetails.size(); i++) {
            PayeeACHAccountExtractDetail expectedDetail = expectedSuccessfulDetails.get(i);
            PayeeACHAccountExtractDetail actualDetail = actualSuccessfulDetails.get(i);
            assertEquals("Wrong payment detail employee ID", expectedDetail.getEmployeeID(), actualDetail.getEmployeeID());
            assertEquals("Wrong payment detail netID", expectedDetail.getNetID(), actualDetail.getNetID());
            assertEquals("Wrong payment detail last name", expectedDetail.getLastName(), actualDetail.getLastName());
            assertEquals("Wrong payment detail first name", expectedDetail.getFirstName(), actualDetail.getFirstName());
            assertEquals("Wrong payment detail payment type", expectedDetail.getPaymentType(), actualDetail.getPaymentType());
            assertEquals("Wrong payment detail balance account indicator", expectedDetail.getBalanceAccount(), actualDetail.getBalanceAccount());
            assertEquals("Wrong payment detail routing number", expectedDetail.getBankRoutingNumber(), actualDetail.getBankRoutingNumber());
            assertEquals("Wrong payment detail account number", expectedDetail.getBankAccountNumber(), actualDetail.getBankAccountNumber());
            assertEquals("Wrong payment detail account type", expectedDetail.getBankAccountType(), actualDetail.getBankAccountType());
        }
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

    private AchRowResults getExpectedResultsForBadLinesFile() {
        // All file lines should fail.
        return new AchRowResults(
                new AchSuccessfulRowResults(PayeeIdTypeCodes.EMPLOYEE, 0, 0), new AchSuccessfulRowResults(PayeeIdTypeCodes.ENTITY, 0, 0), 7);
    }

    private AchRowResults getExpectedResultsForGoodFile() {
        // File line should succeed, resulting in 1 new employee account and 1 new entity account with no errors.
        return new AchRowResults(
                new AchSuccessfulRowResults(PayeeIdTypeCodes.EMPLOYEE, 1, 0), new AchSuccessfulRowResults(PayeeIdTypeCodes.ENTITY, 1, 0), 0);
    }

    private AchRowResults getExpectedResultsForMixedGoodBadLinesFile() {
        // Ten file lines should fail, and four file lines should succeed
        return new AchRowResults(
                new AchSuccessfulRowResults(PayeeIdTypeCodes.EMPLOYEE, 0, 4), new AchSuccessfulRowResults(PayeeIdTypeCodes.ENTITY, 4, 0), 14);
    }

    private PayeeACHAccountExtractDetail getExpectedExtractDetailForTestAccount() {
        PayeeACHAccountExtractDetail detail = new PayeeACHAccountExtractDetail();
        detail.setEmployeeID(TEST_EMPLOYEEID);
        detail.setNetID(TEST_PRINCIPALNAME);
        detail.setLastName(StringUtils.substringBefore(TEST_NAME, ","));
        detail.setFirstName(StringUtils.substringBetween(TEST_NAME, ", ", " "));
        detail.setPaymentType(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE);
        detail.setBalanceAccount(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_BALANCE_ACCOUNT_YES_INDICATOR);
        detail.setCompletedDate(COMPLETED_DATE_FROM_TEST_FILES);
        detail.setBankName(TEST_BANK_NAME);
        detail.setBankRoutingNumber(TEST_BANK_ROUTING_NUMBER);
        detail.setBankAccountNumber(TEST_ACCOUNT_NUMBER);
        detail.setBankAccountType(TEST_BANK_ACCOUNT_TYPE);
        return detail;
    }

    private PayeeACHAccountExtractDetail getExpectedExtractDetailForAlternateTestAccount() {
        PayeeACHAccountExtractDetail detail = new PayeeACHAccountExtractDetail();
        detail.setEmployeeID(TEST_ALT_EMPLOYEEID);
        detail.setNetID(TEST_ALT_PRINCIPALNAME);
        detail.setLastName(StringUtils.substringBefore(TEST_ALT_NAME, ","));
        detail.setFirstName(StringUtils.substringAfter(TEST_ALT_NAME, ", "));
        detail.setPaymentType(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_DIRECT_DEPOSIT_PAYMENT_TYPE);
        detail.setBalanceAccount(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_BALANCE_ACCOUNT_YES_INDICATOR);
        detail.setCompletedDate(COMPLETED_DATE_FROM_TEST_FILES);
        detail.setBankName(TEST_ALT_BANK_NAME);
        detail.setBankRoutingNumber(TEST_ALT_BANK_ROUTING_NUMBER);
        detail.setBankAccountNumber(TEST_ALT_ACCOUNT_NUMBER);
        detail.setBankAccountType(TEST_ALT_BANK_ACCOUNT_TYPE);
        return detail;
    }

    private PayeeACHAccountExtractCsvInputFileType getBatchInputFileType() {
        PayeeACHAccountExtractCsvInputFileType fileType = new PayeeACHAccountExtractCsvInputFileType();
        fileType.setDirectoryPath(ACH_TESTING_FILE_PATH);
        fileType.setFileExtension(ACH_CSV_FILE_EXTENSION);
        fileType.setCsvEnumClass(PayeeACHAccountExtractCsv.class);
        return fileType;
    }

    @SuppressWarnings("unchecked")
    private DocumentService createMockDocumentService() throws Exception {
        DocumentService mockDocumentService = EasyMock.createMock(DocumentServiceImpl.class);
        EasyMock.expect(mockDocumentService.getNewDocument(CUPdpConstants.PAYEE_ACH_ACCOUNT_EXTRACT_MAINT_DOC_TYPE)).andStubAnswer(new IAnswer<Document>() {
                @Override
                public Document answer() throws Throwable {
                    return createMockPAATDocument();
                }
        });
        EasyMock.expect(mockDocumentService.routeDocument(EasyMock.isA(MaintenanceDocument.class),
                EasyMock.or(EasyMock.isA(String.class), EasyMock.isNull(String.class)),
                EasyMock.or(EasyMock.isA(List.class), EasyMock.isNull(List.class)))).andStubAnswer(new IAnswer<Document>() {
                        @Override
                        public Document answer() throws Throwable {
                            return (Document) EasyMock.getCurrentArguments()[0];
                        }
                });
        EasyMock.replay(mockDocumentService);
        return mockDocumentService;
    }

    private MaintenanceDocument createMockPAATDocument() throws Exception {
        MaintenanceDocument paatDocument = EasyMock.createMock(MaintenanceDocumentBase.class);
        EasyMock.expect(paatDocument.getNewMaintainableObject()).andStubReturn(createNewMaintainableForPAAT());
        EasyMock.expect(paatDocument.getDocumentHeader()).andStubReturn(new DocumentHeader());
        EasyMock.expect(paatDocument.getObjectId()).andStubReturn("0");
        paatDocument.addNote(EasyMock.isA(Note.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(paatDocument);
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
        PersonService personService = EasyMock.createMock(PersonServiceImpl.class);
        EasyMock.expect(personService.getPersonByPrincipalName(TEST_PRINCIPALNAME)).andStubReturn(createTestUser());
        EasyMock.expect(personService.getPersonByPrincipalName(TEST_ALT_PRINCIPALNAME)).andStubReturn(createAlternateTestUser());
        EasyMock.expect(personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER)).andStubReturn(createSystemUser());
        EasyMock.expect(personService.getPersonByPrincipalName(
                EasyMock.and(EasyMock.not(EasyMock.eq(TEST_PRINCIPALNAME)), EasyMock.and(
                        EasyMock.not(EasyMock.eq(TEST_ALT_PRINCIPALNAME)), EasyMock.not(EasyMock.eq(KFSConstants.SYSTEM_USER)))
                ))).andStubReturn(null);
        EasyMock.replay(personService);
        return personService;
    }

    private Person createTestUser() {
        Principal.Builder principal = Principal.Builder.create(TEST_PRINCIPALNAME);
        principal.setPrincipalId(TEST_PRINCIPALID);
        principal.setEntityId(TEST_ENTITYID);
        principal.setActive(true);
        
        return new MockPersonImpl(principal.build(), TEST_EMPLOYEEID, TEST_NAME, TEST_EMAIL);
    }

    private Person createAlternateTestUser() {
        Principal.Builder principal = Principal.Builder.create(TEST_ALT_PRINCIPALNAME);
        principal.setPrincipalId(TEST_ALT_PRINCIPALID);
        principal.setEntityId(TEST_ALT_ENTITYID);
        principal.setActive(true);
        
        return new MockPersonImpl(principal.build(), TEST_ALT_EMPLOYEEID, TEST_ALT_NAME, TEST_ALT_EMAIL);
    }

    private Person createSystemUser() {
        Principal.Builder principal = Principal.Builder.create(KFSConstants.SYSTEM_USER);
        principal.setPrincipalId(KFSConstants.SYSTEM_USER);
        principal.setEntityId(KFSConstants.SYSTEM_USER);
        principal.setActive(true);
        
        return new MockPersonImpl(principal.build());
    }

    private AchService createMockAchService() throws Exception {
        AchService achService = EasyMock.createMock(AchServiceImpl.class);
        // Only the alternate employee ID is expected to return a non-null payee result.
        EasyMock.expect(achService.getAchInformation(EasyMock.eq(PayeeIdTypeCodes.ENTITY),
                EasyMock.isA(String.class), EasyMock.eq(KFS_DIRECT_DEPOSIT_TYPE))).andStubReturn(null);
        EasyMock.expect(achService.getAchInformation(PayeeIdTypeCodes.EMPLOYEE, TEST_ALT_EMPLOYEEID, KFS_DIRECT_DEPOSIT_TYPE)).andStubReturn(
                createEmployeePayeeACHAccountForAlternateUser());
        EasyMock.expect(achService.getAchInformation(EasyMock.eq(PayeeIdTypeCodes.EMPLOYEE),
                EasyMock.not(EasyMock.eq(TEST_ALT_EMPLOYEEID)), EasyMock.eq(KFS_DIRECT_DEPOSIT_TYPE))).andStubReturn(null);
        EasyMock.replay(achService);
        return achService;
    }

    private PayeeACHAccount createEmployeePayeeACHAccountForAlternateUser() {
        PayeeACHAccount achAccount = new PayeeACHAccount();
        achAccount.setPayeeIdentifierTypeCode(PayeeIdTypeCodes.EMPLOYEE);
        achAccount.setPayeeIdNumber(TEST_ALT_EMPLOYEEID);
        achAccount.setAchTransactionType(KFS_DIRECT_DEPOSIT_TYPE);
        achAccount.setBankRoutingNumber(TEST_ALT_BANK_ROUTING_NUMBER);
        achAccount.setBankAccountTypeCode("32PPD");
        achAccount.setBankAccountNumber(TEST_ALT_ACCOUNT_NUMBER);
        achAccount.setBankRouting(createBank(TEST_ALT_BANK_ROUTING_NUMBER, TEST_ALT_BANK_NAME));
        return achAccount;
    }

    private AchBankService createMockAchBankService() throws Exception {
        AchBankService achBankService = EasyMock.createMock(AchBankServiceImpl.class);
        EasyMock.expect(achBankService.getByPrimaryId(TEST_BANK_ROUTING_NUMBER)).andStubReturn(
                createBank(TEST_BANK_ROUTING_NUMBER, TEST_BANK_NAME));
        EasyMock.expect(achBankService.getByPrimaryId(TEST_ALT_BANK_ROUTING_NUMBER)).andStubReturn(
                createBank(TEST_ALT_BANK_ROUTING_NUMBER, TEST_ALT_BANK_NAME));
        EasyMock.expect(achBankService.getByPrimaryId(EasyMock.and(
                EasyMock.not(EasyMock.eq(TEST_BANK_ROUTING_NUMBER)), EasyMock.not(EasyMock.eq(TEST_ALT_BANK_ROUTING_NUMBER))))).andStubReturn(null);
        EasyMock.replay(achBankService);
        return achBankService;
    }

    private ACHBank createBank(String bankRoutingNumber, String bankName) {
        ACHBank bank = new ACHBank();
        bank.setBankRoutingNumber(bankRoutingNumber);
        bank.setBankName(bankName);
        bank.setActive(true);
        return bank;
    }

    private static class MockPersonImpl extends PersonImpl {
        private static final long serialVersionUID = 1L;
        
        public MockPersonImpl(Principal principal) {
            super();
            this.principalId = principal.getPrincipalId();
            this.principalName = principal.getPrincipalName();
            this.entityId = principal.getEntityId();
            this.active = principal.isActive();
        }
        
        public MockPersonImpl(Principal principal, String employeeId, String name, String emailAddress) {
            this(principal);
            this.employeeId = employeeId;
            this.name = name;
            this.emailAddress = emailAddress;
        }
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

    // Helper implementation of PayeeACHAccountExtractService that tracks various statistics.
    private static class TestPayeeACHAccountExtractService extends PayeeACHAccountExtractServiceImpl {
        private AchFileResults fileResults = new AchFileResults();
        private AchRowResults rowResults = new AchRowResults();
        private List<PayeeACHAccountExtractDetail> successfulDetails = new ArrayList<>();
        
        @Override
        protected List<String> loadACHBatchDetailFile(String inputFileName, BatchInputFileType batchInputFileType) {
            try {
                List<String>failedRowsErrors = new ArrayList<String>();
                failedRowsErrors = super.loadACHBatchDetailFile(inputFileName, batchInputFileType);
                if (failedRowsErrors.isEmpty()) {
                    fileResults.incrementNumSuccessfulFiles();
                } else {
                    fileResults.incrementNumFilesWithBadRows();
                }
                return failedRowsErrors;
            } catch (Exception e) {
                fileResults.incrementNumBadFiles();
                throw e;
            }
        }
        
        @Override
        protected String processACHBatchDetail(PayeeACHAccountExtractDetail achDetail) {
            String failureMessage = super.processACHBatchDetail(achDetail);
            if (ObjectUtils.isNull(failureMessage) || StringUtils.isBlank(failureMessage)) {
                successfulDetails.add(achDetail);
            } else {
                rowResults.incrementNumBadRows();
            }
            return failureMessage;
        }
        
        @Override
        protected String addACHAccount(Person payee, PayeeACHAccountExtractDetail achDetail, String payeeType) {
            String processingResults = super.addACHAccount(payee, achDetail, payeeType);
            if (PayeeIdTypeCodes.EMPLOYEE.equals(payeeType)) {
                rowResults.getEmployeeRowResults().incrementNumNewRows();
            } else if (PayeeIdTypeCodes.ENTITY.equals(payeeType)) {
                rowResults.getEntityRowResults().incrementNumNewRows();
            }
            return processingResults;
        }
        
        @Override
        protected String updateACHAccountIfNecessary(Person payee, PayeeACHAccountExtractDetail achDetail, PayeeACHAccount achAccount) {
            String processingResults = super.updateACHAccountIfNecessary(payee, achDetail, achAccount);
            if (PayeeIdTypeCodes.EMPLOYEE.equals(achAccount.getPayeeIdentifierTypeCode())) {
                rowResults.getEmployeeRowResults().incrementNumExistingRows();
            } else if (PayeeIdTypeCodes.ENTITY.equals(achAccount.getPayeeIdentifierTypeCode())) {
                rowResults.getEntityRowResults().incrementNumExistingRows();
            }
            return processingResults;
        }
        
        @Override
        protected Note createEmptyNote() {
            Note note = EasyMock.createMock(Note.class);
            
            note.setNoteText(EasyMock.isA(String.class));
            note.setRemoteObjectIdentifier(EasyMock.isA(String.class));
            note.setAuthorUniversalIdentifier(EasyMock.isA(String.class));
            note.setNoteTypeCode(EasyMock.isA(String.class));
            note.setNotePostedTimestampToCurrent();
            EasyMock.replay(note);
            
            return note;
        }
        
        // Increase visibility to "public" for testing convenience.
        @Override
        public String getResolvedEmailBody(PayeeACHAccount achAccount, String emailBody) {
            return super.getResolvedEmailBody(achAccount, emailBody);
        }
        
        public AchFileResults getFileResults() {
            return fileResults;
        }
        
        public AchRowResults getRowResults() {
            return rowResults;
        }
        
        public List<PayeeACHAccountExtractDetail> getSuccessfulDetails() {
            return successfulDetails;
        }
    }

    // Helper object summarizing the statistics for the file rows.
    private static class AchRowResults {
        private final AchSuccessfulRowResults employeeRowResults;
        private final AchSuccessfulRowResults entityRowResults;
        private int numBadRows;

        public AchRowResults() {
            this.employeeRowResults = new AchSuccessfulRowResults(PayeeIdTypeCodes.EMPLOYEE);
            this.entityRowResults = new AchSuccessfulRowResults(PayeeIdTypeCodes.ENTITY);
        }

        public AchRowResults(AchSuccessfulRowResults employeeRowResults, AchSuccessfulRowResults entityRowResults, int numBadRows) {
            if (!PayeeIdTypeCodes.EMPLOYEE.equals(employeeRowResults.getPayeeType())) {
                throw new IllegalArgumentException("employeeRowResults is not configured for employee payee types");
            } else if (!PayeeIdTypeCodes.ENTITY.equals(entityRowResults.getPayeeType())) {
                throw new IllegalArgumentException("entityRowResults is not configured for entity payee types");
            }
            this.employeeRowResults = employeeRowResults;
            this.entityRowResults = entityRowResults;
            this.numBadRows = numBadRows;
        }

        public void incrementNumBadRows() {
            numBadRows++;
        }

        public AchSuccessfulRowResults getEmployeeRowResults() {
            return employeeRowResults;
        }

        public AchSuccessfulRowResults getEntityRowResults() {
            return entityRowResults;
        }

        public int getNumBadRows() {
            return numBadRows;
        }

        public static AchRowResults createSummarizedResults(AchRowResults... results) {
            AchRowResults summary = new AchRowResults();
            for (AchRowResults result : results) {
                summary.employeeRowResults.addResults(result.employeeRowResults);
                summary.entityRowResults.addResults(result.entityRowResults);
                summary.numBadRows += result.numBadRows;
            }
            return summary;
        }
    }

    // Helper object summarizing the statistics for a single file's impacts to payees.
    private static class AchSuccessfulRowResults {
        private final String payeeType;
        private int numNewRows;
        private int numExistingRows;

        public AchSuccessfulRowResults(String payeeType) {
            this.payeeType = payeeType;
        }

        protected AchSuccessfulRowResults(String payeeType, int numNewRows, int numExistingRows) {
            this.payeeType = payeeType;
            this.numNewRows = numNewRows;
            this.numExistingRows = numExistingRows;
        }

        public void incrementNumNewRows() {
            numNewRows++;
        }

        public void incrementNumExistingRows() {
            numExistingRows++;
        }

        public String getPayeeType() {
            return payeeType;
        }

        public int getNumNewRows() {
            return numNewRows;
        }

        public int getNumExistingRows() {
            return numExistingRows;
        }

        public void addResults(AchSuccessfulRowResults resultsToAdd) {
            if (!StringUtils.equals(payeeType, resultsToAdd.payeeType)) {
                throw new IllegalArgumentException("resultsToAdd does not have the same payee type as the current results");
            }
            numNewRows += resultsToAdd.numNewRows;
            numExistingRows += resultsToAdd.numExistingRows;
        }
    }

    // Helper object summarizing the statistics for the file processing as a whole.
    private static class AchFileResults {
        private int numSuccessfulFiles;
        private int numFilesWithBadRows;
        private int numBadFiles;

        public AchFileResults() {
        }

        public AchFileResults(int numSuccessfulFiles, int numFilesWithBadRows, int numBadFiles) {
            this.numSuccessfulFiles = numSuccessfulFiles;
            this.numFilesWithBadRows = numFilesWithBadRows;
            this.numBadFiles = numBadFiles;
        }

        public void incrementNumSuccessfulFiles() {
            numSuccessfulFiles++;
        }

        public void incrementNumFilesWithBadRows() {
            numFilesWithBadRows++;
        }

        public void incrementNumBadFiles() {
            numBadFiles++;
        }

        public int getNumSuccessfulFiles() {
            return numSuccessfulFiles;
        }

        public int getNumFilesWithBadRows() {
            return numFilesWithBadRows;
        }

        public int getNumBadFiles() {
            return numBadFiles;
        }
    }

}
