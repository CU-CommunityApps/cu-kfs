package edu.cornell.kfs.vnd.batch.service;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.vnd.util.VendorAccountFinder;

public interface CemiSupplierDataBuilder extends Closeable {

    void writeSupplierDataToIntermediateStorage(final Iterator<VendorDetail> vendors,
            final VendorAccountFinder accountFinder, final LocalDateTime jobRunDate) throws IOException;

}
