package org.kuali.kfs.sys.fixture;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kim.api.identity.Person;

public enum UserNameFixture {
    NO_SESSION, // This is not a user name. It is a Sentinal value telling KualiTestBase not to create a session. (It's needed
    // because null is not a valid default for the ConfigureContext annotation's session element.)
kfs, // This is the KualiUser.SYSTEM_USER, which certain automated document type authorizers require.
khuntley, // KualiTestBaseWithSession used this one by default. (testUsername in configuration.properties, no longer used but
    // cannot be removed because that file cannot be committed).
ghatten, stroud, dfogle, rjweiss, rorenfro, sterner, ferland, hschrein, hsoucy, lraab, jhavens, kcopley, mhkozlow, ineff, vputman, cswinson, mylarge, rruffner, season, dqperron, aatwood, parke, appleton, twatson, butt, jkitchen , bomiddle, aickes, day, jgerhart,
// Regional Budget Manager
ocmcnall,
// University Administration Budget Manager
wbrazil,
// Non-KFS User
bcoffee,
// Account secondary delegate
rmunroe,
// Org Hierarchy Reviewer
cknotts,
// Accounting Hierarchy Reviewer
jrichard,
ccs1,
rae28,
// favorite account manager
db18;

static {
// Assert.assertEquals(KualiUser.SYSTEM_USER, kuluser.toString());
}

public Person getPerson() {
return SpringContext.getBean(org.kuali.rice.kim.api.identity.PersonService.class).getPersonByPrincipalName(toString());
}
}
