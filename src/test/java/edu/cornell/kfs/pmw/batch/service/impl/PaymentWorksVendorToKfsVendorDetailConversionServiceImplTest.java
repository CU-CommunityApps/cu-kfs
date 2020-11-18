package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorContact;
import org.kuali.kfs.vnd.businessobject.VendorContactPhoneNumber;
import org.mockito.Mockito;

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
import edu.cornell.kfs.pmw.batch.service.PaymentWorksFormModeService;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;
import edu.cornell.kfs.vnd.businessobject.VendorDetailExtension;

class PaymentWorksVendorToKfsVendorDetailConversionServiceImplTest {
    
    private static final String DIVERSITY_TEST_VALUE = "diversity test";
    private static final String LEGACY_PO_COUNTRY = "legacy po country";
    private static final String FOREIGN_PO_COUNTRY = "foreign po country";
    private static final String ARUBA_ISO_CODE = "AW";
    private static final String ARUBA_FIPS_CODE = "AA";
    private static final String CONTACT_NAME = "Jane Doe";
    private static final String CONTACT_EMAIL_ADDRESS  = "tester@cornell.edu";
    private static final String CONTACT_PHONE_NUMBER = "111-222-3333";
    private static final String CONTACT_PHONE_NUMBER_EXTENSION = "987";
    private PaymentWorksVendorToKfsVendorDetailConversionServiceImpl conversionService;
    private PaymentWorksVendor pmwVendor;

    @BeforeEach
    void setUp() throws Exception {
        Configurator.setLevel(PaymentWorksVendorToKfsVendorDetailConversionServiceImpl.class.getName(), Level.DEBUG);
        conversionService = new PaymentWorksVendorToKfsVendorDetailConversionServiceImpl();
        conversionService.setDateTimeService(new TestDateTimeServiceImpl());
        conversionService.setPaymentWorksTaxRuleDependencyService(new PaymentWorksTaxRuleDependencyServiceImpl());
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
        pmwVendor.setPoCountry(FOREIGN_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(StringUtils.upperCase(PaymentWorksConstants.PO_ADDRESS_COUNTRY_OTHER));
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(FOREIGN_PO_COUNTRY, actual);
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
        countryMap.put(ARUBA_ISO_CODE, buildPaymentWorksIsoFipsCountryItems(ARUBA_FIPS_CODE));
        countryMap.put(KFSConstants.COUNTRY_CODE_UNITED_STATES, buildPaymentWorksIsoFipsCountryItems(KFSConstants.COUNTRY_CODE_UNITED_STATES));
        return countryMap;
    }
    
    private List<PaymentWorksIsoFipsCountryItem> buildPaymentWorksIsoFipsCountryItems(String fipsCode) {
        List<PaymentWorksIsoFipsCountryItem> itemList = new ArrayList<PaymentWorksIsoFipsCountryItem>();
        PaymentWorksIsoFipsCountryItem item = new PaymentWorksIsoFipsCountryItem();
        item.setFipsCountryCode(fipsCode);
        itemList.add(item);
        return itemList;
    }
    
    @Test
    void testBuildContactWithPhone() {
        VendorContact actualContact = conversionService.buildContact(PaymentWorksConstants.KFSVendorContactTypes.E_INVOICING, 
                PaymentWorksConstants.KFSVendorContactPhoneTypes.E_INVOICING, CONTACT_NAME, CONTACT_EMAIL_ADDRESS, 
                CONTACT_PHONE_NUMBER, CONTACT_PHONE_NUMBER_EXTENSION);
        assertBaseContactDetails(actualContact);
        assertEquals(1, CollectionUtils.size(actualContact.getVendorContactPhoneNumbers()));
        
        VendorContactPhoneNumber actualPhoneContact = actualContact.getVendorContactPhoneNumbers().get(0);
        assertEquals(PaymentWorksConstants.KFSVendorContactPhoneTypes.E_INVOICING, actualPhoneContact.getVendorPhoneTypeCode());
        assertEquals(CONTACT_PHONE_NUMBER, actualPhoneContact.getVendorPhoneNumber());
        assertEquals(CONTACT_PHONE_NUMBER_EXTENSION, actualPhoneContact.getVendorPhoneExtensionNumber());
    }
    
    @Test
    void testBuildContactWithPhoneNoExtension() {
        VendorContact actualContact = conversionService.buildContact(PaymentWorksConstants.KFSVendorContactTypes.E_INVOICING, 
                PaymentWorksConstants.KFSVendorContactPhoneTypes.E_INVOICING, CONTACT_NAME, CONTACT_EMAIL_ADDRESS, 
                CONTACT_PHONE_NUMBER, StringUtils.EMPTY);
        assertBaseContactDetails(actualContact);
        assertEquals(1, CollectionUtils.size(actualContact.getVendorContactPhoneNumbers()));
        
        VendorContactPhoneNumber actualPhoneContact = actualContact.getVendorContactPhoneNumbers().get(0);
        assertEquals(PaymentWorksConstants.KFSVendorContactPhoneTypes.E_INVOICING, actualPhoneContact.getVendorPhoneTypeCode());
        assertEquals(CONTACT_PHONE_NUMBER, actualPhoneContact.getVendorPhoneNumber());
        assertEquals(null, actualPhoneContact.getVendorPhoneExtensionNumber());
    }
    
    @Test
    void testBuildContactWithEmptyPhone() {
        VendorContact actualContact = conversionService.buildContact(PaymentWorksConstants.KFSVendorContactTypes.E_INVOICING, 
                PaymentWorksConstants.KFSVendorContactPhoneTypes.E_INVOICING, CONTACT_NAME, CONTACT_EMAIL_ADDRESS, 
                StringUtils.EMPTY, StringUtils.EMPTY);
        assertBaseContactDetails(actualContact);
        assertEquals(0, CollectionUtils.size(actualContact.getVendorContactPhoneNumbers()));
    }
    
    @Test
    void testBuildContactWithNullPhone() {
        VendorContact actualContact = conversionService.buildContact(PaymentWorksConstants.KFSVendorContactTypes.E_INVOICING, 
                PaymentWorksConstants.KFSVendorContactPhoneTypes.E_INVOICING, CONTACT_NAME, CONTACT_EMAIL_ADDRESS, 
                null, null);
        assertBaseContactDetails(actualContact);
        assertEquals(0, CollectionUtils.size(actualContact.getVendorContactPhoneNumbers()));
    }
    
    void assertBaseContactDetails(VendorContact actualContact) {
        assertEquals(PaymentWorksConstants.KFSVendorContactTypes.E_INVOICING, actualContact.getVendorContactTypeCode());
        assertEquals(CONTACT_NAME, actualContact.getVendorContactName());
        assertEquals(CONTACT_EMAIL_ADDRESS, actualContact.getVendorContactEmailAddress());
    }
    
    @Test
    void testbuildVendorDetailExtensionLegacyForm() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        VendorDetailExtension actualDetail = conversionService.buildVendorDetailExtension(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, actualDetail.getDefaultB2BPaymentMethodCode());
    }
    
    @Test
    void testbuildVendorDetailExtensionLegacyFormForeignWire() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.WIRE);
        VendorDetailExtension actualDetail = conversionService.buildVendorDetailExtension(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, actualDetail.getDefaultB2BPaymentMethodCode());
    }

    @Test
    void testbuildVendorDetailExtensionForeignFormForeignCheck() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(ARUBA_ISO_CODE);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.CHECK);
        
        VendorDetailExtension actualDetail = conversionService.buildVendorDetailExtension(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, actualDetail.getDefaultB2BPaymentMethodCode());
    }

    @Test
    void testbuildVendorDetailExtensionForeignFormForeignAch() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(ARUBA_ISO_CODE);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.ACH);
        VendorDetailExtension actualDetail = conversionService.buildVendorDetailExtension(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, actualDetail.getDefaultB2BPaymentMethodCode());
    }
    
    @Test
    void testbuildVendorDetailExtensionForeignFormWire() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(ARUBA_ISO_CODE);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.WIRE);
        VendorDetailExtension actualDetail = conversionService.buildVendorDetailExtension(pmwVendor,  buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE, actualDetail.getDefaultB2BPaymentMethodCode());
    }
    
    @Test
    void testbuildVendorDetailExtensionForeignFormEmpty() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(ARUBA_ISO_CODE);
        pmwVendor.setPaymentMethod(StringUtils.EMPTY);
        try {
            conversionService.buildVendorDetailExtension(pmwVendor,  buildPaymentWorksIsoToFipsCountryMap());
            fail("With no payment method provided, an Illegal Argument Exception should have been thrown.");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }
    
    @Test
    void testbuildVendorDetailExtensionForeignFormDomesticCheck() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.CHECK);
        
        VendorDetailExtension actualDetail = conversionService.buildVendorDetailExtension(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, actualDetail.getDefaultB2BPaymentMethodCode());
    }
    
    @Test
    void testbuildVendorDetailExtensionForeignFormDomesticAch() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.ACH);
        
        VendorDetailExtension actualDetail = conversionService.buildVendorDetailExtension(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, actualDetail.getDefaultB2BPaymentMethodCode());
    }
    
    @Test
    void testbuildVendorDetailExtensionForeignFormDomesticWire() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.WIRE);
        
        VendorDetailExtension actualDetail = conversionService.buildVendorDetailExtension(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, actualDetail.getDefaultB2BPaymentMethodCode());
    }
    
    @Test
    void testbuildVendorDetailExtensionForeignFormDomesticEmpty() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        pmwVendor.setPaymentMethod(StringUtils.EMPTY);
        
        VendorDetailExtension actualDetail = conversionService.buildVendorDetailExtension(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, actualDetail.getDefaultB2BPaymentMethodCode());
    }
    
    @Test
    void testIsDiverseLegacyDiverseYes() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setDiverseBusiness(true);
        assertTrue(conversionService.isDiverseBusiness(pmwVendor));
    }
    
    @Test
    void testIsDiverseLegacyDiverseNo() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(false));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setDiverseBusiness(false);
        assertFalse(conversionService.isDiverseBusiness(pmwVendor));
    }
    
    @Test
    void testIsDiverseForeignDiverseState() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setDiverseBusiness(false);
        pmwVendor.setStateDiversityClassifications(DIVERSITY_TEST_VALUE);
        assertTrue(conversionService.isDiverseBusiness(pmwVendor));
    }
    
    @Test
    void testIsDiverseForeignDiverseStateFed() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setDiverseBusiness(false);
        pmwVendor.setStateDiversityClassifications(DIVERSITY_TEST_VALUE);
        pmwVendor.setFederalDiversityClassifications(DIVERSITY_TEST_VALUE);
        assertTrue(conversionService.isDiverseBusiness(pmwVendor));
    }
    
    @Test
    void testIsDiverseForeignDiverseFed() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setDiverseBusiness(false);
        pmwVendor.setFederalDiversityClassifications(DIVERSITY_TEST_VALUE);
        assertTrue(conversionService.isDiverseBusiness(pmwVendor));
    }
    
    @Test
    void testIsDiverseForeignDiverseNone() {
        conversionService.setPaymentWorksFormModeService(buildMockPaymentWorksFormModeService(true));
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setDiverseBusiness(false);
        assertFalse(conversionService.isDiverseBusiness(pmwVendor));
    }
    
}
