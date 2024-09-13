package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.cornell.kfs.vnd.businessobject.options.EinvoiceIndicatorValuesFinder;
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

import edu.cornell.kfs.pmw.batch.PaymentWorksConstants;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksIsoFipsCountryItem;
import edu.cornell.kfs.pmw.batch.businessobject.PaymentWorksVendor;
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
    
    @Test
    void testFindPoCountryToUseUS() {
        pmwVendor.setPoCountryLegacy(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(KFSConstants.COUNTRY_CODE_UNITED_STATES, actual);
    }
    
    @Test
    void testFindPoCountryToUseAustralia() {
        pmwVendor.setPoCountryLegacy(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.FIPS_COUNTRY_CODE_AUSTRALIA);
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(PaymentWorksConstants.FIPS_COUNTRY_CODE_AUSTRALIA, actual);
    }
    
    @Test
    void testFindPoCountryToUseOther() {
        pmwVendor.setPoCountryLegacy(LEGACY_PO_COUNTRY);
        pmwVendor.setPoCountry(FOREIGN_PO_COUNTRY);
        pmwVendor.setPoCountryUsCanadaAustraliaOther(StringUtils.upperCase(PaymentWorksConstants.PO_ADDRESS_COUNTRY_OTHER, Locale.US));
        String actual = conversionService.findPoCountryToUse(pmwVendor);
        assertEquals(FOREIGN_PO_COUNTRY, actual);
    }
    
    @Test
    void testShouldCreateContactGoodContact() {
        String contactName = "foo";
        assertTrue(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactNullContact() {
        String contactName = null;
        assertFalse(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testShouldCreateContactEmptyContact() {
        String contactName = StringUtils.EMPTY;
        assertFalse(conversionService.shouldCreateContact(contactName));
    }
    
    @Test
    void testBuildPOFipsCountryCodeUS() {
        String expectedPoCountry = PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.fipsCountryCode;
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.UNITED_STATES.pmwCountryOptionAsString);
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    @Test
    void testBuildPOFipsCountryCodeCanada() {
        String expectedPoCountry = PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.fipsCountryCode;
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.CANADA.pmwCountryOptionAsString);
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    @Test
    void testBuildPOFipsCountryCodeAruba() {
        String expectedPoCountry = ARUBA_FIPS_CODE;
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.OTHER.pmwCountryOptionAsString);
        pmwVendor.setPoCountry(ARUBA_ISO_CODE);
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    @Test
    void testBuildPOFipsCountryCodeEmptyCountry() {
        String expectedPoCountry = StringUtils.EMPTY;
        pmwVendor.setPoCountryUsCanadaAustraliaOther(PaymentWorksConstants.PaymentWorksPurchaseOrderCountryFipsOption.OTHER.pmwCountryOptionAsString);
        pmwVendor.setPoCountry(StringUtils.EMPTY);
        String actualPoCountry = conversionService.buildPOFipsCountryCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(expectedPoCountry, actualPoCountry);
    }
    
    @Test
    void testBuildPOFipsCountryCodeBadCountry() {
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
    void testbuildVendorDetailExtensionCheck() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(ARUBA_ISO_CODE);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.CHECK);
        
        String generatedDefaultPaymentMethodCode = conversionService.buildDefaultKFSPaymentMethodCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, generatedDefaultPaymentMethodCode);
    }

    @Test
    void testbuildVendorDetailExtensionAch() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(ARUBA_ISO_CODE);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.ACH);
        
        String generatedDefaultPaymentMethodCode = conversionService.buildDefaultKFSPaymentMethodCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, generatedDefaultPaymentMethodCode);
    }
    
    @Test
    void testbuildVendorDetailExtensionWire() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(ARUBA_ISO_CODE);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.WIRE);
        
        String generatedDefaultPaymentMethodCode = conversionService.buildDefaultKFSPaymentMethodCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_WIRE, generatedDefaultPaymentMethodCode);
    }
    
    @Test
    void testbuildVendorDetailExtensionEmpty() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(ARUBA_ISO_CODE);
        pmwVendor.setPaymentMethod(StringUtils.EMPTY);
        try {
            conversionService.buildDefaultKFSPaymentMethodCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
            fail("With no payment method provided, an Illegal Argument Exception should have been thrown.");
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    @Test
    void testbuildVendorDetailExtensionDefaultEinvoiceIndicator() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        VendorDetailExtension actualDetail = conversionService.buildVendorDetailExtension(pmwVendor);
        assertEquals(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.NONE.code, actualDetail.getEinvoiceVendorIndicator());
    }
    
    @Test
    void testbuildVendorDetailExtensionDomesticCheck() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.CHECK);
        
        String generatedDefaultPaymentMethodCode = conversionService.buildDefaultKFSPaymentMethodCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, generatedDefaultPaymentMethodCode);
    }
    
    @Test
    void testbuildVendorDetailExtensionDomesticAch() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.ACH);
        
        String generatedDefaultPaymentMethodCode = conversionService.buildDefaultKFSPaymentMethodCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, generatedDefaultPaymentMethodCode);
    }
    
    @Test
    void testbuildVendorDetailExtensionDomesticWire() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        pmwVendor.setPaymentMethod(PaymentWorksConstants.PaymentWorksPaymentMethods.WIRE);
        
        String generatedDefaultPaymentMethodCode = conversionService.buildDefaultKFSPaymentMethodCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, generatedDefaultPaymentMethodCode);
    }
    
    @Test
    void testbuildVendorDetailExtensionDomesticEmpty() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setRequestingCompanyTaxCountry(KFSConstants.COUNTRY_CODE_UNITED_STATES);
        pmwVendor.setPaymentMethod(StringUtils.EMPTY);
        
        String generatedDefaultPaymentMethodCode = conversionService.buildDefaultKFSPaymentMethodCode(pmwVendor, buildPaymentWorksIsoToFipsCountryMap());
        assertEquals(KFSConstants.PaymentSourceConstants.PAYMENT_METHOD_CHECK, generatedDefaultPaymentMethodCode);
    }
    
    @Test
    void testIsDiverseState() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setDiverseBusiness(false);
        pmwVendor.setStateDiversityClassifications(DIVERSITY_TEST_VALUE);
        assertTrue(conversionService.isDiverseBusiness(pmwVendor));
    }
    
    @Test
    void testIsDiverseStateFed() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setDiverseBusiness(false);
        pmwVendor.setStateDiversityClassifications(DIVERSITY_TEST_VALUE);
        pmwVendor.setFederalDiversityClassifications(DIVERSITY_TEST_VALUE);
        assertTrue(conversionService.isDiverseBusiness(pmwVendor));
    }
    
    @Test
    void testIsDiverseFed() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setDiverseBusiness(false);
        pmwVendor.setFederalDiversityClassifications(DIVERSITY_TEST_VALUE);
        assertTrue(conversionService.isDiverseBusiness(pmwVendor));
    }
    
    @Test
    void testIsDiverseNone() {
        PaymentWorksVendor pmwVendor = new PaymentWorksVendor();
        pmwVendor.setDiverseBusiness(false);
        assertFalse(conversionService.isDiverseBusiness(pmwVendor));
    }
    
}
