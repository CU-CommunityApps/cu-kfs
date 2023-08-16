/*
 * Copyright 2008 The Kuali Foundation.
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
package com.rsmart.kuali.kfs.cr;

/**
 * Check Reconciliation Constants
 * 
 * @author Derek Helbert
 * @version $Revision$
 */
public class CRConstants {

    public static final String REPORT_TEMPLATE_CLASSPATH = "com/rsmart/kuali/kfs/cr/report/";
    
    public static final String REPORT_MESSAGES_CLASSPATH = REPORT_TEMPLATE_CLASSPATH + "CRReport";
    
    public static final String REPORT_TEMPLATE_NAME = "CheckReconciliationReport";
    
    public static final String REPORT_FILE_NAME = "CheckReconciliationReport";
    
    public static final String CLEARED = "CLRD";
    
    public static final String CANCELLED = "CDIS";
    
    public static final String VOIDED = "VOID";
    
    public static final String ISSUED = "ISSD";
    
    public static final String STALE = "STAL";
    
    public static final String STOP = "STOP";
    
    public static final String EXCP = "EXCP";
    
    public static final String CHECK_FILE_TYPE = "CR_FILE_TYPE";
    
    public static final String CHECK_FILE_DELIMETER = "CR_FILE_DELIMETER";
    
    public static final String CHECK_FILE_COLUMNS = "CR_FILE_COL_LENGTHS";
    
    public static final String CHECK_FILE_HEADER = "CR_FILE_HEADER_IND";
    
    public static final String CHECK_FILE_HEADER_COLUMNS = "CR_FILE_HEADER_COL_LENGTHS";
    
    public static final String CHECK_FILE_FOOTER = "CR_FILE_FOOTER_IND";
    
    public static final String CHECK_FILE_FOOTER_COLUMNS = "CR_FILE_FOOTER_COL_LENGTHS";
    
    public static final String CU_CR_CHECK_RECON_T_CHECK_NBR_COL = "CHECK_NBR";
    public static final String CU_CR_CHECK_RECON_T_BNK_CD_COL = "BNK_CD";

    public static final String CHECK_NUM_COL = "CR_CHECK_NUM_COL";
    
    public static final String CHECK_DATE_COL = "CR_CHECK_DATE_COL";
    
    public static final String AMOUNT_COL = "CR_CHECK_AMT_COL";
    
    public static final String ACCOUNT_NUM_COL = "CR_ACCT_NUM_COL";
    
    public static final String ISSUE_DATE_COL = "CR_ISSUE_DATE_COL";
    
    public static final String PAYEE_ID_COL = "CR_PAYEE_ID_COL";
    
    public static final String PAYEE_NAME_COL = "CR_PAYEE_NAME_COL";
    
    public static final String AMOUNT_DECIMAL_IND = "CR_AMOUNT_DECIMAL_IND";
    
    public static final String ACCOUNT_NUM_HEADER_IND = "CR_ACCT_NUM_COL_HEADER_IND";
        
    public static final String STATUS_COL = "CR_STATUS_COL";
    
    public static final String CLRD_STATUS = "CR_STATUS_CLRD_CODES";
    
    public static final String VOID_STATUS = "CR_STATUS_VOID_CODES";
    
    public static final String CNCL_STATUS = "CR_STATUS_CNCL_CODES";
    
    public static final String ISSD_STATUS = "CR_STATUS_ISSD_CODES";
    
    public static final String STAL_STATUS = "CR_STATUS_STAL_CODES";
    
    public static final String STOP_STATUS = "CR_STATUS_STOP_CODES";
    
    public static final String CHECK_DATE_FORMAT = "CR_CHECK_DATE_FORMAT";
    
    public static final String DELIMITED = "DELIMITED";
    
    public static final String FIXED = "FIXED";
    
    public static final String CLEARING_ACCOUNT = "CR_CLEARING_ACCOUNT";
    
    public static final String CLEARING_COA = "CR_CLEARING_COA";
    
    public static final String CLEARING_OBJECT_CODE = "CR_CLEARING_OBJECT_CODE";
    
    public static final String PARAMETER_PREFIX = "CR_PARAMETER_PREFIX";
    
    public static final String CR_FDOC_ORIGIN_CODE = "CR";
    
    public static final String PDP_SRC = "P";
    public static final String ACCOUNT_NUM = "CR_ACCOUNT_NUMBER";
    public static final String SRC_NOT_FOUND = "CR_SOURCE_FOR_NOT_FOUND";
    public static final String BNK_CD_NOT_FOUND = "CR_NOT_FOUND_BANK_CD";

    public static final String STALE_CHECK_EXTRACT_FILE_TYPE_ID = "staleCheckExtractCsvInputFileType";
    
    public static final String LEGACY_DATE_FORMAT_yyMMdd = "yyMMdd";

}
