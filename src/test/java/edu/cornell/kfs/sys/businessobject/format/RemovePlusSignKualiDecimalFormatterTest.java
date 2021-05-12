package edu.cornell.kfs.sys.businessobject.format;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

public class RemovePlusSignKualiDecimalFormatterTest {
    
    private RemovePlusSignKualiDecimalFormatter kualiDecimalFormatter;
    private static final String ASSERT_SHOULD_EQUAL_MESSAGE = "The expected and actual should be the same";

    @Before
    public void setUp() throws Exception {
        kualiDecimalFormatter = new RemovePlusSignKualiDecimalFormatter();
    }

    @After
    public void tearDown() throws Exception {
        kualiDecimalFormatter = null;
    }

    @Test
    public void convertNormalNumberString() {
        KualiDecimal expected = new KualiDecimal(100.55);
        KualiDecimal actual = (KualiDecimal) kualiDecimalFormatter.convertToObject("100.55");
        assertEquals(ASSERT_SHOULD_EQUAL_MESSAGE, expected, actual);
    }
    
    @Test
    public void convertNormalNumberStringWithPlus() {
        KualiDecimal expected = new KualiDecimal(100.55);
        KualiDecimal actual = (KualiDecimal) kualiDecimalFormatter.convertToObject("+100.55");
        assertEquals(ASSERT_SHOULD_EQUAL_MESSAGE, expected, actual);
    }
    
    @Test
    public void convertNormalNumberStringWithMinus() {
        KualiDecimal expected = new KualiDecimal(-100.55);
        KualiDecimal actual = (KualiDecimal) kualiDecimalFormatter.convertToObject("-100.55");
        assertEquals(ASSERT_SHOULD_EQUAL_MESSAGE, expected, actual);
    }
    
    @Test
    public void convertEmptyString() {
        KualiDecimal expected = null;
        KualiDecimal actual = (KualiDecimal) kualiDecimalFormatter.convertToObject("");
        assertEquals(ASSERT_SHOULD_EQUAL_MESSAGE, expected, actual);
    }
    
    @Test
    public void convertNonNumeric() {
        KualiDecimal expected = null;
        KualiDecimal actual = (KualiDecimal) kualiDecimalFormatter.convertToObject("xyz");
        assertEquals(ASSERT_SHOULD_EQUAL_MESSAGE, expected, actual);
    }

}
