package edu.cornell.kfs.module.ld.document;

import java.util.List;

import org.kuali.kfs.module.ld.LaborConstants;
import org.kuali.kfs.module.ld.businessobject.ExpenseTransferAccountingLine;
import org.kuali.kfs.module.ld.businessobject.LaborLedgerPendingEntry;
import org.kuali.kfs.module.ld.document.SalaryExpenseTransferDocument;
import org.kuali.kfs.module.ld.util.LaborPendingEntryGenerator;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.COMPONENT;
import org.kuali.kfs.coreservice.framework.parameter.ParameterConstants.NAMESPACE;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.module.ld.util.CuLaborPendingEntryGenerator;

@NAMESPACE(namespace = KFSConstants.OptionalModuleNamespaces.LABOR_DISTRIBUTION)
@COMPONENT(component = "SalaryExpenseTransfer")
public class CuSalaryExpenseTransferDocument extends SalaryExpenseTransferDocument {
    private static final Logger LOG = LogManager.getLogger();

    private static final long serialVersionUID = 1L;

    @Override
    public boolean generateLaborLedgerPendingEntries(AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LOG.debug("started generateLaborLedgerPendingEntries()");

        boolean isSuccessful = true;
        ExpenseTransferAccountingLine expenseTransferAccountingLine = (ExpenseTransferAccountingLine) accountingLine;

        List<LaborLedgerPendingEntry> expensePendingEntries = LaborPendingEntryGenerator.generateExpensePendingEntries(this, expenseTransferAccountingLine, sequenceHelper);
        if (expensePendingEntries != null && !expensePendingEntries.isEmpty()) {
            isSuccessful &= this.getLaborLedgerPendingEntries().addAll(expensePendingEntries);
        }

        List<LaborLedgerPendingEntry> benefitPendingEntries = CuLaborPendingEntryGenerator.generateBenefitPendingEntries(this, expenseTransferAccountingLine, sequenceHelper);
        if (benefitPendingEntries != null && !benefitPendingEntries.isEmpty()) {
            isSuccessful &= this.getLaborLedgerPendingEntries().addAll(benefitPendingEntries);
        }
        
        return isSuccessful;
    }
    
    @Override
    public boolean generateLaborLedgerBenefitClearingPendingEntries(GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {
        LOG.debug("started generateLaborLedgerBenefitClearingPendingEntries()");

        String chartOfAccountsCode = SpringContext.getBean(ParameterService.class).getParameterValueAsString(SalaryExpenseTransferDocument.class, LaborConstants.SalaryExpenseTransfer.BENEFIT_CLEARING_CHART_PARM_NM);
        String accountNumber = SpringContext.getBean(ParameterService.class).getParameterValueAsString(SalaryExpenseTransferDocument.class, LaborConstants.SalaryExpenseTransfer.BENEFIT_CLEARING_ACCOUNT_PARM_NM);

        List<LaborLedgerPendingEntry> benefitClearingPendingEntries = CuLaborPendingEntryGenerator.generateBenefitClearingPendingEntries(this, sequenceHelper, accountNumber, chartOfAccountsCode);

        if (benefitClearingPendingEntries != null && !benefitClearingPendingEntries.isEmpty()) {
            return this.getLaborLedgerPendingEntries().addAll(benefitClearingPendingEntries);
        }

        return true;
    }

}
