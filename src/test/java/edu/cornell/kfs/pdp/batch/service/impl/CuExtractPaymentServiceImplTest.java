package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.pdp.businessobject.AchAccountNumber;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class CuExtractPaymentServiceImplTest {
    
    private static final String ACH_BATCH_TEST_FILE_PATH = "test";
    private static final String ACH_EXTRACT_TEST_FILE_PATH = ACH_BATCH_TEST_FILE_PATH + "/outputFiles";
    private static final String ACH_EXTRACT_TEST_FILE_NAME = "testAchExtractPaymentsOuput.txt";
    private static final String ACH_EXTRACT_TEST_PATH_AND_FILE = ACH_EXTRACT_TEST_FILE_PATH + File.separator + ACH_EXTRACT_TEST_FILE_NAME;
    private static final String ACH_EXTRACT_TEST_PATH_AND_FILE_FOR_VALIDATION = ACH_EXTRACT_TEST_PATH_AND_FILE + ".READY";
    private static final SimpleDateFormat yyyyMMddHHmmss_DATE_FORMATTER = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMddHHmmss, Locale.US);
    private static final SimpleDateFormat yyyyMMdd_DATE_FORMATTER = new SimpleDateFormat(CUKFSConstants.DATE_FORMAT_yyyyMMdd, Locale.US);
    private static final String[] NOTIFICATION_EMAIL_ADDRESSES = {"abc@xyz.edu", "def@xyz.edu", "ghi@xyz.edu"};
    private static final int HEADER_LINE_IN_OUTPUT_FILE = 1;
    private static final int FIRST_PAYEE_LINE_IN_OUTPUT_FILE = HEADER_LINE_IN_OUTPUT_FILE + NOTIFICATION_EMAIL_ADDRESSES.length + 1;
    private static final Logger LOG = LogManager.getLogger(CuExtractPaymentServiceImplTest.class);
    
    private static final String EDOC_DV_IDENTIFIER_AND_NUMBER = CuDisbursementVoucherConstants.DV_EXTRACT_EDOC_NUMBER_PREFIX_IDENTIFIER + "12345678 ";
    private static final int MAX_NUM_DV_CHECK_STUB_NOTES_TESTS = 5;
    private static final int MAX_NUM_DV_CHECK_STUB_LINES_PER_TEST = 3;
    
                                                       //         1         2         3         4         5         6         7
                                                       //012345678901234567890123456789012345678901234567890123456789012345678901
    private static final String[] DV_NOTES_TEST_DATA = {"First line of note text present. Next two lines blank.",
                                                        "",
                                                        "",
                                                        
                                                        "First line for note text fully filled with data to see what will happen", 
                                                        " Second line partially filled. Third is blank",
                                                        "",
                                                        
                                                        "First line notes text fully filled with characters to see how this wrap", 
                                                        "Second line notes text is fully filled with data to see what it will do",
                                                        "Third line is partially filled.",
                                                        
                                                        "First line where line fully filled with data to see what will happen to", 
                                                        "Second line notes text is fully filled with data to see what it will do",
                                                        "Third line notes text fully filled should truncate all extra characters",
                                                       
                                                        "First line with data up to the very end with no wrap to see", 
                                                        "Second line note text not fully filled with data to see what it will do",
                                                        "Third lines notes text fully filled should not truncate extra characters"};

                                                                //         1         2         3         4         5         6         7
                                                                //012345678901234567890123456789012345678901234567890123456789012345678901
    private static final String[] DV_CHECK_STUB_EXPECTED_TEXT = {"Doc:12345678 First line of note text present. Next two lines blank.",
                                                                 "",
                                                                 "",
                                                                 
                                                                 "Doc:12345678 First line for note text fully filled with data to see what", 
                                                                 "will happen Second line partially filled. Third is blank",
                                                                 "",
            
                                                                 "Doc:12345678 First line notes text fully filled with characters to see", 
                                                                 "how this wrap Second line notes text is fully filled with data to see",
                                                                 "what it will do Third line is partially filled.",
            
                                                                 "Doc:12345678 First line where line fully filled with data to see what", 
                                                                 "will happen to Second line notes text is fully filled with data to see",
                                                                 "what it will do Third line notes text fully filled should truncate all",
                                                                 
                                                                 "Doc:12345678 First line with data up to the very end with no wrap to see", 
                                                                 "Second line note text not fully filled with data to see what it will do",
                                                                 "Third lines notes text fully filled should not truncate extra characters"};
    
    private CuExtractPaymentServiceImpl cuExtractPaymentServiceImpl;

    @Before
    public void setUp() throws Exception {
        cuExtractPaymentServiceImpl = new TestCuExtractPaymentServiceImpl();
        FileUtils.forceMkdir(new File(ACH_EXTRACT_TEST_FILE_PATH));
    }

    @After
    public void tearDown() throws Exception {
        cuExtractPaymentServiceImpl = null;
        FileUtils.forceDelete(new File(ACH_BATCH_TEST_FILE_PATH).getAbsoluteFile());
    }
    
    @Test
    public void testDvCheckStubCreation() {
        int line1ArrayPosition = 0;
        int line2ArrayPosition = 1;
        int line3ArrayPosition = 2;
        
        for (int testIndex = 1; testIndex <= MAX_NUM_DV_CHECK_STUB_NOTES_TESTS; testIndex++) {
            LOG.info("testDvCheckStubCreation: Test[" + testIndex + "]");
            String stubLine1 = EDOC_DV_IDENTIFIER_AND_NUMBER;
            String stubLine2 = "";
            String stubLine3 = "";
            String noteLine = DV_NOTES_TEST_DATA[line1ArrayPosition];
            
            if (noteLine.length() >= 0) {
                //Trim initialization data of any leading or trailing spaces.
                //Subsequent methods called deal with this internally for note lines 2&3 initalization.
                //Being done here so test mimics actual processing method logic.
                stubLine1 = cuExtractPaymentServiceImpl.stripTrailingSpace(cuExtractPaymentServiceImpl.stripLeadingSpace(stubLine1));
                
                stubLine2 = cuExtractPaymentServiceImpl.obtainNoteLineSectionExceedingCheckStubLine(noteLine, stubLine1);
                
                stubLine1 = (StringUtils.isBlank(stubLine1))
                        ? stubLine1.concat(cuExtractPaymentServiceImpl.obtainLeadingNoteLineSection(noteLine, stubLine1))
                                : stubLine1.concat(KFSConstants.BLANK_SPACE).concat(cuExtractPaymentServiceImpl.obtainLeadingNoteLineSection(noteLine, stubLine1));
                
                noteLine = DV_NOTES_TEST_DATA[line2ArrayPosition];
                
                if (noteLine.length() >= 0) {
                    stubLine3 = cuExtractPaymentServiceImpl.obtainNoteLineSectionExceedingCheckStubLine(noteLine, stubLine2);
                    
                    stubLine2 = (StringUtils.isBlank(stubLine2))
                            ? stubLine2.concat(cuExtractPaymentServiceImpl.obtainLeadingNoteLineSection(noteLine, stubLine2))
                                    : stubLine2.concat(KFSConstants.BLANK_SPACE).concat(cuExtractPaymentServiceImpl.obtainLeadingNoteLineSection(noteLine, stubLine2));
                            
                    noteLine = DV_NOTES_TEST_DATA[line3ArrayPosition];
                    
                    if (noteLine.length() >= 0) {
                        stubLine3 = (StringUtils.isBlank(stubLine3))
                                ? stubLine3.concat(cuExtractPaymentServiceImpl.obtainLeadingNoteLineSection(noteLine, stubLine3))
                                        : stubLine3.concat(KFSConstants.BLANK_SPACE).concat(cuExtractPaymentServiceImpl.obtainLeadingNoteLineSection(noteLine, stubLine3));
                    }
                }
            }
          
            LOG.info("stubLine1.length() = '" + stubLine1.length() + "'" );
            LOG.info("stubLine2.length() = '" + stubLine2.length() + "'" );
            LOG.info("stubLine3.length() = '" + stubLine3.length() + "'" );
            
            LOG.info("Check Stub Text for check #" + testIndex);
            LOG.info("stubLine1 = '" + stubLine1 + "'" );
            LOG.info("stubLine2 = '" + stubLine2 + "'" );
            LOG.info("stubLine3 = '" + stubLine3 + "'" );
            LOG.info("");
            
            assertEquals("TEST(" + testIndex + ") should equal results for Check Stub Data("+ line1ArrayPosition + "): " , DV_CHECK_STUB_EXPECTED_TEXT[line1ArrayPosition], stubLine1);
            assertEquals("TEST(" + testIndex + ") should equal results for Check Stub Data("+ line2ArrayPosition + "): " , DV_CHECK_STUB_EXPECTED_TEXT[line2ArrayPosition], stubLine2);
            assertEquals("TEST(" + testIndex + ") should equal results for Check Stub Data("+ line3ArrayPosition + "): " , DV_CHECK_STUB_EXPECTED_TEXT[line3ArrayPosition], stubLine3);
            
            line1ArrayPosition = line1ArrayPosition + MAX_NUM_DV_CHECK_STUB_LINES_PER_TEST;
            line2ArrayPosition = line2ArrayPosition + MAX_NUM_DV_CHECK_STUB_LINES_PER_TEST;
            line3ArrayPosition = line3ArrayPosition + MAX_NUM_DV_CHECK_STUB_LINES_PER_TEST;
        }
    }

    @Test
    public void testCalculateHeaderDate() {
        Date now = new Date(Calendar.getInstance().getTimeInMillis());
        Date functionReturnedDate = cuExtractPaymentServiceImpl.calculateHeaderDate(now);
        Calendar calendarCalculator = Calendar.getInstance();
        calendarCalculator.setTimeInMillis(now.getTime());
        calendarCalculator.add(Calendar.DATE, 1);
        Date expectedDate = new Date(calendarCalculator.getTimeInMillis());
        assertEquals("headerDate from function should equal date we calculate in the same manner:  ", expectedDate, functionReturnedDate);
    }
    
    @Test
    public void testAchFileHeaderDateIsOneDayAfterProcessDate() {
        cuExtractPaymentServiceImpl.setAchBundlerHelperService(new TestAchBundlerHelperService());
        
        PaymentStatus extractedStatus = new PaymentStatus();
        Date processDate = new Date(Calendar.getInstance().getTimeInMillis());
        List<String> notificationEmailAddresses = new ArrayList<String>(Arrays.asList(NOTIFICATION_EMAIL_ADDRESSES));
        
        cuExtractPaymentServiceImpl.writeExtractAchFileMellonBankFastTrack(extractedStatus, ACH_EXTRACT_TEST_PATH_AND_FILE, processDate, yyyyMMddHHmmss_DATE_FORMATTER, notificationEmailAddresses);
        
        Date expectedHeaderDate = cuExtractPaymentServiceImpl.calculateHeaderDate(processDate);
        assertTrue("ACH Extract Payments File Header Date is not what was expected.", isDateInOutputFileWhereExpected(HEADER_LINE_IN_OUTPUT_FILE, yyyyMMddHHmmss_DATE_FORMATTER.format(expectedHeaderDate)));
        assertTrue("ACH Extract Payments File Payee Date is not what was expected.", isDateInOutputFileWhereExpected(FIRST_PAYEE_LINE_IN_OUTPUT_FILE, yyyyMMdd_DATE_FORMATTER.format(processDate)));
    }
    
    private boolean isDateInOutputFileWhereExpected(int numberOfLinesToRead, String formattedDateExpected) {
        BufferedReader inputReader = null;
        boolean expectedWasFound = false;
        try {
            String lineWeWant = null;
            inputReader = new BufferedReader(new FileReader(new String (ACH_EXTRACT_TEST_PATH_AND_FILE_FOR_VALIDATION)));
            for (int i=0; i<numberOfLinesToRead; i++) {
                lineWeWant = inputReader.readLine();
            }
            expectedWasFound = lineWeWant.contains(formattedDateExpected);
        }
        catch (IOException ie) {
            expectedWasFound = false;
        }
        finally {
            if (inputReader != null) {
                try {
                    inputReader.close();
                }
                catch (IOException ie) {
                    expectedWasFound = false;
                }
            }
        }
        return expectedWasFound;
    }

    private class TestCuExtractPaymentServiceImpl extends CuExtractPaymentServiceImpl {
        @Override
        protected boolean isProduction() {
            return false;
        }
    }

    private class TestAchBundlerHelperService implements AchBundlerHelperService {

        private static final String BANKCODE = "bankCode";
        private final Integer DISB_ID = new Integer(1);

        @Override
        public Iterator<PaymentDetail> getPendingAchPaymentDetailsByDisbursementNumberAndBank(Integer disbursementNumber, String bankCode) {
            ArrayList<PaymentDetail> paymentDetails = new ArrayList<PaymentDetail>();

            PaymentDetail paymentDetail = new PaymentDetail();
            paymentDetail.setNetPaymentAmount(new KualiDecimal(1.11));

            PaymentGroup paymentGroup = new PaymentGroup();
            Bank bank = new Bank();
            bank.setBankAccountNumber("123-456789");
            bank.setBankRoutingNumber("111111111");
            paymentGroup.setBank(bank);
            paymentGroup.setPymtAttachment(new Boolean(false));
            paymentGroup.setPymtSpecialHandling(new Boolean(false));
            paymentGroup.setPayeeIdTypeCd("V");
            paymentGroup.setAchAccountType("22ACHaccount");
            paymentGroup.setAchBankRoutingNbr("22222222222");
            AchAccountNumber achAccount = new AchAccountNumber();
            achAccount.setAchBankAccountNbr("666666666");
            paymentGroup.setAchAccountNumber(achAccount);
            paymentGroup.setDisbursementNbr(new KualiInteger(123456789));

            paymentDetail.setPaymentGroup(paymentGroup);
            paymentDetails.add(paymentDetail);
            return paymentDetails.iterator();
        }

        @Override
        public Iterator<PaymentDetail> getPendingAchPaymentDetailsByBank(String bankCode) {
            return null;
        }

        @Override
        public HashSet<String> getDistinctBankCodesForPendingAchPayments() {
            HashSet<String> returnVals = new HashSet<String>();
            returnVals.add(BANKCODE);
            return returnVals;
        }

        @Override
        public HashSet<Integer> getDistinctDisbursementNumbersForPendingAchPaymentsByBankCode(String bankCode) {
            HashSet<Integer> returnVals = new HashSet<Integer>();
            returnVals.add(DISB_ID);
            return returnVals;
        }

        @Override
        public boolean shouldBundleAchPayments() {
            return true;
        }
        
        @Override
        public String getPdpFormatFailureToEmailAddress() {
            return KFSConstants.EMPTY_STRING;
        }
        
    }

}
