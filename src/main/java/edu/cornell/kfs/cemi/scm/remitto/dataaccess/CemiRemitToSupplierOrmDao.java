package edu.cornell.kfs.cemi.scm.remitto.dataaccess;

import java.util.stream.Stream;

import org.kuali.kfs.vnd.businessobject.VendorHeader;

public interface CemiRemitToSupplierOrmDao {

    Stream<VendorHeader> getVendorsForCemiRemitToExtractAsCloseableStream();
    
}
