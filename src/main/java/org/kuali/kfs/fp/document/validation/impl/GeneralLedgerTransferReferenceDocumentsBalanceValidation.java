/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
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
package org.kuali.kfs.fp.document.validation.impl;

import org.kuali.kfs.fp.FPKeyConstants;
import org.kuali.kfs.fp.document.GeneralLedgerTransferDocument;
import org.kuali.kfs.fp.document.service.GeneralLedgerTransferService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;

import java.util.Set;

/*
 * CU Customization:
 * Added the FINP-6813 fix from the 2020-07-02 financials patch.
 */
public class GeneralLedgerTransferReferenceDocumentsBalanceValidation extends GenericValidation {

    private GeneralLedgerTransferService generalLedgerTransferService;

    @Override
    public boolean validate(AttributedDocumentEvent event) {
        GeneralLedgerTransferDocument document = (GeneralLedgerTransferDocument) event.getDocument();

        Set<String> set =
            generalLedgerTransferService.determineAccountingLineReferenceDocumentsThatDoNotBalance(
                document.getSourceAccountingLines(), document.getTargetAccountingLines());
        if (set.size() != 0) {
            GlobalVariables.getMessageMap().putErrorForSectionId(KFSConstants.ACCOUNTING_LINE_ERRORS,
                FPKeyConstants.ERROR_GENERAL_LEDGER_TRANSFER_REF_DOCS_DONT_BALANCE, set.toString());

            return false;
        }

        return true;
    }

    public void setGeneralLedgerTransferService(GeneralLedgerTransferService generalLedgerTransferService) {
        this.generalLedgerTransferService = generalLedgerTransferService;
    }
}
