package edu.cornell.kfs.module.purap;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.module.purap.PaymentRequestStatuses;
import org.kuali.kfs.module.purap.PurapWorkflowConstants;


public class CUPurapWorkflowConstants {

	  public static final String B2B_AUTO_PURCHASE_ORDER = "B2BAutoPurchaseOrder";
	  // KFSPTS-1891
	  public static final String TREASURY_MANAGER = "TreasuryManager";
    
	  
	  
	  public interface NodeDetails {
	        public String getName();

	        public String getAwaitingStatusCode();

	        public String getDisapprovedStatusCode();

	        public NodeDetails getPreviousNodeDetails();

	        public NodeDetails getNextNodeDetails();

	        public NodeDetails getNodeDetailByName(String name);

	        public int getOrdinal();
	    }
	  
	  public static class PaymentRequestDocument {
	        public enum NodeDetailEnum implements NodeDetails {
	            ADHOC_REVIEW(PurapWorkflowConstants.DOC_ADHOC_NODE_NAME, null, PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS, false), 
	            ACCOUNTS_PAYABLE_REVIEW("ImageAttachment", PaymentRequestStatuses.APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS, false), 
	            AWAITING_RECEIVING_REVIEW("PurchaseWasReceived", PaymentRequestStatuses.APPDOC_AWAITING_RECEIVING_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, false), 
	            RECEIVING("Receiving", PaymentRequestStatuses.APPDOC_AWAITING_RECEIVING_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, false),
	            SUB_ACCOUNT_REVIEW("SubAccount", PaymentRequestStatuses.APPDOC_AWAITING_SUB_ACCT_MGR_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, false), 
	            ACCOUNT_REVIEW("Account", PaymentRequestStatuses.APPDOC_AWAITING_FISCAL_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, true), 
	            ORG_REVIEW("AccountingOrganizationHierarchy", PaymentRequestStatuses.APPDOC_AWAITING_ORG_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, false), 
	            // KFSPTS-1891
	            PAYMENT_METHOD_REVIEW("PaymentMethod", CUPurapConstants.PAYMENT_METHODL_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, true), 
	            VENDOR_TAX_REVIEW("Tax", PaymentRequestStatuses.APPDOC_AWAITING_TAX_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, true);
	            
	            private final String name;
	            private final String awaitingStatusCode;
	            private final String disapprovedStatusCode;
	            private final boolean correctingGeneralLedgerEntriesRequired;

	            NodeDetailEnum(String name, String awaitingStatusCode, String disapprovedStatusCode, boolean correctingGeneralLedgerEntriesRequired) {
	                this.name = name;
	                this.awaitingStatusCode = awaitingStatusCode;
	                this.disapprovedStatusCode = disapprovedStatusCode;
	                this.correctingGeneralLedgerEntriesRequired = correctingGeneralLedgerEntriesRequired;
	            }

	            public String getName() {
	                return name;
	            }

	            public String getAwaitingStatusCode() {
	                return awaitingStatusCode;
	            }

	            public String getDisapprovedStatusCode() {
	                return disapprovedStatusCode;
	            }

	            public boolean isCorrectingGeneralLedgerEntriesRequired() {
	                return correctingGeneralLedgerEntriesRequired;
	            }

	            public NodeDetails getPreviousNodeDetails() {
	                if (this.ordinal() > 0) {
	                    return NodeDetailEnum.values()[this.ordinal() - 1];
	                }
	                return null;
	            }

	            public NodeDetails getNextNodeDetails() {
	                if (this.ordinal() < (NodeDetailEnum.values().length - 1)) {
	                    return NodeDetailEnum.values()[this.ordinal() + 1];
	                }
	                return null;
	            }

	            public NodeDetails getNodeDetailByName(String name) {
	                return getNodeDetailEnumByName(name);
	            }

	            public static NodeDetails getNodeDetailEnumByName(String name) {
	                for (NodeDetailEnum nodeDetailEnum : NodeDetailEnum.values()) {
	                    if (nodeDetailEnum.name.equals(name)) {
	                        return nodeDetailEnum;
	                    }
	                }
	                return null;
	            }

	            public static List<String> getNodesRequiringCorrectingGeneralLedgerEntries() {
	                List<String> returnEnumNames = new ArrayList<String>();
	                for (NodeDetailEnum currentEnum : NodeDetailEnum.values()) {
	                    if (currentEnum.isCorrectingGeneralLedgerEntriesRequired()) {
	                        returnEnumNames.add(currentEnum.getName());
	                    }
	                }
	                return returnEnumNames;
	            }

	            public int getOrdinal() {
	                return this.ordinal();
	            }
	        }
	    }
	 
}
