package edu.cornell.kfs.cemi.scm.remitto.batch.service;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.kuali.kfs.vnd.businessobject.VendorHeader;

public interface CemiRemitToSupplierDataBuilder extends Closeable {
    
    void writeRemitToSupplierDataToIntermediateStorage(final Iterator<VendorHeader> suppliers,
            final LocalDateTime jobRunDate) throws IOException;

}
