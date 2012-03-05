/**
 * 
 */
package edu.cornell.kfs.coa.businessobject;

import java.util.HashMap;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.bo.PersistableBusinessObjectExtensionBase;
import org.kuali.rice.kns.service.BusinessObjectService;

/**
 * @author kwk43
 *
 */
public class ObjectCodeExtendedAttribute extends PersistableBusinessObjectExtensionBase {

	/**
	 * 
	 */
	private Integer universityFiscalYear;
    private String chartOfAccountsCode;
    private String financialObjectCode;
    private String sunyObjectCode;
    private String cgReportingCode;
    
    private ContractGrantReportingCode contractGrantReportingCode;
    
    
    /*
     * Class constructor
     */
    public ObjectCodeExtendedAttribute() {
    	
    }
       
	/**
	 * @return the universityFiscalYear
	 */
	public Integer getUniversityFiscalYear() {
		return universityFiscalYear;
	}
	/**
	 * @param universityFiscalYear the universityFiscalYear to set
	 */
	public void setUniversityFiscalYear(Integer universityFiscalYear) {
		this.universityFiscalYear = universityFiscalYear;
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
	
}
