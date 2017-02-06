package edu.cornell.kfs.sys.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import java.util.List;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

public class DocumentRequeueStep extends AbstractStep {
	private DocumentMaintenanceService documentMaintenanceService;

	/**
	 * @see org.kuali.kfs.kns.bo.Step#execute(java.lang.String, java.util.Date)
	 */
	@Override
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		return documentMaintenanceService.requeueDocuments();
	}

	public DocumentMaintenanceService getDocumentMaintenanceService() {
		return documentMaintenanceService;
	}

	public void setDocumentMaintenanceService(DocumentMaintenanceService documentMaintenanceService) {
		this.documentMaintenanceService = documentMaintenanceService;
	}
}
