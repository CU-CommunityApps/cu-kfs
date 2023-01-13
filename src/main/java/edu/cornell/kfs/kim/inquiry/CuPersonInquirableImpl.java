package edu.cornell.kfs.kim.inquiry;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.inquiry.PersonInquirableImpl;
import org.kuali.kfs.kim.service.UiDocumentService;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.kim.CuKimConstants.PersonInquirySections;
import edu.cornell.kfs.kim.CuKimPropertyConstants;
import edu.cornell.kfs.sys.CUKFSPropertyConstants;

@SuppressWarnings("deprecation")
public class CuPersonInquirableImpl extends PersonInquirableImpl {

    private static final Logger LOG = LogManager.getLogger();

    private static final Set<String> INQUIRY_SECTIONS_WITH_SUPPRESSED_FIELDS = Set.of(
            PersonInquirySections.NAMES,
            PersonInquirySections.ADDRESSES,
            PersonInquirySections.PHONE_NUMBERS,
            PersonInquirySections.EMAIL_ADDRESSES
    );

    private static final Set<String> UNMASKABLE_INQUIRY_FIELDS = Set.of(
            CuKimPropertyConstants.EntityName.NAME_PREFIX,
            CuKimPropertyConstants.EntityName.FIRST_NAME,
            CuKimPropertyConstants.EntityName.MIDDLE_NAME,
            CuKimPropertyConstants.EntityName.LAST_NAME,
            CuKimPropertyConstants.EntityName.NAME_SUFFIX,
            CuKimPropertyConstants.EntityAddress.LINE_1,
            CuKimPropertyConstants.EntityAddress.LINE_2,
            CuKimPropertyConstants.EntityAddress.LINE_3,
            CuKimPropertyConstants.EntityAddress.CITY,
            CuKimPropertyConstants.EntityAddress.STATE_PROVINCE_CODE,
            CuKimPropertyConstants.EntityAddress.POSTAL_CODE,
            CuKimPropertyConstants.SharedProperties.COUNTRY_CODE,
            CuKimPropertyConstants.EntityPhone.PHONE_NUMBER,
            CuKimPropertyConstants.EntityPhone.EXTENSION_NUMBER,
            CuKimPropertyConstants.EntityEmail.EMAIL_ADDRESS
    );

    private UiDocumentService uiDocumentService;

    @SuppressWarnings("rawtypes")
    @Override
    public BusinessObject getBusinessObject(Map fieldValues) {
        // KFSPTS-19308 CU customization to allow inquiry by principalName
        Person person = null;
        if (fieldValues.containsKey("principalId")) {
            person = getPersonService().getPerson(fieldValues.get("principalId").toString());
        }

        if (person == null && fieldValues.containsKey("principalName")) {
            String principalName = fieldValues.get("principalName").toString();
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
    public List<Section> getSections(BusinessObject businessObject) {
        List<Section> sections = super.getSections(businessObject);
        Person currentUser = GlobalVariables.getUserSession().getPerson();
        Person person = (Person) businessObject;
        if (getUiDocumentService().canOverrideEntityPrivacyPreferences(
                currentUser.getPrincipalId(), person.getPrincipalId())) {
            LOG.info("getSections, Current user can override privacy preferences for user " + person.getPrincipalName()
                    + ", will forcibly unmask inquiry fields that may have been suppressed by privacy preferences");
            modifyInquirySectionsToUnmaskPotentiallySuppressedPersonFields(sections, businessObject);
        }
        return sections;
    }

    private void modifyInquirySectionsToUnmaskPotentiallySuppressedPersonFields(
            List<Section> sections, BusinessObject businessObject) {
        sections.stream()
                .filter(section -> sectionContainsPotentiallySuppressedFields(section))
                .flatMap(section -> section.getRows().stream())
                .flatMap(row -> row.getFields().stream())
                .filter(field -> CollectionUtils.isNotEmpty(field.getContainerRows()))
                .flatMap(field -> field.getContainerRows().stream())
                .flatMap(row -> row.getFields().stream())
                .filter(field -> shouldForciblyUnmaskField(field))
                .forEach(field -> modifyFieldToDisplayUnmaskedValue(field, businessObject));
    }

    private boolean sectionContainsPotentiallySuppressedFields(Section section) {
        return INQUIRY_SECTIONS_WITH_SUPPRESSED_FIELDS.contains(section.getSectionTitle());
    }

    private boolean shouldForciblyUnmaskField(Field field) {
        String simplePropertyName = StringUtils.substringAfterLast(field.getPropertyName(), KFSConstants.DELIMITER);
        return StringUtils.isNotBlank(simplePropertyName) && UNMASKABLE_INQUIRY_FIELDS.contains(simplePropertyName);
    }

    private void modifyFieldToDisplayUnmaskedValue(Field field, BusinessObject businessObject) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("modifyFieldToDisplayUnmaskedValue, Forcibly unmasking field: " + field.getPropertyName());
        }
        String propertyNameForUnmaskedValue = field.getPropertyName()
                + CUKFSPropertyConstants.UNMASKED_PROPERTY_SUFFIX;
        Object unmaskedValue = ObjectUtils.getPropertyValue(businessObject, propertyNameForUnmaskedValue);
        field.setAlternateDisplayPropertyName(propertyNameForUnmaskedValue);
        field.setAlternateDisplayPropertyValue(unmaskedValue);
    }

    public UiDocumentService getUiDocumentService() {
        if (uiDocumentService == null) {
            uiDocumentService = SpringContext.getBean(UiDocumentService.class);
        }
        return uiDocumentService;
    }

}
