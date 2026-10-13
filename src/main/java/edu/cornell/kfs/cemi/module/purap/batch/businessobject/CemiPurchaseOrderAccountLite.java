package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import java.math.BigDecimal;

import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

/*
 * Lightweight implementation of PurchaseOrderAccount that retrieves the needed PUR_PO_ACCT_T fields
 * without loading other unneeded reference objects. This dramatically improves the batch job's execution time.
 */
public class CemiPurchaseOrderAccountLite extends PersistableBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String documentNumber;
    private Integer accountIdentifier;
    private Integer itemIdentifier;
    private String chartOfAccountsCode;
    private String accountNumber;
    private String subAccountNumber;
    private String financialObjectCode;
    private String financialSubObjectCode;
    private String projectCode;
    private String organizationReferenceId;
    private BigDecimal accountLinePercent;
    private KualiDecimal itemAccountOutstandingEncumbranceAmount;
    private KualiDecimal amount;
    private Integer sequenceNumber;
    private String overrideCode;

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public Integer getAccountIdentifier() {
        return accountIdentifier;
    }

    public void setAccountIdentifier(final Integer accountIdentifier) {
        this.accountIdentifier = accountIdentifier;
    }

    public Integer getItemIdentifier() {
        return itemIdentifier;
    }

    public void setItemIdentifier(final Integer itemIdentifier) {
        this.itemIdentifier = itemIdentifier;
    }

    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }

    public void setChartOfAccountsCode(final String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(final String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getSubAccountNumber() {
        return subAccountNumber;
    }

    public void setSubAccountNumber(final String subAccountNumber) {
        this.subAccountNumber = subAccountNumber;
    }

    public String getFinancialObjectCode() {
        return financialObjectCode;
    }

    public void setFinancialObjectCode(final String financialObjectCode) {
        this.financialObjectCode = financialObjectCode;
    }

    public String getFinancialSubObjectCode() {
        return financialSubObjectCode;
    }

    public void setFinancialSubObjectCode(final String financialSubObjectCode) {
        this.financialSubObjectCode = financialSubObjectCode;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(final String projectCode) {
        this.projectCode = projectCode;
    }

    public String getOrganizationReferenceId() {
        return organizationReferenceId;
    }

    public void setOrganizationReferenceId(final String organizationReferenceId) {
        this.organizationReferenceId = organizationReferenceId;
    }

    public BigDecimal getAccountLinePercent() {
        return accountLinePercent;
    }

    public void setAccountLinePercent(final BigDecimal accountLinePercent) {
        this.accountLinePercent = accountLinePercent;
    }

    public KualiDecimal getItemAccountOutstandingEncumbranceAmount() {
        return itemAccountOutstandingEncumbranceAmount;
    }

    public void setItemAccountOutstandingEncumbranceAmount(final KualiDecimal itemAccountOutstandingEncumbranceAmount) {
        this.itemAccountOutstandingEncumbranceAmount = itemAccountOutstandingEncumbranceAmount;
    }

    public KualiDecimal getAmount() {
        return amount;
    }

    public void setAmount(final KualiDecimal amount) {
        this.amount = amount;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(final Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getOverrideCode() {
        return overrideCode;
    }

    public void setOverrideCode(final String overrideCode) {
        this.overrideCode = overrideCode;
    }

}
