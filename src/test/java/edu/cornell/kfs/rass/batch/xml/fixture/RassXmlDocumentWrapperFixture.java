package edu.cornell.kfs.rass.batch.xml.fixture;

import java.util.List;

import org.joda.time.DateTime;

import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;
import edu.cornell.kfs.sys.xmladapters.RassStringToJavaLongDateTimeAdapter;

public enum RassXmlDocumentWrapperFixture {
        RASS_EXAMPLE("2019-03-15T22:15:07.273", awardFixtures(RassXmlAwardEntryFixture.FIRST, RassXmlAwardEntryFixture.SECOND, RassXmlAwardEntryFixture.ANOTHER, RassXmlAwardEntryFixture.NULL_AMOUNTS),
                agencyFixtures(RassXmlAgencyEntryFixture.SOME, RassXmlAgencyEntryFixture.DoS, RassXmlAgencyEntryFixture.TEST)),
        RASS_AWARDS_ONLY("2019-04-15T22:15:07.273",
                awardFixtures(RassXmlAwardEntryFixture.FIRST, RassXmlAwardEntryFixture.ANOTHER, RassXmlAwardEntryFixture.NULL_AMOUNTS),
                agencyFixtures()),
        RASS_AGENCIES_ONLY("2019-03-19T22:15:07.273",
                awardFixtures(),
                agencyFixtures(RassXmlAgencyEntryFixture.SOME, RassXmlAgencyEntryFixture.DoS, RassXmlAgencyEntryFixture.TEST)),
        RASS_SINGLE_AGENCY_UPDATE_FILE("2019-03-15T22:15:07.273", awardFixtures(), agencyFixtures(RassXmlAgencyEntryFixture.SOME_V2)),
        RASS_SINGLE_AGENCY_UPDATE_FILE_V2(RASS_SINGLE_AGENCY_UPDATE_FILE),
        RASS_SINGLE_AGENCY_CREATE_FILE("2019-03-16T22:15:07.273", awardFixtures(), agencyFixtures(RassXmlAgencyEntryFixture.LIMITED_LTD)),
        RASS_SINGLE_AGENCY_CREATE_FILE_V2(RASS_SINGLE_AGENCY_CREATE_FILE),
        RASS_ANOTHER_SINGLE_AGENCY_CREATE_FILE("2019-03-16T22:15:08.278", awardFixtures(), agencyFixtures(RassXmlAgencyEntryFixture.UNLIMITED_LTD)),
        RASS_MULTIPLE_AGENCIES_CREATE_UPDATE_FILE("2019-03-17T22:15:07.273", awardFixtures(),
                agencyFixtures(RassXmlAgencyEntryFixture.LIMITED_LTD, RassXmlAgencyEntryFixture.SOME_V2)),
        RASS_MULTIPLE_AGENCIES_TIMEOUT_TEST_FILE("2019-03-17T22:15:07.277", awardFixtures(),
                agencyFixtures(RassXmlAgencyEntryFixture.SOME_V2, RassXmlAgencyEntryFixture.LIMITED_LTD)),
        RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE("2019-03-18T12:15:07.273", awardFixtures(), agencyFixtures(RassXmlAgencyEntryFixture.FIJI_DOT)),
        RASS_SINGLE_EXISTING_AGENCY_FILE("2019-03-18T22:15:07.273", awardFixtures(), agencyFixtures(RassXmlAgencyEntryFixture.SOME)),
        RASS_AGENCY_UPDATE_LENGTH_TRUNCATE_FILE("2019-03-18T22:15:37.273", awardFixtures(), agencyFixtures(RassXmlAgencyEntryFixture.DoS_LONG_DESC)),
        RASS_AGENCY_UPDATE_MISSING_FIELD_FILE("2019-03-18T22:15:38.273", awardFixtures(), agencyFixtures(RassXmlAgencyEntryFixture.SOME_V2_MISSING_REQ_FIELD)),
        RASS_LONG_AGENCY_NUMBER_CREATE_FILE("2019-03-18T22:15:38.279", awardFixtures(), agencyFixtures(RassXmlAgencyEntryFixture.LONG_KEY)),
        RASS_FORCE_AGENCY_GROUP_ERROR_FILE("2019-03-18T23:15:07.273", awardFixtures(), agencyFixtures(RassXmlAgencyEntryFixture.FORCE_ERROR)),
        RASS_SINGLE_AWARD_CREATE_FILE("2019-03-18T23:18:07.273", awardFixtures(RassXmlAwardEntryFixture.SAMPLE_PROJECT), agencyFixtures()),
        RASS_SINGLE_AWARD_UPDATE_FILE("2019-03-19T23:18:07.273", awardFixtures(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V2), agencyFixtures()),
        RASS_SINGLE_AWARD_ORG_UPDATE_FILE("2019-03-20T23:18:07.273",
                awardFixtures(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V3_ORG_CHANGE), agencyFixtures()),
        RASS_SINGLE_AWARD_DIRECTOR_UPDATE_FILE("2019-03-20T23:19:07.273",
                awardFixtures(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V4_DIRECTOR_CHANGE), agencyFixtures()),
        RASS_SINGLE_AWARD_DIRECTOR_UPDATE_FILE2("2019-03-20T23:19:08.273",
                awardFixtures(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V5_DIRECTOR_CHANGE2), agencyFixtures()),
        RASS_SINGLE_UNCHANGED_AWARD_FILE("2019-03-19T23:18:07.277", awardFixtures(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT), agencyFixtures()),
        RASS_DUPLICATED_NEW_AWARD_FILE("2019-03-20T23:19:08.973",
                awardFixtures(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassXmlAwardEntryFixture.SAMPLE_PROJECT), agencyFixtures()),
        RASS_AWARD_CREATE_MISSING_FIELD_FILE("2019-03-18T20:19:06.273",
                awardFixtures(RassXmlAwardEntryFixture.SAMPLE_PROJECT_MISSING_REQ_FIELD), agencyFixtures()),
        RASS_MULTIPLE_AGENCIES_AND_AWARDS_FILE("2019-03-20T23:59:59.273",
                awardFixtures(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V2),
                agencyFixtures(RassXmlAgencyEntryFixture.LIMITED_LTD, RassXmlAgencyEntryFixture.SOME_V2)),
        RASS_EMPTY_FILE("2019-03-18T23:15:07.999", awardFixtures(), agencyFixtures()),
        RASS_AWARD_ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_EXISTS_AWARD_EXISTS("2019-03-15T22:15:07.273", awardFixtures(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_EXISTS_AWARD_EXISTS),
                agencyFixtures()),
        RASS_AWARD_ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_EXISTS_AWARD_NOT_EXISTS("2019-03-15T22:15:07.273", awardFixtures(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_EXISTS_AWARD_NOT_EXISTS),
                agencyFixtures()),
        RASS_AWARD_ZERO_AMOUNT_COST_SHARE_NO_PROPOSAL_EXISTS_AWARD_EXISTS("2019-03-15T22:15:07.273", awardFixtures(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_NO_PROPOSAL_EXISTS_AWARD_EXISTS),
                agencyFixtures()),
        RASS_AWARD_ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_DOES_NOT_EXIST_AWARD_EXISTS("2019-03-15T22:15:07.273", awardFixtures(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_DOES_NOT_EXIST_AWARD_EXISTS),
                agencyFixtures()),
        RASS_AWARD_ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_DOES_NOT_EXIST_AWARD_NOT_EXISTS("2019-03-15T22:15:07.273", awardFixtures(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_DOES_NOT_EXIST_AWARD_NOT_EXISTS),
                agencyFixtures()),
        RASS_AWARD_ZERO_AMOUNT_COST_SHARE_NO_PROPOSAL_DOES_NOT_EXIST("2019-03-15T22:15:07.273", awardFixtures(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_NO_PROPOSAL_DOES_NOT_EXIST),
                agencyFixtures()),
        RASS_AWARD_WITH_VARIOUS_NULL_FIELDS("2019-03-15T22:15:07.274",
                awardFixtures(RassXmlAwardEntryFixture.SAMPLE_PROJECT_NULL_FIELDS_TEST),
                agencyFixtures()),
        RASS_AWARD_SKIP_DUE_TO_NULLS("2019-03-15T22:15:07.275",
                awardFixtures(RassXmlAwardEntryFixture.SAMPLE_PROJECT_SKIP_DUE_TO_NULLS),
                agencyFixtures()),
        RASS_AWARD_SKIP_DUE_TO_NULLS_V2("2019-03-15T22:15:07.276",
                awardFixtures(RassXmlAwardEntryFixture.SAMPLE_PROJECT_SKIP_DUE_TO_NULLS_V2),
                agencyFixtures()),
        RASS_AWARD_SKIP_DUE_TO_NULLS_V3("2019-03-15T22:15:07.277",
                awardFixtures(RassXmlAwardEntryFixture.SAMPLE_PROJECT_SKIP_DUE_TO_NULLS_V3),
                agencyFixtures());
    
    
    public final DateTime extractDate;
    public final List<RassXmlAwardEntryFixture> awards;
    public final List<RassXmlAgencyEntryFixture> agencies;
    
    private RassXmlDocumentWrapperFixture(RassXmlDocumentWrapperFixture fixtureToCopy) {
        this.extractDate = fixtureToCopy.extractDate;
        this.awards = fixtureToCopy.awards;
        this.agencies = fixtureToCopy.agencies;
    }
    
    private RassXmlDocumentWrapperFixture(String extractDateString, RassXmlAwardEntryFixture[] awardsArray, RassXmlAgencyEntryFixture[] agencyArray) {
        extractDate = RassStringToJavaLongDateTimeAdapter.parseToDateTime(extractDateString);
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
    
    public String getGeneratedFileName() {
        return name() + CUKFSConstants.XML_FILE_EXTENSION;
    }
    
    private static RassXmlAgencyEntryFixture[] agencyFixtures(RassXmlAgencyEntryFixture... fixtures) {
        return fixtures;
    }
    
    private static RassXmlAwardEntryFixture[] awardFixtures(RassXmlAwardEntryFixture... fixtures) {
        return fixtures;
    }

}
