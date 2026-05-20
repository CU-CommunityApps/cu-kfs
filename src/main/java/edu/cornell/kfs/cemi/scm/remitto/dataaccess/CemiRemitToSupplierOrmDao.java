package edu.cornell.kfs.cemi.scm.remitto.dataaccess;

import java.util.stream.Stream;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;

public interface CemiRemitToSupplierOrmDao {

    Stream<CemiSupplierBo> getCemiSuppliersExtractAsCloseableStream();
    
}
