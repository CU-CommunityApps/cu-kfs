package edu.cornell.kfs.module.cg.document.validation.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.module.cg.document.validation.impl.ProposalRule;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.cg.document.service.CuCGMaintenanceDocumentService;

@SuppressWarnings("deprecation")
public class CuProposalRule extends ProposalRule {
    private static final Logger LOG = LogManager.getLogger(CuProposalRule.class);
    
    protected CuCGMaintenanceDocumentService cuCGMaintenanceDocumentService;
    
    /**
     * Verifies the required federal pass through agency number is filled in when the
     * federal pass through indicator is set. Uses a service method to perform this check
     * so the same logic is invoked for both Awards and Proposals.
     */
    @Override
    protected boolean checkFederalPassThrough(boolean federalPassThroughIndicator, Agency primaryAgency,
            String federalPassThroughAgencyNumber, Class propertyClass, String federalPassThroughIndicatorFieldName) {
        
        boolean success = getCuCGMaintenanceDocumentService().validFederalPassThroughData(
                newProposalCopy.getProposalFederalPassThroughIndicator(), 
                newProposalCopy.getFederalPassThroughAgencyNumber());
               
        if (!success) {
            putFieldError(KFSPropertyConstants.FEDERAL_PASS_THROUGH_AGENCY_NUMBER, KFSKeyConstants.ERROR_FPT_AGENCY_NUMBER_REQUIRED);
        }
        
        return success;
    }
    
    public CuCGMaintenanceDocumentService getCuCGMaintenanceDocumentService() {
        if ( cuCGMaintenanceDocumentService == null ) {
            cuCGMaintenanceDocumentService = SpringContext.getBean(CuCGMaintenanceDocumentService.class);
        }
        return cuCGMaintenanceDocumentService;
    }
}