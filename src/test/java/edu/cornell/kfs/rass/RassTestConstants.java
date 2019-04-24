package edu.cornell.kfs.rass;

public final class RassTestConstants {

    private RassTestConstants() {
        throw new UnsupportedOperationException("Instantiating constants class is prohibited");
    }

    public static final String AGENCY_DOC_TYPE_NAME = "AGCY";
    public static final Integer DEFAULT_DD_FIELD_MAX_LENGTH = Integer.valueOf(50);
    public static final String ERROR_OBJECT_KEY = "ERR";
    public static final String LONG_OBJECT_KEY = "KEY_WITH_EXTREMELY_LONG_TEXT_LENGTH";

    public static final String RASS_SERVICE_BEAN_NAME = "rassService";
    public static final String RASS_ROUTING_SERVICE_BEAN_NAME = "rassRoutingService";
    public static final String AGENCY_SERVICE_BEAN_NAME = "agencyService";
    public static final String ROUTE_HEADER_SERVICE_BEAN_NAME = "enDocumentRouteHeaderService";

    public static final class ResourcePropertyValues {
        
        private ResourcePropertyValues() {
            throw new UnsupportedOperationException("Instantiating nested constants class is prohibited");
        }
        
        public static final String MESSAGE_RASS_DOCUMENT_DESCRIPTION = "Auto-Generated {0} of {1}";
        public static final String MESSAGE_RASS_DOCUMENT_ANNOTATION_ROUTE = "Automatically created and routed";
    }

}
