package edu.cornell.kfs.rass;

public final class RassConstants {

    private RassConstants() {
        throw new UnsupportedOperationException("Instantiating constants class is prohibited");
    }

    public static final String RASS_MAINTENANCE_DOCUMENT_DESCRIPTION_FORMAT = "Auto-Generated %s of %s";
    public static final String RASS_MAINTENANCE_NEW_ACTION_DESCRIPTION = "Creation";
    public static final String RASS_ROUTE_ACTION_ANNOTATION = "Automatically created and routed";

    public enum RassProcessingStatus {
        SUCCESS,
        ERROR,
        SKIPPED;
    }

}
