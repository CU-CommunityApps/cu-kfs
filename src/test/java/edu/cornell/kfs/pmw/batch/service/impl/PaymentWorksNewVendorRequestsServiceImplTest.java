package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;

class PaymentWorksNewVendorRequestsServiceImplTest extends PaymentWorksNewVendorRequestsServiceImpl {
    
    private PaymentWorksNewVendorRequestsServiceImpl paymentWorksNewVendorRequestsServiceImpl;
    private List<String> errorMessages;
    
    private String FOREIGN_VENDOR_FORM_DATE_FORMAT_EXAMPLE = "2020-05-26";
    private String LEGACY_FORM_DATE_FORMAT_EXAMPLE = "05/26/2020";
    private static final String DATE_FORMAT_ERROR_MESSAGE_TEST = "just a test error message";
    
    @BeforeEach
    void setUp() throws Exception {
        paymentWorksNewVendorRequestsServiceImpl = new PaymentWorksNewVendorRequestsServiceImpl();
        errorMessages = new ArrayList<String>();
    }

    @AfterEach
    void tearDown() throws Exception {
        paymentWorksNewVendorRequestsServiceImpl = null;
        errorMessages = null;
    }

    @Test
    void testEnteredDateIsFormattedProperlyLegacy() {
        boolean actualResults = paymentWorksNewVendorRequestsServiceImpl.enteredDateIsFormattedProperly(LEGACY_FORM_DATE_FORMAT_EXAMPLE, 
                DATE_FORMAT_ERROR_MESSAGE_TEST, PaymentWorksConstants.PATTERN_COMPILED_REGEX_FOR_MM_SLASH_DD_SLASH_YYYY, errorMessages);
        assertTrue(actualResults);
        assertTrue(errorMessages.isEmpty());
    }
    
    @Test
    void testEnteredDateIsFormattedProperlyForeign() {
        boolean actualResults = paymentWorksNewVendorRequestsServiceImpl.enteredDateIsFormattedProperly(FOREIGN_VENDOR_FORM_DATE_FORMAT_EXAMPLE, 
                DATE_FORMAT_ERROR_MESSAGE_TEST, PaymentWorksConstants.PATTERN_COMPILED_REGEX_FOR_YYYY_SLASH_MM_SLASH_DD, errorMessages);
        assertTrue(actualResults);
        assertTrue(errorMessages.isEmpty());
    }

}
