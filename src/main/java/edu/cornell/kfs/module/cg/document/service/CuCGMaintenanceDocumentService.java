package edu.cornell.kfs.module.cg.document.service;

public interface CuCGMaintenanceDocumentService {
    
    /**
     * Verifies the required federal pass through agency number is filled in when the federal pass through indicator is set.
     * It is the calling routine's responsibility to set the correct error message on the appropriate edoc (Proposal or Award)
     * when this validation check does not pass.
     */
    boolean validFederalPassThroughData(boolean federalPassThroughIndicator, String federalPassThroughAgencyNumber);
}
