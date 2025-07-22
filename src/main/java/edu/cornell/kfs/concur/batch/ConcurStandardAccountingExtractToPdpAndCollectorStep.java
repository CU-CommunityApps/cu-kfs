package edu.cornell.kfs.concur.batch;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractReportService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

public class ConcurStandardAccountingExtractToPdpAndCollectorStep extends AbstractStep {
	private static final Logger LOG = LogManager.getLogger(ConcurStandardAccountingExtractToPdpAndCollectorStep.class);
    protected ConcurStandardAccountingExtractService concurStandardAccountingExtractService;
    protected ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService;
    protected ConcurStandardAccountingExtractReportService concurStandardAccountingExtractReportService;
    protected FileStorageService fileStorageService;

    @Override
    public boolean execute(String jobName, LocalDateTime jobRunDate) throws InterruptedException {
        List<String> listOfSaeFullyQualifiedFileNames = getConcurStandardAccountingExtractService().buildListOfFullyQualifiedFileNamesToBeProcessed();
        if (CollectionUtils.isNotEmpty(listOfSaeFullyQualifiedFileNames)) {
            for (String saeFullyQualifiedFileName : listOfSaeFullyQualifiedFileNames) {
                LOG.debug("execute, processing: " + saeFullyQualifiedFileName);
                ConcurStandardAccountingExtractBatchReportData reportData = new ConcurStandardAccountingExtractBatchReportData();
                try {
                    processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile(saeFullyQualifiedFileName, reportData);
                    File reportFile = getConcurStandardAccountingExtractReportService().generateReport(reportData);
                    getConcurStandardAccountingExtractReportService().sendResultsEmail(reportData, reportFile);
                } catch (Exception e) {
                    LOG.error("execute, there was an unexpected error processing a file: ", e);
                } finally {
                    getFileStorageService().removeDoneFiles(Collections.singletonList(saeFullyQualifiedFileName));
                }
            }
        } else {
            LOG.info("There were no SAE files to process");
            getConcurStandardAccountingExtractReportService().sendEmailThatNoFileWasProcesed();
        }
        return true;
    }

    protected boolean processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile(String saeFullyQualifiedFileName, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean success = true;
        LOG.info("processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile, current File: " + saeFullyQualifiedFileName);
        ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile = getConcurStandardAccountingExtractService()
                .parseStandardAccoutingExtractFile(saeFullyQualifiedFileName);
        reportData.setConcurFileName(concurStandardAccountingExtractFile.getOriginalFileName());
        getConcurStandardAccountingExtractService().populateReportWithInformationOnSpecialCharacterRemoval(
                reportData, concurStandardAccountingExtractFile);
        if (getConcurStandardAccountingExtractValidationService().validateConcurStandardAccountExtractFile(concurStandardAccountingExtractFile, reportData)) {
            String pdpOutputFileName = getConcurStandardAccountingExtractService()
                    .extractPdpFeedFromStandardAccountingExtract(concurStandardAccountingExtractFile, reportData);
            String collectorOutputFileName = null;
            if (StringUtils.isEmpty(pdpOutputFileName)) {
                success = false;
                LOG.error("processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile, could not produce a PDP XML file for " + saeFullyQualifiedFileName);
            }
            if (success) {
                collectorOutputFileName = getConcurStandardAccountingExtractService()
                        .extractCollectorFeedFromStandardAccountingExtract(concurStandardAccountingExtractFile, reportData);
                if (StringUtils.isEmpty(collectorOutputFileName)) {
                    LOG.error("processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile, could not produce a Collector flat file for "
                            + saeFullyQualifiedFileName);
                    success = false;
                }
            }
            if (success) {
                boolean createdPdpDoneFile = false;
                try {
                    getConcurStandardAccountingExtractService().createDoneFileForPdpFile(pdpOutputFileName);
                    createdPdpDoneFile = true;
                    getConcurStandardAccountingExtractService().createDoneFileForCollectorFile(collectorOutputFileName);
                } catch (IOException e) {
                    LOG.error("processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile, unable to create .done file: ", e);
                    success = false;
                    if (createdPdpDoneFile) {
                        getConcurStandardAccountingExtractService().removeDoneFileForPdpFileQuietly(pdpOutputFileName);
                    }
                }
            }
        } else {
            success = false;
            LOG.error("processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile, the SAE file failed high level validation.");
        }
        return success;
    }

    public ConcurStandardAccountingExtractService getConcurStandardAccountingExtractService() {
        return concurStandardAccountingExtractService;
    }

    public void setConcurStandardAccountingExtractService(ConcurStandardAccountingExtractService concurStandardAccountingExtractService) {
        this.concurStandardAccountingExtractService = concurStandardAccountingExtractService;
    }

    public FileStorageService getFileStorageService() {
        return fileStorageService;
    }

    public ConcurStandardAccountingExtractValidationService getConcurStandardAccountingExtractValidationService() {
        return concurStandardAccountingExtractValidationService;
    }

    public void setConcurStandardAccountingExtractValidationService(
            ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService) {
        this.concurStandardAccountingExtractValidationService = concurStandardAccountingExtractValidationService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public ConcurStandardAccountingExtractReportService getConcurStandardAccountingExtractReportService() {
        return concurStandardAccountingExtractReportService;
    }

    public void setConcurStandardAccountingExtractReportService(
            ConcurStandardAccountingExtractReportService concurStandardAccountingExtractReportService) {
        this.concurStandardAccountingExtractReportService = concurStandardAccountingExtractReportService;
    }
}
