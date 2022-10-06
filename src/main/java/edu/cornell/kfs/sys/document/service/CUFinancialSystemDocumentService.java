package edu.cornell.kfs.sys.document.service;

import org.kuali.kfs.kew.api.document.search.DocumentSearchCriteria;
import org.kuali.kfs.sys.document.AccountingDocument;

public interface CUFinancialSystemDocumentService extends org.kuali.kfs.sys.document.service.FinancialSystemDocumentService{

    public void checkAccountingLinesForChanges(AccountingDocument accountingDocument);
    public int getMaxResultCap(DocumentSearchCriteria criteria);
    public int getFetchMoreIterationLimit();
	
}
