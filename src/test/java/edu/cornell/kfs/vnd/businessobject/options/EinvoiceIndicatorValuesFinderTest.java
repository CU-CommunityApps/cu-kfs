package edu.cornell.kfs.vnd.businessobject.options;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kuali.rice.core.api.util.KeyValue;

class EinvoiceIndicatorValuesFinderTest {
    
    private EinvoiceIndicatorValuesFinder valuesFinder;

    @BeforeEach
    void setUp() {
        valuesFinder = new EinvoiceIndicatorValuesFinder();
    }

    @AfterEach
    void tearDown() {
        valuesFinder = null;
    }

    @Test
    void testGetKeyValues() {
        List<KeyValue> results = valuesFinder.getKeyValues();
        assertEquals(3, results.size());
        assertKeyValueValues(results.get(0), EinvoiceIndicatorValuesFinder.EinvoiceIndicator.NONE);
        assertKeyValueValues(results.get(1), EinvoiceIndicatorValuesFinder.EinvoiceIndicator.SFTP);
        assertKeyValueValues(results.get(2), EinvoiceIndicatorValuesFinder.EinvoiceIndicator.WEB);
    }
    
    private void assertKeyValueValues(KeyValue keyValue, EinvoiceIndicatorValuesFinder.EinvoiceIndicator indicator) {
        assertEquals(indicator.code, keyValue.getKey());
        assertEquals(indicator.description, keyValue.getValue());
    }
    
    @Test
    void testGetEinvoiceIndicatorFromCodeSFTP() {
        EinvoiceIndicatorValuesFinder.EinvoiceIndicator results = EinvoiceIndicatorValuesFinder.EinvoiceIndicator.getEinvoiceIndicatorFromCode("S");
        assertEquals(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.SFTP.description, results.description);
        assertEquals(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.SFTP.code, results.code);
    }
    
    @Test
    void testGetEinvoiceIndicatorFromCodeEmptyString() {
        EinvoiceIndicatorValuesFinder.EinvoiceIndicator results = EinvoiceIndicatorValuesFinder.EinvoiceIndicator.getEinvoiceIndicatorFromCode(StringUtils.EMPTY);
        assertEquals(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.NONE.description, results.description);
        assertEquals(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.NONE.code, results.code);
    }
    
    @Test
    void testGetEinvoiceIndicatorFromCodeEmptyNull() {
        EinvoiceIndicatorValuesFinder.EinvoiceIndicator results = EinvoiceIndicatorValuesFinder.EinvoiceIndicator.getEinvoiceIndicatorFromCode(null);
        assertEquals(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.NONE.description, results.description);
        assertEquals(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.NONE.code, results.code);
    }
    
    @Test
    void testGetEinvoiceIndicatorFromCodeEmptyInvalidValue() {
        EinvoiceIndicatorValuesFinder.EinvoiceIndicator results = EinvoiceIndicatorValuesFinder.EinvoiceIndicator.getEinvoiceIndicatorFromCode("foo");
        assertEquals(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.NONE.description, results.description);
        assertEquals(EinvoiceIndicatorValuesFinder.EinvoiceIndicator.NONE.code, results.code);
    }

}
