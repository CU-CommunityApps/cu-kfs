package edu.cornell.kfs.kns.datadictionary.validation.fieldlevel;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.kns.datadictionary.validation.fieldlevel.EmailAddressValidationPattern;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.gl.service.impl.fixture.EmailAddressTestValue;
import edu.cornell.kfs.sys.service.impl.TestConfigurationServiceImpl;

@SuppressWarnings("deprecation")
public class CuEmailAddressValidationPatternTest {
    private EmailAddressValidationPattern pattern;
    private TestConfigurationServiceImpl configurationService;

    private static final String APPLICATION_RESOURCES_FILE = "classpath:CU-ApplicationResources.properties";

    @Before
    public void setUp() throws Exception {
        configurationService = new TestConfigurationServiceImpl();
        configurationService.setProperties(getProperties());
    }

    private Properties getProperties() throws IOException {
        try (
            InputStream propertyFileStream = CuCoreUtilities.getResourceAsStream(APPLICATION_RESOURCES_FILE);
        ) {
            Properties properties = new Properties();
            properties.load(propertyFileStream);
            return properties;
        }
    }    

    @After
    public void tearDown() {
        pattern = null;
        configurationService = null;
    }

    @Test
    public void testValidateEmailAddressByRegularExpression() {
         try (MockedStatic<KRADServiceLocator> serviceLocatorMockedStatic = Mockito.mockStatic(KRADServiceLocator.class)) {
            serviceLocatorMockedStatic.when(KRADServiceLocator::getKualiConfigurationService).thenReturn(configurationService);
            pattern = new EmailAddressValidationPattern();
            for (EmailAddressTestValue testAddress : EmailAddressTestValue.values()) {
                boolean actualResults = pattern.matches(testAddress.emailAddress);
                assertEquals(testAddress.emailAddress + " should be " + testAddress.validAddress, testAddress.validAddress, actualResults);   
            }
        }
    }

    @Test
    public void testValidateEmailAddressByInternetAddress() {
        for (EmailAddressTestValue testAddress : EmailAddressTestValue.values()) {
            boolean actualResults = validateEmailByInternetAddress(testAddress.emailAddress);
            assertEquals(testAddress.emailAddress + " should be " + testAddress.validAddress, testAddress.validAddress,
                    actualResults);
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
