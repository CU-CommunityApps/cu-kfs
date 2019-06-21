package edu.cornell.kfs.module.purap.dataaccess;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

import java.util.List;

public interface CuEinvoiceDao {
    List<VendorDetail> getVendors(List<String> vendorNumbers);
}
