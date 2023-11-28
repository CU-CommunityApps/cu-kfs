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

import java.util.HashMap;
import java.util.Map;

public final class RequisitionStatuses {

    // Added for updating app doc status for disapproved
    public static final String APPDOC_IN_PROCESS = "In Process";
    public static final String APPDOC_CANCELLED = "Cancelled";
    public static final String APPDOC_CLOSED = "Closed";
    public static final String APPDOC_AWAIT_FISCAL_REVIEW = "Awaiting Fiscal Officer";
    public static final String APPDOC_AWAIT_CONTENT_REVIEW = "Awaiting Content Approval";
    public static final String APPDOC_AWAIT_HAS_ACCOUNTING_LINES = "Awaiting Accounting Lines";
    public static final String APPDOC_AWAIT_SUB_ACCT_REVIEW = "Awaiting Sub Account";
    public static final String APPDOC_AWAIT_CHART_REVIEW = "Awaiting Base Org Review";
    public static final String APPDOC_AWAIT_OBJECT_CODE_REVIEW = "Awaiting Object Code Review";
    // CU Customization: Changed doc status name to Awaiting Commodity Review
    public static final String APPDOC_AWAIT_COMMODITY_CODE_REVIEW = "Awaiting Commodity Review";
    // End CU Customization
    public static final String APPDOC_AWAIT_SEP_OF_DUTY_REVIEW = "Awaiting Separation of Duties";
    public static final String APPDOC_AWAIT_CONTRACT_MANAGER_ASSGN = "Awaiting Contract Manager Assignment";

    // CU Customization: Added new Disapproved Award status
    public static final String APPDOC_DAPRVD_AWARD = "Disapproved Award";
    // End CU Customization
    public static final String APPDOC_DAPRVD_CONTENT = "Disapproved Content";
    public static final String APPDOC_DAPRVD_HAS_ACCOUNTING_LINES = "Disapproved Accounting Lines";
    public static final String APPDOC_DAPRVD_SUB_ACCT = "Disapproved Sub Account";
    public static final String APPDOC_DAPRVD_FISCAL = "Disapproved Fiscal";
    public static final String APPDOC_DAPRVD_CHART = "Disapproved Base Org Review";
    public static final String APPDOC_DAPRVD_OBJECT_CODE = "Disapproved Object Code Review";
    // CU Customization: Changed doc status name to Disapproved Commodity Review
    public static final String APPDOC_DAPRVD_COMMODITY_CODE = "Disapproved Commodity Review";
    // End CU Customization
    public static final String APPDOC_DAPRVD_SEP_OF_DUTY = "Disapproved Separation of Duties";
    public static final String APPDOC_DAPRVD_AD_HOC = "Disapproved Ad Hoc";

    // Node Name Declarations
    // CU Customization: Added new Award node
    public static final String NODE_AWARD = "Award";
    // End CU Customization
    public static final String NODE_CONTENT_REVIEW = "Organization";
    public static final String NODE_SUBACCOUNT = "SubAccount";
    public static final String NODE_SEPARATION_OF_DUTIES = "SeparationOfDuties";
    public static final String NODE_ACCOUNT = "Account";
    public static final String NODE_OBJECT_CODE = "ObjectCode";
    public static final String NODE_HAS_ACCOUNTING_LINES = "Initiator";
    public static final String NODE_ORG_REVIEW = "AccountingOrganizationHierarchy";
    public static final String NODE_COMMODITY_CODE_REVIEW = "Commodity";
    // CU Customization: Added new CommodityAPO node
    public static final String NODE_COMMODITY_CODE_APO_REVIEW = "CommodityAPO";
    // End CU Customization
    public static final String NODE_ADHOC_REVIEW = "AdHoc";

    /**
     * Private Constructor since this is a constants class that should never be instantiated.
     */
    private RequisitionStatuses() {
    }

    public static Map<String, String> getAllAppDocStatuses() {
        final Map<String, String> appDocStatusMap = new HashMap<>();

        appDocStatusMap.put(APPDOC_IN_PROCESS, APPDOC_IN_PROCESS);
        appDocStatusMap.put(APPDOC_CANCELLED, APPDOC_CANCELLED);
        appDocStatusMap.put(APPDOC_CLOSED, APPDOC_CLOSED);
        appDocStatusMap.put(APPDOC_AWAIT_FISCAL_REVIEW, APPDOC_AWAIT_FISCAL_REVIEW);
        appDocStatusMap.put(APPDOC_AWAIT_CONTENT_REVIEW, APPDOC_AWAIT_CONTENT_REVIEW);
        appDocStatusMap.put(APPDOC_AWAIT_HAS_ACCOUNTING_LINES, APPDOC_AWAIT_HAS_ACCOUNTING_LINES);
        appDocStatusMap.put(APPDOC_AWAIT_SUB_ACCT_REVIEW, APPDOC_AWAIT_SUB_ACCT_REVIEW);
        appDocStatusMap.put(APPDOC_AWAIT_CHART_REVIEW, APPDOC_AWAIT_CHART_REVIEW);
        appDocStatusMap.put(APPDOC_AWAIT_OBJECT_CODE_REVIEW, APPDOC_AWAIT_OBJECT_CODE_REVIEW);
        appDocStatusMap.put(APPDOC_AWAIT_COMMODITY_CODE_REVIEW, APPDOC_AWAIT_COMMODITY_CODE_REVIEW);
        appDocStatusMap.put(APPDOC_AWAIT_SEP_OF_DUTY_REVIEW, APPDOC_AWAIT_SEP_OF_DUTY_REVIEW);
        appDocStatusMap.put(APPDOC_AWAIT_CONTRACT_MANAGER_ASSGN, APPDOC_AWAIT_CONTRACT_MANAGER_ASSGN);
        // CU Customization: Added mapping for APPDOC_DAPRVD_AWARD
        appDocStatusMap.put(APPDOC_DAPRVD_AWARD, APPDOC_DAPRVD_AWARD);
        // End CU Customization
        appDocStatusMap.put(APPDOC_DAPRVD_CONTENT, APPDOC_DAPRVD_CONTENT);
        appDocStatusMap.put(APPDOC_DAPRVD_HAS_ACCOUNTING_LINES, APPDOC_DAPRVD_HAS_ACCOUNTING_LINES);
        appDocStatusMap.put(APPDOC_DAPRVD_SUB_ACCT, APPDOC_DAPRVD_SUB_ACCT);
        appDocStatusMap.put(APPDOC_DAPRVD_FISCAL, APPDOC_DAPRVD_FISCAL);
        appDocStatusMap.put(APPDOC_DAPRVD_CHART, APPDOC_DAPRVD_CHART);
        appDocStatusMap.put(APPDOC_DAPRVD_OBJECT_CODE, APPDOC_DAPRVD_OBJECT_CODE);
        appDocStatusMap.put(APPDOC_DAPRVD_COMMODITY_CODE, APPDOC_DAPRVD_COMMODITY_CODE);
        appDocStatusMap.put(APPDOC_DAPRVD_SEP_OF_DUTY, APPDOC_DAPRVD_SEP_OF_DUTY);

        return appDocStatusMap;
    }

    public static Map<String, String> getRequistionAppDocStatuses() {
        final Map<String, String> reqAppDocStatusMap = new HashMap<>();

        // CU Customization: Added mapping for NODE_AWARD
        reqAppDocStatusMap.put(NODE_AWARD, APPDOC_DAPRVD_AWARD);
        // End CU Customization
        reqAppDocStatusMap.put(NODE_CONTENT_REVIEW, APPDOC_DAPRVD_CONTENT);
        reqAppDocStatusMap.put(NODE_HAS_ACCOUNTING_LINES, APPDOC_DAPRVD_HAS_ACCOUNTING_LINES);
        reqAppDocStatusMap.put(NODE_SUBACCOUNT, APPDOC_DAPRVD_SUB_ACCT);
        reqAppDocStatusMap.put(NODE_ACCOUNT, APPDOC_DAPRVD_FISCAL);
        reqAppDocStatusMap.put(NODE_ORG_REVIEW, APPDOC_DAPRVD_CHART);
        reqAppDocStatusMap.put(NODE_OBJECT_CODE, APPDOC_DAPRVD_OBJECT_CODE);
        reqAppDocStatusMap.put(NODE_COMMODITY_CODE_REVIEW, APPDOC_DAPRVD_COMMODITY_CODE);
        // CU Customization: Added mapping for NODE_COMMODITY_CODE_APO_REVIEW
        reqAppDocStatusMap.put(NODE_COMMODITY_CODE_APO_REVIEW, APPDOC_DAPRVD_COMMODITY_CODE);
        // End CU Customization
        reqAppDocStatusMap.put(NODE_SEPARATION_OF_DUTIES, APPDOC_DAPRVD_SEP_OF_DUTY);
        reqAppDocStatusMap.put(APPDOC_IN_PROCESS, APPDOC_IN_PROCESS);
        reqAppDocStatusMap.put(APPDOC_CLOSED, APPDOC_CLOSED);
        reqAppDocStatusMap.put(APPDOC_CANCELLED, APPDOC_CANCELLED);
        reqAppDocStatusMap.put(APPDOC_AWAIT_CONTRACT_MANAGER_ASSGN, APPDOC_AWAIT_CONTRACT_MANAGER_ASSGN);
        reqAppDocStatusMap.put(NODE_ADHOC_REVIEW, APPDOC_DAPRVD_AD_HOC);

        return reqAppDocStatusMap;
    }

}
