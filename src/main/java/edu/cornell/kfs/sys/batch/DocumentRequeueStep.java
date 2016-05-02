/**
 * 
 */
package edu.cornell.kfs.sys.batch;

import java.util.Date;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import java.util.List;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

/**
 * @author kwk43
 *
 */
public class DocumentRequeueStep extends AbstractStep {
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentRequeueStep.class);
	
	/* (non-Javadoc)
	 * @see org.kuali.kfs.kns.bo.Step#execute(java.lang.String, java.util.Date)
	 */
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		return SpringContext.getBean(DocumentMaintenanceService.class).requeueDocuments();
	}

}
