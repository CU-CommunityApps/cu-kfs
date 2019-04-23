package edu.cornell.kfs.rass.batch.service.impl;

import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;

import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAgencyEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAwardEntryFixture;

public final class ExpectedProcessingResults {

    private final ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> expectedAgencyResults;
    private final ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Proposal> expectedProposalResults;
    private final ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> expectedAwardResults;

    public ExpectedProcessingResults(
            ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> expectedAgencyResults,
            ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Proposal> expectedProposalResults,
            ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> expectedAwardResults) {
        this.expectedAgencyResults = expectedAgencyResults;
        this.expectedProposalResults = expectedProposalResults;
        this.expectedAwardResults = expectedAwardResults;
    }

    public ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> getExpectedAgencyResults() {
        return expectedAgencyResults;
    }

    public ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Proposal> getExpectedProposalResults() {
        return expectedProposalResults;
    }

    public ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> getExpectedAwardResults() {
        return expectedAwardResults;
    }

}
