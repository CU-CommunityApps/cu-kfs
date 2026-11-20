package edu.cornell.kfs.cemi.vnd.batch.service;

import java.util.Iterator;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileAddressesTabRowBo;

public interface CemiRemitToSupplierFileExtractDataBuilder {

    void writeRemitToSupplierFileRemitToSupplierTabExtractDataToIntermediateStorage(
            final Iterator<CemiSupplierFileAddressesTabRowBo> supplierAddresses);

}
