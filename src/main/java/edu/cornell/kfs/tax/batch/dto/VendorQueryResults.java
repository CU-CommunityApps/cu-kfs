package edu.cornell.kfs.tax.batch.dto;

public final class VendorQueryResults {

    private final VendorDetailLite vendor;
    private final VendorDetailLite parentVendor;

    public VendorQueryResults(final VendorDetailLite vendor, final VendorDetailLite parentVendor) {
        this.vendor = vendor;
        this.parentVendor = parentVendor;
    }

    public VendorDetailLite getVendor() {
        return vendor;
    }

    public VendorDetailLite getParentVendor() {
        return parentVendor;
    }

}
