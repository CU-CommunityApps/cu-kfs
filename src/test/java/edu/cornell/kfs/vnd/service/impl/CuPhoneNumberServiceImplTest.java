package edu.cornell.kfs.vnd.service.impl;

import edu.cornell.kfs.sys.service.mock.MockParameterServiceImpl;
import edu.cornell.kfs.vnd.fixture.PhoneNumberFixture;
import junit.framework.TestCase;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import java.util.Collection;

@ConfigureContext
public class CuPhoneNumberServiceImplTest extends TestCase {

	CuPhoneNumberServiceImpl cuPhoneNumberServiceImpl;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cuPhoneNumberServiceImpl = new CuPhoneNumberServiceImpl();
		cuPhoneNumberServiceImpl.setParameterService(new MockParameterServiceImpl());
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
