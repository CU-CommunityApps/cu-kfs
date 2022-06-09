package edu.cornell.kfs.module.purap.batch.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import edu.cornell.kfs.module.purap.CUPurapConstants;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.batch.service.impl.AutoClosePurchaseOrderServiceImpl;
import org.kuali.kfs.module.purap.businessobject.AutoClosePurchaseOrderView;
import org.kuali.kfs.module.purap.businessobject.PaymentRequestView;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.util.PurApRelatedViews;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import java.util.List;

public class CuAutoClosePurchaseOrderServiceImpl extends AutoClosePurchaseOrderServiceImpl {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public boolean autoCloseFullyDisencumberedOrders() {
        LOG.debug("autoCloseFullyDisencumberedOrders() started");

        List<AutoClosePurchaseOrderView> autoCloseList = this.getAllOpenPurchaseOrdersForAutoClose();

        for (AutoClosePurchaseOrderView poAutoClose : autoCloseList) {
            if ((poAutoClose.getTotalAmount() != null) && ((KualiDecimal.ZERO.compareTo(poAutoClose.getTotalAmount())) != 0)) {
                // KFSUPGRADE-363
                if (paymentRequestsStatusCanAutoClose(poAutoClose)) {
                    //TODO: can the following be replaced with this one line?
                    // autoClosePurchaseOrder(poAutoClose);

                    LOG.info("autoCloseFullyDisencumberedOrders() PO ID " + poAutoClose.getPurapDocumentIdentifier() + " with total " + poAutoClose.getTotalAmount().doubleValue() + " will be closed");
                    String newStatus = PurchaseOrderStatuses.APPDOC_PENDING_CLOSE;
                    String annotation = "This PO was automatically closed in batch.";
                    String documentType = PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_CLOSE_DOCUMENT;
                    PurchaseOrderDocument document = purchaseOrderService.getPurchaseOrderByDocumentNumber(poAutoClose.getDocumentNumber());
                    this.createNoteForAutoCloseOrders(document, annotation);
                    purchaseOrderService.createAndRoutePotentialChangeDocument(poAutoClose.getDocumentNumber(), documentType, annotation, null, newStatus);
                }
            }
        }

        LOG.debug("autoCloseFullyDisencumberedOrders() ended");

        return true;
    }

    /**
     * Check to make sure all PaymentRequestViews related to the passed in AutoClosePurchaseOrderView
     * are in statuses that allow auto close. If so, return true. If not return false.
     *
     * @param poAutoClose The AutoClosePurchaseOrderView used to get related PaymentRequestView(s) to check
     * @return whether the PaymentRequestView(s) are in a status that will should allow auto closing the related PO.
     */
    private boolean paymentRequestsStatusCanAutoClose(AutoClosePurchaseOrderView poAutoClose) {
        PurApRelatedViews relatedViews = new PurApRelatedViews(poAutoClose.getPurapDocumentIdentifier().toString(), poAutoClose.getAccountsPayablePurchasingDocumentLinkIdentifier());
        if (relatedViews.getRelatedPaymentRequestViews() != null) {
            for (PaymentRequestView paymentRequestView : relatedViews.getRelatedPaymentRequestViews()) {
                if (!CUPurapConstants.CUPaymentRequestStatuses.STATUSES_ALLOWING_AUTO_CLOSE.contains(paymentRequestView.getApplicationDocumentStatus())) {
                    return false;
                }
            }
        }
        return true;
    }

}
