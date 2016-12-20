package edu.cornell.kfs.concur.rest.services;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.concur.service.impl.ConcurAuthenticationServiceImpl;

public class ConcurAuthenticationServiceImplTest {

    private static final String GOOD_TOKEN = "goodToken";
    private static final String BAD_TOKEN = "badToken";

    private ConcurAuthenticationServiceImpl concurAuthenticationService;

    @Before
    public void setUp() {
        concurAuthenticationService = new ConcurAuthenticationServiceImpl();
        concurAuthenticationService.setConcurToken(GOOD_TOKEN);
    }

    @Test
    public void isConcurTokenValidGoodToken() {
        assertEquals("The token should have been valid", true,
                concurAuthenticationService.isConcurTokenValid(GOOD_TOKEN));
    }

    @Test
    public void isConcurTokenValidBadToken() {
        assertEquals("The token should have been invalid", false,
                concurAuthenticationService.isConcurTokenValid(BAD_TOKEN));
    }

    @Test
    public void isConcurTokenValidEmptyToken() {
        assertEquals("The empty token should have been invalid", false,
                concurAuthenticationService.isConcurTokenValid(StringUtils.EMPTY));
    }
}
