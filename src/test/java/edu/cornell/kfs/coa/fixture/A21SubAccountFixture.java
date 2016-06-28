package edu.cornell.kfs.coa.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.rice.krad.util.ObjectUtils;

public enum A21SubAccountFixture {
	A21_SUB_ACCOUNT_1111111_2222222_98_2(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_ACTIVE
					.getA21IndirectCostRecoveryAccountChange(),
			A21IndirectCostRecoveryAccountFixture.A21_ICR_2222222_2_PERCENT_ACTIVE
					.getA21IndirectCostRecoveryAccountChange(), null),

	A21_SUB_ACCOUNT_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_INACTIVE
					.getA21IndirectCostRecoveryAccountChange(),
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_INACTIVE
					.getA21IndirectCostRecoveryAccountChange(),
			A21IndirectCostRecoveryAccountFixture.A21_ICR_3333333_100_PERCENT_ACTIVE
					.getA21IndirectCostRecoveryAccountChange()),

	A21_SUB_ACCOUNT_3333333_100(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_3333333_100_PERCENT_ACTIVE
					.getA21IndirectCostRecoveryAccountChange(), null, null),

	A21_SUB_ACCOUNT_3333333_100_INACTIVATE(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_3333333_100_PERCENT_ACTIVE
					.getA21IndirectCostRecoveryAccountChange(),
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_INACTIVE
					.getA21IndirectCostRecoveryAccountChange(),
			A21IndirectCostRecoveryAccountFixture.A21_ICR_2222222_2_PERCENT_INACTIVE
					.getA21IndirectCostRecoveryAccountChange());

	public A21IndirectCostRecoveryAccount icrAccount1;
	public A21IndirectCostRecoveryAccount icrAccount2;
	public A21IndirectCostRecoveryAccount icrAccount3;

	private A21SubAccountFixture(A21IndirectCostRecoveryAccount icrAccount1,
			A21IndirectCostRecoveryAccount icrAccount2,
			A21IndirectCostRecoveryAccount icrAccount3) {
		this.icrAccount1 = icrAccount1;
		this.icrAccount2 = icrAccount2;
		this.icrAccount3 = icrAccount3;
	}

	public A21SubAccount getA21SubAccount() {
		A21SubAccount a21SubAccount = new A21SubAccount();
		List<A21IndirectCostRecoveryAccount> icrAccounts = new ArrayList<A21IndirectCostRecoveryAccount>();
		if (ObjectUtils.isNotNull(icrAccount1)) {
			icrAccounts.add(icrAccount1);
		}
		if (ObjectUtils.isNotNull(icrAccount2)) {
			icrAccounts.add(icrAccount2);
		}
		if (ObjectUtils.isNotNull(icrAccount3)) {
			icrAccounts.add(icrAccount3);
		}
		a21SubAccount.setA21IndirectCostRecoveryAccounts(icrAccounts);
		return a21SubAccount;
	}

}
