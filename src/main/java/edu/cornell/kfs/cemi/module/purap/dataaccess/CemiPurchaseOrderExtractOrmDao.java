package edu.cornell.kfs.cemi.module.purap.dataaccess;

import java.util.stream.Stream;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderIdBo;

public interface CemiPurchaseOrderExtractOrmDao {

    Stream<CemiPurchaseOrderIdBo> getIdsOfPurchaseOrdersToExtractAsCloseableStream();

}
