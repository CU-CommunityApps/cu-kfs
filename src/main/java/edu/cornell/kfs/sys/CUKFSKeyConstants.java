package edu.cornell.kfs.sys;

import org.kuali.kfs.sys.KFSKeyConstants;

public class CUKFSKeyConstants extends KFSKeyConstants{
	
    public static final String ERROR_DOCUMENT_ACCMAINT_PROGRAM_CODE_NOT_GROUP_CODE = "error.document.accountMaintenance.programCodeNotAssociatedWithGroupCode";
    public static final String ERROR_DOCUMENT_ACCMAINT_PROGRAM_CODE_CANNOT_BE_BLANK_FOR_GROUP_CODE ="error.document.accountMaintenance.programCodeCannotBeBlank";
	public static final String ERROR_DOCUMENT_ACCMAINT_APPROP_ACCT_NOT_GROUP_CODE = "error.document.accountMaintenance.appropAcctNotAssociatedWithGroupCode";	
	public static final String ERROR_DOCUMENT_ACCMAINT_MJR_RPT_CAT_CODE_NOT_EXIST = "error.document.accountMaintenance.majorRptgCatCodeEnteredDoesNotExist";				
	public static final String ERROR_DOCUMENT_OBJCDMAINT_CG_RPT_CAT_CODE_NOT_EXIST = "error.document.objectCodeMaintenance.cgReptgCodeEnteredDoesNotExist";	
	public static final String ERROR_DOCUMENT_VNDMAINT_W9RECEIVED_NOT_POPULATED = "error.document.vendor.w9ReceivedNotPopulatedButIndicatorIsTrue";
	public static final String ERROR_DOCUMENT_VNDMAINT_W9RECEIVED_POPULATED_W_O_IND = "error.document.vendor.w9ReceivedPopulatedButIndicatorIsNull";
	public static final String ERROR_NO_UNIT_COST_WITH_ACCOUNTS = "error.no.unit.cost.with.accounts";
	public static final String ERROR_UNIT_COST_W_O_ACCOUNT = "error.unit.cost.with.out.accounts";
	public static final String ERROR_NO_ZERO_PERCENT_ACCOUNT_LINES_ALLOWED = "error.no.zero.percent.account.lines.allowed";
		
	public static final String ERROR_DOCUMENT_PREENCUMBER_CONFLICTING_START_END = "error.document.preEncumbrance.startAfterEnd";	
	public static final String ERROR_DOCUMENT_PREENCUMBER_INVALID_END = "error.document.preEncumbrance.endBeforeToday";
	public static final String ERROR_DOCUMENT_PREENCUMBER_INVALID_START = "error.document.preEncumbrance.startBeforeToday";
	public static final String ERROR_DOCUMENT_PREENCUMBER_DATE_PAST_YEAR_END = "error.document.preEncumbrance.dateAfterYearEnd";
	public static final String ERROR_DOCUMENT_PREENCUMBER_NEEDS_START_DATE = "error.document.preEncumbrance.startDateRequired";
	public static final String ERROR_DOCUMENT_PREENCUMBER_NEEDS_END_OR_COUNT = "error.document.preEncumbrance.endDateOrCountRequired";
	public static final String ERROR_DOCUMENT_PREENCUMBER_NEEDS_AMOUNT_SPECIFIED = "error.document.preEncumbrance.amountRequired";
	public static final String ERROR_DOCUMENT_PREENCUMBER_BOTH_REV_DATES_USED = "error.document.preEncumbrance.bothRevDatesUsed";
	public static final String ERROR_DOCUMENT_PREENCUMBER_GENERATED_ENTRIES_SPAN_FY = "error.document.preEncumbrance.generatedEntriesSpanFiscalYears";

}