package edu.cornell.kfs.kns.datadictionary.validation.fieldlevel;

import org.junit.Test;
import org.kuali.kfs.kns.datadictionary.validation.fieldlevel.EmailAddressValidationPattern;
import org.powermock.api.mockito.PowerMockito;

import edu.cornell.kfs.gl.service.impl.fixture.EmailAdressTestValue;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;

@SuppressWarnings( "deprecation" )
public class CuEmailAddressValidationPatternTest {
    private EmailAddressValidationPattern pattern;
    
    private static final String PATTERN_CONSTRAINT = "validationPatternRegex.emailAddress";
    private static final String CU_APPLICATION_RESOURCES_PATH = "CU-ApplicationResources.properties";

    @Before
    public void setUp() throws Exception {
        pattern = PowerMockito.spy(new EmailAddressValidationPattern());
        String emailRegEx = getProperty(PATTERN_CONSTRAINT);
        System.err.println(emailRegEx);
        PowerMockito.doReturn(emailRegEx).when(pattern, "getRegexString");
    }
    
    private String getProperty(String key) {
        String value = null;
        Properties properties = new Properties();
        try {
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(CU_APPLICATION_RESOURCES_PATH);
            properties.load(in);
            value = properties.getProperty(key);
        } catch (IOException e) {
            
        }
        return value;
    }
    
    @After
    public void tearDown() {
        pattern = null;
    }
    
    @Test
    public void testValidateEmailAddressByRegularExpression() {
        for (EmailAdressTestValue testAddress : EmailAdressTestValue.values()) {
            boolean actualResults = pattern.matches(testAddress.emailAdress);
            assertEquals(testAddress.emailAdress + " should be " + testAddress.validAdress, testAddress.validAdress, actualResults);
        }
    }
   
}
