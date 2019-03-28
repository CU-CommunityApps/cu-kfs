package edu.cornell.kfs.rass.batch.xml.fixture;

import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;

import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;
import edu.cornell.kfs.sys.xmladapters.RassStringToJavaLongDateTimeAdapter;

public enum RassXmlDocumentWrapperFixture {
        RASS_EXAMPLE("2019-03-15T22:15:07.273", awardFixtures(RassXmlAwardEntryFixture.FIRST, RassXmlAwardEntryFixture.ANOTHER, RassXmlAwardEntryFixture.NULL_AMOUNTS), 
                agencyFixtures(RassXmlAgencyEntryFixture.SOME, RassXmlAgencyEntryFixture.DoS, RassXmlAgencyEntryFixture.TEST));
    
    public final DateTime extractDate;
    public final List<RassXmlAwardEntryFixture> awards;
    public final List<RassXmlAgencyEntryFixture> agencies;
    
    private RassXmlDocumentWrapperFixture(String extractDateString, RassXmlAwardEntryFixture[] awardsArray, RassXmlAgencyEntryFixture[] agencyArray) {
        extractDate = new DateTime(RassStringToJavaLongDateTimeAdapter.parseToDateTime(extractDateString).toDate());
        awards = XmlDocumentFixtureUtils.toImmutableList(awardsArray);
        agencies = XmlDocumentFixtureUtils.toImmutableList(agencyArray);
    }
    
    public RassXmlDocumentWrapper toRassXmlDocumentWrapper() {
        RassXmlDocumentWrapper wrapper = new RassXmlDocumentWrapper();
        wrapper.setExtractDate(extractDate.toDate());
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
