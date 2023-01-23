package edu.cornell.kfs.pdp.batch.fixture;

import org.kuali.kfs.pdp.businessobject.options.StandardEntryClassValuesFinder;
import edu.cornell.kfs.coa.businessobject.options.CuCheckingSavingsValuesFinder;

public class StandardEntryClassConversionFixture {
    
    public enum ExpectFailureData {
        NULL_TEST(null),
        BLANK_TEST(""),
        WHITESPACE_TEST("        "),
        SINGLE_CHAR_TEST("P"),
        TWO_CHAR_TEST("PD"),
        INVALID_THREE_CHAR_TEST("ABC"),
        INVALID_FOUR_CHAR_TEST("PPD2");
    
        public final String achBankAccountTypeBeingTested;
        
        private ExpectFailureData(String achBankAccountTypeBeingTested) {
            this.achBankAccountTypeBeingTested = achBankAccountTypeBeingTested;
        }
    }
    
    public enum ExpectPDPSuccessData {
        WORKDAY_PERSONAL_CHECKING_TEST("22PPD", StandardEntryClassValuesFinder.StandardEntryClass.PPD),
        WORKDAY_PERSONAL_SAVINGS_TEST("32PPD", StandardEntryClassValuesFinder.StandardEntryClass.PPD);
    
        public final String achBankAccountTypeBeingTested;
        public final String standardEntryClassExpected;
        
        private ExpectPDPSuccessData(String achBankAccountTypeBeingTested,
                    StandardEntryClassValuesFinder.StandardEntryClass standardEntryClassExpected) {
            this.achBankAccountTypeBeingTested = achBankAccountTypeBeingTested;
            this.standardEntryClassExpected = standardEntryClassExpected.toString();
        }
    }
    
    public enum ExpectPaymentWorksSuccessData {
        CORPORATE_CHECKING_TEST(CuCheckingSavingsValuesFinder.BankAccountTypes.CORPORATE_CHECKING,
                StandardEntryClassValuesFinder.StandardEntryClass.CTX),
        CORPORATE_SAVINGS_TEST(CuCheckingSavingsValuesFinder.BankAccountTypes.CORPORATE_SAVINGS,
                StandardEntryClassValuesFinder.StandardEntryClass.CTX),
        PERSONAL_CHECKING_TEST(CuCheckingSavingsValuesFinder.BankAccountTypes.PERSONAL_CHECKING,
                StandardEntryClassValuesFinder.StandardEntryClass.PPD),
        PERSONAL_SAVINGS_TEST(CuCheckingSavingsValuesFinder.BankAccountTypes.PERSONAL_SAVINGS,
                StandardEntryClassValuesFinder.StandardEntryClass.PPD);
    
        public final String achBankAccountTypeBeingTested;
        public final String standardEntryClassExpected;
        
        private ExpectPaymentWorksSuccessData(String achBankAccountTypeBeingTested,
                StandardEntryClassValuesFinder.StandardEntryClass standardEntryClassExpected) {
            this.achBankAccountTypeBeingTested = achBankAccountTypeBeingTested;
            this.standardEntryClassExpected = standardEntryClassExpected.toString();
        }
    }
    
} 