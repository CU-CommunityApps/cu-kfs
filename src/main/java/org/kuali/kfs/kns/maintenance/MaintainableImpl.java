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
package org.kuali.kfs.kns.maintenance;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ojb.broker.metadata.ClassNotPersistenceCapableException;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.datadictionary.legacy.DocumentDictionaryService;
import org.kuali.kfs.datadictionary.legacy.MaintenanceDocumentDictionaryService;
import org.kuali.kfs.kns.datadictionary.MaintainableCollectionDefinition;
import org.kuali.kfs.kns.datadictionary.MaintainableFieldDefinition;
import org.kuali.kfs.kns.datadictionary.MaintainableItemDefinition;
import org.kuali.kfs.kns.datadictionary.MaintainableSectionDefinition;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.document.authorization.FieldRestriction;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentPresentationController;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentRestrictions;
import org.kuali.kfs.kns.lookup.LookupUtils;
import org.kuali.kfs.kns.service.BusinessObjectAuthorizationService;
import org.kuali.kfs.kns.service.BusinessObjectMetaDataService;
import org.kuali.kfs.kns.service.DocumentHelperService;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.util.FieldUtils;
import org.kuali.kfs.kns.util.InactiveRecordsHidingUtils;
import org.kuali.kfs.kns.util.MaintenanceUtils;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.kns.web.ui.SectionBridge;
import org.kuali.kfs.krad.bo.DataObjectRelationship;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.datadictionary.AttributeSecurity;
import org.kuali.kfs.krad.datadictionary.exception.UnknownBusinessClassAttributeException;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DataObjectAuthorizationService;
import org.kuali.kfs.krad.service.DataObjectMetaDataService;
import org.kuali.kfs.krad.service.KRADServiceLocator;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.LookupService;
import org.kuali.kfs.krad.service.MaintenanceDocumentService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.KRADPropertyConstants;
import org.kuali.kfs.krad.util.MessageMap;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.valuefinder.DefaultValueFinder;
import org.kuali.kfs.core.api.CoreApiServiceLocator;
import org.kuali.kfs.core.api.encryption.EncryptionService;
import org.kuali.kfs.core.web.format.FormatException;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.api.services.KimApiServiceLocator;
import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.springframework.cache.annotation.Cacheable;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/*
 * KFSPTS-26091 CU customization to cache the results of getLockingDocumentId()
 */

/**
 * Base Maintainable class to hold things common to all maintainables.
 */
@Deprecated
public class MaintainableImpl implements Maintainable {

    private static final Logger LOG = LogManager.getLogger();
    
    public static final String CACHE_NAME = "MaintainableImpl";

    private String documentNumber;
    private Object dataObject;
    private Class<?> dataObjectClass;
    private String maintenanceAction;

    private transient LookupService lookupService;
    private transient DataDictionaryService dataDictionaryService;
    private transient DataObjectAuthorizationService dataObjectAuthorizationService;
    private transient DataObjectMetaDataService dataObjectMetaDataService;
    private transient DocumentDictionaryService documentDictionaryService;
    private transient EncryptionService encryptionService;
    private transient BusinessObjectService businessObjectService;
    private transient MaintenanceDocumentService maintenanceDocumentService;

    protected PersistableBusinessObject businessObject;

    protected Map<String, PersistableBusinessObject> newCollectionLines = new HashMap<>();
    protected Map<String, Boolean> inactiveRecordDisplay = new HashMap<>();

    protected transient PersistenceStructureService persistenceStructureService;
    protected transient BusinessObjectDictionaryService businessObjectDictionaryService;
    protected transient PersonService personService;
    protected transient BusinessObjectMetaDataService businessObjectMetaDataService;
    protected transient BusinessObjectAuthorizationService businessObjectAuthorizationService;
    protected transient DocumentHelperService documentHelperService;
    protected transient MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService;

    public MaintainableImpl() {
        super();
    }

    /**
     * Constructor which initializes the business object to be maintained.
     *
     * @param businessObject
     */
    public MaintainableImpl(PersistableBusinessObject businessObject) {
        super();
        this.businessObject = businessObject;
        setDataObject(businessObject);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map populateBusinessObject(Map<String, String> fieldValues, MaintenanceDocument maintenanceDocument,
            String methodToCall) {
        fieldValues = decryptEncryptedData(fieldValues, maintenanceDocument, methodToCall);
        Map newFieldValues = getMaintenanceDocumentService().resolvePrincipalNamesToPrincipalIds(getBusinessObject(),
                fieldValues);

        Map cachedValues = FieldUtils.populateBusinessObjectFromMap(getBusinessObject(), newFieldValues);
        performForceUpperCase(newFieldValues);

        return cachedValues;
    }

    /**
     * Special hidden parameters are set on the maintenance jsp starting with a prefix that tells us which fields have
     * been encrypted. This field finds the those parameters in the map, whose value gives us the property name that
     * has an encrypted value. We then need to decrypt the value in the Map before the business object is populated.
     *
     * @param fieldValues possibly with encrypted values
     * @return Map fieldValues with no encrypted values
     */
    protected Map<String, String> decryptEncryptedData(Map<String, String> fieldValues,
            MaintenanceDocument maintenanceDocument, String methodToCall) {
        try {
            MaintenanceDocumentRestrictions auths = KNSServiceLocator.getBusinessObjectAuthorizationService()
                    .getMaintenanceDocumentRestrictions(maintenanceDocument,
                            GlobalVariables.getUserSession().getPerson());
            for (String fieldName : fieldValues.keySet()) {
                String fieldValue = fieldValues.get(fieldName);

                if (StringUtils.isNotEmpty(fieldValue)
                        && fieldValue.endsWith(EncryptionService.ENCRYPTION_POST_PREFIX)) {
                    if (shouldFieldBeEncrypted(maintenanceDocument, fieldName, auths, methodToCall)) {
                        String encryptedValue = fieldValue;

                        // take off the postfix
                        encryptedValue =
                                StringUtils.stripEnd(encryptedValue, EncryptionService.ENCRYPTION_POST_PREFIX);
                        if (CoreApiServiceLocator.getEncryptionService().isEnabled()) {
                            String decryptedValue = getEncryptionService().decrypt(encryptedValue);
                            fieldValues.put(fieldName, decryptedValue);
                        }
                    } else {
                        throw new RuntimeException("The field value for field name " + fieldName +
                                " should not be encrypted.");
                    }
                } else if (StringUtils.isNotEmpty(fieldValue) && auths.hasRestriction(fieldName)
                        && shouldFieldBeEncrypted(maintenanceDocument, fieldName, auths, methodToCall)) {
                    throw new RuntimeException("The field value for field name " + fieldName + " should be encrypted.");
                }
            }
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("Unable to decrypt secure data: " + e.getMessage());
        }

        return fieldValues;
    }

    /**
     * Determines whether the field in a request should be encrypted. This base implementation does not work for
     * properties of collection elements.
     * <p>
     * This base implementation will only return true if the maintenance document is being refreshed after a lookup
     * (i.e. methodToCall is "refresh") and the data dictionary-based attribute security definition has any
     * restriction defined, whether the user would be authorized to view the field. This assumes that only fields
     * returned from a lookup should be encrypted in a request. If the user otherwise has no permissions to view/edit
     * the field, then a request parameter will not be sent back to the server for population.
     *
     * @param maintenanceDocument
     * @param fieldName
     * @param auths
     * @param methodToCall
     * @return
     */
    protected boolean shouldFieldBeEncrypted(MaintenanceDocument maintenanceDocument, String fieldName,
            MaintenanceDocumentRestrictions auths, String methodToCall) {
        if ("refresh".equals(methodToCall) && fieldName != null) {
            fieldName = fieldName.replaceAll("\\[[0-9]*+\\]", "");
            fieldName = fieldName.replaceAll("^add\\.", "");
            Map<String, AttributeSecurity> fieldNameToAttributeSecurityMap = MaintenanceUtils
                    .retrievePropertyPathToAttributeSecurityMappings(getDocumentTypeName());
            AttributeSecurity attributeSecurity = fieldNameToAttributeSecurityMap.get(fieldName);
            return attributeSecurity != null && attributeSecurity.hasRestrictionThatRemovesValueFromUI();
        } else {
            return false;
        }
    }

    /**
     * Calls method to get all the core sections for the business object defined in the data dictionary. Then
     * determines if the bo has custom attributes, if so builds a custom attribute section and adds to the section
     * list.
     *
     * @return List of org.kuali.ui.Section objects
     */
    @Override
    public List getSections(MaintenanceDocument document, Maintainable oldMaintainable) {
        return new ArrayList<>(getCoreSections(document, oldMaintainable));
    }

    /**
     * Gets list of maintenance sections built from the data dictionary. If the section contains maintenance fields,
     * construct Row/Field UI objects and place under Section UI. If section contains a maintenance collection,
     * call method to build a Section UI which contains rows of Container Fields.
     *
     * @return List of org.kuali.ui.Section objects
     */
    public List<Section> getCoreSections(MaintenanceDocument document, Maintainable oldMaintainable) {
        List<Section> sections = new ArrayList<>();
        MaintenanceDocumentRestrictions maintenanceRestrictions = KNSServiceLocator
                .getBusinessObjectAuthorizationService().getMaintenanceDocumentRestrictions(document,
                        GlobalVariables.getUserSession().getPerson());

        MaintenanceDocumentPresentationController maintenanceDocumentPresentationController =
                (MaintenanceDocumentPresentationController) getDocumentHelperService()
                        .getDocumentPresentationController(document);
        Set<String> conditionallyRequiredFields = maintenanceDocumentPresentationController
                .getConditionallyRequiredPropertyNames(document);

        List<MaintainableSectionDefinition> sectionDefinitions = getMaintenanceDocumentDictionaryService()
                .getMaintainableSections(getDocumentTypeName());
        try {
            // iterate through section definitions and create Section UI object
            for (MaintainableSectionDefinition maintSectionDef : sectionDefinitions) {
                List<String> displayedFieldNames = new ArrayList<>();
                if (!maintenanceRestrictions.isHiddenSectionId(maintSectionDef.getId())) {
                    for (MaintainableItemDefinition item : maintSectionDef.getMaintainableItems()) {
                        if (item instanceof MaintainableFieldDefinition) {
                            displayedFieldNames.add(item.getName());
                        }
                    }

                    Section section = SectionBridge.toSection(maintSectionDef, getBusinessObject(), this, oldMaintainable,
                            getMaintenanceAction(), displayedFieldNames, conditionallyRequiredFields);
                    if (maintenanceRestrictions.isReadOnlySectionId(maintSectionDef.getId())) {
                        section.setReadOnly(true);
                    }

                    sections.add(section);
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            LOG.error("Unable to create instance of object class" + e.getMessage());
            throw new RuntimeException("Unable to create instance of object class" + e.getMessage());
        }

        if (oldMaintainable != null) {
            for (MaintainableSectionDefinition sectionDefinition : sectionDefinitions) {
                for (MaintainableItemDefinition itemDefinition : sectionDefinition.getMaintainableItems()) {
                    if (itemDefinition instanceof MaintainableFieldDefinition) {
                        MaintainableFieldDefinition fieldDefinition = (MaintainableFieldDefinition) itemDefinition;
                        KNSServiceLocator.getSecurityLoggingService().logFieldAccess(getBusinessObject(),
                                fieldDefinition.getName(), document, maintenanceRestrictions, true, null);
                    } else if (itemDefinition instanceof MaintainableCollectionDefinition) {
                        logCollectionAccess((MaintainableCollectionDefinition) itemDefinition, maintenanceRestrictions,
                                getBusinessObject(), document);
                    }
                }
            }
        }
        return sections;
    }

    protected void logCollectionAccess(MaintainableCollectionDefinition collectionDefinition,
            MaintenanceDocumentRestrictions maintenanceRestrictions, BusinessObject bo, MaintenanceDocument document) {
        if (collectionDefinition.getMaintainableFields() != null) {
            for (MaintainableFieldDefinition fieldDefinition : collectionDefinition.getMaintainableFields()) {
                KNSServiceLocator.getSecurityLoggingService().logFieldAccess(bo, fieldDefinition.getName(), document,
                        maintenanceRestrictions, true, null);
            }
        }
        if (collectionDefinition.getMaintainableCollections() != null) {
            for (MaintainableCollectionDefinition subCollectionDefinition :
                    collectionDefinition.getMaintainableCollections()) {
                logCollectionAccess(subCollectionDefinition, maintenanceRestrictions, bo, document);
            }
        }
    }

    @Override
    public void saveBusinessObject() {
        getBusinessObjectService().linkAndSave(businessObject);
    }

    /**
     * Retrieves title for maintenance document from data dictionary
     */
    @Override
    public String getMaintainableTitle() {
        return getMaintenanceDocumentDictionaryService().getMaintenanceLabel(getDocumentTypeName());
    }

    @Override
    public boolean isBoNotesEnabled() {
        return getDataObjectMetaDataService().areNotesSupported(getDataObjectClass());
    }

    /**
     * Overriding to call old (KNS) name of the method
     */
    @Override
    public boolean isNotesEnabled() {
        return isBoNotesEnabled();
    }

    @Override
    public void refresh(String refreshCaller, Map fieldValues, MaintenanceDocument document) {
        String referencesToRefresh = (String) fieldValues.get(KRADConstants.REFERENCES_TO_REFRESH);
        refreshReferences(referencesToRefresh);
    }

    protected void refreshReferences(String referencesToRefresh) {
        PersistenceStructureService persistenceStructureService = getPersistenceStructureService();
        if (StringUtils.isNotBlank(referencesToRefresh)) {
            String[] references = StringUtils.split(referencesToRefresh, KRADConstants.REFERENCES_TO_REFRESH_SEPARATOR);
            for (String reference : references) {
                if (StringUtils.isNotBlank(reference)) {
                    if (reference.startsWith(KRADConstants.ADD_PREFIX + ".")) {
                        // add one for the period
                        String referenceWithoutAddPrefix = reference.substring(KRADConstants.ADD_PREFIX.length() + 1);

                        String boToRefreshName = StringUtils.substringBeforeLast(referenceWithoutAddPrefix, ".");
                        String propertyToRefresh = StringUtils.substringAfterLast(referenceWithoutAddPrefix, ".");
                        if (StringUtils.isNotBlank(propertyToRefresh)) {
                            PersistableBusinessObject addlineBO = getNewCollectionLine(boToRefreshName);
                            Class addlineBOClass = addlineBO.getClass();
                            LOG.debug("Refresh this \"new\"/add object for the collections:  "
                                    + referenceWithoutAddPrefix);
                            if (persistenceStructureService.hasReference(addlineBOClass, propertyToRefresh)
                                    || persistenceStructureService.hasCollection(addlineBOClass, propertyToRefresh)) {
                                addlineBO.refreshReferenceObject(propertyToRefresh);
                            } else {
                                if (getDataDictionaryService().hasRelationship(addlineBOClass.getName(),
                                        propertyToRefresh)) {
                                    // a DD mapping, try to go straight to the object and refresh it there
                                    Object possibleBO = ObjectUtils.getPropertyValue(addlineBO, propertyToRefresh);
                                    if (possibleBO instanceof PersistableBusinessObject) {
                                        ((PersistableBusinessObject) possibleBO).refresh();
                                    }
                                }
                            }
                        } else {
                            LOG.error("Error: unable to refresh this \"new\"/add object for the collections:  "
                                + referenceWithoutAddPrefix);
                        }
                    } else if (ObjectUtils.isNestedAttribute(reference)) {
                        Object nestedObject = ObjectUtils.getNestedValue(getBusinessObject(),
                                ObjectUtils.getNestedAttributePrefix(reference));
                        if (nestedObject == null) {
                            LOG.warn("Unable to refresh ReferenceToRefresh (" + reference +
                                    ")  was found to be null");
                        } else {
                            // do nothing for Collections, probably because it's not really a collection reference but
                            // a relationship defined in the DD for a collections lookup this part will need to be
                            // rewritten when the DD supports true collection references
                            if (!(nestedObject instanceof Collection)) {
                                if (nestedObject instanceof PersistableBusinessObject) {
                                    String propertyToRefresh = ObjectUtils.getNestedAttributePrimitive(reference);
                                    if (persistenceStructureService.hasReference(nestedObject.getClass(),
                                            propertyToRefresh)
                                            || persistenceStructureService.hasCollection(nestedObject.getClass(),
                                        propertyToRefresh)) {
                                        LOG.debug("Refreshing " + ObjectUtils.getNestedAttributePrefix(reference) +
                                                " " + ObjectUtils.getNestedAttributePrimitive(reference));
                                        ((PersistableBusinessObject) nestedObject)
                                                .refreshReferenceObject(propertyToRefresh);
                                    } else {
                                        // a DD mapping, try to go straight to the object and refresh it there
                                        Object possibleBO = ObjectUtils.getPropertyValue(nestedObject, propertyToRefresh);
                                        if (possibleBO instanceof PersistableBusinessObject
                                                && getDataDictionaryService()
                                                        .hasRelationship(possibleBO.getClass().getName(),
                                                                propertyToRefresh)) {
                                            ((PersistableBusinessObject) possibleBO).refresh();
                                        }
                                    }
                                } else {
                                    LOG.warn("Expected that a referenceToRefresh (" + reference
                                            + ")  would be a PersistableBusinessObject or Collection, but instead, " +
                                            "it was of class " + nestedObject.getClass().getName());
                                }
                            }
                        }
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Refreshing " + reference);
                        }
                        if (persistenceStructureService.hasReference(getDataObjectClass(), reference)
                                || persistenceStructureService.hasCollection(getDataObjectClass(), reference)) {
                            getBusinessObject().refreshReferenceObject(reference);
                        } else {
                            if (getDataDictionaryService().hasRelationship(getBusinessObject().getClass().getName(),
                                    reference)) {
                                // a DD mapping, try to go straight to the object and refresh it there
                                Object possibleRelationship = ObjectUtils.getPropertyValue(getBusinessObject(),
                                        reference);
                                if (possibleRelationship != null) {
                                    if (possibleRelationship instanceof PersistableBusinessObject) {
                                        ((PersistableBusinessObject) possibleRelationship).refresh();
                                    } else if (!(possibleRelationship instanceof Collection)) {
                                        // do nothing for Collections, probably because it's not really a collection
                                        // reference but a relationship defined in the DD for a collections lookup
                                        // this part will need to be rewritten when the DD supports true collection
                                        // references
                                        LOG.warn("Expected that a referenceToRefresh (" + reference
                                                + ")  would be a PersistableBusinessObject or Collection, but " +
                                                "instead, it was of class " + possibleRelationship.getClass().getName());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void addMultipleValueLookupResults(MaintenanceDocument document, String collectionName,
            Collection<PersistableBusinessObject> rawValues, boolean needsBlank, PersistableBusinessObject bo) {
        Collection maintCollection = (Collection) ObjectUtils.getPropertyValue(bo, collectionName);
        String docTypeName = document.getDocumentHeader().getWorkflowDocument().getDocumentTypeName();

        List<String> duplicateIdentifierFieldsFromDataDictionary = getDuplicateIdentifierFieldsFromDataDictionary(
                docTypeName, collectionName);

        List<String> existingIdentifierList = getMultiValueIdentifierList(maintCollection,
                duplicateIdentifierFieldsFromDataDictionary);

        Class collectionClass = getMaintenanceDocumentDictionaryService().getCollectionBusinessObjectClass(docTypeName,
                collectionName);

        List<MaintainableSectionDefinition> sections = getMaintenanceDocumentDictionaryService()
                .getMaintainableSections(docTypeName);
        Map<String, String> template = MaintenanceUtils.generateMultipleValueLookupBOTemplate(sections, collectionName);
        try {
            for (PersistableBusinessObject nextBo : rawValues) {
                PersistableBusinessObject templatedBo;
                if (needsBlank) {
                    templatedBo = (PersistableBusinessObject) collectionClass.newInstance();
                } else {
                    try {
                        ModuleService moduleService = KRADServiceLocatorWeb.getKualiModuleService()
                                .getResponsibleModuleService(collectionClass);
                        if (moduleService != null && moduleService.isExternalizable(collectionClass)) {
                            templatedBo = (PersistableBusinessObject) moduleService
                                    .createNewObjectFromExternalizableClass(collectionClass);
                        } else {
                            templatedBo = (PersistableBusinessObject) collectionClass.newInstance();
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("Cannot instantiate " + collectionClass.getName(), e);
                    }
                    // first set the default values specified in the DD
                    setNewCollectionLineDefaultValues(collectionName, templatedBo);
                    // then set the values from the multiple value lookup result
                    ObjectUtils.createHybridBusinessObject(templatedBo, nextBo, template);
                    prepareBusinessObjectForAdditionFromMultipleValueLookup(collectionName, templatedBo);
                }
                templatedBo.setNewCollectionRecord(true);

                if (!hasBusinessObjectExisted(templatedBo, existingIdentifierList,
                    duplicateIdentifierFieldsFromDataDictionary)) {
                    maintCollection.add(templatedBo);

                }
            }
        } catch (Exception e) {
            LOG.error("Unable to add multiple value lookup results " + e.getMessage());
            throw new RuntimeException("Unable to add multiple value lookup results " + e.getMessage());
        }
    }

    /**
     * This method is to retrieve a List of fields which are specified in the maintenance document data dictionary as
     * the duplicateIdentificationFields. This List is used to determine whether the new entry being added to the
     * collection is a duplicate entry and if so, we should not add the new entry to the existing collection
     *
     * @param docTypeName
     * @param collectionName
     */
    @Override
    public List<String> getDuplicateIdentifierFieldsFromDataDictionary(String docTypeName, String collectionName) {
        List<String> duplicateIdentifierFieldNames = new ArrayList<>();
        MaintainableCollectionDefinition collDef = getMaintenanceDocumentDictionaryService().getMaintainableCollection(
                docTypeName, collectionName);
        Collection<MaintainableFieldDefinition> fieldDef = collDef.getDuplicateIdentificationFields();
        for (MaintainableFieldDefinition eachFieldDef : fieldDef) {
            duplicateIdentifierFieldNames.add(eachFieldDef.getName());
        }
        return duplicateIdentifierFieldNames;
    }

    @Override
    public List<String> getMultiValueIdentifierList(Collection maintCollection, List<String> duplicateIdentifierFields) {
        List<String> identifierList = new ArrayList<>();
        for (PersistableBusinessObject bo : (Collection<PersistableBusinessObject>) maintCollection) {
            String uniqueIdentifier = "";
            for (String identifierField : duplicateIdentifierFields) {
                uniqueIdentifier = uniqueIdentifier + identifierField + "-" +
                        ObjectUtils.getPropertyValue(bo, identifierField);
            }
            if (StringUtils.isNotEmpty(uniqueIdentifier)) {
                identifierList.add(uniqueIdentifier);
            }
        }
        return identifierList;
    }

    @Override
    public boolean hasBusinessObjectExisted(BusinessObject bo, List<String> existingIdentifierList,
            List<String> duplicateIdentifierFields) {
        String uniqueIdentifier = "";
        for (String identifierField : duplicateIdentifierFields) {
            uniqueIdentifier = uniqueIdentifier + identifierField + "-"
                + ObjectUtils.getPropertyValue(bo, identifierField);
        }
        return existingIdentifierList.contains(uniqueIdentifier);
    }

    public void prepareBusinessObjectForAdditionFromMultipleValueLookup(String collectionName, BusinessObject bo) {
        // default implementation does nothing
    }

    /**
     * @return the instance of the business object being maintained.
     */
    @Override
    public PersistableBusinessObject getBusinessObject() {
        return businessObject;
    }

    /**
     * @param businessObject Sets the instance of a business object that will be maintained.
     */
    @Override
    public void setBusinessObject(PersistableBusinessObject businessObject) {
        this.businessObject = businessObject;
        setDataObject(businessObject);
    }

    @Override
    public void setGenerateDefaultValues(String docTypeName) {
        List<MaintainableSectionDefinition> sectionDefinitions = getMaintenanceDocumentDictionaryService()
                .getMaintainableSections(docTypeName);
        Map<String, String> defaultValues = new HashMap<>();

        try {
            for (MaintainableSectionDefinition maintSectionDef : sectionDefinitions) {
                Collection maintItems = maintSectionDef.getMaintainableItems();
                for (Object maintItem : maintItems) {
                    MaintainableItemDefinition item = (MaintainableItemDefinition) maintItem;
                    if (item instanceof MaintainableFieldDefinition) {
                        MaintainableFieldDefinition maintainableFieldDefinition = (MaintainableFieldDefinition) item;

                        String defaultValue = maintainableFieldDefinition.getDefaultValue();
                        if (defaultValue != null) {
                            if ("true".equals(defaultValue)) {
                                defaultValue = "Yes";
                            } else if ("false".equals(defaultValue)) {
                                defaultValue = "No";
                            }
                        }

                        DefaultValueFinder defaultValueFinder = maintainableFieldDefinition.getDefaultValueFinder();
                        if (defaultValueFinder != null) {
                            defaultValue = defaultValueFinder.getDefaultValue();
                        }
                        if (defaultValue != null) {
                            defaultValues.put(item.getName(), defaultValue);
                        }
                    }
                }
            }
            FieldUtils.populateBusinessObjectFromMap(getBusinessObject(), defaultValues);
        } catch (Exception e) {
            LOG.error("Unable to set default value " + e.getMessage(), e);
            throw new RuntimeException("Unable to set default value " + e.getMessage(), e);
        }
    }

    @Override
    public void setGenerateBlankRequiredValues(String docTypeName) {
        try {
            List<MaintainableSectionDefinition> sectionDefinitions = getMaintenanceDocumentDictionaryService()
                    .getMaintainableSections(docTypeName);
            Map<String, String> defaultValues = new HashMap<>();

            for (MaintainableSectionDefinition maintSectionDef : sectionDefinitions) {
                for (MaintainableItemDefinition item : maintSectionDef.getMaintainableItems()) {
                    if (item instanceof MaintainableFieldDefinition) {
                        MaintainableFieldDefinition maintainableFieldDefinition = (MaintainableFieldDefinition) item;
                        if (maintainableFieldDefinition.isRequired()
                                && maintainableFieldDefinition.isUnconditionallyReadOnly()) {
                            Object currPropVal = ObjectUtils.getPropertyValue(this.getBusinessObject(), item.getName());
                            if (currPropVal == null
                                || currPropVal instanceof String && StringUtils.isBlank((String) currPropVal)) {
                                DefaultValueFinder defaultValueFinder = maintainableFieldDefinition
                                    .getDefaultValueFinder();
                                if (defaultValueFinder != null) {
                                    String defaultValue = defaultValueFinder.getDefaultValue();
                                    if (defaultValue != null) {
                                        defaultValues.put(item.getName(), defaultValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            FieldUtils.populateBusinessObjectFromMap(getBusinessObject(), defaultValues);
        } catch (Exception e) {
            LOG.error("Unable to set blank required value " + e.getMessage(), e);
            throw new RuntimeException("Unable to set blank required value" + e.getMessage(), e);
        }
    }

    @Deprecated
    public void processAfterAddLine(String colName, Class colClass) {
    }

    @Override
    public void processBeforeAddLine(String colName, Class colClass, BusinessObject addBO) {
    }

    @Override
    public boolean getShowInactiveRecords(String collectionName) {
        return InactiveRecordsHidingUtils.getShowInactiveRecords(inactiveRecordDisplay, collectionName);
    }

    @Override
    public void setShowInactiveRecords(String collectionName, boolean showInactive) {
        InactiveRecordsHidingUtils.setShowInactiveRecords(inactiveRecordDisplay, collectionName, showInactive);
    }

    @Override
    public Map<String, Boolean> getInactiveRecordDisplay() {
        return inactiveRecordDisplay;
    }

    @Override
    public void addNewLineToCollection(String collectionName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("addNewLineToCollection( " + collectionName + " )");
        }
        // get the new line from the map
        PersistableBusinessObject addLine = newCollectionLines.get(collectionName);
        if (addLine != null) {
            // mark the isNewCollectionRecord so the option to delete this line will be presented
            addLine.setNewCollectionRecord(true);

            // if we add back add button on sub collection of an "add line" we may need extra logic here

            // get the collection from the business object
            Collection maintCollection = (Collection) ObjectUtils.getPropertyValue(getBusinessObject(), collectionName);
            maintCollection.add(addLine);
            // refresh parent object since attributes could of changed prior to
            // user clicking add

            String referencesToRefresh = LookupUtils.convertReferencesToSelectCollectionToString(
                    getAllRefreshableReferences(getBusinessObject().getClass()));
            if (LOG.isInfoEnabled()) {
                LOG.info("References to refresh for adding line to collection " + collectionName + ": "
                    + referencesToRefresh);
            }
            refreshReferences(referencesToRefresh);
        }

        initNewCollectionLine(collectionName);

    }

    @Override
    public PersistableBusinessObject getNewCollectionLine(String collectionName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("newCollectionLines: " + newCollectionLines);
        }
        PersistableBusinessObject addLine = newCollectionLines.get(collectionName);
        if (addLine == null) {
            addLine = initNewCollectionLine(collectionName);
        }
        return addLine;
    }

    public PersistableBusinessObject initNewCollectionLine(String collectionName) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("initNewCollectionLine( " + collectionName + " )");
        }
        PersistableBusinessObject addLine;
        try {
            addLine = (PersistableBusinessObject) getMaintenanceDocumentDictionaryService()
                    .getCollectionBusinessObjectClass(getDocumentTypeName(), collectionName).newInstance();
        } catch (Exception ex) {
            LOG.error("unable to instantiate new collection line", ex);
            throw new RuntimeException("unable to instantiate new collection line", ex);
        }
        newCollectionLines.put(collectionName, addLine);
        // set its values to the defaults
        setNewCollectionLineDefaultValues(collectionName, addLine);
        return addLine;
    }

    @Override
    public Map<String, String> populateNewCollectionLines(Map<String, String> fieldValues,
            MaintenanceDocument maintenanceDocument, String methodToCall) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("populateNewCollectionLines: " + fieldValues);
        }
        fieldValues = decryptEncryptedData(fieldValues, maintenanceDocument, methodToCall);

        Map<String, String> cachedValues = new HashMap<>();

        // loop over all collections with an enabled add line
        List<MaintainableCollectionDefinition> collections = getMaintenanceDocumentDictionaryService()
                .getMaintainableCollections(getDocumentTypeName());

        for (MaintainableCollectionDefinition coll : collections) {
            String collName = coll.getName();
            if (LOG.isDebugEnabled()) {
                LOG.debug("checking for collection: " + collName);
            }

            Map<String, String> collectionValues = new HashMap<>();
            Map<String, String> subCollectionValues = new HashMap<>();
            // loop over the collection, extracting entries with a matching prefix
            for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(collName)) {
                    String subStrKey = key.substring(collName.length() + 1);
                    // check for subcoll w/ '[', set collName to proper name and put in correct name for collection
                    // values (i.e. strip '*[x].')
                    if (key.contains("[")) {
                        // need the whole thing if subcollection
                        subCollectionValues.put(key, entry.getValue());
                    } else {
                        collectionValues.put(subStrKey, entry.getValue());
                    }
                }
            }
            // send those values to the business object
            if (LOG.isDebugEnabled()) {
                LOG.debug("values for collection: " + collectionValues);
            }
            cachedValues.putAll(FieldUtils.populateBusinessObjectFromMap(getNewCollectionLine(collName),
                collectionValues, KRADConstants.MAINTENANCE_ADD_PREFIX + collName + "."));
            performFieldForceUpperCase(getNewCollectionLine(collName), collectionValues);

            cachedValues.putAll(populateNewSubCollectionLines(coll, subCollectionValues));
        }

        return cachedValues;
    }

    /*
     * Yes, I think this could be merged with the above code - I'm leaving it separate until I figure out of there are
     * any issues which would require that it be separated.
     */
    protected Map populateNewSubCollectionLines(MaintainableCollectionDefinition parentCollection, Map fieldValues) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("populateNewSubCollectionLines: " + fieldValues);
        }
        Map cachedValues = new HashMap();

        for (MaintainableCollectionDefinition coll : parentCollection.getMaintainableCollections()) {
            String collName = coll.getName();

            if (LOG.isDebugEnabled()) {
                LOG.debug("checking for sub collection: " + collName);
            }
            Map<String, String> parents = new HashMap<>();
            // get parents from list
            for (Object entry : fieldValues.entrySet()) {
                String key = (String) ((Map.Entry) entry).getKey();
                if (key.contains(collName)) {
                    parents.put(StringUtils.substringBefore(key, "."), "");
                }
            }

            for (String parent : parents.keySet()) {
                // build a map for that collection
                Map<String, Object> collectionValues = new HashMap<>();
                // loop over the collection, extracting entries with a matching prefix
                for (Object entry : fieldValues.entrySet()) {
                    String key = (String) ((Map.Entry) entry).getKey();
                    if (key.contains(parent)) {
                        String substr = StringUtils.substringAfterLast(key, ".");
                        collectionValues.put(substr, ((Map.Entry) entry).getValue());
                    }
                }
                // send those values to the business object
                if (LOG.isDebugEnabled()) {
                    LOG.debug("values for sub collection: " + collectionValues);
                }
                GlobalVariables.getMessageMap().addToErrorPath(
                    KRADConstants.MAINTENANCE_ADD_PREFIX + parent + "." + collName);
                cachedValues.putAll(FieldUtils.populateBusinessObjectFromMap(getNewCollectionLine(parent + "."
                    + collName), collectionValues, KRADConstants.MAINTENANCE_ADD_PREFIX + parent + "." + collName
                    + "."));
                performFieldForceUpperCase(getNewCollectionLine(parent + "." + collName), collectionValues);
                GlobalVariables.getMessageMap().removeFromErrorPath(
                    KRADConstants.MAINTENANCE_ADD_PREFIX + parent + "." + collName);
            }

            cachedValues.putAll(populateNewSubCollectionLines(coll, fieldValues));
        }

        return cachedValues;
    }

    @Override
    public Collection<String> getAffectedReferencesFromLookup(BusinessObject baseBO, String attributeName,
            String collectionPrefix) {
        PersistenceStructureService pss = getPersistenceStructureService();
        String nestedBOPrefix = "";
        if (ObjectUtils.isNestedAttribute(attributeName)) {
            // if we're performing a lookup on a nested attribute, we need to use the nested BO all the way down the
            // chain
            nestedBOPrefix = ObjectUtils.getNestedAttributePrefix(attributeName);

            // renormalize the base BO so that the attribute name is not nested anymore
            Class reference = ObjectUtils.getPropertyType(baseBO, nestedBOPrefix, pss);
            if (!PersistableBusinessObject.class.isAssignableFrom(reference)) {
                return new ArrayList<>();
            }

            try {
                baseBO = (PersistableBusinessObject) reference.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                LOG.error(e);
            }
            attributeName = ObjectUtils.getNestedAttributePrimitive(attributeName);
        }

        if (baseBO == null) {
            return new ArrayList<>();
        }

        Map<String, Class> referenceNameToClassFromPSS = LookupUtils.getPrimitiveReference(baseBO, attributeName);
        if (referenceNameToClassFromPSS.size() > 1) {
            LOG.error("LookupUtils.getPrimitiveReference return results should only have at most one element");
        }

        BusinessObjectMetaDataService businessObjectMetaDataService = getBusinessObjectMetaDataService();
        DataObjectRelationship relationship = businessObjectMetaDataService.getBusinessObjectRelationship(baseBO,
                attributeName);
        if (relationship == null) {
            return new ArrayList<>();
        }

        Map<String, String> fkToPkMappings = relationship.getParentToChildReferences();

        Collection<String> affectedReferences = generateAllAffectedReferences(baseBO.getClass(), fkToPkMappings,
                nestedBOPrefix, collectionPrefix);
        if (LOG.isDebugEnabled()) {
            LOG.debug("References affected by a lookup on BO attribute \"" + collectionPrefix + nestedBOPrefix +
                    "." + attributeName + ": " + affectedReferences);
        }

        return affectedReferences;
    }

    protected boolean isRelationshipRefreshable(Class boClass, String relationshipName) {
        if (getPersistenceStructureService().isPersistable(boClass)) {
            if (getPersistenceStructureService().hasCollection(boClass, relationshipName)) {
                return !getPersistenceStructureService().isCollectionUpdatable(boClass, relationshipName);
            } else if (getPersistenceStructureService().hasReference(boClass, relationshipName)) {
                return !getPersistenceStructureService().isReferenceUpdatable(boClass, relationshipName);
            }
            // else, assume that the relationship is defined in the DD
        }

        return true;
    }

    protected Collection<String> generateAllAffectedReferences(Class boClass, Map<String, String> fkToPkMappings,
            String nestedBOPrefix, String collectionPrefix) {
        Set<String> allAffectedReferences = new HashSet<>();
        DataDictionaryService dataDictionaryService = getDataDictionaryService();
        PersistenceStructureService pss = getPersistenceStructureService();

        collectionPrefix = StringUtils.isBlank(collectionPrefix) ? "" : collectionPrefix;

        // retrieve the attributes that are affected by a lookup on attributeName.
        Collection<String> attributeReferenceFKAttributes = fkToPkMappings.keySet();

        // a lookup on an attribute may cause other attributes to be updated (e.g. account code lookup would also
        // affect chart code) build a list of all affected FK values via mapKeyFields above, and for each FK, see if
        // there are any non-updatable references with that FK

        // deal with regular simple references (<reference-descriptor>s in OJB)
        for (String fkAttribute : attributeReferenceFKAttributes) {
            for (String affectedReference : pss.getReferencesForForeignKey(boClass, fkAttribute).keySet()) {
                if (isRelationshipRefreshable(boClass, affectedReference)) {
                    if (StringUtils.isBlank(nestedBOPrefix)) {
                        allAffectedReferences.add(collectionPrefix + affectedReference);
                    } else {
                        allAffectedReferences.add(collectionPrefix + nestedBOPrefix + "." + affectedReference);
                    }
                }
            }
        }

        // now with collection references (<collection-descriptor>s in OJB)
        for (String collectionName : pss.listCollectionObjectTypes(boClass).keySet()) {
            if (isRelationshipRefreshable(boClass, collectionName)) {
                Map<String, String> keyMappingsForCollection = pss.getInverseForeignKeysForCollection(boClass,
                    collectionName);
                for (String collectionForeignKey : keyMappingsForCollection.keySet()) {
                    if (attributeReferenceFKAttributes.contains(collectionForeignKey)) {
                        if (StringUtils.isBlank(nestedBOPrefix)) {
                            allAffectedReferences.add(collectionPrefix + collectionName);
                        } else {
                            allAffectedReferences.add(collectionPrefix + nestedBOPrefix + "." + collectionName);
                        }
                    }
                }
            }
        }

        // now use the DD to compute more affected references
        List<String> ddDefinedRelationships = dataDictionaryService.getRelationshipNames(boClass.getName());
        for (String ddRelationship : ddDefinedRelationships) {
            // note that this map is PK (key/target) => FK (value/source)
            Map<String, String> referencePKtoFKmappings = dataDictionaryService.getRelationshipAttributeMap(
                boClass.getName(), ddRelationship);
            for (String sourceAttribute : referencePKtoFKmappings.values()) {
                // the sourceAttribute is the FK pointing to the target attribute (PK)
                if (attributeReferenceFKAttributes.contains(sourceAttribute)) {
                    for (String affectedReference : dataDictionaryService.getRelationshipEntriesForSourceAttribute(
                        boClass.getName(), sourceAttribute)) {
                        if (isRelationshipRefreshable(boClass, ddRelationship)) {
                            if (StringUtils.isBlank(nestedBOPrefix)) {
                                allAffectedReferences.add(affectedReference);
                            } else {
                                allAffectedReferences.add(nestedBOPrefix + "." + affectedReference);
                            }
                        }
                    }
                }
            }
        }
        return allAffectedReferences;
    }

    protected Collection<String> getAllRefreshableReferences(Class boClass) {
        HashSet<String> references = new HashSet<>();
        for (String referenceName : getPersistenceStructureService().listReferenceObjectFields(boClass).keySet()) {
            if (isRelationshipRefreshable(boClass, referenceName)) {
                references.add(referenceName);
            }
        }
        for (String collectionName : getPersistenceStructureService().listCollectionObjectTypes(boClass).keySet()) {
            if (isRelationshipRefreshable(boClass, collectionName)) {
                references.add(collectionName);
            }
        }
        for (String relationshipName : getDataDictionaryService().getRelationshipNames(boClass.getName())) {
            if (isRelationshipRefreshable(boClass, relationshipName)) {
                references.add(relationshipName);
            }
        }
        return references;
    }

    protected void setNewCollectionLineDefaultValues(String collectionName, PersistableBusinessObject addLine) {
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(addLine);
        for (PropertyDescriptor propertyDescriptor : descriptors) {
            String fieldName = propertyDescriptor.getName();
            Class propertyType = propertyDescriptor.getPropertyType();
            String value = getMaintenanceDocumentDictionaryService().getCollectionFieldDefaultValue(
                    getDocumentTypeName(), collectionName, fieldName);

            if (value != null) {
                try {
                    ObjectUtils.setObjectProperty(addLine, fieldName, propertyType, value);
                } catch (Exception ex) {
                    LOG.error("Unable to set default property of collection object: " + "\nobject: " + addLine
                            + "\nfieldName=" + fieldName + "\npropertyType=" + propertyType + "\nvalue=" + value, ex);
                }
            }
        }
    }

    @Override
    public void clearBusinessObjectOfRestrictedValues(MaintenanceDocumentRestrictions maintenanceDocumentRestrictions) {
        List<MaintainableSectionDefinition> sections = getMaintenanceDocumentDictionaryService()
            .getMaintainableSections(getDocumentTypeName());
        for (MaintainableSectionDefinition sectionDefinition : sections) {
            for (MaintainableItemDefinition itemDefinition : sectionDefinition.getMaintainableItems()) {
                if (itemDefinition instanceof MaintainableFieldDefinition) {
                    clearFieldRestrictedValues("", businessObject, (MaintainableFieldDefinition) itemDefinition,
                        maintenanceDocumentRestrictions);
                } else if (itemDefinition instanceof MaintainableCollectionDefinition) {
                    clearCollectionRestrictedValues("", businessObject,
                        (MaintainableCollectionDefinition) itemDefinition, maintenanceDocumentRestrictions);
                }
            }
        }
    }

    protected void clearCollectionRestrictedValues(String fieldNamePrefix, BusinessObject businessObject,
            MaintainableCollectionDefinition collectionDefinition,
            MaintenanceDocumentRestrictions maintenanceDocumentRestrictions) {
        String collectionName = fieldNamePrefix + collectionDefinition.getName();
        Collection<BusinessObject> collection = (Collection<BusinessObject>) ObjectUtils.getPropertyValue(
            businessObject, collectionDefinition.getName());

        if (collection != null) {
            int i = 0;
            // even though it's technically a Collection, we're going to index it like a list
            for (BusinessObject collectionItem : collection) {
                String collectionItemNamePrefix = collectionName + "[" + i + "].";
                for (MaintainableCollectionDefinition subCollectionDefinition : collectionDefinition
                        .getMaintainableCollections()) {
                    clearCollectionRestrictedValues(collectionItemNamePrefix, collectionItem, subCollectionDefinition,
                            maintenanceDocumentRestrictions);
                }
                for (MaintainableFieldDefinition fieldDefinition : collectionDefinition.getMaintainableFields()) {
                    clearFieldRestrictedValues(collectionItemNamePrefix, collectionItem, fieldDefinition,
                            maintenanceDocumentRestrictions);
                }
                i++;
            }
        }
    }

    protected void clearFieldRestrictedValues(String fieldNamePrefix, BusinessObject businessObject,
            MaintainableFieldDefinition fieldDefinition,
            MaintenanceDocumentRestrictions maintenanceDocumentRestrictions) {
        String fieldName = fieldNamePrefix + fieldDefinition.getName();

        FieldRestriction fieldRestriction = maintenanceDocumentRestrictions.getFieldRestriction(fieldName);
        if (fieldRestriction.isRestricted()) {
            String defaultValue = null;
            if (StringUtils.isNotBlank(fieldDefinition.getDefaultValue())) {
                defaultValue = fieldDefinition.getDefaultValue();
            } else if (fieldDefinition.getDefaultValueFinder() != null) {
                defaultValue = fieldDefinition.getDefaultValueFinder().getDefaultValue();
            }
            try {
                ObjectUtils.setObjectProperty(businessObject, fieldDefinition.getName(), defaultValue);
            } catch (Exception e) {
                // throw an exception, because we don't want users to be able to see the restricted value
                LOG.error("Unable to clear maintenance document values for field name: " + fieldName
                    + " default value: " + defaultValue, e);
                throw new RuntimeException("Unable to clear maintenance document values for field name: " + fieldName,
                    e);
            }
        }
    }

    protected void performForceUpperCase(Map fieldValues) {
        List<MaintainableSectionDefinition> sections = getMaintenanceDocumentDictionaryService()
            .getMaintainableSections(getDocumentTypeName());
        for (MaintainableSectionDefinition sectionDefinition : sections) {
            for (MaintainableItemDefinition itemDefinition : sectionDefinition.getMaintainableItems()) {
                if (itemDefinition instanceof MaintainableFieldDefinition) {
                    performFieldForceUpperCase("", businessObject, (MaintainableFieldDefinition) itemDefinition,
                        fieldValues);
                } else if (itemDefinition instanceof MaintainableCollectionDefinition) {
                    performCollectionForceUpperCase("", businessObject,
                        (MaintainableCollectionDefinition) itemDefinition, fieldValues);
                }
            }
        }
    }

    protected void performFieldForceUpperCase(String fieldNamePrefix, BusinessObject bo,
            MaintainableFieldDefinition fieldDefinition, Map fieldValues) {
        MessageMap errorMap = GlobalVariables.getMessageMap();
        String fieldName = fieldDefinition.getName();
        String mapKey = fieldNamePrefix + fieldName;
        if (fieldValues != null && fieldValues.get(mapKey) != null) {
            if (PropertyUtils.isWriteable(bo, fieldName) && ObjectUtils.getNestedValue(bo, fieldName) != null) {
                try {
                    Class type = ObjectUtils.easyGetPropertyType(bo, fieldName);
                    // convert to upperCase based on data dictionary
                    Class businessObjectClass = bo.getClass();
                    boolean upperCase = false;
                    try {
                        upperCase = getDataDictionaryService().getAttributeForceUppercase(businessObjectClass,
                                fieldName);
                    } catch (UnknownBusinessClassAttributeException t) {
                        // I'm not sure why this catch block is empty
                    }

                    Object fieldValue = ObjectUtils.getNestedValue(bo, fieldName);

                    if (upperCase && fieldValue instanceof String) {
                        fieldValue = ((String) fieldValue).toUpperCase(Locale.US);
                    }
                    ObjectUtils.setObjectProperty(bo, fieldName, type, fieldValue);
                } catch (FormatException e) {
                    errorMap.putError(fieldName, e.getErrorKey(), e.getErrorArgs());
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    LOG.error("unable to populate business object" + e.getMessage());
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        }
    }

    protected void performFieldForceUpperCase(BusinessObject bo, Map fieldValues) {
        MessageMap errorMap = GlobalVariables.getMessageMap();

        try {
            for (Object entry : fieldValues.keySet()) {
                String propertyName = (String) entry;

                if (PropertyUtils.isWriteable(bo, propertyName) && fieldValues.get(propertyName) != null) {
                    // if the field propertyName is a valid property on the bo class
                    Class type = ObjectUtils.easyGetPropertyType(bo, propertyName);
                    try {
                        // Keep the convert to upperCase logic here. It will be used in populateNewCollectionLines,
                        // populateNewSubCollectionLines convert to upperCase based on data dictionary
                        Class businessObjectClass = bo.getClass();
                        boolean upperCase = false;
                        try {
                            upperCase = getDataDictionaryService().getAttributeForceUppercase(businessObjectClass,
                                    propertyName);
                        } catch (UnknownBusinessClassAttributeException t) {
                            // I'm not sure why this catch block is empty
                        }

                        Object fieldValue = fieldValues.get(propertyName);

                        if (upperCase && fieldValue instanceof String) {
                            fieldValue = ((String) fieldValue).toUpperCase(Locale.US);
                        }
                        ObjectUtils.setObjectProperty(bo, propertyName, type, fieldValue);
                    } catch (FormatException e) {
                        errorMap.putError(propertyName, e.getErrorKey(), e.getErrorArgs());
                    }
                }
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOG.error("unable to populate business object" + e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected void performCollectionForceUpperCase(String fieldNamePrefix, BusinessObject bo,
            MaintainableCollectionDefinition collectionDefinition, Map fieldValues) {
        String collectionName = fieldNamePrefix + collectionDefinition.getName();
        Collection<BusinessObject> collection = (Collection<BusinessObject>) ObjectUtils.getPropertyValue(bo,
            collectionDefinition.getName());
        if (collection != null) {
            int i = 0;
            // even though it's technically a Collection, we're going to index it like a list
            for (BusinessObject collectionItem : collection) {
                String collectionItemNamePrefix = collectionName + "[" + i + "].";
                for (MaintainableFieldDefinition fieldDefinition : collectionDefinition.getMaintainableFields()) {
                    performFieldForceUpperCase(collectionItemNamePrefix, collectionItem, fieldDefinition, fieldValues);
                }
                for (MaintainableCollectionDefinition subCollectionDefinition : collectionDefinition
                        .getMaintainableCollections()) {
                    performCollectionForceUpperCase(collectionItemNamePrefix, collectionItem, subCollectionDefinition,
                        fieldValues);
                }
                i++;
            }
        }
    }

    /**
     * By default a maintainable is not external
     */
    @Override
    public boolean isExternalBusinessObject() {
        return false;
    }

    @Override
    public void prepareBusinessObject(BusinessObject businessObject) {
        // Do nothing by default
    }

    @Override
    public void deleteBusinessObject() {
        if (businessObject == null) {
            return;
        }

        KRADServiceLocator.getBusinessObjectService().delete(businessObject);
        businessObject = null;
    }

    @Override
    public boolean isOldBusinessObjectInDocument() {
        return isOldDataObjectInDocument();
    }

    /**
     * @return the document type name from the data dictionary based on business object class
     */
    protected String getDocumentTypeName() {
        return getDocumentDictionaryService().getMaintenanceDocumentTypeName(dataObjectClass);
    }

    @Override
    public void setDataObject(Object object) {
        this.dataObject = object;

        if (object instanceof PersistableBusinessObject) {
            this.businessObject = (PersistableBusinessObject) object;
        }
    }

    @Override
    public String getDocumentTitle(MaintenanceDocument document) {
        // default implementation is to allow MaintenanceDocumentBase to generate the doc title
        return "";
    }

    @Override
    public Object retrieveObjectForEditOrCopy(MaintenanceDocument document, Map<String, String> dataObjectKeys) {
        Object dataObject = null;

        try {
            dataObject = getLookupService().findObjectBySearch(getDataObjectClass(), dataObjectKeys);
        } catch (ClassNotPersistenceCapableException ex) {
            if (!document.getOldMaintainableObject().isExternalBusinessObject()) {
                throw new RuntimeException("Data Object Class: " + getDataObjectClass()
                        + " is not persistable and is not externalizable - configuration error");
            }
            // otherwise, let fall through
        }

        return dataObject;
    }

    @Override
    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    @Override
    public Object getDataObject() {
        return dataObject;
    }

    @Override
    public Class getDataObjectClass() {
        return dataObjectClass;
    }

    @Override
    public void setDataObjectClass(Class dataObjectClass) {
        this.dataObjectClass = dataObjectClass;
    }

    /**
     * Persistable business objects are lockable
     */
    @Override
    public boolean isLockable() {
        return KRADServiceLocator.getPersistenceStructureService().isPersistable(getDataObject().getClass());
    }

    /**
     * @return the data object if its persistable, null otherwise
     */
    @Override
    public PersistableBusinessObject getPersistableBusinessObject() {
        if (KRADServiceLocator.getPersistenceStructureService().isPersistable(getDataObject().getClass())) {
            return (PersistableBusinessObject) getDataObject();
        } else {
            return null;
        }
    }

    @Override
    public String getMaintenanceAction() {
        return maintenanceAction;
    }

    @Override
    public void setMaintenanceAction(String maintenanceAction) {
        this.maintenanceAction = maintenanceAction;
    }

    /**
     * Note: as currently implemented, every key field for a given data object class must have a visible getter
     */
    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        List<MaintenanceLock> maintenanceLocks = new ArrayList<>();
        StringBuffer lockRepresentation = new StringBuffer(dataObjectClass.getName());
        lockRepresentation.append(KRADConstants.Maintenance.LOCK_AFTER_CLASS_DELIM);

        Object bo = getDataObject();
        List keyFieldNames = getDocumentDictionaryService().getLockingKeys(getDocumentTypeName());

        for (Iterator i = keyFieldNames.iterator(); i.hasNext(); ) {
            String fieldName = (String) i.next();
            Object fieldValue = ObjectUtils.getPropertyValue(bo, fieldName);
            if (fieldValue == null) {
                fieldValue = "";
            }

            // check if field is a secure
            if (getDataObjectAuthorizationService()
                    .attributeValueNeedsToBeEncryptedOnFormsAndLinks(dataObjectClass, fieldName)) {
                try {
                    if (CoreApiServiceLocator.getEncryptionService().isEnabled()) {
                        fieldValue = getEncryptionService().encrypt(fieldValue);
                    }
                } catch (GeneralSecurityException e) {
                    LOG.error("Unable to encrypt secure field for locking representation " + e.getMessage());
                    throw new RuntimeException("Unable to encrypt secure field for locking representation " +
                            e.getMessage());
                }
            }

            lockRepresentation.append(fieldName);
            lockRepresentation.append(KRADConstants.Maintenance.LOCK_AFTER_FIELDNAME_DELIM);
            lockRepresentation.append(fieldValue);
            if (i.hasNext()) {
                lockRepresentation.append(KRADConstants.Maintenance.LOCK_AFTER_VALUE_DELIM);
            }
        }

        MaintenanceLock maintenanceLock = new MaintenanceLock();
        maintenanceLock.setDocumentNumber(documentNumber);
        maintenanceLock.setLockingRepresentation(lockRepresentation.toString());
        maintenanceLocks.add(maintenanceLock);

        return maintenanceLocks;
    }

    @Override
    public void deleteDataObject() {
        if (dataObject == null) {
            return;
        }

        if (dataObject instanceof PersistableBusinessObject) {
            getBusinessObjectService().delete((PersistableBusinessObject) dataObject);
            dataObject = null;
        } else {
            throw new RuntimeException("Cannot delete object of type: " + dataObjectClass +
                    " with business object service");
        }
    }

    @Override
    public void doRouteStatusChange(DocumentHeader documentHeader) {
        // no default implementation
    }
    
    /*
     * CU Customization to cache this function
     */
    @Override
    @Cacheable(value = CACHE_NAME, key = "'lockingDocumentId-'+#root.target.documentNumber")
    public String getLockingDocumentId() {
        return getMaintenanceDocumentService().getLockingDocumentId(this, documentNumber);
    }

    @Override
    public List<String> getWorkflowEngineDocumentIdsToLock() {
        return null;
    }

    @Override
    public void prepareExternalBusinessObject(BusinessObject businessObject) {
        // by default do nothing
    }

    /**
     * Checks whether the data object is not null and has its primary key values populated
     */
    @Override
    public boolean isOldDataObjectInDocument() {
        boolean isOldDataObjectInExistence = true;

        if (getDataObject() == null) {
            isOldDataObjectInExistence = false;
        } else {
            Map<String, ?> keyFieldValues = getDataObjectMetaDataService().getPrimaryKeyFieldValues(getDataObject());
            for (Object keyValue : keyFieldValues.values()) {
                if (keyValue == null) {
                    isOldDataObjectInExistence = false;
                } else if (keyValue instanceof String && StringUtils.isBlank((String) keyValue)) {
                    isOldDataObjectInExistence = false;
                }

                if (!isOldDataObjectInExistence) {
                    break;
                }
            }
        }

        return isOldDataObjectInExistence;
    }

    @Override
    public void prepareForSave() {
        // by default do nothing
    }

    @Override
    public void processAfterRetrieve() {
        // by default do nothing
    }

    @Override
    public void setupNewFromExisting(MaintenanceDocument document, Map<String, String[]> parameters) {
        // by default do nothing
    }

    /**
     * Set the new collection records back to true so they can be deleted (copy should act like new)
     */
    @Override
    public void processAfterCopy(MaintenanceDocument document, Map<String, String[]> parameters) {
        try {
            ObjectUtils.setObjectPropertyDeep(businessObject, KRADPropertyConstants.NEW_COLLECTION_RECORD,
                    boolean.class, true, 2);
        } catch (Exception e) {
            LOG.error("unable to set newCollectionRecord property: " + e.getMessage(), e);
            throw new RuntimeException("unable to set newCollectionRecord property: " + e.getMessage(), e);
        }
    }

    @Override
    public void processAfterEdit(MaintenanceDocument document, Map<String, String[]> requestParameters) {
        // by default do nothing
    }

    @Override
    public void processAfterNew(MaintenanceDocument document, Map<String, String[]> requestParameters) {
        // by default do nothing
    }

    @Override
    public void processAfterPost(MaintenanceDocument document, Map<String, String[]> requestParameters) {
        // by default do nothing
    }

    protected String getDocumentNumber() {
        return this.documentNumber;
    }

    protected LookupService getLookupService() {
        if (lookupService == null) {
            lookupService = KRADServiceLocatorWeb.getLookupService();
        }
        return this.lookupService;
    }

    public void setLookupService(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    protected DataObjectAuthorizationService getDataObjectAuthorizationService() {
        if (dataObjectAuthorizationService == null) {
            this.dataObjectAuthorizationService = KRADServiceLocatorWeb.getDataObjectAuthorizationService();
        }
        return dataObjectAuthorizationService;
    }

    protected DataObjectMetaDataService getDataObjectMetaDataService() {
        if (dataObjectMetaDataService == null) {
            this.dataObjectMetaDataService = KRADServiceLocatorWeb.getDataObjectMetaDataService();
        }
        return dataObjectMetaDataService;
    }

    public DocumentDictionaryService getDocumentDictionaryService() {
        if (documentDictionaryService == null) {
            this.documentDictionaryService = KRADServiceLocatorWeb.getDocumentDictionaryService();
        }
        return documentDictionaryService;
    }

    public void setDocumentDictionaryService(DocumentDictionaryService documentDictionaryService) {
        this.documentDictionaryService = documentDictionaryService;
    }

    protected EncryptionService getEncryptionService() {
        if (encryptionService == null) {
            encryptionService = CoreApiServiceLocator.getEncryptionService();
        }
        return encryptionService;
    }

    public void setEncryptionService(EncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    protected BusinessObjectService getBusinessObjectService() {
        if (businessObjectService == null) {
            businessObjectService = KRADServiceLocator.getBusinessObjectService();
        }
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    protected MaintenanceDocumentService getMaintenanceDocumentService() {
        if (maintenanceDocumentService == null) {
            maintenanceDocumentService = KRADServiceLocatorWeb.getMaintenanceDocumentService();
        }
        return maintenanceDocumentService;
    }

    public void setMaintenanceDocumentService(MaintenanceDocumentService maintenanceDocumentService) {
        this.maintenanceDocumentService = maintenanceDocumentService;
    }

    protected DataDictionaryService getDataDictionaryService() {
        if (this.dataDictionaryService == null) {
            this.dataDictionaryService = KNSServiceLocator.getDataDictionaryService();
        }

        return this.dataDictionaryService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    protected PersistenceStructureService getPersistenceStructureService() {
        if (persistenceStructureService == null) {
            persistenceStructureService = KRADServiceLocator.getPersistenceStructureService();
        }
        return persistenceStructureService;
    }

    protected BusinessObjectDictionaryService getBusinessObjectDictionaryService() {
        if (businessObjectDictionaryService == null) {
            businessObjectDictionaryService = KNSServiceLocator.getBusinessObjectDictionaryService();
        }
        return businessObjectDictionaryService;
    }

    protected PersonService getPersonService() {
        if (personService == null) {
            personService = KimApiServiceLocator.getPersonService();
        }
        return personService;
    }

    protected BusinessObjectMetaDataService getBusinessObjectMetaDataService() {
        if (businessObjectMetaDataService == null) {
            businessObjectMetaDataService = KNSServiceLocator.getBusinessObjectMetaDataService();
        }
        return businessObjectMetaDataService;
    }

    protected BusinessObjectAuthorizationService getBusinessObjectAuthorizationService() {
        if (businessObjectAuthorizationService == null) {
            businessObjectAuthorizationService = KNSServiceLocator.getBusinessObjectAuthorizationService();
        }
        return businessObjectAuthorizationService;
    }

    protected DocumentHelperService getDocumentHelperService() {
        if (documentHelperService == null) {
            documentHelperService = KNSServiceLocator.getDocumentHelperService();
        }
        return documentHelperService;
    }

    public void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

    public void setBusinessObjectDictionaryService(BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    public void setBusinessObjectMetaDataService(BusinessObjectMetaDataService businessObjectMetaDataService) {
        this.businessObjectMetaDataService = businessObjectMetaDataService;
    }

    public void setBusinessObjectAuthorizationService(
        BusinessObjectAuthorizationService businessObjectAuthorizationService) {
        this.businessObjectAuthorizationService = businessObjectAuthorizationService;
    }

    public void setDocumentHelperService(DocumentHelperService documentHelperService) {
        this.documentHelperService = documentHelperService;
    }

    public MaintenanceDocumentDictionaryService getMaintenanceDocumentDictionaryService() {
        if (maintenanceDocumentDictionaryService == null) {
            this.maintenanceDocumentDictionaryService = KNSServiceLocator.getMaintenanceDocumentDictionaryService();
        }
        return maintenanceDocumentDictionaryService;
    }

    public void setMaintenanceDocumentDictionaryService(
        MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService) {
        this.maintenanceDocumentDictionaryService = maintenanceDocumentDictionaryService;
    }
}
