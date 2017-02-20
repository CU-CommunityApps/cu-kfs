package edu.cornell.kfs.concur.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.batch.InitiateDirectoryBase;
import edu.cornell.kfs.concur.service.ConcurRequestExtractFileService;
import edu.cornell.kfs.concur.service.ConcurRequestExtractFileValidationService;

public class ConcurRequestExtractFileServiceImpl extends InitiateDirectoryBase implements ConcurRequestExtractFileService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractFileServiceImpl.class);
    protected String incomingDirectoryName;
    protected ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService;
    
    
    public ConcurRequestExtractFileServiceImpl() {
    	super();
    }
    
    @Override
    public List<String> getRequiredDirectoryNames() {
        return new ArrayList<String>() {{add(incomingDirectoryName); }};
    }
    
    public void setConcurRequestExtractFileValidationService(ConcurRequestExtractFileValidationService concurRequestExtractFileValidationService) {
        this.concurRequestExtractFileValidationService = concurRequestExtractFileValidationService;
    }
    
    public ConcurRequestExtractFileValidationService getConcurRequestExtractFileValidationService() {
        return this.concurRequestExtractFileValidationService;
    }
    
}
