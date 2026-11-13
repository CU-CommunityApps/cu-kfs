package edu.cornell.kfs.cemi.module.cg.batch.factory;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiImportBudgetFileBudgetLinesDataTabRowBo;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;

public class CemiImportBudgetFileBudgetLinesDataTabRowBoFactory {

    public CemiImportBudgetFileBudgetLinesDataTabRowBo createCemiImportBudgetFileBudgetLinesDataTabRowBo() {
        final CemiImportBudgetFileBudgetLinesDataTabRowBo row =
                new CemiImportBudgetFileBudgetLinesDataTabRowBo();

        // Both of these attributes are set by CemiOrmDataBuilderBase when storeSheetRow is invoked:
        //      attribute jobRunRowIndex
        //      attribute jobRunDateString

        row.setHeaderKey("budget-lines-header-key");
        row.setLineKey(CemiBaseConstants.EMPTY_STRING);
        row.setLineOrder(CemiBaseConstants.EMPTY_STRING);
        row.setCompany(CemiBaseConstants.EMPTY_STRING);
        row.setYear(CemiBaseConstants.EMPTY_STRING);
        row.setFiscalTimeInterval(CemiBaseConstants.EMPTY_STRING);
        row.setLedgerAccountOrLedgerAccountSummary(CemiBaseConstants.EMPTY_STRING);
        row.setAccountSet(CemiBaseConstants.EMPTY_STRING);
        row.setBookCode(CemiBaseConstants.EMPTY_STRING);
        row.setBudgetCurrency(CemiBaseConstants.EMPTY_STRING);
        row.setBudgetDebitAmount(CemiBaseConstants.EMPTY_STRING);
        row.setBudgetCreditAmount(CemiBaseConstants.EMPTY_STRING);
        row.setQuantity(CemiBaseConstants.EMPTY_STRING);
        row.setUnitOfTime(CemiBaseConstants.EMPTY_STRING);
        row.setMemo(CemiBaseConstants.EMPTY_STRING);
        row.setGrantId(CemiBaseConstants.EMPTY_STRING);
        row.setObjectClassId(CemiBaseConstants.EMPTY_STRING);
        row.setExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);

        return row;
    }

}
