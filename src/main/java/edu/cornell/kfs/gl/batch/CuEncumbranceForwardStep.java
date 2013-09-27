package edu.cornell.kfs.gl.batch;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.sys.batch.AbstractWrappedBatchStep;
import org.kuali.kfs.sys.batch.service.WrappedBatchExecutorService.CustomBatchExecutor;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.springframework.util.StopWatch;

import edu.cornell.kfs.gl.CuGeneralLedgerConstants;
import edu.cornell.kfs.gl.batch.service.CuYearEndService;

@NAMESPACE(namespace = KfsParameterConstants.GENERAL_LEDGER_NAMESPACE)
@COMPONENT(component = KfsParameterConstants.BATCH_COMPONENT)
public class CuEncumbranceForwardStep extends AbstractWrappedBatchStep {
    public static final String TRANSACTION_DATE_FORMAT_STRING = "yyyy-MM-dd";
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuEncumbranceForwardStep.class);
    
    private CuYearEndService yearEndService;


    /**
     * @see org.kuali.kfs.sys.batch.AbstractWrappedBatchStep#getCustomBatchExecutor()
     */
    @Override
    protected CustomBatchExecutor getCustomBatchExecutor() {
        return new CustomBatchExecutor() {
            /**
             * This step runs the forward encumbrance process, including retrieving the parameters needed to run the job, creating the
             * origin entry group where output origin entries will go, and having the job's reports generated.
             * 
             * @return true if the step completed successfully, false if otherwise
             * @see org.kuali.kfs.sys.batch.Step#performStep()
             */
            public boolean execute() {
                StopWatch stopWatch = new StopWatch();
                stopWatch.start("EncumbranceForwardStep");

                Map<String, Object> jobParameters = new HashMap<String, Object>();
                Integer varFiscalYear = null;
                Date varTransactionDate = null;
                List<String> varCharts = null;

                // Get the current fiscal year.
                varFiscalYear = new Integer(getParameterService().getParameterValueAsString(
                        KfsParameterConstants.GENERAL_LEDGER_BATCH.class, CuGeneralLedgerConstants.ANNUAL_CLOSING_FISCAL_YEAR_PARM));

                // Get the current date (transaction date).
                try {
                    DateFormat transactionDateFormat = new SimpleDateFormat(TRANSACTION_DATE_FORMAT_STRING);
                    varTransactionDate = new Date(transactionDateFormat.parse(
                            getParameterService().getParameterValueAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, 
                                    CuGeneralLedgerConstants.ANNUAL_CLOSING_TRANSACTION_DATE_PARM)).getTime());
                } catch (ParseException pe) {
                    LOG.error("Failed to parse TRANSACTION_DT from kualiConfigurationService");
                    throw new RuntimeException("Unable to get transaction date from kualiConfigurationService", pe);
                }
                
                varCharts = new ArrayList<String>();
                try {
                    String[] varChartsArray = getParameterService().getParameterValuesAsString(
                            KfsParameterConstants.GENERAL_LEDGER_BATCH.class, CuGeneralLedgerConstants.ANNUAL_CLOSING_CHARTS_PARAM).toArray(new String[] {});
                    
                    if ((varChartsArray != null) && (varChartsArray.length != 0)) {
                        //transfer charts from parameter to List for database query         
                        for (String chartParam : varChartsArray) {
                            varCharts.add(chartParam);                          
                        }
                        LOG.info("EncumbranceForwardJob ANNUAL_CLOSING_CHARTS parameter value = " + varCharts.toString());
                    } else {
                        //Parameter existed but no values were listed.  Act on all charts which is the default action in the delivered foundation code.
                        LOG.info("ANNUAL_CLOSING_CHARTS parameter defined for KFS-GL Batch but no values "
                                + "were specified. All charts will be acted upon for EncumbranceForwardJob.");
                    }
                } catch (IllegalArgumentException e) {
                    //parameter is not defined, act on all charts per foundation delivered code   
                    LOG.info("ANNUAL_CLOSING_CHARTS parameter was not defined for KFS-GL Batch. "
                            + "All charts will be acted upon for EncumbranceForwardJob.");         
                } 

                jobParameters.put(CuGeneralLedgerConstants.ColumnNames.UNIVERSITY_FISCAL_YEAR, varFiscalYear);
                jobParameters.put(CuGeneralLedgerConstants.ColumnNames.UNIV_DT, varTransactionDate);
                jobParameters.put(CuGeneralLedgerConstants.ColumnNames.CHART_OF_ACCOUNTS_CODE, varCharts);

                String encumbranceForwardFileName = CuGeneralLedgerConstants.BatchFileSystem.ENCUMBRANCE_FORWARD_FILE 
                        + CuGeneralLedgerConstants.BatchFileSystem.EXTENSION;
                Map<String, Integer> forwardEncumbranceCounts = new HashMap<String, Integer>();

                yearEndService.forwardEncumbrances(encumbranceForwardFileName, jobParameters, forwardEncumbranceCounts);

                stopWatch.stop();
                LOG.info("EncumbranceForwardStep took " + (stopWatch.getTotalTimeSeconds() / 60.0) + " minutes to complete");

                return true;
            }
        };
    }

    /**
     * Sets the yearEndService attribute, allowing the injection of an implementation of that service
     * 
     * @param yearEndService the yearEndService to set
     * @see org.kuali.module.gl.service.YearEndService
     */
    public void setYearEndService(CuYearEndService yearEndService) {
        this.yearEndService = yearEndService;
    }
}
