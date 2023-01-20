package edu.cornell.kfs.pdp.batch.fixture;

import org.kuali.kfs.pdp.businessobject.options.StandardEntryClassValuesFinder;
import edu.cornell.kfs.coa.businessobject.options.CuCheckingSavingsValuesFinder;

public class StandardEntryClassConversionFixture {
    
    public enum ExpectFailureData {
        NULL_TEST("NULL_TEST", null),
        BLANK_TEST("BLANK_TEST", ""),
        WHITESPACE_TEST("WHITESPACE_TEST", "        "),
        SINGLE_CHAR_TEST("SINGLE_CHAR_TEST", "P"),
        TWO_CHAR_TEST("TWO_CHAR_TEST", "PD"),
        INVALID_THREE_CHAR_TEST("INVALID_THREE_CHAR_TEST", "ABC"),
        INVALID_FOUR_CHAR_TEST("INVALID_FOUR_CHAR_TEST", "PPD2");
    
        public final String typeOfTest;
        public final String achBankAccountTypeBeingTested;
        
        private ExpectFailureData(String typeOfTest, String achBankAccountTypeBeingTested) { 
            this.typeOfTest = typeOfTest;
            this.achBankAccountTypeBeingTested = achBankAccountTypeBeingTested;
        }
    }
    
    public enum ExpectPDPSuccessData {
        WORKDAY_PERSONAL_CHECKING_TEST("WORKDAY_PERSONAL_CHECKING_TEST", "22PPD", StandardEntryClassValuesFinder.StandardEntryClass.PPD.toString()),
        WORKDAY_PERSONAL_SAVINGS_TEST("WORKDAY_PERSONAL_SAVINGS_TEST", "32PPD", StandardEntryClassValuesFinder.StandardEntryClass.PPD.toString());
    
        public final String typeOfTest;
        public final String achBankAccountTypeBeingTested;
        public final String standardEntryClassExpected;
        
        private ExpectPDPSuccessData(String typeOfTest, String achBankAccountTypeBeingTested, String standardEntryClassExpected) { 
            this.typeOfTest = typeOfTest;
            this.achBankAccountTypeBeingTested = achBankAccountTypeBeingTested;
            this.standardEntryClassExpected = standardEntryClassExpected;
        }
    }
    
    public enum ExpectPaymentWorksSuccessData {
        CORPORATE_CHECKING_TEST("CORPORATE_CHECKING_TEST",
                CuCheckingSavingsValuesFinder.BankAccountTypes.CORPORATE_CHECKING, StandardEntryClassValuesFinder.StandardEntryClass.CTX.toString()),
        CORPORATE_SAVINGS_TEST("CORPORATE_SAVINGS_TEST",
                CuCheckingSavingsValuesFinder.BankAccountTypes.CORPORATE_SAVINGS, StandardEntryClassValuesFinder.StandardEntryClass.CTX.toString()),
        PERSONAL_CHECKING_TEST("PERSONAL_CHECKING_TEST",
                CuCheckingSavingsValuesFinder.BankAccountTypes.PERSONAL_CHECKING, StandardEntryClassValuesFinder.StandardEntryClass.PPD.toString()),
        PERSONAL_SAVINGS_TEST("PERSONAL_SAVINGS_TEST",
                CuCheckingSavingsValuesFinder.BankAccountTypes.PERSONAL_SAVINGS, StandardEntryClassValuesFinder.StandardEntryClass.PPD.toString());
    
        public final String typeOfTest;
        public final String achBankAccountTypeBeingTested;
        public final String standardEntryClassExpected;
        
        private ExpectPaymentWorksSuccessData(String typeOfTest, String achBankAccountTypeBeingTested, String standardEntryClassExpected) { 
            this.typeOfTest = typeOfTest;
            this.achBankAccountTypeBeingTested = achBankAccountTypeBeingTested;
            this.standardEntryClassExpected = standardEntryClassExpected;
        }
    }
    
} 