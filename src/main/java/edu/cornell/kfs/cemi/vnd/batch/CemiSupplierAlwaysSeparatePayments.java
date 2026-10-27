package edu.cornell.kfs.cemi.vnd.batch;

import java.util.Arrays;

// TODO: Replace this enum with a mapping table.
public enum CemiSupplierAlwaysSeparatePayments {
    VENDOR_7840_0(7840, 0),
    VENDOR_12587_0(12587, 0),
    VENDOR_12587_1(12587, 1);

    public final Integer vendorHeaderGeneratedIdentifier;
    public final Integer vendorDetailAssignedIdentifier;

    private CemiSupplierAlwaysSeparatePayments(final Integer vendorHeaderGeneratedIdentifier,
            final Integer vendorDetailAssignedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    public static boolean shouldAlwaysSeparatePaymentsForVendor(final Integer vendorHeaderGeneratedIdentifier,
            final Integer vendorDetailAssignedIdentifier) {
        return Arrays.stream(CemiSupplierAlwaysSeparatePayments.values())
                .anyMatch(entry -> entry.vendorHeaderGeneratedIdentifier.equals(vendorHeaderGeneratedIdentifier)
                        && entry.vendorDetailAssignedIdentifier.equals(vendorDetailAssignedIdentifier));
    }

}
