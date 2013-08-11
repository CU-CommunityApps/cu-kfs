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
package org.kuali.kfs.gl.batch.service.impl;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.coa.businessobject.ClosedAccountReversion;
import org.kuali.kfs.coa.businessobject.Reversion;
import org.kuali.kfs.coa.service.OrganizationReversionService;
import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.service.ReversionUnitOfWorkService;
import org.kuali.kfs.gl.batch.service.impl.exception.FatalErrorException;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.gl.businessobject.ReversionUnitOfWork;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.OptionsService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class actually runs the year end organization reversion process
 */
@Transactional
public class OrganizationReversionProcessImpl extends ReversionProcessBase implements  InitializingBean {
    static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(OrganizationReversionProcessImpl.class);

    // Services
    private OrganizationReversionService organizationReversionService;
    private ReversionUnitOfWorkService orgReversionUnitOfWorkService;
    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        this.CARRY_FORWARD_OBJECT_CODE = getParameterService().getParameterValueAsString(Reversion.class, GeneralLedgerConstants.ReversionProcess.CARRY_FORWARD_OBJECT_CODE);
        this.DEFAULT_FINANCIAL_DOCUMENT_TYPE_CODE = getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_DOCUMENT_TYPE);
        this.DEFAULT_FINANCIAL_SYSTEM_ORIGINATION_CODE = getParameterService().getParameterValueAsString(Reversion.class, GeneralLedgerConstants.ReversionProcess.DEFAULT_FINANCIAL_SYSTEM_ORIGINATION_CODE);
        this.DEFAULT_FINANCIAL_BALANCE_TYPE_CODE = getParameterService().getParameterValueAsString(Reversion.class, GeneralLedgerConstants.ReversionProcess.DEFAULT_FINANCIAL_BALANCE_TYPE_CODE);
        this.DEFAULT_FINANCIAL_BALANCE_TYPE_CODE_YEAR_END = getParameterService().getParameterValueAsString(Reversion.class, GeneralLedgerConstants.ReversionProcess.DEFAULT_FINANCIAL_BALANCE_TYPE_CODE_YEAR_END);
        this.DEFAULT_DOCUMENT_NUMBER_PREFIX = getParameterService().getParameterValueAsString(Reversion.class, GeneralLedgerConstants.ReversionProcess.DEFAULT_DOCUMENT_NUMBER_PREFIX);

        this.CASH_REVERTED_TO_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ReversionProcess.CASH_REVERTED_TO);
        this.FUND_BALANCE_REVERTED_TO_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ReversionProcess.FUND_BALANCE_REVERTED_TO);
        this.CASH_REVERTED_FROM_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ReversionProcess.CASH_REVERTED_FROM);
        this.FUND_BALANCE_REVERTED_FROM_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ReversionProcess.FUND_BALANCE_REVERTED_FROM);
        this.FUND_CARRIED_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ReversionProcess.FUND_CARRIED);
        this.FUND_REVERTED_TO_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ReversionProcess.FUND_REVERTED_TO);
        this.FUND_REVERTED_FROM_MESSAGE = getConfigurationService().getPropertyValueAsString(KFSKeyConstants.ReversionProcess.FUND_REVERTED_FROM);
        
        outputFileName = getBatchFileDirectoryName() + File.separator + (usePriorYearInformation ? GeneralLedgerConstants.BatchFileSystem.ORGANIZATION_REVERSION_CLOSING_FILE : GeneralLedgerConstants.BatchFileSystem.ORGANIZATION_REVERSION_PRE_CLOSING_FILE) + GeneralLedgerConstants.BatchFileSystem.EXTENSION;
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
        
        Balance bal;
        while (balances.hasNext()) {
            bal = balances.next();
            if (LOG.isDebugEnabled()) {
                LOG.debug("BALANCE SELECTED: " + bal.getUniversityFiscalYear() + bal.getChartOfAccountsCode() + bal.getAccountNumber() + bal.getSubAccountNumber() + bal.getObjectCode() + bal.getSubObjectCode() + bal.getBalanceTypeCode() + bal.getObjectTypeCode() + " " + bal.getAccountLineAnnualBalanceAmount().add(bal.getBeginningBalanceLineAmount()));
            }

            try {
                if (!unitOfWork.isInitialized()) {
                    unitOfWork.setFields(bal.getChartOfAccountsCode(), bal.getAccountNumber(), bal.getSubAccountNumber());
                    retrieveCurrentReversionAndAccount(bal);
                }
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
                        getOrgReversionUnitOfWorkService().save(unitOfWork);
                    }
                    unitOfWork.setFields(bal.getChartOfAccountsCode(), bal.getAccountNumber(), bal.getSubAccountNumber());
                    retrieveCurrentReversionAndAccount(bal);
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
                getOrgReversionUnitOfWorkService().save(unitOfWork);
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

        if ((cfReversionProcessInfo == null) || (!cfReversionProcessInfo.getChartOfAccountsCode().equals(bal.getChartOfAccountsCode())) || (!cfReversionProcessInfo.getSourceAttribute().equals(account.getOrganizationCode()))) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Organization Reversion Service: " + getOrganizationReversionService() + "; fiscal year: " + (Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR) + "; account: " + account + "; account organization code: " + account.getOrganizationCode() + "; balance: " + bal + "; balance chart: " + bal.getChartOfAccountsCode());
            }
            cfReversionProcessInfo = getOrganizationReversionService().getByPrimaryId((Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR), bal.getChartOfAccountsCode(), account.getOrganizationCode());
        }

        if (cfReversionProcessInfo == null) {
            // we can't find an organization reversion for this balance? Throw exception
            throw new FatalErrorException("No Organization Reversion found for: " + (Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR) + "-" + bal.getChartOfAccountsCode() + "-" + account.getOrganizationCode());
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
        orgReversionUnitOfWorkService.destroyAllUnitOfWorkSummaries();

        categories = getOrganizationReversionService().getCategories();
        categoryList = getOrganizationReversionService().getCategoryList();

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
    public OrganizationReversionService getOrganizationReversionService() {
        return organizationReversionService;
    }

    /**
     * Sets the organizationReversionService attribute value.
     * @param organizationReversionService The organizationReversionService to set.
     */
    public void setOrganizationReversionService(OrganizationReversionService organizationReversionService) {
        this.organizationReversionService = organizationReversionService;
    }

    /**
     * Gets the orgReversionUnitOfWorkService attribute. 
     * @return Returns the orgReversionUnitOfWorkService.
     */
    public ReversionUnitOfWorkService getOrgReversionUnitOfWorkService() {
        return orgReversionUnitOfWorkService;
    }

    /**
     * Sets the orgReversionUnitOfWorkService attribute value.
     * @param orgReversionUnitOfWorkService The orgReversionUnitOfWorkService to set.
     */
    public void setOrgReversionUnitOfWorkService(ReversionUnitOfWorkService orgReversionUnitOfWorkService) {
        this.orgReversionUnitOfWorkService = orgReversionUnitOfWorkService;
    }


    
}
