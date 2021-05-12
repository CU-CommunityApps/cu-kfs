/*
 * Copyright 2012 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.gl.batch.service.impl;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.AccountIntf;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.service.PriorYearAccountService;
import org.kuali.kfs.gl.batch.service.impl.exception.FatalErrorException;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.gl.service.BalanceService;
import org.kuali.kfs.gl.service.OriginEntryService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.service.FlexibleOffsetAccountService;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.PersistenceService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.springframework.beans.factory.InitializingBean;

import edu.cornell.kfs.coa.businessobject.CarryForwardReversionProcessInfo;
import edu.cornell.kfs.coa.businessobject.ReversionCategory;
import edu.cornell.kfs.coa.businessobject.ReversionCategoryInfo;
import edu.cornell.kfs.gl.batch.service.ReversionCategoryLogic;
import edu.cornell.kfs.gl.batch.service.ReversionProcess;
import edu.cornell.kfs.gl.businessobject.ReversionUnitOfWork;
import edu.cornell.kfs.gl.businessobject.ReversionUnitOfWorkCategoryAmount;
import edu.cornell.kfs.gl.service.CuBalanceService;
import edu.cornell.kfs.sys.CUKFSConstants;

public abstract class ReversionProcessBase implements ReversionProcess {

	private static final Logger LOG = LogManager.getLogger(ReversionProcessBase.class);

    
    private OriginEntryService originEntryService;
    private PersistenceService persistenceService;
    private DateTimeService dateTimeService;
    private PriorYearAccountService priorYearAccountService;
    private FlexibleOffsetAccountService flexibleOffsetAccountService;
    protected ParameterService parameterService;
    private ConfigurationService configurationService;
    protected CuBalanceService balanceService;
    private String batchFileDirectoryName;
    protected String outputFileName;
    protected List<ReversionCategory> categoryList;
    protected CarryForwardReversionProcessInfo cfReversionProcessInfo;
    protected AccountIntf account;
    protected Map jobParameters;
    protected Map<String, Integer> reversionCounts;
    protected boolean usePriorYearInformation;
    protected boolean holdGeneratedOriginEntries = false;
    protected List<OriginEntryFull> generatedOriginEntries;
    protected SystemOptions systemOptions;
    protected Integer paramFiscalYear;
    
    public String CARRY_FORWARD_OBJECT_CODE;
    public String DEFAULT_FINANCIAL_DOCUMENT_TYPE_CODE;
    public String DEFAULT_FINANCIAL_SYSTEM_ORIGINATION_CODE;
    public String DEFAULT_FINANCIAL_BALANCE_TYPE_CODE;
    public String DEFAULT_FINANCIAL_BALANCE_TYPE_CODE_YEAR_END;
    public String DEFAULT_DOCUMENT_NUMBER_PREFIX;

    protected String CASH_REVERTED_TO_MESSAGE;
    protected String FUND_BALANCE_REVERTED_TO_MESSAGE;
    protected String CASH_REVERTED_FROM_MESSAGE;
    protected String FUND_BALANCE_REVERTED_FROM_MESSAGE;
    protected String FUND_CARRIED_MESSAGE;
    protected String FUND_REVERTED_TO_MESSAGE;
    protected String FUND_REVERTED_FROM_MESSAGE;
    
    private ReversionCategoryLogic cashReversionCategoryLogic;


    protected ReversionUnitOfWork unitOfWork;


    protected Map<String, ReversionCategoryLogic> categories;


    protected LedgerSummaryReport ledgerReport;


    PrintStream outputPs;

    public ReversionProcessBase() {
        super();
    }

    protected int writeOriginEntries(List<OriginEntryFull> originEntriesToWrite) {
        int originEntriesWritten = 0;
    
        for (OriginEntryFull originEntry : originEntriesToWrite) {
            getOriginEntryService().createEntry(originEntry, outputPs);
            originEntriesWritten += 1;
        }
    
        return originEntriesWritten;
    }
    
    /**
     * This evilly named method actually runs the  reversion process.
     */
    public void reversionProcess(Map jobParameters, Map<String, Integer> reversionCounts) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("organizationReversionProcess() started");
        }
        this.jobParameters = jobParameters;
        this.reversionCounts = reversionCounts;

        LOG.info("Initializing the process");
        initializeProcess();
        
        //create files
        File outputFile = new File(outputFileName);
        
        try {
            outputPs = new PrintStream(outputFile);
        
            Iterator<Balance> balances = getBalanceService().findReversionBalancesForFiscalYear((Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR), usePriorYearInformation);
            processBalances(balances);
            
            outputPs.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Reversion File doesn't exist " + outputFileName);
        }

    }

    
    public abstract void initializeProcess();
    
    public abstract void processBalances(Iterator<Balance> balances);

    
    /**
     * Sets the holdGeneratedOriginEntries attribute value.
     * 
     * @param holdGeneratedOriginEntries The holdGeneratedOriginEntries to set.
     */
    public void setHoldGeneratedOriginEntries(boolean holdGeneratedOriginEntries) {
        this.holdGeneratedOriginEntries = holdGeneratedOriginEntries;
        this.generatedOriginEntries = new ArrayList<OriginEntryFull>();
    }

    /**
     * Returns the total number of balances for the previous fiscal year
     * 
     * @return the total number of balances for the previous fiscal year
     */
    public int getBalancesRead() {
        return reversionCounts.get("balancesRead").intValue();
    }

    /**
     * Returns the total number of balances selected for inclusion in this process
     * 
     * @return the total number of balances selected for inclusion in this process
     */
    public int getBalancesSelected() {
        return reversionCounts.get("balancesSelected").intValue();
    }
    
    public List<OriginEntryFull> getGeneratedOriginEntries() {
        return generatedOriginEntries;
    }

    public int getRecordsWritten() {
        return reversionCounts.get("recordsWritten").intValue();
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    protected void incrementCount(String countName) {
        incrementCount(countName, 1);
    }

    protected void incrementCount(String countName, int increment) {
        Integer count = reversionCounts.get(countName);
        if (countName.equals("recordsWritten")) {
            int countAsInt = count.intValue();
            // add by 1, so we're guaranteed to hit the 1000th
            for (int i = 1; i <= increment; i++) {
                countAsInt += 1;
                if (countAsInt % 1000 == 0) {
                    LOG.info(" ORIGIN ENTRIES INSERTED = "+countAsInt);
                } else if (countAsInt == 367471) {
                    LOG.info(" YOU HAVE ACHIEVED 367471 ORIGIN ENTRIES INSERTED!  TRIUMPH IS YOURS!  ");
                }
            }
            reversionCounts.put(countName, new Integer(countAsInt));
        } else {
            reversionCounts.put(countName, new Integer(count.intValue() + increment));
        }
    }

    protected  OriginEntryFull getEntry() {
        OriginEntryFull entry = new OriginEntryFull();
        entry.setUniversityFiscalYear((Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR));
        entry.setUniversityFiscalPeriodCode(KFSConstants.MONTH13);
        entry.setFinancialDocumentTypeCode(DEFAULT_FINANCIAL_DOCUMENT_TYPE_CODE);
        entry.setFinancialSystemOriginationCode(DEFAULT_FINANCIAL_SYSTEM_ORIGINATION_CODE);
        entry.setTransactionLedgerEntrySequenceNumber(1);
        entry.setTransactionDebitCreditCode(KFSConstants.GL_BUDGET_CODE);
        entry.setTransactionDate((Date) jobParameters.get(KFSConstants.TRANSACTION_DT));
        entry.setProjectCode(KFSConstants.getDashProjectCode());
        return entry;
    }
    
    public CuBalanceService getBalanceService() {
        return balanceService;
    }

    public void setBalanceService(CuBalanceService balanceService) {
        this.balanceService = balanceService;
    }

    public OriginEntryService getOriginEntryService() {
        return originEntryService;
    }

    public void setOriginEntryService(OriginEntryService originEntryService) {
        this.originEntryService = originEntryService;
    }

    public PersistenceService getPersistenceService() {
        return persistenceService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public PriorYearAccountService getPriorYearAccountService() {
        return priorYearAccountService;
    }

    public void setPriorYearAccountService(PriorYearAccountService priorYearAccountService) {
        this.priorYearAccountService = priorYearAccountService;
    }

    public FlexibleOffsetAccountService getFlexibleOffsetAccountService() {
        return flexibleOffsetAccountService;
    }

    public void setFlexibleOffsetAccountService(FlexibleOffsetAccountService flexibleOffsetAccountService) {
        this.flexibleOffsetAccountService = flexibleOffsetAccountService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public boolean isUsePriorYearInformation() {
        return usePriorYearInformation;
    }

    public void setUsePriorYearInformation(boolean endOfYear) {
        this.usePriorYearInformation = endOfYear;
    }

    public String getBatchFileDirectoryName() {
        return batchFileDirectoryName;
    }

    public void setBatchFileDirectoryName(String batchFileDirectoryName) {
        this.batchFileDirectoryName = batchFileDirectoryName;
    }

    public void setReversionCounts(Map<String, Integer> reversionCounts) {
        this.reversionCounts = reversionCounts;
    }
    
    /**
     * Gets the cashOrganizationReversionCategoryLogic attribute. 
     * @return Returns the cashOrganizationReversionCategoryLogic.
     */
    public ReversionCategoryLogic getCashReversionCategoryLogic() {
        return cashReversionCategoryLogic;
    }

    /**
     * Sets the cashOrganizationReversionCategoryLogic attribute value.
     * @param cashOrganizationReversionCategoryLogic The cashOrganizationReversionCategoryLogic to set.
     */
    public void setCashReversionCategoryLogic(ReversionCategoryLogic cashReversionCategoryLogic) {
        this.cashReversionCategoryLogic = cashReversionCategoryLogic;
    }

    protected void calculateBucketAmounts(Balance bal) {
        getPersistenceService().retrieveReferenceObject(bal, "financialObject");
    
        if (LOG.isDebugEnabled()) {
            LOG.debug("CONSIDERING IF TO ADD BALANCE: " + bal.getUniversityFiscalYear() + bal.getChartOfAccountsCode() + bal.getAccountNumber() + bal.getSubAccountNumber() + bal.getObjectCode() + bal.getSubObjectCode() + bal.getBalanceTypeCode() + bal.getObjectTypeCode() + " " + bal.getAccountLineAnnualBalanceAmount().add(bal.getBeginningBalanceLineAmount()));
        }
        
       // Check here too...
        if (getCashReversionCategoryLogic().containsObjectCode(bal.getFinancialObject()) && bal.getBalanceTypeCode().equals(systemOptions.getActualFinancialBalanceTypeCd())) {
            if (bal.getObjectTypeCode().equals("LI")) {
            	unitOfWork.addTotalCash(bal.getBeginningBalanceLineAmount().negated());
                unitOfWork.addTotalCash(bal.getAccountLineAnnualBalanceAmount().negated());
            }
            else {
            	unitOfWork.addTotalCash(bal.getBeginningBalanceLineAmount());
            	unitOfWork.addTotalCash(bal.getAccountLineAnnualBalanceAmount());
            }
            incrementCount("balancesSelected");
            if (LOG.isDebugEnabled()) {
                LOG.debug("ADDING BALANCE TO CASH: " + bal.getUniversityFiscalYear() + bal.getChartOfAccountsCode() + bal.getAccountNumber() + bal.getSubAccountNumber() + bal.getObjectCode() + bal.getSubObjectCode() + bal.getBalanceTypeCode() + bal.getObjectTypeCode() + " " + bal.getAccountLineAnnualBalanceAmount().add(bal.getBeginningBalanceLineAmount()) + " TO CASH, TOTAL CASH NOW = " + unitOfWork.getTotalCash());
            }
        }
//        else {
//            for (ReversionCategory cat : categoryList) {
//                ReversionCategoryLogic logic = categories.get(cat.getReversionCategoryCode());
//                if (logic.containsObjectCode(bal.getFinancialObject())) {
//                    if (systemOptions.getActualFinancialBalanceTypeCd().equals(bal.getBalanceTypeCode())) {
//                        // Actual
//                        unitOfWork.addActualAmount(cat.getReversionCategoryCode(), bal.getBeginningBalanceLineAmount());
//                        unitOfWork.addActualAmount(cat.getReversionCategoryCode(), bal.getAccountLineAnnualBalanceAmount());
//                        incrementCount("balancesSelected");
//                       // if (LOG.isDebugEnabled()) {
//                            LOG.info("ADDING BALANCE TO ACTUAL: " + bal.getUniversityFiscalYear() + bal.getChartOfAccountsCode() + bal.getAccountNumber() + bal.getSubAccountNumber() + bal.getObjectCode() + bal.getSubObjectCode() + bal.getBalanceTypeCode() + bal.getObjectTypeCode() + " " + bal.getAccountLineAnnualBalanceAmount().add(bal.getBeginningBalanceLineAmount()) + " TO ACTUAL, ACTUAL FOR CATEGORY " + cat.getReversionCategoryName() + " NOW = " + unitOfWork.getCategoryAmounts().get(cat.getReversionCategoryCode()).getActual());
//                        //}
//                    }
//                    else if (systemOptions.getFinObjTypeExpenditureexpCd().equals(bal.getBalanceTypeCode()) || systemOptions.getCostShareEncumbranceBalanceTypeCd().equals(bal.getBalanceTypeCode()) || systemOptions.getIntrnlEncumFinBalanceTypCd().equals(bal.getBalanceTypeCode())) {
//                        // Encumbrance
//                        KualiDecimal amount = bal.getBeginningBalanceLineAmount().add(bal.getAccountLineAnnualBalanceAmount());
//                        if (amount.isPositive()) {
//                            unitOfWork.addEncumbranceAmount(cat.getReversionCategoryCode(), amount);
//                            incrementCount("balancesSelected");
//                            if (LOG.isDebugEnabled()) {
//                                LOG.debug("ADDING BALANCE TO ENCUMBRANCE: " + bal.getUniversityFiscalYear() + bal.getChartOfAccountsCode() + bal.getAccountNumber() + bal.getSubAccountNumber() + bal.getObjectCode() + bal.getSubObjectCode() + bal.getBalanceTypeCode() + bal.getObjectTypeCode() + " " + bal.getAccountLineAnnualBalanceAmount().add(bal.getBeginningBalanceLineAmount()) + " TO ENCUMBRANCE, ENCUMBRANCE FOR CATEGORY " + cat.getReversionCategoryName() + " NOW = " + unitOfWork.getCategoryAmounts().get(cat.getReversionCategoryCode()).getEncumbrance());
//                            }
//                        }
//                    }
//                    else if (KFSConstants.BALANCE_TYPE_CURRENT_BUDGET.equals(bal.getBalanceTypeCode())) {
//                        // Budget
//                        if (!CARRY_FORWARD_OBJECT_CODE.equals(bal.getObjectCode())) {
//                            unitOfWork.addBudgetAmount(cat.getReversionCategoryCode(), bal.getBeginningBalanceLineAmount());
//                            unitOfWork.addBudgetAmount(cat.getReversionCategoryCode(), bal.getAccountLineAnnualBalanceAmount());
//                            incrementCount("balancesSelected");
//                            if (LOG.isDebugEnabled()) {
//                                LOG.debug("ADDING BALANCE TO BUDGET: " + bal.getUniversityFiscalYear() + bal.getChartOfAccountsCode() + bal.getAccountNumber() + bal.getSubAccountNumber() + bal.getObjectCode() + bal.getSubObjectCode() + bal.getBalanceTypeCode() + bal.getObjectTypeCode() + " " + bal.getAccountLineAnnualBalanceAmount().add(bal.getBeginningBalanceLineAmount()) + " TO CURRENT BUDGET, CURRENT BUDGET FOR CATEGORY " + cat.getReversionCategoryName() + " NOW = " + unitOfWork.getCategoryAmounts().get(cat.getReversionCategoryCode()).getBudget());
//                            }
//                        }
//                    }
//                    break;
//                }
//            }
//        }
    }

    public List<OriginEntryFull> generateOutputOriginEntries() throws FatalErrorException {
        List<OriginEntryFull> originEntriesToWrite = new ArrayList<OriginEntryFull>();
        if (unitOfWork.getTotalReversion().compareTo(KualiDecimal.ZERO) != 0) {
            generateReversions(originEntriesToWrite);
        }
        if ((unitOfWork.getTotalCarryForward().compareTo(KualiDecimal.ZERO) != 0)) {
            if (!cfReversionProcessInfo.isCarryForwardByObjectCodeIndicator()) {
                generateCarryForwards(originEntriesToWrite);
            }
            else {
                generateMany(originEntriesToWrite);
            }
        }
        if (unitOfWork.getTotalCash().compareTo(KualiDecimal.ZERO) != 0) {
            generateCashReversions(originEntriesToWrite);
        }
        return originEntriesToWrite;
    }

    public void generateCashReversions(List<OriginEntryFull> originEntriesToWrite) throws FatalErrorException {
        int entriesWritten = 0;
        
        // Reversion of cash from the actual account in the fiscal year ending (balance type of NB)
        OriginEntryFull entry = getEntry();
        entry.refreshReferenceObject("option");
    
        entry.setChartOfAccountsCode(unitOfWork.chartOfAccountsCode);
        entry.setAccountNumber(unitOfWork.accountNumber);
        entry.setSubAccountNumber(unitOfWork.subAccountNumber);
        entry.setFinancialObjectCode(cfReversionProcessInfo.getCashReversionChartCashObjectCode());
        entry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        entry.setFinancialBalanceTypeCode(DEFAULT_FINANCIAL_BALANCE_TYPE_CODE);
    
        getPersistenceService().retrieveReferenceObject(entry, KFSPropertyConstants.FINANCIAL_OBJECT);
        if (ObjectUtils.isNull(entry.getFinancialObject())) {
            throw new FatalErrorException("Object Code for Entry not found: " + entry);
        }
    
        entry.setDocumentNumber(DEFAULT_DOCUMENT_NUMBER_PREFIX + entry.getAccountNumber());
        entry.setTransactionLedgerEntryDescription(CASH_REVERTED_TO_MESSAGE + " " + cfReversionProcessInfo.getCashReversionAccountNumber());
        entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalCash());
        if (unitOfWork.getTotalCash().compareTo(KualiDecimal.ZERO) > 0) {
            entry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
        }
        else {
            entry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalCash().negated());
        }
        entry.setFinancialObjectTypeCode(entry.getFinancialObject().getFinancialObjectTypeCode());
    
        // 3468 MOVE TRN-LDGR-ENTR-AMT TO WS-AMT-W-PERIOD
        // 3469 WS-AMT-N.
        // 3470 MOVE WS-AMT-X TO TRN-AMT-RED-X.
    
        originEntriesToWrite.add(entry);
    
        // Reversion of fund balance, starting with the actual account, to match the cash that was reverted (balance type of NB) 
        entry = getEntry();
        entry.setChartOfAccountsCode(unitOfWork.chartOfAccountsCode);
        entry.setAccountNumber(unitOfWork.accountNumber);
        entry.setSubAccountNumber(unitOfWork.subAccountNumber);
        entry.setFinancialObjectCode((String) jobParameters.get(CUKFSConstants.CASH_REVERSION_OBJECT_CD));
        entry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        entry.setFinancialBalanceTypeCode(DEFAULT_FINANCIAL_BALANCE_TYPE_CODE);
    
        getPersistenceService().retrieveReferenceObject(entry, KFSPropertyConstants.FINANCIAL_OBJECT);
        if (ObjectUtils.isNull(entry.getFinancialObject())) {
            throw new FatalErrorException("Object Code for Entry not found: " + entry);
        }
    
        entry.setDocumentNumber(DEFAULT_DOCUMENT_NUMBER_PREFIX + unitOfWork.accountNumber);
        entry.setTransactionLedgerEntryDescription(FUND_BALANCE_REVERTED_TO_MESSAGE + cfReversionProcessInfo.getCashReversionAccountNumber());
        entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalCash().abs());
        if (unitOfWork.getTotalCash().compareTo(KualiDecimal.ZERO) > 0) {
            entry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
        }
        else {
            entry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
        }
        entry.setFinancialObjectTypeCode(entry.getFinancialObject().getFinancialObjectTypeCode());
    
        // 3570 MOVE TRN-LDGR-ENTR-AMT TO WS-AMT-W-PERIOD
        // 3571 WS-AMT-N.
        // 3572 MOVE WS-AMT-X TO TRN-AMT-RED-X.
    
        getFlexibleOffsetAccountService().updateOffset(entry);
        originEntriesToWrite.add(entry);
    
        // Reversion of cash to the cash reversion account in the fiscal year ending (balance type of NB)
        entry = getEntry();
        entry.setChartOfAccountsCode(cfReversionProcessInfo.getCashReversionFinancialChartOfAccountsCode());
        entry.setAccountNumber(cfReversionProcessInfo.getCashReversionAccountNumber());
        entry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
        entry.setFinancialObjectCode(cfReversionProcessInfo.getCashReversionChartCashObjectCode());
        entry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        entry.setFinancialBalanceTypeCode(DEFAULT_FINANCIAL_BALANCE_TYPE_CODE);
    
        getPersistenceService().retrieveReferenceObject(entry, KFSPropertyConstants.FINANCIAL_OBJECT);
        if (ObjectUtils.isNull(entry.getFinancialObject())) {
            throw new FatalErrorException("Object Code for Entry not found: " + entry);
        }
    
        entry.setDocumentNumber(DEFAULT_DOCUMENT_NUMBER_PREFIX + unitOfWork.accountNumber);
        entry.setTransactionLedgerEntryDescription(CASH_REVERTED_FROM_MESSAGE + unitOfWork.accountNumber + " " + unitOfWork.subAccountNumber);
        entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalCash());
        if (unitOfWork.getTotalCash().compareTo(KualiDecimal.ZERO) > 0) {
            entry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
        }
        else {
            entry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
            entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalCash().negated());
        }
        entry.setFinancialObjectTypeCode(entry.getFinancialObject().getFinancialObjectTypeCode());
    
        // 3668 MOVE TRN-LDGR-ENTR-AMT TO WS-AMT-W-PERIOD
        // 3669 WS-AMT-N.
        // 3670 MOVE WS-AMT-X TO TRN-AMT-RED-X.
    
        originEntriesToWrite.add(entry);
    
        // Reversion of fund balance, starting with the cash reversion account, to match the cash that was reverted (balance type of NB) 
        entry = getEntry();
        entry.setChartOfAccountsCode(cfReversionProcessInfo.getCashReversionFinancialChartOfAccountsCode());
        entry.setAccountNumber(cfReversionProcessInfo.getCashReversionAccountNumber());
        entry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
        entry.setFinancialObjectCode((String) jobParameters.get(CUKFSConstants.CASH_REVERSION_OBJECT_CD));
        entry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        entry.setFinancialBalanceTypeCode(DEFAULT_FINANCIAL_BALANCE_TYPE_CODE);
    
        getPersistenceService().retrieveReferenceObject(entry, KFSPropertyConstants.FINANCIAL_OBJECT);
        if (ObjectUtils.isNull(entry.getFinancialObject())) {
            throw new FatalErrorException("Object Code for Entry not found: " + entry);
        }
    
        entry.setDocumentNumber(DEFAULT_DOCUMENT_NUMBER_PREFIX + unitOfWork.accountNumber);
        entry.setTransactionLedgerEntryDescription(FUND_BALANCE_REVERTED_FROM_MESSAGE + unitOfWork.accountNumber + " " + unitOfWork.subAccountNumber);
        entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalCash());
        if (unitOfWork.getTotalCash().compareTo(KualiDecimal.ZERO) > 0) {
            entry.setTransactionDebitCreditCode(KFSConstants.GL_CREDIT_CODE);
        }
        else {
            entry.setTransactionDebitCreditCode(KFSConstants.GL_DEBIT_CODE);
            entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalCash().negated());
        }
        entry.setFinancialObjectTypeCode(entry.getFinancialObject().getFinancialObjectTypeCode());
    
        // 3768 MOVE TRN-LDGR-ENTR-AMT TO WS-AMT-W-PERIOD
        // 3769 WS-AMT-N.
        // 3770 MOVE WS-AMT-X TO TRN-AMT-RED-X.
        
        getFlexibleOffsetAccountService().updateOffset(entry);
        originEntriesToWrite.add(entry);
    }

    public void generateMany(List<OriginEntryFull> originEntriesToWrite) throws FatalErrorException {
        int originEntriesCreated = 0;
        for (Iterator<ReversionCategory> iter = categoryList.iterator(); iter.hasNext();) {
            ReversionCategory cat = iter.next();
            ReversionCategoryInfo detail = cfReversionProcessInfo.getReversionDetail(cat.getReversionCategoryCode());
            ReversionUnitOfWorkCategoryAmount amount = unitOfWork.amounts.get(cat.getReversionCategoryCode());
    
            if (!amount.getCarryForward().isZero()) {
                KualiDecimal commonAmount = amount.getCarryForward();
                String commonObject = detail.getReversionObjectCode();
    
                OriginEntryFull entry = getEntry();
                entry.setUniversityFiscalYear((Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR) + 1);
                entry.setChartOfAccountsCode(unitOfWork.chartOfAccountsCode);
                entry.setAccountNumber(unitOfWork.accountNumber);
                entry.setSubAccountNumber(unitOfWork.subAccountNumber);
                entry.setFinancialObjectCode((String) jobParameters.get(KFSConstants.BEG_BUD_CASH_OBJECT_CD));
                entry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
                entry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_CURRENT_BUDGET);
    
                getPersistenceService().retrieveReferenceObject(entry, KFSPropertyConstants.FINANCIAL_OBJECT);
                if (ObjectUtils.isNull(entry.getFinancialObject())) {
                    throw new FatalErrorException("Object Code for Entry not found: " + entry);
                }
    
                ObjectCode objectCode = entry.getFinancialObject();
                entry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
                entry.setUniversityFiscalPeriodCode(KFSConstants.MONTH1);
                entry.setDocumentNumber(DEFAULT_DOCUMENT_NUMBER_PREFIX + unitOfWork.accountNumber);
                entry.setTransactionLedgerEntryDescription(FUND_CARRIED_MESSAGE + (Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR));
                entry.setTransactionLedgerEntryAmount(commonAmount);
    
                // 3259 MOVE TRN-LDGR-ENTR-AMT TO WS-AMT-W-PERIOD
                // 3260 WS-AMT-N.
                // 3261 MOVE WS-AMT-X TO TRN-AMT-RED-X.
    
                originEntriesToWrite.add(entry);
    
                entry = getEntry();
                entry.setUniversityFiscalYear((Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR) + 1);
                entry.setChartOfAccountsCode(unitOfWork.chartOfAccountsCode);
                entry.setAccountNumber(unitOfWork.accountNumber);
                entry.setSubAccountNumber(unitOfWork.subAccountNumber);
    
                entry.setFinancialObjectCode(commonObject);
                entry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
                entry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_CURRENT_BUDGET);
    
                getPersistenceService().retrieveReferenceObject(entry, KFSPropertyConstants.FINANCIAL_OBJECT);
                if (ObjectUtils.isNull(entry.getFinancialObject())) {
                    throw new FatalErrorException("Object Code for Entry not found: " + entry);
                }
    
                objectCode = entry.getFinancialObject();
                entry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
                entry.setUniversityFiscalPeriodCode(KFSConstants.MONTH1);
                entry.setDocumentNumber(DEFAULT_DOCUMENT_NUMBER_PREFIX + unitOfWork.accountNumber);
                entry.setTransactionLedgerEntryDescription(FUND_CARRIED_MESSAGE + (Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR));
                entry.setTransactionLedgerEntryAmount(commonAmount);
    
                // 3343 MOVE TRN-LDGR-ENTR-AMT TO WS-AMT-W-PERIOD
                // 3344 WS-AMT-N.
                // 3345 MOVE WS-AMT-X TO TRN-AMT-RED-X.
    
                originEntriesToWrite.add(entry);
            }
        }
    }

    public void generateCarryForwards(List<OriginEntryFull> originEntriesToWrite) throws FatalErrorException {
        int originEntriesWritten = 0;
    
        OriginEntryFull entry = getEntry();
        entry.setUniversityFiscalYear((Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR) + 1);
        entry.setChartOfAccountsCode(unitOfWork.chartOfAccountsCode);
        entry.setAccountNumber(unitOfWork.accountNumber);
        entry.setSubAccountNumber(unitOfWork.subAccountNumber);
        entry.setFinancialObjectCode((String) jobParameters.get(KFSConstants.BEG_BUD_CASH_OBJECT_CD));
        entry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        entry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_CURRENT_BUDGET);
    
        getPersistenceService().retrieveReferenceObject(entry, KFSPropertyConstants.FINANCIAL_OBJECT);
        if (ObjectUtils.isNull(entry.getFinancialObject())) {
            throw new FatalErrorException("Object Code for Entry not found: " + entry);
        }
    
        ObjectCode objectCode = entry.getFinancialObject();
        entry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
        entry.setUniversityFiscalPeriodCode(KFSConstants.MONTH1);
        entry.setFinancialDocumentTypeCode(DEFAULT_FINANCIAL_DOCUMENT_TYPE_CODE);
        entry.setFinancialSystemOriginationCode(DEFAULT_FINANCIAL_SYSTEM_ORIGINATION_CODE);
        entry.setDocumentNumber(DEFAULT_DOCUMENT_NUMBER_PREFIX + unitOfWork.accountNumber);
        entry.setTransactionLedgerEntrySequenceNumber(1);
        entry.setTransactionLedgerEntryDescription(FUND_CARRIED_MESSAGE + (Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR));
        entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalCarryForward());
        entry.setTransactionDate((Date) jobParameters.get(KFSConstants.TRANSACTION_DT));
        entry.setProjectCode(KFSConstants.getDashProjectCode());
        // 2995 MOVE TRN-LDGR-ENTR-AMT TO WS-AMT-W-PERIOD
        // 2996 WS-AMT-N.
        // 2997 MOVE WS-AMT-X TO TRN-AMT-RED-X.
    
        originEntriesToWrite.add(entry);
    
        entry = getEntry();
        entry.setUniversityFiscalYear((Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR) + 1);
        entry.setChartOfAccountsCode(unitOfWork.chartOfAccountsCode);
        entry.setAccountNumber(unitOfWork.accountNumber);
        entry.setSubAccountNumber(unitOfWork.subAccountNumber);
        entry.setFinancialObjectCode((String) jobParameters.get(KFSConstants.UNALLOC_OBJECT_CD));
    
        getPersistenceService().retrieveReferenceObject(entry, KFSPropertyConstants.FINANCIAL_OBJECT);
        if (ObjectUtils.isNull(entry.getFinancialObject())) {
            throw new FatalErrorException("Object Code for Entry not found: " + entry);
        }
    
        objectCode = entry.getFinancialObject();
        entry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
        entry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        entry.setFinancialBalanceTypeCode(KFSConstants.BALANCE_TYPE_CURRENT_BUDGET);
        entry.setUniversityFiscalPeriodCode(KFSConstants.MONTH1);
        entry.setDocumentNumber(DEFAULT_DOCUMENT_NUMBER_PREFIX + unitOfWork.accountNumber);
        entry.setTransactionLedgerEntryDescription(FUND_CARRIED_MESSAGE + (Integer) jobParameters.get(KFSConstants.UNIV_FISCAL_YR));
        entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalCarryForward());
    
        // 3079 MOVE TRN-LDGR-ENTR-AMT TO WS-AMT-W-PERIOD
        // 3080 WS-AMT-N.
        // 3081 MOVE WS-AMT-X TO TRN-AMT-RED-X.
    
        originEntriesToWrite.add(entry);
    
    }

    public void generateReversions(List<OriginEntryFull> originEntriesToWrite) throws FatalErrorException {
        int originEntriesWritten = 0;
    
        OriginEntryFull entry = getEntry();
        entry.setChartOfAccountsCode(unitOfWork.chartOfAccountsCode);
        entry.setAccountNumber(unitOfWork.accountNumber);
        entry.setSubAccountNumber(unitOfWork.subAccountNumber);
        entry.setFinancialObjectCode((String) jobParameters.get(KFSConstants.UNALLOC_OBJECT_CD));
        entry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        entry.setFinancialBalanceTypeCode(DEFAULT_FINANCIAL_BALANCE_TYPE_CODE_YEAR_END);
    
        getPersistenceService().retrieveReferenceObject(entry, KFSPropertyConstants.FINANCIAL_OBJECT);
        if (ObjectUtils.isNull(entry.getFinancialObject())) {
            throw new FatalErrorException("Object Code for Entry not found: " + entry);
        }
    
        ObjectCode objectCode = entry.getFinancialObject();
        entry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
    
        entry.setUniversityFiscalPeriodCode(KFSConstants.MONTH13);
    
        entry.setDocumentNumber(DEFAULT_DOCUMENT_NUMBER_PREFIX + entry.getAccountNumber());
    
        entry.setTransactionLedgerEntryDescription(FUND_REVERTED_TO_MESSAGE + cfReversionProcessInfo.getBudgetReversionAccountNumber());
        entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalReversion().negated());
    
        originEntriesToWrite.add(entry);
    
        entry = getEntry();
        entry.setChartOfAccountsCode(cfReversionProcessInfo.getBudgetReversionChartOfAccountsCode());
        entry.setAccountNumber(cfReversionProcessInfo.getBudgetReversionAccountNumber());
        entry.setSubAccountNumber(KFSConstants.getDashSubAccountNumber());
        entry.setFinancialObjectCode((String) jobParameters.get(KFSConstants.UNALLOC_OBJECT_CD));
        entry.setFinancialSubObjectCode(KFSConstants.getDashFinancialSubObjectCode());
        entry.setFinancialBalanceTypeCode(DEFAULT_FINANCIAL_BALANCE_TYPE_CODE_YEAR_END);
        entry.setFinancialObjectTypeCode(objectCode.getFinancialObjectTypeCode());
        entry.setUniversityFiscalPeriodCode(KFSConstants.MONTH13);
        entry.setDocumentNumber(DEFAULT_DOCUMENT_NUMBER_PREFIX + unitOfWork.accountNumber);
        if (unitOfWork.accountNumber.equals(KFSConstants.getDashSubAccountNumber())) {
            entry.setTransactionLedgerEntryDescription(FUND_REVERTED_FROM_MESSAGE + unitOfWork.accountNumber);
        }
        else {
            entry.setTransactionLedgerEntryDescription(FUND_REVERTED_FROM_MESSAGE + unitOfWork.accountNumber + " " + unitOfWork.subAccountNumber);
        }
        entry.setTransactionLedgerEntryAmount(unitOfWork.getTotalReversion());
    
        // 2899 MOVE TRN-LDGR-ENTR-AMT TO WS-AMT-W-PERIOD
        // 2900 WS-AMT-N.
        // 2901 MOVE WS-AMT-X TO TRN-AMT-RED-X.
    
        originEntriesToWrite.add(entry);
    }

    public void calculateTotals() throws FatalErrorException {
        /*
         * How this works: At the start, in the clearCalculationTotals(), both the unit of work's totalAvailable and totalReversion
         * are set to the available amounts from each of the category amounts. Then, as the logic is applied, the totalCarryForward
         * is added to and the totalReversion is subtracted from. Let's look at a simple example: Let's say you've got an amount for
         * C01, which has $2000 available, no encumbrances, that's all you've got. This means that at the end of
         * clearCalculationTotals(), there's $2000 in totalAvailable, $2000 in totalReversion, and $0 in totalCarryForward. Now, C01,
         * let's say, is for code A. So, look below at the if that catches Code A. You'll note that it adds the available amount to
         * totalCarryForward, it's own carryForward, the negated available to totalReversion, and that, done, it sets available to
         * $0. With our example, that means that $2000 is in totalCarryForward (and in the amount's carryForward), the
         * totalReversion has been knocked down to $0, and the available is $0. So, carry forward origin entries get created, and
         * reversions do not. This is also why you don't see a block about calculating R2 totals below...the process has a natural
         * inclination towards creating R2 (ie, ignore encumbrances and revert all available) entries.
         */
    
        // clear out the unit of work totals we're going to calculate values in, in preperation for applying rules
        clearCalculationTotals();
    
        // For each category, apply the rules
        for (ReversionCategory category : categoryList) {
            String categoryCode = category.getReversionCategoryCode();
            ReversionCategoryLogic logic = categories.get(categoryCode);
            ReversionUnitOfWorkCategoryAmount amount = unitOfWork.amounts.get(categoryCode);
    
            ReversionCategoryInfo detail = cfReversionProcessInfo.getReversionDetail(categoryCode);
    
            if (detail == null) {
                throw new FatalErrorException(" Reversion " + cfReversionProcessInfo.getUniversityFiscalYear() + "-" + cfReversionProcessInfo.getChartOfAccountsCode() + "-" + cfReversionProcessInfo.getSourceAttribute() + " does not have a detail for category " + categoryCode);
            }
            String ruleCode = detail.getReversionCode();
    
            //if (LOG.isDebugEnabled()) {
                LOG.info("Unit of Work: " + unitOfWork.getChartOfAccountsCode() + unitOfWork.getAccountNumber() + unitOfWork.getSubAccountNumber() + ", category " + category.getReversionCategoryName() + ": budget = " + amount.getBudget() + "; actual = " + amount.getActual() + "; encumbrance = " + amount.getEncumbrance() + "; available = " + amount.getAvailable() + "; apply rule code " + ruleCode);
           // }
    
                
                //xe
            if (KFSConstants.RULE_CODE_R1.equals(ruleCode) || KFSConstants.RULE_CODE_N1.equals(ruleCode) || KFSConstants.RULE_CODE_C1.equals(ruleCode)) {
                if (amount.getAvailable().compareTo(KualiDecimal.ZERO) > 0) { // do we have budget left?
                    if (amount.getAvailable().compareTo(amount.getEncumbrance()) > 0) { // is it more than enough to cover our
                        // encumbrances?
                        unitOfWork.addTotalCarryForward(amount.getEncumbrance());
                        amount.addCarryForward(amount.getEncumbrance());
                        unitOfWork.addTotalReversion(amount.getEncumbrance().negated());
                        amount.addAvailable(amount.getEncumbrance().negated());
                    }
                    else {
                        // there's not enough available left to cover the encumbrances; cover what we can
                        unitOfWork.addTotalCarryForward(amount.getAvailable());
                        amount.addCarryForward(amount.getAvailable());
                        unitOfWork.addTotalReversion(amount.getAvailable().negated());
                        amount.setAvailable(KualiDecimal.ZERO);
                    }
                }
            }
    
            
            //Check this in the debugger to see if this is the right amt to get..
            if (CUKFSConstants.RULE_CODE_CA.equals(ruleCode)) {
            	
            	//just gonna break this right here...amount.
//            	unitOfWork.addTotalCash(amount.getAvailable());
//                amount.addActual(amount.getAvailable());
//            	unitOfWork.addTotalReversion(amount.getAvailable().negated());
//                amount.setAvailable(KualiDecimal.ZERO);
            }
            //xa
            if (KFSConstants.RULE_CODE_A.equals(ruleCode)) {
                unitOfWork.addTotalCarryForward(amount.getAvailable());
                amount.addCarryForward(amount.getAvailable());
                unitOfWork.addTotalReversion(amount.getAvailable().negated());
                amount.setAvailable(KualiDecimal.ZERO);
            }
            //xp
            if (KFSConstants.RULE_CODE_C1.equals(ruleCode) || KFSConstants.RULE_CODE_C2.equals(ruleCode)) {
                if (amount.getAvailable().compareTo(KualiDecimal.ZERO) > 0) {
                    unitOfWork.addTotalCarryForward(amount.getAvailable());
                    amount.addCarryForward(amount.getAvailable());
                    unitOfWork.addTotalReversion(amount.getAvailable().negated());
                    amount.setAvailable(KualiDecimal.ZERO);
                }
            }
            //xn
            if (KFSConstants.RULE_CODE_N1.equals(ruleCode) || KFSConstants.RULE_CODE_N2.equals(ruleCode)) {
                if (amount.getAvailable().compareTo(KualiDecimal.ZERO) < 0) {
                    unitOfWork.addTotalCarryForward(amount.getAvailable());
                    amount.addCarryForward(amount.getAvailable());
                    unitOfWork.addTotalReversion(amount.getAvailable().negated());
                    amount.setAvailable(KualiDecimal.ZERO);
                }
            }
    
            if (LOG.isDebugEnabled()) {
                LOG.debug("Totals Now: " + unitOfWork.getChartOfAccountsCode() + unitOfWork.getAccountNumber() + unitOfWork.getSubAccountNumber() + ", total cash now " + unitOfWork.getTotalCash() + ": total available = " + unitOfWork.getTotalAvailable() + "; total reversion = " + unitOfWork.getTotalReversion() + "; total carry forward = " + unitOfWork.getTotalCarryForward());
            }
        }
    }

    protected void clearCalculationTotals() {
        // Initialize all the amounts before applying the proper rule
        KualiDecimal totalAvailable = KualiDecimal.ZERO;
        for (ReversionCategory category : categoryList) {
            ReversionCategoryLogic logic = categories.get(category.getReversionCategoryCode());
    
            ReversionUnitOfWorkCategoryAmount amount = unitOfWork.amounts.get(category.getReversionCategoryCode());
            if (logic.isExpense()) {
                amount.setAvailable(amount.getBudget().subtract(amount.getActual()));
            }
            else {
                amount.setAvailable(amount.getActual().subtract(amount.getBudget()));
            }
            totalAvailable = totalAvailable.add(amount.getAvailable());
            amount.setCarryForward(KualiDecimal.ZERO);
        }
        unitOfWork.setTotalAvailable(totalAvailable);
        unitOfWork.setTotalReversion(totalAvailable);
        unitOfWork.setTotalCarryForward(KualiDecimal.ZERO);
    }

    protected void summarizeOriginEntries(List<OriginEntryFull> originEntries) {
        for (OriginEntryFull originEntry: originEntries) {
            ledgerReport.summarizeEntry(originEntry);
        }
    }

    public ReversionUnitOfWork getUnitOfWork() {
        return unitOfWork;
    }

    public <T extends ReversionUnitOfWork> void setUnitOfWork(T unitOfWork) {
        this.unitOfWork = unitOfWork;
    }

    public List<ReversionCategory> getCategoryList() {
        return this.categoryList;
    }

    public void writeLedgerSummaryReport(ReportWriterService reportWriterService) {
        ledgerReport.writeReport(reportWriterService);
    }
    
    /**
     * Sets the jobParameters attribute value.
     * @param jobParameters The jobParameters to set.
     */
    public void setJobParameters(Map jobParameters) {
        this.jobParameters = jobParameters;
    }

}