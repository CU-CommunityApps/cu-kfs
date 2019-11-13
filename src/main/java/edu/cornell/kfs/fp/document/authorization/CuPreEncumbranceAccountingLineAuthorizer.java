package edu.cornell.kfs.fp.document.authorization;

import org.kuali.kfs.fp.document.authorization.FinancialProcessingAccountingLineAuthorizer;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.rice.kew.api.WorkflowDocument;

public class CuPreEncumbranceAccountingLineAuthorizer extends FinancialProcessingAccountingLineAuthorizer {
    @Override
    public boolean determineEditPermissionOnField(AccountingDocument accountingDocument, AccountingLine accountingLine,
        String accountingLineCollectionProperty, String fieldName, boolean editablePage) {
        final FinancialSystemDocumentHeader documentHeader = (FinancialSystemDocumentHeader) accountingDocument.getDocumentHeader();
        final WorkflowDocument workflowDocument = documentHeader.getWorkflowDocument();
        if (workflowDocument.isInitiated()) {
            return true;
        } else {
     // no edits by default on non editable pages
            if (!editablePage) { 
                return false; 
            }
            // if a document is cancelled or in error, all of its fields cannot be editable
            if (workflowDocument.isCanceled()) {
                return false;
            }
        }
        return true;
    }
}

