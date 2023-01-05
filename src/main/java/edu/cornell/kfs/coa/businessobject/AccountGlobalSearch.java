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

	private static final long serialVersionUID = 1L;
	private boolean useOrgHierarchy;

	public boolean isUseOrgHierarchy() {
		return useOrgHierarchy;
	}

	public void setUseOrgHierarchy(boolean useOrgHierarchy) {
		this.useOrgHierarchy = useOrgHierarchy;
	}
	
	
}
