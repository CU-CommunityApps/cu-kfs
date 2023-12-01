package edu.cornell.kfs.module.cg.document.validation.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.cg.CGConstants;
import org.kuali.kfs.module.cg.document.validation.impl.AgencyRule;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;

import edu.cornell.kfs.module.cg.CuCGKeyConstants;

@SuppressWarnings("deprecation")
public class CuAgencyRule extends AgencyRule {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    protected boolean validateAgencyReportingName(final MaintenanceDocument document) {
        LOG.debug("entering validateAgencyReportingName");
        
        final String agencyReportingName = newAgency.getReportingName();
        final String agencyExistsValue = newAgency.getCustomerCreationOptionCode();
        if (CGConstants.AGENCY_CREATE_NEW_CUSTOMER_CODE.equalsIgnoreCase(agencyExistsValue)) {
            if (StringUtils.isBlank(agencyReportingName)
                    || ((agencyReportingName.length() > 0) 
                            && (agencyReportingName.substring(0, 1).equalsIgnoreCase(KFSConstants.BLANK_SPACE)))) {
                putFieldError(KFSPropertyConstants.REPORTING_NAME, CuCGKeyConstants.AgencyConstants.ERROR_AGENCY_NAME_NOT_BLANK_OR_NO_SPACE_IN_FIRST_CHARACTER);
                return false;
            }
        }
        return true;
    }
    
    @Override
    protected boolean checkAgencyReportsTo(final MaintenanceDocument document) {
        LOG.debug("entering checkAgencyReportsTo");
        
        if (StringUtils.isNotBlank(newAgency.getReportsToAgencyNumber())) {
            if (ObjectUtils.isNull(newAgency.getReportsToAgency())) {
                putFieldError(KFSPropertyConstants.REPORTS_TO_AGENCY_NUMBER, KFSKeyConstants.ERROR_AGENCY_NOT_FOUND, newAgency.getReportsToAgencyNumber());
                return false;
            }
        }
        
        return super.checkAgencyReportsTo(document);
    }

}
