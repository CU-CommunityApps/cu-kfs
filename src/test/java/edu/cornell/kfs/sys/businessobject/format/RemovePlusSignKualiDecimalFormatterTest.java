package edu.cornell.kfs.sys.businessobject.format;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class RemovePlusSignKualiDecimalFormatterTest {
    
    private RemovePlusSignKualiDecimalFormatter removePlusSignKualiDecimalFormatter;
    private static final String STANDARD_ASSERT_MESSAGE = "The expected and actual should be the same";

    @Before
    public void setUp() throws Exception {
        removePlusSignKualiDecimalFormatter = new RemovePlusSignKualiDecimalFormatter();
    }

    @After
    public void tearDown() throws Exception {
        removePlusSignKualiDecimalFormatter = null;
    }

    @Test
    public void concertNormalNumberString() {
        KualiDecimal expected = new KualiDecimal(100.55);
        KualiDecimal actual = (KualiDecimal) removePlusSignKualiDecimalFormatter.convertToObject("100.55");
        assertEquals(STANDARD_ASSERT_MESSAGE, expected, actual);
    }
    
    @Test
    public void concertNormalNumberStringWithPlus() {
        KualiDecimal expected = new KualiDecimal(100.55);
        KualiDecimal actual = (KualiDecimal) removePlusSignKualiDecimalFormatter.convertToObject("+100.55");
        assertEquals(STANDARD_ASSERT_MESSAGE, expected, actual);
    }
    
    @Test
    public void concertNormalNumberStringWithMinus() {
        KualiDecimal expected = new KualiDecimal(-100.55);
        KualiDecimal actual = (KualiDecimal) removePlusSignKualiDecimalFormatter.convertToObject("-100.55");
        assertEquals(STANDARD_ASSERT_MESSAGE, expected, actual);
    }
    
    @Test
    public void concertEmptyString() {
        KualiDecimal expected = null;
        KualiDecimal actual = (KualiDecimal) removePlusSignKualiDecimalFormatter.convertToObject("");
        assertEquals(STANDARD_ASSERT_MESSAGE, expected, actual);
    }
    
    @Test
    public void concertNonNumberic() {
        KualiDecimal expected = null;
        KualiDecimal actual = (KualiDecimal) removePlusSignKualiDecimalFormatter.convertToObject("xyz");
        assertEquals(STANDARD_ASSERT_MESSAGE, expected, actual);
    }

}
