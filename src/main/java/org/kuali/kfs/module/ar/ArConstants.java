/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.ar;

import com.lowagie.text.Font;
import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.integration.ar.ArIntegrationConstants;
import org.kuali.kfs.integration.ar.Billable;

import java.util.ArrayList;
import java.util.List;

/*
 * CU Customization: Backported the FINP-7147 changes into this file.
 * This overlay can be removed when we upgrade to the 2023-06-28 financials patch.
 */
public final class ArConstants {

    public static final String AR_NAMESPACE_CODE = "KFS-AR";

    public static final String INVOICE_DOC_TYPE = "Invoice";
    public static final String CREDIT_MEMO_DOC_TYPE = "Credit Memo";
    public static final String PAYMENT_DOC_TYPE = "Payment";
    public static final String WRITEOFF_DOC_TYPE = "Writeoff";
    public static final String SORT_INDEX_SESSION_KEY = "sortIndex";

    public static final String NUM_SORT_INDEX_CLICK_SESSION_KEY = "numberOfSortClicked";
    public static final String TOTALS_TABLE_KEY = "totalsTable";
    public static final String COLLECTION_ACTIVITY_REPORT_LOOKUP = "collectionActivityReportLookup";

    // System Parameters

    public static final String INCLUDE_ZERO_BALANCE_CUSTOMERS = "INCLUDE_ZERO_BALANCE_CUSTOMERS";

    public static final String BASIS_OF_ACCOUNTING_CASH = "1";
    public static final String BASIS_OF_ACCOUNTING_ACCRUAL = "2";
    public static final String DUNNING_LETTER_TEMPLATE_UPLOAD = "template.dunningLetterTemplateUpload";
    public static final String DUNNING_LETTER_TEMPLATE_TYPE = "Dunning Letter";
    public static final String DUNNING_LETTER_TEMPLATE_NEW_FILE_NAME_PREFIX = "Dunning_Letter_Template_";
    public static final String INVOICE_TEMPLATE_UPLOAD = "document.invoiceTemplateUpload";
    public static final String INVOICE_TEMPLATE_TYPE = "Invoice";
    public static final String INVOICE_TEMPLATE_NEW_FILE_NAME_PREFIX = "Invoice_Template_";
    public static final String CONTRACTS_GRANTS_INVOICE_COMPONENT = "ContractsGrantsInvoice";

    public static final String NEW_CASH_CONTROL_DETAIL_ERROR_PATH_PREFIX = "newCashControlDetail";

    public static final String CUSTOMER_INVOICE_DETAIL_UOM_DEFAULT = "EA";

    public static final String WRITEOFF_METHOD_CHART = "1";
    public static final String WRITEOFF_METHOD_ORG_ACCT_DEFAULT = "2";
    public static final String COPY_CUSTOMER_INVOICE_DOCUMENT_WITH_DISCOUNTS_QUESTION = "ConfirmationForCopyingInvoiceWithDiscounts";
    public static final String SUSPENSION_CATEGORIES_PRESENT_QUESTION = "ConfirmationForSuspensionCategoriesPresent";
    public static final String CUST_ADDR_ID_SEQ = "CUST_ADDR_ID_SEQ";

    // We need a higher scale than the default 2 for item quantity in Customer Invoice and Credit Memo,
    // because we might have a very small invoice open amount and need to apply very small item quantity in credit memo.
    public static int ITEM_QUANTITY_SCALE = 10;

    public static final String AWARD_LOOKUP_IMPL = "awardLookupable";
    public static final String AWARD_ACCOUNT_LOOKUP_IMPL = "awardAccountLookupable";
    public static final String CUSTOMER_OPEN_ITEM_REPORT_LOOKUPABLE_IMPL = "arCustomerOpenItemReportLookupable";

    public static final String INV_DOCUMENT_DESCRIPTION = "Customer Invoice";

    public static final String CUSTOMER_COMPONENT = "Customer";

    public static final String CLEAR_INIT_TAB_METHOD = "clearInitTab";
    public static final String CONTINUE_LOC_REVIEW_METHOD = "continueLOCReview";
    public static final String DOWNLOAD_METHOD = "download";
    public static final String EXPORT_METHOD = "export";
    public static final String PRINT_METHOD = "print";
    public static final String PRINT_CREDIT_MEMO_PDF_METHOD = "printCreditMemoPDF";
    public static final String PRINT_INVOICE_PDF_METHOD = "printInvoicePDF";
    public static final String UPLOAD_METHOD = "upload";
    public static final String CLEAR_BUTTON_FILE_NAME = "buttonsmall_clear.gif";
    public static final String CLEAR_BUTTON_ALT_TEXT = "Clear";
    public static final String CONTINUE_BUTTON_FILE_NAME = "buttonsmall_continue.gif";
    public static final String CONTINUE_BUTTON_ALT_TEXT = "Continue";
    public static final String EXPORT_BUTTON_FILE_NAME = "buttonsmall_export.gif";
    public static final String EXPORT_BUTTON_ALT_TEXT = "Export";
    public static final String EXPORT_BUTTON_ONCLICK_TEXT = "excludeSubmitRestriction=true";
    public static final String PRINT_BUTTON_FILE_NAME = "buttonsmall_genprintfile.gif";
    public static final String PRINT_BUTTON_ALT_TEXT = "Print";
    public static final String PRORATE_BUTTON_METHOD = "methodToCall.prorateBill";
    public static final String PRORATE_BUTTON_FILE_NAME = "buttonsmall_prorate.gif";
    public static final String PRORATE_BUTTON_ALT_TEXT = "Prorate Bill";
    public static final String TRANSMIT_GENERATE_BUTTON_FILE_NAME = "buttonsmall_transmitgenerate.gif";
    public static final String TRANSMIT_GENERATE_BUTTON_ALT_TEXT = "Transmit/Generate Print File";
    public static final String UPDATE_FINAL_BILL_IND_BUTTON_METHOD = "methodToCall.updateFinalBillIndicator";

    // Agency Address
    public static final String AGENCY_PRIMARY_ADDRESSES_TYPE_CODE = "P";
    public static final String AGENCY_ALTERNATE_ADDRESSES_TYPE_CODE = "A";

    public static final String DISCOUNT_PREFIX = "DISCOUNT - ";
    public static final String CUSTOMER_INVOICE_WRITEOFF_SUMMARY_ACTION = "viewSummary";
    public static final String CUSTOMER_INVOICE_WRITEOFF_DOCUMENT_DESCRIPTION = "Writeoff for ";

    public static final String LOCKBOX_DOCUMENT_DESCRIPTION = "Created by Lockbox ";
    public static final String LOCKBOX_REMITTANCE_FOR_INVALID_INVOICE_NUMBER = "Lockbox: Remittance for INVALID invoice number ";
    public static final String LOCKBOX_REMITTANCE_FOR_CLOSED_INVOICE_NUMBER = "Lockbox: Remittance for CLOSED invoice number ";
    public static final String LOCKBOX_REMITTANCE_FOR_INVOICE_NUMBER = "Lockbox: Remittance for invoice number ";

    // Award
    public static final String LOC_BY_LOC_FUND = "LOC By Letter of Credit Fund";
    public static final String LOC_BY_LOC_FUND_GRP = "LOC By Letter of Credit Fund Group";

    public static final String AWARD_TOTAL = "awardTotal";
    public static final String AWARD_FUND_MANAGER = "awardFundManager";
    public static final String AWARD_FUND_MANAGERS = "awardFundManagers";
    public static final String AWARD_PROJECT_DIRECTORS = "awardProjectDirectors";
    public static final String AWARD_PROJECT_DIRECTOR = "awardProjectDirector";
    public static final String INVOICE_REPORT_OPTION = "dummyBusinessObject.invoiceReportOption";
    public static final String CGINV_DOC_ERR_LOG_REPORT = "Contracts & Grants Invoice Document Error Log Report";
    public static final String OUTSTANDING_INVOICE_REPORT = "Outstanding Invoice Report";
    public static final String OUTSTANDING_INVOICES = "Outstanding Invoices";
    public static final String PAST_DUE_INVOICES = "Past Due Invoices";
    public static final String CONTRACTS_GRANTS_SUSPENDED_INVOICE_SUMMARY_REPORT = "ContractsGrantsSuspendedInvoiceSummaryReport";
    public static final String CONTRACTS_GRANTS_SUSPENDED_INVOICE_DETAIL_REPORT = "ContractsGrantsSuspendedInvoiceDetailReport";
    public static final String CONTRACTS_GRANTS_PAYMENT_HISTORY_REPORT = "ContractsGrantsPaymentHistoryReport";
    public static final String CONTRACTS_GRANTS_LOC_REPORT = "ContractsGrantsLOCReport";
    public static final String CONTRACTS_GRANTS_MILESTONE_REPORT = "ContractsGrantsMilestoneReport";
    public static final String COLLECTION_ACTIVITY_REPORT_SORT_FIELD = "CollectionActivityReport";
    public static final String CONTRACTS_GRANTS_INVOICE_REPORT_SORT_FIELD = "ContractsGrantsInvoiceReport";
    public static final String CONTRACTS_GRANTS_INVOICE_ERROR_LOG_REPORT_SORT_FIELD = "ContractsGrantsInvoiceDocumentErrorLog";
    public static final String TICKLERS_REPORT_SORT_FIELD = "TicklersReport";
    public static final String COLLECTION_ACTIVITY_REPORT_TITLE = "Collection Activity Report";
    public static final String TICKLER_REPORT = "Tickler Report";
    public static final String QUARTER1 = "q1";
    public static final String QUARTER2 = "q2";
    public static final String QUARTER3 = "q3";
    public static final String QUARTER4 = "q4";
    public static final String SEMI_ANNUAL = "sa";
    public static final String ANNUAL = "an";
    public static final String FINAL = "f";

    // CG Invoice Document
    public static final String CONTRACTS_GRANTS_INVOICE_DOCUMENT_DESCRIPTION = "Contracts & Grants Invoice";
    public static final String ACCOUNT = "Account";
    public static final String CONTRACT_CONTROL_ACCOUNT = "Contract Control Account";
    public static final String BILL_SECTION = "Bills";
    public static final String MILESTONES_SECTION = "Milestones";

    public static final String INCOME_ACCOUNT = "Income";
    public static final String INV_AWARD = ArIntegrationConstants.AwardInvoicingOptions.INV_AWARD;
    public static final String INV_ACCOUNT = ArIntegrationConstants.AwardInvoicingOptions.INV_ACCOUNT;
    public static final String INV_CONTRACT_CONTROL_ACCOUNT = ArIntegrationConstants.AwardInvoicingOptions.INV_CONTRACT_CONTROL_ACCOUNT;
    public static final String INV_SCHEDULE = ArIntegrationConstants.AwardInvoicingOptions.INV_SCHEDULE;
    public static final String INVOICE_AMOUNT_LABEL = "Invoice Amount";
    public static final String PRINT_INVOICES_FROM_LABEL = "Print Invoices From";
    public static final String PRINT_INVOICES_TO_LABEL = "Print Invoices To";

    public static final String LETTER_OF_CREDIT_REVIEW_DOCUMENT = "Letter Of Credit Review Document.";

    public static final String UPDATE_FINAL_BILL_INDICATOR_QUESTION = "FinalBillIndicatorUpdateQuestion";

    // Agency Collections Maintenance
    public static final String CHAPTER7_CODE = "C7";
    public static final String CHAPTER11_CODE = "C11";
    public static final String CHAPTER13_CODE = "C13";
    public static final String JUDGMENT_OBTAINED_CODE = "JO";

    public static final String CHAPTER7 = "Chapter 7";
    public static final String CHAPTER11 = "Chapter 11";
    public static final String CHAPTER13 = "Chapter 13";
    public static final String JUDGMENT_OBTAINED = "Judgment Obtained";

    public static final String INVOICES_FILE_PREFIX = "Invoices-";
    public static final String INVOICE_ENVELOPES_FILE_PREFIX = "InvoiceEnvelopes-";
    public static final String INVOICE_ZIP_FILE_PREFIX = "Invoice-report";

    // federal financial report
    public static final String FF_425_TEMPLATE_NM = "/org/kuali/kfs/module/ar/document/FEDERAL_FINANCIAL_FORM_425.pdf";
    public static final String FF_425A_TEMPLATE_NM = "/org/kuali/kfs/module/ar/document/FEDERAL_FINANCIAL_FORM_425A.pdf";
    public static final String FEDERAL_FORM_425 = "425";
    public static final String FEDERAL_FORM_425A = "425A";

    public static final String FEDERAL_FUND_425_REPORT_ABBREVIATION = "FF425";
    public static final String FEDERAL_FUND_425A_REPORT_ABBREVIATION = "FF425A";

    public static final String FROM_SUFFIX = " From";
    public static final String TO_SUFFIX = " To";

    public static final String LETTER_OF_CREDIT_REVIEW_INIT_SECTION = "letterOfCreditReviewInitSection";
    public static final String LETTER_OF_CREDIT_REVIEW_AMOUNT_TO_DRAW_QUESTION_ID =
            "LetterOfCreditReviewDocumentAmountToDrawQuestionID";

    /**
     * Private Constructor since this is a constants class that should never be instantiated.
     */
    private ArConstants() {
    }

    public static class PermissionNames {
        public static final String REPORT = "Report";
    }

    public static class TemplateUploadSystem {
        public static final String EXTENSION = ".pdf";
        public static final String TEMPLATE_MIME_TYPE = "application/pdf";
    }

    public static class PaymentMediumCode {
        public static final String CASH = "CA";
        public static final String CHECK = "CK";
        public static final String WIRE_TRANSFER = "WT";
        public static final String CREDIT_CARD = "CR";
    }

    public static class ReportOptionFieldValues {
        public static final String PROCESSING_ORG = "Processing Organization";
        public static final String BILLING_ORG = "Billing Organization";
    }

    public static class LOCReportTypeFieldValues {
        public static final String DRAW_DETAILS = "Details Report";
        public static final String AMOUNTS_NOT_DRAWN = "Amounts Not Drawn Report";
    }

    public static class CustomerAgingReportFields {
        public static final String ACCT = "Account";

        public static final String TOTAL_0_TO_30 = "total0to30";
        public static final String TOTAL_31_TO_60 = "total31to60";
        public static final String TOTAL_61_TO_90 = "total61to90";
        public static final String TOTAL_91_TO_SYSPR = "total90toSYSPR";
        public static final String TOTAL_AMOUNT_DUE = "totalAmountDue";
    }

    public static class CustomerCreditMemoStatuses {
        public static final String INITIATE = "INIT";
        public static final String IN_PROCESS = "INPR";
    }

    public static class CustomerInvoiceWriteoffStatuses {
        public static final String INITIATE = "INIT";
        public static final String IN_PROCESS = "INPR";
    }

    public static class CustomerCreditMemoConstants {
        public static final String CUSTOMER_CREDIT_MEMO_ITEM_QUANTITY = "qty";
        public static final String CUSTOMER_CREDIT_MEMO_ITEM_TOTAL_AMOUNT = "itemAmount";
        public static final String BOTH_QUANTITY_AND_ITEM_TOTAL_AMOUNT_ENTERED = "both";
        public static final String GENERATE_CUSTOMER_CREDIT_MEMO_DOCUMENT_QUESTION_ID = "GenerateCustomerCreditMemoDocumentQuestionID";
    }

    public static final class CustomerLoad {
        public static final String CUSTOMER_LOAD_FILE_TYPE_IDENTIFIER = "customerLoadInputFileType";
        public static final String CUSTOMER_CSV_LOAD_FILE_TYPE_IDENTIFIER = "customerLoadCSVInputFileType";
        public static final String CUSTOMER_LOAD_REPORT_SUBFOLDER = "ar";
        public static final String BATCH_REPORT_BASENAME = "ar_customer_load";
    }

    public static final class CustomerInvoiceWriteoff {
        public static final String CUSTOMER_INVOICE_WRITEOFF_FILE_TYPE_IDENTIFIER = "customerInvoiceWriteoffInputFileType";
        public static final String CUSTOMER_INVOICE_WRITEOFF_REPORT_SUBFOLDER = "ar";
        public static final String BATCH_REPORT_BASENAME = "customer_invoice_writeoff";
    }

    public static final class Lockbox {
        public static final String LOCKBOX_REPORT_SUBFOLDER = "ar";
        public static final String BATCH_REPORT_BASENAME = "lockbox_batch";
    }

    public static final class MilestoneScheduleImport {
        public static final String FILE_TYPE_IDENTIFIER = "MilestoneScheduleCsvInputFileType";
    }

    public static final class PredeterminedBillingScheduleImport {
        public static final String FILE_TYPE_IDENTIFIER = "PredeterminedBillingScheduleCsvInputFileType";
    }

    public static class PrintInvoiceOptions {
        public static final String PRINT_BY_PROCESSING_ORG = "Q";
        public static final String PRINT_BY_USER = "U";
        public static final String PRINT_BY_BILLING_ORG = "B";
    }

    // Organization Options Section Ids
    public static class OrganizationOptionsSections {
        public static final String EDIT_ORGANIZATION_REMIT_TO_ADDRESS = "Edit Organization Remit To Address";
    }

    public static class BatchFileSystem {
        public static final String EXTENSION = ".txt";

        public static final String CGINVOICE_BATCH_VALIDATION_ERROR_OUTPUT_FILE = "cgin_batch_validation_err";
        public static final String CGINVOICE_BATCH_CREATION_ERROR_OUTPUT_FILE = "cgin_batch_create_doc_err";

        public static final String LOC_CREATION_BY_LOCF_ERROR_OUTPUT_FILE = "cgin_loc_by_loc_fund_create_doc_err";
    }

    public static class ReportsConstants {

        public static final List<String> awardBalancesReportSubtotalFieldsList = new ArrayList<>();
        static {
            awardBalancesReportSubtotalFieldsList.add("agency.fullName");
            awardBalancesReportSubtotalFieldsList.add("awardStatusCode");
            awardBalancesReportSubtotalFieldsList.add("awardPrimaryProjectDirector.projectDirector.name");
            awardBalancesReportSubtotalFieldsList.add("awardPrimaryFundManager.fundManager.name");
        }

        public static final List<String> reportSearchCriteriaExceptionList = new ArrayList<>();
        static {
            reportSearchCriteriaExceptionList.add("backLocation");
            reportSearchCriteriaExceptionList.add("docFormKey");
            reportSearchCriteriaExceptionList.add("dummyBusinessObject.invoiceReportOption");
        }

        public static final List<String> cgInvoiceReportSubtotalFieldsList = new ArrayList<>();
        static {
            cgInvoiceReportSubtotalFieldsList.add("proposalNumber");
        }

        public static final List<String> cgPaymentHistoryReportSubtotalFieldsList = new ArrayList<>();
        static {
            cgPaymentHistoryReportSubtotalFieldsList.add("awardNumber");
            cgPaymentHistoryReportSubtotalFieldsList.add("customerName");
            cgPaymentHistoryReportSubtotalFieldsList.add("paymentNumber");
            cgPaymentHistoryReportSubtotalFieldsList.add("paymentDate");
        }

        public static final List<String> cgLOCReportSubtotalFieldsList = new ArrayList<>();
        public static final List<String> cgSuspendedInvoiceDetailReportSubtotalFieldsList = new ArrayList<>();

        public static final String INVOICE_INDICATOR_OPEN = "Open";
        public static final String INVOICE_INDICATOR_CLOSE = "Close";
    }

    public static class InvoiceTransmissionMethod {
        public static final String MAIL = "MAIL";
        public static final String EMAIL = "EMAIL";
    }

    public static class DunningLetters {
        public static final String DYS_PST_DUE_CURRENT = "Current";
        public static final String DYS_PST_DUE_31_60 = "31-60";
        public static final String DYS_PST_DUE_61_90 = "61-90";
        public static final String DYS_PST_DUE_91_120 = "91-120";
        public static final String DYS_PST_DUE_121 = "121+";
        public static final String DYS_PST_DUE_FINAL = "FINAL";
        public static final String DYS_PST_DUE_STATE_AGENCY_FINAL = "State Agency FINAL";
        public static final String DUNNING_LETTER_SENT_TXT = "Dunning Letter has been sent to sponsor.";
    }

    public static class ContractsGrantsAgingReportFields {
        public static final String OPEN_INVOCE_REPORT_NAME = "Contracts & Grants Open Invoices Report";
        public static final String AGENCY_SHORT_NAME = "Agency Short Name";
        public static final String TOTAL_WRITEOFF = "Total Write-Off";
        public static final String TOTAL_CREDITS = "Total Credits";
    }

    public static class ArDocumentTypeCodes {
        public static final String CASH_CONTROL = "CTRL";
        public static final String COLLECTION_EVENT = "CVNT";
        public static final String CONTRACTS_GRANTS_COLLECTION_ACTIVTY = "CCA";
        public static final String CONTRACTS_GRANTS_INVOICE = "CINV";
        public static final String CUSTOMER_CREDIT_MEMO_DOCUMENT_TYPE_CODE = "CRM";
        public static final String FINAL_BILLED_INDICATOR_DOCUMENT_TYPE_CODE = "FBI";
        public static final String INV_DOCUMENT_TYPE = "INV";
        public static final String INVOICE_WRITEOFF_DOCUMENT_TYPE_CODE = "INVW";
        public static final String LETTER_OF_CREDIT_REVIEW = "LCR";
        public static final String MILESTONE_SCHEDULE = "MILE";
        public static final String PAYMENT_APPLICATION_ADJUSTMENT_DOCUMENT_TYPE_CODE = "APPA";
        public static final String PAYMENT_APPLICATION_DOCUMENT_TYPE_CODE = "APP";
        public static final String PREDETERMINED_BILLING_SCHEDULE = "PDBS";
    }

    public enum BillingFrequencyValues {
        PREDETERMINED_BILLING(ArIntegrationConstants.BillingFrequencyValues.PREDETERMINED_BILLING),
        MILESTONE(ArIntegrationConstants.BillingFrequencyValues.MILESTONE),
        MONTHLY(ArIntegrationConstants.BillingFrequencyValues.MONTHLY),
        QUARTERLY(ArIntegrationConstants.BillingFrequencyValues.QUARTERLY),
        SEMI_ANNUALLY(ArIntegrationConstants.BillingFrequencyValues.SEMI_ANNUALLY),
        ANNUALLY(ArIntegrationConstants.BillingFrequencyValues.ANNUALLY),
        LETTER_OF_CREDIT(ArIntegrationConstants.BillingFrequencyValues.LETTER_OF_CREDIT),
        MANUAL(ArIntegrationConstants.BillingFrequencyValues.MANUAL);

        private final String code;

        BillingFrequencyValues(final String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }

        public static BillingFrequencyValues fromCode(final String code) {
            if (code == null || code.isEmpty()) {
                return null;
            }

            for (final BillingFrequencyValues billingFrequency : BillingFrequencyValues.values()) {
                if (code.equals(billingFrequency.code)) {
                    return billingFrequency;
                }
            }

            return null;
        }

        public static boolean isPredeterminedBilling(final Billable billable) {
            return StringUtils.equals(billable.getBillingFrequencyCode(), PREDETERMINED_BILLING.code);
        }

        public static boolean isMilestone(final Billable billable) {
            return StringUtils.equals(billable.getBillingFrequencyCode(), MILESTONE.code);
        }

        public static boolean isLetterOfCredit(final Billable billable) {
            return StringUtils.equals(billable.getBillingFrequencyCode(), LETTER_OF_CREDIT.code);
        }

        public static boolean isTimeBased(final Billable billable) {
            return StringUtils.equals(MONTHLY.code, billable.getBillingFrequencyCode())
                    || StringUtils.equals(QUARTERLY.code, billable.getBillingFrequencyCode())
                    || StringUtils.equals(SEMI_ANNUALLY.code, billable.getBillingFrequencyCode())
                    || StringUtils.equals(ANNUALLY.code, billable.getBillingFrequencyCode())
                    || StringUtils.equals(MANUAL.code, billable.getBillingFrequencyCode());
        }
    }

    public static class ReportBuilderDataServiceBeanNames {
        public static final String CONTRACTS_GRANTS_SUSPENDED_INVOICE_SUMMARY = "contractsGrantsSuspendedInvoiceSummaryReportBuilderService";
        public static final String CONTRACTS_GRANTS_AGING = "contractsGrantsAgingReportBuilderService";
        public static final String CONTRACTS_GRANTS_INVOICE = "contractsGrantsInvoiceReportBuilderService";
        public static final String CONTRACTS_GRANTS_INVOICE_ERROR_LOG = "contractsGrantsInvoiceDocumentErrorLogReportBuilderService";
        public static final String COLLECTION_ACTIVITY = "collectionActivityReportBuilderService";
        public static final String CONTRACTS_GRANTS_LOC = "contractsGrantsLOCReportBuilderService";
        public static final String CONTRACTS_GRANTS_MILESTONE = "contractsGrantsMilestoneReportBuilderService";
        public static final String CONTRACTS_GRANTS_PAYMENT_HISTORY = "contractsGrantsPaymentHistoryReportBuilderService";
        public static final String TICKLERS = "ticklersReportBuilderService";
        public static final String CONTRACTS_GRANTS_SUSPENDED_INVOICE_DETAIL = "contractsGrantsSuspendedInvoiceDetailReportBuilderService";
    }

    public static class Actions {
        public static final String ACCOUNTS_RECEIVABLE_INVOICE_TEMPLATE_UPLOAD = "arAccountsReceivableInvoiceTemplateUpload";
        public static final String ACCOUNTS_RECEIVABLE_DUNNING_LETTER_TEMPLATE_UPLOAD = "arAccountsReceivableDunningLetterTemplateUpload";
        public static final String TRANSMIT_CONTRACTS_AND_GRANTS_INVOICES = "arTransmitContractsAndGrantsInvoices";
        public static final String CONTRACTS_GRANTS_LOC_REPORT = "contractsGrantsLOCReport";
        public static final String AR_TICKLER_REPORT = "arTicklersReport";
    }

    public static class MultipleValueReturnActions {
        public static final String CONTRACTS_GRANTS_COLLECTION_ACTIVITY_INVOICES = "arContractsGrantsCollectionActivity.do";
        public static final String CONTRACTS_GRANTS_INVOICES = "arContractsGrantsInvoiceSummary.do";
        public static final String GENERATE_DUNNING_LETTERS_SUMMARY = "arGenerateDunningLettersSummary.do";
    }

    public static class UrlActions {
        public static final String CUSTOMER_OPEN_ITEM_REPORT_LOOKUP = "arCustomerOpenItemReportLookup.do";
        public static final String CASH_CONTROL_DOCUMENT = "arCashControl.do";
        public static final String FEDERAL_FINANCIAL_REPORT = "arFederalFinancialReport.do";
        public static final String TICKLER_REPORT = "arTicklersReport.do";
    }

    public static class CostCategoryMaintenanceSections {
        public static final String EDIT_OBJECT_CODES = "EditObjectCodes";
        public static final String EDIT_OBJECT_LEVELS = "EditObjectLevels";
        public static final String EDIT_OBJECT_CONSOLIDATIONS = "EditObjectConsolidations";
    }

    public enum ContractsAndGrantsInvoiceDocumentCreationProcessType {
        BATCH("B", "Batch"), LOC("L", "LOC"), MANUAL("M", "Manual");

        private final String code;
        private final String name;

        ContractsAndGrantsInvoiceDocumentCreationProcessType(final String code, final String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static String getName(final String code) {
            for (final ContractsAndGrantsInvoiceDocumentCreationProcessType type :
                    ContractsAndGrantsInvoiceDocumentCreationProcessType.values()) {
                if (type.getCode().equals(code)) {
                    return type.getName();
                }
            }
            return null;
        }
    }

    public static class PdfReportFonts {
        public static final Font LOC_REVIEW_TITLE_FONT = new Font(Font.TIMES_ROMAN, 18, Font.BOLD);
        public static final Font LOC_REVIEW_HEADER_FONT = new Font(Font.TIMES_ROMAN, 16, Font.BOLD);
        public static final Font LOC_REVIEW_SMALL_BOLD = new Font(Font.TIMES_ROMAN, 14, Font.BOLD);
        public static final Font ENVELOPE_TITLE_FONT = new Font(Font.TIMES_ROMAN, 12, Font.BOLD);
        public static final Font ENVELOPE_SMALL_FONT = new Font(Font.TIMES_ROMAN, 9, Font.NORMAL);
    }

    public static class LOCReviewPdf {
        public static final float LENGTH = 1350f;
        public static final float WIDTH = 595f;
        public static final float RESULTS_TABLE_WIDTH = 1300f;
        public static final float RESULTS_TABLE_CELL_PADDING = 20f;
        public static final int RESULTS_TABLE_COLSPAN = 11;
        public static final int INNER_TABLE_COLUMNS = 8;
        public static final float INNER_TABLE_WIDTH = 1000f;
    }

    public static class InvoiceEnvelopePdf {
        public static final float LENGTH = 650f;
        public static final float WIDTH = 320f;
        public static final float INDENTATION_LEFT = 20f;
    }

    public static class Federal425APdf {
        public static final int NUMBER_OF_SUMMARIES_PER_PAGE = 30;
    }
}
