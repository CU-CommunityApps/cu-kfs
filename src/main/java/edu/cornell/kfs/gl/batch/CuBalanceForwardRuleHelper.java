package edu.cornell.kfs.gl.batch;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.gl.batch.BalanceForwardRuleHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.gl.CuGeneralLedgerConstants;

public class CuBalanceForwardRuleHelper extends BalanceForwardRuleHelper {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuBalanceForwardRuleHelper.class);
    private List<String> annualClosingCharts;
    
    public CuBalanceForwardRuleHelper() {
        super();
        initiateAnnualClosingCharts();
    }
    
    public CuBalanceForwardRuleHelper(Integer closingFiscalYear) {
        super(closingFiscalYear);
        initiateAnnualClosingCharts();
    }
    public CuBalanceForwardRuleHelper(Integer varFiscalYear,
            Date varTransactionDate, String balanceForwardsclosedFileName,
            String balanceForwardsUnclosedFileName) {
        super(varFiscalYear, varTransactionDate, balanceForwardsclosedFileName,
                 balanceForwardsUnclosedFileName);
        initiateAnnualClosingCharts();
           
    }

    private void initiateAnnualClosingCharts() {
        //Obtain list of charts to for the balance forwarding from Parameter ANNUAL_CLOSING_CHARTS_PARAM.
        //If no parameter value exists, act on all charts which is the default action in the delivered foundation code.
        annualClosingCharts = new ArrayList<String>();
        try {
            String[] varChartsArray = SpringContext.getBean(ParameterService.class)
                    .getParameterValuesAsString(KfsParameterConstants.GENERAL_LEDGER_BATCH.class, 
                            CuGeneralLedgerConstants.ANNUAL_CLOSING_CHARTS_PARAM).toArray(new String[] {});
            
            if ((varChartsArray != null) && (varChartsArray.length != 0)) {
                //transfer charts from parameter to List for database query         
                for (String chartParam : varChartsArray) {
                    annualClosingCharts.add(chartParam);                    
                }        
                LOG.info("BalanceForwardJob ANNUAL_CLOSING_CHARTS parameter value = " + annualClosingCharts.toString());
            } else {
                //Parameter existed but no values were listed.  Act on all charts which is the default action in the delivered foundation code.
                LOG.info("ANNUAL_CLOSING_CHARTS parameter defined for KFS-GL Batch but no values were specified. "
                        + "All charts will be acted upon for BalanceForwardJob.");
            }
        } catch (IllegalArgumentException e) {
            //parameter is not defined, act on all charts per foundation delivered code   
            LOG.info("ANNUAL_CLOSING_CHARTS parameter was not defined for KFS-GL Batch. All charts will be acted upon for BalanceForwardJob.");         
        }   
    }

    public List<String> getAnnualClosingCharts() {
        return annualClosingCharts;
    }
    
    public boolean isAnnualClosingChartParamterBlank() {
        return annualClosingCharts.isEmpty();   
    }
}
