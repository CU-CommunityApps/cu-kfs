package edu.cornell.kfs.sys.util;

import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.impl.identity.PersonImpl;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;

public class MockPersonUtil {
    public static UserSession createMockUserSession(Person person) {
        UserSession userSession = mock(UserSession.class);
        
        String principalId = person.getPrincipalId();
        when(userSession.getPrincipalId()).thenReturn(principalId);
        
        String principalName = person.getPrincipalName();
        when(userSession.getPrincipalName()).thenReturn(principalName);
        
        when(userSession.getLoggedInUserPrincipalName()).thenReturn(principalName);
        when(userSession.getPerson()).thenReturn(person);
        when(userSession.getActualPerson()).thenReturn(person);
        
        return userSession;
    }

    public static Person createMockPerson(UserNameFixture userNameFixture) {
        Person person = spy(PersonImpl.class);
        when(person.getPrincipalName()).thenReturn(userNameFixture.toString());
        when(person.getPrincipalId()).thenReturn(userNameFixture.toString());
        when(person.getCampusCode()).thenReturn(userNameFixture.toString());
        return person;
    }
}
