package edu.cornell.kfs.coa.businessobject;

import java.util.LinkedHashMap;




import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

public class ContractGrantReportingCode extends PersistableBusinessObjectBase implements MutableInactivatable {
	

	private static final long serialVersionUID = -8218088622597106234L;
	
	private String chartOfAccountsCode;
    private String code;
    private String name;
    private boolean active;
    
    
    private Chart chartOfAccounts;
    

    public ContractGrantReportingCode() {
    }
    
    /**
	 * @return the chartOfAccounts
	 */
	public Chart getChartOfAccounts() {
		return chartOfAccounts;
	}
	
	/**
	 * @param chartOfAccounts the chartOfAccounts to set
	 */
	public void setChartOfAccounts(Chart chartOfAccounts) {
		this.chartOfAccounts = chartOfAccounts;
	}
    
    /**
     * Gets the active attribute.
     * 
     * @return Returns the active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active attribute.
     * 
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
     * @see org.kuali.kfs.kns.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        m.put("chartOfAccountsCode", chartOfAccountsCode);
        m.put("code", this.code);
        return m;
    }



}