package edu.cornell.kfs.cemi.pdp;

import java.util.Map;

import edu.cornell.kfs.coa.businessobject.options.CuCheckingSavingsValuesFinder.BankAccountTypes;

public class CemiPaymentElectionConstants {
    
    public static final String TRUE = "TRUE";
    public static final String EXPENSE_PAYMENTS = "Expense_Payments";
    public static final String US = "US";
    public static final String USD = "USD";
    public static final String DIRECT_DEPOSIT = "DIRECT_DEPOSIT";
    
    public static final String WORKDAY_CHECKING_ACCOUNT_TYPE = "DDA";
    public static final String WORKDAY_SAVINGS_ACCOUNT_TYPE = "SA";
    
    public static final Map<String, String> KfsToWorkdayBankAccountTypeCodeConverter = Map.ofEntries(
            Map.entry(BankAccountTypes.PERSONAL_CHECKING, WORKDAY_CHECKING_ACCOUNT_TYPE),
            Map.entry(BankAccountTypes.PERSONAL_SAVINGS, WORKDAY_SAVINGS_ACCOUNT_TYPE),
            Map.entry(BankAccountTypes.CORPORATE_CHECKING, WORKDAY_CHECKING_ACCOUNT_TYPE),
            Map.entry(BankAccountTypes.CORPORATE_SAVINGS, WORKDAY_SAVINGS_ACCOUNT_TYPE)
    );

    public static final String DUMMY_ACCOUNT_NUMBER = "XXXXXXXXX";

    public static final String PAYMENT_ELECTION_OUTPUT_DEFINITION_FILE_PATH = "classpath:edu/cornell/kfs/cemi/pdp/batch/CemiPaymentElectionExtractFileOutputDefinition.xml";
    public static final String PAYMENT_ELECTION_TEMPLATE_FILE_PATH = "classpath:edu/cornell/kfs/cemi/pdp/batch/Payment_Election.xlsx";
    public static final String PAYMENT_ELECTION_EXTRACT_FILENAME_PREFIX = "Payment_Election_ITH_";
    public static final String PAYMENT_ELECTION_EXTRACT_PLAIN_FILENAME = "Payment_Election.xlsx";
    
    public static final class PaymentElectionExtractSheets {
        public static final String GROUP_TWO = "Group_TWO";
    }
    
    public static final String CU_CEMI_EXTR_GRP_TWO_TAB_PYMNT_ELCTN_SEQ = "CU_CEMI_EXTR_GRP_TWO_TAB_PYMNT_ELCTN_SEQ";
    
}
