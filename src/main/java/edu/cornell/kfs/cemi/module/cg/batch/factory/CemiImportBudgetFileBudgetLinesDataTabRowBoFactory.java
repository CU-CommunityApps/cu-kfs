package edu.cornell.kfs.cemi.module.cg.batch.factory;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiImportBudgetFileBudgetLinesDataTabRowBo;

public class CemiImportBudgetFileBudgetLinesDataTabRowBoFactory {

    public CemiImportBudgetFileBudgetLinesDataTabRowBo createCemiImportBudgetFileBudgetLinesDataTabRowBo() {
        final CemiImportBudgetFileBudgetLinesDataTabRowBo row =
                new CemiImportBudgetFileBudgetLinesDataTabRowBo();

        row.setHeaderKey("budget-lines-header-key");
        row.setLineKey("line-key");
        row.setLineOrder("line-order");
        row.setCompany("company");
        row.setYear("year");
        row.setFiscalTimeInterval("fiscal-time-interval");
        row.setLedgerAccountOrLedgerAccountSummary("ledger-account-or-acct-sum");
        row.setAccountSet("account-set");
        row.setBookCode("book-code");
        row.setBudgetCurrency("budget-currency");
        row.setBudgetDebitAmount("budget-debit-amount");
        row.setBudgetCreditAmount("budget-credit-amount");
        row.setQuantity("quantity");
        row.setUnitOfTime("unit-of-time");
        row.setMemo("memo");
        row.setGrantId("grant-id");
        row.setObjectClassId("object-class-id");
        row.setExternalSupplierInvoiceSource("external-supplier-invoice-source");

        return row;
    }

}
