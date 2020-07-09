package edu.cornell.kfs.rass.batch.service.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.mutable.MutableInt;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.MaintenanceDocumentService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.module.cg.document.ProposalMaintainableImpl;
import org.kuali.kfs.module.cg.service.AgencyService;
import org.kuali.kfs.module.cg.service.AwardService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.FinancialSystemMaintenanceDocument;
import org.kuali.kfs.sys.fixture.UserNameFixture;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.document.DocumentProcessingQueue;
import org.kuali.rice.kew.routeheader.service.RouteHeaderService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;

import edu.cornell.kfs.module.cg.document.CuAgencyMaintainableImpl;
import edu.cornell.kfs.module.cg.document.CuAwardMaintainableImpl;
import edu.cornell.kfs.rass.RassKeyConstants;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAgencyEntryFixture;
import edu.cornell.kfs.rass.batch.xml.fixture.RassXmlAwardEntryFixture;
import edu.cornell.kfs.sys.service.mock.MockParameterServiceImpl;
import edu.cornell.kfs.sys.util.MockPersonUtil;

import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.kfs.kim.impl.role.RoleBo;


public class RassMockServiceFactory {

    public static final int MOCK_DOCUMENT_ID_SEQUENCE_START_VALUE = 1000;
    public static final int FIRST_AUTO_GENERATED_MOCK_DOCUMENT_ID = MOCK_DOCUMENT_ID_SEQUENCE_START_VALUE + 1;

    public MaintenanceDocumentService buildMockMaintenanceDocumentService() throws Exception {
        MaintenanceDocumentService maintenanceDocumentService = Mockito.mock(MaintenanceDocumentService.class);
        MutableInt documentIdSequence = new MutableInt(MOCK_DOCUMENT_ID_SEQUENCE_START_VALUE);
        
        Mockito.when(maintenanceDocumentService.setupNewMaintenanceDocument(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .then(invocation -> buildNewMaintenanceDocument(invocation, documentIdSequence.addAndGet(1)));
        
        return maintenanceDocumentService;
    }

    protected MaintenanceDocument buildNewMaintenanceDocument(InvocationOnMock invocation, int documentNumberAsInt) {
        try {
            String businessObjectClassName = Objects.requireNonNull(invocation.getArgument(0));
            String documentTypeName = Objects.requireNonNull(invocation.getArgument(1));
            String maintenanceAction = Objects.requireNonNull(invocation.getArgument(2));
            String documentNumber = String.valueOf(documentNumberAsInt);
            
            Class<? extends PersistableBusinessObject> businessObjectClass = getBusinessObjectClass(businessObjectClassName, documentTypeName);
            Class<? extends Maintainable> maintainableClass = getMaintainableClass(documentTypeName);
            validateMaintenanceAction(maintenanceAction);
            
            FinancialSystemMaintenanceDocument maintenanceDocument = Mockito.mock(
                    FinancialSystemMaintenanceDocument.class, Mockito.CALLS_REAL_METHODS);
            FinancialSystemDocumentHeader documentHeader = new FinancialSystemDocumentHeader();
            Maintainable oldMaintainable = maintainableClass.newInstance();
            Maintainable newMaintainable = maintainableClass.newInstance();
            PersistableBusinessObject oldBo = businessObjectClass.newInstance();
            PersistableBusinessObject newBo = businessObjectClass.newInstance();
            
            oldMaintainable.setDataObject(oldBo);
            oldMaintainable.setDataObjectClass(businessObjectClass);
            newMaintainable.setDataObject(newBo);
            newMaintainable.setDataObjectClass(businessObjectClass);
            documentHeader.setDocumentNumber(documentNumber);
            maintenanceDocument.setOldMaintainableObject(oldMaintainable);
            maintenanceDocument.setNewMaintainableObject(newMaintainable);
            maintenanceDocument.setDocumentHeader(documentHeader);
            maintenanceDocument.setDocumentNumber(documentNumber);
            
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
            case RassTestConstants.PROPOSAL_DOC_TYPE_NAME :
                expectedBusinessObjectClass = Proposal.class;
                break;
            case RassTestConstants.AWARD_DOC_TYPE_NAME :
                expectedBusinessObjectClass = Award.class;
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
            case RassTestConstants.PROPOSAL_DOC_TYPE_NAME :
                return ProposalMaintainableImpl.class;
            case RassTestConstants.AWARD_DOC_TYPE_NAME :
                return CuAwardMaintainableImpl.class;
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
        
        Mockito.when(documentService.routeDocument(Mockito.any(MaintenanceDocument.class), Mockito.anyString(), Mockito.any()))
                .then(invocation -> invocation.getArgument(0));
        
        return documentService;
    }
    
    public ParameterService buildMockParameterService() throws Exception {
        return new MockParameterServiceImpl();
    }
    
    public BusinessObjectService buildMockBusinessObjectService() throws Exception {
        BusinessObjectService businessObjectService = Mockito.mock(BusinessObjectService.class);
        
        Arrays.stream(RassXmlAwardEntryFixture.values())
                .filter(fixture -> fixture.proposalExistsByDefaultForSearching)
                .forEach(fixture -> {
                    Map<String, Object> proposalPrimaryKeys = Collections.singletonMap(
                            KFSPropertyConstants.PROPOSAL_NUMBER, fixture.proposalNumber);
                    Mockito.when(businessObjectService.findByPrimaryKey(Proposal.class, proposalPrimaryKeys))
                            .thenReturn(fixture.toProposal());
                });
        
        return businessObjectService;
    }

    public DataDictionaryService buildMockDataDictionaryService() throws Exception {
        DataDictionaryService dataDictionaryService = Mockito.mock(DataDictionaryService.class);
        
        Mockito.when(dataDictionaryService.getAttributeMaxLength(Mockito.any(Class.class), Mockito.anyString()))
                .thenReturn(RassTestConstants.DEFAULT_DD_FIELD_MAX_LENGTH);
        
        return dataDictionaryService;
    }

    public RouteHeaderService buildMockRouteHeaderService() throws Exception {
        RouteHeaderService routeHeaderService = Mockito.mock(RouteHeaderService.class);
        
        Mockito.when(routeHeaderService.getDocumentStatus(Mockito.anyString()))
                .thenReturn(KewApiConstants.ROUTE_HEADER_FINAL_CD);
        
        return routeHeaderService;
    }

    public AgencyService buildMockAgencyService() throws Exception {
        AgencyService agencyService = Mockito.mock(AgencyService.class);
        
        Arrays.stream(RassXmlAgencyEntryFixture.values())
                .filter(fixture -> fixture.existsByDefaultForSearching)
                .forEach(fixture -> {
                    Mockito.when(agencyService.getByPrimaryId(fixture.number))
                            .thenReturn(fixture.toAgency());
                });
        
        return agencyService;
    }

    public ConfigurationService buildMockConfigurationService() throws Exception {
        ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
        
        Mockito.when(configurationService.getPropertyValueAsString(RassKeyConstants.MESSAGE_RASS_DOCUMENT_DESCRIPTION))
                .thenReturn(RassTestConstants.ResourcePropertyValues.MESSAGE_RASS_DOCUMENT_DESCRIPTION);
        Mockito.when(configurationService.getPropertyValueAsString(RassKeyConstants.MESSAGE_RASS_DOCUMENT_ANNOTATION_ROUTE))
                .thenReturn(RassTestConstants.ResourcePropertyValues.MESSAGE_RASS_DOCUMENT_ANNOTATION_ROUTE);
        Mockito.when(configurationService.getPropertyValueAsString(RassKeyConstants.MESSAGE_RASS_REPORT_ERROR_HEADER_LINE1))
                .thenReturn(RassTestConstants.ResourcePropertyValues.MESSAGE_RASS_REPORT_ERROR_HEADER_LINE1);
        Mockito.when(configurationService.getPropertyValueAsString(RassKeyConstants.MESSAGE_RASS_REPORT_ERROR_HEADER_LINE2))
                .thenReturn(RassTestConstants.ResourcePropertyValues.MESSAGE_RASS_REPORT_ERROR_HEADER_LINE2);
        Mockito.when(configurationService.getPropertyValueAsString(RassKeyConstants.MESSAGE_RASS_REPORT_ERROR_HEADER_LINE3))
                .thenReturn(RassTestConstants.ResourcePropertyValues.MESSAGE_RASS_REPORT_ERROR_HEADER_LINE3);
        
        return configurationService;
    }

    public AwardService buildMockAwardService() throws Exception {
        AwardService awardService = Mockito.mock(AwardService.class);
        
        Arrays.stream(RassXmlAwardEntryFixture.values())
                .filter(fixture -> fixture.awardExistsByDefaultForSearching)
                .forEach(fixture -> {
                    Mockito.when(awardService.getByPrimaryId(fixture.proposalNumber))
                            .thenReturn(fixture.toAward());
                });
        
        return awardService;
    }

    public PersonService buildMockPersonService() throws Exception {
        PersonService personService = Mockito.mock(PersonService.class);
        
        Stream.of(UserNameFixture.mgw3, UserNameFixture.kan2, UserNameFixture.mo14)
                .forEach(fixture -> {
                    Person mockPerson = MockPersonUtil.createMockPerson(fixture);
                    String principalName = mockPerson.getPrincipalName();
                    Mockito.when(personService.getPersonByPrincipalName(principalName))
                            .thenReturn(mockPerson);
                });
        
        return personService;
    }
    
    public RoleService buildMockRoleService() {
        RoleBo projectDirectRole = new RoleBo();
        projectDirectRole.setId("123");
        projectDirectRole.setName(KFSConstants.SysKimApiConstants.CONTRACTS_AND_GRANTS_PROJECT_DIRECTOR);
        projectDirectRole.setNamespaceCode(KFSConstants.CoreModuleNamespaces.KFS);
        projectDirectRole.setKimTypeId("R");
        
        RoleService roleService = Mockito.mock(RoleService.class);
        Mockito.when(roleService.getRoleByNamespaceCodeAndName(KFSConstants.CoreModuleNamespaces.KFS,
                KFSConstants.SysKimApiConstants.CONTRACTS_AND_GRANTS_PROJECT_DIRECTOR)).thenReturn(RoleBo.to(projectDirectRole));
        Mockito.when(roleService.principalHasRole(Mockito.anyString(), Mockito.any(), Mockito.any())).thenReturn(true);
        
        return roleService;
    }

    public EmailService buildMockEmailService() throws Exception {
        return Mockito.mock(EmailService.class);
    }
    
    public DocumentProcessingQueue buildMockDocumentProcessingQueue() {
        return Mockito.mock(DocumentProcessingQueue.class);
    }

}
