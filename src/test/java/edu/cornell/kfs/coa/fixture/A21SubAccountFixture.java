package edu.cornell.kfs.coa.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.krad.util.ObjectUtils;

public enum A21SubAccountFixture {
	A21_SUB_ACCOUNT_1111111_2222222_98_2(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_ACTIVE,
			A21IndirectCostRecoveryAccountFixture.A21_ICR_2222222_2_PERCENT_ACTIVE),

	A21_SUB_ACCOUNT_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_INACTIVE,
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_INACTIVE,
			A21IndirectCostRecoveryAccountFixture.A21_ICR_3333333_100_PERCENT_ACTIVE),

	A21_SUB_ACCOUNT_3333333_100(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_3333333_100_PERCENT_ACTIVE),

	A21_SUB_ACCOUNT_3333333_100_INACTIVATE(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_3333333_100_PERCENT_ACTIVE,
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_INACTIVE,
			A21IndirectCostRecoveryAccountFixture.A21_ICR_2222222_2_PERCENT_INACTIVE);

	public final List<A21IndirectCostRecoveryAccountFixture> icrAccounts;

	private A21SubAccountFixture(A21IndirectCostRecoveryAccountFixture... icrAccounts) {
		this.icrAccounts = Collections.unmodifiableList(Arrays.asList(icrAccounts));
	}

	public A21SubAccount getA21SubAccount() {
		A21SubAccount a21SubAccount = new A21SubAccount();
		List<A21IndirectCostRecoveryAccount> icrAccountsList = new ArrayList<A21IndirectCostRecoveryAccount>();

		for(A21IndirectCostRecoveryAccountFixture icrAccount : icrAccounts){
			icrAccountsList.add(icrAccount.getA21IndirectCostRecoveryAccountChange());
		}

		a21SubAccount.setA21IndirectCostRecoveryAccounts(icrAccountsList);
		return a21SubAccount;
	}

}
