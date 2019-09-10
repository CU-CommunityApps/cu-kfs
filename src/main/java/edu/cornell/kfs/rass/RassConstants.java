package edu.cornell.kfs.rass;

public final class RassConstants {

    private RassConstants() {
        throw new UnsupportedOperationException("Instantiating constants class is prohibited");
    }

    public static final String RASS_MAINTENANCE_NEW_ACTION_DESCRIPTION = "Creation";

    public enum RassParseResultCode {
        SUCCESS,
        ERROR;
    }

    public enum RassObjectGroupingUpdateResultCode {
        SUCCESS,
        ERROR;
    }

    public enum RassObjectUpdateResultCode {
        SUCCESS_NEW,
        SUCCESS_EDIT,
        ERROR,
        SKIPPED;
        
        public static boolean isSuccessfulResult(RassObjectUpdateResultCode resultCode) {
            switch (resultCode) {
                case SUCCESS_NEW :
                case SUCCESS_EDIT :
                    return true;
                default :
                    return false;
            }
        }
    }
    
    public static final String RASS_DEFAULT_PROPOSAL_AWARD_TYPE_PARAMETER = "RASS_DEFAULT_PROPOSAL_AWARD_TYPE";
    
    public static final String PROPOSAL_ORG_CHART = "IT";
    
    public static final String RASS_MODULE = "KFS-RASS";

}
