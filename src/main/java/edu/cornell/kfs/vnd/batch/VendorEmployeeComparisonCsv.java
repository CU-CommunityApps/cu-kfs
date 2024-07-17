package edu.cornell.kfs.vnd.batch;

public enum VendorEmployeeComparisonCsv {
    KFS_VENDOR_ID("KFS_VENDOR_ID", "vendorId"),
    VENDOR_SSN("VENDOR_SSN", "vendorTaxNumber");

    public final String headerLabel;
    public final String vendorDtoPropertyName;

    private VendorEmployeeComparisonCsv(String headerLabel, String vendorDtoPropertyName) {
        this.headerLabel = headerLabel;
        this.vendorDtoPropertyName = vendorDtoPropertyName;
    }

    public String getHeaderLabel() {
        return headerLabel;
    }

    public String getVendorDtoPropertyName() {
        return vendorDtoPropertyName;
    }
}
