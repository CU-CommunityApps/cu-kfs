package edu.cornell.kfs.module.ar;

import org.kuali.kfs.integration.ar.ArIntegrationConstants;

public class CuArConstants {
    public static final String CINV_FINAL_BILL_INDICATOR_CONFIRMATION_QUESTION = "ConfirmationForFinalBillIndicatorOnCINV";
    public static final int CINV_DATE_RANGE_EXPECTED_FORMAT_LENGTH = 24;
    public static final int CINV_DATE_RANGE_START_DATE_START_INDEX = 0;
    public static final int CINV_DATE_RANGE_START_DATE_END_INDEX = 10;
    public static final int CINV_DATE_RANGE_END_DATE_START_INDEX = 14;
    public static final String QUESTION_NEWLINE_STRING = "[br]";
    public static final String MTDCY = "MTDCY";
    public static final String MTDCN = "MTDCN";
    public static final String MTDC_TOTAL_INDIRECT_BASE_BUDGET_PLACEHOLDER = "totalInvoiceDetail.MTDCtotalIndirectBaseBudget";
    public static final String MTDC_TOTAL_INDIRECT_EXCLUSION_BUDGET_PACEHOLDER = "totalInvoiceDetail.MTDCtotalIndirectExclusionsBudget";
    public static final String MTDC_TOTAL_INDIRECT_BASE_INVOICE_AMOUNT_PLACEHOLDER = "totalInvoiceDetail.MTDCtotalIndirectBaseinvoiceAmount";
    public static final String MTDC_TOTAL_INDIRECT_EXCLUSION_INVOICE_AMOUNT_PLACEHOLDER = "totalInvoiceDetail.MTDCtotalIndirectExclusionsinvoiceAmount";
    public static final String MTDC_TOTAL_INDIRECT_BASE_INVOICE_AMOUNT_BILLED_TO_DATE_PLACEHOLDER = "directCostInvoiceDetail.MTDCtotalIndirectBaseAmountBilledToDate";
    public static final String MTDC_TOTAL_INDIRECT_EXCLUSION_INVOICE_AMOUNT_BILLED_TO_DATE_PLACEHOLDER = "totalInvoiceDetail.MTDCtotalIndirectExclusionsAmountBilledToDate";
    public static final String DIRECT_COST_MTDC_TOTAL_BUDGET_PLACEHOLDER = "directCostInvoiceDetail.MTDCtotalBudget";
    public static final String TOTAL_INVOICE_MTDC_TOTAL_BUDGET_PLACEHOLDER = "totalInvoiceDetail.MTDCtotalBudget";
    public static final String DIRECT_COST_MTDC_TOTAL_INVOICE_AMOUNT_PLACEHOLDER = "directCostInvoiceDetail.MTDCinvoiceAmount";
    public static final String TOTAL_INVOICE_MTDC_TOTAL_INVOICE_AMOUNT_PLACEHOLDER = "totalInvoiceDetail.MTDCinvoiceAmount";
    public static final String DIRECT_COST_MTDC_TOTAL_INVOICE_AMOUNT_TO_DATE_PLACEHOLDER = "directCostInvoiceDetail.MTDCtotalAmountBilledToDate";
    public static final String TOTAL_INVOICE_MTDC_TOTAL_INVOICE_AMOUNT_TO_DATE_PLACEHOLDER = "totalInvoiceDetail.MTDCtotalAmountBilledToDate";

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
