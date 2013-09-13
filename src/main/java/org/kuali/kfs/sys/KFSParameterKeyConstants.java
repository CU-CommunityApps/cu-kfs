/*
 * Copyright 2008 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.sys;

/**
 * Parameter name constants for system parameters used by the kfs sys.
 */
public class KFSParameterKeyConstants implements ParameterKeyConstants {
    public static final String ENABLE_BANK_SPECIFICATION_IND = "ENABLE_BANK_SPECIFICATION_IND";
    public static final String DEFAULT_BANK_BY_DOCUMENT_TYPE = "DEFAULT_BANK_BY_DOCUMENT_TYPE";
    public static final String BANK_CODE_DOCUMENT_TYPES = "BANK_CODE_DOCUMENT_TYPES";

    public static class YearEndAutoDisapprovalConstants {
        public static final String YEAR_END_AUTO_DISAPPROVE_ANNOTATION = "YEAR_END_AUTO_DISAPPROVE_ANNOTATION";
        public static final String YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE = "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE";
        public static final String YEAR_END_AUTO_DISAPPROVE_DOCUMENT_STEP_RUN_DATE = "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_RUN_DATE";
        public static final String YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES = "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES";
        public static final String YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE = "YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE";
		public static final String YEAR_END_AUTO_DISAPPROVE_START_DATE = "YEAR_END_AUTO_DISAPPROVE_START_DATE";
    }
    
    public static class FpParameterConstants {
    	public static final String FP_ALLOWED_BUDGET_BALANCE_TYPES = "ALLOWED_BUDGET_BALANCE_TYPES";
    	public static final String FP_ALLOW_MULTIPLE_SUBFUNDS = "ALLOW_MULTIPLE_SUB_FUNDS";
    }
    
    public static class PurapPdpParameterConstants {
        public static final String PURAP_PDP_ORG_CODE = "PRE_DISBURSEMENT_EXTRACT_ORGANIZATION";
        public static final String PURAP_PDP_SUB_UNIT_CODE = "PRE_DISBURSEMENT_EXTRACT_SUB_UNIT";
    }
 
    public static class GlParameterConstants {
        public static final String PLANT_INDEBTEDNESS_OFFSET_CODE = "PLANT_INDEBTEDNESS_OFFSET_CODE";
        public static final String CAPITALIZATION_OFFSET_CODE = "CAPITALIZATION_OFFSET_CODE";
        public static final String LIABILITY_OFFSET_CODE = "LIABILITY_OFFSET_CODE";
    }
    
    public static class LdParameterConstants {
        public static final String DEMERGE_DOCUMENT_TYPES = "DEMERGE_DOCUMENT_TYPES";
        // KFSPTS-1627
        public static final String VALIDATE_TRANSFER_ACCOUNT_TYPES_IND = "VALIDATE_TRANSFER_ACCOUNT_TYPES_IND";
        public static final String INVALID_TO_ACCOUNT_BY_FROM_ACCOUNT = "INVALID_TO_ACCOUNT_BY_FROM_ACCOUNT";
    }
    
    public static class GeneralLedgerSysParmeterKeys {
    	 public static final String EXPENSE_OBJECT_TYPE_CODE_PARAM = "EXPENSE_OBJECT_TYPE_CODE";
    	 public static final String INCOME_OBJECT_TYPE_CODE_PARAM = "INCOME_OBJECT_TYPE_CODE";
    	 public static final String CASH_BUDGET_RECORD_LEVEL_PARM = "CASH_BUDGET_RECORD_LEVEL";
    	 public static final String FUND_BALANCE_OBJECT_CODE_PARAM = "FUND_BALANCE_OBJECT_CODE";
    	 public static final String CURRENT_ASSET_OBJECT_CODE_PARAM = "CURRENT_ASSET_OBJECT_TYPE_CODE";
    	 public static final String CURRENT_LIABILITY_OBJECT_CODE_PARAM = "CURRENT_LIABILITY_OBJECT_TYPE_CODE";
    	 public static final String ENCUMBRANCE_BALANCE_TYPE_PARAM = "ENCUMBRANCE_BALANCE_TYPE";
    	 }
    
    //KFSPTS-1460: Added constants.
	public static final String KFS_PDP = "KFS-PDP";
    public static final String ALL_COMPONENTS = "All";
    public static final String ACH_PAYMENT_COMBINING_IND = "ACH_PAYMENT_COMBINING_IND";
    public static final String BANK_PAYMENT_FILE_EMAIL_NOTIFICATION = "BANK_PAYMENT_FILE_EMAIL_NOTIFICATION";  
    public static final String PDP_CUSTOMER_MISSING_ADVICE_RETURN_EMAIL = "PDP_CUSTOMER_MISSING_ADVICE_RETURN_EMAIL";
}
