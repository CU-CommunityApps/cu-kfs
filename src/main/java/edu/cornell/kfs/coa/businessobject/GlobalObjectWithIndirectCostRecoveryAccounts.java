package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.rice.krad.bo.GlobalBusinessObjectDetailBase;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.util.ObjectUtils;

public abstract class GlobalObjectWithIndirectCostRecoveryAccounts extends PersistableBusinessObjectBase {
	protected List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts;
	
	public GlobalObjectWithIndirectCostRecoveryAccounts() {
		indirectCostRecoveryAccounts = new ArrayList<IndirectCostRecoveryAccountChange>();
	}

	public List<IndirectCostRecoveryAccountChange> getIndirectCostRecoveryAccounts() {
		return indirectCostRecoveryAccounts;
	}

	public void setIndirectCostRecoveryAccounts(
			List<IndirectCostRecoveryAccountChange> indirectCostRecoveryAccounts) {
		this.indirectCostRecoveryAccounts = indirectCostRecoveryAccounts;
	}
	
    public List<IndirectCostRecoveryAccountChange> getActiveIndirectCostRecoveryAccounts() {
        List<IndirectCostRecoveryAccountChange> activeList = new ArrayList<IndirectCostRecoveryAccountChange>();
        for (IndirectCostRecoveryAccountChange icr : getIndirectCostRecoveryAccounts()){
            if (icr.isActive()){
                activeList.add(IndirectCostRecoveryAccountChange.copyICRAccount(icr));
            }
        }
        return activeList;
    }
	
	public abstract List<? extends GlobalBusinessObjectDetailBase> getGlobalObjectDetails();
	
	public abstract Map<GlobalBusinessObjectDetailBase, List<IndirectCostRecoveryAccount>> getGlobalObjectDetailsAndIcrAccountsMap();
	
	public abstract IndirectCostRecoveryAccount createIndirectCostRecoveryAccountFromChange(GlobalBusinessObjectDetailBase globalDetail, IndirectCostRecoveryAccountChange newICR);
	
	public boolean hasIcrAccounts(){
		return ObjectUtils.isNotNull(indirectCostRecoveryAccounts) && indirectCostRecoveryAccounts.size() > 0;
	}
	
	protected void updateIcrAccounts(GlobalBusinessObjectDetailBase globalDetail, List<IndirectCostRecoveryAccount> icrAccounts) {
		Map<Integer, Integer> alreadyUpdatedIndexes = new HashMap<Integer, Integer>();
		List<IndirectCostRecoveryAccount> addList = new ArrayList<IndirectCostRecoveryAccount>();

		if (indirectCostRecoveryAccounts.size() > 0) {
			for (IndirectCostRecoveryAccountChange newICR : indirectCostRecoveryAccounts) {
				boolean found = false;

				int temp = -1;
				int i = 0;

				while (i < icrAccounts.size() && (!found || (found && temp!=-1))) {
					if (!alreadyUpdatedIndexes.containsKey(i)) {
						IndirectCostRecoveryAccount existingICR = icrAccounts.get(i);
						// check if we have a match on chart, account and percentage
						if (newICR.matchesICRAccount(existingICR)) {
							// set this to true if we have found a match
							found = true;
							// check if the they don't already both have the same active indicator
							if (newICR.isActive() == existingICR.isActive()) {
								// both the same, save position in temp and keep looking, if an entry exists that matches on chart, account and percentage but not same active indicator then we will update that one, otherwise we will consider a match on this temp entry
								temp = i;
							} else {
								// done, stop looking and update the active indicator
								existingICR.setActive(newICR.isActive());
								alreadyUpdatedIndexes.put(i, i);
								
								// reset temp since we have found a better match
								if(temp != -1){
									temp = -1;
								}
							}
						}
					}
					i++;
				}

				if (found && temp != -1) {
					// no need to update but we will add the index in already updated since there was a match
					alreadyUpdatedIndexes.put(temp, temp);
				}

				if (!found) {
					// add to active add or inactive add list
					IndirectCostRecoveryAccount icrAccount = createIndirectCostRecoveryAccountFromChange(globalDetail, newICR);
					addList.add(icrAccount);

				}
			}
			
			List<IndirectCostRecoveryAccount> updatedIcrAccounts = buildUpdatedIcrAccountsList(globalDetail, addList);
			updateGlobalDetailICRAccountCollection(globalDetail, updatedIcrAccounts);
		}
	}
	
	protected abstract List<IndirectCostRecoveryAccount> buildUpdatedIcrAccountsList(GlobalBusinessObjectDetailBase globalDetail, List<IndirectCostRecoveryAccount> addList);

	protected abstract void updateGlobalDetailICRAccountCollection(GlobalBusinessObjectDetailBase globalDetail, List<IndirectCostRecoveryAccount> updatedIcrAccounts);
	
	public abstract String getGlobalDetailsPropertyName();
}
