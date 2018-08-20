package edu.cornell.kfs.coa.businessobject.options;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.kfs.krad.keyvalues.KeyValuesBase;
import org.kuali.kfs.krad.service.KeyValuesService;
import org.kuali.kfs.krad.util.GlobalVariables;

import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

@SuppressWarnings("serial")
public class CUSimpleChartValuesFinder extends KeyValuesBase {

	 protected ParameterService parameterService;
	
    /**
     * Creates a list of {@link Chart}s using their code as their key, and their code as the display value
     */
    public List<KeyValue> getKeyValues() {
        parameterService = SpringContext.getBean(ParameterService.class);
        String defaultChartCode = "";
        String defaultChartCodeMethod = "";
        
        try {
        	defaultChartCode = parameterService.getParameterValueAsString(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, CUKFSParameterKeyConstants.DEFAULT_CHART_CODE);
        	defaultChartCodeMethod = parameterService.getParameterValueAsString(KfsParameterConstants.FINANCIAL_SYSTEM_DOCUMENT.class, CUKFSParameterKeyConstants.DEFAULT_CHART_CODE_METHOD);
        } catch (Exception e) {
        	//Do nothing
        }
        KeyValuesService boService = SpringContext.getBean(KeyValuesService.class);
        Collection<Chart> chartCodes = boService.findAll(Chart.class);
        List<KeyValue> chartKeyLabels = new ArrayList<KeyValue>();

        //If the DEFAULT_CHART_CODE_METHOD parameter DNE or has no value assigned to it, no default
        if (defaultChartCodeMethod.equals("")) { 
	        chartKeyLabels.add(new ConcreteKeyValue("", ""));
	        for (Iterator<Chart> iter = chartCodes.iterator(); iter.hasNext();) {
	            Chart element = (Chart) iter.next();
	            if (element.isActive()) { // only show active charts
	                chartKeyLabels.add(new ConcreteKeyValue(element.getChartOfAccountsCode(), element.getChartOfAccountsCode()));
	            }
	        }
        }
        //populate with the default chart
        if (defaultChartCodeMethod.equals("1")) {
	        chartKeyLabels.add(new ConcreteKeyValue(defaultChartCode, defaultChartCode));
	        for (Iterator<Chart> iter = chartCodes.iterator(); iter.hasNext();) {
	            Chart element = (Chart) iter.next();
	            if (element.isActive() && !element.getChartOfAccountsCode().equals(defaultChartCode)) { // only show active charts
	                chartKeyLabels.add(new ConcreteKeyValue(element.getChartOfAccountsCode(), element.getChartOfAccountsCode()));
	            }
	        }
        }
        //populate with chart code of the user's primary department
        if (defaultChartCodeMethod.equals("2")) {
        	Person currentUser = GlobalVariables.getUserSession().getPerson();
        	String primaryDepartmentChartCode = SpringContext.getBean(FinancialSystemUserService.class).getPrimaryOrganization(currentUser, "KFS-SYS").getChartOfAccountsCode();
        	chartKeyLabels.add(new ConcreteKeyValue(primaryDepartmentChartCode, primaryDepartmentChartCode));
	        for (Iterator<Chart> iter = chartCodes.iterator(); iter.hasNext();) {
	            Chart element = (Chart) iter.next();
	            if (element.isActive() && !element.getChartOfAccountsCode().equals(primaryDepartmentChartCode)) { // only show active charts
	                chartKeyLabels.add(new ConcreteKeyValue(element.getChartOfAccountsCode(), element.getChartOfAccountsCode()));
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
	        chartKeyLabels.add(new ConcreteKeyValue(chartUsed, chartUsed));
	        for (Iterator<Chart> iter = chartCodes.iterator(); iter.hasNext();) {
	            Chart element = (Chart) iter.next();
	            if (element.isActive() && !element.getChartOfAccountsCode().equals(chartUsed)) { // only show active charts
	                chartKeyLabels.add(new ConcreteKeyValue(element.getChartOfAccountsCode(), element.getChartOfAccountsCode()));
	            }
	        }
        }
        
        return chartKeyLabels;
    }

	
}
