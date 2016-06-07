package edu.cornell.kfs.coa.document.validation.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.kuali.kfs.coa.businessobject.AccountGlobal;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;

import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;
import edu.cornell.kfs.coa.fixture.AccountGlobalDetailFixture;
import edu.cornell.kfs.coa.fixture.AccountGlobalFixture;
import edu.cornell.kfs.coa.fixture.SubAccountGlobalDetailFixture;
import edu.cornell.kfs.coa.fixture.SubAccountGlobalFixture;

public class GlobalIndirectCostRecoveryAccountsRuleTest extends TestCase {
	
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(GlobalIndirectCostRecoveryAccountsRuleTest.class);
	private GlobalIndirectCostRecoveryAccountsRule globalIndirectCostRecoveryAccountsRule;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();		

		globalIndirectCostRecoveryAccountsRule = new GlobalIndirectCostRecoveryAccountsRule();
	}
	
	public void testAccountGlobalCheckICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate_Pass() {
		AccountGlobal accountGlobal = (AccountGlobal)AccountGlobalFixture.ACCT_GLOBAL_3333333_100_ACTIVE_1111111_2222222_98_2_INACTIVE.getAccountGlobal();
		AccountGlobalDetail accountGlobalDetail = AccountGlobalDetailFixture.ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2.getAccountGlobalDetail();
		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate(
				accountGlobal.getIndirectCostRecoveryAccounts(),
				accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts(), 
				accountGlobalDetail, accountGlobal);
		LOG.info("Check Account Global updated ICR accounts distribution is 100%: " + result);
		assertTrue("Updated Account Global ICR account distribution is 100%", result);
	}
	
	public void testAccountGlobalCheckICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate_Fail() {
		AccountGlobal accountGlobal = (AccountGlobal)AccountGlobalFixture.ACCT_GLOBAL_3333333_100.getAccountGlobal();
		AccountGlobalDetail accountGlobalDetail = AccountGlobalDetailFixture.ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2.getAccountGlobalDetail();
		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate(
				accountGlobal.getIndirectCostRecoveryAccounts(),
				accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts(), 
				accountGlobalDetail, accountGlobal);
		LOG.info("Check Account Global updated ICR account distribution is NOT 100%: " + result);
		assertFalse("Updated Account Global ICR account distribution is NOT 100%", result);
	}
	
	public void testAccountGlobalCheckICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdateTwoSameAccounts_Pass() {
		AccountGlobal accountGlobal = (AccountGlobal)AccountGlobalFixture.ACCT_GLOBAL_1111111_2222222_3333333_98_2_ACTIVE_100_INACTIVE.getAccountGlobal();
		AccountGlobalDetail accountGlobalDetail = AccountGlobalDetailFixture.ACCOUNT_GLOBAL_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE.getAccountGlobalDetail();
		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate(
				accountGlobal.getIndirectCostRecoveryAccounts(),
				accountGlobalDetail.getAccount().getIndirectCostRecoveryAccounts(), 
				accountGlobalDetail, accountGlobal);
		LOG.info("Check Account Global updated ICR account distribution is 100%: " + result);
		assertTrue("Updated Account Global ICR account distribution is 100%", result);
	}
	
	public void testSubAccountGlobalCheckICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate_Pass() {
		SubAccountGlobal subAccountGlobal = (SubAccountGlobal)SubAccountGlobalFixture.SUB_ACCT_GLOBAL_3333333_100_ACTIVE_1111111_2222222_98_2_INACTIVE.getSubAccountGlobal();
		SubAccountGlobalDetail subAccountGlobalDetail = SubAccountGlobalDetailFixture.SUB_ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2.getSubAccountGlobalDetail();
		List<IndirectCostRecoveryAccount> existingIcrAccountsOnDetail = new ArrayList<IndirectCostRecoveryAccount>();
		existingIcrAccountsOnDetail.addAll(subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts());
		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate(
				subAccountGlobal.getIndirectCostRecoveryAccounts(),
				existingIcrAccountsOnDetail, 
				subAccountGlobalDetail, subAccountGlobal);
		LOG.info("Check Sub Account Global updated ICR account distribution is 100%: " + result);
		assertTrue("Updated Sub Account Global ICR account distribution is 100%", result);
	}
	
	public void testSubAccountGlobalCheckICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate_Fail() {
		SubAccountGlobal subAccountGlobal = (SubAccountGlobal)SubAccountGlobalFixture.SUB_ACCT_GLOBAL_3333333_100.getSubAccountGlobal();
		SubAccountGlobalDetail subAccountGlobalDetail = SubAccountGlobalDetailFixture.SUB_ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2.getSubAccountGlobalDetail();
		List<IndirectCostRecoveryAccount> existingIcrAccountsOnDetail = new ArrayList<IndirectCostRecoveryAccount>();
		existingIcrAccountsOnDetail.addAll(subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts());
		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate(
				subAccountGlobal.getIndirectCostRecoveryAccounts(),
				existingIcrAccountsOnDetail, 
				subAccountGlobalDetail, subAccountGlobal);
		LOG.info("Check Sub Account Global updated ICR account distribution is NOT 100%: " + result);
		assertFalse("Updated Sub Account Global ICR account distribution is NOT 100%", result);
	}
	
	public void testSubAccountGlobalCheckICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdateTwoSameAccounts_Pass() {
		SubAccountGlobal subAccountGlobal = (SubAccountGlobal)SubAccountGlobalFixture.SUB_ACCT_GLOBAL_1111111_2222222_3333333_98_2_ACTIVE_100_INACTIVE.getSubAccountGlobal();
		SubAccountGlobalDetail subAccountGlobalDetail = SubAccountGlobalDetailFixture.SUB_ACCOUNT_GLOBAL_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE.getSubAccountGlobalDetail();
		List<IndirectCostRecoveryAccount> existingIcrAccountsOnDetail = new ArrayList<IndirectCostRecoveryAccount>();
		existingIcrAccountsOnDetail.addAll(subAccountGlobalDetail.getSubAccount().getA21SubAccount().getA21IndirectCostRecoveryAccounts());
		boolean result = globalIndirectCostRecoveryAccountsRule.checkICRAccuntTotalDistributionOnDetailWillBe100PercentAfterUpdate(
				subAccountGlobal.getIndirectCostRecoveryAccounts(),
				existingIcrAccountsOnDetail, 
				subAccountGlobalDetail, subAccountGlobal);
		LOG.info("Check Sub Account Global updated ICR account distribution is 100%: " + result);
		assertTrue("Updated Sub Account Global ICR account distribution is 100%", result);
	}

}
