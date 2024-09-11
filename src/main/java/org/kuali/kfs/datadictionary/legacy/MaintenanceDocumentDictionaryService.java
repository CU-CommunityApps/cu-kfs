/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.datadictionary.legacy;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.service.KEWServiceLocator;
import org.kuali.kfs.kns.datadictionary.MaintainableCollectionDefinition;
import org.kuali.kfs.kns.datadictionary.MaintainableFieldDefinition;
import org.kuali.kfs.kns.datadictionary.MaintainableItemDefinition;
import org.kuali.kfs.kns.datadictionary.MaintainableSectionDefinition;
import org.kuali.kfs.kns.datadictionary.MaintenanceDocumentEntry;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.datadictionary.ReferenceDefinition;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.valuefinder.DefaultValueFinder;
import org.kuali.kfs.sys.KFSKeyConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ====
 * CU Customization: Backported KualiCo's FINP-10787 fix into this file.
 * This overlay can be removed when we upgrade to the 2024-07-17 version of financials.
 * ====
 * 
 * Defines the API for the interacting with Document-related entries in the data dictionary.
 */
@Deprecated
public class MaintenanceDocumentDictionaryService {

    private static final Logger LOG = LogManager.getLogger();

    private DataDictionaryService dataDictionaryService;

    /**
     * @param documentTypeName
     * @return the workflow document type for the given documentTypeName
     */
    protected DocumentType getDocumentType(final String documentTypeName) {
        return KEWServiceLocator.getDocumentTypeService().getDocumentTypeByName(documentTypeName);
    }

    /**
     * @param docTypeName doc type to retrieve label for
     * @return String doc type label for the maintenance document type
     */
    public String getMaintenanceLabel(final String docTypeName) {
        String label = null;

        final DocumentType docType = getDocumentType(docTypeName);
        if (docType != null) {
            label = docType.getLabel();
        }

        return label;
    }

    /**
     * Retrieves an instance of the class that represents the maintenance document. This is done by
     *
     * @param docTypeName
     * @return A class instance.
     */
    @Deprecated
    public Class getMaintainableClass(final String docTypeName) {
        Class maintainableClass = null;

        final MaintenanceDocumentEntry entry = getMaintenanceDocumentEntry(docTypeName);
        if (entry != null) {
            LOG.debug("supplying a generic Rule to insure basic validation");
            maintainableClass = entry.getMaintainableClass();
        }

        return maintainableClass;
    }

    /**
     * @param docTypeName
     * @return The class instance associated with the document type name.
     */
    public Class getDataObjectClass(final String docTypeName) {
        Class dataObjectClass = null;

        final MaintenanceDocumentEntry entry = getMaintenanceDocumentEntry(docTypeName);
        if (entry != null) {
            dataObjectClass = entry.getDataObjectClass();
        }

        return dataObjectClass;
    }

    /**
     * @param businessObjectClass
     * @return The document type name for the class as a String.
     */
    public String getDocumentTypeName(final Class businessObjectClass) {
        String documentTypeName = null;

        final MaintenanceDocumentEntry entry = getMaintenanceDocumentEntry(businessObjectClass);
        if (entry != null) {
            documentTypeName = entry.getDocumentTypeName();
        }

        return documentTypeName;
    }

    /**
     * @param docTypeName
     * @return A List of maintainable section object instances corresponding to the document type name.
     */
    @Deprecated
    public List<MaintainableSectionDefinition> getMaintainableSections(final String docTypeName) {
        List<MaintainableSectionDefinition> sections = null;

        final MaintenanceDocumentEntry entry = getMaintenanceDocumentEntry(docTypeName);
        if (entry != null) {
            sections = entry.getMaintainableSections();
        }

        return sections;
    }

    /**
     * @param businessObjectClass
     * @return A list of ReferenceDefinitions defined as DefaultExistenceChecks for the MaintenanceDocument
     */
    public List<ReferenceDefinition> getDefaultExistenceChecks(final Class businessObjectClass) {
        return getDefaultExistenceChecks(getDocumentTypeName(businessObjectClass));
    }

    /**
     * @param docTypeName
     * @return A list of ReferenceDefinitions defined as DefaultExistenceChecks for the MaintenanceDocument
     */
    public List<ReferenceDefinition> getDefaultExistenceChecks(final String docTypeName) {
        List<ReferenceDefinition> defaultExistenceChecks = null;

        final MaintenanceDocumentEntry entry = getMaintenanceDocumentEntry(docTypeName);
        if (entry != null) {
            defaultExistenceChecks = entry.getDefaultExistenceChecks();
        }

        return defaultExistenceChecks;
    }

    /**
     * @param docTypeName
     * @return
     */
    public MaintenanceDocumentEntry getMaintenanceDocumentEntry(final String docTypeName) {
        if (StringUtils.isBlank(docTypeName)) {
            throw new IllegalArgumentException("invalid (blank) docTypeName");
        }

        return (MaintenanceDocumentEntry) getDataDictionary().getDocumentEntry(docTypeName);
    }

    public MaintenanceDocumentEntry getMaintenanceDocumentEntry(final Class businessObjectClass) {
        if (businessObjectClass == null) {
            throw new IllegalArgumentException("invalid (blank) dataObjectClass");
        }

        return getDataDictionary().getMaintenanceDocumentEntryForBusinessObjectClass(businessObjectClass);
    }

    /**
     * This method returns the defaultValue as it would appear in the UI on a maintenance document.
     * <p>
     * If both a defaultValue and a defaultValueFinder is present in the MaintainableFieldDefinition instance, then
     * the defaultValue will be preferentially returned. If only one is present, then that will be returned.
     * <p>
     * Note that if a defaultValueFinder value is present, then this method will attempt to create a new instance
     * of the specified class. If this attempt to generate a new instance fails, the error will be suppressed, and a
     * null result will be returned.
     *
     * @param boClass   the class of BO being maintained
     * @param fieldName the fieldName of the attribute for which the default is desired
     * @return the default if one is available, null otherwise
     */
    @Deprecated
    public String getFieldDefaultValue(final Class boClass, final String fieldName) {
        if (boClass == null) {
            throw new IllegalArgumentException("The boClass parameter value specified was null. A valid class " +
                    "representing the boClass must be specified.");
        }

        // call the twin
        return getFieldDefaultValue(getDocumentTypeName(boClass), fieldName);
    }

    /**
     * This method returns the defaultValue as it would appear in the UI on a maintenance document.
     * <p>
     * If both a defaultValue and a defaultValueFinder is present in the MaintainableFieldDefinition instance, then
     * the defaultValue will be preferentially returned. If only one is present, then that will be returned.
     * <p>
     * Note that if a defaultValueFinder value is present, then this method will attempt to create a new instance
     * of the specified class. If this attempt to generate a new instance fails, the error will be suppressed, and a
     * null result will be returned.
     *
     * @param docTypeName the document type name of the maintainable
     * @param fieldName   the fieldName of the attribute for which the default is desired
     * @return the default if one is available, null otherwise
     */
    @Deprecated
    public String getFieldDefaultValue(final String docTypeName, final String fieldName) {
        if (StringUtils.isBlank(docTypeName)) {
            throw new IllegalArgumentException("The docTypeName parameter value specified was blank, whitespace, " +
                    "or null.  A valid string representing the docTypeName must be specified.");
        }
        if (StringUtils.isBlank(fieldName)) {
            throw new IllegalArgumentException("The fieldName parameter value specified was blank, whitespace, or " +
                    "null.  A valid string representing the fieldName must be specified.");
        }

        // walk through the sections
        final List<MaintainableSectionDefinition> sections = getMaintainableSections(docTypeName);
        for (final MaintainableSectionDefinition section : sections) {
            // walk through the fields
            final Collection fields = section.getMaintainableItems();
            final String defaultValue = getFieldDefaultValue(fields, fieldName);
            // need to keep trying sections until a match is found
            if (defaultValue != null) {
                return defaultValue;
            }
        }
        return null;
    }

    private String getFieldDefaultValue(final Collection maintainableFields, final String fieldName) {
        for (final Object maintainableField : maintainableFields) {
            final MaintainableItemDefinition item = (MaintainableItemDefinition) maintainableField;
            // only check fields...skip subcollections
            if (item instanceof MaintainableFieldDefinition) {
                final MaintainableFieldDefinition field = (MaintainableFieldDefinition) item;

                // if the field name matches
                if (field.getName().endsWith(fieldName)) {

                    // preferentially take the raw default value
                    if (StringUtils.isNotBlank(field.getDefaultValue())) {
                        return field.getDefaultValue();
                    } else if (field.getDefaultValueFinder() != null) {
                        final DefaultValueFinder defaultValueFinder = field.getDefaultValueFinder();

                        if (defaultValueFinder != null) {
                            return defaultValueFinder.getDefaultValue();
                        }
                    } else {
                        // if we found the field, but no default anything, then we're done
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * This method returns the defaultValue as it would appear in the UI on a maintenance document for a collection.
     * <p>
     * If both a defaultValue and a defaultValueFinder is present in the MaintainableFieldDefinition instance, then
     * the defaultValue will be preferentially returned. If only one is present, then that will be returned.
     * <p>
     * Note that if a defaultValueFinder value is present, then this method will attempt to create a new instance
     * of the specified class. If this attempt to generate a new instance fails, the error will be suppressed, and a
     * null result will be returned.
     *
     * @param docTypeName    the document type name of the maintainable
     * @param collectionName the name attribute of the collection to which the field belongs
     * @param fieldName      the fieldName of the attribute for which the default is desired
     * @return the default if one is available, null otherwise
     */
    @Deprecated
    public String getCollectionFieldDefaultValue(final String docTypeName, final String collectionName, final String fieldName) {
        if (StringUtils.isBlank(docTypeName)) {
            throw new IllegalArgumentException("The docTypeName parameter value specified was blank, whitespace, " +
                    "or null.  A valid string representing the docTypeName must be specified.");
        }
        if (StringUtils.isBlank(fieldName)) {
            throw new IllegalArgumentException("The fieldName parameter value specified was blank, whitespace, " +
                    "or null.  A valid string representing the fieldName must be specified.");
        }
        if (StringUtils.isBlank(collectionName)) {
            throw new IllegalArgumentException("The collectionName parameter value specified was null. A valid " +
                    "string representing the collectionName must be specified.");
        }

        final MaintainableCollectionDefinition coll = getMaintainableCollection(docTypeName, collectionName);
        if (coll != null) {
            final Collection collectionFields = coll.getMaintainableFields();
            return getFieldDefaultValue(collectionFields, fieldName);
        }
        return null;
    }

    /**
     * Returns whether or not this document's data dictionary file has flagged it to allow document copies
     *
     * @param document maintenance document instance to check copy flag for
     * @return boolean true if copies are allowed, false otherwise
     */
    public Boolean getAllowsCopy(final MaintenanceDocument document) {
        Boolean allowsCopy = Boolean.FALSE;
        if (document != null && document.getNewMaintainableObject() != null) {
            final MaintenanceDocumentEntry entry = getMaintenanceDocumentEntry(document.getNewMaintainableObject()
                    .getDataObjectClass());
            if (entry != null) {
                allowsCopy = entry.getAllowsCopy();
            }
        }

        return allowsCopy;
    }

    /**
     * Returns whether or not this document's data dictionary file has flagged it to allow maintenance new
     * or copy actions
     *
     * @param docTypeName maintenance document instance to check new or copy flag for
     * @return boolean true if new or copy maintenance actions are allowed
     */
    public Boolean getAllowsNewOrCopy(final String docTypeName) {
        Boolean allowsNewOrCopy = Boolean.FALSE;

        if (docTypeName != null) {
            final MaintenanceDocumentEntry entry = getMaintenanceDocumentEntry(docTypeName);
            if (entry != null) {
                allowsNewOrCopy = entry.getAllowsNewOrCopy();
            }
        }

        return allowsNewOrCopy;
    }

    /**
     * Returns the definition for the maintainable item identified by "itemName".
     *
     * @param docTypeName
     * @param itemName
     * @return The item or <b>null</b> if the item does not exist.
     */
    @Deprecated
    public MaintainableItemDefinition getMaintainableItem(final String docTypeName, final String itemName) {
        if (StringUtils.isBlank(docTypeName)) {
            throw new IllegalArgumentException("The docTypeName parameter value specified was blank, whitespace, " +
                    "or null.  A valid string representing the docTypeName must " + "be specified.");
        }
        if (StringUtils.isBlank(itemName)) {
            throw new IllegalArgumentException("The itemName parameter value specified was blank, whitespace, " +
                    "or null.  A valid string representing the itemName must " + "be specified.");
        }

        // split name for subcollections
        final String[] subItems = StringUtils.split(itemName, ".");

        // walk through the sections
        final List<MaintainableSectionDefinition> sections = getMaintainableSections(docTypeName);
        for (final MaintainableSectionDefinition section : sections) {
            // walk through the fields
            final List<MaintainableItemDefinition> fields = section.getMaintainableItems();
            for (final MaintainableItemDefinition item : fields) {
                if (item.getName().equals(itemName)) {
                    return item;
                }
                // if collection check to see if it has sub collections
                // for now this only allows 1 level (i.e. a.b) it should be expanded at some point
                if (item instanceof MaintainableCollectionDefinition) {
                    final MaintainableCollectionDefinition col = (MaintainableCollectionDefinition) item;
                    if (subItems.length > 1 && StringUtils.equals(col.getName(), subItems[0])) {
                        for (final MaintainableCollectionDefinition subCol : col.getMaintainableCollections()) {
                            if (subCol.getName().equals(subItems[1])) {
                                return subCol;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the definition for the maintainable field identified by "fieldName".
     * @param docTypeName
     * @param fieldName
     * @return The field or <b>null</b> if the item does not exist or is not a field.
     */
    public MaintainableFieldDefinition getMaintainableField(final String docTypeName, final String fieldName) {
        final MaintainableItemDefinition item = getMaintainableItem(docTypeName, fieldName);
        if (item instanceof MaintainableFieldDefinition) {
            return (MaintainableFieldDefinition) item;
        }
        return null;
    }

    /**
     * Returns the definition for the maintainable collection identified by "collectionName".
     *
     * @param docTypeName
     * @param collectionName
     * @return The collection or <b>null</b> if the item does not exist or is not a collection.
     */
    @Deprecated
    public MaintainableCollectionDefinition getMaintainableCollection(final String docTypeName, String collectionName) {
        // strip brackets as they are not needed to get to collection class
        // Like the other subcollections changes this currently only supports one sub level
        if (StringUtils.contains(collectionName, "[")) {
            collectionName = StringUtils.substringBefore(collectionName, "[") +
                    StringUtils.substringAfter(collectionName, "]");
        }
        final MaintainableItemDefinition item = getMaintainableItem(docTypeName, collectionName);
        if (item instanceof MaintainableCollectionDefinition) {
            return (MaintainableCollectionDefinition) item;
        }
        return null;
    }

    /**
     * @param docTypeName
     * @param collectionName
     * @return the business object used to store the values for the given collection.
     */
    @Deprecated
    public Class getCollectionBusinessObjectClass(final String docTypeName, final String collectionName) {
        final MaintainableCollectionDefinition coll = getMaintainableCollection(docTypeName, collectionName);
        if (coll != null) {
            return coll.getBusinessObjectClass();
        }
        return null;
    }

    /**
     * @param docTypeName
     * @return a List of all top-level maintainable collections on the document
     */
    @Deprecated
    public List<MaintainableCollectionDefinition> getMaintainableCollections(final String docTypeName) {
        final ArrayList<MaintainableCollectionDefinition> collections = new ArrayList<>();

        // walk through the sections
        final List<MaintainableSectionDefinition> sections = getMaintainableSections(docTypeName);
        for (final MaintainableSectionDefinition section : sections) {
            // walk through the fields
            final List<MaintainableItemDefinition> fields = section.getMaintainableItems();
            for (final MaintainableItemDefinition item : fields) {
                if (item instanceof MaintainableCollectionDefinition) {
                    collections.add((MaintainableCollectionDefinition) item);
                }
            }
        }

        return collections;
    }

    /**
     * @param parentCollection
     * @return a List of all collections within the given collection
     */
    @Deprecated
    public List<MaintainableCollectionDefinition> getMaintainableCollections(
            final MaintainableCollectionDefinition parentCollection) {
        final ArrayList<MaintainableCollectionDefinition> collections = new ArrayList<>();

        // walk through the sections
        final Collection<MaintainableCollectionDefinition> colls = parentCollection.getMaintainableCollections();
        for (final MaintainableCollectionDefinition coll : colls) {
            collections.add(coll);
            collections.addAll(getMaintainableCollections(coll));
        }

        return collections;
    }

    /**
     * Validates the maintenance document contains values for the fields declared as required in the maintenance
     * document data dictionary file.
     *
     * @param document
     */
    @Deprecated
    public void validateMaintenanceRequiredFields(final MaintenanceDocument document) {
        final Maintainable newMaintainableObject = document.getNewMaintainableObject();
        if (newMaintainableObject == null) {
            LOG.error("New maintainable is null");
            throw new RuntimeException("New maintainable is null");
        }

        final List<MaintainableSectionDefinition> maintainableSectionDefinitions =
                getMaintainableSections(getDocumentTypeName(newMaintainableObject.getDataObjectClass()));
        for (final MaintainableSectionDefinition maintainableSectionDefinition : maintainableSectionDefinitions) {
            for (final MaintainableItemDefinition maintainableItemDefinition : maintainableSectionDefinition
                    .getMaintainableItems()) {
                // validate fields
                if (maintainableItemDefinition instanceof MaintainableFieldDefinition) {
                    validateMaintainableFieldRequiredFields((MaintainableFieldDefinition) maintainableItemDefinition,
                            newMaintainableObject.getBusinessObject(), maintainableItemDefinition.getName());
                } else if (maintainableItemDefinition instanceof MaintainableCollectionDefinition) {
                    // validate collections
                    validateMaintainableCollectionsRequiredFields(newMaintainableObject.getBusinessObject(),
                            (MaintainableCollectionDefinition) maintainableItemDefinition);
                }
            }
        }
    }

    /**
     * generates error message if a field is marked as required but is not filled in
     *
     * @param maintainableFieldDefinition
     * @param businessObject
     * @param fieldName
     */
    private void validateMaintainableFieldRequiredFields(
            final MaintainableFieldDefinition maintainableFieldDefinition,
            final PersistableBusinessObject businessObject, final String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            throw new IllegalArgumentException("invalid fieldName parameter.");
        }
        // if required check we have a value for this field
        if (maintainableFieldDefinition.isRequired() && !maintainableFieldDefinition.isUnconditionallyReadOnly()) {
            try {
                final Object obj = ObjectUtils.getNestedValue(businessObject, fieldName);

                if (obj == null || StringUtils.isBlank(obj.toString())) {
                    final String attributeLabel = dataDictionaryService.getAttributeLabel(businessObject.getClass(), fieldName);
                    final String shortLabel = dataDictionaryService.getAttributeShortLabel(businessObject.getClass(), fieldName);
                    GlobalVariables.getMessageMap().putError(fieldName, KFSKeyConstants.ERROR_REQUIRED, attributeLabel +
                            " (" + shortLabel + ")");
                } else if (fieldName.endsWith(".principalName")) {
                    // special handling to catch when the principalName is not really a valid user pull the Person
                    // object and test the entity ID.  If that's null, then this is just a shell user instance and
                    // does not represent a true user the main principalId property on the main object would be null
                    // at this point but it is also unconditionally read only and not tested - checking that would
                    // require checking the relationships and be more complex than we want to get here
                    final String personProperty = ObjectUtils.getNestedAttributePrefix(fieldName);
                    if (StringUtils.isNotBlank(personProperty)) {
                        if (StringUtils.isBlank((String) ObjectUtils.getNestedValue(businessObject, personProperty +
                                ".entityId"))) {
                            final String attributeLabel = dataDictionaryService.getAttributeLabel(businessObject.getClass(),
                                    fieldName);
                            GlobalVariables.getMessageMap().putError(fieldName, KFSKeyConstants.ERROR_EXISTENCE,
                                    attributeLabel);
                        }
                    }
                }
            } catch (final Exception ex) {
                LOG.error("unable to read property during doc required field checks", ex);
            }
        }
    }

    private MaintainableCollectionDefinition getCollectionDefinition(final String docTypeName, final String collectionName) {
        String currentCollection = collectionName;
        String nestedCollections = "";
        if (StringUtils.contains(collectionName, "[")) {
            // strip off any array indexes
            currentCollection = StringUtils.substringBefore(collectionName, "[");
            nestedCollections = StringUtils.substringAfter(collectionName, ".");
        }

        // loop over all sections to find this collection
        final List<MaintainableSectionDefinition> maintainableSectionDefinitions = getMaintainableSections(docTypeName);
        for (final MaintainableSectionDefinition maintainableSectionDefinition : maintainableSectionDefinitions) {
            for (final MaintainableItemDefinition maintainableItemDefinition : maintainableSectionDefinition
                    .getMaintainableItems()) {
                if (maintainableItemDefinition instanceof MaintainableCollectionDefinition
                        && maintainableItemDefinition.getName().equals(currentCollection)) {
                    if (StringUtils.isBlank(nestedCollections)) {
                        return (MaintainableCollectionDefinition) maintainableItemDefinition;
                    }

                    return getCollectionDefinition((MaintainableCollectionDefinition) maintainableItemDefinition,
                            nestedCollections);
                }
            }
        }

        return null;
    }

    private MaintainableCollectionDefinition getCollectionDefinition(
            final MaintainableCollectionDefinition collectionDef,
            final String collectionName) {
        String currentCollection = collectionName;
        String nestedCollections = "";
        if (StringUtils.contains(collectionName, "[")) {
            // strip off any array indexes
            currentCollection = StringUtils.substringBefore(collectionName, "[");
            nestedCollections = StringUtils.substringAfter(collectionName, ".");
        }

        // loop over all nested collections
        for (final MaintainableCollectionDefinition maintainableCollectionDefinition : collectionDef
                .getMaintainableCollections()) {
            if (maintainableCollectionDefinition.getName().equals(currentCollection)) {
                if (StringUtils.isBlank(nestedCollections)) {
                    return maintainableCollectionDefinition;
                }
                return getCollectionDefinition(maintainableCollectionDefinition, nestedCollections);
            }
        }

        return null;
    }

    @Deprecated
    public void validateMaintainableCollectionsAddLineRequiredFields(
            final MaintenanceDocument document,
            final PersistableBusinessObject businessObject, final String collectionName) {
        final MaintainableCollectionDefinition def = getCollectionDefinition(getDocumentTypeName(businessObject.getClass()),
                collectionName);
        if (def != null) {
            validateMaintainableCollectionsAddLineRequiredFields(document, collectionName, def, 0);
        }
    }

    /**
     * calls code to generate error messages if maintainableFields within any collections or sub-collections are
     * marked as required
     *
     * @param document
     * @param collectionName
     * @param maintainableCollectionDefinition
     * @param depth
     */
    private void validateMaintainableCollectionsAddLineRequiredFields(
            final MaintenanceDocument document,
            final String collectionName, final MaintainableCollectionDefinition maintainableCollectionDefinition, final int depth) {
        if (depth == 0) {
            GlobalVariables.getMessageMap().addToErrorPath("add");
        }
        // validate required fields on fields withing collection definition
        final PersistableBusinessObject element = document.getNewMaintainableObject().getNewCollectionLine(collectionName);
        // ==== CU Customization: Backport FINP-10787 fix ====
        // refresh non-updatable references that may not have been loaded when FK fields were set (e.g. Person
        // references); without doing this the required field check may incorrectly fail on nested fields
        element.refreshNonUpdateableReferences();
        // ==== End CU Customization ====
        GlobalVariables.getMessageMap().addToErrorPath(collectionName);
        for (final MaintainableFieldDefinition maintainableFieldDefinition : maintainableCollectionDefinition
                .getMaintainableFields()) {
            final String fieldName = maintainableFieldDefinition.getName();
            validateMaintainableFieldRequiredFields(maintainableFieldDefinition, element, fieldName);

        }

        GlobalVariables.getMessageMap().removeFromErrorPath(collectionName);
        if (depth == 0) {
            GlobalVariables.getMessageMap().removeFromErrorPath("add");
        }
    }

    /**
     * calls code to generate error messages if maintainableFields within any collections or sub-collections are
     * marked as required
     *
     * @param businessObject
     * @param maintainableCollectionDefinition
     */
    private void validateMaintainableCollectionsRequiredFields(
            final PersistableBusinessObject businessObject,
            final MaintainableCollectionDefinition maintainableCollectionDefinition) {
        final String collectionName = maintainableCollectionDefinition.getName();

        // validate required fields on fields withing collection definition
        final Collection<PersistableBusinessObject> collection = (Collection) ObjectUtils.getPropertyValue(businessObject,
                collectionName);
        if (collection != null && !collection.isEmpty()) {
            for (final MaintainableFieldDefinition maintainableFieldDefinition : maintainableCollectionDefinition
                    .getMaintainableFields()) {
                int pos = 0;
                final String fieldName = maintainableFieldDefinition.getName();
                for (final PersistableBusinessObject element : collection) {
                    final String parentName = collectionName + "[" + pos++ + "]";
                    GlobalVariables.getMessageMap().addToErrorPath(parentName);
                    validateMaintainableFieldRequiredFields(maintainableFieldDefinition, element, fieldName);
                    GlobalVariables.getMessageMap().removeFromErrorPath(parentName);
                }
            }

            // recursively validate required fields on subcollections
            GlobalVariables.getMessageMap().addToErrorPath(collectionName);
            for (final MaintainableCollectionDefinition nestedMaintainableCollectionDefinition :
                    maintainableCollectionDefinition.getMaintainableCollections()) {
                for (final PersistableBusinessObject element : collection) {
                    validateMaintainableCollectionsRequiredFields(element, nestedMaintainableCollectionDefinition);
                }
            }
            GlobalVariables.getMessageMap().removeFromErrorPath(collectionName);
        }
    }

    /**
     * validates the collections of the maintenance document checking to see if duplicate entries in the collection
     * exist. For this impl that check is based on the key of the objects only.
     *
     * @param document
     */
    public void validateMaintainableCollectionsForDuplicateEntries(final MaintenanceDocument document) {
        final Maintainable newMaintainableObject = document.getNewMaintainableObject();
        if (newMaintainableObject == null) {
            LOG.error("New maintainable is null");
            throw new RuntimeException("New maintainable is null");
        }

        final List<MaintainableSectionDefinition> maintainableSectionDefinitions =
                getMaintainableSections(getDocumentTypeName(newMaintainableObject.getDataObjectClass()));
        for (final MaintainableSectionDefinition maintainableSectionDefinition : maintainableSectionDefinitions) {
            for (final MaintainableItemDefinition maintainableItemDefinition : maintainableSectionDefinition
                    .getMaintainableItems()) {
                // validate collections
                if (maintainableItemDefinition instanceof MaintainableCollectionDefinition) {
                    validateMaintainableCollectionsForDuplicateEntries(newMaintainableObject.getBusinessObject(),
                            (MaintainableCollectionDefinition) maintainableItemDefinition);
                }
            }
        }
    }

    /**
     * recursively checks collections for duplicate entries based on key values
     *
     * @param businessObject
     * @param maintainableCollectionDefinition
     */
    private void validateMaintainableCollectionsForDuplicateEntries(
            final PersistableBusinessObject businessObject,
            final MaintainableCollectionDefinition maintainableCollectionDefinition) {
        final String collectionName = maintainableCollectionDefinition.getName();

        if (maintainableCollectionDefinition.dissallowDuplicateKey()) {
            final Class maintainableBusinessObjectClass = businessObject.getClass();
            // validate that no duplicates based on keys exist
            final Collection<PersistableBusinessObject> collection = (Collection) ObjectUtils.getPropertyValue(
                    businessObject, collectionName);
            if (collection != null && !collection.isEmpty()) {
                final String propertyName = maintainableCollectionDefinition.getAttributeToHighlightOnDuplicateKey();
                // get collection label for dd
                final String label = dataDictionaryService.getCollectionLabel(maintainableBusinessObjectClass, collectionName);
                final String shortLabel = dataDictionaryService.getCollectionShortLabel(maintainableBusinessObjectClass, collectionName);
                int pos = 0;
                for (final PersistableBusinessObject element : collection) {
                    final String pathToElement = collectionName + "[" + pos++ + "]";
                    if (ObjectUtils.countObjectsWithIdentitcalKey(collection, element) > 1) {
                        GlobalVariables.getMessageMap().addToErrorPath(pathToElement);
                        GlobalVariables.getMessageMap().putError(propertyName, KFSKeyConstants.ERROR_DUPLICATE_ELEMENT,
                                label, shortLabel);
                        GlobalVariables.getMessageMap().removeFromErrorPath(pathToElement);
                    }
                }

                // recursively check for duplicate entries on subcollections
                GlobalVariables.getMessageMap().addToErrorPath(collectionName);
                for (final MaintainableCollectionDefinition nestedMaintainableCollectionDefinition :
                        maintainableCollectionDefinition.getMaintainableCollections()) {
                    for (final PersistableBusinessObject element : collection) {
                        validateMaintainableCollectionsForDuplicateEntries(element,
                                nestedMaintainableCollectionDefinition);
                    }
                }
                GlobalVariables.getMessageMap().removeFromErrorPath(collectionName);
            }
        }
    }

    /**
     * Indicates whether the configured locking keys for a class should be cleared on a maintenance copy action or
     * values carried forward
     *
     * @param businessObjectClass class for the data object to check
     * @return boolean true if locking keys should be copied, false if they should be cleared
     */
    public boolean getPreserveLockingKeysOnCopy(final Class businessObjectClass) {
        boolean preserveLockingKeysOnCopy = false;

        final MaintenanceDocumentEntry docEntry = getMaintenanceDocumentEntry(businessObjectClass);

        if (docEntry != null) {
            preserveLockingKeysOnCopy = docEntry.getPreserveLockingKeysOnCopy();
        }

        return preserveLockingKeysOnCopy;
    }

    /**
     * Indicates whether the given data object class is configured to allow record deletions
     *
     * @param businessObjectClass class for the data object to check
     * @return Boolean true if record deletion is allowed, false if not allowed, null if not configured
     */
    public Boolean getAllowsRecordDeletion(final Class businessObjectClass) {
        Boolean allowsRecordDeletion = Boolean.FALSE;

        final MaintenanceDocumentEntry docEntry = getMaintenanceDocumentEntry(businessObjectClass);

        if (docEntry != null) {
            allowsRecordDeletion = docEntry.getAllowsRecordDeletion();
        }

        return allowsRecordDeletion;
    }

    /**
     * @param businessObjectClass business object class for maintenance definition
     * @return Boolean indicating whether translating of codes is configured to true in maintenance definition
     */
    @Deprecated
    public Boolean translateCodes(final Class businessObjectClass) {
        boolean translateCodes = false;

        final MaintenanceDocumentEntry docEntry = getMaintenanceDocumentEntry(businessObjectClass);

        if (docEntry != null) {
            translateCodes = docEntry.isTranslateCodes();
        }

        return translateCodes;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    private DataDictionary getDataDictionary() {
        return dataDictionaryService.getDataDictionary();
    }
}
