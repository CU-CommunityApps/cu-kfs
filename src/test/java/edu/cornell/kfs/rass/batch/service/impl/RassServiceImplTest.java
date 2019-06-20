package edu.cornell.kfs.rass.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.Maintainable;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardOrganization;
import org.kuali.kfs.module.cg.businessobject.CGProjectDirector;
import org.kuali.kfs.module.cg.businessobject.Primaryable;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.businessobject.ProposalOrganization;
import org.kuali.kfs.module.cg.service.AgencyService;
import org.kuali.kfs.module.cg.service.AwardService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.routeheader.service.RouteHeaderService;
import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtendedAttribute;
import edu.cornell.kfs.module.cg.businessobject.AwardExtendedAttribute;
import edu.cornell.kfs.rass.RassConstants.RassObjectGroupingUpdateResultCode;
import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;
import edu.cornell.kfs.rass.RassConstants.RassParseResultCode;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResult;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResultGrouping;
import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.RassXmlProcessingResults;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXMLAwardPiCoPiEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAgencyEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAwardEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlDocumentWrapperFixture;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.LoadSpringFile;
import edu.cornell.kfs.sys.util.SpringEnabledMicroTestBase;

@LoadSpringFile("edu/cornell/kfs/rass/batch/cu-spring-rass-service-test.xml")
public class RassServiceImplTest extends SpringEnabledMicroTestBase {

    private AgencyService mockAgencyService;
    private BusinessObjectService mockBusinessObjectService;
    private AwardService mockAwardService;
    private TestRassServiceImpl rassService;
    private TestRassRoutingServiceImpl rassRoutingService;

    private List<Maintainable> agencyUpdates;
    private List<Maintainable> proposalUpdates;
    private List<Maintainable> awardUpdates;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        agencyUpdates = new ArrayList<>();
        proposalUpdates = new ArrayList<>();
        awardUpdates = new ArrayList<>();
        mockAgencyService = springContext.getBean(RassTestConstants.AGENCY_SERVICE_BEAN_NAME, AgencyService.class);
        mockBusinessObjectService = springContext.getBean(RassTestConstants.BUSINESS_OBJECT_SERVICE_BEAN_NAME, BusinessObjectService.class);
        mockAwardService = springContext.getBean(RassTestConstants.AWARD_SERVICE_BEAN_NAME, AwardService.class);
        rassService = springContext.getBean(RassTestConstants.RASS_SERVICE_BEAN_NAME, TestRassServiceImpl.class);
        
        rassRoutingService = springContext.getBean(RassTestConstants.RASS_ROUTING_SERVICE_BEAN_NAME, TestRassRoutingServiceImpl.class);
        rassRoutingService.setDocumentTracker(this::processMaintenanceDocument);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        agencyUpdates = null;
        mockAgencyService = null;
        rassService = null;
        rassRoutingService = null;
    }

    @Test
    public void testLoadEmptyFile() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_EMPTY_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testUpdateSingleAgency() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testCreateSingleAgency() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testCreateAndUpdateMultipleAgenciesFromSingleFile() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_MULTIPLE_AGENCIES_CREATE_UPDATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testUpdateAgencyWithTextFieldsExceedingMaxLength() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_AGENCY_UPDATE_LENGTH_TRUNCATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.DoS_LONG_DESC, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testUpdateAgencyWithMissingRequiredField() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_AGENCY_UPDATE_MISSING_FIELD_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2_MISSING_REQ_FIELD, RassObjectUpdateResultCode.ERROR)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testCreateAgencyWithTruncatedDocDescription() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_LONG_AGENCY_NUMBER_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LONG_KEY, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testHandleSingleExistingAgencyWithNoChanges() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_EXISTING_AGENCY_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME, RassObjectUpdateResultCode.SKIPPED)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testUpdateAgencyAndIgnoreSubsequentDuplicateUpdate() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SKIPPED)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testCreateAgencyAndIgnoreSubsequentDuplicateCreate() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SKIPPED)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testLoadMultipleFilesWithAgencies() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_MULTIPLE_AGENCIES_CREATE_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SKIPPED),
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testWaitForRouteStatusAfterUpdateToSameAgency() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_PROCESSED_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SKIPPED)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testWaitForRouteStatusAfterUpdateToReferencedAgency() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_PROCESSED_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testTimeoutOfRouteStatusCheck() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_ENROUTE_CD,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_PROCESSED_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.ERROR)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testRouteStatusCheckErrorWhenDocumentEntersUnsuccessfulStatus() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.ERROR)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testErrorObjectCausesSubsequentUpdatesReferencingErrorObjectToFail() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_ANOTHER_SINGLE_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.ERROR),
                                agency(RassXmlAgencyEntryFixture.UNLIMITED_LTD, RassObjectUpdateResultCode.ERROR)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testErrorObjectDoesNotAffectSubsequentUnrelatedObjects() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.ERROR),
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testWaitOnlyAtEndIfObjectUpdatesDoNotReferencePriorOnes() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_PROCESSED_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testHandleRoutingTimeoutQuietlyIfObjectUpdatesDoNotReferencePriorOnes() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_ENROUTE_CD,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_PROCESSED_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testHandleRouteStatusCheckErrorQuietlyIfObjectUpdatesDoNotReferencePriorOnes() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testHandleErrorAtObjectGroupLevel() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_FORCE_AGENCY_GROUP_ERROR_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS)));
    }

    @Test
    public void testCreateSingleProposalAndAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_CREATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS,
                                proposal(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW))));
    }

    @Test
    public void testUpdateSingleAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_UPDATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS,
                                proposal(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT, RassObjectUpdateResultCode.SKIPPED)),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V2, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testUpdateOrgCodeOnExistingAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_ORG_UPDATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS,
                                proposal(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT, RassObjectUpdateResultCode.SKIPPED)),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V3_ORG_CHANGE, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testUpdateDirectorsOnExistingAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_DIRECTOR_UPDATE_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS,
                                proposal(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT, RassObjectUpdateResultCode.SKIPPED)),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V4_DIRECTOR_CHANGE, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testAlternateUpdateOfDirectorsOnExistingAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_DIRECTOR_UPDATE_FILE2),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS,
                                proposal(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT, RassObjectUpdateResultCode.SKIPPED)),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V5_DIRECTOR_CHANGE2, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testSkipUpdatesWhenLoadingUnchangedAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_UNCHANGED_AWARD_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS,
                                proposal(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT, RassObjectUpdateResultCode.SKIPPED)),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT, RassObjectUpdateResultCode.SKIPPED))));
    }

    @Test
    public void testLoadNewAwardAndSkipSubsequentDuplicate() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_DUPLICATED_NEW_AWARD_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS,
                                proposal(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW),
                                proposal(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SKIPPED)),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW),
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SKIPPED))));
    }

    @Test
    public void testLoadAwardWithMissingRequiredField() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_CREATE_MISSING_FIELD_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS),
                        proposals(RassObjectGroupingUpdateResultCode.ERROR,
                                proposal(RassXmlAwardEntryFixture.SAMPLE_PROJECT_MISSING_REQ_FIELD, RassObjectUpdateResultCode.ERROR)),
                        awards(RassObjectGroupingUpdateResultCode.ERROR,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_MISSING_REQ_FIELD, RassObjectUpdateResultCode.ERROR))));
    }

    @Test
    public void testLoadMultipleAgenciesAndProposalsAndAwards() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_MULTIPLE_AGENCIES_AND_AWARDS_FILE),
                expectedResults(
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        proposals(RassObjectGroupingUpdateResultCode.SUCCESS,
                                proposal(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW),
                                proposal(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT, RassObjectUpdateResultCode.SKIPPED)),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW),
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V2, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    private void assertXmlContentsPerformExpectedObjectUpdates(List<RassXmlDocumentWrapperFixture> xmlContents,
            ExpectedProcessingResults expectedProcessingResults) throws Exception {
        List<RassXmlFileParseResult> fileResults = xmlContents.stream()
                .map(RassXmlDocumentWrapperFixture::toRassXmlDocumentWrapper)
                .map(this::encaseWrapperInSuccessfulFileResult)
                .collect(Collectors.toCollection(ArrayList::new));
        
        RassXmlProcessingResults actualResults = rassService.updateKFS(fileResults);
        
        assertAgenciesWereUpdatedAndReportedAsExpected(expectedProcessingResults, actualResults);
        assertProposalsWereUpdatedAndReportedAsExpected(expectedProcessingResults, actualResults);
        assertAwardsWereUpdatedAndReportedAsExpected(expectedProcessingResults, actualResults);
    }

    private RassXmlFileParseResult encaseWrapperInSuccessfulFileResult(RassXmlDocumentWrapper documentWrapper) {
        return new RassXmlFileParseResult(KFSConstants.EMPTY_STRING, RassParseResultCode.SUCCESS, Optional.of(documentWrapper));
    }

    private void assertAgenciesWereUpdatedAndReportedAsExpected(
            ExpectedProcessingResults expectedProcessingResults, RassXmlProcessingResults actualProcessingResults) {
        ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> expectedAgencyResultGrouping = expectedProcessingResults
                .getExpectedAgencyResults();
        RassBusinessObjectUpdateResultGrouping<Agency> actualAgencyResultGrouping = actualProcessingResults.getAgencyResults();
        
        assertCorrectObjectResultsWereReported(expectedAgencyResultGrouping, actualAgencyResultGrouping);
        assertObjectsWereUpdatedAsExpected(expectedAgencyResultGrouping, agencyUpdates,
                this::assertAgencyWasUpdatedAsExpected);
    }

    private void assertProposalsWereUpdatedAndReportedAsExpected(
            ExpectedProcessingResults expectedProcessingResults, RassXmlProcessingResults actualProcessingResults) {
        ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Proposal> expectedProposalResultGrouping = expectedProcessingResults
                .getExpectedProposalResults();
        RassBusinessObjectUpdateResultGrouping<Proposal> actualProposalResultGrouping = actualProcessingResults.getProposalResults();
        
        assertCorrectObjectResultsWereReported(expectedProposalResultGrouping, actualProposalResultGrouping);
        assertObjectsWereUpdatedAsExpected(expectedProposalResultGrouping, proposalUpdates,
                this::assertProposalWasUpdatedAsExpected);
    }

    private void assertAwardsWereUpdatedAndReportedAsExpected(
            ExpectedProcessingResults expectedProcessingResults, RassXmlProcessingResults actualProcessingResults) {
        ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> expectedProposalResultGrouping = expectedProcessingResults
                .getExpectedAwardResults();
        RassBusinessObjectUpdateResultGrouping<Award> actualAwardResultGrouping = actualProcessingResults.getAwardResults();
        
        assertCorrectObjectResultsWereReported(expectedProposalResultGrouping, actualAwardResultGrouping);
        assertObjectsWereUpdatedAsExpected(expectedProposalResultGrouping, awardUpdates,
                this::assertAwardWasUpdatedAsExpected);
    }

    private <E extends Enum<E>, R extends PersistableBusinessObject> void assertCorrectObjectResultsWereReported(
            ExpectedObjectUpdateResultGrouping<E, R> expectedResultGrouping, RassBusinessObjectUpdateResultGrouping<R> actualResultGrouping) {
        Class<?> expectedBusinessObjectClass = expectedResultGrouping.getBusinessObjectClass();
        List<ExpectedObjectUpdateResult<E>> expectedResults = expectedResultGrouping.getExpectedObjectUpdateResults();
        List<RassBusinessObjectUpdateResult<R>> actualResults = actualResultGrouping.getObjectResults();
        
        assertEquals("Wrong businessobject class for result grouping", expectedBusinessObjectClass, actualResultGrouping.getBusinessObjectClass());
        assertEquals("Wrong result code for grouping", expectedResultGrouping.getResultCode(), actualResultGrouping.getResultCode());
        assertEquals("Wrong number of processing results for type " + expectedBusinessObjectClass, expectedResults.size(), actualResults.size());
        
        for (int i = 0; i < expectedResults.size(); i++) {
            ExpectedObjectUpdateResult<E> expectedResult = expectedResults.get(i);
            RassBusinessObjectUpdateResult<R> actualResult = actualResults.get(i);
            assertEquals("Wrong business object class at index " + i, expectedBusinessObjectClass, actualResult.getBusinessObjectClass());
            assertEquals("Wrong object primary key at index " + i, expectedResult.getPrimaryKey(), actualResult.getPrimaryKey());
            assertEquals("Wrong result code at index " + i, expectedResult.getResultCode(), actualResult.getResultCode());
            if (RassObjectUpdateResultCode.isSuccessfulResult(expectedResult.getResultCode())) {
                assertTrue("A document should have been created for successful result at index " + i,
                        StringUtils.isNotBlank(actualResult.getDocumentId()));
            } else {
                assertTrue("A document should not have been created for result at index " + i,
                        StringUtils.isBlank(actualResult.getDocumentId()));
            }
        }
    }

    private <E extends Enum<E>, R extends PersistableBusinessObject> void assertObjectsWereUpdatedAsExpected(
            ExpectedObjectUpdateResultGrouping<E, R> expectedResultGrouping, List<Maintainable> actualResults,
            ObjectUpdateValidator<E, R> resultValidator) {
        
        List<ExpectedObjectUpdateResult<E>> expectedResults = findExpectedSuccessfulResults(
                expectedResultGrouping.getExpectedObjectUpdateResults());
        Class<R> businessObjectClass = expectedResultGrouping.getBusinessObjectClass();
        String objectLabel = businessObjectClass.getSimpleName();
        
        assertEquals("Wrong number of " + objectLabel + " objects created or updated", expectedResults.size(), actualResults.size());
        for (int i = 0; i < expectedResults.size(); i++) {
            ExpectedObjectUpdateResult<E> expectedResult = expectedResults.get(i);
            Maintainable actualResult = actualResults.get(i);
            assertEquals("Wrong maintenance action for " + objectLabel + " at index " + i,
                    expectedResult.getExpectedMaintenanceAction(), actualResult.getMaintenanceAction());
            
            E expectedObjectFixture = expectedResult.getBusinessObjectFixture();
            R actualObject = businessObjectClass.cast(actualResult.getDataObject());
            resultValidator.assertObjectWasUpdatedAsExpected(expectedObjectFixture, actualObject, i);
        }
    }

    private void assertAgencyWasUpdatedAsExpected(RassXmlAgencyEntryFixture expectedAgency, Agency actualAgency, int i) {
        assertEqualsOrBothBlank("Wrong agency number at index " + i, expectedAgency.number, actualAgency.getAgencyNumber());
        assertEqualsOrBothBlank("Wrong reporting name at index " + i, expectedAgency.reportingName, actualAgency.getReportingName());
        assertEqualsOrBothBlank("Wrong full name at index " + i, expectedAgency.getTruncatedFullName(), actualAgency.getFullName());
        assertEqualsOrBothBlank("Wrong type code at index " + i, expectedAgency.typeCode, actualAgency.getAgencyTypeCode());
        assertEqualsOrBothBlank("Wrong reports-to agency number at index " + i,
                expectedAgency.reportsToAgencyNumber, actualAgency.getReportsToAgencyNumber());
        
        AgencyExtendedAttribute actualExtension = (AgencyExtendedAttribute) actualAgency.getExtension();
        assertEqualsOrBothBlank("Wrong common name at index " + i,
                expectedAgency.getTruncatedCommonName(), actualExtension.getAgencyCommonName());
        assertEqualsOrBothBlank("Wrong origin code at index " + i, expectedAgency.agencyOrigin, actualExtension.getAgencyOriginCode());
    }

    private void assertProposalWasUpdatedAsExpected(RassXmlAwardEntryFixture expectedProposal, Proposal actualProposal, int i) {
        List<ProposalOrganization> proposalOrganizations = actualProposal.getProposalOrganizations();
        assertEquals("Wrong number of proposal organizations at index " + i, 1, proposalOrganizations.size());
        ProposalOrganization proposalOrganization = proposalOrganizations.get(0);
        assertNotNull("Proposal organization should not have been null at index " + i, proposalOrganization);
        
        assertEqualsOrBothBlank("Wrong proposal number at index " + i, expectedProposal.proposalNumber, actualProposal.getProposalNumber());
        assertEqualsOrBothBlank("Wrong proposal status at index " + i, expectedProposal.status, actualProposal.getProposalStatusCode());
        assertEqualsOrBothBlank("Wrong agency number at index " + i, expectedProposal.agencyNumber, actualProposal.getAgencyNumber());
        assertEqualsOrBothBlank("Wrong project title at index " + i, expectedProposal.projectTitle, actualProposal.getProposalProjectTitle());
        assertEquals("Wrong start date at index " + i, expectedProposal.getStartDateAsSqlDate(), actualProposal.getProposalBeginningDate());
        assertEquals("Wrong stop date at index " + i, expectedProposal.getStopDateAsSqlDate(), actualProposal.getProposalEndingDate());
        assertEquals("Wrong direct cost amount at index " + i, expectedProposal.directCostAmount, actualProposal.getProposalDirectCostAmount());
        assertEquals("Wrong indirect cost amount at index " + i, expectedProposal.indirectCostAmount, actualProposal.getProposalIndirectCostAmount());
        assertEqualsOrBothBlank("Wrong proposal purpose at index " + i, expectedProposal.purpose, actualProposal.getProposalPurposeCode());
        assertEqualsOrBothBlank("Wrong grant number at index " + i, expectedProposal.grantNumber, actualProposal.getGrantNumber());
        assertEquals("Wrong federal pass-through indicator at index " + i,
                expectedProposal.federalPassThrough, actualProposal.getProposalFederalPassThroughIndicator());
        assertEqualsOrBothBlank("Wrong federal pass-through agency number at index " + i,
                expectedProposal.federalPassThroughAgencyNumber, actualProposal.getFederalPassThroughAgencyNumber());
        assertEqualsOrBothBlank("Wrong CFDA number at index " + i, expectedProposal.cfdaNumber, actualProposal.getCfdaNumber());
        assertEqualsOrBothBlank("Wrong organization code at index " + i,
                expectedProposal.organizationCode, proposalOrganization.getOrganizationCode());
        
        assertProjectDirectorsWereUpdatedAsExpected(expectedProposal, actualProposal.getProposalProjectDirectors(), i);
    }

    private void assertAwardWasUpdatedAsExpected(RassXmlAwardEntryFixture expectedAward, Award actualAward, int i) {
        assertEqualsOrBothBlank("Wrong proposal number at index " + i, expectedAward.proposalNumber, actualAward.getProposalNumber());
        assertEqualsOrBothBlank("Wrong award status at index " + i, expectedAward.status, actualAward.getAwardStatusCode());
        assertEqualsOrBothBlank("Wrong agency number at index " + i, expectedAward.agencyNumber, actualAward.getAgencyNumber());
        assertEqualsOrBothBlank("Wrong project title at index " + i, expectedAward.projectTitle, actualAward.getAwardProjectTitle());
        assertEquals("Wrong start date at index " + i, expectedAward.getStartDateAsSqlDate(), actualAward.getAwardBeginningDate());
        assertEquals("Wrong stop date at index " + i, expectedAward.getStopDateAsSqlDate(), actualAward.getAwardEndingDate());
        assertEquals("Wrong direct cost amount at index " + i, expectedAward.directCostAmount, actualAward.getAwardDirectCostAmount());
        assertEquals("Wrong indirect cost amount at index " + i, expectedAward.indirectCostAmount, actualAward.getAwardIndirectCostAmount());
        assertEqualsOrBothBlank("Wrong award purpose at index " + i, expectedAward.purpose, actualAward.getAwardPurposeCode());
        assertEqualsOrBothBlank("Wrong grant description at index " + i, expectedAward.grantDescription, actualAward.getGrantDescriptionCode());
        assertEquals("Wrong federal pass-through indicator at index " + i,
                expectedAward.federalPassThrough, actualAward.getFederalPassThroughIndicator());
        assertEqualsOrBothBlank("Wrong federal pass-through agency number at index " + i,
                expectedAward.federalPassThroughAgencyNumber, actualAward.getFederalPassThroughAgencyNumber());
        
        AwardExtendedAttribute actualExtension = (AwardExtendedAttribute) actualAward.getExtension();
        assertEquals("Wrong cost share required indicator at index " + i,
                expectedAward.costShareRequired, actualExtension.isCostShareRequired());
        assertEquals("Wrong final fiscal report date at index " + i,
                expectedAward.getFinalReportDueDateAsSqlDate(), actualExtension.getFinalFiscalReportDate());
        
        assertAwardOrganizationsWereUpdatedAsExpected(expectedAward.organizations, actualAward.getAwardOrganizations(), i);
        assertProjectDirectorsWereUpdatedAsExpected(expectedAward, actualAward.getAwardProjectDirectors(), i);
    }

    private void assertAwardOrganizationsWereUpdatedAsExpected(
            List<Pair<String, Boolean>> expectedOrganizations, List<AwardOrganization> actualOrganizations, int i) {
        assertEquals("Wrong number of award organizations at index " + i, expectedOrganizations.size(), actualOrganizations.size());
        for (int j = 0; j < expectedOrganizations.size(); j++) {
            String multiIndex = i + KFSConstants.COMMA + j;
            Pair<String, Boolean> expectedOrgData = expectedOrganizations.get(j);
            AwardOrganization actualOrganization = actualOrganizations.get(j);
            assertEquals("Wrong org code for organization at index " + multiIndex,
                    expectedOrgData.getLeft(), actualOrganization.getOrganizationCode());
            assertEquals("Wrong primary indicator for organization at index " + multiIndex,
                    true, actualOrganization.isPrimary());
            assertEquals("Wrong active indicator for organization at index " + multiIndex,
                    expectedOrgData.getRight(), actualOrganization.isActive());
        }
    }

    private void assertProjectDirectorsWereUpdatedAsExpected(
            RassXmlAwardEntryFixture expectedAwardOrProposal, List<? extends CGProjectDirector> actualDirectors, int i) {
        List<RassXMLAwardPiCoPiEntryFixture> expectedDirectors = expectedAwardOrProposal.piFixtures;
        assertEquals("Wrong number of directors at index " + i, expectedDirectors.size(), actualDirectors.size());
        
        for (int j = 0; j < expectedDirectors.size(); j++) {
            String multiIndex = i + KFSConstants.COMMA + j;
            RassXMLAwardPiCoPiEntryFixture expectedDirector = expectedDirectors.get(j);
            CGProjectDirector actualDirector = actualDirectors.get(j);
            assertEquals("Wrong proposal number for director at index " + multiIndex,
                    expectedAwardOrProposal.proposalNumber, actualDirector.getProposalNumber());
            assertEquals("Wrong principal ID/name for director at index " + multiIndex
                    + " in spite of principalId/principalName equivalency for this test scenario",
                    expectedDirector.projectDirectorPrincipalName, actualDirector.getPrincipalId());
            assertTrue("A primary director indicator should have been supported for director at index " + multiIndex,
                    actualDirector instanceof Primaryable);
            assertEquals("Wrong primary director indicator for director at index " + multiIndex,
                    expectedDirector.primary, ((Primaryable) actualDirector).isPrimary());
        }
    }

    private void assertEqualsOrBothBlank(String message, String expected, String actual) {
        assertEquals(message, StringUtils.defaultIfBlank(expected, null), StringUtils.defaultIfBlank(actual, null));
    }

    private <E extends Enum<E>> List<ExpectedObjectUpdateResult<E>> findExpectedSuccessfulResults(
            List<ExpectedObjectUpdateResult<E>> expectedResults) {
        return expectedResults.stream()
                .filter(expectedResult -> RassObjectUpdateResultCode.isSuccessfulResult(expectedResult.getResultCode()))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private void processMaintenanceDocument(MaintenanceDocument maintenanceDocument) {
        Maintainable maintainable = maintenanceDocument.getNewMaintainableObject();
        Object businessObject = maintainable.getDataObject();
        String documentDescription = maintenanceDocument.getDocumentHeader().getDocumentDescription();
        String objectKey = null;
        
        if (businessObject instanceof Agency) {
            objectKey = ((Agency) businessObject).getAgencyNumber();
            recordModifiedAgencyAndUpdateAgencyService(maintainable);
        } else if (businessObject instanceof Proposal) {
            objectKey = ((Proposal) businessObject).getProposalNumber();
            recordModifiedProposalAndUpdateBusinessObjectService(maintainable);
        } else if (businessObject instanceof Award) {
            objectKey = ((Award) businessObject).getProposalNumber();
            recordModifiedAwardAndUpdateAwardService(maintainable);
        } else {
            fail("Maintenance document had an unexpected business object type: " + businessObject.getClass());
        }
        
        assertDocumentDescriptionHasExpectedSuffix(objectKey, documentDescription);
    }

    private void assertDocumentDescriptionHasExpectedSuffix(String objectKey, String documentDescription) {
        if (StringUtils.equals(RassTestConstants.LONG_OBJECT_KEY, objectKey)) {
            assertTrue("Document description should have ended with an ellipsis due to truncation",
                    StringUtils.endsWith(documentDescription, CUKFSConstants.ELLIPSIS));
        } else {
            assertFalse("Document description should not have ended with an ellipsis",
                    StringUtils.endsWith(documentDescription, CUKFSConstants.ELLIPSIS));
        }
    }

    private void recordModifiedAgencyAndUpdateAgencyService(Maintainable agencyMaintainable) {
        Agency agency = (Agency) agencyMaintainable.getDataObject();
        String agencyNumber = agency.getAgencyNumber();
        agencyUpdates.add(agencyMaintainable);
        Mockito.doReturn(agency)
                .when(mockAgencyService).getByPrimaryId(agencyNumber);
    }

    private void recordModifiedProposalAndUpdateBusinessObjectService(Maintainable proposalMaintainable) {
        Proposal proposal = (Proposal) proposalMaintainable.getDataObject();
        Map<String, Object> proposalPrimaryKeys = Collections.singletonMap(
                KFSPropertyConstants.PROPOSAL_NUMBER, proposal.getProposalNumber());
        proposalUpdates.add(proposalMaintainable);
        Mockito.doReturn(proposal)
                .when(mockBusinessObjectService).findByPrimaryKey(Proposal.class, proposalPrimaryKeys);
    }

    private void recordModifiedAwardAndUpdateAwardService(Maintainable awardMaintainable) {
        Award award = (Award) awardMaintainable.getDataObject();
        String proposalNumber = award.getProposalNumber();
        awardUpdates.add(awardMaintainable);
        Mockito.doReturn(award)
                .when(mockAwardService).getByPrimaryId(proposalNumber);
    }

    private void overrideStatusesReturnedByRouteHeaderService(int documentNumberAsInt, String... routeStatuses) {
        if (routeStatuses == null || routeStatuses.length == 0) {
            throw new IllegalArgumentException("at least one route status must be specified");
        }
        RouteHeaderService routeHeaderService = springContext.getBean(RassTestConstants.ROUTE_HEADER_SERVICE_BEAN_NAME, RouteHeaderService.class);
        String documentNumber = String.valueOf(documentNumberAsInt);
        
        Stubber valuesToReturn = Mockito.doReturn(routeStatuses[0]);
        for (int i = 1; i < routeStatuses.length; i++) {
            valuesToReturn = valuesToReturn.doReturn(routeStatuses[i]);
        }
        valuesToReturn.when(routeHeaderService).getDocumentStatus(Mockito.eq(documentNumber));
    }

    private List<RassXmlDocumentWrapperFixture> xmlFiles(RassXmlDocumentWrapperFixture... xmlFileFixtures) {
        return Arrays.asList(xmlFileFixtures);
    }

    private ExpectedProcessingResults expectedResults(
            ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> expectedAgencies,
            ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Proposal> expectedProposals,
            ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> expectedAwards) {
        return new ExpectedProcessingResults(expectedAgencies, expectedProposals, expectedAwards);
    }

    @SafeVarargs
    private final ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> agencies(
            RassObjectGroupingUpdateResultCode resultCode, ExpectedObjectUpdateResult<RassXmlAgencyEntryFixture>... expectedAgencies) {
        return new ExpectedObjectUpdateResultGrouping<>(Agency.class, resultCode, expectedAgencies);
    }

    private ExpectedObjectUpdateResult<RassXmlAgencyEntryFixture> agency(RassXmlAgencyEntryFixture agencyFixture, RassObjectUpdateResultCode resultCode) {
        return new ExpectedObjectUpdateResult<>(agencyFixture, resultCode, fixture -> fixture.number);
    }

    @SafeVarargs
    private final ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Proposal> proposals(
            RassObjectGroupingUpdateResultCode resultCode, ExpectedObjectUpdateResult<RassXmlAwardEntryFixture>... expectedProposals) {
        return new ExpectedObjectUpdateResultGrouping<>(Proposal.class, resultCode, expectedProposals);
    }

    private ExpectedObjectUpdateResult<RassXmlAwardEntryFixture> proposal(RassXmlAwardEntryFixture awardFixture, RassObjectUpdateResultCode resultCode) {
        return new ExpectedObjectUpdateResult<>(awardFixture, resultCode, fixture -> fixture.proposalNumber);
    }

    @SafeVarargs
    private final ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> awards(
            RassObjectGroupingUpdateResultCode resultCode, ExpectedObjectUpdateResult<RassXmlAwardEntryFixture>... expectedAwards) {
        return new ExpectedObjectUpdateResultGrouping<>(Award.class, resultCode, expectedAwards);
    }

    private ExpectedObjectUpdateResult<RassXmlAwardEntryFixture> award(RassXmlAwardEntryFixture awardFixture, RassObjectUpdateResultCode resultCode) {
        return new ExpectedObjectUpdateResult<>(awardFixture, resultCode, fixture -> fixture.proposalNumber);
    }

    @FunctionalInterface
    private interface ObjectUpdateValidator<T, U> {
        void assertObjectWasUpdatedAsExpected(T expectedObject, U actualObject, int index);
    }

}
