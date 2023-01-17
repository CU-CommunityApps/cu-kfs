/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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

/**
 * Error Key Constants for KFS-AR.
 */
//CU customization: backport FINP-5292, this file can be removed when we upgrade to the 06/30/2022 version of financials
public final class ArKeyConstants {

    // Customer Invoice Document errors:
    public static final String ERROR_CUSTOMER_INVOICE_DETAIL_TOTAL_AMOUNT_LESS_THAN_OR_EQUAL_TO_ZERO = "error.document.customerInvoiceDocument.invalidCustomerInvoiceDetailTotalAmount";
    public static final String ERROR_CUSTOMER_INVOICE_DETAIL_UNIT_PRICE_LESS_THAN_OR_EQUAL_TO_ZERO = "error.document.customerInvoiceDocument.invalidCustomerInvoiceDetailUnitPrice";
    public static final String ERROR_CUSTOMER_INVOICE_DETAIL_QUANTITY_LESS_THAN_OR_EQUAL_TO_ZERO = "error.document.customerInvoiceDocument.invalidCustomerInvoiceDetailQuantityPrice";
    public static final String ERROR_CUSTOMER_INVOICE_DETAIL_INVALID_ITEM_CODE = "error.document.customerInvoiceDocument.invalidCustomerInvoiceDetailItemCode";
    public static final String ERROR_CUSTOMER_INVOICE_DETAIL_DISCOUNT_AMOUNT_GREATER_THAN_PARENT_AMOUNT = "error.document.customerInvoiceDocument.discountAmountGreaterThanParentAmount";
    public static final String ERROR_CUSTOMER_INVOICE_DETAIL_SYSTEM_INFORMATION_DISCOUNT_DOES_NOT_EXIST = "error.document.customerInvoiceDocument.systemInformationDiscountDoesNotExist";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INVALID_BILLED_BY_CHART_OF_ACCOUNTS_CODE = "error.document.customerInvoiceDocument.invalidBilledByChartOfAccountsCode";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INVALID_BILLED_BY_ORGANIZATION_CODE = "error.document.customerInvoiceDocument.invalidBilledByOrganizationCode";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INVALID_INVOICE_DUE_DATE_MORE_THAN_X_DAYS = "error.document.customerInvoiceDocument.invalidInvoiceDueDateMoreThanXDays";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INVALID_INVOICE_DUE_DATE_BEFORE_OR_EQUAL_TO_BILLING_DATE = "error.document.customerInvoiceDocument.invalidInvoiceDueDateBeforeOrEqualBillingDate";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INVALID_CUSTOMER_NUMBER = "error.document.customerInvoiceDocument.invalidCustomerNumber";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_NO_CUSTOMER_INVOICE_DETAILS = "error.document.customerInvoiceDocument.noCustomerInvoiceDetails";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INVALID_BILLING_PROCESSING_ORGANIZATION_IN_ORG_OPTIONS = "error.document.customerInvoiceDocument.invalidBilingProcessingOrgInOrgOptions";

    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INVALID_SHIP_TO_ADDRESS_IDENTIFIER = "error.document.customerInvoiceDocument.invalidShipToAddressIdentifier";

    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INVALID_BILL_TO_ADDRESS_IDENTIFIER = "error.document.customerInvoiceDocument.invalidBillToAddressIdentifier";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INACTIVE_SHIP_TO_ADDRESS_IDENTIFIER = "error.document.customerInvoiceDocument.inactiveShipToAddressIdentifier";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INACTIVE_BILL_TO_ADDRESS_IDENTIFIER = "error.document.customerInvoiceDocument.inactiveBillToAddressIdentifier";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INVALID_CHART_WITH_NO_AR_OBJ_CD = "error.document.customerInvoiceDocument.invalidChartWithNoARObjectCode";

    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_INVALID_UNIT_OF_MEASURE_CD = "error.document.customerInvoiceDocument.invalidUnitOfMeasureCode";
    public static final String CORRECTED_INVOICE_NOT_FOUND_ERROR = "error.invoice.not.correct";

    // Invoice Recurrence errors/warnings:
    public static final String ERROR_RECURRING_INVOICE_NUMBER_MUST_BE_APPROVED = "error.document.invoiceRecurrenceMaintenance.invoiceMustBeApproved";
    public static final String ERROR_MAINTENANCE_DOCUMENT_ALREADY_EXISTS = "error.document.invoiceRecurrenceMaintenance.maintenanceDocumentsExists";
    public static final String ERROR_INVOICE_RECURRENCE_BEGIN_DATE_IS_REQUIRED = "error.document.invoiceRecurrenceMaintenance.beginDateIsRequired";
    public static final String ERROR_INVOICE_RECURRENCE_BEGIN_DATE_EARLIER_THAN_TODAY = "error.document.invoiceRecurrenceMaintenance.beginDateMustBeEarlierThanToday";
    public static final String ERROR_INVOICE_DOES_NOT_EXIST = "error.document.invoiceRecurrenceMaintenance.invoiceDoesNotExist";
    public static final String ERROR_END_DATE_EARLIER_THAN_BEGIN_DATE = "error.document.invoiceRecurrenceMaintenance.endDateEarlierThanBeginDate";
    public static final String ERROR_END_DATE_OR_TOTAL_NUMBER_OF_RECURRENCES = "error.document.invoiceRecurrenceMaintenance.enterEndDateOrTotalNumberOfRecurrences";
    public static final String ERROR_TOTAL_NUMBER_OF_RECURRENCES_GREATER_THAN_ALLOWED = "error.document.invoiceRecurrenceMaintenance.totalRecurrencesMoreThanAllowed";
    public static final String ERROR_END_DATE_AND_TOTAL_NUMBER_OF_RECURRENCES_NOT_VALID = "error.document.invoiceRecurrenceMaintenance.endDateAndTotalNumberOfRecurrencesNotValid";
    public static final String ERROR_INVOICE_RECURRENCE_INTERVAL_CODE_IS_REQUIRED = "error.document.invoiceRecurrenceMaintenance.intervalCodeIsRequired";
    public static final String ERROR_INVOICE_RECURRENCE_INITIATOR_IS_REQUIRED = "error.document.invoiceRecurrenceMaintenance.initiatorIsRequired";
    public static final String ERROR_INVOICE_RECURRENCE_INITIATOR_DOES_NOT_EXIST = "error.document.invoiceRecurrenceMaintenance.initiatorDoesNotExist";
    public static final String ERROR_INVOICE_RECURRENCE_INITIATOR_IS_NOT_AUTHORIZED = "error.document.invoiceRecurrenceMainitenance.initiatorIsNotAuthorized";
    public static final String ERROR_INVOICE_RECURRENCE_DATA_SUFFICIENCY = "error.document.invoiceRecurrenceMaintenance.notEnoughDataToGenerateRecurrence";
    public static final String ERROR_INVOICE_RECURRENCE_ACTIVE_MUST_BE_TRUE = "error.document.invoiceRecurrenceMaintenance.activeIndicatorMustBeTrue";

    // CustomerInvoiceDocument constants:
    public static final String INVOICE_CLOSE_NOTE_TEXT = "note.document.customerInvoiceDocument.closeInvoiceNote";

    //Batch File System
    public static final String CGINVOICE_CREATION_AWARD_START_DATE_MISSING_ERROR = "error.cginvoice.award.startDate.missing";
    public static final String CGINVOICE_CREATION_AWARD_EXCLUDED_FROM_INVOICING = "error.cginvoice.award.excluded.from.invoicing";
    public static final String CGINVOICE_CREATION_AWARD_LOCB_BILLING_FREQUENCY = "error.cginvoice.award.locb.billing.frequency";
    public static final String CGINVOICE_CREATION_AWARD_INACTIVE_ERROR = "error.cginvoice.award.inactive";
    public static final String CGINVOICE_CREATION_INVOICING_OPTION_MISSING_ERROR = "error.cginvoice.award.missing";
    public static final String CGINVOICE_CREATION_BILLING_FREQUENCY_MISSING_ERROR = "error.cginvoice.billing.missing.frequency";
    public static final String CGINVOICE_CREATION_NO_ACTIVE_ACCOUNTS_ASSIGNED_ERROR = "error.cginvoice.no.active.accounts.assigned";
    public static final String CGINVOICE_CREATION_AWARD_FINAL_BILLED_ERROR = "error.cginvoice.already.billed";
    public static final String LOC_CREATION_ERROR_INVOICE_PAID = "error.loc.invoice.paid";
    public static final String LOC_CREATION_ERROR_NO_INVOICES_TO_PROCESS = "error.loc.no.invoices.to.process";
    public static final String CGINVOICE_CREATION_AWARD_INVALID_BILLING_PERIOD = "error.cginvoice.award.not.eligible.invoice";
    public static final String CGINVOICE_CREATION_AWARD_NO_VALID_MILESTONES = "error.cginvoice.award.not.valid.milestones";
    public static final String CGINVOICE_CREATION_AWARD_NO_VALID_BILLS = "error.cginvoice.award.not.valid.bills";
    public static final String CGINVOICE_CREATION_AWARD_NO_VALID_ACCOUNTS = "error.cgivoice.award.not.valid.accounts";
    public static final String CGINVOICE_CREATION_AWARD_AGENCY_NO_CUSTOMER_RECORD = "error.cginvoice.award.not.valid.customer";
    public static final String CGINVOICE_CREATION_SYS_INFO_OADF_NOT_SETUP = "error.cginvoice.sys.info.not.setup";
    public static final String CGINVOICE_CREATION_ACCOUNT_ON_MULTIPLE_AWARDS = "error.cginvoice.account.on.multiple.awards";
    public static final String CGINVOICE_CREATION_MANUAL_BILLING_FREQUENCY = "error.cginvoice.manual.billing.frequency";

    public static final String MESSAGE_AR_MILESTONE_SCHEDULE_IMPORT_TITLE = "message.ar.milestone.schedule.import.title";
    public static final String MESSAGE_AR_MILESTONE_SCHEDULE_IMPORT_SUCCESSFUL = "message.ar.milestone.schedule.import.successful";
    public static final String ERROR_AR_MILESTONE_SCHEDULE_IMPORT_NONRECOVERABLE = "error.ar.milestone.schedule.import.nonrecoverable";
    public static final String ERROR_AR_MILESTONE_SCHEDULE_IMPORT_SAVE_FAILURE = "error.ar.milestone.schedule.import.save.failure";
    public static final String MESSAGE_AR_PREDETERMINED_BILLING_SCHEDULE_IMPORT_TITLE = "message.ar.predetermined.billing.schedule.import.title";
    public static final String MESSAGE_AR_PREDETERMINED_BILLING_SCHEDULE_IMPORT_SUCCESSFUL = "message.ar.predetermined.billing.schedule.import.successful";
    public static final String ERROR_AR_PREDETERMINED_BILLING_SCHEDULE_IMPORT_NONRECOVERABLE = "error.ar.predetermined.billing.schedule.import.nonrecoverable";
    public static final String ERROR_AR_PREDETERMINED_BILLING_SCHEDULE_IMPORT_SAVE_FAILURE = "error.ar.predetermined.billing.schedule.import.save.failure";

    // Customer Credit Memo errors/warnings:
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DETAIL_INVALID_DATA_INPUT = "error.document.customerCreditMemoDocument.invalidDataInput";
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DETAIL_ITEM_QUANTITY_LESS_THAN_OR_EQUAL_TO_ZERO = "error.document.customerCreditMemoDocument.invalidCustomerCreditMemoItemQuantity";
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DETAIL_ITEM_AMOUNT_LESS_THAN_OR_EQUAL_TO_ZERO = "error.document.customerCreditMemoDocument.invalidCustomerCreditMemoItemAmount";
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DETAIL_ITEM_QUANTITY_GREATER_THAN_INVOICE_ITEM_QUANTITY = "error.document.customerCreditMemoDocument.itemQuantityGreaterThanParentItemQuantity";
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DETAIL_ITEM_AMOUNT_GREATER_THAN_INVOICE_ITEM_AMOUNT = "error.document.customerCreditMemoDocument.itemAmountGreaterThanParentItemAmount";
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DOCUMENT_INVALID_INVOICE_DOCUMENT_NUMBER = "error.document.customerCreditMemoDocument.invalidInvoiceDocumentNumber";
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DOCUMENT_ONE_CRM_IN_ROUTE_PER_INVOICE = "error.document.customerCreditMemoDocument.onlyOneCRMInRoutePerInvoice";
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DOCUMENT__INVOICE_DOCUMENT_NUMBER_IS_REQUIRED = "error.document.customerCreditMemoDocument.invRefNumberIsRequired";
    public static final String WARNING_CUSTOMER_CREDIT_MEMO_DOCUMENT_INVOICE_HAS_DISCOUNT = "warning.documnet.customerCreditMemoDocument.invoiceHasAppliedDiscount";
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DOCUMENT_NO_DATA_TO_SUBMIT = "error.document.customerCreditMemoDocument.noDataToSubmit";
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DOCUMENT_CORRECTED_INVOICE = "error.document.customerCreditMemoDocument.invoiceHasBeenCorrected";
    public static final String ERROR_CUSTOMER_CREDIT_MEMO_DOCUMENT_CORRECTING_INVOICE = "error.document.customerCreditMemoDocument.invoiceCorrectsAnotherInvoice";
    public static final String ERROR_CUSTOMER_INVOICE_DOCUMENT_NOT_FINAL = "error.document.customerInvoiceDocument.notFinal";

    // Cash Control Document errors
    public static final String ERROR_REFERENCE_DOC_NUMBER_CANNOT_BE_NULL_FOR_PAYMENT_MEDIUM_CASH = "error.ar.ReferenceDocNumberCannotBeNullforPaymentMediumCash";
    public static final String ERROR_REFERENCE_DOC_NUMBER_MUST_BE_VALID_FOR_PAYMENT_MEDIUM_CASH = "error.ar.ReferenceDocNumberMustBeValidforPaymentMediumCash";
    public static final String ERROR_PAYMENT_MEDIUM_IS_NOT_VALID = "error.ar.CustomerPaymentMediumIsNotValid";
    public static final String ERROR_ALL_APPLICATION_DOCS_MUST_BE_APPROVED_CANCELED_OR_DISAPPROVED = "error.ar.AllApplicationDocumentsMustBeApprovedCanceledOrDisapproved";
    public static final String ERROR_NO_LINES_TO_PROCESS = "error.ar.NoLinesToProcess";
    public static final String ERROR_LINE_AMOUNT_CANNOT_BE_ZERO = "error.ar.LineAmountCannotBeZero";
    public static final String ERROR_LINE_AMOUNT_CANNOT_BE_NEGATIVE = "error.ar.LineAmountCannotBeNegative";
    public static final String ERROR_CUSTOMER_NUMBER_IS_NOT_VALID = "error.ar.CustomerNumberIsNotValid";
    public static final String ERROR_CANT_CANCEL_CASH_CONTROL_DOC_WITH_ASSOCIATED_APPROVED_PAYMENT_APPLICATION = "error.ar.CantCancelCashControlDocWithAssociatedApprovedPaymentApplicationDoc";
    public static final String ERROR_INVALID_BANK_CODE = "error.ar.invalidBankCode";
    public static final String ERROR_BANK_NOT_ELIGIBLE_FOR_DEPOSIT_ACTIVITY = "error.ar.bankNotEligibleForDepositActivity";
    public static final String ERROR_BANK_CODE_REQUIRED = "error.ar.bankCodeRequired";
    public static final String CASH_CTRL_DOC_CREATED_BY_BATCH = "message.ar.cashControlDocCreatedByLOC";
    public static final String CREATED_BY_CASH_CTRL_DOC = "message.ar.createdByCashControlDocument";
    public static final String DOCUMENT_DELETED_FROM_CASH_CTRL_DOC = "message.ar.documentDeletedFromCashControl";
    public static final String ELECTRONIC_PAYMENT_CLAIM = "message.ar.electronicPaymentClaim";

    // Customer Invoice Writeoff Document errors
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_CHART_WRITEOFF_OBJECT_DOESNT_EXIST = "error.document.customerInvoiceWriteoff.chartWriteoffObjectDoesntExist";
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_FAU_MUST_EXIST = "error.document.customerInvoiceWriteoff.writeoffFAUMustExist";
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_FAU_CHART_MUST_EXIST = "error.document.customerInvoiceWriteoff.chartWriteoffFAUChartMustExist";
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_FAU_ACCOUNT_MUST_EXIST = "error.document.customerInvoiceWriteoff.chartWriteoffFAUAccountMustExist";
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_FAU_OBJECT_CODE_MUST_EXIST = "error.document.customerInvoiceWriteoff.chartWriteoffFAUObjectMustExist";
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_INVOICE_HAS_CREDIT_BALANCE = "error.document.customerInvoiceWriteoff.invoiceHasCreditBalance";
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_NO_INVOICES_SELECTED = "error.document.customerInvoiceWriteoff.noInvoicesSelected";
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_BATCH_SENT = "error.document.customerInvoiceWriteoff.batchSent";
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_CUSTOMER_NOTE_REQUIRED = "error.document.customerInvoiceWriteoff.emptyCustomerNote";
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_CUSTOMER_NOTE_INVALID = "error.document.customerInvoiceWriteoff.invalidCustomerNote";
    public static final String ERROR_CUSTOMER_INVOICE_WRITEOFF_ONE_WRITEOFF_IN_ROUTE_PER_INVOICE = "error.document.customerInvoiceWriteoff.onlyOneWriteoffInRoutePerInvoice";

    public static final String ERROR_FILE_UPLOAD_NO_PDF_FILE_SELECTED_FOR_SAVE = "error.fileUpload.save.noPdfFileSelected";

    public static final String MESSAGE_CG_UPCOMING_MILESTONES_EMAIL_LINE_1 = "message.cg.upcoming.milestones.email.line1";
    public static final String MESSAGE_CG_UPCOMING_MILESTONES_EMAIL_LINE_2 = "message.cg.upcoming.milestones.email.line2";

    // Kim Type Services error messages
    public static final String ERROR_BILLINGCHART_OR_BILLINGORG_NOTEMPTY_ALL_REQUIRED = "error.billingchart.or.billingorg.notempty.all.required";
    public static final String ERROR_EITHER_BILLINGCHART_OR_PROCESSCHART_REQUIRED_NOT_BOTH = "error.either.billingchart.or.processchart.required.not.both";
    public static final String ERROR_PROCESSCHART_OR_PROCESSORG_NOTEMPTY_ALL_REQUIRED = "error.processchart.or.processorg.notempty.all.required";
    public static final String ERROR_STARTLETTER_AFTER_ENDLETTER = "error.startletter.after.endletter";
    public static final String ERROR_STARTLETTER_OR_ENDLETTER_NOTEMPTY_ALL_REQUIRED = "error.startletter.or.endletter.notempty.all.required";

    public static final String ERROR_AWARD_DOES_NOT_EXIST = "error.award.does.not.exist";
    public static final String ERROR_AWARD_MILESTONE_SCHEDULE_EXISTS = "error.cg.award.milestone.schedule.exists";
    public static final String ERROR_AWARD_MILESTONE_SCHEDULE_INCORRECT_BILLING_FREQUENCY = "error.cg.award.milestone.schedule.incorrect.billing.frequency";
    public static final String ERROR_AWARD_PREDETERMINED_BILLING_SCHEDULE_EXISTS = "error.cg.award.predetermined.billing.schedule.exists";
    public static final String ERROR_AWARD_PREDETERMINED_BILLING_SCHEDULE_INCORRECT_BILLING_FREQUENCY = "error.cg.award.predetermined.billing.schedule.incorrect.billing.frequency";
    public static final String ERROR_DUPLICATE_BILL_NUMBER = "error.duplicate.bill.number";
    public static final String ERROR_DUPLICATE_MILESTONE_NUMBER = "error.duplicate.milestone.number";
    public static final String ERROR_BILL_TOTAL_EXCEEDS_AWARD_TOTAL = "error.bill.total.exceeds.award.total";
    public static final String ERROR_MILESTONE_SCHEDULE_ACCOUNT_DOES_NOT_EXIST_ON_AWARD = "error.milestone.schedule.account.does.not.exist.on.award";
    public static final String ERROR_MILESTONE_ACTUAL_COMPLETION_DATE_IN_FUTURE = "error.milestone.actual.completion.date.in.future";
    public static final String ERROR_MILESTONE_TOTAL_EXCEEDS_AWARD_TOTAL = "error.milestone.total.exceeds.award.total";
    public static final String ERROR_PREDETERMINED_BILLING_SCHEDULE_ACCOUNT_DOES_NOT_EXIST_ON_AWARD = "error.predetermined.billing.schedule.account.does.not.exist.on.award";

    public static final String ERROR_REPORT_INVALID_CALCULATED_PATTERN = "error.report.invalid.calculated.pattern";

    public static final String NO_FUND_MANAGER_PRINCIPAL_NAME_FOUND = "error.no.fund.manager.principal.name.found";

    // messages for Transmit Contracts & Grants Invoices
    public static final String NO_PRINCIPAL_NAME_FOUND = "error.no.principal.name.found";
    public static final String INVOICE_EMAILS_SENT = "message.invoice.emails.sent";
    public static final String ERROR_SENDING_INVOICE_EMAILS = "error.sending.invoice.emails";
    public static final String INVOICES_PRINT_SUCCESSFULL = "message.invoices.print.successfull";
    public static final String INVOICES_PRINT_UNSUCCESSFULL = "error.invoices.print.unsuccessful";

    public static final String CONTRACTS_REPORTS_AGING_REPORT_TITLE = "contracts.grants.aging.report.title";

    public static final String CGINVOICE_EMAIL_SUBJECT = "cginvoice.email.subject";
    public static final String CGINVOICE_EMAIL_SUBJECT_NO_GRANT_NUMBER = "cginvoice.email.subject.nograntnumber";
    public static final String CGINVOICE_EMAIL_BODY = "cginvoice.email.body";

    // messages for LOC pdf
    public static final String LOC_REVIEW_PDF_TITLE = "locreview.pdf.title";
    public static final String LOC_REVIEW_PDF_HEADER_FUND_GROUP_CODE = "locreview.pdf.header.fund.group.code";
    public static final String LOC_REVIEW_PDF_HEADER_FUND_CODE = "locreview.pdf.header.fund.code";
    public static final String LOC_REVIEW_PDF_HEADER_DOCUMENT_NUMBER = "locreview.pdf.header.document.number";
    public static final String LOC_REVIEW_PDF_HEADER_APP_DOC_STATUS = "locreview.pdf.header.application.document.status";
    public static final String LOC_REVIEW_PDF_HEADER_DOCUMENT_INITIATOR = "locreview.pdf.header.initiator.principal.name";
    public static final String LOC_REVIEW_PDF_HEADER_DOCUMENT_CREATE_DATE = "locreview.pdf.header.create.date";
    public static final String LOC_REVIEW_PDF_SUBHEADER_AWARDS = "locreview.pdf.subheader.awards";

    public static final String ERROR_LOC_REVIEW_FUND_OR_FUND_GROUP_REQUIRED = "error.locreview.fund.or.fund.group.required";
    public static final String ERROR_LOC_REVIEW_ONLY_ONE_FUND_OR_FUND_GROUP = "error.locreview.only.one.fund.or.fund.group";

    // Final Billed Indicator Validation error messages
    public static final String ERROR_FINAL_BILLED_INDICATOR_INVOICE_NOT_FINAL = "error.final.billed.indicator.invoice.not.final";
    public static final String ERROR_FINAL_BILLED_INDICATOR_INVOICE_NOT_MARKED_FINAL_BILL = "error.final.billed.indicator.invoice.not.marked.final.bill";
    public static final String ERROR_FINAL_BILLED_INDICATOR_INVALID_INVOICE = "error.final.billed.indicator.invalid.invoice";
    public static final String ERROR_FINAL_BILLED_INDICATOR_NO_INVOICE = "error.final.billed.indicator.no.invoice";

    public static final String INVOICE_ADDRESS_PDF_WATERMARK_FINAL = "invoice.address.pdf.final.watermark";
    public static final String INVOICE_ADDRESS_PDF_FINAL_NOTE = "invoice.address.pdf.final.note";
    public static final String INVOICE_ADDRESS_TRANSMISSION_NOTE = "invoice.address.transmission.note";
    public static final String INVOICE_ADDRESS_MANUAL_TRANSMISSION_NOTE = "invoice.address.manual.transmission.note";

    public static final String CONTRACTS_GRANTS_INVOICE_DETAILS_INDIRECT_SUBTOTAL_LABEL = "contracts.grants.invoice.details.indirect.subtotal.label";
    public static final String CONTRACTS_GRANTS_INVOICE_DETAILS_DIRECT_SUBTOTAL_LABEL = "contracts.grants.invoice.details.direct.subtotal.label";
    public static final String CONTRACTS_GRANTS_INVOICE_DETAILS_TOTAL_LABEL = "contracts.grants.invoice.details.total.label";
    public static final String CONTRACTS_GRANTS_INVOICE_DETAILS_PREVIOUSLY_BILLED_LABEL = "contracts.grants.invoice.details.previously.billed.label";
    public static final String CONTRACTS_GRANTS_INVOICE_DETAILS_TOTAL_INVOICE_LABEL = "contracts.grants.invoice.details.total.invoice.label";

    // Suspension Category Description Prefix
    public static final String INVOICE_DOCUMENT_SUSPENSION_CATEGORY = "invoice.document.suspension.category.";

    public static final String WARNING_SUSPENSION_CATEGORIES_PRESENT = "warning.suspension.categories.present";

    public static final String ERROR_DOCUMENT_COST_CATEGORY_NO_DETAILS = "error.document.cost.category.no.details";
    public static final String ERROR_DOCUMENT_COST_CATEGORY_OBJECT_CODE_NOT_UNIQUE = "error.document.cost.category.object.code.not.unique";
    public static final String ERROR_DOCUMENT_COST_CATEGORY_OBJECT_LEVEL_NOT_UNIQUE = "error.document.cost.category.object.level.not.unique";
    public static final String ERROR_DOCUMENT_COST_CATEGORY_OBJECT_CONSOLIDATION_NOT_UNIQUE = "error.document.cost.category.object.consolidation.not.unique";
    public static final String ERROR_DOCUMENT_COST_CATEGORY_SORT_CODE_NOT_UNIQUE = "error.document.cost.category.sort.code.not.unique";

    public static final String ERROR_DOCUMENT_COLLECTION_ACTIVITY_NO_INVOICE_SELECTED = "error.document.collectionActvity.noInvoiceSelected";

    public static final String CONTRACTS_GRANTS_INVOICE_DOCUMENT_DESCRIPTION_FORMAT = "contracts.grants.invoice.document.description.format";

    //ContractsGrantsInvoiceObjectCode
    public static final String ERROR_CONTRACTS_GRANTS_INVOICE_OBJECT_ONE_FUND_FIELD_REQUIRED = "error.contracts.grants.invoice.object.one.fund.field.required";
    public static final String ERROR_CONTRACTS_GRANTS_INVOICE_OBJECT_ONLY_ONE_FUND_FIELD_ALLOWED = "error.contracts.grants.invoice.object.only.one.fund.field.allowed";
    public static final String ERROR_CONTRACTS_GRANTS_INVOICE_OBJECT_CODE_ALREADY_USED = "error.contracts.grants.invoice.object.code.already.used";

    public static final String WARNING_LOC_REVIEW_DOCUMENT_AMOUNT_TO_DRAW_EXCEEDS_MAXIMUM
            = "warning.contractsGrantsLetterOfCreditReviewDocument.amountToDrawExceedsMaximum";

    public static final String LOC_REVIEW_DOCUMENT_AMOUNT_TO_DRAW_EXCEEDED_NOTE =
            "contractsGrantsLetterOfCreditReviewDocument.amountToDraw.exceeded.note";
    /**
     * Private Constructor since this is a constants class that should never be instantiated.
     */
    private ArKeyConstants() {
    }

    // Organization Accounting Defaults errors
    public static final class OrganizationAccountingDefaultErrors {
        public static final String WRITE_OFF_OBJECT_CODE_INVALID = "error.document.organizationAccountingDefaultMaintenance.writeOffObjectCodeInvalid";
        public static final String LATE_CHARGE_OBJECT_CODE_INVALID = "error.document.organizationAccountingDefaultMaintenance.lateChargeObjectCodeInvalid";
        public static final String DEFAULT_INVOICE_FINANCIAL_OBJECT_CODE_INVALID = "error.document.organizationAccountingDefaultMaintenance.defaultInvoiceFinancialObjectCodeInvalid";
        public static final String DEFAULT_INVOICE_FINANCIAL_OBJECT_CODE_INVALID_RESTRICTED = "error.document.organizationAccountingDefaultMaintenance.defaultInvoiceFinancialObjectCodeInvalidRestricted";
        public static final String DEFAULT_CHART_OF_ACCOUNTS_REQUIRED_IF_DEFAULT_OBJECT_CODE_EXISTS = "error.document.organizationAccountingDefaultMaintenance.defaultInvoiceChartOfAccountsCodeMustExist";
        public static final String ERROR_WRITEOFF_OBJECT_CODE_REQUIRED = "error.document.customerInvoiceDocument.writeoffFinancialObjectCodeRequired";
        public static final String ERROR_WRITEOFF_CHART_OF_ACCOUNTS_CODE_REQUIRED = "error.document.customerInvoiceDocument.writeoffChartOfAccountsCodeRequired";
        public static final String ERROR_WRITEOFF_ACCOUNT_NUMBER_REQUIRED = "error.document.customerInvoiceDocument.writeoffAccountNumberRequired";
    }

    // System Information errors
    public static final class SystemInformation {
        public static final String ERROR_CLEARING_ACCOUNT_INACTIVE = "error.SystemInformation.clearingAccountInactive";
        public static final String ERROR_LOCKBOX_NUMBER_NOT_UNIQUE = "error.SystemInformation.lockboxNumberNotUnique";
    }

    // Invoice Item Code errors
    public static final class InvoiceItemCode {
        public static final String NONPOSITIVE_ITEM_DEFAULT_PRICE = "error.invoiceItemCode.nonPositiveNumericValue";
        public static final String NONPOSITIVE_ITEM_DEFAULT_QUANTITY = "error.invoiceItemCode.nonPositiveNumericValue";
        public static final String ORG_OPTIONS_DOES_NOT_EXIST_FOR_CHART_AND_ORG = "error.invoiceItemCode.orgOptionsDoesNotExistForChartAndOrg";
        public static final String ERROR_INVALID_CHART_OF_ACCOUNTS_CODE = "error.invoiceItemCode.errorInvalidChartOfAccountsCode";
        public static final String ERROR_INVALID_ORGANIZATION_CODE = "error.invoiceItemCode.errorInvalidOrganizationCode";
    }

    // Customer Type errors
    public static class CustomerTypeConstants {
        public static final String ERROR_CUSTOMER_TYPE_DUPLICATE_VALUE = "error.document.customerType.duplicateValue";
    }

    // Customer Load messages
    public static final class CustomerLoad {
        public static final String MESSAGE_BATCH_UPLOAD_XML_TITLE_CUSTOMER = "message.ar.customerLoad.batchUpload.xml.title";
        public static final String MESSAGE_BATCH_UPLOAD_CSV_TITLE_CUSTOMER = "message.ar.customerLoad.batchUpload.csv.title";
    }

    // Customer errors
    public static class CustomerConstants {
        public static final String MESSAGE_CUSTOMER_WITH_SAME_NAME_EXISTS = "message.document.customerMaintenance.customerWithSameNameExists";
        public static final String GENERATE_CUSTOMER_QUESTION_ID = "GenerateCustomerQuestionID";
        public static final String ERROR_AT_LEAST_ONE_ADDRESS = "error.document.customer.addressRequired";
        public static final String ERROR_ONLY_ONE_PRIMARY_ADDRESS = "error.document.customer.oneAndOnlyOnePrimaryAddressRequired";
        public static final String ERROR_CUSTOMER_NAME_LESS_THAN_THREE_CHARACTERS = "error.document.customer.customerNameLessThanThreeCharacters";
        public static final String ERROR_CUSTOMER_NAME_NO_SPACES_IN_FIRST_THREE_CHARACTERS = "error.document.customer.customerNameNoSpacesInFirstThreeCharacters";
        public static final String CUSTOMER_ADDRESS_TYPE_CODE_PRIMARY = "P";
        public static final String CUSTOMER_ADDRESS_TYPE_CODE_ALTERNATE = "A";
        public static final String CUSTOMER_ADDRESS_TYPE_CODE_US = "US";
        public static final String ERROR_CUSTOMER_ADDRESS_STATE_CODE_REQUIRED_WHEN_COUNTTRY_US = "error.document.customer.stateCodeIsRequiredWhenCountryUS";
        public static final String ERROR_CUSTOMER_ADDRESS_ZIP_CODE_REQUIRED_WHEN_COUNTTRY_US = "error.document.customer.zipCodeIsRequiredWhenCountryUS";
        public static final String ERROR_TAX_NUMBER_IS_REQUIRED = "error.document.customer.taxNumberRequired";
        public static final String ACTIONS_REPORT = "report";
        public static final String ERROR_CUSTOMER_ADDRESS_END_DATE_MUST_BE_FUTURE_DATE = "error.document.customer.endDateMustBeFutureDate";
        public static final String ERROR_CUSTOMER_ADDRESS_END_DATE_MUST_BE_CURRENT_OR_FUTURE_DATE = "error.document.customer.endDateMustBeCurrenOrFutureDate";
        public static final String ERROR_CUSTOMER_PRIMARY_ADDRESS_MUST_HAVE_FUTURE_END_DATE = "error.document.customer.primaryAddressMustHaveFutureEndDate";
        public static final String ERROR_CUSTOMER_AT_LEAST_ONE_EMAIL_REQUIRED = "error.document.customer.atLeastOneEmailRequired";
        public static final String ERROR_CUSTOMER_DUPLICATE_EMAIL_FOR_ADDRESS = "error.document.customer.duplicateEmailForAddress";
        public static final String ERROR_CUSTOMER_METHOD_OF_INVOICE_TRANSMISSION_REQUIRED = "error.document.customer.methodOfInvoiceTransmissionRequired";
    }

    // Organization Options errors
    public static class OrganizationOptionsErrors {
        public static final String SYS_INFO_DOES_NOT_EXIST_FOR_PROCESSING_CHART_AND_ORG = "error.document.organizationOptions.sysInfoDoesNotExistForProcessingChartAndOrg";
        public static final String ERROR_ORG_OPTIONS_ZIP_CODE_REQUIRED = "error.document.organizationOptions.orgOptionsZipCodeRequired";
    }

    public static class PaymentApplicationDocumentErrors {
        public static final String AMOUNT_TO_BE_APPLIED_CANNOT_BE_ZERO = "error.document.paymentApplication.amountToBeAppliedCannotBeZero";
        public static final String AMOUNT_TO_BE_APPLIED_EXCEEDS_AMOUNT_OUTSTANDING = "error.document.paymentApplication.amountToBeAppliedExceedsAmountOutstanding";
        public static final String AMOUNT_TO_BE_APPLIED_MUST_BE_GREATER_THAN_ZERO = "error.document.paymentApplication.amountToBeAppliedCannotBeZero";
        public static final String FULL_AMOUNT_NOT_APPLIED = "error.document.paymentApplication.fullAmountNotApplied";
        public static final String AMOUNT_TO_BE_APPLIED_MUST_BE_POSTIIVE = "error.document.paymentApplication.amountToBeAppliedMustBePositive";
        public static final String NON_AR_CHART_INVALID = "error.document.paymentApplication.nonArLineChartInvalid";
        public static final String NON_AR_ACCOUNT_INVALID = "error.document.paymentApplication.nonArLineAccountInvalid";
        public static final String NON_AR_SUB_ACCOUNT_INVALID = "error.document.paymentApplication.nonArLineSubAccountInvalid";
        public static final String NON_AR_OBJECT_CODE_INVALID = "error.document.paymentApplication.nonArLineObjectCodeInvalid";
        public static final String NON_AR_SUB_OBJECT_CODE_INVALID = "error.document.paymentApplication.nonArLineSubObjectCodeInvalid";
        public static final String NON_AR_PROJECT_CODE_INVALID = "error.document.paymentApplication.nonArLineProjectCodeInvalid";
        public static final String NON_AR_AMOUNT_REQUIRED = "error.document.paymentApplication.nonArLineAmountRequired";
        public static final String CANNOT_APPLY_MORE_THAN_CASH_CONTROL_TOTAL_AMOUNT = "error.document.paymentApplication.quickAppliedExceedsCashControlTotalAmount";
        public static final String CANNOT_APPLY_MORE_THAN_BALANCE_TO_BE_APPLIED = "error.document.paymentApplication.AppliedAmountExceedsBalanceToBeApplied";
        public static final String UNAPPLIED_AMOUNT_CANNOT_EXCEED_AVAILABLE_AMOUNT = "error.document.paymentApplication.unappliedAmountCannotExceedAvailableAmount";
        public static final String UNAPPLIED_AMOUNT_CANNOT_EXCEED_BALANCE_TO_BE_APPLIED = "error.document.paymentApplication.unappliedAmountCannotExceedBalanceToBeApplied";
        public static final String UNAPPLIED_AMOUNT_CANNOT_BE_EMPTY_OR_ZERO = "error.document.paymentApplication.unappliedAmountCannotBeEmptyOrZero";
        public static final String UNAPPLIED_AMOUNT_CANNOT_BE_NEGATIVE = "error.document.paymentApplication.unappliedAmountCannotBeNegative";
        public static final String NON_AR_AMOUNT_EXCEEDS_BALANCE_TO_BE_APPLIED = "error.document.paymentApplication.nonArLineAmountExceedsBalanceToBeApplied";
        public static final String CANNOT_QUICK_APPLY_ON_INVOICE_WITH_ZERO_OPEN_AMOUNT = "error.document.paymentApplication.cannotQuickApplyOnInvoiceWithZeroOpenAmount";
        public static final String ENTERED_INVOICE_CUSTOMER_NUMBER_INVALID = "error.document.paymentApplication.enteredInvoiceCustomerNumberInvalid";
    }

    public static class PaymentApplicationAdjustmentDocumentErrors {
        public static final String AMOUNT_TO_BE_APPLIED_EXCEEDS_AMOUNT_OUTSTANDING = "error.document.paymentApplicationAdjustment.amountToBeAppliedExceedsAmountOutstanding";
        public static final String AMOUNT_TO_BE_APPLIED_EXCEEDS_AMOUNT_OUTSTANDING_TITLE = "error.modal.paymentApplicationAdjustment.amountToBeAppliedExceedsAmountOutstanding.title";
        public static final String AMOUNT_TO_BE_APPLIED_EXCEEDS_AMOUNT_OUTSTANDING_MESSAGE = "error.modal.paymentApplicationAdjustment.amountToBeAppliedExceedsAmountOutstanding.message";
        public static final String NON_AR_ACCOUNTING_LINE_MISSING_FIELD = "error.document.paymentApplicationAdjustment.nonArAccountingLineMissingField";
        public static final String NON_AR_ACCOUNTING_LINE_IS_CLOSED = "error.document.paymentApplicationAdjustment.nonArAccountingLineIsClosed";
        public static final String NON_AR_ACCOUNTING_LINE_VALIDATION_ERROR_MESSAGE = "error.modal.paymentApplicationAdjustment.nonArAccountingLineValidationError.message";
        public static final String NON_AR_ACCOUNTING_LINE_VALIDATION_ERROR_TITLE = "error.modal.paymentApplicationAdjustment.nonArAccountingLineValidationError.title";
        public static final String NON_AR_ACCOUNTING_LINE_INVALID_ACCOUNT = "error.document.paymentApplicationAdjustment.nonArAccountingLineInvalidAccount";
        public static final String NON_AR_ACCOUNTING_LINE_INVALID_SUB_ACCOUNT = "error.document.paymentApplicationAdjustment.nonArAccountingLineInvalidSubAccount";
        public static final String NON_AR_ACCOUNTING_LINE_INVALID_OBJECT = "error.document.paymentApplicationAdjustment.nonArAccountingLineInvalidObject";
        public static final String NON_AR_ACCOUNTING_LINE_INVALID_SUB_OBJECT = "error.document.paymentApplicationAdjustment.nonArAccountingLineInvalidSubObject";
        public static final String OPEN_AMOUNT_IS_NON_ZERO_TITLE = "error.modal.paymentApplicationAdjustment.openAmountIsNonZero.title";
        public static final String OPEN_AMOUNT_IS_NON_ZERO_MESSAGE = "error.modal.paymentApplicationAdjustment.openAmountIsNonZero.message";
    }

    public static class ContractsGrantsCollectionActivityDocumentConstants {
        public static final String TITLE_PROPERTY = "message.inquiry.contractsGrantsCollectionActivity.title";
        public static final String VIEW_HISTORY = "message.contractsGrantsCollectionActivityReport.view.hisory.report";
    }

    // Contracts & Grants Invoice constants and errors
    public static class ContractsGrantsInvoiceConstants {
        public static final String MESSAGE_CONTRACTS_GRANTS_INVOICE_BATCH_SENT = "message.document.contractsGrantsInvoice.batchSent";
        public static final String ERROR_NO_AWARDS_RETRIEVED = "error.document.no.awards.retrieved";
        public static final String ERROR_AWARDS_INVALID = "error.document.awards.invalid";
        public static final String ERROR_SOME_AWARDS_INVALID = "error.document.some.awards.invalid";
        public static final String ERROR_TEMPLATE_REQUIRED = "error.document.contractsGrantsInvoice.template.code.required";
        public static final String ERROR_INVOICE_DETAIL_ACCOUNT_OBJECT_CODE_FINANCIAL_OBJECT_CODE_REQUIRED = "error.document.contractsGrantsInvoice.invoiceDetailAccountObjectCode.financialObjectCode.required";
        public static final String ERROR_INVOICE_TOTAL_AMOUNT_INVALID = "error.document.contractsGrantsInvoice.invoice.total.amount.invalid";
        public static final String ERROR_ONE_TRANSMISSION_DETAIL_QUEUE_REQUIRED = "error.one.transmission.detail.queue.required";
        public static final String ERROR_NO_MATCHING_CONTRACT_GRANTS_INVOICE_OBJECT_CODE = "error.contracts.grants.invoice.missing.cgbi.object.code";
        public static final String ERROR_SUBMIT_NO_MATCHING_CONTRACT_GRANTS_INVOICE_OBJECT_CODE = "error.contracts.grants.invoice.submission.missing.cgbi.object.code";
        public static final String MESSAGE_CONTRACTS_GRANTS_INVOICE_TRANSMISSION_QUEUED = "message.document.contracts.grants.invoice.transmission.queued";
        public static final String MESSAGE_CONTRACTS_GRANTS_INVOICE_TRANSMISSION_UNQUEUED = "message.document.contracts.grants.invoice.transmission.unqueued";
        //CU customization: backport FINP-5292
        public static final String PROMPT_FINAL_BILL_INDICATOR = "prompt.document.cinv.final.bill.indicator";
    }

    // contracts & grants invoice create document constants
    public static class ContractsGrantsInvoiceCreateDocumentConstants {
        public static final String NO_ORGANIZATION_ON_AWARD = "error.invoice.create.document.no.organization.on.award";
        public static final String NON_BILLABLE = "error.invoice.create.document.non.billable";
        public static final String NO_CONTROL_ACCOUNT = "error.invoice.create.document.no.control.account";
        public static final String CONTROL_ACCOUNT_NON_BILLABLE = "error.invoice.create.document.control.account.non.billable";
        public static final String BILL_BY_CONTRACT_LACKS_CONTROL_ACCOUNT = "error.invoice.create.document.bill.by.contract.control.no.control.account";
        public static final String BILL_BY_CONTRACT_VALID_ACCOUNTS = "error.invoice.create.document.bill.by.contract.control.no.accounts";
        public static final String DIFFERING_CONTROL_ACCOUNTS = "error.invoice.create.document.different.control.accounts";
        public static final String NOT_ALL_BILLABLE_ACCOUNTS = "error.invoice.create.document.not.all.billable.accounts";
        public static final String NO_AWARD = "error.invoice.create.document.no.award";
        public static final String NO_CHART_OR_ORG = "error.invoice.create.document.no.chart.or.org";
    }

    public static class DunningCampaignConstantsAndErrors {
        public static final String MESSAGE_DUNNING_CAMPAIGN_BATCH_NOT_SENT = "message.document.dunningCampaign.batchNotSent";

    }

    public static class DunningLetterDistributionErrors {
        public static final String ERROR_DAYS_PAST_DUE_DUPLICATE = "error.document.daysPastDue.duplicateValue";
    }

    // Award Constants and errors
    public static class AwardConstants {
        public static final String ERROR_NO_CTRL_ACCT = "error.cg.no.control.account";
        public static final String ERROR_MULTIPLE_CTRL_ACCT = "error.cg.multiple.control.account";
    }

    public static class TemplateUploadErrors {
        public static final String ERROR_TEMPLATE_UPLOAD_NO_TEMPLATE = "error.template.upload.no.template";
        public static final String ERROR_TEMPLATE_UPLOAD_NO_TEMPLATE_TYPE = "error.template.upload.no.template.type";
        public static final String ERROR_TEMPLATE_UPLOAD_USER_NOT_AUTHORIZED = "error.template.upload.user.not.authorized";
        public static final String ERROR_TEMPLATE_UPLOAD_TEMPLATE_NOT_AVAILABLE = "error.template.upload.template.not.available";
        public static final String ERROR_TEMPLATE_UPLOAD_INVALID_FILE_TYPE = "error.template.upload.invalid.file.type";
    }
}
