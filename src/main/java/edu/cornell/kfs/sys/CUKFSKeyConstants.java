package edu.cornell.kfs.sys;

public class CUKFSKeyConstants {

    public static final String MESSAGE_BATCH_UPLOAD_TITLE_COMMODITY_CODE = "message.batchUpload.title.commodityCode";
    public static final String MESSAGE_BATCH_UPLOAD_TITLE_INACTIVATE_CONVERT_CODE = "message.batchUpload.title.InactivateConvertCode";

    public static final String ERROR_DOCUMENT_ACCMAINT_PROGRAM_CODE_NOT_GROUP_CODE = "error.document.accountMaintenance.programCodeNotAssociatedWithGroupCode";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCT_PROGRAM_CODE_NOT_GROUP_CODE = "error.document.accountGlobalMaintenance.acct.programCodeNotAssociatedWithGroupCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_PROGRAM_CODE_CANNOT_BE_BLANK_FOR_GROUP_CODE = "error.document.accountMaintenance.programCodeCannotBeBlank";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCT_PROGRAM_CODE_CANNOT_BE_BLANK_FOR_GROUP_CODE = "error.document.accountGlobalMaintenance.acct.programCodeCannotBeBlank";
    public static final String ERROR_DOCUMENT_ACCMAINT_APPROP_ACCT_NOT_GROUP_CODE = "error.document.accountMaintenance.appropAcctNotAssociatedWithGroupCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_ACCT_APPROP_ACCT_NOT_GROUP_CODE = "error.document.accountMaintenance.acct.appropAcctNotAssociatedWithGroupCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_MJR_RPT_CAT_CODE_NOT_EXIST = "error.document.accountMaintenance.majorRptgCatCodeEnteredDoesNotExist";
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
    public static final String ERROR_DV_CHECK_TOTAL_MUST_EQUAL_ACCOUNTING_LINE_TOTAL = "error.dv.checkTotalMustEqualAccountingLineTotal";
    public static final String ERROR_DV_CHECK_STUB_REQUIRED ="error.dv.checkStubRequired";
    public static final String ERROR_RCDV_END_DATE_PASSED_MAX_END_DATE = "error.rcdv_end_date_passed_max_end_date";
    public static final String ERROR_RCDV_PAYMENT_CANCEL_REASON_REQUIRED = "error.rcdv_paymeent_cancel_reason_required";
    public static final String ERROR_RCDV_RECURRENCE_SUM_LESS_THAN_TANSACTION = "error.rcdv_recurrence_sum_less_than_transaction_amount";
    public static final String ERROR_RCDV_TOO_MANY_RECURRENCES="error.rcdv_too_many_recurrences";
    public static final String ERROR_RCDV_NO_FOREIGN_VENDORS = "error.rcdv_no_foreign_vendors";
    public static final String INFO_RCDV_NO_PAYMENTS_OR_DVS_CANCELED = "info.rcdv_no_payments_or_dvs_canceled";
    public static final String INFO_RCDV_LIST_OF_PAYMENT_GROUPS_CANCELED = "info.rcdv_list_of_payment_groups_canceled";
    public static final String INFO_RCDV_LIST_OF_DVS_CANCELED = "info.rcdv_list_of_dvs_canceled";
    
    // KFSPTS-1786
    public static final String ERROR_CURRBALANCE_LOOKUP_CRITERIA_REQD = "error.currentbalance.lookup.criteria.required";
    public static final String ERROR_DV_INITIATOR_INVALID_PRIMARY_DEPARTMENT = "error.dv.initiatorInvalidPrimaryDepartment";
    
    public static final String ERROR_FINAL_FINANCIAL_REPORT_DATE_REQUIRED = "error.cg.requiredFinancialReportDateNotSupplied";    
    public static final String ERROR_AUTO_APPROVE_REASON_REQUIRED = "error.cg.requiredAutoApporoveRason";
    
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
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_MISSING_FIELDS_FOR_NEW_REVERSION
            = "error.document.globalAcctReversion.missingFieldsForNewReversion";
    public static final String ERROR_DOCUMENT_GLOBAL_ACCT_REVERSION_MISSING_FIELDS_FOR_NEW_REVERSION_DETAIL
            = "error.document.globalAcctReversion.missingFieldsForNewReversionDetail";
    
    public static final String ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_INACTIVATION = "note.trickleDownInactivation.inactivatedACCOUNTReversionDetail";
    public static final String ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_INACTIVATION_ERROR_DURING_PERSISTENCE = 
            "note.trickleDownInactivation.inactivatedACCOUNTReversionDetail.errorDuringPersistence";
    public static final String ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_ACTIVATION = "note.trickleDownActivation.activatedACCOUNTReversionDetail";
    public static final String ACCOUNT_REVERSION_DETAIL_TRICKLE_DOWN_ACTIVATION_ERROR_DURING_PERSISTENCE = 
            "note.trickleDownActivation.activatedACCOUNTReversionDetail.errorDuringPersistence";
    public static final String MESSAGE_REPORT_YEAR_END_ACCOUNT_REVERSION_LEDGER_TITLE_LINE = "message.report.yearEnd.accountReversion.ledger.titleLine";

    // Error messages pertaining to particular attachment types.
    public static final String ERROR_DOCUMENT_ADD_TYPED_ATTACHMENT = "error.document.add.typed.attachment";
    public static final String ERROR_DOCUMENT_ADD_UNFLAGGED_CONFIDENTIAL_ATTACHMENT = "error.document.add.unflagged.confidential.attachment";

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


    // KFSPTS-3933
    public static final String ERROR_AWARD_ACCOUNT_ALREADY_IN_USE = "error.award.awardAccount.alreadyInUse";

    // KFSPTS-4337
    public static final String QUESTION_ACCOUNT_OFF_CAMPUS_INDICATOR = "question.coa.account.confirm.offCampusIndicator";
    public static final String QUESTION_A21SUBACCOUNT_OFF_CAMPUS_INDICATOR = "question.coa.a21subaccount.confirm.offCampusIndicator";

	// KFSPTS-4366
    public static final String QUESTION_CLEAR_UNNEEDED_WIRE_TAB = "question.dv.clearUnneededWireTab";

    // KFSPTS-4388
    public static final String ERROR_ZERO_OR_NEGATIVE_ACCOUNTING_TOTAL = "error.document.accountingtotal.zeroOrNegative";

    // KFSPTS-4438
    public static final String ERROR_DOCUMENT_DV_BLANK_STATE_AND_COUNTRY = "error.document.dv.blank.state.and.country";
  
    // KFSPTS-4566
    public static final String ERROR_DOCUMENT_ACCMAINT_ICR_ACCOUNT_CANNOT_BE_INACTIVE ="error.document.accountMaintenance.icrAccountCannotBeInactive";  

    //KFSPTS-4563
    public static final String ERROR_DOCUMENT_GLOBAL_SUS_ACCOUNT_NO_SUB_ACCOUNTS = "error.document.subAccountGlobalDetails.noSubAccountsEntered";
    public static final String ERROR_DOCUMENT_GLOBAL_SUB_ACCOUNT_INVALID_ACCOUNT = "error.document.subAccountGlobalDetails.invalidAccount";
    public static final String ERROR_DOCUMENT_GLOBAL_SUB_ACCOUNT_INVALID_SUB_ACCOUNT = "error.document.subAccountGlobalDetails.invalidSubAccount";

    // KFSPTS-3956
    public static final String ERROR_DOCUMENT_GLOBAL_SUBPOBJ_CD_INACTIVATION_NO_SUB_OBJ_CDS = "error.document.subObjCdGlobalEditDetails.noSubObjectCodesEntered";

    // KFSPTS-3959
    public static final String ERROR_DOCUMENT_GLOBAL_ORGANIZATION_COUNTRY_AND_ZIP_MISMATCH = "error.document.organizationGlobal.countryAndZipMismatch";
    public static final String ERROR_DOCUMENT_GLOBAL_ORGANIZATION_NO_ORGANIZATIONS = "error.document.organizationGlobalDetails.noOrganizationsEntered";
    public static final String ERROR_DOCUMENT_GLOBAL_ORGANIZATION_INVALID_ORGANIZATION = "error.document.organizationGlobalDetails.invalidOrganization";

    // KFSPTS-4905
    public static final String ERROR_CSACCOUNT_CONTINUATION_ACCOUNT_CLOSED = "error.csAccount.continuationAccount.closed";
    public static final String WARNING_CSACCOUNT_CONTINUATION_ACCOUNT_USED = "warning.csAccount.continuationAccount.used";

    // KFSPTS-4792
    public static final String ERROR_DOCUMENT_GLB_MAINT_ICR_ACCOUNT_TOTAL_NOT_100_PERCENT = "error.document.global.indirectCostRecoveryAccounts.totalNot100Percent";

    // KFSPTS-3599
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_INVALID_CG_RESPONSIBILITY = "error.document.accountGlobalMaintenance.invalidContractsAndGrantsResponsibility";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_EXP_DATE_CANNOT_BE_BEFORE_EFFECTIVE_DATE  = "error.document.accountGlobal.acct.expDateCannotBeBeforeEffectiveDate";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCOUNT_CLOSED_PENDING_LEDGER_ENTRIES = "error.document.accountGlobal.acct.closedAccount.noPendingLedgerEntriesAllowed"; 	   
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCOUNT_CLOSED_NO_LOADED_BEGINNING_BALANCE = "error.document.accountGlobal.acct.closedAccount.beginningBalanceNotLoaded";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCOUNT_CLOSED_NO_FUND_BALANCES = "error.document.accountGlobal.acct.closedAccount.noFundBalances";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCOUNT_CLOSED_PENDING_LABOR_LEDGER_ENTRIES = "error.document.accountGlobal.acct.closedAccount.noPendingLaborLedgerEntriesAllowed";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ICR_ACCOUNT_TOTAL_NOT_100_PERCENT = "error.document.accountGlobal.acct.indirectCostRecoveryAccounts.totalNot100Percent";

    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_CG_ICR_FIELDS_FILLED_FOR_NON_CG_ACCOUNT = "error.document.accountGlobal.acct.cgICRFieldsFilledInForNonCGAccount";

    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_CG_FIELDS_FILLED_FOR_NON_CG_ACCOUNT = "error.document.accountGlobalMaintenance.cgFieldsFilledInForNonCGAccount";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ACCOUNT_CG_FIELDS_FILLED_FOR_NON_CG_ACCOUNT = "error.document.accountGlobalMaintenance.acct.cgFieldsFilledInForNonCGAccount";

    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_EXP_DATE_NOT_EMPTY_AND_REMOVE_EXP_DATE_CHECKED = "error.document.accountGlobalMaintenance.expDateNotEmptyAndRemoveExpDateChecked";

    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_CNT_CHART_NOT_EMPTY_AND_REMOVE_CNT_CHART_AND_ACCT_CHECKED = "error.document.accountGlobalMaintenance.contChartNotEmptyAndRemoveContChartAndAcctChecked";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_CNT_ACCT_NOT_EMPTY_AND_REMOVE_CNT_CHART_AND_ACCT_CHECKED = "error.document.accountGlobalMaintenance.contAcctNotEmptyAndRemoveContChartAndAcctChecked";

    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_INC_STR_CHART_NOT_EMPTY_AND_REMOVE_INC_STR_CHART_AND_ACCT_CHECKED = "error.document.accountGlobalMaintenance.incStrChartNotEmptyAndRemoveIncStrChartAndAcctChecked";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_INC_STR_ACCT_NOT_EMPTY_AND_REMOVE_INC_STR_CHART_AND_ACCT_CHECKED = "error.document.accountGlobalMaintenance.incStrAcctNotEmptyAndRemoveIncStrChartAndAcctChecked";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ICR_ACCT_EXISTS = "error.document.accountGlobalMaintenance.icrAccountExists";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ICR_ACCT_DUPLICATE = "error.document.accountGlobalMaintenance.icrAccountDuplicate";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ICR_EMPTY_FOR_CG_ACCOUNT = "error.document.accountGlobal.acct.cgICREmptyForCGAccount";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_ICR_NOT_EMPTY_FOR_NON_CG_ACCOUNT = "error.document.accountGlobal.acct.cgICRNotEmptyForNonCGAccount";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_EXP_DT_AND_REMOVE_CONT_ACCT = "error.document.accountGlobal.acct.expirationDate.removeContAcct";

    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_REMOVE_INC_STR_CHART_AND_ACCT_CHECKED_WHEN_INC_STR_REQ = "error.document.accountGlobalMaintenance.removeIncStrChartAndAcctChecked.incStrReq";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_REMOVE_INC_STR_CHART_AND_ACCT_CHECKED_WHEN_INC_STR_REQ_FOR_ACCT = "error.document.accountGlobalMaintenance.removeIncStrChartAndAcctChecked.incStrReqForAcct";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_CLOSED_CHECKED_WHEN_ACCOUNT_HAS_OPEN_ENCUMBRENCES = "error.document.accountGlobalMaintenance.accountCannotCloseOpenEncumbrance";
    public static final String ERROR_DOCUMENT_ACCT_GLB_MAINT_CANNOT_BE_CLOSED_EXP_DATE_NOT_ENTERED_EXISTING_DATE_INVALID = "error.document.accountGlobalMaintenance.accountCannotBeClosedExpDateNotEnteredExistingDateInvalid";
    
    public static final String OBJECT_CODE_ACTIVATION_GLOBAL_OBJECT_CODE_LIST_ERROR = "object.code.activation.global.object.code.list.empty.error";
    public static final String OBJECT_CODE_ACTIVATION_GLOBAL_OBJECT_CODE_NEW_CODE_ERROR = "object.code.activation.global.object.code.list.new.code.error";
    
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_DETAILS_INVALID_RESTRICTION_CODE_CHANGE = "error.document.subAccountGlobalDetails.invalidRestrictionCodeChange";

    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_INVALID_PROPERTY_FOR_NEW_SUB_ACCOUNT = "error.document.subAccountGlobal.invalidPropertyForNewSubAccount";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_CANNOT_SPECIFY_CHANGES_AND_ADDITIONS = "error.document.subAccountGlobal.cannotSpecifyChangesAndAdditions";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_REQUIRED_FIELD_FOR_NEW_SUB_ACCOUNT = "error.document.subAccountGlobal.requiredFieldForNewSubAccount";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_ACCOUNT_FOR_NEW_SUB_ACCOUNT_NOT_FOUND = "error.document.subAccountGlobal.accountForNewSubAccountNotFound";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_ACCOUNT_FOR_NEW_SUB_ACCOUNT_CLOSED = "error.document.subAccountGlobal.accountForNewSubAccountClosed";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NON_BLANK_GLOBAL_FIELD = "error.document.subAccountGlobal.nonBlankGlobalField";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_REQUIRED_GLOBAL_FIELD = "error.document.subAccountGlobal.requiredGlobalField";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NON_BLANK_LINE_FIELD = "error.document.subAccountGlobal.nonBlankLineField";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_REQUIRED_LINE_FIELD = "error.document.subAccountGlobal.requiredLineField";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NEW_SUB_ACCOUNT_DUPLICATE = "error.document.subAccountGlobal.newSubAccountDuplicate";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_NEW_SUB_ACCOUNT_EXISTS = "error.document.subAccountGlobal.newSubAccountExists";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_CG_AND_NON_CG_MIX = "error.document.subAccountGlobal.cgAndNonCgMix";
    public static final String ERROR_DOCUMENT_SUB_ACCOUNT_GLOBAL_CG_EXPENSE_REQUIRED_SECTION = "error.document.subAccountGlobal.cgExpenseRequiredSection";
    
    public static final String ERROR_DOCUMENT_CONTRACT_GRANT_INVOICE_PRORATE_NO_AWARD_BUDGET_TOTAL = "error.document.contractsGrantsInvoice.prorate.no.award.budget.total";
    public static final String WARNING_CINV_FINAL_BILL_INDICATOR = "warning.document.contractGrantsInvoice.final.bill.indicator";
    public static final String WARNING_CINV_BILLING_PERIOD_END_DATE_BEFORE_LAST_BILLED_DATE = "warning.document.contractGrantsInvoice.billingPeriodEndDate.before.lastBilledDate";
    public static final String WARNING_CINV_BILLING_PERIOD_END_DATE_BEFORE_BILLING_PERIOD_START_DATE = "warning.document.contractGrantsInvoice.billingPeriodEndDate.before.billingPeriodStartDate";
    public static final String WARNING_CINV_BILLING_PERIOD_START_DATE_BEFORE_AWARD_START_DATE = "warning.document.contractGrantsInvoice.billingPeriodStartDate.before.awardStartDate";
    public static final String WARNING_CINV_DATE_RANGE_INVALID_FORMAT_LENGTH = "warning.document.contractGrantsInvoice.dateRange.invalidFormatLength";
    public static final String WARNING_CINV_DATE_RANGE_PARSE_EXCEPTION = "warning.document.contractGrantsInvoice.dateRange.parseException";
    public static final String ERROR_CINV_SETTING_LAST_BILLED_DATE = "error.document.contractGrantsInvoice.settingLastBilledDate.exception";
    public static final String WARNING_CINV_BILLING_PERIOD_END_DATE_AFTER_LAST_BILLED_DATE = "warning.document.contractGrantsInvoice.billingPeriodEndDate.after.lastBilledDate";
    public static final String WARNING_CINV_BILLING_PERIOD_START_DATE_AFTER_LAST_BILLED_DATE = "warning.document.contractGrantsInvoice.billingPeriodStartDate.after.lastBilledDate";
    public static final String WARNING_CINV_BILLING_PERIOD_END_DATE_AFTER_TODAY = "warning.document.contractGrantsInvoice.billingPeriodEndDate.after.today";

    public static final String ERROR_BANK_REQUIRED_PER_PAYMENT_METHOD = "error.document.withBanking.bank.required";
    public static final String ERROR_BANK_INVALID = "error.document.withBanking.bank.invalid";

    // NOTE: The following set of constants can be removed if KualiCo adds variants that are more publicly visible.
    public static final String ERROR_UPLOADFILE_EMPTY = "error.uploadFile.empty";
    public static final String ERROR_DOCUMENT_INVALID_VALUE_ALLOWED_VALUES_PARAMETER =
            "error.document.invalid.value.allowedValuesParameter";
}
