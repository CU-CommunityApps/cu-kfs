package edu.cornell.kfs.cemi.vnd.dataaccess;

import java.util.stream.Stream;

import org.kuali.kfs.vnd.businessobject.VendorContact;

public interface CemiEntityContactExtractOrmDao {

    Stream<VendorContact> getVendorContactsForCemiEntityContactExtractAsCloseableStream();

}
