package edu.cornell.kfs.pmw.batch.businessobject;

import static org.junit.Assert.assertTrue;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

class PaymentWorksVendorTest {
    private static final Logger LOG = LogManager.getLogger(PaymentWorksVendorTest.class);
    
    private static final String REQUESTING_COMPANY_DESCRIPTION = "Testing Company";
    
    PaymentWorksVendor pmwVendor;

    @BeforeEach
    void setUp() throws Exception {
        pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyDesc(REQUESTING_COMPANY_DESCRIPTION);
        pmwVendor.setRequestingCompanyTin("123232");
    }

    @AfterEach
    void tearDown() throws Exception {
        pmwVendor = null;
    }

    @Test
    void testToString() {
        String actualToString = pmwVendor.toString();
        LOG.info("testToStringForign: " + actualToString);
        checkToStringValue(actualToString, "=");
    }
    
    private void checkToStringValue(String toStringValue, String connector) {
        assertTrue("Should find the requesting company TIN", StringUtils.contains(toStringValue, "requestingCompanyTin" + 
                connector + PaymentWorksConstants.OUTPUT_RESTRICTED_DATA_PRESENT));
        assertTrue("Should find the requesting company description", StringUtils.contains(toStringValue, "requestingCompanyDesc" + 
                connector + REQUESTING_COMPANY_DESCRIPTION));
        assertTrue("Should find the informal marketing ", StringUtils.contains(toStringValue, "informalMarketing" + 
                connector + "false"));
    }

}
