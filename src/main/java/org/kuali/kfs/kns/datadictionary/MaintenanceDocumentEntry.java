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
package org.kuali.kfs.kns.datadictionary;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.document.MaintenanceDocumentBase;
import org.kuali.kfs.kns.document.authorization.DocumentAuthorizer;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentAuthorizerBase;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentPresentationControllerBase;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.rule.PromptBeforeValidation;
import org.kuali.kfs.kns.rules.MaintenanceDocumentRule;
import org.kuali.kfs.datadictionary.legacy.BusinessObjectDictionaryService;
import org.kuali.kfs.kns.web.derivedvaluesetter.DerivedValuesSetter;
import org.kuali.kfs.krad.datadictionary.DataDictionaryException;
import org.kuali.kfs.krad.datadictionary.ReferenceDefinition;
import org.kuali.kfs.krad.datadictionary.exception.AttributeValidationException;
import org.kuali.kfs.krad.datadictionary.exception.ClassValidationException;
import org.kuali.kfs.krad.datadictionary.exception.DuplicateEntryException;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.document.DocumentPresentationController;
import org.kuali.kfs.krad.maintenance.MaintenanceDocumentAuthorizer;
import org.kuali.kfs.krad.rules.MaintenanceDocumentRuleBase;
import org.kuali.kfs.krad.bo.BusinessObject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Data dictionary entry class for {@link MaintenanceDocument}.
 */
public class MaintenanceDocumentEntry extends DocumentEntry {

    protected List<MaintainableSectionDefinition> maintainableSections = new ArrayList<>();
    protected List<String> lockingKeys = new ArrayList<>();

    protected Map<String, MaintainableSectionDefinition> maintainableSectionMap = new LinkedHashMap<>();

    protected boolean allowsNewOrCopy = true;
    protected String additionalSectionsFile;

    //for issue KULRice3072, to enable PK field copy
    protected boolean preserveLockingKeysOnCopy;

    // for issue KULRice3070, to enable deleting a db record using maintenance doc
    protected boolean allowsRecordDeletion;

    protected boolean translateCodes;

    protected Class<? extends PromptBeforeValidation> promptBeforeValidationClass;
    protected Class<? extends DerivedValuesSetter> derivedValuesSetterClass;
    protected List<String> webScriptFiles = new ArrayList<>(3);
    protected List<HeaderNavigation> headerNavigationList = new ArrayList<>();

    protected boolean sessionDocument;
    protected Class<?> dataObjectClass;
    protected Class<? extends Maintainable> maintainableClass;
    private BusinessObjectDictionaryService businessObjectDictionaryService;

    public MaintenanceDocumentEntry() {
        super();
        setDocumentClass(getStandardDocumentBaseClass());

        documentAuthorizerClass = MaintenanceDocumentAuthorizerBase.class;
        documentPresentationControllerClass = MaintenanceDocumentPresentationControllerBase.class;
    }

    @Override
    public Class<? extends PromptBeforeValidation> getPromptBeforeValidationClass() {
        return promptBeforeValidationClass;
    }

    /**
     * The promptBeforeValidationClass element is the full class name of the java class which determines whether the
     * user should be asked any questions prior to running validation.
     */
    @Override
    public void setPromptBeforeValidationClass(final Class<? extends PromptBeforeValidation> preRulesCheckClass) {
        promptBeforeValidationClass = preRulesCheckClass;
    }

    public Class<? extends Document> getStandardDocumentBaseClass() {
        return MaintenanceDocumentBase.class;
    }

    /**
     * This attribute is used in many contexts, for example, in maintenance docs, it's used to specify the classname
     * of the BO being maintained.
     */
    public void setBusinessObjectClass(final Class<? extends BusinessObject> businessObjectClass) {
        if (businessObjectClass == null) {
            throw new IllegalArgumentException("invalid (null) businessObjectClass");
        }

        setDataObjectClass(businessObjectClass);
    }

    public Class<? extends BusinessObject> getBusinessObjectClass() {
        return (Class<? extends BusinessObject>) getDataObjectClass();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class getEntryClass() {
        return dataObjectClass;
    }

    public Class<? extends Maintainable> getMaintainableClass() {
        return maintainableClass;
    }

    /**
     * @return List of MaintainableSectionDefinition objects contained in this document
     */
    public List<MaintainableSectionDefinition> getMaintainableSections() {
        return maintainableSections;
    }

    /**
     * @return List of all lockingKey fieldNames associated with this LookupDefinition, in the order in which they
     *         were added
     */
    public List<String> getLockingKeyFieldNames() {
        return lockingKeys;
    }

    public boolean getAllowsNewOrCopy() {
        return allowsNewOrCopy;
    }

    /**
     * @param allowsNewOrCopy element contains a value of true or false. If true, this indicates the maintainable
     *                        should allow the new and/or copy maintenance actions.
     */
    public void setAllowsNewOrCopy(final boolean allowsNewOrCopy) {
        this.allowsNewOrCopy = allowsNewOrCopy;
    }

    /**
     * Directly validate simple fields, call completeValidation on Definition fields.
     */
    @Override
    public void completeValidation() {
        if (!MaintenanceDocumentRule.class.isAssignableFrom(getBusinessRulesClass())) {
            throw new DataDictionaryException("ERROR: Business rules class for KNS Maintenance document entry " +
                getBusinessRulesClass().getName() + " does not implement the expected " +
                MaintenanceDocumentRule.class.getName() + " interface.");
        }
        super.completeValidation();

        for (final String lockingKey : lockingKeys) {
            if (!businessObjectDictionaryService.isPropertyOf(dataObjectClass, lockingKey)) {
                throw new AttributeValidationException("unable to find attribute '" + lockingKey +
                        "' for lockingKey in dataObjectClass '" + dataObjectClass.getName());
            }
        }

        for (final ReferenceDefinition reference : defaultExistenceChecks) {
            reference.completeValidation(dataObjectClass, null);
        }

        if (documentAuthorizerClass != null
                && !MaintenanceDocumentAuthorizer.class.isAssignableFrom(documentAuthorizerClass)) {
            throw new ClassValidationException("This maintenance document for '" + getDataObjectClass().getName() +
                    "' has an invalid documentAuthorizerClass ('" + documentAuthorizerClass.getName() + "').  " +
                    "Maintenance Documents must use an implementation of MaintenanceDocumentAuthorizer.");
        }

        for (final MaintainableSectionDefinition maintainableSectionDefinition : maintainableSections) {
            maintainableSectionDefinition.completeValidation(getDataObjectClass(), null);
        }
    }

    @Override
    public String toString() {
        return "MaintenanceDocumentEntry for documentType " + getDocumentTypeName();
    }

    @Deprecated
    public String getAdditionalSectionsFile() {
        return additionalSectionsFile;
    }

    /**
     * The additionalSectionsFile element specifies the name of the location of an additional JSP file to include in
     * the maintenance document after the generation sections but before the notes. The location semantics are those
     * of jsp:include.
     */
    @Deprecated
    public void setAdditionalSectionsFile(final String additionalSectionsFile) {
        this.additionalSectionsFile = additionalSectionsFile;
    }

    public List<String> getLockingKeys() {
        return lockingKeys;
    }

    /**
     * The lockingKeys element specifies a list of fields that comprise a unique key. This is used for record locking
     * during the file maintenance process.
     */
    public void setLockingKeys(final List<String> lockingKeys) {
        for (final String lockingKey : lockingKeys) {
            if (lockingKey == null) {
                throw new IllegalArgumentException("invalid (null) lockingKey");
            }
        }
        this.lockingKeys = lockingKeys;
    }

    /**
     * The maintainableSections elements allows the maintenance document to be presented in sections. Each section can
     * have a different title.
     * <p>
     * JSTL: maintainbleSections is a Map which is accessed by a key of "maintainableSections". This map contains
     * entries with the following keys:
     * "0"   (for first section)
     * "1"   (for second section)
     * etc.
     * The corresponding value for each entry is a maintainableSection ExportMap.
     * @see org.kuali.kfs.kns.datadictionary.exporter.MaintenanceDocumentEntryMapper
     */
    @Deprecated
    public void setMaintainableSections(final List<MaintainableSectionDefinition> maintainableSections) {
        maintainableSectionMap.clear();
        for (final MaintainableSectionDefinition maintainableSectionDefinition : maintainableSections) {
            if (maintainableSectionDefinition == null) {
                throw new IllegalArgumentException("invalid (null) maintainableSectionDefinition");
            }

            final String sectionTitle = maintainableSectionDefinition.getTitle();
            if (maintainableSectionMap.containsKey(sectionTitle)) {
                throw new DuplicateEntryException(
                    "section '" + sectionTitle + "' already defined for maintenanceDocument '" +
                        getDocumentTypeName() + "'");
            }

            maintainableSectionMap.put(sectionTitle, maintainableSectionDefinition);
        }
        this.maintainableSections = maintainableSections;
    }

    public boolean getPreserveLockingKeysOnCopy() {
        return preserveLockingKeysOnCopy;
    }

    public void setPreserveLockingKeysOnCopy(final boolean preserveLockingKeysOnCopy) {
        this.preserveLockingKeysOnCopy = preserveLockingKeysOnCopy;
    }

    public boolean getAllowsRecordDeletion() {
        return allowsRecordDeletion;
    }

    public void setAllowsRecordDeletion(final boolean allowsRecordDeletion) {
        this.allowsRecordDeletion = allowsRecordDeletion;
    }

    @Deprecated
    public boolean isTranslateCodes() {
        return translateCodes;
    }

    @Deprecated
    public void setTranslateCodes(final boolean translateCodes) {
        this.translateCodes = translateCodes;
    }

    @Override
    public Class<? extends DocumentAuthorizer> getDocumentAuthorizerClass() {
        return (Class<? extends DocumentAuthorizer>) super.getDocumentAuthorizerClass();
    }

    @Override
    public Class<? extends DocumentPresentationController> getDocumentPresentationControllerClass() {
        return super.getDocumentPresentationControllerClass();
    }

    @Override
    public List<HeaderNavigation> getHeaderNavigationList() {
        return headerNavigationList;
    }

    @Override
    public List<String> getWebScriptFiles() {
        return webScriptFiles;
    }

    /**
     * The webScriptFile element defines the name of javascript files that are necessary for processing the document.
     * The specified javascript files will be included in the generated html.
     */
    @Override
    public void setWebScriptFiles(final List<String> webScriptFiles) {
        this.webScriptFiles = webScriptFiles;
    }

    /**
     * The headerNavigation element defines a set of additional tabs which will appear on the document.
     */
    @Override
    public void setHeaderNavigationList(final List<HeaderNavigation> headerNavigationList) {
        this.headerNavigationList = headerNavigationList;
    }

    @Override
    public boolean isSessionDocument() {
        return sessionDocument;
    }

    @Override
    public void setSessionDocument(final boolean sessionDocument) {
        this.sessionDocument = sessionDocument;
    }

    @Override
    public Class<? extends DerivedValuesSetter> getDerivedValuesSetterClass() {
        return derivedValuesSetterClass;
    }

    @Override
    public void setDerivedValuesSetterClass(final Class<? extends DerivedValuesSetter> derivedValuesSetter) {
        derivedValuesSetterClass = derivedValuesSetter;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        /*
         * CU Customization (KFSPTS-23970):
         * Added temporary section in the afterPropertiesSet() method to forcibly remove "../" prefixes
         * from any of the "webScriptFiles" entries. This provides a simpler short-term workaround
         * for the FINP-7386 issue (which was fixed in the 2021-02-25 financials patch).
         * 
         * TODO: Remove this entire class overlay when upgrading to the 2021-02-25 financials patch or later.
         */
        if (CollectionUtils.isNotEmpty(webScriptFiles)) {
            webScriptFiles = webScriptFiles.stream()
                    .map(scriptFile -> StringUtils.removeStart(scriptFile, "../"))
                    .collect(Collectors.toCollection(ArrayList::new));
        }
        /*
         * End CU Customization
         */
        if (getBusinessRulesClass() == null || getBusinessRulesClass().equals(MaintenanceDocumentRuleBase.class)) {
            setBusinessRulesClass(org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase.class);
        }
        super.afterPropertiesSet();
    }

    // This attribute is used in many contexts, for example, in maintenance docs, it's used to specify the classname
    // of the BO being maintained.
    public void setDataObjectClass(final Class<?> dataObjectClass) {
        if (dataObjectClass == null) {
            throw new IllegalArgumentException("invalid (null) dataObjectClass");
        }

        this.dataObjectClass = dataObjectClass;
    }

    public Class<?> getDataObjectClass() {
        return dataObjectClass;
    }

    // The maintainableClass element specifies the name of the java class which is responsible for implementing the
    // maintenance logic. The normal one is KualiMaintainableImpl.java.
    public void setMaintainableClass(final Class<? extends Maintainable> maintainableClass) {
        if (maintainableClass == null) {
            throw new IllegalArgumentException("invalid (null) maintainableClass");
        }
        this.maintainableClass = maintainableClass;
    }

    public void setBusinessObjectDictionaryService(
            final BusinessObjectDictionaryService businessObjectDictionaryService) {
        this.businessObjectDictionaryService = businessObjectDictionaryService;
    }
}
