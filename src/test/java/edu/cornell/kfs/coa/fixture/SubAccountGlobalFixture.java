package edu.cornell.kfs.coa.fixture;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.GlobalObjectWithIndirectCostRecoveryAccounts;
import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;
import edu.cornell.kfs.coa.businessobject.SubAccountGlobal;

public enum SubAccountGlobalFixture {

	SUB_ACCT_GLOBAL_1111111_2222222_98_2(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_1111111_98_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_2222222_2_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(), null),

	SUB_ACCT_GLOBAL_1111111_2222222_3333333_98_2_ACTIVE_100_INACTIVE(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_1111111_98_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_2222222_2_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_3333333_100_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange()),

	SUB_ACCT_GLOBAL_3333333_100_ACTIVE_1111111_2222222_98_2_INACTIVE(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_1111111_98_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_2222222_2_PERCENT_INACTIVE
					.getIndirectCostRecoveryAccountChange(),
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_3333333_100_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange()),

	SUB_ACCT_GLOBAL_3333333_100(
			null,
			null,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_3333333_100_PERCENT_ACTIVE
					.getIndirectCostRecoveryAccountChange());

	public IndirectCostRecoveryAccountChange icrChange1;
	public IndirectCostRecoveryAccountChange icrChange2;
	public IndirectCostRecoveryAccountChange icrChange3;

	private SubAccountGlobalFixture(
			IndirectCostRecoveryAccountChange icrChange1,
			IndirectCostRecoveryAccountChange icrChange2,
			IndirectCostRecoveryAccountChange icrChange3) {
		this.icrChange1 = icrChange1;
		this.icrChange2 = icrChange2;
		this.icrChange3 = icrChange3;
	}

	public GlobalObjectWithIndirectCostRecoveryAccounts getSubAccountGlobal() {
		GlobalObjectWithIndirectCostRecoveryAccounts subAccountGlobal = new SubAccountGlobal();
		List<IndirectCostRecoveryAccountChange> icrAccounts = new ArrayList<IndirectCostRecoveryAccountChange>();
		if (ObjectUtils.isNotNull(icrChange1)) {
			icrAccounts.add(icrChange1);
		}
		if (ObjectUtils.isNotNull(icrChange2)) {
			icrAccounts.add(icrChange2);
		}
		if (ObjectUtils.isNotNull(icrChange3)) {
			icrAccounts.add(icrChange3);
		}

		subAccountGlobal.setIndirectCostRecoveryAccounts(icrAccounts);
		return subAccountGlobal;

	}
}
