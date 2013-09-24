package edu.cornell.kfs.gl.batch;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.gl.batch.NominalActivityClosingHelper;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.apache.log4j.Logger;

import edu.cornell.kfs.gl.CuGeneralLedgerConstants;

public class CuNominalActivityClosingHelper extends NominalActivityClosingHelper {
    private static final Logger LOG = Logger.getLogger(CuNominalActivityClosingHelper.class);

    protected List<String> varCharts;
    
    public CuNominalActivityClosingHelper(Integer fiscalYear, Date transactionDate, 
            ParameterService parameterService, ConfigurationService configurationService) {
        super(fiscalYear, transactionDate, parameterService, configurationService);
        
        //Obtain list of charts to close from Parameter ANNUAL_CLOSING_CHARTS_PARAM.
        //If no parameter value exists, act on all charts which is the default action in the delivered foundation code.
        varCharts = new ArrayList<String>();
        try {
            String[] varChartsArray = parameterService.getParameterValuesAsString(
                    KfsParameterConstants.GENERAL_LEDGER_BATCH.class, 
                            CuGeneralLedgerConstants.ANNUAL_CLOSING_CHARTS_PARAM).toArray(new String[] {});
            
            if ((varChartsArray != null) && (varChartsArray.length != 0)) {
                //transfer charts from parameter to List for ojb dao query          
                for (String chartParam : varChartsArray) {
                    varCharts.add(chartParam);                  
                }           
                LOG.info("NominalActivityClosingJob ANNUAL_CLOSING_CHARTS parameter value = " + varCharts.toString());
            } else {
                //Parameter existed but no values were listed.  Act on all charts which is the default action in the delivered foundation code.
                LOG.info("ANNUAL_CLOSING_CHARTS parameter defined for KFS-GL Batch but no values were specified. All charts will be acted "
                        + "upon for NominalActivityClosingJob.");
            }
        } catch (IllegalArgumentException e) {
            //parameter is not defined, act on all charts per foundation delivered code   
            LOG.info("ANNUAL_CLOSING_CHARTS parameter was not defined for KFS-GL Batch. All charts will be acted upon for NominalActivityClosingJob.");         
        }  
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void addNominalClosingJobParameters(Map nominalClosingJobParameters) {
        super.addNominalClosingJobParameters(nominalClosingJobParameters);
        nominalClosingJobParameters.put(CuGeneralLedgerConstants.ColumnNames.CHART_OF_ACCOUNTS_CODE, varCharts);
    }
    
    /**
     * Returns the boolean from the chart parameter list being empty
     * @return isEmpty boolean value for chart List
     */
    public boolean isAnnualClosingChartParamterBlank() {
        return varCharts.isEmpty(); 
    }

}
