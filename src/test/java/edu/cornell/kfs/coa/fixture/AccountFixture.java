package edu.cornell.kfs.coa.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.krad.util.ObjectUtils;

public enum AccountFixture {
	ACCOUNT_1111111_2222222_98_2(
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_ACTIVE),

	ACCOUNT_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE(
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE),

	ACCOUNT_3333333_100(
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE),

	ACCOUNT_3333333_100_INACTIVATE(
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_INACTIVE);

	public final List<IndirectCostRecoveryAccountFixture> icrAccounts;

	private AccountFixture(IndirectCostRecoveryAccountFixture... icrAccounts) {
		this.icrAccounts = Collections.unmodifiableList(Arrays.asList(icrAccounts));
	}

	public Account getAccount() {
		Account account = new Account();
		List<IndirectCostRecoveryAccount> icrAccountsList = new ArrayList<IndirectCostRecoveryAccount>();

		for(IndirectCostRecoveryAccountFixture icrAccount : icrAccounts){
			icrAccountsList.add(icrAccount.getIndirectCostRecoveryAccountChange());
		}

		account.setIndirectCostRecoveryAccounts(icrAccountsList);
		return account;
	}
}
