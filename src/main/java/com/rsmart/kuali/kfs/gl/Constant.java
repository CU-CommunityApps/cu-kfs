/*
 * Copyright 2011 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rsmart.kuali.kfs.gl;

import org.kuali.kfs.sys.KFSPropertyConstants;

/**
 * This class contains the constants being used by various general ledger components
 */
public class Constant {

    public static final String INCOME_STATEMENT_ONLY = "Income Statement Only";
    public static final String BALANCE_SHEET_ONLY = "Balance Sheet Only";
    public static final String BOTH = "Both";
    
    // Field names
    public static final String FRS_ACCOUNT_NUMBER = "frsAcctNbr";
    public static final String FRS_OBJECT_CODE = "frsObjCd";

    // Column names
    public static final String CHART_OF_ACCOUNTS_COLUMN = "FIN_COA_CD";
    public static final String FRS_ACCOUNT_NUMBER_COLUMN = "FRS_ACCT_NBR";
    public static final String FRS_OBJECT_CODE_COLUMN = "FRS_OBJECT_CD";
    public static final String ORG_CD_COLUMN = "ORG_CD";
    public static final String UNIV_FISCAL_YR = "UNIV_FISCAL_YR";
    public static final String ACCT_NUMBER = "ACCOUNT_NBR";
    public static final String SUB_ACCT_NUMBER = "SUB_ACCT_NBR";
    public static final String FIN_OBJECT_CD = "FIN_OBJECT_CD";
    public static final String FIN_SUB_OBJ_CD = "FIN_SUB_OBJ_CD";
    public static final String FIN_BALANCE_TYP_CD = "FIN_BALANCE_TYP_CD";
    public static final String DOC_TYP = "DOC_TYP_NM";
    public static final String ORIGIN_CD = "FS_ORIGIN_CD";
    public static final String DOC_TYP_NM = "DOC_TYP_NM";
    public static final String DOC_TYP_ID = "DOC_TYP_ID";
    public static final String DOC_HDR_STAT_CD = "DOC_HDR_STAT_CD";
    public static final String BRS_GS_CD_COLUMN = "BRS_GS_CD";
    
    // System Parameter names
    public static final String DEFAULT_ACCOUNT_DEBIT_CODE = "BRStoKFS_DEFAULT_ACCOUNT_DEBIT_CODE";
    public static final String DEFAULT_ACCOUNT_CREDIT_CODE = "BRStoKFS_DEFAULT_ACCOUNT_CREDIT_CODE";
    public static final String DEFAULT_OBJECT_CODE = "BRStoKFS_DEFAULT_OBJECT_CODE";
    
    // Error message paths
    public static final String ERROR_FRS_ACCOUNT_INVALID = "error.frskfs.frsAccountNumberAlreadyExistsforChartCode";
    public static final String ERROR_FRS_OBJECT_CODE_INVALID = "error.frskfs.frsObjectCodeAlreadyExistsforChartCode";
    
    // Constants for BRS Accounting Interface File Upload
    public static final String DAT_FILE_TYPE = "DAT";
    public static final String BRS_ACCOUNT_INTERFACE_FILE_INPUT_TYPE_INDENTIFIER = "brsAccountingInterfaceFileInputType";
    public static final String MESSAGE_BATCH_UPLOAD_TITLE_BRS_ACCOUNT_INTERFACE = "message.batchUpload.title.brs.accounting.interface";
    
    //Start NSF Science Code Constants
    
    // Error message paths
    public static final String ERROR_NSF_CODE_INVALID = "error.nsfsc.codeAlreadyExists";
    public static final String ERROR_NSF_CODE_NOT_ALLOWED_FOR_HEFC = "error.nsf.codeNotAllowedforHefc";
    
    //System Parameter name
    public static final String NSF_VALID_HEFC_VALUES = "NSF_VALID_HEFC_VALUES";
    
    //End NSF Science Code Constants
    
    //Start FRS Account Extension Code Constants
    
    // Error message paths
    public static final String ERROR_FRS_EXTENSION_FORMAT = "error.frs.extension.format";
    public static final String LOG_FRS_PARAMETER_FORMAT = "DISPLAY_FRS_ACCT_ON_ACCT_LOOKUP_SCREEN encountered an incorrect value.  Assuming No and removing FRS Account Number field.";
    public static final String LOG_FRS_PARAMETER_MISSING = "DISPLAY_FRS_ACCT_ON_ACCT_LOOKUP_SCREEN is missing.  Assuming No and removing FRS Account Number field.";
    public static final String LOG_FRS_PARAMETER_REMOVE = "DISPLAY_FRS_ACCT_ON_ACCT_LOOKUP_SCREEN is set to N,  removing FRS Account Number field.";
    
    //System Parameter name
    public static final String DISPLAY_FRS_ACCT_ON_ACCT_LOOKUP_SCREEN = "DISPLAY_FRS_ACCT_ON_ACCT_LOOKUP_SCREEN";
    //End FRS Account Extension Code Constants
    
    //Cash Balance Errors
    public static final String ERROR_GL_LOOKUP_ACCOUNTNUMBER_ORGANIZATIONCODE = "error.gl.lookup.accountNumber.organizationCode";
    public static final String ERROR_GL_LOOKUP_PARAMETER_NONEXISTENT = "error.gl.lookup.parameter.nonExistent";

    public static class GeneralLedgerLabels {
        public static final String FISCAL_YEAR = "Fiscal Year";
        public static final String CHART_CODE = "Chart Code";
        public static final String ACCOUNT_NUMBER = "Account Number";
        public static final String ORGANIZATION_CODE = "Organization Code";
        public static final String SUB_ACCOUNT_NUMBER = "Sub-Account Number";
        public static final String OBJECT_CODE = "Object Code";
        public static final String SUB_OBJECT_CODE = "Sub-Object Code";
        public static final String BALANCE_TYPE_CODE = "Balance Type Code";
        public static final String OBJECT_TYPE_CODE = "Object Type Code";
        public static final String INCOME_TRANSFER_OBJECT_TYPE_CODES = "INCOME_TRANSFER_OBJECT_TYPE_CODES";
        public static final String EXPENSE_OBJECT_TYPE_CODES = "EXPENSE_OBJECT_TYPE_CODES";
        public static final String EXPENSE_TRANSFER_OBJECT_TYPE_CODES = "EXPENSE_TRANSFER_OBJECT_TYPE_CODES";
    }
    
    public static final String ACCOUNT_ORG_CODE = KFSPropertyConstants.ACCOUNT + ".organizationCode";
    
    public static final String LD_GL_OFFSET_ENABLED = "LD_GL_OFFSET_ENABLED";
    public static final String LD_GL_OFFSET_CLEARING_ACCOUNT_OBJECT_CODE = "LD_GL_OFFSET_CLEARING_ACCOUNT_OBJECT_CODE";
    public static final String LD_GL_OFFSET_CLAIM_ON_CASH_ACCOUNT_OBJECT_CODE = "LD_GL_OFFSET_CLAIM_ON_CASH_ACCOUNT_OBJECT_CODE";
    
}
