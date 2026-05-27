package edu.cornell.kfs.cemi.vnd.dataaccess;

import java.util.stream.Stream;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

public interface CemiVendorOrmDao {

    Stream<VendorDetail> getVendorsForCemiSupplierExtractAsCloseableStream();

}
