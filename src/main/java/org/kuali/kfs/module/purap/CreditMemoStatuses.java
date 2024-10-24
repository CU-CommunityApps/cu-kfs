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
package org.kuali.kfs.module.purap;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public final class CreditMemoStatuses {

    public static final String APPDOC_INITIATE = "Initiated";
    public static final String APPDOC_IN_PROCESS = "In Process";
    public static final String APPDOC_CANCELLED_IN_PROCESS = "Cancelled In Process";
    public static final String APPDOC_CANCELLED_PRIOR_TO_AP_APPROVAL = "Void";
    public static final String APPDOC_CANCELLED_POST_AP_APPROVE = "Cancelled";
    public static final String APPDOC_COMPLETE = "Complete";
    public static final String APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW = "Awaiting AP Review";
    // CU Customization: Added new APPDOC status
    public static final String APPDOC_PAYMENT_METHOD_REVIEW = "Awaiting Treasury Manager Approval";

    public static final String NODE_ADHOC_REVIEW = "AdHoc";
    public static final String NODE_ACCOUNT_REVIEW = "Account";
    // CU Customization: Added new node for KFSUPGRADE-779
    public static final String NODE_PAYMENT_METHOD_REVIEW = "PaymentMethod";

    public static final String[] STATUSES_ALLOWED_FOR_EXTRACTION =
        {APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, APPDOC_COMPLETE};

    public static final String[] STATUSES_POTENTIALLY_ACTIVE =
        {APPDOC_IN_PROCESS, APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW};

    public static final Set<String> CANCELLED_STATUSES = new HashSet<>();
    public static final Set<String> STATUSES_DISALLOWING_HOLD = new HashSet<>();
    public static final Set<String> STATUSES_NOT_REQUIRING_ENTRY_REVERSAL = new HashSet<>();

    /**
     * Private Constructor since this is a constants class that should never be instantiated.
     */
    private CreditMemoStatuses() {
    }

    public enum STATUS_ORDER {
        CANCELLED_IN_PROCESS(CreditMemoStatuses.APPDOC_CANCELLED_IN_PROCESS, false),
        CANCELLED_PRIOR_TO_AP_APPROVAL(CreditMemoStatuses.APPDOC_CANCELLED_PRIOR_TO_AP_APPROVAL, false),
        CANCELLED_POST_AP_APPROVE(CreditMemoStatuses.APPDOC_CANCELLED_POST_AP_APPROVE, false),
        INITIATE(CreditMemoStatuses.APPDOC_INITIATE, true),
        IN_PROCESS(CreditMemoStatuses.APPDOC_IN_PROCESS, true),
        AWAITING_ACCOUNTS_PAYABLE_REVIEW(CreditMemoStatuses.APPDOC_AWAITING_ACCOUNTS_PAYABLE_REVIEW, false),
        // CU Customization: Added PAYMENT_METHOD_REVIEW constant
        PAYMENT_METHOD_REVIEW(CreditMemoStatuses.APPDOC_PAYMENT_METHOD_REVIEW, false),
        // End CU Customization
        COMPLETE(CreditMemoStatuses.APPDOC_COMPLETE, false);

        private final String statusCode;
        private final boolean fullEntryAllowed;

        STATUS_ORDER(final String statusCode, final boolean fullEntry) {
            this.statusCode = statusCode;
            fullEntryAllowed = fullEntry;
        }

        public static STATUS_ORDER getByStatusCode(final String statusCode) {
            for (final STATUS_ORDER status : STATUS_ORDER.values()) {
                if (StringUtils.equals(status.statusCode, statusCode)) {
                    return status;
                }
            }
            return null;
        }

        public static boolean isFullDocumentEntryCompleted(final String status) {
            return !getByStatusCode(status).fullEntryAllowed;
        }

        public static STATUS_ORDER getPreviousStatus(final String statusCode) {
            final STATUS_ORDER statusOrder = STATUS_ORDER.getByStatusCode(statusCode);
            if (statusOrder.ordinal() > 0) {
                return STATUS_ORDER.values()[statusOrder.ordinal() - 1];
            }
            return null;
        }

        public static boolean isFirstFullEntryStatus(final String statusCode) {
            // NOTE this won't work if there endsup being two ways to get to the first full entry status
            // (i.e. like AUTO/DEPT for final)
            return getByStatusCode(statusCode).fullEntryAllowed && !getPreviousStatus(statusCode).fullEntryAllowed;
        }
    }

    static {
        CANCELLED_STATUSES.add(APPDOC_CANCELLED_IN_PROCESS);
        CANCELLED_STATUSES.add(APPDOC_CANCELLED_PRIOR_TO_AP_APPROVAL);
        CANCELLED_STATUSES.add(APPDOC_CANCELLED_POST_AP_APPROVE);

        STATUSES_DISALLOWING_HOLD.add(APPDOC_INITIATE);
        STATUSES_DISALLOWING_HOLD.add(APPDOC_IN_PROCESS);
        STATUSES_DISALLOWING_HOLD
                .addAll(Arrays.asList(CANCELLED_STATUSES.toArray(new String[CANCELLED_STATUSES.size()])));

        STATUSES_NOT_REQUIRING_ENTRY_REVERSAL.add(APPDOC_INITIATE);
        STATUSES_NOT_REQUIRING_ENTRY_REVERSAL.add(APPDOC_IN_PROCESS);
        STATUSES_NOT_REQUIRING_ENTRY_REVERSAL
                .addAll(Arrays.asList(CANCELLED_STATUSES.toArray(new String[CANCELLED_STATUSES.size()])));
    }

    public static HashMap<String, String> getCreditMemoAppDocDisapproveStatuses() {
        final HashMap<String, String> appDocStatusMap = new HashMap<>();

        appDocStatusMap.put(NODE_ADHOC_REVIEW, APPDOC_CANCELLED_IN_PROCESS);
        appDocStatusMap.put(PurapConstants.AccountsPayableStatuses.NODE_ACCOUNT_PAYABLE_REVIEW,
                APPDOC_CANCELLED_PRIOR_TO_AP_APPROVAL);
        appDocStatusMap.put(NODE_ACCOUNT_REVIEW, APPDOC_CANCELLED_POST_AP_APPROVE);

        return appDocStatusMap;
    }
}
