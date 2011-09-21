/**
 * 
 */
package edu.cornell.kfs.coa.businessobject;

import org.kuali.kfs.coa.businessobject.Account;

/**
 * @author kwk43
 *
 */
public class AccountGlobalSearch extends Account {

	private boolean useOrgHierarchy;

	public boolean isUseOrgHierarchy() {
		return useOrgHierarchy;
	}

	public void setUseOrgHierarchy(boolean useOrgHierarchy) {
		this.useOrgHierarchy = useOrgHierarchy;
	}
	
	
	
}
