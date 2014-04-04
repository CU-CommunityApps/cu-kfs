package edu.cornell.kfs.module.ld.document;

import java.util.List;

import org.kuali.kfs.module.ld.businessobject.ExpenseTransferAccountingLine;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.document.SalaryExpenseTransferDocument;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;

import edu.cornell.kfs.module.ld.util.CuLaborPendingEntryGenerator;

public class CuSalaryExpenseTransferDocument extends SalaryExpenseTransferDocument {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean generateLaborLedgerPendingEntries(AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LOG.debug("started generateLaborLedgerPendingEntries()");

        boolean isSuccessful = true;
        ExpenseTransferAccountingLine expenseTransferAccountingLine = (ExpenseTransferAccountingLine) accountingLine;

        List<LaborLedgerPendingEntry> expensePendingEntries = CuLaborPendingEntryGenerator.generateExpensePendingEntries(this, expenseTransferAccountingLine, sequenceHelper);
        if (expensePendingEntries != null && !expensePendingEntries.isEmpty()) {
            isSuccessful &= this.getLaborLedgerPendingEntries().addAll(expensePendingEntries);
        }

        List<LaborLedgerPendingEntry> benefitPendingEntries = CuLaborPendingEntryGenerator.generateBenefitPendingEntries(this, expenseTransferAccountingLine, sequenceHelper);
        if (benefitPendingEntries != null && !benefitPendingEntries.isEmpty()) {
            isSuccessful &= this.getLaborLedgerPendingEntries().addAll(benefitPendingEntries);
        }
        
        List<LaborLedgerPendingEntry> offsetPendingEntries = CuLaborPendingEntryGenerator.generateOffsetPendingEntries(expensePendingEntries, sequenceHelper);
        if (offsetPendingEntries != null && !offsetPendingEntries.isEmpty()) {
            isSuccessful &= this.getLaborLedgerPendingEntries().addAll(offsetPendingEntries);
        }

        return isSuccessful;
    }

}
