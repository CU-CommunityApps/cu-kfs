package edu.cornell.kfs.rass.batch.xml.fixture;

import java.util.Date;
import java.util.List;

import org.joda.time.format.DateTimeFormatter;

import edu.cornell.kfs.fp.batch.xml.fixture.AccountingXmlDocumentFixtureUtils;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapperMarshalTest;

public enum RassXmlDocumentWrapperFixture {
        RASS_EXAMPLE("2019-03-15T22:15:07.273", awardFixtures(RassXmlAwardEntryFixture.FIRST, RassXmlAwardEntryFixture.ANOTHER), 
                agencyFixtures(RassXmlAgencyEntryFixture.SOME, RassXmlAgencyEntryFixture.DoS, RassXmlAgencyEntryFixture.TEST));
    
    public final Date extractDate;
    public final List<RassXmlAwardEntryFixture> awards;
    public final List<RassXmlAgencyEntryFixture> agencies;
    
    private RassXmlDocumentWrapperFixture(String extractDateString, RassXmlAwardEntryFixture[] awardsArray, RassXmlAgencyEntryFixture[] agencyArray) {
        DateTimeFormatter dateformatter = RassXmlDocumentWrapperMarshalTest.getRASSDateTimeFormatter();
        extractDate = dateformatter.parseDateTime(extractDateString).toDate();
        awards = AccountingXmlDocumentFixtureUtils.toImmutableList(awardsArray);
        agencies = AccountingXmlDocumentFixtureUtils.toImmutableList(agencyArray);
    }
    
    public RassXmlDocumentWrapper toRassXmlDocumentWrapper() {
        RassXmlDocumentWrapper wrapper = new RassXmlDocumentWrapper();
        wrapper.setExtractDate(extractDate);
        for (RassXmlAgencyEntryFixture fixture : agencies) {
            wrapper.getAgencies().add(fixture.toRassXmlAgencyEntry());
        }
        for (RassXmlAwardEntryFixture fixture : awards) {
            wrapper.getAwards().add(fixture.toRassXmlAwardEntry());
        }
        return wrapper;
    }
    
    private static RassXmlAgencyEntryFixture[] agencyFixtures(RassXmlAgencyEntryFixture... fixtures) {
        return fixtures;
    }
    
    private static RassXmlAwardEntryFixture[] awardFixtures(RassXmlAwardEntryFixture... fixtures) {
        return fixtures;
    }

}
