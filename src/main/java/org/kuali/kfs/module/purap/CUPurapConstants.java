/**
 * 
 */
package org.kuali.kfs.module.purap;

import java.util.HashSet;

/**
 * Cornell University specific constants class for holding and defining constants necessary for Cornell's implementation of the Kuali Financial System.
 *
 */
public class CUPurapConstants extends PurapConstants {

    private static final long serialVersionUID = 1L;

    public static class PaymentRequestDefaults {
        public static final String DEFAULT_PROCESSING_CAMPUS_CODE = "IT";
    }
    
    public static final class CUPaymentRequestStatuses {

        public static final HashSet<String> STATUSES_ALLOWING_AUTO_CLOSE = new HashSet<String>();

        static {
            STATUSES_ALLOWING_AUTO_CLOSE.add(PaymentRequestStatuses.DEPARTMENT_APPROVED);
            STATUSES_ALLOWING_AUTO_CLOSE.add(PaymentRequestStatuses.AUTO_APPROVED);
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
    
    public static final String IWNT_DOC_CREATE_REQ = "createReq";
    public static final String IWNT_DOC_USE_LOOKUPS = "iwntUseLookups";
    
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
    
    public static final String B2B_HIGHER_LIMIT_PERMISSION = "B2B Higher APO Limit";

}
