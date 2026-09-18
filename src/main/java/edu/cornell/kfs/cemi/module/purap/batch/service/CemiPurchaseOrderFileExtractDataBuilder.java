package edu.cornell.kfs.cemi.module.purap.batch.service;

import java.util.Iterator;

import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;

public interface CemiPurchaseOrderFileExtractDataBuilder {

    void writePurchaseOrderFileSubmitPurchaseOrderTabExtractDataToIntermediateStorage(
            final Iterator<PurchaseOrderDocument> legacyPurchaseOrders);

}
