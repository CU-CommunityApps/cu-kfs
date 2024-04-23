package edu.cornell.kfs.kns.util;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.sys.context.SpringContext;

public final class CuWebUtils {

    private CuWebUtils() {
        throw new UnsupportedOperationException("do not call");
    }

    /*
     * This is a copy of WebUtils.getPrincipalDisplayName() that has been modified
     * to return the potentially masked Person name instead.
     */
    public static String getPrincipalDisplayName(final String principalId) {
        if (StringUtils.isBlank(principalId)) {
            throw new IllegalArgumentException("Principal ID must have a value");
        }
        final Person person = SpringContext.getBean(PersonService.class).getPerson(principalId);
        if (person == null) {
            return "";
        } else {
            // ==== CU Customization: Return potentially masked Person name instead. ====
            return person.getNameMaskedIfNecessary();
        }
    }

}
