package edu.cornell.kfs.cemi.module.purap.batch.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.Predicate;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.kim.CuKimPropertyConstants;

/*
 * Performs the Person lookups needed by the PO extract, caching the results for the duration of the job run.
 * Many POs share the same requestors and delivery recipients, so caching avoids repeating identical
 * Person searches for each PO.
 */
public class CemiPurchaseOrderEmployeeIdLookup {

    private static final Logger LOG = LogManager.getLogger();

    private enum LookupOutcome {
        INSUFFICIENT_DATA,
        NOT_FOUND,
        MULTIPLE_FOUND,
        FOUND
    }

    private final PersonService personService;
    private final Map<Pair<String, String>, Pair<LookupOutcome, String>> employeeIdsByNameAndEmail;
    private String defaultBuyerEmployeeId;

    public CemiPurchaseOrderEmployeeIdLookup(final PersonService personService) {
        Validate.notNull(personService, "personService cannot be null");
        this.personService = personService;
        this.employeeIdsByNameAndEmail = new HashMap<>();
    }

    /*
     * Certain people mentioned on the PO (such as the requestor) are only identified by name, phone and/or email,
     * not by Principal ID or Principal Name. Thus, the only practical way to find the corresponding Person record
     * is to perform a search based on name and email. (The code below currently doesn't search by phone number,
     * due to the complexities of stripping out non-digit characters in order to perform an accurate search.)
     */
    public String getEmployeeIdByNameAndEmail(final String personName, final String emailAddress,
            final String label, final String documentNumber) {
        final Pair<LookupOutcome, String> result = employeeIdsByNameAndEmail.computeIfAbsent(
                Pair.of(personName, emailAddress), key -> lookUpEmployeeIdByNameAndEmail(personName, emailAddress));

        switch (result.getLeft()) {
            case INSUFFICIENT_DATA:
                LOG.warn("getEmployeeIdByNameAndEmail, Insufficient {} information was available on PO document "
                        + "number {}; will return an empty employee ID", label, documentNumber);
                break;
            case NOT_FOUND:
                LOG.warn("getEmployeeIdByNameAndEmail, Could not find a Person record for the {} on PO document "
                        + "number {}; will return an empty employee ID", label, documentNumber);
                break;
            case MULTIPLE_FOUND:
                LOG.warn("getEmployeeIdByNameAndEmail, Found multiple Person records for the {} on PO document "
                        + "number {}; will return an empty employee ID", label, documentNumber);
                break;
            default:
                break;
        }

        return result.getRight();
    }

    public String getDefaultBuyerEmployeeId() {
        if (defaultBuyerEmployeeId == null) {
            final Person defaultBuyer = personService.getPersonByPrincipalName(
                    CemiPurchaseOrderConstants.DEFAULT_BUYER_PRINCIPAL_NAME);
            if (ObjectUtils.isNotNull(defaultBuyer) && StringUtils.isNotBlank(defaultBuyer.getEmployeeId())) {
                defaultBuyerEmployeeId = defaultBuyer.getEmployeeId();
            } else {
                LOG.warn("getDefaultBuyerEmployeeId, Default buyer {} does not exist or has no employee ID; "
                        + "defaulting to a blank buyer employee ID",
                        CemiPurchaseOrderConstants.DEFAULT_BUYER_PRINCIPAL_NAME);
                defaultBuyerEmployeeId = CemiBaseConstants.EMPTY_STRING;
            }
        }
        return defaultBuyerEmployeeId;
    }

    private Pair<LookupOutcome, String> lookUpEmployeeIdByNameAndEmail(final String personName,
            final String emailAddress) {
        final Predicate[] criteria = createCriteriaForPersonNameAndEmailQuery(personName, emailAddress);
        if (criteria.length == 0) {
            return Pair.of(LookupOutcome.INSUFFICIENT_DATA, CemiBaseConstants.EMPTY_STRING);
        }

        final QueryByCriteria query = QueryByCriteria.Builder.fromPredicates(criteria);
        final GenericQueryResults<Person> results = personService.findPeople(query);
        final List<Person> dataResults = results.getResults();

        final int numResults = dataResults.size();
        if (numResults == 0) {
            return Pair.of(LookupOutcome.NOT_FOUND, CemiBaseConstants.EMPTY_STRING);
        } else if (numResults != 1) {
            return Pair.of(LookupOutcome.MULTIPLE_FOUND, CemiBaseConstants.EMPTY_STRING);
        } else {
            final Person matchingPerson = dataResults.get(0);
            return Pair.of(LookupOutcome.FOUND,
                    StringUtils.defaultIfBlank(matchingPerson.getEmployeeId(), CemiBaseConstants.EMPTY_STRING));
        }
    }

    private Predicate[] createCriteriaForPersonNameAndEmailQuery(final String personName, final String emailAddress) {
        final List<Pair<String, String>> personNameCriteria = getPersonNameCriteria(personName);
        if (personNameCriteria.isEmpty() || StringUtils.isBlank(emailAddress)) {
            LOG.debug("createCriteriaForPersonNameAndEmailQuery, The PO contained insufficient name and/or email data "
                    + "to perform an accurate query; will return an empty criteria array");
            return new Predicate[0];
        }

        final List<Pair<String, String>> emailCriteria = List.of(
                Pair.of(KRADPropertyConstants.EMAIL_ADDRESS, emailAddress));

        return Stream.of(personNameCriteria, emailCriteria)
                .flatMap(List::stream)
                .map(criterion -> PredicateFactory.equalIgnoreCase(criterion.getLeft(), criterion.getRight()))
                .toArray(Predicate[]::new);
    }

    private List<Pair<String, String>> getPersonNameCriteria(final String personName) {
        final List<String> personNameFieldNames = List.of(KIMPropertyConstants.Person.FIRST_NAME,
                CuKimPropertyConstants.MIDDLE_NAME, KIMPropertyConstants.Person.LAST_NAME);
        final List<String> personNameSegments = getPersonNameSegments(personName);
        final long numValidNameSegments = personNameSegments.stream()
                .filter(StringUtils::isNotBlank)
                .count();

        if (numValidNameSegments >= 2L) {
            return IntStream.range(0, 3)
                    .filter(index -> StringUtils.isNotBlank(personNameSegments.get(index)))
                    .mapToObj(index -> Pair.of(personNameFieldNames.get(index), personNameSegments.get(index)))
                    .collect(Collectors.toUnmodifiableList());
        } else {
            LOG.debug("getPersonNameCriteria, First Name and/or Last Name are missing; will return an empty list "
                    + "of criteria because there's not enough name data to perform an accurate query");
            return List.of();
        }
    }

    private List<String> getPersonNameSegments(final String personName) {
        final String[] splitName = StringUtils.split(personName, KFSConstants.BLANK_SPACE);
        if (splitName != null && (splitName.length == 2 || splitName.length == 3)) {
            final String firstName;
            final String middleName;
            final String lastName;
            if (splitName[0].endsWith(KFSConstants.COMMA)) {
                lastName = StringUtils.substringBeforeLast(splitName[0], KFSConstants.COMMA);
                firstName = splitName[1];
                middleName = (splitName.length == 3) ? splitName[2] : CemiBaseConstants.EMPTY_STRING;
            } else {
                firstName = splitName[0];
                lastName = splitName[splitName.length - 1];
                middleName = (splitName.length == 3) ? splitName[1] : CemiBaseConstants.EMPTY_STRING;
            }
            return List.of(firstName, middleName, lastName);
        } else {
            LOG.debug("getPersonNameSegments, First Name and/or Last Name are missing; will return empty values "
                    + "to indicate that insufficient name data was provided on the PO");
            return CemiUtils.createListOfEmptyStrings(3);
        }
    }

}
