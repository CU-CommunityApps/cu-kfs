package edu.cornell.kfs.module.purap.document.service.impl;

import static org.junit.Assert.*;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.service.PostalCodeValidationService;
import org.kuali.kfs.vnd.service.PhoneNumberService;

import edu.cornell.kfs.module.purap.document.service.PurchaseOrderTransmissionMethodDataRulesService;

public class PurchaseOrderTransmissionMethodDataRulesServiceImplTest {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(PurchaseOrderTransmissionMethodDataRulesServiceImpl.class);
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
		
		PostalCodeValidationService mockPostalCodeValidationService = EasyMock.createMock(PostalCodeValidationService.class);
		EasyMock.expect(mockPostalCodeValidationService.validateAddress(COUNTRY, STATE, ZIP_GOOD, EMPTY, EMPTY)).andReturn(true);
		EasyMock.expect(mockPostalCodeValidationService.validateAddress(COUNTRY, STATE, ZIP_BAD, EMPTY, EMPTY)).andReturn(false);
		EasyMock.replay(mockPostalCodeValidationService);
		((PurchaseOrderTransmissionMethodDataRulesServiceImpl)this.poTransMethodSataRulesService).setPostalCodeValidationService(mockPostalCodeValidationService);
		
		PhoneNumberService mockPhoneNumberService = EasyMock.createMock(PhoneNumberService.class);
		EasyMock.expect(mockPhoneNumberService.isValidPhoneNumber(PHONE_NUMBER_GOOD)).andReturn(true);
		EasyMock.expect(mockPhoneNumberService.isValidPhoneNumber(PHONE_NUMBER_BAD)).andReturn(false);
		EasyMock.replay(mockPhoneNumberService);
		((PurchaseOrderTransmissionMethodDataRulesServiceImpl)this.poTransMethodSataRulesService).setPhoneNumberService(mockPhoneNumberService);
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
}
