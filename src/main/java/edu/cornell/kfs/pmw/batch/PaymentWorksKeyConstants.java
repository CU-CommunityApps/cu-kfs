package edu.cornell.kfs.pmw.batch;

public class PaymentWorksKeyConstants {

    //Boiler plate for PaymentWorks generated KFS Notes
    public static final String NOTES_INITIATOR_LABEL = "note.label.paymentworks.initiator";
    public static final String NEW_VENDOR_PVEN_NOTES_W8_URL_EXISTS_MESSAGE = "note.message.paymentworks.pven.w8.exists";
    public static final String NEW_VENDOR_PVEN_NOTES_W9_URL_EXISTS_MESSAGE = "note.message.paymentworks.pven.w9.exists";
    public static final String NEW_VENDOR_PVEN_NOTES_COMPLIANCE_SCREENING_MESSAGE = "note.message.paymentworks.pven.compliance.screening";
    public static final String NEW_VENDOR_PVEN_NOTES_PAYMENT_REASON_LABEL = "note.label.paymentworks.pven.payment.reason";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_MESSAGE = "note.message.paymentworks.pven.conflict.of.interest";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_EMPLOYEE_NAME_LABEL = "note.label.paymentworks.pven.conflict.of.interest.employee.name";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_PHONE_NUMBER_LABEL = "note.label.paymentworks.pven.conflict.of.interest.phone.number";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_RELATIONSHIP_LABEL = "note.label.paymentworks.pven.conflict.of.interest.relationship";
    public static final String NEW_VENDOR_PAAT_NOTES_PMW_VENDOR_NUMBER_LABEL = "note.label.paymentworks.vendor.number.paat.created.from";
    
    //Boiler plate for PaymentWorks generated KFS Payee ACH Account Explanation
    public static final String PAYEE_ACH_ACCOUNT_BANK_ACCOUNT_LABEL = "explanation.label.paymentworks.bank.account";
    public static final String PAYEE_ACH_ACCOUNT_NAME_ON_ACCOUNT_LABEL  = "explanation.label.paymentworks.name.on.account";
    public static final String PAYEE_ACH_ACCOUNT_BANK_ADDRESS_LABEL = "explanation.label.paymentworks.bank.address";
    public static final String PAYEE_ACH_ACCOUNT_BANK_ADDRESS_COUNTRY_LABEL  = "explanation.label.paymentworks.bank.address.country";
    public static final String PAYEE_ACH_ACCOUNT_BANK_ADDRESS_STREET1_LABEL = "explanation.label.paymentworks.bank.address.street1";
    public static final String PAYEE_ACH_ACCOUNT_BANK_ADDRESS_STREET2_LABEL = "explanation.label.paymentworks.bank.address.street2";
    public static final String PAYEE_ACH_ACCOUNT_BANK_ADDRESS_CITY_LABEL = "explanation.label.paymentworks.bank.address.city";
    public static final String PAYEE_ACH_ACCOUNT_BANK_ADDRESS_STATE_LABEL = "explanation.label.paymentworks.bank.address.state";
    public static final String PAYEE_ACH_ACCOUNT_BANK_ADDRESS_ZIP_LABEL = "explanation.label.paymentworks.bank.address.zip";
    public static final String PAYEE_ACH_ACCOUNT_BANK_SWIFTCODE = "explanation.label.paymentworks.bank.swiftcode";
    
    public static final String MESSAGE_AUTH_TOKEN_REFRESH_SUCCESS = "message.paymentworks.auth.token.refresh.success";
    public static final String ERROR_AUTH_TOKEN_REFRESH_FAILURE = "error.paymentworks.auth.token.refresh.failure";
    
    //PaymentWorks Batch Report Messages
    public static final String NEW_VENDOR_REQUEST_CUSTOM_FIELD_MISSING_ERROR_MESSAGE = "error.paymentworks.new.vendor.custom.field.not.staging.table.column";
    public static final String NEW_VENDOR_REQUEST_CUSTOM_FIELD_CONVERSION_EXCEPTION_ERROR_MESSAGE = "error.paymentworks.new.vendor.custom.field.conversion.exception"; 
    public static final String NEW_VENDOR_DETAIL_WAS_NOT_FOUND_ERROR_MESSAGE = "error.paymentworks.new.vendor.detail.not.returned.by.web.service";
    public static final String DUPLICATE_NEW_VENDOR_REQUEST_ERROR_MESSAGE = "error.paymentworks.duplicate.new.vendor.request"; 
    public static final String INITIAL_SAVE_TO_PMW_STAGING_TABLE_FAILED_ERROR_MESSAGE = "error.paymentworks.intial.staging.table.save.failed";
    public static final String INITIAL_PAYMENT_WORKS_VENDOR_RETRIEVAL_ERROR = "error.paymentworks.paymentworksvendor.unable.to.retrieve";
    public static final String EXCEPTION_GENERATED_DURING_PROCESSING = "error.paymentworks.exception.generated";
    public static final String END_OF_REPORT_MESSAGE = "message.paymentworks.end.of.report";
    public static final String NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE = "message.paymentworks.no.records.with.validation.errors";
    public static final String NO_VALIDATION_ERRORS_TO_OUTPUT_MESSAGE = "message.paymentworks.no.validation.errors";
    public static final String NO_RECORDS_PROCESSED_MESSAGE = "message.paymentworks.no.records.processed";
    public static final String MANUAL_DATA_ENTRY_NOT_REQUIRED_MESSAGE = "message.paymentworks.no.records.needing.manual.data.entry";
    public static final String NO_DISAPPROVED_VENDORS_WITH_PENDING_ACH_DATA_MESSAGE = "message.paymentworks.no.disapproved.vendors.with.pending.ach.data";
    public static final String NO_VENDORS_WITHOUT_ACH_DATA_MESSAGE = "message.paymentworks.no.vendors.without.ach.data";
    public static final String NO_RECORDS_GENERATING_EXCEPTIONS_MESSAGE = "message.paymentworks.no.records.generating.exceptions";
    public static final String NO_RECORDS_FOREIGN_ACH_BANK = "message.paymentworks.no.records.foreign.ach.bank";
    public static final String NO_RECORDS_FOREIGN_WIRE_WITH_ACH = "message.paymentworks.no.records.foreign.wire.with.ach";
    public static final String NO_RECORDS_DOMESTIC_WIRE_WITH_ACH = "message.paymentworks.no.records.domestic.wire.with.ach";
    
    //PaymentWorks Business Rule Failure Messages
    public static final String ERROR_LEGAL_NAME_NULL_OR_BLANK = "error.paymentworks.legel.name.null.or.blank";
    public static final String ERROR_DATE_IS_NOT_FORMATTED_CORRECTLY = "error.paymentworks.date.bad.format";
    public static final String ERROR_COUNTRY_OF_INCORPORATION_BLANK = "error.paymentworks.incorporation.country.blank";
    public static final String ERROR_PRIMARY_ADDRESS_COUNTRY_BLANK = "error.paymentworks.primary.address.country.blank";
    public static final String ERROR_REMITTANCE_ADDRESS_COUNTRY_BLANK = "error.paymentworks.remittance.address.country.blank";
    public static final String ERROR_W8_W9_URL_IS_NULL_OR_BLANK = "error.paymentworks.w8.w9.url.null.or.blank";
    public static final String ERROR_TAX_NUMBER_IS_NULL_OR_BLANK = "error.paymentworks.tax.number.null.or.blank";
    public static final String ERROR_TAX_NUMBER_TYPE_IS_NULL_OR_BLANK = "error.paymentworks.tax.number.type.null.or.blank";
    public static final String ERROR_PAYMENTWORKS_VENDOR_TYPE_EMPTY = "error.paymentworks.empty.vendor.type";
    public static final String ERROR_VENDOR_HEADER_GENERATED_IDENTIFIER_MISSING = "error.paymentworks.vendor.header.generated.identifier.missing";
    public static final String ERROR_VENDOR_HEADER_DETAILED_ASSIGNED_IDENTIFIER_MISSING = "error.paymentworks.vendor.header.detailed.assigned.identifier.missing";
    public static final String ERROR_BANK_ROUTING_NUMBER_INVALID = "error.paymentworks.bank.routing.number.invalid";
    public static final String ERROR_BANK_ACCOUNT_NUMBER_INVALID = "error.paymentworks.bank.account.number.invalid";
    public static final String ERROR_BANK_ACCOUNT_TYPE_INVALID = "error.paymentworks.bank.account.type.invalid";
    public final static String NO_ACH_DATA_PROVIDED_BY_VENDOR_MESSAGE = "message.paymentworks.no.ach.data.provided";
    public static final String ERROR_PAYMENTWORKS_DATE_OF_BIRTH_DESCRIPTION = "error.paymentworks.date.of.birth.description";
    public static final String ERROR_W8_SIGNED_DATE_DESCRIPTION = "error.paymentworks.w8.signed.date.description";
    public static final String ERROR_PAYMENTWORKS_BANK_NOT_US = "error.paymentworks.bank.not.us";
    public static final String ERROR_PAYMENTWORKS_DOMESTIC_WIRE_VENDOR_ACH = "error.paymentworks.domestic.wire.vendor.with.ach";
    public static final String ERROR_PAYMENTWORKS_FOREIGN_WIRE_VENDOR_ACH = "error.paymentworks.foreign.wire.vendor.with.ach";

    public static final String ERROR_PAYMENTWORKS_VENDOR_GLOBAL_ACTION_EMPTY = "error.paymentworks.vendor.global.action.empty";
    public static final String ERROR_PAYMENTWORKS_VENDOR_GLOBAL_ACTION_INVALID = "error.paymentworks.vendor.global.action.invalid";
    public static final String ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_EMPTY = "error.paymentworks.vendor.global.details.empty";
    public static final String ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_DUPLICATE = "error.paymentworks.vendor.global.details.duplicate";
    public static final String ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_ADDLINE_NOT_FOUND = "error.paymentworks.vendor.global.details.addline.not.found";
    public static final String ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_NOT_FOUND = "error.paymentworks.vendor.global.details.not.found";
    public static final String ERROR_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_UPLOAD_STATUS_INELIGIBLE = "error.paymentworks.vendor.global.details.upload.status.ineligible";
    public static final String WARNING_PAYMENTWORKS_VENDOR_GLOBAL_DETAILS_UPLOAD_STATUS_MATCH = "warning.paymentworks.vendor.global.details.upload.status.match";

}
