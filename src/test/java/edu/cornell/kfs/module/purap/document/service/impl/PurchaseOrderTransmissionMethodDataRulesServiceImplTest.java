package edu.cornell.kfs.module.purap.document.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.service.PostalCodeValidationService;
import org.kuali.kfs.sys.service.impl.PostalCodeValidationServiceImpl;
import org.kuali.kfs.vnd.service.PhoneNumberService;
import org.kuali.kfs.vnd.service.impl.PhoneNumberServiceImpl;
import org.kuali.kfs.kns.datadictionary.validation.fieldlevel.ZipcodeValidationPattern;
import org.kuali.kfs.sys.businessobject.State;
import org.kuali.kfs.sys.service.LocationService;
import org.springframework.util.StringUtils;

import edu.cornell.kfs.module.purap.document.service.PurchaseOrderTransmissionMethodDataRulesService;

public class PurchaseOrderTransmissionMethodDataRulesServiceImplTest {
	private static final Logger LOG = LogManager.getLogger(PurchaseOrderTransmissionMethodDataRulesServiceImplTest.class);
	private static final String PHONE_NUMBER_GOOD = "607-220-3712";
	private static final String PHONE_NUMBER_BAD = "ab258h c23";
	private static final String ADDRESS = "line 1 address";
	private static final String CITY = "Ithaca";
	private static final String STATE = "NY";
	private static final String COUNTRY = "US";
	private static final String ZIP_GOOD = "14850";
	private static final String ZIP_BAD = "64835634856";
	private static final String EMPTY = "";
	
	private PurchaseOrderTransmissionMethodDataRulesService poTransMethodSataRulesService;
	
	@Before
	public void setUp() throws Exception {
		poTransMethodSataRulesService = new PurchaseOrderTransmissionMethodDataRulesServiceImpl();

		PostalCodeValidationService postalCodeValidationSerivce = new testablePostalCodeValidationServiceImpl();
		LocationService mockLocationService = mock(LocationService.class);
		when(mockLocationService.getState(COUNTRY, STATE)).thenReturn(makeState());
		((PostalCodeValidationServiceImpl)postalCodeValidationSerivce).setLocationService(mockLocationService);
		((PurchaseOrderTransmissionMethodDataRulesServiceImpl)this.poTransMethodSataRulesService).setPostalCodeValidationService(postalCodeValidationSerivce);
		
		PhoneNumberService phoneNumberService = new testablePhoneNumberServiceImpl();
		((PurchaseOrderTransmissionMethodDataRulesServiceImpl)this.poTransMethodSataRulesService).setPhoneNumberService(phoneNumberService);
	}
	
	@After
	public void testDown() {
		poTransMethodSataRulesService = null;
	}
	
	@Test
	public void testIsFaxNumberValid_Success(){	
		boolean valid = poTransMethodSataRulesService.isFaxNumberValid(PHONE_NUMBER_GOOD);
		assertTrue(valid);	
	}
	
	@Test
	public void testIsFaxNumberValid_Fail(){		
		boolean valid = poTransMethodSataRulesService.isFaxNumberValid(PHONE_NUMBER_BAD);
		assertFalse(valid);	
	}
	
	@Test
	public void testIsEmailAddressValid_Success(){
		boolean valid = poTransMethodSataRulesService.isEmailAddressValid("abc@email.com");
		assertTrue(valid);
	}
	
	@Test
	public void testIsEmailAddressValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isEmailAddressValid("abc.email.com");
		assertFalse(valid);
	}
	
	@Test
	public void testIsCountryCodeValid_Success(){
		boolean valid = poTransMethodSataRulesService.isCountryCodeValid(COUNTRY);
		assertTrue(valid);
	}
	
	@Test
	public void testIsCountryCodeValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isCountryCodeValid(EMPTY);
		assertFalse(valid);
	}
	
	@Test
	public void testIsStateCodeValid_Success(){
		boolean valid = poTransMethodSataRulesService.isStateCodeValid(STATE);
		assertTrue(valid);
	}
	
	@Test
	public void testIsStateCodeValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isStateCodeValid(EMPTY);
		assertFalse(valid);
	}
	
	@Test
	public void testIsZipCodeValid_Success(){
		boolean valid = poTransMethodSataRulesService.isZipCodeValid(ZIP_GOOD);
		assertTrue(valid);
	}
	
	@Test
	public void testIsZipCodeValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isZipCodeValid(EMPTY);
		assertFalse(valid);
	}
	
	@Test
	public void testIsAddress1Valid_Success(){
		boolean valid = poTransMethodSataRulesService.isAddress1Valid(ADDRESS);
		assertTrue(valid);
	}
	
	@Test
	public void testIsAddress1Valid_Fail(){
		boolean valid = poTransMethodSataRulesService.isAddress1Valid(EMPTY);
		assertFalse(valid);
	}
	
	@Test
	public void testIsCityValid_Success(){
		boolean valid = poTransMethodSataRulesService.isCityValid(CITY);
		assertTrue(valid);
	}
	
	@Test
	public void testIsCityValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isCityValid(EMPTY);
		assertFalse(valid);
	}
	
	@Test
	public void testIsPostalAddressValid_Success(){
		boolean valid = poTransMethodSataRulesService.isPostalAddressValid(ADDRESS, CITY, STATE, ZIP_GOOD, COUNTRY);
		assertTrue(valid);
	}
	
	@Test
	public void testIsPostalAddressValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isPostalAddressValid(ADDRESS, CITY, STATE, ZIP_BAD, COUNTRY);
		assertFalse(valid);
	}
	
	private State makeState() {
		State bo = new State();
		bo.setCode(STATE);
		bo.setName("New York");
		bo.setCountryCode(COUNTRY);
		return bo;
	}
	
	private class testablePhoneNumberServiceImpl extends PhoneNumberServiceImpl{
		@Override
		protected String[] parseFormats() {
			String phoneNumberRegX = "\\d{3}-\\d{3}-\\d{4};\\(\\d{3}\\)\\s\\d{3}-\\d{4};\\d{3}\\s\\d{3}\\s\\d{4};\\d{10}";
			return StringUtils.delimitedListToStringArray(phoneNumberRegX, ";");
		}
	}
	
	private class testablePostalCodeValidationServiceImpl extends PostalCodeValidationServiceImpl {
		@Override
		protected ZipcodeValidationPattern getZipcodeValidatePattern() {
			return new testableZipcodeValidationPattern();
		}
	}
	
	private class testableZipcodeValidationPattern extends ZipcodeValidationPattern {
		@Override
		protected String getRegexString() {
			String zipCodeRegX = "[0-9]{5}(\\-[0-9]{4})?"; 
	        return zipCodeRegX;
	    }
	}
	
}
