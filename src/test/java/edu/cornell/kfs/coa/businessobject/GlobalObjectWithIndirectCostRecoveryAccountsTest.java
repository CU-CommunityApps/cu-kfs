package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.AccountGlobal;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;

import edu.cornell.kfs.coa.fixture.AccountGlobalDetailFixture;
import edu.cornell.kfs.coa.fixture.AccountGlobalFixture;
import edu.cornell.kfs.coa.fixture.IndirectCostRecoveryAccountFixture;
import edu.cornell.kfs.coa.fixture.SubAccountGlobalDetailFixture;
import edu.cornell.kfs.coa.fixture.SubAccountGlobalFixture;
import junit.framework.TestCase;


public class GlobalObjectWithIndirectCostRecoveryAccountsTest extends TestCase {

	public void testAccountGlobalUpdateIcrAccounts_ReplaceExisting() {	
		GlobalObjectWithIndirectCostRecoveryAccounts accountGlobal = (AccountGlobal)AccountGlobalFixture.ACCT_GLOBAL_3333333_100_ACTIVE_1111111_2222222_98_2_INACTIVE.getAccountGlobal();
		AccountGlobalDetail accountGlobalDetail = AccountGlobalDetailFixture.ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2.getAccountGlobalDetail();
		assertEquals(2, accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts().size());
		
		accountGlobal.updateIcrAccounts(accountGlobalDetail, accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts());

		assertEquals(3, accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts().size());
		assertTrue(doesListContainIcr(accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts(), IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE.getIndirectCostRecoveryAccountChange()));
		assertTrue(doesListContainIcr(accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts(), IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_INACTIVE.getIndirectCostRecoveryAccountChange()));
		assertTrue(doesListContainIcr(accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts(), IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE.getIndirectCostRecoveryAccountChange()));
	}
	
	public void testAccountGlobalUpdateIcrAccounts_UpdateExistingWithTwoSameExistingPlusAdd() {	
		AccountGlobal accountGlobal = (AccountGlobal)AccountGlobalFixture.ACCT_GLOBAL_1111111_2222222_3333333_98_2_ACTIVE_100_INACTIVE.getAccountGlobal();
		AccountGlobalDetail accountGlobalDetail = AccountGlobalDetailFixture.ACCOUNT_GLOBAL_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE.getAccountGlobalDetail();
		
		assertEquals(3, accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts().size());
		
		accountGlobal.updateIcrAccounts(accountGlobalDetail, accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts());

		assertEquals(4, accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts().size());
		assertTrue(doesListContainIcr(accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts(), IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE.getIndirectCostRecoveryAccountChange()));
		assertTrue(doesListContainIcr(accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts(), IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_ACTIVE.getIndirectCostRecoveryAccountChange()));
		assertTrue(doesListContainIcr(accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts(), IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_ACTIVE.getIndirectCostRecoveryAccountChange()));
		assertTrue(doesListContainIcr(accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts(), IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_INACTIVE.getIndirectCostRecoveryAccountChange()));
	}
	
	private boolean doesListContainIcr(List<IndirectCostRecoveryAccount> list, IndirectCostRecoveryAccount icrAccount){
		for(IndirectCostRecoveryAccount icr : list){
			if(icr.getIndirectCostRecoveryFinCoaCode().equalsIgnoreCase(icrAccount.getIndirectCostRecoveryFinCoaCode()) &&
					icr.getIndirectCostRecoveryAccountNumber().equalsIgnoreCase(icrAccount.getIndirectCostRecoveryAccountNumber()) &&
					icr.getAccountLinePercent().compareTo(icrAccount.getAccountLinePercent()) == 0 &&
					icr.isActive() == icrAccount.isActive()){
				return true;
			}
		}
		return false;
	}

}
