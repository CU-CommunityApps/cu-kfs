/*
 * Copyright 2008 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.module.bc.document.service.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.module.bc.BCConstants;
import org.kuali.kfs.module.bc.BCKeyConstants;
import org.kuali.kfs.module.bc.BCPropertyConstants;
import edu.cornell.kfs.module.bc.CUBCPropertyConstants;
import org.kuali.kfs.module.bc.CUBCKeyConstants;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionHeader;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionLockStatus;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionPayRateHolding;
import org.kuali.kfs.module.bc.businessobject.CalculatedSalaryFoundationTracker;
import org.kuali.kfs.module.bc.businessobject.PendingBudgetConstructionAppointmentFunding;
import org.kuali.kfs.module.bc.document.service.BudgetDocumentService;
import org.kuali.kfs.module.bc.document.service.LockService;
import org.kuali.kfs.module.bc.exception.BudgetConstructionLockUnavailableException;
import org.kuali.kfs.module.bc.util.BudgetParameterFinder;
import org.kuali.kfs.module.bc.util.ExternalizedMessageWrapper;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.ObjectUtil;
import org.kuali.kfs.sys.KFSConstants.ReportGeneration;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.validation.impl.AccountingDocumentRuleBaseConstants.ACCOUNT_NUMBER;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.bo.BusinessObject;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.KualiInteger;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.util.WebUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import edu.cornell.kfs.module.bc.batch.dataaccess.PSPositionDataDao;
import edu.cornell.kfs.module.bc.businessobject.PSJobData;
import edu.cornell.kfs.module.bc.businessobject.SipImportData;
import edu.cornell.kfs.module.bc.document.dataaccess.SipImportDao;
import edu.cornell.kfs.module.bc.document.service.SipImportService;
import edu.cornell.kfs.module.bc.util.CUBudgetParameterFinder;

/**
 *  The following describes what the class' goals:
 * 1.	Read imported data into a string, parse into individual fields using the business object.
 * 		1.	Validate imported data is correct (i.e. SIP Eligibility flag, etc) one line at a time.
 * 		2.	Apply business rules (i.e. deferred requires a note) to each imported line.
 *		3.	If line is in error:
 *			1.  Write out the imported line (as it was read, without modification) 
 *			to the log file followed by any errors found, one line for each error with an indicator 
 *			explaining which column had the error and what the error was.
 *	2.  Summarize all errors by org and by error type with a count for each.  Use this an input
 *			To the error summary page.
 *	3.  Set a boolean to true to indicate that errors were found.  This will be used later to not upload
 *			the data to the SIP table in the KFS DB as only a completely clean file will allow all the
 *			records to be saved.               
 *	4.	If a line is not in error, leave it out of log file there by only showing the lines in 
 * 			error along with the detail of those error messages.
 * 	5.	Once all records have been processed:
 * 			1.  Return the error log file to the user as a download
 * 	6.	If no errors were found:
 * 			1.	Return the error log file to the user as a download but instead with a message that no errors were found.
 * 			2.  Store all imported lines to the CU_LD_BCN_SIP_T table.
 * 
 */


public class SipImportServiceImpl implements SipImportService {
    
    private BusinessObjectService businessObjectService;
    private LockService lockService;
    private int importCount;
    private OptionsService optionsService;
    private SipImportDao sipImportDao;
    private BudgetDocumentService budgetDocumentService;
	protected PSPositionDataDao positionDataDao;
	public List<SipImportData> entireSipImport;
	
	// This stores the number of errors for each error message regardless of UnitId (C Level org)
	protected int errorCount[];
	String RulesErrorList;
	String ValuesErrorList;
	
	protected int warningCount[];
	String RulesWarningList;
	String ValuesWarningList;

	// Variables for parameters
	String SIP_IMPORT_MODE;
	List<String> SIP_EXPORT_EXECUTIVES;
	String SIP_IMPORT_AWARD_CHECK;
	
	// This stores the number of errors for each org (UnitId) for each error message as they are found
	Map<String, HashMap<Integer, Integer>> errorCountByUnitId;
	
	// This stores the number of warning for each org (UnitId) for each error message as they are found
	Map<String, HashMap<Integer, Integer>> warningCountByUnitId;

	protected String[] ErrorMessages = {
			"\tPosition number was not found in the original SIP exported data.\n",
			"\tEmployee Id was not found in the original SIP exported data.\n",
			"\tThis position number / employee id combination is not eligible for SIP.\n",
			"\tThis position number / employee id combination compensation rate is different than that in KFS.\n",
			"\tThe SIP award (Increase To minimum + Merit + Equity) is greater than zero AND the Deferred amount is also greater than 0.\n",
			"\tThe SIP award (Increase To minimum + Merit + Equity) is greater than zero AND a note was also provided.\n",
			"\tDeferred is greater than 0 AND a note was not provided.\n",
			"\tThe SIP award (Increase To minimum + Merit + Equity) is zero AND a note was not provided.\n",
			"\tThe ABBR flag is set to Y.\n",
			"\tExecutive position found."
	};
	
	protected String WarningMessages[];
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SipImportService.class);

    /**
     * 
     * @see edu.cornell.kfs.module.bc.document.service.SipImportService#importFile(java.io.InputStream)
     */
    @Transactional
    public String importFile(InputStream fileImportStream, List<ExternalizedMessageWrapper> errorReport , String principalId) {
		// Get values for parameters
		SIP_IMPORT_MODE = CUBudgetParameterFinder.getSipImportMode();
		SIP_EXPORT_EXECUTIVES = CUBudgetParameterFinder.getSIPExecutives();
		SIP_IMPORT_AWARD_CHECK = CUBudgetParameterFinder.getSipImportAwardCheck();
		
		int TotalErrorCount = 0;
        errorCountByUnitId = new HashMap<String, HashMap<Integer, Integer>>();
        warningCountByUnitId = new HashMap<String, HashMap<Integer, Integer>>();
        SipImportData sipImportData;
        entireSipImport = new ArrayList<SipImportData>();
        this.importCount = 0;
        List<ExternalizedMessageWrapper> errorReportDetail = new ArrayList<ExternalizedMessageWrapper>();
        
        // Error message setup
        errorCount = new int[10];

    	
        //Warning message setup
    	warningCount = new int[2];
    	WarningMessages = new String[2];
    	WarningMessages[0]= new String("\tSIP Total exceeds " + SIP_IMPORT_AWARD_CHECK + "% of prior year annual rate as defined in PeopleSoft.\n");
    	WarningMessages[1] = new String("\tPercent Distribution is not equal to 1.\n");
 
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileImportStream));
		errorReportDetail.add(new ExternalizedMessageWrapper("\nUnitID\tHR_Dept_ID\tKFS_Dept_ID\tDepartment_Name\tPosition_Nbr\tPosition_Description\tEmplID\tPerson_Name\tSIP_Eligible\tSIP_Employee_Type\tEmployee_RCD\tJob_Code\tJob_Code_Short_Desc\tJob_Family\tPosition_FTE\tGrade\tCU_State_Cert\tComp_Frequency\tAnnual_Rate\tComp_Rate\tJob_Standard_Hours\tWork_Months\tJob_Function\tJob_Function_Description\tIncrease_to_Minimum\tEquity\tMerit\tNote\tDeferred\tCU_ABBR_Flag\tTOTAL_Job_Planned_Commit\tTotal_Distributions\tBGP_FLSA"));

        try {
        	// Loops through each line in the SIP import file, validating each one.   Records errors encountered for each line both across all UnitIds (C level orgs) and 
        	//   for each UnitId.  The variable "errorCount" contains a count of each type of error (not warning) message across all UnitIDs and the variable
        	//   "errorCountByUnitId" keeps track of the error counts by message for each UnitId.        	
        	while(fileReader.ready()) {
            	// Read one line from the import file
            	String sipImportLine = fileReader.readLine();
            	
            	// Add 1 to the counter and create a new sipImportData object
            	this.importCount++;
            	
            	// Get a new business object to hold the line in
            	sipImportData = new SipImportData();
            	
            	// Parse the imported line into separate fields, preserving empty fields as well
            	sipImportLine = sipImportLine.replace("\"","");
				String[] tokens = StringUtils.splitPreserveAllTokens(sipImportLine, "\t");
				
				//Check to make sure that the line is tabbed delimited.  If not then return immediately.
				if (tokens.length != DefaultImportFileFormat.fieldNames.length)
					return "NOT TAB DELIMITED|" + this.importCount;
				
				// Apply data from the line to the business object
				ObjectUtil.buildObject(sipImportData, tokens, Arrays.asList(DefaultImportFileFormat.fieldNames));
				
				// Reset variables that will contain the errors and warnings for this line
		    	RulesErrorList = "";
		    	ValuesErrorList = "";
		    	RulesWarningList = "";
		    	ValuesWarningList = "";
		    	
				// VALIDATE the line
				validateSipRules(sipImportData.getPositionNbr(), sipImportData.getEmplId(), sipImportData.getCompRt(),
								  sipImportData.getUnitId(), sipImportData.getCuAbbrFlag());
				validateSipValues(sipImportData.getIncToMin(), sipImportData.getMerit(), sipImportData.getEquity(), 
									sipImportData.getDeferred(), sipImportData.getNote(), sipImportData.getCompRt(),
									sipImportData.getUnitId());
				
				// Generate WARNING messages
				if (!RulesWarningList.isEmpty())
					if (!ValuesWarningList.isEmpty()) {
						errorReportDetail.add(new ExternalizedMessageWrapper("\n" + sipImportLine + "\n" + RulesWarningList + ValuesWarningList));
					}
					else {
						errorReportDetail.add(new ExternalizedMessageWrapper("\n" + sipImportLine + "\n" + RulesWarningList));
					}

				else
					if (!ValuesWarningList.isEmpty()) {
						errorReportDetail.add(new ExternalizedMessageWrapper("\n" + sipImportLine + "\n" + ValuesWarningList));
					}
				
				// Generate ERROR Messages.
				if (!RulesErrorList.isEmpty())
					if (!ValuesErrorList.isEmpty()) {
						errorReportDetail.add(new ExternalizedMessageWrapper("\n" + sipImportLine + "\n" + RulesErrorList + ValuesErrorList));
					}
					else {
						errorReportDetail.add(new ExternalizedMessageWrapper("\n" + sipImportLine + "\n" + RulesErrorList));
					}

				else
					if (!ValuesErrorList.isEmpty()) {
						errorReportDetail.add(new ExternalizedMessageWrapper("\n" + sipImportLine + "\n" + ValuesErrorList));
					}
					else
						// Add to sip Import Data list
						entireSipImport.add(sipImportData);
            }
        	//  Add some blank lines after the last detail line followed by the header row.
			errorReportDetail.add(new ExternalizedMessageWrapper("\n\n"));
        	
            // Count the errors across all UnitIds (C Level orgs) regardless of the error message
            for ( int i=0; i<=(ErrorMessages.length-1); i++ ) { TotalErrorCount += errorCount[i]; }
            
            // If there were no errors at all, then do the following tasks:
            if (TotalErrorCount != 0)
            {
    			// Generate SIP Import log/error report
            	// ======================================
    			// Attach summary information
            	SipImportWarningSummary(errorReport);

            	SipImportErrorSummary(errorReport);
            	
            	//  Next, append the errorReportDetail.
            	errorReport.addAll(errorReportDetail);
            }
            else
            	errorReport.add(new ExternalizedMessageWrapper("\n\tNo SIP IMPORT ERRORS FOUND\n"));
            
            // Additional messages and information.  NOTE:  you cannot put new line characters in front of anything with a parameter value as it will cause 
            //   "temp = SpringContext.getBean(KualiConfigurationService.class).getPropertyString(messageWrapper.getMessageKey());" to be null!
            if (importCount == 0 ) 
            	errorReport.add(new ExternalizedMessageWrapper(CUBCKeyConstants.MSG_SIP_IMPORT_NO_IMPORT_RECORDS));
            else
            	errorReport.add(new ExternalizedMessageWrapper(CUBCKeyConstants.MSG_SIP_IMPORT_COUNT, String.valueOf(importCount)));
            
            return "OK";
            
        }
        catch (Exception e) {
        	errorReport.add(new ExternalizedMessageWrapper(CUBCKeyConstants.ERROR_SIP_IMPORT_ABORTED, e.getMessage()));
            return e.getMessage();
        }
    }
 
    @NonTransactional
    public void generateValidationReportInTextFormat(List<ExternalizedMessageWrapper> logMessages, ByteArrayOutputStream baos) {
    	try {
            String message = "";
          	String temp = "";
            for (ExternalizedMessageWrapper messageWrapper : logMessages) {
                if (messageWrapper.getParams().length == 0 ) 
                	message = messageWrapper.getMessageKey();
                else {
                    temp = SpringContext.getBean(KualiConfigurationService.class).getPropertyString(messageWrapper.getMessageKey());
                    message = MessageFormat.format(temp, messageWrapper.getParams());
                }
                if (ObjectUtils.isNotNull(message))
                	baos.write(message.getBytes());
            }
            
    	}
		catch (Exception e) {
			LOG.error("generateValidationReportInText Exception: " + e.getMessage());
		}
    }

    
    @NonTransactional
    public void generateValidationReportInPdfFormat(List<ExternalizedMessageWrapper> logMessages, ByteArrayOutputStream baos) throws DocumentException {
        Document document = new Document();
        PdfWriter.getInstance(document, baos);
        document.open();
        for (ExternalizedMessageWrapper messageWrapper : logMessages) {
            String message, temp;
            if (messageWrapper.getParams().length == 0 ) 
            	message = SpringContext.getBean(KualiConfigurationService.class).getPropertyString(messageWrapper.getMessageKey());
            else {
                temp = SpringContext.getBean(KualiConfigurationService.class).getPropertyString(messageWrapper.getMessageKey());
                message = MessageFormat.format(temp, messageWrapper.getParams());
            }
            document.add(new Paragraph(message));
        }

        document.close();
    }
    
    /**
     * Sets the business object service
     * 
     * @param businessObjectService
     */
    @NonTransactional
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
    
   
    /**
     * Sets the SIP import DAO
     * 
     * @param sipImportDao
     */
    @NonTransactional
    public void setSipImportDao(SipImportDao sipImportDao) {
        this.sipImportDao = sipImportDao;
    }

    
    /**
     * Sip import file field names and lengths used by 
     * 
     */
    protected static class DefaultImportFileFormat {
        private static final int[] fieldLengths = new int[] {40,10,10,40,8,30,7,123,1,1,3,6,10,6,22,3,1,1,12,12,4,3,3,10,19,19,19,500,19,1,19,22,2};
        private static final String[] fieldNames = new String[] {
        	 "unitId", "hrDeptId", "kfsDeptId", "deptName", "positionNbr", "posDescr", "emplId", "personNm", "sipEligFlag", "emplType",
        	 "emplRcd", "jobCode", "jobCdDescShrt", "jobFamily", "posFte", "posGradeDflt", "cuStateCert", "compFreq", "annlRt",
        	 "compRt", "jobStdHrs", "wrkMnths", "jobFunc", "jobFuncDesc", "incToMin", "equity", "merit", "note", "deferred",
        	 "cuAbbrFlag", "apptTotIntndAmt", "apptRqstFteQty", "positionType"
        };
    }
    
	/**
	 * Validates that Position is in HR / P has a "Budgeted Position" = "Y".
	 * 
	 * @param positionNumber as a string
	 * @return true if valid, false otherwise
	 */
	protected boolean validPosition(String positionNumber) {
		// The SIP export generates either a CSV or tab delimited file.  Sometimes, MS Excel is used to read in the data.  In
		//   the process, Excel removes leading zeros, even on quoted data.  So since we know that this is happening with the
		//   position number, we check here to make sure that we are getting all 8 characters (digits).  If we not assume that
		//   the leading zeros were truncated.  In other systems, they may not have truncated the zeros so to work with that we
		//   check the length and don't assume that everyone uses Excel, though most probably will!
		try {
			if (ObjectUtils.isNotNull(positionNumber)) {
				if (positionNumber.length()==6)
					positionNumber = "00" + positionNumber;
				
				// Verify that the position number is 8 otherwise it is invalid
				if (positionNumber.length()==8) {
					//  Verify if this position number exists or not.
					Map<String, Object> myCriteria = new HashMap<String, Object>();
					myCriteria.put(KFSPropertyConstants.POSITION_NUMBER, positionNumber);
					if(businessObjectService.countMatching(PendingBudgetConstructionAppointmentFunding.class, myCriteria) > 0)
						return true;
					else
						return false;
				}
				else
					return false;	
			}
			else
				return false;
		}
		catch (Exception e) {
			LOG.error("validPosition Exception: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Returns whether or not the EmplID exists in the LD_PNDBC_APPTFND_T table
	 * 
	 * @param emplid as a string
	 * @return true if found, false if not found
	 */
	protected boolean validEmplid(String emplid) {
		try {
			if (ObjectUtils.isNotNull(emplid)) {
				Map<String, Object> myCriteria = new HashMap<String, Object>();
				myCriteria.put(KFSPropertyConstants.EMPLID, emplid);
				if(businessObjectService.countMatching(PendingBudgetConstructionAppointmentFunding.class, myCriteria) > 0)
					return true;
				else
					return false;
			}
			else 
				return false;
		}
		catch (Exception e) {
			LOG.error("validEmplid Exception: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Returns whether or not the ABBR Flag is valid.  The ABBR Flag
	 * is valid if it's value is NOT Y otherwise it is invalid if its
	 * value is "Y".
	 * 
	 * @param ABBR Flag as a string
	 * @param emplid as a string
	 * @return true if valid (as defined in the description), false if not valid
	 */
	protected boolean isAbbrFlagValid(String AbbrFlag) {
		try
		{
			if (ObjectUtils.isNotNull(AbbrFlag) )
			{
				String myABBRFlag = AbbrFlag.toUpperCase();
				if (myABBRFlag.equals("Y"))
					return false;
				else
					return true;
			}
			else
				return false;
		}
		catch (Exception e) {
			LOG.error("isSipEligible Exception: " + e.getMessage());
			return false;
		}
	}

	
	/**
	 * Returns whether or not the provided position number, employee identifier return a 
	 * SIP Eligible value of "Y" as found in the CU_PS_JOB_DATA table.
	 * 
	 * @param positionNumber as a string
	 * @param emplid as a string
	 * @return true if found, false if not found
	 */
	protected boolean isSipEligible(String positionNumber, String emplid) {
		try {
			if( (ObjectUtils.isNotNull(positionNumber)) && ObjectUtils.isNotNull(emplid) ) {
				//  See comment above in validPosition() for details
				if (positionNumber.length()==6)
					positionNumber = "00" + positionNumber;
				
				// Verify that the position number is 8 otherwise it is invalid
				if (positionNumber.length()==8) {
					Map<String, Object> myCriteria = new HashMap<String, Object>();
					myCriteria.put(CUBCPropertyConstants.PSJobDataProperties.POSITION_NBR, positionNumber);
					myCriteria.put(CUBCPropertyConstants.PSJobDataProperties.EMPLID, emplid);
					PSJobData psJobData = (PSJobData) businessObjectService.findByPrimaryKey(PSJobData.class, myCriteria);
					if (ObjectUtils.isNotNull(psJobData))
						if (ObjectUtils.isNotNull(psJobData.getSipEligibility()))
							if (psJobData.getSipEligibility().equals("Y"))
								return true;
							else
								return false;
						else
							return false;
					else
						return false;
				}
				else
					return false;
				}
			else
				return false;
		}
		catch (Exception e) {
			LOG.error("isSipEligible Exception: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Returns whether or not the position number/employee id as provided
	 * in the SIP Import agrees with the compensation rate that is currently
	 * stored in the database.
	 * 
	 * @param positionNumber as a string
	 * @param emplid as a string
	 * @param compensation rate as a KualiDecimal
	 * @return true if the data agrees with the database, false if not found
	 */
	protected boolean validCompRate(String positionNumber, String emplid, KualiDecimal CompRate ) {
		try {
		if( (ObjectUtils.isNotNull(positionNumber)) && ObjectUtils.isNotNull(emplid) && ObjectUtils.isNotNull(CompRate)) {
			//  See comment above in validPosition() for details
			if (positionNumber.length()==6)
				positionNumber = "00" + positionNumber;
			
			// Verify that the position number is 8 otherwise it is invalid
			if (positionNumber.length()==8) {
				Map<String, Object> myCriteria = new HashMap<String, Object>();
				myCriteria.put(CUBCPropertyConstants.PSJobDataProperties.POSITION_NBR, positionNumber);
				myCriteria.put(CUBCPropertyConstants.PSJobDataProperties.EMPLID, emplid);
				PSJobData psJobData = (PSJobData) businessObjectService.findByPrimaryKey(PSJobData.class, myCriteria);
				if(ObjectUtils.isNotNull(psJobData)) {
					KualiDecimal DBCompRate = psJobData.getCompRate();
					if (CompRate.equals(DBCompRate))
						return true;
					else
						return false;
				}
				else
					return false;
			}
			else
				return false;
		}
		else
			return false;
		}
		catch (Exception e) {
			LOG.error("validCompRate Exception: " + e.getMessage());
			return false;
		}
	}
	

	/**
	 * 
	 * @param positioNumber is the position number to check against the SIP_EXPORT_EXECUTIVES list.
	 * @return boolean of true if it was found in the list or false if it was not found in the list.
	 * 
	 */
	protected boolean isSipExecutive(String positionNumber) {
		if (SIP_EXPORT_EXECUTIVES.contains(positionNumber))
			return true;
		else
			return false;
	}
	
	protected double requestedPerCentDistributionSum(String positionNumber, String emplId) {
		return sipImportDao.getTotalPerCentDistribution(positionNumber, emplId);
	}
	
  
	/**
	 * Coordinates checking all of the SIP rules and returns error and warning messages as needed.
	 * Also tracks counts of error and warning messages within orgs (UnitId's).
	 * 
	 * @param positionNumber The position number from the SIP Import line
	 * @param emplId The emplId from the SIP Import line
	 * @param CompRate The CompRate from the SIP Import line
	 * @param UnitId The UnitId from the SIP Import line
	 * @param AbbrFlag The AbbrFlag from the SIP Import line
	 * @return boolean - If the method completes, it will return true, otherwise it will return false
	 */
	protected boolean validateSipRules(String positionNumber, String emplId, KualiDecimal CompRate, String UnitId, String AbbrFlag)
	{
		try {
				// Is this position found?
				if (!validPosition(positionNumber)){ 
					int ErrorMessageNumber = 0;
					RulesErrorList += ErrorMessages[ErrorMessageNumber];
					UpdateErrorCounts(ErrorMessageNumber, UnitId);
				}
				
				// Is this EmplId found?
				if (!validEmplid(emplId)) {
					int ErrorMessageNumber = 1;
					RulesErrorList += ErrorMessages[ErrorMessageNumber];
					UpdateErrorCounts(ErrorMessageNumber, UnitId);
				}
				
				// Is this position number / emplid combination found?
				if (!isSipEligible(positionNumber, emplId)){
					int ErrorMessageNumber = 2;
					RulesErrorList += ErrorMessages[ErrorMessageNumber];
					UpdateErrorCounts(ErrorMessageNumber, UnitId);
				}
						
				// if the compensation rate supplied by the import is different than that already stored in the database, issue an error message
				if (!validCompRate(positionNumber, emplId, CompRate)) {
					int ErrorMessageNumber = 3;
					RulesErrorList += ErrorMessages[ErrorMessageNumber];
					UpdateErrorCounts(ErrorMessageNumber, UnitId);
				}
				
				// if the AbbrFlag column is "Y" then issue an error message
				if (!isAbbrFlagValid(AbbrFlag)) {
					int ErrorMessageNumber = 8;
					RulesErrorList += ErrorMessages[ErrorMessageNumber];
					UpdateErrorCounts(ErrorMessageNumber, UnitId);
				}
				
				// If this position is in the SIP_EXPORT_EXECUTIVES list then issue an error message.
				if (isSipExecutive(positionNumber)) {
					int ErrorMessageNumber = 9;
					RulesErrorList += ErrorMessages[ErrorMessageNumber];
					UpdateErrorCounts(ErrorMessageNumber, UnitId);	 
				}
				 
				// If the sum of the APPT_RQST_FTE_QTY column in the LD_PNDBC_APPTFND_T table totals other than 1, issue a warning
				if (requestedPerCentDistributionSum(positionNumber, emplId) != 1.00) {
					int WarningMessageNumber = 1;
					RulesWarningList += WarningMessages[WarningMessageNumber];
					UpdateWarningCounts(WarningMessageNumber, UnitId);		
				}
			
				return true;
		}
		catch (Exception e) {
			LOG.error("validateSipRules Exception: " + e.getMessage());
			return false;
		}
	}
	
	// Coordinates validating the SIP business rules based on the values submitted for SIP.
	//  The rules being validated are provided below in comments before each validation.
	protected boolean validateSipValues(KualiDecimal IncToMin, KualiDecimal Merit, 
												KualiDecimal Equity, KualiDecimal Deferred,
												String Note, KualiDecimal CompRate,
												String UnitId)
	{
		try {
			
			KualiDecimal totalSIP =  IncToMin.add(Merit.add(Equity));
			
	//		1. If there is a SIP award (Merit + Increase to Minimum + Equity >0) then:
	//			1. There can't be a Deferred amount.
	//			2. There can't be a Note.
			if (totalSIP.isGreaterThan(KualiDecimal.ZERO))
			{
				if (Deferred.isPositive())
				{
					ValuesErrorList = ErrorMessages[4];
					UpdateErrorCounts(4, UnitId);
				}
// The following was deemed not required (see JIRA KITI-3086 comment on 4-10-2012)
//				if (ObjectUtils.isNotNull(Note))
//				{
//					ValuesErrorList += ErrorMessages[5];
//					UpdateErrorCounts(5, UnitId);
//				}
			}
	
	//		2. If there is a Deferred amount
	//			1. There can't be a SIP award
	//			2. There must be a Note
			if (Deferred.isPositive()) {
				if (ObjectUtils.isNull(Note)) {
					ValuesErrorList += ErrorMessages[6];
					UpdateErrorCounts(6, UnitId);
				}
			}
	
		
	//		3. If there is not a SIP award
	//			1. There must be a Note
			if (totalSIP.isZero())
				if (ObjectUtils.isNull(Note)) {
					ValuesErrorList += ErrorMessages[7];
					UpdateErrorCounts(7, UnitId);
				}
				
	//		4. SIP Awards are 'reasonable' (metric needed) : is sum of SIP awards between 0 and SIP_IMPORT_AWARD_CHECK parameter of the Compensation Rate?
			if ( totalSIP.isGreaterThan(CompRate.multiply(new KualiDecimal(SIP_IMPORT_AWARD_CHECK)))) {
				ValuesWarningList += WarningMessages[0];
				UpdateWarningCounts(0, UnitId);
			}
			
			return true;
		}
		catch (Exception e) {
			LOG.error("validateSipValues Exception: " + e.getMessage());
			return false;
		}
	}

	/**
	 * 
	 * @param WarningMessageNumber - The number of the error message to increment by 1
	 * @param UnitId - The org as provided in the SIP Import file
	 * @return void
	 */
	protected void UpdateWarningCounts (int WarningMessageNumber, String UnitId) {
		try {
			// This code manages the warning counts regardless of the org (UnitID)
			warningCount[WarningMessageNumber]++;
			
			// This code manages the counts for a specific warning message for each UnitID (C Level org)
			HashMap<Integer, Integer> myWarningCount = new HashMap<Integer, Integer>();
			if (warningCountByUnitId.get(UnitId)==null)
			{
				// Since we have nothing (null) for this UnitID this code initializes the HashMap for these structures.
				myWarningCount.put(WarningMessageNumber, 1);  			// Generate the warning message and initialize its counter to 1.
				warningCountByUnitId.put(UnitId, myWarningCount); 		// Initializes the warning count for this message for this UnitID to 1.
			}
			else
			{
				// Check and make sure that for this UnitId for this warning we already have it initialized to 1, if not, then just set its value to 1.
				if (ObjectUtils.isNull(warningCountByUnitId.get(UnitId).get(WarningMessageNumber))) {
					//Get all the current warnings and their counts so we don't lose them!
					myWarningCount = warningCountByUnitId.get(UnitId);
					//Add the new warningMessageNumber and initialize it to 1
					myWarningCount.put(WarningMessageNumber, 1);
					// Update the warning counts for this specific UnitId
					warningCountByUnitId.put(UnitId,myWarningCount);
				}
				else
					// Adds 1 to an existing warning count for this UnitId for this warning message
					warningCountByUnitId.get(UnitId).put(WarningMessageNumber, warningCountByUnitId.get(UnitId).get(WarningMessageNumber)+1);	
			}
		}
		catch (Exception e) {
			LOG.error("UpdateWarningCounts Exception: " + e.getMessage());
		}
	}	
	
	/**
	 * 
	 * @param ErrorMessageNumber - The number of the error message to increment by 1
	 * @param UnitId - The org as provided in the SIP Import file
	 * @return void
	 */
	protected void UpdateErrorCounts (int ErrorMessageNumber, String UnitId) {
		try {
			// This code manages the error counts regardless of the org (UnitID)
			errorCount[ErrorMessageNumber]++;
			
			// This code manages the counts for a specific error message for each UnitID (C Level org)
			HashMap<Integer, Integer> myErrCount = new HashMap<Integer, Integer>();
			if (errorCountByUnitId.get(UnitId)==null)
			{
				// Since we have nothing (null) for this UnitID this code initializes the HashMap for these structures.
				myErrCount.put(ErrorMessageNumber, 1);  			// Generate the error message and initialize its counter to 1.
				errorCountByUnitId.put(UnitId, myErrCount); 		//Initializes the error count for this message for this UnitID to 1.
			}
			else
			{
				// Check and make sure that for this UnitId for this error we already have it initialized to 1, if not, then just set its value to 1.
				if (ObjectUtils.isNull(errorCountByUnitId.get(UnitId).get(ErrorMessageNumber))) {
					//Get all the current errors and their counts so we don't lose them!
					myErrCount = errorCountByUnitId.get(UnitId);
					//Add the new errorMessageNumber and initialize it to 1
					myErrCount.put(ErrorMessageNumber, 1);
					// Update the error counts for this specific UnitId
					errorCountByUnitId.put(UnitId,myErrCount);
				}
				else
					// Adds 1 to an existing error count for this UnitId for this error message
					errorCountByUnitId.get(UnitId).put(ErrorMessageNumber, errorCountByUnitId.get(UnitId).get(ErrorMessageNumber)+1);	
			}
		}
		catch (Exception e) {
			LOG.error("UpdateErrorCounts Exception: " + e.getMessage());
		}
	}	
	
	
	/**
	 * 
	 * @return a String value containing two error summaries.  The first is a list of each type of error
	 * found prefaced by a count across all UnitIds (orgs). The second summary is a listing of each type
	 * of error found within each UnitId (C Level org) prefaced with a count.  The summaries are separated by 3
	 * blank lines (newline characters). 
	 */
	// Build Summary 
	protected int SipImportErrorSummary(List<ExternalizedMessageWrapper> errorReport) {
		int TotalErrorCount = 0;
		try {
			if (errorCount.length > 0)
				errorReport.add(new ExternalizedMessageWrapper("\n\n===========   SIP IMPORT ERROR SUMMARY ACROSS ALL C-LEVEL ORGS   ====================\n\n"));
			int i;
			// Generate summary of each error type prefaced with a count.
			for ( i=0; i<=(ErrorMessages.length-1); i++ ){
				if (errorCount[i]!=0) {  // Only display the error if there is an error count > 0
					errorReport.add(new ExternalizedMessageWrapper(errorCount[i] + " - " + ErrorMessages[i] + "\n"));
					TotalErrorCount += errorCount[i];
				}
			}
			
		    errorReport.add(new ExternalizedMessageWrapper("\n\n===============   SIP ERROR SUMMARY - ERRORS BY C-LEVEL ORG   =======================\n"));
				
			// Generate summary of the counts for each UnitId (C Level org) for each error type, prefaced with a count.
		    Iterator it = errorCountByUnitId.entrySet().iterator(); 
		    while (it.hasNext()) { 
		        Map.Entry pairs = (Map.Entry)it.next(); 
		        String UnitId = (String) pairs.getKey();
		        errorReport.add(new ExternalizedMessageWrapper("\n" + UnitId + "\n"));
		        // Display Errors
		        for (Map.Entry<Integer, Integer> myErrCount : errorCountByUnitId.get(UnitId).entrySet()) {
		        	errorReport.add(new ExternalizedMessageWrapper("\t" + myErrCount.getValue() + " - " + ErrorMessages[myErrCount.getKey()] + "\n"));
		        }
		    } 
		    
		    errorReport.add(new ExternalizedMessageWrapper("\n\n=====================   SIP WARNINGS AND ERRORS DETAIL   ============================\n") );

		    return TotalErrorCount;

		}
		catch (Exception e) {
			LOG.error("SipImportErrorSummary Exception: " + e.getMessage());
			errorReport.add(new ExternalizedMessageWrapper("SipImportErrorSummary Exception: " + e.getMessage() + "\n"));
			return TotalErrorCount;
		}
	}
	
	/**
	 * 
	 * @return a String value containing two error summaries.  The first is a list of each type of error
	 * found prefaced by a count across all UnitIds (orgs). The second summary is a listing of each type
	 * of error found within each UnitId (C Level org) prefaced with a count.  The summaries are separated by 3
	 * blank lines (newline characters). 
	 */
	protected int SipImportWarningSummary(List<ExternalizedMessageWrapper> warningReport) {
		int TotalWarningCount = 0;
		try {
			if (warningCount.length > 0)
				warningReport.add(new ExternalizedMessageWrapper("\n\n===========   SIP IMPORT WARNING SUMMARY ACROSS ALL C-LEVEL ORGS   ====================\n\n"));

			int i;
			// Generate summary of each warning type prefaced with a count.
			for ( i=0; i<=(WarningMessages.length-1); i++ ){
				if (warningCount[i]!=0) {  // Only display the warning if there is an warning count > 0
					warningReport.add(new ExternalizedMessageWrapper(warningCount[i] + " - " + WarningMessages[i] + "\n"));
					TotalWarningCount += warningCount[i];
				}
			}
			
			warningReport.add(new ExternalizedMessageWrapper("\n\n===============   SIP WARNING SUMMARY - WARNINGS BY C-LEVEL ORG   =======================\n"));
				
			// Generate summary of the counts for each UnitId (C Level org) for each warning type, prefaced with a count.
		    Iterator it = warningCountByUnitId.entrySet().iterator(); 
		    while (it.hasNext()) { 
		        Map.Entry pairs = (Map.Entry)it.next(); 
		        String UnitId = (String) pairs.getKey();
		        warningReport.add(new ExternalizedMessageWrapper("\n" + UnitId + "\n"));
		        // Display Warnings
		        for (Map.Entry<Integer, Integer> myWarningCount : warningCountByUnitId.get(UnitId).entrySet()) {
		        	warningReport.add(new ExternalizedMessageWrapper("\t" + myWarningCount.getValue() + " - " + WarningMessages[myWarningCount.getKey()] + "\n"));
		        }		        
		    } 
		    
		    return TotalWarningCount;

		}
		catch (Exception e) {
			LOG.error("SipImportWarningSummary Exception: " + e.getMessage());
			warningReport.add(new ExternalizedMessageWrapper("SipImportWarningSummary Exception: " + e.getMessage() + "\n"));
			return TotalWarningCount;
		}
	}
}

