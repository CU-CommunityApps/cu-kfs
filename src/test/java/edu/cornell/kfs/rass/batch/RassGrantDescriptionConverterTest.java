package edu.cornell.kfs.rass.batch;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.mockito.Mockito;

public class RassGrantDescriptionConverterTest {
    private RassGrantDescriptionConverter rassGrantDescriptionConverter;

    @Before
    public void setUp() throws Exception {
        Configurator.setLevel(RassGrantDescriptionConverter.class.getName(), Level.DEBUG);
        rassGrantDescriptionConverter = new RassGrantDescriptionConverter();
        rassGrantDescriptionConverter.setParameterService(buildMockParameterService());
    }

    @After
    public void tearDown() throws Exception {
        rassGrantDescriptionConverter = null;
    }
    
    private ParameterService buildMockParameterService() {
        ParameterService service = Mockito.mock(ParameterService.class);
        //public String getSubParameterValueAsString(String namespaceCode, String componentCode, String parameterName, String subParameterName) {
        
        for (TestData data : TestData.values()) {
            Mockito.when(service.getSubParameterValueAsString(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.eq(data.searchValue))).
                thenReturn(data.parameterServiceResults);
        }
        return service;
    }

    @Test
    public void testMapping() {
        for (TestData data : TestData.values()) {
            String actualResults = (String) rassGrantDescriptionConverter.convert(null, null, data.searchValue);
            assertEquals("Did not get the expected results for look up " + data.searchValue, data.expectedResults, actualResults);
        }
    }
    
    private enum TestData {
       GOOD_TEST("A", "ALPHA"),
       NO_RESULTS("B", "B", null),
       EMPTY_RESULTS("C", "C", StringUtils.EMPTY),
       SPACE_RESULTS("D", "D", StringUtils.SPACE);
        
        
        public final String searchValue;
        public final String expectedResults;
        public final String parameterServiceResults;
        
        TestData(String searchValue, String expectedResults) {
            this(searchValue, expectedResults, expectedResults);
        }
        
        TestData(String searchValue, String expectedResults, String parameterServiceResults) {
            this.searchValue = searchValue;
            this.expectedResults = expectedResults;
            this.parameterServiceResults = parameterServiceResults;
        }
    }

}

