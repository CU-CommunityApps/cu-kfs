package edu.cornell.kfs.pdp.batch.fixture;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.kim.impl.identity.Person;

public enum ACHPersonPayeeFixture {
    JOHN_DOE("1234567", "jad987", "2345678", "Doe, John A.", "1122333", "jad987@someplace.edu"),
    JANE_DOE("9876543", "jd8888", "8765432", "Doe, Jane", "4455666", "jd8888@someplace.edu"),
    ROBERT_SMITH("5555555", "rs321", "4545454", "Smith, Robert", "7766888", "rs321@anotherplace.org"),
    MARY_SMITH("9797979", "mjs22", "8866444", "Smith, Mary J.", "9999999", "mjs22@anotherplace.org"),
    KFS_SYSTEM_USER("2", KFSConstants.SYSTEM_USER, "2", "SYSTEMUSER, KFS", "1", "kfs-system@someplace.edu");

    public final String principalId;
    public final String principalName;
    public final String entityId;
    public final String name;
    public final String employeeId;
    public final String emailAddress;

    private ACHPersonPayeeFixture(String principalId, String principalName, String entityId, String name,
            String employeeId, String emailAddress) {
        this.principalId = principalId;
        this.principalName = principalName;
        this.entityId = entityId;
        this.name = name;
        this.employeeId = employeeId;
        this.emailAddress = emailAddress;
    }

    public Person toPerson() {
        Person person = new Person();
        person.setPrincipalId(principalId);
        person.setPrincipalName(principalName);
        person.setEntityId(entityId);
        person.setEmployeeId(employeeId);
        person.setEmailAddress(emailAddress);
        person.setName(name);
        person.setActive(true);
        return person;
    }

}
