package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.mockito.Mockito;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksParameterConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksPropertiesConstants;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

class PaymentWorksFormModeServiceImplTtest {
    
    private PaymentWorksFormModeServiceImpl paymentWorksFormModeServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        paymentWorksFormModeServiceImpl = new PaymentWorksFormModeServiceImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        paymentWorksFormModeServiceImpl = null;
    }
    
    @Test
    void testForeignMode() {
        paymentWorksFormModeServiceImpl.setParameterService(buildMockParameterService(
                PaymentWorksPropertiesConstants.PaymentWorksFromModes.FOREIGN_FORM_MODE));
        assertTrue(paymentWorksFormModeServiceImpl.shouldUseForeignFormProcessingMode());
        assertFalse(paymentWorksFormModeServiceImpl.shouldUseLegacyFormProcessingMode());
    }
    
    @Test
    void testLegacyModeLowerCase() {
        paymentWorksFormModeServiceImpl.setParameterService(buildMockParameterService(
                StringUtils.lowerCase(PaymentWorksPropertiesConstants.PaymentWorksFromModes.LEGACY_FORM_MODE)));
        assertFalse(paymentWorksFormModeServiceImpl.shouldUseForeignFormProcessingMode());
        assertTrue(paymentWorksFormModeServiceImpl.shouldUseLegacyFormProcessingMode());
    }
    
    @Test
    void testInvalidFormMode() {
        paymentWorksFormModeServiceImpl.setParameterService(buildMockParameterService("foo"));
        try {
            boolean results = paymentWorksFormModeServiceImpl.shouldUseForeignFormProcessingMode();
        } catch (IllegalArgumentException iae) {
            assertTrue("Successfully caught an illegal argument exception", true);
            return;
        }
        assertTrue("Should have caught an exception above", false);
    }
    
    private ParameterService buildMockParameterService(String formMode) {
        ParameterService service = Mockito.mock(ParameterService.class);
        Mockito.when(service.getParameterValueAsString(PaymentWorksConstants.PAYMENTWORKS_NAMESPACE_CODE, 
                CUKFSParameterKeyConstants.ALL_COMPONENTS, PaymentWorksParameterConstants.PAYMENTWORKS_FORM_PROCESSING_MODE)).thenReturn(formMode);
        return service;
    }

}
