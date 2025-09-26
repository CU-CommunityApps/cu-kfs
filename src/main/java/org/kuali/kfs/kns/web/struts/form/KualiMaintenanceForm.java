/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.kns.web.struts.form;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.ConfigurationException;
import org.kuali.kfs.core.web.format.Formatter;
import org.kuali.kfs.datadictionary.legacy.MaintenanceDocumentDictionaryService;
import org.kuali.kfs.kew.api.WorkflowDocument;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentRestrictions;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.util.FieldUtils;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.datadictionary.exception.UnknownDocumentTypeException;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.KRADServiceLocatorWeb;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;

/**
 * This class is the base action form for all maintenance documents.
 */
public class KualiMaintenanceForm extends KualiDocumentFormBase {

    private static final Logger LOG = LogManager.getLogger();

    protected static final long serialVersionUID = 1L;

    protected String businessObjectClassName;
    protected String description;
    protected boolean readOnly;
    protected Map<String, String> oldMaintainableValues;
    protected Map<String, String> newMaintainableValues;
    protected String maintenanceAction;

    /**
     * Used to indicate which result set we're using when refreshing/returning from a multi-value lookup
     */
    protected String lookupResultsSequenceNumber;
    /**
     * The type of result returned by the multi-value lookup
     * <p>
     * TODO: to be persisted in the lookup results service instead?
     */
    protected String lookupResultsBOClassName;

    /**
     * The name of the collection looked up (by a multiple value lookup)
     */
    protected String lookedUpCollectionName;

    protected MaintenanceDocumentRestrictions authorizations;

    @Override
    public void addRequiredNonEditableProperties() {
        super.addRequiredNonEditableProperties();
        registerRequiredNonEditableProperty(KRADConstants.BUSINESS_OBJECT_CLASS_ATTRIBUTE);
        registerRequiredNonEditableProperty(KRADConstants.LOOKUP_RESULTS_BO_CLASS_NAME);
        registerRequiredNonEditableProperty(KRADConstants.LOOKED_UP_COLLECTION_NAME);
        registerRequiredNonEditableProperty(KRADConstants.LOOKUP_RESULTS_SEQUENCE_NUMBER);
        registerRequiredNonEditableProperty(KRADConstants.FIELD_NAME_TO_FOCUS_ON_AFTER_SUBMIT);
    }

    /**
     * Override the default method to add the if statement which can't be called until after parameters from a
     * multipart request have been made accessible, but which must be called before the parameter values are used to
     * instantiate and populate business objects.
     *
     * @param requestParameters
     */
    @Override
    public void postprocessRequestParameters(final Map requestParameters) {
        super.postprocessRequestParameters(requestParameters);

        String docTypeName = null;
        final String[] docTypeNames = (String[]) requestParameters.get(KRADConstants.DOCUMENT_TYPE_NAME);
        if (docTypeNames != null && docTypeNames.length > 0) {
            docTypeName = docTypeNames[0];
        }

        if (StringUtils.isNotBlank(docTypeName)) {
            if (getDocument() == null) {
                setDocTypeName(docTypeName);
                final Class documentClass = KNSServiceLocator.getDataDictionaryService()
                        .getDocumentClassByTypeName(docTypeName);
                if (documentClass == null) {
                    throw new UnknownDocumentTypeException("unable to get class for unknown documentTypeName '" +
                            docTypeName + "'");
                }
                if (!MaintenanceDocument.class.isAssignableFrom(documentClass)) {
                    throw new ConfigurationException("Document class '" + documentClass + "' is not assignable to '"
                                                     + FinancialSystemMaintenanceDocument.class + "'");
                }
                final Document document;
                try {
                    final Class[] defaultConstructor = {String.class};
                    final Constructor cons = documentClass.getConstructor(defaultConstructor);
                    if (ObjectUtils.isNull(cons)) {
                        throw new ConfigurationException("Could not find constructor with document type name " +
                                "parameter needed for Maintenance Document Base class");
                    }
                    document = (Document) cons.newInstance(docTypeName);
                } catch (SecurityException | IllegalAccessException | InstantiationException |
                        IllegalArgumentException | InvocationTargetException e) {
                    throw new RuntimeException("Error instantiating Maintenance Document", e);
                } catch (final NoSuchMethodException e) {
                    throw new RuntimeException("Error instantiating Maintenance Document: No constructor with " +
                            "String parameter found", e);
                }
                if (document == null) {
                    throw new RuntimeException("Unable to instantiate document with type name '" + docTypeName +
                            "' and document class '" + documentClass + "'");
                }
                setDocument(document);
            }
        }
    }

    /**
     * Hook into populate so we can set the maintenance documents and feed the field values to its maintainables.
     */
    @Override
    public void populate(final HttpServletRequest request) {
        super.populate(request);

        // document type name is null on start, otherwise should be here
        if (StringUtils.isNotBlank(getDocTypeName())) {
            final Map<String, String> localOldMaintainableValues = new HashMap<>();
            final Map<String, String> localNewMaintainableValues = new HashMap<>();
            Map<String, String> localNewCollectionValues = new HashMap<>();
            for (final Enumeration i = request.getParameterNames(); i.hasMoreElements(); ) {
                final String parameter = (String) i.nextElement();
                if (parameter.toUpperCase(Locale.US).startsWith(KRADConstants.MAINTENANCE_OLD_MAINTAINABLE.toUpperCase(Locale.US))) {
                    if (shouldPropertyBePopulatedInForm(parameter, request)) {
                        final String propertyName = parameter.substring(KRADConstants.MAINTENANCE_OLD_MAINTAINABLE.length());
                        localOldMaintainableValues.put(propertyName, request.getParameter(parameter));
                    }
                }
                if (parameter.toUpperCase(Locale.US).startsWith(KRADConstants.MAINTENANCE_NEW_MAINTAINABLE.toUpperCase(Locale.US))) {
                    if (shouldPropertyBePopulatedInForm(parameter, request)) {
                        final String propertyName = parameter.substring(KRADConstants.MAINTENANCE_NEW_MAINTAINABLE.length());
                        localNewMaintainableValues.put(propertyName, request.getParameter(parameter));
                    }
                }
            }

            // now, get all add lines and store them to a separate map for use in a separate call to the maintainable
            for (final Map.Entry<String, String> entry : localNewMaintainableValues.entrySet()) {
                final String key = entry.getKey();
                if (key.startsWith(KRADConstants.MAINTENANCE_ADD_PREFIX)) {
                    localNewCollectionValues.put(key.substring(KRADConstants.MAINTENANCE_ADD_PREFIX.length()),
                        entry.getValue());
                }
            }
            LOG.debug("checked for add line parameters - got: {}", localNewCollectionValues);

            newMaintainableValues = localNewMaintainableValues;
            oldMaintainableValues = localOldMaintainableValues;

            final MaintenanceDocument maintenanceDocument = (MaintenanceDocument) getDocument();

            GlobalVariables.getMessageMap().addToErrorPath("document.oldMaintainableObject");
            maintenanceDocument.getOldMaintainableObject().populateBusinessObject(localOldMaintainableValues,
                    maintenanceDocument, getMethodToCall());
            GlobalVariables.getMessageMap().removeFromErrorPath("document.oldMaintainableObject");

            GlobalVariables.getMessageMap().addToErrorPath("document.newMaintainableObject");
            // update the main object
            final Map cachedValues =
                maintenanceDocument.getNewMaintainableObject().populateBusinessObject(localNewMaintainableValues,
                        maintenanceDocument, getMethodToCall());

            // update add lines
            localNewCollectionValues = KRADServiceLocatorWeb.getMaintenanceDocumentService()
                    .resolvePrincipalNamesToPrincipalIds(
                            maintenanceDocument.getNewMaintainableObject().getBusinessObject(),
                            localNewCollectionValues);
            cachedValues.putAll(maintenanceDocument.getNewMaintainableObject().populateNewCollectionLines(
                    localNewCollectionValues, maintenanceDocument, getMethodToCall()));
            GlobalVariables.getMessageMap().removeFromErrorPath("document.newMaintainableObject");

            if (cachedValues.size() > 0) {
                GlobalVariables.getMessageMap().putError(KRADConstants.DOCUMENT_ERRORS,
                        KFSKeyConstants.ERROR_DOCUMENT_MAINTENANCE_FORMATTING_ERROR);
                for (final Object key : cachedValues.keySet()) {
                    final String propertyName = (String) key;
                    final String value = (String) cachedValues.get(propertyName);
                    cacheUnconvertedValue(KRADConstants.MAINTENANCE_NEW_MAINTAINABLE + propertyName, value);
                }
            }
        }
    }

    /**
     * Merges rows of old and new for each section (tab) of the ui. Also, renames fields to prevent naming conflicts
     * and does setting of read only fields.
     *
     * @return Returns the maintenanceSections.
     */
    public List getSections() {
        if (getDocument() == null) {
            throw new RuntimeException("Document not set in maintenance form.");
        }
        if (((MaintenanceDocument) getDocument()).getNewMaintainableObject() == null) {
            throw new RuntimeException("New maintainable not set in document.");
        }
        if ((KRADConstants.MAINTENANCE_EDIT_ACTION.equals(getMaintenanceAction())
            || KRADConstants.MAINTENANCE_COPY_ACTION.equals(getMaintenanceAction())
            || KRADConstants.MAINTENANCE_DELETE_ACTION.equals(getMaintenanceAction()))
            && ((MaintenanceDocument) getDocument()).getOldMaintainableObject() == null) {
            throw new RuntimeException("Old maintainable not set in document.");
        }

        // get business object being maintained and its keys
        final List keyFieldNames = KNSServiceLocator.getBusinessObjectMetaDataService()
                .listPrimaryKeyFieldNames(((MaintenanceDocument) getDocument()).getNewMaintainableObject()
                        .getBusinessObject()
                        .getClass());

        // sections for maintenance document
        final Maintainable oldMaintainable =
                ((MaintenanceDocument) getDocument()).getOldMaintainableObject();
        oldMaintainable.setMaintenanceAction(getMaintenanceAction());
        final List oldMaintSections = oldMaintainable.getSections((MaintenanceDocument) getDocument(), null);

        final Maintainable newMaintainable =
                ((MaintenanceDocument) getDocument()).getNewMaintainableObject();
        newMaintainable.setMaintenanceAction(getMaintenanceAction());
        final List newMaintSections = newMaintainable.getSections((MaintenanceDocument) getDocument(), oldMaintainable);
        final WorkflowDocument workflowDocument = getDocument().getDocumentHeader().getWorkflowDocument();
        final String documentStatus = workflowDocument.getStatus().getCode();
        final String documentInitiatorPrincipalId = workflowDocument.getInitiatorPrincipalId();

        // mesh sections for proper jsp display
        return FieldUtils.meshSections(oldMaintSections, newMaintSections, keyFieldNames, getMaintenanceAction(),
                isReadOnly(), authorizations, documentStatus, documentInitiatorPrincipalId);
    }

    public String getMaintenanceAction() {
        return maintenanceAction;
    }

    public String getBusinessObjectClassName() {
        return businessObjectClassName;
    }

    public void setBusinessObjectClassName(final String businessObjectClassName) {
        this.businessObjectClassName = businessObjectClassName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    public Map getNewMaintainableValues() {
        return newMaintainableValues;
    }

    public Map getOldMaintainableValues() {
        return oldMaintainableValues;
    }

    public void setMaintenanceAction(final String maintenanceAction) {
        this.maintenanceAction = maintenanceAction;
    }

    public MaintenanceDocumentRestrictions getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(final MaintenanceDocumentRestrictions authorizations) {
        this.authorizations = authorizations;
    }

    public void setNewMaintainableValues(final Map newMaintainableValues) {
        this.newMaintainableValues = newMaintainableValues;
    }

    public void setOldMaintainableValues(final Map oldMaintainableValues) {
        this.oldMaintainableValues = oldMaintainableValues;
    }

    public String getLookupResultsSequenceNumber() {
        return lookupResultsSequenceNumber;
    }

    public void setLookupResultsSequenceNumber(final String lookupResultsSequenceNumber) {
        this.lookupResultsSequenceNumber = lookupResultsSequenceNumber;
    }

    public String getLookupResultsBOClassName() {
        return lookupResultsBOClassName;
    }

    public void setLookupResultsBOClassName(final String lookupResultsBOClassName) {
        this.lookupResultsBOClassName = lookupResultsBOClassName;
    }

    public String getLookedUpCollectionName() {
        return lookedUpCollectionName;
    }

    public void setLookedUpCollectionName(final String lookedUpCollectionName) {
        this.lookedUpCollectionName = lookedUpCollectionName;
    }

    public String getAdditionalSectionsFile() {
        if (businessObjectClassName != null) {
            final MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService =
                    KNSServiceLocator.getMaintenanceDocumentDictionaryService();
            return maintenanceDocumentDictionaryService.getMaintenanceDocumentEntry(businessObjectClassName)
                    .getAdditionalSectionsFile();
        } else {
            final MaintenanceDocumentDictionaryService maintenanceDocumentDictionaryService = KNSServiceLocator
                .getMaintenanceDocumentDictionaryService();
            return maintenanceDocumentDictionaryService.getMaintenanceDocumentEntry(getDocTypeName())
                    .getAdditionalSectionsFile();
        }
    }

    /**
     * This overridden method handles the case where maint doc properties do not reflect the true nature of the
     */
    @Override
    public String retrieveFormValueForLookupInquiryParameters(final String parameterName, final String parameterValueLocation) {
        final MaintenanceDocument maintDoc = (MaintenanceDocument) getDocument();
        if (parameterValueLocation.toLowerCase(Locale.US).startsWith(KRADConstants.MAINTENANCE_OLD_MAINTAINABLE.toLowerCase(Locale.US))) {
            final String propertyName = parameterValueLocation.substring(KRADConstants.MAINTENANCE_OLD_MAINTAINABLE.length());
            if (maintDoc.getOldMaintainableObject() != null
                    && maintDoc.getOldMaintainableObject().getBusinessObject() != null) {
                final Object parameterValue = ObjectUtils.getPropertyValue(
                        maintDoc.getOldMaintainableObject().getBusinessObject(), propertyName);
                if (parameterValue == null) {
                    return null;
                }
                if (parameterValue instanceof String) {
                    return (String) parameterValue;
                }
                final Formatter formatter = Formatter.getFormatter(parameterValue.getClass());
                return (String) formatter.format(parameterValue);
            }
        }
        if (parameterValueLocation.toLowerCase(Locale.US).startsWith(KRADConstants.MAINTENANCE_NEW_MAINTAINABLE.toLowerCase(Locale.US))) {
            // remove MAINT_NEW_MAINT from the pVL
            String propertyName = parameterValueLocation.substring(KRADConstants.MAINTENANCE_NEW_MAINTAINABLE.length());
            final String addPrefix = KRADConstants.ADD_PREFIX.toLowerCase(Locale.US) + ".";

            if (propertyName.toLowerCase(Locale.US).startsWith(addPrefix)) {
                // remove addPrefix from the propertyName
                propertyName = propertyName.substring(addPrefix.length());
                final String collectionName = parseAddCollectionName(propertyName);
                // remove collectionName from pN
                propertyName = propertyName.substring(collectionName.length());
                if (propertyName.startsWith(".")) {
                    // strip beginning "."
                    propertyName = propertyName.substring(1);
                }
                final PersistableBusinessObject newCollectionLine =
                    maintDoc.getNewMaintainableObject().getNewCollectionLine(collectionName);
                final Object parameterValue = ObjectUtils.getPropertyValue(newCollectionLine, propertyName);
                if (parameterValue == null) {
                    return null;
                }
                if (parameterValue instanceof String) {
                    return (String) parameterValue;
                }
                final Formatter formatter = Formatter.getFormatter(parameterValue.getClass());
                return (String) formatter.format(parameterValue);
            } else if (maintDoc.getNewMaintainableObject() != null
                    && maintDoc.getNewMaintainableObject().getBusinessObject() != null) {
                final Object parameterValue = ObjectUtils.getPropertyValue(
                        maintDoc.getNewMaintainableObject().getBusinessObject(), propertyName);
                if (parameterValue == null) {
                    return null;
                }
                if (parameterValue instanceof String) {
                    return (String) parameterValue;
                }
                final Formatter formatter = Formatter.getFormatter(parameterValue.getClass());
                return (String) formatter.format(parameterValue);
            }
        }
        return super.retrieveFormValueForLookupInquiryParameters(parameterName, parameterValueLocation);
    }

    /**
     * This method returns the collection name (including nested collections) from a propertyName string
     *
     * @param propertyName a parameterValueLocation w/ KRADConstants.MAINTENANCE_NEW_MAINTAINABLE +
     *                     KRADConstants.ADD_PREFIX + "." stripped off the front
     * @return the collectionName
     */
    protected String parseAddCollectionName(final String propertyName) {
        final StringBuilder collectionNameBuilder = new StringBuilder();

        boolean firstPathElement = true;
        for (final String pathElement : propertyName.split("\\.")) {
            if (StringUtils.isNotBlank(pathElement)) {
                if (firstPathElement) {
                    firstPathElement = false;
                } else {
                    collectionNameBuilder.append(".");
                }
                collectionNameBuilder.append(pathElement);
                if (!(pathElement.endsWith("]") && pathElement.contains("["))) {
                    break;
                }
            }
        }
        return collectionNameBuilder.toString();
    }

    @Override
    public boolean shouldPropertyBePopulatedInForm(
        final String requestParameterName, final HttpServletRequest request) {
        // CU Customization: Exclude "additionalScriptFiles" and "additionalScriptFile[x]" from being editable.
        if (StringUtils.startsWithIgnoreCase(requestParameterName, CUKFSPropertyConstants.ADDITIONAL_SCRIPT_FILE_PREFIX)) {
            return false;
        }
        // End CU Customization
        // the user clicked on a document initiation link add delete check for 3070
        final String methodToCallActionName = request.getParameter(KRADConstants.DISPATCH_REQUEST_PARAMETER);
        if (StringUtils.equals(methodToCallActionName, KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL) ||
            StringUtils.equals(methodToCallActionName, KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL) ||
            StringUtils.equals(methodToCallActionName, KRADConstants.MAINTENANCE_NEW_METHOD_TO_CALL) ||
            StringUtils.equals(methodToCallActionName, KRADConstants.MAINTENANCE_NEWWITHEXISTING_ACTION) ||
            StringUtils.equals(methodToCallActionName, KRADConstants.MAINTENANCE_DELETE_METHOD_TO_CALL)) {
            return true;
        }
        if (StringUtils.indexOf(methodToCallActionName, KRADConstants.TOGGLE_INACTIVE_METHOD) == 0) {
            return true;
        }
        return super.shouldPropertyBePopulatedInForm(requestParameterName, request);
    }

    @Override
    public boolean shouldMethodToCallParameterBeUsed(
        final String methodToCallParameterName,
        final String methodToCallParameterValue, final HttpServletRequest request) {
        // the user clicked on a document initiation link
        if (StringUtils.equals(methodToCallParameterValue, KRADConstants.MAINTENANCE_COPY_METHOD_TO_CALL) ||
            StringUtils.equals(methodToCallParameterValue, KRADConstants.MAINTENANCE_EDIT_METHOD_TO_CALL) ||
            StringUtils.equals(methodToCallParameterValue, KRADConstants.MAINTENANCE_NEW_METHOD_TO_CALL) ||
            StringUtils.equals(methodToCallParameterValue, KRADConstants.MAINTENANCE_NEWWITHEXISTING_ACTION) ||
            StringUtils.equals(methodToCallParameterValue, KRADConstants.MAINTENANCE_DELETE_METHOD_TO_CALL)) {
            return true;
        }
        if (StringUtils.indexOf(methodToCallParameterName, KRADConstants.DISPATCH_REQUEST_PARAMETER + "." +
                KRADConstants.TOGGLE_INACTIVE_METHOD) == 0) {
            return true;
        }
        return super.shouldMethodToCallParameterBeUsed(methodToCallParameterName,
            methodToCallParameterValue, request);
    }
}
