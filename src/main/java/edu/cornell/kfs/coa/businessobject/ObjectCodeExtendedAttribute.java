/**
 * 
 */
package edu.cornell.kfs.coa.businessobject;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.service.BusinessObjectService;

import edu.cornell.kfs.sys.businessobject.YearEndPersistableBusinessObjectExtensionBase;

/**
 * @author kwk43
 *
 */
public class ObjectCodeExtendedAttribute extends YearEndPersistableBusinessObjectExtensionBase {


	/**
	 * 
	 */
    private String chartOfAccountsCode;
    private String financialObjectCode;
    private String sunyObjectCode;
    private String cgReportingCode;
    private String financialObjectCodeDescr;
    
    private ContractGrantReportingCode contractGrantReportingCode;
    
    
    /*
     * Class constructor
     */
    public ObjectCodeExtendedAttribute() {
    	
    }
       
	/**
	 * @return the chartOfAccountsCode
	 */
	public String getChartOfAccountsCode() {
		return chartOfAccountsCode;
	}
	/**
	 * @param chartOfAccountsCode the chartOfAccountsCode to set
	 */
	public void setChartOfAccountsCode(String chartOfAccountsCode) {
		this.chartOfAccountsCode = chartOfAccountsCode;
	}
	/**
	 * @return the financialObjectCode
	 */
	public String getFinancialObjectCode() {
		return financialObjectCode;
	}
	/**
	 * @param financialObjectCode the financialObjectCode to set
	 */
	public void setFinancialObjectCode(String financialObjectCode) {
		this.financialObjectCode = financialObjectCode;
	}
	/**
	 * @return the sunyObjectCode
	 */
	public String getSunyObjectCode() {
		return sunyObjectCode;
	}
	/**
	 * @param sunyObjectCode the sunyObjectCode to set
	 */
	public void setSunyObjectCode(String sunyObjectCode) {
		this.sunyObjectCode = sunyObjectCode;
	}
	/**
	 * @return the financialObjectCodeDescr
	 */
	public String getFinancialObjectCodeDescr() {
		return financialObjectCodeDescr;
	}
	/**
	 * @param financialObjectCodeDescr the financialObjectCodeDescr to set
	 */
	public void setFinancialObjectCodeDescr(String financialObjectCodeDescr) {
		this.financialObjectCodeDescr = financialObjectCodeDescr;
	}
	/**
	 * @return the contractGrantReportingCode
	 */
	public ContractGrantReportingCode getContractGrantReportingCode() {
		return contractGrantReportingCode;
	}
	/**
	 * @param contractGrantReportingCode the contractGrantReportingCode to set
	 */
	public void setContractGrantReportingCode(ContractGrantReportingCode contractGrantReportingCode) {
		this.contractGrantReportingCode = contractGrantReportingCode;
	}
	/**
	 * @return the cgReportingCode
	 */
	public String getCgReportingCode() {
		return cgReportingCode;
	}
	/**
	 * @param cgReportingCode the cgReportingCode to set
	 */
	public void setCgReportingCode(String cgReportingCode) {		
		this.cgReportingCode = cgReportingCode;
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		HashMap<String,String> keys = new HashMap<String,String>();
		keys.put("chartOfAccountsCode", this.chartOfAccountsCode);
		//lookup table has class attribute defined as "code"
	    keys.put("code", this.cgReportingCode);
		contractGrantReportingCode = (ContractGrantReportingCode) bos.findByPrimaryKey(ContractGrantReportingCode.class, keys );
	}
	
	@Override
	protected LinkedHashMap toStringMapper() {
		LinkedHashMap m = new LinkedHashMap();
		m.put(KFSPropertyConstants.UNIVERSITY_FISCAL_YEAR, getUniversityFiscalYear());
		m.put(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, this.chartOfAccountsCode);
		m.put(KFSPropertyConstants.FINANCIAL_OBJECT_CODE, this.financialObjectCode);
		
        return m;
	}
	
}
