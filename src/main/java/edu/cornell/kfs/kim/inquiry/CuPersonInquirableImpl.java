package edu.cornell.kfs.kim.inquiry;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.impl.KIMPropertyConstants;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.inquiry.PersonInquirableImpl;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.kim.CuKimConstants.PersonInquirySections;
import edu.cornell.kfs.kim.CuKimPropertyConstants;
import edu.cornell.kfs.kim.util.CuKimUtils;

@SuppressWarnings("deprecation")
public class CuPersonInquirableImpl extends PersonInquirableImpl {

    private static final Logger LOG = LogManager.getLogger();

    private static final Set<String> INQUIRY_SECTIONS_AFFECTED_BY_PRIVACY_PREFERENCES = Set.of(
            PersonInquirySections.NAME,
            PersonInquirySections.PHONE_NUMBER,
            PersonInquirySections.EMAIL_ADDRESS
    );

    private static final Set<String> FIELDS_AFFECTED_BY_PRIVACY_PREFERENCES = Set.of(
            KIMPropertyConstants.Person.FIRST_NAME + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX,
            CuKimPropertyConstants.MIDDLE_NAME + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX,
            KIMPropertyConstants.Person.LAST_NAME + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX,
            KRADPropertyConstants.EMAIL_ADDRESS + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX,
            KFSPropertyConstants.PHONE_NUMBER + CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX
    );

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

    @Override
    public List<Section> getSections(final BusinessObject businessObject) {
        final List<Section> sections = super.getSections(businessObject);
        final Person person = (Person) businessObject;
        if (CuKimUtils.currentUserIsPresentAndCanOverridePrivacyPreferencesForUser(person.getPrincipalId())) {
            LOG.info("getSections, Current user can override privacy preferences for user " + person.getPrincipalName()
                    + ", will forcibly unmask inquiry fields that may have been suppressed by privacy preferences");
            modifyInquirySectionsToUnmaskFieldsAffectedByPrivacyPreferences(sections, businessObject);
        }
        return sections;
    }

    private void modifyInquirySectionsToUnmaskFieldsAffectedByPrivacyPreferences(
            final List<Section> sections, 
            final BusinessObject businessObject) {
        sections.stream()
                .filter(section -> sectionContainsFieldsAffectedByPrivacyPreferences(section))
                .flatMap(section -> section.getRows().stream())
                .flatMap(row -> row.getFields().stream())
                .filter(field -> shouldForciblyUnmaskField(field))
                .forEach(field -> modifyFieldToDisplayUnmaskedValue(field, businessObject));
    }

    private boolean sectionContainsFieldsAffectedByPrivacyPreferences(final Section section) {
        String title = section.getSectionTitle();
        return StringUtils.isNotBlank(title) && INQUIRY_SECTIONS_AFFECTED_BY_PRIVACY_PREFERENCES.contains(title);
    }

    private boolean shouldForciblyUnmaskField(final Field field) {
        String propertyName = field.getPropertyName();
        return StringUtils.isNotBlank(propertyName) && FIELDS_AFFECTED_BY_PRIVACY_PREFERENCES.contains(propertyName);
    }

    private void modifyFieldToDisplayUnmaskedValue(
            final Field field, final BusinessObject businessObject) {
        LOG.debug("modifyFieldToDisplayUnmaskedValue, Forcibly unmasking field: {}", field::getPropertyName);
        final String propertyNameForMaskedValue = field.getPropertyName();
        final String propertyNameForUnmaskedValue = StringUtils.substringBeforeLast(
                propertyNameForMaskedValue, CuKimPropertyConstants.MASKED_IF_NECESSARY_SUFFIX);
        final Object unmaskedValue = ObjectUtils.getPropertyValue(businessObject, propertyNameForUnmaskedValue);
        field.setAlternateDisplayPropertyName(propertyNameForUnmaskedValue);
        field.setAlternateDisplayPropertyValue(unmaskedValue);
    }

}
