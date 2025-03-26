package edu.cornell.kfs.pmw.batch.businessobject;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

class PaymentWorksVendorTest {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String REQUESTING_COMPANY_DESCRIPTION = "Testing Company";
    
    PaymentWorksVendor pmwVendor;

    @BeforeEach
    void setUp() throws Exception {
        pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyDesc(REQUESTING_COMPANY_DESCRIPTION);
        pmwVendor.setRequestingCompanyTin("123232");
        pmwVendor.setRequestingCompanyW8W9("w8w9");
        pmwVendor.setBankAcctRoutingNumber("566");
        pmwVendor.setBankAcctBankAccountNumber("0998");
        pmwVendor.setBankAcctBankValidationFile("bankaccoountfile.pdf");
        pmwVendor.setPmwVendorRequestId("123");
        pmwVendor.setRequestingCompanyLegalName("Foo Bar Inc");
        
        LocalDateTime processingDateTime = LocalDateTime.of(2021, Month.JULY, 15, 7, 51);
        pmwVendor.setProcessTimestamp(Timestamp.valueOf(processingDateTime));
    }

    @AfterEach
    void tearDown() throws Exception {
        pmwVendor = null;
    }

    @Test
    void testToString() {
        String actualToString = pmwVendor.toString();
        LOG.info("testToStringForign: " + actualToString);
        
        assertRestrictedFieldFormattedCorrectly(actualToString, PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_TIN);
        assertRestrictedFieldFormattedCorrectly(actualToString, PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_W8_W9);
        assertRestrictedFieldFormattedCorrectly(actualToString, PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_ROUTING_NUMBER);
        assertRestrictedFieldFormattedCorrectly(actualToString, PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_BANK_ACCOUNT_NUMBER);
        assertRestrictedFieldFormattedCorrectly(actualToString, PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_BANK_VALIDATION_FILE);
        
        
        assertTrue("Should find the requesting company description", StringUtils.contains(actualToString, "requestingCompanyDesc" + 
                CUKFSConstants.EQUALS_SIGN + REQUESTING_COMPANY_DESCRIPTION));
        
        assertTrue("Should find the conflict of interest ", StringUtils.contains(actualToString, "conflictOfInterest" + 
                CUKFSConstants.EQUALS_SIGN + "false"));
    }
    
    private void assertRestrictedFieldFormattedCorrectly(String toStringValue, String searchField) {
        String searchString = searchField + CUKFSConstants.EQUALS_SIGN + PaymentWorksConstants.OUTPUT_RESTRICTED_DATA_PRESENT;
        assertTrue("Should find restricted value for " + searchField, StringUtils.contains(toStringValue, searchString));
    }
    
    @Test
    void testBuildPurgeableRecordingString() {
        String purgeRecordingString = pmwVendor.buildPurgeableRecordingString();
        LOG.info("testGetPurgeRecordingString: " + purgeRecordingString);
        
        assertPurgeRecordingStringContains(purgeRecordingString, "processTimestamp=07/15/2021 07:51:00 AM");
        assertPurgeRecordingStringContains(purgeRecordingString, "pmwVendorRequestId=123");
        assertPurgeRecordingStringContains(purgeRecordingString, "requestingCompanyLegalName=Foo Bar Inc");
    }

    public void assertPurgeRecordingStringContains(String purgeRecordingString, String searchString) {
        assertTrue("Should find " + searchString, StringUtils.contains(purgeRecordingString, searchString));
    }

}
