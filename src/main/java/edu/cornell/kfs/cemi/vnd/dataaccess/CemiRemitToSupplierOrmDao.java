package edu.cornell.kfs.cemi.vnd.dataaccess;

import java.util.List;
import java.util.stream.Stream;

import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;

public interface CemiRemitToSupplierOrmDao {

    Stream<CemiSupplierAddressBo> getAddressesForCemiRemitToSupplierExtractAsCloseableStream();

    List<VendorAddress> getKfsVendorAddresses(final String supplierId, final String supplierJobRunDate);

}
