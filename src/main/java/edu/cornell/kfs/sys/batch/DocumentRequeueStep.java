/**
 * 
 */
package edu.cornell.kfs.sys.batch;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.actionrequest.service.ActionRequestService;

/**
 * @author kwk43
 *
 */
public class DocumentRequeueStep extends AbstractStep {
	
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		Set respIds = new HashSet();
		respIds.add("29");
		ActionRequestService ars = SpringContext.getBean(ActionRequestService.class);
		ars.updateActionRequestsForResponsibilityChange(respIds);
		
		return true;
	}
	
	
	
/**
	ParameterService parameterService = SpringContext.getBean(ParameterService.class);
	static String BEFORE_DATE = "REQUEUE_BEFORE_DATE";
	static String AFTER_DATE = "REQUEUE_AFTER_DATE";
	static String RESPONSIBILITY_IDS = "RESPONSIBILITY_IDS";
	
	/* (non-Javadoc)
	 * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
	 
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		
		RouteHeaderService routeHeaderService = SpringContext.getBean(RouteHeaderService.class);
		DocumentRequeuerService requeuer = SpringContext.getBean(DocumentRequeuerService.class);

		Set respIds = new HashSet();
		List<String> respList = parameterService.getParameterValues("KFS-SYS", "BATCH", RESPONSIBILITY_IDS);
		for (String resp : respList) {
			respIds.add(resp);
		}
		
		Date before = getParameterDate(BEFORE_DATE);
		Date after = getParameterDate(AFTER_DATE);
		
		Collection docIds = routeHeaderService.findPendingByResponsibilityIds(respIds);
		
		for (Iterator it = docIds.iterator(); it.hasNext(); ) {
			Long id = (Long)it.next();
			DocumentRouteHeaderValue header = routeHeaderService.getRouteHeader(id);
			Timestamp createDate = header.getCreateDate();
			if (createDate.after(before) && createDate.before(after))
				requeuer.requeueDocument(id);
		}
		return true;
	}
	
	
	private Date getParameterDate(String paramName) {
		String dateString = parameterService.getParameterValue("KFS-SYS", "Batch", paramName);
		DateTimeService dtService = SpringContext.getBean(DateTimeService.class);
		java.sql.Date date = null;
		if (dateString !=null) {
			try {
				date = dtService.convertToSqlDate(dateString);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		return date;
	}
*/
}
