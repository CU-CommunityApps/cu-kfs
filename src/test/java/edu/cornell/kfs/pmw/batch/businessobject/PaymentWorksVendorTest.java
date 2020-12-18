package edu.cornell.kfs.pmw.batch.businessobject;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

class PaymentWorksVendorTest {
    private static final Logger LOG = LogManager.getLogger(PaymentWorksVendorTest.class);
    
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
    }

    @AfterEach
    void tearDown() throws Exception {
        pmwVendor = null;
    }

    @Test
    void testToString() {
        String actualToString = pmwVendor.toString();
        LOG.info("testToStringForign: " + actualToString);
        
        assertRestringFieldExists(actualToString, PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_TIN);
        assertRestringFieldExists(actualToString, PaymentWorksConstants.PaymentWorksVendorFieldName.REQUESTING_COMPANY_W8_W9);
        assertRestringFieldExists(actualToString, PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_ROUTING_NUMBER);
        assertRestringFieldExists(actualToString, PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_BANK_ACCOUNT_NUMBER);
        assertRestringFieldExists(actualToString, PaymentWorksConstants.PaymentWorksVendorFieldName.BANK_ACCOUNT_BANK_VALIDATION_FILE);
        
        
        assertTrue("Should find the requesting company description", StringUtils.contains(actualToString, "requestingCompanyDesc" + 
                CUKFSConstants.EQUALS_SIGN + REQUESTING_COMPANY_DESCRIPTION));
        
        assertTrue("Should find the informal marketing ", StringUtils.contains(actualToString, "informalMarketing" + 
                CUKFSConstants.EQUALS_SIGN + "false"));
    }
    
    private void assertRestringFieldExists(String toStringValue, String searchField) {
        String searchString = searchField + CUKFSConstants.EQUALS_SIGN + PaymentWorksConstants.OUTPUT_RESTRICTED_DATA_PRESENT;
        assertTrue("Should find restricted value for " + searchField, StringUtils.contains(toStringValue, searchString));
    }

}
