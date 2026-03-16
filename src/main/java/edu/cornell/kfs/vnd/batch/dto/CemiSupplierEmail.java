package edu.cornell.kfs.vnd.batch.dto;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kuali.kfs.vnd.businessobject.VendorDetail;

public class CemiSupplierEmail {
    
    private final VendorDetail vendorDetail;
    private final String supplierId;
    
    private final List<CemiSupplierEmailSubEntry> supplierEmails;
    
    public CemiSupplierEmail(final VendorDetail vendorDetail, final String supplierId,
                final Stream<CemiSupplierEmailSubEntry> supplierEmails) {
        this.vendorDetail = vendorDetail;
        this.supplierId = supplierId;
        this.supplierEmails = supplierEmails.collect(Collectors.toUnmodifiableList());
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
