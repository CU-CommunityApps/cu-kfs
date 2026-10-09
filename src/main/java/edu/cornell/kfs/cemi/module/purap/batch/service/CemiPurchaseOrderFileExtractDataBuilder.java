package edu.cornell.kfs.cemi.module.purap.batch.service;

import java.util.Iterator;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiLegacyPurchaseOrder;

public interface CemiPurchaseOrderFileExtractDataBuilder {

    void writePurchaseOrderFileSubmitPurchaseOrderTabExtractDataToIntermediateStorage(
            final Iterator<CemiLegacyPurchaseOrder> legacyPurchaseOrders);

}
