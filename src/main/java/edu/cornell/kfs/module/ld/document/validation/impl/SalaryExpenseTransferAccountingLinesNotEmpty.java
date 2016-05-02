package edu.cornell.kfs.module.ld.document.validation.impl;

import java.util.List;

import org.kuali.kfs.module.ld.businessobject.ExpenseTransferSourceAccountingLine;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferTargetAccountingLine;
import org.kuali.kfs.module.ld.document.LaborExpenseTransferDocumentBase;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.validation.GenericValidation;
import org.kuali.kfs.sys.document.validation.event.AttributedDocumentEvent;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.cornell.kfs.module.ld.CuLaborKeyConstants;

public class SalaryExpenseTransferAccountingLinesNotEmpty extends GenericValidation {
    private Document documentForValidation;
    
    /**
     * Validates that the accounting lines in the accounting document are not empty
     * <strong>Expects an accounting document as the first a parameter</strong>
     * @see org.kuali.kfs.validation.Validation#validate(java.lang.Object[])
     */
    public boolean validate(AttributedDocumentEvent event) {
        boolean result = true;
           
        // ensure the employee ids in the source accounting lines are same
        AccountingDocument accountingDocument = (AccountingDocument) documentForValidation;
        if (!hasEmptyAccountingLines(accountingDocument)) {
            result = false;
        }
        
        return result;    
    }
    
    @SuppressWarnings("unchecked")
    protected boolean hasEmptyAccountingLines(AccountingDocument accountingDocument) {
        
        LaborExpenseTransferDocumentBase expenseTransferDocument = (LaborExpenseTransferDocumentBase) accountingDocument;  
        List<ExpenseTransferSourceAccountingLine> sourceAccountingLines = expenseTransferDocument.getSourceAccountingLines();
        List<ExpenseTransferTargetAccountingLine> targetAccountingLines = expenseTransferDocument.getTargetAccountingLines();

        //If the lines are emoty the validation results should be false
        boolean sourceAccountingLinesValidationResult = !sourceAccountingLines.isEmpty();
        boolean targetAccountingLinesValidationResult = !targetAccountingLines.isEmpty();

        if (!sourceAccountingLinesValidationResult) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.SOURCE_ACCOUNTING_LINES, CuLaborKeyConstants.ERROR_ACCOUNTING_LINE_EMPTY);
        }

        if (!targetAccountingLinesValidationResult) {
            GlobalVariables.getMessageMap().putError(KFSPropertyConstants.TARGET_ACCOUNTING_LINES, CuLaborKeyConstants.ERROR_ACCOUNTING_LINE_EMPTY);
        }

        return sourceAccountingLinesValidationResult && targetAccountingLinesValidationResult;
    }

    /**
     * Gets the accountingDocumentForValidation attribute. 
     * @return Returns the accountingDocumentForValidation.
     */
    public Document getDocumentForValidation() {
        return documentForValidation;
    }

    /**
     * Sets the accountingDocumentForValidation attribute value.
     * @param accountingDocumentForValidation The accountingDocumentForValidation to set.
     */
    public void setDocumentForValidation(Document documentForValidation) {
        this.documentForValidation = documentForValidation;
    } 
}
