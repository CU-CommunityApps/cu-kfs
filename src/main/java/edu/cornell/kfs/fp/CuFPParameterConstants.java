package edu.cornell.kfs.fp;

/**
 Portions Modified 04/2016 and Copyright Cornell University

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 Copyright Indiana University
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class CuFPParameterConstants {

    public static class AchIncome {
        public static final String ACH_INCOME_SUMMARY_EMAIL_SUBJECT = "ACH_INCOME_SUMMARY_EMAIL_SUBJECT";
        public static final String ACH_INCOME_SUMMARY_FROM_EMAIL_ADDRESS = "ACH_INCOME_SUMMARY_FROM_EMAIL_ADDRESS";
        public static final String ACH_INCOME_SUMMARY_TO_EMAIL_ADDRESSES = "ACH_INCOME_SUMMARY_TO_EMAIL_ADDRESSES";
    }

    public static class AdvanceDepositDocument {
        public static final String DETAIL_REFERENCE_NUMBER = "DETAIL_REFERENCE_NUMBER";
        public static final String DETAIL_DESCRIPTION = "DETAIL_DESCRIPTION";
        public static final String BANK_CODE = "BANK_CODE";
        public static final String DOCUMENT_DESCRIPTION = "DOCUMENT_DESCRIPTION";
        public static final String CHART = "CHART";
        public static final String OBJECT_CODE = "OBJECT_CODE";
        public static final String ACCOUNT = "ACCOUNT";
    }
    
    public static class CorporateBilledCorporatePaidDocument {
        public static final String CBCP_COMPONENT_NAME = "CorporateBilledCorporatePaidDocument";
        public static final String CBCP_ACCOUNTING_DEFAULT_IND_PARAMETER_NAME = "CBCP_ACCOUNTING_DEFAULT_IND";
        public static final String CBCP_HOLDER_DEFAULT_IND_PARAMETER_NAME = "CBCP_HOLDER_DEFAULT_IND";
        public static final String DEFAULT_ACCOUNT_PARAMETER_NAME = "DEFAULT_ACCOUNT";
        public static final String DEFAULT_AMOUNT_OWED_OBJECT_CODE_PARAMETER_NAME = "DEFAULT_AMOUNT_OWED_OBJECT_CODE";
        public static final String DEFAULT_CHART_PARAMETER_NAME = "DEFAULT_CHART";
        public static final String DEFAULT_LIABILITY_OBJECT_CODE_PARAMETER_NAME = "DEFAULT_LIABILITY_OBJECT_CODE";
        public static final String DOCUMENT_EXPLANATION_PARAMETER_NAME = "DOCUMENT_EXPLANATION";
        public static final String SINGLE_TRANSACTION_IND_PARAMETER_NAME = "SINGLE_TRANSACTION_IND";
        public static final String CBCP_REPORT_EMAIL_ADDRESS_PARAMETER_NAME = "CBCP_REPORT_EMAIL_ADDRESS";
    }
    
    public static class CreateAccountingDocumentService {
        public static final String CREATE_ACCOUNTING_DOCUMENT_SERVICE_COMPONENT_NAME = "CreateAccountingDocumentService";
        public static final String CREATE_ACCT_DOC_REPORT_EMAIL_ADDRESS = "CREATE_ACCT_DOC_REPORT_EMAIL_ADDRESS";
    }

    public static final String INTERNAL_BILLING_COMPONENT = "InternalBilling";
    public static final String MAX_TOTAL_THRESHOLD_AMOUNT = "MAX_TOTAL_THRESHOLD_AMOUNT";
}
