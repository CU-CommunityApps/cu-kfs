package edu.cornell.kfs.module.purap.document.service.impl;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.UserNameFixture;

import edu.cornell.kfs.module.purap.document.service.PurchaseOrderTransmissionMethodDataRulesService;

@ConfigureContext(session = UserNameFixture.ccs1)
public class PurchaseOrderTransmissionMethodDataRulesServiceImplTest extends KualiTestBase {
	
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(PurchaseOrderTransmissionMethodDataRulesServiceImpl.class);

	private PurchaseOrderTransmissionMethodDataRulesService poTransMethodSataRulesService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		poTransMethodSataRulesService = SpringContext.getBean(PurchaseOrderTransmissionMethodDataRulesService.class);
	}
	
	public void testIsFaxNumberValid_Success(){		
		boolean valid = poTransMethodSataRulesService.isFaxNumberValid("607-220-3712");
		assertTrue(valid);	
	}
	
	public void testIsFaxNumberValid_Fail(){		
		boolean valid = poTransMethodSataRulesService.isFaxNumberValid("ab258h c23");
		assertFalse(valid);	
	}
	
	public void testIsEmailAddressValid_Success(){
		boolean valid = poTransMethodSataRulesService.isEmailAddressValid("abc@email.com");
		assertTrue(valid);
	}
	
	public void testIsEmailAddressValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isEmailAddressValid("abc.email.com");
		assertFalse(valid);
	}
	
	public void testIsCountryCodeValid_Success(){
		boolean valid = poTransMethodSataRulesService.isCountryCodeValid("US");
		assertTrue(valid);
	}
	
	public void testIsCountryCodeValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isCountryCodeValid("");
		assertFalse(valid);
	}
	
	public void testIsStateCodeValid_Success(){
		boolean valid = poTransMethodSataRulesService.isStateCodeValid("NY");
		assertTrue(valid);
	}
	
	public void testIsStateCodeValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isStateCodeValid("");
		assertFalse(valid);
	}
	
	public void testIsZipCodeValid_Success(){
		boolean valid = poTransMethodSataRulesService.isZipCodeValid("14950");
		assertTrue(valid);
	}
	
	public void testIsZipCodeValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isZipCodeValid("");
		assertFalse(valid);
	}
	
	public void testIsAddress1Valid_Success(){
		boolean valid = poTransMethodSataRulesService.isAddress1Valid("line 1 address");
		assertTrue(valid);
	}
	
	public void testIsAddress1Valid_Fail(){
		boolean valid = poTransMethodSataRulesService.isAddress1Valid("");
		assertFalse(valid);
	}
	
	public void testIsCityValid_Success(){
		boolean valid = poTransMethodSataRulesService.isCityValid("Ithaca");
		assertTrue(valid);
	}
	
	public void testIsCityValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isCityValid("");
		assertFalse(valid);
	}
	
	public void testIsPostalAddressValid_Success(){
		boolean valid = poTransMethodSataRulesService.isPostalAddressValid("line 1 address", "Ithaca", "NY", "14850", "US");
		assertTrue(valid);
	}
	
	public void testIsPostalAddressValid_Fail(){
		boolean valid = poTransMethodSataRulesService.isPostalAddressValid("line 1 address", "Ithaca", "NY", "64835634856", "US");
		assertFalse(valid);
	}
}
