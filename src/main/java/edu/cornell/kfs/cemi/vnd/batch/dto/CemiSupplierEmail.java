package edu.cornell.kfs.cemi.vnd.batch.dto;

import java.util.List;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;

public class CemiSupplierEmail {
    
    private final VendorDetail vendorDetail;
    private final String supplierId;
    
    private final List<CemiSupplierEmailSubEntry> supplierEmails;
    
    public CemiSupplierEmail(final VendorDetail vendorDetail, final String supplierId,
                final CemiSupplierEmailSubEntry... supplierEmails) {
        this.vendorDetail = vendorDetail;
        this.supplierId = supplierId;
        this.supplierEmails = CemiUtils.createListPaddedToMinimumSizeIfNecessary(
                CemiSupplierEmailSubEntry.EMPTY, CemiVendorConstants.MAX_SUPPLIER_EMAIL_ENTRIES, supplierEmails);
    }

    public VendorDetail getVendorDetail() {
        return vendorDetail;
    }
    
    public String getSupplierId() {
        return supplierId;
    }

    public List<CemiSupplierEmailSubEntry> getSupplierEmails() {
        return supplierEmails;
    }

}
