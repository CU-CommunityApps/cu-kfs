package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksFormModeService;

class PaymentWorksVendorToKfsVendorDetailConversionServiceImplTest {
    
    private static final String LEGACY_PO_COUNTRY = "legacy po country";
    private PaymentWorksVendorToKfsVendorDetailConversionServiceImpl conversionService;
    private PaymentWorksVendor pmwVendor;

    @BeforeEach
    void setUp() throws Exception {
        Configurator.setLevel(PaymentWorksVendorToKfsVendorDetailConversionServiceImpl.class.getName(), Level.DEBUG);
        conversionService = new PaymentWorksVendorToKfsVendorDetailConversionServiceImpl();
        pmwVendor = new PaymentWorksVendor();
    }

    @AfterEach
    void tearDown() throws Exception {
        conversionService = null;
        pmwVendor = null;
    }
    
    private PaymentWorksFormModeService buildMockPaymentWorksFormModeService(boolean useLegacy, boolean useForeign) {
        PaymentWorksFormModeService service = Mockito.mock(PaymentWorksFormModeService.class);
        Mockito.when(service.shouldUseForeignFormProcessingMode()).thenReturn(useForeign);
        Mockito.when(service.shouldUseLegacyFormProcessingMode()).thenReturn(useLegacy);
        return service;
    }

    @Test
    void testFindPoCountryToUseLegacy() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true, false));
        pmwVendor.setPoCountry(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(LEGACY_PO_COUNTRY, actual);
    }
    
    @Test
    void testFindPoCountryToUseForeignUS() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false, true));
        pmwVendor.setPoCountry(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(KFSConstants.COUNTRY_CODE_UNITED_STATES, actual);
    }
    
    @Test
    void testFindPoCountryToUseForeignAustralia() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false, true));
        pmwVendor.setPoCountry(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.FIPS_COUNTRY_CODE_AUSTRALIA);
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(PaymentWorksConstants.FIPS_COUNTRY_CODE_AUSTRALIA, actual);
    }
    
    @Test
    void testFindPoCountryToUseForeignOther() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false, true));
        pmwVendor.setPoCountry(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(StringUtils.upperCase(PaymentWorksConstants.PO_COUNTRY_US_CANADA_AUSTRALIA_OTHER_VALUE_OTHER));
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(LEGACY_PO_COUNTRY, actual);
    }
    
    @Test
    void testShouldCreateContactLegacyGoodContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true, false));
        String contactName = "foo";
        assertTrue(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactLegacyNullContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true, false));
        String contactName = null;
        assertTrue(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactLegacyEmptyContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true, false));
        String contactName = StringUtils.EMPTY;
        assertTrue(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactForeignGoodContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false, true));
        String contactName = "foo";
        assertTrue(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactForeignNullContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false, true));
        String contactName = null;
        assertFalse(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactForeignEmptyContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false, true));
        String contactName = StringUtils.EMPTY;
        assertFalse(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testFormatFaxNumberPaymentWorksExample() {
        String actual = conversionService.formatFaxNumber("+14053000111");
        String expected = "405-300-0111";
        assertEquals(expected, actual);
    }
    
    @Test
    void testFormatFaxNumberSimple() {
        String actual = conversionService.formatFaxNumber("1234567890");
        String expected = "123-456-7890";
        assertEquals(expected, actual);
    }
    
    @Test
    void testFormatFaxNumberNull() {
        String actual = conversionService.formatFaxNumber(null);
        String expected = null;
        assertEquals(expected, actual);
    }
    
    @Test
    void testFormatFaxNumberEmptyString() {
        String actual = conversionService.formatFaxNumber(StringUtils.EMPTY);
        String expected = StringUtils.EMPTY;
        assertEquals(expected, actual);
    }
    
    @Test
    void testFormatFaxNumberEmptyNoFormatNeeded() {
        String goodFormat = "607-255=2047";
        String actual = conversionService.formatFaxNumber(goodFormat);
        String expected = goodFormat;
        assertEquals(expected, actual);
    }
    
    @Test
    void testFormatFaxNumberEmptyUnexpected() {
        String unexpectedFormat = "607*255zåå2047";
        String actual = conversionService.formatFaxNumber(unexpectedFormat);
        String expected = unexpectedFormat;
        assertEquals(expected, actual);
    }

}
