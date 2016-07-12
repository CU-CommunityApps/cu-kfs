package edu.cornell.kfs.coa.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.krad.bo.GlobalBusinessObjectDetailBase;

import edu.cornell.kfs.coa.businessobject.GlobalObjectWithIndirectCostRecoveryAccounts;
import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;
import edu.cornell.kfs.coa.service.GlobalObjectWithIndirectCostRecoveryAccountsService;

public class GlobalObjectWithIndirectCostRecoveryAccountsServiceImpl implements GlobalObjectWithIndirectCostRecoveryAccountsService{
	
	public List<IndirectCostRecoveryAccountChange> getActiveIndirectCostRecoveryAccounts(
			GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts) {
        List<IndirectCostRecoveryAccountChange> activeList = new ArrayList<IndirectCostRecoveryAccountChange>();
        for (IndirectCostRecoveryAccountChange icr : globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts()){
            if (icr.isActive()){
                activeList.add(IndirectCostRecoveryAccountChange.copyICRAccount(icr));
            }
        }
        return activeList;
    }
    
	public void updateIcrAccounts(
			GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts,
			GlobalBusinessObjectDetailBase globalDetail,
			List<IndirectCostRecoveryAccount> icrAccounts) {
    	List<IndirectCostRecoveryAccount> updatedIcrAccounts = buildUpdatedIcrAccounts(globalObjectWithIndirectCostRecoveryAccounts, globalDetail, icrAccounts);
    	globalObjectWithIndirectCostRecoveryAccounts.updateGlobalDetailICRAccountCollection(globalDetail, updatedIcrAccounts);
    }
    
	public List<IndirectCostRecoveryAccount> buildUpdatedIcrAccounts(
			GlobalObjectWithIndirectCostRecoveryAccounts globalObjectWithIndirectCostRecoveryAccounts,
			GlobalBusinessObjectDetailBase globalDetail,
			List<IndirectCostRecoveryAccount> icrAccounts) {
		List<IndirectCostRecoveryAccount> updatedIcrAccounts = new ArrayList<IndirectCostRecoveryAccount>();
		Map<Integer, Integer> alreadyUpdatedIndexes = new HashMap<>();
		List<IndirectCostRecoveryAccount> addList = new ArrayList<IndirectCostRecoveryAccount>();
		List<IndirectCostRecoveryAccountChange> newIndirectCostRecoveryAccounts = globalObjectWithIndirectCostRecoveryAccounts.getIndirectCostRecoveryAccounts();

		if (newIndirectCostRecoveryAccounts.size() > 0) {
			for (IndirectCostRecoveryAccountChange newICR : newIndirectCostRecoveryAccounts) {
				boolean foundMatch = false;

				int positionForMatchWithSameActiveIndicator = -1;
				int currentPosition = 0;
				int maxPosition = icrAccounts.size();

				while (noMatchFoundOrSameActiveIndicatorMatchFoundAndStillHaveICRAccountToCheck(currentPosition, maxPosition, foundMatch, positionForMatchWithSameActiveIndicator)) {
					if (!alreadyUpdatedIndexes.containsKey(currentPosition)) {
						IndirectCostRecoveryAccount existingICR = icrAccounts.get(currentPosition);

						if (newICR.matchesICRAccount(existingICR)) {
							foundMatch = true;

							if (newICR.isActive() == existingICR.isActive()) {
								positionForMatchWithSameActiveIndicator = currentPosition;
							} else {
								existingICR.setActive(newICR.isActive());
								alreadyUpdatedIndexes.put(currentPosition, currentPosition);

								if(positionForMatchWithSameActiveIndicator != -1){
									positionForMatchWithSameActiveIndicator = -1;
								}
							}
						}
					}
					currentPosition++;
				}

				if (foundMatch && positionForMatchWithSameActiveIndicator != -1) {
					alreadyUpdatedIndexes.put(positionForMatchWithSameActiveIndicator, positionForMatchWithSameActiveIndicator);
				}

				if (!foundMatch) {
					IndirectCostRecoveryAccount icrAccount = globalObjectWithIndirectCostRecoveryAccounts.createIndirectCostRecoveryAccountFromChange(globalDetail, newICR);
					addList.add(icrAccount);

				}
			}

			updatedIcrAccounts = combineExistingAndNewAccounts(icrAccounts, addList);						
		}
		else {
			updatedIcrAccounts = icrAccounts;
		}

		return updatedIcrAccounts;
    }
    
    private List<IndirectCostRecoveryAccount> combineExistingAndNewAccounts(List<IndirectCostRecoveryAccount> icrAccounts, List<IndirectCostRecoveryAccount> addList){
		List<IndirectCostRecoveryAccount> updatedIcrAccounts = new ArrayList<IndirectCostRecoveryAccount>();
		
		for(IndirectCostRecoveryAccount existingAccount : icrAccounts){
			updatedIcrAccounts.add(existingAccount);
		}
		
		for(IndirectCostRecoveryAccount addAccount : addList){
			updatedIcrAccounts.add(addAccount);
		}

		return updatedIcrAccounts;
    }

	private boolean noMatchFoundOrSameActiveIndicatorMatchFoundAndStillHaveICRAccountToCheck(int currentPosition, 
			int maxPosition, 
			boolean foundMatch, 
			int positionForMatchWithSameActiveIndicator) {
		return currentPosition < maxPosition && (!foundMatch || (foundMatch && positionForMatchWithSameActiveIndicator !=-1));
	}

}
