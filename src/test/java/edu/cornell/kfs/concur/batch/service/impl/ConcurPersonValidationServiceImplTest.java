package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.impl.identity.PersonServiceImpl;

import edu.cornell.kfs.concur.batch.businessobject.AddressValidationResults;
import edu.cornell.kfs.concur.batch.service.impl.fixture.ConcurPersonFixture;

public class ConcurPersonValidationServiceImplTest {
    
    ConcurPersonValidationServiceImpl personValidationService;
    
    @Before
    public void setUp() throws Exception {
        personValidationService = new ConcurPersonValidationServiceImpl();
        personValidationService.setPersonService(new TestablePersonService());
    }

    @After
    public void tearDown() throws Exception {
        personValidationService = null;
    }

    @Test
    public void validPerson() {
        assertTrue(personValidationService.validPerson(ConcurPersonFixture.JOHN_STATE_COUNTRY.person.getEmployeeId()));
    }
    
    @Test
    public void invalidPerson() {
        assertFalse(personValidationService.validPerson("foo"));
    }

    @Test
    public void validPdpAddress_bothStateAndCountry() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.JOHN_STATE_COUNTRY.person.getEmployeeId());
        assertTrue(results.isValid());
    }
    
    @Test
    public void validPdpAddress_justState() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.JANE_STATE.person.getEmployeeId());
        assertTrue(results.isValid());
    }
    
    @Test
    public void validPdpAddress_justCountry() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.TOM_COUNTRY.person.getEmployeeId());
        assertTrue(results.isValid());
    }
    
    @Test
    public void invalidPdpAddress() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.TINA_INVALID_ADDRESS.person.getEmployeeId());
        assertFalse(results.isValid());
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
