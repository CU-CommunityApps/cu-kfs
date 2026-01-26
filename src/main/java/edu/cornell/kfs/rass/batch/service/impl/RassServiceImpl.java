package edu.cornell.kfs.rass.batch.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.util.ObjectPropertyUtils;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.service.FileStorageService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.rass.RassConstants.RassObjectGroupingUpdateResultCode;
import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;
import edu.cornell.kfs.rass.RassConstants.RassParseResultCode;
import edu.cornell.kfs.rass.batch.PendingDocumentTracker;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResult;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResultGrouping;
import edu.cornell.kfs.rass.batch.RassObjectTranslationDefinition;
import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.RassXmlFileProcessingResult;
import edu.cornell.kfs.rass.batch.service.RassService;
import edu.cornell.kfs.rass.batch.service.RassSortService;
import edu.cornell.kfs.rass.batch.service.RassUpdateService;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.rass.batch.xml.RassXmlObject;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class RassServiceImpl implements RassService {

    private static final Logger LOG = LogManager.getLogger();

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType batchInputFileType;
    protected FileStorageService fileStorageService;
    protected RassUpdateService rassUpdateService;
    protected RassObjectTranslationDefinition<RassXmlAgencyEntry, Agency> agencyDefinition;
    protected RassObjectTranslationDefinition<RassXmlAwardEntry, Award> awardDefinition;
    protected RassSortService rassSortService;

    protected String rassFilePath;
    protected BiConsumer<String, Class<?>> parsedObjectTypeProcessingWatcher;

    public RassServiceImpl() {
        this.parsedObjectTypeProcessingWatcher = (xmlFileName, boClass) -> {};
    }

    @Override
    public List<RassXmlFileParseResult> readXML() {
        List<String> rassInputFileNames = batchInputFileService.listInputFileNamesWithDoneFile(batchInputFileType);
        sortFileNameList(rassInputFileNames);
        if (CollectionUtils.isEmpty(rassInputFileNames)) {
            LOG.info("readXML, No RASS XML files were found for processing");
            return Collections.emptyList();
        }
        LOG.info("readXML, Reading " + rassInputFileNames.size() + " RASS XML files to process into KFS");
        return rassInputFileNames.stream()
                .map(this::parseRassXml)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    
    protected void sortFileNameList(List<String> rassInputFileNames) {
        Collections.sort(rassInputFileNames);
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
    public Map<String, RassXmlFileProcessingResult> updateKFS(List<RassXmlFileParseResult> successfullyParsedFiles) {
        LOG.info("updateKFS, Processing " + successfullyParsedFiles.size() + " RASS XML files into KFS");
        
        PendingDocumentTracker documentTracker = new PendingDocumentTracker();
        
        Map<String, RassBusinessObjectUpdateResultGrouping<Agency>> agencyResults = updateBOs(
                successfullyParsedFiles, agencyDefinition, documentTracker);
        Map<String, RassBusinessObjectUpdateResultGrouping<Award>> awardResults = updateBOs(
                successfullyParsedFiles, awardDefinition, documentTracker);
        
        rassUpdateService.waitForRemainingGeneratedDocumentsToFinish(documentTracker);
        
        return buildProcessingResults(successfullyParsedFiles, agencyResults, awardResults);
    }

    protected Map<String, RassXmlFileProcessingResult> buildProcessingResults(
            List<RassXmlFileParseResult> parseResults,
            Map<String, RassBusinessObjectUpdateResultGrouping<Agency>> agencyResults,
            Map<String, RassBusinessObjectUpdateResultGrouping<Award>> awardResults) {
        return parseResults.stream()
                .map(RassXmlFileParseResult::getRassXmlFileName)
                .map(xmlFileName -> new RassXmlFileProcessingResult(
                        xmlFileName, agencyResults.get(xmlFileName),
                        awardResults.get(xmlFileName)))
                .collect(
                        Collectors.toMap(RassXmlFileProcessingResult::getRassXmlFileName, Function.identity()));
    }

    protected <T extends RassXmlObject, R extends PersistableBusinessObject> Map<String, RassBusinessObjectUpdateResultGrouping<R>> updateBOs(
            List<RassXmlFileParseResult> parsedFiles, RassObjectTranslationDefinition<T, R> objectDefinition,
            PendingDocumentTracker documentTracker) {
        Map<String, RassBusinessObjectUpdateResultGrouping<R>> resultGroupings = new HashMap<>();
        
        LOG.info("updateBOs, Started processing objects of type " + objectDefinition.getObjectLabel());
        
        for (RassXmlFileParseResult parsedFile : parsedFiles) {
            LOG.info("updateBOs, Processing results from file " + parsedFile.getRassXmlFileName());
            parsedObjectTypeProcessingWatcher.accept(
                    parsedFile.getRassXmlFileName(), objectDefinition.getBusinessObjectClass());
            List<RassBusinessObjectUpdateResult<R>> objectResults = new ArrayList<>();
            RassObjectGroupingUpdateResultCode groupingResultCode = RassObjectGroupingUpdateResultCode.SUCCESS;
            
            try {
                RassXmlDocumentWrapper documentWrapper = parsedFile.getParsedDocumentWrapper();
                LocalDateTime extractDateTime = documentWrapper.getExtractDateTime();
                if (extractDateTime == null) {
                    LOG.warn("updateBOs, Processing a file that does not specify an extract date-time value.");
                } else {
                    LOG.info("updateBOs, Processing file with extract date-time {}", extractDateTime);
                }
                
                List<?> xmlObjects = (List<?>) ObjectPropertyUtils.getPropertyValue(
                        documentWrapper, objectDefinition.getRootXmlObjectListPropertyName());
                
                if (Agency.class.getSimpleName().equals(objectDefinition.getObjectLabel())) {
                    LOG.info("updateBOs, found a collection of Agencies, we need to sort the agencies such any agencies that are the 'reports to agency' for other agencies are created or updated first.");
                    xmlObjects = rassSortService.sortRassXmlAgencyEntriesForUpdate((List<RassXmlAgencyEntry>) xmlObjects);
                }
                
                LOG.info("updateBOs, Found " + xmlObjects.size()
                        + KFSConstants.BLANK_SPACE + objectDefinition.getObjectLabel() + " objects to process");
                for (Object xmlObject : xmlObjects) {
                    T typedXmlObject = objectDefinition.getXmlObjectClass().cast(xmlObject);
                    RassBusinessObjectUpdateResult<R> result = processObject(objectDefinition, documentTracker, typedXmlObject);
                    if (RassObjectUpdateResultCode.isSuccessfulResult(result.getResultCode())) {
                        documentTracker.addDocumentIdToTrack(result);
                    } else if (RassObjectUpdateResultCode.ERROR.equals(result.getResultCode())) {
                        documentTracker.addObjectUpdateFailureToTrack(result);
                        groupingResultCode = RassObjectGroupingUpdateResultCode.ERROR;
                    }
                    objectResults.add(result);
                }
                LOG.info("updateBOs, Finished processing " + objectDefinition.getObjectLabel() + " objects from file "
                        + parsedFile.getRassXmlFileName());
            } catch (RuntimeException e) {
                LOG.error("updateBOs, Unexpected exception encountered when processing BOs of type "
                        + objectDefinition.getObjectLabel() + " for file " + parsedFile.getRassXmlFileName(), e);
                groupingResultCode = RassObjectGroupingUpdateResultCode.ERROR;
            }
            
            resultGroupings.put(parsedFile.getRassXmlFileName(), new RassBusinessObjectUpdateResultGrouping<>(
                    objectDefinition.getBusinessObjectClass(), objectResults, groupingResultCode));
        }
        LOG.info("updateBOs, Finished processing objects of type " + objectDefinition.getObjectLabel());
        
        return resultGroupings;
    }

    private <R extends PersistableBusinessObject, T extends RassXmlObject> RassBusinessObjectUpdateResult<R> processObject(
            RassObjectTranslationDefinition<T, R> objectDefinition, PendingDocumentTracker documentTracker, T typedXmlObject) {
        RassBusinessObjectUpdateResult<R> result;
        try {
            result = rassUpdateService.processObject(typedXmlObject, objectDefinition, documentTracker);
        } catch (Throwable e) {
            LOG.error("updateBOs, caught an error while processing the objects", e);
            result = new RassBusinessObjectUpdateResult<>(objectDefinition.getBusinessObjectClass(), objectDefinition.printObjectLabelAndKeys(typedXmlObject), 
                    RassObjectUpdateResultCode.ERROR, e.getMessage());
        }
        return result;
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

    public void setRassUpdateService(RassUpdateService rassUpdateService) {
        this.rassUpdateService = rassUpdateService;
    }

    public void setAgencyDefinition(RassObjectTranslationDefinition<RassXmlAgencyEntry, Agency> agencyDefinition) {
        this.agencyDefinition = agencyDefinition;
    }

    public void setAwardDefinition(RassObjectTranslationDefinition<RassXmlAwardEntry, Award> awardDefinition) {
        this.awardDefinition = awardDefinition;
    }

    public void setRassFilePath(String rassFilePath) {
        this.rassFilePath = rassFilePath;
    }

    public void setRassSortService(RassSortService rassSortService) {
        this.rassSortService = rassSortService;
    }

    public void setParsedObjectTypeProcessingWatcher(BiConsumer<String, Class<?>> parsedObjectTypeProcessingWatcher) {
        this.parsedObjectTypeProcessingWatcher = parsedObjectTypeProcessingWatcher;
    }

}
