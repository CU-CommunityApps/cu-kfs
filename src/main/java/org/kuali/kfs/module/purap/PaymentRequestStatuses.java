package org.kuali.kfs.module.purap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public final class PaymentRequestStatuses {
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
        PAYMENT_METHOD_REVIEW(PaymentRequestStatuses.APPDOC_PAYMENT_METHOD_REVIEW, false),
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
        appDocStatusMap.put(PurapConstants.AccountsPayableStatuses.NODE_ACCOUNT_PAYABLE_REVIEW, PaymentRequestStatuses.APPDOC_CANCELLED_IN_PROCESS);
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