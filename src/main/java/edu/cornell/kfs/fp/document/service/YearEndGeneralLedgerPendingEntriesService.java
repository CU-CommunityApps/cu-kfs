package edu.cornell.kfs.fp.document.service;

import java.util.Collection;
import java.util.List;

import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.document.AccountingDocumentBase;

public interface YearEndGeneralLedgerPendingEntriesService {
	
	/**
	 * Generated year end general ledger pending entries for the given doc.
	 * 
	 * @param document
	 * @param documentTypeCode
	 * @param accountingLines
	 * @param sequenceHelper
	 * @param closingCharts
	 * @return
	 */
	public boolean generateYearEndGeneralLedgerPendingEntries(AccountingDocumentBase document, String documentTypeCode, List<AccountingLine> accountingLines, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, Collection<String> closingCharts);

}
