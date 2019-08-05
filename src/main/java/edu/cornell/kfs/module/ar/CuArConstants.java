package edu.cornell.kfs.module.ar;

import org.kuali.kfs.integration.ar.ArIntegrationConstants;

public class CuArConstants {
    public static final String CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION = "ConfirmationForFinalBillIndicatorOnCINV";
    public static final int CINV_DATE_RANGE_EXPECTED_FORMAT_LENGTH = 24;
    public static final int CINV_DATE_RANGE_START_DATE_START_INDEX = 0;
    public static final int CINV_DATE_RANGE_START_DATE_END_INDEX = 10;
    public static final int CINV_DATE_RANGE_END_DATE_START_INDEX = 14;
    public static final String QUESTION_NEWLINE_STRING = "[br]";

    public enum AwardInvoicingOptionCodeToName {
        INV_AWARD(ArIntegrationConstants.AwardInvoicingOptions.INV_AWARD, "Invoice By Award"),
        INV_ACCOUNT(ArIntegrationConstants.AwardInvoicingOptions.INV_ACCOUNT, "Invoice By Account"),
        INV_CONTRACT_CONTROL_ACCOUNT(ArIntegrationConstants.AwardInvoicingOptions.INV_CONTRACT_CONTROL_ACCOUNT, "Invoice By Contract Control Account"),
        INV_SCHEDULE(ArIntegrationConstants.AwardInvoicingOptions.INV_SCHEDULE, "Invoice By Schedule");

        private String code;
        private String name;

        AwardInvoicingOptionCodeToName(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return this.code;
        }

        public String getName() {
            return this.name;
        }

        public static String getName(String code) {
            for (AwardInvoicingOptionCodeToName option : AwardInvoicingOptionCodeToName.values()) {
                if (option.getCode().equals(code)) {
                    return option.getName();
                }
            }
            return null;
        }
    }

}
