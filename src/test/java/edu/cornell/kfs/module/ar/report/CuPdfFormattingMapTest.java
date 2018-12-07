package edu.cornell.kfs.module.ar.report;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.module.ar.service.ContractsGrantsBillingUtilityService;
import org.kuali.kfs.module.ar.service.impl.ContractsGrantsBillingUtilityServiceImpl;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class CuPdfFormattingMapTest {

    private ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService;

    @Before
    public void setUp() throws Exception {
        this.contractsGrantsBillingUtilityService = new ContractsGrantsBillingUtilityServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        this.contractsGrantsBillingUtilityService = null;
    }

    @Test
    public void testFormattingForSingleNumber() throws Exception {
        assertItemsAreFormattedProperlyByPdfMap(
                TestMapItem.forKualiNumber("Item1", 100.00, "$100.00"));
    }

    @Test
    public void testFormattingForSingleString() throws Exception {
        assertItemsAreFormattedProperlyByPdfMap(
                TestMapItem.forString("Item02", "300.00", "300.00"));
    }

    @Test
    public void testFormattingForSingleNonKualiNumber() throws Exception {
        assertItemsAreFormattedProperlyByPdfMap(
                TestMapItem.forNonKualiNumber("Item_3", 100.00, "100.0"));
    }

    @Test
    public void testFormattingForMultipleNumbers() throws Exception {
        assertItemsAreFormattedProperlyByPdfMap(
                TestMapItem.forKualiNumber("FirstItem", 100.00, "$100.00"),
                TestMapItem.forKualiNumber("SecondItem", 555.6, "$555.60"),
                TestMapItem.forKualiNumber("ThirdItem", 1200.34, "$1,200.34"),
                TestMapItem.forKualiNumber("FourthItem", 0, "$0.00"),
                TestMapItem.forKualiNumber("FifthItem", 12333456.99, "$12,333,456.99"),
                TestMapItem.forKualiNumber("SixthItem", 13300.444, "$13,300.44"));
    }

    @Test
    public void testFormattingForMultipleMixedValueTypes() throws Exception {
        assertItemsAreFormattedProperlyByPdfMap(
                TestMapItem.forNonKualiNumber("Item1", 0.1, "0.1"),
                TestMapItem.forKualiNumber("Item2", 555.6, "$555.60"),
                TestMapItem.forString("Item3", "30000", "30000"),
                TestMapItem.forNonKualiNumber("Item4", 55000, "55000.0"),
                TestMapItem.forString("Item5", "Ten", "Ten"),
                TestMapItem.forKualiNumber("Item6", 13300.444, "$13,300.44"));
    }

    private void assertItemsAreFormattedProperlyByPdfMap(TestMapItem... items) throws Exception {
        Map<String, Object> itemsMap = Stream.of(items)
                .map(TestMapItem::getKeyValuePair)
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        
        CuPdfFormattingMap pdfMap = new TestCuPdfFormattingMap(itemsMap, contractsGrantsBillingUtilityService);
        
        Stream.of(items)
                .forEach((item) -> assertItemIsFormattedProperlyByPdfMap(item, pdfMap));
    }

    private void assertItemIsFormattedProperlyByPdfMap(TestMapItem item, CuPdfFormattingMap pdfMap) {
        String itemKey = item.getKey();
        String expectedValue = item.getExpectedFormattedValue();
        String actualValue = pdfMap.get(itemKey);
        assertEquals("Item '" + itemKey + "' has the wrong PDF-formatted value", expectedValue, actualValue);
    }

    private static class TestCuPdfFormattingMap extends CuPdfFormattingMap {
        private ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService;
        
        public TestCuPdfFormattingMap(Map<?, ?> mapToWrap, ContractsGrantsBillingUtilityService contractsGrantsBillingUtilityService) {
            super(mapToWrap);
            this.contractsGrantsBillingUtilityService = contractsGrantsBillingUtilityService;
        }
        
        @Override
        protected ContractsGrantsBillingUtilityService getContractsGrantsBillingUtilityService() {
            return contractsGrantsBillingUtilityService;
        }
    }

    private static class TestMapItem {
        private final Pair<String, Object> keyValuePair;
        private final String expectedFormattedValue;
        
        public TestMapItem(String key, Object value, String expectedFormattedValue) {
            this.keyValuePair = Pair.of(key, value);
            this.expectedFormattedValue = expectedFormattedValue;
        }
        
        public String getKey() {
            return keyValuePair.getKey();
        }
        
        public Pair<String, Object> getKeyValuePair() {
            return keyValuePair;
        }
        
        public String getExpectedFormattedValue() {
            return expectedFormattedValue;
        }
        
        public static TestMapItem forString(String key, String value, String expectedFormattedValue) {
            return new TestMapItem(key, value, expectedFormattedValue);
        }
        
        public static TestMapItem forKualiNumber(String key, double value, String expectedFormattedValue) {
            return new TestMapItem(key, new KualiDecimal(value), expectedFormattedValue);
        }
        
        public static TestMapItem forNonKualiNumber(String key, double value, String expectedFormattedValue) {
            return new TestMapItem(key, Double.valueOf(value), expectedFormattedValue);
        }
    }

}
