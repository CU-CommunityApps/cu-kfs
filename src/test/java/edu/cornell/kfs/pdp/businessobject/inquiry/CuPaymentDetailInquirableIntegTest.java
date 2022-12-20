package edu.cornell.kfs.pdp.businessobject.inquiry;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pdp.CUPdpPropertyConstants;

/*
 * TODO: This has been marked as an integration test because the relied-upon code
 * in PaymentDetailInquirable uses a SpringContext-derived LocationService instance.
 * If/When KualiCo updates the code to allow for injecting a LocationService object,
 * we should rename this class to have it run with the non-integration-test suite.
 */
@SuppressWarnings("deprecation")
public class CuPaymentDetailInquirableIntegTest {

    private static final String COUNTRY_NAME_UNITED_STATES = "United States";
    private static final String COUNTRY_NAME_EGYPT = "Egypt";
    private static final String INVALID_COUNTRY_CODE = "QQ";

    private static final String TEST_PAYEE_ID = "1234-0";
    private static final String TEST_PAYEE_NAME = "Test Vendor";

    private CuPaymentDetailInquirable paymentDetailInquirable;

    @Before
    public void setUp() throws Exception {
        paymentDetailInquirable = new CuPaymentDetailInquirable();
    }

    @Test
    public void testConvertUnitedStatesCountryCode() throws Exception {
        assertSectionWithCountryValueIsConvertedProperly(KFSConstants.COUNTRY_CODE_UNITED_STATES);
    }

    @Test
    public void testUnitedStatesCountryNameIsNotConverted() throws Exception {
        assertSectionWithCountryValueIsConvertedProperly(COUNTRY_NAME_UNITED_STATES);
    }

    @Test
    public void testEgyptCountryNameIsNotConverted() throws Exception {
        assertSectionWithCountryValueIsConvertedProperly(COUNTRY_NAME_EGYPT);
    }

    @Test
    public void testInvalidCountryCodeIsNotConverted() throws Exception {
        assertSectionWithCountryValueIsConvertedProperly(INVALID_COUNTRY_CODE);
    }

    @Test
    public void testNullCountryCodeIsNotConverted() throws Exception {
        assertSectionWithCountryValueIsConvertedProperly(null);
    }

    @Test
    public void testEmptyCountryCodeIsNotConverted() throws Exception {
        assertSectionWithCountryValueIsConvertedProperly(StringUtils.EMPTY);
    }

    @Test
    public void testBlankCountryCodeIsNotConverted() throws Exception {
        assertSectionWithCountryValueIsConvertedProperly(KFSConstants.BLANK_SPACE);
    }

    @Test
    public void testSectionWithoutCountryPropertyIsNotConverted() throws Exception {
        assertSectionStaysUnalteredWhenCountryPropertyIsNotPresent();
    }

    private void assertSectionWithCountryValueIsConvertedProperly(String countryPropertyValue) {
        String expectedConvertedCountryValue = getExpectedPostConversionCountryValue(countryPropertyValue);
        Map<String, String> expectedPropertyValues = new HashMap<>();
        expectedPropertyValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_ID, TEST_PAYEE_ID);
        expectedPropertyValues.put(CUPdpPropertyConstants.PAYMENT_COUNTRY, expectedConvertedCountryValue);
        expectedPropertyValues.put(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_NAME, TEST_PAYEE_NAME);
        
        Section section = buildSectionWithCountryPropertyValue(countryPropertyValue);
        assertSectionIsUpdatedProperly(expectedPropertyValues, section);
    }

    private String getExpectedPostConversionCountryValue(String countryPropertyValue) {
        if (StringUtils.isBlank(countryPropertyValue)) {
            return StringUtils.EMPTY;
        } else if (StringUtils.equals(countryPropertyValue, KFSConstants.COUNTRY_CODE_UNITED_STATES)) {
            return COUNTRY_NAME_UNITED_STATES;
        } else {
            return countryPropertyValue;
        }
    }

    private void assertSectionStaysUnalteredWhenCountryPropertyIsNotPresent() {
        Map<String, String> expectedPropertyValues = Collections.singletonMap(
                PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_ID, TEST_PAYEE_ID);
        Section section = buildSection("Payment",
                buildRow(
                        buildField(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_ID, TEST_PAYEE_ID)));
        
        assertSectionIsUpdatedProperly(expectedPropertyValues, section);
    }

    private void assertSectionIsUpdatedProperly(Map<String, String> expectedPropertyValues, Section section) {
        paymentDetailInquirable.convertCountryForDisplay(section);
        
        Map<String, String> actualPropertyValues = section.getRows().stream()
                .flatMap((row) -> row.getFields().stream())
                .collect(Collectors.toMap(Field::getPropertyName, Field::getPropertyValue));
        
        assertEquals("Wrong property value mappings from inquiry section", expectedPropertyValues, actualPropertyValues);
    }

    private Section buildSectionWithCountryPropertyValue(String countryValue) {
        return buildSection("Payee",
                buildRow(
                        buildField(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_ID, TEST_PAYEE_ID),
                        buildField(CUPdpPropertyConstants.PAYMENT_COUNTRY, countryValue)),
                buildRow(
                        buildField(PdpPropertyConstants.PaymentDetail.PAYMENT_PAYEE_NAME, TEST_PAYEE_NAME)));
    }

    private Section buildSection(String title, Row... rows) {
        Section section = new Section();
        section.setSectionTitle(title);
        section.setRows(Arrays.asList(rows));
        return section;
    }

    private Row buildRow(Field... fields) {
        Row row = new Row();
        row.setFields(Arrays.asList(fields));
        return row;
    }

    private Field buildField(String propertyName, String propertyValue) {
        Field field = new Field();
        field.setPropertyName(propertyName);
        field.setPropertyValue(propertyValue);
        return field;
    }

}
