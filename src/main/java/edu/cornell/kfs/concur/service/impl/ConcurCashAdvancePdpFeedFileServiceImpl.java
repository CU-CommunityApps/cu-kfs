package edu.cornell.kfs.concur.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.batch.InitiateDirectoryBase;
import edu.cornell.kfs.concur.service.ConcurCashAdvancePdpFeedFileService;

public class ConcurCashAdvancePdpFeedFileServiceImpl extends InitiateDirectoryBase implements ConcurCashAdvancePdpFeedFileService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurCashAdvancePdpFeedFileServiceImpl.class);
    protected String outgoingDirectoryName;
    
    public ConcurCashAdvancePdpFeedFileServiceImpl() {
    	super();
    }

    @Override
    public List<String> getRequiredDirectoryNames() {
    	List<String> directoryNames = new ArrayList<String>();
    	directoryNames.add(getOutgoingDirectoryName());
        return directoryNames;
    }
    
    public void createDoneFileFor(String concurCashAdvancePdpFeedFileName) {
        /* create .done file to coexist with  xml file specified by input parameter
         */ 
    }

    public void setOutgoingDirectoryName(String outgoingDirectoryName) {
        this.outgoingDirectoryName = outgoingDirectoryName;
    }

    public String getOutgoingDirectoryName() {
        return this.outgoingDirectoryName;
    }
}
