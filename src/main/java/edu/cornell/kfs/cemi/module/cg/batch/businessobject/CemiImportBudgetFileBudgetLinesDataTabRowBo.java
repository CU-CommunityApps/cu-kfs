package edu.cornell.kfs.cemi.module.cg.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

public class CemiImportBudgetFileBudgetLinesDataTabRowBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = 5671033246180925834L;

    private String headerKey;
    private String lineKey;
    private String lineOrder;
    private String company;
    private String year;
    private String fiscalTimeInterval;
    private String ledgerAccountOrLedgerAccountSummary;
    private String accountSet;
    private String bookCode;
    private String budgetCurrency;
    private String budgetDebitAmount;
    private String budgetCreditAmount;
    private String quantity;
    private String unitOfTime;
    private String memo;
    private String grantId;
    private String objectClassId;
    private String externalSupplierInvoiceSource;

    public String getHeaderKey() { return headerKey; }
    public void setHeaderKey(final String headerKey) { this.headerKey = headerKey; }

    public String getLineKey() { return lineKey; }
    public void setLineKey(final String lineKey) { this.lineKey = lineKey; }

    public String getLineOrder() { return lineOrder; }
    public void setLineOrder(final String lineOrder) { this.lineOrder = lineOrder; }

    public String getCompany() { return company; }
    public void setCompany(final String company) { this.company = company; }

    public String getYear() { return year; }
    public void setYear(final String year) { this.year = year; }

    public String getFiscalTimeInterval() { return fiscalTimeInterval; }
    public void setFiscalTimeInterval(final String fiscalTimeInterval) { this.fiscalTimeInterval = fiscalTimeInterval; }

    public String getLedgerAccountOrLedgerAccountSummary() { return ledgerAccountOrLedgerAccountSummary; }
    public void setLedgerAccountOrLedgerAccountSummary(final String ledgerAccountOrLedgerAccountSummary) { this.ledgerAccountOrLedgerAccountSummary = ledgerAccountOrLedgerAccountSummary; }

    public String getAccountSet() { return accountSet; }
    public void setAccountSet(final String accountSet) { this.accountSet = accountSet; }

    public String getBookCode() { return bookCode; }
    public void setBookCode(final String bookCode) { this.bookCode = bookCode; }

    public String getBudgetCurrency() { return budgetCurrency; }
    public void setBudgetCurrency(final String budgetCurrency) { this.budgetCurrency = budgetCurrency; }

    public String getBudgetDebitAmount() { return budgetDebitAmount; }
    public void setBudgetDebitAmount(final String budgetDebitAmount) { this.budgetDebitAmount = budgetDebitAmount; }

    public String getBudgetCreditAmount() { return budgetCreditAmount; }
    public void setBudgetCreditAmount(final String budgetCreditAmount) { this.budgetCreditAmount = budgetCreditAmount; }

    public String getQuantity() { return quantity; }
    public void setQuantity(final String quantity) { this.quantity = quantity; }

    public String getUnitOfTime() { return unitOfTime; }
    public void setUnitOfTime(final String unitOfTime) { this.unitOfTime = unitOfTime; }

    public String getMemo() { return memo; }
    public void setMemo(final String memo) { this.memo = memo; }

    public String getGrantId() { return grantId; }
    public void setGrantId(final String grantId) { this.grantId = grantId; }

    public String getObjectClassId() { return objectClassId; }
    public void setObjectClassId(final String objectClassId) { this.objectClassId = objectClassId; }

    public String getExternalSupplierInvoiceSource() { return externalSupplierInvoiceSource; }
    public void setExternalSupplierInvoiceSource(final String externalSupplierInvoiceSource) { this.externalSupplierInvoiceSource = externalSupplierInvoiceSource; }

}
