package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.identity.PersonServiceImpl;

import edu.cornell.kfs.concur.batch.service.impl.fixture.ConcurPersonFixture;

public class ConcurEmployeeInfoValidationServiceImplTest {
    
    ConcurEmployeeInfoValidationServiceImpl concurEmployeeInfoValidationService;
    
    @Before
    public void setUp() throws Exception {
        concurEmployeeInfoValidationService = new ConcurEmployeeInfoValidationServiceImpl();
        concurEmployeeInfoValidationService.setPersonService(new TestablePersonService());
    }

    @After
    public void tearDown() throws Exception {
        concurEmployeeInfoValidationService = null;
    }

    @Test
    public void validPerson() {
        assertTrue(concurEmployeeInfoValidationService.validPerson(ConcurPersonFixture.JOHN_STATE_COUNTRY.employeeId));
    }
    
    @Test
    public void invalidPerson() {
        assertFalse(concurEmployeeInfoValidationService.validPerson("foo"));
    }

    @Test
    public void validPdpAddress_bothStateAndCountry() {
        boolean results = concurEmployeeInfoValidationService.validPdpAddress(ConcurPersonFixture.JOHN_STATE_COUNTRY.employeeId);
        assertTrue(results);
    }
    
    @Test
    public void validPdpAddress_justState() {
        boolean results = concurEmployeeInfoValidationService.validPdpAddress(ConcurPersonFixture.JANE_STATE.employeeId);
        assertTrue(results);
    }
    
    @Test
    public void validPdpAddress_justCountry() {
        boolean results = concurEmployeeInfoValidationService.validPdpAddress(ConcurPersonFixture.TOM_COUNTRY.employeeId);
        assertTrue(results);
    }
    
    @Test
    public void invalidPdpAddress() {
        boolean results = concurEmployeeInfoValidationService.validPdpAddress(ConcurPersonFixture.TINA_INVALID_ADDRESS.employeeId);
        assertFalse(results);
    }
    
    private class TestablePersonService extends PersonServiceImpl {
        @Override
        public Person getPersonByEmployeeId(String employeeId) {
            try {
                return ConcurPersonFixture.personFromEmployeeId(employeeId);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

}
