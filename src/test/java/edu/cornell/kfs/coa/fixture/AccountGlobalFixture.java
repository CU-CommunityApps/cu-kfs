package edu.cornell.kfs.coa.fixture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.kuali.kfs.krad.util.ObjectUtils;

import edu.cornell.kfs.coa.businessobject.CuAccountGlobal;
import edu.cornell.kfs.coa.businessobject.GlobalObjectWithIndirectCostRecoveryAccounts;
import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;

public enum AccountGlobalFixture {
	ACCT_GLOBAL_1111111_2222222_98_2_ACTIVE_3333333_100_INACTIVE(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_1111111_98_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_2222222_2_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_3333333_100_PERCENT_INACTIVE),

	ACCT_GLOBAL_1111111_2222222_3333333_98_2_ACTIVE_100_INACTIVE(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_1111111_98_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_2222222_2_PERCENT_ACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_3333333_100_PERCENT_INACTIVE),

	ACCT_GLOBAL_3333333_100_ACTIVE_1111111_2222222_98_2_INACTIVE(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_1111111_98_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_2222222_2_PERCENT_INACTIVE,
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_3333333_100_PERCENT_ACTIVE),

	ACCT_GLOBAL_1111111_98_ACTIVE(
			IndirectCostRecoveryAccountChangeFixture.ICR_CHANGE_1111111_98_PERCENT_ACTIVE);

	public final List<IndirectCostRecoveryAccountChangeFixture> icrChanges;

	private AccountGlobalFixture(IndirectCostRecoveryAccountChangeFixture... icrChanges) {
		this.icrChanges = Collections.unmodifiableList(Arrays.asList(icrChanges));
	}

	public GlobalObjectWithIndirectCostRecoveryAccounts getAccountGlobal() {
		GlobalObjectWithIndirectCostRecoveryAccounts accountGlobal = new CuAccountGlobal();
		List<IndirectCostRecoveryAccountChange> icrAccountsList = new ArrayList<IndirectCostRecoveryAccountChange>();
		
		for(IndirectCostRecoveryAccountChangeFixture icrChange : icrChanges){
			icrAccountsList.add(icrChange.getIndirectCostRecoveryAccountChange());
		}

		accountGlobal.setIndirectCostRecoveryAccounts(icrAccountsList);
		return accountGlobal;
	}

}
