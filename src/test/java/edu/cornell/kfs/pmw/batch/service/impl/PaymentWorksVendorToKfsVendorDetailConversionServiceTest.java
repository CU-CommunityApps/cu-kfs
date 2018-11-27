package edu.cornell.kfs.pmw.batch.service.impl;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PaymentWorksVendorToKfsVendorDetailConversionServiceTest {

	private static final int MAX_LENGTH = 5;
	private static final String EMPTY_STRING = "";
	private static final String DELIM_LENGTH_ONE = ":";

	private static final String INPUT_VALUE_MORE_THAN_MAX_LENGTH = "abcdefg";
	private static final String INPUT_VALUE_LESS_THAN_MAX_LENGTH = "ab";
	private static final String INPUT_VALUE_EQUAL_MAX_LENGTH = "abcde";

	private static final String FIRST_NAME_MORE_THAN_MAX_LENGTH = "123456";

	private static final String LAST_NAME_MORE_THAN_MAX_LENGTH = "123456";
	private static final String LAST_NAME_EQUAL_MAX_LENGTH = "12345";
	private static final String LAST_NAME_LESS_THAN_MAX_LENGTH_BY_DELIM = "1234";
	private static final String LAST_NAME_LESS_THAN_MAX_LENGTH_BY_MORE_THAN_DELIM_LESS_FIRST_NAME = "12";

	private static final String TRUNCATED_FIRST_NAME_WHEN_LAST_NAME_LESS_THAN_MAX_LENGTH_BY_MORE_THAN_DELIM_LESS_FIRST_NAME = "12";
	private static final String FIRST_NAME_NO_TRUNCATION = "123";
	private static final String LAST_NAME_LESS_MAX_DELIM_AND_FIRST_NAME = "1";


	@Test
	public void testTruncateValueToMaxLengthInputMoreThanMaxLength() {
		String truncatedValue = PaymentWorksVendorToKfsVendorDetailConversionServiceImpl
				.truncateValueToMaxLength(INPUT_VALUE_MORE_THAN_MAX_LENGTH, MAX_LENGTH);
		assertEquals("Input Value length longer than max length was not properly truncated:  ",
				INPUT_VALUE_EQUAL_MAX_LENGTH, truncatedValue);
	}

	@Test
	public void testTruncateValueToMaxLengthInputLessThanMaxLength() {
		String truncatedValue = PaymentWorksVendorToKfsVendorDetailConversionServiceImpl
				.truncateValueToMaxLength(INPUT_VALUE_LESS_THAN_MAX_LENGTH, MAX_LENGTH);
		assertEquals("Input Value length shorter than max length was not properly truncated:  ",
				INPUT_VALUE_LESS_THAN_MAX_LENGTH, truncatedValue);
	}

	@Test
	public void testTruncateValueToMaxLengthInputEqualsMaxLength() {
		String truncatedValue = PaymentWorksVendorToKfsVendorDetailConversionServiceImpl
				.truncateValueToMaxLength(INPUT_VALUE_EQUAL_MAX_LENGTH, MAX_LENGTH);
		assertEquals("Input Value length equal to max length was not properly truncated:  ",
				INPUT_VALUE_EQUAL_MAX_LENGTH, truncatedValue);
	}

	@Test
	public void testTruncateValueToMaxLengthInputEmpty() {
		String truncatedValue = PaymentWorksVendorToKfsVendorDetailConversionServiceImpl
				.truncateValueToMaxLength(EMPTY_STRING, MAX_LENGTH);
		assertEquals("Empty input value was not properly truncated:  ", EMPTY_STRING, truncatedValue);
	}

	@Test
	public void testTruncateFirstNameWhenLastNameLongerThanMaxLength() {
		String truncatedValue = PaymentWorksVendorToKfsVendorDetailConversionServiceImpl
				.truncateLegalFirstNameToMaximumAllowedLengthWhenFormattedWithLegalLastName(
						LAST_NAME_MORE_THAN_MAX_LENGTH, FIRST_NAME_MORE_THAN_MAX_LENGTH, DELIM_LENGTH_ONE, MAX_LENGTH);
		assertEquals("Firt name should be truncated to empty string when last name length is more than max length:  ",
				EMPTY_STRING, truncatedValue);
	}

	@Test
	public void testTruncateFirstNameWhenLastNameEqualMaxLength() {
		String truncatedValue = PaymentWorksVendorToKfsVendorDetailConversionServiceImpl
				.truncateLegalFirstNameToMaximumAllowedLengthWhenFormattedWithLegalLastName(LAST_NAME_EQUAL_MAX_LENGTH,
						FIRST_NAME_MORE_THAN_MAX_LENGTH, DELIM_LENGTH_ONE, MAX_LENGTH);
		assertEquals("Firt name should be truncated to empty string when last name length is equal max length:  ",
				EMPTY_STRING, truncatedValue);
	}

	@Test
	public void testTruncateFirstNameWhenLastNameShorterThanMaxLengthByTheLengthOfDelim() {
		String truncatedValue = PaymentWorksVendorToKfsVendorDetailConversionServiceImpl
				.truncateLegalFirstNameToMaximumAllowedLengthWhenFormattedWithLegalLastName(
						LAST_NAME_LESS_THAN_MAX_LENGTH_BY_DELIM, FIRST_NAME_MORE_THAN_MAX_LENGTH, DELIM_LENGTH_ONE,
						MAX_LENGTH);
		assertEquals(
				"Firt name should be truncated to empty string when last name is just the length of delimiter less than max length:  ",
				EMPTY_STRING, truncatedValue);
	}

	@Test
	public void testTruncateFirstNameWhenLastNameShorterThanMaxLengthByMoreThanLengthOfDelimButLessLengthOfFirstName() {
		String truncatedValue = PaymentWorksVendorToKfsVendorDetailConversionServiceImpl
				.truncateLegalFirstNameToMaximumAllowedLengthWhenFormattedWithLegalLastName(
						LAST_NAME_LESS_THAN_MAX_LENGTH_BY_MORE_THAN_DELIM_LESS_FIRST_NAME,
						FIRST_NAME_MORE_THAN_MAX_LENGTH, DELIM_LENGTH_ONE, MAX_LENGTH);
		assertEquals(
				"Firt name should be truncated when last name is less max length by more than delim length but less than first name length:  ",
				TRUNCATED_FIRST_NAME_WHEN_LAST_NAME_LESS_THAN_MAX_LENGTH_BY_MORE_THAN_DELIM_LESS_FIRST_NAME,
				truncatedValue);
	}

	@Test
	public void testTruncateLegalFirstNameWhenLastNameShorterThanMaxLengthByLengthOfFirstName() {
		String truncatedValue = PaymentWorksVendorToKfsVendorDetailConversionServiceImpl
				.truncateLegalFirstNameToMaximumAllowedLengthWhenFormattedWithLegalLastName(
						LAST_NAME_LESS_MAX_DELIM_AND_FIRST_NAME, FIRST_NAME_NO_TRUNCATION, DELIM_LENGTH_ONE,
						MAX_LENGTH);
		assertEquals(
				"Firt name should not be truncated when last name length less than max by more than first name and delim:  ",
				FIRST_NAME_NO_TRUNCATION, truncatedValue);
	}

}
