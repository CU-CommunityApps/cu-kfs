package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.concur.ConcurConstants;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractDetailLine;
import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractValidationService;

/**
 * @author jdh34
 *
 */
public class ConcurStandardAccountingExtractServiceImpl implements ConcurStandardAccountingExtractService {
    private static final Logger LOG = Logger.getLogger(ConcurStandardAccountingExtractServiceImpl.class);

    protected String reimbursementFeedDirectory;
    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType batchInputFileType;
    protected ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService;

    @Override
    public ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFileToStandardAccountingExtractFile(
            File standardAccountingExtractFile) throws ValidationException {
        byte[] fileByteContent = safelyLoadFileBytes(standardAccountingExtractFile);
        LOG.debug("Attempting to parse the file ");

        ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile = null;

        try {
            List parsed = (List) batchInputFileService.parse(batchInputFileType, fileByteContent);
            if (parsed == null || parsed.size() != 1) {
                LOG.error("parseStandardAccoutingExtractFileToStandardAccountingExtractFile, Unable to parse the file into exactly 1 POJO");
                throw new ValidationException(
                        "parseStandardAccoutingExtractFileToStandardAccountingExtractFile, did not parse the file into exactly 1 parse file ");
            }
            concurStandardAccountingExtractFile = (ConcurStandardAccountingExtractFile) parsed.get(0);
        } catch (org.kuali.kfs.sys.exception.ParseException e) {
            LOG.error("parseStandardAccoutingExtractFileToStandardAccountingExtractFile, Error parsing batch file: " + e.getMessage());
            throw new ValidationException(e.getMessage());
        }

        validateConcurStandardAccountExtractFile(concurStandardAccountingExtractFile);

        return concurStandardAccountingExtractFile;
    }

    protected byte[] safelyLoadFileBytes(File file) {
        InputStream fileContents;
        byte[] fileByteContent;
        String fileName = file.getName();
        try {
            fileContents = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            LOG.error("safelyLoadFileBytes, Batch file not found [" + fileName + "]. " + e1.getMessage(), e1);
            throw new RuntimeException("Batch File not found [" + fileName + "]. " + e1.getMessage());
        }
        try {
            fileByteContent = IOUtils.toByteArray(fileContents);
        } catch (IOException e1) {
            LOG.error("safelyLoadFileBytes, IO Exception loading: [" + fileName + "]. " + e1.getMessage(), e1);
            throw new RuntimeException("IO Exception loading: [" + fileName + "]. " + e1.getMessage());
        } finally {
            IOUtils.closeQuietly(fileContents);
        }
        return fileByteContent;
    }

    protected void validateConcurStandardAccountExtractFile(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) throws ValidationException {
        getConcurStandardAccountingExtractValidationService().validateDetailCount(concurStandardAccountingExtractFile);
        getConcurStandardAccountingExtractValidationService().validateAmounts(concurStandardAccountingExtractFile);
        getConcurStandardAccountingExtractValidationService().validateDate(concurStandardAccountingExtractFile.getBatchDate());
    }

    @Override
    public boolean extractPdpFeedFromStandardAccounitngExtract(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) {
        return true;
    }

    public String getReimbursementFeedDirectory() {
        return reimbursementFeedDirectory;
    }

    public void setReimbursementFeedDirectory(String reimbursementFeedDirectory) {
        this.reimbursementFeedDirectory = reimbursementFeedDirectory;
    }

    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public BatchInputFileType getBatchInputFileType() {
        return batchInputFileType;
    }

    public void setBatchInputFileType(BatchInputFileType batchInputFileType) {
        this.batchInputFileType = batchInputFileType;
    }

    public ConcurStandardAccountingExtractValidationService getConcurStandardAccountingExtractValidationService() {
        return concurStandardAccountingExtractValidationService;
    }

    public void setConcurStandardAccountingExtractValidationService(ConcurStandardAccountingExtractValidationService concurStandardAccountingExtractValidationService) {
        this.concurStandardAccountingExtractValidationService = concurStandardAccountingExtractValidationService;
    }

}
