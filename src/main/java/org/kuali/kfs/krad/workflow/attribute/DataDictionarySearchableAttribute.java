/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.krad.workflow.attribute;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.CoreConstants;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.uif.AttributeError;
import org.kuali.kfs.datadictionary.legacy.MaintenanceDocumentDictionaryService;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttribute;
import org.kuali.kfs.kew.api.document.attribute.DocumentAttributeString;
import org.kuali.kfs.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.kfs.kew.framework.document.attribute.SearchableAttribute;
import org.kuali.kfs.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.kfs.kew.rule.bo.RuleAttribute;
import org.kuali.kfs.kns.datadictionary.DocumentEntry;
import org.kuali.kfs.kns.datadictionary.MaintainableFieldDefinition;
import org.kuali.kfs.kns.datadictionary.MaintenanceDocumentEntry;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.lookup.LookupUtils;
import org.kuali.kfs.kns.maintenance.GlobalMaintainableImpl;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.service.BusinessObjectMetaDataService;
import org.kuali.kfs.kns.service.DictionaryValidationService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.util.FieldUtils;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.datadictionary.SearchingAttribute;
import org.kuali.kfs.krad.datadictionary.SearchingTypeDefinition;
import org.kuali.kfs.krad.datadictionary.WorkflowAttributes;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorInternal;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.workflow.service.WorkflowAttributePropertyResolutionService;
import org.kuali.kfs.sys.businessobject.DocumentHeader;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
 * CU Customization: Backported the FINP-8760 fix from the 2022-08-17 financials patch.
 * This overlay can be removed when we upgrade to the 2022-08-17 patch or later.
 */
public class DataDictionarySearchableAttribute implements SearchableAttribute {

    private static final long serialVersionUID = 173059488280366451L;
    private static final Logger LOG = LogManager.getLogger();
    public static final String DATA_TYPE_BOOLEAN = "boolean";

    @Override
    public String generateSearchContent(RuleAttribute ruleAttribute, String documentTypeName) {
        return "";
    }

    @Override
    public List<DocumentAttribute> extractDocumentAttributes(RuleAttribute ruleAttribute,
            DocumentRouteHeaderValue document) {
        List<DocumentAttribute> attributes = new ArrayList<>();

        String docId = document.getDocumentId();

        DocumentService docService = KRADServiceLocatorWeb.getDocumentService();
        Document doc = docService.getByDocumentHeaderIdSessionless(docId);

        String attributeValue;
        if (doc != null) {
            if (doc.getDocumentHeader() != null) {
                attributeValue = doc.getDocumentHeader().getDocumentDescription();
            } else {
                attributeValue = "null document header";
            }
        } else {
            attributeValue = "null document";
        }
        attributes.add(new DocumentAttributeString("documentDescription", attributeValue));

        if (doc != null) {
            if (doc.getDocumentHeader() != null) {
                attributeValue = doc.getDocumentHeader().getOrganizationDocumentNumber();
            } else {
                attributeValue = "null document header";
            }
        } else {
            attributeValue = "null document";
        }
        attributes.add(new DocumentAttributeString("organizationDocumentNumber", attributeValue));

        if (doc instanceof MaintenanceDocument) {
            final Class<? extends BusinessObject> businessObjectClass = getBusinessObjectClass(
                    document.getDocumentTypeName());
            if (businessObjectClass != null) {
                if (GlobalBusinessObject.class.isAssignableFrom(businessObjectClass)) {
                    final GlobalBusinessObject globalBO = retrieveGlobalBusinessObject(docId, businessObjectClass);

                    if (globalBO != null) {
                        attributes.addAll(findAllDocumentAttributesForGlobalBusinessObject(globalBO));
                    }
                } else {
                    attributes.addAll(parsePrimaryKeyValuesFromDocument(businessObjectClass, (MaintenanceDocument) doc));
                }

            }
        }
        if (doc != null) {
            DocumentEntry docEntry = KRADServiceLocatorWeb.getDocumentDictionaryService()
                    .getDocumentEntry(document.getDocumentTypeName());
            if (docEntry != null) {
                WorkflowAttributes workflowAttributes = docEntry.getWorkflowAttributes();
                WorkflowAttributePropertyResolutionService waprs = getWorkflowAttributePropertyResolutionService();
                attributes.addAll(waprs.resolveSearchableAttributeValues(doc, workflowAttributes));
            } else {
                LOG.error("Unable to find DD document entry for document type: {}", document::getDocumentTypeName);
            }
        }
        return attributes;
    }

    @Override
    public List<Field> getSearchFields(final RuleAttribute ruleAttribute, final String documentTypeName) {
        final List<Row> searchRows = getSearchingRows(documentTypeName);
        return FieldUtils.convertRowsToAttributeFields(searchRows);
    }

    /**
     * Produces legacy KNS rows to use for search attributes.  This method was left intact to help ease conversion
     * until KNS is replaced with KRAD.
     */
    protected List<Row> getSearchingRows(final String documentTypeName) {
        final List<Row> docSearchRows = new ArrayList<>();

        final Class boClass = DocumentHeader.class;

        final Field descriptionField = FieldUtils.getPropertyField(boClass, "documentDescription", true);
        descriptionField.setFieldDataType(CoreConstants.DATA_TYPE_STRING);

        final Field orgDocNumberField = FieldUtils.getPropertyField(boClass, "organizationDocumentNumber", true);
        orgDocNumberField.setFieldDataType(CoreConstants.DATA_TYPE_STRING);

        List<Field> fieldList = new ArrayList<>();
        fieldList.add(descriptionField);
        docSearchRows.add(new Row(fieldList));

        fieldList = new ArrayList<>();
        fieldList.add(orgDocNumberField);
        docSearchRows.add(new Row(fieldList));

        final DocumentEntry entry =
                KRADServiceLocatorWeb.getDocumentDictionaryService().getDocumentEntry(documentTypeName);
        if (entry == null) {
            return docSearchRows;
        }
        if (entry instanceof MaintenanceDocumentEntry) {
            Class<? extends BusinessObject> businessObjectClass = getBusinessObjectClass(documentTypeName);
            final Class<? extends Maintainable> maintainableClass = getMaintainableClass(documentTypeName);

            final GlobalMaintainableImpl globalMaintainable;
            try {
                globalMaintainable = (GlobalMaintainableImpl) maintainableClass.getConstructor().newInstance();
                businessObjectClass = globalMaintainable.getPrimaryEditedBusinessObjectClass();
            } catch (final Exception ie) {
                //was not a globalMaintainable.
            }

            if (businessObjectClass != null) {
                docSearchRows.addAll(createFieldRowsForBusinessObject(businessObjectClass, documentTypeName));
            }
        }

        final WorkflowAttributes workflowAttributes = entry.getWorkflowAttributes();
        if (workflowAttributes != null) {
            docSearchRows.addAll(createFieldRowsForWorkflowAttributes(workflowAttributes));
        }

        return docSearchRows;
    }

    @Override
    public List<AttributeError> validateDocumentAttributeCriteria(RuleAttribute ruleAttribute,
            DocumentSearchCriteria documentSearchCriteria) {
        List<AttributeError> validationErrors = new ArrayList<>();
        DictionaryValidationService validationService = getKnsDictionaryValidationService();

        // validate the document attribute values
        Map<String, List<String>> documentAttributeValues = documentSearchCriteria.getDocumentAttributeValues();
        for (String key : documentAttributeValues.keySet()) {
            List<String> values = documentAttributeValues.get(key);
            if (CollectionUtils.isNotEmpty(values)) {
                for (String value : values) {
                    if (StringUtils.isNotBlank(value)) {
                        validationService.validateAttributeFormat(documentSearchCriteria.getDocumentTypeName(), key,
                                value, key);
                    }
                }
            }
        }

        retrieveValidationErrorsFromGlobalVariables(validationErrors);

        return validationErrors;
    }

    /**
     * Retrieves validation errors from GlobalVariables MessageMap and appends to the given list of
     * AttributeError
     *
     * @param validationErrors list to append validation errors
     */
    protected void retrieveValidationErrorsFromGlobalVariables(List<AttributeError> validationErrors) {
        // can we use KualiConfigurationService?  It seemed to be used elsewhere...
        ConfigurationService configurationService = KRADServiceLocator.getKualiConfigurationService();

        if (GlobalVariables.getMessageMap().hasErrors()) {
            MessageMap deepCopy = (MessageMap) deepCopy();
            for (String errorKey : deepCopy.getErrorMessages().keySet()) {
                List<ErrorMessage> errorMessages = deepCopy.getErrorMessages().get(errorKey);
                if (CollectionUtils.isNotEmpty(errorMessages)) {
                    List<String> errors = new ArrayList<>();
                    for (ErrorMessage errorMessage : errorMessages) {
                        // need to materialize the message from it's parameters so we can send it back to the framework
                        String error = MessageFormat.format(configurationService.getPropertyValueAsString(
                                errorMessage.getErrorKey()), errorMessage.getMessageParameters());
                        errors.add(error);
                    }
                    AttributeError attributeError = AttributeError.Builder.create(errorKey,
                            errors).build();
                    validationErrors.add(attributeError);
                }
            }
            // we should now strip the error messages from the map because they have moved to validationErrors
            GlobalVariables.getMessageMap().clearErrorMessages();
        }
    }

    protected List<Row> createFieldRowsForWorkflowAttributes(final WorkflowAttributes attrs) {
        final List<Row> searchFields = new ArrayList<>();

        final List<SearchingTypeDefinition> searchingTypeDefinitions = attrs.getSearchingTypeDefinitions();
        final WorkflowAttributePropertyResolutionService propertyResolutionService =
                getWorkflowAttributePropertyResolutionService();
        for (final SearchingTypeDefinition definition : searchingTypeDefinitions) {
            final SearchingAttribute attr = definition.getSearchingAttribute();

            final String attributeName = attr.getAttributeName();
            final String businessObjectClassName = attr.getBusinessObjectClassName();
            final Class boClass;
            final BusinessObject businessObject;
            try {
                boClass = Class.forName(businessObjectClassName);
                businessObject = (BusinessObject) boClass.getConstructor().newInstance();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

            final Field searchField = FieldUtils.getPropertyField(boClass, attributeName, false);
            searchField.setColumnVisible(attr.isShowAttributeInResultSet());

            //TODO this is a workaround to hide the Field from the search criteria.
            //This should be removed once hiding the entire Row is working
            if (!attr.isShowAttributeInSearchCriteria()) {
                searchField.setFieldType(Field.HIDDEN);
            }
            String fieldDataType = propertyResolutionService.determineFieldDataType(boClass, attributeName);
            if (fieldDataType.equals(DATA_TYPE_BOOLEAN)) {
                fieldDataType = CoreConstants.DATA_TYPE_STRING;
            }

            // Allow inline range searching on dates and numbers
            if (fieldDataType.equals(CoreConstants.DATA_TYPE_FLOAT)
                    || fieldDataType.equals(CoreConstants.DATA_TYPE_LONG)
                    || fieldDataType.equals(CoreConstants.DATA_TYPE_DATE)) {

                searchField.setAllowInlineRange(true);
            }
            searchField.setFieldDataType(fieldDataType);
            final List<String> displayedFieldNames = new ArrayList<>();
            displayedFieldNames.add(attributeName);

            if (!attr.isNoLookup()) {
                LookupUtils.setFieldQuickfinder(businessObject, attributeName, searchField, displayedFieldNames);
            }

            final List<Field> fieldList = new ArrayList<>();
            fieldList.add(searchField);

            final Row row = new Row(fieldList);
            if (!attr.isShowAttributeInSearchCriteria()) {
                row.setHidden(true);
            }
            searchFields.add(row);
        }

        return searchFields;
    }

    protected List<DocumentAttribute> parsePrimaryKeyValuesFromDocument(
            Class<? extends BusinessObject> businessObjectClass, MaintenanceDocument document) {
        List<DocumentAttribute> values = new ArrayList<>();

        final List primaryKeyNames = getBusinessObjectMetaDataService().listPrimaryKeyFieldNames(
                businessObjectClass);

        for (Object primaryKeyNameAsObj : primaryKeyNames) {
            final String primaryKeyName = (String) primaryKeyNameAsObj;
            final DocumentAttribute searchableValue = parseSearchableAttributeValueForPrimaryKey(primaryKeyName,
                    businessObjectClass, document);
            if (searchableValue != null) {
                values.add(searchableValue);
            }
        }
        return values;
    }

    /**
     * Creates a searchable attribute value for the given property name out of the document XML
     *
     * @param propertyName        the name of the property to return
     * @param businessObjectClass the class of the business object maintained
     * @param document            the document XML
     * @return a generated SearchableAttributeValue, or null if a value could not be created
     */
    protected DocumentAttribute parseSearchableAttributeValueForPrimaryKey(String propertyName,
            Class<? extends BusinessObject> businessObjectClass, MaintenanceDocument document) {
        Maintainable maintainable = document.getNewMaintainableObject();
        PersistableBusinessObject bo = maintainable.getBusinessObject();

        final Object propertyValue = getPropertyValue(propertyName, bo);
        if (propertyValue == null) {
            return null;
        }

        final WorkflowAttributePropertyResolutionService propertyResolutionService =
                getWorkflowAttributePropertyResolutionService();
        return propertyResolutionService.buildSearchableAttribute(businessObjectClass, propertyName, propertyValue);
    }

    /**
     * Returns the class of the object being maintained by the given maintenance document type name
     *
     * @param documentTypeName the name of the document type to look up the maintained business object for
     * @return the class of the maintained business object
     */
    protected Class<? extends BusinessObject> getBusinessObjectClass(String documentTypeName) {
        MaintenanceDocumentEntry entry = retrieveMaintenanceDocumentEntry(documentTypeName);
        return entry == null ? null : (Class<? extends BusinessObject>) entry.getDataObjectClass();
    }

    /**
     * Returns the maintainable of the object being maintained by the given maintenance document type name
     *
     * @param documentTypeName the name of the document type to look up the maintained business object for
     * @return the Maintainable of the maintained business object
     */
    protected Class<? extends Maintainable> getMaintainableClass(String documentTypeName) {
        MaintenanceDocumentEntry entry = retrieveMaintenanceDocumentEntry(documentTypeName);
        return entry == null ? null : entry.getMaintainableClass();
    }

    /**
     * Retrieves the maintenance document entry for the given document type name
     *
     * @param documentTypeName the document type name to look up the data dictionary document entry for
     * @return the corresponding data dictionary entry for a maintenance document
     */
    protected MaintenanceDocumentEntry retrieveMaintenanceDocumentEntry(String documentTypeName) {
        return (MaintenanceDocumentEntry) KRADServiceLocatorWeb.getDocumentDictionaryService()
                .getDocumentEntry(documentTypeName);
    }

    protected GlobalBusinessObject retrieveGlobalBusinessObject(String documentNumber,
            Class<? extends BusinessObject> businessObjectClass) {
        GlobalBusinessObject globalBO = null;

        Map<String, String> pkMap = new LinkedHashMap<>();
        pkMap.put(KRADPropertyConstants.DOCUMENT_NUMBER, documentNumber);

        List returnedBOs = (List) KRADServiceLocator.getBusinessObjectService().findMatching(businessObjectClass,
                pkMap);
        if (returnedBOs.size() > 0) {
            globalBO = (GlobalBusinessObject) returnedBOs.get(0);
        }

        return globalBO;
    }

    protected List<DocumentAttribute> findAllDocumentAttributesForGlobalBusinessObject(GlobalBusinessObject globalBO) {
        List<DocumentAttribute> searchValues = new ArrayList<>();

        List<PersistableBusinessObject> globalChangesToPersist = globalBO.generateGlobalChangesToPersist();
        if (!CollectionUtils.isEmpty(globalChangesToPersist)) {
            for (PersistableBusinessObject bo : globalChangesToPersist) {
                List<DocumentAttribute> values = generateSearchableAttributeFromChange(bo);
                if (!CollectionUtils.isEmpty(values)) {
                    searchValues.addAll(values);
                }
            }
        }

        return searchValues;
    }

    protected List<DocumentAttribute> generateSearchableAttributeFromChange(PersistableBusinessObject changeToPersist) {
        final List<String> primaryKeyNames = getBusinessObjectMetaDataService()
                                            .listPrimaryKeyFieldNames(changeToPersist.getClass())
                                            .stream()
                                            .distinct()
                                            .collect(Collectors.toList());
        final WorkflowAttributePropertyResolutionService propertyResolutionService =
                getWorkflowAttributePropertyResolutionService();
        List<DocumentAttribute> documentAttributes = new ArrayList<>();

        for (Object primaryKeyNameAsObject : primaryKeyNames) {
            String primaryKeyName = (String) primaryKeyNameAsObject;
            Object value = getPropertyValue(primaryKeyName, changeToPersist);

            if (value != null) {
                DocumentAttribute saValue = propertyResolutionService.buildSearchableAttribute(
                        changeToPersist.getClass(), primaryKeyName, value);
                documentAttributes.add(saValue);
            }
        }
        return documentAttributes;
    }

    /**
     * Creates a list of search fields, one for each primary key of the maintained business object
     *
     * @param businessObjectClass the class of the maintained business object
     * @param documentTypeName the doc type name used to find a maintenance field
     * @return a List of KEW search fields
     */
    // CU Customization: Backport FINP-8760 fix
    protected List<Row> createFieldRowsForBusinessObject(
            final Class<? extends BusinessObject> businessObjectClass,
            final String documentTypeName
    ) {
        final List<Row> searchFields = new ArrayList<>();

        final List<String> primaryKeyNamesAsObjects = getBusinessObjectMetaDataService()
                .listPrimaryKeyFieldNames(businessObjectClass);
        final WorkflowAttributePropertyResolutionService propertyResolutionService =
                getWorkflowAttributePropertyResolutionService();
        for (final String attributeName : primaryKeyNamesAsObjects) {
            final BusinessObject businessObject;
            try {
                businessObject = businessObjectClass.getConstructor().newInstance();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }

            final Field searchField = FieldUtils.getPropertyField(businessObjectClass, attributeName, false);
            final String dataType =
                    propertyResolutionService.determineFieldDataType(businessObjectClass, attributeName);
            searchField.setFieldDataType(dataType);
            final List<Field> fieldList = new ArrayList<>();

            final List<String> displayedFieldNames = new ArrayList<>();
            displayedFieldNames.add(attributeName);

            final MaintainableFieldDefinition maintainableField =
                    getMaintenanceDocumentDictionaryService().getMaintainableField(documentTypeName, attributeName);
            if (maintainableField != null) {
                searchField.setNewLookup(maintainableField.isNewLookup());
            }

            if (!searchField.isNewLookup()) {
                LookupUtils.setFieldQuickfinder(businessObject, attributeName, searchField, displayedFieldNames);
            }

            fieldList.add(searchField);
            searchFields.add(new Row(fieldList));
        }

        return searchFields;
    }

    protected Serializable deepCopy() {
        return ObjectUtils.deepCopy(GlobalVariables.getMessageMap());
    }

    protected WorkflowAttributePropertyResolutionService getWorkflowAttributePropertyResolutionService() {
        return KRADServiceLocatorInternal
                .getWorkflowAttributePropertyResolutionService();
    }

    protected Object getPropertyValue(String propertyName, PersistableBusinessObject bo) {
        return ObjectUtils.getPropertyValue(bo, propertyName);
    }

    protected DictionaryValidationService getKnsDictionaryValidationService() {
        return KNSServiceLocator.getKNSDictionaryValidationService();
    }

    protected BusinessObjectMetaDataService getBusinessObjectMetaDataService() {
        return KNSServiceLocator.getBusinessObjectMetaDataService();
    }

    private MaintenanceDocumentDictionaryService getMaintenanceDocumentDictionaryService() {
        return KNSServiceLocator.getMaintenanceDocumentDictionaryService();
    }

}
