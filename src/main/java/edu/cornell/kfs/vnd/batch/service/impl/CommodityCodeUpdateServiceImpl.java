package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.batch.service.BatchInputFileService;
import org.kuali.kfs.sys.batch.service.WrappingBatchService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.ParseException;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.kfs.vnd.businessobject.CommodityCode;
import org.kuali.kfs.vnd.service.CommodityCodeService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.cornell.kfs.vnd.batch.CommodityCodeInputFileType;
import edu.cornell.kfs.vnd.batch.service.CommodityCodeUpdateService;

public class CommodityCodeUpdateServiceImpl implements CommodityCodeUpdateService {
	private static final Logger LOG = LogManager.getLogger(CommodityCodeUpdateServiceImpl.class);

    private BatchInputFileService batchInputFileService;
    private CommodityCodeInputFileType commodityCodeInputFileType;
    private CommodityCodeService commodityCodeService;
    private BusinessObjectService businessObjectService;
    private ConfigurationService configurationService;
    private DateTimeService dateTimeService;
    private ParameterService parameterService;
    private String batchFileDirectoryName;
    
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private ReportWriterService commodityCodeReportWriterService;

    private static final String NEW_CODES = "newCodes";
    private static final String UPDATE_CODES = "updateCodes";
    private static final String INACTIVE_CODES = "inactiveCodes";
    
    private static final String NEW_COUNTS = "newCounts";
    private static final String UPDATE_COUNTS = "updateCounts";
    private static final String INACTIVE_COUNTS = "inactiveCounts";
    
    /**
     * 
     */
//    public void loadCommodityCodeFile(String fileName) {
//        FileInputStream fileContents;
//        try {
//            fileContents = new FileInputStream(fileName);
//        }
//        catch (FileNotFoundException e1) {
//            LOG.error("Commodity Code update file to parse not found " + fileName, e1);
//            throw new RuntimeException("Cannot find the file requested to be parsed " + fileName + " " + e1.getMessage(), e1);
//        }
//
//    	Collection<CommodityCode> batchList = null;
//        try {
//            byte[] fileByteContent = IOUtils.toByteArray(fileContents);
//            batchList = (Collection<CommodityCode>) batchInputFileService.parse(commodityCodeInputFileType, fileByteContent);
//        }
//        catch (IOException e) {
//            LOG.error("error while getting file bytes:  " + e.getMessage(), e);
//            throw new RuntimeException("Error encountered while attempting to get file bytes: " + e.getMessage(), e);
//        }
//        catch (ParseException e) {
//            LOG.error("Error parsing flat file " + e.getMessage());
//            throw new RuntimeException("Error parsing flat file " + e.getMessage(), e);
//        }
//
//        if (batchList == null || batchList.isEmpty()) {
//            LOG.warn("No commodity codes in input file " + fileName);
//        }
//
//        updateCommodityCodes((List<CommodityCode>) batchList);
//
//        LOG.info("Total transactions loaded: " + Integer.toString(batchList.size()));
//    }
    
    public boolean loadCommodityCodeFile(String fileName) {
    	byte[] fileContent = getFileContent(fileName);
    	Collection <CommodityCode> batchList = parseCommodityCodeList(fileContent);
    	
        if (batchList == null || batchList.isEmpty()) {
            LOG.warn("No commodity codes in input file " + fileName);
            return false;
        }

        updateCommodityCodes((List<CommodityCode>) batchList);

        LOG.info("Total transactions loaded: " + Integer.toString(batchList.size()));

        return true;
    }
    
    public byte[] getFileContent(String fileName) {
    	FileInputStream fileInputStream;
    	byte[] contents = {}; 
    	try {
    		fileInputStream = new FileInputStream(fileName);
    		contents = IOUtils.toByteArray(fileInputStream);
    	} catch (FileNotFoundException fnfe) {
            LOG.error("Commodity Code update file to parse not found " + fileName, fnfe);
    	} catch (IOException ioe) {
            LOG.error("error while getting file bytes:  " + ioe.getMessage(), ioe);
    	} finally {
    		return contents;
    	}
    }
    
   public Collection<CommodityCode> parseCommodityCodeList(byte[] fileContents) {
	   Collection<CommodityCode> commodityCodeList = new ArrayList<CommodityCode>();
	   try {
		   commodityCodeList = (Collection<CommodityCode>) batchInputFileService.parse(commodityCodeInputFileType, fileContents);
	   } catch (ParseException pe) {
           LOG.error("Error parsing flat file " + pe.getMessage());   
	   } finally {
		   return commodityCodeList;
	   }
   }

    /**
     * 
     * @param batchList
     * @return
     */
    protected boolean updateCommodityCodes(List<CommodityCode> batchList) {
    	// Retrieve complete list of codes to compare new data set against
    	Collection<CommodityCode> codes = SpringContext.getBean(BusinessObjectService.class).findAll(CommodityCode.class);
    	if(ObjectUtils.isNull(codes) || codes.size() < 1) {
    		// Report error message and exit update process
    		return false;
    	}
    	HashMap<String, CommodityCode> codesFromDatabase = buildMapOfCommodityCodes(codes);
    	List<CommodityCode> newCodes = new ArrayList<CommodityCode>();
    	List<CommodityCode> updatedCodes = new ArrayList<CommodityCode>();
    	List<CommodityCode> inactivatedCodes = new ArrayList<CommodityCode>();
    	
    	for(CommodityCode codesFromFile : batchList) {
    		CommodityCode ccRetrieved = codesFromDatabase.get(codesFromFile.getPurchasingCommodityCode());
    		if(ObjectUtils.isNotNull(ccRetrieved)) {
    			// Compare old and new to see if any changes are required
    			if(!StringUtils.equalsIgnoreCase(codesFromFile.getCommodityDescription().trim(), ccRetrieved.getCommodityDescription().trim())) {
	    			// Update the code with the new values because descriptions don't match
	    			LOG.info("Updating commodity code description from '"+ccRetrieved.getPurchasingCommodityCode()+": "+ccRetrieved.getCommodityDescription()+
	    					 "' to '"+codesFromFile.getPurchasingCommodityCode()+": "+codesFromFile.getCommodityDescription()+"'");
	    			ccRetrieved.setCommodityDescription(codesFromFile.getCommodityDescription());
	    			updatedCodes.add(ccRetrieved);
    			}
    			else if(!ccRetrieved.isActive()) {
    				// Do something here if the code is in the file (thus active), but inactive in the DB
    				ccRetrieved.setActive(true);
    				updatedCodes.add(ccRetrieved);
	    			LOG.info("Reactivating commodity code: "+ccRetrieved.getPurchasingCommodityCode()+": "+ccRetrieved.getCommodityDescription());
    			}
    		}
    		else {
    			// Add the new commodity code
    			codesFromFile.setSalesTaxIndicator(false);
    			codesFromFile.setRestrictedItemsIndicator(false);
    			codesFromFile.setActive(true);
    			newCodes.add(codesFromFile);
    			// Report new code was added 
    			LOG.info("Adding new commodity code: "+codesFromFile.getPurchasingCommodityCode()+": "+codesFromFile.getCommodityDescription());
    		}
    		// Remove from collection to keep collection representing only values not yet reviewed.
    		codesFromDatabase.remove(codesFromFile.getPurchasingCommodityCode());
    	}

    	if(!codesFromDatabase.isEmpty()) {
    		// Need to inactivate any remaining codes, as they are no longer in the file 
    		Collection<CommodityCode> dbCodesToInactivate = codesFromDatabase.values();
			LOG.info("Number of commodity codes to inactivate: "+dbCodesToInactivate.size());
    		for(CommodityCode codeToInactivate: dbCodesToInactivate) {
    			// Only bother inactivating if it's an active code, otherwise, ignore
    			if(codeToInactivate.isActive()) {
    				codeToInactivate.setActive(false);
	    			inactivatedCodes.add(codeToInactivate);
	    			LOG.info("Inactivating commodity code: "+codeToInactivate.getPurchasingCommodityCode()+": "+codeToInactivate.getCommodityDescription());
    			}
    		}
    		codesFromDatabase.clear();
    	}

    	Map<String, Integer> loadCounts = new HashMap<String, Integer>();
    	Map<String, List<CommodityCode>> loadCodes = new HashMap<String, List<CommodityCode>>();
    	
    	// Save all new commodity codes
		LOG.info("NEW: Number of new commodity codes: "+newCodes.size());
		loadCounts.put(NEW_COUNTS, new Integer(newCodes.size()));
		loadCodes.put(NEW_CODES, newCodes);
    	businessObjectService.save(newCodes);
    	
    	// Save all commodity code changes
		LOG.info("UPDATE: Number of commodity codes to update: "+updatedCodes.size());
		loadCounts.put(UPDATE_COUNTS, new Integer(updatedCodes.size()));
		loadCodes.put(UPDATE_CODES, updatedCodes);
    	businessObjectService.save(updatedCodes);
    	
    	// Save all inactivated commodity code 
		LOG.info("INACTIVE: Number of commodity codes to inactivate: "+inactivatedCodes.size());
		loadCounts.put(INACTIVE_COUNTS, new Integer(inactivatedCodes.size()));
		loadCodes.put(INACTIVE_CODES, inactivatedCodes);
    	businessObjectService.save(inactivatedCodes);
    	
    	writeReports(getJobParameters(), loadCounts, loadCodes);
    	
    	return true;
    }
    
    /**
     * Returns a Map with the properly initialized parameters for an organization reversion job that is about to run
     * @return a Map holding parameters for the job
     * @see org.kuali.kfs.gl.batch.service.ReversionProcessService#getJobParameters()
     */
    public Map getJobParameters() {
        // Get job parameters
        Map jobParameters = new HashMap();
        return jobParameters;
    }
    
    /**
     * 
     * @param organizationReversionProcess
     * @param jobParameters
     * @param counts
     */
    public void writeReports(Map jobParameters, Map<String, Integer> counts, Map<String, List<CommodityCode>> loadedCodes) {
        // write job parameters
        for (Object jobParameterKeyAsObject : jobParameters.keySet()) {
            if (jobParameterKeyAsObject != null) {
                final String jobParameterKey = jobParameterKeyAsObject.toString();
                commodityCodeReportWriterService.writeParameterLine("%32s %10s", jobParameterKey, jobParameters.get(jobParameterKey));
            }
        }

        try {
	        ((WrappingBatchService) commodityCodeReportWriterService).initialize();
	        
	        // write statistics
	        commodityCodeReportWriterService.writeStatisticLine("NUMBER OF CODES ADDED........: %10d", counts.get(NEW_COUNTS));
	        commodityCodeReportWriterService.writeStatisticLine("NUMBER OF CODES UPDATED......: %10d", counts.get(UPDATE_COUNTS));
	        commodityCodeReportWriterService.writeStatisticLine("NUMBER OF CODES INACTIVATED..: %10d", counts.get(INACTIVE_COUNTS));
	        commodityCodeReportWriterService.pageBreak();
	        
	        // write report
	        commodityCodeReportWriterService.writeSubTitle(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_REPORT_NEW_COMMODITY_CODE_TITLE_LINE));
	        commodityCodeReportWriterService.writeNewLines(2);
	        writeCommodityCodeReportData(loadedCodes.get(NEW_CODES));
	        commodityCodeReportWriterService.pageBreak();
	        
	        commodityCodeReportWriterService.writeSubTitle(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_REPORT_UPDATE_COMMODITY_CODE_TITLE_LINE));
	        commodityCodeReportWriterService.writeNewLines(2);
	        writeCommodityCodeReportData(loadedCodes.get(UPDATE_CODES));
	        commodityCodeReportWriterService.pageBreak();
	        
	        commodityCodeReportWriterService.writeSubTitle(getConfigurationService().getPropertyValueAsString(CUKFSKeyConstants.MESSAGE_REPORT_INACTIVE_COMMODITY_CODE_TITLE_LINE));
	        commodityCodeReportWriterService.writeNewLines(2);
	        writeCommodityCodeReportData(loadedCodes.get(INACTIVE_CODES));
	        commodityCodeReportWriterService.pageBreak();
	        
        }
        finally {
	        ((WrappingBatchService) commodityCodeReportWriterService).destroy();
        }
    }

    
    /**
     * Writes the report of totals to the given reportWriterService
     * @param reportWriterService a report writer service to write the ledger summary report to
     */
    public void writeCommodityCodeReportData(List<CommodityCode> codes) {
    	boolean firstTime = true;
    	if (ObjectUtils.isNotNull(codes) && codes.size() > 0) {
            for (CommodityCode codeLine : codes) {
            	if(firstTime) {
            		commodityCodeReportWriterService.writeTableHeader(codeLine);
            		firstTime = false;
            	}
            	commodityCodeReportWriterService.writeTableRow(codeLine);
            }
        }
    }
    
    /**
     * 
     * @param commodityCodes
     * @return
     */
    protected HashMap<String, CommodityCode> buildMapOfCommodityCodes(Collection<CommodityCode> commodityCodes) {
    	HashMap<String, CommodityCode> mapOfCodes = new HashMap<String, CommodityCode>();

    	for(CommodityCode cc : commodityCodes) {
    		mapOfCodes.put(cc.getPurchasingCommodityCode(), cc);
    	}
    	
    	return mapOfCodes;
    }
    
    protected String getBaseDirName(){
        return commodityCodeInputFileType.getDirectoryPath() + File.separator;
    }
    
    
    public void setBatchInputFileService(BatchInputFileService batchInputFileService) {
        this.batchInputFileService = batchInputFileService;
    }

    public void setCommodityCodeInputFileType(CommodityCodeInputFileType commodityCodeInputFileType) {
        this.commodityCodeInputFileType = commodityCodeInputFileType;
    }

    /**
     * Gets the dateTimeService attribute.
     * 
     * @return Returns the dateTimeService.
     */
    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    /**
     * Sets the dateTimeService attribute value.
     * 
     * @param dateTimeService The dateTimeService to set.
     */
    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    /**
     * Gets the commodityCodeService attribute.
     * 
     * @return Returns the commodityCodeService.
     */
    public CommodityCodeService getCommodityCodeService() {
        return commodityCodeService;
    }

    /**
     * Sets the commodityCodeService attribute value.
     * 
     * @param commodityCodeService The commodityCodeService to set.
     */
    public void setCommodityCodeService(CommodityCodeService commodityCodeService) {
        this.commodityCodeService = commodityCodeService;
    }

    /**
     * Gets the businessObjectService attribute.
     * 
     * @return Returns the businessObjectService.
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Sets the businessObjectService attribute value.
     * 
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * 
     * @param batchFileDirectoryName
     */
    public void setBatchFileDirectoryName(String batchFileDirectoryName) {
        this.batchFileDirectoryName = batchFileDirectoryName;
    }

    /**
     * Gets the collectorReportWriterService attribute value.
     * @return collectorReportWriterService The collectorReportWriterService to be retrieved.
     */
    public ReportWriterService getCommodityCodeReportWriterService() {
        return this.commodityCodeReportWriterService;
    }

    /**
     * Sets the collectorReportWriterService attribute value.
     * @param collectorReportWriterService The collectorReportWriterService to set.
     */
    public void setCommodityCodeReportWriterService(ReportWriterService commodityCodeReportWriterService) {
        this.commodityCodeReportWriterService = commodityCodeReportWriterService;
    }

    /**
     * Sets the implementation of the KualiConfigurationService to use
     * @param configurationService an implementation of the KualiConfigurationService
     */
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    /**
     * Gets the configurationService attribute. 
     * @return Returns the configurationService.
     */
    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    /**
     * Sets the implementation of ParameterService to use
     * @param parameterService an implementation of ParameterService
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Gets the parameterService attribute. 
     * @return Returns the parameterService.
     */
    public ParameterService getParameterService() {
        return parameterService;
    }

}
