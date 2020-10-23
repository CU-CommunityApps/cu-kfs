package edu.cornell.kfs.tax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class FormTypes1099Test {

    private static final String SHOULD_HAVE_AN_ILLEGAL_ARGUMENT_EXCEPTION = "Should have an illegal argument exception";

    @Test
    void findFormTypes1099FromFormCodeMisc() {
        FormTypes1099 expectedType = FormTypes1099.MISC;
        FormTypes1099 actualType = FormTypes1099.findFormTypes1099FromFormCode(FormTypes1099.MISC.formCode);
        assertEquals(expectedType.formDescription, actualType.formDescription);
        assertEquals(expectedType.formCode, actualType.formCode);
    }

    @Test
    void findFormTypes1099FromFormCodeNec() {
        FormTypes1099 expectedType = FormTypes1099.NEC;
        FormTypes1099 actualType = FormTypes1099.findFormTypes1099FromFormCode(FormTypes1099.NEC.formCode);
        assertEquals(expectedType.formDescription, actualType.formDescription);
        assertEquals(expectedType.formCode, actualType.formCode);
    }

    @Test
    void findFormTypes1099FromFormCodeNull() {
        try {
            FormTypes1099.findFormTypes1099FromFormCode(null);
            fail(SHOULD_HAVE_AN_ILLEGAL_ARGUMENT_EXCEPTION);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    @Test
    void findFormTypes1099FromFormCodeEmptyString() {
        try {
            FormTypes1099.findFormTypes1099FromFormCode(StringUtils.EMPTY);
            fail(SHOULD_HAVE_AN_ILLEGAL_ARGUMENT_EXCEPTION);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

    @Test
    void findFormTypes1099FromFormCodeDummyText() {
        try {
            FormTypes1099.findFormTypes1099FromFormCode("Foo");
            fail(SHOULD_HAVE_AN_ILLEGAL_ARGUMENT_EXCEPTION);
        } catch (Exception e) {
            assertEquals(IllegalArgumentException.class, e.getClass());
        }
    }

}
