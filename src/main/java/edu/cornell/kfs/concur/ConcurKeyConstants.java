package edu.cornell.kfs.concur;

public class ConcurKeyConstants {
    public static final String CONCUR_ACCOUNT_INFO_IS_REQUIRED = "error.concur.account.info.required";
    public static final String INCORRECT_CONCUR_STATUS_CODE = "error.concur.status.invalid";

    public static final String CONCUR_SAE_ORPHANED_CASH_ADVANCE = "validation.error.concur.sae.orphaned.cash.advance";
    public static final String CONCUR_SAE_GROUP_WITH_ORPHANED_CASH_ADVANCE = "validation.error.concur.sae.group.with.orphaned.cash.advance";

    public static final String CONCUR_REQUEST_EXTRACT_NOT_CASH_ADVANCE_DATA_LINE = "validation.error.concur.request.extract.not.cash.advance";
    public static final String CONCUR_REQUEST_EXTRACT_CASH_ADVANCE_USED_IN_EXPENSE_REPORT = "validation.error.concur.request.extract.cash.advance.in.expense.report";
    public static final String CONCUR_REQUEST_EXTRACT_REQUEST_ID_INVALID = "validation.error.concur.request.extract.invalid.request.id";
    public static final String CONCUR_REQUEST_EXTRACT_EMPLOYEE_ID_NULL_OR_BLANK = "validation.error.concur.request.extract.invalid.employee.id";
    public static final String CONCUR_EMPLOYEE_ID_NOT_FOUND_IN_KFS = "validation.error.concur.employee.id.not.found";
    public static final String CONCUR_REQUEST_EXTRACT_PAYEE_ID_TYPE_INVALID = "validation.error.concur.request.extract.invalid.payee.id.type";
    public static final String CONCUR_REQUEST_EXTRACT_REQUEST_AMOUNT_INVALID = "validation.error.concur.request.extract.invalid.request.amount";
    public static final String CONCUR_REQUEST_EXTRACT_DUPLICATE_CASH_ADVANCE_DETECTED = "validation.error.concur.request.extract.duplicate.cash.advance";
    public static final String CONCUR_REQUEST_EXTRACT_INVALID_KEYS_FOR_DUPLICATE_CHECK = "validation.error.concur.request.extract.invalid.keys.duplicate.check";
    public static final String CONCUR_REQUEST_EXTRACT_HEADER_ROW_COUNT_FAILED = "validation.error.concur.request.extract.header.row.count.failed";
    public static final String CONCUR_REQUEST_EXTRACT_HAS_NO_REQUEST_DETAIL_LINES = "validation.error.concur.request.extract.no.request.detail.lines";
    public static final String CONCUR_REQUEST_EXTRACT_CONTAINS_BAD_CUSTOMER_PROFILE_GROUP = "validation.error.concur.request.extract.contains.bad.customer.profile.group";
    public static final String CONCUR_REQUEST_EXTRACT_HEADER_AMOUNT_FILE_AMOUNT_MISMATCH = "validation.error.concur.request.extract.header.amount.file.amount.mismatch";
    public static final String CONCUR_REQUEST_EXTRACT_NO_REPORT_EMAIL_SUBJECT = "message.concur.requestExtract.no.report.email.subject";
    public static final String CONCUR_REQUEST_EXTRACT_NO_REPORT_EMAIL_BODY = "message.concur.requestExtract.no.report.email.body";
    public static final String CONCUR_SAE_NO_REPORT_EMAIL_SUBJECT = "message.concur.sae.no.report.email.subject";
    public static final String CONCUR_SAE_NO_REPORT_EMAIL_BODY = "message.concur.sae.no.report.email.body";
    public static final String CONCUR_INCOMPLETE_ADDRESS = "validation.error.concur.incomplete.address";
    public static final String MESSAGE_CONCUR_TOKEN_REPLACE_SUCCESS = "message.concur.token.replace.success";
    public static final String MESSAGE_CONCUR_TOKEN_REVOKE_AND_REPLACE_SUCCESS = "message.concur.token.revoke.and.replace.success";
    public static final String MESSAGE_CONCUR_TOKEN_REFRESH_SUCCESS = "message.concur.token.refresh.success";
    public static final String MESSAGE_CONCUR_TOKEN_REVOKE_SUCCESS = "message.concur.token.revoke.success";
    public static final String ERROR_CONCUR_TOKEN_REVOKE_NO_TOKEN = "error.concur.token.revoke.no.token";
    public static final String MESSAGE_CONCUR_TOKEN_RESET_SUCCESS = "message.concur.token.reset.success";
    public static final String ERROR_CONCUR_TOKEN_RESET_IN_PRODUCTION = "error.concur.token.retet.production"; 

    public static final String CONCUR_REQUEST_EXTRACT_CASH_ADVANCE_INVALID_UNIQUE_IDENTIFIER = "validation.error.concur.request.extract.cash.advance.invalid.unique.identifier";
    public static final String CONCUR_SAE_NOT_REQUESTED_CASH_ADVANCE_DATA_LINE = "validation.error.concur.sae.not.requested.cash.advance";
    public static final String CONCUR_SAE_NOT_APPROVED_REQUESTED_CASH_ADVANCE_DATA_LINE = "validation.error.concur.sae.not.cash.admin.approved";
    public static final String CONCUR_SAE_REQUESTED_CASH_ADVANCE_USED_IN_EXPENSE_REPORT = "validation.error.concur.sae.requested.cash.advance.in.expense.report";
    public static final String CONCUR_SAE_REQUESTED_DUPLICATE_CASH_ADVANCE_DETECTED = "validation.error.concur.duplicate.sae.requested.cash.advance";
    public static final String CONCUR_SAE_REQUESTED_CASH_ADVANCE_INVALID_KEYS_FOR_DUPLICATE_CHECK = "validation.error.concur.sae.invalid.keys.duplicate.check";
    public static final String CONCUR_SAE_REQUESTED_CASH_ADVANCE_INVALID_UNIQUE_IDENTIFIER = "validation.error.concur.sae.requested.cash.advance.invalid.unique.identifier";
    public static final String CONCUR_SAE_REQUESTED_CASH_ADVANCE_EMPLOYEE_ID_NULL_OR_BLANK = "validation.error.concur.sae.requested.cash.advance.invalid.employee.id";
    public static final String CONCUR_SAE_REQUESTED_CASH_ADVANCE_AMOUNT_INVALID = "validation.error.concur.sae.requested.cash.advance.invalid.request.amount";
    public static final String CONCUR_SAE_HEADER_ROW_COUNT_FAILED = "validation.error.concur.sae.requested.cash.advance.header.row.count.failed";
    public static final String CONCUR_SAE_HAS_NO_REQUEST_DETAIL_LINES = "validation.error.concur.sae.requested.cash.advance.no.request.detail.lines";
    public static final String CONCUR_SAE_CONTAINS_BAD_CUSTOMER_PROFILE_GROUP = "validation.error.concur.sae.requested.cash.advance.contains.bad.customer.profile.group";
    public static final String CONCUR_SAE_HEADER_JOURNAL_AMOUNT_FILE_AMOUNT_MISMATCH = "validation.error.concur.sae.requested.cash.advance.header.amount.file.amount.mismatch";
    public static final String CONCUR_SAE_DETAIL_JOURNAL_AMOUNT_NULL_DETECTED = "validation.error.concur.sae.detail.journal.amount.null.detected";
    public static final String CONCUR_SAE_REQUESTED_CASH_ADVANCES_NO_REPORT_EMAIL_SUBJECT = "message.concur.sae.requested.cash.advance.no.report.email.subject";
    public static final String CONCUR_SAE_REQUESTED_CASH_ADVANCES_NO_REPORT_EMAIL_BODY = "message.concur.sae.requested.cash.advance.no.report.email.body";
    public static final String MESSAGE_CONCUR_REFRESH_TOKEN_NONPROD_WARNING = "message.concur.refresh.token.nonprod.warning";
    public static final String MESSAGE_CONCUR_TOKEN_DATE = "message.concur.token.date";
    public static final String MESSAGE_CONCUR_TOKEN_UPDATE_SUCCESS = "message.concur.token.update.success";
    
    public static final String MESSAGE_CONCUR_EXPENSEV3_INTIAL_EXPENSE_LISTING = "message.concur.expensev3.intial.expense.listing";
    public static final String MESSAGE_CONCUR_EXPENSEV3_EXPENSE_LISTING_NEXT_PAGE = "message.concur.expensev3.intial.expense.listing.next.page";
    public static final String MESSAGE_CONCUR_EXPENSEV3_EXPENSE_REPORT = "message.concur.expensev3.expense.report";
    public static final String MESSAGE_CONCUR_EXPENSEV3_EXPENSE_ALLOCATION_LISTING = "message.concur.expensev3.expense.allocation.listing";
    public static final String MESSAGE_CONCUR_EXPENSEV3_EXPENSE_ALLOCATION_LISTING_NEXT_PAGE = "message.concur.expensev3.expense.allocation.listing.next.page";

    public static final String MESSAGE_CONCUR_EXPENSEV4_EXPENSE_REPORT_WORKFLOW = "message.concur.expensev4.expense.report.workflow";

    public static final String MESSAGE_CONCUR_REQUESTV4_LISTING = "message.concur.requestv4.listing";
    public static final String MESSAGE_CONCUR_REQUESTV4_REQUEST = "message.concur.requestv4.request";
}
