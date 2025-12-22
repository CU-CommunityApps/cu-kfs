/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2025 Kuali, Inc.
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
package org.kuali.kfs.module.ld.document.service;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferAccountingLine;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.document.LaborLedgerPostingDocument;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;

import java.util.List;

public interface LaborPendingEntryGeneratorService {
    /**
     * generate the expense pending entries based on the given document, accounting line and sequence helper
     *
     * @param document       the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @return a set of expense pending entries
     */
    List<LaborLedgerPendingEntry> generateExpensePendingEntries(
            LaborLedgerPostingDocument document,
            ExpenseTransferAccountingLine accountingLine,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper);

    /**
     * generate the benefit pending entries based on the given document, accounting line and sequence helper
     *
     * @param document       the given accounting document
     * @param accountingLine the given accounting line
     * @param sequenceHelper the given sequence helper
     * @return a set of benefit pending entries
     */
    List<LaborLedgerPendingEntry> generateBenefitPendingEntries(
            LaborLedgerPostingDocument document,
            ExpenseTransferAccountingLine accountingLine,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper);

    /**
     * generate benefit pending entries with the given benefit amount and fringe benefit object code based on the
     * given document, accounting line and sequence helper
     *
     * @param document                the given accounting document
     * @param accountingLine          the given accounting line
     * @param sequenceHelper          the given sequence helper
     * @param benefitAmount           the given benefit amount
     * @param fringeBenefitObjectCode the given fringe benefit object code
     * @return a set of benefit pending entries with the given benefit amount and fringe benefit object code
     */
    List<LaborLedgerPendingEntry> generateBenefitPendingEntries(
            LaborLedgerPostingDocument document,
            ExpenseTransferAccountingLine accountingLine,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
            KualiDecimal benefitAmount,
            String fringeBenefitObjectCode);

    /**
     * generate benefit clearing pending entries with the given document, sequence helper, chart and account
     *
     * @param document            the given accounting document
     * @param sequenceHelper      the given sequence helper
     * @param accountNumber       the given clearing account number
     * @param chartOfAccountsCode the given clearing chart of accounts code
     * @return a set of benefit clearing pending entries
     */
    List<LaborLedgerPendingEntry> generateBenefitClearingPendingEntries(
            LaborLedgerPostingDocument document,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper,
            String accountNumber,
            String chartOfAccountsCode);
}
