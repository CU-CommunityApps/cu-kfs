package edu.cornell.kfs.gl;

public class CuGeneralLedgerConstants {

	public static final String ANNUAL_CLOSING_CHARTS_PARAM = "ANNUAL_CLOSING_CHARTS";

	public static class ReversionProcess extends OrganizationReversionProcess {
		public static final String CASH_REVERSION_OBJECT_CODE_PARM = "CASH_REVERSION_OBJECT_CODE";
	}

	public static class CuBatchFileSystem {
		public static final String ACCOUNT_REVERSION_CLOSING_FILE = "gl_acct_reversion_closing";
		public static final String ACCOUNT_REVERSION_PRE_CLOSING_FILE = "gl_acct_reversion_pre_closing";
	}

	public static class OrganizationReversionProcess {
		public static final String ORGANIZATION_REVERSION_COA = "ORGANIZATION_REVERSION_COA";
		public static final String CARRY_FORWARD_OBJECT_CODE = "CARRY_FORWARD_OBJECT_CODE";
		public static final String MANUAL_FEED_ORIGINATION = "MANUAL_FEED_ORIGINATION";
		public static final String CASH_BALANCE_TYPE = "CASH_BALANCE_TYPE";
		public static final String BUDGET_BALANCE_TYPE = "BUDGET_BALANCE_TYPE";
		public static final String DOCUMENT_NUMBER_PREFIX = "DOCUMENT_NUMBER_PREFIX";
		public static final String UNALLOC_OBJECT_CODE_PARM = "UNALLOCATED_OBJECT_CODE";
		public static final String CASH_REVERSION_OBJECT_CODE_PARM = "CASH_REVERSION_OBJECT_CODE";
	}

    public static class CuGlScrubberGroupRules {
        public static final String DOCUMENT_TYPES = "DOCUMENT_TYPES";
    }
    
    public static class BatchFileSystem {
        public static final String TEXT_EXTENSION = ".txt";
        
    }

}
