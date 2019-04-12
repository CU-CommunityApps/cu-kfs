package edu.cornell.kfs.rass.batch.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.MaintenanceDocumentService;
import org.kuali.kfs.krad.uif.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kew.routeheader.service.RouteHeaderService;

import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.RassConstants.RassProcessingStatus;
import edu.cornell.kfs.rass.batch.service.RassService;
import edu.cornell.kfs.rass.batch.xml.RassObjectTranslationDefinition;
import edu.cornell.kfs.rass.batch.xml.RassPropertyDefinition;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.sys.service.CUMarshalService;

public class RassServiceImpl implements RassService {

    private static final Logger LOG = LogManager.getLogger(RassServiceImpl.class);

    protected CUMarshalService cuMarshalService;
    protected MaintenanceDocumentService maintenanceDocumentService;
    protected DocumentService documentService;
    protected DataDictionaryService dataDictionaryService;
    protected RouteHeaderService routeHeaderService;
    protected RassObjectTranslationDefinition<RassXmlAgencyEntry, Agency> agencyDefinition;
    protected String rassFilePath;
    protected long documentStatusCheckDelayMillis;
    protected int maxStatusCheckAttempts;

    @Override
    public List<RassXmlDocumentWrapper> readXML() {
        List<RassXmlDocumentWrapper> wrappers = new ArrayList<RassXmlDocumentWrapper>();
        return wrappers;
    }

    @Override
    public boolean updateKFS(List<RassXmlDocumentWrapper> rassXmlDocumentWrappers) {
        LOG.info("updateKFS, Processing " + rassXmlDocumentWrappers.size() + " RASS XML files into KFS");
        
        PendingDocumentTracker documentTracker = new PendingDocumentTracker();
        
        boolean successfullyUpdated = updateBOs(rassXmlDocumentWrappers, agencyDefinition, documentTracker);
        successfullyUpdated &= updateProposals();
        successfullyUpdated &= updateAwards();
        
        waitForRemainingGeneratedDocumentsToFinish(documentTracker);
        
        return successfullyUpdated;
    }

    protected <T, R extends PersistableBusinessObject> boolean updateBOs(List<RassXmlDocumentWrapper> rassXmlDocumentWrappers,
            RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker) {
        boolean success = true;
        
        LOG.info("updateBOs, Started processing objects of type " + objectDefinition.getObjectLabel());
        try {
            for (RassXmlDocumentWrapper documentWrapper : rassXmlDocumentWrappers) {
                Date extractDate = documentWrapper.getExtractDate();
                if (extractDate == null) {
                    LOG.warn("updateBOs, Processing a file that does not specify an extract date");
                } else {
                    LOG.info("updateBOs, Processing file with extract date " + extractDate);
                }
                
                List<?> xmlObjects = (List<?>) ObjectPropertyUtils.getPropertyValue(
                        documentWrapper, objectDefinition.getRootXmlObjectListPropertyName());
                
                LOG.info("updateBOs, Found " + xmlObjects.size()
                        + KFSConstants.BLANK_SPACE + objectDefinition.getObjectLabel() + " objects to process");
                for (Object xmlObject : xmlObjects) {
                    T typedXmlObject = objectDefinition.getXmlObjectClass().cast(xmlObject);
                    ObjectProcessingResult result = processObject(typedXmlObject, objectDefinition, documentTracker);
                    if (RassProcessingStatus.SUCCESS.equals(result)) {
                        documentTracker.addDocumentIdToTrack(result);
                    } else if (RassProcessingStatus.ERROR.equals(result)) {
                        documentTracker.addObjectUpdateFailureToTrack(result);
                        success = false;
                    }
                }
                LOG.info("updateBOs, Finished processing " + objectDefinition.getObjectLabel() + " objects from file");
            }
        } catch (RuntimeException e) {
            LOG.error("updateBOs, Unexpected exception encountered when processing BOs of type " + objectDefinition.getObjectLabel(), e);
            success = false;
        }
        LOG.info("updateBOs, Finished processing objects of type " + objectDefinition.getObjectLabel());
        
        return success;
    }

    protected <T, R extends PersistableBusinessObject> ObjectProcessingResult processObject(
            T xmlObject, RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker) {
        ObjectProcessingResult result;
        try {
            LOG.info("processObject, Processing " + objectDefinition.printObjectLabelAndKeys(xmlObject));
            waitForMatchingPriorDocumentsToFinish(xmlObject, objectDefinition, documentTracker);
            R existingObject = objectDefinition.findExistingObject(xmlObject);
            if (ObjectUtils.isNull(existingObject)) {
                result = createObject(xmlObject, objectDefinition);
            } else {
                result = updateObject(xmlObject, existingObject, objectDefinition);
            }
            LOG.info("processObject, Successfully processed " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        } catch (RuntimeException e) {
            LOG.error("processObject, Failed to process update for " + objectDefinition.printObjectLabelAndKeys(xmlObject), e);
            result = new ObjectProcessingResult(
                    objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(xmlObject),
                    RassProcessingStatus.ERROR);
        }
        return result;
    }

    protected void waitForRemainingGeneratedDocumentsToFinish(PendingDocumentTracker documentTracker) {
        try {
            waitForMatchingPriorDocumentsToFinish(documentTracker.getIdsForAllObjectsWithTrackedDocuments(), documentTracker);
        } catch (RuntimeException e) {
            LOG.error("waitForAllRemainingDocumentsToFinish, unexpected error while checking document route statuses", e);
        }
    }

    protected <T> void waitForMatchingPriorDocumentsToFinish(
            T xmlObject, RassObjectTranslationDefinition<T, ?> objectDefinition, PendingDocumentTracker documentTracker) {
        List<Pair<Class<?>, String>> objectsToWaitFor = objectDefinition.getListOfObjectUpdatesToWaitFor(xmlObject);
        waitForMatchingPriorDocumentsToFinish(objectsToWaitFor, documentTracker);
    }

    protected void waitForMatchingPriorDocumentsToFinish(
            Collection<Pair<Class<?>, String>> objectsToWaitFor, PendingDocumentTracker documentTracker) {
        for (Pair<Class<?>, String> objectToWaitFor : objectsToWaitFor) {
            if (documentTracker.didObjectFailToUpdate(objectToWaitFor)) {
                throw new RuntimeException("Cannot proceed with processing object because an update failure was detected for "
                        + objectToWaitFor.getLeft().getSimpleName() + " with ID " + objectToWaitFor.getRight());
            }
            
            String documentId = documentTracker.getTrackedDocumentId(objectToWaitFor);
            if (StringUtils.isNotBlank(documentId)) {
                waitForDocumentToFinishRouting(documentId);
                documentTracker.stopTrackingDocumentForObject(objectToWaitFor);
            }
        }
    }

    protected void waitForDocumentToFinishRouting(String documentId) {
        int timesChecked = 0;
        while (shouldKeepWaitingForDocument(documentId)) {
            timesChecked++;
            if (timesChecked == maxStatusCheckAttempts) {
                throw new RuntimeException("Document finalization checking timed out for document " + documentId);
            }
            
            try {
                Thread.sleep(documentStatusCheckDelayMillis);
            } catch (InterruptedException e) {
                LOG.warn("waitForDocumentToFinishRouting, document-checking delay was interrupted", e);
            }
        }
    }

    protected boolean shouldKeepWaitingForDocument(String documentId) {
        String documentStatus = routeHeaderService.getDocumentStatus(documentId);
        switch (documentStatus) {
            case KewApiConstants.ROUTE_HEADER_PROCESSED_CD :
            case KewApiConstants.ROUTE_HEADER_FINAL_CD :
                return false;
            case KewApiConstants.ROUTE_HEADER_EXCEPTION_CD :
            case KewApiConstants.ROUTE_HEADER_CANCEL_CD :
            case KewApiConstants.ROUTE_HEADER_DISAPPROVED_CD :
            case KewApiConstants.ROUTE_HEADER_CANCEL_DISAPPROVE_CD :
                throw new RuntimeException("Document " + documentId + " entered an unexpected unsuccessful status of " + documentStatus);
            default :
                return true;
        }
    }

    protected <T, R extends PersistableBusinessObject> ObjectProcessingResult createObject(
            T xmlObject, RassObjectTranslationDefinition<T, R> objectDefinition) {
        LOG.info("createObject, Creating " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        Supplier<R> businessObjectGenerator = () -> createMinimalObject(objectDefinition.getBusinessObjectClass());
        R businessObject = buildAndPopulateBusinessObjectFromPojo(xmlObject, businessObjectGenerator, objectDefinition);
        if (businessObject instanceof MutableInactivatable) {
            ((MutableInactivatable) businessObject).setActive(true);
        }
        objectDefinition.processCustomTranslationForBusinessObjectCreate(xmlObject, businessObject);
        Pair<R, R> pairWithNewObjectOnly = Pair.of(null, businessObject);
        MaintenanceDocument maintenanceDocument = createAndRouteMaintenanceDocumentAsSystemUser(
                pairWithNewObjectOnly, KRADConstants.MAINTENANCE_NEW_ACTION, objectDefinition);
        LOG.info("createObject, Successfully routed document " + maintenanceDocument.getDocumentNumber()
                + " to create " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        return new ObjectProcessingResult(
                objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(businessObject),
                maintenanceDocument.getDocumentNumber(), RassProcessingStatus.SUCCESS);
    }

    protected <R extends PersistableBusinessObject> R createMinimalObject(Class<R> businessObjectClass) {
        try {
            return businessObjectClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T, R extends PersistableBusinessObject> ObjectProcessingResult updateObject(T xmlObject, R oldBusinessObject,
            RassObjectTranslationDefinition<T, R> objectDefinition) {
        LOG.info("updateObject, Updating " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        Supplier<R> businessObjectCopier = () -> deepCopyObject(oldBusinessObject);
        R newBusinessObject = buildAndPopulateBusinessObjectFromPojo(xmlObject, businessObjectCopier, objectDefinition);
        
        if (businessObjectWasUpdatedByXml(oldBusinessObject, newBusinessObject, objectDefinition)) {
            objectDefinition.processCustomTranslationForBusinessObjectEdit(xmlObject, oldBusinessObject, newBusinessObject);
            Pair<R, R> oldAndNewBos = Pair.of(oldBusinessObject, newBusinessObject);
            MaintenanceDocument maintenanceDocument = createAndRouteMaintenanceDocumentAsSystemUser(
                    oldAndNewBos, KRADConstants.MAINTENANCE_EDIT_ACTION, objectDefinition);
            LOG.info("updateObject, Successfully routed document " + maintenanceDocument.getDocumentNumber()
                    + " to edit " + objectDefinition.printObjectLabelAndKeys(xmlObject));
            return new ObjectProcessingResult(
                    objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(newBusinessObject),
                    maintenanceDocument.getDocumentNumber(), RassProcessingStatus.SUCCESS);
        } else {
            LOG.info("updateObject, Skipping updates for " + objectDefinition.printObjectLabelAndKeys(xmlObject)
                    + " because either no changes were made or changes were only made to the truncated data portions");
            return new ObjectProcessingResult(
                    objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(newBusinessObject),
                    RassProcessingStatus.SKIPPED);
        }
    }

    @SuppressWarnings("unchecked")
    protected <R extends PersistableBusinessObject> R deepCopyObject(R businessObject) {
        return (R) ObjectUtils.deepCopy(businessObject);
    }

    protected <T, R extends PersistableBusinessObject> R buildAndPopulateBusinessObjectFromPojo(T xmlObject, Supplier<R> businessObjectSupplier,
            RassObjectTranslationDefinition<T, R> objectDefinition) {
        Class<R> businessObjectClass = objectDefinition.getBusinessObjectClass();
        R businessObject = businessObjectSupplier.get();
        for (RassPropertyDefinition propertyMapping : objectDefinition.getPropertyMappings()) {
            Object xmlPropertyValue = ObjectPropertyUtils.getPropertyValue(xmlObject, propertyMapping.getXmlName());
            Object cleanedPropertyValue = cleanPropertyValue(businessObjectClass, propertyMapping.getBoName(), xmlPropertyValue);
            if (cleanedPropertyValue == null && propertyMapping.isRequired()) {
                LOG.warn("buildAndPopulateBusinessObjectFromPojo, required field " + propertyMapping.getBoName()
                        + "is null/blank for " + objectDefinition.getObjectLabel() + "; will leave this value as-is on the KFS object");
            } else {
                ObjectPropertyUtils.setPropertyValue(businessObject, propertyMapping.getBoName(), cleanedPropertyValue);
            }
        }
        return businessObject;
    }

    protected Object cleanPropertyValue(Class<?> businessObjectClass, String propertyName, Object propertyValue) {
        if (propertyValue instanceof String) {
            return cleanStringValue(businessObjectClass, propertyName, (String) propertyValue);
        } else {
            return propertyValue;
        }
    }

    protected String cleanStringValue(Class<?> businessObjectClass, String propertyName, String propertyValue) {
        String cleanedValue = StringUtils.defaultIfBlank(propertyValue, null);
        Integer maxLength = dataDictionaryService.getAttributeMaxLength(businessObjectClass, propertyName);
        if (maxLength != null && maxLength > 0 && StringUtils.length(cleanedValue) > maxLength) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("cleanStringValue, Truncating value for business object " + businessObjectClass.getName()
                        + " and property " + propertyName);
            }
            cleanedValue = StringUtils.left(cleanedValue, maxLength);
        }
        return cleanedValue;
    }

    protected <R extends PersistableBusinessObject> boolean businessObjectWasUpdatedByXml(R oldBo, R newBo,
            RassObjectTranslationDefinition<?, R> objectDefinition) {
        return objectDefinition.getPropertyMappings().stream()
                .map(RassPropertyDefinition::getBoName)
                .anyMatch(propertyName -> {
                    Object oldValue = ObjectPropertyUtils.getPropertyValue(oldBo, propertyName);
                    Object newValue = ObjectPropertyUtils.getPropertyValue(newBo, propertyName);
                    return !Objects.equals(oldValue, newValue);
                });
    }

    protected <R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocumentAsSystemUser(
            Pair<R, R> businessObjects, String maintenanceAction, RassObjectTranslationDefinition<?, R> objectDefinition) {
        try {
            return GlobalVariables.doInNewGlobalVariables(
                    buildSessionForSystemUser(),
                    () -> createAndRouteMaintenanceDocument(businessObjects, maintenanceAction, objectDefinition));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected UserSession buildSessionForSystemUser() {
        return new UserSession(KFSConstants.SYSTEM_USER);
    }

    protected <R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocument(
            Pair<R, R> businessObjects, String maintenanceAction, RassObjectTranslationDefinition<?, R> objectDefinition)
            throws WorkflowException {
        R newBo = businessObjects.getRight();
        MaintenanceDocument maintenanceDocument = maintenanceDocumentService.setupNewMaintenanceDocument(
                objectDefinition.getBusinessObjectClass().getName(), objectDefinition.getDocumentTypeName(), maintenanceAction);
        if (StringUtils.equals(KRADConstants.MAINTENANCE_EDIT_ACTION, maintenanceAction)) {
            maintenanceDocument.getOldMaintainableObject().setDataObject(businessObjects.getLeft());
        }
        maintenanceDocument.getNewMaintainableObject().setDataObject(newBo);
        maintenanceDocument.getDocumentHeader().setDocumentDescription(
                buildDocumentDescription(newBo, maintenanceAction, objectDefinition));
        
        maintenanceDocument = (MaintenanceDocument) documentService.saveDocument(maintenanceDocument);
        GlobalVariables.getMessageMap().clearErrorMessages();
        maintenanceDocument = (MaintenanceDocument) documentService.routeDocument(
                maintenanceDocument, RassConstants.RASS_ROUTE_ACTION_ANNOTATION, null);
        
        return maintenanceDocument;
    }

    protected <R extends PersistableBusinessObject> String buildDocumentDescription(
            R newBo, String maintenanceAction, RassObjectTranslationDefinition<?, R> objectDefinition) {
        String actionLabel = getLabelForAction(maintenanceAction);
        return String.format(RassConstants.RASS_MAINTENANCE_DOCUMENT_DESCRIPTION_FORMAT,
                actionLabel, objectDefinition.printObjectLabelAndKeys(newBo));
    }

    protected String getLabelForAction(String maintenanceAction) {
        return StringUtils.equals(KRADConstants.MAINTENANCE_NEW_ACTION, maintenanceAction)
                ? RassConstants.RASS_MAINTENANCE_NEW_ACTION_DESCRIPTION : maintenanceAction;
    }

    protected boolean updateProposals() {
        return false;
    }

    protected boolean updateAwards() {
        return false;
    }

    public void setCuMarshalService(CUMarshalService cuMarshalService) {
        this.cuMarshalService = cuMarshalService;
    }

    public void setMaintenanceDocumentService(MaintenanceDocumentService maintenanceDocumentService) {
        this.maintenanceDocumentService = maintenanceDocumentService;
    }

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    public void setRouteHeaderService(RouteHeaderService routeHeaderService) {
        this.routeHeaderService = routeHeaderService;
    }

    public void setAgencyDefinition(RassObjectTranslationDefinition<RassXmlAgencyEntry, Agency> agencyDefinition) {
        this.agencyDefinition = agencyDefinition;
    }

    public void setRassFilePath(String rassFilePath) {
        this.rassFilePath = rassFilePath;
    }

    public void setDocumentStatusCheckDelayMillis(long documentStatusCheckDelayMillis) {
        this.documentStatusCheckDelayMillis = documentStatusCheckDelayMillis;
    }

    public void setMaxStatusCheckAttempts(int maxStatusCheckAttempts) {
        this.maxStatusCheckAttempts = maxStatusCheckAttempts;
    }

    protected static final class ObjectProcessingResult {
        private final Class<?> businessObjectClass;
        private final String primaryKey;
        private final String documentId;
        private final RassProcessingStatus resultStatus;
        
        public ObjectProcessingResult(Class<?> businessObjectClass, String primaryKey, RassProcessingStatus resultStatus) {
            this(businessObjectClass, primaryKey, null, resultStatus);
        }
        
        public ObjectProcessingResult(Class<?> businessObjectClass, String primaryKey, String documentId, RassProcessingStatus resultStatus) {
            this.businessObjectClass = businessObjectClass;
            this.primaryKey = primaryKey;
            this.documentId = documentId;
            this.resultStatus = resultStatus;
        }
        
        public Class<?> getBusinessObjectClass() {
            return businessObjectClass;
        }

        public String getPrimaryKey() {
            return primaryKey;
        }

        public String getDocumentId() {
            return documentId;
        }

        public RassProcessingStatus getResultStatus() {
            return resultStatus;
        }
    }

    protected static class PendingDocumentTracker {
        private Map<Pair<Class<?>, String>, String> objectKeysToDocumentIdsMap = new HashMap<>();
        private Set<Pair<Class<?>, String>> objectsWithFailedUpdates = new HashSet<>();
        
        public void addDocumentIdToTrack(ObjectProcessingResult processingResult) {
            objectKeysToDocumentIdsMap.put(
                    Pair.of(processingResult.getBusinessObjectClass(), processingResult.getPrimaryKey()),
                    processingResult.getDocumentId());
        }
        
        public String getTrackedDocumentId(Pair<Class<?>, String> classAndKeyIdentifier) {
            return objectKeysToDocumentIdsMap.get(classAndKeyIdentifier);
        }
        
        public Set<Pair<Class<?>, String>> getIdsForAllObjectsWithTrackedDocuments() {
            return objectKeysToDocumentIdsMap.keySet();
        }
        
        public void stopTrackingDocumentForObject(Pair<Class<?>, String> classAndKeyIdentifier) {
            objectKeysToDocumentIdsMap.remove(classAndKeyIdentifier);
        }
        
        public void addObjectUpdateFailureToTrack(ObjectProcessingResult processingResult) {
            if (!RassProcessingStatus.ERROR.equals(processingResult.getResultStatus())) {
                throw new IllegalArgumentException(
                        "processingResult should have had a status of ERROR, but instead had " + processingResult.getResultStatus());
            }
            objectsWithFailedUpdates.add(
                    Pair.of(processingResult.getBusinessObjectClass(), processingResult.getPrimaryKey()));
        }
        
        public boolean didObjectFailToUpdate(Pair<Class<?>, String> classAndKeyIdentifier) {
            return objectsWithFailedUpdates.contains(classAndKeyIdentifier);
        }
    }

}
