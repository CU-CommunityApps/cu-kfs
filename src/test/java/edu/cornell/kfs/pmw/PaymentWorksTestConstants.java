package edu.cornell.kfs.pmw;

public class PaymentWorksTestConstants {

    public static final class RefreshTokenErrorMessages {
        public static final String INVALID_USER_ID = "Invalid User ID";
        public static final String INVALID_AUTHORIZATION_TOKEN = "Invalid Authorization Token";
    }

    public static final class SupplierUploadErrorMessages {
        public static final String FILE_CONTAINED_ZERO_ROWS = "File contained zero rows";
        public static final String FILE_ONLY_CONTAINED_HEADER_ROW = "File contained a header row but no data rows";
        public static final String FILE_CONTAINED_INVALID_HEADERS = "File did not contain the correct headers";
        public static final String VENDOR_DATA_MISMATCH_ERROR_PREFIX = "Unexpected data values detected on vendor at index ";
    }

    public static final class ParameterTestValues {
        public static final String PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_COUNT_MISMATCH_MESSAGE_PREFIX = "Mismatched vendor upload counts "
                + "between KFS and PMW for these vendors: ";
        public static final String PAYMENTWORKS_UPLOAD_SUPPLIERS_REPORT_VENDOR_UPLOAD_FAILURE_MESSAGE_PREFIX = "Vendor upload failed for these vendors: ";
    }

}
