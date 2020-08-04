package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.mockito.Mockito;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorPayeeAchBatchReportData;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksFormModeService;

class PaymentWorksNewVendorPayeeAchServiceImplTest {
    
    private static final String ACH_BANK_NOT_US_ERROR_MESSAGE = "The bank has a country code of {0}.  We can only create ACH records for banks that have a US address";
    
    private PaymentWorksNewVendorPayeeAchServiceImpl achService;
    private PaymentWorksVendor pmwVendor;
    private PaymentWorksNewVendorPayeeAchBatchReportData reportData;

    @BeforeEach
    void setUp() throws Exception {
        achService = new PaymentWorksNewVendorPayeeAchServiceImpl();
        achService.setConfigurationService(buildMockConfigurationService());
        achService.setPaymentWorksNewVendorPayeeAchReportService(new PaymentWorksNewVendorPayeeAchReportServiceImpl());
        
        pmwVendor = new PaymentWorksVendor();
        reportData = new PaymentWorksNewVendorPayeeAchBatchReportData();
    }

    @AfterEach
    void tearDown() throws Exception {
        achService = null;
        pmwVendor = null;
        reportData = null;
    }

    @Test
    void testIsUsAchBankLegayForm() {
        achService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        boolean actualResults = achService.isUsAchBank(pmwVendor, reportData);
        assertTrue(actualResults);
        assertEquals(0, reportData.getRecordsThatCouldNotBeProcessedSummary().getRecordCount());
    }
    
    @Test
    void testIsUsAchBankForeignFormUsBank() {
        achService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        pmwVendor.setBankAddressCountry(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.getPmwCountryOptionAsString());
        boolean actualResults = achService.isUsAchBank(pmwVendor, reportData);
        assertTrue(actualResults);
        assertEquals(0, reportData.getRecordsThatCouldNotBeProcessedSummary().getRecordCount());
    }
    
    @Test
    void testIsUsAchBankForeignFormCanadaBank() {
        achService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        pmwVendor.setBankAddressCountry(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.getPmwCountryOptionAsString());
        boolean actualResults = achService.isUsAchBank(pmwVendor, reportData);
        assertFalse(actualResults);
        assertEquals(1, reportData.getRecordsThatCouldNotBeProcessedSummary().getRecordCount());
    }
    
    private PaymentWorksFormModeService buildMockPaymentWorksFormModeService(boolean shouldUseForeignForm) {
        PaymentWorksFormModeService formService = Mockito.mock(PaymentWorksFormModeService.class);
        Mockito.when(formService.shouldUseForeignFormProcessingMode()).thenReturn(shouldUseForeignForm);
        return formService;
    }
    
    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService configService = Mockito.mock(ConfigurationService.class);
        Mockito.when(configService.getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_BANK_NOT_US)).thenReturn(ACH_BANK_NOT_US_ERROR_MESSAGE);
        return configService;
    }

}
