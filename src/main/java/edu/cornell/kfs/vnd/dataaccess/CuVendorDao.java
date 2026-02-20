package edu.cornell.kfs.vnd.dataaccess;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.kuali.kfs.krad.bo.BusinessObject;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.dataaccess.VendorDao;

import edu.cornell.kfs.vnd.businessobject.VendorWithTaxId;

public interface CuVendorDao extends VendorDao {

    List<BusinessObject> getSearchResults(Map<String,String> fieldValues);

    Stream<VendorWithTaxId> getPotentialEmployeeVendorsAsCloseableStream();

    Stream<VendorDetail> getVendorsForCemiSupplierExtractAsCloseableStream();

}
