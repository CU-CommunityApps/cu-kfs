package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.mockito.Mockito;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksFormModeService;

class PaymentWorksVendorToKfsVendorDetailConversionServiceImplTest {
    
    private static final String LEGACY_PO_COUNTRY = "legacy po country";
    private static final String FOREGIN_PO_COUNTRY = "foreign po country";
    private static final String ARUBA_ISO_CODE = "AW";
    private static final String ARUBA_FIPS_CODE = "AA";
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
    
    private PaymentWorksFormModeService buildMockPaymentWorksFormModeService(boolean useForeign) {
        PaymentWorksFormModeService service = Mockito.mock(PaymentWorksFormModeService.class);
        Mockito.when(service.shouldUseForeignFormProcessingMode()).thenReturn(useForeign);
        Mockito.when(service.shouldUseLegacyFormProcessingMode()).thenReturn(!useForeign);
        return service;
    }

    @Test
    void testFindPoCountryToUseLegacy() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        pmwVendor.setPoCountryLegacy(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(LEGACY_PO_COUNTRY, actual);
    }
    
    @Test
    void testFindPoCountryToUseLegacyBlankPoCountry() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        pmwVendor.setPoCountryLegacy(KFSConstants.BLANK_SPACE);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(KFSConstants.COUNTRY_CODE_UNITED_STATES, actual);
    }
    
    @Test
    void testFindPoCountryToUseForeignUS() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        pmwVendor.setPoCountryLegacy(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(KFSConstants.COUNTRY_CODE_UNITED_STATES, actual);
    }
    
    @Test
    void testFindPoCountryToUseForeignAustralia() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        pmwVendor.setPoCountryLegacy(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.FIPS_COUNTRY_CODE_AUSTRALIA);
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(PaymentWorksConstants.FIPS_COUNTRY_CODE_AUSTRALIA, actual);
    }
    
    @Test
    void testFindPoCountryToUseForeignOther() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        pmwVendor.setPoCountryLegacy(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountry(FOREGIN_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(StringUtils.upperCase(PaymentWorksConstants.PO_ADDRESS_COUNTRY_OTHER));
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(FOREGIN_PO_COUNTRY, actual);
    }
    
    @Test
    void testShouldCreateContactLegacyGoodContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        String contactName = "foo";
        assertTrue(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactLegacyNullContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        String contactName = null;
        assertTrue(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactLegacyEmptyContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        String contactName = StringUtils.EMPTY;
        assertTrue(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactForeignGoodContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        String contactName = "foo";
        assertTrue(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactForeignNullContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        String contactName = null;
        assertFalse(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactForeignEmptyContact() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        String contactName = StringUtils.EMPTY;
        assertFalse(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testBuildPOFipsCountryCodeUSForeignMode() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        String expectedPoCountry = PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.fipsCountryCode;
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.pmwCountryOptionAsString);
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    @Test
    void testBuildPOFipsCountryCodeUSLegacyMode() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        String expectedPoCountry = PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.fipsCountryCode;
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.pmwCountryOptionAsString);
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    @Test
    void testBuildPOFipsCountryCodeCanadaForeignMode() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        String expectedPoCountry = PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.fipsCountryCode;
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.pmwCountryOptionAsString);
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    @Test
    void testBuildPOFipsCountryCodeCanadaLegacyMode() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        String expectedPoCountry = PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.fipsCountryCode;
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.pmwCountryOptionAsString);
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    @Test
    void testBuildPOFipsCountryCodeArubaForeignMode() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        String expectedPoCountry = ARUBA_FIPS_CODE;
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.OTHER.pmwCountryOptionAsString);
        pmwVendor.setPoCountry(ARUBA_ISO_CODE);
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    @Test
    void testBuildPOFipsCountryCodeEmptyCountryForeignMode() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        String expectedPoCountry = StringUtils.EMPTY;
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.OTHER.pmwCountryOptionAsString);
        pmwVendor.setPoCountry(StringUtils.EMPTY);
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    @Test
    void testBuildPOFipsCountryCodeBadCountryForeignMode() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        String expectedPoCountry = StringUtils.EMPTY;
        pmwVendor.setPoCountryUsCanadaAustraliaOther("foo");
        pmwVendor.setPoCountry("foo");
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    private Map<String, List<PaymentWorksIsoFipsCountryItem>> buildPaymentWorksIsoToFipsCountryMap() {
        Map<String, List<PaymentWorksIsoFipsCountryItem>> countryMap = new HashMap<String, List<PaymentWorksIsoFipsCountryItem>>();
        PaymentWorksIsoFipsCountryItem arubaItem = new PaymentWorksIsoFipsCountryItem();
        arubaItem.setFipsCountryCode(ARUBA_FIPS_CODE);
        List<PaymentWorksIsoFipsCountryItem> items = new ArrayList<PaymentWorksIsoFipsCountryItem>();
        items.add(arubaItem);
        countryMap.put(ARUBA_ISO_CODE, items);
        return countryMap;
    }
    
}
