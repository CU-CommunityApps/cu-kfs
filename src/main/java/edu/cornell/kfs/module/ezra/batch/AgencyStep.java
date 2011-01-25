package edu.cornell.kfs.module.ezra.batch;

import java.text.ParseException;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.ParameterService;

import edu.cornell.kfs.module.ezra.service.EzraService;

public class AgencyStep extends AbstractStep {

	private EzraService ezraService;
	private ParameterService parameterService;
	private static String LAST_SUCCESSFUL_RUN = "LAST_SUCCESSFUL_RUN";
	
	private static final String RUN_INDICATOR_PARAMETER_NAMESPACE_CODE = "KFS-BC";
    private static final String RUN_INDICATOR_PARAMETER_NAMESPACE_STEP = "GenesisBatchStep";
    private static final String RUN_INDICATOR_PARAMETER_VALUE = "N";
    private static final String RUN_INDICATOR_PARAMETER_ALLOWED = "A";
    private static final String RUN_INDICATOR_PARAMETER_DESCRIPTION = "Tells the job framework whether to run this job or not; set to know because the GenesisBatchJob needs to only be run once after database initialization.";
    private static final String RUN_INDICATOR_PARAMETER_TYPE = "CONFG";
    private static final String RUN_INDICATOR_PARAMETER_APPLICATION_NAMESPACE_CODE = "KFS";
	
	public boolean execute(String arg0, java.util.Date arg1) throws InterruptedException {
		String dateString = parameterService.getParameterValue(AgencyStep.class, LAST_SUCCESSFUL_RUN);
		DateTimeService dtService = SpringContext.getBean(DateTimeService.class);
		java.sql.Date lastRun = null;
		try {
			lastRun = dtService.convertToSqlDate(dateString);
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		 boolean result = true;
	        try {
	            result = ezraService.updateSponsorsSince(lastRun);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        if (result) {
	        	//parameterService.
	        }
	        return result;
	}
	

	/**
	 * @param ezraService the ezraService to set
	 */
	public void setEzraService(EzraService ezraService) {
		this.ezraService = ezraService;
	}

	/**
	 * @param parameterService the parameterService to set
	 */
	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}
}
