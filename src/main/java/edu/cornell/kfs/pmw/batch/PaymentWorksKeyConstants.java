package edu.cornell.kfs.pmw.batch;

public class PaymentWorksKeyConstants {

    //Boiler plate for PaymentWorks generated KFS Notes
    public static final String NOTES_INITIATOR_LABEL = "note.label.paymentworks.initiator";
    public static final String NEW_VENDOR_PVEN_NOTES_W9_URL_EXISTS_MESSAGE = "note.message.paymentworks.pven.w9.exists";
    public static final String NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_PROVIDED_LABEL = "note.label.paymentworks.pven.goods.services.provided";
    public static final String NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_CURRENT_PAYROLL_PAID_LABEL = "note.label.paymentworks.pven.goods.services.provider.currently.payroll.paid";
    public static final String NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_EVER_PAYROLL_PAID_LABEL = "note.label.paymentworks.pven.goods.services.provider.ever.payroll.paid";
    public static final String NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_NOT_SOLE_PROPRIETOR_LABEL = "note.label.paymentworks.pven.goods.services.provider.not.sole.proprietor";
    public static final String NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_RECEIVING_EQUIPMENT_TRAINING_LABEL = "note.label.paymentworks.pven.goods.services.provider.receiving.equipment.training";
    public static final String NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_NO_MARKETING_LABEL = "note.label.paymentworks.pven.goods.services.provider.no.marketing";
    public static final String NEW_VENDOR_PVEN_NOTES_GOODS_AND_SERVICES_NO_INSURANCE_LABEL = "note.label.paymentworks.pven.goods.services.provider.no.insurance.policy"; 
    public static final String NEW_VENDOR_PVEN_NOTES_PAYMENT_REASON_LABEL = "note.label.paymentworks.pven.payment.reason";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_MESSAGE = "note.message.paymentworks.pven.conflict.of.interest";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_EMPLOYEE_NAME_LABEL = "note.label.paymentworks.pven.conflict.of.interest.employee.name";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_PHONE_NUMBER_LABEL = "note.label.paymentworks.pven.conflict.of.interest.phone.number";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_RELATIONSHIP_LABEL = "note.label.paymentworks.pven.conflict.of.interest.relationship";
    public static final String NEW_VENDOR_PVEN_NOTES_INSURANCE_CERTIFICATE_URL_EXISTS_MESSAGE = "note.message.paymentworks.pven.insurance.certificate.exists";
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
    
    //PaymentWorks Business Rule Failure Messages
    public static final String ERROR_COMBINED_LEGAL_FIRST_LAST_NAME_TOO_LONG_FOR_KFS = "error.paymentworks.combined.last.first.names.too.long";
    public static final String ERROR_LEGAL_NAME_TOO_LONG_FOR_KFS = "error.paymentworks.legel.name.too.long";
    public static final String ERROR_LEGAL_NAME_NULL_OR_BLANK = "error.paymentworks.legel.name.null.or.blank";
    public static final String ERROR_DATE_IS_NOT_FORMATTED_CORRECTLY = "error.paymentworks.date.bad.format";
    public static final String ERROR_NYS_CERTIFIED_MINORTY_BUSINESS_DESCRIPTION = "error.paymentworks.nys.certified.mbe.description";
    public static final String ERROR_NYS_CERTIFIED_WOMAN_OWNED_BUSINESS_DESCRIPTION = "error.paymentworks.nys.certified.wbe.description";
    public static final String ERROR_NYS_CERTIFIED_DISABLED_VETERAN_BUSINESS_DESCRIPTION = "error.paymentworks.nys.certified.disabled.veteran.description";
    public static final String ERROR_COUNTRY_OF_INCORPORATION_BLANK = "error.paymentworks.incorporation.country.blank";
    public static final String ERROR_PRIMARY_ADDRESS_COUNTRY_BLANK = "error.paymentworks.primary.address.country.blank";
    public static final String ERROR_REMITTANCE_ADDRESS_COUNTRY_BLANK = "error.paymentworks.remittance.address.country.blank";
    public static final String ERROR_BANK_ADDRESS_COUNTRY_BLANK = "error.paymentworks.bank.address.country.blank";
    public static final String ERROR_FIPS_TAX_COUNTRY_BLANK = "error.paymentworks.tax.address.fips.country.blank.";
    public static final String ERROR_SINGLE_ISO_MAPS_TO_MULTIPLE_FIPS = "error.paymentworks.single.iso.maps.to.multiple.fips";
    public static final String ERROR_ISO_COUNTRY_NOT_FOUND = "error.paymentworks.iso.country.not.found";
    public static final String ERROR_W8_W9_URL_IS_NULL_OR_BLANK = "error.paymentworks.w8.w9.url.null.or.blank";
    public static final String ERROR_TAX_NUMBER_IS_NULL_OR_BLANK = "error.paymentworks.tax.number.null.or.blank";
    public static final String ERROR_TAX_NUMBER_TYPE_IS_NULL_OR_BLANK = "error.paymentworks.tax.number.type.null.or.blank";
    public static final String FOREIGN_VENDOR_PROCESSING_NOT_AUTOMATIC_YET = "error.paymentworks.foreign.vendor.not.implemented.yet"; //REMOVE THIS MESSAGE AFTER FOREIGN VENDORS HAVE BEEN IMPLEMENTED
    public static final String COULD_NOT_DETERMINE_TAX_BUSINESS_RULE_TO_USE = "error.paymentworks.unknown.tax.business.rule";
    public static final String ERROR_PAYMENTWORKS_VENDOR_TYPE_EMPTY = "error.paymentworks.empty.vendor.type";
    public static final String ERROR_VENDOR_HEADER_GENERATED_IDENTIFIER_MISSING = "error.paymentworks.vendor.header.generated.identifier.missing";
    public static final String ERROR_VENDOR_HEADER_DETAILED_ASSIGNED_IDENTIFIER_MISSING = "error.paymentworks.vendor.header.detailed.assigned.identifier.missing";
    public static final String ERROR_BANK_ROUTING_NUMBER_INVALID = "error.paymentworks.bank.routing.number.invalid";
    public static final String ERROR_BANK_ACCOUNT_NUMBER_INVALID = "error.paymentworks.bank.account.number.invalid";
    public static final String ERROR_BANK_ACCOUNT_TYPE_INVALID = "error.paymentworks.bank.account.type.invalid";
    public final static String NO_ACH_DATA_PROVIDED_BY_VENDOR_MESSAGE = "message.paymentworks.no.ach.data.provided";

    public final static String WARNING_PMW_TOKEN_REFRESH_UPDATE_NONPRODSQL = "warning.paymentworks.token.refresh.nonprod";

}
