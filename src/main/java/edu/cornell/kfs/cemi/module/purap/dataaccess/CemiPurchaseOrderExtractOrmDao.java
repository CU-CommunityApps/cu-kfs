package edu.cornell.kfs.cemi.module.purap.dataaccess;

import java.util.stream.Stream;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderDocumentLite;

public interface CemiPurchaseOrderExtractOrmDao {

    Stream<CemiPurchaseOrderDocumentLite> getPurchaseOrdersToExtractAsCloseableStream();

}
