package org.kuali.kfs.pdp.businessobject;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.sys.businessobject.PostalCode;
import org.kuali.kfs.sys.businessobject.State;

public class ACHBankTest {
    
    private ACHBank bank;
    private State state;
    private PostalCode postalCode;

    @Before
    public void setUp() throws Exception {
        bank = new ACHBank();
        state = new State();
        state.setCode("NY");
        postalCode = new PostalCode();
        postalCode.setCode("14850");
    }

    @After
    public void tearDown() throws Exception {
        bank = null;
        state = null;
        postalCode = null;
    }

    @Test
    public void testSetBank() {
        bank.setBankState(state);
        assertEquals(state, bank.getBankState());
    }
    
    @Test
    public void testSetBankMull() {
        bank.setBankState(null);
        assertEquals(null, bank.getBankState());
    }
    
    @Test
    public void testSetPostalCode() {
        bank.setPostalCode(postalCode);
        assertEquals(postalCode, bank.getPostalCode());
    }
    
    @Test
    public void testSetPostalCodeNull() {
        bank.setPostalCode(null);
        assertEquals(null, bank.getPostalCode());
    }


}
