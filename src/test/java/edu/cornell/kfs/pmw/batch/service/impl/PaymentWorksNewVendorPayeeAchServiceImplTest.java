package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.mockito.Mockito;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.PaymentWorksKeyConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksBatchReportVendorItem;
import edu.cornell.kfs.pmw.batch.report.PaymentWorksNewVendorPayeeAchBatchReportData;

class PaymentWorksNewVendorPayeeAchServiceImplTest {
    
    private static final String ACH_BANK_NOT_US_ERROR_MESSAGE = "The bank has a country code of {0}.  We can only create ACH records for banks that have a US address";
    
    private PaymentWorksNewVendorPayeeAchServiceImpl achService;
    private PaymentWorksVendor pmwVendor;
    private PaymentWorksNewVendorPayeeAchBatchReportData reportData;

    @BeforeEach
    void setUp() throws Exception {
        Configurator.setLevel(PaymentWorksNewVendorPayeeAchServiceImpl.class.getName(), Level.DEBUG);
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
    void testIsUsAchBankForeignFormUsBank() {
        pmwVendor.setBankAddressCountry(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.getPmwCountryOptionAsString());
        boolean actualResults = achService.isUsAchBank(pmwVendor, reportData);
        assertTrue(actualResults);
        assertEquals(0, reportData.getRecordsThatCouldNotBeProcessedSummary().getRecordCount());
    }
    
    @Test
    void testIsUsAchBankForeignFormCanadaBank() {
        pmwVendor.setBankAddressCountry(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.getPmwCountryOptionAsString());
        boolean actualIsUsBankAccount = achService.isUsAchBank(pmwVendor, reportData);
        assertFalse(actualIsUsBankAccount);
        
        assertEquals(0, reportData.getRecordsThatCouldNotBeProcessedSummary().getRecordCount());
        assertEquals(0, reportData.getPmwVendorAchsThatCouldNotBeProcessed().size());
        
        assertEquals(1, reportData.getRecordsWithForeignAchSummary().getRecordCount());
        List<PaymentWorksBatchReportVendorItem> foreignAchItems = reportData.getForeignAchItems();
        assertEquals(1, foreignAchItems.size());
        List<String> foreignAchErrorMessages = foreignAchItems.get(0).getErrorMessages();
        assertEquals(1, foreignAchErrorMessages.size());
        
        String actualErrorMessage = foreignAchErrorMessages.get(0);
        assertEquals("The bank has a country code of Canada.  We can only create ACH records for banks that have a US address", actualErrorMessage);
    }
    
    @Test
    void testIsAchPaymentMethodForeignFormAch() {
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.ACH);
        boolean actualAchPaymentMethod = achService.isAchPaymentMethod(pmwVendor, reportData);
        assertTrue(actualAchPaymentMethod);
    }
    
    @Test
    void testIsAchPaymentMethodForeignFormCheck() {
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.CHECK);
        boolean actualAchPaymentMethod = achService.isAchPaymentMethod(pmwVendor, reportData);
        assertFalse(actualAchPaymentMethod);
    }
    
    @Test
    void testIsAchPaymentMethodForeignFormForeignWire() {
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.WIRE);
        pmwVendor.setRequestingCompanyTaxCountry(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.getPmwCountryOptionAsString());
        boolean actualAchPaymentMethod = achService.isAchPaymentMethod(pmwVendor, reportData);
        assertFalse(actualAchPaymentMethod);
    }
    
    @Test
    void testIsAchPaymentMethodForeignFormDomesticWire() {
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.WIRE);
        pmwVendor.setRequestingCompanyTaxCountry(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.getPmwCountryOptionAsString());
        boolean actualAchPaymentMethod = achService.isAchPaymentMethod(pmwVendor, reportData);
        assertFalse(actualAchPaymentMethod);
    }
    
    private ConfigurationService buildMockConfigurationService() {
        ConfigurationService configService = Mockito.mock(ConfigurationService.class);
        Mockito.when(configService.getPropertyValueAsString(PaymentWorksKeyConstants.ERROR_PAYMENTWORKS_BANK_NOT_US)).thenReturn(ACH_BANK_NOT_US_ERROR_MESSAGE);
        return configService;
    }

}
