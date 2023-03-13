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
package org.kuali.kfs.module.purap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

public final class PurchaseOrderStatuses {

    // Added for updating app doc status for disapproved
    public static final String APPDOC_DAPRVD_UNORDERED_ITEM = "Disapproved New Unordered Item Review";
    public static final String APPDOC_DAPRVD_PURCHASING = "Disapproved Purchasing";
    public static final String APPDOC_DAPRVD_OBJECT_CODE = "Disapproved Object Code";
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
    public static final String APPDOC_AWAIT_OBJECT_CODE_REVIEW = "Awaiting Object Code Approval";
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
    public static final String NODE_OBJECT_CODE_REVIEW = "ObjectCode";
    public static final String NODE_COMMODITY_CODE_REVIEW = "Commodity";
    // CU Customization: Added new CommodityAPO node
    public static final String NODE_COMMODITY_CODE_APO_REVIEW = "CommodityAPO";
    // End CU Customization
    public static final String NODE_CONTRACTS_AND_GRANTS_REVIEW = "Award";
    public static final String NODE_BUDGET_OFFICE_REVIEW = "Budget";
    public static final String NODE_VENDOR_TAX_REVIEW = "Tax";
    public static final String NODE_DOCUMENT_TRANSMISSION = "JoinVendorIsEmployeeOrNonresident";

    public static final Set<String> INCOMPLETE_STATUSES = new HashSet<>();
    public static final Set<String> COMPLETE_STATUSES = new HashSet<>();

    public static final Map<String, String> STATUSES_BY_TRANSMISSION_TYPE = getStatusesByTransmissionType();

    /**
     * Private Constructor since this is a constants class that should never be instantiated.
     */
    private PurchaseOrderStatuses() {
    }

    public static Map<String, String> getAllAppDocStatuses() {
        Map<String, String> appDocStatusMap = new HashMap<>();

        appDocStatusMap.put(APPDOC_DAPRVD_UNORDERED_ITEM, APPDOC_DAPRVD_UNORDERED_ITEM);
        appDocStatusMap.put(APPDOC_DAPRVD_PURCHASING, APPDOC_DAPRVD_PURCHASING);
        appDocStatusMap.put(APPDOC_DAPRVD_OBJECT_CODE, APPDOC_DAPRVD_OBJECT_CODE);
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
        appDocStatusMap.put(APPDOC_AWAIT_OBJECT_CODE_REVIEW, APPDOC_AWAIT_OBJECT_CODE_REVIEW);
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

    public static Map<String, String> getPurchaseOrderAppDocDisapproveStatuses() {
        Map<String, String> poAppDocStatusMap = new HashMap<>();

        poAppDocStatusMap.put(NODE_ADHOC_REVIEW, PurchaseOrderStatuses.APPDOC_CANCELLED);
        poAppDocStatusMap
                .put(NODE_AWAIT_NEW_UNORDERED_ITEM_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_UNORDERED_ITEM);
        poAppDocStatusMap.put(NODE_INTERNAL_PURCHASING_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_PURCHASING);
        poAppDocStatusMap.put(NODE_OBJECT_CODE_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_OBJECT_CODE);
        poAppDocStatusMap.put(NODE_COMMODITY_CODE_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_COMMODITY_CODE);
        // CU Customization: Added mapping for NODE_COMMODITY_CODE_APO_REVIEW
        poAppDocStatusMap.put(NODE_COMMODITY_CODE_APO_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_COMMODITY_CODE);
        // End CU Customization
        poAppDocStatusMap.put(NODE_CONTRACTS_AND_GRANTS_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_CG_APPROVAL);
        poAppDocStatusMap.put(NODE_BUDGET_OFFICE_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_BUDGET);
        poAppDocStatusMap.put(NODE_VENDOR_TAX_REVIEW, PurchaseOrderStatuses.APPDOC_DAPRVD_TAX);
        poAppDocStatusMap.put(PurchaseOrderStatuses.APPDOC_CANCELLED, PurchaseOrderStatuses.APPDOC_CANCELLED);
        poAppDocStatusMap.put(PurchaseOrderStatuses.APPDOC_VOID, PurchaseOrderStatuses.APPDOC_VOID);
        poAppDocStatusMap.put(PurchaseOrderStatuses.APPDOC_IN_PROCESS, PurchaseOrderStatuses.APPDOC_IN_PROCESS);
        poAppDocStatusMap.put(PurchaseOrderStatuses.APPDOC_CLOSED, PurchaseOrderStatuses.APPDOC_CLOSED);
        // CU Customization: Added three new mappings for KFSUPGRADE-345
        poAppDocStatusMap.put(RequisitionStatuses.NODE_ORG_REVIEW, RequisitionStatuses.APPDOC_DAPRVD_CHART);
        poAppDocStatusMap.put(RequisitionStatuses.NODE_ACCOUNT, RequisitionStatuses.APPDOC_DAPRVD_FISCAL);
        poAppDocStatusMap.put(RequisitionStatuses.NODE_SUBACCOUNT,  RequisitionStatuses.APPDOC_DAPRVD_SUB_ACCT);
        // End CU Customization

        return poAppDocStatusMap;
    }

    static {
        INCOMPLETE_STATUSES.add(APPDOC_AWAIT_TAX_REVIEW);
        INCOMPLETE_STATUSES.add(APPDOC_AWAIT_BUDGET_REVIEW);
        INCOMPLETE_STATUSES.add(APPDOC_AWAIT_CONTRACTS_GRANTS_REVIEW);
        INCOMPLETE_STATUSES.add(APPDOC_AWAIT_PURCHASING_REVIEW);
        INCOMPLETE_STATUSES.add(APPDOC_AWAIT_NEW_UNORDERED_ITEM_REVIEW);
        INCOMPLETE_STATUSES.add(APPDOC_AWAIT_OBJECT_CODE_REVIEW);
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
        COMPLETE_STATUSES.add(APPDOC_DAPRVD_OBJECT_CODE);
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
     * Do not include 'OPEN' status in this map. The 'OPEN' status is the default status that is set when no status
     * exists for a particular pending transmission type code.
     */
    private static Map<String, String> getStatusesByTransmissionType() {
        // TODO: Consider making this a constant field.
        return Map.ofEntries(
                entry(PurapConstants.POTransmissionMethods.PRINT, APPDOC_PENDING_PRINT),
                entry(PurapConstants.POTransmissionMethods.ELECTRONIC, APPDOC_PENDING_CXML),
                entry(PurapConstants.POTransmissionMethods.FAX, APPDOC_PENDING_FAX)
        );
    }
}
