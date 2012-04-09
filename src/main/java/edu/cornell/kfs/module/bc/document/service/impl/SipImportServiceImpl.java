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

/**
 *  The following describes what this class' goals:
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
	
	// This stores the number of errors for each error message regardless of UnitId (C Level org)
	protected int errorCount[] = new int[8];
	protected int warningCount[] = new int[1];

	// This stores the number of errors for each org (UnitId) for each error message as they are found
	Map<String, HashMap<Integer, Integer>> errorCountByUnitId = new HashMap<String, HashMap<Integer, Integer>>();

	protected String[] ErrorMessages = {
			"\tPosition number was not found in the original SIP exported data.\n",
			"\tEmployee Id was not found in the original SIP exported data.\n",
			"\tThis position number / employee id combination is not eligible for SIP.\n",
			"\tThis position number / employee id combination compensation rate is different than that in KFS.\n",
			"\tThe SIP award (Increase To minimum + Merit + Equity) is greater than zero AND the Deferred amount is also greater than 0.\n",
			"\tThe SIP award (Increase To minimum + Merit + Equity) is greater than zero AND a note was also provided.\n",
			"\tDeferred is greater than 0 AND a note was not provided.\n",
			"\tThe SIP award (Increase To minimum + Merit + Equity) is zero AND a note was not provided.\n"
	};
	
	protected String[] WarningMessages = {
			"\tSIP Total exceeds 5% of prior year compensation\n"
	};
	
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(SipImportService.class);
	
    /**
     * 
     * @see edu.cornell.kfs.module.bc.document.service.SipImportService#importFile(java.io.InputStream)
     */
    @Transactional
    public boolean importFile(InputStream fileImportStream, List<ExternalizedMessageWrapper> errorReport , String principalId) {
        int TotalErrorCount = 0;
        SipImportData sipImportData;
        List<SipImportData> entireSipImport = new ArrayList<SipImportData>();
        this.importCount = 0;
        List<ExternalizedMessageWrapper> errorReportDetail = new ArrayList<ExternalizedMessageWrapper>();
        
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(fileImportStream));
        
        try {
        	errorReport.add(new ExternalizedMessageWrapper("\n\n"));
        	errorReport.add(new ExternalizedMessageWrapper("==========================   SIP IMPORT ERROR SUMMARY   ============================="));
        	errorReport.add(new ExternalizedMessageWrapper("\n\n"));
            
        	// Loops through each line in the SIP import file, validating each one.   Records errors encountered for each line both across all UnitIds (C level orgs) and 
        	//   for each UnitId.  The variable "errorCount" contains a count of each type of error (not warning) message across all UnitIDs and the variable
        	//   "errorCountByUnitId" keeps track of the error counts by message for each UnitId.        	
        	while(fileReader.ready()) {
            	// Read one line from the import file
            	String sipImportLine = fileReader.readLine();
            	
            	// Add 1 to the counter and create a new sipImportData object
            	this.importCount++;
            	sipImportData = new SipImportData();
            	
            	// Parse the imported line into separate fields
            	sipImportLine = sipImportLine.replace("\"","");
				String[] tokens = StringUtils.splitPreserveAllTokens(sipImportLine, "\t");
				ObjectUtil.buildObject(sipImportData, tokens, Arrays.asList(DefaultImportFileFormat.fieldNames));
				
				// Performs validation and collects error and warning counts across all orgs and for each org.
				String SipRulesErrorList = validateSipRules(sipImportData.getPositionNbr(), sipImportData.getEmplId(), sipImportData.getCompRt(),
															  sipImportData.getUnitId());
				String SipValueErrorList = validateSipValues(sipImportData.getIncToMin(), sipImportData.getMerit(), sipImportData.getEquity(), 
																sipImportData.getDeferred(), sipImportData.getNote(), sipImportData.getCompRt(),
																sipImportData.getUnitId());
				
				// For each line read in, there will be errors / warnings generated if needed.  Add these details to errorReportDetail
				if (!SipRulesErrorList.isEmpty())
					if (!SipValueErrorList.isEmpty())
						errorReportDetail.add(new ExternalizedMessageWrapper("\n" + sipImportLine + "\n" + SipRulesErrorList + SipValueErrorList));
					else
						errorReportDetail.add(new ExternalizedMessageWrapper("\n" + sipImportLine + "\n" + SipRulesErrorList));

				else
					if (!SipValueErrorList.isEmpty())
						errorReportDetail.add(new ExternalizedMessageWrapper("\n" + sipImportLine + "\n" + SipValueErrorList));
            }
        	//  Add some blank lines after the last detail line
        	errorReportDetail.add(new ExternalizedMessageWrapper("\n\n"));
        	
            // Count the errors across all UnitIds (C Level orgs) regardless of the error message
            for ( int i=0; i<=(ErrorMessages.length-1); i++ ) { TotalErrorCount += errorCount[i]; }
            
            // If there were no errors at all, then do the following tasks:
            if (TotalErrorCount == 0)
            {
            	// Add the ArrayList of SipImportData objects to the CU_LD_BCN_SIP_T table.
				businessObjectService.save(entireSipImport);
							
				// Return a message to the user on the page that there were no errors in the file
				// NOTE: do not put "\n" characters in front of the ExternalizedMessageWrapper parameter otherwise it will cause the 
				//    line SpringContext.getBean(KualiConfigurationService.class).getPropertyString(messageWrapper.getMessageKey())
				//    to return a null.
				errorReport.add(new ExternalizedMessageWrapper(CUBCKeyConstants.MSG_SIP_IMPORT_COUNT, String.valueOf(importCount)));
            }
            else
            {
    			// Generate SIP Import log/error report
    			// Attach summary information
            	SipImportErrorSummary(errorReport);
            	
            	//  Next, append the errorReportDetail.
            	errorReport.addAll(errorReportDetail);
            }
            
            // Additional messages and information.  NOTE:  you cannot put new line characters in front of anything with a parameter value as it will cause 
            //   "temp = SpringContext.getBean(KualiConfigurationService.class).getPropertyString(messageWrapper.getMessageKey());" to be null!
            if (importCount == 0 ) 
            	errorReport.add(new ExternalizedMessageWrapper(CUBCKeyConstants.MSG_SIP_IMPORT_NO_IMPORT_RECORDS));
            else
            	errorReport.add(new ExternalizedMessageWrapper(CUBCKeyConstants.MSG_SIP_IMPORT_COUNT, String.valueOf(importCount)));
            
            return true;
            
        }
        catch (Exception e) {
        	errorReport.add(new ExternalizedMessageWrapper(CUBCKeyConstants.ERROR_SIP_IMPORT_ABORTED, e.getMessage()));
            return false;
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
     * sets lock service
     * 
     * @param lockService
     */
    @NonTransactional
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }
    
    /**
     * sets option service
     * 
     * @param optionsService
     */
    @NonTransactional
    public void setOptionsService(OptionsService optionsService) {
        this.optionsService = optionsService;
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
     * Sets the budgetDocumentService attribute value.
     * @param budgetDocumentService The budgetDocumentService to set.
     */
    @NonTransactional
    public void setBudgetDocumentService(BudgetDocumentService budgetDocumentService) {
        this.budgetDocumentService = budgetDocumentService;
    }
        
    /**
     * Creates the locking key to use in retrieving account locks
     * 
     * @param record
     * @return
     */
    protected String getLockingKeyString(PendingBudgetConstructionAppointmentFunding record) {
        return record.getUniversityFiscalYear() + "-" + record.getChartOfAccountsCode() + "-" + record.getAccountNumber() + "-" + record.getSubAccountNumber();
    }
    
    /**
     * Retrieves Account locks for sip import records
     * 
     * @param lockMap
     * @param messageList
     * @param budgetYear
     * @param user
     * @param records
     * @return
     */
    @Transactional
    protected boolean getSipLock(List<PendingBudgetConstructionAppointmentFunding> lockedRecords, List<ExternalizedMessageWrapper> messageList, Integer budgetYear, Person user, List<BudgetConstructionPayRateHolding> records) {
        List<String> biweeklyPayObjectCodes = BudgetParameterFinder.getBiweeklyPayObjectCodes();
        
        for (BudgetConstructionPayRateHolding record: records) {
            List<PendingBudgetConstructionAppointmentFunding> fundingRecords = this.sipImportDao.getFundingRecords(record, budgetYear, biweeklyPayObjectCodes);
            try {
                lockedRecords.addAll(this.lockService.lockPendingBudgetConstructionAppointmentFundingRecords(fundingRecords, user));
            } catch(BudgetConstructionLockUnavailableException e) {
                BudgetConstructionLockStatus lockStatus = e.getLockStatus();
                if ( lockStatus.getLockStatus().equals(BCConstants.LockStatus.BY_OTHER) ) {
                    messageList.add(new ExternalizedMessageWrapper(BCKeyConstants.ERROR_PAYRATE_ACCOUNT_LOCK_EXISTS));
                    
                    return false;
                } else if ( lockStatus.getLockStatus().equals(BCConstants.LockStatus.FLOCK_FOUND) ) {
                    messageList.add(new ExternalizedMessageWrapper(BCKeyConstants.ERROR_PAYRATE_FUNDING_LOCK_EXISTS));
                    
                    return false;
                } else if ( !lockStatus.getLockStatus().equals(BCConstants.LockStatus.SUCCESS) ) {
                    messageList.add(new ExternalizedMessageWrapper(BCKeyConstants.ERROR_PAYRATE_BATCH_ACCOUNT_LOCK_FAILED));
                    return false;
                }
          }
        }
        return true;
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
     * If retrieving budget locks fails, this method rolls back previous changes
     * 
     */
    protected void doRollback() {
        PlatformTransactionManager transactionManager = SpringContext.getBean(PlatformTransactionManager.class);
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        TransactionStatus transactionStatus = transactionManager.getTransaction(defaultTransactionDefinition);
        transactionManager.rollback( transactionStatus );

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
			LOG.error("validPosition Exceptiom: " + e.getMessage());
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
			LOG.error("validEmplid Exceptiom: " + e.getMessage());
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
			LOG.error("isSipEligible Exceptiom: " + e.getMessage());
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
			LOG.error("validCompRate Exceptiom: " + e.getMessage());
			return false;
		}
	}
	
	//  Coordinates checking all of the SIP rules and returning error messages as needed.  Also tracks counts
	//    of error messages within orgs (UnitId's).
	protected String validateSipRules(String positionNumber, String emplId, KualiDecimal CompRate, String UnitId)
	{
		try {
				String RulesErrorList = "";
				
				if (!validPosition(positionNumber)){ 
					int ErrorMessageNumber = 0;
					RulesErrorList += ErrorMessages[ErrorMessageNumber];
					UpdateErrorCounts(ErrorMessageNumber, UnitId);
				}
				
				if (!validEmplid(emplId)) {
					int ErrorMessageNumber = 1;
					RulesErrorList += ErrorMessages[ErrorMessageNumber];
					UpdateErrorCounts(ErrorMessageNumber, UnitId);
				}
				
				if (!isSipEligible(positionNumber, emplId)){
					int ErrorMessageNumber = 2;
					RulesErrorList += ErrorMessages[ErrorMessageNumber];
					UpdateErrorCounts(ErrorMessageNumber, UnitId);
				}
						
				if (!validCompRate(positionNumber, emplId, CompRate)) {
					int ErrorMessageNumber = 3;
					RulesErrorList += ErrorMessages[ErrorMessageNumber];
					UpdateErrorCounts(ErrorMessageNumber, UnitId);
				}
			
				return RulesErrorList;
		}
		catch (Exception e) {
			LOG.error("validateSipRules Exceptiom: " + e.getMessage());
			return "validateSipRules Exceptiom: " + e.getMessage();
		}
	}
	
	// Coordinates validating the SIP business rules based on the values submitted for SIP.
	//  The rules being validated are provided below in comments before each validation.
	protected String validateSipValues(KualiDecimal IncToMin, KualiDecimal Merit, 
												KualiDecimal Equity, KualiDecimal Deferred,
												String Note, KualiDecimal CompRate,
												String UnitId)
	{
		try {
			String ValuesErrorList = "";
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
				if (ObjectUtils.isNotNull(Note))
				{
					ValuesErrorList += ErrorMessages[5];
					UpdateErrorCounts(5, UnitId);
				}
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
				
	//		4. SIP Awards are 'reasonable' (metric needed) : is sum of SIP awards between 0 and 5% of the Compensation Rate?
			if ( totalSIP.isGreaterThan(CompRate.multiply(new KualiDecimal(.05)))) {
				ValuesErrorList += WarningMessages[0];
				warningCount[0]++;
			}
	
			return ValuesErrorList;
		}
		catch (Exception e) {
			LOG.error("validateSipValues Exceptiom: " + e.getMessage());
			return "validateSipValues Exceptiom: " + e.getMessage();
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
					myErrCount.put(ErrorMessageNumber, 1);  			// Generate the error message and initialize its counter to 1.
					errorCountByUnitId.put(UnitId,myErrCount);
				}
				else
					// Adds 1 to an existing error count for this UnitId for this error message
					errorCountByUnitId.get(UnitId).put(ErrorMessageNumber, errorCountByUnitId.get(UnitId).get(ErrorMessageNumber)+1);	
			}
		}
		catch (Exception e) {
			LOG.error("UpdateErrorCounts Exceptiom: " + e.getMessage());
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
			errorReport.add(new ExternalizedMessageWrapper("\n\n\n"));
			int i;
			// Generate summary of each error type prefaced with a count.
			for ( i=0; i<=(ErrorMessages.length-1); i++ ){
				if (errorCount[i]!=0) {  // Only display the error if there is an error count > 0
					errorReport.add(new ExternalizedMessageWrapper((errorCount[i]==1 ? "There was " : "There were " ) + 
										errorCount[i] + (errorCount[i]==1 ? " occurrance of the error message:  " : " occurrances of the error message:  " ) + 
										ErrorMessages[i] + "\n"));
					TotalErrorCount += errorCount[i];
				}
			}
			
		    errorReport.add(new ExternalizedMessageWrapper("\n\n"));   // Add some blank lines 
		    errorReport.add(new ExternalizedMessageWrapper("===============   SIP ERROR SUMMARY - ERRORS BY C-LEVEL ORG   ======================="));
		    errorReport.add(new ExternalizedMessageWrapper("\n\n"));   // Add some blank lines
				
			// Generate summary of the counts for each UnitId (C Level org) for each error type, prefaced with a count.
		    Iterator it = errorCountByUnitId.entrySet().iterator(); 
		    while (it.hasNext()) { 
		        Map.Entry pairs = (Map.Entry)it.next(); 
		        String UnitId = (String) pairs.getKey();
		        errorReport.add(new ExternalizedMessageWrapper("" + UnitId + " Error Summary:\n"));
		        for (Map.Entry<Integer, Integer> myErrCount : errorCountByUnitId.get(UnitId).entrySet())
		        {
		        	errorReport.add(new ExternalizedMessageWrapper("\t" + myErrCount.getValue() + " - " + ErrorMessages[myErrCount.getKey()]));
		        }
		    } 
		    
		    errorReport.add(new ExternalizedMessageWrapper("\n\n"));   // Add some blank lines 
		    errorReport.add(new ExternalizedMessageWrapper("==============================   SIP ERROR DETAIL   ================================="));
		    errorReport.add(new ExternalizedMessageWrapper("\n\n"));   // Add some blank lines

		    return TotalErrorCount;

		}
		catch (Exception e) {
			LOG.error("SipImportErrorSummary Exceptiom: " + e.getMessage());
			errorReport.add(new ExternalizedMessageWrapper("SipImportErrorSummary Exceptiom: " + e.getMessage()));
			return TotalErrorCount;
		}
	}
}

