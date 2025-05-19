package edu.cornell.kfs.module.cg.businessobject.fixture;

import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.cg.businessobject.Award;

import edu.cornell.kfs.module.ar.CuArTestConstants;

public enum AwardFixture {
    AWARD_12345_INV_AWARD(CuArTestConstants.PROPOSAL_12345, ArConstants.INV_AWARD),
    AWARD_66666_INV_AWARD(CuArTestConstants.PROPOSAL_66666, ArConstants.INV_AWARD),
    AWARD_97979_INV_AWARD(CuArTestConstants.PROPOSAL_97979, ArConstants.INV_AWARD),
    AWARD_30000_INV_ACCOUNT(CuArTestConstants.PROPOSAL_30000, ArConstants.INV_ACCOUNT),
    AWARD_11114_INV_ACCOUNT(CuArTestConstants.PROPOSAL_11114, ArConstants.INV_ACCOUNT),
    AWARD_24680_INV_SCHEDULE(CuArTestConstants.PROPOSAL_24680, ArConstants.INV_SCHEDULE),
    AWARD_97531_INV_SCHEDULE(CuArTestConstants.PROPOSAL_97531, ArConstants.INV_SCHEDULE),
    AWARD_33433_INV_CC_ACCOUNT(CuArTestConstants.PROPOSAL_33433, ArConstants.INV_CONTRACT_CONTROL_ACCOUNT),
    AWARD_99899_INV_CC_ACCOUNT(CuArTestConstants.PROPOSAL_99899, ArConstants.INV_CONTRACT_CONTROL_ACCOUNT);

    public final String proposalNumber;
    public final String invoicingOptionCode;

    private AwardFixture(String proposalNumber, String invoicingOptionCode) {
        this.proposalNumber = proposalNumber;
        this.invoicingOptionCode = invoicingOptionCode;
    }

    public Award toAward() {
        Award award = new Award();
        award.setProposalNumber(proposalNumber);
        award.setInvoicingOptionCode(invoicingOptionCode);
        return award;
    }

}
