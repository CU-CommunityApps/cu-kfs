/**
 * 
 */
package edu.cornell.kfs.sys.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.actionrequest.service.DocumentRequeuerService;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.routeheader.service.RouteHeaderService;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.dataaccess.DocumentMaintenanceDao;
import edu.cornell.kfs.sys.service.DocumentMaintenanceService;

/**
 * @author kwk43
 *
 */
public class DocumentRequeueStep extends AbstractStep {
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DocumentRequeueStep.class);
	
	/* (non-Javadoc)
	 * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
	 */
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		return SpringContext.getBean(DocumentMaintenanceService.class).requeueDocuments();
	}

}
