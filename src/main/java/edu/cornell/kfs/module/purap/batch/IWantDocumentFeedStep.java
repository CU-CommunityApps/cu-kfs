package edu.cornell.kfs.module.purap.batch;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;

import edu.cornell.kfs.module.purap.batch.service.IWantDocumentFeedService;

public class IWantDocumentFeedStep extends AbstractStep {

	private static final Logger LOG = LogManager.getLogger(IWantDocumentFeedStep.class);

	protected IWantDocumentFeedService iWantDocumentFeedService;

	/**
	 * @see org.kuali.kfs.kns.bo.Step#execute(java.lang.String, java.util.Date)
	 */
	public boolean execute(String jobName, LocalDateTime jobRunDate) {
		return iWantDocumentFeedService.processIWantDocumentFiles();

	}

	/**
	 * Sete the iWantDocumentFeedService.
	 * 
	 * @param iWantDocumentFeedService
	 */
	public void setiWantDocumentFeedService(IWantDocumentFeedService iWantDocumentFeedService) {
		this.iWantDocumentFeedService = iWantDocumentFeedService;
	}

}
