package edu.cornell.kfs.coa.fixture;

import java.math.BigDecimal;

import edu.cornell.kfs.coa.businessobject.IndirectCostRecoveryAccountChange;

public enum IndirectCostRecoveryAccountChangeFixture {
	ICR_CHANGE_1111111_98_PERCENT_ACTIVE("IT", "1111111", new BigDecimal(98),
			true),

	ICR_CHANGE_1111111_98_PERCENT_INACTIVE("IT", "1111111", new BigDecimal(98),
			false),

	ICR_CHANGE_1111111_100_PERCENT_ACTIVE("IT", "1111111", new BigDecimal(100),
			true),

	ICR_CHANGE_1111111_100_PERCENT_INACTIVE("IT", "1111111",
			new BigDecimal(100), false),

	ICR_CHANGE_2222222_2_PERCENT_ACTIVE("IT", "2222222", new BigDecimal(2),
			true),

	ICR_CHANGE_2222222_2_PERCENT_INACTIVE("IT", "2222222", new BigDecimal(2),
			false),

	ICR_CHANGE_3333333_100_PERCENT_ACTIVE("IT", "3333333", new BigDecimal(100),
			true),

	ICR_CHANGE_3333333_100_PERCENT_INACTIVE("IT", "3333333",
			new BigDecimal(100), false);

	public final String indirectCostRecoveryFinCoaCode;
	public final String indirectCostRecoveryAccountNumber;
	public final BigDecimal accountLinePercent;
	public final boolean active;

	private IndirectCostRecoveryAccountChangeFixture(
			String indirectCostRecoveryFinCoaCode,
			String indirectCostRecoveryAccountNumber,
			BigDecimal accountLinePercent, boolean active) {
		this.indirectCostRecoveryFinCoaCode = indirectCostRecoveryFinCoaCode;
		this.indirectCostRecoveryAccountNumber = indirectCostRecoveryAccountNumber;
		this.accountLinePercent = accountLinePercent;
		this.active = active;
	}

	public IndirectCostRecoveryAccountChange getIndirectCostRecoveryAccountChange() {
		IndirectCostRecoveryAccountChange icr = new IndirectCostRecoveryAccountChange();
		icr.setIndirectCostRecoveryFinCoaCode(indirectCostRecoveryFinCoaCode);
		icr.setIndirectCostRecoveryAccountNumber(indirectCostRecoveryAccountNumber);
		icr.setAccountLinePercent(accountLinePercent);
		icr.setActive(active);
		return icr;

	}

}
