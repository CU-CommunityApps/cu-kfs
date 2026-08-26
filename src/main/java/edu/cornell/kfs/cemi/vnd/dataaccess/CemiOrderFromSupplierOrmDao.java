package edu.cornell.kfs.cemi.vnd.dataaccess;

import java.util.List;
import java.util.stream.Stream;

import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;

public interface CemiOrderFromSupplierOrmDao {

    Stream<VendorAddress> getKfsVendorAddressesForExtractedSuppliers();

    Stream<CemiSupplierFileAddressesTabRowBo> getSupplierAddressesForExtractedSuppliers();

    Stream<CemiSupplierFileAddressesTabRowBo> getSupplierAddressesForOrderFromSupplierExtract();

    List<VendorAddress> getKfsVendorAddresses(final String supplierId, final String supplierJobRunDate);

}
