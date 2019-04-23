package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;

public class RassXmlProcessingResults {

    private final RassBusinessObjectUpdateResultGrouping<Agency> agencyResults;
    private final RassBusinessObjectUpdateResultGrouping<Proposal> proposalResults;
    private final RassBusinessObjectUpdateResultGrouping<Award> awardResults;

    public RassXmlProcessingResults(
            RassBusinessObjectUpdateResultGrouping<Agency> agencyResults, RassBusinessObjectUpdateResultGrouping<Proposal> proposalResults,
            RassBusinessObjectUpdateResultGrouping<Award> awardResults) {
        this.agencyResults = agencyResults;
        this.proposalResults = proposalResults;
        this.awardResults = awardResults;
    }

    public RassBusinessObjectUpdateResultGrouping<Agency> getAgencyResults() {
        return agencyResults;
    }

    public RassBusinessObjectUpdateResultGrouping<Proposal> getProposalResults() {
        return proposalResults;
    }

    public RassBusinessObjectUpdateResultGrouping<Award> getAwardResults() {
        return awardResults;
    }

}
