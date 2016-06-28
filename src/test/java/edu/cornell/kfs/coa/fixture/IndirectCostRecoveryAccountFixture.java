package edu.cornell.kfs.coa.fixture;

import java.math.BigDecimal;

import org.kuali.kfs.coa.businessobject.IndirectCostRecoveryAccount;

public enum IndirectCostRecoveryAccountFixture {
	ICR_1111111_98_PERCENT_ACTIVE("IT", "1111111", new BigDecimal(98), true),

	ICR_1111111_98_PERCENT_INACTIVE("IT", "1111111", new BigDecimal(98), false),

	ICR_2222222_2_PERCENT_ACTIVE("IT", "2222222", new BigDecimal(2), true),

	ICR_2222222_2_PERCENT_INACTIVE("IT", "2222222", new BigDecimal(2), false),

	ICR_3333333_100_PERCENT_ACTIVE("IT", "3333333", new BigDecimal(100), true),

	ICR_3333333_100_PERCENT_INACTIVE("IT", "3333333", new BigDecimal(100),
			false);

	public String indirectCostRecoveryFinCoaCode;
	public String indirectCostRecoveryAccountNumber;
	public BigDecimal accountLinePercent;
	public boolean active;

	private IndirectCostRecoveryAccountFixture(
			String indirectCostRecoveryFinCoaCode,
			String indirectCostRecoveryAccountNumber,
			BigDecimal accountLinePercent, boolean active) {
		this.indirectCostRecoveryFinCoaCode = indirectCostRecoveryFinCoaCode;
		this.indirectCostRecoveryAccountNumber = indirectCostRecoveryAccountNumber;
		this.accountLinePercent = accountLinePercent;
		this.active = active;
	}

	public IndirectCostRecoveryAccount getIndirectCostRecoveryAccountChange() {
		IndirectCostRecoveryAccount icr = new IndirectCostRecoveryAccount();
		icr.setIndirectCostRecoveryFinCoaCode(indirectCostRecoveryFinCoaCode);
		icr.setIndirectCostRecoveryAccountNumber(indirectCostRecoveryAccountNumber);
		icr.setAccountLinePercent(accountLinePercent);
		icr.setActive(active);
		return icr;

	}

}
