package edu.cornell.kfs.tax;
import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mchange.util.AssertException;

class FormTypes1099Test {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void findFormTypes1099FromFormCodeMisc() {
		FormTypes1099 expectedType = FormTypes1099.MISC;
		FormTypes1099 actualType = FormTypes1099.findFormTypes1099FromFormCode(FormTypes1099.MISC.formCode);
		assertEquals(expectedType.formDescription, actualType.formDescription);
	}
	
	@Test
	void findFormTypes1099FromFormCodeNec() {
		FormTypes1099 expectedType = FormTypes1099.NEC;
		FormTypes1099 actualType = FormTypes1099.findFormTypes1099FromFormCode(FormTypes1099.NEC.formCode);
		assertEquals(expectedType.formDescription, actualType.formDescription);
	}
	
	@Test
	void findFormTypes1099FromFormCodeNull() {
		try {
			FormTypes1099.findFormTypes1099FromFormCode(null);
			fail("Should have an illegal argument exception");
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}
	}
	
	@Test
	void findFormTypes1099FromFormCodeEmptyString() {
		try {
			FormTypes1099.findFormTypes1099FromFormCode(StringUtils.EMPTY);
			fail("Should have an illegal argument exception");
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}
	}

}
