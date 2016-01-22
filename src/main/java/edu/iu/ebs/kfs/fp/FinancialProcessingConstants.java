package edu.iu.ebs.kfs.fp;

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

public class FinancialProcessingConstants {
    public static final String ACH_INCOME_FILE_DATE_FORMAT = "yymmddhhmm"; // (change "m"s appropriately to distinguish between hours and minutes)
    public static final String ADVACE_DEPOSIT_DEFAULT_CAMPUS_CODE = "IT";
    public static final String ADVACE_DEPOSIT_REFERENCE_ORIGIN_CODE = "01";
    public static final String ADVACE_DEPOSIT_REFEENCE_TYPE_CODE = "CR";
    public static final String ADVANCE_DEPOSIT_NOTE_FILE_PREFIX = "advancedeposit_note_"; // (the IU process embeds notes in the file)
    public static final String BATCH_DETAIL_TYPE = "Batch";
    public static final String IU_FROM_EMAIL_ADDRESS_PARM_NM = "prod_control@cornell.edu";

    public static class AchIncomeFile {
        public static final String PRD_FILE_IND = "P"; // what should this be?
    }

    public static class AchIncomeFileGroup {
        public static final String GROUP_FUNCTIONAL_IDENTIFER_CD_RA = "RA";
    }

    public static class AchIncomeFileTransaction {
        public static final String DEBIT_TRANS_IND = "D";
        public static final String TRANS_PAYMENT_METHOD_ACH = "ACH";
        public static final String TRANS_PAYMENT_METHOD_FWT = "FWT";

    }

    public static class AchIncomeFileTransactionOpenItemReference {
        public static final String RMR_REFERENCE_TYPE_CR = "CR";
        public static final String RMR_REFERENCE_TYPE_IV = "IV";
        public static final String RMR_REFERENCE_TYPE_OI = "OI";
        public static final int RMR_NOTE_LINE_COLUMN_WIDTH = 8;
        public static final int RMR_NOTE_INVOICE_NUMBER_COLUMN_WIDTH = 22;
        public static final int RMR_NOTE_NET_AMOUNT_COLUMN_WIDTH = 20;
        public static final int RMR_NOTE_INVOICE_AMOUNT_COLUMN_WIDTH = 20;
    }

    public static class AchIncomeFileTransactionReference {
        public static final String REF_REFERENCE_TYPE_TN = "TN";
        public static final String REF_REFERENCE_TYPE_CT = "CT";
        public static final String REF_REFERENCE_TYPE_VV = "VV";

    }

    public static class AchIncomeFileTransactionDateTime {
        public static final String DTM_DATE_TYPE_097 = "097";
    }

    public static class AchIncomeFileTransactionPayerOrPayeeName {
        public static final int ACH_TRN_PAYR_NM_DB_SIZE = 40;
        public static final String PAYER_TYPE_PE = "PE";
        public static final String PAYER_TYPE_PR = "PR";
    }

}
