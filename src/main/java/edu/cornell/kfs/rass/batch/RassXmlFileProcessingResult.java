package edu.cornell.kfs.rass.batch;

import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;

public class RassXmlFileProcessingResult {

    private final String rassXmlFileName;
    private final RassBusinessObjectUpdateResultGrouping<Agency> agencyResults;
    private final RassBusinessObjectUpdateResultGrouping<Proposal> proposalResults;
    private final RassBusinessObjectUpdateResultGrouping<Award> awardResults;

    public RassXmlFileProcessingResult(
            String rassXmlFileName, RassBusinessObjectUpdateResultGrouping<Agency> agencyResults,
            RassBusinessObjectUpdateResultGrouping<Proposal> proposalResults,
            RassBusinessObjectUpdateResultGrouping<Award> awardResults) {
        this.rassXmlFileName = rassXmlFileName;
        this.agencyResults = agencyResults;
        this.proposalResults = proposalResults;
        this.awardResults = awardResults;
    }

    public String getRassXmlFileName() {
        return rassXmlFileName;
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
