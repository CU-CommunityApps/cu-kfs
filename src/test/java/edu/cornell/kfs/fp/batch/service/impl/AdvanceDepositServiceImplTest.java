package edu.cornell.kfs.fp.batch.service.impl;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.fp.businessobject.AchIncomeFile;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileGroup;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileGroupTrailer;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTrailer;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransaction;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionOpenItemReference;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionPayerOrPayeeName;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionSet;
import edu.cornell.kfs.fp.businessobject.AchIncomeFileTransactionSetTrailer;
import edu.cornell.kfs.fp.businessobject.AchIncomeNote;
import edu.cornell.kfs.fp.businessobject.AchIncomeTransaction;
import edu.cornell.kfs.fp.businessobject.IncomingWireAchMapping;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.batch.service.impl.CuBatchInputFileServiceImpl;
import edu.cornell.kfs.sys.service.mock.MockParameterServiceImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.fp.document.AdvanceDepositDocument;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.FlatFileParserBase;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.service.impl.EmailServiceImpl;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.impl.datetime.DateTimeServiceImpl;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.GlobalVariables;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.CALLS_REAL_METHODS;

public class AdvanceDepositServiceImplTest {
    protected AdvanceDepositServiceImpl advanceDepositService;
    protected IncomingWireAchMapping mapping;
    protected AdvanceDepositDocument advanceDepositDocument;
    protected FlatFileParserBase achIncomeInputFileType;

    private static final String TXT_FILE_EXTENSION = "txt";
    private static final String DONE_FILE_EXTENSION = "done";
    private static final String DATA_FILE_PATH = "src/test/resources/edu/cornell/kfs/fp/batch/service/fixture/";
    private static final String DATA_FILE_GOOD_NAME = "achIncome_test";
    private static final String DATA_FILE_EMPTY_NAME = "achIncome_empty_test";
    private static final String BATCH_DIRECTORY = "test/fp/achIncome/";
    private static final String GOOD_DONE_FILE_2 = BATCH_DIRECTORY + DATA_FILE_GOOD_NAME + "2." + DONE_FILE_EXTENSION;
    private static final String GOOD_DATA_FILE_2 = BATCH_DIRECTORY + DATA_FILE_GOOD_NAME + "2." + TXT_FILE_EXTENSION;
    private static final String EMPTY_DONE_FILE = BATCH_DIRECTORY + DATA_FILE_EMPTY_NAME + "." + DONE_FILE_EXTENSION;
    private static final String EMPTY_DATA_FILE = BATCH_DIRECTORY + DATA_FILE_EMPTY_NAME + "." + TXT_FILE_EXTENSION;
    private static final String GOOD_DONE_FILE = BATCH_DIRECTORY + DATA_FILE_GOOD_NAME + "." + DONE_FILE_EXTENSION;
    private static final String GOOD_DATA_FILE = BATCH_DIRECTORY + DATA_FILE_GOOD_NAME + "." + TXT_FILE_EXTENSION;
    
    private static final String EXPENSE_OBJECT_CODE = "EXOBJ";
    private static final String ASSET_OBJECT_CODE = "ASSETOBJ";
    private static final String NON_ASSET_EXPENSE_OBJECT_CODE = "SOMEOBJ";

    @Before
    public void setUp() {
        advanceDepositService = new TestableAdvanceDepositServiceImpl();
        advanceDepositService.setBusinessObjectService(new MockBusinessObjectService());
        advanceDepositService.setDateTimeService(new DateTimeServiceImpl());
        advanceDepositService.setParameterService(new MockParameterServiceImpl());

        mapping = new IncomingWireAchMapping();
        mapping.setShortDescription("ARMY");

        advanceDepositDocument = mock(AdvanceDepositDocument.class, CALLS_REAL_METHODS);

        GlobalVariables.getMessageMap().clearErrorMessages();
    }

    @Test
    public void testCreateSourceAccountingLineDefault() {
        AchIncomeTransaction transaction = new AchIncomeTransaction();
        transaction.setNotes(new ArrayList<AchIncomeNote>());
        advanceDepositService.createSourceAccountingLine(transaction, advanceDepositDocument);
        assertNotNull(advanceDepositDocument.getSourceAccountingLines());
        assertEquals(1, advanceDepositDocument.getSourceAccountingLines().size());
        assertEquals("IT", advanceDepositDocument.getSourceAccountingLine(0).getChartOfAccountsCode());
        assertEquals("2240", advanceDepositDocument.getSourceAccountingLine(0).getFinancialObjectCode());
        assertEquals("G621060", advanceDepositDocument.getSourceAccountingLine(0).getAccountNumber());
    }

    @Test
    public void testCreateSourceAccountingLineMatchingNote() {
        AchIncomeTransaction transaction = new AchIncomeTransaction();
        List<AchIncomeNote> notes = setupNotes("an ARMY of one");
        transaction.setNotes(notes);
        advanceDepositService.createSourceAccountingLine(transaction, advanceDepositDocument);
        assertNotNull(advanceDepositDocument.getSourceAccountingLines());
        assertEquals(1, advanceDepositDocument.getSourceAccountingLines().size());
        assertEquals("CU", advanceDepositDocument.getSourceAccountingLine(0).getChartOfAccountsCode());
        assertEquals("5000", advanceDepositDocument.getSourceAccountingLine(0).getFinancialObjectCode());
        assertEquals("1234567", advanceDepositDocument.getSourceAccountingLine(0).getAccountNumber());
    }

    @Test
    public void testDoNotesMatch() throws Exception {
        List<AchIncomeNote> notes = setupNotes("an ARMY of one");
        assertTrue(advanceDepositService.doNotesMatch(mapping, notes));
    }

    @Test
    public void testDoNotesMatchDoNotMatch() throws Exception {
        List<AchIncomeNote> notes = setupNotes("a NAVY of one");
        assertFalse(advanceDepositService.doNotesMatch(mapping, notes));
    }

    @Test
    public void testTruncatePayerNameIfNecessary() {
        AchIncomeFileTransaction achIncomeFileTransaction = new AchIncomeFileTransaction();
        achIncomeFileTransaction.setPayerOrPayees(setupPayerAndPayee("THIS NAME IS TOO LONG BECAUSE IT IS LONGER THAN 40 CHARACTERS"));
        String payerName = advanceDepositService.truncatePayerNameIfNecessary(achIncomeFileTransaction);
        assertEquals("name wasn't truncated as expected?", "THIS NAME IS TOO LONG BECAUSE IT IS LONG", payerName);
    }

    @Test
    public void testValidateMissingGood() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();

        assertTrue("contents should validate", advanceDepositService.validate(achIncomeFiles));
        assertEquals("expecting no errors", 0, GlobalVariables.getMessageMap().getErrorCount());

        final String regex = ".*Payer Name was not found for transaction amount $12,761\\.79 [ Date: 160223 ].*";
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);

        assertFalse("email message text should NOT indicate payer not found", p.matcher(achIncomeFiles.get(0).getEmailMessageText()).matches());
    }

    @Test
    public void testValidateMissingPayee() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesMissingPayee();

        assertTrue("contents should validate", advanceDepositService.validate(achIncomeFiles));
        assertEquals("expecting no errors", 0, GlobalVariables.getMessageMap().getErrorCount());

        final String notFoundLine = "Payer Name was not found for transaction amount $12,761.79 [Date: 2016-02-23]";

        String[] messageLines = achIncomeFiles.get(0).getEmailMessageText().split("\\n");
        String lastMessageLine = messageLines[messageLines.length - 1];

        assertEquals("email message text should indicate payer not found", notFoundLine, lastMessageLine);
    }

    @Test
    public void testValidateMissingTrailer() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).setTrailer(null);

        performValidationAsserts(achIncomeFiles, "No logical file trailer found for file header :000000000");
    }

    @Test
    public void testValidateGroupCountMismatch() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).getTrailer().setTotalGroups(2);

        performValidationAsserts(achIncomeFiles, "The group count on the file trailer,2,does not match the number of groups,1,in the file:000000000");
    }

    @Test
    public void testValidateControlNumberMismatch() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).getTrailer().setInterchangeControlNumber("000000001");

        performValidationAsserts(achIncomeFiles, "Cannot match logical file header to file trailer for file: ISA Control Number [000000000] IEA Control Number [000000001]");
    }

    @Test
    public void testValidateMissingGroupTrailer() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).getGroups().get(0).setGroupTrailer(null);

        performValidationAsserts(achIncomeFiles, "No group trailer found for group: [0]");
    }

    @Test
    public void testValidateTransactionCountMismatch() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).getGroups().get(0).getGroupTrailer().setTotalTransactionSets(2);

        performValidationAsserts(achIncomeFiles, "The transaction count on the group trailer, 2, does not match the number of transactions, 1, in the group: [0]");
    }

    @Test
    public void testValidateGroupControlNumberMismatch() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).getGroups().get(0).setGroupControlNumber("092016170");

        performValidationAsserts(achIncomeFiles, "Cannot match group header to group trailer for group: GS Control Number [092016170] IEA Control Number [0]");
    }

    @Test
    public void testValidateInvalidGroupFunctionalIdentifierCode() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).getGroups().get(0).setGroupFunctionIdentifierCode("XX");
        assertTrue("contents should validate", advanceDepositService.validate(achIncomeFiles));
        assertEquals("expecting no errors", 0, GlobalVariables.getMessageMap().getErrorCount());
        assertEquals("expecting one warning", 1, GlobalVariables.getMessageMap().getWarningCount());
        assertEquals("error message didn't match what we were expecting", "The Functional Identifier Code is not " + CuFPConstants.AchIncomeFileGroup.GROUP_FUNCTIONAL_IDENTIFIER_CD_RA + " for group: 0-XX", getWarningMessage());
    }

    @Test
    public void testValidateMissingTransactionSetTrailer() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).getGroups().get(0).getTransactionSets().get(0).setTransactionSetTrailer(null);

        performValidationAsserts(achIncomeFiles, "No transaction trailer found for transaction: [0001]");
    }

    @Test
    public void testValidateTransactionSetControlNumberMismatch() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).getGroups().get(0).getTransactionSets().get(0).setTransactionSetControlNumber("000000001");

        performValidationAsserts(achIncomeFiles, "Cannot match transaction header to transaction trailer for transaction set: ST Control Number: [000000001] SE Control Number: [0001]");
    }

    @Test
    public void testValidateInvalidDateInvalidYear() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).setFileDate("XX1201");
        String expectedMessage = "fileDate/Time must be of the format " + CuFPConstants.ACH_INCOME_FILE_DATE_FORMAT + "\n"
                + "java.text.ParseException: Unparseable date: &quot;XX12012143&quot;, error.custom[fileDate/Time must be of the format " + CuFPConstants.ACH_INCOME_FILE_DATE_FORMAT + "\n"
                + "java.text.ParseException: Unparseable date: &quot;XX12012143&quot;]";

        performValidationAsserts(achIncomeFiles, expectedMessage);
    }

    @Test
    public void testValidateInvalidDateInvalidMonth() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).setFileDate("164501");
        String expectedMessage = "fileDate/Time must be of the format " + CuFPConstants.ACH_INCOME_FILE_DATE_FORMAT + "\n"
                + "java.text.ParseException: Unparseable date: &quot;1645012143&quot;, error.custom[fileDate/Time must be of the format " + CuFPConstants.ACH_INCOME_FILE_DATE_FORMAT + "\n"
                + "java.text.ParseException: Unparseable date: &quot;1645012143&quot;]";

        performValidationAsserts(achIncomeFiles, expectedMessage);
    }

    @Test
    public void testValidateInvalidDateInvalidDay() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).setFileDate("160432");
        String expectedMessage = "fileDate/Time must be of the format " + CuFPConstants.ACH_INCOME_FILE_DATE_FORMAT + "\n"
                + "java.text.ParseException: Unparseable date: &quot;1604322143&quot;, error.custom[fileDate/Time must be of the format " + CuFPConstants.ACH_INCOME_FILE_DATE_FORMAT + "\n"
                + "java.text.ParseException: Unparseable date: &quot;1604322143&quot;]";

        performValidationAsserts(achIncomeFiles, expectedMessage);
    }

    @Test
    public void testValidateInvalidTime() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).setFileTime("XXXX");
        String expectedMessage = "fileDate/Time must be of the format " + CuFPConstants.ACH_INCOME_FILE_DATE_FORMAT + "\n"
            + "java.text.ParseException: Unparseable date: &quot;160223XXXX&quot;, error.custom[fileDate/Time must be of the format " + CuFPConstants.ACH_INCOME_FILE_DATE_FORMAT + "\n"
            + "java.text.ParseException: Unparseable date: &quot;160223XXXX&quot;]";

        performValidationAsserts(achIncomeFiles, expectedMessage);
    }

    @Test
    public void testValidateMultipleMissingTrailerErrors() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).setTrailer(null);
        String expectedErrorMessage1 = "No logical file trailer found for file header :000000000";

        achIncomeFiles.get(0).getGroups().get(0).setGroupTrailer(null);
        String expectedErrorMessage2 = "No group trailer found for group: [0]";

        assertFalse("contents should NOT validate", advanceDepositService.validate(achIncomeFiles));
        assertEquals("expecting one errors", 2, GlobalVariables.getMessageMap().getErrorCount());
        assertEquals("error message didn't match what we were expecting", expectedErrorMessage1, getErrorMessage(0));
        assertEquals("error message didn't match what we were expecting", expectedErrorMessage2, getErrorMessage(1));
    }

    @Test
    public void testValidateMultipleErrorsGroupCountAndControlNumber() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        achIncomeFiles.get(0).getTrailer().setTotalGroups(2);
        String expectedErrorMessage1 = "The group count on the file trailer,2,does not match the number of groups,1,in the file:000000000";

        achIncomeFiles.get(0).getTrailer().setInterchangeControlNumber("000000001");
        String expectedErrorMessage2 = "Cannot match logical file header to file trailer for file: ISA Control Number [000000000] IEA Control Number [000000001]";

        assertFalse("contents should NOT validate", advanceDepositService.validate(achIncomeFiles));
        assertEquals("expecting one errors", 2, GlobalVariables.getMessageMap().getErrorCount());
        assertEquals("error message didn't match what we were expecting", expectedErrorMessage1, getErrorMessage(0));
        assertEquals("error message didn't match what we were expecting", expectedErrorMessage2, getErrorMessage(1));
    }

    @Test
    public void testSetAchTransactions() throws ParseException {
        List<AchIncomeTransaction> achIncomeTransactions = new ArrayList();
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesGood();
        advanceDepositService.setAchTransactions(achIncomeFiles.get(0), achIncomeTransactions);
        assertEquals("should have one ach income transaction", 1, achIncomeTransactions.size());
    }
    
    @Test
    public void testSetupSourceAccountingLine_NegativeAmount() {
        SourceAccountingLine sourceAccountingLine = new SourceAccountingLine();
        AchIncomeTransaction achIncomeTransaction = new AchIncomeTransaction();
        achIncomeTransaction.setTransactionAmount(new KualiDecimal(10));
        advanceDepositService.setupSourceAccountingLine(achIncomeTransaction, advanceDepositDocument, "IT", EXPENSE_OBJECT_CODE, "1234567");

        assertEquals("Accounting line amount should be the transaction amount negated", achIncomeTransaction.getTransactionAmount().negated(), advanceDepositDocument.getSourceAccountingLine(0).getAmount());
    }

    @Test
    public void testSetupSourceAccountingLine_PositiveAmount() {
        SourceAccountingLine sourceAccountingLine = new SourceAccountingLine();
        AchIncomeTransaction achIncomeTransaction = new AchIncomeTransaction();
        achIncomeTransaction.setTransactionAmount(new KualiDecimal(10));
        advanceDepositService.setupSourceAccountingLine(achIncomeTransaction, advanceDepositDocument, "IT", NON_ASSET_EXPENSE_OBJECT_CODE, "1234567");

        assertEquals("Accounting line amount should equal the transaction amount", achIncomeTransaction.getTransactionAmount(), advanceDepositDocument.getSourceAccountingLine(0).getAmount());
    }
    
    @Test
    public void testLoadFile() throws IOException, ParseException {
        additionalSetupForFileProcessing();

        assertTrue("good data file [" + GOOD_DATA_FILE + "] doesn't exist but should", new File(GOOD_DATA_FILE).exists());
        assertTrue("good done file [" + GOOD_DONE_FILE + "] doesn't exist but should", new File(GOOD_DONE_FILE).exists());
        assertTrue("empty data file [" + EMPTY_DATA_FILE + "] doesn't exist but should", new File(EMPTY_DATA_FILE).exists());
        assertTrue("empty done file [" + EMPTY_DONE_FILE + "] doesn't exist but should", new File(EMPTY_DONE_FILE).exists());
        assertTrue("good data file 2 [" + GOOD_DATA_FILE_2 + "] doesn't exist but should", new File(GOOD_DATA_FILE_2).exists());
        assertTrue("good done file 2 [" + GOOD_DONE_FILE_2 + "] doesn't exist but should", new File(GOOD_DONE_FILE_2).exists());

        boolean runtimeExceptionThrown = false;
        try {
            advanceDepositService.loadFile();
        } catch (RuntimeException e) {
            runtimeExceptionThrown = true;
        }

        assertTrue("good data file [" + GOOD_DATA_FILE + "] doesn't exist but should", new File(GOOD_DATA_FILE).exists());
        assertFalse("good done file [" + GOOD_DONE_FILE + "] exists but shouldn't", new File(GOOD_DONE_FILE).exists());
        assertTrue("empty data file [" + EMPTY_DATA_FILE + "] doesn't exist but should", new File(EMPTY_DATA_FILE).exists());
        assertFalse("empty done file [" + EMPTY_DONE_FILE + "] exists but shouldn't", new File(EMPTY_DONE_FILE).exists());
        assertTrue("good data file 2 [" + GOOD_DATA_FILE_2 + "] doesn't exist but should", new File(GOOD_DATA_FILE_2).exists());
        assertTrue("good done file 2 [" + GOOD_DONE_FILE_2 + "] doesn't exist but should", new File(GOOD_DONE_FILE_2).exists());
        assertTrue(runtimeExceptionThrown);

        cleanupBatchFiles();
    }
    protected void additionalSetupForFileProcessing() throws IOException, ParseException {
        List<String> fileNames = new ArrayList<>();
        fileNames.add(GOOD_DATA_FILE);
        fileNames.add(EMPTY_DATA_FILE);
        fileNames.add(GOOD_DATA_FILE_2);

        BatchInputFileService batchInputFileService = mock(CuBatchInputFileServiceImpl.class);
        when(batchInputFileService.listInputFileNamesWithDoneFile(isA(BatchInputFileType.class))).thenReturn(fileNames);
        when(batchInputFileService.parse(isA(BatchInputFileType.class), isA(byte[].class))).thenReturn(setupAchIncomeFilesGood()).thenThrow(new RuntimeException()).thenReturn(setupAchIncomeFilesGood());

        advanceDepositService.setBatchInputFileService(batchInputFileService);
        advanceDepositService.setEmailService(new EmailServiceImpl());

        achIncomeInputFileType = new FlatFileParserBase();
        achIncomeInputFileType.setDirectoryPath(BATCH_DIRECTORY);
        achIncomeInputFileType.setFileExtension(TXT_FILE_EXTENSION);
        advanceDepositService.setBatchInputFileType(achIncomeInputFileType);

        setupBatchFiles(DATA_FILE_GOOD_NAME);
        setupBatchFiles(DATA_FILE_EMPTY_NAME);
        setupBatchFiles(DATA_FILE_GOOD_NAME + "2");
    }

    protected void setupBatchFiles(String fileName) throws IOException {
        File dataFileSrc = new File(DATA_FILE_PATH + fileName + "." + TXT_FILE_EXTENSION);
        File dataFileDest = new File(BATCH_DIRECTORY + fileName + "." + TXT_FILE_EXTENSION);
        FileUtils.copyFile(dataFileSrc, dataFileDest);

        String doneFileName = BATCH_DIRECTORY + fileName + "." + DONE_FILE_EXTENSION;
        File doneFile = new File(doneFileName);
        if (!doneFile.exists()) {
            doneFile.createNewFile();
        }
    }

    public void cleanupBatchFiles() throws IOException {
        File batchDirectory = new File(BATCH_DIRECTORY);
        if (batchDirectory.exists() && batchDirectory.isDirectory()) {
            for (File batchFile: batchDirectory.listFiles()) {
                batchFile.delete();
            }
            batchDirectory.delete();
            File batchFpDirectory = new File("test/fp/");
            batchFpDirectory.delete();
            File batchRootDirectory = new File("test/");
            batchRootDirectory.delete();
        }
    }

    protected void performValidationAsserts(List<AchIncomeFile> achIncomeFiles, String expectedErrorMessage) {
        assertFalse("contents should NOT validate", advanceDepositService.validate(achIncomeFiles));
        assertEquals("expecting one errors", 1, GlobalVariables.getMessageMap().getErrorCount());
        assertEquals("error message didn't match what we were expecting", expectedErrorMessage, getErrorMessage(0));
    }

    private String getErrorMessage(int index) {
        return GlobalVariables.getMessageMap().getErrorMessages().get("GLOBAL_ERRORS").get(index).getMessageParameters()[0];
    }

    private String getWarningMessage() {
        return GlobalVariables.getMessageMap().getWarningMessages().get("GLOBAL_ERRORS").get(0).getMessageParameters()[0];
    }

    protected List<AchIncomeFile> setupAchIncomeFilesGood() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = setupAchIncomeFilesMissingPayee();

        achIncomeFiles.get(0).getGroups().get(0).getTransactionSets().get(0).getTransactionGuts().get(0).setPayerOrPayees(setupPayerAndPayee());

        return achIncomeFiles;
    }

    private List<AchIncomeFileTransactionPayerOrPayeeName> setupPayerAndPayee() {
        return setupPayerAndPayee("NATIONAL SCIENCE FOUNDATION");
    }

    private List<AchIncomeFileTransactionPayerOrPayeeName> setupPayerAndPayee(String payerName) {
        AchIncomeFileTransactionPayerOrPayeeName achIncomeFileTransactionPayeeName = new AchIncomeFileTransactionPayerOrPayeeName();
        achIncomeFileTransactionPayeeName.setType("PE");
        achIncomeFileTransactionPayeeName.setName("CORNELL UNIVERSITY, INC");
        achIncomeFileTransactionPayeeName.setIdCode("93");
        achIncomeFileTransactionPayeeName.setIdQualifier("4B578");

        AchIncomeFileTransactionPayerOrPayeeName achIncomeFileTransactionPayerName = new AchIncomeFileTransactionPayerOrPayeeName();
        achIncomeFileTransactionPayerName.setType("PR");
        achIncomeFileTransactionPayerName.setName(payerName);

        List<AchIncomeFileTransactionPayerOrPayeeName> payerOrPayees = new ArrayList<>();
        payerOrPayees.add(achIncomeFileTransactionPayeeName);
        payerOrPayees.add(achIncomeFileTransactionPayerName);
        return payerOrPayees;
    }

    protected List<AchIncomeFile> setupAchIncomeFilesMissingPayee() throws ParseException {
        List<AchIncomeFile> achIncomeFiles = new ArrayList<>();
        AchIncomeFile achIncomeFile = new AchIncomeFile();
        achIncomeFile.setFileDate("160223");
        achIncomeFile.setFileTime("2143");
        achIncomeFile.setProductionOrTestIndicator("P");
        achIncomeFile.setInterchangeControlNumber("000000000");

        List<AchIncomeFileGroup> groups = new ArrayList<>();
        AchIncomeFileGroup achIncomeFileGroup = new AchIncomeFileGroup();
        achIncomeFileGroup.setGroupControlNumber("0");
        achIncomeFileGroup.setGroupFunctionIdentifierCode("RA");

        List<AchIncomeFileTransactionSet> achIncomeFileTransactionSets = new ArrayList<>();
        AchIncomeFileTransactionSet achIncomeFileTransactionSet = new AchIncomeFileTransactionSet();
        achIncomeFileTransactionSet.setTransactionSetControlNumber("0001");

        AchIncomeFileTransactionSetTrailer achIncomeFileTransactionSetTrailer = new AchIncomeFileTransactionSetTrailer();
        achIncomeFileTransactionSetTrailer.setTransactionSetControlNumber("0001");
        achIncomeFileTransactionSet.setTransactionSetTrailer(achIncomeFileTransactionSetTrailer);

        List<AchIncomeFileTransaction> transactionGuts = new ArrayList<>();
        AchIncomeFileTransaction achIncomeFileTransaction = new AchIncomeFileTransaction();
        achIncomeFileTransaction.setTransactionAmount(new KualiDecimal("12761.79"));
        achIncomeFileTransaction.setCreditDebitIndicator("C");
        achIncomeFileTransaction.setCompanyId("1111541330");
        achIncomeFileTransaction.setPaymentMethodCode("ACH");

        Date expectedDate = new Date(new SimpleDateFormat("yyyyMMdd").parse("20160223").getTime());
        achIncomeFileTransaction.setEffectiveDate(expectedDate);

        List<AchIncomeFileTransactionOpenItemReference> openItemReferences = new ArrayList<>();
        openItemReferences.add(setupOpenItemReference("OI", "71201-16"));
        achIncomeFileTransaction.setOpenItemReferences(openItemReferences);

        transactionGuts.add(achIncomeFileTransaction);
        achIncomeFileTransactionSet.setTransactionGuts(transactionGuts);
        achIncomeFileTransactionSets.add(achIncomeFileTransactionSet);
        achIncomeFileGroup.setTransactionSets(achIncomeFileTransactionSets);

        AchIncomeFileGroupTrailer achIncomeFileGroupTrailer = new AchIncomeFileGroupTrailer();
        achIncomeFileGroupTrailer.setGroupControlNumber("0");
        achIncomeFileGroupTrailer.setTotalTransactionSets(1);
        achIncomeFileGroup.setGroupTrailer(achIncomeFileGroupTrailer);
        groups.add(achIncomeFileGroup);
        achIncomeFile.setGroups(groups);

        AchIncomeFileTrailer achIncomeFileTrailer = new AchIncomeFileTrailer();
        achIncomeFileTrailer.setInterchangeControlNumber("000000000");
        achIncomeFileTrailer.setTotalGroups(1);

        achIncomeFile.setTrailer(achIncomeFileTrailer);
        achIncomeFiles.add(achIncomeFile);
        return achIncomeFiles;
    }

    private AchIncomeFileTransactionOpenItemReference setupOpenItemReference(String type, String invoiceNumber) {
        AchIncomeFileTransactionOpenItemReference openItemReference = new AchIncomeFileTransactionOpenItemReference();
        openItemReference.setInvoiceAmount(new KualiDecimal("12761.79"));
        openItemReference.setNetAmount(new KualiDecimal("12761.79"));
        openItemReference.setType(type);
        openItemReference.setInvoiceNumber(invoiceNumber);
        return openItemReference;
    }

    protected List<AchIncomeNote> setupNotes(String noteText) {
        List<AchIncomeNote> notes = new ArrayList<>();
        AchIncomeNote note = new AchIncomeNote();
        note.setNoteText(noteText);
        notes.add(note);
        return notes;
    }

    private class TestableAdvanceDepositServiceImpl extends AdvanceDepositServiceImpl {

        @Override
        protected Integer getSourceAccountingLinePostingYear(SourceAccountingLine sourceAccountingLine) {
            return new Integer("2016");
        }

        @Override
        protected void setSourceAccountingLineAccountNumber(String account, SourceAccountingLine sourceAccountingLine) {
            Field accountNumber;
            try {
                accountNumber = sourceAccountingLine.getClass().getSuperclass().getDeclaredField("accountNumber");
            } catch (NoSuchFieldException e) {
                throw new RuntimeException("huh, how did this happen?", e);
            }
            accountNumber.setAccessible(true);
            try {
                accountNumber.set(sourceAccountingLine, account);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("huh, how did this happen?", e);
            }
        }
        
        @Override
        protected String getObjectCodeType(String chart, String objectCode) {
            if (EXPENSE_OBJECT_CODE.equalsIgnoreCase(objectCode)) {
                return CUKFSConstants.BasicAccountingCategory.EXPENSE;
            } else if (ASSET_OBJECT_CODE.equalsIgnoreCase(objectCode)) {
                return CUKFSConstants.BasicAccountingCategory.ASSET;
            } else
                return StringUtils.EMPTY;
        }     

    }

    private class MockBusinessObjectService implements BusinessObjectService {

        @Override
        public <T extends PersistableBusinessObject> T save(T bo) {
            return null;
        }

        @Override
        public List<? extends PersistableBusinessObject> save(List<? extends PersistableBusinessObject> businessObjects) {
            return null;
        }

        @Override
        public PersistableBusinessObject linkAndSave(PersistableBusinessObject bo) {
            return null;
        }

        @Override
        public List<? extends PersistableBusinessObject> linkAndSave(List<? extends PersistableBusinessObject> businessObjects) {
            return null;
        }

        @Override
        public <T extends BusinessObject> T findBySinglePrimaryKey(Class<T> clazz, Object primaryKey) {
            return null;
        }

        @Override
        public <T extends BusinessObject> T findByPrimaryKey(Class<T> clazz, Map<String, ?> primaryKeys) {
            return null;
        }

        @Override
        public PersistableBusinessObject retrieve(PersistableBusinessObject object) {
            return null;
        }

        @Override
        public <T extends BusinessObject> Collection<T> findAll(Class<T> clazz) {
            if (clazz.getName().equals("edu.cornell.kfs.fp.businessobject.IncomingWireAchMapping")) {
                IncomingWireAchMapping incomingWireAchMapping = new IncomingWireAchMapping();
                incomingWireAchMapping.setShortDescription("ARMY");
                incomingWireAchMapping.setAccountNumber("1234567");
                incomingWireAchMapping.setChartOfAccountsCode("CU");
                incomingWireAchMapping.setFinancialObjectCode("5000");
                List incomingWireAchMappings = new ArrayList<>();
                incomingWireAchMappings.add(incomingWireAchMapping);
                return incomingWireAchMappings;
            }
            return null;
        }

        @Override
        public <T extends BusinessObject> Collection<T> findAllOrderBy(Class<T> clazz, String sortField, boolean sortAscending) {
            return null;
        }

        @Override
        public <T extends BusinessObject> Collection<T> findMatching(Class<T> clazz, Map<String, ?> fieldValues) {
            return null;
        }

        @Override
        public <T extends BusinessObject> Collection<T> findMatching(Class<T> aClass, Map<String, ?> map, int i, int i1, Instant instant, Instant instant1, String[] strings) {
            return null;
        }

        @Override
        public int countMatching(Class clazz, Map<String, ?> fieldValues) {
            return 0;
        }

        @Override
        public int countMatching(Class aClass, Map<String, ?> map, Instant instant, Instant instant1) {
            return 0;
        }

        @Override
        public int countMatching(Class clazz, Map<String, ?> positiveFieldValues, Map<String, ?> negativeFieldValues) {
            return 0;
        }

        @Override
        public <T extends BusinessObject> Collection<T> findMatchingOrderBy(Class<T> clazz, Map<String, ?> fieldValues, String sortField, boolean sortAscending) {
            return null;
        }

        @Override
        public void delete(PersistableBusinessObject bo) {

        }

        @Override
        public void delete(List<? extends PersistableBusinessObject> boList) {

        }

        @Override
        public void deleteMatching(Class clazz, Map<String, ?> fieldValues) {

        }

        @Override
        public BusinessObject getReferenceIfExists(BusinessObject bo, String referenceName) {
            return null;
        }

        @Override
        public void linkUserFields(PersistableBusinessObject bo) {

        }

        @Override
        public void linkUserFields(List<PersistableBusinessObject> bos) {

        }

        @Override
        public PersistableBusinessObject manageReadOnly(PersistableBusinessObject bo) {
            return null;
        }
    }

}