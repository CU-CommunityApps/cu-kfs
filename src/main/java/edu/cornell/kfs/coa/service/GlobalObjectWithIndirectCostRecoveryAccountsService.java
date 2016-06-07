package edu.cornell.kfs.coa.service;

import java.util.List;

import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.rice.krad.bo.GlobalBusinessObjectDetailBase;

import edu.cornell.kfs.coa.businessobject.GlobalObjectWithIndirectCostRecoveryAccounts;
import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;

public interface GlobalObjectWithIndirectCostRecoveryAccountsService {
	
	public List<IndirectCostRecoveryAccountChange> getActiveIndirectCostRecoveryAccounts(GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts);
	
    public List<IndirectCostRecoveryAccount>  buildUpdatedIcrAccounts(GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts, GlobalBusinessObjectDetailBase globalDetail, List<IndirectCostRecoveryAccount> icrAccounts);

    public void updateIcrAccounts(GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts, GlobalBusinessObjectDetailBase globalDetail, List<IndirectCostRecoveryAccount> icrAccounts);
}
