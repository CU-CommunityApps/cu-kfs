package edu.cornell.kfs.coa.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;
import org.kuali.kfs.krad.util.ObjectUtils;

public enum AccountGlobalDetailFixture {
	ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2(
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_ACTIVE),

	ACCOUNT_GLOBAL_DETAIL_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE(
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE),

	ACCOUNT_GLOBAL_DETAIL_3333333_100_ACTIVE_1111111_98_INACTIVE_98_INACTIVE(
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE),

	ACCOUNT_GLOBAL_DETAIL_3333333_100_ACTIVE_1111111_98_INACTIVE_2222222_2_INACTIVE(
			IndirectCostRecoveryAccountFixture.ICR_3333333_100_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_1111111_98_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountFixture.ICR_2222222_2_PERCENT_INACTIVE);

	public final List<IndirectCostRecoveryAccountFixture> icrAccounts;

	private AccountGlobalDetailFixture(IndirectCostRecoveryAccountFixture... icrAccounts) {
		this.icrAccounts = Collections.unmodifiableList(Arrays.asList(icrAccounts));
	}

	public AccountGlobalDetail getAccountGlobalDetail() {
		AccountGlobalDetail accountGlobalDetail = new AccountGlobalDetail();
		Account account = new Account();
		List<IndirectCostRecoveryAccount> icrAccountsList = new ArrayList<IndirectCostRecoveryAccount>();

		for(IndirectCostRecoveryAccountFixture icrAccount : icrAccounts){
			icrAccountsList.add(icrAccount.getIndirectCostRecoveryAccountChange());
		}

		account.setIndirectCostRecoveryAccounts(icrAccountsList);
		accountGlobalDetail.setAccount(account);
		return accountGlobalDetail;
	}
}
