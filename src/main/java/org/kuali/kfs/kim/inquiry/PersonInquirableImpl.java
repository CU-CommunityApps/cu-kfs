/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.kim.inquiry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kim.impl.common.attribute.KimAttributeData;
import org.kuali.kfs.kim.impl.identity.PersonImpl;
import org.kuali.kfs.kim.impl.type.KimTypeAttribute;
import org.kuali.kfs.kns.datadictionary.InquirySectionDefinition;
import org.kuali.kfs.kns.inquiry.InquiryRestrictions;
import org.kuali.kfs.kns.inquiry.KualiInquirableImpl;
import org.kuali.kfs.kns.lookup.HtmlData;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.kns.web.ui.SectionBridge;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.kim.api.identity.Person;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.krad.bo.BusinessObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class PersonInquirableImpl extends KualiInquirableImpl {

    private static final Logger LOG = LogManager.getLogger();
    private static final Map<String, String> SUB_SECTION_LABELS = Map.ofEntries(
            entry("rolesSection", "Role Qualifier"),
            entry("delegationsSection", "Delegation Member Qualifier")
    );

    PersonService personService;

    public PersonInquirableImpl() {
        super();
        initiateInactiveRecordsDisplay();
    }

    /**
     * By default, the children record collections will hide inactive records
     */
    private void initiateInactiveRecordsDisplay() {
        inactiveRecordDisplay.put("groupMembers", Boolean.FALSE);
        inactiveRecordDisplay.put("roleMembers", Boolean.FALSE);
        inactiveRecordDisplay.put("delegateMembers", Boolean.FALSE);
    }

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

        if (person != null && person instanceof PersonImpl) {
            ((PersonImpl) person).populateMembers();
        }
        return person;

    }

    @Override
    public HtmlData getInquiryUrl(BusinessObject businessObject, String attributeName, boolean forceInquiry) {
        List<String> primaryKeys = new ArrayList<>();
        primaryKeys.add("principalId");
        return getInquiryUrlForPrimaryKeys(PersonImpl.class, businessObject, primaryKeys, null);
    }

    /**
     * Add Role Qualifier Attributes to Roles section of Person Inquiry.
     */
    @Override
    public List<Section> getSections(BusinessObject businessObject) {
        List<Section> sections = new ArrayList<>();
        if (getBusinessObjectClass() == null) {
            LOG.error("Business object class not set in inquirable.");
            throw new RuntimeException("Business object class not set in inquirable.");
        }

        InquiryRestrictions inquiryRestrictions = KNSServiceLocator.getBusinessObjectAuthorizationService()
                .getInquiryRestrictions(businessObject, GlobalVariables.getUserSession().getPerson());

        Collection<InquirySectionDefinition> inquirySections = getBusinessObjectDictionaryService().getInquirySections(
                getBusinessObjectClass());
        for (InquirySectionDefinition inquirySection : inquirySections) {
            String sectionId = inquirySection.getId();
            if (!inquiryRestrictions.isHiddenSectionId(sectionId)) {
                Section section = SectionBridge.toSection(this, inquirySection, businessObject, inquiryRestrictions);
                if (StringUtils.equals(sectionId, "rolesSection")
                        || StringUtils.equals(sectionId, "delegationsSection")) {
                    int memberIndex = 0;
                    for (Row containerRow: section.getRows()) {
                        List<Row> containerRows = containerRow.getFields().get(0).getContainerRows();
                        Row lastRow = containerRows.get(containerRows.size() - 1);
                        // we want the new fields added before any sub-sections (i.e. container fields)
                        if (Field.CONTAINER.equals(lastRow.getField(0).getFieldType())) {
                            containerRows.addAll(containerRows.size() - 1,
                                    buildRowsWithQualifierFields(sectionId, (PersonImpl) businessObject, memberIndex));
                        } else {
                            containerRows.addAll(buildRowsWithQualifierFields(sectionId, (PersonImpl) businessObject,
                                    memberIndex));
                        }
                        memberIndex++;
                    }
                }
                sections.add(section);
            }
        }

        return sections;
    }

    private List<Row> buildRowsWithQualifierFields(String sectionId, PersonImpl person, int memberIndex) {
        List<KimTypeAttribute> attributeDefinitions;
        List<KimAttributeData> attributeDetails;
        if (StringUtils.equals(sectionId, "rolesSection")) {
            attributeDefinitions = person.getRoleMembers().get(memberIndex).getRole().getKimType()
                    .getAttributeDefinitions();
            attributeDetails = new ArrayList<>(person.getRoleMembers().get(memberIndex).getAttributeDetails());
        } else {
            attributeDefinitions = person.getDelegateMembers().get(memberIndex).getRoleMember()
                    .getRole().getKimType().getAttributeDefinitions();
            attributeDetails = new ArrayList<>(person.getDelegateMembers().get(memberIndex).getAttributeDetails());
        }
        Map<String, Field> fieldsToAdd = new LinkedHashMap<>(buildQualifierAttributeFieldMap(attributeDefinitions));
        setFieldValuesForMember(attributeDetails, fieldsToAdd);
        List<Row> qualifierRows = fieldsToAdd.values().stream().map(Row::new).collect(Collectors.toList());
        if (!fieldsToAdd.isEmpty()) {
            qualifierRows.add(0, new Row(buildSeparatorField(SUB_SECTION_LABELS.get(sectionId))));
        }
        return qualifierRows;
    }

    private Map<String, Field> buildQualifierAttributeFieldMap(List<KimTypeAttribute> attributeDefinitions) {
        Map<String, Field> fieldsToAdd = new LinkedHashMap<>();
        attributeDefinitions.sort(Comparator.comparing(KimTypeAttribute::getSortCode));
        attributeDefinitions.stream().map(KimTypeAttribute::getKimAttribute).forEach(kimAttribute -> {
            String componentName = kimAttribute.getComponentName();
            String attributeName = kimAttribute.getAttributeName();
            DataDictionaryService dataDictionaryService = getDataDictionaryService();
            Field newField = new Field();
            newField.setFieldLabel(dataDictionaryService.getAttributeLabel(componentName, attributeName));
            newField.setFieldShortLabel(dataDictionaryService.getAttributeShortLabel(componentName, attributeName));
            newField.setPropertyName(attributeName);
            newField.setFieldType(Field.TEXT);
            fieldsToAdd.put(attributeName, newField);
        });
        return fieldsToAdd;
    }

    private void setFieldValuesForMember(List<KimAttributeData> attributeDetails, Map<String, Field> fieldsToAdd) {
        attributeDetails.forEach(memberAttributeData -> fieldsToAdd
                .get(memberAttributeData.getKimAttribute().getAttributeName())
                .setPropertyValue(memberAttributeData.getAttributeValue()));
    }

    private Field buildSeparatorField(String fieldLabel) {
        Field separatorField = new Field();
        separatorField.setFieldLabel(fieldLabel);
        separatorField.setFieldType(Field.SUB_SECTION_SEPARATOR);
        separatorField.setReadOnly(true);
        return separatorField;
    }

    public PersonService getPersonService() {
        if (personService == null) {
            personService = KimApiServiceLocator.getPersonService();
        }
        return personService;
    }
}
