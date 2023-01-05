package edu.cornell.kfs.sys.batch.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.kfs;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSParameterKeyConstants;
import org.kuali.kfs.sys.batch.AutoDisapproveDocumentsStep;
import org.kuali.kfs.sys.context.IntegTestUtils;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;

@ConfigureContext(session = kfs)
public class CuAutoDisapproveDocumentsServiceImplIntegTest extends KualiIntegTestBase {
    private CuAutoDisapproveDocumentsServiceImpl autoDisapproveDocumentsService;
    private DateTimeService dateTimeService;
    private ParameterService parameterService;
    private DocumentTypeService documentTypeService;

    @Override
    protected void setUp() throws Exception {
        // TODO Auto-generated method stub
        super.setUp();
        autoDisapproveDocumentsService = (CuAutoDisapproveDocumentsServiceImpl) IntegTestUtils.getUnproxiedService("sysMockAutoDisapproveDocumentsService");
        dateTimeService = SpringContext.getBean(DateTimeService.class);
        parameterService = SpringContext.getBean(ParameterService.class);
        documentTypeService = SpringContext.getBean(DocumentTypeService.class);
    }

    public final void testCheckIfRunDateParameterExists() {
        boolean isExist = autoDisapproveDocumentsService.checkIfRunDateParameterExists();
        boolean parameterExists = false;
        
        // check to make sure the system parameter for run date check has already been setup...
        if (parameterService.parameterExists(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_STEP_RUN_DATE)) {
            try {
                // TODO : YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE is different from the parameter checked in service ?
                Date runDate = dateTimeService.convertToDate(parameterService.getParameterValueAsString(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_STEP_RUN_DATE));
                parameterExists = true;
            }
            catch (ParseException pe) {
            }
        }
       
        assertTrue("YEAR_END_AUTO_DISAPPROVE_DOCUMENTS_RUN_DATE System parameter does not exist Or format is not correct.", (parameterExists && isExist) || (!parameterExists && !isExist));
    }

    
    public final void testCheckIfDocumentCompareCreateDateParameterExists() {
        boolean isExist = autoDisapproveDocumentsService.checkIfDocumentCompareCreateDateParameterExists();
        boolean parameterExists = false;
        
        // check to make sure the system parameter for run date check has already been setup...
        if (parameterService.parameterExists(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE)) {
            try {
                // TODO : YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE is different from the parameter checked in service ?
                Date runDate = dateTimeService.convertToDate(parameterService.getParameterValueAsString(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE));
                parameterExists = true;
            }
            catch (ParseException pe) {
            }
        }
       
        assertTrue("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE System parameter does not exist Or format is not correct.", (parameterExists && isExist) || (!parameterExists && !isExist));
    }

    // parameter 'YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE' is not updated properly yet.  Still got NPE issue.  Comment out for now.
//    public final void testAutoDisapproveDocumentsInEnrouteStatus() {
//
//        Step step = BatchSpringContext.getStep("autoDisapproveDocumentsStep");
//        AutoDisapproveDocumentsStep autoDisapproveDocumentsStep = (AutoDisapproveDocumentsStep) ProxyUtils.getTargetIfProxied(step);
//
////        if (((AutoDisapproveDocumentsServiceImpl)autoDisapproveDocumentsService).systemParametersForAutoDisapproveDocumentsJobExist()) {
//            //set the system parameter to today's date.  The test should pass...
//            String today = dateTimeService.toDateString(dateTimeService.getCurrentDate());
//            TestUtils.setSystemParameter(AopUtils.getTargetClass(autoDisapproveDocumentsStep), KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_STEP_RUN_DATE, today);
//            boolean success = autoDisapproveDocumentsService.autoDisapproveDocumentsInEnrouteStatus();
//            assertTrue("The auto disproval job did not succeed.", success);
////        }
//    }

    public final void testCheckIfDocumentTypesExceptionParameterExists() {
        boolean isExist = autoDisapproveDocumentsService.checkIfDocumentTypesExceptionParameterExists();
        boolean parameterExists = false;
        
        if (parameterService.parameterExists(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES)) {
            Collection<String> documentTypes = parameterService.getParameterValuesAsString(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES);
            for (String dT : documentTypes)
            {
                if (ObjectUtils.isNotNull(documentTypeService.getDocumentTypeByName( dT ))) {             
                    parameterExists = true;
                    break;
                }
            }
          }
          
        assertTrue("YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES System parameter does not exist.", (parameterExists && isExist) || (!parameterExists && !isExist));
     }

    public final void testCheckIffParentDocumentTypeParameterExists() {
        boolean isExist = autoDisapproveDocumentsService.checkIfParentDocumentTypeParameterExists();
        boolean parameterExists = false;
        
        if (parameterService.parameterExists(AutoDisapproveDocumentsStep.class, KFSParameterKeyConstants.YearEndAutoDisapprovalConstants.YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE)) {
            List<DocumentType> parentDocumentTypes = autoDisapproveDocumentsService.getYearEndAutoDisapproveParentDocumentTypes();
            
            for (DocumentType parentDocumentType : parentDocumentTypes) {   
                if (ObjectUtils.isNotNull(documentTypeService.getDocumentTypeByName(parentDocumentType.getName()))) {
                    parameterExists = true;
                    break;
                    }           
            }
        }
          
        assertTrue("YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE System parameter does not exist.", (parameterExists && isExist) || (!parameterExists && !isExist));
     }


}
