package edu.cornell.kfs.rass.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.kew.api.KewApiConstants;
import org.kuali.kfs.kew.routeheader.service.RouteHeaderService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.AwardAccount;
import org.kuali.kfs.module.cg.businessobject.AwardFundManager;
import org.kuali.kfs.module.cg.businessobject.AwardOrganization;
import org.kuali.kfs.module.cg.businessobject.CGProjectDirector;
import org.kuali.kfs.module.cg.businessobject.Primaryable;
import org.kuali.kfs.module.cg.businessobject.ProposalOrganization;
import org.kuali.kfs.module.cg.service.AgencyService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
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
import edu.cornell.kfs.rass.batch.RassXmlFileProcessingResult;
import edu.cornell.kfs.rass.batch.util.RassTestUtils;
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
    private RassServiceImpl rassService;
    private TestRassUpdateServiceImpl rassUpdateService;

    private Map<String, List<Maintainable>> agencyUpdates;
    private List<Maintainable> agencyUpdatesForCurrentFile;
    private Map<String, List<Maintainable>> awardUpdates;
    private List<Maintainable> awardUpdatesForCurrentFile;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        agencyUpdates = new HashMap<>();
        awardUpdates = new HashMap<>();
        mockAgencyService = springContext.getBean(RassTestConstants.AGENCY_SERVICE_BEAN_NAME, AgencyService.class);
        mockBusinessObjectService = springContext.getBean(RassTestConstants.BUSINESS_OBJECT_SERVICE_BEAN_NAME, BusinessObjectService.class);
        
        rassService = springContext.getBean(RassTestConstants.RASS_SERVICE_BEAN_NAME, RassServiceImpl.class);
        rassService.setParsedObjectTypeProcessingWatcher(this::handleStartOfFileProcessing);
        
        rassUpdateService = springContext.getBean(RassTestConstants.RASS_UPDATE_SERVICE_BEAN_NAME, TestRassUpdateServiceImpl.class);
        rassUpdateService.setDocumentTracker(this::processMaintenanceDocument);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        agencyUpdates = null;
        mockAgencyService = null;
        rassService = null;
        rassUpdateService = null;
    }

    @Test
    public void testLoadEmptyFile() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_EMPTY_FILE,
                        emptyAgencyResults(),
                        emptyAwardResults()));
    }

    @Test
    public void testUpdateSingleAgency() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()));
    }

    @Test
    public void testCreateSingleAgency() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()));
    }

    @Test
    public void testCreateAndUpdateMultipleAgenciesFromSingleFile() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_MULTIPLE_AGENCIES_CREATE_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()));
    }

    @Test
    public void testUpdateAgencyWithTextFieldsExceedingMaxLength() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AGENCY_UPDATE_LENGTH_TRUNCATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.DoS_LONG_DESC, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()));
    }

    @Test
    public void testUpdateAgencyWithMissingRequiredField() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AGENCY_UPDATE_MISSING_FIELD_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2_MISSING_REQ_FIELD, RassObjectUpdateResultCode.ERROR)),
                        emptyAwardResults()));
    }

    @Test
    public void testCreateAgencyWithTruncatedDocDescription() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_LONG_AGENCY_NUMBER_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LONG_KEY, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()));
    }

    @Test
    public void testHandleSingleExistingAgencyWithNoChanges() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_EXISTING_AGENCY_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME, RassObjectUpdateResultCode.SKIPPED)),
                        emptyAwardResults()));
    }

    @Test
    public void testUpdateAgencyAndIgnoreSubsequentDuplicateUpdate() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE_V2,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SKIPPED)),
                        emptyAwardResults()));
    }

    @Test
    public void testCreateAgencyAndIgnoreSubsequentDuplicateCreate() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE_V2,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SKIPPED)),
                        emptyAwardResults()));
    }

    @Test
    public void testLoadMultipleFilesWithAgencies() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_MULTIPLE_AGENCIES_CREATE_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SKIPPED)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()));
    }

    @Test
    public void testWaitForRouteStatusAfterUpdateToSameAgency() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_FINAL_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE_V2,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SKIPPED)),
                        emptyAwardResults()));
    }

    @Test
    public void testWaitForRouteStatusAfterUpdateToReferencedAgency() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_PROCESSED_CD,
                KewApiConstants.ROUTE_HEADER_FINAL_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(        
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()));
    }

    @Test
    public void testTimeoutOfRouteStatusCheck() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_ENROUTE_CD,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_FINAL_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.ERROR)),
                        emptyAwardResults()));
    }

    @Test
    public void testTimeoutOfRouteStatusCheckForMultipleAgenciesInSingleFile() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_ENROUTE_CD,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_FINAL_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_MULTIPLE_AGENCIES_TIMEOUT_TEST_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.ERROR)),
                        emptyAwardResults()));
    }

    @Test
    public void testRouteStatusCheckErrorWhenDocumentEntersUnsuccessfulStatus() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.ERROR)),
                        emptyAwardResults()));
    }

    @Test
    public void testErrorObjectCausesSubsequentUpdatesReferencingErrorObjectToFail() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.ERROR)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_ANOTHER_SINGLE_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.UNLIMITED_LTD, RassObjectUpdateResultCode.ERROR)),
                        emptyAwardResults()));
    }

    @Test
    public void testErrorObjectDoesNotAffectSubsequentUnrelatedObjects() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.ERROR)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()));
    }

    @Test
    public void testWaitOnlyAtEndIfObjectUpdatesDoNotReferencePriorOnes() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_FINAL_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()));
    }

    @Test
    public void testHandleRoutingTimeoutQuietlyIfObjectUpdatesDoNotReferencePriorOnes() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_ENROUTE_CD,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_FINAL_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()));
    }

    @Test
    public void testHandleRouteStatusCheckErrorQuietlyIfObjectUpdatesDoNotReferencePriorOnes() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()));
    }

    @Test
    public void testSubsequentChangeToSameAgencyProceedsIfFirstAttemptFails() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AGENCY_UPDATE_MISSING_FIELD_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2_MISSING_REQ_FIELD, RassObjectUpdateResultCode.ERROR)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()));
    }

    @Test
    public void testSubsequentChangeToSameProposalProceedsIfFirstAttemptFails() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_CREATE_MISSING_FIELD_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.ERROR,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_MISSING_REQ_FIELD, RassObjectUpdateResultCode.ERROR))),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_CREATE_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW))));
    }

    @Test
    public void testSubsequentChangeToSameAwardProceedsIfFirstAttemptFails() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_UPDATE_MISSING_FIELD_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.ERROR,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_MISSING_REQ_FIELD, RassObjectUpdateResultCode.ERROR))),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_UPDATE_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V2, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testSubsequentChangeToSameObjectProceedsIfCheckForFirstDocumentTimesOut() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_ENROUTE_CD,
                KewApiConstants.ROUTE_HEADER_ENROUTE_CD, KewApiConstants.ROUTE_HEADER_FINAL_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_AFTER_CREATE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD_UPDATE, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()));
    }

    @Test
    public void testSubsequentChangeToSameObjectProceedsIfFirstDocumentHasRoutingFailure() throws Exception {
        overrideStatusesReturnedByRouteHeaderService(RassMockServiceFactory.FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID,
                KewApiConstants.ROUTE_HEADER_EXCEPTION_CD);
        
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_SUBSEQUENT_UPDATE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V3, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()));
    }

    @Test
    public void testDownstreamObjectUpdatesProceedIfUpstreamObjectUpdateReattemptSucceeds() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AGENCY_UPDATE_MISSING_FIELD_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2_MISSING_REQ_FIELD, RassObjectUpdateResultCode.ERROR)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_SUBSEQUENT_UPDATE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V3, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_CREATE_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW))));
    }

    @Test
    public void testHandleErrorAtObjectGroupLevel() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT)),
                        emptyAwardResults()),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_FORCE_AGENCY_GROUP_ERROR_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.ERROR, agency(RassXmlAgencyEntryFixture.FORCE_ERROR, RassObjectUpdateResultCode.ERROR)),
                        emptyAwardResults()));
    }

    @Test
    public void testCreateSingleProposalAndAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_CREATE_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW))));
    }

    @Test
    public void testUpdateSingleAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_UPDATE_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V2, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testUpdateOrgCodeOnExistingAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_ORG_UPDATE_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V3_ORG_CHANGE, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testUpdateDirectorsOnExistingAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_DIRECTOR_UPDATE_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V4_DIRECTOR_CHANGE, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testAlternateUpdateOfDirectorsOnExistingAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_DIRECTOR_UPDATE_FILE2,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V5_DIRECTOR_CHANGE2, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testSkipUpdatesWhenLoadingUnchangedAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_UNCHANGED_AWARD_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT, RassObjectUpdateResultCode.SKIPPED))));
    }

    @Test
    public void testLoadNewAwardAndSkipSubsequentDuplicate() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_DUPLICATED_NEW_AWARD_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW),
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SKIPPED))));
    }

    @Test
    public void testLoadAwardWithMissingRequiredField() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_CREATE_MISSING_FIELD_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.ERROR,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_MISSING_REQ_FIELD, RassObjectUpdateResultCode.ERROR))));
    }

    @Test
    public void testLoadMultipleAgenciesAndProposalsAndAwards() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_MULTIPLE_AGENCIES_AND_AWARDS_FILE,
                        agencies(RassObjectGroupingUpdateResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassObjectUpdateResultCode.SUCCESS_NEW)),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT, RassObjectUpdateResultCode.SUCCESS_NEW),
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_V2, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }
    
    @Test
    public void testZeroAmountAndCostShareYesProposalExistsAwardExists() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_EXISTS_AWARD_EXISTS, 
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_EXISTS_AWARD_EXISTS, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }
    
    @Test
    public void testZeroAmountAndCostShareYesProposalExistsAwardNotExists() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_EXISTS_AWARD_NOT_EXISTS, 
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_EXISTS_AWARD_NOT_EXISTS, RassObjectUpdateResultCode.SUCCESS_NEW))));
    }

    @Test
    public void testZeroAmountAndCostShareNoProposalExistsAwardExists() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_ZERO_AMOUNT_COST_SHARE_NO_PROPOSAL_EXISTS_AWARD_EXISTS, 
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_NO_PROPOSAL_EXISTS_AWARD_EXISTS, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }
    
    @Test
    public void testZeroAmountAndCostShareYesProposalDoesNotExistAwardExists() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_DOES_NOT_EXIST_AWARD_EXISTS, 
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_DOES_NOT_EXIST_AWARD_EXISTS, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }
    
    @Test
    public void testZeroAmountAndCostShareYesProposalDoesNotExistAwardDoesNotExist() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_DOES_NOT_EXIST_AWARD_NOT_EXISTS, 
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_YES_PROPOSAL_DOES_NOT_EXIST_AWARD_NOT_EXISTS, RassObjectUpdateResultCode.SUCCESS_NEW))));
    }

    @Test
    public void testZeroAmountAndCostShareNoProposalDoesNotExist() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_ZERO_AMOUNT_COST_SHARE_NO_PROPOSAL_DOES_NOT_EXIST, 
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.ZERO_AMOUNT_COST_SHARE_NO_PROPOSAL_DOES_NOT_EXIST, RassObjectUpdateResultCode.SKIPPED))));
    }

    @Test
    public void testNullValueHandlingForSingleAward() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_WITH_VARIOUS_NULL_FIELDS,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_NULL_FIELDS_TEST, RassObjectUpdateResultCode.SUCCESS_NEW))));
    }

    @Test
    public void testSingleAwardWithNullValuesThatCauseCreateToBeSkipped() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_SKIP_DUE_TO_NULLS,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_SKIP_DUE_TO_NULLS, RassObjectUpdateResultCode.SKIPPED))));
    }

    @Test
    public void testSingleAwardWithNullValuesThatCauseCreateToBeSkipped_Version2() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_SKIP_DUE_TO_NULLS_V2,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_SKIP_DUE_TO_NULLS_V2, RassObjectUpdateResultCode.SKIPPED))));
    }

    @Test
    public void testSingleAwardWithNullValuesThatCauseCreateToBeSkipped_Version3() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_SKIP_DUE_TO_NULLS_V3,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_SKIP_DUE_TO_NULLS_V3, RassObjectUpdateResultCode.SKIPPED))));
    }

    @Test
    public void testSingleAwardWithNullValuesThatCauseUpdateFailure() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_WITH_VARIOUS_NULL_FIELDS,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_NULL_FIELDS_TEST, RassObjectUpdateResultCode.SUCCESS_NEW))),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_SKIP_DUE_TO_NULLS,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.ERROR,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_SKIP_DUE_TO_NULLS, RassObjectUpdateResultCode.ERROR))));
    }

    @Test
    public void testSingleAwardWithNullValuesThatCauseUpdateFailure_Version2() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_WITH_VARIOUS_NULL_FIELDS,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_NULL_FIELDS_TEST, RassObjectUpdateResultCode.SUCCESS_NEW))),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_SKIP_DUE_TO_NULLS_V2,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.ERROR,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_SKIP_DUE_TO_NULLS_V2, RassObjectUpdateResultCode.ERROR))));
    }

    @Test
    public void testSingleAwardWithNullValuesThatCauseUpdateFailure_Version3() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_WITH_VARIOUS_NULL_FIELDS,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_NULL_FIELDS_TEST, RassObjectUpdateResultCode.SUCCESS_NEW))),
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_SKIP_DUE_TO_NULLS_V3,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.ERROR,
                                award(RassXmlAwardEntryFixture.SAMPLE_PROJECT_SKIP_DUE_TO_NULLS_V3, RassObjectUpdateResultCode.ERROR))));
    }

    @Test
    public void testUpdateGrantNumberOnProposal() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_GRANT_NUM_UPDATE_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_GRANT_NUM_CHANGE, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testClearAndSetGrantNumberOnProposal() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_AWARD_MULTI_CHANGE_GRANT_NUM_FILE,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_GRANT_NUM_CLEAR, RassObjectUpdateResultCode.SUCCESS_EDIT),
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_GRANT_NUM_CHANGE, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    @Test
    public void testUpdateProposalGrantNumberPlusOtherAwardField() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                fileWithResults(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AWARD_GRANT_NUM_UPDATE_FILE2,
                        emptyAgencyResults(),
                        awards(RassObjectGroupingUpdateResultCode.SUCCESS,
                                award(RassXmlAwardEntryFixture.SOME_DEPARTMENT_PROJECT_GRANT_NUM_CHANGE2, RassObjectUpdateResultCode.SUCCESS_EDIT))));
    }

    private void assertXmlContentsPerformExpectedObjectUpdates(
            FileWithExpectedResults... filesWithResults) throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(Arrays.asList(filesWithResults));
    }

    private void assertXmlContentsPerformExpectedObjectUpdates(
            final List<FileWithExpectedResults> filesWithResults) throws Exception {
        final List<RassXmlFileParseResult> parseResults = filesWithResults.stream()
                .map(FileWithExpectedResults::getFileFixture)
                .map(this::buildWrapperEncasedInSuccessfulFileResult)
                .collect(Collectors.toCollection(ArrayList::new));
        
        final Map<String, RassXmlFileProcessingResult> actualFileResults = RassTestUtils
                .doWithMockHandlingOfProjectDirectorRefreshes(() -> rassService.updateKFS(parseResults));
        assertFilesPerformedExpectedObjectUpdates(filesWithResults, actualFileResults);
    }

    private RassXmlFileParseResult buildWrapperEncasedInSuccessfulFileResult(
            RassXmlDocumentWrapperFixture fileFixture) {
        RassXmlDocumentWrapper documentWrapper = fileFixture.toRassXmlDocumentWrapper();
        return new RassXmlFileParseResult(
                fileFixture.getGeneratedFileName(), RassParseResultCode.SUCCESS, Optional.of(documentWrapper));
    }

    private void assertFilesPerformedExpectedObjectUpdates(
            List<FileWithExpectedResults> expectedFileResults,
            Map<String, RassXmlFileProcessingResult> actualFileResults) {
        assertEquals("Wrong number of file results", expectedFileResults.size(), actualFileResults.size());
        
        for (FileWithExpectedResults expectedFileResult : expectedFileResults) {
            String xmlFileName = expectedFileResult.getExpectedXmlFileName();
            RassXmlFileProcessingResult actualFileResult = actualFileResults.get(xmlFileName);
            assertNotNull("No results were found for filename key: " + xmlFileName, actualFileResult);
            assertEquals("Wrong filename for file result entry", xmlFileName, actualFileResult.getRassXmlFileName());
            assertAgenciesWereUpdatedAndReportedAsExpected(expectedFileResult, actualFileResult);
            assertAwardsWereUpdatedAndReportedAsExpected(expectedFileResult, actualFileResult);
        }
    }

    private void assertAgenciesWereUpdatedAndReportedAsExpected(
            FileWithExpectedResults expectedFileResult, RassXmlFileProcessingResult actualFileResult) {
        ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> expectedAgencyResultGrouping =
                expectedFileResult.getExpectedAgencyResults();
        RassBusinessObjectUpdateResultGrouping<Agency> actualAgencyResultGrouping =
                actualFileResult.getAgencyResults();
        List<Maintainable> agencyUpdatesToCheck = agencyUpdates.getOrDefault(
                expectedFileResult.getExpectedXmlFileName(), Collections.emptyList());
        
        assertCorrectObjectResultsWereReported(expectedAgencyResultGrouping, actualAgencyResultGrouping);
        assertObjectsWereUpdatedAsExpected(expectedAgencyResultGrouping, agencyUpdatesToCheck,
                this::assertAgencyWasUpdatedAsExpected);
    }

    private void assertAwardsWereUpdatedAndReportedAsExpected(
            FileWithExpectedResults expectedFileResult, RassXmlFileProcessingResult actualFileResult) {
        ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> expectedProposalResultGrouping =
                expectedFileResult.getExpectedAwardResults();
        RassBusinessObjectUpdateResultGrouping<Award> actualAwardResultGrouping = actualFileResult.getAwardResults();
        List<Maintainable> awardUpdatesToCheck = awardUpdates.getOrDefault(
                expectedFileResult.getExpectedXmlFileName(), Collections.emptyList());
        
        assertCorrectObjectResultsWereReported(expectedProposalResultGrouping, actualAwardResultGrouping);
        assertObjectsWereUpdatedAsExpected(expectedProposalResultGrouping, awardUpdatesToCheck,
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
            String expectedPriamryKey =  expectedResult.getPrimaryKey();
            
            if (StringUtils.equalsIgnoreCase(expectedPriamryKey, RassTestConstants.ERROR_OBJECT_KEY)) {
                expectedPriamryKey = RassTestConstants.AGENCY_EXPECTED_PRIMARY_KEY_FOR_ERROR_TEST;
            }
            assertEquals("Wrong object primary key at index " + i, expectedPriamryKey, actualResult.getPrimaryKey());
            
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
        
        assertEquals("Wrong number of " + objectLabel + " objects created or updated for file",
                expectedResults.size(), actualResults.size());
        for (int i = 0; i < expectedResults.size(); i++) {
            ExpectedObjectUpdateResult<E> expectedResult = expectedResults.get(i);
            Maintainable actualResult = actualResults.get(i);
            assertEquals("Wrong maintenance action for " + objectLabel + " at index " + i,
                    expectedResult.getExpectedMaintenanceAction(), actualResult.getMaintenanceAction());
            
            E expectedObjectFixture = expectedResult.getBusinessObjectFixture();
            R actualObject = businessObjectClass.cast(actualResult.getDataObject());
            if ((actualObject instanceof MutableInactivatable) 
                    && (KRADConstants.MAINTENANCE_NEW_ACTION.equalsIgnoreCase(actualResult.getMaintenanceAction()))) {
                assertTrue(objectLabel + " at index " + i + " should have been marked as active",
                        ((MutableInactivatable) actualObject).isActive());
            }
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

    private void assertAwardWasUpdatedAsExpected(RassXmlAwardEntryFixture expectedAward, Award actualAward, int i) {
        assertEquals("Wrong proposal/award type at index " + i,
                RassTestConstants.DEFAULT_PROPOSAL_AWARD_TYPE, actualAward.getProposalAwardTypeCode());
        assertEqualsOrBothBlank("Wrong proposal number at index " + i, expectedAward.proposalNumber, actualAward.getProposalNumber());
        assertEqualsOrBothBlank("Wrong award status at index " + i, expectedAward.status, actualAward.getAwardStatusCode());
        assertEqualsOrBothBlank("Wrong agency number at index " + i, expectedAward.agencyNumber, actualAward.getAgencyNumber());
        assertEqualsOrBothBlank("Wrong project title at index " + i, expectedAward.projectTitle, actualAward.getAwardProjectTitle());
        assertEquals("Wrong start date at index " + i, expectedAward.getStartDateAsSqlDate(), actualAward.getAwardBeginningDate());
        assertEquals("Wrong stop date at index " + i, expectedAward.getStopDateAsSqlDate(), actualAward.getAwardEndingDate());
        assertEquals("Wrong direct cost amount at index " + i, expectedAward.directCostAmount, actualAward.getAwardDirectCostAmount());
        assertEquals("Wrong indirect cost amount at index " + i, expectedAward.indirectCostAmount, actualAward.getAwardIndirectCostAmount());
        assertEqualsOrBothBlank("Wrong award purpose at index " + i, expectedAward.purpose, actualAward.getAwardPurposeCode());
        assertEqualsOrBothBlank("Wrong grant number at index " + i, expectedAward.grantNumber, actualAward.getGrantNumber());
        assertEqualsOrBothBlank("Wrong grant description at index " + i, expectedAward.grantDescription, actualAward.getGrantDescriptionCode());
        assertEquals("Wrong federal pass-through indicator at index " + i,
                expectedAward.getNullSafeFederalPassThrough(), actualAward.getFederalPassThroughIndicator());
        assertEqualsOrBothBlank("Wrong federal pass-through agency number at index " + i,
                expectedAward.federalPassThroughAgencyNumber, actualAward.getFederalPassThroughAgencyNumber());
        assertEqualsOrBothBlank("Wrong instrument type code at index " + i, expectedAward.pricingType, actualAward.getInstrumentTypeCode());
        
        AwardExtendedAttribute actualExtension = (AwardExtendedAttribute) actualAward.getExtension();
        assertEquals("Wrong cost share required indicator at index " + i,
                expectedAward.getNullSafeCostShareRequired(), actualExtension.isCostShareRequired());
        assertEquals("Wrong final fiscal report date at index " + i,
                expectedAward.getFinalReportDueDateAsSqlDate(), actualExtension.getFinalFiscalReportDate());
        assertEqualsOrBothBlank("Wrong Prime Agreement Number at index " + i,
                expectedAward.primeAgreementNumber, actualExtension.getPrimeAgreementNumber());
        assertEquals("Wrong Budget Beginning Date at index " + i,
                expectedAward.getBudgetBeginningDateAsSqlDate(), actualExtension.getBudgetBeginningDate());
        assertEquals("Wrong Budget Ending Date at index " + i,
                expectedAward.getBudgetEndingDateAsSqlDate(), actualExtension.getBudgetEndingDate());
        assertEquals("Wrong Budget Total Amount at index " + i,
                expectedAward.budgetTotalAmount, actualExtension.getBudgetTotalAmount());
        assertEquals("Wrong Everify at index " + i,
                expectedAward.getNullSafeEverify(), actualExtension.isEverify());
        assertEquals("Wrong Final Financial Report Required at index " + i,
                expectedAward.getNullSafeFinalFinancialReportRequired(), actualExtension.isFinalFinancialReportRequired());
        
        assertAwardOrganizationsWereUpdatedAsExpected(expectedAward.organizations, actualAward.getAwardOrganizations(), i);
        assertAwardAccountWasUpdatedAsExpected(expectedAward, actualAward.getAwardAccounts(), i);
        assertAwardFundManagerWasUpdatedAsExpected(expectedAward, actualAward.getAwardFundManagers(), i);
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
                    expectedOrgData.getRight(), actualOrganization.isPrimary());
            assertEquals("Wrong active indicator for organization at index " + multiIndex,
                    expectedOrgData.getRight(), actualOrganization.isActive());
        }
    }

    private void assertAwardAccountWasUpdatedAsExpected(
            RassXmlAwardEntryFixture expectedAward, List<AwardAccount> actualAwardAccounts, int i) {
        assertEquals("Wrong number of award accounts at index " + i, 1, actualAwardAccounts.size());
        
        AwardAccount actualAwardAccount = actualAwardAccounts.get(0);
        assertEquals("Wrong award account proposal number at index " + i,
                expectedAward.proposalNumber, actualAwardAccount.getProposalNumber());
        assertEquals("Wrong award account principal ID at index " + i,
                RassTestConstants.DEFAULT_PROJECT_DIRECTOR_PRINCIPAL_ID, actualAwardAccount.getPrincipalId());
        assertEquals("Wrong chart code for award account at index " + i,
                RassTestConstants.DEFAULT_AWARD_CHART, actualAwardAccount.getChartOfAccountsCode());
        assertEquals("Wrong account number for award account at index " + i,
                RassTestConstants.DEFAULT_AWARD_ACCOUNT, actualAwardAccount.getAccountNumber());
    }

    private void assertAwardFundManagerWasUpdatedAsExpected(
            RassXmlAwardEntryFixture expectedAward, List<AwardFundManager> actualFundManagers, int i) {
        assertEquals("Wrong number of fund managers at index " + i, 1, actualFundManagers.size());
        
        AwardFundManager actualFundManager = actualFundManagers.get(0);
        assertEquals("Wrong fund manager proposal number at index " + i,
                expectedAward.proposalNumber, actualFundManager.getProposalNumber());
        assertEquals("Wrong fund manager principal ID at index " + i,
                RassTestConstants.DEFAULT_FUND_MANAGER_PRINCIPAL_ID, actualFundManager.getPrincipalId());
        assertTrue("Fund manager should have been flagged as primary at index " + i,
                actualFundManager.isPrimaryFundManagerIndicator());
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
                    expectedDirector.getNullSafePrimary(), ((Primaryable) actualDirector).isPrimary());
            assertTrue("An active-object indicator should have been supported for director at index " + multiIndex,
                    actualDirector instanceof MutableInactivatable);
            assertEquals("Wrong active indicator for director at index " + multiIndex,
                    expectedDirector.getNullSafeActive(), ((MutableInactivatable) actualDirector).isActive());
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

    private void handleStartOfFileProcessing(String xmlFileName, Class<?> businessObjectClass) {
        if (Agency.class.isAssignableFrom(businessObjectClass)) {
            agencyUpdatesForCurrentFile = agencyUpdates.computeIfAbsent(xmlFileName, key -> new ArrayList<>());
        } else if (Award.class.isAssignableFrom(businessObjectClass)) {
            awardUpdatesForCurrentFile = awardUpdates.computeIfAbsent(xmlFileName, key -> new ArrayList<>());
        } else {
            fail("Service was updating an unexpected business object type: " + businessObjectClass);
        }
    }

    private void processMaintenanceDocument(MaintenanceDocument maintenanceDocument) {
        Maintainable maintainable = maintenanceDocument.getNewMaintainableObject();
        Object businessObject = maintainable.getDataObject();
        String documentDescription = maintenanceDocument.getDocumentHeader().getDocumentDescription();
        String objectKey = null;
        
        if (businessObject instanceof Agency) {
            objectKey = ((Agency) businessObject).getAgencyNumber();
            recordModifiedAgencyAndUpdateAgencyService(maintainable);
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
        agencyUpdatesForCurrentFile.add(agencyMaintainable);
        Mockito.doReturn(agency)
                .when(mockAgencyService).getByPrimaryId(agencyNumber);
    }

    private void recordModifiedAwardAndUpdateAwardService(Maintainable awardMaintainable) {
        Award award = (Award) awardMaintainable.getDataObject();
        String proposalNumber = award.getProposalNumber();
        awardUpdatesForCurrentFile.add(awardMaintainable);
        Map<String, Object> awardPrimaryKeys = Collections.singletonMap(
                KFSPropertyConstants.PROPOSAL_NUMBER, award.getProposalNumber());
        Mockito.doReturn(award)
                .when(mockBusinessObjectService).findByPrimaryKey(Award.class, awardPrimaryKeys);
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

    private FileWithExpectedResults fileWithResults(
            RassXmlDocumentWrapperFixture fileFixture,
            ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> expectedAgencies,
            ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> expectedAwards) {
        return new FileWithExpectedResults(fileFixture, expectedAgencies, expectedAwards);
    }

    private ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> emptyAgencyResults() {
        return agencies(RassObjectGroupingUpdateResultCode.SUCCESS);
    }

    @SafeVarargs
    private final ExpectedObjectUpdateResultGrouping<RassXmlAgencyEntryFixture, Agency> agencies(
            RassObjectGroupingUpdateResultCode resultCode, ExpectedObjectUpdateResult<RassXmlAgencyEntryFixture>... expectedAgencies) {
        return new ExpectedObjectUpdateResultGrouping<>(Agency.class, resultCode, expectedAgencies);
    }

    private ExpectedObjectUpdateResult<RassXmlAgencyEntryFixture> agency(RassXmlAgencyEntryFixture agencyFixture, RassObjectUpdateResultCode resultCode) {
        return new ExpectedObjectUpdateResult<>(agencyFixture, resultCode, fixture -> fixture.number);
    }

    private ExpectedObjectUpdateResult<RassXmlAwardEntryFixture> proposal(RassXmlAwardEntryFixture awardFixture, RassObjectUpdateResultCode resultCode) {
        return new ExpectedObjectUpdateResult<>(awardFixture, resultCode, fixture -> fixture.proposalNumber);
    }

    private ExpectedObjectUpdateResultGrouping<RassXmlAwardEntryFixture, Award> emptyAwardResults() {
        return awards(RassObjectGroupingUpdateResultCode.SUCCESS);
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
    
    @Test
    public void testSortFileNames() {
        String a = "a";
        String b = "b";
        String c = "c";
        List<String> results = buildFileNameList(c, b, a);
        
        assertEquals(a, results.get(2));
        
        rassService.sortFileNameList(results);
        
        assertEquals(a, results.get(0));
        assertEquals(b, results.get(1));
        assertEquals(c, results.get(2));
    }
    
    @Test
    public void testSortFileNamesWithRealLifeFileNames() {
        String fullExtractFileName = "kfs.xml";
        String sept8FileName = "rass_20190908044608.xml";
        String sept9FileName = "rass_20190909044604.xml";
        String sept10FileName = "rass_20190910044609.xml";
        String aug10FileName = "rass_20190810044609.xml";
        
        List<String> fileNames = buildFileNameList(aug10FileName, sept10FileName, sept9FileName, sept8FileName, fullExtractFileName);
        assertEquals(aug10FileName, fileNames.get(0));
        
        rassService.sortFileNameList(fileNames);
        
        assertEquals(fullExtractFileName, fileNames.get(0));
        assertEquals(aug10FileName, fileNames.get(1));
        assertEquals(sept8FileName, fileNames.get(2));
        assertEquals(sept9FileName, fileNames.get(3));
        assertEquals(sept10FileName, fileNames.get(4));
        
    }
    
    private List<String> buildFileNameList(String... fileNames) {
        List<String> fileNameList = Arrays.asList(fileNames);
        return fileNameList;
    }

}
