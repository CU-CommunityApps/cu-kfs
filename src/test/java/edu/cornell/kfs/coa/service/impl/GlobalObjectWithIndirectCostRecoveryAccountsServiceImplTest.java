package edu.cornell.kfs.coa.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;

import edu.cornell.kfs.coa.businessobject.CuAccountGlobal;
import edu.cornell.kfs.coa.businessobject.GlobalObjectWithIndirectCostRecoveryAccounts;
import edu.cornell.kfs.coa.fixture.AccountGlobalDetailFixture;
import edu.cornell.kfs.coa.fixture.AccountGlobalFixture;
import edu.cornell.kfs.coa.fixture.IndirectCostRecoveryAccountFixture;

public class GlobalObjectWithIndirectCostRecoveryAccountsServiceImplTest {
	
	private GlobalObjectWithIndirectCostRecoveryAccountsServiceImpl globalObjectWithIndirectCostRecoveryAccountsService;
	
	@Before
	public void setUp() {
		globalObjectWithIndirectCostRecoveryAccountsService = new GlobalObjectWithIndirectCostRecoveryAccountsServiceImpl();
	}

	@Test
	public void testAccountGlobalUpdateIcrAccounts_ReplaceExisting() {
		GlobalObjectWithIndirectCostRecoveryAccounts accountGlobal = (CuAccountGlobal) AccountGlobalFixture.ACCT_GLOBAL_3333333_100_ACTIVE_1111111_2222222_98_2_INACTIVE
				.getAccountGlobal();
		AccountGlobalDetail accountGlobalDetail = AccountGlobalDetailFixture.ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2
				.getAccountGlobalDetail();

		assertEquals(
				"The Account Global Detail ICR accounts list should have 2 ICR accounts before update",
				2, accountGlobalDetail.getAccount()
						.getIndirectCostRecoveryAccounts().size());

		globalObjectWithIndirectCostRecoveryAccountsService.updateIcrAccounts(accountGlobal, accountGlobalDetail,
				accountGlobalDetail.getAccount()
						.getIndirectCostRecoveryAccounts());

		assertEquals(
				"The Account Global Detail ICR accounts list should have 3 ICR accounts after update",
				3, accountGlobalDetail.getAccount()
						.getIndirectCostRecoveryAccounts().size());
		assertTrue(
				"Updated ICR accounts list should have contained ICR account 1111111 with 98% inactive",
				doesListContainIcr(
						accountGlobalDetail.getAccount()
								.getIndirectCostRecoveryAccounts(),
						IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE
								.getIndirectCostRecoveryAccountChange()));
		assertTrue(
				"Updated ICR accounts list should have contained ICR account 2222222 with 2% inactive",
				doesListContainIcr(
						accountGlobalDetail.getAccount()
								.getIndirectCostRecoveryAccounts(),
						IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_INACTIVE
								.getIndirectCostRecoveryAccountChange()));
		assertTrue(
				"Updated ICR accounts list should have contained ICR account 3333333 with 100% inactive",
				doesListContainIcr(
						accountGlobalDetail.getAccount()
								.getIndirectCostRecoveryAccounts(),
						IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE
								.getIndirectCostRecoveryAccountChange()));
	}

	@Test
	public void testAccountGlobalUpdateIcrAccounts_UpdateExistingWithTwoSameExistingPlusAdd() {
		CuAccountGlobal accountGlobal = (CuAccountGlobal) AccountGlobalFixture.ACCT_GLOBAL_1111111_2222222_3333333_98_2_ACTIVE_100_INACTIVE
				.getAccountGlobal();
		AccountGlobalDetail accountGlobalDetail = AccountGlobalDetailFixture.ACCOUNT_GLOBAL_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE
				.getAccountGlobalDetail();

		assertEquals(
				"The Account Global Detail ICR accounts list should have 3 ICR accounts before update",
				3, accountGlobalDetail.getAccount()
						.getIndirectCostRecoveryAccounts().size());

		globalObjectWithIndirectCostRecoveryAccountsService.updateIcrAccounts(accountGlobal, accountGlobalDetail,
				accountGlobalDetail.getAccount()
						.getIndirectCostRecoveryAccounts());

		assertEquals(
				"The Account Global Detail ICR accounts list should have 4 ICR accounts after update",
				4, accountGlobalDetail.getAccount()
						.getIndirectCostRecoveryAccounts().size());
		assertTrue(
				"Updated ICR accounts list should have contained ICR account 1111111 with 98% inactive",
				doesListContainIcr(
						accountGlobalDetail.getAccount()
								.getIndirectCostRecoveryAccounts(),
						IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE
								.getIndirectCostRecoveryAccountChange()));
		assertTrue(
				"Updated ICR accounts list should have contained ICR account 1111111 with 98% active",
				doesListContainIcr(
						accountGlobalDetail.getAccount()
								.getIndirectCostRecoveryAccounts(),
						IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_ACTIVE
								.getIndirectCostRecoveryAccountChange()));
		assertTrue(
				"Updated ICR accounts list should have contained ICR account 2222222 with 2% active",
				doesListContainIcr(
						accountGlobalDetail.getAccount()
								.getIndirectCostRecoveryAccounts(),
						IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_ACTIVE
								.getIndirectCostRecoveryAccountChange()));
		assertTrue(
				"Updated ICR accounts list should have contained ICR account 3333333 with 100% active",
				doesListContainIcr(
						accountGlobalDetail.getAccount()
								.getIndirectCostRecoveryAccounts(),
						IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_INACTIVE
								.getIndirectCostRecoveryAccountChange()));
	}

	private boolean doesListContainIcr(List<IndirectCostRecoveryAccount> list,
			IndirectCostRecoveryAccount icrAccount) {
		for (IndirectCostRecoveryAccount icr : list) {
			if (icr.getIndirectCostRecoveryFinCoaCode().equalsIgnoreCase(
					icrAccount.getIndirectCostRecoveryFinCoaCode())
					&& icr.getIndirectCostRecoveryAccountNumber()
							.equalsIgnoreCase(
									icrAccount
											.getIndirectCostRecoveryAccountNumber())
					&& icr.getAccountLinePercent().compareTo(
							icrAccount.getAccountLinePercent()) == 0
					&& icr.isActive() == icrAccount.isActive()) {
				return true;
			}
		}
		return false;
	}

}
