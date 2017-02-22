package edu.cornell.kfs.concur.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.batch.InitiateDirectoryBase;

import edu.cornell.kfs.concur.service.ConcurRequestExtractFileService;
import edu.cornell.kfs.concur.service.ConcurRequestExtractFileValidationService;

public class ConcurRequestExtractFileServiceImpl extends InitiateDirectoryBase implements ConcurRequestExtractFileService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractFileServiceImpl.class);
    protected String incomingDirectoryName;
    protected String acceptedDirectoryName;
    protected String rejectedDirectoryName;
    protected ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService;

    public ConcurRequestExtractFileServiceImpl() {
        super();
    }

    public boolean requestExtractHeaderRowValidatesToFileContents(String fileName) {
        boolean headerValidationPassed = true;
        return headerValidationPassed;
    }

    public void performRejectedRequestExtractFileTasks(String fileName) {
        LOG.debug("Processing was performed.");
    }

    public void performAcceptedRequestExtractFileTasks(String fileName) {
        LOG.debug("Processing was performed.");
    }

    public void processRequestExtractFile(String fileName) {
        LOG.debug("Processing was performed.");
    }

    @Override
    public List<String> getRequiredDirectoryNames() {
        List<String> directoryNames = new ArrayList<String>();
        directoryNames.add(getIncomingDirectoryName());
        directoryNames.add(getAcceptedDirectoryName());
        directoryNames.add(getRejectedDirectoryName());
        return directoryNames;
    }

    public void setIncomingDirectoryName(String incomingDirectoryName) {
        this.incomingDirectoryName = incomingDirectoryName;
    }

    public String getIncomingDirectoryName() {
        return this.incomingDirectoryName;
    }

    public void setAcceptedDirectoryName(String acceptedDirectoryName) {
        this.acceptedDirectoryName = acceptedDirectoryName;
    }

    public String getAcceptedDirectoryName() {
        return this.acceptedDirectoryName;
    }

    public void setRejectedDirectoryName(String rejectedDirectoryName) {
        this.rejectedDirectoryName = rejectedDirectoryName;
    }

    public String getRejectedDirectoryName() {
        return this.rejectedDirectoryName;
    }

    public void setConcurRequestExtractFileValidationService(
            ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService) {
        this.concurRequestExtractFileValidationService = concurRequestExtractFileValidationService;
    }

    public ConcurRequestExtractFileValidationService getConcurRequestExtractFileValidationService() {
        return this.concurRequestExtractFileValidationService;
    }
}
