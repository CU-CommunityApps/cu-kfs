package edu.cornell.kfs.module.ar;

public final class CuArKeyConstants {
    public static final String CGINVOICE_CREATION_AGENCY_CUSTOMER_MISMATCH = "cginvoice.creation.agency.customer.mismatch";
    public static final String CGINVOICE_CREATION_CUSTOMER_INVOICING_ADDRESS_MISSING = "cginvoice.creation.customer.invoicing.address.missing";

    /* CU Customization: Backport FINP-10040 These constants can be removed 2023-09-20 financials patch. */
    public static final class CashControlDetailConstants {
        public static final String ERROR_DETAILPARSER_INVALID_FILE_FORMAT = "error.detailParser.invalidFileFormat";
        public static final String ERROR_DETAILPARSER_DETAIL_LINE = "error.detailParser.detailLine";
        public static final String ERROR_DETAILPARSER_DETAIL_PROPERTY = "error.detailParser.itemProperty";
        public static final String ERROR_DETAILPARSER_INVALID_NUMERIC_VALUE = "error.detailParser.invalidNumericValue";
        public static final String ERROR_DETAILPARSER_WRONG_PROPERTY_NUMBER = "error.detailParser.wrongPropertyNumber";

        private CashControlDetailConstants() {
            // Prevent instantiation
        }
    }
    /* End CU Customization FINP-10040 Backport 20230920 */
}