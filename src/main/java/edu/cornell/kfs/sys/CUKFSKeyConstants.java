package edu.cornell.kfs.sys;

import org.kuali.kfs.sys.KFSKeyConstants;

public class CUKFSKeyConstants extends KFSKeyConstants {

    public static final String MESSAGE_BATCH_UPLOAD_TITLE_COMMODITY_CODE = "message.batchUpload.title.commodityCode";
    public static final String MESSAGE_BATCH_UPLOAD_TITLE_INACTIVATE_CONVERT_CODE = "message.batchUpload.title.InactivateConvertCode";

    public static final String ERROR_DOCUMENT_ACCMAINT_PROGRAM_CODE_NOT_GROUP_CODE = "error.document.accountMaintenance.programCodeNotAssociatedWithGroupCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_PROGRAM_CODE_CANNOT_BE_BLANK_FOR_GROUP_CODE = 
            "error.document.accountMaintenance.programCodeCannotBeBlank";
    public static final String ERROR_DOCUMENT_ACCMAINT_APPROP_ACCT_NOT_GROUP_CODE = "error.document.accountMaintenance.appropAcctNotAssociatedWithGroupCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_MJR_RPT_CAT_CODE_NOT_EXIST = 
            "error.document.accountMaintenance.majorRptgCatCodeEnteredDoesNotExist";
    public static final String ERROR_DOCUMENT_BA_ACCOUNT_AMOUNTS_BALANCED = "error.document.ba.accountAmountsNotBalanced";
    public static final String ERROR_DOCUMENT_BA_ACCOUNT_BASE_AMOUNTS_BALANCED = "error.document.ba.accountBaseAmountsNotBalanced";
    public static final String ERROR_DOCUMENT_OBJCDMAINT_CG_RPT_CAT_CODE_NOT_EXIST = "error.document.objectCodeMaintenance.cgReptgCodeEnteredDoesNotExist";
    public static final String ERROR_DOCUMENT_VNDMAINT_W9RECEIVED_NOT_POPULATED = "error.document.vendor.w9ReceivedNotPopulatedButIndicatorIsTrue";
    public static final String ERROR_DOCUMENT_VNDMAINT_W9RECEIVED_POPULATED_W_O_IND = "error.document.vendor.w9ReceivedPopulatedButIndicatorIsNull";
    public static final String ERROR_NO_UNIT_COST_WITH_ACCOUNTS = "error.no.unit.cost.with.accounts";
    public static final String ERROR_UNIT_COST_W_O_ACCOUNT = "error.unit.cost.with.out.accounts";
    public static final String ERROR_NO_ZERO_PERCENT_ACCOUNT_LINES_ALLOWED = "error.no.zero.percent.account.lines.allowed";
    
    public static final String MESSAGE_REPORT_NEW_COMMODITY_CODE_TITLE_LINE = "message.report.commodityCode.new.titleLine";
    public static final String MESSAGE_REPORT_UPDATE_COMMODITY_CODE_TITLE_LINE = "message.report.commodityCode.update.titleLine";
    public static final String MESSAGE_REPORT_INACTIVE_COMMODITY_CODE_TITLE_LINE = "message.report.commodityCode.inactive.titleLine";
    
    public static final String WARNING_DV_PAYEE_MUST_BE_ACTIVE = "warning.dv.payee.mustBeActive";
    
    public static final String MESSAGE_BATCH_UPLOAD_TITLE_REQUEUER_FLAT_FILE = "message.batchUpload.title.requeuer";
    public static final String MESSAGE_BATCH_UPLOAD_TITLE_REINDEX_FLAT_FILE = "message.batchUpload.title.reindex";
    // KFSPTS-2238
    public static final String MESSAGE_BATCH_UPLOAD_TITLE_ACCOUNT_REVERSION_FLAT_FILE = "message.batchUpload.title.accountReversion";
    
    // KFSPTS-985 User Favorites Profile
    public static final String ERROR_MULTIPLE_FAVORITE_ACCOUNTS = "error.multiple.primary.favorite.account";
    public static final String ERROR_DUPLICATE_FAVORITE_ACCOUNT_DESCRIPTION = "error.duplicate.favorite.account.description";
    public static final String ERROR_DUPLICATE_ACCOUNTINGLINE = "error.duplicate.accountline";
    public static final String ERROR_FAVORITE_ACCOUNT_NOT_SELECTED = "error.favorite.account.not.selected";
    public static final String ERROR_FAVORITE_ACCOUNT_NOT_EXIST = "error.favorite.account.not.exist";
    public static final String ERROR_FAVORITE_ACCOUNT_EXIST = "error.favorite.account.exist";
    public static final String ERROR_USER_PROFILE_EXIST = "error.user.profile.exist";
    public static final String ERROR_ACCOUNT_EXPIRED = "error.account.expired";
    
    public static final String ERROR_YEAREND_DOCUMENT_MISSING_ORG_REVIEWER = "error.yearend.document.missing.org.reviewer";
    
    public static final String ERROR_DOCUMENT_PREENCUMBER_CONFLICTING_START_END = "error.document.preEncumbrance.startAfterEnd";    
    public static final String ERROR_DOCUMENT_PREENCUMBER_INVALID_END = "error.document.preEncumbrance.endBeforeToday";
    public static final String ERROR_DOCUMENT_PREENCUMBER_INVALID_START = "error.document.preEncumbrance.startBeforeToday";
    public static final String ERROR_DOCUMENT_PREENCUMBER_DATE_PAST_YEAR_END = "error.document.preEncumbrance.dateAfterYearEnd";
    public static final String ERROR_DOCUMENT_PREENCUMBER_NEEDS_START_DATE = "error.document.preEncumbrance.startDateRequired";
    public static final String ERROR_DOCUMENT_PREENCUMBER_NEEDS_END_OR_COUNT = "error.document.preEncumbrance.endDateOrCountRequired";
    public static final String ERROR_DOCUMENT_PREENCUMBER_NEEDS_AMOUNT_SPECIFIED = "error.document.preEncumbrance.amountRequired";
    public static final String ERROR_DOCUMENT_PREENCUMBER_BOTH_REV_DATES_USED = "error.document.preEncumbrance.bothRevDatesUsed";
    public static final String ERROR_DOCUMENT_PREENCUMBER_GENERATED_ENTRIES_SPAN_FY = "error.document.preEncumbrance.generatedEntriesSpanFiscalYears";
    public static final String ERROR_DOCUMENT_PREENCUMBER_START_DATE = "error.document.preEncumbrance.invalidStartDate";
    public static final String ERROR_DOCUMENT_PREENCUMBER_NEEDS_COUNT_SPECIFIED = "error.document.preEncumbrance.countRequired";
    public static final String ERROR_DOCUMENT_PREENCUMBER_NO_AUTODISENCUMBER_TYPE = "error.document.preEncumbrance.noTypeSpecified";
    public static final String ERROR_DOCUMENT_PREENCUMBER_NEEDS_REFERENCE_NUMBER = "error.document.preEncumbrance.referenceNumberRequired";
    public static final String ERROR_DOCUMENT_PREENCUMBER_WRONG_COUNT = "error.document.preEncumbrance.count";
    public static final String ERROR_DV_CHECK_TOTAL_NO_CHANGE = "error.dv.checkTotalNoChange";
    
    // KFSPTS-1786
    public static final String ERROR_CURRBALANCE_LOOKUP_CRITERIA_REQD = "error.currentbalance.lookup.criteria.required";
    public static final String ERROR_DV_INITIATOR_INVALID_PRIMARY_DEPARTMENT = "error.dv.initiatorInvalidPrimaryDepartment";
    
    public static final String ERROR_FINAL_FINANCIAL_REPORT_DATE_REQUIRED = "error.cg.requiredFinancialReportDateNotSupplied";    
    
    // Account Reversion Errors
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_BUDGET_REVERSION_INCOMPLETE = 
            "error.document.globalAcctReversion.incompleteBudgetReversion";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_CASH_REVERSION_INCOMPLETE = "error.document.globalAcctReversion.incompleteCashReversion";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_INVALID_ACCT_REVERSION_CATEGORY = 
            "error.document.globalAcctReversion.invalidAcctReversionCategory";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_INVALID_ACCT_REVERSION_CODE = "error.document.globalAcctReversion.invalidAcctReversionCode";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_OBJECT_CODE_INVALID = "error.document.globalAcctReversion.objectCodeInvalid";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_NO_ACCOUNTS = "error.document.globalAcctReversion.noAccounts";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_INVALID_CHART = "error.document.globalAcctReversion.invalidChart";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_INVALID_ACCOUNT = "error.document.globalAcctReversion.invalidAccount";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_NO_ACCT_REVERSION = "error.document.globalAcctReversion.noAcctReversion";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_DUPLICATE_ACCOUNTS = "error.document.globalAcctReversion.duplicateAccts";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_NO_REVERSION_CODE = "error.document.globalAcctReversion.noReversionCode";
    
    public static final String ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_INACTIVATION = "note.trickleDownInactivation.inactivatedACCOUNTReversionDetail";
    public static final String ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE = 
            "note.trickleDownInactivation.inactivatedACCOUNTReversionDetail.errorDuringPersistence";
    public static final String ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_ACTIVATION = "note.trickleDownActivation.activatedACCOUNTReversionDetail";
    public static final String ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_ACTIVATION_ERROR_DURING_PERSISTENCE = 
            "note.trickleDownActivation.activatedACCOUNTReversionDetail.errorDuringPersistence";
    public static final String MESSAGE_REPORT_YEAR_END_ACCOUNT_REVERSION_LEDGER_TITLE_LINE = "message.report.yearEnd.accountReversion.ledger.titleLine";

    //KFSPTS-2400
    public static final String MESSAGE_BATCH_UPLOAD_TITLE_DISENCUMBRANCE_FILE = "message.batchUpload.title.labor.disencumbrance.file";
    public static final String MESSAGE_CREATE_DISENCUMBRANCE_ERROR = "message.ld.disencumbrance.error";
    public static final String MESSAGE_CREATE_DISENCUMBRANCE_SUCCESSFUL = "message.ld.disencumber.successful";

	    //KFSPTS-990 Award 
	    public static final String ERROR_DUPLICATE_AWARD_ACCOUNT = "error.duplicate.awardAccount";
	    public static final String ERROR_DUPLICATE_AWARD_PROJECT_DIRECTOR = "error.duplicate.awardProjectDirector";
	    public static final String ERROR_DUPLICATE_AWARD_ORGANIZATION = "error.duplicate.awardOrganization";
	    
	    // KFSUPGRADE-779
	    public static final String QUESTION_CLEAR_UNNEEDED_WIRW_TAB = "question.preq.clearUnneededTab";
	    public static final String QUESTION_CLEAR_UNNEEDED_CM_WIRW_TAB = "question.cm.clearUnneededTab";

	    public static final String ACCOUNT_REVERSION_TRICKLE_DOWN_INACTIVATION = "note.trickleDownInactivation.inactivatedAccountReversions";
	    public static final String ACCOUNT_REVERSION_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE = "note.trickleDownInactivation.inactivatedAccountReversions.errorDuringPersistence";
	    public static final String ACCOUNT_REVERSION_TRICKLE_DOWN_INACTIVATION_RECORD_ALREADY_MAINTENANCE_LOCKED = "note.trickleDownInactivation.inactivatedAccountReversions.recordAlreadyMaintenanceLocked";

	    // KFSPTS-3416
	    public static final String MESSAGE_DONE_FILE_ALREADY_EXISTS = "message.system.batch.doneFileAlreadyExists";
	    public static final String MESSAGE_DONE_FILE_SUCCESSFULLY_CREATED = "message.system.batch.doneFileSuccessfullyCreated";

}
