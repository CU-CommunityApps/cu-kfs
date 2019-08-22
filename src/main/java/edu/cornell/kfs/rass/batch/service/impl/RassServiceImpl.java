package edu.cornell.kfs.rass.batch.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.uif.util.ObjectPropertyUtils;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;
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
import edu.cornell.kfs.rass.batch.RassXmlProcessingResults;
import edu.cornell.kfs.rass.batch.service.RassService;
import edu.cornell.kfs.rass.batch.service.RassUpdateService;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlAgencyEntryComparator;
import edu.cornell.kfs.rass.batch.xml.RassXmlAwardEntry;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.rass.batch.xml.RassXmlObject;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class RassServiceImpl implements RassService {

    private static final Logger LOG = LogManager.getLogger(RassServiceImpl.class);

    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType batchInputFileType;
    protected FileStorageService fileStorageService;
    protected RassUpdateService rassUpdateService;
    protected RassObjectTranslationDefinition<RassXmlAgencyEntry, Agency> agencyDefinition;
    protected RassObjectTranslationDefinition<RassXmlAwardEntry, Proposal> proposalDefinition;
    protected RassObjectTranslationDefinition<RassXmlAwardEntry, Award> awardDefinition;

    protected String rassFilePath;

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
        RassBusinessObjectUpdateResultGrouping<Award> awardResults = updateBOs(
                successfullyParsedFiles, awardDefinition, documentTracker);
        LOG.debug("updateKFS, NOTE: Proposal and Award processing still needs to be implemented!");
        
        rassUpdateService.waitForRemainingGeneratedDocumentsToFinish(documentTracker);
        
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
                
                if (Agency.class.getSimpleName().equals(objectDefinition.getObjectLabel())) {
                    LOG.info("updateBOs, found a collection of Agencies, need to do our custom sort.");
                    Collections.sort((List<RassXmlAgencyEntry>)xmlObjects, new RassXmlAgencyEntryComparator());
                }
                
                LOG.info("updateBOs, Found " + xmlObjects.size()
                        + KFSConstants.BLANK_SPACE + objectDefinition.getObjectLabel() + " objects to process");
                for (Object xmlObject : xmlObjects) {
                    T typedXmlObject = objectDefinition.getXmlObjectClass().cast(xmlObject);
                    RassBusinessObjectUpdateResult<R> result = rassUpdateService.processObject(
                            typedXmlObject, objectDefinition, documentTracker);
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

    public void setProposalDefinition(RassObjectTranslationDefinition<RassXmlAwardEntry, Proposal> proposalDefinition) {
        this.proposalDefinition = proposalDefinition;
    }

    public void setAwardDefinition(RassObjectTranslationDefinition<RassXmlAwardEntry, Award> awardDefinition) {
        this.awardDefinition = awardDefinition;
    }

    public void setRassFilePath(String rassFilePath) {
        this.rassFilePath = rassFilePath;
    }

}
