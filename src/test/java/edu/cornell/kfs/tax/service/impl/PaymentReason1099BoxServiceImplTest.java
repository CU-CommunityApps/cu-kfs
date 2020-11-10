package edu.cornell.kfs.tax.service.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.tax.service.PaymentReason1099BoxService;

public class PaymentReason1099BoxServiceImplTest {
	
	private PaymentReason1099BoxService paymentReasonService;

	@Before
	public void setUp() throws Exception {
		paymentReasonService = new TestablePaymentReason1099BoxServiceImpl();
	}

	@After
	public void tearDown() throws Exception {
		paymentReasonService = null;
	}

	@Test
	public void testisPaymentReasonMappedTo1099BoxTrue() {
		assertTrue(paymentReasonService.isPaymentReasonMappedTo1099Box("R"));
	}
	
	@Test
	public void testisPaymentReasonMappedTo1099BoxFalse() {
		assertFalse(paymentReasonService.isPaymentReasonMappedTo1099Box("X"));
	}
	
	@Test
	public void testgetPaymentReason1099BoxGood() {
		String expected = "MISC(2)";
		String results = paymentReasonService.getPaymentReason1099Box("R");
		String message = "We expected MISC(2) but got " + results;
		assertEquals(message, expected, results);
	}
	
	@Test
	public void testgetPaymentReason1099BoxGood2() {
		String expected = "NEC(3)";
		String results = paymentReasonService.getPaymentReason1099Box("O");
		String message = "We expected NEC(3) but got " + results;
		assertEquals(message, expected, results);
	}
	
	@Test
	public void testgetPaymentReason1099BoxBad() {
		String expected = null;
		String results = paymentReasonService.getPaymentReason1099Box("X");
		String message = "We expected NULL but got " + results;
		assertEquals(message, expected, results);
	}
	
	@Test
	public void testgetPaumentReasonToNo1099BoxesGood() {
		boolean results = paymentReasonService.isPaymentReasonMappedToNo1099Box("B");
		assertTrue(results);
	}
	
	@Test
	public void testgetPaumentReasonToNo1099BoxesGood2() {
		boolean results = paymentReasonService.isPaymentReasonMappedToNo1099Box("J");
		assertTrue(results);
	}
	
	@Test
	public void testgetPaumentReasonToNo1099BoxesBad() {
		boolean results = paymentReasonService.isPaymentReasonMappedToNo1099Box("x");
		assertFalse(results);
	}
	
	/**
	 * This circumvents the parameter service and returns a collection of values that are good test criteria. 
	 */
	private class TestablePaymentReason1099BoxServiceImpl extends PaymentReason1099BoxServiceImpl {
		public Collection<String> getPaymentReasonTo1099BoxMappings() {
			Collection<String> strings = new ArrayList<String>();
			strings.add("R=MISC(2)");
			strings.add("O=NEC(3)");
			return strings;
		}
		
		public Collection<String> getPaymentReasonToNo1099Boxes() {
			Collection<String> strings = new ArrayList<String>();
			strings.add("B");
			strings.add("J");
			return strings;
		}
	}

}
