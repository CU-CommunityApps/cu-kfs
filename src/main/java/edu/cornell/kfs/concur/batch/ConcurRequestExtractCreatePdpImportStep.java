package edu.cornell.kfs.concur.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.concur.batch.service.ConcurEventNotificationProcessingService;
import edu.cornell.kfs.concur.batch.service.ConcurRequestExtractCreatePdpImportService;

public class ConcurRequestExtractCreatePdpImportStep extends AbstractStep {
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ConcurRequestExtractCreatePdpImportStep.class);
	protected ConcurRequestExtractCreatePdpImportService concurRequestExtractCreatePdpImportService;
	
	@Override
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		boolean jobCompletedSuccessfully = true;
		
		concurRequestExtractCreatePdpImportService.convertConcurRequestExtractsToPdpImports();
		return jobCompletedSuccessfully;
	}
	
    public void setConcurRequestExtractCreatePdpImportService(ConcurRequestExtractCreatePdpImportService concurRequestExtractCreatePdpImportService) {
        this.concurRequestExtractCreatePdpImportService = concurRequestExtractCreatePdpImportService;       
    }

    public ConcurRequestExtractCreatePdpImportService getConcurRequestExtractCreatePdpImportService() {
        return concurRequestExtractCreatePdpImportService;
    }
}
