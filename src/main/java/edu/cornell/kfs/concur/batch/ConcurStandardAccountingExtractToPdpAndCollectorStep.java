package edu.cornell.kfs.concur.batch;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.service.FileStorageService;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurStandardAccountingExtractBatchReportData;
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
        List<String> listOfSaeFullyQualifiedFileNames = getConcurStandardAccountingExtractService().buildListOfFullyQualifiedFileNamesToBeProcessed();
        for (String saeFullyQualifiedFileName : listOfSaeFullyQualifiedFileNames) {
            LOG.debug("execute, processing: " + saeFullyQualifiedFileName);
            ConcurStandardAccountingExtractBatchReportData reportData = new ConcurStandardAccountingExtractBatchReportData();
            try {
                processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile(saeFullyQualifiedFileName, reportData);
                getConcurStandardAccountingExtractReportService().generateReport(reportData);
            } catch (Exception e) {
                LOG.error("execute, there was an unexpected error processing a file: ", e);
            } finally {
                getFileStorageService().removeDoneFiles(Collections.singletonList(saeFullyQualifiedFileName));
            }
        }
        return true;
    }

    protected boolean processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile(String saeFullyQualifiedFileName, ConcurStandardAccountingExtractBatchReportData reportData) {
        boolean success = true;
        LOG.info("processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile, current File: " + saeFullyQualifiedFileName);
        ConcurStandardAccountingExtractFile concurStandardAccoutingExtractFile = getConcurStandardAccountingExtractService()
                .parseStandardAccoutingExtractFile(saeFullyQualifiedFileName);
        reportData.setConcurFileName(concurStandardAccoutingExtractFile.getOriginalFileName());
        if (getConcurStandardAccountingExtractValidationService().validateConcurStandardAccountExtractFile(concurStandardAccoutingExtractFile, reportData)) {
            String outputFileName = getConcurStandardAccountingExtractService().extractPdpFeedFromStandardAccountingExtract(concurStandardAccoutingExtractFile, reportData);
            if (StringUtils.isEmpty(outputFileName)) {
                success = false;
                LOG.error("processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile, could not produce a PDP XML file for " + saeFullyQualifiedFileName);
            }
            if (success) {
                success &= getConcurStandardAccountingExtractService()
                        .extractCollectorFeedFromStandardAccountingExtract(concurStandardAccoutingExtractFile);
            }
            if (success) {
                try {
                    getConcurStandardAccountingExtractService().createDoneFileForPdpFile(outputFileName);
                } catch (IOException e) {
                    LOG.error("processCurrentFileAndExtractPdpFeedAndCollectorFromSAEFile, unable to create .done file: ", e);
                    success = false;
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
