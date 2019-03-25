package edu.cornell.kfs.module.cg.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.module.cg.CGConstants;
import org.kuali.kfs.module.cg.document.validation.impl.AgencyRule;
import org.kuali.kfs.sys.KFSConstants;
import edu.cornell.kfs.module.cg.CuCGKeyConstants;

public class CuAgencyRule extends AgencyRule {

    @Override
    protected boolean validateAgencyReportingName (MaintenanceDocument document) {
        String agencyReportingName = newAgency.getReportingName();
        String agencyExistsValue = newAgency.getCustomerCreationOptionCode();
        if (CGConstants.AGENCY_CREATE_NEW_CUSTOMER_CODE.equalsIgnoreCase(agencyExistsValue)) {
            if (StringUtils.isBlank(agencyReportingName)
                    || ( (agencyReportingName.length() > 0) 
                            && (agencyReportingName.substring(0, 1).equalsIgnoreCase(KFSConstants.BLANK_SPACE))) ){
                putFieldError("reportingName", 
                        CuCGKeyConstants.AgencyConstants.ERROR_AGENCY_NAME_NOT_BLANK_OR_NO_SPACE_IN_FIRST_CHARACTER);
                return false;
            }
        }
        return true;
    }

}
