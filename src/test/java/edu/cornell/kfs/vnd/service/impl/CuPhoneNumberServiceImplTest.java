package edu.cornell.kfs.vnd.service.impl;

import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.vnd.fixture.PhoneNumberFixture;

@ConfigureContext
public class CuPhoneNumberServiceImplTest extends KualiTestBase {

	CuPhoneNumberServiceImpl cuPhoneNumberServiceImpl;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cuPhoneNumberServiceImpl = SpringContext.getBean(CuPhoneNumberServiceImpl.class);
	}
	
	public void testFormatNumberIfPossible( ) {
		
		PhoneNumberFixture oneEightHundred = PhoneNumberFixture.EIGHT_HUNNID;
		PhoneNumberFixture withParentheses = PhoneNumberFixture.W_PARENS;
		PhoneNumberFixture losAngeles = PhoneNumberFixture.LOS_ANGELES;
		PhoneNumberFixture nyc = PhoneNumberFixture.NYC_SPACES;

		assertEquals(cuPhoneNumberServiceImpl.formatNumberIfPossible(oneEightHundred.unformatted), oneEightHundred.formatted);
		assertEquals(cuPhoneNumberServiceImpl.formatNumberIfPossible(withParentheses.unformatted), withParentheses.formatted);
		assertEquals(cuPhoneNumberServiceImpl.formatNumberIfPossible(losAngeles.unformatted), losAngeles.formatted);
		assertEquals(cuPhoneNumberServiceImpl.formatNumberIfPossible(nyc.unformatted), nyc.formatted);
	}
}
