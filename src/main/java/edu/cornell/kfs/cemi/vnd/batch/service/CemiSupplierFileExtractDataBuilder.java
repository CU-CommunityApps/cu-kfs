package edu.cornell.kfs.cemi.vnd.batch.service;

import java.util.Iterator;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

public interface CemiSupplierFileExtractDataBuilder {

    void writeSupplierFileExtractDataForAllMappedTabsToIntermediateStorage(final Iterator<VendorDetail> vendors);

}
