package edu.cornell.kfs.kim.batch.service.impl;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.criteria.CuPredicateFactory;
import org.kuali.kfs.core.api.criteria.GenericQueryResults;
import org.kuali.kfs.core.api.criteria.PredicateFactory;
import org.kuali.kfs.core.api.criteria.QueryByCriteria;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kim.api.KimConstants.EntityTypes;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.kim.CuKimConstants;
import edu.cornell.kfs.kim.CuKimConstants.AddressTypes;
import edu.cornell.kfs.kim.CuKimConstants.AffiliationStatuses;
import edu.cornell.kfs.kim.CuKimConstants.EdwAffiliations;
import edu.cornell.kfs.kim.CuKimConstants.KfsAffiliations;
import edu.cornell.kfs.kim.batch.dataaccess.KimFeedEdwDao;
import edu.cornell.kfs.kim.batch.service.KimFeedService;
import edu.cornell.kfs.kim.businessobject.EdwPerson;

public class KimFeedServiceImpl implements KimFeedService {

    private static final Logger LOG = LogManager.getLogger();

    private static final int ID_CHUNK_SIZE = 500;
    private static final String DISABLED_NETID_PREFIX = "DIS-";
    private static final String DISABLED_NETID_SEARCH_PATTERN = DISABLED_NETID_PREFIX + "%";
    private static final String UPPER_FUNC_FORMAT = "UPPER({0})";

    private static final List<String> AFFILIATION_STATUS_PRIORITIES = List.of(
            AffiliationStatuses.ACTIVE, AffiliationStatuses.INACTIVE, AffiliationStatuses.RETIRED);

    private KimFeedEdwDao kimFeedEdwDao;
    private PersonService personService;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    private BigDecimal dummyBaseSalaryAmount;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void processPersonDataChanges() {
        LOG.info("processPersonDataChanges, Starting Execution of KIM Data Changes");
        try (
            Stream<EdwPerson> personChanges = kimFeedEdwDao.getEdwDataAsCloseableStream();
        ) {
            KimFeedReport report = new KimFeedReport();
            for (Iterator<EdwPerson> personIter = personChanges.iterator(); personIter.hasNext();) {
                processPersonChange(report, personIter.next());
            }
            LOG.info("processPersonDataChanges, Finished Executing KIM Data Changes");
            LOG.info("processPersonDataChanges, Total Changes: {}", report.rowsProcessed);
            LOG.info("processPersonDataChanges, Successful Changes: {}", report.rowsSuccessful);
            LOG.info("processPersonDataChanges, Failed Changes: {}", report.rowsWithErrors);
        } catch (Exception e) {
            LOG.error("processPersonDataChanges, Unexpected error encountered during processing", e);
            throw e;
        }
    }

    private void processPersonChange(KimFeedReport report, EdwPerson edwPerson) {
        try {
            report.rowsProcessed++;
            verifyPersonHasRequiredIdentifiers(edwPerson);
            Person kfsPerson = getPersonByPrincipalNameIgnoringCache(edwPerson.getNetId());
            if (ObjectUtils.isNotNull(kfsPerson)) {
                LOG.info("processPersonChange, Updating KFS Person record for {}", edwPerson);
                verifyExistingPersonHasExpectedIdentifiers(kfsPerson, edwPerson);
            } else {
                LOG.info("processPersonChange, Inserting new KFS Person record for {}", edwPerson);
                verifyNewPersonCanBeSafelyInsertedIntoKfs(edwPerson);
                kfsPerson = new Person();
            }
            mergePersonIntoKfs(kfsPerson, edwPerson);
            report.rowsSuccessful++;
        } catch (RuntimeException e) {
            LOG.error("processPersonChange, Unable to insert or update {}", edwPerson, e);
            report.rowsWithErrors++;
        }
    }

    private void verifyPersonHasRequiredIdentifiers(EdwPerson edwPerson) {
        if (StringUtils.isBlank(edwPerson.getNetId())) {
            throw new RuntimeException("No NetID was defined for user: " + edwPerson);
        } else if (StringUtils.isBlank(edwPerson.getCuPersonId())) {
            throw new RuntimeException("No CU Person ID was defined for user: " + edwPerson);
        }
    }

    private Person getPersonByPrincipalNameIgnoringCache(String principalName) {
        QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.equal(KIMPropertyConstants.Principal.PRINCIPAL_NAME, principalName));
        GenericQueryResults<Person> queryResults = personService.findPeople(criteria);
        List<Person> people = queryResults.getResults();
        if (people.isEmpty()) {
            return null;
        } else if (people.size() > 1) {
            throw new RuntimeException("Found multiple people with Principal Name: " + principalName);
        } else {
            return people.get(0);
        }
    }

    private void verifyNewPersonCanBeSafelyInsertedIntoKfs(EdwPerson edwPerson) {
        QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(
                PredicateFactory.or(
                        PredicateFactory.equal(KIMPropertyConstants.Principal.PRINCIPAL_ID, edwPerson.getCuPersonId()),
                        PredicateFactory.equal(KIMPropertyConstants.Person.ENTITY_ID, edwPerson.getCuPersonId())
                ),
                CuPredicateFactory.notEqual(KIMPropertyConstants.Principal.PRINCIPAL_NAME, edwPerson.getNetId()));
        GenericQueryResults<Person> queryResults = personService.findPeople(criteria);
        if (!queryResults.getResults().isEmpty()) {
            throw new RuntimeException("One or more KFS users have a Principal or Entity ID of "
                    + edwPerson.getCuPersonId() + " but do not have a Principal Name of " + edwPerson.getNetId());
        }
    }

    private void verifyExistingPersonHasExpectedIdentifiers(Person kfsPerson, EdwPerson edwPerson) {
        if (!StringUtils.equals(edwPerson.getCuPersonId(), kfsPerson.getPrincipalId())
                || !StringUtils.equals(edwPerson.getCuPersonId(), kfsPerson.getEntityId())) {
            throw new RuntimeException("KFS user with principal ID " + kfsPerson.getPrincipalId()
                        + " and entity ID " + kfsPerson.getEntityId() + " does not match EDW row " + edwPerson);
        }
    }

    private void mergePersonIntoKfs(Person kfsPerson, EdwPerson edwPerson) {
        if (StringUtils.isBlank(kfsPerson.getPrincipalId())) {
            kfsPerson.setPrincipalId(edwPerson.getCuPersonId());
            kfsPerson.setEntityId(edwPerson.getCuPersonId());
            kfsPerson.setPrincipalName(edwPerson.getNetId());
            kfsPerson.setEntityTypeCode(EntityTypes.PERSON);
        }
        mergeAffiliationsAndEmploymentInformation(kfsPerson, edwPerson);
        mergeName(kfsPerson, edwPerson);
        mergeHomeAddress(kfsPerson, edwPerson);
        mergeCampusAddress(kfsPerson, edwPerson);
        mergePrivacyPreferences(kfsPerson, edwPerson);
        mergeMiscellaneousPersonData(kfsPerson, edwPerson);
        businessObjectService.save(kfsPerson);
    }

    private void mergeAffiliationsAndEmploymentInformation(Person kfsPerson, EdwPerson edwPerson) {
        kfsPerson.setAffiliationTypeCode(convertPrimaryAffiliationCode(edwPerson));
        kfsPerson.setCampusCode(CuKimConstants.CORNELL_IT_CAMPUS);
        kfsPerson.setAcademicAffiliation(getAndVerifyAffiliationStatus(edwPerson, EdwPerson::getAcademicAffil));
        kfsPerson.setAffiliateAffiliation(getAndVerifyAffiliationStatus(edwPerson, EdwPerson::getAffiliateAffil));
        kfsPerson.setAlumniAffiliation(getAndVerifyAffiliationStatus(edwPerson, EdwPerson::getAlumniAffil));
        kfsPerson.setExceptionAffiliation(getAndVerifyAffiliationStatus(edwPerson, EdwPerson::getExceptionAffil));
        kfsPerson.setFacultyAffiliation(getAndVerifyAffiliationStatus(edwPerson, EdwPerson::getFacultyAffil));
        kfsPerson.setStaffAffiliation(getAndVerifyAffiliationStatus(edwPerson, EdwPerson::getStaffAffil));
        kfsPerson.setStudentAffiliation(getAndVerifyAffiliationStatus(edwPerson, EdwPerson::getStudentAffil));

        String primaryEmploymentAffiliation = getPrimaryEmploymentAffiliation(edwPerson);
        if (StringUtils.isNotBlank(primaryEmploymentAffiliation)) {
            kfsPerson.setPrimaryDepartmentCode(getPrimaryDepartmentCode(edwPerson));
            kfsPerson.setEmployeeId(edwPerson.getEmployeeId());
            kfsPerson.setEmployeeStatusCode(
                    getPrimaryEmploymentAffiliationStatus(edwPerson, primaryEmploymentAffiliation));
            kfsPerson.setEmployeeTypeCode(KFSConstants.PROFESSIONAL_EMPLOYEE_TYPE_CODE);
        } else {
            kfsPerson.setPrimaryDepartmentCode(null);
            kfsPerson.setEmployeeId(null);
            kfsPerson.setEmployeeStatusCode(null);
            kfsPerson.setEmployeeTypeCode(null);
        }
    }

    private String convertPrimaryAffiliationCode(EdwPerson edwPerson) {
        String edwAffiliation = StringUtils.defaultString(edwPerson.getPrimaryAffiliation());
        switch (edwAffiliation) {
            case EdwAffiliations.ACADEMIC:
                return KfsAffiliations.ACADEMIC;
            case EdwAffiliations.AFFILIATE:
                return KfsAffiliations.AFFILIATE;
            case EdwAffiliations.ALUMNI:
                return KfsAffiliations.ALUMNI;
            case EdwAffiliations.EXCEPTION:
                return KfsAffiliations.EXCEPTION;
            case EdwAffiliations.FACULTY:
                return KfsAffiliations.FACULTY;
            case EdwAffiliations.STAFF:
                return KfsAffiliations.STAFF;
            case EdwAffiliations.STUDENT:
                return KfsAffiliations.STUDENT;
            default:
                return KfsAffiliations.NONE;
        }
    }

    private String getAndVerifyAffiliationStatus(EdwPerson edwPerson, Function<EdwPerson, String> statusGetter) {
        String affiliationStatus = StringUtils.defaultString(statusGetter.apply(edwPerson));
        switch (affiliationStatus) {
            case AffiliationStatuses.ACTIVE:
            case AffiliationStatuses.INACTIVE:
            case AffiliationStatuses.RETIRED:
            case AffiliationStatuses.NONEXISTENT:
                return affiliationStatus;
            default:
                throw new RuntimeException("Found an invalid affiliation status of '" + affiliationStatus
                        + "' on " + edwPerson);
        }
    }

    private String getPrimaryEmploymentAffiliation(EdwPerson edwPerson) {
        String edwAffiliation = StringUtils.defaultString(edwPerson.getPrimaryAffiliation());
        switch (edwAffiliation) {
            case EdwAffiliations.AFFILIATE:
                return KfsAffiliations.AFFILIATE;
            case EdwAffiliations.FACULTY:
                return KfsAffiliations.FACULTY;
            case EdwAffiliations.STAFF:
                return KfsAffiliations.STAFF;
            default:
                for (String affiliationStatus : AFFILIATION_STATUS_PRIORITIES) {
                    if (StringUtils.equals(edwPerson.getFacultyAffil(), affiliationStatus)) {
                        return KfsAffiliations.FACULTY;
                    } else if (StringUtils.equals(edwPerson.getStaffAffil(), affiliationStatus)) {
                        return KfsAffiliations.STAFF;
                    } else if (StringUtils.equals(edwPerson.getAffiliateAffil(), affiliationStatus)) {
                        return KfsAffiliations.AFFILIATE;
                    }
                }
                return null;
        }
    }

    private String getPrimaryEmploymentAffiliationStatus(EdwPerson edwPerson, String primaryEmploymentAffiliation) {
        switch (primaryEmploymentAffiliation) {
            case KfsAffiliations.AFFILIATE:
                return getAndVerifyAffiliationStatus(edwPerson, EdwPerson::getAffiliateAffil);
            case KfsAffiliations.FACULTY:
                return getAndVerifyAffiliationStatus(edwPerson, EdwPerson::getFacultyAffil);
            case KfsAffiliations.STAFF:
                return getAndVerifyAffiliationStatus(edwPerson, EdwPerson::getStaffAffil);
            default :
                throw new RuntimeException("Invalid primary employment affiliation '" + primaryEmploymentAffiliation
                        + "' was detected for " + edwPerson);
        }
    }

    private String getPrimaryDepartmentCode(EdwPerson edwPerson) {
        String orgCode = edwPerson.getPrimaryOrgCode();
        if (StringUtils.isBlank(orgCode)) {
            LOG.warn("getPrimaryDepartmentCode, Entry for user {} has a blank org/department code; defaulting to {}",
                    edwPerson.getNetId(), CuKimConstants.DEPARTMENT_CODE_PREFIX);
            return CuKimConstants.DEPARTMENT_CODE_PREFIX;
        } else {
            return CuKimConstants.DEPARTMENT_CODE_PREFIX + orgCode;
        }
    }

    private void mergeName(Person kfsPerson, EdwPerson edwPerson) {
        kfsPerson.setFirstName(edwPerson.getFirstName());
        kfsPerson.setMiddleName(edwPerson.getMiddleName());
        kfsPerson.setLastName(edwPerson.getLastName());
    }

    private void mergeHomeAddress(Person kfsPerson, EdwPerson edwPerson) {
        String homeCountryCode = StringUtils.left(edwPerson.getHomeCountryCode(), 2);
        String line1 = edwPerson.getHomeAddressLine1();
        String line2 = edwPerson.getHomeAddressLine2();
        String line3 = edwPerson.getHomeAddressLine3();
        
        if (StringUtils.length(line1) > CuKimConstants.MAX_ADDRESS_LINE_LENGTH) {
            if (StringUtils.isBlank(line2)) {
                line2 = StringUtils.substring(line1, CuKimConstants.MAX_ADDRESS_LINE_LENGTH);
            }
            line1 = StringUtils.left(line1, CuKimConstants.MAX_ADDRESS_LINE_LENGTH);
        }

        if (StringUtils.length(line2) > CuKimConstants.MAX_ADDRESS_LINE_LENGTH) {
            if (StringUtils.isBlank(line3)) {
                line3 = StringUtils.substring(line2, CuKimConstants.MAX_ADDRESS_LINE_LENGTH);
            }
            line2 = StringUtils.left(line2, CuKimConstants.MAX_ADDRESS_LINE_LENGTH);
        }

        if (StringUtils.length(line3) > CuKimConstants.MAX_ADDRESS_LINE_LENGTH) {
            line3 = StringUtils.left(line3, CuKimConstants.MAX_ADDRESS_LINE_LENGTH);
        }

        kfsPerson.setAddressTypeCode(AddressTypes.HOME);
        kfsPerson.setAddressLine1(line1);
        kfsPerson.setAddressLine2(line2);
        kfsPerson.setAddressLine3(line3);
        kfsPerson.setAddressCity(edwPerson.getHomeCity());
        kfsPerson.setAddressStateProvinceCode(StringUtils.left(edwPerson.getHomeState(), 2));
        kfsPerson.setAddressPostalCode(edwPerson.getHomePostalCode());
        kfsPerson.setAddressCountryCode(homeCountryCode);
    }

    private void mergeCampusAddress(Person kfsPerson, EdwPerson edwPerson) {
        String line1 = edwPerson.getCampusAddress();
        String line2 = StringUtils.length(line1) > CuKimConstants.MAX_ADDRESS_LINE_LENGTH
                ? StringUtils.substring(line1, CuKimConstants.MAX_ADDRESS_LINE_LENGTH) : KFSConstants.BLANK_SPACE;
        String line3 = StringUtils.length(line2) > CuKimConstants.MAX_ADDRESS_LINE_LENGTH
                ? StringUtils.substring(line2, CuKimConstants.MAX_ADDRESS_LINE_LENGTH) : KFSConstants.BLANK_SPACE;
        line1 = StringUtils.left(line1, CuKimConstants.MAX_ADDRESS_LINE_LENGTH);
        line2 = StringUtils.left(line2, CuKimConstants.MAX_ADDRESS_LINE_LENGTH);
        line3 = StringUtils.left(line3, CuKimConstants.MAX_ADDRESS_LINE_LENGTH);

        kfsPerson.setAltAddressTypeCode(AddressTypes.CAMPUS);
        kfsPerson.setAltAddressLine1(line1);
        kfsPerson.setAltAddressLine2(line2);
        kfsPerson.setAltAddressLine3(line3);
        kfsPerson.setAltAddressCity(edwPerson.getCampusCity());
        kfsPerson.setAltAddressStateProvinceCode(StringUtils.left(edwPerson.getCampusState(), 2));
        kfsPerson.setAltAddressPostalCode(edwPerson.getCampusPostalCode());
        kfsPerson.setAltAddressCountryCode(KFSConstants.BLANK_SPACE);
    }

    private void mergePrivacyPreferences(Person kfsPerson, EdwPerson edwPerson) {
        if (StringUtils.equalsIgnoreCase(edwPerson.getLdapSuppress(), KRADConstants.YES_INDICATOR_VALUE)) {
            kfsPerson.setSuppressName(true);
            kfsPerson.setSuppressEmail(true);
            kfsPerson.setSuppressPhone(true);
            kfsPerson.setSuppressPersonal(true);
        }
    }

    private void mergeMiscellaneousPersonData(Person kfsPerson, EdwPerson edwPerson) {
        kfsPerson.setEmailAddress(edwPerson.getEmailAddress());
        kfsPerson.setPhoneNumber(edwPerson.getCampusPhone());
        kfsPerson.setBaseSalaryAmount(new KualiDecimal(dummyBaseSalaryAmount));
        kfsPerson.setTaxId(null);
        kfsPerson.setActive(true);
        kfsPerson.setLastUpdatedTimestamp(dateTimeService.getCurrentTimestamp());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void processPersonDataMarkedForDisabling() {
        List<String> principalIdsToDisable = kimFeedEdwDao.getIdsOfPersonsToMarkAsDisabled();
        if (CollectionUtils.isNotEmpty(principalIdsToDisable)) {
            LOG.info("processPersonDataMarkedForDisabling, Marking the following users as disabled: {}",
                    principalIdsToDisable);
            markPeopleAsDisabled(principalIdsToDisable);
            LOG.info("processPersonDataMarkedForDisabling, Complete!");
        } else {
            LOG.info("processPersonDataMarkedForDisabling, No users were marked for disabling");
        }
    }

    private void markPeopleAsDisabled(List<String> principalIds) {
        int startIndex = 0;
        do {
            int endIndex = Math.min(startIndex + ID_CHUNK_SIZE, principalIds.size());
            List<String> subIds = principalIds.subList(startIndex, endIndex);
            GenericQueryResults<Person> results = personService.findPeople(QueryByCriteria.Builder.fromPredicates(
                    PredicateFactory.in(KIMPropertyConstants.Principal.PRINCIPAL_ID, subIds),
                    CuPredicateFactory.notLike(
                            getUppercasedPropertyExpression(KIMPropertyConstants.Principal.PRINCIPAL_NAME),
                            DISABLED_NETID_SEARCH_PATTERN)));
            List<Person> usersToUpdate = results.getResults();
            if (!usersToUpdate.isEmpty()) {
                for (Person person : usersToUpdate) {
                    person.setPrincipalName(DISABLED_NETID_PREFIX + person.getPrincipalName());
                    person.setLastUpdatedTimestamp(dateTimeService.getCurrentTimestamp());
                }
                businessObjectService.save(usersToUpdate);
            }
            startIndex += ID_CHUNK_SIZE;
        } while (startIndex < principalIds.size());
    }

    private String getUppercasedPropertyExpression(String propertyName) {
        return MessageFormat.format(UPPER_FUNC_FORMAT, propertyName);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void markPersonDataChangesAsRead() {
        int numRowsMarked = kimFeedEdwDao.markEdwDataAsRead();
        LOG.info("markPersonDataChangesAsRead, Successfully marked {} EDW rows as read", numRowsMarked);
    }

    @CacheEvict(value = { Person.CACHE_NAME }, allEntries = true)
    @Override
    public void flushPersonCache() {
        LOG.info("flushPersonCache, Cleared out cached KFS Person data");
    }

    public void setKimFeedEdwDao(KimFeedEdwDao kimFeedEdwDao) {
        this.kimFeedEdwDao = kimFeedEdwDao;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setDummyBaseSalaryAmount(BigDecimal dummyBaseSalaryAmount) {
        this.dummyBaseSalaryAmount = dummyBaseSalaryAmount;
    }

    private static final class KimFeedReport {
        private int rowsProcessed;
        private int rowsSuccessful;
        private int rowsWithErrors;
    }

}
