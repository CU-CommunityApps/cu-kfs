package edu.cornell.kfs.coa.document.validation.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;

import edu.cornell.kfs.coa.businessobject.CuAccountGlobal;
import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;
import edu.cornell.kfs.coa.fixture.AccountGlobalDetailFixture;
import edu.cornell.kfs.coa.fixture.AccountGlobalFixture;
import edu.cornell.kfs.coa.fixture.SubAccountGlobalDetailFixture;
import edu.cornell.kfs.coa.fixture.SubAccountGlobalFixture;

public class GlobalIndirectCostRecoveryAccountsRuleTest {

	private GlobalIndirectCostRecoveryAccountsRule globalIndirectCostRecoveryAccountsRule;

	private static final Logger LOG = LogManager.getLogger(GlobalIndirectCostRecoveryAccountsRuleTest.class);

	@Before
	public void setUp() {
		globalIndirectCostRecoveryAccountsRule = new GlobalIndirectCostRecoveryAccountsRule();
	}

	@Test
	public void testAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate_Pass() {
		LOG.debug("enter testAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate_Pass");
		CuAccountGlobal accountGlobal = (CuAccountGlobal) AccountGlobalFixture.ACCT_GLOBAL_3333333_100_ACTIVE_1111111_2222222_98_2_INACTIVE.getAccountGlobal();
		AccountGlobalDetail accountGlobalDetail = AccountGlobalDetailFixture.ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2.getAccountGlobalDetail();

		LOG.debug("updates");
		List<IndirectCostRecoveryAccountChange> updates = accountGlobal.getIndirectCostRecoveryAccounts();
		logDetailsForIcrAccountChanges(updates);
		
		LOG.debug("existing");
		List<IndirectCostRecoveryAccount> existing = accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts();
		logDetailsForIcrAccounts(existing);

		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate(updates, existing, accountGlobalDetail, accountGlobal);

		assertTrue("Updated Account Global ICR account distribution should have been 100%", result);
	}

	@Test
	public void testAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate_Fail() {
		LOG.info("enter testAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate_Fail");
		CuAccountGlobal accountGlobal = (CuAccountGlobal) AccountGlobalFixture.ACCT_GLOBAL_1111111_98_ACTIVE.getAccountGlobal();
		AccountGlobalDetail accountGlobalDetail = AccountGlobalDetailFixture.ACCOUNT_GLOBAL_DETAIL_3333333_100_ACTIVE_1111111_98_INACTIVE_2222222_2_INACTIVE.getAccountGlobalDetail();
		
		LOG.debug("updates");
		List<IndirectCostRecoveryAccountChange> updates = accountGlobal.getIndirectCostRecoveryAccounts();
		logDetailsForIcrAccountChanges(updates);

		LOG.debug("existing");
		List<IndirectCostRecoveryAccount> existing = accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts();
		logDetailsForIcrAccounts(existing);

		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate(updates, existing, accountGlobalDetail, accountGlobal);

		assertFalse("Updated Account Global ICR account distribution should NOT have been 100%", result);
	}

	@Test
	public void testAccountGlobalCheckICRAccountTotalDistributionOnDetailWillNotBe100PercentAfterUpdateTwoSameAccounts_Pass() {
		LOG.debug("enter testAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdateTwoSameAccounts_Pass");
		CuAccountGlobal accountGlobal = (CuAccountGlobal) AccountGlobalFixture.ACCT_GLOBAL_1111111_2222222_98_2_ACTIVE_3333333_100_INACTIVE.getAccountGlobal();
		AccountGlobalDetail accountGlobalDetail = AccountGlobalDetailFixture.ACCOUNT_GLOBAL_DETAIL_3333333_100_ACTIVE_1111111_98_INACTIVE_98_INACTIVE.getAccountGlobalDetail();
		
		LOG.debug("updates");
		List<IndirectCostRecoveryAccountChange> updates = accountGlobal.getIndirectCostRecoveryAccounts();
		logDetailsForIcrAccountChanges(updates);

		LOG.debug("existing");
		List<IndirectCostRecoveryAccount> existing = accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts();
		logDetailsForIcrAccounts(existing);
		
		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate(updates, existing, accountGlobalDetail, accountGlobal);

		assertTrue("Updated Account Global ICR account distribution should have been 100%", result);
	}

	@Test
	public void testSubAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate_Pass() {
		LOG.debug("enter testSubAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate_Pass");
		
		SubAccountGlobal subAccountGlobal = (SubAccountGlobal) SubAccountGlobalFixture.SUB_ACCT_GLOBAL_3333333_100_ACTIVE_1111111_2222222_98_2_INACTIVE.getSubAccountGlobal();
		SubAccountGlobalDetail subAccountGlobalDetail = SubAccountGlobalDetailFixture.SUB_ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2.getSubAccountGlobalDetail();
		List<IndirectCostRecoveryAccount> existingIcrAccountsOnDetail = new ArrayList<IndirectCostRecoveryAccount>();
		existingIcrAccountsOnDetail.addAll(subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts());

		LOG.debug("updates");
		List<IndirectCostRecoveryAccountChange> updates = subAccountGlobal.getIndirectCostRecoveryAccounts();
		logDetailsForIcrAccountChanges(updates);

		LOG.debug("existing");
		logDetailsForIcrAccounts(existingIcrAccountsOnDetail);

		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate(updates, existingIcrAccountsOnDetail,subAccountGlobalDetail, subAccountGlobal);
		assertTrue("Updated Sub Account Global ICR account distribution should have been 100%", result);
	}

	@Test
	public void testSubAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate_Fail() {
		LOG.debug("enter testSubAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate_Fail");
		SubAccountGlobal subAccountGlobal = (SubAccountGlobal) SubAccountGlobalFixture.SUB_ACCT_GLOBAL_1111111_2222222_98_2.getSubAccountGlobal();
		SubAccountGlobalDetail subAccountGlobalDetail = SubAccountGlobalDetailFixture.SUB_ACCOUNT_GLOBAL_DETAIL_3333333_100_INACTIVATE.getSubAccountGlobalDetail();
		List<IndirectCostRecoveryAccount> existingIcrAccountsOnDetail = new ArrayList<IndirectCostRecoveryAccount>();
		existingIcrAccountsOnDetail.addAll(subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts());

		LOG.debug("updates");
		List<IndirectCostRecoveryAccountChange> updates = subAccountGlobal.getIndirectCostRecoveryAccounts();
        logDetailsForIcrAccountChanges(updates);
        
		LOG.debug("existing");
		logDetailsForIcrAccounts(existingIcrAccountsOnDetail);

		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate(updates, existingIcrAccountsOnDetail, subAccountGlobalDetail, subAccountGlobal);

		assertFalse("Updated Sub Account Global ICR account distribution should NOT have been 100%", result);
	}

	@Test
	public void testSubAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdateTwoSameAccounts_Pass() {
		LOG.debug("enter testSubAccountGlobalCheckICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdateTwoSameAccounts_Pass");
		SubAccountGlobal subAccountGlobal = (SubAccountGlobal) SubAccountGlobalFixture.SUB_ACCT_GLOBAL_1111111_2222222_3333333_98_2_ACTIVE_100_INACTIVE.getSubAccountGlobal();
		SubAccountGlobalDetail subAccountGlobalDetail = SubAccountGlobalDetailFixture.SUB_ACCOUNT_GLOBAL_DETAIL_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE.getSubAccountGlobalDetail();
		List<IndirectCostRecoveryAccount> existingIcrAccountsOnDetail = new ArrayList<IndirectCostRecoveryAccount>();
		existingIcrAccountsOnDetail.addAll(subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts());
		
		LOG.debug("updates");
		List<IndirectCostRecoveryAccountChange> updates = subAccountGlobal.getIndirectCostRecoveryAccounts();
		logDetailsForIcrAccountChanges(updates);

		LOG.debug("existing");
		logDetailsForIcrAccounts(existingIcrAccountsOnDetail);

		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccountTotalDistributionOnDetailWillBe100PercentAfterUpdate(updates, existingIcrAccountsOnDetail, subAccountGlobalDetail, subAccountGlobal);

		assertTrue("Updated Sub Account Global ICR account distribution should have been 100%", result);
	}
	
	private void logDetailsForIcrAccounts(List<IndirectCostRecoveryAccount> icrAccounts){
		for (IndirectCostRecoveryAccount icrAccount : icrAccounts) {
			LOG.debug("Chart " + icrAccount.getIndirectCostRecoveryFinCoaCode()
					+ " account " + icrAccount.getIndirectCostRecoveryAccountNumber()
					+ " percent " + icrAccount.getAccountLinePercent()
					+ " active " + icrAccount.isActive());
		}
	}
	
	private void logDetailsForIcrAccountChanges(List<IndirectCostRecoveryAccountChange> updates){
		for (IndirectCostRecoveryAccountChange icrChange : updates) {
			LOG.debug("Chart " + icrChange.getIndirectCostRecoveryFinCoaCode()
					+ " account " + icrChange.getIndirectCostRecoveryAccountNumber()
					+ " percent " + icrChange.getAccountLinePercent()
					+ " active " + icrChange.isActive());
		}
	}

}
