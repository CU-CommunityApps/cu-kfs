package edu.cornell.kfs.rass;

public final class RassConstants {

    private RassConstants() {
        throw new UnsupportedOperationException("Instantiating constants class is prohibited");
    }

    public static final String RASS_MAINTENANCE_NEW_ACTION_DESCRIPTION = "Creation";

    public enum RassResultCode {
        SUCCESS,
        SUCCESS_NEW,
        SUCCESS_EDIT,
        ERROR,
        SKIPPED;
        
        public static boolean isSuccessfulResult(RassResultCode resultCode) {
            switch (resultCode) {
                case SUCCESS :
                case SUCCESS_NEW :
                case SUCCESS_EDIT :
                    return true;
                default :
                    return false;
            }
        }
    }

}
