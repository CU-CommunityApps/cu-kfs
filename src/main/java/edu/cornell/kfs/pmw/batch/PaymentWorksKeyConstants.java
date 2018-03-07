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
    public static final String NEW_VENDOR_PVEN_NOTES_BUSINESS_PURPOSE_LABEL = "note.label.paymentworks.pven.business.purpose";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_MESSAGE = "note.message.paymentworks.pven.conflict.of.interest";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_EMPLOYEE_NAME_LABEL = "note.label.paymentworks.pven.conflict.of.interest.employee.name";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_PHONE_NUMBER_LABEL = "note.label.paymentworks.pven.conflict.of.interest.phone.number";
    public static final String NEW_VENDOR_PVEN_NOTES_CONFLICT_OF_INTEREST_RELATIONSHIP_LABEL = "note.label.paymentworks.pven.conflict.of.interest.relationship";
    
    //PaymentWorks Batch Report Messages
    public static final String NEW_VENDOR_REQUEST_CUSTOM_FIELD_MISSING_ERROR_MESSAGE = "error.paymentworks.new.vendor.custom.field.not.staging.table.column";
    public static final String NEW_VENDOR_REQUEST_CUSTOM_FIELD_CONVERSION_EXCEPTION_ERROR_MESSAGE = "error.paymentworks.new.vendor.custom.field.conversion.exception"; 
    public static final String NEW_VENDOR_DETAIL_WAS_NOT_FOUND_ERROR_MESSAGE = "error.paymentworks.new.vendor.detail.not.returned.by.web.service";
    public static final String DUPLICATE_NEW_VENDOR_REQUEST_ERROR_MESSAGE = "error.paymentworks.duplicate.new.vendor.request"; 
    public static final String INITIAL_SAVE_TO_PMW_STAGING_TABLE_FAILED_ERROR_MESSAGE = "error.paymentworks.intial.staging.table.save.failed";
    public static final String END_OF_REPORT_MESSAGE = "message.paymentworks.end.of.report";
    public static final String NO_RECORDS_WITH_VALIDATION_ERRORS_MESSAGE = "message.paymentworks.no.records.with.validation.errors";
    public static final String NO_VALIDATION_ERRORS_TO_OUTPUT_MESSAGE = "message.paymentworks.no.validation.errors";
    public static final String NO_RECORDS_PROCESSED_MESSAGE = "message.paymentworks.no.records.processed";
    public static final String MANUAL_DATA_ENTRY_NOT_REQUIRED_MESSAGE = "message.paymentworks.no.records.needing.manual.data.entry";
    
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
    public static final String PO_VENDOR_PROCESSING_NOT_AUTOMATIC_YET = "error.paymentworks.po.vendor.not.implemented.yet"; //REMOVE THIS MESSAGE AFTER PO VENDORS HAVE BEEN IMPLEMENTED
    public static final String COULD_NOT_DETERMINE_TAX_BUSINESS_RULE_TO_USE = "error.paymentworks.unknown.tax.business.rule";

}
