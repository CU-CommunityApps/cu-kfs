
/*
 * Copyright 2006-2009 The Kuali Foundation
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
package edu.cornell.kfs.gl.batch.service.impl;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.gl.GeneralLedgerConstants;

import org.kuali.kfs.gl.GLParameterConstants;
import org.kuali.kfs.gl.batch.service.impl.exception.FatalErrorException;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.coa.businessobject.AccountReversion;
import edu.cornell.kfs.coa.businessobject.ClosedAccountReversion;
import edu.cornell.kfs.coa.businessobject.Reversion;
import edu.cornell.kfs.coa.service.AccountReversionService;
import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.gl.batch.service.ReversionUnitOfWorkService;
import edu.cornell.kfs.gl.businessobject.ReversionUnitOfWork;
import edu.cornell.kfs.sys.CUKFSKeyConstants;
import edu.emory.mathcs.backport.java.util.concurrent.BrokenBarrierException;

/**
 * This class actually runs the year end organization reversion process
 */
@Transactional
public class AccountReversionProcessImpl extends ReversionProcessBase implements  InitializingBean {
	private static final Logger LOG = LogManager.getLogger(AccountReversionProcessImpl.class);

    // Services
    private AccountReversionService accountReversionService;
    private ReversionUnitOfWorkService reversionUnitOfWorkService;
    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        this.CARRY_FORWARD_OBJECT_CODE = getParameterService().getParameterValueAsString(Reversion.class, CuGeneralLedgerConstants.ReversionProcess.CARRY_FORWARD_OBJECT_CODE);
        this.DEFAULT_FINANCIAL_DOCUMENT_TYPE_CODE = getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_NAMESPACE, GLParameterConstants.ANNUAL_CLOSING_COMPONENT, GLParameterConstants.DOCUMENT_TYPE);
        this.DEFAULT_FINANCIAL_SYSTEM_ORIGINATION_CODE = getParameterService().getParameterValueAsString(Reversion.class, CuGeneralLedgerConstants.ReversionProcess.MANUAL_FEED_ORIGINATION);
        this.DEFAULT_FINANCIAL_BALANCE_TYPE_CODE = getParameterService().getParameterValueAsString(Reversion.class, CuGeneralLedgerConstants.ReversionProcess.CASH_BALANCE_TYPE);
        this.DEFAULT_FINANCIAL_BALANCE_TYPE_CODE_YEAR_END = getParameterService().getParameterValueAsString(Reversion.class, CuGeneralLedgerConstants.ReversionProcess.BUDGET_BALANCE_TYPE);
        this.DEFAULT_DOCUMENT_NUMBER_PREFIX = getParameterService().getParameterValueAsString(Reversion.class, CuGeneralLedgerConstants.ReversionProcess.DOCUMENT_NUMBER_PREFIX);

        this.CASH_REVERTED_TO_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.OrganizationReversionProcess.CASH_REVERTED_TO);
        this.FUND_BALANCE_REVERTED_TO_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.OrganizationReversionProcess.FUND_BALANCE_REVERTED_TO);
        this.CASH_REVERTED_FROM_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.OrganizationReversionProcess.CASH_REVERTED_FROM);
        this.FUND_BALANCE_REVERTED_FROM_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.OrganizationReversionProcess.FUND_BALANCE_REVERTED_FROM);
        this.FUND_CARRIED_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.OrganizationReversionProcess.FUND_CARRIED);
        this.FUND_REVERTED_TO_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.OrganizationReversionProcess.FUND_REVERTED_TO);
        this.FUND_REVERTED_FROM_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.OrganizationReversionProcess.FUND_REVERTED_FROM);
        
        outputFileName = getBatchFileDirectoryName() + File.separator + (usePriorYearInformation ? CuGeneralLedgerConstants.CuBatchFileSystem.ACCOUNT_REVERSION_CLOSING_FILE : CuGeneralLedgerConstants.CuBatchFileSystem.ACCOUNT_REVERSION_PRE_CLOSING_FILE) + GeneralLedgerConstants.BatchFileSystem.EXTENSION;
    }



    /**
     * Given a list of balances, this method generates the origin entries for the organization reversion/carry forward process, and saves those
     * to an initialized origin entry group
     * 
     * @param balances an iterator of balances to process; each balance returned by the iterator will be processed by this method
     */
    public void processBalances(Iterator<Balance> balances) {
        boolean skipToNextUnitOfWork = false;
        unitOfWork = new ReversionUnitOfWork();
        unitOfWork.setCategories(categoryList);
        
        int brokenCodeCount = 0;
        Balance bal;
        while (balances.hasNext()) {
            bal = balances.next();
            
            // we only want AC balance types so we will limit them here rather than in the query used for both Account Reversion and Organization Reversion.
            if (!bal.getBalanceTypeCode().equals(KFSConstants.BALANCE_TYPE_ACTUAL)){
            	continue;
            }
            
            String acctNumber = bal.getAccountNumber();
            
            if (LOG.isDebugEnabled()) {
                LOG.debug("BALANCE SELECTED: " + bal.getUniversityFiscalYear() + bal.getChartOfAccountsCode() + bal.getAccountNumber() + bal.getSubAccountNumber() + bal.getObjectCode() + bal.getSubObjectCode() + bal.getBalanceTypeCode() + bal.getObjectTypeCode() + " " + bal.getAccountLineAnnualBalanceAmount().add(bal.getBeginningBalanceLineAmount()));
            }

            try {
                if (!unitOfWork.isInitialized()) {
                    unitOfWork.setFields(bal.getChartOfAccountsCode(), bal.getAccountNumber(), bal.getSubAccountNumber());
                    retrieveCurrentReversionAndAccount(bal);
                }
                
                //This area is suspect. 
               // just gonna leave this broken code here....
                else if (!unitOfWork.wouldHold(bal)) {
                    if (!skipToNextUnitOfWork) {
                        calculateTotals();
                        List<OriginEntryFull> originEntriesToWrite = generateOutputOriginEntries();
                        summarizeOriginEntries(originEntriesToWrite);
                        if (holdGeneratedOriginEntries) {
                            generatedOriginEntries.addAll(originEntriesToWrite);
                        }
                        int recordsWritten = writeOriginEntries(originEntriesToWrite);
                        incrementCount("recordsWritten", recordsWritten);
                        getReversionUnitOfWorkService().save(unitOfWork);
                    }
                    unitOfWork.setFields(bal.getChartOfAccountsCode(), bal.getAccountNumber(), bal.getSubAccountNumber());
                    retrieveCurrentReversionAndAccount(bal);
                    brokenCodeCount++;
                    skipToNextUnitOfWork = false;
                }
                if (skipToNextUnitOfWork) {
                    continue; // if there is no org reversion or an org reversion detail is missing or the balances are off for
                    // this unit of work,
                    // just skip all the balances until we change unit of work
                }
                calculateBucketAmounts(bal);
            }
            catch (FatalErrorException fee) {
                LOG.info(fee.getMessage());
                skipToNextUnitOfWork = true;
            }
        }
        
        System.out.println("Total broken code balances processed: "+brokenCodeCount);
        
        // save the final unit of work
        if (!skipToNextUnitOfWork && getBalancesSelected() > 0) {
            try {
                calculateTotals();
                List<OriginEntryFull> originEntriesToWrite = generateOutputOriginEntries();
                summarizeOriginEntries(originEntriesToWrite);
                if (holdGeneratedOriginEntries) {
                    generatedOriginEntries.addAll(originEntriesToWrite);
                }
                int recordsWritten = writeOriginEntries(originEntriesToWrite);
                incrementCount("recordsWritten", recordsWritten);
                getReversionUnitOfWorkService().save(unitOfWork);
            }
            catch (FatalErrorException fee) {
                LOG.info(fee.getMessage());
            }
        }
        
    }

    /**
     * Given a balance, returns the current organization reversion record and account or prior year account for the balance; it sets them
     * to private properties
     * 
     * @param bal the balance to find the account/prior year account and organization reversion record for 
     * @throws FatalErrorException if an organization reversion record cannot be found in the database 
     */
    protected void retrieveCurrentReversionAndAccount(Balance bal) throws FatalErrorException {
        // initialize the account
        if ((account == null) || (!bal.getChartOfAccountsCode().equals(account.getChartOfAccountsCode())) || (!bal.getAccountNumber().equals(account.getAccountNumber()))) {
            if (usePriorYearInformation) {
                account = getPriorYearAccountService().getByPrimaryKey(bal.getChartOfAccountsCode(), bal.getAccountNumber());
            }
            else {
                account = bal.getAccount();
            }
        }

        if ((cfReversionProcessInfo == null) || (!cfReversionProcessInfo.getChartOfAccountsCode().equals(bal.getChartOfAccountsCode())) || (!cfReversionProcessInfo.getSourceAttribute().equals(account.getAccountNumber()))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Account Reversion Service: " + getAccountReversionService() + "; fiscal year: " + (Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR) + "; account: " + account + "; account account number: " + account.getAccountNumber() + "; balance: " + bal + "; balance chart: " + bal.getChartOfAccountsCode());
            }
            AccountReversion acctRev = getAccountReversionService().getByPrimaryId((Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR), bal.getChartOfAccountsCode(), account.getAccountNumber());
            
            if (ObjectUtils.isNotNull(acctRev) && acctRev.isActive()) {
            	cfReversionProcessInfo = acctRev;
            } 
            else {
            	cfReversionProcessInfo = null;
            }
        
        }

        if (cfReversionProcessInfo == null) {
            // we can't find an organization reversion for this balance? Throw exception
            throw new FatalErrorException("No Account Reversion found for: " + (Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR) + "-" + bal.getChartOfAccountsCode() + "-" + account.getAccountNumber());
        }
        
        if (account.isClosed()) {
            cfReversionProcessInfo = new ClosedAccountReversion(cfReversionProcessInfo);
        }
    }

    /**
     * This method initializes several properties needed for the process to run correctly
     */
    public void initializeProcess() {

        // clear out summary tables
        LOG.info("destroying all unit of work summaries");
        reversionUnitOfWorkService.destroyAllUnitOfWorkSummaries();

        categories = getAccountReversionService().getCategories();
        categoryList = getAccountReversionService().getCategoryList();

        paramFiscalYear = (Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR);

        reversionCounts.put("balancesRead", balanceService.countBalancesForFiscalYear(paramFiscalYear));
        reversionCounts.put("balancesSelected", new Integer(0));
        reversionCounts.put("recordsWritten", new Integer(0));

        this.systemOptions = SpringContext.getBean(OptionsService.class).getOptions(paramFiscalYear);
        
        ledgerReport = new LedgerSummaryReport();
    }

    /**
     * Gets the organizationReversionService attribute. 
     * @return Returns the organizationReversionService.
     */
    public AccountReversionService getAccountReversionService() {
        return accountReversionService;
    }

    /**
     * Sets the organizationReversionService attribute value.
     * @param organizationReversionService The organizationReversionService to set.
     */
    public void setAccountReversionService(AccountReversionService accountReversionService) {
        this.accountReversionService = accountReversionService;
    }

    /**
     * Gets the orgReversionUnitOfWorkService attribute. 
     * @return Returns the orgReversionUnitOfWorkService.
     */
    public ReversionUnitOfWorkService getReversionUnitOfWorkService() {
        return reversionUnitOfWorkService;
    }

    /**
     * Sets the orgReversionUnitOfWorkService attribute value.
     * @param orgReversionUnitOfWorkService The orgReversionUnitOfWorkService to set.
     */
    public void setReversionUnitOfWorkService(ReversionUnitOfWorkService reversionUnitOfWorkService) {
        this.reversionUnitOfWorkService = reversionUnitOfWorkService;
    }


    
}
