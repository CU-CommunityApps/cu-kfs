package edu.cornell.kfs.coa.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.util.KeyLabelPair;
import org.kuali.rice.kim.bo.Person;
import org.kuali.rice.kns.lookup.keyvalues.KeyValuesBase;
import org.kuali.rice.kns.service.KNSServiceLocator;
import org.kuali.rice.kns.service.KeyValuesService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.GlobalVariables;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class CUSimpleChartValuesFinder extends KeyValuesBase {

	 protected ParameterService parameterService;
	
    /**
     * Creates a list of {@link Chart}s using their code as their key, and their code as the display value
     * 
     * @see org.kuali.rice.kns.lookup.keyvalues.KeyValuesFinder#getKeyValues()
     */
    public List getKeyValues() {
        parameterService = KNSServiceLocator.getParameterService();
        String defaultChartCode = "";
        String defaultChartCodeMethod = "";
        
        try {
        	defaultChartCode = parameterService.getParameterValue(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, CUKFSParameterKeyConstants.DEFAULT_CHART_CODE);
        	defaultChartCodeMethod = parameterService.getParameterValue(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, CUKFSParameterKeyConstants.DEFAULT_CHART_CODE_METHOD);
        } catch (Exception e) {
        	//Do nothing
        }
        KeyValuesService boService = SpringContext.getBean(KeyValuesService.class);
        Collection chartCodes = boService.findAll(Chart.class);
        List chartKeyLabels = new ArrayList();

        //If the DEFAULT_CHART_CODE_METHOD parameter DNE or has no value assigned to it, no default
        if (defaultChartCodeMethod.equals("")) { 
	        chartKeyLabels.add(new KeyLabelPair("", ""));
	        for (Iterator iter = chartCodes.iterator(); iter.hasNext();) {
	            Chart element = (Chart) iter.next();
	            if (element.isActive()) { // only show active charts
	                chartKeyLabels.add(new KeyLabelPair(element.getChartOfAccountsCode(), element.getChartOfAccountsCode()));
	            }
	        }
        }
        //populate with the default chart
        if (defaultChartCodeMethod.equals("1")) {
	        chartKeyLabels.add(new KeyLabelPair(defaultChartCode, defaultChartCode));
	        for (Iterator iter = chartCodes.iterator(); iter.hasNext();) {
	            Chart element = (Chart) iter.next();
	            if (element.isActive() && !element.getChartOfAccountsCode().equals(defaultChartCode)) { // only show active charts
	                chartKeyLabels.add(new KeyLabelPair(element.getChartOfAccountsCode(), element.getChartOfAccountsCode()));
	            }
	        }
        }
        //populate with chart code of the user's primary department
        if (defaultChartCodeMethod.equals("2")) {
        	Person currentUser = GlobalVariables.getUserSession().getPerson();
        	String primaryDepartmentChartCode = SpringContext.getBean(FinancialSystemUserService.class).getPrimaryOrganization(currentUser, "KFS-SYS").getChartOfAccountsCode();
        	chartKeyLabels.add(new KeyLabelPair(primaryDepartmentChartCode, primaryDepartmentChartCode));
	        for (Iterator iter = chartCodes.iterator(); iter.hasNext();) {
	            Chart element = (Chart) iter.next();
	            if (element.isActive() && !element.getChartOfAccountsCode().equals(primaryDepartmentChartCode)) { // only show active charts
	                chartKeyLabels.add(new KeyLabelPair(element.getChartOfAccountsCode(), element.getChartOfAccountsCode()));
	            }
	        }
        }
        //populate with the default chart unless user's primary department has been defined
        if (defaultChartCodeMethod.equals("3")) {
        	Person currentUser = GlobalVariables.getUserSession().getPerson();
        	String primaryDepartmentChartCode = SpringContext.getBean(FinancialSystemUserService.class).getPrimaryOrganization(currentUser, "KFS-SYS").getChartOfAccountsCode();
        	String chartUsed = null;
        	if (primaryDepartmentChartCode!= null && !primaryDepartmentChartCode.equals("")) {
            	chartUsed = primaryDepartmentChartCode;        		
        	} else {
        		chartUsed = defaultChartCode;
        	}
	        chartKeyLabels.add(new KeyLabelPair(chartUsed, chartUsed));
	        for (Iterator iter = chartCodes.iterator(); iter.hasNext();) {
	            Chart element = (Chart) iter.next();
	            if (element.isActive() && !element.getChartOfAccountsCode().equals(chartUsed)) { // only show active charts
	                chartKeyLabels.add(new KeyLabelPair(element.getChartOfAccountsCode(), element.getChartOfAccountsCode()));
	            }
	        }
        }
        
        return chartKeyLabels;
    }

	
}
