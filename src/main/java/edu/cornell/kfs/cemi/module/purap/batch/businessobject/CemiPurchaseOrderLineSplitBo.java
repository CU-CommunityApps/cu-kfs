package edu.cornell.kfs.cemi.module.purap.batch.businessobject;

import org.kuali.kfs.krad.bo.TransientBusinessObjectBase;

public class CemiPurchaseOrderLineSplitBo extends TransientBusinessObjectBase {

    private static final long serialVersionUID = 1L;

    private String lineSplitRowId;
    private String existingBusinessDocumentLineSplitId;
    private String newBusinessDocumentLineSplitId;
    private String lineSplitQuantity;
    private String lineSplitExtendedAmount;
    private String lineSplitBudgetDate;
    private String lineSplitMemo;
    private String lineSplitAllocation;
    private String lineSplitCostCenter;
    private String lineSplitCostCenterExternalSupplierInvoiceSource;
    private String lineSplitProject;
    private String lineSplitProjectExternalSupplierInvoiceSource;
    private String lineSplitGrant;
    private String lineSplitGrantExternalSupplierInvoiceSource;
    private String lineSplitGift;
    private String lineSplitGiftExternalSupplierInvoiceSource;
    private String lineSplitFund;
    private String lineSplitFundExternalSupplierInvoiceSource;

    public String getLineSplitRowId() {
        return lineSplitRowId;
    }

    public void setLineSplitRowId (final String lineSplitRowId) {
        this.lineSplitRowId = lineSplitRowId;
    }

    public String getExistingBusinessDocumentLineSplitId() {
        return existingBusinessDocumentLineSplitId;
    }

    public void setExistingBusinessDocumentLineSplitId (final String existingBusinessDocumentLineSplitId) {
        this.existingBusinessDocumentLineSplitId = existingBusinessDocumentLineSplitId;
    }

    public String getNewBusinessDocumentLineSplitId() {
        return newBusinessDocumentLineSplitId;
    }

    public void setNewBusinessDocumentLineSplitId (final String newBusinessDocumentLineSplitId) {
        this.newBusinessDocumentLineSplitId = newBusinessDocumentLineSplitId;
    }

    public String getLineSplitQuantity() {
        return lineSplitQuantity;
    }

    public void setLineSplitQuantity (final String lineSplitQuantity) {
        this.lineSplitQuantity = lineSplitQuantity;
    }

    public String getLineSplitExtendedAmount() {
        return lineSplitExtendedAmount;
    }

    public void setLineSplitExtendedAmount (final String lineSplitExtendedAmount) {
        this.lineSplitExtendedAmount = lineSplitExtendedAmount;
    }

    public String getLineSplitBudgetDate() {
        return lineSplitBudgetDate;
    }

    public void setLineSplitBudgetDate (final String lineSplitBudgetDate) {
        this.lineSplitBudgetDate = lineSplitBudgetDate;
    }

    public String getLineSplitMemo() {
        return lineSplitMemo;
    }

    public void setLineSplitMemo (final String lineSplitMemo) {
        this.lineSplitMemo = lineSplitMemo;
    }

    public String getLineSplitAllocation() {
        return lineSplitAllocation;
    }

    public void setLineSplitAllocation (final String lineSplitAllocation) {
        this.lineSplitAllocation = lineSplitAllocation;
    }

    public String getLineSplitCostCenter() {
        return lineSplitCostCenter;
    }

    public void setLineSplitCostCenter (final String lineSplitCostCenter) {
        this.lineSplitCostCenter = lineSplitCostCenter;
    }

    public String getLineSplitCostCenterExternalSupplierInvoiceSource() {
        return lineSplitCostCenterExternalSupplierInvoiceSource;
    }

    public void setLineSplitCostCenterExternalSupplierInvoiceSource(
            String lineSplitCostCenterExternalSupplierInvoiceSource) {
        this.lineSplitCostCenterExternalSupplierInvoiceSource = lineSplitCostCenterExternalSupplierInvoiceSource;
    }

    public String getLineSplitProject() {
        return lineSplitProject;
    }

    public void setLineSplitProject (final String lineSplitProject) {
        this.lineSplitProject = lineSplitProject;
    }

    public String getLineSplitProjectExternalSupplierInvoiceSource() {
        return lineSplitProjectExternalSupplierInvoiceSource;
    }

    public void setLineSplitProjectExternalSupplierInvoiceSource (final String lineSplitProjectExternalSupplierInvoiceSource) {
        this.lineSplitProjectExternalSupplierInvoiceSource = lineSplitProjectExternalSupplierInvoiceSource;
    }

    public String getLineSplitGrant() {
        return lineSplitGrant;
    }

    public void setLineSplitGrant (final String lineSplitGrant) {
        this.lineSplitGrant = lineSplitGrant;
    }

    public String getLineSplitGrantExternalSupplierInvoiceSource() {
        return lineSplitGrantExternalSupplierInvoiceSource;
    }

    public void setLineSplitGrantExternalSupplierInvoiceSource (final String lineSplitGrantExternalSupplierInvoiceSource) {
        this.lineSplitGrantExternalSupplierInvoiceSource = lineSplitGrantExternalSupplierInvoiceSource;
    }

    public String getLineSplitGift() {
        return lineSplitGift;
    }

    public void setLineSplitGift (final String lineSplitGift) {
        this.lineSplitGift = lineSplitGift;
    }

    public String getLineSplitGiftExternalSupplierInvoiceSource() {
        return lineSplitGiftExternalSupplierInvoiceSource;
    }

    public void setLineSplitGiftExternalSupplierInvoiceSource (final String lineSplitGiftExternalSupplierInvoiceSource) {
        this.lineSplitGiftExternalSupplierInvoiceSource = lineSplitGiftExternalSupplierInvoiceSource;
    }

    public String getLineSplitFund() {
        return lineSplitFund;
    }

    public void setLineSplitFund (final String lineSplitFund) {
        this.lineSplitFund = lineSplitFund;
    }

    public String getLineSplitFundExternalSupplierInvoiceSource() {
        return lineSplitFundExternalSupplierInvoiceSource;
    }

    public void setLineSplitFundExternalSupplierInvoiceSource (final String lineSplitFundExternalSupplierInvoiceSource) {
        this.lineSplitFundExternalSupplierInvoiceSource = lineSplitFundExternalSupplierInvoiceSource;
    }

}
