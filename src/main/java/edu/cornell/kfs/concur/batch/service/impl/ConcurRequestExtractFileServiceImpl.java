package edu.cornell.kfs.concur.batch.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurRequestExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractFileValidationService;

public class ConcurRequestExtractFileServiceImpl implements ConcurRequestExtractFileService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractFileServiceImpl.class);
    protected BatchInputFileType batchInputFileType;
    protected BatchInputFileService batchInputFileService;
    protected ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService;

    public List<String> getUnprocessedRequestExtractFiles() {
        List<String> fileNamesToLoad = getBatchInputFileService().listInputFileNamesWithDoneFile(getBatchInputFileType());
        return fileNamesToLoad;
    }

    public boolean processFile(String requestExtractFileName) {
        boolean processingSuccess = true;
        try {
            Object parsedFile = loadFile(requestExtractFileName);
            processingSuccess = requestExtractHeaderRowValidatesToFileContents(parsedFile);
        } catch (RuntimeException re) {
            LOG.error("Caught exception trying to load: " + requestExtractFileName, re);
            processingSuccess = false;
        }
        return processingSuccess;
    }

    public boolean requestExtractHeaderRowValidatesToFileContents(Object parsedFile) {
        boolean headerValidationPassed = false;
        List<ConcurRequestExtractFile> requestExtractFiles = (ArrayList<ConcurRequestExtractFile>) parsedFile;
        if (requestExtractFiles.isEmpty() || requestExtractFiles.size() != 1) {
            LOG.error("Header validation should have received single parsed file. More or less than one parsed file was detected.");
        }
        else {
            ConcurRequestExtractFile requestExtractFile = requestExtractFiles.get(0);
            LOG.info("batchDate=" + requestExtractFile.getBatchDate() + "=   recordCount=" + requestExtractFile.getRecordCount() + "=     totalApprovedAmount=" + requestExtractFile.getTotalApprovedAmount() + "=");
            headerValidationPassed = (getConcurRequestExtractFileValidationService().fileRowCountMatchesHeaderRowCount(requestExtractFile) &&
                                      getConcurRequestExtractFileValidationService().fileApprovedAmountsMatchHeaderApprovedAmount(requestExtractFile));
        }
        return headerValidationPassed;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }

    public BatchInputFileType getBatchInputFileType() {
        return batchInputFileType;
    }

    public void setConcurRequestExtractFileValidationService(ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService) {
        this.concurRequestExtractFileValidationService = concurRequestExtractFileValidationService;
    }

    public ConcurRequestExtractFileValidationService getConcurRequestExtractFileValidationService() {
        return this.concurRequestExtractFileValidationService;
    }

    private Object loadFile(String fileName) {
        byte[] fileByteContent = safelyLoadFileBytes(fileName);
        Object parsedObject = getBatchInputFileService().parse(getBatchInputFileType(), fileByteContent);
        return parsedObject;
    }

    private byte[] safelyLoadFileBytes(String fileName) {
        InputStream fileContents;
        byte[] fileByteContent;
        try {
            fileContents = new FileInputStream(fileName);
        } catch (FileNotFoundException e1) {
            LOG.error("Batch file not found [" + fileName + "]. " + e1.getMessage());
            throw new RuntimeException("Batch File not found [" + fileName + "]. " + e1.getMessage());
        }
        try {
            fileByteContent = IOUtils.toByteArray(fileContents);
        } catch (IOException e1) {
            LOG.error("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
            throw new RuntimeException("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
        } finally {
            IOUtils.closeQuietly(fileContents);
        }
        return fileByteContent;
    }

}
