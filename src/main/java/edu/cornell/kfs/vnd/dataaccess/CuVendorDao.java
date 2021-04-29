package edu.cornell.kfs.vnd.dataaccess;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.vnd.dataaccess.VendorDao;
import org.kuali.kfs.krad.bo.BusinessObject;

public interface CuVendorDao extends VendorDao {

    public List<BusinessObject> getSearchResults(Map<String,String> fieldValues);

}
