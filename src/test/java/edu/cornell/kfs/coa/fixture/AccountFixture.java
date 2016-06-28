package edu.cornell.kfs.coa.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.rice.krad.util.ObjectUtils;

public enum AccountFixture {
	ACCOUNT_1111111_2222222_98_2(
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(), null),

	ACCOUNT_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE(
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange()),

	ACCOUNT_3333333_100(
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(), null, null),

	ACCOUNT_3333333_100_INACTIVATE(
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange());

	public IndirectCostRecoveryAccount icrAccount1;
	public IndirectCostRecoveryAccount icrAccount2;
	public IndirectCostRecoveryAccount icrAccount3;

	private AccountFixture(IndirectCostRecoveryAccount icrAccount1,
			IndirectCostRecoveryAccount icrAccount2,
			IndirectCostRecoveryAccount icrAccount3) {
		this.icrAccount1 = icrAccount1;
		this.icrAccount2 = icrAccount2;
		this.icrAccount3 = icrAccount3;
	}

	public Account getAccount() {
		Account account = new Account();
		List<IndirectCostRecoveryAccount> icrAccounts = new ArrayList<IndirectCostRecoveryAccount>();
		if (ObjectUtils.isNotNull(icrAccount1)) {
			icrAccounts.add(icrAccount1);
		}
		if (ObjectUtils.isNotNull(icrAccount2)) {
			icrAccounts.add(icrAccount2);
		}
		if (ObjectUtils.isNotNull(icrAccount3)) {
			icrAccounts.add(icrAccount3);
		}
		account.setIndirectCostRecoveryAccounts(icrAccounts);
		return account;
	}
}
