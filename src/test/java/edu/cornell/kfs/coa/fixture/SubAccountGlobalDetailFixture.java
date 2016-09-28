package edu.cornell.kfs.coa.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kuali.kfs.coa.businessobject.A21IndirectCostRecoveryAccount;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.SubAccount;

import edu.cornell.kfs.coa.businessobject.SubAccountGlobalDetail;

public enum SubAccountGlobalDetailFixture {

	SUB_ACCOUNT_GLOBAL_DETAIL_1111111_2222222_98_2(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_ACTIVE,
			A21IndirectCostRecoveryAccountFixture.A21_ICR_2222222_2_PERCENT_ACTIVE),

	SUB_ACCOUNT_GLOBAL_DETAIL_1111111_98_INACTIVE_98_INACTIVE_3333333_100_PERCENT_ACTIVE(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_INACTIVE,
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_INACTIVE,
			A21IndirectCostRecoveryAccountFixture.A21_ICR_3333333_100_PERCENT_ACTIVE),

	SUB_ACCOUNT_GLOBAL_DETAIL_3333333_100(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_3333333_100_PERCENT_ACTIVE),

	SUB_ACCOUNT_GLOBAL_DETAIL_3333333_100_INACTIVATE(
			A21IndirectCostRecoveryAccountFixture.A21_ICR_3333333_100_PERCENT_ACTIVE,
			A21IndirectCostRecoveryAccountFixture.A21_ICR_1111111_98_PERCENT_INACTIVE,
			A21IndirectCostRecoveryAccountFixture.A21_ICR_2222222_2_PERCENT_INACTIVE);

	public final List<A21IndirectCostRecoveryAccountFixture> icrAccounts;

	private SubAccountGlobalDetailFixture(A21IndirectCostRecoveryAccountFixture... icrAccounts) {
		this.icrAccounts = Collections.unmodifiableList(Arrays.asList(icrAccounts));
	}

	public SubAccountGlobalDetail getSubAccountGlobalDetail() {
		A21SubAccount a21SubAccount = new A21SubAccount();
		SubAccount subAccount = new SubAccount();
		SubAccountGlobalDetail subAccountGlobalDetail = new SubAccountGlobalDetail();
		List<A21IndirectCostRecoveryAccount> icrAccountsList = new ArrayList<A21IndirectCostRecoveryAccount>();

		for(A21IndirectCostRecoveryAccountFixture icrAccount : icrAccounts){
			icrAccountsList.add(icrAccount.getA21IndirectCostRecoveryAccountChange());
		}

		a21SubAccount.setA21IndirectCostRecoveryAccounts(icrAccountsList);
		subAccount.setA21SubAccount(a21SubAccount);
		subAccountGlobalDetail.setSubAccount(subAccount);
		return subAccountGlobalDetail;
	}

}
