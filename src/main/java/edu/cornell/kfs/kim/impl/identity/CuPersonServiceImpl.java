package edu.cornell.kfs.kim.impl.identity;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.identity.PersonServiceImpl;
import org.kuali.kfs.kim.impl.identity.affiliation.EntityAffiliationType;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.springframework.cache.annotation.Cacheable;

import edu.cornell.kfs.kim.CuKimPropertyConstants;
import edu.cornell.kfs.kim.api.identity.CuPersonService;

public class CuPersonServiceImpl extends PersonServiceImpl implements CuPersonService {

    // Temporary customization to add a constant from FINP-10589 in the 2024-04-03 financials release.
    private static final String LOWER_PRINCIPAL_NAME = "LOWER(" + KIMPropertyConstants.Principal.PRINCIPAL_NAME + ")";

    private CriteriaLookupService criteriaLookupService;
    private BusinessObjectService businessObjectService;

    /*
     * Temporary override of getPersonByPrincipalName() to backport the FINP-10589 fix
     * from the 2024-04-03 financials release. We may be able to remove this backport
     * when we upgrade to version 2024-04-03 or later; however, KualiCo configured the fix
     * to use the default Locale instead of Locale.US, so we've updated it to use the latter.
     * We may have to wait for a follow-up Locale usage fix before removing this backport.
     */
    @Cacheable(cacheNames = Person.CACHE_NAME, key = "'{getPerson}-principalName=' + #p0")
    @Override
    public Person getPersonByPrincipalName(final String principalName) {
        if (StringUtils.isBlank(principalName)) {
            return null;
        }

        return businessObjectService
                .findMatching(Person.class,
                        Map.of(LOWER_PRINCIPAL_NAME, principalName.toLowerCase(Locale.US))
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    @Cacheable(cacheNames = Person.CACHE_NAME, key = "'{getAffiliationType}-affiliationTypeCode=' + #p0")
    @Override
    public EntityAffiliationType getAffiliationType(final String affiliationTypeCode) {
        if (StringUtils.isBlank(affiliationTypeCode)) {
            return null;
        }
        final QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal(KFSPropertyConstants.CODE, affiliationTypeCode),
                PredicateFactory.equal(KFSPropertyConstants.ACTIVE, KRADConstants.YES_INDICATOR_VALUE));
        final GenericQueryResults<EntityAffiliationType> results = criteriaLookupService.lookup(
                EntityAffiliationType.class, criteria);
        return results.getResults().stream()
                .findFirst()
                .orElse(null);
    }

    @Cacheable(cacheNames = Person.CACHE_NAME, key = "'{getAffiliationTypesSupportingEmploymentInformation}'")
    @Override
    public Set<String> getAffiliationTypesSupportingEmploymentInformation() {
        final QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal(
                        CuKimPropertyConstants.EMPLOYMENT_AFFILIATION_TYPE, KRADConstants.YES_INDICATOR_VALUE),
                PredicateFactory.equal(KFSPropertyConstants.ACTIVE, KRADConstants.YES_INDICATOR_VALUE));
        final GenericQueryResults<EntityAffiliationType> results = criteriaLookupService.lookup(
                EntityAffiliationType.class, criteria);
        return results.getResults().stream()
                .map(EntityAffiliationType::getCode)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public void setCriteriaLookupService(final CriteriaLookupService criteriaLookupService) {
        super.setCriteriaLookupService(criteriaLookupService);
        this.criteriaLookupService = criteriaLookupService;
    }

    @Override
    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        super.setBusinessObjectService(businessObjectService);
        this.businessObjectService = businessObjectService;
    }

}
