package edu.cornell.kfs.kim.datadictionary.validation.fieldlevel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.core.api.util.CuCoreUtilities;

public class CuKimPhoneNumberValidationPatternTest {

    private static final String REGEX_PROPERTY_PREFIX = "validationPatternRegex.";
    private static final String CU_KIM_RESOURCES_PATH = "classpath:edu/cornell/kfs/kim/cu-kim-resources.properties";

    private TestKimPhoneNumberValidationPattern phonePattern;

    @Before
    public void setUp() throws Exception {
        Properties kimProperties = getCuKimProperties();
        phonePattern = new TestKimPhoneNumberValidationPattern(kimProperties);
    }

    private Properties getCuKimProperties() throws IOException {
        try (
            InputStream propertyFileStream = CuCoreUtilities.getResourceAsStream(CU_KIM_RESOURCES_PATH);
        ) {
            Properties properties = new Properties();
            properties.load(propertyFileStream);
            return properties;
        }
    }

    @After
    public void tearDown() throws Exception {
        if (phonePattern != null) {
            phonePattern.cleanUp();
        }
        phonePattern = null;
    }

    @Test
    public void testBlankNonNullValues() throws Exception {
        assertValuesDoNotMatch(KFSConstants.EMPTY_STRING, KFSConstants.BLANK_SPACE);
    }

    @Test
    public void testSingleValidValue() throws Exception {
        assertValuesMatch("123-456-7890");
    }

    @Test
    public void testSingleInvalidValue() throws Exception {
        assertValuesDoNotMatch("123-456-789");
    }

    @Test
    public void testMultipleValidValues() throws Exception {
        assertValuesMatch("000-000-0000", "135/246-7777", "(800) 555-9876", "(800)555-9876", "1116667070");
    }

    @Test
    public void testMultipleInvalidValues() throws Exception {
        assertValuesDoNotMatch("Two-One-Four", "(135)/246-7777", "(800)  555-9876", "(800)-555-9876",
                "11166670707", "654/4567", "(888 765-7899", "lkeu4tliey8w4l5o8yg");
    }

    @Test
    public void testMixOfValidAndInvalidValues() throws Exception {
        assertValuesDoNotMatch("(###) ###-####");
        assertValuesMatch("(765) 432-1000", "555-444-3333");
        assertValuesDoNotMatch("666-4321", "6664321", "700/800/9000");
        assertValuesMatch("700/800-9000");
    }

    private void assertValuesMatch(String... values) throws Exception {
        for (String value : values) {
            assertTrue("Phone validation should have succeeded for '" + value + "'", phonePattern.matches(value));
        }
    }

    private void assertValuesDoNotMatch(String... values) throws Exception {
        for (String value : values) {
            assertFalse("Phone validation should have failed for '" + value + "'", phonePattern.matches(value));
        }
    }

    private class TestKimPhoneNumberValidationPattern extends CuKimPhoneNumberValidationPattern {
        private static final long serialVersionUID = 1L;
        
        private Properties properties;
        
        private TestKimPhoneNumberValidationPattern(Properties properties) {
            this.properties = properties;
        }
        
        private void cleanUp() {
            properties = null;
        }
        
        @Override
        protected String getRegexString() {
            return properties.getProperty(REGEX_PROPERTY_PREFIX + getPatternTypeName());
        }
    }

}
