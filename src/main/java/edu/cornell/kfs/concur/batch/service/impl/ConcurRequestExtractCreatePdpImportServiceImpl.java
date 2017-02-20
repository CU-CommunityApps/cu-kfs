package edu.cornell.kfs.concur.batch.service.impl;

import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationProcessingService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractCreatePdpImportService;
import edu.cornell.kfs.concur.service.ConcurRequestExtractFileService;

public class ConcurRequestExtractCreatePdpImportServiceImpl implements ConcurRequestExtractCreatePdpImportService {
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractCreatePdpImportServiceImpl.class);
	protected ConcurRequestExtractFileService concurRequestExtractFileService;

	@Override
    public void convertConcurRequestExtractsToPdpImports() {
    	
    }
	
    public void setConcurRequestExtractFileService(ConcurRequestExtractFileService concurRequestExtractFileService) {
        this.concurRequestExtractFileService = concurRequestExtractFileService;       
    }

    public ConcurRequestExtractFileService getConcurEventNotificationProcessingService() {
        return concurRequestExtractFileService;
    }
}
