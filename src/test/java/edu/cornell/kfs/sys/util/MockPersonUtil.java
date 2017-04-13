package edu.cornell.kfs.sys.util;

import org.easymock.EasyMock;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.impl.identity.PersonImpl;

public class MockPersonUtil {
    public static UserSession createMockUserSession(Person person) {
        UserSession userSession = EasyMock.createMock(UserSession.class);
        EasyMock.expect(userSession.getPrincipalId()).andStubReturn(person.getPrincipalId());
        EasyMock.expect(userSession.getPrincipalName()).andStubReturn(person.getPrincipalName());
        EasyMock.expect(userSession.getLoggedInUserPrincipalName()).andStubReturn(person.getPrincipalName());
        EasyMock.expect(userSession.getPerson()).andStubReturn(person);
        EasyMock.expect(userSession.getActualPerson()).andStubReturn(person);
        EasyMock.replay(userSession);
        return userSession;
    }

    public static Person createMockPerson(UserNameFixture userNameFixture) {
        Person person = EasyMock.createMock(PersonImpl.class);
        EasyMock.expect(person.getPrincipalName()).andStubReturn(userNameFixture.toString());
        EasyMock.expect(person.getPrincipalId()).andStubReturn(userNameFixture.toString());
        EasyMock.expect(person.getCampusCode()).andStubReturn(userNameFixture.toString());
        EasyMock.replay(person);
        return person;
    }
}
