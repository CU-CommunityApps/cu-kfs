package edu.cornell.kfs.coa.businessobject;

import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ObjectCodeGlobal;
import org.kuali.kfs.coa.businessobject.ObjectCodeGlobalDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;

public class CUObjectCodeGlobal extends ObjectCodeGlobal implements GlobalBusinessObject {
	
	private static final Logger LOG = LogManager.getLogger();
 
    //added for SUNY Object Code and CG Reporting Code extended attributes
    private String sunyObjectCode;
    
    //NOTE:
    // ContractGrantReportingCode prompt table needs cgReportingCode attribute defined 
    // as "code" to perform the lookup on ObjectCodeGlobalMaintenanceDocument but for clarity
    // cgReportingCode needs to be used as database table column name in CA_OBJ_CD_CHG_DOC_T
    // for this attribute so that it matches the ObjectCode extended table column name; 
    // therefore, cgReportingCode and code are both used and the underlying value is 
    // kept in sync in the getter and setter methods of this class.  
    // If this was not done in this manner, the magnifying glass to get the ContractGrantReportingCode
    // prompt table would not show on the ObjectCodeGlobalMaintenanceDocument and the 
    // database value for cgReportingCode would not be returned to the eDoc even though
    // it was saved to the database.
    
    private String financialObjectCodeDescr;
    
    private String cgReportingCode;
    private String code;
    private ContractGrantReportingCode contractGrantReportingCode;

    public void populate(ObjectCode old, ObjectCodeGlobalDetail detail) {
    	super.populate(old, detail);
        //set extended attribute values that may have changed ... 
        //also ensure values for primary key are set so extended table insert does not fail on create new
        ObjectCodeExtendedAttribute cuObjectCodeExtendedData = (ObjectCodeExtendedAttribute) old.getExtension();
        cuObjectCodeExtendedData.setUniversityFiscalYear(detail.getUniversityFiscalYear());
        cuObjectCodeExtendedData.setChartOfAccountsCode(detail.getChartOfAccountsCode());
        cuObjectCodeExtendedData.setFinancialObjectCode(financialObjectCode);
        cuObjectCodeExtendedData.setCgReportingCode(update(cgReportingCode, cuObjectCodeExtendedData.getCgReportingCode()));
        cuObjectCodeExtendedData.setSunyObjectCode(update(sunyObjectCode, cuObjectCodeExtendedData.getSunyObjectCode()));
        cuObjectCodeExtendedData.setFinancialObjectCodeDescr(update(financialObjectCodeDescr, cuObjectCodeExtendedData.getFinancialObjectCodeDescr()));

    }

	public String getSunyObjectCode() {
		return sunyObjectCode;
	}


	public void setSunyObjectCode(String sunyObjectCode) {
		this.sunyObjectCode = sunyObjectCode;
	}
	
	public String getFinancialObjectCodeDescr() {
		return financialObjectCodeDescr;
	}

	public void setFinancialObjectCodeDescr(String financialObjectCodeDescr) {
		this.financialObjectCodeDescr = financialObjectCodeDescr;
	}

	public String getCgReportingCode() {
		return this.cgReportingCode;
	}

	public void setCgReportingCode(String cgReportingCode) {
		this.cgReportingCode = cgReportingCode;
		this.code = cgReportingCode;
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		HashMap<String,String> keys = new HashMap<String,String>();
		keys.put("chartOfAccountsCode", this.chartOfAccountsCode);
		//lookup table has class attribute defined as "code"
	    keys.put("code", this.cgReportingCode);
		contractGrantReportingCode = (ContractGrantReportingCode) bos.findByPrimaryKey(ContractGrantReportingCode.class, keys );
	}
	
	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
		this.cgReportingCode = code;
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		HashMap<String,String> keys = new HashMap<String,String>();
		keys.put("chartOfAccountsCode", this.chartOfAccountsCode);
		//lookup table has class attribute defined as "code"
	    keys.put("code", this.code);
		contractGrantReportingCode = (ContractGrantReportingCode) bos.findByPrimaryKey(ContractGrantReportingCode.class, keys );
	}

	public ContractGrantReportingCode getContractGrantReportingCode() {
		return this.contractGrantReportingCode;
	}	

	public void setContractGrantReportingCode(ContractGrantReportingCode contractGrantReportingCode) {
		this.contractGrantReportingCode = contractGrantReportingCode;
	}
	
}
