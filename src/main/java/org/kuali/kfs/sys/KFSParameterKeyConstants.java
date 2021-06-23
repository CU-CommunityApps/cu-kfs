/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.sys;

/**
 * CU Customization: Backported this file from the 2021-04-08 financials patch to bring in the FINP-7470 changes.
 * This overlay should be removed when we upgrade to the 2021-04-08 financials patch.
 * 
 * Parameter name constants for system parameters used by the kfs sys.
 */
public final class KFSParameterKeyConstants {

    public static final String ENABLE_BANK_SPECIFICATION_IND = "ENABLE_BANK_SPECIFICATION_IND";
    public static final String DEFAULT_BANK_BY_DOCUMENT_TYPE = "DEFAULT_BANK_BY_DOCUMENT_TYPE";
    public static final String BANK_CODE_DOCUMENT_TYPES = "BANK_CODE_DOCUMENT_TYPES";
    public static final String DEFAULT_CHART_CODE_METHOD = "DEFAULT_CHART_CODE_METHOD";
    public static final String DEFAULT_CHART_CODE = "DEFAULT_CHART_CODE";
    // Stuck Documents
    public static final String ENABLED_IND = "ENABLED_IND";
    public static final String FROM_EMAIL = "FROM_EMAIL";
    public static final String MAX_ATTEMPTS = "MAX_ATTEMPTS";
    public static final String NOTIFICATION_SUBJECT = "NOTIFICATION_SUBJECT";
    public static final String TO_EMAIL = "TO_EMAIL";

    /**
     * Private Constructor since this is a constants class that should never be instantiated.
     */
    private KFSParameterKeyConstants() {
    }

    public static class YearEndAutoDisapprovalConstants {
        public static final String YEAR_END_AUTO_DISAPPROVE_ANNOTATION = "YEAR_END_AUTO_DISAPPROVE_ANNOTATION";
        public static final String YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE = "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_CREATE_DATE";
        public static final String YEAR_END_AUTO_DISAPPROVE_DOCUMENT_STEP_RUN_DATE = "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_RUN_DATE";
        public static final String YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES = "YEAR_END_AUTO_DISAPPROVE_DOCUMENT_TYPES";
        public static final String YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE = "YEAR_END_AUTO_DISAPPROVE_PARENT_DOCUMENT_TYPE";
    }

    public static class PopulateFinancialSystemDocumentHeaderParameterNames {
        public static final String POPULATION_LIMIT = "POPULATION_LIMIT";
        public static final String BATCH_SIZE = "BATCH_SIZE";
        public static final String DOCUMENT_STATUSES_TO_POPULATE = "DOCUMENT_STATUSES_TO_POPULATE";
    }

    public static class PurapPdpParameterConstants {
        public static final String PURAP_PDP_ORG_CODE = "PRE_DISBURSEMENT_EXTRACT_ORGANIZATION";
        public static final String PURAP_PDP_SUB_UNIT_CODE = "PRE_DISBURSEMENT_EXTRACT_SUB_UNIT";
    }

    public static class LdParameterConstants {
        public static final String ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY_IND =
                "ENABLE_FRINGE_BENEFIT_CALC_BY_BENEFIT_RATE_CATEGORY_IND";
        public static final String DEFAULT_BENEFIT_RATE_CATEGORY_CODE = "DEFAULT_BENEFIT_RATE_CATEGORY_CODE";
    }

    public static class CamParameterConstants {
        public static final String OBJECT_SUB_TYPE_GROUPS = "OBJECT_SUB_TYPE_GROUPS";
    }

    public static class DetectDocumentsMissingPendingEntriesConstants {
        public static final String LEDGER_ENTRY_GENERATING_DOCUMENT_TYPES = "LEDGER_ENTRY_GENERATING_DOCUMENT_TYPES";
        public static final String MISSING_PLES_NOTIFICATION_EMAIL_ADDRESSES = "MISSING_PLES_NOTIFICATION_EMAIL_ADDRESSES";
    }
}
