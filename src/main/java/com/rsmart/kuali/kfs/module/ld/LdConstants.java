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
package com.rsmart.kuali.kfs.module.ld;

/**
 * This class contains the constants being used by various labor distribution components
 */
public class LdConstants {
    public static final String MESSAGE_FILE_UPLOAD_TITLE_ADP_NON_DIST = "message.file.upload.title.non.dist";
    public static final String MESSAGE_FILE_UPLOAD_TITLE_ADP_CSF_TRACKER = "message.file.upload.title.csf.tracker";
    public static final String ADP_NON_DIST_FILE_INPUT_TYPE_INDENTIFIER = "adpNonLaborDistBatchFileInputType";
    public static final String ADP_CSF_TRACKER_FILE_INPUT_TYPE_INDENTIFIER = "adpCSFTrackerBatchFileInputType";
    
    // Constants for ADP Position File Upload
    public static final String DAT_FILE_TYPE = "DAT";
    public static final String ADP_POSITION_FILE_INPUT_TYPE_INDENTIFIER = "adpPositionFileInputType";
    public static final String MESSAGE_BATCH_UPLOAD_TITLE_ADP_POSITION = "message.batchUpload.title.adp.position";
    
    // Constants for ADP Actual Payroll File Upload
    public static final String ADP_ACTUAL_PAYROLL_FILE_INPUT_TYPE_INDENTIFIER = "adpActualPayrollFileInputType";
    public static final String MESSAGE_BATCH_UPLOAD_TITLE_ADP_ACTUAL_PAYROLL = "message.batchUpload.title.adp.actualPayroll";

    // Constants for ADP Payroll Encumbrance File Upload
    public static final String ADP_PAYROLL_ENCUMBRANCE_FILE_INPUT_TYPE_INDENTIFIER = "adpPayrollEncumbranceFileInputType";
    public static final String MESSAGE_BATCH_UPLOAD_TITLE_ADP_PAYROLL_ENCUMBRANCE = "message.batchUpload.title.adp.payrollEncumbrance";
    
    //CSF Tracker
    public static final String FINANCIAL_CHART_OF_ACCOUNTS_CODE = "FIN_COA_CD";
    public static final String ACCOUNT_NUMBER = "ACCOUNT_NBR";
    public static final String SUB_ACCOUNT_NUMBER = "SUB_ACCT_NBR";
    public static final String FINANCIAL_OBJECT_CODE = "FIN_OBJECT_CD";
    public static final String FINANCIAL_SUB_OBJECT_CODE = "FIN_SUB_OBJ_CD";
    public static final String POS_CSF_CREATE_TIME = "POS_CSF_CREATE_TM";
    
    // Constants for the Salary Benefit Offset
    public static final String LABOR_BENEFIT_CALCULATION_OFFSET = "LABOR_BENEFIT_CALCULATION_OFFSET";
    public static final String LABOR_BENEFIT_OFFSET_DOCTYPE = "LABOR_BENEFIT_OFFSET_DOCTYPE";
    public static final String ACCOUNT_CODE_OFFSET_PROPERTY_NAME = "extension.accountCodeOffset";
    public static final String OBJECT_CODE_OFFSET_PROPERTY_NAME = "extension.objectCodeOffset";
    public static final String LABOR_BENEFIT_OFFSET_ORIGIN_CODE = "LABOR_BENEFIT_OFFSET_ORIGIN_CODE";
    
    
    // Constants for the Labor Ledger Inquiry Restriction 
    public static final String LD_MODIFIED_INQUIRY_ACTION = "ldModifiedInquiry.do";
    public static final String LD_BALANCE_INQUIRY_ACTION = "ldBalanceInquiry.do";
}
