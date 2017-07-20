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
    public static final String CONCUR_EMPLOYEE_ID_NOT_FOUND_IN_KFS = "validation.error.concur.id.not.found";
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
    public static final String MESSAGE_CONCUR_TOKEN_REPLACE_QUESTION = "message.concur.token.replace.question";
    public static final String MESSAGE_CONCUR_TOKEN_REPLACE_SUCCESS = "message.concur.token.replace.success";
    public static final String MESSAGE_CONCUR_TOKEN_REVOKE_AND_REPLACE_SUCCESS = "message.concur.token.revoke.and.replace.success";
    public static final String MESSAGE_CONCUR_TOKEN_REFRESH_SUCCESS = "message.concur.token.refresh.success";
    public static final String ERROR_CONCUR_REFRESH_NONEXISTENT_TOKEN = "error.concur.refresh.nonexistent.token";
}
