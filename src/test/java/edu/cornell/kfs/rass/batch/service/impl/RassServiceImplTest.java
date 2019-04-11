package edu.cornell.kfs.rass.batch.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.kns.document.MaintenanceDocumentBase;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.krad.maintenance.Maintainable;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.MaintenanceDocumentService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.service.AgencyService;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kim.api.identity.Person;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.cg.businessobject.AgencyExtendedAttribute;
import edu.cornell.kfs.module.cg.document.CuAgencyMaintainableImpl;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.xml.RassObjectTranslationDefinition;
import edu.cornell.kfs.rass.batch.xml.RassXmlDocumentWrapper;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAgencyEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlDocumentWrapperFixture;
import edu.cornell.kfs.sys.util.LoadSpringFile;
import edu.cornell.kfs.sys.util.MockPersonUtil;
import edu.cornell.kfs.sys.util.SpringEnabledMicroTestBase;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("org.apache.logging.log4j.*")
@PrepareForTest({MaintenanceDocumentBase.class})
@LoadSpringFile("edu/cornell/kfs/rass/batch/cu-spring-rass-service-test.xml")
public class RassServiceImplTest extends SpringEnabledMicroTestBase {

    private AgencyService mockAgencyService;
    private TestRassServiceImpl rassService;

    private List<Pair<Agency, String>> agencyUpdates;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        PowerMockito.suppress(PowerMockito.defaultConstructorIn(MaintenanceDocumentBase.class));
        
        agencyUpdates = new ArrayList<>();
        
        mockAgencyService = springContext.getBean(RassTestConstants.AGENCY_SERVICE_BEAN_NAME, AgencyService.class);
        
        rassService = springContext.getBean(RassTestConstants.RASS_SERVICE_BEAN_NAME, TestRassServiceImpl.class);
        rassService.setAgencyUpdateTracker(this::recordModifiedAgencyAndUpdateAgencyService);
    }

    @Test
    public void testUpdateSingleAgency() throws Exception {
        List<RassXmlDocumentWrapperFixture> xmlContents = Collections.singletonList(RassXmlDocumentWrapperFixture.RASS_SINGLE_AGENCY_FILE);
        assertXmlContentsPerformExpectedObjectUpdates(xmlContents,
                expectedResults(
                        agencies(Pair.of(RassXmlAgencyEntryFixture.SOME_V2, KRADConstants.MAINTENANCE_EDIT_ACTION))));
    }

    private final void assertXmlContentsPerformExpectedObjectUpdates(List<RassXmlDocumentWrapperFixture> xmlContents,
            ExpectedResultsHolder expectedResultsHolder) throws Exception {
        List<RassXmlDocumentWrapper> rassXmlDocumentWrappers = xmlContents.stream()
                .map(RassXmlDocumentWrapperFixture::toRassXmlDocumentWrapper)
                .collect(Collectors.toCollection(ArrayList::new));
        
        rassService.updateKFS(rassXmlDocumentWrappers);
        
        assertAgenciesWereUpdatedAsExpected(expectedResultsHolder.expectedAgencies);
    }

    private void assertAgenciesWereUpdatedAsExpected(List<Pair<RassXmlAgencyEntryFixture, String>> expectedResults) {
        assertEquals("Wrong number of agencies created or updated", expectedResults.size(), agencyUpdates.size());
        for (int i = 0; i < expectedResults.size(); i++) {
            Pair<RassXmlAgencyEntryFixture, String> expectedResult = expectedResults.get(i);
            Pair<Agency, String> actualResult = agencyUpdates.get(i);
            assertEquals("Wrong maintenance action for agency at index " + i, expectedResult.getRight(), actualResult.getRight());
            
            RassXmlAgencyEntryFixture expectedAgency = expectedResult.getLeft();
            Agency actualAgency = actualResult.getLeft();
            assertEquals("Wrong agency number at index " + i, expectedAgency.number, actualAgency.getAgencyNumber());
            assertEquals("Wrong reporting name at index " + i, expectedAgency.reportingName, actualAgency.getReportingName());
            assertEquals("Wrong full name at index " + i, expectedAgency.fullName, actualAgency.getFullName());
            assertEquals("Wrong type code at index " + i, expectedAgency.typeCode, actualAgency.getAgencyTypeCode());
            assertEquals("Wrong reports-to agency number at index " + i,
                    expectedAgency.reportsToAgencyNumber, actualAgency.getReportsToAgencyNumber());
            
            AgencyExtendedAttribute actualExtension = (AgencyExtendedAttribute) actualAgency.getExtension();
            assertEquals("Wrong common name at index " + i, expectedAgency.commonName, actualExtension.getAgencyCommonName());
            assertEquals("Wrong origin code at index " + i, expectedAgency.agencyOrigin, actualExtension.getAgencyOriginCode());
        }
    }

    private void recordModifiedAgencyAndUpdateAgencyService(Agency agency, String maintenanceAction) {
        String agencyNumber = agency.getAgencyNumber();
        agencyUpdates.add(Pair.of(agency, maintenanceAction));
        Mockito.doReturn(agency)
                .when(mockAgencyService).getByPrimaryId(agencyNumber);
    }

    private ExpectedResultsHolder expectedResults(Pair<RassXmlAgencyEntryFixture, String>[] expectedAgencies) {
        return new ExpectedResultsHolder(expectedAgencies);
    }

    @SafeVarargs
    private final Pair<RassXmlAgencyEntryFixture, String>[] agencies(Pair<RassXmlAgencyEntryFixture, String>... expectedAgencies) {
        return expectedAgencies;
    }

    private static class ExpectedResultsHolder {
        private final List<Pair<RassXmlAgencyEntryFixture, String>> expectedAgencies;
        
        private ExpectedResultsHolder(Pair<RassXmlAgencyEntryFixture, String>[] expectedAgencies) {
            this.expectedAgencies = Collections.unmodifiableList(Arrays.asList(expectedAgencies));
        }
    }

    public static class TestRassServiceImpl extends RassServiceImpl {
        
        private BiConsumer<Agency, String> agencyUpdateTracker;
        
        private void setAgencyUpdateTracker(BiConsumer<Agency, String> agencyUpdateTracker) {
            this.agencyUpdateTracker = agencyUpdateTracker;
        }
        
        @Override
        protected <R extends PersistableBusinessObject> R createMinimalObject(Class<R> businessObjectClass) {
            R businessObject = super.createMinimalObject(businessObjectClass);
            if (businessObject instanceof Agency) {
                businessObject.setExtension(new AgencyExtendedAttribute());
            }
            return wrapObjectAndSuppressReferenceRefreshes(businessObject);
        }
        
        @Override
        protected <R extends PersistableBusinessObject> R deepCopyObject(R businessObject) {
            R objectCopy = super.deepCopyObject(businessObject);
            return wrapObjectAndSuppressReferenceRefreshes(objectCopy);
        }
        
        protected <R extends PersistableBusinessObject> R wrapObjectAndSuppressReferenceRefreshes(R businessObject) {
            R businessObjectSpy = Mockito.spy(businessObject);
            Mockito.doNothing()
                    .when(businessObjectSpy).refreshReferenceObject(Mockito.anyString());
            
            if (businessObject instanceof Agency) {
                PersistableBusinessObjectExtension extensionSpy = Mockito.spy(businessObjectSpy.getExtension());
                Mockito.doNothing()
                        .when(extensionSpy).refreshReferenceObject(Mockito.anyString());
                businessObjectSpy.setExtension(extensionSpy);
            }
            
            return businessObjectSpy;
        }
        
        @Override
        protected <R extends PersistableBusinessObject> MaintenanceDocument createAndRouteMaintenanceDocument(
                Pair<R, R> businessObjects, String maintenanceAction, RassObjectTranslationDefinition<?, R> objectDefinition)
                throws WorkflowException {
            MaintenanceDocument maintenanceDocument = super.createAndRouteMaintenanceDocument(businessObjects, maintenanceAction, objectDefinition);
            Object newBusinessObject = maintenanceDocument.getNewMaintainableObject().getDataObject();
            if (newBusinessObject instanceof Agency) {
                agencyUpdateTracker.accept((Agency) newBusinessObject, maintenanceAction);
            }
            return maintenanceDocument;
        }
        
        @Override
        protected UserSession buildSessionForSystemUser() {
            Person mockSystemUser = MockPersonUtil.createMockPerson(UserNameFixture.kfs);
            return MockPersonUtil.createMockUserSession(mockSystemUser);
        }
        
    }

    public static class ServiceFactory {
        
        public MaintenanceDocumentService buildMockMaintenanceDocumentService() throws Exception {
            MaintenanceDocumentService maintenanceDocumentService = Mockito.mock(MaintenanceDocumentService.class);
            
            Mockito.when(maintenanceDocumentService.setupNewMaintenanceDocument(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                    .then(this::buildNewMaintenanceDocument);
            
            return maintenanceDocumentService;
        }
        
        protected MaintenanceDocument buildNewMaintenanceDocument(InvocationOnMock invocation) {
            try {
                String businessObjectClassName = Objects.requireNonNull(invocation.getArgument(0));
                String documentTypeName = Objects.requireNonNull(invocation.getArgument(1));
                String maintenanceAction = Objects.requireNonNull(invocation.getArgument(2));
                
                Class<? extends PersistableBusinessObject> businessObjectClass = getBusinessObjectClass(businessObjectClassName, documentTypeName);
                Class<? extends Maintainable> maintainableClass = getMaintainableClass(documentTypeName);
                validateMaintenanceAction(maintenanceAction);
                
                MaintenanceDocumentBase maintenanceDocument = PowerMockito.spy(new MaintenanceDocumentBase());
                FinancialSystemDocumentHeader documentHeader = new FinancialSystemDocumentHeader();
                Maintainable oldMaintainable = maintainableClass.newInstance();
                Maintainable newMaintainable = maintainableClass.newInstance();
                PersistableBusinessObject oldBo = businessObjectClass.newInstance();
                PersistableBusinessObject newBo = businessObjectClass.newInstance();
                
                oldMaintainable.setDataObject(oldBo);
                oldMaintainable.setDataObjectClass(businessObjectClass);
                newMaintainable.setDataObject(newBo);
                newMaintainable.setDataObjectClass(businessObjectClass);
                maintenanceDocument.setOldMaintainableObject(oldMaintainable);
                maintenanceDocument.setNewMaintainableObject(newMaintainable);
                maintenanceDocument.setDocumentHeader(documentHeader);
                
                return maintenanceDocument;
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        
        @SuppressWarnings("unchecked")
        protected Class<? extends PersistableBusinessObject> getBusinessObjectClass(String businessObjectClassName, String documentTypeName)
                throws ClassNotFoundException {
            Class<? extends PersistableBusinessObject> businessObjectClass = (Class<? extends PersistableBusinessObject>) Class.forName(businessObjectClassName);
            Class<? extends PersistableBusinessObject> expectedBusinessObjectClass;
            
            switch (documentTypeName) {
                case RassTestConstants.AGENCY_DOC_TYPE_NAME :
                    expectedBusinessObjectClass = Agency.class;
                    break;
                default :
                    throw new IllegalArgumentException("Cannot find BO class for document type " + documentTypeName);
            }
            
            if (!expectedBusinessObjectClass.isAssignableFrom(businessObjectClass)) {
                throw new IllegalArgumentException(
                        expectedBusinessObjectClass.getSimpleName() + " does not support BO type " + businessObjectClassName);
            }
            
            return businessObjectClass;
        }
        
        protected Class<? extends Maintainable> getMaintainableClass(String documentTypeName) {
            switch (documentTypeName) {
                case RassTestConstants.AGENCY_DOC_TYPE_NAME :
                    return CuAgencyMaintainableImpl.class;
                default :
                    throw new IllegalArgumentException("Cannot find maintainable class for document type " + documentTypeName);
            }
        }
        
        protected void validateMaintenanceAction(String maintenanceAction) {
            switch (maintenanceAction) {
                case KRADConstants.MAINTENANCE_NEW_ACTION :
                case KRADConstants.MAINTENANCE_EDIT_ACTION :
                    return;
                default :
                    throw new IllegalArgumentException("Unexpected or unsupported maintenance action " + maintenanceAction);
            }
        }
        
        public DocumentService buildMockDocumentService() throws Exception {
            DocumentService documentService = Mockito.mock(DocumentService.class);
            
            Mockito.when(documentService.saveDocument(Mockito.any(MaintenanceDocument.class)))
                    .then(invocation -> invocation.getArgument(0));
            Mockito.when(documentService.routeDocument(Mockito.any(MaintenanceDocument.class), Mockito.anyString(), Mockito.any()))
                    .then(invocation -> invocation.getArgument(0));
            
            return documentService;
        }
        
        public DataDictionaryService buildMockDataDictionaryService() {
            DataDictionaryService dataDictionaryService = Mockito.mock(DataDictionaryService.class);
            
            Mockito.when(dataDictionaryService.getAttributeMaxLength(Mockito.any(), Mockito.anyString()))
                    .thenReturn(RassTestConstants.DEFAULT_DD_FIELD_MAX_LENGTH);
            
            return dataDictionaryService;
        }
        
        public AgencyService buildMockAgencyService() {
            AgencyService agencyService = Mockito.mock(AgencyService.class);
            
            Arrays.stream(RassXmlAgencyEntryFixture.values())
                    .filter(fixture -> fixture.existsByDefaultForSearching)
                    .forEach(fixture -> {
                        Mockito.when(agencyService.getByPrimaryId(fixture.number))
                                .thenReturn(fixture.toAgency());
                    });
            
            return agencyService;
        }
        
    }

}
