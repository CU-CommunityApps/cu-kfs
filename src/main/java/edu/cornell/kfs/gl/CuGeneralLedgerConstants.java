package edu.cornell.kfs.gl;

import org.kuali.kfs.gl.GeneralLedgerConstants;

public class CuGeneralLedgerConstants extends GeneralLedgerConstants {

	public static final String ANNUAL_CLOSING_CHARTS_PARAM = "ANNUAL_CLOSING_CHARTS";
	public static final String COLLECTOR_HEADER_RECORD_TYPE = "HD";
	public static final String COLLECTOR_TRAILER_RECORD_TYPE = "TL";
	public static final String COLLECTOR_FILE_DATE_FORMAT = "yyyy-MM-dd";

	public static class ReversionProcess extends OrganizationReversionProcess {
		static final public String CASH_REVERSION_OBJECT_CODE_PARM = "CASH_REVERSION_OBJECT_CODE";
	}

	public static class CuBatchFileSystem extends BatchFileSystem {
		static final public String ACCOUNT_REVERSION_CLOSING_FILE = "gl_acct_reversion_closing";
		static final public String ACCOUNT_REVERSION_PRE_CLOSING_FILE = "gl_acct_reversion_pre_closing";
	}

	public static class OrganizationReversionProcess {
		public static final String ORGANIZATION_REVERSION_COA = "ORGANIZATION_REVERSION_COA";
		public static final String CARRY_FORWARD_OBJECT_CODE = "CARRY_FORWARD_OBJECT_CODE";
		public static final String DEFAULT_FINANCIAL_SYSTEM_ORIGINATION_CODE = "MANUAL_FEED_ORIGINATION";
		public static final String DEFAULT_FINANCIAL_BALANCE_TYPE_CODE = "CASH_REVERSION_DEFAULT_BALANCE_TYPE";
		public static final String DEFAULT_FINANCIAL_BALANCE_TYPE_CODE_YEAR_END = "BUDGET_REVERSION_DEFAULT_BALANCE_TYPE";
		public static final String DEFAULT_DOCUMENT_NUMBER_PREFIX = "DEFAULT_DOCUMENT_NUMBER_PREFIX";
		public static final String UNALLOC_OBJECT_CODE_PARM = "UNALLOCATED_OBJECT_CODE";
		public static final String CASH_REVERSION_OBJECT_CODE_PARM = "CASH_REVERSION_OBJECT_CODE";
	}

    public static class CuGlScrubberGroupRules {
        public static final String PLANT_INDEBTEDNESS_DOC_TYPE_CODES = "PLANT_INDEBTEDNESS_DOCUMENT_TYPES";
    }

}
