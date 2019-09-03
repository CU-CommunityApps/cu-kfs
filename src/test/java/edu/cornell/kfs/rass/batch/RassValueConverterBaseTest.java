package edu.cornell.kfs.rass.batch;

import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.mockito.Mockito;

public class RassValueConverterBaseTest {
    private RassValueConverterBase rassValueConverterBase;
    private static final String TEST_STRING = "Testing String";

    @Before
    public void setUp() throws Exception {
        Configurator.setLevel(RassValueConverterBase.class.getName(), Level.DEBUG);
        rassValueConverterBase = new RassValueConverterBase();
    }

    @After
    public void tearDown() throws Exception {
        rassValueConverterBase = null;
        
    }
    
    @Test
    public void testTruncateToEllipsis() {
        rassValueConverterBase.setDataDictionaryService(buildMockDataDictionaryService(5));
        String expectedResult = "Te...";
        
        String result = rassValueConverterBase.cleanStringValue(Agency.class, buildRassPropertyDefinition(true), TEST_STRING);
        assertEquals(expectedResult, result);
        
    }
    
    @Test
    public void testTruncateToEllipsisEdgeCase() {
        rassValueConverterBase.setDataDictionaryService(buildMockDataDictionaryService(TEST_STRING.length() - 1));
        String expectedResult = "Testing St...";
        
        String result = rassValueConverterBase.cleanStringValue(Agency.class, buildRassPropertyDefinition(true), TEST_STRING);
        assertEquals(expectedResult, result);
        
    }
    
    @Test
    public void testTruncateWithOutEllipsis() {
        rassValueConverterBase.setDataDictionaryService(buildMockDataDictionaryService(5));
        String expectedResult = "Testi";
        
        String result = rassValueConverterBase.cleanStringValue(Agency.class, buildRassPropertyDefinition(false), TEST_STRING);
        assertEquals(expectedResult, result);
        
    }
    
    @Test
    public void testTruncateWithOutEllipsisNullPropertyBoolean() {
        rassValueConverterBase.setDataDictionaryService(buildMockDataDictionaryService(5));
        String expectedResult = "Testi";
        
        String result = rassValueConverterBase.cleanStringValue(Agency.class, buildRassPropertyDefinition(null), TEST_STRING);
        assertEquals(expectedResult, result);
        
    }
    
    @Test
    public void testNoTruncate() {
        rassValueConverterBase.setDataDictionaryService(buildMockDataDictionaryService(TEST_STRING.length()));
        
        String result = rassValueConverterBase.cleanStringValue(Agency.class, buildRassPropertyDefinition(true), TEST_STRING);
        assertEquals(TEST_STRING, result);
        
    }
    
    private DataDictionaryService buildMockDataDictionaryService(Integer maxSize) {
        DataDictionaryService service = Mockito.mock(DataDictionaryService.class);
        Mockito.when(service.getAttributeMaxLength(Mockito.eq(Agency.class), Mockito.anyString())).thenReturn(maxSize);
        return service;
    }
    
    private RassPropertyDefinition buildRassPropertyDefinition(Boolean truncateWithEllipsis) {
        RassPropertyDefinition prop = new RassPropertyDefinition();
        prop.setBoPropertyName("FOO");
        prop.setRequired(true);
        prop.setXmlPropertyName("BAR");
        if (truncateWithEllipsis != null) {
            prop.setTruncateWithEllipsis(truncateWithEllipsis);
        }
        return prop;
    }

}
