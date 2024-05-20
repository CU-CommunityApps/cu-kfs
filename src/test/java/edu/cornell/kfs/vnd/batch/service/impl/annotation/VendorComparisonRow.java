package edu.cornell.kfs.vnd.batch.service.impl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.vnd.businessobject.VendorWithSSN;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VendorComparisonRow {

    String vendorId();

    String taxId();

    boolean forceException() default false;

    public static final class Converters {
        public static VendorWithSSN toVendorWithSSN(final VendorComparisonRow comparisonRow) {
            if (comparisonRow.forceException()) {
                throw new RuntimeException("Forcing a RuntimeException for: " + comparisonRow);
            }
            final String vendorHeaderId = StringUtils.substringBefore(comparisonRow.vendorId(), KFSConstants.DASH);
            final String vendorDetailId = StringUtils.substringAfter(comparisonRow.vendorId(), KFSConstants.DASH);
            final VendorWithSSN ssnVendor = new VendorWithSSN();
            ssnVendor.setVendorHeaderGeneratedIdentifier(Integer.valueOf(vendorHeaderId));
            ssnVendor.setVendorDetailAssignedIdentifier(Integer.valueOf(vendorDetailId));
            ssnVendor.setVendorTaxNumber(comparisonRow.taxId());
            return ssnVendor;
        }
    }

}
