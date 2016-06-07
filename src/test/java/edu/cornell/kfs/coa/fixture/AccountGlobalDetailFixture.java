package edu.cornell.kfs.coa.fixture;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;

public enum AccountGlobalDetailFixture {
	ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2(
			AccountFixture.ACCOUNT_1111111_2222222_98_2.getAccount()),

	ACCOUNT_GLOBAL_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE(
			AccountFixture.ACCOUNT_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE
					.getAccount());

	public Account account;

	private AccountGlobalDetailFixture(Account account) {
		this.account = account;
	}

	public AccountGlobalDetail getAccountGlobalDetail() {
		AccountGlobalDetail accountGlobalDetail = new AccountGlobalDetail();
		accountGlobalDetail.setAccount(account);
		return accountGlobalDetail;
	}

	public Account getAccount() {
		return account;
	}

}
