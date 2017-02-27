package edu.cornell.kfs.concur.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.krad.exception.ValidationException;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.FlatFileInformation;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;

import edu.cornell.kfs.concur.batch.businessobject.ConcurStandardAccountingExtractFile;
import edu.cornell.kfs.concur.batch.service.ConcurStandardAccountingExtractService;
import edu.cornell.kfs.fp.batch.service.impl.AdvanceDepositServiceImpl;

public class ConcurStandardAccountingExtractServiceImpl implements ConcurStandardAccountingExtractService {
    private static final Logger LOG = Logger.getLogger(ConcurStandardAccountingExtractServiceImpl.class);

    protected String reimbursementFeedDirectory;
	protected BatchInputFileService batchInputFileService;
    protected BatchInputFileType batchInputFileType;
	
	@Override
	public ConcurStandardAccountingExtractFile parseStandardAccoutingExtractFileToStandardAccountingExtractFile(File standardAccountingExtractFile) throws ValidationException {
	    
	    byte[] fileByteContent = safelyLoadFileBytes(standardAccountingExtractFile);

        if (LOG.isInfoEnabled()) {
            LOG.info("Attempting to parse the file ");
        }
        ConcurStandardAccountingExtractFile parsedObject = null;

        try {
            List parsed = (List) batchInputFileService.parse(batchInputFileType, fileByteContent);
            if (parsed == null || parsed.size() != 1) {
                throw new org.kuali.kfs.sys.exception.ParseException("parseStandardAccoutingExtractFileToStandardAccountingExtractFile, did not parse the file into exactly 1 parse file ");
            }
            parsedObject = (ConcurStandardAccountingExtractFile) parsed.get(0);
        } catch (org.kuali.kfs.sys.exception.ParseException e) {
            LOG.error("parseStandardAccoutingExtractFileToStandardAccountingExtractFile, Error parsing batch file: " + e.getMessage());
        }
	    
		return parsedObject;
	}
	
	protected byte[] safelyLoadFileBytes(File file) {
        InputStream fileContents;
        byte[] fileByteContent;
        String fileName = file.getName();
        try {
            fileContents = new FileInputStream(file);
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
