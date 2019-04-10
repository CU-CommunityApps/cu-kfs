package edu.cornell.kfs.rass.batch.service.impl;

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.kfs.kns.document.MaintenanceDocumentBase;
import org.kuali.kfs.krad.maintenance.Maintainable;
import org.kuali.kfs.krad.maintenance.MaintenanceDocument;
import org.kuali.kfs.krad.service.DataDictionaryService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.krad.service.MaintenanceDocumentService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.service.AgencyService;
import org.kuali.rice.krad.bo.BusinessObject;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import edu.cornell.kfs.module.cg.document.CuAgencyMaintainableImpl;
import edu.cornell.kfs.rass.RassTestConstants;
import edu.cornell.kfs.sys.util.LoadSpringFile;
import edu.cornell.kfs.sys.util.SpringEnabledMicroTestBase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MaintenanceDocumentBase.class})
@LoadSpringFile("edu/cornell/kfs/rass/batch/cu-spring-rass-service-test.xml")
public class RassServiceImplTest extends SpringEnabledMicroTestBase {

    @Before
    public void setUp() throws Exception {
        super.setUp();
        PowerMockito.suppress(PowerMockito.defaultConstructorIn(MaintenanceDocumentBase.class));
    }

    @Test
    public void testSomething() throws Exception {
        
    }

    public static class Factory {
        
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
                
                Class<? extends BusinessObject> businessObjectClass = getBusinessObjectClass(businessObjectClassName, documentTypeName);
                Class<? extends Maintainable> maintainableClass = getMaintainableClass(documentTypeName);
                validateMaintenanceAction(maintenanceAction);
                
                MaintenanceDocumentBase maintenanceDocument = PowerMockito.spy(new MaintenanceDocumentBase());
                Maintainable oldMaintainable = maintainableClass.newInstance();
                Maintainable newMaintainable = maintainableClass.newInstance();
                BusinessObject oldBo = businessObjectClass.newInstance();
                BusinessObject newBo = businessObjectClass.newInstance();
                
                oldMaintainable.setDataObject(oldBo);
                oldMaintainable.setDataObjectClass(businessObjectClass);
                newMaintainable.setDataObject(newBo);
                newMaintainable.setDataObjectClass(businessObjectClass);
                maintenanceDocument.setOldMaintainableObject(oldMaintainable);
                maintenanceDocument.setNewMaintainableObject(newMaintainable);
                
                return maintenanceDocument;
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
        
        @SuppressWarnings("unchecked")
        protected Class<? extends BusinessObject> getBusinessObjectClass(String businessObjectClassName, String documentTypeName)
                throws ClassNotFoundException {
            Class<? extends BusinessObject> businessObjectClass = (Class<? extends BusinessObject>) Class.forName(businessObjectClassName);
            Class<? extends BusinessObject> expectedBusinessObjectClass;
            
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
            return agencyService;
        }
        
    }

}
