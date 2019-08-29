package edu.cornell.kfs.module.cg.document.service.impl;

import org.apache.commons.lang3.StringUtils;
import edu.cornell.kfs.module.cg.document.service.CuCGMaintenanceDocumentService;

public class CuCGMaintenanceDocumentServiceImpl implements CuCGMaintenanceDocumentService {
    
    /**
     * Verifies the required federal pass through agency number is 
     * filled in when the federal pass through indicator is set.
     */
    public boolean validFederalPassThroughData(boolean federalPassThroughIndicator, String federalPassThroughAgencyNumber) {
        boolean success = true;
        if (federalPassThroughIndicator && StringUtils.isBlank(federalPassThroughAgencyNumber)) {
            success = false;
        }
        return success;
    }

}
