package edu.cornell.kfs.kim.inquiry;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.inquiry.PersonInquirableImpl;
import org.kuali.kfs.krad.bo.BusinessObject;

public class CuPersonInquirableImpl extends PersonInquirableImpl {

    @SuppressWarnings("rawtypes")
    @Override
    public BusinessObject getBusinessObject(final Map fieldValues) {
        // KFSPTS-19308 CU customization to allow inquiry by principalName
        Person person = null;
        if (fieldValues.containsKey("principalId")) {
            person = getPersonService().getPerson(fieldValues.get("principalId").toString());
        }

        if (person == null && fieldValues.containsKey("principalName")) {
            final String principalName = fieldValues.get("principalName").toString();
            if (StringUtils.isNotBlank(principalName)) {
                person = getPersonService().getPersonByPrincipalName(principalName);
            }
        }

        if (person != null && person instanceof Person) {
            ((Person) person).populateMembers();
        }
        return person;

    }

}
