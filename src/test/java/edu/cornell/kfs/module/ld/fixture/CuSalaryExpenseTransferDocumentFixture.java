package edu.cornell.kfs.module.ld.fixture;

import org.kuali.kfs.module.ld.businessobject.ExpenseTransferAccountingLine;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferSourceAccountingLine;
import org.kuali.kfs.module.ld.document.SalaryExpenseTransferDocument;
import org.kuali.kfs.sys.DocumentTestUtils;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.fixture.AccountingLineFixture;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.DocumentService;

import edu.cornell.kfs.module.ld.document.CuSalaryExpenseTransferDocument;

public enum CuSalaryExpenseTransferDocumentFixture {
    
    GOOD_ST("1", 2014);
    
    public final String emplid;
    public final Integer postingYear;

    private CuSalaryExpenseTransferDocumentFixture(String emplid, Integer postingYear) {
       this.emplid = emplid;
       this.postingYear = postingYear;
    }
    
    public CuSalaryExpenseTransferDocument createSalaryExpenseDocument() throws IllegalAccessException, InstantiationException, WorkflowException {
        CuSalaryExpenseTransferDocument document = DocumentTestUtils.createDocument(SpringContext.getBean(DocumentService.class),  CuSalaryExpenseTransferDocument.class);
        document.setEmplid(emplid);
        document.setPostingYear(postingYear);
        
        ExpenseTransferSourceAccountingLine expenseTransferSourceAccountingLine = AccountingLineFixture.ST_LINE_1.createAccountingLine(
                ExpenseTransferSourceAccountingLine.class, document.getDocumentNumber(), document.getPostingYear(), document.getNextSourceLineNumber());
        expenseTransferSourceAccountingLine.setPositionNumber("1");
        expenseTransferSourceAccountingLine.setPayrollEndDateFiscalYear(postingYear);
        expenseTransferSourceAccountingLine.setEmplid(emplid);
        
        document.addSourceAccountingLine(expenseTransferSourceAccountingLine);
        
        //AccountingLineFixture.ST_LINE_2.addAsTargetTo(document);

        return document;
    }
}
