package edu.cornell.kfs.module.bc;

import org.kuali.kfs.module.bc.BCConstants;

public class CUBCConstants extends BCConstants {
    public static final String DEFAULT_FINANCIAL_SUB_OBJECT_CODE = "---";
    public static final String DEFAULT_SUB_ACCOUNT_NUMBER = "-----";
    public static final String POSITION_NUMBER_PREFIX = "00";

    public static final String SIP_IMPORT_LOG_FILE = "sip_import_log.txt";
    
    // parameter constants
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_CODE = "KFS-BC";
    public static final String BUDGET_CONSTRUCTION_DOCUMENT_TYPE = "BCLO";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_NAMESPACE_STEP = "PSBudgetFeedStep";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_VALUE = "N";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_ALLOWED = "A";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_DESCRIPTION = "Tells the psBudgetFeedJob if it should wipe out the entries in the BC and SIP related tables";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_TYPE = "CONFG";
    public static final String RUN_FOR_NEW_YEAR_PARAMETER_APPLICATION_NAMESPACE_CODE = "KFS";

    public enum PSEntryStatus {
        ADD, DELETE, UPDATE
    }

    public enum StatusFlag {
        NEW("N"), CHANGED("C"), DELETED("D"), ACTIVE("-");

        private StatusFlag(String flagValue) {
            this.flagValue = flagValue;
        }

        public String getFlagValue() {
            return flagValue;
        }

        private String flagValue;
    }

    public static final String SOME_CSF_CHANGE_FLAG = "*";

    public static final class SIPEligibility {
        public static final String YES = "Y";
        public static final String NO = "N";

    }

    public static final class EmployeeType {
        public static final String FACULTY = "F";
        public static final String NON_FACULTY_ACADEMICS = "A";
        public static final String EXEMPT_STAFF = "E";
        public static final String NON_EXEMPT_STAFF = "N";
        public static final String DEFAULT = "W";
        public static final String EXECUTIVES = "Z";
        public static final String UNION = "U";
    }

    public static final class JobFunction {

        public static final String ACF = "ACF";

        public static final String ACO = "ACO";
        public static final String ACL = "ACL";
        public static final String ACR = "ACR";
        public static final String ACS = "ACS";

    }

    public static final class JobCode {
        public static final String _10841 = "10841";
        public static final String _10835 = "10835";
    }

    public static final class PositionType {
        public static final String EXEMPT = "S";
        public static final String NON_EXEMPT_E = "E";
        public static final String NON_EXEMPT_H = "H";
    }

    public static final String[] UNION_CODES_SIP = new String[]{"BTC", "CPU", "CWA", "IOE", "LOC", "PRO", "SPA", "UAW"};

    public static final String BUDGET_ONLY_OBJECT_CODE_SUB_TYPE = "BU";


    public static final String MESSAGE_BUDGET_CONST_REPORT_HEADER = "message.batch.bc.runStatsReportHeader";
    

    public static final String BALANCE_TYPE_TRUSTEES_BUDGET = "TB";

    // the transaction ledger description for the general ledger budget load
    public final static String TB_TRN_LDGR_ENTR_DESC = "Trustee Budget Load";

    public static final class CompFreq {
        public static final String HOURLY = "H";
        public static final String ANNUALLY = "A";
    }

    public static final class SipImportModeValues {
        public static final String REPORT = "REPORT";
        public static final String UPDATE = "UPDATE";
    }
    
    public static final String SIP_PLUG_OBJECT_CODE = "6995";
    
    public static final String SIP_FINANCIAL_OBJECT_LEVEL_CODE = "SIP";
    
    public static final String DEFAULT_EMPL_RECORD = "000";
}
