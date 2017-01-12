/*
 * Copyright 2007 The Kuali Foundation
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
package edu.cornell.kfs.sys;

import org.kuali.kfs.sys.KFSPropertyConstants;

/**
 * Property name constants.
 */
public class CUKFSPropertyConstants extends KFSPropertyConstants {

    // KFSUPGRADE-779
    public static final String DOC_HDR_FINANCIAL_DOCUMENT_STATUS_CODE = "documentHeader.financialDocumentStatusCode";
    
    public static final String PROGRAM_CODE = "programCode";
    public static final String APPROPRIATION_ACCT_NUMBER = "appropriationAccountNumber";

    public static final String ACCT_REVERSION_CHART_OF_ACCT_CODE = "chartOfAccountsCode";
    public static final String ACCT_REVERSION_BUDGET_REVERSION_CHART_OF_ACCT_CODE = "budgetReversionChartOfAccountsCode";
    public static final String ACCT_REVERSION_CASH_REVERSION_CHART_OF_ACCT_CODE = "cashReversionFinancialChartOfAccountsCode";
    public static final String ACCT_REVERSION_ACCT_NUMBER = "accountNumber";
    public static final String ACCT_REVERSION_BUDGET_REVERSION_ACCT_NUMBER = "budgetReversionAccountNumber";
    public static final String ACCT_REVERSION_CASH_REVERSION_ACCT_NUMBER = "cashReversionAccountNumber";
    public static final String ACCT_REVERSION_CATEGORY_CODE = "accountReversionCategoryCode";
    public static final String ACCT_REVERSION_ACTIVE = "active";
    
    public static final String SUB_OBJ_CODE_EDIT_CHANGE_DETAILS = "subObjCdGlobalEditDetails";
    public static final String SUB_ACCOUNT_GLBL_CHANGE_DETAILS = "subAccountGlobalDetails";

    public static final String DOCUMENT_FAVORITE_ACCOUNT_LINE_IDENTIFIER = "document.favoriteAccountLineIdentifier";

    public static final String AWARD_EXTENSION_BUDGET_ENDING_DATE = "extension.budgetEndingDate";
    
    public static final String RECURRING_DV_PARTIAL_TRANSACTION_COUNT_FIELD_NAME = "partialTransactionCount";

    public static final String CONTRACTS_AND_GRANTS_ACCOUNT_RESPOSIBILITY_ID = "contractsAndGrantsAccountResponsibilityId";
    public static final String REMOVE_INCOME_STREAM_CHART_AND_ACCOUNT = "removeIncomeStreamChartAndAccount";
    public static final String REMOVE_CONTINUATION_CHART_AND_ACCOUNT = "removeContinuationChartAndAccount";
    public static final String RECURRING_DV_PAYMENT_CANCEL_REASON_FIELD_NAME = "paymentCancelReason";
    
    public static final String WEB_SERVICE_CREDENTIAL_KEY = "credentialKey";
}
