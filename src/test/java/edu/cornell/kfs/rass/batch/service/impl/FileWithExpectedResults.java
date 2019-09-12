package edu.cornell.kfs.rass.batch.service.impl;

import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;

import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAgencyEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAwardEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlDocumentWrapperFixture;

public final class FileWithExpectedResults {

    private final RassXmlDocumentWrapperFixture fileFixture;
    private final ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> expectedAgencyResults;
    private final ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Proposal> expectedProposalResults;
    private final ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> expectedAwardResults;

    public FileWithExpectedResults(
            RassXmlDocumentWrapperFixture fileFixture,
            ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> expectedAgencyResults,
            ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Proposal> expectedProposalResults,
            ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> expectedAwardResults) {
        this.fileFixture = fileFixture;
        this.expectedAgencyResults = expectedAgencyResults;
        this.expectedProposalResults = expectedProposalResults;
        this.expectedAwardResults = expectedAwardResults;
    }

    public RassXmlDocumentWrapperFixture getFileFixture() {
        return fileFixture;
    }

    public String getExpectedXmlFileName() {
        return fileFixture.getGeneratedFileName();
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
