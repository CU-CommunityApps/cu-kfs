package edu.cornell.kfs.concur.batch.service.impl;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Map;

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
        assertTrue(personValidationService.validPerson(ConcurPersonFixture.JOHN_ITHACA.person.getEmployeeId()));
    }
    
    @Test
    public void invalidPerson() {
        assertFalse(personValidationService.validPerson("foo"));
    }

    @Test
    public void validPdpAddress() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.JOHN_ITHACA.person.getEmployeeId());
        assertTrue(results.isValid());
    }
    
    @Test
    public void invalidPdpAddress_noAddress() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.JANE_NO_ADDRESS.person.getEmployeeId());
        assertFalse(results.isValid());
        assertEquals(1, results.getErrorMessages().size());
        assertEquals("Address is empty. ", results.getErrorMessages().get(0));
    }
    
    @Test
    public void validPdpAddress_useOtherAddressField() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.TANYA_THIRD_ADDRESS.person.getEmployeeId());
        assertTrue(results.isValid());
    }
    
    @Test
    public void invalidPdpAddress_noCity() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.TOM_NO_CITY.person.getEmployeeId());
        assertFalse(results.isValid());
        assertEquals(1, results.getErrorMessages().size());
        assertEquals("City is empty. ", results.getErrorMessages().get(0));
    }
    
    @Test
    public void invalidPdpAddress_noState() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.DICK_NO_STATE.person.getEmployeeId());
        assertFalse(results.isValid());
        assertEquals(1, results.getErrorMessages().size());
        assertEquals("State/Province is empty. ", results.getErrorMessages().get(0));
    }
    
    @Test
    public void invalidPdpAddress_noPostal() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.HARRY_NO_POSTAL.person.getEmployeeId());
        assertFalse(results.isValid());
        assertEquals(1, results.getErrorMessages().size());
        assertEquals("Postal Code is empty. ", results.getErrorMessages().get(0));
    }
    
    @Test
    public void invalidPdpAddress_noCountry() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.FRANK_NO_COUNTRY.person.getEmployeeId());
        assertFalse(results.isValid());
        assertEquals(1, results.getErrorMessages().size());
        assertEquals("Country Code is empty. ", results.getErrorMessages().get(0));
    }
    
    @Test
    public void invalidPdpAddress_noMultipleBlankFields() {
        AddressValidationResults results = personValidationService.validPdpAddress(ConcurPersonFixture.ALLIE_NO_COUNTRY_STATE_ZIP.person.getEmployeeId());
        assertFalse(results.isValid());
        assertEquals(3, results.getErrorMessages().size());
        assertEquals("State/Province is empty. ", results.getErrorMessages().get(0));
        assertEquals("Postal Code is empty. ", results.getErrorMessages().get(1));
        assertEquals("Country Code is empty. ", results.getErrorMessages().get(2));
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
