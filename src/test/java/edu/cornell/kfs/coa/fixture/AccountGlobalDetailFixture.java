package edu.cornell.kfs.coa.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.krad.util.ObjectUtils;

public enum AccountGlobalDetailFixture {
	ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2(
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(), null),

	ACCOUNT_GLOBAL_DETAIL_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE(
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange()),

	ACCOUNT_GLOBAL_DETAIL_3333333_100_ACTIVE_1111111_98_INACTIVE_98_INACTIVE(
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange()),

	ACCOUNT_GLOBAL_DETAIL_3333333_100_ACTIVE_1111111_98_INACTIVE_2222222_2_INACTIVE(
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange());

	public IndirectCostRecoveryAccount icrAccount1;
	public IndirectCostRecoveryAccount icrAccount2;
	public IndirectCostRecoveryAccount icrAccount3;

	private AccountGlobalDetailFixture(IndirectCostRecoveryAccount icrAccount1,
			IndirectCostRecoveryAccount icrAccount2,
			IndirectCostRecoveryAccount icrAccount3) {
		this.icrAccount1 = icrAccount1;
		this.icrAccount2 = icrAccount2;
		this.icrAccount3 = icrAccount3;
	}

	public AccountGlobalDetail getAccountGlobalDetail() {
		AccountGlobalDetail accountGlobalDetail = new AccountGlobalDetail();
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
		accountGlobalDetail.setAccount(account);
		return accountGlobalDetail;
	}
}
