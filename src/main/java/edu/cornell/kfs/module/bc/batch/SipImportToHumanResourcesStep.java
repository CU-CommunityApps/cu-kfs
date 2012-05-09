package edu.cornell.kfs.module.bc.batch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.ParameterService;

import edu.cornell.kfs.module.bc.batch.service.SipImportToHumanResourcesService;

public class SipImportToHumanResourcesStep extends AbstractStep {
    
    protected BatchInputFileService batchInputFileService;
    protected BusinessObjectService businessObjectService;
    protected DateTimeService dateTimeService;
    protected ParameterService parameterService;
    protected SipImportToHumanResourcesService sipImportToHumanResourcesService;
    
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SipImportToHumanResourcesStep.class);
	private KualiConfigurationService  kualiConfigurationService;

    public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
    	StringBuilder fileString;
    	try {
            fileString = SpringContext.getBean(SipImportToHumanResourcesService.class).createSipImportFileForHumanResources();
            
            // Write the results to a file as it is provided
            BufferedWriter os;
            os = new BufferedWriter(new FileWriter(getFileName("SipImportToHRFile",dateTimeService.getCurrentDate())));
            os.write(fileString.toString());
            os.close();
            
            return true;
    	}
    	
    	catch (Exception ex)
    	{
    		LOG.error("SipImportToHumanResourcesStep exception: " + ex.getMessage());
    		return false;
    	}
    }
    
    protected String getFileName(String fileName, Date runDate) {
		String directoryName = kualiConfigurationService.getPropertyString(KFSConstants.REPORTS_DIRECTORY_KEY) + "/bc/sipImportFileForHR";
        String filename = directoryName + "/" + fileName + "_";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        filename = filename + sdf.format(runDate);
        filename = filename + ".xls";

        return filename;
    }

    public SipImportToHumanResourcesService getSipImportToHumanResourcesService() {
        return sipImportToHumanResourcesService;
    }

    public void setSipImportToHumanResourcesService(SipImportToHumanResourcesService sipImportToHumanResourcesService) {
        this.sipImportToHumanResourcesService = sipImportToHumanResourcesService;
    }

    public BatchInputFileService getBatchInputFileService() {
        return batchInputFileService;
    }

    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }


    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

	/**
	 * @return the kualiConfigurationService
	 */
	public KualiConfigurationService getKualiConfigurationService() {
		return kualiConfigurationService;
	}

	/**
	 * @param kualiConfigurationService the kualiConfigurationService to set
	 */
	public void setKualiConfigurationService(
			KualiConfigurationService kualiConfigurationService) {
		this.kualiConfigurationService = kualiConfigurationService;
	}
}
