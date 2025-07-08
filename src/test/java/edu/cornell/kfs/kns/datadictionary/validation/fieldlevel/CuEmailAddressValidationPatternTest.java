package edu.cornell.kfs.kns.datadictionary.validation.fieldlevel;

import org.junit.Test;
import org.kuali.kfs.kns.datadictionary.validation.fieldlevel.EmailAddressValidationPattern;

import edu.cornell.kfs.gl.service.impl.fixture.EmailAddressTestValue;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.After;
import org.junit.Before;

@SuppressWarnings("deprecation")
public class CuEmailAddressValidationPatternTest {
    private EmailAddressValidationPattern pattern;
    
    private static final String PATTERN_CONSTRAINT = "validationPatternRegex.emailAddress";
    private static final String CU_APPLICATION_RESOURCES_PATH = "CU-ApplicationResources.properties";

    @Before
    public void setUp() throws Exception {
        final String emailRegEx = getProperty(PATTERN_CONSTRAINT);
        
        // Create a custom implementation that uses our regex pattern
        pattern = new EmailAddressValidationPattern() {
            @Override
            public boolean matches(String value) {
                if (value == null) {
                    return false;
                }
                return Pattern.compile(emailRegEx).matcher(value).matches();
            }
        };
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
        for (EmailAddressTestValue testAddress : EmailAddressTestValue.values()) {
            boolean actualResults = pattern.matches(testAddress.emailAddress);
            assertEquals(testAddress.emailAddress + " should be " + testAddress.validAddress, testAddress.validAddress, actualResults);
        }
    }
    
    @Test
    public void testValidateEmailAddressByInternetAddress() {
        for (EmailAddressTestValue testAddress : EmailAddressTestValue.values()) {
            boolean actualResults = validateEmailByInternetAddress(testAddress.emailAddress);
            assertEquals(testAddress.emailAddress + " should be " + testAddress.validAddress, testAddress.validAddress, actualResults);
        }
    }
    
    private boolean validateEmailByInternetAddress(String email) {
        try {
            InternetAddress ia = new InternetAddress(email);
            ia.validate();
            return true;
        } catch (AddressException e) {
            return false;
        }
        
    }
   
}
