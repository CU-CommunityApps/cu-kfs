package edu.cornell.kfs.module.bc.batch.service.impl;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.kuali.kfs.sys.batch.BatchInputFileType;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.DictionaryValidationService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.module.bc.CUBCPropertyConstants;
import edu.cornell.kfs.module.bc.batch.dataaccess.impl.SipImportToHumanResourcesDaoJdbc.SipImportDataForHr;
import edu.cornell.kfs.module.bc.batch.service.SipImportToHumanResourcesService;
import edu.cornell.kfs.module.bc.document.dataaccess.SipImportToHumanResourcesDao;
import edu.cornell.kfs.module.bc.batch.service.impl.SipImportToHumanResourcesServiceImpl;

public class SipImportToHumanResourcesServiceImpl implements SipImportToHumanResourcesService {

	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SipImportToHumanResourcesServiceImpl.class);

	protected BatchInputFileService batchInputFileService;
	protected BatchInputFileType SipImportToHumanResourcesFileType;
	protected DateTimeService dateTimeService;
	protected BusinessObjectService businessObjectService;
	protected DictionaryValidationService dictionaryValidationService;
	protected ParameterService parameterService;
	protected SipImportToHumanResourcesDao sipImportToHumanResourcesDao;	
	
	public StringBuilder createSipImportFileForHumanResources() {
        StringBuilder results = new StringBuilder();
        String fieldSeparator = "\t";
        String textDelimiter = "";
        
        // construct and append the header line
        results.append(createHeaderLine(fieldSeparator));
        Collection<SipImportDataForHr> sipImportDataForHr = sipImportToHumanResourcesDao.getSipImportDataForHr();
        
        if (ObjectUtils.isNotNull(sipImportDataForHr))
	        for (SipImportDataForHr sipRecord : sipImportDataForHr) {
	           results.append(this.createDataLine(sipRecord, fieldSeparator, textDelimiter));
	        }
        
        return results;
	}
	
    /**
     * Constructs a header line for the Funding Dump File
     * 
     * @param fieldSeparator
     * 
     * @return
     */
    protected String createHeaderLine(String fieldSeparator) {

        String line = "";
        line = line + CUBCPropertyConstants.BudgetConstructionSipImportFileToHumanResourcesProperties.POSITION_NBR + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSipImportFileToHumanResourcesProperties.EMPLID + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSipImportFileToHumanResourcesProperties.PRE_SIP_COMP_RT + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSipImportFileToHumanResourcesProperties.POST_SIP_COMP_RT + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSipImportFileToHumanResourcesProperties.ACTION_CODE + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSipImportFileToHumanResourcesProperties.ACTION_REASON + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSipImportFileToHumanResourcesProperties.SIP_EFFECTIVE_DATE + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSipImportFileToHumanResourcesProperties.COMP_FREQ + fieldSeparator;
        line = line + CUBCPropertyConstants.BudgetConstructionSipImportFileToHumanResourcesProperties.UAW_POST_SIP_STEP;
        line = line + "\r\n";

        return line;
    }
	
    private Object createDataLine(SipImportDataForHr sipImportRecord, String fieldSeperator, String textDelimiter) {
		// Generate each line as a single String and return it
        String line = "";
        line = textDelimiter + sipImportRecord.getPositionID() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipImportRecord.getEmplID() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipImportRecord.getPreSIPCompRate() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipImportRecord.getPostSIPCompRate() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipImportRecord.getActionCode() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipImportRecord.getActionReason() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipImportRecord.getSIPEffectiveDate() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipImportRecord.getCompensationFrequency() + textDelimiter + fieldSeperator;
        line = line + textDelimiter + sipImportRecord.getUAWPostSIPStep() + textDelimiter;

        line = line + "\r\n";
    	return line;
	}
 
	/**
	 * @return the batchInputFileService
	 */
	public BatchInputFileService getBatchInputFileService() {
		return batchInputFileService;
	}


	/**
	 * @param batchInputFileService the batchInputFileService to set
	 */
	public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
		this.batchInputFileService = batchInputFileService;
	}


	/**
	 * @return the sipImportToHumanResourcesFileType
	 */
	public BatchInputFileType getSipImportToHumanResourcesFileType() {
		return SipImportToHumanResourcesFileType;
	}


	/**
	 * @param sipImportToHumanResourcesFileType the sipImportToHumanResourcesFileType to set
	 */
	public void setSipImportToHumanResourcesFileType(
			BatchInputFileType sipImportToHumanResourcesFileType) {
		SipImportToHumanResourcesFileType = sipImportToHumanResourcesFileType;
	}


	/**
	 * @return the dateTimeService
	 */
	public DateTimeService getDateTimeService() {
		return dateTimeService;
	}


	/**
	 * @param dateTimeService the dateTimeService to set
	 */
	public void setDateTimeService(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}


	/**
	 * @return the businessObjectService
	 */
	public BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}


	/**
	 * @param businessObjectService the businessObjectService to set
	 */
	public void setBusinessObjectService(BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}


	/**
	 * @return the dictionaryValidationService
	 */
	public DictionaryValidationService getDictionaryValidationService() {
		return dictionaryValidationService;
	}


	/**
	 * @param dictionaryValidationService the dictionaryValidationService to set
	 */
	public void setDictionaryValidationService(
			DictionaryValidationService dictionaryValidationService) {
		this.dictionaryValidationService = dictionaryValidationService;
	}


	/**
	 * @return the parameterService
	 */
	public ParameterService getParameterService() {
		return parameterService;
	}


	/**
	 * @param parameterService the parameterService to set
	 */
	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}


	/**
	 * @return the sipImportToHumanResourcesDao
	 */
	public SipImportToHumanResourcesDao getSipImportToHumanResourcesDao() {
		return sipImportToHumanResourcesDao;
	}


	/**
	 * @param sipImportToHumanResourcesDao the sipImportToHumanResourcesDao to set
	 */
	public void setSipImportToHumanResourcesDao(
			SipImportToHumanResourcesDao sipImportToHumanResourcesDao) {
		this.sipImportToHumanResourcesDao = sipImportToHumanResourcesDao;
	}
}