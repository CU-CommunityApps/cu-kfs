package edu.cornell.kfs.gl.batch.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.gl.batch.BalanceForwardRuleHelper;
import org.kuali.kfs.gl.batch.service.impl.OriginEntryOffsetPair;
import org.kuali.kfs.gl.batch.service.impl.YearEndServiceImpl;
import org.kuali.kfs.gl.batch.service.impl.exception.FatalErrorException;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.kfs.gl.businessobject.Encumbrance;
import org.kuali.kfs.gl.businessobject.OriginEntryFull;
import org.kuali.kfs.gl.report.LedgerSummaryReport;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;

import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.gl.batch.CuBalanceForwardRuleHelper;
import edu.cornell.kfs.gl.batch.CuNominalActivityClosingHelper;
import edu.cornell.kfs.gl.batch.dataaccess.CuYearEndDao;
import edu.cornell.kfs.gl.batch.service.CuYearEndService;
import edu.cornell.kfs.gl.dataaccess.CuEncumbranceDao;
import edu.cornell.kfs.gl.service.CuBalanceService;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CuYearEndServiceImpl extends YearEndServiceImpl implements CuYearEndService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuYearEndServiceImpl.class);

    @Override
    public void closeNominalActivity(String nominalClosingFileName, Map nominalClosingJobParameters) {

        Integer varFiscalYear = (Integer) nominalClosingJobParameters.get(GeneralLedgerConstants.ColumnNames.UNIVERSITY_FISCAL_YEAR);
        CuNominalActivityClosingHelper closingHelper = new CuNominalActivityClosingHelper(
                varFiscalYear, (Date) nominalClosingJobParameters.get(GeneralLedgerConstants.ColumnNames.UNIV_DT), 
                parameterService, configurationService);
        
        closingHelper.addNominalClosingJobParameters(nominalClosingJobParameters);

        Map<String, Integer> nominalActivityClosingCounts = new HashMap<String, Integer>();
        
        Iterator<Balance> balanceIterator = null;
        if (closingHelper.isAnnualClosingChartParamterBlank()) {
            //execute delivered foundation code, either ANNUAL_CLOSING_CHARTS parameter did not exist or there were no values specified
            nominalActivityClosingCounts.put("globalReadCount", new Integer(balanceService.countBalancesForFiscalYear(varFiscalYear)));
            balanceIterator = balanceService.findNominalActivityBalancesForFiscalYear(varFiscalYear);           
        } else {
            //ANNUAL_CLOSING_CHARTS parameter was detected and contained values
            nominalActivityClosingCounts.put("globalReadCount", new Integer(((CuBalanceService) balanceService).countBalancesForFiscalYear(
                    varFiscalYear,  (Collection<String>) nominalClosingJobParameters.get(GeneralLedgerConstants.ColumnNames.CHART_OF_ACCOUNTS_CODE))));
            balanceIterator = ((CuBalanceService) balanceService).findNominalActivityBalancesForFiscalYear(
                    varFiscalYear, (Collection<String>) nominalClosingJobParameters.get(GeneralLedgerConstants.ColumnNames.CHART_OF_ACCOUNTS_CODE));  
        }
        
        String accountNumberHold = null;

        nominalActivityClosingCounts.put("globalSelectCount", new Integer(0));
        nominalActivityClosingCounts.put("sequenceNumber", new Integer(0));
        nominalActivityClosingCounts.put("sequenceWriteCount", new Integer(0));
        nominalActivityClosingCounts.put("sequenceCheckCount", new Integer(0));

        //create files
        File nominalClosingFile = new File(batchFileDirectoryName + File.separator + nominalClosingFileName);
        PrintStream nominalClosingPs = null;
        
        try {
            nominalClosingPs = new PrintStream(nominalClosingFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("nominalClosingFile Files doesn't exist " + nominalClosingFileName);
        }
        
        LedgerSummaryReport ledgerReport = new LedgerSummaryReport();
        
        while (balanceIterator.hasNext()) {

            Balance balance = balanceIterator.next();
            balance.refreshReferenceObject("option");

            try {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Balance selected.");
                }
                if (balance.getAccountNumber().equals(accountNumberHold)) {
                    incrementCount(nominalActivityClosingCounts, "sequenceNumber");
                } else {
                    nominalActivityClosingCounts.put("sequenceNumber", new Integer(1));
                }
                incrementCount(nominalActivityClosingCounts, "globalSelectCount");
                OriginEntryFull activityEntry = closingHelper.generateActivityEntry(balance, new Integer(1));
                originEntryService.createEntry(activityEntry, nominalClosingPs);
                ledgerReport.summarizeEntry(activityEntry);
                incrementCount(nominalActivityClosingCounts, "sequenceWriteCount");
                nominalActivityClosingCounts.put("sequenceCheckCount", new Integer(nominalActivityClosingCounts.get("sequenceWriteCount").intValue()));
                if (0 == nominalActivityClosingCounts.get("sequenceCheckCount").intValue() % 1000) {
                    LOG.info(new StringBuffer("  SEQUENTIAL RECORDS WRITTEN = ").append(nominalActivityClosingCounts.get("sequenceCheckCount")).toString());
                }
                OriginEntryFull offsetEntry = closingHelper.generateOffset(balance, new Integer(1));           
                originEntryService.createEntry(offsetEntry, nominalClosingPs);
                ledgerReport.summarizeEntry(offsetEntry);
                incrementCount(nominalActivityClosingCounts, "sequenceWriteCount");
                nominalActivityClosingCounts.put("sequenceCheckCount", new Integer(nominalActivityClosingCounts.get("sequenceWriteCount").intValue()));
                if (0 == nominalActivityClosingCounts.get("sequenceCheckCount").intValue() % 1000) {
                    LOG.info(new StringBuffer(" ORIGIN ENTRIES INSERTED = ").append(
                            nominalActivityClosingCounts.get("sequenceCheckCount")).toString());
                }
                if (nominalActivityClosingCounts.get("globalSelectCount").intValue() % 1000 == 0) {
                //    persistenceService.clearCache();
                }
                accountNumberHold = balance.getAccountNumber();
            } catch (FatalErrorException fee) {
                LOG.warn("Failed to create entry pair for balance.", fee);
            }
        }
        nominalActivityClosingCounts.put("nonFatalCount", closingHelper.getNonFatalErrorCount());
        nominalClosingPs.close();
        
        // now write parameters
        for (Object jobParameterKeyAsObject : nominalClosingJobParameters.keySet()) {
            if (jobParameterKeyAsObject != null) {
                final String jobParameterKey = jobParameterKeyAsObject.toString();
                getNominalActivityClosingReportWriterService().writeParameterLine("%32s %10s", jobParameterKey, nominalClosingJobParameters.get(jobParameterKey));
            }
        }
        
        // now write statistics
        getNominalActivityClosingReportWriterService().writeStatisticLine("NUMBER OF GLBL RECORDS READ       %9d", nominalActivityClosingCounts.get("globalReadCount"));
        getNominalActivityClosingReportWriterService().writeStatisticLine("NUMBER OF GLBL RECORDS SELECTED   %9d", nominalActivityClosingCounts.get("globalSelectCount"));
        getNominalActivityClosingReportWriterService().writeStatisticLine("NUMBER OF SEQ RECORDS WRITTEN     %9d", nominalActivityClosingCounts.get("sequenceWriteCount"));
        getNominalActivityClosingReportWriterService().pageBreak();
        
        // finally, put a header on the ledger report and write it
        getNominalActivityClosingReportWriterService().writeSubTitle(configurationService.getPropertyValueAsString(KFSKeyConstants.MESSAGE_REPORT_YEAR_END_NOMINAL_ACTIVITY_CLOSING_LEDGER_TITLE_LINE));
        ledgerReport.writeReport(getNominalActivityClosingReportWriterService());
    }

    @Override
    public void forwardBalances(String balanceForwardsUnclosedFileName, String balanceForwardsclosedFileName, BalanceForwardRuleHelper balanceForwardRuleHelperIn) {
        LOG.debug("forwardBalances() started");
        CuBalanceForwardRuleHelper balanceForwardRuleHelper = (CuBalanceForwardRuleHelper) balanceForwardRuleHelperIn;
        
        // The rule helper maintains the state of the overall processing of the entire
        // set of year end balances. This state is available via balanceForwardRuleHelper.getState().
        // The helper and this class (YearEndServiceImpl) are heavily dependent upon one
        // another in terms of expected behavior and shared responsibilities.
        balanceForwardRuleHelper.setPriorYearAccountService(priorYearAccountService);
        balanceForwardRuleHelper.setSubFundGroupService(subFundGroupService);
        balanceForwardRuleHelper.setOriginEntryService(originEntryService);
        balanceForwardRuleHelper.getState().setGlobalReadCount(balanceService.countBalancesForFiscalYear(balanceForwardRuleHelper.getClosingFiscalYear()));
        
        if (balanceForwardRuleHelper.isAnnualClosingChartParamterBlank()) {
            //execute delivered foundation code, either ANNUAL_CLOSING_CHARTS parameter did not exist or there were no values specified
            balanceForwardRuleHelper.getState().setGlobalReadCount(balanceService.countBalancesForFiscalYear(balanceForwardRuleHelper.getClosingFiscalYear()));
        } else {
            //ANNUAL_CLOSING_CHARTS parameter was detected and contained values
            balanceForwardRuleHelper.getState().setGlobalReadCount(((CuBalanceService) balanceService)
                    .countBalancesForFiscalYear(balanceForwardRuleHelper.getClosingFiscalYear(), balanceForwardRuleHelper.getAnnualClosingCharts()));
        }
        Balance balance;

        //create files
        File unclosedOutputFile = new File(batchFileDirectoryName + File.separator + balanceForwardsUnclosedFileName);
        File closedOutputFile = new File(batchFileDirectoryName + File.separator + balanceForwardsclosedFileName);
        PrintStream unclosedPs = null;
        PrintStream closedPs = null;
        
        try {
            unclosedPs = new PrintStream(unclosedOutputFile);
            closedPs = new PrintStream(closedOutputFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("balanceForwards Files don't exist " + balanceForwardsUnclosedFileName + " and " + balanceForwardsclosedFileName);
        }
        
        // do the general forwards
        Iterator<Balance> generalBalances = balanceService.findGeneralBalancesToForwardForFiscalYear(balanceForwardRuleHelper.getClosingFiscalYear());
        if (balanceForwardRuleHelper.isAnnualClosingChartParamterBlank()) {
            //execute delivered foundation code, either ANNUAL_CLOSING_CHARTS parameter did not exist or there were no values specified
            generalBalances = balanceService.findGeneralBalancesToForwardForFiscalYear(balanceForwardRuleHelper.getClosingFiscalYear());
            LOG.info("doing general forwards for fiscal year");
        } else {
            //ANNUAL_CLOSING_CHARTS parameter was detected and contained values
            generalBalances = ((CuBalanceService) balanceService).findGeneralBalancesToForwardForFiscalYear(
                    balanceForwardRuleHelper.getClosingFiscalYear(), balanceForwardRuleHelper.getAnnualClosingCharts());
            LOG.info("doing general forwards for fiscal year and charts");
        }  
        
        while (generalBalances.hasNext()) {
            balance = generalBalances.next();
            balanceForwardRuleHelper.processGeneralForwardBalance(balance, closedPs, unclosedPs);
            if (balanceForwardRuleHelper.getState().getGlobalSelectCount() % 1000 == 0) {
              //  persistenceService.clearCache();
            }
        }

        // do the cumulative forwards
        Iterator<Balance> cumulativeBalances = balanceService.findCumulativeBalancesToForwardForFiscalYear(balanceForwardRuleHelper.getClosingFiscalYear());
        if (balanceForwardRuleHelper.isAnnualClosingChartParamterBlank()) {
            //execute delivered foundation code, either ANNUAL_CLOSING_CHARTS parameter did not exist or there were no values specified
            cumulativeBalances = balanceService.findCumulativeBalancesToForwardForFiscalYear(balanceForwardRuleHelper.getClosingFiscalYear());
            LOG.info("doing cumulative forwards for fiscal year");
        } else {
            //ANNUAL_CLOSING_CHARTS parameter was detected and contained values
            cumulativeBalances = ((CuBalanceService) balanceService).findCumulativeBalancesToForwardForFiscalYear(
                    balanceForwardRuleHelper.getClosingFiscalYear(), balanceForwardRuleHelper.getAnnualClosingCharts());
            LOG.info("doing cumulative forwards for fiscal year and charts");
        } 
        
        while (cumulativeBalances.hasNext()) {
            balance = cumulativeBalances.next();
            balanceForwardRuleHelper.processCumulativeForwardBalance(balance, closedPs, unclosedPs);
            if (balanceForwardRuleHelper.getState().getGlobalSelectCount() % 1000 == 0) {
            //    persistenceService.clearCache();
            }
        }

        // write parameters
        getBalanceForwardReportWriterService().writeParameterLine("%32s %10s", GeneralLedgerConstants.ANNUAL_CLOSING_TRANSACTION_DATE_PARM, balanceForwardRuleHelper.getTransactionDate().toString());
        getBalanceForwardReportWriterService().writeParameterLine("%32s %10s", GeneralLedgerConstants.ANNUAL_CLOSING_FISCAL_YEAR_PARM, balanceForwardRuleHelper.getClosingFiscalYear().toString());
        getBalanceForwardReportWriterService().writeParameterLine("%32s %10s", CuGeneralLedgerConstants.ANNUAL_CLOSING_CHARTS_PARAM, balanceForwardRuleHelper.getAnnualClosingCharts().toString());
        getBalanceForwardReportWriterService().writeParameterLine("%32s %10s", KFSConstants.SystemGroupParameterNames.GL_ANNUAL_CLOSING_DOC_TYPE, balanceForwardRuleHelper.getAnnualClosingDocType());
        getBalanceForwardReportWriterService().writeParameterLine("%32s %10s", KFSConstants.SystemGroupParameterNames.GL_ORIGINATION_CODE, balanceForwardRuleHelper.getGlOriginationCode());
        
        // write statistics
        getBalanceForwardReportWriterService().writeStatisticLine("NUMBER OF GLBL RECORDS READ....: %10d", balanceForwardRuleHelper.getState().getGlobalReadCount());
        getBalanceForwardReportWriterService().writeStatisticLine("NUMBER OF GLBL RECORDS SELECTED: %10d", balanceForwardRuleHelper.getState().getGlobalSelectCount());
        getBalanceForwardReportWriterService().writeStatisticLine("NUMBER OF SEQ RECORDS WRITTEN..: %10d", balanceForwardRuleHelper.getState().getSequenceWriteCount());
        getBalanceForwardReportWriterService().writeStatisticLine("RECORDS FOR CLOSED ACCOUNTS....: %10d", balanceForwardRuleHelper.getState().getSequenceClosedCount());
        getBalanceForwardReportWriterService().pageBreak();
        
        // write ledger reports
        getBalanceForwardReportWriterService().writeSubTitle(configurationService.getPropertyValueAsString(KFSKeyConstants.MESSAGE_REPORT_YEAR_END_BALANCE_FORWARD_OPEN_ACCOUNT_LEDGER_TITLE_LINE));
        balanceForwardRuleHelper.writeOpenAccountBalanceForwardLedgerSummaryReport(getBalanceForwardReportWriterService());
        getBalanceForwardReportWriterService().writeNewLines(4);
        getBalanceForwardReportWriterService().writeSubTitle(configurationService.getPropertyValueAsString(KFSKeyConstants.MESSAGE_REPORT_YEAR_END_BALANCE_FORWARD_CLOSED_ACCOUNT_LEDGER_TITLE_LINE));
        balanceForwardRuleHelper.writeClosedAccountBalanceForwardLedgerSummaryReport(getBalanceForwardReportWriterService());
    }

    public void forwardEncumbrances(String encumbranceForwardFileName, Map jobParameters, Map<String, Integer> counts) {
        LOG.debug("forwardEncumbrances() started");

        // counters for the report
        counts.put("encumbrancesRead", new Integer(0));
        counts.put("encumbrancesSelected", new Integer(0));
        counts.put("originEntriesWritten", new Integer(0));
        
        LedgerSummaryReport forwardEncumbranceLedgerReport = new LedgerSummaryReport();

        //create files
        File encumbranceForwardFile = new File(batchFileDirectoryName + File.separator + encumbranceForwardFileName);
        PrintStream encumbranceForwardPs = null;
        
        try {
            encumbranceForwardPs = new PrintStream(encumbranceForwardFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("forwardEncumbrances Files doesn't exist " + encumbranceForwardFileName);
        }
        
        //values from ANNUAL_CLOSING_CHARTS parameter, parameter may not be defined(execute foundation code) or may not have values 
        //specified(execute foundation code) or may be defined with values specified(execute Cornell mod)
        List<String> charts = (List<String>) jobParameters.get(GeneralLedgerConstants.ColumnNames.CHART_OF_ACCOUNTS_CODE);
        Iterator encumbranceIterator;
        if (charts.isEmpty()) {
            //execute delivered foundation code
            // encumbranceDao will return all encumbrances for the fiscal year sorted properly by all of the appropriate keys.
            encumbranceIterator = encumbranceDao.getEncumbrancesToClose((Integer) jobParameters.get(GeneralLedgerConstants.ColumnNames.UNIVERSITY_FISCAL_YEAR));
        } else {          
            // encumbranceDao will return all encumbrances for the fiscal year and specified charts sorted properly by all of the appropriate keys.
            encumbranceIterator = ((CuEncumbranceDao) encumbranceDao).getEncumbrancesToClose(
                    (Integer) jobParameters.get(GeneralLedgerConstants.ColumnNames.UNIVERSITY_FISCAL_YEAR), 
                    (Collection<String>) jobParameters.get(GeneralLedgerConstants.ColumnNames.CHART_OF_ACCOUNTS_CODE));
        }
        
        // encumbranceDao will return all encumbrances for the fiscal year sorted properly by all of the appropriate keys.
        while (encumbranceIterator.hasNext()) {

            Encumbrance encumbrance = (Encumbrance) encumbranceIterator.next();
            incrementCount(counts, "encumbrancesRead");
            
            // if the encumbrance is not completely relieved
            if (getEncumbranceClosingOriginEntryGenerationService().shouldForwardEncumbrance(encumbrance)) {

                incrementCount(counts, "encumbrancesSelected");

                // build a pair of origin entries to carry forward the encumbrance.
                OriginEntryOffsetPair beginningBalanceEntryPair = getEncumbranceClosingOriginEntryGenerationService()
                        .createBeginningBalanceEntryOffsetPair(encumbrance, (Integer) 
                                jobParameters.get(GeneralLedgerConstants.ColumnNames.UNIVERSITY_FISCAL_YEAR), (Date) 
                                jobParameters.get(GeneralLedgerConstants.ColumnNames.UNIV_DT));

                if (beginningBalanceEntryPair.isFatalErrorFlag()) {

                    continue;

                } else {
                    // save the entries.
                    originEntryService.createEntry(beginningBalanceEntryPair.getEntry(), encumbranceForwardPs);
                    forwardEncumbranceLedgerReport.summarizeEntry(beginningBalanceEntryPair.getEntry());
                    originEntryService.createEntry(beginningBalanceEntryPair.getOffset(), encumbranceForwardPs);
                    forwardEncumbranceLedgerReport.summarizeEntry(beginningBalanceEntryPair.getOffset());
                    incrementCount(counts, "originEntriesWritten");
                    incrementCount(counts, "originEntriesWritten");
                    if (0 == counts.get("originEntriesWritten").intValue() % 1000) {
                        LOG.info(new StringBuffer(" ORIGIN ENTRIES INSERTED = ").append(counts.get("originEntriesWritten")).toString());
                    }
                }

                // handle cost sharing if appropriate.
                boolean isEligibleForCostShare = false;
                try {
                    isEligibleForCostShare = this.getEncumbranceClosingOriginEntryGenerationService()
                            .shouldForwardCostShareForEncumbrance(beginningBalanceEntryPair.getEntry(), 
                             beginningBalanceEntryPair.getOffset(), encumbrance, beginningBalanceEntryPair.getEntry().getFinancialObjectTypeCode());
                } catch (FatalErrorException fee) {
                    LOG.info(fee.getMessage());
                }

                if (isEligibleForCostShare) {
                    // build and save an additional pair of origin entries to carry forward the encumbrance.
                    OriginEntryOffsetPair costShareBeginningBalanceEntryPair = getEncumbranceClosingOriginEntryGenerationService()
                            .createCostShareBeginningBalanceEntryOffsetPair(encumbrance, (Date) 
                                    jobParameters.get(GeneralLedgerConstants.ColumnNames.UNIV_DT));
                    if (!costShareBeginningBalanceEntryPair.isFatalErrorFlag()) {
                        // save the cost share entries.
                        originEntryService.createEntry(costShareBeginningBalanceEntryPair.getEntry(), encumbranceForwardPs);
                        forwardEncumbranceLedgerReport.summarizeEntry(costShareBeginningBalanceEntryPair.getEntry());
                        originEntryService.createEntry(costShareBeginningBalanceEntryPair.getOffset(), encumbranceForwardPs);
                        forwardEncumbranceLedgerReport.summarizeEntry(costShareBeginningBalanceEntryPair.getOffset());
                        incrementCount(counts, "originEntriesWritten");
                        incrementCount(counts, "originEntriesWritten");
                        if (0 == counts.get("originEntriesWritten").intValue() % 1000) {
                            LOG.info(new StringBuffer(" ORIGIN ENTRIES INSERTED = ").append(counts.get("originEntriesWritten")).toString());
                        }
                    }
                }
            }
            if (counts.get("encumbrancesSelected").intValue() % 1000 == 0) {
             //   persistenceService.clearCache();
            }
        }
        encumbranceForwardPs.close();
        
        // write job parameters
        for (Object jobParameterKeyAsObject : jobParameters.keySet()) {
            if (jobParameterKeyAsObject != null) {
                final String jobParameterKey = jobParameterKeyAsObject.toString();
                getEncumbranceClosingReportWriterService().writeParameterLine("%32s %10s", jobParameterKey, jobParameters.get(jobParameterKey));
            }
        }
        
        // write statistics
        getEncumbranceClosingReportWriterService().writeStatisticLine("NUMBER OF ENCUMBRANCE RECORDS READ:     %10d", counts.get("encumbrancesRead"));
        getEncumbranceClosingReportWriterService().writeStatisticLine("NUMBER OF ENCUMBRANCE RECORDS SELECTED  %10d", counts.get("encumbrancesSelected"));
        getEncumbranceClosingReportWriterService().writeStatisticLine("NUMBER OF SEQ RECORDS WRITTEN           %10d", counts.get("originEntriesWritten"));
        getEncumbranceClosingReportWriterService().pageBreak();
        
        // write ledger summary report
        getEncumbranceClosingReportWriterService().writeSubTitle(configurationService.getPropertyValueAsString(
                KFSKeyConstants.MESSAGE_REPORT_YEAR_END_ENCUMBRANCE_FORWARDS_LEDGER_TITLE_LINE));
        forwardEncumbranceLedgerReport.writeReport(getEncumbranceClosingReportWriterService());
    }

    @Override
    public void logAllMissingPriorYearAccounts(Integer fiscalYear,
            Collection<String> charts) {
        Set<Map<String, String>> missingPriorYearAccountKeys = 
                ((CuYearEndDao) yearEndDao).findKeysOfMissingPriorYearAccountsForBalances(fiscalYear, charts);
        missingPriorYearAccountKeys.addAll(
                ((CuYearEndDao) yearEndDao).findKeysOfMissingPriorYearAccountsForOpenEncumbrances(fiscalYear, charts));
        for (Map<String, String> key : missingPriorYearAccountKeys) {
            LOG.info("PRIOR YEAR ACCOUNT MISSING FOR " + key.get("chartOfAccountsCode") + "-" + key.get("accountNumber"));
        }        
    }

    @Override
    public void logAllMissingSubFundGroups(Integer fiscalYear,
            Collection<String> charts) {
        Set<Map<String, String>> missingSubFundGroupKeys = 
                ((CuYearEndDao) yearEndDao).findKeysOfMissingSubFundGroupsForBalances(fiscalYear, charts);
        missingSubFundGroupKeys.addAll(
                ((CuYearEndDao) yearEndDao).findKeysOfMissingSubFundGroupsForOpenEncumbrances(fiscalYear, charts));
        for (Map<String, String> key : missingSubFundGroupKeys) {
            LOG.info("SUB FUND GROUP MISSING FOR " + (String) key.get("subFundGroupCode"));
        }        
    }

    
}
