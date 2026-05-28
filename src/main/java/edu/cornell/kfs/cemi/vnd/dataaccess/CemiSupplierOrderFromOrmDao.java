package edu.cornell.kfs.cemi.vnd.dataaccess;

import java.util.stream.Stream;

import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;

public interface CemiSupplierOrderFromOrmDao {

    Stream<VendorAddress> getKfsVendorAddressesForExtractedSuppliers();

    Stream<CemiSupplierAddressBo> getSupplierAddressesForExtractedSuppliers();

    Stream<CemiSupplierAddressBo> getSupplierAddressesForSupplierOrderFromExtract();

}
