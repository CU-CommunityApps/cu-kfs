package edu.cornell.kfs.rass;

import edu.cornell.kfs.sys.CUKFSConstants;

public final class RassTestConstants {

    private RassTestConstants() {
        throw new UnsupportedOperationException("Instantiating constants class is prohibited");
    }

    public static final String AGENCY_DOC_TYPE_NAME = "AGCY";
    public static final String PROPOSAL_DOC_TYPE_NAME = "PRPL";
    public static final String AWARD_DOC_TYPE_NAME = "AWRD";
    public static final String DEFAULT_PROPOSAL_AWARD_TYPE = "S";
    public static final Integer DEFAULT_DD_FIELD_MAX_LENGTH = Integer.valueOf(50);
    public static final String ERROR_OBJECT_KEY = "ERR";
    public static final String AGENCY_EXPECTED_PRIMARY_KEY_FOR_ERROR_TEST = "Agency " + ERROR_OBJECT_KEY;
    public static final String LONG_OBJECT_KEY = "KEY_WITH_EXTREMELY_LONG_TEXT_LENGTH";
    public static final String DEFAULT_AWARD_CHART = RassConstants.PROPOSAL_ORG_CHART;
    public static final String DEFAULT_AWARD_ACCOUNT = "3575357";
    public static final String DEFAULT_AWARD_ACCOUNT_ACTIVE_INDICATOR = "N";
    public static final String DEFAULT_AWARD_ACCOUNT_PARAMETER_VALUE = DEFAULT_AWARD_CHART
            + CUKFSConstants.COLON + DEFAULT_AWARD_ACCOUNT + CUKFSConstants.COLON + DEFAULT_AWARD_ACCOUNT_ACTIVE_INDICATOR;
    public static final String DEFAULT_FUND_MANAGER_PRINCIPAL_ID = "mgw3";
    public static final String DEFAULT_PROJECT_DIRECTOR_PRINCIPAL_ID = "mls398";
    public static final String PROPOSAL_AWARDED_STATUS = "A";

    public static final String RASS_SERVICE_BEAN_NAME = "rassService";
    public static final String RASS_UPDATE_SERVICE_BEAN_NAME = "rassUpdateService";
    public static final String RASS_REPORT_SERVICE_BEAN_NAME = "rassReportService";
    public static final String AGENCY_SERVICE_BEAN_NAME = "agencyService";
    public static final String BUSINESS_OBJECT_SERVICE_BEAN_NAME = "businessObjectService";
    public static final String ROUTE_HEADER_SERVICE_BEAN_NAME = "documentRouteHeaderService";

    public static final class ResourcePropertyValues {
        
        private ResourcePropertyValues() {
            throw new UnsupportedOperationException("Instantiating nested constants class is prohibited");
        }
        
        public static final String MESSAGE_RASS_DOCUMENT_DESCRIPTION = "Auto-Generated {0} of {1}";
        public static final String MESSAGE_RASS_DOCUMENT_ANNOTATION_ROUTE = "Automatically created and routed";
        public static final String MESSAGE_RASS_REPORT_ERROR_HEADER_LINE1 = "Unexpected errors were encountered "
                + "when attempting to read the following RASS XML files.";
        public static final String MESSAGE_RASS_REPORT_ERROR_HEADER_LINE2 = "The failures were likely the result "
                + "of incorrect XML formatting.";
        public static final String MESSAGE_RASS_REPORT_ERROR_HEADER_LINE3 = "Specific details are available "
                + "in the RASS batch job logs.";
    }

}
