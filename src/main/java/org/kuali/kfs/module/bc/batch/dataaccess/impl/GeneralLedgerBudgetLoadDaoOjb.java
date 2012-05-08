/*
 * Copyright 2007 The Kuali Foundation
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
package org.kuali.kfs.module.bc.batch.dataaccess.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.apache.ojb.broker.query.ReportQueryByCriteria;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.SubFundGroup;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.Entry;
import org.kuali.kfs.module.bc.BCConstants;
import org.kuali.kfs.module.bc.BCParameterKeyConstants;
import org.kuali.kfs.module.bc.batch.dataaccess.GeneralLedgerBudgetLoadDao;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionHeader;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionMonthly;
import org.kuali.kfs.module.bc.businessobject.PendingBudgetConstructionGeneralLedger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.service.HomeOriginationService;
import org.kuali.rice.core.config.ConfigContext;
import org.kuali.rice.kew.util.KEWConstants;
import org.kuali.rice.kns.bo.Parameter;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.KualiInteger;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.SubFundProgram;
import edu.cornell.kfs.module.bc.CUBCConstants;

public class GeneralLedgerBudgetLoadDaoOjb extends BudgetConstructionBatchHelperDaoOjb implements GeneralLedgerBudgetLoadDao {

    /* turn on the logger for the persistence broker */
    private static Logger LOG = org.apache.log4j.Logger.getLogger(GeneralLedgerBudgetLoadDaoOjb.class);
    public final static String REPORT_FILE_NAME_PATTERN = "{0}/{1}_{2}{3}";

    private DateTimeService dateTimeService;
    private HomeOriginationService homeOriginationService;
    private ParameterService parameterService;
    private BusinessObjectService boService;
    private KualiConfigurationService kualiConfigurationService;
    private boolean tbRunFlag = false;
    private boolean productionFlag = false;
    

    private String budgetReportDirectory;
    private String budgetReportFilePrefix;
    private String reportFileName;

    /*
     * see GeneralLedgerBudgetLoadDao.LoadGeneralLedgerFromBudget
     */
    public void loadGeneralLedgerFromBudget(Integer fiscalYear) {
        //if we are calling this as a TB report the fiscalYear should be the currentFiscalYear + 1
        /**
         * this method calls a series of steps that load the general ledger from the budget into the general ledger pending entry
         * table. this method takes a fiscal year as input, but all that is required is that this object be a key labeling the
         * budget construction general ledger rows for the budget period to be loaded. it need not be an actual fiscal year.
         */
        //changes for KITI-2999

        
        PrintStream reportDataStream = this.getReportPrintStream();
        //First we need to determine if we are running this for Trustee Budget(TB) or regular Budget Construction (BC)
       
    	//first we need to check all params and echo if they are setup properly
    	Parameter tbRunFlagParameter = parameterService.retrieveParameter(BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, BCParameterKeyConstants.BUDGET_CONSTRUCTION_PARAM_DTL, BCParameterKeyConstants.BC_TRUSTEE_ONLY_BUDGET);
    	Parameter subFundsParameter = parameterService.retrieveParameter(BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, BCParameterKeyConstants.BUDGET_CONSTRUCTION_PARAM_DTL, BCParameterKeyConstants.BC_GL_SUB_FUNDS);
    	Parameter subFundProgramsParameter = parameterService.retrieveParameter(BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, BCParameterKeyConstants.BUDGET_CONSTRUCTION_PARAM_DTL, BCParameterKeyConstants.BC_GL_SUB_FUNDS_PROGRAM);
        Parameter glAcObjectsParameter = parameterService.retrieveParameter(BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, BCParameterKeyConstants.BUDGET_CONSTRUCTION_PARAM_DTL, BCParameterKeyConstants.BC_GL_AC_OBJECTS);
        productionFlag = isProduction();
        String productionSetting = ConfigContext.getCurrentContextConfig().getEnvironment();   
        boolean errorEncountered = printOutEnvironment(reportDataStream, tbRunFlagParameter, subFundsParameter, subFundProgramsParameter, glAcObjectsParameter, fiscalYear, productionSetting);
        
        
        if(tbRunFlagParameter.getParameterValue() != null) {
        	if(tbRunFlagParameter.getParameterValue().equals("Y")) {
        		tbRunFlag = true;
        	} else if(tbRunFlagParameter.getParameterValue().equals("N")) {
        		tbRunFlag = false;
        	}	
        }
        /**
         * initiliaze the counter variables
         */
        DiagnosticCounters diagnosticCounters = new DiagnosticCounters();

        boolean notProductionError = removeOldBudgetGeneralLedgerEntries(fiscalYear, diagnosticCounters);
        if(notProductionError) {
        	StringBuilder body = new StringBuilder();
        	body.append("\nERROR - Executing in production and prior budget data already exists.  Program terminating.");
        	reportDataStream.print(body);
        }
        errorEncountered |= notProductionError;
        if(errorEncountered) {
        	StringBuilder body = new StringBuilder();
        	DateFormat format = new SimpleDateFormat("yyyy/MM/dd @ HH:mm:ss");
        	java.util.Date now = new java.util.Date();
        	String date = format.format(now);
        	body.append("\nCornell BC > GL load job ending at " + date);
        	reportDataStream.print(body);
        	//we need to close the report and break out of the run
        	if (reportDataStream != null) {
                reportDataStream.flush();
                reportDataStream.close();
            }
        	return;
        }
        //
        // set up the global variables
        // this is a single object that can be passed to all methods that need it, to make the code "thread safe"
        // (1) the fiscal year to load
        // (2) the initial sequence numbers for each document to be loaded
        // (3) the run date (which will be the transaction date)
        // (4) the "origination code", which comes from the database
        DaoGlobalVariables daoGlobalVariables = new DaoGlobalVariables(fiscalYear);
        

        
        /**
         * make sure all the accounting periods for the load year are open, so the entry lines we create can be posted
         */
        openAllAccountingPeriods(fiscalYear);
        /**
         * process pending budget construction general ledger rows
         */
        loadPendingBudgetConstructionGeneralLedger(daoGlobalVariables, diagnosticCounters);
        /**
         * process budget construction monthly budget rows
         */
        if(!tbRunFlag) {
        	loadBudgetConstructionMonthlyBudget(daoGlobalVariables, diagnosticCounters);
        }
        
        // write out the counts for verification
        //diagnosticCounters.writeDiagnosticCounters();
        Parameter param = this.getParameterService().retrieveParameter(BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, BCParameterKeyConstants.BUDGET_CONSTRUCTION_PARAM_DTL, BCParameterKeyConstants.BC_TRUSTEE_ONLY_BUDGET);
        
        if(tbRunFlag) {
            param.setParameterValue("N");
            this.getBusinessObjectService().save(param);
        } else if(!tbRunFlag) {
        	param.setParameterValue("X");
            this.getBusinessObjectService().save(param);
        }
        writeReport(reportDataStream, diagnosticCounters);
        
        
    }

    private boolean printOutEnvironment(PrintStream reportDataStream, Parameter tbRunFlagParameter,
			Parameter subFundsParameter, Parameter subFundProgramsParameter,
			Parameter glAcObjectsParameter, Integer fiscalYear, String productionSetting) {
    	
    	boolean errorEncountered = false;
    	boolean warningEncountered = false;
    	StringBuilder body = new StringBuilder();
    	DateFormat format = new SimpleDateFormat("yyyy/MM/dd @ HH:mm:ss");
    	java.util.Date now = new java.util.Date();
    	String date = format.format(now);
    	body.append("\nCornell BC > GL load job beginning at " + date);
    	body.append(String.format("\n********************************************"));
    	body.append(String.format("\n\nBudget Construction Environment Variables\n"));
    	body.append(String.format("\n********************************************\n"));

    	body.append(String.format("\nINFO: Report being run for Fiscal Year %d\n", fiscalYear));
    	body.append(String.format("\nINFO: Report is being run on the %s system. Is running on production system: %b\n", productionSetting, productionFlag));
    	if(tbRunFlagParameter != null) {
    		if(tbRunFlagParameter.getParameterValue() == null) {
    			warningEncountered = true;
    			body.append(String.format("\nWARNING: %s is NULL, suggest using 'N' as value\n", tbRunFlagParameter.getParameterName()));
    		} else if(tbRunFlagParameter.getParameterValue().equals("Y") || tbRunFlagParameter.getParameterValue().equals("N")) {
    			body.append(String.format("\nINFO: %s %s %s \n", tbRunFlagParameter.getParameterName(), BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, tbRunFlagParameter.getParameterValue() ));
    		} else {
    			errorEncountered = true;
    			body.append(String.format("\nERROR: %s is %s, valid values are Y/N/Null\n", tbRunFlagParameter.getParameterName(), tbRunFlagParameter.getParameterValue()));
    		}
    	} else {
    		errorEncountered = true;
    		body.append("\nERROR: The BC_TRUSTEE_ONLY_BUDGET parameter was not found");
    	}
    	
    	if(subFundsParameter != null) {
    		if(subFundsParameter.getParameterValue() == null) {
    			warningEncountered = true;
    			body.append(String.format("\nWARNING: %s is NULL\n", subFundsParameter.getParameterName()));
    		} else if(StringUtils.isNotEmpty(subFundsParameter.getParameterValue())) {
    			body.append(String.format("\nINFO: %s %s %s \n", subFundsParameter.getParameterName(), BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, subFundsParameter.getParameterValue() ));
    		} else {
    			warningEncountered = true;
    			body.append(String.format("\nWARNING: %s is empty\n", tbRunFlagParameter.getParameterName()));
    		}
    		
    	} else {
    		errorEncountered = true;
    		body.append("\nERROR: The BC_GL_SUB_FUNDS parameter was not found");
    	}
    	
    	if(subFundProgramsParameter != null) {
    		if(subFundProgramsParameter.getParameterValue() == null) {
    			warningEncountered = true;
    			body.append(String.format("\nWARNING: %s is NULL\n", subFundProgramsParameter.getParameterName()));
    		} else if(StringUtils.isNotEmpty(subFundProgramsParameter.getParameterValue())) {
    			body.append(String.format("\nINFO: %s %s %s \n", subFundProgramsParameter.getParameterName(), BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, subFundProgramsParameter.getParameterValue() ));
    		} else {
    			warningEncountered = true;
    			body.append(String.format("\nWARNING: %s is empty\n", tbRunFlagParameter.getParameterName()));
    		}
    		
    	} else {
    		errorEncountered = true;
    		body.append("\nERROR: The BC_GL_SUB_FUNDS_PROGRAM parameter was not found");
    	}
    	
    	if(glAcObjectsParameter != null) {
    		if(glAcObjectsParameter.getParameterValue() == null) {
    			warningEncountered = true;
    			body.append(String.format("\nWARNING: %s is NULL\n", glAcObjectsParameter.getParameterName()));
    		} else if(StringUtils.isNotEmpty(glAcObjectsParameter.getParameterValue())) {
    			body.append(String.format("\nINFO: %s %s %s \n", glAcObjectsParameter.getParameterName(), BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, glAcObjectsParameter.getParameterValue() ));
    		} else {
    			warningEncountered = true;
    			body.append(String.format("\nWARNING: %s is empty\n", tbRunFlagParameter.getParameterName()));
    		}
    	} else {
    		errorEncountered = true;
    		body.append("\nERROR: The BC_GL_AC_OBJECTS parameter was not found");
    	}

    	if(errorEncountered) {
    		body.append("\n\nERROR conditions were encountered.  Processing terminated.  Please correct errors.");
    	} else if(warningEncountered) {
    		body.append("\n\nWARNINGs were found.  Please validate results and re-run if needed.");
    	} else {
    		body.append("\n\nNo ERROR conditions were encountered, so processing will begin.");
    	} 
    	body.append(String.format("\n********************************************\n"));
    	reportDataStream.print(body);
    	return errorEncountered;
	}

	/*******************************************************************************************************************************
     * methods to do the actual load *
     ******************************************************************************************************************************/

    private boolean removeOldBudgetGeneralLedgerEntries(Integer year, DiagnosticCounters diagnosticCounters) {
    	int deletedPendingEntries = 0;
    	int deletedEntries = 0;
    	int deletedBalanceEntries = 0;
    	
    	int deletedAcPendingEntries = 0;
    	int deletedAcEntries = 0;
    	int deletedAcBalanceEntries = 0;
    	
    	int deletedBbPendingEntries = 0;
    	int deletedBbEntries = 0;
    	int deletedBbBalanceEntries = 0;
    	
    	int deletedCbPendingEntries = 0;
    	int deletedCbEntries = 0;
    	int deletedCbBalanceEntries = 0;
    	
    	int deletedTbPendingEntries = 0;
    	int deletedTbEntries = 0;
    	int deletedTbBalanceEntries = 0;
		if(!tbRunFlag) {
			List<String> nonTbCollection = Arrays.asList("CB", "AC", "BB");
			
			getPersistenceBrokerTemplate().clearCache();
			//CB Pending first
			Criteria pendingEntryDeletion = new Criteria();
			pendingEntryDeletion.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
			pendingEntryDeletion.addEqualTo(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, "CB");
			QueryByCriteria pendingEntryQuery = new QueryByCriteria(GeneralLedgerPendingEntry.class, pendingEntryDeletion);
			deletedCbPendingEntries = getPersistenceBrokerTemplate().getCount(pendingEntryQuery);
			if(deletedCbPendingEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
				diagnosticCounters.setGeneralLedgerCbPendingEntriesDeleted(deletedCbPendingEntries);
			}
			
			//BB entries
			pendingEntryDeletion = new Criteria();
			pendingEntryDeletion.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
			pendingEntryDeletion.addEqualTo(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, "BB");
			pendingEntryQuery = new QueryByCriteria(GeneralLedgerPendingEntry.class, pendingEntryDeletion);
			deletedBbPendingEntries = getPersistenceBrokerTemplate().getCount(pendingEntryQuery);
			if(deletedBbPendingEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
				diagnosticCounters.setGeneralLedgerBbPendingEntriesDeleted(deletedBbPendingEntries);
			}
			
			//AC entries
			pendingEntryDeletion = new Criteria();
			pendingEntryDeletion.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
			pendingEntryDeletion.addEqualTo(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, "AC");
			pendingEntryQuery = new QueryByCriteria(GeneralLedgerPendingEntry.class, pendingEntryDeletion);
			deletedAcPendingEntries = getPersistenceBrokerTemplate().getCount(pendingEntryQuery);
			if(deletedAcPendingEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
				diagnosticCounters.setGeneralLedgerAcPendingEntriesDeleted(deletedAcPendingEntries);
			}
			//Now the same for entries
			//CB Pending first
			Criteria entryDeletion = new Criteria();
			entryDeletion.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
			entryDeletion.addEqualTo(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, "CB");
			QueryByCriteria entryQuery = new QueryByCriteria(Entry.class, entryDeletion);
			deletedCbEntries = getPersistenceBrokerTemplate().getCount(entryQuery);
			if(deletedCbEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
				diagnosticCounters.setGeneralLedgerCbEntriesDeleted(deletedCbEntries);
			}
			
			//BB entries
			entryDeletion = new Criteria();
			entryDeletion.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
			entryDeletion.addEqualTo(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, "BB");
			entryQuery = new QueryByCriteria(Entry.class, entryDeletion);
			deletedBbEntries = getPersistenceBrokerTemplate().getCount(entryQuery);
			if(deletedBbEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
				diagnosticCounters.setGeneralLedgerBbEntriesDeleted(deletedBbEntries);
			}
			//AC entries
			entryDeletion = new Criteria();
			entryDeletion.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
			entryDeletion.addEqualTo(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, "AC");
			entryQuery = new QueryByCriteria(Entry.class, entryDeletion);
			deletedAcEntries = getPersistenceBrokerTemplate().getCount(entryQuery);
			if(deletedAcEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
				diagnosticCounters.setGeneralLedgerAcEntriesDeleted(deletedAcEntries);
			}
	        
			//Now the same for balances
			//CB Pending first
			Criteria balanceDeletion = new Criteria();
			balanceDeletion.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
			balanceDeletion.addEqualTo(KFSPropertyConstants.BALANCE_TYPE_CODE, "CB");
			QueryByCriteria balanceQuery = new QueryByCriteria(Balance.class, balanceDeletion);
			deletedCbBalanceEntries = getPersistenceBrokerTemplate().getCount(balanceQuery);
			if(deletedCbBalanceEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
				diagnosticCounters.setGeneralLedgerCbBalanceEntriesDeleted(deletedCbBalanceEntries);
			}
			//BB entries
			balanceDeletion = new Criteria();
			balanceDeletion.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
			balanceDeletion.addEqualTo(KFSPropertyConstants.BALANCE_TYPE_CODE, "BB");
			balanceQuery = new QueryByCriteria(Balance.class, balanceDeletion);
			deletedBbBalanceEntries = getPersistenceBrokerTemplate().getCount(balanceQuery);
			if(deletedBbBalanceEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
				diagnosticCounters.setGeneralLedgerBbBalanceEntriesDeleted(deletedBbBalanceEntries);
			}
			//AC entries
			balanceDeletion = new Criteria();
			balanceDeletion.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
			balanceDeletion.addEqualTo(KFSPropertyConstants.BALANCE_TYPE_CODE, "AC");
			balanceQuery = new QueryByCriteria(Balance.class, balanceDeletion);
			deletedAcBalanceEntries = getPersistenceBrokerTemplate().getCount(balanceQuery);
			if(deletedAcBalanceEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
				diagnosticCounters.setGeneralLedgerAcBalanceEntriesDeleted(deletedAcBalanceEntries);
			}
			
			if(!productionFlag) {
				Criteria pendingEntryCriteria = new Criteria();
		        pendingEntryCriteria.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
		        pendingEntryCriteria.addIn(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, nonTbCollection);
		        QueryByCriteria deletePendingQry = new QueryByCriteria(GeneralLedgerPendingEntry.class, pendingEntryCriteria);
		        deletedPendingEntries = getPersistenceBrokerTemplate().getCount(deletePendingQry);
		        diagnosticCounters.setGeneralLedgerPendingEntriesDeleted(deletedPendingEntries);
		        getPersistenceBrokerTemplate().deleteByQuery(deletePendingQry);
		        
		        Criteria glEntryCriteria = new Criteria();
		        glEntryCriteria.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
		        glEntryCriteria.addIn(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, nonTbCollection);
		        QueryByCriteria deleteEntryQry = new QueryByCriteria(Entry.class, glEntryCriteria);
		        deletedEntries = getPersistenceBrokerTemplate().getCount(deleteEntryQry);
		        diagnosticCounters.setGeneralLedgerEntriesDeleted(deletedEntries);
		        getPersistenceBrokerTemplate().deleteByQuery(deleteEntryQry);
		        
		        Criteria glBalanceCriteria = new Criteria();
		        glBalanceCriteria.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
		        glBalanceCriteria.addIn(KFSPropertyConstants.BALANCE_TYPE_CODE, nonTbCollection);
		        QueryByCriteria deleteBalanceQry = new QueryByCriteria(Balance.class, glBalanceCriteria);
		        deletedBalanceEntries = getPersistenceBrokerTemplate().getCount(deleteBalanceQry);
		        diagnosticCounters.setGeneralLedgerBalanceEntriesDeleted(deletedBalanceEntries);
		        getPersistenceBrokerTemplate().deleteByQuery(deleteBalanceQry);	
			}
	        
	        
	        getPersistenceBrokerTemplate().clearCache();
	        return false;
			
		} else if(tbRunFlag) {
			getPersistenceBrokerTemplate().clearCache();
	        Criteria pendingEntryCriteria = new Criteria();
	        pendingEntryCriteria.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
	        pendingEntryCriteria.addEqualTo(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, "TB");
	        QueryByCriteria deletePendingQry = new QueryByCriteria(GeneralLedgerPendingEntry.class, pendingEntryCriteria);

	        deletedPendingEntries = getPersistenceBrokerTemplate().getCount(deletePendingQry);
	        if(deletedPendingEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
		        diagnosticCounters.setGeneralLedgerTbPendingEntriesDeleted(deletedPendingEntries);
		        diagnosticCounters.setGeneralLedgerPendingEntriesDeleted(deletedPendingEntries);
		        getPersistenceBrokerTemplate().deleteByQuery(deletePendingQry);
			}
	        
	        Criteria glEntryCriteria = new Criteria();
	        glEntryCriteria.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
	        glEntryCriteria.addEqualTo(KFSPropertyConstants.FINANCIAL_BALANCE_TYPE_CODE, "TB");
	        QueryByCriteria deleteEntryQry = new QueryByCriteria(Entry.class, glEntryCriteria);
	        deletedEntries = getPersistenceBrokerTemplate().getCount(deleteEntryQry);
	        if(deletedEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
		        diagnosticCounters.setGeneralLedgerTbEntriesDeleted(deletedEntries);
		        diagnosticCounters.setGeneralLedgerEntriesDeleted(deletedEntries);
		        getPersistenceBrokerTemplate().deleteByQuery(deleteEntryQry);
			}
	        
	        Criteria glBalanceCriteria = new Criteria();
	        glBalanceCriteria.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, year);
	        glBalanceCriteria.addEqualTo(KFSPropertyConstants.BALANCE_TYPE_CODE, "TB");
	        QueryByCriteria deleteBalanceQry = new QueryByCriteria(Balance.class, glBalanceCriteria);
	        deletedBalanceEntries = getPersistenceBrokerTemplate().getCount(deleteBalanceQry);
	        if(deletedBalanceEntries > 0 && productionFlag) {
				return true;
			} else if(!productionFlag) {
		        diagnosticCounters.setGeneralLedgerTbBalanceEntriesDeleted(deletedBalanceEntries);
		        diagnosticCounters.setGeneralLedgerBalanceEntriesDeleted(deletedBalanceEntries);
		        getPersistenceBrokerTemplate().deleteByQuery(deleteBalanceQry);
			}
	        
	        getPersistenceBrokerTemplate().clearCache();
	        return false;
		}
		return false;
		
	}

	/**
     * build a hashmap containing the next entry sequence number to use for each document (document number) to be loaded from budget
     * construction to the general ledger
	 * @param financialSystemOriginationCode 
     * 
     * @param target fiscal year for the budget load
     * @return HashMapap keyed on document number containing the next entry sequence number to use for the key
     */

    protected HashMap<String, Integer> entrySequenceNumber(Integer requestYear, String financialSystemOriginationCode) {
        HashMap<String, Integer> nextEntrySequenceNumber;
        // set up the query: each distinct document number in the source load table
        Criteria criteriaID = new Criteria();
        criteriaID.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, requestYear);
        ReportQueryByCriteria queryID = new ReportQueryByCriteria(BudgetConstructionHeader.class, criteriaID);
        queryID.setAttributes(new String[] { KFSPropertyConstants.DOCUMENT_NUMBER });

        nextEntrySequenceNumber = new HashMap<String, Integer>(hashCapacity(queryID));

        
       
        // OK. build the hash map
        // there are NO entries for these documents yet, so we initialize the entry sequence number to 0
        Iterator documentNumbersToLoad = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(queryID);
        while (documentNumbersToLoad.hasNext()) {
            Object[] resultRow = (Object[]) documentNumbersToLoad.next();
            //first we need to see what the last sequence number used was for this document number
            Criteria sequenceCriteriaID = new Criteria();
            sequenceCriteriaID.addEqualTo("financialSystemOriginationCode", financialSystemOriginationCode);
            sequenceCriteriaID.addEqualTo("documentNumber", resultRow[0]);
            
            ReportQueryByCriteria sequenceQueryID = new ReportQueryByCriteria(GeneralLedgerPendingEntry.class, sequenceCriteriaID);
            sequenceQueryID.setAttributes(new String[] { "max(transactionLedgerEntrySequenceNumber)" });
            Iterator maxSequenceIterator = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(sequenceQueryID);
            Object[] maxRow = (Object[])maxSequenceIterator.next();
            Integer maxSequence = 0;
            if(ObjectUtils.isNotNull(maxRow)) {
            	maxSequence = ((BigDecimal)maxRow[0]).intValue();
            }
            nextEntrySequenceNumber.put((String) resultRow[0], new Integer(maxSequence + 1));
        }

        return nextEntrySequenceNumber;
    }

    /**
     * This method creates a new generalLedgerPendingEntry object and initializes it with the default settings for the budget
     * construction general ledger load.
     * 
     * @param requestYear
     * @param financialSystemOriginationCode
     * @return intiliazed GeneralLedgerPendingEntry business object
     */

    protected GeneralLedgerPendingEntry getNewPendingEntryWithDefaults(DaoGlobalVariables daoGlobalVariables) {
        GeneralLedgerPendingEntry newRow = new GeneralLedgerPendingEntry();
        newRow.setUniversityFiscalYear(daoGlobalVariables.getRequestYear());
        newRow.setTransactionLedgerEntryDescription(BCConstants.BC_TRN_LDGR_ENTR_DESC);
        newRow.setFinancialDocumentTypeCode(CUBCConstants.BUDGET_CONSTRUCTION_DOCUMENT_TYPE);
        newRow.setFinancialDocumentApprovedCode(KFSConstants.PENDING_ENTRY_APPROVED_STATUS_CODE.APPROVED);
        newRow.setTransactionDate(daoGlobalVariables.getTransactionDate());
        newRow.setTransactionEntryOffsetIndicator(false);
        newRow.setFinancialSystemOriginationCode(daoGlobalVariables.getFinancialSystemOriginationcode());
        // the fields below are set to null
        newRow.setOrganizationDocumentNumber(null);
        newRow.setProjectCode(null);
        newRow.setOrganizationReferenceId(null);
        newRow.setReferenceFinancialDocumentTypeCode(null);
        newRow.setReferenceOriginationCode(null);
        newRow.setReferenceFinancialDocumentNumber(null);
        newRow.setFinancialDocumentReversalDate(null);
        newRow.setTransactionEncumbranceUpdateCode(null);
        newRow.setAcctSufficientFundsFinObjCd(null);
        newRow.setTransactionDebitCreditCode(null);
        newRow.setTransactionEntryProcessedTs(null);
        return newRow;
    }

    protected void loadBudgetConstructionMonthlyBudget(DaoGlobalVariables daoGlobalVariables, DiagnosticCounters diagnosticCounters) {
        QueryByCriteria queryID = queryForBudgetConstructionMonthly(daoGlobalVariables.getRequestYear());
        Iterator<BudgetConstructionMonthly> monthlyBudgetRows = getPersistenceBrokerTemplate().getIteratorByQuery(queryID);
        while (monthlyBudgetRows.hasNext()) {
            BudgetConstructionMonthly monthlyBudgetIn = monthlyBudgetRows.next();
            diagnosticCounters.increaseBudgetConstructionMonthlyBudgetRead();
            if (daoGlobalVariables.shouldThisAccountLoad(monthlyBudgetIn.getAccountNumber() + monthlyBudgetIn.getChartOfAccountsCode())) {
                GeneralLedgerPendingEntry newRow = getNewPendingEntryWithDefaults(daoGlobalVariables);
                writeGeneralLedgerPendingEntryFromMonthly(newRow, monthlyBudgetIn, daoGlobalVariables, diagnosticCounters);
            }
            else {
                diagnosticCounters.increaseBudgetConstructionMonthlyBudgetSkipped();
            }
        }
    }

    /**
     * This method loads all the eligible pending budget construction general ledger rows
     * 
     * @param daoGlobalVariables
     * @param diagnosticCounters
     */
    protected void loadPendingBudgetConstructionGeneralLedger(DaoGlobalVariables daoGlobalVariables, DiagnosticCounters diagnosticCounters) {
        QueryByCriteria queryID = queryForPendingBudgetConstructionGeneralLedger(daoGlobalVariables.getRequestYear());
        Iterator<PendingBudgetConstructionGeneralLedger> pbglRows = getPersistenceBrokerTemplate().getIteratorByQuery(queryID);
        while (pbglRows.hasNext()) {
            PendingBudgetConstructionGeneralLedger pbglIn = pbglRows.next();
            diagnosticCounters.increaseBudgetConstructionPendingGeneralLedgerRead();
            if (daoGlobalVariables.shouldThisAccountLoad(pbglIn.getAccountNumber() + pbglIn.getChartOfAccountsCode())) {
                GeneralLedgerPendingEntry newRow = getNewPendingEntryWithDefaults(daoGlobalVariables);
                writeGeneralLedgerPendingEntryFromAnnual(newRow, pbglIn, daoGlobalVariables, diagnosticCounters);
            }
            else {
                diagnosticCounters.increaseBudgetConstructionPendingGeneralLedgerSkipped();
            }
        }
    }

    /**
     * This method builds the query to fetch the monthly budget general ledger lines to be loaded
     * 
     * @param fiscalYear : the year to be loaded
     * @return query for fetching monthly budget rows
     */
    protected QueryByCriteria queryForBudgetConstructionMonthly(Integer fiscalYear) {
        // we only select rows which have non-zero budget amounts
        // on this object, proxy=true, so we can do a regular query for a business object instead of a report query
        Criteria criteriaID = new Criteria();
        criteriaID.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear);
        // we want to test for at least one non-zero monthly amount
        Criteria orCriteriaID = new Criteria();
        Iterator<String[]> monthlyPeriods = BCConstants.BC_MONTHLY_AMOUNTS.iterator();
        while (monthlyPeriods.hasNext()) {
            // the first array element is the amount field name (the second is the corresponding accounting period)
            String monthlyAmountName = monthlyPeriods.next()[0];
            Criteria amountCriteria = new Criteria();
            amountCriteria.addNotEqualTo(monthlyAmountName, new KualiInteger(0));
            orCriteriaID.addOrCriteria(amountCriteria);
        }
        criteriaID.addAndCriteria(orCriteriaID);
        QueryByCriteria queryID = new QueryByCriteria(BudgetConstructionMonthly.class, criteriaID);
        return queryID;
    }

    /**
     * This method builds the query to fetch the pending budget construction general ledger rows to be loaded
     * 
     * @param fiscalYear: the year to be loaded
     * @return query for fetching pending budget construction GL rows
     */

    protected QueryByCriteria queryForPendingBudgetConstructionGeneralLedger(Integer fiscalYear) {
        // we only select rows which have non-zero budget amounts
        // on this object, proxy=true, so we can do a regular query for a business object instead of a report query
        Criteria criteriaID = new Criteria();
        criteriaID.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, fiscalYear);
        criteriaID.addNotEqualTo(KFSPropertyConstants.ACCOUNT_LINE_ANNUAL_BALANCE_AMOUNT, new KualiInteger(0));
        QueryByCriteria queryID = new QueryByCriteria(PendingBudgetConstructionGeneralLedger.class, criteriaID);
        return queryID;
    }

    /**
     * complete the pending entry row based on the data returned from the DB store it to the DB
     * 
     * @param newRow
     * @param source annual budget construction GL row
     * @param object containing global constants
     */
    protected void writeGeneralLedgerPendingEntryFromAnnual(GeneralLedgerPendingEntry newRow, PendingBudgetConstructionGeneralLedger pbgl, DaoGlobalVariables daoGlobalVariables, DiagnosticCounters diagnosticCounters) {
        /**
         * first get the document number
         */
        String incomingDocumentNumber = pbgl.getDocumentNumber();
        
        
        if(!tbRunFlag) {
            /**
             * write a base budget row
             */
            newRow.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_BASE_BUDGET);
            newRow.setUniversityFiscalPeriodCode(KFSConstants.PERIOD_CODE_BEGINNING_BALANCE);
            /**
             * set the variable fields
             */
            newRow.setTransactionLedgerEntrySequenceNumber(daoGlobalVariables.getNextSequenceNumber(incomingDocumentNumber));
            newRow.setDocumentNumber(incomingDocumentNumber); // document number
            newRow.setChartOfAccountsCode(pbgl.getChartOfAccountsCode()); // chart of accounts
            newRow.setAccountNumber(pbgl.getAccountNumber()); // account number
            newRow.setSubAccountNumber(pbgl.getSubAccountNumber()); // sub account number
            newRow.setFinancialObjectCode(pbgl.getFinancialObjectCode()); // object code
            newRow.setFinancialSubObjectCode(pbgl.getFinancialSubObjectCode()); // sub object code
            newRow.setFinancialObjectTypeCode(pbgl.getFinancialObjectTypeCode()); // object type code
            /**
             * the budget works with whole numbers--we must convert to decimal for the general ledger
             */
            newRow.setTransactionLedgerEntryAmount(pbgl.getAccountLineAnnualBalanceAmount().kualiDecimalValue());
            /**
             * now we store the base budget value
             */
            getPersistenceBrokerTemplate().store(newRow);
            diagnosticCounters.increaseGeneralLedgerBaseBudgetWritten();
            if(diagnosticCounters.generalLedgerBaseBudgetWritten % 100 == 0) {
            	String[] bbEntry = {newRow.getUniversityFiscalYear().toString(), newRow.getAccountNumber(), 
                		newRow.getFinancialObjectCode(), newRow.getFinancialBalanceTypeCode(), newRow.getUniversityFiscalPeriodCode(),
                		newRow.getTransactionDate().toString(), newRow.getTransactionLedgerEntryDescription(), 
                		newRow.getTransactionLedgerEntryAmount().toString(), newRow.getTransactionDebitCreditCode()};
                diagnosticCounters.addToBbCreated(bbEntry);
            }
            /**
             * the same row needs to be written as a current budget item we change only the balance type and the sequence number
             */
            newRow.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_CURRENT_BUDGET);
            newRow.setTransactionLedgerEntrySequenceNumber(daoGlobalVariables.getNextSequenceNumber(incomingDocumentNumber));
            
            /**
             * store the current budget value
             */
            getPersistenceBrokerTemplate().store(newRow);
            diagnosticCounters.increaseGeneralLedgerCurrentBudgetWritten();
            if(diagnosticCounters.generalLedgerCurrentBudgetWritten % 100 == 0) {
            	String[] cbEntry = {newRow.getUniversityFiscalYear().toString(), newRow.getAccountNumber(), 
                		newRow.getFinancialObjectCode(), newRow.getFinancialBalanceTypeCode(), newRow.getUniversityFiscalPeriodCode(),
                		newRow.getTransactionDate().toString(), newRow.getTransactionLedgerEntryDescription(), 
                		newRow.getTransactionLedgerEntryAmount().toString(), newRow.getTransactionDebitCreditCode()};
                diagnosticCounters.addToCbCreated(cbEntry);
            }
            
            /**
             * the same row needs to be written as a actual budget item we change only the balance type and the sequence number
             */
            newRow.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_ACTUAL);
            newRow.setTransactionLedgerEntrySequenceNumber(daoGlobalVariables.getNextSequenceNumber(incomingDocumentNumber));
            
            /**
             * store the current budget value
             */
            getPersistenceBrokerTemplate().store(newRow);
            diagnosticCounters.increaseGeneralLedgerActualBudgetWritten();
            if(diagnosticCounters.generalLedgerActualBudgetWritten % 100 == 0) {
            	String[] acEntry = {newRow.getUniversityFiscalYear().toString(), newRow.getAccountNumber(), 
                		newRow.getFinancialObjectCode(), newRow.getFinancialBalanceTypeCode(), newRow.getUniversityFiscalPeriodCode(),
                		newRow.getTransactionDate().toString(), newRow.getTransactionLedgerEntryDescription(), 
                		newRow.getTransactionLedgerEntryAmount().toString(), newRow.getTransactionDebitCreditCode()};
                diagnosticCounters.addToAcCreated(acEntry);
            }
            
        } else {
            /**
             * write a trustee budget row
             */
        	newRow.setTransactionLedgerEntryDescription(CUBCConstants.TB_TRN_LDGR_ENTR_DESC);
            newRow.setFinancialBalanceTypeCode(CUBCConstants.BALANCE_TYPE_TRUSTEES_BUDGET);
            newRow.setUniversityFiscalPeriodCode(KFSConstants.PERIOD_CODE_BEGINNING_BALANCE);
            /**
             * set the variable fields
             */
            newRow.setTransactionLedgerEntrySequenceNumber(daoGlobalVariables.getNextSequenceNumber(incomingDocumentNumber));
            newRow.setDocumentNumber(incomingDocumentNumber); // document number
            newRow.setChartOfAccountsCode(pbgl.getChartOfAccountsCode()); // chart of accounts
            newRow.setAccountNumber(pbgl.getAccountNumber()); // account number
            newRow.setSubAccountNumber(pbgl.getSubAccountNumber()); // sub account number
            newRow.setFinancialObjectCode(pbgl.getFinancialObjectCode()); // object code
            newRow.setFinancialSubObjectCode(pbgl.getFinancialSubObjectCode()); // sub object code
            newRow.setFinancialObjectTypeCode(pbgl.getFinancialObjectTypeCode()); // object type code
            /**
             * the budget works with whole numbers--we must convert to decimal for the general ledger
             */
            newRow.setTransactionLedgerEntryAmount(pbgl.getAccountLineAnnualBalanceAmount().kualiDecimalValue());
            /**
             * now we store the base budget value
             */
            getPersistenceBrokerTemplate().store(newRow);
            diagnosticCounters.increaseGeneralLedgerTrusteesBudgetWritten();
            if(diagnosticCounters.generalLedgerTrusteesBudgetWritten % 100 == 0) {
            	String[] tbEntry = {newRow.getUniversityFiscalYear().toString(), newRow.getAccountNumber(), 
                		newRow.getFinancialObjectCode(), newRow.getFinancialBalanceTypeCode(), newRow.getUniversityFiscalPeriodCode(),
                		newRow.getTransactionDate().toString(), newRow.getTransactionLedgerEntryDescription(), 
                		newRow.getTransactionLedgerEntryAmount().toString(), newRow.getTransactionDebitCreditCode()};
                diagnosticCounters.addToTbCreated(tbEntry);
            }
        }
        
        
    }

    protected void writeGeneralLedgerPendingEntryFromMonthly(GeneralLedgerPendingEntry newRow, BudgetConstructionMonthly pbglMonthly, DaoGlobalVariables daoGlobalVariables, DiagnosticCounters diagnosticCounters) {
        /**
         * first get the document number
         */
        String incomingDocumentNumber = pbglMonthly.getDocumentNumber();
        /**
         * write a base budget row
         */
        newRow.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_MONTHLY_BUDGET);
        /**
         * set the variable fields
         */
        newRow.setDocumentNumber(incomingDocumentNumber); // document number
        newRow.setChartOfAccountsCode(pbglMonthly.getChartOfAccountsCode()); // chart of accounts
        newRow.setAccountNumber(pbglMonthly.getAccountNumber()); // account number
        newRow.setSubAccountNumber(pbglMonthly.getSubAccountNumber()); // sub account number
        newRow.setFinancialObjectCode(pbglMonthly.getFinancialObjectCode()); // object code
        newRow.setFinancialSubObjectCode(pbglMonthly.getFinancialSubObjectCode()); // sub object code
        newRow.setFinancialObjectTypeCode(pbglMonthly.getFinancialObjectTypeCode()); // object type code

        /**
         * we have to loop through the monthly array, and write an MB row for each monthly row with a non-zero amount (we do this to
         * write less code. we hope that the extra hit from reflection won't be too bad)
         */
        Iterator<String[]> monthlyPeriodAmounts = BCConstants.BC_MONTHLY_AMOUNTS.iterator();
        while (monthlyPeriodAmounts.hasNext()) {
            String[] monthlyPeriodProperties = monthlyPeriodAmounts.next();
            KualiInteger monthlyAmount;
            try {
                monthlyAmount = (KualiInteger) PropertyUtils.getSimpleProperty(pbglMonthly, monthlyPeriodProperties[0]);
            }
            catch (IllegalAccessException ex) {
                LOG.error(String.format("\nunable to use get method to access value of %s in %s\n", monthlyPeriodProperties[0], BudgetConstructionMonthly.class.getName()), ex);
                diagnosticCounters.writeDiagnosticCounters();
                throw new RuntimeException(ex);
            }
            catch (InvocationTargetException ex) {
                LOG.error(String.format("\nunable to invoke get method for %s in %s\n", monthlyPeriodProperties[0], BudgetConstructionMonthly.class.getName()), ex);
                diagnosticCounters.writeDiagnosticCounters();
                throw new RuntimeException(ex);
            }
            catch (NoSuchMethodException ex) {
                LOG.error(String.format("\nNO get method found for %s in %s ???\n", monthlyPeriodProperties[0], BudgetConstructionMonthly.class.getName()), ex);
                diagnosticCounters.writeDiagnosticCounters();
                throw new RuntimeException(ex);
            }
            if (!(monthlyAmount.isZero())) {
                newRow.setTransactionLedgerEntrySequenceNumber(daoGlobalVariables.getNextSequenceNumber(incomingDocumentNumber));
                newRow.setUniversityFiscalPeriodCode(monthlyPeriodProperties[1]); // accounting period
                newRow.setTransactionLedgerEntryAmount(monthlyAmount.kualiDecimalValue()); // amount
                getPersistenceBrokerTemplate().store(newRow);
                diagnosticCounters.increaseBudgetConstructionMonthlyBudgetWritten();
                if(diagnosticCounters.budgetConstructionMonthlyBudgetWritten % 100 == 0) {
                	String[] monthlyEntry = {newRow.getUniversityFiscalYear().toString(), newRow.getAccountNumber(), 
                    		newRow.getFinancialObjectCode(), newRow.getFinancialBalanceTypeCode(), newRow.getUniversityFiscalPeriodCode(),
                    		newRow.getTransactionDate().toString(), newRow.getTransactionLedgerEntryDescription(), 
                    		newRow.getTransactionLedgerEntryAmount().toString(), newRow.getTransactionDebitCreditCode()};
                    diagnosticCounters.addToMonthlyCreated(monthlyEntry);
                }
            }
        }
    }


    /*******************************************************************************************************************************
     * * This section build the list of accounts that SHOULD NOT be loaded to the general ledger * (This may seem strange--why build
     * a budget if you aren't going to load it--but in the FIS the budget * loaded to payroll as well. For grant accounts, the FIS
     * allowed people to set salaries for the new year * so those would load to payroll. But, the actual budget for a grant account
     * was not necessarily done on a * fiscal year basis, and was not part of the university's operating budget, so there was no
     * "base budget" * for a grant account to load to the general ledger.) * (1) We will inhibit the load to the general ledger of
     * all accounts in given sub fund groups * (2) (We WILL allow closed accounts to load. There should not be any--they should have
     * been filtered * out in the budget application, but if there are, they will be caught by the GL scrubber. We want * people to
     * have a record of this kind of load failure, so it can be corrected. * * *
     ******************************************************************************************************************************/

    /**
     * get a list of accounts that should not be loaded from the budget to the General Ledger
     * 
     * @return hashset of accounts NOT to be loaded
     */

    protected HashSet<String> getAccountsNotToBeLoaded() {
        HashSet<String> bannedAccounts;
        /**
         * list of subfunds which should not be loaded
         */
        HashSet<String> bannedSubFunds = getSubFundsNotToBeLoaded();
        HashSet<String> bannedSubFundPrograms = getSubFundsProgramsNotToBeLoaded();
        Iterator<String> additionalBannedSubFundPrograms = bannedSubFundPrograms.iterator();
        while (additionalBannedSubFundPrograms.hasNext()) {
            bannedSubFunds.add(additionalBannedSubFundPrograms.next());
        }
        
        /**
         * query for the subfund property for each account in the DB
         */
        ReportQueryByCriteria queryID = new ReportQueryByCriteria(Account.class, org.apache.ojb.broker.query.ReportQueryByCriteria.CRITERIA_SELECT_ALL);
        queryID.setAttributes(new String[] { KFSPropertyConstants.ACCOUNT_NUMBER, KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, KFSPropertyConstants.SUB_FUND_GROUP_CODE });
        bannedAccounts = new HashSet<String>(hashCapacity(queryID));
        /**
         * use the results to build a hash set of accounts which should NOT be loaded (that is, their subfunds are in the list of
         * subfunds we do not want
         */
        Iterator accountProperties = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(queryID);
        while (accountProperties.hasNext()) {
            Object[] selectListValues = (Object[]) accountProperties.next();
            /**
             * we will add an account/chart to the list if it has a no-load subfundgroup
             */
            if (bannedSubFunds.contains((String) selectListValues[2])) {
                /**
                 * hash content is account number concatenated with chart (the key of the chart of accounts table)
                 */
                bannedAccounts.add(((String) selectListValues[0]) + ((String) selectListValues[1]));
            }
        }
        return bannedAccounts;
    }

    /**
     * build a hash set of subfunds whose accounts should NOT be loaded this can be done by either a list of FUND groups and/or a
     * list of subfund groups
     * 
     * @see org.kuali.kfs.module.bc.BCConstants to initialize the String[] array(s) as desired
     * @return list of subfunds whose accounts will NOT be loaded
     */
    protected HashSet<String> getSubFundsNotToBeLoaded() {
        List<String> subFunds = this.parameterService.getParameterValues(BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, BCParameterKeyConstants.BUDGET_CONSTRUCTION_PARAM_DTL, BCParameterKeyConstants.BC_GL_SUB_FUNDS);
        HashSet<String> bannedSubFunds;
        if(!tbRunFlag) {
            if (subFunds.size() !=0) {
                /**
                 * look for subfunds in the banned fund groups
                 */
                Criteria criteriaID = new Criteria();
                criteriaID.addIn(KFSPropertyConstants.FUND_GROUP_CODE, subFunds);
                ReportQueryByCriteria queryID = new ReportQueryByCriteria(SubFundGroup.class, criteriaID);
                queryID.setAttributes(new String[] { KFSPropertyConstants.SUB_FUND_GROUP_CODE });
                /**
                 * set the size of the hashset based on the number of rows the query will return
                 */
                bannedSubFunds = new HashSet<String>(hashCapacity(queryID) + subFunds.size());
                Iterator subfundsForBannedFunds = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(queryID);
                /**
                 * add the subfunds for the fund groups to be skipped to the hash set
                 */
                while (subfundsForBannedFunds.hasNext()) {
                    bannedSubFunds.add((String) ((Object[]) subfundsForBannedFunds.next())[0]);
                }
            }
            else {
            	bannedSubFunds = new HashSet<String>(BCConstants.NO_BC_GL_LOAD_SUBFUND_GROUPS.size() + 1);
            }   
            /**
             * now add the specific sub funds we don't want from the hard-coded array in BCConstants to the hash set
             */
            Iterator<String> additionalBannedSubFunds = subFunds.iterator();
            while (additionalBannedSubFunds.hasNext()) {
                bannedSubFunds.add(additionalBannedSubFunds.next());
            }
        } else {
        	bannedSubFunds = new HashSet<String>();
        }
        
        return bannedSubFunds;
    }
    
    /**
     * build a hash set of subfunds whose accounts should NOT be loaded this can be done by either a list of FUND groups and/or a
     * list of subfund groups
     * 
     * @see org.kuali.kfs.module.bc.BCConstants to initialize the String[] array(s) as desired
     * @return list of subfunds whose accounts will NOT be loaded
     */
    protected HashSet<String> getSubFundsProgramsNotToBeLoaded() {
        HashSet<String> bannedSubFunds;
        List<String> subFundsProgram = this.parameterService.getParameterValues(BCConstants.BUDGET_CONSTRUCTION_NAMESPACE, BCParameterKeyConstants.BUDGET_CONSTRUCTION_PARAM_DTL, BCParameterKeyConstants.BC_GL_SUB_FUNDS_PROGRAM);
        if(!tbRunFlag) {
            //KITI-2999 we also need to add to this list 
            /**
             * look for subfunds in the banned fund programs
             */
            Criteria criteriaID = new Criteria();
            criteriaID.addIn("programCode", subFundsProgram);
            ReportQueryByCriteria queryID = new ReportQueryByCriteria(SubFundProgram.class, criteriaID);
            queryID.setAttributes(new String[] { KFSPropertyConstants.SUB_FUND_GROUP_CODE });
            /**
             * set the size of the hashset based on the number of rows the query will return
             */
            bannedSubFunds = new HashSet<String>(hashCapacity(queryID) + subFundsProgram.size());
            Iterator subfundsForBannedFunds = getPersistenceBrokerTemplate().getReportQueryIteratorByQuery(queryID);
            /**
             * add the subfunds for the fund groups to be skipped to the hash set
             */
            while (subfundsForBannedFunds.hasNext()) {
                bannedSubFunds.add((String) ((Object[]) subfundsForBannedFunds.next())[0]);
            }
        }
        else {
            bannedSubFunds = new HashSet<String>(subFundsProgram.size() + 1);
        }
        
        return bannedSubFunds; 
    }

    /*******************************************************************************************************************************
     * This section sets all the accounting periods for the coming year to open. * The monthly budget will load by accounting
     * period. If some are not open, some monthly rows will error * out in the scrubber. Current FIS procedure is to prevent this
     * from happening, by opening all the * accounting periods and letting the university chart manager close them after the budget
     * is loaded if that * is desirable for some reason. If an institution prefers another policy, just don't call these methods. *
     * But, even if we let the scrubber fail, there will be no way to load the monthly rows from the error tables* unless the
     * corresponding accounting periods are open. *
     ******************************************************************************************************************************/

    /**
     * this method makes sure all accounting periods inn the target fiscal year are open
     * 
     * @param request fiscal year (or other fiscal period) which is the TARGET of the load
     */
    protected void openAllAccountingPeriods(Integer requestYear) {
        Criteria criteriaID = new Criteria();
        criteriaID.addEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, requestYear);
        criteriaID.addNotEqualTo(KFSPropertyConstants.UNIVERSITY_FISCAL_PERIOD_STATUS_CODE, "Y");
        QueryByCriteria queryID = new QueryByCriteria(AccountingPeriod.class, criteriaID);
        Iterator<AccountingPeriod> unopenPeriods = getPersistenceBrokerTemplate().getIteratorByQuery(queryID);
        int periodsOpened = 0;
        while (unopenPeriods.hasNext()) {
            AccountingPeriod periodToOpen = unopenPeriods.next();
            periodToOpen.setActive(true);
            getPersistenceBrokerTemplate().store(periodToOpen);
            periodsOpened = periodsOpened + 1;
        }
        LOG.warn(String.format("\n\naccounting periods for %d changed to open status: %d", requestYear, new Integer(periodsOpened)));
    }

    /*******************************************************************************************************************************
     * These two classes are containers so we can make certain variables accessible to all methods without making them global to the *
     * outer class and without cluttering up the method signatures. *
     ******************************************************************************************************************************/

    /**
     * This class keeps a set of counters and provides a method to print them out This allows us to set up thread-local counters in
     * the unlikely event this code is run by more than one thread
     */
    protected class DiagnosticCounters {
        long budgetConstructionPendingGeneralLedgerRead = 0;
        long budgetConstructionPendingGeneralLedgerSkipped = 0;
        long generalLedgerBaseBudgetWritten = 0;
        long generalLedgerCurrentBudgetWritten = 0;
        long generalLedgerActualBudgetWritten = 0;
        long generalLedgerTrusteesBudgetWritten = 0;

        long budgetConstructionMonthlyBudgetRead = 0;
        long budgetConstructionMonthlyBudgetSkipped = 0;
        long budgetConstructionMonthlyBudgetWritten = 0;
        
        long generalLedgerPendingEntriesDeleted = 0;
        long generalLedgerAcPendingEntriesDeleted = 0;
        long generalLedgerCbPendingEntriesDeleted = 0;
        long generalLedgerTbPendingEntriesDeleted = 0;
        long generalLedgerBbPendingEntriesDeleted = 0;
        long generalLedgerEntriesDeleted = 0;
        long generalLedgerAcEntriesDeleted = 0;
        long generalLedgerCbEntriesDeleted = 0;
        long generalLedgerTbEntriesDeleted = 0;
        long generalLedgerBbEntriesDeleted = 0;
        long generalLedgerBalanceEntriesDeleted = 0;
        long generalLedgerAcBalanceEntriesDeleted = 0;
        long generalLedgerCbBalanceEntriesDeleted = 0;
        long generalLedgerTbBalanceEntriesDeleted = 0;
        long generalLedgerBbBalanceEntriesDeleted = 0;

        List<String[]> tbCreated = new ArrayList<String[]>();
        List<String[]> acCreated = new ArrayList<String[]>();
        List<String[]> bbCreated = new ArrayList<String[]>();
        List<String[]> cbCreated = new ArrayList<String[]>();
        List<String[]> monthlyCreated = new ArrayList<String[]>();
        
        public void addToTbCreated(String[] entry) {
        	tbCreated.add(entry);
        }
        public void addToAcCreated(String[] entry) {
        	acCreated.add(entry);
        }
        public void addToBbCreated(String[] entry) {
        	bbCreated.add(entry);
        }
        public void addToCbCreated(String[] entry) {
        	cbCreated.add(entry);
        }

        public void addToMonthlyCreated(String[] entry) {
        	monthlyCreated.add(entry);
        }
        
        public void increaseBudgetConstructionPendingGeneralLedgerRead() {
            budgetConstructionPendingGeneralLedgerRead++;
        }

        public void increaseGeneralLedgerTrusteesBudgetWritten() {
        	generalLedgerTrusteesBudgetWritten++;
			
		}

		public void increaseGeneralLedgerActualBudgetWritten() {
			generalLedgerActualBudgetWritten++;
			
		}

		public void increaseBudgetConstructionPendingGeneralLedgerSkipped() {
            budgetConstructionPendingGeneralLedgerSkipped++;
        }

        public void increaseGeneralLedgerBaseBudgetWritten() {
            generalLedgerBaseBudgetWritten++;
        }

        public void increaseGeneralLedgerCurrentBudgetWritten() {
            generalLedgerCurrentBudgetWritten++;
        }

        public void increaseBudgetConstructionMonthlyBudgetRead() {
            budgetConstructionMonthlyBudgetRead++;
        }

        public void increaseBudgetConstructionMonthlyBudgetSkipped() {
            budgetConstructionMonthlyBudgetSkipped++;
        }

        public void increaseBudgetConstructionMonthlyBudgetWritten() {
            budgetConstructionMonthlyBudgetWritten++;
        }

        /**
		 * @return the generalLedgerPendingEntriesDeleted
		 */
		public long getGeneralLedgerPendingEntriesDeleted() {
			return generalLedgerPendingEntriesDeleted;
		}

		/**
		 * @param generalLedgerPendingEntriesDeleted the generalLedgerPendingEntriesDeleted to set
		 */
		public void setGeneralLedgerPendingEntriesDeleted(
				long generalLedgerPendingEntriesDeleted) {
			this.generalLedgerPendingEntriesDeleted = generalLedgerPendingEntriesDeleted;
		}

		/**
		 * @return the generalLedgerEntriesDeleted
		 */
		public long getGeneralLedgerEntriesDeleted() {
			return generalLedgerEntriesDeleted;
		}

		/**
		 * @param generalLedgerEntriesDeleted the generalLedgerEntriesDeleted to set
		 */
		public void setGeneralLedgerEntriesDeleted(long generalLedgerEntriesDeleted) {
			this.generalLedgerEntriesDeleted = generalLedgerEntriesDeleted;
		}

		/**
		 * @return the generalLedgerBalanceEntriesDeleted
		 */
		public long getGeneralLedgerBalanceEntriesDeleted() {
			return generalLedgerBalanceEntriesDeleted;
		}

		/**
		 * @param generalLedgerBalanceEntriesDeleted the generalLedgerBalanceEntriesDeleted to set
		 */
		public void setGeneralLedgerBalanceEntriesDeleted(
				long generalLedgerBalanceEntriesDeleted) {
			this.generalLedgerBalanceEntriesDeleted = generalLedgerBalanceEntriesDeleted;
		}
		
		

		/**
		 * @return the generalLedgerBaseBudgetWritten
		 */
		public long getGeneralLedgerBaseBudgetWritten() {
			return generalLedgerBaseBudgetWritten;
		}

		/**
		 * @param generalLedgerBaseBudgetWritten the generalLedgerBaseBudgetWritten to set
		 */
		public void setGeneralLedgerBaseBudgetWritten(
				long generalLedgerBaseBudgetWritten) {
			this.generalLedgerBaseBudgetWritten = generalLedgerBaseBudgetWritten;
		}

		/**
		 * @return the generalLedgerCurrentBudgetWritten
		 */
		public long getGeneralLedgerCurrentBudgetWritten() {
			return generalLedgerCurrentBudgetWritten;
		}

		/**
		 * @param generalLedgerCurrentBudgetWritten the generalLedgerCurrentBudgetWritten to set
		 */
		public void setGeneralLedgerCurrentBudgetWritten(
				long generalLedgerCurrentBudgetWritten) {
			this.generalLedgerCurrentBudgetWritten = generalLedgerCurrentBudgetWritten;
		}

		/**
		 * @return the generalLedgerAcPendingEntriesDeleted
		 */
		public long getGeneralLedgerAcPendingEntriesDeleted() {
			return generalLedgerAcPendingEntriesDeleted;
		}

		/**
		 * @param generalLedgerAcPendingEntriesDeleted the generalLedgerAcPendingEntriesDeleted to set
		 */
		public void setGeneralLedgerAcPendingEntriesDeleted(
				long generalLedgerAcPendingEntriesDeleted) {
			this.generalLedgerAcPendingEntriesDeleted = generalLedgerAcPendingEntriesDeleted;
		}

		/**
		 * @return the generalLedgerCbPendingEntriesDeleted
		 */
		public long getGeneralLedgerCbPendingEntriesDeleted() {
			return generalLedgerCbPendingEntriesDeleted;
		}

		/**
		 * @param generalLedgerCbPendingEntriesDeleted the generalLedgerCbPendingEntriesDeleted to set
		 */
		public void setGeneralLedgerCbPendingEntriesDeleted(
				long generalLedgerCbPendingEntriesDeleted) {
			this.generalLedgerCbPendingEntriesDeleted = generalLedgerCbPendingEntriesDeleted;
		}

		/**
		 * @return the generalLedgerTbPendingEntriesDeleted
		 */
		public long getGeneralLedgerTbPendingEntriesDeleted() {
			return generalLedgerTbPendingEntriesDeleted;
		}

		/**
		 * @param generalLedgerTbPendingEntriesDeleted the generalLedgerTbPendingEntriesDeleted to set
		 */
		public void setGeneralLedgerTbPendingEntriesDeleted(
				long generalLedgerTbPendingEntriesDeleted) {
			this.generalLedgerTbPendingEntriesDeleted = generalLedgerTbPendingEntriesDeleted;
		}

		/**
		 * @return the generalLedgerBbPendingEntriesDeleted
		 */
		public long getGeneralLedgerBbPendingEntriesDeleted() {
			return generalLedgerBbPendingEntriesDeleted;
		}

		/**
		 * @param generalLedgerBbPendingEntriesDeleted the generalLedgerBbPendingEntriesDeleted to set
		 */
		public void setGeneralLedgerBbPendingEntriesDeleted(
				long generalLedgerBbPendingEntriesDeleted) {
			this.generalLedgerBbPendingEntriesDeleted = generalLedgerBbPendingEntriesDeleted;
		}

		/**
		 * @return the generalLedgerAcEntriesDeleted
		 */
		public long getGeneralLedgerAcEntriesDeleted() {
			return generalLedgerAcEntriesDeleted;
		}

		/**
		 * @param generalLedgerAcEntriesDeleted the generalLedgerAcEntriesDeleted to set
		 */
		public void setGeneralLedgerAcEntriesDeleted(long generalLedgerAcEntriesDeleted) {
			this.generalLedgerAcEntriesDeleted = generalLedgerAcEntriesDeleted;
		}

		/**
		 * @return the generalLedgerCbEntriesDeleted
		 */
		public long getGeneralLedgerCbEntriesDeleted() {
			return generalLedgerCbEntriesDeleted;
		}

		/**
		 * @param generalLedgerCbEntriesDeleted the generalLedgerCbEntriesDeleted to set
		 */
		public void setGeneralLedgerCbEntriesDeleted(long generalLedgerCbEntriesDeleted) {
			this.generalLedgerCbEntriesDeleted = generalLedgerCbEntriesDeleted;
		}

		/**
		 * @return the generalLedgerTbEntriesDeleted
		 */
		public long getGeneralLedgerTbEntriesDeleted() {
			return generalLedgerTbEntriesDeleted;
		}

		/**
		 * @param generalLedgerTbEntriesDeleted the generalLedgerTbEntriesDeleted to set
		 */
		public void setGeneralLedgerTbEntriesDeleted(long generalLedgerTbEntriesDeleted) {
			this.generalLedgerTbEntriesDeleted = generalLedgerTbEntriesDeleted;
		}

		/**
		 * @return the generalLedgerBbEntriesDeleted
		 */
		public long getGeneralLedgerBbEntriesDeleted() {
			return generalLedgerBbEntriesDeleted;
		}

		/**
		 * @param generalLedgerBbEntriesDeleted the generalLedgerBbEntriesDeleted to set
		 */
		public void setGeneralLedgerBbEntriesDeleted(long generalLedgerBbEntriesDeleted) {
			this.generalLedgerBbEntriesDeleted = generalLedgerBbEntriesDeleted;
		}

		/**
		 * @return the generalLedgerAcBalanceEntriesDeleted
		 */
		public long getGeneralLedgerAcBalanceEntriesDeleted() {
			return generalLedgerAcBalanceEntriesDeleted;
		}

		/**
		 * @param generalLedgerAcBalanceEntriesDeleted the generalLedgerAcBalanceEntriesDeleted to set
		 */
		public void setGeneralLedgerAcBalanceEntriesDeleted(
				long generalLedgerAcBalanceEntriesDeleted) {
			this.generalLedgerAcBalanceEntriesDeleted = generalLedgerAcBalanceEntriesDeleted;
		}

		/**
		 * @return the generalLedgerCbBalanceEntriesDeleted
		 */
		public long getGeneralLedgerCbBalanceEntriesDeleted() {
			return generalLedgerCbBalanceEntriesDeleted;
		}

		/**
		 * @param generalLedgerCbBalanceEntriesDeleted the generalLedgerCbBalanceEntriesDeleted to set
		 */
		public void setGeneralLedgerCbBalanceEntriesDeleted(
				long generalLedgerCbBalanceEntriesDeleted) {
			this.generalLedgerCbBalanceEntriesDeleted = generalLedgerCbBalanceEntriesDeleted;
		}

		/**
		 * @return the generalLedgerTbBalanceEntriesDeleted
		 */
		public long getGeneralLedgerTbBalanceEntriesDeleted() {
			return generalLedgerTbBalanceEntriesDeleted;
		}

		/**
		 * @param generalLedgerTbBalanceEntriesDeleted the generalLedgerTbBalanceEntriesDeleted to set
		 */
		public void setGeneralLedgerTbBalanceEntriesDeleted(
				long generalLedgerTbBalanceEntriesDeleted) {
			this.generalLedgerTbBalanceEntriesDeleted = generalLedgerTbBalanceEntriesDeleted;
		}

		/**
		 * @return the generalLedgerBbBalanceEntriesDeleted
		 */
		public long getGeneralLedgerBbBalanceEntriesDeleted() {
			return generalLedgerBbBalanceEntriesDeleted;
		}

		/**
		 * @param generalLedgerBbBalanceEntriesDeleted the generalLedgerBbBalanceEntriesDeleted to set
		 */
		public void setGeneralLedgerBbBalanceEntriesDeleted(
				long generalLedgerBbBalanceEntriesDeleted) {
			this.generalLedgerBbBalanceEntriesDeleted = generalLedgerBbBalanceEntriesDeleted;
		}

		public void writeDiagnosticCounters() {
            LOG.warn(String.format("\n\nPending Budget Construction General Ledger Load\n"));
            LOG.warn(String.format("\n  pending budget construction GL rows read:        %,d", budgetConstructionPendingGeneralLedgerRead));
            LOG.warn(String.format("\n  pending budget construction GL rows skipped:     %,d", budgetConstructionPendingGeneralLedgerSkipped));
            LOG.warn(String.format("\n\n  base budget rows written:                        %,d", generalLedgerBaseBudgetWritten));
            LOG.warn(String.format("\n  current budget rows written:                     %,d", generalLedgerCurrentBudgetWritten));
            LOG.warn(String.format("\n\n  pending budget construction monthly rows read:   %,d", budgetConstructionMonthlyBudgetRead));
            LOG.warn(String.format("\n  pending budget construction monthly rows skipped: %,d", budgetConstructionMonthlyBudgetSkipped));
            LOG.warn(String.format("\n  pending budget construction monthly rows written: %,d", budgetConstructionMonthlyBudgetWritten));
        }
    }

    /**
     * This class allows us to create global variables and pass them around. This should make the code thread safe, in the unlikely
     * event it is called by more than one thread. it also allows us to fetch constants and build datas stuctures from the DB once
     * upon instantiation of this class, and make them available for the duration of the run
     * 
     * @param requestYear
     * @param <documentNumber, ledger sequence number> HashMap
     * @param current SQL Date (which will be the transaction date in the general ledger entry rows we create)
     * @param the "financial system Origination Code" for this database
     */
    protected class DaoGlobalVariables {
        private Integer requestYear;
        private HashMap<String, Integer> entrySequenceNumber;
        private Date transactionDate;
        private String financialSystemOriginationCode;
        private HashSet<String> accountsNotToBeLoaded;

        public DaoGlobalVariables(Integer requestYear) {
            this.requestYear = requestYear;
            // this.transactionDate = SpringContext.getBean(DateTimeService.class).getCurrentSqlDate();
            this.transactionDate = dateTimeService.getCurrentSqlDate();
            this.financialSystemOriginationCode = homeOriginationService.getHomeOrigination().getFinSystemHomeOriginationCode();
            this.entrySequenceNumber = entrySequenceNumber(requestYear, financialSystemOriginationCode);
            this.accountsNotToBeLoaded = getAccountsNotToBeLoaded();
        }

        public Integer getRequestYear() {
            return this.requestYear;
        }

        /**
         * return the next available sequence number for the input key and update "next available"
         */
        public Integer getNextSequenceNumber(String seqKey) {
            Integer newSeqNumber = entrySequenceNumber.get(seqKey);
            entrySequenceNumber.put(seqKey, new Integer(newSeqNumber.intValue() + 1));
            return newSeqNumber;
        }

        public Date getTransactionDate() {
            return this.transactionDate;
        }

        public String getFinancialSystemOriginationcode() {
            return this.financialSystemOriginationCode;
        }

        public boolean shouldThisAccountLoad(String accountAndChart) {
            return (!accountsNotToBeLoaded.contains(accountAndChart));
        }
    }
    
    protected void writeReport(PrintStream reportDataStream, DiagnosticCounters diagnosticCounters) {
    	StringBuilder body = new StringBuilder();
    	if(!tbRunFlag) {
    		body.append(String.format("\n\nRegular (non-TB) Budget Construction General Ledger Load beginning\n"));
    	} else {
    		body.append(String.format("\n\nTB Budget Construction General Ledger Load beginning\n"));
    	}

    	//first output deleted rows

    	body.append(String.format("\n  Total pending GL Entries deleted:                %,d", diagnosticCounters.getGeneralLedgerPendingEntriesDeleted()));
    	body.append(String.format("\n  Total GL Entries deleted:                        %,d", diagnosticCounters.getGeneralLedgerEntriesDeleted()));
    	body.append(String.format("\n  Total balance GL Entries deleted:                %,d", diagnosticCounters.getGeneralLedgerBalanceEntriesDeleted()));
    	
    	body.append(String.format("\n\n  AC pending GL Entries deleted:                   %,d", diagnosticCounters.getGeneralLedgerAcPendingEntriesDeleted()));
    	body.append(String.format("\n  AC GL Entries deleted:                           %,d", diagnosticCounters.getGeneralLedgerAcEntriesDeleted()));
    	body.append(String.format("\n  AC balance GL Entries deleted:                   %,d", diagnosticCounters.getGeneralLedgerAcBalanceEntriesDeleted()));
    	
    	body.append(String.format("\n\n  CB pending GL Entries deleted:                   %,d", diagnosticCounters.getGeneralLedgerCbPendingEntriesDeleted()));
    	body.append(String.format("\n  CB GL Entries deleted:                           %,d", diagnosticCounters.getGeneralLedgerCbEntriesDeleted()));
    	body.append(String.format("\n  CB balance GL Entries deleted:                   %,d", diagnosticCounters.getGeneralLedgerCbBalanceEntriesDeleted()));

    	body.append(String.format("\n\n  BB pending GL Entries deleted:                   %,d", diagnosticCounters.getGeneralLedgerBbPendingEntriesDeleted()));
    	body.append(String.format("\n  BB GL Entries deleted:                           %,d", diagnosticCounters.getGeneralLedgerBbEntriesDeleted()));
    	body.append(String.format("\n  BB balance GL Entries deleted:                   %,d", diagnosticCounters.getGeneralLedgerBbBalanceEntriesDeleted()));

    	body.append(String.format("\n\n  TB pending GL Entries deleted:                   %,d", diagnosticCounters.getGeneralLedgerTbPendingEntriesDeleted()));
    	body.append(String.format("\n  TB GL Entries deleted:                           %,d", diagnosticCounters.getGeneralLedgerTbEntriesDeleted()));
    	body.append(String.format("\n  TB balance GL Entries deleted:                   %,d", diagnosticCounters.getGeneralLedgerTbBalanceEntriesDeleted()));
    	
        body.append(String.format("\n\n  pending budget construction GL rows read:        %,d", diagnosticCounters.budgetConstructionPendingGeneralLedgerRead));
        body.append(String.format("\n  pending budget construction GL rows skipped:     %,d", diagnosticCounters.budgetConstructionPendingGeneralLedgerSkipped));
        body.append(String.format("\n\n  base budget rows written:                        %,d", diagnosticCounters.generalLedgerBaseBudgetWritten));
        body.append(String.format("\n  current budget rows written:                     %,d", diagnosticCounters.generalLedgerCurrentBudgetWritten));
        body.append(String.format("\n  actual budget rows written:                      %,d", diagnosticCounters.generalLedgerActualBudgetWritten));
        body.append(String.format("\n  trustee budget rows written:                     %,d", diagnosticCounters.generalLedgerTrusteesBudgetWritten));
        
        body.append(String.format("\n\n  pending budget construction monthly rows read:   %,d", diagnosticCounters.budgetConstructionMonthlyBudgetRead));
        body.append(String.format("\n  pending budget construction monthly rows skipped: %,d", diagnosticCounters.budgetConstructionMonthlyBudgetSkipped));
        body.append(String.format("\n  pending budget construction monthly rows written: %,d", diagnosticCounters.budgetConstructionMonthlyBudgetWritten));
        
        body.append("\n\nOutputting " + diagnosticCounters.bbCreated.size() + " sample of Base Budget records (every 100th record saved)\n");
        for(String[] entry: diagnosticCounters.bbCreated) {
        	body.append("\n BB Entry: ");
        	for(String item: entry) {
        		body.append(item + " ");
        	}
        }
        body.append("\n");

        body.append("\nOutputting " + diagnosticCounters.cbCreated.size() + " sample of Current Budget records (every 100th record saved)\n");
        for(String[] entry: diagnosticCounters.cbCreated) {
        	body.append("\n CB Entry: ");
        	for(String item: entry) {
        		body.append(item + " ");
        	}
        }
        body.append("\n");
        
        body.append("\nOutputting " + diagnosticCounters.acCreated.size() + " sample of Actual Budget records (every 100th record saved)\n");
        for(String[] entry: diagnosticCounters.acCreated) {
        	body.append("\n AC Entry: ");
        	for(String item: entry) {
        		body.append(item + " ");
        	}
        }
        body.append("\n");

        body.append("\nOutputting " + diagnosticCounters.tbCreated.size() + " sample of Trustee Budget records (every 100th record saved)\n");
        for(String[] entry: diagnosticCounters.tbCreated) {
        	body.append("\n TB Entry: ");
        	for(String item: entry) {
        		body.append(item + " ");
        	}
        }
        body.append("\n");
    	DateFormat format = new SimpleDateFormat("yyyy/MM/dd @ HH:mm:ss");
    	java.util.Date now = new java.util.Date();
    	String date = format.format(now);
    	body.append("\nCornell BC > GL load job ending at " + date);
    	
    	reportDataStream.print(body);
        if (reportDataStream != null) {
            reportDataStream.flush();
            reportDataStream.close();
        }
    }
    
    /**
     * get print stream for report
     */
    protected PrintStream getReportPrintStream() {
        String dateTime = dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentSqlDate());
        reportFileName = MessageFormat.format(REPORT_FILE_NAME_PATTERN, this.getBudgetReportDirectory(), this.getBudgetReportFilePrefix(), dateTime, ".txt");

        File outputfile = new File(reportFileName);

        try {
            return new PrintStream(outputfile);
        }
        catch (FileNotFoundException e) {
            String errorMessage = "Cannot find the output file: " + reportFileName;

            LOG.error(errorMessage);
            throw new RuntimeException(errorMessage, e);
        }
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setHomeOriginationService(HomeOriginationService homeOriginationService) {
        this.homeOriginationService = homeOriginationService;
    }
    
    /**
     * Gets the parameterService attribute.
     * 
     * @return Returns the parameterService.
     */
    public ParameterService getParameterService() {
        return parameterService;
    }

    /**
     * Sets the parameterService attribute value.
     * 
     * @param parameterService The parameterService to set.
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Gets the boService attribute. 
     * @return Returns the boService.
     */
    public BusinessObjectService getBusinessObjectService() {
        return boService;
    }

    /**
     * Sets the boService attribute value.
     * @param boService The boService to set.
     */
    public void setBusinessObjectService(BusinessObjectService boService) {
        this.boService = boService;
    }

    /**
     * Gets the budgetReportDirectory attribute. 
     * @return Returns the budgetReportDirectory.
     */
    public String getBudgetReportDirectory() {
        return budgetReportDirectory;
    }

    /**
     * Sets the budgetReportDirectory attribute value.
     * @param budgetReportDirectory The budgetReportDirectory to set.
     */
    public void setBudgetReportDirectory(String budgetReportDirectory) {
        this.budgetReportDirectory = budgetReportDirectory;
    }

    /**
     * Gets the budgetReportFilePrefix attribute. 
     * @return Returns the budgetReportFilePrefix.
     */
    public String getBudgetReportFilePrefix() {
        return budgetReportFilePrefix;
    }

    /**
     * Sets the budgetReportFilePrefix attribute value.
     * @param budgetReportFilePrefix The budgetReportFilePrefix to set.
     */
    public void setBudgetReportFilePrefix(String budgetReportFilePrefix) {
        this.budgetReportFilePrefix = budgetReportFilePrefix;
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

	protected boolean isProduction() {
	     return ConfigContext.getCurrentContextConfig().getProperty(KEWConstants.PROD_DEPLOYMENT_CODE).equalsIgnoreCase(
	       ConfigContext.getCurrentContextConfig().getEnvironment());
	}
}
