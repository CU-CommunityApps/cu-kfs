package edu.cornell.kfs.coa.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import edu.cornell.kfs.coa.businessobject.GlobalObjectWithIndirectCostRecoveryAccounts;
import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;

public enum SubAccountGlobalFixture {

	SUB_ACCT_GLOBAL_1111111_2222222_98_2(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_1111111_98_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_2222222_2_PERCENT_ACTIVE),

	SUB_ACCT_GLOBAL_1111111_2222222_3333333_98_2_ACTIVE_100_INACTIVE(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_1111111_98_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_2222222_2_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_3333333_100_PERCENT_INACTIVE),

	SUB_ACCT_GLOBAL_3333333_100_ACTIVE_1111111_2222222_98_2_INACTIVE(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_1111111_98_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_2222222_2_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_3333333_100_PERCENT_ACTIVE),

	SUB_ACCT_GLOBAL_3333333_100(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_3333333_100_PERCENT_ACTIVE);

	public final List<IndirectCostRecoveryAccountChangeFixture> icrChanges;

	private SubAccountGlobalFixture(IndirectCostRecoveryAccountChangeFixture... icrChanges) {
		this.icrChanges = Collections.unmodifiableList(Arrays.asList(icrChanges));
	}

	public GlobalObjectWithIndirectCostRecoveryAccounts getSubAccountGlobal() {
		GlobalObjectWithIndirectCostRecoveryAccounts subAccountGlobal = new SubAccountGlobal();
		List<IndirectCostRecoveryAccountChange> icrAccountsList = new ArrayList<IndirectCostRecoveryAccountChange>();

		for(IndirectCostRecoveryAccountChangeFixture icrChange : icrChanges){
			icrAccountsList.add(icrChange.getIndirectCostRecoveryAccountChange());
		}

		subAccountGlobal.setIndirectCostRecoveryAccounts(icrAccountsList);
		return subAccountGlobal;
	}
}
