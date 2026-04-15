package edu.cornell.kfs.cemi.vnd.dataaccess;

import java.util.List;
import java.util.stream.Stream;

import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

public interface CemiVendorOrmDao {

    Stream<VendorDetail> getVendorsForCemiSupplierExtractAsCloseableStream();

    List<VendorAddress> getKfsVendorAddresses(final String supplierId, final String supplierJobRunDate);

}
