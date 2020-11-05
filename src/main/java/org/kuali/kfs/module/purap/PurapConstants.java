/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.module.purap;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.businessobject.AvailabilityMatrix;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderVendorQuote;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.PurchasingAccountsPayableDocumentBase;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

/**
 * Holds constants for PURAP.
 */
public final class PurapConstants {

    public static final String PURAP_NAMESPACE = "KFS-PURAP";

    public static final KualiDecimal HUNDRED = new KualiDecimal(100);


    public static final String B2B_PUNCHBACK_METHOD_TO_CALL = "returnFromShopping";

    public static final String PDP_PURAP_EXTRACT_FILE_NAME = "extr_fr_purap";

    public static final String NOTE_TAB_WARNING = "noteWarning";

    public static final String QUESTION_INDEX = "questionIndex";
    public static final String REMOVE_ACCOUNTS_QUESTION = "RemoveAccounts";
    public static final String CLEAR_COMMODITY_CODES_QUESTION = "ClearCommodityCodes";
    public static final String QUESTION_ROUTE_DOCUMENT_TO_COMPLETE = "Completing this document will remove it from your Action List.<br/><br/>  Are you sure you want to continue?";
    public static final String QUESTION_REQUISITON_ROUTE_WITHOUT_ACCOUNTING_LINES = "This document is missing accounting lines on one or more items. Are you sure you want to continue?";
    public static final String REQUISITION_ACCOUNTING_LINES_QUESTION = "RequisitionAccountingLinesQuestion";
    public static final String QUESTION_REMOVE_ACCOUNTS = "question.document.purap.removeAccounts";
    public static final String QUESTION_CLEAR_ALL_COMMODITY_CODES = "question.document.pur.clearCommodityCodes";
    // TODO - This constant is referenced, but has no corresponding value in ApplicationResources.properties????
    public static final String CONFIRM_CHANGE_DFLT_RVNG_ADDR = "confirm.change.dflt.rcvng.addr";
    public static final String CONFIRM_CHANGE_DFLT_RVNG_ADDR_TXT = "Setting this receiving address to be default will unset the current default address. Do you want to proceed?";

    public static final String REQ_REASON_NOT_APO = "ORDER ROUTED TO PURCHASING FOR PROCESSING: ";
    public static final String REQ_UNABLE_TO_CREATE_NOTE = "Unable to create a note on this document.";
    public static final String REQ_NO_ACCOUNTING_LINES="message.requisition.no.accounting.lines";

    public static final String TRADE_IN_OBJECT_CODE_FOR_CAPITAL_ASSET_OBJECT_SUB_TYPE = "7070";
    public static final String TRADE_IN_OBJECT_CODE_FOR_CAPITAL_LEASE_OBJECT_SUB_TYPE = "7099";

    // PDF KFSConstants
    public static final String IMAGE_TEMP_PATH = "PDF_IMAGE_TEMP_PATH";
    public static final String PDF_DIRECTORY = "PDF_DIRECTORY";
    public static final String STATUS_INQUIRY_URL = "PDF_STATUS_INQUIRY_URL";
    public static final String PURCHASING_DIRECTOR_IMAGE_PREFIX = "PDF_DIRECTOR_IMAGE_PREFIX";
    public static final String PURCHASING_DIRECTOR_IMAGE_EXTENSION = "PDF_DIRECTOR_IMAGE_EXTENSION";
    public static final String CONTRACT_MANAGER_IMAGE_PREFIX = "PDF_IMAGE_PREFIX";
    public static final String CONTRACT_MANAGER_IMAGE_EXTENSION = "PDF_IMAGE_EXTENSION";
    public static final String LOGO_IMAGE_PREFIX = "PDF_LOGO_IMAGE_PREFIX";
    public static final String LOGO_IMAGE_EXTENSION = "PDF_LOGO_IMAGE_EXTENSION";
    public static final String PDF_IMAGES_AVAILABLE_INDICATOR = "PDF_IMAGES_AVAILABLE_IND";
    public static final String PDF_IMAGE_LOCATION_URL = "PDF_IMAGE_LOCATION_URL";

    public static final String TAX_RECALCULATION_INFO = "TaxRecalculationQuestion";
    public static final String TAX_RECALCULATION_QUESTION = "The postal code of the delivery address has changed.[br]Selecting \"Yes\" will submit the document without recalculating the taxes.[br]Selecting \"No\" will return you to the document so that the taxes can be recalculated.[br]The \"clear all\" button can be used to clear all tax amounts which will force taxes to be recalculated upon submission.";
    public static final String PURCHASE_ORDER_TRANSMISSION_METHOD = "PMTM";

    public static final String REQ_B2B_ALLOW_COPY_DAYS = "5";
    public static final String B2B_VENDOR_CONTRACT_NOT_FOUND_ERROR_MESSAGE = "The vendor whose shopping cart you are attempting to return does not have an active contract for your organization.";
    public static final String B2B_URL_STRING = "&channelUrl=b2b.do?methodToCall=shopCatalogs";

    public static String[] AUTO_CLOSE_EXCLUSION_VNDR_CHOICE_CODES = {VendorChoice.SUBCONTRACT};

    public static Integer APO_CONTRACT_MANAGER = new Integer(99);

    // Requisition/Purchase Order Tab Errors
    public static final String DELIVERY_TAB_ERRORS = "document.delivery*";
    public static final String DETAIL_TAB_ERRORS = "document";
    public static final String VENDOR_ERRORS = "document.vendor*,document.purchaseOrderVendorChoiceCode,document.alternateVendorName,document.shipmentReceivedDate,document.vendorContractEndDate";
    public static final String ADDITIONAL_TAB_ERRORS = "document.requestor*,document.institution*,document.purchaseOrderTransmissionMethodCode,document.purchaseOrderCostSourceCode,document.purchaseOrderTotalLimit";
    public static final String ITEM_TAB_ERRORS = "document.item*,newPurchasingItemLine*,itemQuantity,document.grandTotal,accountDistributionnewSourceLine*,distributePurchasingCommodityCode,accountNumber*,chartOfAccountsCode*";
    public static final String LINEITEM_TAB_ERRORS = "document.item*,newLineItemReceivingItemLine*";
    public static final String ITEM_TAB_ERROR_PROPERTY = ITEM_TAB_ERRORS;
    public static final String ACCOUNT_SUMMARY_TAB_ERRORS = "document.accountSummary*";
    public static final String ACCOUNT_DISTRIBUTION_ERROR_KEY = "accountDistributionnewSourceLine";
    public static final String RELATED_DOCS_TAB_ERRORS = "";
    public static final String PAYMENT_HISTORY_TAB_ERRORS = "";
    public static final String PAYMENT_INFO_ERRORS = "document.paymentInfo";
    public static final String PAYMENT_INFO_TAB_ERRORS = "document.paymentInfo*,document.purchaseOrderBeginDate,document.purchaseOrderEndDate,document.finalPaymentAmount,document.initialPaymentAmount,document.recurringPaymentAmount";
    public static final String CAPITAL_ASSET_TAB_ERRORS = "document.purchasingCapitalAsset*,newPurchasingItemCapitalAssetLine*,newPurchasingCapitalAssetLocationLine*,document.capitalAssetSystemStateCode";
    public static final String SPLIT_PURCHASE_ORDER_TAB_ERRORS = "document.splitPurchaseOrder";
    public static final String ASSIGN_SENSITIVE_DATA_TAB_ERRORS = "document.assignSensitiveData";
    public static final String STIPULATIONS_TAB_ERRORS = "document.purchaseOrderVendorStipulation*,newPurchaseOrderVendorStipulationLine*";
    // PO/Quotes Tab Constants
    public static final String QUOTE_TAB_ERRORS = "document.quote*,quote*,purchaseOrderVendorQuotes*,newPurchaseOrderVendorQuote*,document.purchaseOrderVendorQuote*";
    //PO Number Warning
    public static final String WARNING_PURCHASEORDER_NUMBER_DONT_DISCLOSE = "warning.purchaseorder.number.dont.disclose";

    // Assign Contract Manager
    public static final String ASSIGN_CONTRACT_MANAGER_DEFAULT_DESC = "Contract Manager Assigned";
    public static final String ASSIGN_CONTRACT_MANAGER_TAB_ERRORS = "document.unassignedRequisition*,document.contractManagerAssignmentDetail*";

    // Item constants
    public static final int DOLLAR_AMOUNT_MIN_SCALE = 2;
    public static final int UNIT_PRICE_MAX_SCALE = 4;
    public static final int PREQ_DESC_LENGTH = 500;
    public static final String PREQ_DISCOUNT_MULT = "-0.01";

    public static final String REQUISITION_DOCUMENT_TYPE = "REQS";
    public static final String RECEIVING_LINE_ITEM_DOCUMENT_TYPE = "RCVL";
    public static final String RECEIVING_THRESHOLD_DOCUMENT_TYPE = "THLD";

    public static final String PO_OVERRIDE_NOT_TO_EXCEED_QUESTION = "OverrideNotToExceed";
    public static final String FIX_CAPITAL_ASSET_WARNINGS = "FixCapitalAssetWarnings";
    public static final String PO_NEXT_FY_WARNING = "NextFiscalYearWarning";

    // ACCOUNTS PAYABLE
    public static final String AP_OVERRIDE_INVOICE_NOMATCH_QUESTION = "OverrideInvoiceNoMatch";

    // PAYMENT REQUEST
    public static final String PAYMENT_REQUEST_ACTION_NAME = "PaymentRequest";
    public static final String PREQ_PAY_DATE_DAYS = "days";
    public static final String PREQ_PAY_DATE_DATE = "date";
    public static final int PREQ_PAY_DATE_EMPTY_TERMS_DEFAULT_DAYS = 28;
    public static final int PREQ_PAY_DATE_DAYS_BEFORE_WARNING = 60;

    //TAB ERROR KEYS
    public static final String PAYMENT_REQUEST_INIT_TAB_ERRORS = "document.purchaseOrderIdentifier,document.invoiceNumber,document.invoiceDate,document.vendorInvoiceAmount,document.specialHandlingInstructionLine1Text,document.specialHandlingInstructionLine2Text,document.specialHandlingInstructionLine3Text";
    public static final String PAYMENT_REQUEST_TAX_TAB_ERRORS = "document.tax*";
    public static final String PAYMENT_REQUEST_INVOICE_TAB_ERRORS = "document.paymentRequestPayDate,document.bank*,bankCode";
    public static final String RECEIVING_LINE_INIT_TAB_ERRORS = "document.purchaseOrderIdentifier,document.shipmentReceivedDate,document.shipmentPackingSlipNumber,document.shipmentBillOfLadingNumber,document.carrierCode";
    public static final String BULK_RECEIVING_INIT_TAB_ERRORS = "document.purchaseOrderIdentifier,document.shipmentReceivedDate,document.shipmentPackingSlipNumber,document.shipmentBillOfLadingNumber,document.carrierCode";
    public static final String BULK_RECEIVING_VENDOR_TAB_ERRORS = "document.vendor*,document.alternate*,document.goodsDelivered*,document.shipmentReceivedDate,document.shipmentPackingSlipNumber,document.shipmentBillOfLadingNumber,document.carrierCode,document.shipmentReferenceNumber,document.shipmentWeight,document.noOfCartons,document.trackingNumber";
    public static final String BULK_RECEIVING_DELIVERY_TAB_ERRORS = "document.delivery*,document.institution*,document.requestor,document.preparer";
    public static final String REJECT_DOCUMENT_TAB_ERRORS = "document.vendorDunsNumber,document.vendor*,document.invoice*";

    // Weird PaymentTermsType is due on either the 10th or 25th with no discount
    public static final String PMT_TERMS_TYP_NO_DISCOUNT_CD = "00N2T";

    public static final String PURAP_AP_SHOW_CONTINUATION_ACCOUNT_WARNING_FISCAL_OFFICERS = "SHOW_CONTINUATION_ACCOUNT_WARNING_FISCAL_OFFICERS_IND";
    public static final String PURAP_AP_SHOW_CONTINUATION_ACCOUNT_WARNING_AP_USERS = "SHOW_CONTINUATION_ACCOUNT_WARNING_AP_USERS_IND";

    public static final String BELOW_THE_LINES_PARAMETER = "ADDITIONAL_CHARGES_ITEM_TYPES";
    public static final String ITEM_ALLOWS_ZERO = "ITEM_TYPES_ALLOWING_ZERO";
    public static final String ITEM_ALLOWS_POSITIVE = "ITEM_TYPES_ALLOWING_POSITIVE";
    public static final String ITEM_ALLOWS_NEGATIVE = "ITEM_TYPES_ALLOWING_NEGATIVE";
    public static final String ITEM_REQUIRES_USER_ENTERED_DESCRIPTION = "ITEM_TYPES_REQUIRING_USER_ENTERED_DESCRIPTION";

    // ELECTRONIC INVOICE REJECT DOCUMENT
    public static final String ELECTROINC_INVOICE_REJECT_ACTION_NAME = "ElectronicInvoiceReject";
    public static final String REJECT_DOCUMENT_RESEARCH_INCOMPETE = "errors.reject.docucument.research.incomplete";

    // CREDIT MEMO DOCUMENT
    public static final String CREDIT_MEMO_ACTION_NAME = "VendorCreditMemo";
    
    // this should be removed - use the central definition in KFSConstants
    @Deprecated
    public static final String PURAP_ORIGIN_CODE = "01";

    public static final Integer PRORATION_SCALE = 6;
    public static final Integer CREDITMEMO_PRORATION_SCALE = 20;

    public static String PRODUCTION_ENVIRONMENT = "PRD";

    public static String FAX_TEST_PHONE_NUMBER = "FAX_TEST_PHONE_NUMBER";

    public static final String ELECTRONIC_INVOICE_FILE_TYPE_INDENTIFIER = "electronicInvoiceInputFileType";
    public static final String B2B_PO_RESPONSE_FILE_TYPE_INDENTIFIER = "b2bPOResponseFileType";
    public static final String B2B_PUNCH_OUT_ORDER_FILE_TYPE_INDENTIFIER = "b2bPunchOutOrderFileType";
    public static final String B2B_PUNCH_OUT_RESPONSE_FILE_TYPE_INDENTIFIER = "b2bPunchOutResponseFileType";

    public static final String PURAP_APPLICATION_DOCUMENT_ID_NOT_AVAILABLE = "Not Available";
    public static final String PURAP_APPLICATION_DOCUMENT_STATUS_NOT_AVAILABLE = "Not Available";

    public static final String DELIVERY_BUILDING_NAME_INACTIVE_ERROR = "document.deliveryBuildingName";
    public static final String DELIVERY_ROOM_NUMBER_INACTIVE_ERROR = "document.deliveryBuildingRoomNumber";

    public static final String PURAP_REQS_ORG_CD = "document.organizationCode";

    public static final String PO_RETRANSMIT_SELECT_TAB_ERRORS = "document.items";

    public static final String ITEM_PURCHASING_COMMODITY_CODE = "distributePurchasingCommodityCode";

    public static final String ITEM_TYPE_QTY = "Qty";
    public static final String ITEM_TYPE_NO_QTY = "No Qty";

    public static final String PO_FINAL_ANNOTATION_TEXT = "message.document.purap.final.annotation";
    public static final String PO_DISAPPROVAL_ANNOTATION_TEXT = "message.document.purap.disapprove.annotation";
    public static final String PO_CANCEL_ANNOTATION_TEXT = "message.document.purap.cancel.annotation";

    /*
     * Fields that shouldn't be copied by our reflective copy method. This should only contain fields that are known throughout
     * objects not item/doc specific ones
     */
    public static final Map<String, Class<?>> KNOWN_UNCOPYABLE_FIELDS = uncopyableFields();

    /*
     * Fields that shouldn't be copied by our reflective copy method. This should only contain fields that are known throughout
     * objects not item/doc specific ones
     */
    public static final Map<String, Class<?>> ITEM_UNCOPYABLE_FIELDS = uncopyableItemFields();

    /*
     * fields that shouldn't be copied on PREQ item
     */
    public static final Map<String, Class<?>> PREQ_ITEM_UNCOPYABLE_FIELDS = uncopyablePREQItemFields();

    public static final Map<String, Class> UNCOPYABLE_FIELDS_FOR_PO = uncopyableFieldsForPurchaseOrder();

    public static final HashMap<String, String> PURAP_DETAIL_TYPE_CODE_MAP = getPurapParameterDetailTypeCodes();

    /**
     * Private Constructor since this is a constants class that should never be instantiated.
     */
    private PurapConstants() {
    }

    private static Map<String, Class<?>> uncopyableFields() {
        Map<String, Class<?>> fields = new HashMap<>();
        fields.put(KFSConstants.VERSION_NUMBER, null);
        fields.put("LOG", null);
        fields.put(KFSPropertyConstants.GENERAL_LEDGER_PENDING_ENTRIES, null);
        fields.put(PurapPropertyConstants.CAPITAL_ASSET_ITEM_IDENTIFIER, null);
        fields.put(PurapPropertyConstants.CAPITAL_ASSET_SYSTEM_IDENTIFIER, null);
        fields.put("serialVersionUID", null);
        return fields;
    }

    private static Map<String, Class<?>> uncopyableItemFields() {
        Map<String, Class<?>> fields = new HashMap<>();
        fields.put(PurapPropertyConstants.ITEM_IDENTIFIER, null);
        fields.put(PurapPropertyConstants.ACCOUNTS, null);
        return fields;
    }

    private static Map<String, Class<?>> uncopyablePREQItemFields() {
        Map<String, Class<?>> fields = new HashMap<>(ITEM_UNCOPYABLE_FIELDS);
        fields.put(PurapPropertyConstants.QUANTITY, null);
        fields.put(PurapPropertyConstants.EXTENDED_PRICE, null);
        fields.put(PurapPropertyConstants.ITEM_TAX_AMOUNT, null);
        fields.put(PurapPropertyConstants.ITEM_SALES_TAX_AMOUNT, null);
        return fields;
    }

    public static Map<String, Class> uncopyableFieldsForPurchaseOrder() {
        Map<String, Class> returnMap = new HashMap<>();
        returnMap.put(KFSPropertyConstants.DOCUMENT_NUMBER, null);
        returnMap.put(PurapPropertyConstants.ITEM_IDENTIFIER, PurchaseOrderItem.class);
        returnMap.put(PurapPropertyConstants.ACCOUNT_IDENTIFIER, PurchaseOrderAccount.class);
        returnMap.put(PurapPropertyConstants.PURCHASE_ORDER_VENDOR_QUOTE_IDENTIFIER, PurchaseOrderVendorQuote.class);
        returnMap.put("relatedRequisitionViews", PurchasingAccountsPayableDocumentBase.class);
        returnMap.put("relatedPurchaseOrderViews", PurchasingAccountsPayableDocumentBase.class);
        returnMap.put("relatedPaymentRequestViews", PurchasingAccountsPayableDocumentBase.class);
        returnMap.put("relatedCreditMemoViews", PurchasingAccountsPayableDocumentBase.class);
        returnMap.put("paymentHistoryPaymentRequestViews", PurchasingAccountsPayableDocumentBase.class);
        returnMap.put("paymentHistoryCreditMemoViews", PurchasingAccountsPayableDocumentBase.class);
        return returnMap;
    }

    public static Map<String, Class<?>> uncopyableFieldsForSplitPurchaseOrder() {
        Map<String, Class<?>> returnMap = new HashMap<>();
        returnMap.put(KFSPropertyConstants.DOCUMENT_HEADER, null);
        returnMap.put(PurapPropertyConstants.PURAP_DOC_ID, null);
        returnMap.put(PurapPropertyConstants.ITEMS, null);
        returnMap.put(PurapPropertyConstants.PURCHASE_ORDER_VENDOR_QUOTES, null);
        returnMap.put(PurapPropertyConstants.PURCHASE_ORDER_QUOTE_INITIALIZATION_DATE, null);
        returnMap.put(PurapPropertyConstants.PURCHASE_ORDER_QUOTE_AWARDED_DATE, null);
        returnMap.put(PurapPropertyConstants.PURCHASE_ORDER_QUOTE_DUE_DATE, null);
        returnMap.put(PurapPropertyConstants.PURCHASE_ORDER_QUOTE_TYPE_CODE, null);
        returnMap.put(PurapPropertyConstants.PURCHASE_ORDER_QUOTE_VENDOR_NOTE_TEXT, null);
        return returnMap;
    }
    
    /**
     * @deprecated This information needs to be looked up from the DD
     * @return map of purap doc types to classes
    */
    @Deprecated
    private static HashMap<String, String> getPurapParameterDetailTypeCodes() {
        HashMap<String, String> map;
        map = new HashMap<>();
        map.put("REQS", RequisitionDocument.class.getName());
        map.put("PO", PurchaseOrderDocument.class.getName());
        map.put("POC", PurchaseOrderDocument.class.getName());
        map.put("POR", PurchaseOrderDocument.class.getName());
        map.put("POA", PurchaseOrderDocument.class.getName());
        map.put("POPH", PurchaseOrderDocument.class.getName());
        map.put("PORH", PurchaseOrderDocument.class.getName());
        map.put("PORT", PurchaseOrderDocument.class.getName());
        map.put("POV", PurchaseOrderDocument.class.getName());
        map.put("POSP", PurchaseOrderDocument.class.getName());
        map.put("PREQ", PaymentRequestDocument.class.getName());
        map.put("CM", VendorCreditMemoDocument.class.getName());
        return map;
    }

    public static class QuoteTypes {
        public static final String COMPETITIVE = "COMP";
        public static final String PRICE_CONFIRMATION = "CONF";
    }

    public static class QuoteTypeDescriptions {
        public static final String COMPETITIVE = "Competitive";
        public static final String PRICE_CONFIRMATION = "Price Confirmation";
    }

    public static class QuoteTransmitTypes {
        public static final String PRINT = "PRINT";
        public static final String FAX = "FAX";
    }

    public static class QuoteStatusCode {
        public static final String DELV = "DELV";
        public static final String FUIP = "FUIP";
        public static final String IIQ = "IIQ";
        public static final String LEXP = "LEXP";
        public static final String MULT = "MULT";
        public static final String NORS = "NORS";
        public static final String PTFE = "PTFE";
        public static final String RECV = "RECV";
        public static final String RIR = "RIR";
        public static final String RECL = "RECL";
        public static final String RNLB = "RNLB";
        public static final String RNLN = "RNLN";
        public static final String NOBD = "NOBD";
        public static final String SQNA = "SQNA";
        public static final String TINC = "TINC";
    }
    
    public static class RequisitionStatuses {
        // Added for updating app doc status for disapproved
        public static final String APPDOC_IN_PROCESS = "In Process";
        public static final String APPDOC_CANCELLED = "Cancelled";
        public static final String APPDOC_CLOSED = "Closed";
        public static final String APPDOC_AWAIT_FISCAL_REVIEW = "Awaiting Fiscal Officer";
        public static final String APPDOC_AWAIT_CONTENT_REVIEW = "Awaiting Content Approval";
        public static final String APPDOC_AWAIT_HAS_ACCOUNTING_LINES = "Awaiting Accounting Lines";
        public static final String APPDOC_AWAIT_SUB_ACCT_REVIEW = "Awaiting Sub Account";
        public static final String APPDOC_AWAIT_CHART_REVIEW = "Awaiting Base Org Review";
        public static final String APPDOC_AWAIT_COMMODITY_CODE_REVIEW = "Awaiting Commodity Review";
        public static final String APPDOC_AWAIT_SEP_OF_DUTY_REVIEW = "Awaiting Separation of Duties";
        public static final String APPDOC_AWAIT_CONTRACT_MANAGER_ASSGN = "Awaiting Contract Manager Assignment";

        public static final String APPDOC_DAPRVD_AWARD = "Disapproved Award";
        public static final String APPDOC_DAPRVD_CONTENT = "Disapproved Content";
        public static final String APPDOC_DAPRVD_HAS_ACCOUNTING_LINES = "Disapproved Accounting Lines";
        public static final String APPDOC_DAPRVD_SUB_ACCT = "Disapproved Sub Account";
        public static final String APPDOC_DAPRVD_FISCAL = "Disapproved Fiscal";
        public static final String APPDOC_DAPRVD_CHART = "Disapproved Base Org Review";
        public static final String APPDOC_DAPRVD_COMMODITY_CODE = "Disapproved Commodity Review";
        public static final String APPDOC_DAPRVD_SEP_OF_DUTY = "Disapproved Separation of Duties";
        public static final String APPDOC_DAPRVD_AD_HOC = "Disapproved Ad Hoc";

        // Node Name Declarations
        public static final String NODE_AWARD= "Award";
        public static final String NODE_CONTENT_REVIEW = "Organization";
        public static final String NODE_SUBACCOUNT = "SubAccount";
        public static final String NODE_SEPARATION_OF_DUTIES = "SeparationOfDuties";
        public static final String NODE_ACCOUNT = "Account";
        public static final String NODE_HAS_ACCOUNTING_LINES = "Initiator";
        public static final String NODE_ORG_REVIEW = "AccountingOrganizationHierarchy";
        public static final String NODE_COMMODITY_CODE_REVIEW = "Commodity";
        public static final String NODE_COMMODITY_CODE_APO_REVIEW = "CommodityAPO";
        public static final String NODE_ADHOC_REVIEW = "AdHoc";
        
        public static HashMap<String, String> getAllAppDocStatuses() {
            HashMap<String, String> appDocStatusMap = new HashMap<>();

            appDocStatusMap.put(APPDOC_IN_PROCESS, APPDOC_IN_PROCESS);
            appDocStatusMap.put(APPDOC_CANCELLED, APPDOC_CANCELLED);
            appDocStatusMap.put(APPDOC_CLOSED, APPDOC_CLOSED);
            appDocStatusMap.put(APPDOC_AWAIT_FISCAL_REVIEW, APPDOC_AWAIT_FISCAL_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_CONTENT_REVIEW, APPDOC_AWAIT_CONTENT_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_HAS_ACCOUNTING_LINES, APPDOC_AWAIT_HAS_ACCOUNTING_LINES);
            appDocStatusMap.put(APPDOC_AWAIT_SUB_ACCT_REVIEW, APPDOC_AWAIT_SUB_ACCT_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_CHART_REVIEW, APPDOC_AWAIT_CHART_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_COMMODITY_CODE_REVIEW, APPDOC_AWAIT_COMMODITY_CODE_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_SEP_OF_DUTY_REVIEW, APPDOC_AWAIT_SEP_OF_DUTY_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_CONTRACT_MANAGER_ASSGN, APPDOC_AWAIT_CONTRACT_MANAGER_ASSGN);
            appDocStatusMap.put(APPDOC_DAPRVD_AWARD, APPDOC_DAPRVD_AWARD);
            appDocStatusMap.put(APPDOC_DAPRVD_CONTENT, APPDOC_DAPRVD_CONTENT);
            appDocStatusMap.put(APPDOC_DAPRVD_HAS_ACCOUNTING_LINES, APPDOC_DAPRVD_HAS_ACCOUNTING_LINES);
            appDocStatusMap.put(APPDOC_DAPRVD_SUB_ACCT, APPDOC_DAPRVD_SUB_ACCT);
            appDocStatusMap.put(APPDOC_DAPRVD_FISCAL, APPDOC_DAPRVD_FISCAL);
            appDocStatusMap.put(APPDOC_DAPRVD_CHART, APPDOC_DAPRVD_CHART);
            appDocStatusMap.put(APPDOC_DAPRVD_COMMODITY_CODE, APPDOC_DAPRVD_COMMODITY_CODE);
            appDocStatusMap.put(APPDOC_DAPRVD_SEP_OF_DUTY, APPDOC_DAPRVD_SEP_OF_DUTY);

            return appDocStatusMap;
        }
        
        public static HashMap<String, String> getRequistionAppDocStatuses() {
            HashMap<String, String> reqAppDocStatusMap;

            reqAppDocStatusMap = new HashMap<>();
            reqAppDocStatusMap.put(NODE_AWARD, APPDOC_DAPRVD_AWARD);
            reqAppDocStatusMap.put(NODE_CONTENT_REVIEW, APPDOC_DAPRVD_CONTENT);
            reqAppDocStatusMap.put(NODE_HAS_ACCOUNTING_LINES, APPDOC_DAPRVD_HAS_ACCOUNTING_LINES);
            reqAppDocStatusMap.put(NODE_SUBACCOUNT,  APPDOC_DAPRVD_SUB_ACCT);
            reqAppDocStatusMap.put(NODE_ACCOUNT, APPDOC_DAPRVD_FISCAL);
            reqAppDocStatusMap.put(NODE_ORG_REVIEW, APPDOC_DAPRVD_CHART);
            reqAppDocStatusMap.put(NODE_COMMODITY_CODE_REVIEW, APPDOC_DAPRVD_COMMODITY_CODE);
            reqAppDocStatusMap.put(NODE_COMMODITY_CODE_APO_REVIEW, APPDOC_DAPRVD_COMMODITY_CODE);
            reqAppDocStatusMap.put(NODE_SEPARATION_OF_DUTIES, APPDOC_DAPRVD_SEP_OF_DUTY);
            reqAppDocStatusMap.put(APPDOC_IN_PROCESS,  APPDOC_IN_PROCESS);
            reqAppDocStatusMap.put(APPDOC_CLOSED, APPDOC_CLOSED);
            reqAppDocStatusMap.put(APPDOC_CANCELLED, APPDOC_CANCELLED);
            reqAppDocStatusMap.put(APPDOC_AWAIT_CONTRACT_MANAGER_ASSGN, APPDOC_AWAIT_CONTRACT_MANAGER_ASSGN);
            reqAppDocStatusMap.put(NODE_ADHOC_REVIEW, APPDOC_DAPRVD_AD_HOC);

            return reqAppDocStatusMap;
        }

    }

    public static class POCostSources {
        public static final String ESTIMATE = "EST";
    }

    public static final class POTransmissionMethods {
        public static final String FAX = "FAX";
        public static final String PRINT = "PRIN";
        public static final String NOPRINT = "NOPR";
        public static final String ELECTRONIC = "ELEC";
    }

    public static class RequisitionSources {
        public static final String STANDARD_ORDER = "STAN";
        public static final String B2B = "B2B";
    }

    // PURCHASE ORDER VENDOR CHOICE CODES
    public static class VendorChoice {
        public static final String CONTRACTED_PRICE = "CONT";
        public static final String SMALL_ORDER = "SMAL";
        public static final String PROFESSIONAL_SERVICE = "PROF";
        public static final String SUBCONTRACT = "SUBC";
    }

    public static class PurchaseOrderStatuses {
        // Added for updating app doc status for disapproved
        public static final String APPDOC_DAPRVD_UNORDERED_ITEM = "Disapproved New Unordered Item Review";
        public static final String APPDOC_DAPRVD_PURCHASING = "Disapproved Purchasing";
        public static final String APPDOC_DAPRVD_COMMODITY_CODE = "Disapproved Commodity Code";
        public static final String APPDOC_DAPRVD_CG_APPROVAL = "Disapproved C and G";
        public static final String APPDOC_DAPRVD_BUDGET = "Disapproved Budget";
        public static final String APPDOC_DAPRVD_TAX = "Disapproved Tax";
        public static final String APPDOC_CANCELLED = "Cancelled";
        public static final String APPDOC_VOID = "Void";
        public static final String APPDOC_IN_PROCESS = "In Process";
        public static final String APPDOC_CHANGE_IN_PROCESS = "Change in Process";
        public static final String APPDOC_CLOSED = "Closed";
        public static final String APPDOC_OPEN = "Open";
        public static final String APPDOC_PAYMENT_HOLD = "Payment Hold";
        public static final String APPDOC_PENDING_PRINT = "Pending Print";
        public static final String APPDOC_AWAIT_TAX_REVIEW = "Awaiting Tax Approval";
        public static final String APPDOC_AWAIT_BUDGET_REVIEW = "Awaiting Budget Approval";
        public static final String APPDOC_AWAIT_CONTRACTS_GRANTS_REVIEW = "Awaiting C and G Approval";
        public static final String APPDOC_AWAIT_PURCHASING_REVIEW = "Awaiting Purchasing Approval";
        public static final String APPDOC_AWAIT_NEW_UNORDERED_ITEM_REVIEW = "Awaiting New Unordered Item Review";
        public static final String APPDOC_AWAIT_COMMODITY_CODE_REVIEW = "Awaiting Commodity Code Approval";
        public static final String APPDOC_FAX_ERROR = "Error occurred sending fax";
        public static final String APPDOC_QUOTE = "Out for Quote";
        public static final String APPDOC_CXML_ERROR = "Error occurred sending cxml";
        public static final String APPDOC_PENDING_CXML = "Pending cxml";
        public static final String APPDOC_PENDING_FAX = "Pending Fax";
        public static final String APPDOC_WAITING_FOR_VENDOR = "Waiting for Vendor";
        public static final String APPDOC_WAITING_FOR_DEPARTMENT = "Waiting for Department";
        public static final String APPDOC_AMENDMENT = "Pending Amendment";
        public static final String APPDOC_PENDING_CLOSE = "Pending Close";
        public static final String APPDOC_CANCELLED_CHANGE = "Cancelled Change";
        public static final String APPDOC_DISAPPROVED_CHANGE = "Disapproved Change";
        public static final String APPDOC_DAPRVD_CONTRACTS_GRANTS = "Disapproved C and G";
        public static final String APPDOC_PENDING_PAYMENT_HOLD = "Pending Payment Hold";
        public static final String APPDOC_PENDING_REMOVE_HOLD = "Pending Remove Hold";
        public static final String APPDOC_PENDING_REOPEN = "Pending Reopen";
        public static final String APPDOC_PENDING_RETRANSMIT = "Pending Retransmit";
        public static final String APPDOC_RETIRED_VERSION = "Retired Version";
        public static final String APPDOC_PENDING_VOID = "Pending Void";
        public static final String APPDOC_CANCELLED_IN_PROCESS = "Cancelled In Process";

        public static final String NODE_ADHOC_REVIEW = "AdHoc";
        public static final String NODE_CONTRACT_MANAGEMENT = "ContractManagement";
        public static final String NODE_AWAIT_NEW_UNORDERED_ITEM_REVIEW = "NewUnorderedItems";
        public static final String NODE_INTERNAL_PURCHASING_REVIEW = "ContractManagement";
        public static final String NODE_COMMODITY_CODE_REVIEW = "Commodity";
        public static final String NODE_COMMODITY_CODE_APO_REVIEW = "CommodityAPO";
        public static final String NODE_CONTRACTS_AND_GRANTS_REVIEW = "Award";
        public static final String NODE_BUDGET_OFFICE_REVIEW = "Budget";
        public static final String NODE_VENDOR_TAX_REVIEW = "Tax";
        public static final String NODE_DOCUMENT_TRANSMISSION = "JoinVendorIsEmployeeOrNonResidentAlien";

        public static final Set<String> INCOMPLETE_STATUSES = new HashSet<String>();
        public static final Set<String> COMPLETE_STATUSES = new HashSet<String>();

        public static final Map<String, String> STATUSES_BY_TRANSMISSION_TYPE = getStatusesByTransmissionType();

        public static HashMap<String, String> getAllAppDocStatuses(){
            HashMap<String, String> appDocStatusMap = new HashMap<>();

            appDocStatusMap.put(APPDOC_DAPRVD_UNORDERED_ITEM, APPDOC_DAPRVD_UNORDERED_ITEM);
            appDocStatusMap.put(APPDOC_DAPRVD_PURCHASING, APPDOC_DAPRVD_PURCHASING);
            appDocStatusMap.put(APPDOC_DAPRVD_COMMODITY_CODE, APPDOC_DAPRVD_COMMODITY_CODE);
            appDocStatusMap.put(APPDOC_DAPRVD_CG_APPROVAL, APPDOC_DAPRVD_CG_APPROVAL);
            appDocStatusMap.put(APPDOC_DAPRVD_BUDGET, APPDOC_DAPRVD_BUDGET);
            appDocStatusMap.put(APPDOC_DAPRVD_TAX, APPDOC_DAPRVD_TAX);
            appDocStatusMap.put(APPDOC_CANCELLED, APPDOC_CANCELLED);
            appDocStatusMap.put(APPDOC_VOID, APPDOC_VOID);
            appDocStatusMap.put(APPDOC_IN_PROCESS, APPDOC_IN_PROCESS);
            appDocStatusMap.put(APPDOC_CHANGE_IN_PROCESS, APPDOC_CHANGE_IN_PROCESS);
            appDocStatusMap.put(APPDOC_CLOSED, APPDOC_CLOSED);
            appDocStatusMap.put(APPDOC_OPEN, APPDOC_OPEN);
            appDocStatusMap.put(APPDOC_PAYMENT_HOLD, APPDOC_PAYMENT_HOLD);
            appDocStatusMap.put(APPDOC_PENDING_PRINT, APPDOC_PENDING_PRINT);
            appDocStatusMap.put(APPDOC_AWAIT_TAX_REVIEW, APPDOC_AWAIT_TAX_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_BUDGET_REVIEW, APPDOC_AWAIT_BUDGET_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_CONTRACTS_GRANTS_REVIEW, APPDOC_AWAIT_CONTRACTS_GRANTS_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_PURCHASING_REVIEW, APPDOC_AWAIT_PURCHASING_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_NEW_UNORDERED_ITEM_REVIEW, APPDOC_AWAIT_NEW_UNORDERED_ITEM_REVIEW);
            appDocStatusMap.put(APPDOC_AWAIT_COMMODITY_CODE_REVIEW, APPDOC_AWAIT_COMMODITY_CODE_REVIEW);
            appDocStatusMap.put(APPDOC_FAX_ERROR, APPDOC_FAX_ERROR);
            appDocStatusMap.put(APPDOC_QUOTE, APPDOC_QUOTE);
            appDocStatusMap.put(APPDOC_CXML_ERROR, APPDOC_CXML_ERROR);
            appDocStatusMap.put(APPDOC_PENDING_CXML, APPDOC_PENDING_CXML);
            appDocStatusMap.put(APPDOC_PENDING_FAX, APPDOC_PENDING_FAX);
            appDocStatusMap.put(APPDOC_WAITING_FOR_VENDOR, APPDOC_WAITING_FOR_VENDOR);
            appDocStatusMap.put(APPDOC_WAITING_FOR_DEPARTMENT, APPDOC_WAITING_FOR_DEPARTMENT);
            appDocStatusMap.put(APPDOC_AMENDMENT, APPDOC_AMENDMENT);
            appDocStatusMap.put(APPDOC_PENDING_CLOSE, APPDOC_PENDING_CLOSE);
            appDocStatusMap.put(APPDOC_CANCELLED_CHANGE, APPDOC_CANCELLED_CHANGE);
            appDocStatusMap.put(APPDOC_DISAPPROVED_CHANGE, APPDOC_DISAPPROVED_CHANGE);
            appDocStatusMap.put(APPDOC_DAPRVD_CONTRACTS_GRANTS, APPDOC_DAPRVD_CONTRACTS_GRANTS);
            appDocStatusMap.put(APPDOC_PENDING_PAYMENT_HOLD, APPDOC_PENDING_PAYMENT_HOLD);
            appDocStatusMap.put(APPDOC_PENDING_REMOVE_HOLD, APPDOC_PENDING_REMOVE_HOLD);
            appDocStatusMap.put(APPDOC_PENDING_REOPEN, APPDOC_PENDING_REOPEN);
            appDocStatusMap.put(APPDOC_PENDING_RETRANSMIT, APPDOC_PENDING_RETRANSMIT);
            appDocStatusMap.put(APPDOC_RETIRED_VERSION, APPDOC_RETIRED_VERSION);
            appDocStatusMap.put(APPDOC_PENDING_VOID, APPDOC_PENDING_VOID);

            return appDocStatusMap;
        }

        public static HashMap<String, String> getPurchaseOrderAppDocDisapproveStatuses(){
            HashMap<String, String> poAppDocStatusMap = new HashMap<>();

            poAppDocStatusMap.put(NODE_ADHOC_REVIEW, PurchaseOrderStatuses.APPDOC_CANCELLED);
            poAppDocStatusMap.put(NODE_AWAIT_NEW_UNORDERED_ITEM_REVIEW,PurchaseOrderStatuses.APPDOC_DAPRVD_UNORDERED_ITEM);
            poAppDocStatusMap.put(NODE_INTERNAL_PURCHASING_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_PURCHASING);
            poAppDocStatusMap.put(NODE_COMMODITY_CODE_REVIEW,  PurchaseOrderStatuses.APPDOC_DAPRVD_COMMODITY_CODE);
            poAppDocStatusMap.put(NODE_COMMODITY_CODE_APO_REVIEW,  PurchaseOrderStatuses.APPDOC_DAPRVD_COMMODITY_CODE);
            poAppDocStatusMap.put(NODE_CONTRACTS_AND_GRANTS_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_CG_APPROVAL);
            poAppDocStatusMap.put(NODE_BUDGET_OFFICE_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_BUDGET);
            poAppDocStatusMap.put(NODE_VENDOR_TAX_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_TAX);
            poAppDocStatusMap.put(PurchaseOrderStatuses.APPDOC_CANCELLED,  PurchaseOrderStatuses.APPDOC_CANCELLED);
            poAppDocStatusMap.put(PurchaseOrderStatuses.APPDOC_VOID,  PurchaseOrderStatuses.APPDOC_VOID);
            poAppDocStatusMap.put(PurchaseOrderStatuses.APPDOC_IN_PROCESS,  PurchaseOrderStatuses.APPDOC_IN_PROCESS);
            poAppDocStatusMap.put(PurchaseOrderStatuses.APPDOC_CLOSED,  PurchaseOrderStatuses.APPDOC_CLOSED);
            // KFSUPGRADE-345
            poAppDocStatusMap.put(RequisitionStatuses.NODE_ORG_REVIEW, RequisitionStatuses.APPDOC_DAPRVD_CHART);
            poAppDocStatusMap.put(RequisitionStatuses.NODE_ACCOUNT, RequisitionStatuses.APPDOC_DAPRVD_FISCAL);
            poAppDocStatusMap.put(RequisitionStatuses.NODE_SUBACCOUNT,  RequisitionStatuses.APPDOC_DAPRVD_SUB_ACCT);

            return poAppDocStatusMap;
        }

        static {
            INCOMPLETE_STATUSES.add(APPDOC_AWAIT_TAX_REVIEW);
            INCOMPLETE_STATUSES.add(APPDOC_AWAIT_BUDGET_REVIEW);
            INCOMPLETE_STATUSES.add(APPDOC_AWAIT_CONTRACTS_GRANTS_REVIEW);
            INCOMPLETE_STATUSES.add(APPDOC_AWAIT_PURCHASING_REVIEW);
            INCOMPLETE_STATUSES.add(APPDOC_AWAIT_NEW_UNORDERED_ITEM_REVIEW);
            INCOMPLETE_STATUSES.add(APPDOC_AWAIT_COMMODITY_CODE_REVIEW);
            INCOMPLETE_STATUSES.add(APPDOC_FAX_ERROR);
            INCOMPLETE_STATUSES.add(APPDOC_QUOTE);
            INCOMPLETE_STATUSES.add(APPDOC_CXML_ERROR);
            INCOMPLETE_STATUSES.add(APPDOC_PENDING_CXML);
            INCOMPLETE_STATUSES.add(APPDOC_IN_PROCESS);
            INCOMPLETE_STATUSES.add(APPDOC_PAYMENT_HOLD);
            INCOMPLETE_STATUSES.add(APPDOC_PENDING_FAX);
            INCOMPLETE_STATUSES.add(APPDOC_PENDING_PRINT);
            INCOMPLETE_STATUSES.add(APPDOC_WAITING_FOR_VENDOR);
            INCOMPLETE_STATUSES.add(APPDOC_WAITING_FOR_DEPARTMENT);

            COMPLETE_STATUSES.add(APPDOC_AMENDMENT);
            COMPLETE_STATUSES.add(APPDOC_CANCELLED);
            COMPLETE_STATUSES.add(APPDOC_CHANGE_IN_PROCESS);
            COMPLETE_STATUSES.add(APPDOC_CLOSED);
            COMPLETE_STATUSES.add(APPDOC_PENDING_CLOSE);
            COMPLETE_STATUSES.add(APPDOC_CANCELLED_CHANGE);
            COMPLETE_STATUSES.add(APPDOC_DISAPPROVED_CHANGE);
            COMPLETE_STATUSES.add(APPDOC_DAPRVD_BUDGET);
            COMPLETE_STATUSES.add(APPDOC_DAPRVD_CONTRACTS_GRANTS);
            COMPLETE_STATUSES.add(APPDOC_DAPRVD_COMMODITY_CODE);
            COMPLETE_STATUSES.add(APPDOC_DAPRVD_PURCHASING);
            COMPLETE_STATUSES.add(APPDOC_DAPRVD_TAX);
            COMPLETE_STATUSES.add(APPDOC_OPEN);
            COMPLETE_STATUSES.add(APPDOC_PENDING_PAYMENT_HOLD);
            COMPLETE_STATUSES.add(APPDOC_PENDING_REMOVE_HOLD);
            COMPLETE_STATUSES.add(APPDOC_PENDING_REOPEN);
            COMPLETE_STATUSES.add(APPDOC_PENDING_RETRANSMIT);
            COMPLETE_STATUSES.add(APPDOC_RETIRED_VERSION);
            COMPLETE_STATUSES.add(APPDOC_VOID);
            COMPLETE_STATUSES.add(APPDOC_PENDING_VOID);
        }


        /**
         * Do not include 'OPEN' status in this map. The 'OPEN' status is the default status that is set when no status exists for a
         * particular pending transmission type code.
         */
        private static Map<String, String> getStatusesByTransmissionType() {
            // TODO: Consider making this a constant field.
            return Map.ofEntries(
                    entry(POTransmissionMethods.PRINT, APPDOC_PENDING_PRINT),
                    entry(POTransmissionMethods.ELECTRONIC, APPDOC_PENDING_CXML),
                    entry(POTransmissionMethods.FAX, APPDOC_PENDING_FAX)
            );
        }

    }

    public static final class ItemTypeCodes {
        // ITEM TYPES
        public static final String ITEM_TYPE_ITEM_CODE = "ITEM";
        public static final String ITEM_TYPE_SERVICE_CODE = "SRVC";
        public static final String ITEM_TYPE_FREIGHT_CODE = "FRHT";
        public static final String ITEM_TYPE_SHIP_AND_HAND_CODE = "SPHD";
        public static final String ITEM_TYPE_TRADE_IN_CODE = "TRDI";
        public static final String ITEM_TYPE_ORDER_DISCOUNT_CODE = "ORDS";
        public static final String ITEM_TYPE_MIN_ORDER_CODE = "MNOR";
        public static final String ITEM_TYPE_MISC_CODE = "MISC";
        public static final String ITEM_TYPE_PMT_TERMS_DISCOUNT_CODE = "DISC";
        public static final String ITEM_TYPE_FEDERAL_TAX_CODE = "FDTX";
        public static final String ITEM_TYPE_STATE_TAX_CODE = "STTX";
        public static final String ITEM_TYPE_FEDERAL_GROSS_CODE = "FDGR";
        public static final String ITEM_TYPE_STATE_GROSS_CODE = "STGR";
        public static final String ITEM_TYPE_RESTCK_FEE_CODE = "RSTO";
        public static final String ITEM_TYPE_MISC_CRDT_CODE = "MSCR";
        public static final String ITEM_TYPE_UNORDERED_ITEM_CODE = "UNOR";
        public static final String ITEM_TYPE_SHIPPING_CODE = "SHIP";

        public static final Set<String> EXCLUDED_ITEM_TYPES = new HashSet<String>();
        
        static {
        		EXCLUDED_ITEM_TYPES.add(ITEM_TYPE_UNORDERED_ITEM_CODE);
        	}
    }

    public static class PurchaseOrderDocTypes {
        public static final String PURCHASE_ORDER_REOPEN_DOCUMENT = "POR";
        public static final String PURCHASE_ORDER_CLOSE_DOCUMENT = "POC";
        public static final String PURCHASE_ORDER_DOCUMENT = "PO";
        public static final String PURCHASE_ORDER_RETRANSMIT_DOCUMENT = "PORT";
        public static final String PURCHASE_ORDER_PRINT_DOCUMENT = "PurchaseOrderPrintDocument";
        public static final String PURCHASE_ORDER_VOID_DOCUMENT = "POV";
        public static final String PURCHASE_ORDER_PAYMENT_HOLD_DOCUMENT = "POPH";
        public static final String PURCHASE_ORDER_REMOVE_HOLD_DOCUMENT = "PORH";
        public static final String PURCHASE_ORDER_AMENDMENT_DOCUMENT = "POA";
        public static final String PURCHASE_ORDER_SPLIT_DOCUMENT = "POSP";
    }

    public static class PODocumentsStrings {
        public static final String CLOSE_QUESTION = "POClose";
        public static final String CLOSE_CONFIRM = "POCloseConfirm";
        public static final String CLOSE_NOTE_PREFIX = "Note entered while closing a Purchase Order :";

        public static final String REOPEN_PO_QUESTION = "ReopenPO";
        public static final String CONFIRM_REOPEN_QUESTION = "ConfirmReopen";
        public static final String REOPEN_NOTE_PREFIX = "Note entered while reopening a Purchase Order : ";

        public static final String VOID_QUESTION = "POVoid";
        public static final String VOID_CONFIRM = "POVoidConfirm";
        public static final String VOID_NOTE_PREFIX = "Note entered while voiding a Purchase Order :";

        public static final String PAYMENT_HOLD_QUESTION = "POPaymentHold";
        public static final String PAYMENT_HOLD_CONFIRM = "POPaymentHoldConfirm";
        public static final String PAYMENT_HOLD_NOTE_PREFIX = "Note entered while putting a Purchase Order on payment hold :";

        public static final String REMOVE_HOLD_QUESTION = "PORemoveHold";
        public static final String REMOVE_HOLD_CONFIRM = "PORemoveHoldConfirm";
        public static final String REMOVE_HOLD_NOTE_PREFIX = "Note entered while removing a Purchase Order from payment hold :";
        public static final String REMOVE_HOLD_FYI = "This document was taken off Payment Hold status.";

        public static final String AMENDMENT_PO_QUESTION = "AmendmentPO";
        public static final String CONFIRM_AMENDMENT_QUESTION = "ConfirmAmendment";
        public static final String AMENDMENT_NOTE_PREFIX = "Note entered while amending a Purchase Order : ";

        public static final String SPLIT_QUESTION = "POSplit";
        public static final String SPLIT_CONFIRM = "POSplitConfirm";
        public static final String SPLIT_NOTE_PREFIX_OLD_DOC = "Note entered while splitting this Purchase Order : ";
        public static final String SPLIT_NOTE_PREFIX_NEW_DOC = "Note entered while being split from Purchase Order ";
        public static final String SPLIT_ADDL_CHARGES_WARNING_LABEL = "WARNING";
        public static final String SPLIT_ADDL_CHARGES_WARNING = "* ADDITIONAL CHARGES EXIST *";
        public static final String SPLIT_NOTE_TEXT = "splitNoteText";
        public static final String ITEMS_MOVING_TO_SPLIT = "movingPOItems";
        public static final String ITEMS_REMAINING = "remainingPOItems";
        public static final String LINE_ITEMS_REMAINING = "remainingPOLineItems";

        public static final String CONFIRM_AWARD_QUESTION = "POConfirmAward";
        public static final String CONFIRM_AWARD_RETURN = "completeQuote";

        public static final String CONFIRM_CANCEL_QUESTION = "POCancelQuote";
        public static final String CONFIRM_CANCEL_RETURN = "cancelQuote";

        public static final String SINGLE_CONFIRMATION_QUESTION = "singleConfirmationQuestion";

        public static final String MANUAL_STATUS_CHANGE_QUESTION = "manualStatusChangeQuestion";
        public static final String WAITING_FOR_VENDOR = "WVEN";
        public static final String WAITING_FOR_DEPARTMENT = "WDPT";

        public static final String OPEN_STATUS = "Open";

        public static final String POSTAL_CODE = "Postal Code";
        public static final String ALTERNATE_PAYEE_VENDOR = "Alternate Payee Vendor";
    }

    public static class AccountsPayableDocumentStrings {
        public static final String CANCEL_NOTE_PREFIX = "Note entered while canceling document: ";
    }

    public static final class PaymentRequestIndicatorText {
        public static final String HOLD = "HOLD";
        public static final String REQUEST_CANCEL = "REQUEST CANCEL";
    }

    public static class AccountsPayableSharedStatuses {
        public static final String IN_PROCESS = "INPR";
        public static final String AWAITING_ACCOUNTS_PAYABLE_REVIEW = "APAD";
    }

    public static final class AccountsPayableStatuses{
        public static final String NODE_ACCOUNT_PAYABLE_REVIEW = "ImageAttachment";
    }

    public static final class PaymentRequestStatuses {
        public static final String APPDOC_INITIATE = "Initiated";
        public static final String APPDOC_IN_PROCESS = "In Process";
        public static final String APPDOC_CANCELLED_IN_PROCESS = "Cancelled In Process";
        public static final String APPDOC_CANCELLED_POST_AP_APPROVE = "Cancelled";
        public static final String APPDOC_DEPARTMENT_APPROVED = "Department-Approved";
        public static final String APPDOC_AUTO_APPROVED = "Auto-Approved";
        public static final String APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW = "Awaiting AP Review";
        public static final String APPDOC_AWAITING_RECEIVING_REVIEW = "Awaiting Receiving";
        public static final String APPDOC_AWAITING_SUB_ACCT_MGR_REVIEW = "Awaiting Sub-Account Manager Approval";
        public static final String APPDOC_AWAITING_FISCAL_REVIEW = "Awaiting Fiscal Officer Approval";
        public static final String APPDOC_AWAITING_ORG_REVIEW = "Awaiting Chart Approval";
        public static final String APPDOC_AWAITING_TAX_REVIEW = "Awaiting Tax Approval";
        public static final String APPDOC_PENDING_E_INVOICE = "Pending Route Electronic Invoice";
        public static final String APPDOC_PAYMENT_METHOD_REVIEW = "Awaiting Treasury Manager Approval";

        public static final String NODE_ADHOC_REVIEW = "AdHoc";
        public static final String NODE_AWAITING_RECEIVING_REVIEW = "PurchaseWasReceived";
        public static final String NODE_SUB_ACCOUNT_REVIEW = "SubAccount";
        public static final String NODE_ACCOUNT_REVIEW = "Account";
        public static final String NODE_ORG_REVIEW = "AccountingOrganizationHierarchy";
        public static final String NODE_VENDOR_TAX_REVIEW = "Tax";
        //KFSUPGRADE-779
        public static final String NODE_PAYMENT_METHOD_REVIEW = "PaymentMethodReviewer";
        
        // KFSDUPGRADE-500
        public static final String NODE_RECEIVING = "Receiving";

        public static final String[] PREQ_STATUSES_FOR_AUTO_APPROVE = { APPDOC_AWAITING_SUB_ACCT_MGR_REVIEW, APPDOC_AWAITING_FISCAL_REVIEW, APPDOC_AWAITING_ORG_REVIEW };

        public static final String[] STATUSES_ALLOWED_FOR_EXTRACTION = { APPDOC_AUTO_APPROVED, APPDOC_DEPARTMENT_APPROVED };

        public static final String[] STATUSES_POTENTIALLY_ACTIVE = { APPDOC_IN_PROCESS, APPDOC_DEPARTMENT_APPROVED, APPDOC_AUTO_APPROVED, APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, APPDOC_AWAITING_RECEIVING_REVIEW, APPDOC_AWAITING_SUB_ACCT_MGR_REVIEW, APPDOC_AWAITING_FISCAL_REVIEW, APPDOC_AWAITING_ORG_REVIEW, APPDOC_AWAITING_TAX_REVIEW };

        public static final Set CANCELLED_STATUSES = new HashSet();
        public static final Set STATUSES_DISALLOWING_HOLD = new HashSet();
        public static final Set STATUSES_DISALLOWING_REMOVE_HOLD = new HashSet();
        public static final Set STATUSES_DISALLOWING_REQUEST_CANCEL = new HashSet();
        public static final Set STATUSES_DISALLOWING_REMOVE_REQUEST_CANCEL = new HashSet();
        public static final Set STATUSES_PREROUTE = new HashSet();
        public static final Set STATUSES_ENROUTE = new HashSet();
        public static final Set STATUSES_POSTROUTE = new HashSet();
        public static HashMap<String, String> getAllAppDocStatuses(){
            HashMap<String, String> appDocStatusMap = new HashMap<>();

            appDocStatusMap.put(APPDOC_INITIATE, APPDOC_INITIATE);
            appDocStatusMap.put(APPDOC_IN_PROCESS, APPDOC_IN_PROCESS);
            appDocStatusMap.put(APPDOC_CANCELLED_IN_PROCESS, APPDOC_CANCELLED_IN_PROCESS);
            appDocStatusMap.put(APPDOC_CANCELLED_POST_AP_APPROVE, APPDOC_CANCELLED_POST_AP_APPROVE);
            appDocStatusMap.put(APPDOC_DEPARTMENT_APPROVED, APPDOC_DEPARTMENT_APPROVED);
            appDocStatusMap.put(APPDOC_AUTO_APPROVED, APPDOC_AUTO_APPROVED);
            appDocStatusMap.put(APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW);
            appDocStatusMap.put(APPDOC_AWAITING_RECEIVING_REVIEW, APPDOC_AWAITING_RECEIVING_REVIEW);
            appDocStatusMap.put(APPDOC_AWAITING_SUB_ACCT_MGR_REVIEW, APPDOC_AWAITING_SUB_ACCT_MGR_REVIEW);
            appDocStatusMap.put(APPDOC_AWAITING_FISCAL_REVIEW, APPDOC_AWAITING_FISCAL_REVIEW);
            appDocStatusMap.put(APPDOC_AWAITING_ORG_REVIEW, APPDOC_AWAITING_ORG_REVIEW);
            appDocStatusMap.put(APPDOC_AWAITING_TAX_REVIEW, APPDOC_AWAITING_TAX_REVIEW);
            appDocStatusMap.put(APPDOC_PAYMENT_METHOD_REVIEW, APPDOC_PAYMENT_METHOD_REVIEW);
            appDocStatusMap.put(APPDOC_PENDING_E_INVOICE, APPDOC_PENDING_E_INVOICE);

            return appDocStatusMap;
        }

        // keep these in the order of potential routing
        // Note it doesn't make much sense to compare auto_approved and dept_approved but this is
        // easier than two enums plus this should primarily be used for user enterred areas
        public enum STATUS_ORDER {
            CANCELLED_IN_PROCESS(PurapConstants.PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS, false),
            CANCELLED_POST_AP_APPROVE(PurapConstants.PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, false),
            INITIATE(PurapConstants.PaymentRequestStatuses.APPDOC_INITIATE, true),
            IN_PROCESS(PurapConstants.PaymentRequestStatuses.APPDOC_IN_PROCESS, true),
            AWAITING_ACCOUNTS_PAYABLE_REVIEW(PurapConstants.PaymentRequestStatuses.APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, false),
            AWAITING_RECEIVING_REVIEW(PurapConstants.PaymentRequestStatuses.APPDOC_AWAITING_RECEIVING_REVIEW, false),
            AWAITING_SUB_ACCT_MGR_REVIEW(PurapConstants.PaymentRequestStatuses.APPDOC_AWAITING_SUB_ACCT_MGR_REVIEW, false),
            AWAITING_FISCAL_REVIEW(PurapConstants.PaymentRequestStatuses.APPDOC_AWAITING_FISCAL_REVIEW, false),
            AWAITING_ORG_REVIEW(PurapConstants.PaymentRequestStatuses.APPDOC_AWAITING_ORG_REVIEW, false),
            AWAITING_TAX_REVIEW(PurapConstants.PaymentRequestStatuses.APPDOC_AWAITING_TAX_REVIEW, false),
            DEPARTMENT_APPROVED(PurapConstants.PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED, false),
            PAYMENT_METHOD_REVIEW(PurapConstants.PaymentRequestStatuses.APPDOC_PAYMENT_METHOD_REVIEW, false),
            AUTO_APPROVED(PurapConstants.PaymentRequestStatuses.APPDOC_AUTO_APPROVED, false), ;

            private String statusCode = new String();
            private boolean fullEntryAllowed = false;

            STATUS_ORDER(String statusCode, boolean fullEntry) {
                this.statusCode = statusCode;
                this.fullEntryAllowed = fullEntry;
            }

            public static STATUS_ORDER getByStatusCode(String statusCode) {
                for (STATUS_ORDER status : STATUS_ORDER.values()) {
                    if (StringUtils.equals(status.statusCode, statusCode)) {
                        return status;
                    }
                }
                return null;
            }

            public static boolean isFullDocumentEntryCompleted(String status) {
                if (StringUtils.isNotBlank(status)) {
                    return !getByStatusCode(status).fullEntryAllowed;
                }
                return false;
            }

            public static STATUS_ORDER getPreviousStatus(String statusCode) {
                STATUS_ORDER statusOrder = STATUS_ORDER.getByStatusCode(statusCode);
                if (statusOrder.ordinal() > 0) {
                    return STATUS_ORDER.values()[statusOrder.ordinal() - 1];
                }
                return null;
            }

            public static boolean isFirstFullEntryStatus(String statusCode) {
                // NOTE this won't work if there endsup being two ways to get to the first full entry status (i.e. like AUTO/DEPT
                // for final)
                return getByStatusCode(statusCode).fullEntryAllowed && !getPreviousStatus(statusCode).fullEntryAllowed;
            }
        }

        static {
            CANCELLED_STATUSES.add(APPDOC_CANCELLED_IN_PROCESS);
            CANCELLED_STATUSES.add(APPDOC_CANCELLED_POST_AP_APPROVE);

            STATUSES_DISALLOWING_HOLD.add(APPDOC_INITIATE);
            STATUSES_DISALLOWING_HOLD.add(APPDOC_IN_PROCESS);
            STATUSES_DISALLOWING_HOLD.addAll(Arrays.asList(CANCELLED_STATUSES.toArray(new String[CANCELLED_STATUSES.size()])));

            STATUSES_DISALLOWING_REMOVE_HOLD.addAll(Arrays.asList(CANCELLED_STATUSES.toArray(new String[CANCELLED_STATUSES.size()])));

            STATUSES_DISALLOWING_REQUEST_CANCEL.add(APPDOC_INITIATE);
            STATUSES_DISALLOWING_REQUEST_CANCEL.add(APPDOC_IN_PROCESS);
            STATUSES_DISALLOWING_REQUEST_CANCEL.add(APPDOC_DEPARTMENT_APPROVED);
            STATUSES_DISALLOWING_REQUEST_CANCEL.add(APPDOC_AUTO_APPROVED);
            STATUSES_DISALLOWING_REQUEST_CANCEL.add(APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW);
            STATUSES_DISALLOWING_REQUEST_CANCEL.addAll(Arrays.asList(CANCELLED_STATUSES.toArray(new String[CANCELLED_STATUSES.size()])));

            STATUSES_DISALLOWING_REMOVE_REQUEST_CANCEL.addAll(Arrays.asList(CANCELLED_STATUSES.toArray(new String[CANCELLED_STATUSES.size()])));

            STATUSES_PREROUTE.add(APPDOC_IN_PROCESS);
            STATUSES_PREROUTE.add(APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW);

            STATUSES_ENROUTE.add(APPDOC_AWAITING_SUB_ACCT_MGR_REVIEW);
            STATUSES_ENROUTE.add(APPDOC_AWAITING_FISCAL_REVIEW);
            STATUSES_ENROUTE.add(APPDOC_AWAITING_ORG_REVIEW);
            STATUSES_ENROUTE.add(APPDOC_AWAITING_TAX_REVIEW);
            // KFSUPGRADE-500
            STATUSES_ENROUTE.add(APPDOC_AWAITING_RECEIVING_REVIEW);

            STATUSES_POSTROUTE.add(APPDOC_DEPARTMENT_APPROVED);
            STATUSES_POSTROUTE.add(APPDOC_AUTO_APPROVED);
        }

        public static HashMap<String, String> getPaymentRequestAppDocDisapproveStatuses() {

            HashMap<String, String> appDocStatusMap = new HashMap<>();

            appDocStatusMap.put(NODE_ADHOC_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS);
            appDocStatusMap.put(AccountsPayableStatuses.NODE_ACCOUNT_PAYABLE_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS);
            appDocStatusMap.put(NODE_AWAITING_RECEIVING_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE);
            appDocStatusMap.put(NODE_SUB_ACCOUNT_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE);
            appDocStatusMap.put(NODE_ACCOUNT_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE);
            appDocStatusMap.put(NODE_ORG_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE);
            appDocStatusMap.put(NODE_VENDOR_TAX_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE);
            // KFSUPGRADE-500
            appDocStatusMap.put(NODE_RECEIVING, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE);
            // KFSUPGRADE-964
            appDocStatusMap.put(NODE_PAYMENT_METHOD_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE);

            return appDocStatusMap;
    }

        public static List<String> getNodesRequiringCorrectingGeneralLedgerEntries() {
            List<String> returnList = new ArrayList<>();

            returnList.add(NODE_ACCOUNT_REVIEW);
            returnList.add(NODE_VENDOR_TAX_REVIEW);
            returnList.add(NODE_PAYMENT_METHOD_REVIEW);

            return returnList;
        }

    }

    public static class LineItemReceivingDocumentStrings{
        public static final String DUPLICATE_RECEIVING_LINE_QUESTION = "DuplicateReceivingLine";
        public static final String VENDOR_DATE = "Vendor Date";
        public static final String AWAITING_PO_OPEN_STATUS = "OutstandingTransactions";
        public static final String JOIN_NODE = "Join";
    }

    public static class LineItemReceivingStatuses{
        public static final String APPDOC_IN_PROCESS = "In Process";
        public static final String APPDOC_AWAITING_PO_OPEN_STATUS = "Awaiting Purchase Order Open Status";
        public static final String APPDOC_COMPLETE = "Complete";
        public static final String APPDOC_CANCELLED = "Cancelled";
    }

    public static class CorrectionReceivingDocumentStrings{
        public static final String NOTE_QUESTION = "CorrectionReceivingNote";
        public static final String NOTE_PREFIX = "Note entered while creating Correction Receiving: ";
        public static final String CORRECTION_RECEIVING_DOCUMENT_TYPE_NAME = "CorrectionReceiving";
        public static final String CORRECTION_RECEIVING_CREATION_NOTE_PARAMETER = "CorrectionReceivingNoteParameter";
    }

    public static class BulkReceivingDocumentStrings{
        public static final String DUPLICATE_BULK_RECEIVING_DOCUMENT_QUESTION = "DuplicateBulkReceiving";
        public static final String VENDOR_DATE = "Vendor Date";
        public static final String MESSAGE_BULK_RECEIVING_DEFAULT_DOC_DESCRIPTION = "Not associated with a PO";
    }

    public static class PREQDocumentsStrings {
        public static final String HOLD_PREQ_QUESTION = "HoldPREQ";
        public static final String CONFIRM_HOLD_QUESTION = "ConfirmHold";
        public static final String HOLD_NOTE_PREFIX = "Note entered while placing Payment Request on hold : ";
        public static final String REMOVE_HOLD_PREQ_QUESTION = "RemoveHoldPREQ";
        public static final String CONFIRM_REMOVE_HOLD_QUESTION = "ConfirmRemoveHold";
        public static final String REMOVE_HOLD_NOTE_PREFIX = "Note entered while removing a hold on Payment Request : ";
        public static final String CANCEL_PREQ_QUESTION = "CancelPREQ";
        public static final String CONFIRM_CANCEL_QUESTION = "ConfirmCancel";
        public static final String CANCEL_NOTE_PREFIX = "Note entered while requesting cancel on Payment Request : ";
        public static final String REMOVE_CANCEL_PREQ_QUESTION = "RemoveCancelPREQ";
        public static final String CONFIRM_REMOVE_CANCEL_QUESTION = "ConfirmRemoveCancel";
        public static final String REMOVE_CANCEL_NOTE_PREFIX = "Note entered while removing a request cancel on Payment Request : ";
        public static final String PURCHASE_ORDER_ID = "Purchase Order Identifier";
        public static final String INVOICE_DATE = "Invoice Date";
        public static final String INVOICE_NUMBER = "Invoice Number";
        public static final String IN_PROCESS = "In Process";
        public static final String THRESHOLD_DAYS_OVERRIDE_QUESTION = "Threshold Days Override Question";
        public static final String UNUSED_TRADE_IN_QUESTION = "There is an unused trade in amount from PO that you could apply to line item; are you sure you want to proceed ?";
        public static final String EXPIRED_ACCOUNT_QUESTION = "There is an expired account on the document; do you want to continue ?";
        public static final String ENCUMBER_NEXT_FISCAL_YEAR_QUESTION = "Encumber Next Fiscal Year Question";
        public static final String ENCUMBER_PRIOR_FISCAL_YEAR_QUESTION = "Encumber Prior Fiscal Year Question";
        public static final String VENDOR_INVOICE_AMOUNT = "Vendor Invoice Amount";
        public static final String VENDOR_STATE = "State";
        public static final String VENDOR_POSTAL_CODE = "Postal Code";
        public static final String PAYEE_TOKEN = "vendor ID";
        public static final String SPECIFIED_TOKEN = "on the specified PO";
    }

    public static class ItemFields {
        public static final String QUANTITY = "Quantity";
        public static final String UNIT_OF_MEASURE = "Unit of Measure";
        public static final String DESCRIPTION = "Description";
        public static final String UNIT_COST = "Unit Cost";
        public static final String INVOICE_QUANTITY = "Qty Invoiced";
        public static final String OPEN_QUANTITY = "Open Qty";
        public static final String INVOICE_EXTENDED_PRICE = "Total Inv Cost";
        public static final String COMMODITY_CODE = "Commodity Code";
    }

    public static class CreditMemoStatuses {
        public static final String APPDOC_INITIATE = "Initiated";
        public static final String APPDOC_IN_PROCESS = "In Process";
        public static final String APPDOC_CANCELLED_IN_PROCESS = "Cancelled In Process";
        public static final String APPDOC_CANCELLED_PRIOR_TO_AP_APPROVAL = "Void";
        public static final String APPDOC_CANCELLED_POST_AP_APPROVE = "Cancelled";
        public static final String APPDOC_COMPLETE = "Complete";
        public static final String APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW = "Awaiting AP Review";
        public static final String APPDOC_PAYMENT_METHOD_REVIEW = "Awaiting Treasury Manager Approval";

        public static final String NODE_ADHOC_REVIEW = "AdHoc";
        public static final String NODE_ACCOUNT_REVIEW = "Account";
        //KFSUPGRADE-779
        public static final String NODE_PAYMENT_METHOD_REVIEW = "PaymentMethodReviewer";

        public static final String[] STATUSES_ALLOWED_FOR_EXTRACTION = { APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, APPDOC_COMPLETE };

        public static final String[] STATUSES_POTENTIALLY_ACTIVE = { APPDOC_IN_PROCESS, APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW };

        public static final Set CANCELLED_STATUSES = new HashSet();
        public static final Set STATUSES_DISALLOWING_HOLD = new HashSet();
        public static final Set STATUSES_NOT_REQUIRING_ENTRY_REVERSAL = new HashSet();
        
        public static HashMap<String, String> getAllAppDocStatuses(){
            HashMap<String, String> appDocStatusMap = new HashMap<>();

            appDocStatusMap.put(APPDOC_INITIATE, APPDOC_INITIATE);
            appDocStatusMap.put(APPDOC_IN_PROCESS, APPDOC_IN_PROCESS);
            appDocStatusMap.put(APPDOC_CANCELLED_IN_PROCESS, APPDOC_CANCELLED_IN_PROCESS);
            appDocStatusMap.put(APPDOC_CANCELLED_PRIOR_TO_AP_APPROVAL, APPDOC_CANCELLED_PRIOR_TO_AP_APPROVAL);
            appDocStatusMap.put(APPDOC_CANCELLED_POST_AP_APPROVE, APPDOC_CANCELLED_POST_AP_APPROVE);
            appDocStatusMap.put(APPDOC_COMPLETE, APPDOC_COMPLETE);
            appDocStatusMap.put(APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW);
            appDocStatusMap.put(APPDOC_PAYMENT_METHOD_REVIEW, APPDOC_PAYMENT_METHOD_REVIEW);

            return appDocStatusMap;
        }

        public enum STATUS_ORDER {
            CANCELLED_IN_PROCESS(PurapConstants.CreditMemoStatuses.APPDOC_CANCELLED_IN_PROCESS, false), CANCELLED_PRIOR_TO_AP_APPROVAL(PurapConstants.CreditMemoStatuses.APPDOC_CANCELLED_PRIOR_TO_AP_APPROVAL, false), CANCELLED_POST_AP_APPROVE(PurapConstants.CreditMemoStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, false), INITIATE(PurapConstants.CreditMemoStatuses.APPDOC_INITIATE, true), IN_PROCESS(PurapConstants.CreditMemoStatuses.APPDOC_IN_PROCESS, true), AWAITING_ACCOUNTS_PAYABLE_REVIEW(PurapConstants.CreditMemoStatuses.APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, false),PAYMENT_METHOD_REVIEW(CreditMemoStatuses.APPDOC_PAYMENT_METHOD_REVIEW, false), COMPLETE(PurapConstants.CreditMemoStatuses.APPDOC_COMPLETE, false), ;

            private String statusCode = new String();
            private boolean fullEntryAllowed = false;

            STATUS_ORDER(String statusCode, boolean fullEntry) {
                this.statusCode = statusCode;
                this.fullEntryAllowed = fullEntry;
            }

            public static STATUS_ORDER getByStatusCode(String statusCode) {
                for (STATUS_ORDER status : STATUS_ORDER.values()) {
                    if (StringUtils.equals(status.statusCode, statusCode)) {
                        return status;
                    }
                }
                return null;
            }

            public static boolean isFullDocumentEntryCompleted(String status) {
                return !getByStatusCode(status).fullEntryAllowed;
            }

            public static STATUS_ORDER getPreviousStatus(String statusCode) {
                STATUS_ORDER statusOrder = STATUS_ORDER.getByStatusCode(statusCode);
                if (statusOrder.ordinal() > 0) {
                    return STATUS_ORDER.values()[statusOrder.ordinal() - 1];
                }
                return null;
            }

            public static boolean isFirstFullEntryStatus(String statusCode) {
                // NOTE this won't work if there endsup being two ways to get to the first full entry status (i.e. like AUTO/DEPT
                // for final)
                return getByStatusCode(statusCode).fullEntryAllowed && !getPreviousStatus(statusCode).fullEntryAllowed;
            }
        }

        static {
            CANCELLED_STATUSES.add(APPDOC_CANCELLED_IN_PROCESS);
            CANCELLED_STATUSES.add(APPDOC_CANCELLED_PRIOR_TO_AP_APPROVAL);
            CANCELLED_STATUSES.add(APPDOC_CANCELLED_POST_AP_APPROVE);

            STATUSES_DISALLOWING_HOLD.add(APPDOC_INITIATE);
            STATUSES_DISALLOWING_HOLD.add(APPDOC_IN_PROCESS);
            STATUSES_DISALLOWING_HOLD.addAll(Arrays.asList(CANCELLED_STATUSES.toArray(new String[CANCELLED_STATUSES.size()])));

            STATUSES_NOT_REQUIRING_ENTRY_REVERSAL.add(APPDOC_INITIATE);
            STATUSES_NOT_REQUIRING_ENTRY_REVERSAL.add(APPDOC_IN_PROCESS);
            STATUSES_NOT_REQUIRING_ENTRY_REVERSAL.addAll(Arrays.asList(CANCELLED_STATUSES.toArray(new String[CANCELLED_STATUSES.size()])));
        }

        public static final HashMap<String, String> getCreditMemoAppDocDisapproveStatuses(){
            HashMap<String, String> appDocStatusMap = new HashMap<>();

            appDocStatusMap.put(NODE_ADHOC_REVIEW,APPDOC_CANCELLED_IN_PROCESS);
            appDocStatusMap.put(AccountsPayableStatuses.NODE_ACCOUNT_PAYABLE_REVIEW, APPDOC_CANCELLED_PRIOR_TO_AP_APPROVAL);
            appDocStatusMap.put(NODE_ACCOUNT_REVIEW, APPDOC_CANCELLED_POST_AP_APPROVE);

            return appDocStatusMap;
        }
    }

    public static class CMDocumentsStrings {
        public static final String DUPLICATE_CREDIT_MEMO_QUESTION = "CMDuplicateInvoice";
        public static final String HOLD_CM_QUESTION = "HoldCM";
        public static final String HOLD_NOTE_PREFIX = "Note entered while placing Credit Memo on hold: ";
        public static final String CANCEL_CM_QUESTION = "CancelCM";
        public static final String REMOVE_HOLD_CM_QUESTION = "RemoveCM";
        public static final String REMOVE_HOLD_NOTE_PREFIX = "Note entered while removing hold on Credit Memo: ";
    }

    public static final class CREDIT_MEMO_TYPE_LABELS {
        public static final String TYPE_PO = "PO";
        public static final String TYPE_PREQ = "PREQ";
        public static final String TYPE_VENDOR = "Vendor";

    }

    public static final Map<String, Class<?>> UNCOPYABLE_FIELDS_FOR_SPLIT_PO = uncopyableFieldsForSplitPurchaseOrder();

    // this is mostly a duplication of an earlier subclass
    @Deprecated
    public static final class PurapDocTypeCodes {
        public static final String PAYMENT_REQUEST_DOCUMENT = "PREQ";
        public static final String CREDIT_MEMO_DOCUMENT = "CM";
        public static final String PO_DOCUMENT = "PO";
        public static final String PO_AMENDMENT_DOCUMENT = "POA";
        public static final String PO_CLOSE_DOCUMENT = "POC";
        public static final String PO_REOPEN_DOCUMENT = "POR";
        public static final String PO_VOID_DOCUMENT = "POV";
    }

    public static class CapitalAssetTabStrings {
        public static final String SYSTEM_DEFINITION = "Definition: A system is any group of line items added together to create one or more identical assets. Systems are further defined as line items that work together to perform one function. Each of the line items must be necessary for the system to function.";

        public static final String INDIVIDUAL_ASSETS = "IND";
        public static final String ONE_SYSTEM = "ONE";
        public static final String MULTIPLE_SYSTEMS = "MUL";
        public static final String INDIVIDUAL_ASSETS_DESC = "Each of the lines will be capitalized as INDIVIDUAL ASSETS.";
        public static final String ONE_SYSTEM_DESC = "Line items are being added together to create ONE SYSTEM.";
        public static final String MULTIPLE_SYSTEMS_DESC = "Any of the line items will be added together to create MULTIPLE SYSTEMS.";
        public static final String ASSET_DATA = "Asset data is on Item Tab.";

        public static final String QUESTION_SYSTEM_SWITCHING = "question.document.pur.systemTypeSwitching";
        public static final String SYSTEM_SWITCHING_QUESTION = "SystemSwitchingQuestion";
    }

    public static class NamedDateFormats {
        public static String CXML_DATE_FORMAT = "CXML_DATE_FORMAT";
        public static String CXML_SIMPLE_DATE_FORMAT = "CXML_SIMPLE_DATE_FORMAT";
        public static String CXML_SIMPLE_TIME_FORMAT = "CXML_SIMPLE_TIME_FORMAT";
        public static String KUALI_DATE_FORMAT = "KUALI_DATE_FORMAT";
        public static String KUALI_SIMPLE_DATE_FORMAT = "KUALI_SIMPLE_DATE_FORMAT";
        public static String KUALI_SIMPLE_DATE_FORMAT_2 = "KUALI_SIMPLE_DATE_FORMAT_2";

    }

    /**
     * Electronic Invoice Constants
     */
    public static class ElectronicInvoice {
        public static String NO_FILES_PROCESSED_EMAIL_MESSAGE = "No invoice files were processed today. " + "The developers will send notification if this was an error with the load process.";

        public static String[] ITEM_TYPES_REQUIRES_DESCRIPTION = {PurapConstants.ItemTypeCodes.ITEM_TYPE_ITEM_CODE,
                                                                  PurapConstants.ItemTypeCodes.ITEM_TYPE_MISC_CODE};

        // ELECTRONIC INVOICE REJECT REASON TYPE CODES
        public static String REJECT_REASON_TYPE_FILE = "FILE";
        public static String REJECT_REASON_TYPE_ORDER = "INVC";

        // ELECTRONIC INVOICE CXML DEPLOYMENT MODE CODE FOR PRODUCTION
        public static String CXML_DEPLOYMENT_MODE_PRODUCTION = "production";

        // ELECTRONIC INVOICE SHIPPING DESCRIPTION
        public static String SHIPPING_DESCRIPTION = "Electronic Invoice Shipping";
        public static String DEFAULT_BELOW_LINE_ITEM_DESCRIPTION = "Electronically entered amount";

        public static final String DEFAULT_SHIPPING_DESCRIPTION = "Shipping";
        public static final String DEFAULT_SPECIAL_HANDLING_DESCRIPTION = "Handling";

        public static String CXML_ADDRESS_SHIP_TO_ROLE_ID = "shipTo";
        public static String CXML_ADDRESS_BILL_TO_ROLE_ID = "billTo";
        public static String CXML_ADDRESS_REMIT_TO_ROLE_ID = "remitTo";

        // name of our default address name we use (null for first available)
        public static String CXML_ADDRESS_SHIP_TO_NAME = null;
        public static String CXML_ADDRESS_BILL_TO_NAME = null;
        public static String CXML_ADDRESS_REMIT_TO_NAME = null;

        /**
         * Here is a list of reject reason type codes
         */
        public static final String DUNS_INVALID = "INDU";
        public static final String INVOICE_ID_EMPTY = "IIDE";
        public static final String INVOICE_DATE_GREATER = "IDAG";
        public static final String INVOICE_DATE_INVALID = "IDIV";
        public static final String INFORMATION_ONLY = "INFO";
        public static final String FILE_FORMAT_INVALID = "INFF";
        public static final String HEADER_INVOICE_IND_ON = "HIIO";
        public static final String INVOICE_ORDERS_NOT_FOUND = "NOIV";
        public static final String DUNS_NOT_FOUND = "NODU";
        public static final String ITEM_MAPPING_NOT_AVAILABLE = "IMNA";
        public static final String DISCOUNT_SUMMARY_AMT_MISMATCH = "DSAM";
        public static final String SPL_HANDLING_SUMMARY_AMT_MISMATCH = "SSAM";
        public static final String SHIPPING_SUMMARY_AMT_MISMATCH = "SHSA";
        public static final String ITEM_TYPE_NAME_NOT_AVAILABLE = "ITNA";
        public static final String INVALID_NUMBER_FORMAT = "NFEX";
        public static final String PO_ID_EMPTY = "POIE";
        public static final String PO_ID_INVALID_FORMAT = "POII";
        public static final String PO_NOT_EXISTS = "PONE";
        public static final String PO_VENDOR_NOT_MATCHES_WITH_INVOICE_VENDOR = "PVNM";
        public static final String PO_NOT_OPEN = "PONO";
        public static final String NO_MATCHING_PO_ITEM = "IMLI";
        public static final String DUPLIATE_INVOICE_LINE_ITEM = "ICSL";
        public static final String INACTIVE_LINE_ITEM = "ILIA";
        public static final String CATALOG_NUMBER_MISMATCH = "IICN";
        public static final String UNIT_OF_MEASURE_MISMATCH = "IUOM";
        public static final String OUTSTANDING_ENCUMBERED_QTY_AVAILABLE = "UEOQ";
        public static final String INVOICE_QTY_EMPTY = "IIQE";
        public static final String PO_ITEM_QTY_LESSTHAN_INVOICE_ITEM_QTY = "PILI";
        public static final String OUTSTANDING_ENCUMBERED_AMT_AVAILABLE = "UEAA";
        public static final String PO_ITEM_AMT_LESSTHAN_INVOICE_ITEM_AMT = "PAIA";
        public static final String PO_COST_SOURCE_EMPTY = "CSEM";
        public static final String INVOICE_AMT_GREATER_THAN_UPPER_VARIANCE = "AGUV";
        public static final String INVOICE_AMT_LESSER_THAN_LOWER_VARIANCE = "ALLV";
        public static final String SALES_TAX_AMT_LESSER_THAN_LOWER_VARIANCE = "STLV";
        public static final String SALES_TAX_AMT_GREATER_THAN_UPPER_VARIANCE = "STUV";
        public static final String TAX_SUMMARY_AMT_EXISTS = "STNV";
        public static final String TAX_SUMMARY_AMT_MISMATCH = "TSAM";
        public static final String INVOICE_ORDER_DUPLICATE = "EIDU";

        public static final String PREQ_WORKLOW_EXCEPTION = "PRWE";
        public static final String PREQ_DISCOUNT_ERROR = "PRDE";
        public static final String PREQ_ROUTING_FAILURE = "PRRF";
        public static final String PREQ_ROUTING_VALIDATION_ERROR = "PRVE";
        public static final String ERROR_ADDING_SCHEMA = "EASC";


        public static class RejectDocumentFields{
            public static final String INVOICE_FILE_NUMBER = "invoiceFileNumber";
            public static final String INVOICE_FILE_DATE = "invoiceFileDate";

            //VendorID?

            public static final String VENDOR_DUNS_NUMBER = "vendorDunsNumber";
            public static final String INVOICE_PO_ID = "invoiceOrderPurchaseOrderIdentifier";
            public static final String INVOICE_ITEM_LINE_NUMBER = "invoiceItemLineNumber";
            public static final String INVOICE_ITEM_QUANTITY = "invoiceItemQuantity";
            public static final String INVOICE_ITEM_UOM = "invoiceItemUnitOfMeasureCode";
            public static final String INVOICE_ITEM_CATALOG_NUMBER = "invoiceItemCatalogNumber";
            public static final String INVOICE_ITEM_UNIT_PRICE = "invoiceItemUnitPrice";
            public static final String INVOICE_ITEM_SUBTOTAL_MAT = "invoiceItemSubTotalAmount";
            public static final String INVOICE_ITEM_TAX_AMT = "invoiceItemTaxAmount";
            public static final String INVOICE_ITEM_SPL_HANDLING_AMT = "invoiceItemSpecialHandlingAmount";
            public static final String INVOICE_ITEM_SHIPPING_AMT = "invoiceItemShippingAmount";
            public static final String INVOICE_ITEM_DISCOUNT_AMT = "invoiceItemDiscountAmount";
            public static final String INVOICE_ITEM_NET_AMT = "invoiceItemNetAmount";

        }

    }

    public static class CapitalAssetAvailability {
        public static final String NONE = "NONE";
        public static final String ONCE = "ONCE";
        public static final String EACH = "EACH";
    }

    public static class CapitalAssetSystemTypes{
        public static final String ONE_SYSTEM = "ONE";
        public static final String INDIVIDUAL = "IND";
        public static final String MULTIPLE = "MUL";
    }

    public static final class CAMS_REQUIREDNESS_FIELDS {
        public static final Map<String, String> REQUIREDNESS_FIELDS_BY_PARAMETER_NAMES = getRequirednessFieldsByParameterNames();
        
        private static Map<String, String> getRequirednessFieldsByParameterNames() {
            
            // TODO: Consider making this a constant field.
            return Map.ofEntries(
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_ASSET_NUMBER_ON_REQUISITION,
                            "itemCapitalAssets.capitalAssetNumber"),
                    entry(PurapParameterConstants.CapitalAsset.
                                    CHARTS_REQUIRING_ASSET_TRANSACTION_TYPE_ON_REQUISITION,
                            "capitalAssetTransactionTypeCode"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_ASSET_TYPE_ON_REQUISITION,
                            "capitalAssetTypeCode"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_COMMENTS_ON_REQUISITION,
                            "capitalAssetNoteText"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_DESCRIPTION_ON_REQUISITION,
                            "capitalAssetSystemDescription"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_LOCATIONS_ADDRESS_ON_REQUISITION,
                            "capitalAssetLocations.capitalAssetLine1Address"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_LOCATIONS_QUANTITY_ON_REQUISITION,
                            "capitalAssetLocations.itemQuantity"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_MANUFACTURER_ON_REQUISITION,
                            "capitalAssetManufacturerName"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_MODEL_ON_REQUISITION,
                            "capitalAssetModelDescription"),
                    entry(PurapParameterConstants.CapitalAsset.
                                    CHARTS_REQUIRING_NOT_CURRENT_FISCAL_YEAR_ON_REQUISITION,
                            "capitalAssetNotReceivedCurrentFiscalYearIndicator"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_NUMBER_OF_ASSETS_ON_REQUISITION,
                            "capitalAssetCountAssetNumber"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_ASSET_NUMBER_ON_PURCHASE_ORDER,
                            "itemCapitalAssets.capitalAssetNumber"),
                    entry(PurapParameterConstants.CapitalAsset.
                                    CHARTS_REQUIRING_ASSET_TRANSACTION_TYPE_ON_PURCHASE_ORDER,
                            "capitalAssetTransactionTypeCode"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_ASSET_TYPE_ON_PURCHASE_ORDER,
                            "capitalAssetTypeCode"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_COMMENTS_ON_PURCHASE_ORDER,
                            "capitalAssetNoteText"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_DESCRIPTION_ON_PURCHASE_ORDER,
                            "capitalAssetSystemDescription"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_LOCATIONS_ADDRESS_ON_PURCHASE_ORDER,
                            "capitalAssetLocations.capitalAssetLine1Address"),
                    entry(PurapParameterConstants.CapitalAsset.
                                    CHARTS_REQUIRING_LOCATIONS_QUANTITY_ON_PURCHASE_ORDER,
                            "capitalAssetLocations.itemQuantity"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_MANUFACTURER_ON_PURCHASE_ORDER,
                            "capitalAssetManufacturerName"),
                    entry(PurapParameterConstants.CapitalAsset.CHARTS_REQUIRING_MODEL_ON_PURCHASE_ORDER,
                            "capitalAssetModelDescription"),
                    entry(PurapParameterConstants.CapitalAsset.
                                    CHARTS_REQUIRING_NOT_CURRENT_FISCAL_YEAR_ON_PURCHASE_ORDER,
                            "capitalAssetNotReceivedCurrentFiscalYearIndicator"),
                    entry(PurapParameterConstants.CapitalAsset.
                                    CHARTS_REQUIRING_NUMBER_OF_ASSETS_ON_PURCHASE_ORDER,
                            "capitalAssetCountAssetNumber"));
        }
    }

    public static class CapitalAssetSystemStates{
        public static final String NEW = "NEW";
        public static final String MODIFY = "MOD";
    }

    public static final class CAMS_AVAILABILITY_MATRIX {

        public static final List<AvailabilityMatrix> MATRIX_LIST = getAllFromAvailabilityMatrix();

        private static List<AvailabilityMatrix> getAllFromAvailabilityMatrix() {
            List<AvailabilityMatrix> result = new ArrayList<AvailabilityMatrix>();
            result.add(AvailabilityMatrix.TRAN_TYPE_ONE_NEW);
            result.add(AvailabilityMatrix.TRAN_TYPE_ONE_MOD);
            result.add(AvailabilityMatrix.TRAN_TYPE_IND_NEW);
            result.add(AvailabilityMatrix.TRAN_TYPE_IND_MOD);
            result.add(AvailabilityMatrix.TRAN_TYPE_MULT_NEW);
            result.add(AvailabilityMatrix.TRAN_TYPE_MULT_MOD);

            result.add(AvailabilityMatrix.ASSET_NUMBER_ONE_NEW);
            result.add(AvailabilityMatrix.ASSET_NUMBER_ONE_MOD);
            result.add(AvailabilityMatrix.ASSET_NUMBER_IND_NEW);
            result.add(AvailabilityMatrix.ASSET_NUMBER_IND_MOD);
            result.add(AvailabilityMatrix.ASSET_NUMBER_MULT_NEW);
            result.add(AvailabilityMatrix.ASSET_NUMBER_MULT_MOD);

            result.add(AvailabilityMatrix.COMMENTS_ONE_NEW);
            result.add(AvailabilityMatrix.COMMENTS_ONE_MOD);
            result.add(AvailabilityMatrix.COMMENTS_IND_NEW);
            result.add(AvailabilityMatrix.COMMENTS_IND_MOD);
            result.add(AvailabilityMatrix.COMMENTS_MULT_NEW);
            result.add(AvailabilityMatrix.COMMENTS_MULT_MOD);

            result.add(AvailabilityMatrix.NOT_CURRENT_FY_ONE_NEW);
            result.add(AvailabilityMatrix.NOT_CURRENT_FY_ONE_MOD);
            result.add(AvailabilityMatrix.NOT_CURRENT_FY_IND_NEW);
            result.add(AvailabilityMatrix.NOT_CURRENT_FY_IND_MOD);
            result.add(AvailabilityMatrix.NOT_CURRENT_FY_MULT_NEW);
            result.add(AvailabilityMatrix.NOT_CURRENT_FY_MULT_MOD);

            result.add(AvailabilityMatrix.ASSET_TYPE_ONE_NEW);
            result.add(AvailabilityMatrix.ASSET_TYPE_ONE_MOD);
            result.add(AvailabilityMatrix.ASSET_TYPE_IND_NEW);
            result.add(AvailabilityMatrix.ASSET_TYPE_IND_MOD);
            result.add(AvailabilityMatrix.ASSET_TYPE_MULT_NEW);
            result.add(AvailabilityMatrix.ASSET_TYPE_MULT_MOD);

            result.add(AvailabilityMatrix.MANUFACTURER_ONE_NEW);
            result.add(AvailabilityMatrix.MANUFACTURER_ONE_MOD);
            result.add(AvailabilityMatrix.MANUFACTURER_IND_NEW);
            result.add(AvailabilityMatrix.MANUFACTURER_IND_MOD);
            result.add(AvailabilityMatrix.MANUFACTURER_MULT_NEW);
            result.add(AvailabilityMatrix.MANUFACTURER_MULT_MOD);

            result.add(AvailabilityMatrix.MODEL_ONE_NEW);
            result.add(AvailabilityMatrix.MODEL_ONE_MOD);
            result.add(AvailabilityMatrix.MODEL_IND_NEW);
            result.add(AvailabilityMatrix.MODEL_IND_MOD);
            result.add(AvailabilityMatrix.MODEL_MULT_NEW);
            result.add(AvailabilityMatrix.MODEL_MULT_MOD);

            result.add(AvailabilityMatrix.DESCRIPTION_ONE_NEW);
            result.add(AvailabilityMatrix.DESCRIPTION_ONE_MOD);
            result.add(AvailabilityMatrix.DESCRIPTION_IND_NEW);
            result.add(AvailabilityMatrix.DESCRIPTION_IND_MOD);
            result.add(AvailabilityMatrix.DESCRIPTION_MULT_NEW);
            result.add(AvailabilityMatrix.DESCRIPTION_MULT_MOD);

            result.add(AvailabilityMatrix.LOC_QUANTITY_ONE_NEW);
            result.add(AvailabilityMatrix.LOC_QUANTITY_ONE_MOD);
            result.add(AvailabilityMatrix.LOC_QUANTITY_IND_NEW);
            result.add(AvailabilityMatrix.LOC_QUANTITY_IND_MOD);
            result.add(AvailabilityMatrix.LOC_QUANTITY_MULT_NEW);
            result.add(AvailabilityMatrix.LOC_QUANTITY_MULT_MOD);

            result.add(AvailabilityMatrix.LOC_ADDRESS_ONE_NEW);
            result.add(AvailabilityMatrix.LOC_ADDRESS_ONE_MOD);
            result.add(AvailabilityMatrix.LOC_ADDRESS_IND_NEW);
            result.add(AvailabilityMatrix.LOC_ADDRESS_IND_MOD);
            result.add(AvailabilityMatrix.LOC_ADDRESS_MULT_NEW);
            result.add(AvailabilityMatrix.LOC_ADDRESS_MULT_MOD);

            result.add(AvailabilityMatrix.HOW_MANY_ASSETS_ONE_NEW);
            result.add(AvailabilityMatrix.HOW_MANY_ASSETS_ONE_MOD);
            result.add(AvailabilityMatrix.HOW_MANY_ASSETS_IND_NEW);
            result.add(AvailabilityMatrix.HOW_MANY_ASSETS_IND_MOD);
            result.add(AvailabilityMatrix.HOW_MANY_ASSETS_MULT_NEW);
            result.add(AvailabilityMatrix.HOW_MANY_ASSETS_MULT_MOD);

            return result;
        }
    }

    public static final class CAMSWarningStatuses {
        public static final Set<String> REQUISITION_STATUS_WARNING_NO_CAMS_DATA = getRequisitionStatusCAMSWarnings();
        public static final Set<String> PURCHASEORDER_STATUS_WARNING_NO_CAMS_DATA = getPurchaseOrderStatusCAMSWarnings();
        public static final Set<String> PAYMENT_REQUEST_STATUS_WARNING_NO_CAMS_DATA = getPurchaseOrderStatusCAMSWarnings();
        public static final Set<String> CREDIT_MEMO_STATUS_WARNING_NO_CAMS_DATA = getPurchaseOrderStatusCAMSWarnings();

        private static HashSet<String> getRequisitionStatusCAMSWarnings() {
            HashSet<String> statuses = new HashSet<String>();
            statuses.add(RequisitionStatuses.APPDOC_IN_PROCESS);
            statuses.add(RequisitionStatuses.APPDOC_AWAIT_CONTENT_REVIEW);
            statuses.add(RequisitionStatuses.APPDOC_AWAIT_FISCAL_REVIEW);
            return statuses;
        }
        private static HashSet<String> getPurchaseOrderStatusCAMSWarnings() {
            HashSet<String> statuses = new HashSet<String>();
            statuses.add(PurchaseOrderStatuses.APPDOC_IN_PROCESS);
            statuses.add(PurchaseOrderStatuses.APPDOC_AWAIT_PURCHASING_REVIEW);
            return statuses;
        }
        private static HashSet<String> getPaymentRequestStatusCAMSWarnings() {
            HashSet<String> statuses = new HashSet<>();
            statuses.add(PaymentRequestStatuses.APPDOC_IN_PROCESS);
            return statuses;
        }
        private static HashSet<String> getCreditMemoStatusCAMSWarnings() {
            HashSet<String> statuses = new HashSet<>();
            statuses.add(CreditMemoStatuses.APPDOC_IN_PROCESS);
            return statuses;
        }
    }

    public static final class AttachmentTypeCodes{
        public static final String ATTACHMENT_TYPE_CM_IMAGE = "Credit Memo Image";
        public static final String ATTACHMENT_TYPE_CONTRACTS = "Contracts";
        public static final String ATTACHMENT_TYPE_CONTRACT_AMENDMENTS = "Contract Amendments";
        public static final String ATTACHMENT_TYPE_OTHER = "Other";
        public static final String ATTACHMENT_TYPE_OTHER_RESTRICTED = "Other - Restricted";
        public static final String ATTACHMENT_TYPE_INVOICE_IMAGE = "Invoice Image";
        public static final String ATTACHMENT_TYPE_QUOTE = "Quotes";
        public static final String ATTACHMENT_TYPE_RFP = "RFPs";
        public static final String ATTACHMENT_TYPE_RFP_RESPONSES = "RFP Responses";
        public static final String ATTACHMENT_TYPE_TAX_DOCUMENTS = "Tax Documents";
    }

    public static final class AccountDistributionMethodCodes {
        public static final String PROPORTIONAL_CODE = "P";
    }
   
}
