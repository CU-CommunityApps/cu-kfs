package edu.cornell.kfs.cemi.vnd.batch.service;

import java.util.Iterator;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;

public interface CemiSupplierOrderFromDataBuilder {

    void writeSupplierOrderFromDataToIntermediateStorage(final Iterator<CemiSupplierAddressBo> supplierAddresses);

}
