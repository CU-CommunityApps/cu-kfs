package edu.cornell.kfs.cemi.module.purap.dataaccess;

import java.util.stream.Stream;

import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderExtractRow;

public interface CemiPurchaseOrderExtractDao {

    void clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();

    void queryAndStoreInScopeBusinessObjectKeysForDataExtract();

    /*
     * Returns a Stream that MUST be closed after use. The rows are returned in ascending order by
     * document number, then by item line number and item identifier, then by account identifier.
     */
    Stream<CemiPurchaseOrderExtractRow> getPurchaseOrderExtractRowsAsCloseableStream(
            final String supplierJobRunDateString);

}
