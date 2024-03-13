package edu.cornell.kfs.kim.api.identity;

import java.util.Set;

import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.affiliation.EntityAffiliationType;

public interface CuPersonService extends PersonService {

    EntityAffiliationType getAffiliationType(final String affiliationTypeCode);

    Set<String> getAffiliationTypesSupportingEmploymentInformation();

}
