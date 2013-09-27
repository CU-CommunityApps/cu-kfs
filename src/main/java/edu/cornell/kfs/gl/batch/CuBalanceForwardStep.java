package edu.cornell.kfs.gl.batch;

import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.kuali.kfs.gl.GeneralLedgerConstants;
import org.kuali.kfs.sys.batch.AbstractWrappedBatchStep;
import org.kuali.kfs.sys.batch.service.WrappedBatchExecutorService.CustomBatchExecutor;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.springframework.util.StopWatch;

import edu.cornell.kfs.gl.batch.service.CuYearEndService;

@NAMESPACE(namespace = KfsParameterConstants.GENERAL_LEDGER_NAMESPACE)
@COMPONENT(component = KfsParameterConstants.BATCH_COMPONENT)
public class CuBalanceForwardStep extends AbstractWrappedBatchStep {
    public static final String TRANSACTION_DATE_FORMAT_STRING = "yyyy-MM-dd";

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuBalanceForwardStep.class);
    private CuYearEndService yearEndService;
    
    @Override
    protected CustomBatchExecutor getCustomBatchExecutor() {
        return new CustomBatchExecutor() {
            /**
             * This step runs the balance forward service, specifically finding the parameters the job needs, creating the origin entry
             * groups for the output origin entries, and creating the process's reports.
             * @return that the job finished successfully
             * @see org.kuali.kfs.sys.batch.Step#execute(String, java.util.Date)
             */
            public boolean execute() {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("Balance Forward Step");

                Date varTransactionDate;
                try {
                    DateFormat transactionDateFormat = new SimpleDateFormat(TRANSACTION_DATE_FORMAT_STRING);
                    varTransactionDate = new Date(transactionDateFormat.parse(
                            getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, 
                                    GeneralLedgerConstants.ANNUAL_CLOSING_TRANSACTION_DATE_PARM)).getTime());
                } catch (ParseException e) {
                    LOG.error("forwardBalances() Unable to parse transaction date", e);
                    throw new IllegalArgumentException("Unable to parse transaction date");
                }

                Integer varFiscalYear = new Integer(getParameterService().getParameterValueAsString(
                        KfsParameterConstants.GENERAL_LEDGER_BATCH.class, GeneralLedgerConstants.ANNUAL_CLOSING_FISCAL_YEAR_PARM));

                String balanceForwardsUnclosedFileName = GeneralLedgerConstants.BatchFileSystem.BALANCE_FORWARDS_FILE
                        + GeneralLedgerConstants.BatchFileSystem.EXTENSION; 
                String balanceForwardsclosedFileName = GeneralLedgerConstants.BatchFileSystem.BALANCE_FORWARDS_CLOSED_FILE 
                        + GeneralLedgerConstants.BatchFileSystem.EXTENSION;
                
                CuBalanceForwardRuleHelper balanceForwardRuleHelper = new CuBalanceForwardRuleHelper(varFiscalYear, 
                        varTransactionDate, balanceForwardsclosedFileName, balanceForwardsUnclosedFileName);
                if (balanceForwardRuleHelper.isAnnualClosingChartParamterBlank()) {
                    yearEndService.logAllMissingPriorYearAccounts(varFiscalYear);
                    yearEndService.logAllMissingSubFundGroups(varFiscalYear);
                } else {
                    //ANNUAL_CLOSING_CHARTS parameter was detected and contained values
                    yearEndService.logAllMissingPriorYearAccounts(varFiscalYear, balanceForwardRuleHelper.getAnnualClosingCharts());
                    yearEndService.logAllMissingSubFundGroups(varFiscalYear, balanceForwardRuleHelper.getAnnualClosingCharts());                            
                    
                }
                
                yearEndService.forwardBalances(balanceForwardsUnclosedFileName, balanceForwardsclosedFileName, balanceForwardRuleHelper);

                stopWatch.stop();
                LOG.info("Balance Forward Step took " + (stopWatch.getTotalTimeSeconds() / 60.0) + " minutes to complete");

                return true;
            }
        };
    }
    
    public void setYearEndService(CuYearEndService yearEndService) {
        this.yearEndService = yearEndService;
    }
}
