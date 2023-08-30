package edu.cornell.kfs.rass.batch.service.impl;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.util.ErrorMessage;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Primaryable;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AutoPopulatingList;

import edu.cornell.kfs.fp.CuFPConstants;
import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;
import edu.cornell.kfs.rass.RassKeyConstants;
import edu.cornell.kfs.rass.batch.PendingDocumentTracker;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResult;
import edu.cornell.kfs.rass.batch.RassListPropertyDefinition;
import edu.cornell.kfs.rass.batch.RassObjectTranslationDefinition;
import edu.cornell.kfs.rass.batch.RassPropertyDefinition;
import edu.cornell.kfs.rass.batch.RassSubObjectDefinition;
import edu.cornell.kfs.rass.batch.RassValueConverter;
import edu.cornell.kfs.rass.batch.service.RassUpdateService;
import edu.cornell.kfs.rass.batch.xml.RassXmlObject;
import edu.cornell.kfs.rass.util.RassUtil;
import edu.cornell.kfs.sys.CUKFSConstants;

public class RassUpdateServiceImpl implements RassUpdateService {

    private static final Logger LOG = LogManager.getLogger(RassUpdateServiceImpl.class);

    protected DocumentService documentService;
    protected DataDictionaryService dataDictionaryService;
    protected ConfigurationService configurationService;
    protected RouteHeaderService routeHeaderService;

    protected long documentStatusCheckDelayMillis;
    protected int maxStatusCheckAttempts;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public <T extends RassXmlObject, R extends PersistableBusinessObject> RassBusinessObjectUpdateResult<R> processObject(T xmlObject,
            RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker) {
        RassBusinessObjectUpdateResult<R> result;
        try {
            LOG.info("processObject, Processing " + objectDefinition.printObjectLabelAndKeys(xmlObject));
            waitForMatchingPriorDocumentsToFinish(xmlObject, objectDefinition, documentTracker);
            R existingObject = objectDefinition.findExistingObject(xmlObject);
            if (ObjectUtils.isNull(existingObject)) {
                result = createObjectAndMaintenanceDocument(xmlObject, objectDefinition);
            } else {
                materializeProxiedCollectionsOnExistingObject(existingObject);
                result = updateObject(xmlObject, existingObject, objectDefinition);
            }
            LOG.info("processObject, Successfully processed " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        } catch (RuntimeException e) {
            LOG.error("processObject, Failed to process update for " + objectDefinition.printObjectLabelAndKeys(xmlObject), e);
            result = new RassBusinessObjectUpdateResult<>(objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(xmlObject),
                    RassObjectUpdateResultCode.ERROR, e.getMessage());
        }
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void waitForRemainingGeneratedDocumentsToFinish(PendingDocumentTracker documentTracker) {
        try {
            LOG.info("waitForRemainingGeneratedDocumentsToFinish, Waiting for pending maintenance documents to finish routing");
            for (String objectKey : documentTracker.getIdsForAllObjectsWithTrackedDocuments()) {
                if (!documentTracker.didObjectFailToUpdate(objectKey)) {
                    String documentId = documentTracker.getTrackedDocumentId(objectKey);
                    waitForDocumentToFinishRoutingQuietly(documentId);
                }
                documentTracker.stopTrackingDocumentForObject(objectKey);
            }
            LOG.info("waitForRemainingGeneratedDocumentsToFinish, Finished waiting for maintenance documents");
        } catch (RuntimeException e) {
            LOG.error("waitForRemainingGeneratedDocumentsToFinish, Unexpected error while checking document route statuses", e);
        }
    }

    protected void materializeProxiedCollectionsOnExistingObject(Object existingObject) {
        try {
            ObjectUtils.materializeUpdateableCollections(existingObject);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> void waitForMatchingPriorDocumentsToFinish(T xmlObject,
            RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker) {
        List<String> objectKeys = objectDefinition.getKeysOfUpstreamObjectUpdatesToWaitFor(xmlObject);
        for (String objectKey : objectKeys) {
            if (documentTracker.didObjectFailToUpdate(objectKey)) {
                throw new RuntimeException("Cannot proceed with processing object because an update failure was detected for "
                        + RassUtil.getSimpleClassNameFromClassAndKeyIdentifier(objectKey) + " with ID " + RassUtil.getKeyFromClassAndKeyIdentifier(objectKey));
            }

            String documentId = documentTracker.getTrackedDocumentId(objectKey);
            if (StringUtils.isNotBlank(documentId)) {
                waitForDocumentToFinishRouting(documentId);
                documentTracker.stopTrackingDocumentForObject(objectKey);
            }
        }
        waitForMatchingPrimaryObjectUpdateToFinishRoutingQuietly(xmlObject, objectDefinition, documentTracker);
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> void waitForMatchingPrimaryObjectUpdateToFinishRoutingQuietly(
            T xmlObject, RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker) {
        String objectKey = objectDefinition.getKeyOfPrimaryObjectUpdateToWaitFor(xmlObject);
        if (documentTracker.didObjectFailToUpdate(objectKey)) {
            LOG.warn("waitForMatchingPrimaryObjectUpdateToFinishRoutingQuietly, An update failure was detected for "
                    + RassUtil.getSimpleClassNameFromClassAndKeyIdentifier(objectKey) + " with ID "
                    + RassUtil.getKeyFromClassAndKeyIdentifier(objectKey)
                    + " but will proceed with a new update attempt anyway");
            documentTracker.stopTrackingFailedUpdateForObject(objectKey);
        } else {
            String documentId = documentTracker.getTrackedDocumentId(objectKey);
            if (StringUtils.isNotBlank(documentId)) {
                waitForDocumentToFinishRoutingQuietly(documentId);
                documentTracker.stopTrackingDocumentForObject(objectKey);
            }
        }
    }

    protected void waitForDocumentToFinishRoutingQuietly(String documentId) {
        try {
            waitForDocumentToFinishRouting(documentId);
        } catch (RuntimeException e) {
            LOG.error("waitForDocumentToFinishRoutingQuietly, Unexpected error encountered when waiting for document " + documentId
                    + "to finish routing; will skip waiting for this document further", e);
        }
    }

    protected void waitForDocumentToFinishRouting(String documentId) {
        int timesChecked = 0;
        while (shouldKeepWaitingForDocument(documentId)) {
            timesChecked++;
            if (timesChecked >= maxStatusCheckAttempts) {
                throw new RuntimeException("Document finalization checking timed out for document " + documentId);
            }

            try {
                Thread.sleep(documentStatusCheckDelayMillis);
            } catch (InterruptedException e) {
                LOG.warn("waitForDocumentToFinishRouting, Document-checking delay was interrupted", e);
            }
        }
    }

    protected boolean shouldKeepWaitingForDocument(String documentId) {
        String documentStatus = routeHeaderService.getDocumentStatus(documentId);
        switch (documentStatus) {
            case KewApiConstants.ROUTE_HEADER_FINAL_CD:
                return false;
            case KewApiConstants.ROUTE_HEADER_EXCEPTION_CD:
            case KewApiConstants.ROUTE_HEADER_CANCEL_CD:
            case KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD:
                throw new RuntimeException("Document " + documentId + " entered an unexpected unsuccessful status of " + documentStatus);
            default:
                return true;
        }
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> R createObject(T xmlObject,
            RassObjectTranslationDefinition<T, R> objectDefinition) {
        LOG.info("createObject, Creating " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        Supplier<R> businessObjectGenerator = () -> createMinimalObject(objectDefinition.getBusinessObjectClass());
        R businessObject = buildAndPopulateBusinessObjectFromPojo(xmlObject, businessObjectGenerator, objectDefinition, KRADConstants.MAINTENANCE_NEW_ACTION);
        objectDefinition.processCustomTranslationForBusinessObjectCreate(xmlObject, businessObject);
        return businessObject;
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> RassBusinessObjectUpdateResult<R> createObjectAndMaintenanceDocument(T xmlObject,
            RassObjectTranslationDefinition<T, R> objectDefinition) {
        LOG.info("createObjectAndMaintenanceDocument, Creating " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        if (!objectDefinition.businessObjectCreateIsPermitted(xmlObject)) {
            LOG.info("createObjectAndMaintenanceDocument, Create not permitted for " + objectDefinition.printObjectLabelAndKeys(xmlObject)
                    + " so any changes to it will be skipped");
            return new RassBusinessObjectUpdateResult<>(objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(xmlObject),
                    RassObjectUpdateResultCode.SKIPPED);
        }
        R businessObject = createObject(xmlObject, objectDefinition);
        Pair<R, R> pairWithNewObjectOnly = Pair.of(null, businessObject);
        MaintenanceDocument maintenanceDocument = createAndRouteMaintenanceDocument(pairWithNewObjectOnly, KRADConstants.MAINTENANCE_NEW_ACTION,
                objectDefinition);
        LOG.info("createObjectAndMaintenanceDocument, Successfully routed document " + maintenanceDocument.getDocumentNumber() + " to create "
                + objectDefinition.printObjectLabelAndKeys(xmlObject));
        return new RassBusinessObjectUpdateResult<>(objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(businessObject),
                maintenanceDocument.getDocumentNumber(), RassObjectUpdateResultCode.SUCCESS_NEW, StringUtils.EMPTY);
    }

    protected <R extends PersistableBusinessObject> R createMinimalObject(Class<R> businessObjectClass) {
        try {
            return businessObjectClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> RassBusinessObjectUpdateResult<R> updateObject(T xmlObject, R oldBusinessObject,
            RassObjectTranslationDefinition<T, R> objectDefinition) {
        LOG.info("updateObject, Updating " + objectDefinition.printObjectLabelAndKeys(xmlObject));

        if (!objectDefinition.businessObjectEditIsPermitted(xmlObject, oldBusinessObject)) {
            LOG.info("updateObject, Updates are not permitted for " + objectDefinition.printObjectLabelAndKeys(xmlObject)
                    + " so any changes to it will be skipped");
            return new RassBusinessObjectUpdateResult<>(objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(xmlObject),
                    RassObjectUpdateResultCode.SKIPPED);
        }

        Supplier<R> businessObjectCopier = () -> deepCopyObject(oldBusinessObject);
        R newBusinessObject = buildAndPopulateBusinessObjectFromPojo(xmlObject, businessObjectCopier, objectDefinition, KRADConstants.MAINTENANCE_EDIT_ACTION);
        updateOldBusinessObjectForMaintenanceDocumentCompatibility(oldBusinessObject, newBusinessObject, objectDefinition);
        objectDefinition.processCustomTranslationForBusinessObjectEdit(xmlObject, oldBusinessObject, newBusinessObject);

        if (businessObjectWasUpdatedByXml(oldBusinessObject, newBusinessObject, objectDefinition)) {
            Pair<R, R> oldAndNewBos = Pair.of(oldBusinessObject, newBusinessObject);
            MaintenanceDocument maintenanceDocument = createAndRouteMaintenanceDocument(oldAndNewBos, KRADConstants.MAINTENANCE_EDIT_ACTION, objectDefinition);
            LOG.info("updateObject, Successfully routed document " + maintenanceDocument.getDocumentNumber() + " to edit "
                    + objectDefinition.printObjectLabelAndKeys(xmlObject));
            return new RassBusinessObjectUpdateResult<>(objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(newBusinessObject),
                    maintenanceDocument.getDocumentNumber(), RassObjectUpdateResultCode.SUCCESS_EDIT, StringUtils.EMPTY);
        } else {
            LOG.info("updateObject, Skipping updates for " + objectDefinition.printObjectLabelAndKeys(xmlObject)
                    + " because either no changes were made or changes were only made to the truncated data portions");
            return new RassBusinessObjectUpdateResult<>(objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(newBusinessObject),
                    RassObjectUpdateResultCode.SKIPPED);
        }
    }

    @SuppressWarnings("unchecked")
    protected <R extends PersistableBusinessObject> R deepCopyObject(R businessObject) {
        return (R) ObjectUtils.deepCopy(businessObject);
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> R buildAndPopulateBusinessObjectFromPojo(T xmlObject,
            Supplier<R> businessObjectSupplier, RassObjectTranslationDefinition<T, R> objectDefinition, String maintenanceAction) {
        Class<R> businessObjectClass = objectDefinition.getBusinessObjectClass();
        R businessObject = businessObjectSupplier.get();
        List<String> missingRequiredFields = new ArrayList<>();
        
        for (RassPropertyDefinition propertyMapping : objectDefinition.getPropertyMappingsApplicableForAction(maintenanceAction)) {
            Object xmlPropertyValue = ObjectPropertyUtils.getPropertyValue(xmlObject, propertyMapping.getXmlPropertyName());
            RassValueConverter valueConverter = propertyMapping.getValueConverter();
            Object convertedValue = valueConverter.convert(businessObjectClass, propertyMapping, xmlPropertyValue);
            if (convertedValue == null && propertyMapping.isRequired()) {
                missingRequiredFields.add(propertyMapping.getXmlPropertyName());
            } else if (propertyMapping instanceof RassListPropertyDefinition) {
                RassListPropertyDefinition listPropertyMapping = (RassListPropertyDefinition) propertyMapping;
                List<?> newSubObjects = (List<?>) convertedValue;
                copyForeignKeyValuesToNewSubObjects(businessObject, listPropertyMapping, newSubObjects);
                mergeBusinessObjectListProperty(businessObject, (RassListPropertyDefinition) propertyMapping, (List<?>) convertedValue);
            } else {
                ObjectPropertyUtils.setPropertyValue(businessObject, propertyMapping.getBoPropertyName(), convertedValue);
            }
        }
        
        if (!missingRequiredFields.isEmpty()) {
            throw new RuntimeException(objectDefinition.printObjectLabelAndKeys(xmlObject) + " is missing values for the following required fields: "
                    + missingRequiredFields.toString());
        }
        if (StringUtils.equalsIgnoreCase(maintenanceAction, KRADConstants.MAINTENANCE_NEW_ACTION)) {
            objectDefinition.makeBusinessObjectActiveIfApplicable(xmlObject, businessObject);
        }
        return businessObject;
    }

    protected void copyForeignKeyValuesToNewSubObjects(Object businessObject, RassListPropertyDefinition listPropertyMapping, List<?> newSubObjects) {
        listPropertyMapping.getForeignKeyMappings().forEach((parentPropertyName, subObjectPropertyName) -> {
            Object foreignKeyValue = ObjectPropertyUtils.getPropertyValue(businessObject, parentPropertyName);
            for (Object newSubObject : newSubObjects) {
                ObjectPropertyUtils.setPropertyValue(newSubObject, subObjectPropertyName, foreignKeyValue);
            }
        });
    }

    protected void mergeBusinessObjectListProperty(Object businessObject, RassListPropertyDefinition listPropertyMapping, List<?> newSubObjects) {
        RassSubObjectDefinition subObjectDefinition = listPropertyMapping.getSubObjectDefinition();
        List<Object> existingList = ObjectPropertyUtils.getPropertyValue(businessObject, listPropertyMapping.getBoPropertyName());
        Set<Integer> indexesOfSubObjectsToInactivate = IntStream.range(0, existingList.size()).mapToObj(Integer::valueOf)
                .collect(Collectors.toCollection(HashSet::new));

        for (Object newSubObject : newSubObjects) {
            int i = 0;
            while (i < existingList.size() && !subObjectPrimaryKeysMatch(subObjectDefinition, existingList.get(i), newSubObject)) {
                i++;
            }

            if (i < existingList.size()) {
                mergeNonPrimaryKeyUpdatesIntoExistingSubObject(subObjectDefinition, existingList.get(i), newSubObject);
                indexesOfSubObjectsToInactivate.remove(i);
            } else {
                existingList.add(newSubObject);
            }
        }

        for (Integer indexOfObjectToInactivate : indexesOfSubObjectsToInactivate) {
            Object existingSubObject = existingList.get(indexOfObjectToInactivate);
            if (existingSubObject instanceof MutableInactivatable) {
                ((MutableInactivatable) existingSubObject).setActive(false);
            }
            if (existingSubObject instanceof Primaryable && StringUtils.isNotBlank(subObjectDefinition.getPrimaryIndicatorPropertyName())) {
                ObjectPropertyUtils.setPropertyValue(existingSubObject, subObjectDefinition.getPrimaryIndicatorPropertyName(), Boolean.FALSE);
            }
        }
    }

    protected void mergeNonPrimaryKeyUpdatesIntoExistingSubObject(RassSubObjectDefinition subObjectDefinition, Object oldBo, Object newBo) {
        subObjectDefinition.getNonKeyPropertyNames().stream().forEach(propertyName -> {
            Object newValue = ObjectPropertyUtils.getPropertyValue(newBo, propertyName);
            ObjectPropertyUtils.setPropertyValue(oldBo, propertyName, newValue);
        });
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> void updateOldBusinessObjectForMaintenanceDocumentCompatibility(
            R oldBusinessObject, R newBusinessObject, RassObjectTranslationDefinition<T, R> objectDefinition) {
        objectDefinition.getPropertyMappings().stream().filter(propertyDefinition -> propertyDefinition instanceof RassListPropertyDefinition)
                .map(propertyDefinition -> (RassListPropertyDefinition) propertyDefinition)
                .forEach(listPropertyDefinition -> addFillerSubObjectsToListOnOldBusinessObjectIfNeeded(oldBusinessObject, newBusinessObject,
                        listPropertyDefinition));
    }

    protected <R extends PersistableBusinessObject> void addFillerSubObjectsToListOnOldBusinessObjectIfNeeded(R oldBusinessObject, R newBusinessObject,
            RassListPropertyDefinition listPropertyDefinition) {
        String listPropertyName = listPropertyDefinition.getBoPropertyName();
        List<Object> oldList = ObjectPropertyUtils.getPropertyValue(oldBusinessObject, listPropertyName);
        List<Object> newList = ObjectPropertyUtils.getPropertyValue(newBusinessObject, listPropertyName);
        if (oldList.size() < newList.size()) {
            RassSubObjectDefinition subObjectDefinition = listPropertyDefinition.getSubObjectDefinition();
            Class<? extends PersistableBusinessObject> subObjectClass = subObjectDefinition.getSubObjectClass();
            for (int i = oldList.size(); i < newList.size(); i++) {
                PersistableBusinessObject minimalSubObject = createMinimalObject(subObjectClass);
                oldList.add(minimalSubObject);
            }
        }
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> boolean businessObjectWasUpdatedByXml(R oldBo, R newBo,
            RassObjectTranslationDefinition<T, R> objectDefinition) {
        return objectDefinition.getPropertyMappings().stream().anyMatch(propertyMapping -> {
            if (propertyMapping instanceof RassListPropertyDefinition) {
                return !allUnorderedSubObjectsMatchForListProperty((RassListPropertyDefinition) propertyMapping, oldBo, newBo);
            } else {
                return !propertyValuesMatch(propertyMapping.getBoPropertyName(), oldBo, newBo);
            }
        });
    }

    protected boolean allUnorderedSubObjectsMatchForListProperty(RassListPropertyDefinition listPropertyMapping, Object oldBo, Object newBo) {
        List<?> oldSubObjects = ObjectPropertyUtils.getPropertyValue(oldBo, listPropertyMapping.getBoPropertyName());
        List<?> newSubObjects = ObjectPropertyUtils.getPropertyValue(newBo, listPropertyMapping.getBoPropertyName());
        return allUnorderedSubObjectsMatch(listPropertyMapping.getSubObjectDefinition(), oldSubObjects, newSubObjects);
    }

    protected boolean allUnorderedSubObjectsMatch(RassSubObjectDefinition subObjectDefinition, List<?> oldSubObjects, List<?> newSubObjects) {
        if (oldSubObjects.size() != newSubObjects.size()) {
            return false;
        }

        List<?> unmatchedSubObjects = new ArrayList<>(oldSubObjects);

        for (Object newBo : newSubObjects) {
            int i = 0;
            while (i < unmatchedSubObjects.size() && !allMappedSubObjectPropertiesMatch(subObjectDefinition, unmatchedSubObjects.get(i), newBo)) {
                i++;
            }

            if (i < unmatchedSubObjects.size()) {
                unmatchedSubObjects.remove(i);
            } else {
                return false;
            }
        }

        return true;
    }

    protected boolean allMappedSubObjectPropertiesMatch(RassSubObjectDefinition subObjectDefinition, Object oldBo, Object newBo) {
        return subObjectPrimaryKeysMatch(subObjectDefinition, oldBo, newBo)
                && specificSubObjectPropertiesMatch(subObjectDefinition.getNonKeyPropertyNames(), oldBo, newBo);
    }

    protected boolean subObjectPrimaryKeysMatch(RassSubObjectDefinition subObjectDefinition, Object oldBo, Object newBo) {
        return specificSubObjectPropertiesMatch(subObjectDefinition.getPrimaryKeyPropertyNames(), oldBo, newBo);
    }

    protected boolean specificSubObjectPropertiesMatch(List<String> propertyNames, Object oldBo, Object newBo) {
        return propertyNames.stream().allMatch(propertyName -> propertyValuesMatch(propertyName, oldBo, newBo));
    }

    protected boolean propertyValuesMatch(String propertyName, Object oldBo, Object newBo) {
        Object oldValue = ObjectPropertyUtils.getPropertyValue(oldBo, propertyName);
        Object newValue = ObjectPropertyUtils.getPropertyValue(newBo, propertyName);
        return Objects.equals(oldValue, newValue);
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocument(Pair<R, R> businessObjects,
            String maintenanceAction, RassObjectTranslationDefinition<T, R> objectDefinition) {
        try {
            return GlobalVariables.doInNewGlobalVariables(buildSessionForSystemUser(),
                    () -> createAndRouteMaintenanceDocumentInternal(businessObjects, maintenanceAction, objectDefinition));
        } catch (ValidationException validationExceptionWithGlobalErrorMessages) {
            throw new RuntimeException(validationExceptionWithGlobalErrorMessages.getMessage());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected UserSession buildSessionForSystemUser() {
        return new UserSession(KFSConstants.SYSTEM_USER);
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocumentInternal(
            Pair<R, R> businessObjects, String maintenanceAction, RassObjectTranslationDefinition<T, R> objectDefinition) throws WorkflowException {
        String annotation = configurationService.getPropertyValueAsString(RassKeyConstants.MESSAGE_RASS_DOCUMENT_ANNOTATION_ROUTE);
        R newBo = businessObjects.getRight();
        MaintenanceDocument maintenanceDocument = (MaintenanceDocument) documentService.getNewDocument(objectDefinition.getDocumentTypeName());
        if (StringUtils.equals(KRADConstants.MAINTENANCE_EDIT_ACTION, maintenanceAction)) {
            maintenanceDocument.getOldMaintainableObject().setDataObject(businessObjects.getLeft());
        }
        maintenanceDocument.getNewMaintainableObject().setDataObject(newBo);
        maintenanceDocument.getNewMaintainableObject().setMaintenanceAction(maintenanceAction);
        maintenanceDocument.getDocumentHeader().setDocumentDescription(buildDocumentDescription(newBo, maintenanceAction, objectDefinition));

        try {
            maintenanceDocument = (MaintenanceDocument) documentService.routeDocument(maintenanceDocument, annotation, null);
            
        } catch (ValidationException ve) {
            LOG.error("createAndRouteMaintenanceDocumentInternal, Error routing document # " + maintenanceDocument.getDocumentNumber() + " " + ve.getMessage(),
                    ve);
            String errorMessage = buildValidationErrorMessage(ve);
            LOG.error("createAndRouteMaintenanceDocumentInternal: Could not route document - " + errorMessage);
            throw new ValidationException(errorMessage);
        }

        return maintenanceDocument;
    }

    public String buildValidationErrorMessage(ValidationException validationException) {
        try {
            Map<String, AutoPopulatingList<ErrorMessage>> errorMessages = GlobalVariables.getMessageMap().getErrorMessages();
            return errorMessages.values().stream().flatMap(List::stream).map(this::buildValidationErrorMessageForSingleError)
                    .collect(Collectors.joining(KFSConstants.NEWLINE));
        } catch (RuntimeException e) {
            LOG.error("buildValidationErrorMessage: Could not build validation error message", e);
            return CuFPConstants.ALTERNATE_BASE_VALIDATION_ERROR_MESSAGE;
        }
    }

    protected String buildValidationErrorMessageForSingleError(ErrorMessage errorMessage) {
        String errorMessageString = configurationService.getPropertyValueAsString(errorMessage.getErrorKey());
        if (StringUtils.isBlank(errorMessageString)) {
            throw new RuntimeException("Cannot find error message for key: " + errorMessage.getErrorKey());
        }

        String[] messageParameters = errorMessage.getMessageParameters();
        if (ArrayUtils.isNotEmpty(messageParameters)) {
            return MessageFormat.format(errorMessageString, messageParameters);
        } else {
            return errorMessageString;
        }
    }

    protected <R extends PersistableBusinessObject> String buildDocumentDescription(R newBo, String maintenanceAction,
            RassObjectTranslationDefinition<?, R> objectDefinition) {
        String actionLabel = getLabelForAction(maintenanceAction);
        String descriptionTemplate = configurationService.getPropertyValueAsString(RassKeyConstants.MESSAGE_RASS_DOCUMENT_DESCRIPTION);
        String formattedDescription = MessageFormat.format(descriptionTemplate, actionLabel, objectDefinition.printObjectLabelAndKeys(newBo));
        int maxLength = getDocumentDescriptionMaxLength();
        if (formattedDescription.length() > maxLength) {
            return StringUtils.left(formattedDescription, maxLength - CUKFSConstants.ELLIPSIS.length()) + CUKFSConstants.ELLIPSIS;
        } else {
            return formattedDescription;
        }
    }

    protected String getLabelForAction(String maintenanceAction) {
        return StringUtils.equals(KRADConstants.MAINTENANCE_NEW_ACTION, maintenanceAction) ? RassConstants.RASS_MAINTENANCE_NEW_ACTION_DESCRIPTION
                : maintenanceAction;
    }

    protected int getDocumentDescriptionMaxLength() {
        return dataDictionaryService.getAttributeMaxLength(DocumentHeader.class, KFSPropertyConstants.DOCUMENT_DESCRIPTION).intValue();
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setRouteHeaderService(RouteHeaderService routeHeaderService) {
        this.routeHeaderService = routeHeaderService;
    }

    public void setDocumentStatusCheckDelayMillis(long documentStatusCheckDelayMillis) {
        this.documentStatusCheckDelayMillis = documentStatusCheckDelayMillis;
    }

    public void setMaxStatusCheckAttempts(int maxStatusCheckAttempts) {
        this.maxStatusCheckAttempts = maxStatusCheckAttempts;
    }


}
