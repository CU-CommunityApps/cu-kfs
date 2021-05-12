package edu.cornell.kfs.concur.batch.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.exception.FileStorageException;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractRequestDetailFileLine;
import edu.cornell.kfs.concur.batch.report.ConcurRequestExtractBatchReportData;
import edu.cornell.kfs.concur.batch.service.ConcurBatchUtilityService;
import edu.cornell.kfs.concur.batch.service.ConcurCashAdvancePdpFeedFileService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileValidationService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractReportService;

public class ConcurRequestExtractFileServiceImpl implements ConcurRequestExtractFileService {
	private static final Logger LOG = LogManager.getLogger(ConcurRequestExtractFileServiceImpl.class);
    protected BatchInputFileType batchInputFileType;
    protected BatchInputFileService batchInputFileService;
    protected ConcurBatchUtilityService concurBatchUtilityService;
    protected ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService;
    protected ConcurCashAdvancePdpFeedFileService concurCashAdvancePdpFeedFileService;
    protected ConcurRequestExtractReportService concurRequestExtractReportService;

    public List<String> getUnprocessedRequestExtractFiles() {
        List<String> fileNamesToLoad = getBatchInputFileService().listInputFileNamesWithDoneFile(getBatchInputFileType());
        return fileNamesToLoad;
    }

    public boolean processFile(String requestExtractFullyQualifiedFileName) {
        boolean processingSuccessful = false;
        ConcurRequestExtractFile requestExtractFile = loadFileIntoParsedDataObject(requestExtractFullyQualifiedFileName);
        ConcurRequestExtractBatchReportData reportData = new ConcurRequestExtractBatchReportData();
        reportData.setConcurFileName(parseRequestExtractFileNameFrom(requestExtractFullyQualifiedFileName));
        if (getConcurRequestExtractFileValidationService().requestExtractHeaderRowValidatesToFileContents(requestExtractFile, reportData)) {
            List<String> uniqueRequestIdsInFile = new ArrayList<String>();
            for (ConcurRequestExtractRequestDetailFileLine detailFileLine : requestExtractFile.getRequestDetails()) {
                getConcurRequestExtractFileValidationService().performRequestDetailLineValidation(detailFileLine, uniqueRequestIdsInFile);
            }
            requestExtractFile.setFileName(parseRequestExtractFileNameFrom(requestExtractFullyQualifiedFileName));
            processingSuccessful = getConcurCashAdvancePdpFeedFileService().createPdpFeedFileForValidatedDetailFileLines(requestExtractFile, reportData);
            if (processingSuccessful && StringUtils.isNotBlank(requestExtractFile.getFullyQualifiedPdpFileName())) {
                try {
                    getConcurCashAdvancePdpFeedFileService().createDoneFileForPdpFile(requestExtractFile.getFullyQualifiedPdpFileName());
                } catch (IOException ioe) {
                    LOG.error("processFile: ConcurCashAdvancePdpFeedFileService().createDoneFileForPdpFile generated IOException attempting to create .done file for generated PdpFeedFile: " + requestExtractFile.getFullyQualifiedPdpFileName());
                    processingSuccessful = false;
                } catch (FileStorageException fse) {
                    LOG.error("processFile: ConcurCashAdvancePdpFeedFileService().createDoneFileForPdpFile generated FileStorageException attempting to create .done file for generated PdpFeedFile: " + requestExtractFile.getFullyQualifiedPdpFileName());
                    processingSuccessful = false;
                }
            }
        }
        File reportFile = getConcurRequestExtractReportService().generateReport(reportData);
        getConcurRequestExtractReportService().sendResultsEmail(reportData, reportFile);
        LOG.debug("method processFile:: requestExtractFile data after processing: " + KFSConstants.NEWLINE + requestExtractFile.toString());
        return processingSuccessful;
    }

    private ConcurRequestExtractFile loadFileIntoParsedDataObject(String requestExtractFullyQualifiedFileName) {
        Object parsedFile = getConcurBatchUtilityService().loadFile(requestExtractFullyQualifiedFileName, getBatchInputFileType());
        List<ConcurRequestExtractFile> requestExtractFiles = (ArrayList<ConcurRequestExtractFile>) parsedFile;
        if (requestExtractFiles.size() != 1) {
            LOG.error("loadFileIntoParsedDataObject: Single physical file " + requestExtractFullyQualifiedFileName + " should have translated into single parsed file. More or less than one parsed file was detected.");
            throw new RuntimeException("Single physical file " + requestExtractFullyQualifiedFileName + " should have translated into single parsed file. More or less than one parsed file was detected.");
        }
        else {
            ConcurRequestExtractFile requestExtractFile = requestExtractFiles.get(0);
            return requestExtractFile;
        }
    }

    private String parseRequestExtractFileNameFrom(String requestExtractFullyQualifiedFileName) {
        //plus 1 because we do not want the forward slash
        return requestExtractFullyQualifiedFileName.substring((requestExtractFullyQualifiedFileName.lastIndexOf(ConcurConstants.FORWARD_SLASH)) + 1);
    }

    public ConcurCashAdvancePdpFeedFileService getConcurCashAdvancePdpFeedFileService() {
        return concurCashAdvancePdpFeedFileService;
    }

    public void setConcurCashAdvancePdpFeedFileService(
            ConcurCashAdvancePdpFeedFileService concurCashAdvancePdpFeedFileService) {
        this.concurCashAdvancePdpFeedFileService = concurCashAdvancePdpFeedFileService;
    }

    public void setConcurRequestExtractFileValidationService(ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService) {
        this.concurRequestExtractFileValidationService = concurRequestExtractFileValidationService;
    }

    public ConcurRequestExtractFileValidationService getConcurRequestExtractFileValidationService() {
        return this.concurRequestExtractFileValidationService;
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

    public ConcurRequestExtractReportService getConcurRequestExtractReportService() {
        return concurRequestExtractReportService;
    }

    public void setConcurRequestExtractReportService(ConcurRequestExtractReportService concurRequestExtractReportService) {
        this.concurRequestExtractReportService = concurRequestExtractReportService;
    }

}
