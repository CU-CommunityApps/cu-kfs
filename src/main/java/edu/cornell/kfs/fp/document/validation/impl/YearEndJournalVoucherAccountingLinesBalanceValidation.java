
/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.fp.document.validation.impl;

import static org.kuali.kfs.sys.KFSConstants.SOURCE_ACCOUNTING_LINE_ERRORS;
import static org.kuali.kfs.sys.KFSKeyConstants.ERROR_DOCUMENT_BALANCE_CONSIDERING_CREDIT_AND_DEBIT_AMOUNTS;

import edu.cornell.kfs.fp.document.YearEndJournalVoucherDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.util.GlobalVariables;

/**
 * Validation for Year End Journal Voucher, which checks that the accounting lines on the document, with all of
 * their various credits and debits, balance.
 */
public class YearEndJournalVoucherAccountingLinesBalanceValidation extends GenericValidation {
    private YearEndJournalVoucherDocument yearEndJournalVoucherDocumentForValidation;

    /**
     * Returns true if credit/debit entries are in balance
     * @see org.kuali.kfs.sys.document.validation.Validation#validate(org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent)
     */
    public boolean validate(AttributedDocumentEvent event) {
        KualiDecimal creditAmount = getYearEndJournalVoucherDocumentForValidation().getCreditTotal();
        KualiDecimal debitAmount = getYearEndJournalVoucherDocumentForValidation().getDebitTotal();

        boolean balanced = debitAmount.equals(creditAmount);
        if (!balanced) {
            String errorParams[] = { creditAmount.toString(), debitAmount.toString() };
            GlobalVariables.getMessageMap().putError(SOURCE_ACCOUNTING_LINE_ERRORS, ERROR_DOCUMENT_BALANCE_CONSIDERING_CREDIT_AND_DEBIT_AMOUNTS, errorParams);
        }
        return balanced;
    }

	/**
	 * Gets the yearEndJournalVoucherDocumentForValidation.
	 * 
	 * @return yearEndJournalVoucherDocumentForValidation
	 */
	public YearEndJournalVoucherDocument getYearEndJournalVoucherDocumentForValidation() {
		return yearEndJournalVoucherDocumentForValidation;
	}

	/**
	 * Sets the yearEndJournalVoucherDocumentForValidation.
	 * 
	 * @param yearEndJournalVoucherDocumentForValidation
	 */
	public void setYearEndJournalVoucherDocumentForValidation(YearEndJournalVoucherDocument yearEndJournalVoucherDocumentForValidation) {
		this.yearEndJournalVoucherDocumentForValidation = yearEndJournalVoucherDocumentForValidation;
	}

}

