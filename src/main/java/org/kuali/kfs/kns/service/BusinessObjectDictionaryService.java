/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.kns.service;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.datadictionary.BusinessObjectAdminService;
import org.kuali.kfs.kns.datadictionary.BusinessObjectEntry;
import org.kuali.kfs.kns.datadictionary.FieldDefinition;
import org.kuali.kfs.kns.datadictionary.InquiryDefinition;
import org.kuali.kfs.kns.datadictionary.InquirySectionDefinition;
import org.kuali.kfs.kns.datadictionary.LookupDefinition;
import org.kuali.kfs.kns.datadictionary.MaintenanceDocumentEntry;
import org.kuali.kfs.kns.inquiry.InquiryAuthorizer;
import org.kuali.kfs.kns.inquiry.InquiryAuthorizerBase;
import org.kuali.kfs.kns.inquiry.InquiryPresentationController;
import org.kuali.kfs.kns.inquiry.InquiryPresentationControllerBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.datadictionary.DataDictionary;
import org.kuali.kfs.krad.datadictionary.LookupAttributeDefinition;
import org.kuali.kfs.krad.datadictionary.LookupResultAttributeDefinition;
import org.kuali.kfs.krad.datadictionary.SortDefinition;
import org.kuali.kfs.krad.datadictionary.exception.CompletionException;
import org.kuali.kfs.krad.exception.IntrospectionException;
import org.kuali.kfs.krad.exception.ReferenceAttributeNotAnOjbReferenceException;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.service.LookupSearchService;
import org.kuali.kfs.krad.service.ModuleService;
import org.kuali.kfs.krad.service.PersistenceStructureService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.krad.valuefinder.DefaultValueFinder;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.krad.bo.BusinessObject;

import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An API for the interacting with the data dictionary.
 */
// CU Customization: Added the 2020-01-09 financials version of this class, to fix some AR report lookup issues.
public class BusinessObjectDictionaryService {

    private static final Logger LOG = LogManager.getLogger(BusinessObjectDictionaryService.class);

    private DataDictionaryService dataDictionaryService;
    private PersistenceStructureService persistenceStructureService;

    public <T extends BusinessObject> InquiryAuthorizer getInquiryAuthorizer(Class<T> businessObjectClass) {
        Class inquiryAuthorizerClass = getDataDictionaryService().getDataDictionary()
                .getBusinessObjectEntry(businessObjectClass.getName()).getInquiryDefinition().getAuthorizerClass();
        if (inquiryAuthorizerClass == null) {
            inquiryAuthorizerClass = InquiryAuthorizerBase.class;
        }
        try {
            return (InquiryAuthorizer) inquiryAuthorizerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate InquiryAuthorizer class: " + inquiryAuthorizerClass, e);
        }
    }

    public <T extends BusinessObject> InquiryPresentationController getInquiryPresentationController(
            Class<T> businessObjectClass) {
        Class inquiryPresentationControllerClass = getDataDictionaryService().getDataDictionary()
                .getBusinessObjectEntry(businessObjectClass.getName()).getInquiryDefinition()
                .getPresentationControllerClass();
        if (inquiryPresentationControllerClass == null) {
            inquiryPresentationControllerClass = InquiryPresentationControllerBase.class;
        }
        try {
            return (InquiryPresentationController) inquiryPresentationControllerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate InquiryPresentationController class: "
                    + inquiryPresentationControllerClass, e);
        }
    }

    /**
     * @return the list of business object class names being maintained.
     */
    @Deprecated
    public List getBusinessObjectClassnames() {
        return getDataDictionaryService().getDataDictionary().getBusinessObjectClassNames();
    }

    /**
     * @return whether business object has lookup defined.
     */
    public Boolean isLookupable(Class businessObjectClass) {
        Boolean isLookupable = Boolean.FALSE;

        BusinessObjectEntry entry = getBusinessObjectEntry(businessObjectClass.getName());
        if (entry != null) {
            isLookupable = entry.hasLookupDefinition();
        }

        return isLookupable;
    }

    /**
     * @return whether business object has inquiry defined.
     */
    public Boolean isInquirable(Class businessObjectClass) {
        Boolean isInquirable = Boolean.FALSE;

        BusinessObjectEntry entry = getBusinessObjectEntry(businessObjectClass.getName());
        if (entry != null) {
            isInquirable = entry.hasInquiryDefinition();
        }

        return isInquirable;
    }

    /**
     * @return whether business object has maintainable defined.
     */
    public Boolean isMaintainable(Class businessObjectClass) {
        Boolean isMaintainable = Boolean.FALSE;

        BusinessObjectEntry entry = getBusinessObjectEntry(businessObjectClass.getName());
        if (entry != null) {
            isMaintainable = getMaintenanceDocumentEntry(businessObjectClass) != null;
        }

        return isMaintainable;
    }

    /**
     * @return whether business object has an exporter defined.
     */
    public Boolean isExportable(Class businessObjectClass) {
        Boolean isExportable = Boolean.FALSE;

        BusinessObjectEntry entry = getBusinessObjectEntry(businessObjectClass.getName());
        if (entry != null) {
            isExportable = entry.getExporterClass() != null;
        }

        return isExportable;
    }

    /**
     * @return the list defined as lookup fields for the business object.
     */
    public List<String> getLookupFieldNames(Class businessObjectClass) {
        List<String> results = null;

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            results = lookupDefinition.getLookupFieldNames();
        }

        return results;
    }

    /**
     * @return the text to be displayed for the title of business object lookup.
     */
    public String getLookupTitle(Class businessObjectClass) {
        String lookupTitle = "";

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            lookupTitle = lookupDefinition.getTitle();
        }

        return lookupTitle;
    }

    /**
     * @return menu bar html defined for the business object.
     */
    public String getLookupMenuBar(Class businessObjectClass) {
        String menubar = "";

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            if (lookupDefinition.hasMenubar()) {
                menubar = lookupDefinition.getMenubar();
            }
        }

        return menubar;
    }

    /**
     * @return source for optional extra button.
     */
    public String getExtraButtonSource(Class businessObjectClass) {
        String buttonSource = "";

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            if (lookupDefinition.hasExtraButtonSource()) {
                buttonSource = lookupDefinition.getExtraButtonSource();
            }
        }

        return buttonSource;
    }

    /**
     * @return parameters for optional extra button.
     */
    public String getExtraButtonParams(Class businessObjectClass) {
        String buttonParams = "";

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            if (lookupDefinition.hasExtraButtonParams()) {
                buttonParams = lookupDefinition.getExtraButtonParams();
            }
        }

        return buttonParams;
    }

    /**
     * @return String indicating the location of the lookup icon.
     */
    public String getSearchIconOverride(Class businessObjectClass) {
        String iconUrl = "";

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            if (lookupDefinition.hasSearchIconOverride()) {
                iconUrl = lookupDefinition.getSearchIconOverride();
            }
        }

        return iconUrl;
    }

    /**
     * @return the property names of the bo used to sort the initial result set.
     */
    public List<String> getLookupDefaultSortFieldNames(Class businessObjectClass) {
        List<String> defaultSort = null;

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            if (lookupDefinition.hasDefaultSort()) {
                defaultSort = lookupDefinition.getDefaultSort().getAttributeNames();
            }
        }
        if (defaultSort == null) {
            defaultSort = new ArrayList<>();
        }

        return defaultSort;
    }

    /**
     * @return the SortDefinition of the bo used to sort the initial result set.
     */
    public SortDefinition getLookupDefaultSortDefinition(Class businessObjectClass) {
        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            if (lookupDefinition.hasDefaultSort()) {
                return lookupDefinition.getDefaultSort();
            }
        }
        return null;
    }

    /**
     * @return the list defined as lookup result fields for the business object.
     */
    public List<String> getLookupResultFieldNames(Class businessObjectClass) {
        List<String> results = null;

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            results = lookupDefinition.getResultFieldNames();
        }

        return results;
    }

    /**
     * This method returns the maximum display length of the value of the given field in the lookup results. While the
     * actual value may be longer than the specified length, this value specifies the maximum length substring that
     * should be displayed. It is up to the UI layer to interpret the results of the field.
     *
     * @param businessObjectClass
     * @param resultFieldName
     * @return the maximum length of the lookup results field that should be displayed. Returns null if this value has
     *         not been defined.  If negative, denotes that the is maximum length is unlimited.
     */
    public Integer getLookupResultFieldMaxLength(Class businessObjectClass, String resultFieldName) {
        Integer resultFieldMaxLength = null;

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            FieldDefinition field = lookupDefinition.getResultField(resultFieldName);
            if (field != null) {
                resultFieldMaxLength = field.getMaxLength();
            }
        }

        return resultFieldMaxLength;
    }

    /**
     * @return String indicating the result set limit for the lookup.
     */
    public Integer getLookupResultSetLimit(Class businessObjectClass) {
        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            return lookupDefinition.getResultSetLimit();
        } else {
            return null;
        }
    }

    /**
     * @return Integer indicating the result set limit for a multiple values lookup.
     */
    public Integer getMultipleValueLookupResultSetLimit(Class businessObjectClass) {
        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            return lookupDefinition.getMultipleValuesResultSetLimit();
        } else {
            return null;
        }
    }

    /**
     * @return number of search columns configured for the lookup associated with the class.
     */
    public Integer getLookupNumberOfColumns(Class businessObjectClass) {
        // default to 1
        int numberOfColumns = 1;

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            if (lookupDefinition.getNumOfColumns() > 1) {
                numberOfColumns = lookupDefinition.getNumOfColumns();
            }
        }

        return numberOfColumns;
    }

    /**
     * @return whether a field is required for a lookup.
     */
    public Boolean getLookupAttributeRequired(Class businessObjectClass, String attributeName) {
        Boolean isRequired = null;

        FieldDefinition definition = getLookupFieldDefinition(businessObjectClass, attributeName);
        if (definition != null) {
            isRequired = definition.isRequired();
        }

        return isRequired;
    }

    /**
     * @return whether a field is read only for a lookup.
     */
    public Boolean getLookupAttributeReadOnly(Class businessObjectClass, String attributeName) {
        Boolean readOnly = null;

        FieldDefinition definition = getLookupFieldDefinition(businessObjectClass, attributeName);
        if (definition != null) {
            readOnly = definition.isReadOnly();
        }

        return readOnly;
    }

    /**
     * @return the list defined as inquiry fields for the business object and inquiry section.
     */
    public List getInquiryFieldNames(Class businessObjectClass, String sectionTitle) {
        List results = null;

        InquirySectionDefinition inquirySection = getInquiryDefinition(
                businessObjectClass).getInquirySection(sectionTitle);
        if (inquirySection != null) {
            results = inquirySection.getInquiryFieldNames();
        }

        return results;
    }

    /**
     * @return the list defined as inquiry sections for the business object.
     */
    public List<InquirySectionDefinition> getInquirySections(Class businessObjectClass) {
        return getInquiryDefinition(businessObjectClass).getInquirySections();
    }

    /**
     * @return the text to be displayed for the title of business object inquiry.
     */
    public String getInquiryTitle(Class businessObjectClass) {
        String title = "";

        InquiryDefinition inquiryDefinition = getInquiryDefinition(businessObjectClass);
        if (inquiryDefinition != null) {
            title = inquiryDefinition.getTitle();
        }

        return title;
    }

    /**
     * @return the class to be used for building inquiry pages.
     */
    public Class getInquirableClass(Class businessObjectClass) {
        Class clazz = null;

        InquiryDefinition inquiryDefinition = getInquiryDefinition(businessObjectClass);
        if (inquiryDefinition != null) {
            clazz = inquiryDefinition.getInquirableClass();
        }

        return clazz;
    }

    /**
     * @return the text to be displayed for the title of business object maintenance document.
     */
    public String getMaintainableLabel(Class businessObjectClass) {
        String label = "";

        MaintenanceDocumentEntry entry = getMaintenanceDocumentEntry(businessObjectClass);
        if (entry != null) {
            label = KewApiServiceLocator.getDocumentTypeService().getDocumentTypeByName(entry.getDocumentTypeName())
                    .getLabel();
        }

        return label;
    }

    /**
     * @return the Lookupable implementation id for the associated Lookup, if one has been specified.
     */
    public String getLookupableID(Class businessObjectClass) {
        String lookupableID = null;

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            lookupableID = lookupDefinition.getLookupableID();
        }

        return lookupableID;
    }

    /**
     * This method takes any business object and recursively walks through it checking to see if any attributes need to
     * be forced to uppercase based on settings in the data dictionary.
     *
     * Recurses down the updatable references and collections of a BO, uppercasing those attributes which are marked as
     * needing to be uppercased in the data dictionary. Updatability of a reference or collection is defined by the
     * PersistenceStructureService.
     *
     * @param bo the BO to uppercase
     * @see PersistenceStructureService#isCollectionUpdatable(Class, String)
     * @see PersistenceStructureService#isReferenceUpdatable(Class, String)
     * @see DataDictionaryService#getAttributeForceUppercase(Class, String)
     */
    public void performForceUppercase(BusinessObject bo) {
        performForceUppercaseCycleSafe(bo, new HashSet<>());
    }

    /**
     * Handles recursion for performForceUppercase in a cycle-safe manner, keeping track of visited BusinessObjects to
     * prevent infinite recursion.
     */
    protected void performForceUppercaseCycleSafe(BusinessObject bo, Set<BusinessObject> visited) {
        if (visited.contains(bo)) {
            return;
        } else {
            visited.add(bo);
        }
        PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(bo);
        for (PropertyDescriptor descriptor : descriptors) {
            try {
                if (!(descriptor instanceof IndexedPropertyDescriptor)) {
                    Object nestedObject = ObjectUtils.getPropertyValue(bo, descriptor.getName());
                    if (ObjectUtils.isNotNull(nestedObject) && nestedObject instanceof BusinessObject) {
                        if (persistenceStructureService.isPersistable(nestedObject.getClass())) {
                            try {
                                if (persistenceStructureService.hasReference(bo.getClass(), descriptor.getName())
                                        && persistenceStructureService.isReferenceUpdatable(bo.getClass(),
                                            descriptor.getName())
                                        && persistenceStructureService
                                            .getForeignKeyFieldsPopulationState((PersistableBusinessObject) bo,
                                                    descriptor.getName()).isAllFieldsPopulated()) {
                                    // check FKs to prevent probs caused by referential integrity problems
                                    performForceUppercaseCycleSafe((BusinessObject) nestedObject, visited);
                                }
                            } catch (ReferenceAttributeNotAnOjbReferenceException ranaore) {
                                LOG.debug("Property " + descriptor.getName() + " is not a foreign key reference.");
                            }
                        }
                    } else if (nestedObject instanceof String) {
                        if (dataDictionaryService.isAttributeDefined(bo.getClass(), descriptor.getName())
                                && dataDictionaryService.getAttributeForceUppercase(bo.getClass(),
                                descriptor.getName())) {
                            String curValue = (String) nestedObject;
                            PropertyUtils.setProperty(bo, descriptor.getName(), curValue.toUpperCase());
                        }
                    } else {
                        if (ObjectUtils.isNotNull(nestedObject) && nestedObject instanceof Collection) {
                            if (persistenceStructureService.hasCollection(bo.getClass(), descriptor.getName())
                                    && persistenceStructureService.isCollectionUpdatable(bo.getClass(),
                                        descriptor.getName())) {
                                for (Object collElem : (Collection) nestedObject) {
                                    if (collElem instanceof BusinessObject && persistenceStructureService
                                            .isPersistable(collElem.getClass())) {
                                        performForceUppercaseCycleSafe((BusinessObject) collElem, visited);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IntrospectionException("unable to performForceUppercase", e);
            } catch (NoSuchMethodException e) {
                // if the getter/setter does not exist, just skip over throw new
                // IntrospectionException("unable to performForceUppercase", e);
            }
        }
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public DataDictionaryService getDataDictionaryService() {
        return this.dataDictionaryService;
    }

    /**
     * I don't really like the double use case here but I also don't want to try to force callers to fall back or
     * scatter these Class.forName calls everywhere. Hopefully I will get a chance to take a pass and make a lot of
     * this better soon.
     *
     * @param businessObjectName the name of a BusinessObject; in some cases this will be a simple name and in other
     *                           cases it might be the name of the class for the BusinessObject
     * @return BusinessObjectEntry for the given businessObjectName, or {@code null} if there is none.
     * @throws IllegalArgumentException if the given businessObjectName is null or is not a BusinessObject class
     */
    public BusinessObjectEntry getBusinessObjectEntry(String businessObjectName) {
        // it's possible we got a classname and it includes this prefix (that's for proxies maybe?); it won't be
        // registered with the DD that way, so let's discard it.
        int index = businessObjectName.indexOf("$$");
        if (index >= 0) {
            businessObjectName = businessObjectName.substring(0, index);
        }

        DataDictionary dataDictionary = getDataDictionaryService().getDataDictionary();
        // try to get it directly as a string. in some use cases, this will be the name of the bo "AccountType" and
        // in others it maybe the fully qualified class name (of the base class or the overridden/customized class)
        BusinessObjectEntry entry = dataDictionary.getBusinessObjectEntries().get(businessObjectName);

        // if entry is still null, then we might have an integration class and we need to ask the module service to
        // get it (that will include mapping it to the impl class). KC would hit this a lot.
        if (entry == null) {
            Class boClass;
            try {
                boClass = Class.forName(businessObjectName);
                ModuleService responsibleModuleService = KRADServiceLocatorWeb.getKualiModuleService()
                        .getResponsibleModuleService(boClass);
                if (responsibleModuleService != null && responsibleModuleService.isExternalizable(boClass)) {
                    entry = responsibleModuleService.getExternalizableBusinessObjectDictionaryEntry(boClass);
                }
            } catch (ClassNotFoundException e) {
                LOG.warn("Unable to find class with class name: " + businessObjectName);
            }

        }
        return entry;
    }

    // I *think* this special case is now covered by getBusinessObjectEntry(String). we can probably remove this.
    public BusinessObjectEntry getBusinessObjectEntryForConcreteClass(String className) {
        return getDataDictionaryService().getDataDictionary().getBusinessObjectEntryForConcreteClass(className);
    }

    /**
     * @return Map of (classname, BusinessObjectEntry) pairs
     */
    public Map<String, BusinessObjectEntry> getBusinessObjectEntries() {
        return getDataDictionaryService().getDataDictionary().getBusinessObjectEntries();
    }

    /**
     * @param businessObjectClass
     * @return MaintenanceDocumentEntry for the given dataObjectClass, or {@code null} if there is none.
     * @throws IllegalArgumentException if the given Class is null or is not a BusinessObject class.
     */
    private MaintenanceDocumentEntry getMaintenanceDocumentEntry(Class businessObjectClass) {
        validateBusinessObjectClass(businessObjectClass);

        return getDataDictionaryService().getDataDictionary()
                .getMaintenanceDocumentEntryForBusinessObjectClass(businessObjectClass);
    }

    /**
     * @param businessObjectClass
     * @return LookupDefinition for the given dataObjectClass, or {@code null} if there is none.
     * @throws IllegalArgumentException if the given Class is null or is not a BusinessObject class.
     */
    public LookupDefinition getLookupDefinition(Class businessObjectClass) {
        LookupDefinition lookupDefinition = null;

        BusinessObjectEntry entry = getBusinessObjectEntry(businessObjectClass.getName());
        if (entry != null) {
            if (entry.hasLookupDefinition()) {
                lookupDefinition = entry.getLookupDefinition();
            }
        }

        return lookupDefinition;
    }

    /**
     * @param businessObjectClass
     * @param lookupFieldName
     * @return FieldDefinition for the given dataObjectClass and lookup field name, or {@code null} if there is none.
     * @throws IllegalArgumentException if the given Class is null or is not a BusinessObject class.
     */
    private FieldDefinition getLookupFieldDefinition(Class businessObjectClass, String lookupFieldName) {
        if (StringUtils.isBlank(lookupFieldName)) {
            throw new IllegalArgumentException("invalid (blank) lookupFieldName");
        }

        FieldDefinition fieldDefinition = null;

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            fieldDefinition = lookupDefinition.getLookupField(lookupFieldName);
        }

        return fieldDefinition;
    }

    /**
     * @param businessObjectClass
     * @param lookupFieldName
     * @return FieldDefinition for the given dataObjectClass and lookup result field name, or {@code null} if there is
     *         none.
     * @throws IllegalArgumentException if the given Class is null or is not a BusinessObject class.
     */
    private FieldDefinition getLookupResultFieldDefinition(Class businessObjectClass, String lookupFieldName) {
        if (StringUtils.isBlank(lookupFieldName)) {
            throw new IllegalArgumentException("invalid (blank) lookupFieldName");
        }

        FieldDefinition fieldDefinition = null;

        LookupDefinition lookupDefinition = getLookupDefinition(businessObjectClass);
        if (lookupDefinition != null) {
            fieldDefinition = lookupDefinition.getResultField(lookupFieldName);
        }

        return fieldDefinition;
    }

    /**
     * @param businessObjectClass
     * @return InquiryDefinition for the given dataObjectClass, or {@code null} if there is none.
     * @throws IllegalArgumentException if the given Class is null or is not a BusinessObject class.
     */
    private InquiryDefinition getInquiryDefinition(Class businessObjectClass) {
        InquiryDefinition inquiryDefinition = null;

        BusinessObjectEntry entry = getBusinessObjectEntry(businessObjectClass.getName());
        if (entry != null) {
            if (entry.hasInquiryDefinition()) {
                inquiryDefinition = entry.getInquiryDefinition();
            }
        }

        return inquiryDefinition;
    }

    /**
     * @return the attribute to be associated with for object level markings.
     */
    public String getTitleAttribute(Class businessObjectClass) {
        String titleAttribute = null;

        BusinessObjectEntry entry = getBusinessObjectEntry(businessObjectClass.getName());
        if (entry != null) {
            titleAttribute = entry.getTitleAttribute();
        }

        return titleAttribute;
    }

    /**
     * @param businessObjectClass
     * @param fieldName
     * @return FieldDefinition for the given dataObjectClass and field name, or {@code null} if there is none.
     * @throws IllegalArgumentException if the given Class is null or is not a BusinessObject class.
     */
    private FieldDefinition getInquiryFieldDefinition(Class businessObjectClass, String fieldName) {
        if (StringUtils.isBlank(fieldName)) {
            throw new IllegalArgumentException("invalid (blank) fieldName");
        }

        FieldDefinition fieldDefinition = null;

        InquiryDefinition inquiryDefinition = getInquiryDefinition(businessObjectClass);
        if (inquiryDefinition != null) {
            fieldDefinition = inquiryDefinition.getFieldDefinition(fieldName);
        }

        return fieldDefinition;
    }

    /**
     * @param businessObjectClass
     * @throws IllegalArgumentException if the given Class is null or is not a BusinessObject class.
     */
    private void validateBusinessObjectClass(Class businessObjectClass) {
        if (businessObjectClass == null) {
            throw new IllegalArgumentException("invalid (null) dataObjectClass");
        }
        if (!BusinessObject.class.isAssignableFrom(businessObjectClass)) {
            throw new IllegalArgumentException("class '" + businessObjectClass.getName()
                    + "' is not a descendant of BusinessObject");
        }
    }

    /**
     * @return boolean indicating whether lookup result field marked to force an inquiry.
     */
    public Boolean forceLookupResultFieldInquiry(Class businessObjectClass, String attributeName) {
        Boolean forceLookup = null;
        if (getLookupResultFieldDefinition(businessObjectClass, attributeName) != null) {
            forceLookup = getLookupResultFieldDefinition(businessObjectClass, attributeName).isForceInquiry();
        }

        return forceLookup;
    }

    /**
     * @return boolean indicating whether lookup result field marked to not do an inquiry.
     */
    public Boolean noLookupResultFieldInquiry(Class businessObjectClass, String attributeName) {
        Boolean noLookup = null;
        if (getLookupResultFieldDefinition(businessObjectClass, attributeName) != null) {
            noLookup = getLookupResultFieldDefinition(businessObjectClass, attributeName).isNoInquiry();
        }

        return noLookup;
    }

    /**
     * @return boolean indicating whether lookup search field marked to force a lookup.
     */
    @Deprecated
    public Boolean forceLookupFieldLookup(Class businessObjectClass, String attributeName) {
        Boolean forceLookup = null;
        if (getLookupFieldDefinition(businessObjectClass, attributeName) != null) {
            forceLookup = getLookupFieldDefinition(businessObjectClass, attributeName).isForceLookup();
        }

        return forceLookup;
    }

    /**
     * @return boolean indicating whether lookup search field marked to force an inquiry.
     */
    @Deprecated
    public Boolean forceInquiryFieldLookup(Class businessObjectClass, String attributeName) {
        Boolean forceInquiry = null;
        if (getLookupFieldDefinition(businessObjectClass, attributeName) != null) {
            forceInquiry = getLookupFieldDefinition(businessObjectClass, attributeName).isForceInquiry();
        }

        return forceInquiry;
    }

    /**
     * @return boolean indicating whether lookup search field marked to not do a lookup.
     */
    public boolean noLookupFieldLookup(Class businessObjectClass, String attributeName) {
        boolean noLookup = false;
        if (getLookupFieldDefinition(businessObjectClass, attributeName) != null) {
            noLookup = getLookupFieldDefinition(businessObjectClass, attributeName).isNoLookup();
        }

        return noLookup;
    }

    /**
     * @return boolean indicating whether lookup search field marked to not do a direct inquiry.
     */
    public Boolean noDirectInquiryFieldLookup(Class businessObjectClass, String attributeName) {
        Boolean noDirectInquiry = null;
        if (getLookupFieldDefinition(businessObjectClass, attributeName) != null) {
            noDirectInquiry = getLookupFieldDefinition(businessObjectClass, attributeName).isNoDirectInquiry();
        }

        return noDirectInquiry;
    }

    /**
     * @return boolean indicating whether lookup result field to use shortLabel.
     */
    public Boolean getLookupResultFieldUseShortLabel(Class businessObjectClass, String attributeName) {
        Boolean useShortLabel = null;
        if (getLookupResultFieldDefinition(businessObjectClass, attributeName) != null) {
            useShortLabel = getLookupResultFieldDefinition(businessObjectClass, attributeName).isUseShortLabel();
        }

        return useShortLabel;
    }

    /**
     * @return boolean indicating whether lookup result field should be totaled.
     */
    public boolean getLookupResultFieldTotal(Class businessObjectClass, String attributeName) {
        boolean total = false;

        if (getLookupResultFieldDefinition(businessObjectClass, attributeName) != null) {
            total = getLookupResultFieldDefinition(businessObjectClass, attributeName).isTotal();
        }

        return total;
    }

    /**
     * @return boolean indicating whether inquiry result field marked to force an inquiry.
     */
    @Deprecated
    public Boolean forceInquiryFieldInquiry(Class businessObjectClass, String attributeName) {
        Boolean forceInquiry = null;
        if (getInquiryFieldDefinition(businessObjectClass, attributeName) != null) {
            forceInquiry = getInquiryFieldDefinition(businessObjectClass, attributeName).isForceInquiry();
        }

        return forceInquiry;
    }

    /**
     * @return boolean indicating whether inquiry result field marked to not do an inquiry.
     */
    public Boolean noInquiryFieldInquiry(Class businessObjectClass, String attributeName) {
        Boolean noInquiry = null;
        if (getInquiryFieldDefinition(businessObjectClass, attributeName) != null) {
            noInquiry = getInquiryFieldDefinition(businessObjectClass, attributeName).isNoInquiry();
        }

        return noInquiry;
    }

    /**
     * @return String indicating the default search value for the lookup field.
     */
    public String getLookupFieldDefaultValue(Class businessObjectClass, String attributeName) {
        return getLookupFieldDefinition(businessObjectClass, attributeName).getDefaultValue();
    }

    /**
     * @return Class used to generate a lookup field default value.
     */
    public DefaultValueFinder getLookupFieldDefaultValueFinder(Class businessObjectClass, String attributeName) {
        return getLookupFieldDefinition(businessObjectClass, attributeName).getDefaultValueFinder();
    }

    /**
     * @return String indicating the default search value for the lookup field.
     * @see FieldDefinition#getQuickfinderParameterString()
     */
    public String getLookupFieldQuickfinderParameterString(Class businessObjectClass, String attributeName) {
        return getLookupFieldDefinition(businessObjectClass, attributeName).getQuickfinderParameterString();
    }

    /**
     * @return Class used to generate quickfinder lookup field default values.
     * @see FieldDefinition#getQuickfinderParameterStringBuilderClass()
     */
    public Class<? extends DefaultValueFinder> getLookupFieldQuickfinderParameterStringBuilderClass(
            Class businessObjectClass, String attributeName) {
        return getLookupFieldDefinition(businessObjectClass, attributeName).getQuickfinderParameterStringBuilderClass();
    }

    public void setPersistenceStructureService(PersistenceStructureService persistenceStructureService) {
        this.persistenceStructureService = persistenceStructureService;
    }

    /**
     * @param businessObjectClass
     * @param attributeName
     * @return whether on a lookup, field/attribute values with wildcards and operators should treat them as literal
     *         characters.
     */
    public boolean isLookupFieldTreatWildcardsAndOperatorsAsLiteral(Class businessObjectClass, String attributeName) {
        FieldDefinition lookupFieldDefinition = getLookupFieldDefinition(businessObjectClass, attributeName);
        return lookupFieldDefinition != null && lookupFieldDefinition.isTreatWildcardsAndOperatorsAsLiteral();
    }

    /**
     * @return String giving additional display attribute name for inquiry field if configured, or null.
     */
    public String getInquiryFieldAdditionalDisplayAttributeName(Class businessObjectClass, String attributeName) {
        String additionalDisplayAttributeName = null;

        if (getInquiryFieldDefinition(businessObjectClass, attributeName) != null) {
            additionalDisplayAttributeName = getInquiryFieldDefinition(businessObjectClass, attributeName)
                    .getAdditionalDisplayAttributeName();
        }

        return additionalDisplayAttributeName;
    }

    /**
     * @return String giving alternate display attribute name for inquiry field if configured, or null.
     */
    public String getInquiryFieldAlternateDisplayAttributeName(Class businessObjectClass, String attributeName) {
        String alternateDisplayAttributeName = null;

        if (getInquiryFieldDefinition(businessObjectClass, attributeName) != null) {
            alternateDisplayAttributeName = getInquiryFieldDefinition(businessObjectClass, attributeName)
                    .getAlternateDisplayAttributeName();
        }

        return alternateDisplayAttributeName;
    }

    /**
     * @return String giving additional display attribute name for lookup field if configured, or null.
     */
    public String getLookupFieldAdditionalDisplayAttributeName(Class businessObjectClass, String attributeName) {
        String additionalDisplayAttributeName = null;

        if (getLookupResultFieldDefinition(businessObjectClass, attributeName) != null) {
            additionalDisplayAttributeName = getLookupResultFieldDefinition(businessObjectClass, attributeName)
                    .getAdditionalDisplayAttributeName();
        }

        return additionalDisplayAttributeName;
    }

    /**
     * @return String giving alternate display attribute name for lookup field if configured, or null.
     */
    public String getLookupFieldAlternateDisplayAttributeName(Class businessObjectClass, String attributeName) {
        String alternateDisplayAttributeName = null;

        if (getLookupResultFieldDefinition(businessObjectClass, attributeName) != null) {
            alternateDisplayAttributeName = getLookupResultFieldDefinition(businessObjectClass, attributeName)
                    .getAlternateDisplayAttributeName();
        }

        return alternateDisplayAttributeName;
    }

    /**
     * @param businessObjectClass business object class for lookup definition
     * @return Boolean indicating whether translating of codes is configured to true in lookup definition.
     */
    public Boolean tranlateCodesInLookup(Class businessObjectClass) {
        boolean translateCodes = false;

        if (getLookupDefinition(businessObjectClass) != null) {
            translateCodes = getLookupDefinition(businessObjectClass).isTranslateCodes();
        }

        return translateCodes;
    }

    /**
     * @param businessObjectClass business object class for inquiry definition
     * @return Boolean indicating whether translating of codes is configured to true in inquiry definition.
     */
    public Boolean tranlateCodesInInquiry(Class businessObjectClass) {
        boolean translateCodes = false;

        if (getInquiryDefinition(businessObjectClass) != null) {
            translateCodes = getInquiryDefinition(businessObjectClass).isTranslateCodes();
        }

        return translateCodes;
    }

    /**
     * Indicates whether a lookup field has been configured to trigger on value change.
     *
     * @param businessObjectClass Class for business object to lookup
     * @param attributeName       name of attribute in the business object to check configuration for
     * @return true if field is configured to trigger on value change, false if not.
     */
    public boolean isLookupFieldTriggerOnChange(Class businessObjectClass, String attributeName) {
        boolean triggerOnChange = false;
        if (getLookupFieldDefinition(businessObjectClass, attributeName) != null) {
            triggerOnChange = getLookupFieldDefinition(businessObjectClass, attributeName).isTriggerOnChange();
        }

        return triggerOnChange;
    }

    /**
     * Indicates whether the search and clear buttons should be disabled based on the data dictionary configuration.
     *
     * @param businessObjectClass business object class for lookup definition
     * @return Boolean indicating whether disable search buttons is configured to true in lookup definition.
     */
    public boolean disableSearchButtonsInLookup(Class businessObjectClass) {
        boolean disableSearchButtons = false;

        if (getLookupDefinition(businessObjectClass) != null) {
            disableSearchButtons = getLookupDefinition(businessObjectClass).isDisableSearchButtons();
        }

        return disableSearchButtons;
    }

    /**
     * @param targetClass
     * @param propertyName
     * @return true if the given propertyName names a property of the given class
     * @throws CompletionException if there is a problem accessing the named property on the given class
     */
    public boolean isPropertyOf(Class targetClass, String propertyName) {
        return dataDictionaryService.getDataDictionary().isPropertyOf(targetClass, propertyName);
    }

    /**
     * This method determines the Class of the attributeName passed in. Null will be returned if the member is not
     * available, or if a reflection exception is thrown.
     *
     * @param boClass       Class that the attributeName property exists in.
     * @param attributeName Name of the attribute you want a class for.
     * @return The Class of the attributeName, if the attribute exists on the rootClass. Null otherwise.
     */
    public Class getAttributeClass(Class boClass, String attributeName) {
        return dataDictionaryService.getDataDictionary().getAttributeClass(boClass, attributeName);
    }

    /**
     * This method determines the Class of the elements in the collectionName passed in.
     *
     * @param boClass        Class that the collectionName collection exists in.
     * @param collectionName the name of the collection you want the element class for
     * @return
     */
    public Class getCollectionElementClass(Class boClass, String collectionName) {
        return dataDictionaryService.getDataDictionary().getCollectionElementClass(boClass, collectionName);
    }

    /**
     * @param targetClass
     * @param propertyName
     * @return true if the given propertyName names a Collection property of the given class
     * @throws CompletionException if there is a problem accessing the named property on the given class
     */
    public boolean isCollectionPropertyOf(Class targetClass, String propertyName) {
        return dataDictionaryService.getDataDictionary().isCollectionPropertyOf(targetClass, propertyName);
    }

    /**
     * @param businessObjectClass business object class whose lookup definition information is desired
     * @return LookupSearchService instance associated with the provided {@code businessObjectClass}.
     */
    public LookupSearchService getLookupSearchServiceForLookup(Class businessObjectClass) {
        return getLookupDefinition(businessObjectClass).getLookupSearchService();
    }

    /**
     * @param businessObjectClass business object class whose lookup definition information is desired
     * @return List of LookupAttributeDefinition associated with the provided {@code businessObjectClass}.
     */
    public List<LookupAttributeDefinition> getLookupAttributeDefinitions(Class businessObjectClass) {
        LookupDefinition definition = getLookupDefinition(businessObjectClass);
        if (definition != null) {
            return definition.getLookupAttributeDefinitions();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * @param businessObjectClass business object class whose lookup definition information is desired
     * @param attributeName name of the result attribute definition desired
     * @return LookupAttributeDefinition associated with the provided {@code businessObjectClass}
     * and {@code attributeName}.
     */
    public LookupAttributeDefinition getLookupAttributeDefinition(Class businessObjectClass, String attributeName) {
        LookupDefinition definition = getLookupDefinition(businessObjectClass);
        LookupAttributeDefinition attributeDefinition = null;
        if (definition != null) {
            attributeDefinition = definition.getLookupAttributeDefinition(attributeName);
        }
        return attributeDefinition;
    }

    /**
     * @param businessObjectClass business object class whose lookup definition information is desired
     * @return List of LookupResultDefinition associated with the provided {@code businessObjectClass}.
     */
    public List<LookupResultAttributeDefinition> getLookupResultAttributeDefinitions(Class businessObjectClass) {
        return getLookupDefinition(businessObjectClass).getLookupResultAttributeDefinitions();
    }

    /**
     * @param businessObjectClass business object class whose lookup definition information is desired
     * @param resultAttributeName name of the result attribute definition desired
     * @return LookupResultAttributeDefinition associated with the provided {@code businessObjectClass}
     * and {@code resultAttributeName}.
     */
    public LookupResultAttributeDefinition getLookupResultAttributeDefinition(Class businessObjectClass,
            String resultAttributeName) {
        return getLookupDefinition(businessObjectClass).getLookupResultAttributeDefinition(resultAttributeName);
    }

    /**
     * @param businessObjectClass business object class whose admin service is desired
     * @return BusinessObjectAdminService instance associated with the provided {@code businessObjectClass}.
     */
    public BusinessObjectAdminService getBusinessObjectAdminService(Class businessObjectClass) {
        return getBusinessObjectEntry(businessObjectClass.getName()).getBusinessObjectAdminService();
    }
}
