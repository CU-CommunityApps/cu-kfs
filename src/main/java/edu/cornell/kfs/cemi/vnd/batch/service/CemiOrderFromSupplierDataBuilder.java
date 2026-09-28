package edu.cornell.kfs.cemi.vnd.batch.service;

import java.util.Iterator;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;

public interface CemiOrderFromSupplierDataBuilder {

    void writeOrderFromSupplierDataToIntermediateStorage(final Iterator<CemiSupplierFileAddressesTabRowBo> supplierAddresses);

}
