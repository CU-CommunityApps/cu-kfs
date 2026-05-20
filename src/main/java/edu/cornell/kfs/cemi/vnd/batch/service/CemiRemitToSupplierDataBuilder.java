package edu.cornell.kfs.cemi.vnd.batch.service;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;

public interface CemiRemitToSupplierDataBuilder extends Closeable {
    
    void writeRemitToSupplierDataToIntermediateStorage(final Iterator<CemiSupplierBo> suppliers,
            final LocalDateTime jobRunDate) throws IOException;
    
    

}
