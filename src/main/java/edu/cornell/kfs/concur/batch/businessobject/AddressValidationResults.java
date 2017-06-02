package edu.cornell.kfs.concur.batch.businessobject;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kim.api.identity.Person;

public class AddressValidationResults {
    private Person person;
    private final boolean valid;
    private final List<String> errorMessages;
    
    public AddressValidationResults(Person person, boolean valid, List<String> errorMessages) {
        this.person = person;
        this.valid = valid;
        this.errorMessages = errorMessages;
    }

    public Person getPerson() {
        return person;
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder("The person is ");
        if (ObjectUtils.isNotNull(person)) {
            sb.append(person.getName()).append(KFSConstants.DELIMITER).append(StringUtils.SPACE);
            sb.append("The address is ");
            if (this.valid) {
                sb.append("valid. ");
            } else {
                sb.append("NOT valid. ");
                if (CollectionUtils.isNotEmpty(errorMessages)) {
                    for (String message : errorMessages) {
                        sb.append(message).append(KFSConstants.DELIMITER).append(StringUtils.SPACE);
                    }
                }
            }
        } else {
            sb.append("NULL. ");
        }
        
        return sb.toString();
    }
}
