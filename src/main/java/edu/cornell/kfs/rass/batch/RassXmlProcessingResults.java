package edu.cornell.kfs.rass.batch;

public class RassXmlProcessingResults {

    private final RassXmlObjectGroupResult agencyResults;
    private final RassXmlObjectGroupResult proposalResults;
    private final RassXmlObjectGroupResult awardResults;

    public RassXmlProcessingResults(
            RassXmlObjectGroupResult agencyResults, RassXmlObjectGroupResult proposalResults, RassXmlObjectGroupResult awardResults) {
        this.agencyResults = agencyResults;
        this.proposalResults = proposalResults;
        this.awardResults = awardResults;
    }

    public RassXmlObjectGroupResult getAgencyResults() {
        return agencyResults;
    }

    public RassXmlObjectGroupResult getProposalResults() {
        return proposalResults;
    }

    public RassXmlObjectGroupResult getAwardResults() {
        return awardResults;
    }

}
