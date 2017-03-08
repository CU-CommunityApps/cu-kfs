package edu.cornell.kfs.concur.batch;

import java.util.Date;
import java.util.List;

import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractReportService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

public class ConcurStandardAccountingExtractToPdpAndCollectorStep extends AbstractStep {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurStandardAccountingExtractToPdpAndCollectorStep.class);

    protected ConcurStandardAccountingExtractService concurStandardAccountingExtractService;
    protected ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService;
    protected ConcurStandardAccountingExtractReportService concurStandardAccountingExtractReportService;
    protected FileStorageService fileStorageService;

    @Override
    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        List<String> listOfSaeFileNames = concurStandardAccountingExtractService.buildListOfFileNamesToBeProcessed();
        int numSuccessfulFiles = 0;
        int numFailedFiles = 0;
        
        for (String saeFileName : listOfSaeFileNames) {
            LOG.info("execute, processing: " + saeFileName);
            boolean success = processCurrentFile(saeFileName);
            if (success) {
                LOG.info("Successfully processed file: " + saeFileName);
                numSuccessfulFiles++;
            } else {
                LOG.error("Could not process file: " + saeFileName);
                numFailedFiles++;
            }
        }
        
        fileStorageService.removeDoneFiles(listOfSaeFileNames);
        
        LOG.info("SAE processing complete.");
        LOG.info("Number of files successfully loaded: " + numSuccessfulFiles);
        LOG.info("Number of files with errors: " + numFailedFiles);
        
        return true;
    }

    protected boolean processCurrentFile(String saeFileName) {
        ConcurStandardAccountingExtractFileSummary summary;
        
        try {
            ConcurStandardAccountingExtractFile saeFileContents = parseAndValidateFile(saeFileName);
            summary = extractToKFS(saeFileContents);
            if (summary.isFileProcessingSucceeded()) {
                for (String newFile : summary.getGeneratedFiles()) {
                    fileStorageService.createDoneFile(newFile);
                }
                concurStandardAccountingExtractReportService.reportSuccessfullyProcessedFile(saeFileName, summary);
            } else {
                concurStandardAccountingExtractReportService.reportFileProcessingFailure(saeFileName, summary.getFailureReason());
            }
        } catch (ValidationException e) {
            LOG.error("SAE file failed high-level validation", e);
            concurStandardAccountingExtractReportService.reportFileProcessingFailure(saeFileName, "The file failed high-level validation");
            return false;
        } catch (Exception e) {
            LOG.error("Unexpected error while processing SAE file", e);
            concurStandardAccountingExtractReportService.reportFileProcessingFailure(saeFileName, "An unexpected KFS error occurred");
            return false;
        }
        
        return summary.isFileProcessingSucceeded();
    }

    protected ConcurStandardAccountingExtractFile parseAndValidateFile(String saeFileName) throws ValidationException {
        LOG.debug("Preparing to parse file: " + saeFileName);
        ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile = concurStandardAccountingExtractService
                .parseStandardAccountingExtractFile(saeFileName);
        if (!concurStandardAccountingExtractValidationService.validateConcurStandardAccountExtractFile(concurStandardAccountingExtractFile)) {
            throw new ValidationException("File failed high-level validation: " + saeFileName);
        }
        
        return concurStandardAccountingExtractFile;
    }

    protected ConcurStandardAccountingExtractFileSummary extractToKFS(ConcurStandardAccountingExtractFile saeFileContents) {
        ConcurStandardAccountingExtractPdpSummary pdpSummary;
        ConcurStandardAccountingExtractCollectorSummary collectorSummary;
        
        pdpSummary = extractToPDP(saeFileContents);
        if (pdpSummary.isFileProcessingSucceeded()) {
            collectorSummary = extractToCollector(saeFileContents);
        } else {
            collectorSummary = new ConcurStandardAccountingExtractCollectorSummary();
            collectorSummary.setFileProcessingSucceeded(false);
        }
        
        return new ConcurStandardAccountingExtractFileSummary(pdpSummary, collectorSummary);
    }

    // TODO: This needs to be refactored by KFSPTS-8040 to implement proper reporting.
    protected ConcurStandardAccountingExtractPdpSummary extractToPDP(ConcurStandardAccountingExtractFile saeFileContents) {
        boolean success = concurStandardAccountingExtractService.extractPdpFeedFromStandardAccountingExtract(saeFileContents);
        ConcurStandardAccountingExtractPdpSummary pdpSummary = new ConcurStandardAccountingExtractPdpSummary();
        pdpSummary.setFileProcessingSucceeded(success);
        return pdpSummary;
    }

    // TODO: This needs to be refactored by KFSPTS-8040 and/or KFSPTS-7912 to implement proper reporting.
    protected ConcurStandardAccountingExtractCollectorSummary extractToCollector(ConcurStandardAccountingExtractFile saeFileContents) {
        boolean success = concurStandardAccountingExtractService.extractCollectorFileFromStandardAccountingExtract(saeFileContents);
        ConcurStandardAccountingExtractCollectorSummary collectorSummary = new ConcurStandardAccountingExtractCollectorSummary();
        collectorSummary.setFileProcessingSucceeded(success);
        return collectorSummary;
    }

    public void setConcurStandardAccountingExtractService(ConcurStandardAccountingExtractService concurStandardAccountingExtractService) {
        this.concurStandardAccountingExtractService = concurStandardAccountingExtractService;
    }

    public void setConcurStandardAccountingExtractValidationService(
            ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService) {
        this.concurStandardAccountingExtractValidationService = concurStandardAccountingExtractValidationService;
    }

    public void setConcurStandardAccountingExtractReportService(
            ConcurStandardAccountingExtractReportService concurStandardAccountingExtractReportService) {
        this.concurStandardAccountingExtractReportService = concurStandardAccountingExtractReportService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
}
