package edu.cornell.kfs.coa.businessobject;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.rice.krad.bo.GlobalBusinessObject;
import org.kuali.rice.krad.bo.GlobalBusinessObjectDetailBase;

public interface GlobalObjectWithIndirectCostRecoveryAccounts extends GlobalBusinessObject {

	List<IndirectCostRecoveryAccountChange> getIndirectCostRecoveryAccounts();

	void setIndirectCostRecoveryAccounts(
			List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts);

	List<IndirectCostRecoveryAccountChange> getActiveIndirectCostRecoveryAccounts();

	List<? extends GlobalBusinessObjectDetailBase> getGlobalObjectDetails();

	Map<GlobalBusinessObjectDetailBase, List<IndirectCostRecoveryAccount>> getGlobalObjectDetailsAndIcrAccountsMap();

	IndirectCostRecoveryAccount createIndirectCostRecoveryAccountFromChange(
			GlobalBusinessObjectDetailBase globalDetail,
			IndirectCostRecoveryAccountChange newICR);

	boolean hasIcrAccounts();

	void updateIcrAccounts(GlobalBusinessObjectDetailBase globalDetail,
			List<IndirectCostRecoveryAccount> icrAccounts);

	void updateGlobalDetailICRAccountCollection(
			GlobalBusinessObjectDetailBase globalDetail,
			List<IndirectCostRecoveryAccount> updatedIcrAccounts);

	String getGlobalDetailsPropertyName();
}
