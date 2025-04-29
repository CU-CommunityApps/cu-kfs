package edu.cornell.kfs.vnd;

/**
 * Holds error key constants for Vendor.
 */
public class CUVendorKeyConstants {

    // Vendor Maintenance
    public static final String CONFIRM_VENDOR_DATE_EXPIRED = "message.vendorMaint.confirm.expired.date";
    public static final String ERROR_PO_VENDOR_REQUIRES_PAYMENT_TERMS = "error.document.vendor.poVendorPaymentTermsRequired";
    
    //Vendor Maintenance Supplier Diversity Expiration Date
    public static final String ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_IN_PAST = "error.document.vendor.supplierDiversityExpirationDateIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_SUPPLIER_DIVERSITY_DATE_BLANK = "error.document.vendor.supplierDiversityExpirationDateCannotBeBlank";
    
    //Vendor Maintenance Insurance Tracking
    public static final String ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_EXPR_DATE_NEEDED = "error.document.vendor.generalLiabilityExpirationDateNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_EXPR_DATE_IN_PAST = "error.document.vendor.generalLiabilityExpirationDateIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_GENERAL_LIABILITY_COVERAGE_NEEDED = "error.document.vendor.generalLiabilityCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_AUTO_EXPR_NEEDED = "error.document.vendor.automobileExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_AUTO_EXPR_IN_PAST = "error.document.vendor.automobileExpirationIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_AUTO_COVERAGE_NEEDED = "error.document.vendor.automobileCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_WC_EXPR_NEEDED = "error.document.vendor.workmansCompExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_WC_EXPR_IN_PAST = "error.document.vendor.workmansCompExpirationIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_WC_COVERAGE_NEEDED = "error.document.vendor.workmansCompCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_UMB_EXPR_NEEDED = "error.document.vendor.umbrellaExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_UMB_EXPR_IN_PAST = "error.document.vendor.umbrellaExpirationIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_UMB_COVERAGE_NEEDED = "error.document.vendor.umbrellaCoverageNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_EXPR_NEEDED = "error.document.vendor.healthLicenseExpirationNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_EXPR_IN_PAST = "error.document.vendor.healthLicenseExpirationIsInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_HEALTH_LICENSE_NEEDED = "error.document.vendor.healthLicenseNeeded";
    public static final String ERROR_DOCUMENT_VNDMAINT_INSURANCE_REQUIRED_USED_WO_DATA = "error.document.vendor.insuranceReqIndicatedWOData";
    
    //Vendor Maintenance Credit Merchant
    
    public static final String ERROR_DOCUMENT_VNDMAINT_CREDIT_MERCHANT_NAME_DUPLICATE = "error.document.vendor.creditMerchantNameDuplicate";
    public static final String ERROR_DOCUMENT_VNDMAINT_CREDIT_MERCHANT_NAME_BLANK = "error.document.vendor.creditMerchantNameBlank";
    public static final String ERROR_DOCUMENT_VNDMAINT_DATE_IN_PAST = "error.document.vendor.dateInPast";
    public static final String ERROR_DOCUMENT_VNDMAINT_DATE_IN_FUTURE = "error.document.vendor.dateInFuture";
    // Vendor Maintenance Commodity Code
    public static final String ERROR_VENDOR_COMMODITY_CODE_REQUIRE_ONE_DEFAULT_IND = "error.vendorCommodityCode.require.one.defaultIndicator";
    public static final String ERROR_VENDOR_COMMODITY_CODE_IS_REQUIRED_FOR_THIS_VENDOR_TYPE = "error.vendorCommodityCode.is.required.for.vendorType";
    public static final String ERROR_VENDOR_COMMODITY_CODE_DEFAULT_IS_REQUIRED_FOR_B2B = "error.vendorCommodityCode.default.required.for.b2b";
    public static final String ERROR_VENDOR_COMMODITY_CODE_DOES_NOT_EXIST = "error.vendorCommodityCode.nonExistance";
    public static final String ERROR_DEFAULT_VENDOR_COMMODITY_CODE_ALREADY_EXISTS = "error.vendorCommodityCode.defaultAlreadySelected";
    public static final String ERROR_VENDOR_COMMODITY_CODE_ALREADY_ASSIGNED_TO_VENDOR = "error.vendorCommodityCode.duplicateCommodityCode";
    public static final String ERROR_PO_TRANSMISSION_REQUIRES_PO_ADDRESS = "error.vendorMaint.vendor.vendorAddress.poTransmissionMethodCode.missing";
    public static final String ERROR_PO_TRANMSISSION_NOT_ALLOWED_FOR_VENDOR_TYPE = "error.vendorMaint.vendor.vendorAddress.poTransmissionMethodCode.notAllowed";
    public static final String ERROR_NO_PO_TRANSMISSION_WITH_NON_PO_ADDRESS = "error.vendorMaint.vendor.vendorAddress.poTransmissionMethodCode.specified";
    public static final String ERROR_PO_TRANSMISSION_METHOD_UNKNOWN = "error.vendorMaint.poTransmissionMethodActionUnknown";
    public static final String ERROR_PO_TRANSMISSION_REQUIRES_FAX_NUMBER = "error.vendorMaint.vendorAddress.faxNumberMissing";
    public static final String ERROR_PO_TRANSMISSION_REQUIRES_EMAIL = "error.vendorMaint.vendorAddress.emailAddressMissing";
    public static final String ERROR_PO_TRANSMISSION_REQUIRES_US_POSTAL = "error.vendorMaint.vendorAddress.USPostalAddressMissing";

    public static final String ERROR_DOCUMENT_VENDOR_TYPE_IS_REQUIRED_FOR_ADD_VENDORADRESS = "error.document.vendortype.isrequired.for.add.vendoraddress";

    public static final String ERROR_VENDOR_TAX_TYPE_AND_NUMBER_COMBO_EXISTS_AND_PRINT_EXISTING = "error.vendorMaint.addVendor.vendor.exists.printExisting";

    public static final class EmployeeComparisonReportKeys {
        public static final String PREFIX = "vendor.employee.comparison.report.";
        public static final String SUMMARY_LABEL_PREFIX = PREFIX + "summary.label.";

        public static final String FILE_NAME = PREFIX + "file.name";
        public static final String TITLE = PREFIX + "title";
        public static final String SECTION_SEPARATOR = PREFIX + "section.separator";
        public static final String SECTION_HEADER = PREFIX + "section.header";
        public static final String SUMMARY_SECTION_TITLE = PREFIX + "summary.section.title";
        public static final String SUMMARY_PROCESSED_FILE_NAME_LABEL = PREFIX + "summary.processed.file.name.label";
        public static final String SUMMARY_PROCESSED_FILE_NAME = PREFIX + "summary.processed.file.name";
        public static final String SUMMARY_LINE = PREFIX + "summary.line";
        public static final String DETAIL_SECTION_TITLE = PREFIX + "detail.section.title";
        public static final String DETAIL_TABLE_HEADER = PREFIX + "detail.table.header";
        public static final String DETAIL_TABLE_SEPARATOR = PREFIX + "detail.table.separator";
        public static final String DETAIL_TABLE_ROW = PREFIX + "detail.table.row";
        public static final String EMPTY_DETAIL_SECTION_MESSAGE = PREFIX + "empty.detail.section.message";
    }
    
    public static final String VENDOR_UNABLE_TO_CALL_WORKDAY = "vendor.workday.call.error";
    public static final String ACTIVE_EMPLOYEE_MESSAGE = "vendor.workday.active";
    public static final String TERMINATED_EMPLOYEE_MESSAGE = "vendor.workday.terminated";

}
