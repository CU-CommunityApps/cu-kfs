package edu.cornell.kfs.pmw.batch;

public class PaymentWorksPropertiesConstants {
    
    public static final class PaymentWorksVendor {
        public static final String PMW_VENDOR_REQUEST_ID = "pmwVendorRequestId";
        public static final String KFS_VENDOR_DOCUMENT_NUMBER = "kfsVendorDocumentNumber";
        public static final String PMW_REQUEST_STATUS = "pmwRequestStatus";
        public static final String KFS_VENDOR_PROCESSING_STATUS = "kfsVendorProcessingStatus";
        public static final String KFS_ACH_PROCESSING_STATUS = "kfsAchProcessingStatus";
        public static final String PMW_TRANSACTION_TYPE = "pmwTransactionType";
    }

    public static final class PaymentWorksFieldMapping {
        public static final String PMW_FIELD_ID = "paymentWorksFieldId";
    }
    
    public static final class PaymentWorksIsoCountryToFipsCountryAssociation {
        public static final String FIPS_CNTRY_CD = "fipsCountryCode";
    }

}
