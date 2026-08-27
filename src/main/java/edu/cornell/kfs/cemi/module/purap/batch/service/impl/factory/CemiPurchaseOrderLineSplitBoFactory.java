package edu.cornell.kfs.cemi.module.purap.batch.service.impl.factory;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderAccount;
import org.kuali.kfs.module.purap.businessobject.PurchaseOrderItem;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderLineSplitBo;
import edu.cornell.kfs.cemi.module.purap.util.CemiPurchaseOrderUtils;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiPurchaseOrderLineSplitBoFactory {

    private Optional<PurchaseOrderItem> purchaseOrderItem;
    private Optional<PurchaseOrderAccount> purchaseOrderAccountingLine;
    private int lineSplitIndex;

    public CemiPurchaseOrderLineSplitBoFactory(final Optional<PurchaseOrderItem> purchaseOrderItem,
            final Optional<PurchaseOrderAccount> purchaseOrderAccountingLine, final int lineSplitIndex) {
        Validate.notNull(purchaseOrderItem, "purchaseOrderItem wrapper object cannot be null");
        Validate.notNull(purchaseOrderAccountingLine, "purchaseOrderAccountingLine wrapper object cannot be null");
        Validate.isTrue(purchaseOrderItem.isPresent() == (lineSplitIndex > 0),
                "lineSplitIndex must be a positive integer if, and only if, the purchaseOrderItem wrapper is non-empty");
        Validate.isTrue(purchaseOrderAccountingLine.isPresent() == (lineSplitIndex > 0),
                "lineSplitIndex must be a positive integer if, and only if, the purchaseOrderAccountingLine wrapper "
                        + "is non-empty");
        this.purchaseOrderItem = purchaseOrderItem;
        this.purchaseOrderAccountingLine = purchaseOrderAccountingLine;
        this.lineSplitIndex = lineSplitIndex;
    }

    public static CemiPurchaseOrderLineSplitBo createLineSplitBoFrom(final Optional<PurchaseOrderItem> purchaseOrderItem,
            final Optional<PurchaseOrderAccount> purchaseOrderAccountingLine, final int lineSplitIndex) {
        final CemiPurchaseOrderLineSplitBoFactory factory = new CemiPurchaseOrderLineSplitBoFactory(
                purchaseOrderItem, purchaseOrderAccountingLine, lineSplitIndex);
        return factory.createCemiPurchaseOrderLineSplitBo();
    }

    public CemiPurchaseOrderLineSplitBo createCemiPurchaseOrderLineSplitBo() {
        final CemiPurchaseOrderLineSplitBo lineSplit = new CemiPurchaseOrderLineSplitBo();

        lineSplit.setLineSplitRowId(determineLineSplitRowId());
        lineSplit.setExistingBusinessDocumentLineSplitId(CemiBaseConstants.EMPTY_STRING);
        lineSplit.setNewBusinessDocumentLineSplitId(determineBusinessDocumentLineSplitId());
        lineSplit.setLineSplitQuantity(CemiBaseConstants.EMPTY_STRING);
        lineSplit.setLineSplitExtendedAmount(determineLineSplitExtendedAmount());
        lineSplit.setLineSplitBudgetDate(CemiBaseConstants.EMPTY_STRING);
        lineSplit.setLineSplitMemo(CemiBaseConstants.EMPTY_STRING);
        lineSplit.setLineSplitAllocation(CemiBaseConstants.EMPTY_STRING);
        lineSplit.setLineSplitCostCenter(determineCostCenter());
        lineSplit.setLineSplitCostCenterExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        lineSplit.setLineSplitProject(determineProject());
        lineSplit.setLineSplitProjectExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        lineSplit.setLineSplitGrant(determineGrant());
        lineSplit.setLineSplitGrantExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        lineSplit.setLineSplitGift(determineGift());
        lineSplit.setLineSplitGiftExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);
        lineSplit.setLineSplitFund(determineFund());
        lineSplit.setLineSplitFundExternalSupplierInvoiceSource(CemiBaseConstants.EMPTY_STRING);

        return lineSplit;
    }

    private String determineLineSplitRowId() {
        return isEmptyFactory() ? CemiBaseConstants.EMPTY_STRING : Integer.toString(lineSplitIndex);
    }

    private String determineBusinessDocumentLineSplitId() {
        if (isEmptyFactory()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        final PurchaseOrderDocument document = (PurchaseOrderDocument) purchaseOrderItem.get().getPurapDocument();
        return StringUtils.joinWith(CUKFSConstants.UNDERSCORE,
                document.getPurapDocumentIdentifier().toString(),
                purchaseOrderItem.get().getItemLineNumber().toString(), Integer.toString(lineSplitIndex));
    }

    private String determineLineSplitExtendedAmount() {
        if (isEmptyFactory()) {
            return CemiBaseConstants.EMPTY_STRING;
        }
        return CemiPurchaseOrderUtils.formatSplitAmount(
                purchaseOrderAccountingLine.get().getItemAccountOutstandingEncumbranceAmount());
    }

    // TODO: At a future date, update this method to use a Cost Center mapping table or API.
    private String determineCostCenter() {
        return isEmptyFactory() ? CemiBaseConstants.EMPTY_STRING : CemiBaseConstants.DEFAULT_ITHACA_COST_CENTER;
    }

    // TODO: At a future date, update this method to use a Project mapping table or API.
    private String determineProject() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    // TODO: At a future date, update this method to use a Grant mapping table or API.
    private String determineGrant() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    // TODO: At a future date, update this method to use a Gift mapping table or API.
    private String determineGift() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    // TODO: At a future date, update this method to use a Fund mapping table or API.
    private String determineFund() {
        return CemiBaseConstants.EMPTY_STRING;
    }

    private boolean isEmptyFactory() {
        return lineSplitIndex <= 0;
    }

}
