package edu.cornell.kfs.cemi.module.cg.batch.factory;

import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.datetime.DateTimeService;

import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiImportBudgetFileBudgetLinesDataTabRowBo;

public class CemiImportBudgetFileBudgetLinesDataTabRowBoFactory {

    private String jobRunDateString;
    private DateTimeService dateTimeService;
    private boolean maskSensitiveData = true;

    public CemiImportBudgetFileBudgetLinesDataTabRowBoFactory(final String jobRunDateString,
            final DateTimeService dateTimeService, final boolean maskSensitiveData) {
        Validate.validState(jobRunDateString != null, "jobRunDateString cannot be null for CemiImportBudgetFileBudgetLinesDataTabRowBoFactory.");
        Validate.validState(dateTimeService != null, "dateTimeService cannot be null for CemiImportBudgetFileBudgetLinesDataTabRowBoFactory.");
        this.jobRunDateString = jobRunDateString;
        this.dateTimeService = dateTimeService;
        this.maskSensitiveData = maskSensitiveData;
    }

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
