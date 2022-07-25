/**
 * 
 */
package edu.cornell.kfs.module.purap;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.module.purap.PaymentRequestStatuses;

/**
 * Cornell University specific constants class for holding and defining constants necessary for Cornell's implementation of the Kuali Financial System.
 *
 */
public class CUPurapConstants {
    
    public static final String SPECIAL_HANDLING_NOTE_LINE_1_NAME = "Send Check To:";
    public static final String SPECIAL_HANDLING_NOTE_LINE_2_ADDRESS = "SH1:";
    public static final String SPECIAL_HANDLING_NOTE_LINE_3_CITY_STATE_ZIP = "SH3:";
    public static final String PURAP_NOTES_IDENTIFIER = "::";
    public static final String PAYMENT_METHODL_REVIEW = "PTMA";
    public static final String B2B_HIGHER_LIMIT_PERMISSION = "B2B Higher APO Limit";

    public static class RequisitionStatuses {
        public static final String AWAIT_CONTRACTS_GRANTS_REVIEW = "WCG";
        public static final String DAPRVD_CONTRACTS_GRANTS = "DCG";
        
    }
           
    public static class RequisitionSources {
        public static final String IWNT = "IWNT";
    }
    
    public static class PurchaseOrderStatuses {
        public static final String AWAIT_FISCAL_REVIEW = "AFIS";
        public static final String DAPRVD_FISCAL = "DFIS";
        public static final String PENDING_CXML = "CXPE";
        // KFSUPGRADE-411
        public static final String CXML_ERROR = "CXER";
        public static final String OPEN = "OPEN";
        public static final String VOID = "VOID";
        //end KFSUPGRADE-411
        // KFSUPGRADE-336.  override purapconstants
            public static final String NODE_DOCUMENT_TRANSMISSION = "JoinRequiresBudgetReview";
            public static final String APPDOC_AWAITING_FISCAL_REVIEW = "Awaiting Fiscal Officer";
    }
    
    public static class PODocumentsStrings {
        // KFSPTS-794
        public static final String CONFIRM_ATT_SEND_CHANGE_QUESTION = "POChangeAtt";
        public static final String CONFIRM_ATT_SEND_CHANGE_RETURN = "changeAtt";
    }       
    
    public enum STATUS_ORDER {
        CANCELLED_IN_PROCESS(PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS, false),
        CANCELLED_POST_AP_APPROVE(PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, false),
        INITIATE(PaymentRequestStatuses.APPDOC_INITIATE, true),
        IN_PROCESS(PaymentRequestStatuses.APPDOC_IN_PROCESS, true),
        AWAITING_ACCOUNTS_PAYABLE_REVIEW(PaymentRequestStatuses.APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, false),
        AWAITING_RECEIVING_REVIEW(PaymentRequestStatuses.APPDOC_AWAITING_RECEIVING_REVIEW, false),
        AWAITING_SUB_ACCT_MGR_REVIEW(PaymentRequestStatuses.APPDOC_AWAITING_SUB_ACCT_MGR_REVIEW, false),
        AWAITING_FISCAL_REVIEW(PaymentRequestStatuses.APPDOC_AWAITING_FISCAL_REVIEW, false),
        AWAITING_ORG_REVIEW(PaymentRequestStatuses.APPDOC_AWAITING_ORG_REVIEW, false),
        AWAITING_TAX_REVIEW(PaymentRequestStatuses.APPDOC_AWAITING_TAX_REVIEW, false),
        DEPARTMENT_APPROVED(PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED, false),        
        // KFSPTS-1891
        PAYMENT_METHOD_REVIEW(CUPurapConstants.PAYMENT_METHODL_REVIEW, false), 
        AUTO_APPROVED(PaymentRequestStatuses.APPDOC_AUTO_APPROVED, false), ;

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
    }
    
    public static class PaymentRequestDefaults {
        public static final String DEFAULT_PROCESSING_CAMPUS_CODE = "IT";
    }
    
    public static final class CUPaymentRequestStatuses {

        public static final HashSet<String> STATUSES_ALLOWING_AUTO_CLOSE = new HashSet<String>();
       
        
        static {
            STATUSES_ALLOWING_AUTO_CLOSE.add(PaymentRequestStatuses.APPDOC_DEPARTMENT_APPROVED);
            STATUSES_ALLOWING_AUTO_CLOSE.add(PaymentRequestStatuses.APPDOC_AUTO_APPROVED);
            STATUSES_ALLOWING_AUTO_CLOSE.addAll(PaymentRequestStatuses.CANCELLED_STATUSES);
  
        }
    }
    
    public static final class IWantDocumentSteps {
        public static final String CUSTOMER_DATA_STEP = "customerDataStep";
        public static final String ITEMS_AND_ACCT_DATA_STEP = "itemAndAcctDataStep";
        public static final String VENDOR_STEP = "vendorStep";
        public static final String ROUTING_STEP = "routingStep";
        public static final String REGULAR = "regular";
    }
    
    public static final String IWNT_DOC_TYPE = "IWNT";
    public static final String IWNT_STEP_PARAMETER = "step";
    
    public static final String IWNT_DOC_CREATE_REQ = "createReq";
    public static final String IWNT_DOC_CREATE_DV = "createDV";
    public static final String IWNT_DOC_USE_LOOKUPS = "iwntUseLookups";
    
    public static final String USER_OPTIONS_PRINCIPAL_ID = "principalId";
    public static final String USER_OPTIONS_OPTION_ID = "optionId";
    
    public static final String USER_OPTIONS_DEFAULT_COLLEGE = "DEFAULT_COLLEGE";
    public static final String USER_OPTIONS_DEFAULT_DEPARTMENT = "DEFAULT_DEPARTMENT";
    public static final String USER_OPTIONS_DEFAULT_DELIVER_TO_NET_ID = "DEFAULT_DELIVER_TO_NET_ID";
    public static final String USER_OPTIONS_DEFAULT_DELIVER_TO_NAME = "DEFAULT_DELIVER_TO_NAME";
    public static final String USER_OPTIONS_DEFAULT_DELIVER_TO_EMAIL_ADDRESS = "DEFAULT_DELIVER_TO_EMAIL_ADDRESS";
    public static final String USER_OPTIONS_DEFAULT_DELIVER_TO_PHONE_NUMBER = "DEFAULT_DELIVER_TO_PHONE_NUMBER";
    public static final String USER_OPTIONS_DEFAULT_DELIVER_TO_ADDRESS = "DEFAULT_DELIVER_TO_ADDRESS";
    
    public static final String AMOUNT = "AMOUNT";
    public static final String PERCENT = "PERCENT";
    
    //KFSPTS-1458:  
    //This email address is ONLY used when the system cannot retrieve or find parameter
    //MANUAL_DISTRIBUTION_EMAIL OR if the value of that parameter is null or a zero length string.
    public static final String MANUAL_DISRIBUTION_FAILSAFE_EMAIL_ADDRESS = "procurement@cornell.edu";
    
    
    //KFSPTS-1458
    //Route nodes that should bypass data validation because users at these nodes do NOT 
    //have the ability to edit the data on the form.
    public static final class MethodOfPOTransmissionByPassValidationNodes {
    	public static final String ACCOUNT_NODE = "Account";
    	public static final String ACCOUNTING_ORGANIZATION_HIERARCHY_NODE = "AccountingOrganizationHierarchy";
    	public static final String AD_HOC_NODE = "AdHoc";
    	public static final String AWARD_NODE = "Award";
    	public static final String COMMODITY_APO_NODE = "CommodityAPO";
    	public static final String COMMODITY_NODE = "Commodity";
    	public static final String SEPARATION_OF_DUTIES_NODE = "SeparationOfDuties";
    	public static final String SUB_ACCOUNT_NODE = "SubAccount";
    }
    
    //KFSPTS-794
    //Defined by CSU as part of their provided mod.
    public static final String MIME_BOUNDARY_FOR_ATTACHMENTS = "-----------------------------MIME_BOUNDARY_FOR_ATTACHMENTS";
    
    //KFSPTS-794
    public static final class AttachemntToVendorIndicators {
    	public static final String DONT_SEND_TO_VENDOR = "dontSendToVendor";
    	public static final String SEND_TO_VENDOR = "sendToVendor";
    }
    public static final class POTransmissionMethods {
        public static final String FAX = "FAX";
        public static final String PRINT = "PRIN";
        public static final String NOPRINT = "NOPR";
        public static final String ELECTRONIC = "ELEC";
        public static final String EMAIL = "EMAL";
        public static final String MANUAL = "MANL";
        public static final String CONVERSION = "CNVS";
    }
    
    public static final class PurapFundingSources {
        public static final String FEDERAL_FUNDING_SOURCE = "FEDR";
    }
    
    public static class ElectronicInvoice {
     // ELECTRONIC INVOICE DISCOUNT DESCRIPTION
        public static String DISCOUNT_DESCRIPTION = "Full Order Discount";

    }
    
    public static final String B2B_SUBMIT_ESHOP_CART_PERMISSION = "B2B Submit Eshop Cart";
	public static final String B2B_SHOPPER_OFFICE_PERMISSION = "B2B Shopper Office";
	public static final String B2B_SHOPPER_LAB_PERMISSION = "B2B Shopper Lab";
	public static final String B2B_SHOPPER_FACILITIES_PERMISSION = "B2B Shopper Facilities";
 
	//assign cart roles
	public static final String SCIQUEST_ROLE_SHOPPER = "Shopper";
	public static final String SCIQUEST_ROLE_BUYER = "Buyer";
	
	//view roles
	public static final String SCIQUEST_ROLE_OFFICE = "Office";
	public static final String SCIQUEST_ROLE_LAB = "Lab";
	public static final String SCIQUEST_ROLE_FACILITIES = "Facilities";
	public static final String SCIQUEST_ROLE_UNRESTRICTED= "Unrestricted";

	
    public static final String PREAUTHORIZED = "Preauthorized";
    public static final String NON_PREAUTHORIZED = "NonPreauthorized";
    
    public static final String MAX_SQ_NO_ATTACHMENTS = "MAX_SQ_NO_ATTACHMENTS";

    // KFSPTS-1705
    public static final String PO_FINAL_ANNOTATION_TEXT = "message.document.purap.final.annotation";
    public static final String PO_DISAPPROVAL_ANNOTATION_TEXT = "message.document.purap.disapprove.annotation";
    public static final String PO_CANCEL_ANNOTATION_TEXT = "message.document.purap.cancel.annotation";

    public static final String RECEIVING_BUTTON_ALT_TEXT = "Receiving";
    
    //KFSUPGRADE-779
    public static final String LABEL_BANK_NAME = "Bank Name";
    public static final String LABEL_BANK_CITY = "Bank City";
    public static final String LABEL_BANK_COUNTRY = "Bank Country";
    public static final String LABEL_CURRENCY = "Currency";
    public static final String LABEL_BANK_ACCT_NUMBER = "Bank Account#";
    public static final String LABEL_BANK_ACCT_NAME = "Bank Account Name";
    public static final String PAYMENT_METHOD = "payment method";
    public static final String WIRE_TRANSFER = "Wire Transfer";
    
    //KFSPTS-3718
    public static final String I_WANT_DOC_FEED_FILE_PREFIX = "purap_iwantdoc_";
    public static final String I_WANT_DOC_FEED_FILE_TYPE_INDENTIFIER = "iWantDocumentInputFileType";

    public static final class Einvoice {
        public static final String OBJECT_NOT_FOUND = "Object not found";
        public static final String ERROR = "error";
        public static final String DUNS = "duns";
        public static final String VENDOR_NAME = "vendor_name";
        public static final String DOCUMENT_NUMBER = "document_number";
        public static final String DOCUMENT_STATUS = "document_status";
        public static final String EMAIL = "email";
        public static final String SHIPPING_NAME = "shipping_name";
        public static final String SHIPPING_DELIVER_TO = "shipping_deliver_to";
        public static final String ADDRESS_LINE1 = "address_line1";
        public static final String ADDRESS_LINE2 = "address_line2";
        public static final String ROOM_NUMBER_LINE = "room_number_line";
        public static final String STATE = "state";
        public static final String CITY = "city";
        public static final String ZIPCODE = "zipcode";
        public static final String COUNTRY_CODE = "country_code";
        public static final String COUNTRY_NAME = "country_name";
        public static final String ADDRESS_TYPE_CODE = "address_type_code";
        public static final String UNIT_OF_MEASURE = "unit_of_measure";
        public static final String UNITS_OF_MEASURE = "units_of_measure";
        public static final String DESCRIPTION = "description";
        public static final String UNIT_PRICE = "unit_price";
        public static final String LINE_NUMBER = "line_number";
        public static final String PART_NUMBER = "part_number";
        public static final String ITEMS = "items";
        public static final String UNAUTHORIZED = "Unauthorized";
        public static final String EINVOICE = "EINVOICE";
        public static final String EINVOICE_API_KEY_CREDENTIAL_NAME = "einvoice_api_key";
        public static final String EINVOICE_KFS_API_DESCRIPTION = "The Cornell eInvoice web tool uses this resource to retrieve data from KFS.";
        public static final String EINVOICE_VENDOR_BAD_REQUEST = "Invalid VendorNumber";
        public static final String EINVOICE_GET_VENDORS_BAD_REQUEST = "Expecting vendor_numbers in url query parameters";
        public static final String VENDOR_NUMBERS = "vendor_numbers";
        public static final String VENDOR_GENERATED_HEADER_ID = "VNDR_HDR_GNRTD_ID";
        public static final String VENDOR_DETAIL_ASSIGNED_ID = "VNDR_DTL_ASND_ID";
        public static final String ITEM_TYPE_DESCRIPTION = "item_type_description";
        public static final String ITEM_TYPE_CODE = "item_type_code";
        public static final String PO_ITEM_ID = "po_item_id";
        public static final String INVOICED_AMOUNT = "invoiced_amount";
        public static final String AMOUNT = "amount";
        public static final String PO_QUANTITY = "po_quantity";
        public static final String INVOICE_QUANTITY = "invoice_quantity";
        public static final String NULL = "null";
        public static final String ROOM_NUMBER_PREFIX = "Room ";
        public static final String VENDORS = "vendors";
    }
    
    public static final class JaggaerLinkMappingFieldNames {
        public static final String JAGGAER_ROLE_NAME = "jaggaerRoleName";
        public static final String ESHOP_LINK = "eShopLink";
        public static final String CONTRACTS_PLUS_LINK = "contractsPlusLink";
        public static final String JAGGAER_ADMIN_LINK = "jaggaerAdminLink";
        public static final String ACTIVE = "active";
    }
    
    public enum JaggaerRoleSet {
        ESHOP(JaggaerLinkMappingFieldNames.ESHOP_LINK),
        CONTRACTS_PLUS(JaggaerLinkMappingFieldNames.CONTRACTS_PLUS_LINK),
        ADMINISTRATOR(JaggaerLinkMappingFieldNames.JAGGAER_ADMIN_LINK);
        
        public final String linkMappingFieldName;
        
        private JaggaerRoleSet(String linkMappingFieldName) {
            this.linkMappingFieldName = linkMappingFieldName;
        }
    }
}
