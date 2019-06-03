package edu.cornell.kfs.rass.batch.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.uif.util.ObjectPropertyUtils;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.routeheader.service.RouteHeaderService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.rass.RassConstants.RassObjectGroupingUpdateResultCode;
import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;
import edu.cornell.kfs.rass.RassConstants.RassParseResultCode;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResult;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResultGrouping;
import edu.cornell.kfs.rass.batch.RassObjectTranslationDefinition;
import edu.cornell.kfs.rass.batch.RassPropertyDefinition;
import edu.cornell.kfs.rass.batch.RassValueConverterBase;
import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.RassXmlProcessingResults;
import edu.cornell.kfs.rass.batch.service.RassRoutingService;
import edu.cornell.kfs.rass.batch.service.RassService;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.rass.batch.xml.RassXmlObject;
import edu.cornell.kfs.rass.util.RassUtil;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class RassServiceImpl implements RassService {

    private static final Logger LOG = LogManager.getLogger(RassServiceImpl.class);

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType batchInputFileType;
    protected FileStorageService fileStorageService;
    protected RassRoutingService rassRoutingService;
    protected DataDictionaryService dataDictionaryService;
    protected RouteHeaderService routeHeaderService;
    protected RassObjectTranslationDefinition<RassXmlAgencyEntry, Agency> agencyDefinition;
    protected RassObjectTranslationDefinition<RassXmlAwardEntry, Proposal> proposalDefinition;
    
    protected String rassFilePath;
    protected long documentStatusCheckDelayMillis;
    protected int maxStatusCheckAttempts;

    @Override
    public List<RassXmlFileParseResult> readXML() {
        List<String> rassInputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
        if (CollectionUtils.isEmpty(rassInputFileNames)) {
            LOG.info("readXML, No RASS XML files were found for processing");
            return Collections.emptyList();
        }
        LOG.info("readXML, Reading " + rassInputFileNames.size() + " RASS XML files to process into KFS");
        return rassInputFileNames.stream()
                .map(this::parseRassXml)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected RassXmlFileParseResult parseRassXml(String rassInputFileName) {
        try {
            LOG.info("parseRassXml, reading RASS XML from file: " + rassInputFileName);
            byte[] rassXmlContent = LoadFileUtils.safelyLoadFileBytes(rassInputFileName);
            RassXmlDocumentWrapper parsedContent = (RassXmlDocumentWrapper) batchInputFileService.parse(batchInputFileType, rassXmlContent);
            LOG.info("parseRassXml, successfully parsed RASS XML into data object from file: " + rassInputFileName);
            return new RassXmlFileParseResult(rassInputFileName, RassParseResultCode.SUCCESS, Optional.of(parsedContent));
        } catch (RuntimeException e) {
            LOG.error("parseRassXml, could not read XML from file: " + rassInputFileName, e);
            return new RassXmlFileParseResult(rassInputFileName, RassParseResultCode.ERROR, Optional.empty());
        } finally {
            removeDoneFileQuietly(rassInputFileName);
        }
    }

    protected void removeDoneFileQuietly(String rassInputFileName) {
        try {
            fileStorageService.removeDoneFiles(Collections.singletonList(rassInputFileName));
            LOG.info("removeDoneFileQuietly, Done file removed for file: " + rassInputFileName);
        } catch (RuntimeException e) {
            LOG.error("removeDoneFileQuietly, Could not delete .done file for data file: " + rassInputFileName, e);
        }
    }

    @Transactional
    @Override
    public RassXmlProcessingResults updateKFS(List<RassXmlFileParseResult> successfullyParsedFiles) {
        LOG.info("updateKFS, Processing " + successfullyParsedFiles.size() + " RASS XML files into KFS");
        
        PendingDocumentTracker documentTracker = new PendingDocumentTracker();
        
        RassBusinessObjectUpdateResultGrouping<Agency> agencyResults = updateBOs(
                successfullyParsedFiles, agencyDefinition, documentTracker);
		RassBusinessObjectUpdateResultGrouping<Proposal> proposalResults = updateBOs(successfullyParsedFiles,
				proposalDefinition, documentTracker);
        RassBusinessObjectUpdateResultGrouping<Award> awardResults = new RassBusinessObjectUpdateResultGrouping<>(
                Award.class, Collections.emptyList(), RassObjectGroupingUpdateResultCode.SUCCESS);
        LOG.debug("updateKFS, NOTE: Proposal and Award processing still needs to be implemented!");
        
        waitForRemainingGeneratedDocumentsToFinish(documentTracker);
        
        return new RassXmlProcessingResults(agencyResults, proposalResults, awardResults);
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> RassBusinessObjectUpdateResultGrouping<R> updateBOs(
            List<RassXmlFileParseResult> parsedFiles, RassObjectTranslationDefinition<T, R> objectDefinition,
            PendingDocumentTracker documentTracker) {
        List<RassBusinessObjectUpdateResult<R>> objectResults = new ArrayList<>();
        RassObjectGroupingUpdateResultCode groupingResultCode = RassObjectGroupingUpdateResultCode.SUCCESS;
        
        LOG.info("updateBOs, Started processing objects of type " + objectDefinition.getObjectLabel());
        try {
            for (RassXmlFileParseResult parsedFile : parsedFiles) {
                RassXmlDocumentWrapper documentWrapper = parsedFile.getParsedDocumentWrapper();
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
                    RassBusinessObjectUpdateResult<R> result = processObject(typedXmlObject, objectDefinition, documentTracker);
                    if (RassObjectUpdateResultCode.isSuccessfulResult(result.getResultCode())) {
                        documentTracker.addDocumentIdToTrack(result);
                    } else if (RassObjectUpdateResultCode.ERROR.equals(result.getResultCode())) {
                        documentTracker.addObjectUpdateFailureToTrack(result);
                        groupingResultCode = RassObjectGroupingUpdateResultCode.ERROR;
                    }
                    objectResults.add(result);
                }
                LOG.info("updateBOs, Finished processing " + objectDefinition.getObjectLabel() + " objects from file");
            }
        } catch (RuntimeException e) {
            LOG.error("updateBOs, Unexpected exception encountered when processing BOs of type " + objectDefinition.getObjectLabel(), e);
            groupingResultCode = RassObjectGroupingUpdateResultCode.ERROR;
            
        }
        LOG.info("updateBOs, Finished processing objects of type " + objectDefinition.getObjectLabel());
        
        return new RassBusinessObjectUpdateResultGrouping<R>(objectDefinition.getBusinessObjectClass(), objectResults, groupingResultCode);
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> RassBusinessObjectUpdateResult<R> processObject(
            T xmlObject, RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker) {
        RassBusinessObjectUpdateResult<R> result;
        try {
            LOG.info("processObject, Processing " + objectDefinition.printObjectLabelAndKeys(xmlObject));
            waitForMatchingPriorDocumentsToFinish(xmlObject, objectDefinition, documentTracker);
            R existingObject = objectDefinition.findExistingObject(xmlObject);
            if (ObjectUtils.isNull(existingObject)) {
                result = createObjectAndMaintenanceDocument(xmlObject, objectDefinition);
            } else {
                result = updateObject(xmlObject, existingObject, objectDefinition);
            }
            LOG.info("processObject, Successfully processed " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        } catch (RuntimeException e) {
            LOG.error("processObject, Failed to process update for " + objectDefinition.printObjectLabelAndKeys(xmlObject), e);
            result = new RassBusinessObjectUpdateResult<>(
                    objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(xmlObject),
                    RassObjectUpdateResultCode.ERROR);
        }
        return result;
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> void waitForMatchingPriorDocumentsToFinish(
            T xmlObject, RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker) {
        List<String> objectKeys = objectDefinition.getKeysOfObjectUpdatesToWaitFor(xmlObject);
        for (String objectKey : objectKeys) {
            if (documentTracker.didObjectFailToUpdate(objectKey)) {
                throw new RuntimeException("Cannot proceed with processing object because an update failure was detected for "
                        + RassUtil.getSimpleClassNameFromClassAndKeyIdentifier(objectKey)
                        + " with ID " + RassUtil.getKeyFromClassAndKeyIdentifier(objectKey));
            }
            
            String documentId = documentTracker.getTrackedDocumentId(objectKey);
            if (StringUtils.isNotBlank(documentId)) {
                waitForDocumentToFinishRouting(documentId);
                documentTracker.stopTrackingDocumentForObject(objectKey);
            }
        }
    }

    protected void waitForRemainingGeneratedDocumentsToFinish(PendingDocumentTracker documentTracker) {
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
    
    protected <T extends RassXmlObject, R extends PersistableBusinessObject> R createObject(
            T xmlObject, RassObjectTranslationDefinition<T, R> objectDefinition) {
        LOG.info("createObject, Creating " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        Supplier<R> businessObjectGenerator = () -> createMinimalObject(objectDefinition.getBusinessObjectClass());
        R businessObject = buildAndPopulateBusinessObjectFromPojo(xmlObject, businessObjectGenerator, objectDefinition);
        if (businessObject instanceof MutableInactivatable) {
            ((MutableInactivatable) businessObject).setActive(true);
        }
        objectDefinition.processCustomTranslationForBusinessObjectCreate(xmlObject, businessObject);
        return businessObject;
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> RassBusinessObjectUpdateResult<R> createObjectAndMaintenanceDocument(
            T xmlObject, RassObjectTranslationDefinition<T, R> objectDefinition) {
        LOG.info("createObjectAndMaintenanceDocument, Creating " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        R businessObject = createObject(xmlObject, objectDefinition);
        Pair<R, R> pairWithNewObjectOnly = Pair.of(null, businessObject);
        MaintenanceDocument maintenanceDocument = rassRoutingService.createAndRouteMaintenanceDocument(
                pairWithNewObjectOnly, KRADConstants.MAINTENANCE_NEW_ACTION, objectDefinition);
        LOG.info("createObjectAndMaintenanceDocument, Successfully routed document " + maintenanceDocument.getDocumentNumber()
                + " to create " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        return new RassBusinessObjectUpdateResult<>(
                objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(businessObject),
                maintenanceDocument.getDocumentNumber(), RassObjectUpdateResultCode.SUCCESS_NEW);
    }

    protected <R extends PersistableBusinessObject> R createMinimalObject(Class<R> businessObjectClass) {
        try {
            return businessObjectClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> RassBusinessObjectUpdateResult<R> updateObject(
            T xmlObject, R oldBusinessObject, RassObjectTranslationDefinition<T, R> objectDefinition) {
        LOG.info("updateObject, Updating " + objectDefinition.printObjectLabelAndKeys(xmlObject));
        
        if (!objectDefinition.businessObjectEditIsPermitted(xmlObject, oldBusinessObject)) {
            LOG.info("updateObject, Updates are not permitted for " + objectDefinition.printObjectLabelAndKeys(xmlObject)
                    + " so any changes to it will be skipped");
            return new RassBusinessObjectUpdateResult<>(
                    objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(xmlObject),
                    RassObjectUpdateResultCode.SKIPPED);
        }
        
        Supplier<R> businessObjectCopier = () -> deepCopyObject(oldBusinessObject);
        R newBusinessObject = buildAndPopulateBusinessObjectFromPojo(xmlObject, businessObjectCopier, objectDefinition);
        objectDefinition.processCustomTranslationForBusinessObjectEdit(xmlObject, oldBusinessObject, newBusinessObject);
        
        if (businessObjectWasUpdatedByXml(oldBusinessObject, newBusinessObject, objectDefinition)) {
            Pair<R, R> oldAndNewBos = Pair.of(oldBusinessObject, newBusinessObject);
            MaintenanceDocument maintenanceDocument = rassRoutingService.createAndRouteMaintenanceDocument(
                    oldAndNewBos, KRADConstants.MAINTENANCE_EDIT_ACTION, objectDefinition);
            LOG.info("updateObject, Successfully routed document " + maintenanceDocument.getDocumentNumber()
                    + " to edit " + objectDefinition.printObjectLabelAndKeys(xmlObject));
            return new RassBusinessObjectUpdateResult<>(
                    objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(newBusinessObject),
                    maintenanceDocument.getDocumentNumber(), RassObjectUpdateResultCode.SUCCESS_EDIT);
        } else {
            LOG.info("updateObject, Skipping updates for " + objectDefinition.printObjectLabelAndKeys(xmlObject)
                    + " because either no changes were made or changes were only made to the truncated data portions");
            return new RassBusinessObjectUpdateResult<>(
                    objectDefinition.getBusinessObjectClass(), objectDefinition.printPrimaryKeyValues(newBusinessObject),
                    RassObjectUpdateResultCode.SKIPPED);
        }
    }

    @SuppressWarnings("unchecked")
    protected <R extends PersistableBusinessObject> R deepCopyObject(R businessObject) {
        return (R) ObjectUtils.deepCopy(businessObject);
    }

	protected <T extends RassXmlObject, R extends PersistableBusinessObject> R buildAndPopulateBusinessObjectFromPojo(
			T xmlObject, Supplier<R> businessObjectSupplier, RassObjectTranslationDefinition<T, R> objectDefinition) {
		Class<R> businessObjectClass = objectDefinition.getBusinessObjectClass();
		R businessObject = businessObjectSupplier.get();
		List<String> missingRequiredFields = new ArrayList<>();
		for (RassPropertyDefinition propertyMapping : objectDefinition.getPropertyMappings()) {
			Object xmlPropertyValue = ObjectPropertyUtils.getPropertyValue(xmlObject,
					propertyMapping.getXmlPropertyName());

			try {
				Class converterClass = Class.forName(propertyMapping.getValueConverter());
				RassValueConverterBase valueConverter = (RassValueConverterBase) converterClass.newInstance();
				Object convertedValue = valueConverter.convert(xmlPropertyValue);
				Object cleanedPropertyValue = cleanPropertyValue(businessObjectClass,
						propertyMapping.getBoPropertyName(), convertedValue);
				if (cleanedPropertyValue == null && propertyMapping.isRequired()) {
					missingRequiredFields.add(propertyMapping.getXmlPropertyName());
				} else {
					ObjectPropertyUtils.setPropertyValue(businessObject, propertyMapping.getBoPropertyName(),
							cleanedPropertyValue);
				}
			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
				throw new RuntimeException(propertyMapping.getValueConverter() + " is not a valid Converter class ");
			}
		}
		if (!missingRequiredFields.isEmpty()) {
			throw new RuntimeException(objectDefinition.printObjectLabelAndKeys(xmlObject)
					+ " is missing values for the following required fields: " + missingRequiredFields.toString());
		}
		return businessObject;
	}

    protected Object cleanPropertyValue(
            Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, Object propertyValue) {
        if (propertyValue instanceof String) {
            return cleanStringValue(businessObjectClass, propertyName, (String) propertyValue);
        }
        if (propertyValue instanceof Date) {
        		return cleanDateValue(businessObjectClass, propertyName, (Date) propertyValue);
        }
        if (propertyValue instanceof Boolean) {
    			return cleanBooleanValue(businessObjectClass, propertyName, (Boolean) propertyValue);
        }
        	else {
            return propertyValue;
        }
    }

    protected String cleanStringValue(
            Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, String propertyValue) {
        String cleanedValue = StringUtils.defaultIfBlank(propertyValue, null);
        Integer maxLength = dataDictionaryService.getAttributeMaxLength(businessObjectClass, propertyName);
        if (maxLength != null && maxLength > 0 && StringUtils.length(cleanedValue) > maxLength) {
            LOG.info("cleanStringValue, Truncating value for business object " + businessObjectClass.getName()
                    + " and property " + propertyName);
            cleanedValue = StringUtils.left(cleanedValue, maxLength);
        }
        return cleanedValue;
    }
    
    protected java.sql.Date cleanDateValue(
            Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, Date propertyValue) {
        return new java.sql.Date(propertyValue.getTime());
    }
    
    protected Boolean cleanBooleanValue(
            Class<? extends PersistableBusinessObject> businessObjectClass, String propertyName, Boolean propertyValue) {
    		if(ObjectUtils.isNull(propertyValue))
    			return false;
    		else 
    			return propertyValue;
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> boolean businessObjectWasUpdatedByXml(
            R oldBo, R newBo, RassObjectTranslationDefinition<T, R> objectDefinition) {
        boolean basicPropertiesHaveDifferences = objectDefinition.getPropertyMappings().stream()
                .map(RassPropertyDefinition::getBoPropertyName)
                .anyMatch(propertyName -> {
                    Object oldValue = ObjectPropertyUtils.getPropertyValue(oldBo, propertyName);
                    Object newValue = ObjectPropertyUtils.getPropertyValue(newBo, propertyName);
                    return !Objects.equals(oldValue, newValue);
                });
        return basicPropertiesHaveDifferences || objectDefinition.otherCustomObjectPropertiesHaveDifferences(oldBo, newBo);
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public void setRassRoutingService(RassRoutingService rassRoutingService) {
        this.rassRoutingService = rassRoutingService;
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

    protected static class PendingDocumentTracker {
        private Map<String, String> objectKeysToDocumentIdsMap = new HashMap<>();
        private Set<String> objectsWithFailedUpdates = new HashSet<>();
        
        public <R extends PersistableBusinessObject> void addDocumentIdToTrack(RassBusinessObjectUpdateResult<R> objectResult) {
            objectKeysToDocumentIdsMap.put(
                    RassUtil.buildClassAndKeyIdentifier(objectResult), objectResult.getDocumentId());
        }
        
        public String getTrackedDocumentId(String classAndKeyIdentifier) {
            return objectKeysToDocumentIdsMap.get(classAndKeyIdentifier);
        }
        
        public List<String> getIdsForAllObjectsWithTrackedDocuments() {
            return new ArrayList<>(objectKeysToDocumentIdsMap.keySet());
        }
        
        public void stopTrackingDocumentForObject(String classAndKeyIdentifier) {
            objectKeysToDocumentIdsMap.remove(classAndKeyIdentifier);
        }
        
        public <R extends PersistableBusinessObject> void addObjectUpdateFailureToTrack(RassBusinessObjectUpdateResult<R> objectResult) {
            if (!RassObjectUpdateResultCode.ERROR.equals(objectResult.getResultCode())) {
                throw new IllegalArgumentException(
                        "processingResult should have had a status of ERROR, but instead had " + objectResult.getResultCode());
            }
            objectsWithFailedUpdates.add(RassUtil.buildClassAndKeyIdentifier(objectResult));
        }
        
        public boolean didObjectFailToUpdate(String classAndKeyIdentifier) {
            return objectsWithFailedUpdates.contains(classAndKeyIdentifier);
        }
    }

	public void setProposalDefinition(RassObjectTranslationDefinition<RassXmlAwardEntry, Proposal> proposalDefinition) {
		this.proposalDefinition = proposalDefinition;
	}

}
