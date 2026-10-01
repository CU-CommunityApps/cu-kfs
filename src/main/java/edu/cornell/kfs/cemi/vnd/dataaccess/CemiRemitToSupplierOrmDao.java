package edu.cornell.kfs.cemi.vnd.dataaccess;

import java.util.stream.Stream;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;

public interface CemiRemitToSupplierOrmDao {

    Stream<CemiSupplierFileAddressesTabRowBo> getAddressesForCemiRemitToSupplierExtractAsCloseableStream();

}
