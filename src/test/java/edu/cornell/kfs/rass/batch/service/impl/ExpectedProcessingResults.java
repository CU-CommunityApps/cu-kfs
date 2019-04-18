package edu.cornell.kfs.rass.batch.service.impl;

import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAgencyEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAwardEntryFixture;

public final class ExpectedProcessingResults {

    private final ExpectedObjectGroupResult<RassXmlAgencyEntryFixture> expectedAgencyResults;
    private final ExpectedObjectGroupResult<RassXmlAwardEntryFixture> expectedProposalResults;
    private final ExpectedObjectGroupResult<RassXmlAwardEntryFixture> expectedAwardResults;

    public ExpectedProcessingResults(
            ExpectedObjectGroupResult<RassXmlAgencyEntryFixture> expectedAgencyResults,
            ExpectedObjectGroupResult<RassXmlAwardEntryFixture> expectedProposalResults,
            ExpectedObjectGroupResult<RassXmlAwardEntryFixture> expectedAwardResults) {
        this.expectedAgencyResults = expectedAgencyResults;
        this.expectedProposalResults = expectedProposalResults;
        this.expectedAwardResults = expectedAwardResults;
    }

    public ExpectedObjectGroupResult<RassXmlAgencyEntryFixture> getExpectedAgencyResults() {
        return expectedAgencyResults;
    }

    public ExpectedObjectGroupResult<RassXmlAwardEntryFixture> getExpectedProposalResults() {
        return expectedProposalResults;
    }

    public ExpectedObjectGroupResult<RassXmlAwardEntryFixture> getExpectedAwardResults() {
        return expectedAwardResults;
    }

}
