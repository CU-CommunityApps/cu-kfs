package edu.cornell.kfs.vnd.batch.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

public interface CemiSupplierDataBuilder extends Closeable {

    void writeSupplierDataToIntermediateStorage(final Iterator<VendorDetail> vendors) throws IOException;

}
