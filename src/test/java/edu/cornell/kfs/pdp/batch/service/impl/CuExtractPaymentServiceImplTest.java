package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.kfs.pdp.businessobject.AchAccountNumber;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.Bank;

import com.rsmart.kuali.kfs.pdp.service.AchBundlerHelperService;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class CuExtractPaymentServiceImplTest {
    
    private static final String ACH_BATCH_TEST_FILE_PATH = "test";
    private static final String ACH_EXTRACT_TEST_FILE_PATH = ACH_BATCH_TEST_FILE_PATH + "/outputFiles";
    private static final String ACH_EXTRACT_TEST_FILE_NAME = "testAchExtractPaymentsOuput.txt";
    private static final String ACH_EXTRACT_TEST_PATH_AND_FILE = ACH_EXTRACT_TEST_FILE_PATH + File.separator + ACH_EXTRACT_TEST_FILE_NAME;
    private static final String ACH_EXTRACT_TEST_PATH_AND_FILE_FOR_VALIDATION = ACH_EXTRACT_TEST_PATH_AND_FILE + ".READY";
    private static final SimpleDateFormat yyyyMMddHHmmss_DATE_FORMATTER = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat yyyyMMdd_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");
    private static final String[] NOTIFICATION_EMAIL_ADDRESSES = {"abc@xyz.edu", "def@xyz.edu", "ghi@xyz.edu"};
    private static final int HEADER_LINE_IN_OUTPUT_FILE = 1;
    private static final int FIRST_PAYEE_LINE_IN_OUTPUT_FILE = HEADER_LINE_IN_OUTPUT_FILE + NOTIFICATION_EMAIL_ADDRESSES.length + 1;
    private static final Logger LOG = LogManager.getLogger(CuExtractPaymentServiceImplTest.class);
    
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
        
    }

}
