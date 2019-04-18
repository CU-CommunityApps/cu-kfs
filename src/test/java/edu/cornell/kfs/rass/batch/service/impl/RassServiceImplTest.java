package edu.cornell.kfs.rass.batch.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kuali.kfs.krad.maintenance.Maintainable;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.service.AgencyService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.routeheader.service.RouteHeaderService;
import org.mockito.Mockito;
import org.mockito.stubbing.Stubber;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtendedAttribute;
import edu.cornell.kfs.rass.RassConstants.RassResultCode;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.RassXmlObjectGroupResult;
import edu.cornell.kfs.rass.batch.RassXmlObjectResult;
import edu.cornell.kfs.rass.batch.RassXmlProcessingResults;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAgencyEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAwardEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlDocumentWrapperFixture;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.util.LoadSpringFile;
import edu.cornell.kfs.sys.util.SpringEnabledMicroTestBase;

@LoadSpringFile("edu/cornell/kfs/rass/batch/cu-spring-rass-service-test.xml")
public class RassServiceImplTest extends SpringEnabledMicroTestBase {

    private AgencyService mockAgencyService;
    private TestRassServiceImpl rassService;
    private TestRassRoutingServiceImpl rassRoutingService;

    private List<Maintainable> agencyUpdates;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        agencyUpdates = new ArrayList<>();
        mockAgencyService = springContext.getBean(RassTestConstants.AGENCY_SERVICE_BEAN_NAME, AgencyService.class);
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
                        agencies(RassResultCode.SUCCESS),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testUpdateSingleAgency() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE),
                expectedResults(
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testCreateSingleAgency() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassResultCode.SUCCESS_NEW)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testCreateAndUpdateMultipleAgenciesFromSingleFile() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_MULTIPLE_AGENCIES_CREATE_UPDATE_FILE),
                expectedResults(
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassResultCode.SUCCESS_NEW),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testUpdateAgencyWithTextFieldsExceedingMaxLength() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_AGENCY_UPDATE_LENGTH_TRUNCATE_FILE),
                expectedResults(
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.DoS_LONG_DESC, RassResultCode.SUCCESS_EDIT)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testUpdateAgencyWithMissingRequiredField() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_AGENCY_UPDATE_MISSING_FIELD_FILE),
                expectedResults(
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testCreateAgencyWithTruncatedDocDescription() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_LONG_AGENCY_NUMBER_CREATE_FILE),
                expectedResults(
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LONG_KEY, RassResultCode.SUCCESS_NEW)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testHandleSingleExistingAgencyWithNoChanges() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_EXISTING_AGENCY_FILE),
                expectedResults(
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME, RassResultCode.SKIPPED)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testUpdateAgencyAndIgnoreSubsequentDuplicateUpdate() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE),
                expectedResults(
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SKIPPED)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testCreateAgencyAndIgnoreSubsequentDuplicateCreate() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassResultCode.SUCCESS_NEW),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassResultCode.SKIPPED)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testLoadMultipleFilesWithAgencies() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_MULTIPLE_AGENCIES_CREATE_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_FOREIGN_AGENCY_CREATE_FILE),
                expectedResults(
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassResultCode.SUCCESS_NEW),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SKIPPED),
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassResultCode.SUCCESS_NEW)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
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
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SKIPPED)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
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
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassResultCode.SUCCESS_NEW)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
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
                        agencies(RassResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassResultCode.ERROR)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
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
                        agencies(RassResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassResultCode.ERROR)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
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
                        agencies(RassResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassResultCode.ERROR),
                                agency(RassXmlAgencyEntryFixture.UNLIMITED_LTD, RassResultCode.ERROR)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
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
                        agencies(RassResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.LIMITED_LTD, RassResultCode.ERROR),
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassResultCode.SUCCESS_NEW)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
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
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassResultCode.SUCCESS_NEW)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
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
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassResultCode.SUCCESS_NEW)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
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
                        agencies(RassResultCode.SUCCESS,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT),
                                agency(RassXmlAgencyEntryFixture.FIJI_DOT, RassResultCode.SUCCESS_NEW)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    @Test
    public void testHandleErrorAtObjectGroupLevel() throws Exception {
        assertXmlContentsPerformExpectedObjectUpdates(
                xmlFiles(
                        RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_UPDATE_FILE,
                        RassXmlDocumentWrapperFixture.RASS_FORCE_AGENCY_GROUP_ERROR_FILE),
                expectedResults(
                        agencies(RassResultCode.ERROR,
                                agency(RassXmlAgencyEntryFixture.SOME_V2, RassResultCode.SUCCESS_EDIT)),
                        proposals(RassResultCode.SUCCESS),
                        awards(RassResultCode.SUCCESS)));
    }

    private void assertXmlContentsPerformExpectedObjectUpdates(List<RassXmlDocumentWrapperFixture> xmlContents,
            ExpectedProcessingResults expectedProcessingResults) throws Exception {
        List<RassXmlFileParseResult> fileResults = xmlContents.stream()
                .map(RassXmlDocumentWrapperFixture::toRassXmlDocumentWrapper)
                .map(this::encaseWrapperInSuccessfulFileResult)
                .collect(Collectors.toCollection(ArrayList::new));
        
        RassXmlProcessingResults actualResults = rassService.updateKFS(fileResults);
        
        assertAgenciesWereUpdatedAndReportedAsExpected(expectedProcessingResults, actualResults);
    }

    private RassXmlFileParseResult encaseWrapperInSuccessfulFileResult(RassXmlDocumentWrapper documentWrapper) {
        return new RassXmlFileParseResult(KFSConstants.EMPTY_STRING, RassResultCode.SUCCESS, Optional.of(documentWrapper));
    }

    private void assertAgenciesWereUpdatedAndReportedAsExpected(
            ExpectedProcessingResults expectedProcessingResults, RassXmlProcessingResults actualProcessingResults) {
        ExpectedObjectGroupResult<RassXmlAgencyEntryFixture> expectedAgencyGroupResults = expectedProcessingResults.getExpectedAgencyResults();
        List<ExpectedObjectResult<RassXmlAgencyEntryFixture>> expectedSuccessfulAgencyChanges = getExpectedSuccessfulResults(
                expectedAgencyGroupResults.getExpectedObjectResults());
        RassXmlObjectGroupResult actualAgencyGroupResults = actualProcessingResults.getAgencyResults();
        
        assertCorrectObjectResultsWereReported(expectedAgencyGroupResults, actualAgencyGroupResults);
        assertAgenciesWereUpdatedAsExpected(expectedSuccessfulAgencyChanges);
    }

    private <E extends Enum<E>> void assertCorrectObjectResultsWereReported(
            ExpectedObjectGroupResult<E> expectedGroupResult, RassXmlObjectGroupResult actualGroupResult) {
        Class<?> expectedBusinessObjectClass = expectedGroupResult.getBusinessObjectClass();
        List<ExpectedObjectResult<E>> expectedResults = expectedGroupResult.getExpectedObjectResults();
        List<RassXmlObjectResult> actualResults = actualGroupResult.getObjectResults();
        
        assertEquals("Wrong businessobject class for result group", expectedBusinessObjectClass, actualGroupResult.getBusinessObjectClass());
        assertEquals("Wrong result code for group", expectedGroupResult.getResultCode(), actualGroupResult.getResultCode());
        assertEquals("Wrong number of processing results for type " + expectedBusinessObjectClass, expectedResults.size(), actualResults.size());
        
        for (int i = 0; i < expectedResults.size(); i++) {
            ExpectedObjectResult<?> expectedResult = expectedResults.get(i);
            RassXmlObjectResult actualResult = actualResults.get(i);
            assertEquals("Wrong business object class at index " + i, expectedBusinessObjectClass, actualResult.getBusinessObjectClass());
            assertEquals("Wrong object primary key at index " + i, expectedResult.getPrimaryKey(), actualResult.getPrimaryKey());
            assertEquals("Wrong result code at index " + i, expectedResult.getResultCode(), actualResult.getResultCode());
            if (RassResultCode.isSuccessfulResult(expectedResult.getResultCode())) {
                assertTrue("A document should have been created for successful result at index " + i,
                        StringUtils.isNotBlank(actualResult.getDocumentId()));
            } else {
                assertTrue("A document should not have been created for result at index " + i,
                        StringUtils.isBlank(actualResult.getDocumentId()));
            }
        }
    }

    private void assertAgenciesWereUpdatedAsExpected(List<ExpectedObjectResult<RassXmlAgencyEntryFixture>> expectedResults) {
        assertEquals("Wrong number of agencies created or updated", expectedResults.size(), agencyUpdates.size());
        for (int i = 0; i < expectedResults.size(); i++) {
            ExpectedObjectResult<RassXmlAgencyEntryFixture> expectedResult = expectedResults.get(i);
            Maintainable actualResult = agencyUpdates.get(i);
            assertEquals("Wrong maintenance action for agency at index " + i,
                    expectedResult.getExpectedMaintenanceAction(), actualResult.getMaintenanceAction());
            
            RassXmlAgencyEntryFixture expectedAgency = expectedResult.getBusinessObjectFixture();
            Agency actualAgency = (Agency) actualResult.getDataObject();
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
    }

    private void assertEqualsOrBothBlank(String message, String expected, String actual) {
        assertEquals(message, StringUtils.defaultIfBlank(expected, null), StringUtils.defaultIfBlank(actual, null));
    }

    private <E extends Enum<E>> List<ExpectedObjectResult<E>> getExpectedSuccessfulResults(List<ExpectedObjectResult<E>> expectedResults) {
        return expectedResults.stream()
                .filter(expectedResult -> RassResultCode.isSuccessfulResult(expectedResult.getResultCode()))
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
            ExpectedObjectGroupResult<RassXmlAgencyEntryFixture> expectedAgencies,
            ExpectedObjectGroupResult<RassXmlAwardEntryFixture> expectedProposals,
            ExpectedObjectGroupResult<RassXmlAwardEntryFixture> expectedAwards) {
        return new ExpectedProcessingResults(expectedAgencies, expectedProposals, expectedAwards);
    }

    @SafeVarargs
    private final ExpectedObjectGroupResult<RassXmlAgencyEntryFixture> agencies(
            RassResultCode resultCode, ExpectedObjectResult<RassXmlAgencyEntryFixture>... expectedAgencies) {
        return new ExpectedObjectGroupResult<>(Agency.class, resultCode, expectedAgencies);
    }

    private ExpectedObjectResult<RassXmlAgencyEntryFixture> agency(RassXmlAgencyEntryFixture agencyFixture, RassResultCode resultCode) {
        return new ExpectedObjectResult<>(agencyFixture, resultCode, fixture -> fixture.number);
    }

    @SafeVarargs
    private final ExpectedObjectGroupResult<RassXmlAwardEntryFixture> proposals(
            RassResultCode resultCode, ExpectedObjectResult<RassXmlAwardEntryFixture>... expectedProposals) {
        return new ExpectedObjectGroupResult<>(Proposal.class, resultCode, expectedProposals);
    }

    @SuppressWarnings("unused")
    private ExpectedObjectResult<RassXmlAwardEntryFixture> proposal(RassXmlAwardEntryFixture awardFixture, RassResultCode resultCode) {
        return new ExpectedObjectResult<>(awardFixture, resultCode, fixture -> fixture.proposalNumber);
    }

    @SafeVarargs
    private final ExpectedObjectGroupResult<RassXmlAwardEntryFixture> awards(
            RassResultCode resultCode, ExpectedObjectResult<RassXmlAwardEntryFixture>... expectedAwards) {
        return new ExpectedObjectGroupResult<>(Award.class, resultCode, expectedAwards);
    }

    @SuppressWarnings("unused")
    private ExpectedObjectResult<RassXmlAwardEntryFixture> award(RassXmlAwardEntryFixture awardFixture, RassResultCode resultCode) {
        return new ExpectedObjectResult<>(awardFixture, resultCode, fixture -> fixture.proposalNumber);
    }

}
