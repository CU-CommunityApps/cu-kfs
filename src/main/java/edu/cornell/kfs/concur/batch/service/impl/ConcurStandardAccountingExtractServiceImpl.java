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

public class ConcurStandardAccountingExtractServiceImpl implements ConcurStandardAccountingExtractService {
    private static final Logger LOG = Logger.getLogger(ConcurStandardAccountingExtractServiceImpl.class);

    protected String reimbursementFeedDirectory;
    protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType batchInputFileType;

    @Override
    public ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFileToStandardAccountingExtractFile(
            File standardAccountingExtractFile) throws ValidationException {
        byte[] fileByteContent = safelyLoadFileBytes(standardAccountingExtractFile);
        LOG.debug("Attempting to parse the file ");

        ConcurStandardAccountingExtractFile parsedObject = null;

        try {
            List parsed = (List) batchInputFileService.parse(batchInputFileType, fileByteContent);
            if (parsed == null || parsed.size() != 1) {
                throw new org.kuali.kfs.sys.exception.ParseException(
                        "parseStandardAccoutingExtractFileToStandardAccountingExtractFile, did not parse the file into exactly 1 parse file ");
            }
            parsedObject = (ConcurStandardAccountingExtractFile) parsed.get(0);
        } catch (org.kuali.kfs.sys.exception.ParseException e) {
            LOG.error("parseStandardAccoutingExtractFileToStandardAccountingExtractFile, Error parsing batch file: " + e.getMessage());
        }

        validateConcureStandardAccountExtractFile(parsedObject);

        return parsedObject;
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

    protected void validateConcureStandardAccountExtractFile(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) throws ValidationException {
        validateDetailCount(concurStandardAccountingExtractFile);
        validateAmounts(concurStandardAccountingExtractFile);
    }

    protected void validateDetailCount(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) throws ValidationException {
        Integer numberOfDetailsInHeader = concurStandardAccountingExtractFile.getRecordCount();
        int actualNumberOfDetails = concurStandardAccountingExtractFile.getConcurStandardAccountingExtractDetailLines()
                .size();
        if (numberOfDetailsInHeader.intValue() == actualNumberOfDetails) {
            LOG.debug("Number of detail ines is what we expected.");
        } else {
            throw new ValidationException("The header said there were " + numberOfDetailsInHeader + " the but the actual number of details was " + actualNumberOfDetails);
        }
    }

    protected void validateAmounts(ConcurStandardAccountingExtractFile concurStandardAccountingExtractFile) throws ValidationException {
        KualiDecimal journalTotal = concurStandardAccountingExtractFile.getJournalAmountTotal();
        double detailTotal = 0;

        for (ConcurStandardAccountingExtractDetailLine line : concurStandardAccountingExtractFile
                .getConcurStandardAccountingExtractDetailLines()) {
            String debitCredit = line.getJounalDebitCredit();
            if (StringUtils.equalsIgnoreCase(debitCredit, ConcurConstants.ConcurPdpConstants.CREDIT)
                    || (StringUtils.equalsIgnoreCase(debitCredit, ConcurConstants.ConcurPdpConstants.DEBIT))) {
                detailTotal = line.getJournalAmount().doubleValue() + detailTotal;
            } else {
                throw new ValidationException(debitCredit + " is not a valid valuee for the debit or credit field.");
            }

        }
        KualiDecimal detailTotalKualiDecimal = new KualiDecimal(detailTotal);
        if (journalTotal.doubleValue() != detailTotalKualiDecimal.doubleValue()) {
            throw new ValidationException("The journal total (" + journalTotal + ") does not equal the detail line total (" + detailTotal + ")");
        } else {
            LOG.debug("validateAmounts, jornal total: " + journalTotal.doubleValue() + " and detailTotal: " + detailTotalKualiDecimal.doubleValue() + " do match.");
        }
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

}
