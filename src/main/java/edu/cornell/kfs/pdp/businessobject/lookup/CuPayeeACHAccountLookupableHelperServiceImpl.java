package edu.cornell.kfs.pdp.businessobject.lookup;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.pdp.PdpConstants.PayeeIdTypeCodes;
import org.kuali.kfs.pdp.PdpPropertyConstants;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.businessobject.lookup.PayeeACHAccountLookupableHelperServiceImpl;
import org.kuali.kfs.core.api.criteria.CriteriaLookupService;
import org.kuali.kfs.core.api.criteria.Predicate;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.PredicateUtils;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kns.lookup.LookupUtils;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.BeanPropertyComparator;
import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.pdp.CUPdpPropertyConstants;

/**
 * Custom override of AbstractPayeeLookupableHelperService that performs special handling
 * when the new principal name search field is given a value.
 * 
 * NOTE: This lookupable does not extend from PayeeACHAccountLookupableHelperServiceImpl,
 * because that implementation removes inquiry hyperlinks in a not-so-configurable manner,
 * and we can at least prevent the inquiries from being opened via KIM permissions instead.
 */
@SuppressWarnings("deprecation")
public class CuPayeeACHAccountLookupableHelperServiceImpl extends PayeeACHAccountLookupableHelperServiceImpl {
    private static final long serialVersionUID = 9164050782113012442L;

    private PersonService personService;
    private CriteriaLookupService criteriaLookupService;

    /**
     * Overridden to perform custom searching when a principal name is specified on the search screen.
     * 
     * @see org.kuali.kfs.kns.lookup.KualiLookupableHelperServiceImpl#getSearchResultsHelper(java.util.Map, boolean)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected List<? extends BusinessObject> getSearchResultsHelper(Map<String,String> fieldValues, boolean unbounded) {
        if (StringUtils.isNotBlank(fieldValues.get(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME))) {
            List<PayeeACHAccount> results = null;
            
            // Search for people with the given principal name(s), in a manner that respects lookup criteria Strings.
            List<Person> people = personService.findPeople(Collections.singletonMap(
                    KIMPropertyConstants.Principal.PRINCIPAL_NAME, fieldValues.get(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME)));
            if (!people.isEmpty()) {
                // Get the users' entity IDs and employee IDs for searching.
                List<String> entityIds = new ArrayList<String>();
                List<String> employeeIds = new ArrayList<String>();
                for (Person person : people) {
                    entityIds.add(person.getEntityId());
                    if (StringUtils.isNotBlank(person.getEmployeeId())) {
                        employeeIds.add(person.getEmployeeId());
                    }
                }
                
                // Create a map without blank values and with all encrypted values decrypted, similar to the ancestor class's logic.
                Map<String,String> finalFieldValues = new HashMap<String,String>();
                for (Map.Entry<String,String> entry : fieldValues.entrySet()) {
                    // Only add non-blank values.
                    if (StringUtils.isBlank(entry.getValue())) {
                        // Do nothing.
                    } else if (entry.getValue().endsWith(EncryptionService.ENCRYPTION_POST_PREFIX)) {
                        // Decrypt encrypted values accordingly, as in the ancestor class.
                        String newValue = StringUtils.removeEnd(entry.getValue(), EncryptionService.ENCRYPTION_POST_PREFIX);
                        if (getEncryptionService().isEnabled()) {
                            try {
                                newValue = getEncryptionService().decrypt(newValue);
                            } catch (GeneralSecurityException e) {
                                throw new RuntimeException("Error decrypting Payee ACH Account attribute value", e);
                            }
                        }
                        finalFieldValues.put(entry.getKey(), newValue);
                    } else {
                        finalFieldValues.put(entry.getKey(), entry.getValue());
                    }
                }
                
                // Remove "payeePrincipalName" from the map, along with any hidden or non-BO-property-related entries (like back location).
                LookupUtils.removeHiddenCriteriaFields(getBusinessObjectClass(), finalFieldValues);
                finalFieldValues.remove(CUPdpPropertyConstants.PAYEE_PRINCIPAL_NAME);
                finalFieldValues.remove(KRADConstants.BACK_LOCATION);
                finalFieldValues.remove(KRADConstants.DOC_FORM_KEY);
                finalFieldValues.remove(KRADConstants.REFERENCES_TO_REFRESH);
                
                // Build the sub-predicate to limit by the entity or employee IDs for the given principal names.
                Predicate principalNameEquivalentPredicate;
                if (employeeIds.isEmpty()) {
                    principalNameEquivalentPredicate = PredicateFactory.and(
                            PredicateFactory.equal(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, PayeeIdTypeCodes.ENTITY),
                            PredicateFactory.in(PdpPropertyConstants.PAYEE_ID_NUMBER, entityIds.toArray(new String[entityIds.size()]))
                    );
                } else {
                    principalNameEquivalentPredicate = PredicateFactory.or(
                            PredicateFactory.and(
                                    PredicateFactory.equal(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, PayeeIdTypeCodes.ENTITY),
                                    PredicateFactory.in(PdpPropertyConstants.PAYEE_ID_NUMBER, entityIds.toArray(new String[entityIds.size()]))
                            ),
                            PredicateFactory.and(
                                    PredicateFactory.equal(PdpPropertyConstants.PAYEE_IDENTIFIER_TYPE_CODE, PayeeIdTypeCodes.EMPLOYEE),
                                    PredicateFactory.in(PdpPropertyConstants.PAYEE_ID_NUMBER, employeeIds.toArray(new String[employeeIds.size()]))
                            )
                    );
                }
                
                // Build the criteria and run the search.
                QueryByCriteria.Builder crit = QueryByCriteria.Builder.create();
                if (!unbounded) {
                    crit.setMaxResults(LookupUtils.getSearchResultsLimit(getBusinessObjectClass()));
                }
                if (!finalFieldValues.isEmpty()) {
                    crit.setPredicates(PredicateUtils.convertMapToPredicate(finalFieldValues), principalNameEquivalentPredicate);
                } else {
                    crit.setPredicates(principalNameEquivalentPredicate);
                }
                results = criteriaLookupService.lookup(getBusinessObjectClass(), crit.build()).getResults();
                
                // Move results to a mutable list, since the result list from CriteriaLookupService is immutable.
                results = new ArrayList<PayeeACHAccount>(results);
                
                // Sort results accordingly using code from the ancestor class's version of the method.
                List<String> defaultSortColumns = getDefaultSortColumns();
                if (defaultSortColumns.size() > 0) {
                    Collections.sort(results, new BeanPropertyComparator(defaultSortColumns, true));
                }
            }
            
            // If no people were found with the given principal names, then return an empty list accordingly; otherwise, return the results.
            return (results != null) ? results : new ArrayList<PayeeACHAccount>();
        } else {
            // If principal name is not specified, then do the normal superclass processing.
            return super.getSearchResultsHelper(fieldValues, unbounded);
        }
    }
    
    @Override
    protected boolean isInquiryRestricted(BusinessObject bo, String propertyName) {
        return false;
    }

    public PersonService getPersonService() {
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public CriteriaLookupService getCriteriaLookupService() {
        return criteriaLookupService;
    }

    public void setCriteriaLookupService(CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }

}
