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

package org.kuali.kfs.pdp.service.impl;

import org.kuali.kfs.pdp.PdpConstants;

import edu.cornell.kfs.pdp.CUPdpConstants;

import java.util.Map;

/**
 * Holds state and options when generating GLPEs for PDP in {@link PendingTransactionServiceImpl}.
 */
// CU customization: copied to cu-kfs project and changed access to public so that it can be accessed from CuPendingTransactionServiceImpl
public class GeneratePdpGlpeState {

    /** Mapping of disbursement type codes to document type codes when processing a payment. */
    private static final Map<String, String> TYPE_MAP_PROCESS = Map.of(
            PdpConstants.DisbursementTypeCodes.ACH, PdpConstants.FinancialDocumentTypeCodes.PROCESS_ACH,
            PdpConstants.DisbursementTypeCodes.CHECK, PdpConstants.FinancialDocumentTypeCodes.PROCESS_CHECK
    );

    /** Mapping of disbursement type codes to document type codes when cancelling a payment. */
    private static final Map<String, String> TYPE_MAP_CANCEL = Map.of(
            PdpConstants.DisbursementTypeCodes.ACH, PdpConstants.FinancialDocumentTypeCodes.CANCEL_ACH,
            PdpConstants.DisbursementTypeCodes.CHECK, PdpConstants.FinancialDocumentTypeCodes.CANCEL_CHECK,
            PdpConstants.DisbursementTypeCodes.DRAFT, PdpConstants.FinancialDocumentTypeCodes.CANCEL_DRAFT,
            PdpConstants.DisbursementTypeCodes.WIRE, PdpConstants.FinancialDocumentTypeCodes.CANCEL_WIRE
    );

    /**
     * Mapping of disbursement type codes to document type codes when cancelling and reissuing a payment in one
     * operation.
     */
    private static final Map<String, String> TYPE_MAP_CANCEL_REISSUE = Map.of(
            PdpConstants.DisbursementTypeCodes.ACH, PdpConstants.FinancialDocumentTypeCodes.CANCEL_REISSUE_ACH,
            PdpConstants.DisbursementTypeCodes.CHECK, PdpConstants.FinancialDocumentTypeCodes.CANCEL_REISSUE_CHECK
    );
    
    /** Mapping of disbursement type codes to document type codes when a check is stale. */
    private static final Map<String, String> TYPE_MAP_STALE = Map.of(
            PdpConstants.DisbursementTypeCodes.CHECK, CUPdpConstants.FDOC_TYP_CD_STALE_CHECK
    );
    
    /** Mapping of disbursement type codes to document type codes when cancelling a payment. */
    private static final Map<String, String> TYPE_MAP_STOP = Map.of(
            PdpConstants.DisbursementTypeCodes.ACH, PdpConstants.FinancialDocumentTypeCodes.CANCEL_ACH,
            PdpConstants.DisbursementTypeCodes.CHECK, CUPdpConstants.FDOC_TYP_CD_STOP_CHECK
    );

    /** Create entries to reverse the payment? */
    private final boolean reversal;
    private final boolean expenseOrLiability;
    /** Maps disbusrement type codes to the corresponding document type code for the created entries. */
    private final Map<String, String> documentTypeMap;

    private String offsetDefinitionObjectCode;
    private boolean relieveLiabilities;
    private String transactionDescription;

    GeneratePdpGlpeState(
            final boolean reversal,
            final boolean expenseOrLiability,
            final Map<String, String> documentTypeMap
    ) {
        this.reversal = reversal;
        this.expenseOrLiability = expenseOrLiability;
        this.documentTypeMap = documentTypeMap;
    }

    /**
     * @return a state object with the correct options defined for processing a payment.
     */
    static GeneratePdpGlpeState forProcess() {
        return new GeneratePdpGlpeState(false, false, TYPE_MAP_PROCESS);
    }

    /**
     * @return a state object with the correct options defined for cancelling a payment.
     */
    // CU customization changed to public
    public static GeneratePdpGlpeState forCancel() {
        return new GeneratePdpGlpeState(true, false, TYPE_MAP_CANCEL);
    }
    
    /**
     * @return a state object with the correct options defined for stale check.
     */
    // CU customization for stale checks
    public static GeneratePdpGlpeState forStale() {
        return new GeneratePdpGlpeState(true, false, TYPE_MAP_STALE);
    }
    
    /**
     * @return a state object with the correct options defined for a stopped payment.
     */
    // CU customization for stale checks
    public static GeneratePdpGlpeState forStop() {
        return new GeneratePdpGlpeState(true, false, TYPE_MAP_STOP);
    }

    /**
     * @return a state object with the correct options defined for cancelling and reissuing a payment in one operation.
     */
    static GeneratePdpGlpeState forCancelReissue() {
        return new GeneratePdpGlpeState(true, false, TYPE_MAP_CANCEL_REISSUE);
    }

    /**
     * @return a state object with the correct options defined for reissuing a payment which has already been processed.
     */
    static GeneratePdpGlpeState forReissue() {
        return new GeneratePdpGlpeState(false, true, Map.of());
    }

    /**
     * @return a state object with the correct options defined for reversing a processed payment which is being reissued
     */
    static GeneratePdpGlpeState forReissueReverse() {
        return new GeneratePdpGlpeState(true, true, Map.of());
    }

    /**
     * @return the document type for GLPEs to use with the given disbursement type code
     */
    // CU customization changed to public
    public String documentTypeForDisbursementType(final String disbursementTypeCode) {
        return documentTypeMap.get(disbursementTypeCode);
    }

    public boolean isReversal() {
        return reversal;
    }

    boolean isExpenseOrLiability() {
        return expenseOrLiability;
    }

    String getOffsetDefinitionObjectCode() {
        return offsetDefinitionObjectCode;
    }

    void setOffsetDefinitionObjectCode(final String offsetDefinitionObjectCode) {
        this.offsetDefinitionObjectCode = offsetDefinitionObjectCode;
    }

    public boolean isRelieveLiabilities() {
        return relieveLiabilities;
    }

    void setRelieveLiabilities(final boolean relieveLiabilities) {
        this.relieveLiabilities = relieveLiabilities;
    }

    String getTransactionDescription() {
        return transactionDescription;
    }

    void setTransactionDescription(final String transactionDescription) {
        this.transactionDescription = transactionDescription;
    }
}
