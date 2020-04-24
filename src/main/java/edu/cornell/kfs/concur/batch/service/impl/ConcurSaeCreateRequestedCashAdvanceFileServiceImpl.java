package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.exception.FileStorageException;
import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.report.ConcurSaeRequestedCashAdvanceBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurCreateCashAdvancePdpFeedFileService;
import edu.cornell.kfs.concur.batch.service.ConcurSaeCreateRequestedCashAdvanceFileService;
import edu.cornell.kfs.concur.batch.service.ConcurSaeCreateRequestedCashAdvanceFileValidationService;
import edu.cornell.kfs.concur.batch.service.ConcurSaeCreateRequestedCashAdvanceReportService;

public class ConcurSaeCreateRequestedCashAdvanceFileServiceImpl implements ConcurSaeCreateRequestedCashAdvanceFileService {
    private static final Logger LOG = LogManager.getLogger(ConcurSaeCreateRequestedCashAdvanceFileServiceImpl.class);
    protected BatchInputFileType batchInputFileType;
    protected BatchInputFileService batchInputFileService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurCreateCashAdvancePdpFeedFileService concurCreateCashAdvancePdpFeedFileService;
    protected ConcurSaeCreateRequestedCashAdvanceReportService concurSaeCreateRequestedCashAdvanceReportService;
    protected ConcurSaeCreateRequestedCashAdvanceFileValidationService concurSaeCreateRequestedCashAdvanceFileValidationService;

    public List<String> getUnprocessedSaeFiles() {
        List<String> fileNamesToLoad = getBatchInputFileService().listInputFileNamesWithDoneFile(getBatchInputFileType());
        return fileNamesToLoad;
    }

    public boolean processFile(String standardAccountingExtractFullyQualifiedFileName) {
        boolean processingSuccessful = false;
        ConcurStandardAccountingExtractFile standardAccountingExtractFile = loadFileIntoParsedDataObject(standardAccountingExtractFullyQualifiedFileName);
        ConcurSaeRequestedCashAdvanceBatchReportData reportData = new ConcurSaeRequestedCashAdvanceBatchReportData();
        reportData.setConcurFileName(parseStandardAccountingExtractFileNameFrom(standardAccountingExtractFullyQualifiedFileName));
        if (getConcurSaeCreateRequestedCashAdvanceFileValidationService().saeHeaderRowValidatesToFileContentsForRequestedCashAdvances(standardAccountingExtractFile, reportData)) {
            List<String> uniqueRequestedCashAdvanceIdsInFile = new ArrayList<String>();
            for (ConcurStandardAccountingExtractDetailLine detailFileLine : standardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()) {
                getConcurSaeCreateRequestedCashAdvanceFileValidationService().performSaeDetailLineValidationForRequestedCashAdvances(detailFileLine, uniqueRequestedCashAdvanceIdsInFile);
            }
            standardAccountingExtractFile.setOriginalFileName(parseStandardAccountingExtractFileNameFrom(standardAccountingExtractFullyQualifiedFileName));
            processingSuccessful = getConcurCreateCashAdvancePdpFeedFileService().createPdpFeedFileForValidatedDetailFileLines(standardAccountingExtractFile, reportData);
            if (processingSuccessful && StringUtils.isNotBlank(standardAccountingExtractFile.getFullyQualifiedRequestedCashAdvancesPdpFileName())) {
                try {
                    getConcurCreateCashAdvancePdpFeedFileService().createDoneFileForPdpFile(standardAccountingExtractFile.getFullyQualifiedRequestedCashAdvancesPdpFileName());
                } catch (IOException ioe) {
                    LOG.error(new String("processFile: ConcurCreateCashAdvancePdpFeedFileService().createDoneFileForPdpFile attempting to create .done file for generated PdpFeedFile: " + standardAccountingExtractFile.getFullyQualifiedRequestedCashAdvancesPdpFileName() + " generated IOException = "), ioe);
                    processingSuccessful = false;
                } catch (FileStorageException fse) {
                    LOG.error(new String("processFile: ConcurCreateCashAdvancePdpFeedFileService().createDoneFileForPdpFile attempting to create .done file for generated PdpFeedFile: " + standardAccountingExtractFile.getFullyQualifiedRequestedCashAdvancesPdpFileName() + " generated FileStorageException = "), fse);
                    processingSuccessful = false;
                }
            }
        }
        File reportFile = getConcurSaeCreateRequestedCashAdvanceReportService().generateReport(reportData);
        getConcurSaeCreateRequestedCashAdvanceReportService().sendResultsEmail(reportData, reportFile);
        LOG.debug("method processFile:: requestExtractFile data after processing: " + KFSConstants.NEWLINE + standardAccountingExtractFile.toString());
        return processingSuccessful;
    }

    private ConcurStandardAccountingExtractFile loadFileIntoParsedDataObject(String standardAccountingExtractFullyQualifiedFileName) {
        Object parsedFile = getConcurBatchUtilityService().loadFile(standardAccountingExtractFullyQualifiedFileName, getBatchInputFileType());
        List<ConcurStandardAccountingExtractFile> standardAccountingExtractFiles = (ArrayList<ConcurStandardAccountingExtractFile>) parsedFile;
        if (standardAccountingExtractFiles.size() != 1) {
            LOG.error("loadFileIntoParsedDataObject: Single physical file " + standardAccountingExtractFullyQualifiedFileName + " should have translated into single parsed file. More or less than one parsed file was detected.");
            throw new RuntimeException("Single physical file " + standardAccountingExtractFullyQualifiedFileName + " should have translated into single parsed file. More or less than one parsed file was detected.");
        }
        else {
            ConcurStandardAccountingExtractFile standardAccountingExtractFile = standardAccountingExtractFiles.get(0);
            return standardAccountingExtractFile;
        }
    }

    private String parseStandardAccountingExtractFileNameFrom(String standardAccountingExtractFullyQualifiedFileName) {
        //plus 1 because we do not want the forward slash
        return standardAccountingExtractFullyQualifiedFileName.substring((standardAccountingExtractFullyQualifiedFileName.lastIndexOf(ConcurConstants.FORWARD_SLASH)) + 1);
    }

    public ConcurCreateCashAdvancePdpFeedFileService getConcurCreateCashAdvancePdpFeedFileService() {
        return concurCreateCashAdvancePdpFeedFileService;
    }

    public void setConcurCreateCashAdvancePdpFeedFileService(
            ConcurCreateCashAdvancePdpFeedFileService concurCreateCashAdvancePdpFeedFileService) {
        this.concurCreateCashAdvancePdpFeedFileService = concurCreateCashAdvancePdpFeedFileService;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }

    public BatchInputFileType getBatchInputFileType() {
        return batchInputFileType;
    }

    public ConcurBatchUtilityService getConcurBatchUtilityService() {
        return concurBatchUtilityService;
    }

    public void setConcurBatchUtilityService(ConcurBatchUtilityService concurBatchUtilityService) {
        this.concurBatchUtilityService = concurBatchUtilityService;
    }

    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public ConcurSaeCreateRequestedCashAdvanceReportService getConcurSaeCreateRequestedCashAdvanceReportService() {
        return concurSaeCreateRequestedCashAdvanceReportService;
    }

    public void setConcurSaeCreateRequestedCashAdvanceReportService(
            ConcurSaeCreateRequestedCashAdvanceReportService concurSaeCreateRequestedCashAdvanceReportService) {
        this.concurSaeCreateRequestedCashAdvanceReportService = concurSaeCreateRequestedCashAdvanceReportService;
    }

    public ConcurSaeCreateRequestedCashAdvanceFileValidationService getConcurSaeCreateRequestedCashAdvanceFileValidationService() {
        return concurSaeCreateRequestedCashAdvanceFileValidationService;
    }

    public void setConcurSaeCreateRequestedCashAdvanceFileValidationService(
            ConcurSaeCreateRequestedCashAdvanceFileValidationService concurSaeCreateRequestedCashAdvanceFileValidationService) {
        this.concurSaeCreateRequestedCashAdvanceFileValidationService = concurSaeCreateRequestedCashAdvanceFileValidationService;
    }

}
