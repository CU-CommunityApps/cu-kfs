package edu.cornell.kfs.concur.batch.service.impl.fixture;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.concur.ConcurTestConstants;
import edu.cornell.kfs.concur.rest.jsonObjects.ConcurRequestV4PersonDTO;
import edu.cornell.kfs.sys.CUKFSConstants;

public enum RequestV4PersonFixture {
    JOHN_DOE("a1b2c3d4-efgh-5678-i9j0-111kk22llmm3", "John", "Q", "Doe", false),
    JOHN_TEST("nmlkjihg-9876-fedc-5432-b1a0b1a0b1a0", "John", "", "Test", true),
    JANE_DOE("99999999-zzzz-8888-yyyy-xxwwvvuu7766", "Jane", "J", "Doe", true),
    BOB_SMITH("11111111-2222-3333-4444-555555555555", "Bob", "B", "Smith", false),
    MARY_GRANT("pppppppp-qqqq-rrrr-ssss-tttttttttttt", "Mary", "R", "Grant", false),
    TEST_MANAGER("11111111-1111-1111-1111-111111111111", "TestManager", "", "TestManager", true),
    TEST_APPROVER("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "TestApprover", "", "TestApprover", true);

    public final String id;
    public final String firstName;
    public final String middleInitial;
    public final String lastName;
    public final boolean testUser;

    private RequestV4PersonFixture(String id, String firstName, String middleInitial, String lastName,
            boolean testUser) {
        this.id = id;
        this.firstName = firstName;
        this.middleInitial = middleInitial;
        this.lastName = lastName;
        this.testUser = testUser;
    }

    public ConcurRequestV4PersonDTO toConcurRequestV4PersonDTO() {
        ConcurRequestV4PersonDTO personDTO = new ConcurRequestV4PersonDTO();
        personDTO.setId(id);
        personDTO.setFirstName(firstName);
        personDTO.setLastName(lastName);
        if (StringUtils.isNotBlank(middleInitial)) {
            personDTO.setMiddleInitial(middleInitial);
        }
        return personDTO;
    }

    public String toKeyValuePairForParameterEntry() {
        return getNameForParameterEntry() + CUKFSConstants.EQUALS_SIGN + id;
    }

    public String getNameForParameterEntry() {
        return StringUtils.startsWithIgnoreCase(firstName, ConcurTestConstants.TEST_NAME_PREFIX)
                ? firstName : firstName + lastName;
    }

    public String getFullName() {
        return StringUtils.joinWith(KFSConstants.BLANK_SPACE, firstName, middleInitial, lastName);
    }

}
