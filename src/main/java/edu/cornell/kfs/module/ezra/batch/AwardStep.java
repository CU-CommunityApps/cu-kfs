package edu.cornell.kfs.module.ezra.batch;

import java.text.ParseException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.api.parameter.Parameter;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.module.ezra.service.EzraService;

public class AwardStep extends AbstractStep {

	private EzraService ezraService;
	private ParameterService parameterService;
	private static String LAST_SUCCESSFUL_RUN = "LAST_SUCCESSFUL_RUN";
	
	private static final String PARAMETER_NAMESPACE_CODE = "KFS-EZRA";
    private static final String PARAMETER_NAMESPACE_STEP = "AwardStep";
	
    private static final Logger LOG = LogManager.getLogger(AwardStep.class);

    
	public boolean execute(String arg0, java.util.Date arg1) throws InterruptedException {
		

		
		String dateString = parameterService.getParameterValueAsString(PARAMETER_NAMESPACE_CODE, PARAMETER_NAMESPACE_STEP, LAST_SUCCESSFUL_RUN);
		LOG.info("AwardStep last successful run parm value= "+ dateString);
		DateTimeService dtService = SpringContext.getBean(DateTimeService.class);
		java.sql.Date lastRun = null;
		if (dateString !=null) {
			try {
				lastRun = dtService.convertToSqlDate(dateString);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
		}
		boolean result = ezraService.updateAwardsSince(lastRun);
	        if (result) {

	            Parameter parm = parameterService.getParameter(PARAMETER_NAMESPACE_CODE, 
                        PARAMETER_NAMESPACE_STEP, 
                        LAST_SUCCESSFUL_RUN);


				Parameter.Builder newParm = Parameter.Builder.create(parm);
				newParm.setValue(dtService.toDateTimeString(dtService.getCurrentSqlDate()));
				parameterService.updateParameter(newParm.build());
	        //	//parameterService.
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
