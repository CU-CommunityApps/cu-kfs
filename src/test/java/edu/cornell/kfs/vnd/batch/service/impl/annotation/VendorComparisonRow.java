package edu.cornell.kfs.vnd.batch.service.impl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.vnd.businessobject.VendorWithTaxId;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VendorComparisonRow {

    String vendorId();

    String taxId();

    boolean forceException() default false;

    public static final class Converters {
        public static VendorWithTaxId toVendorWithTaxId(final VendorComparisonRow comparisonRow) {
            if (comparisonRow.forceException()) {
                throw new RuntimeException("Forcing a RuntimeException for: " + comparisonRow.vendorId());
            }
            final VendorWithTaxId vendor = new VendorWithTaxId();
            vendor.setVendorId(comparisonRow.vendorId());
            vendor.setVendorTaxNumber(comparisonRow.taxId());
            return vendor;
        }

        public static String toCsvRow(final VendorComparisonRow comparisonRow) {
            return Stream.of(comparisonRow.vendorId(), comparisonRow.taxId())
                    .collect(Collectors.joining(CUKFSConstants.COMMA_WITH_QUOTES,
                            CUKFSConstants.DOUBLE_QUOTE, CUKFSConstants.DOUBLE_QUOTE));
        }
    }

}
