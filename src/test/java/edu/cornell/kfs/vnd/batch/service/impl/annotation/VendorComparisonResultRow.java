package edu.cornell.kfs.vnd.batch.service.impl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;

import edu.cornell.kfs.sys.CUKFSConstants;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface VendorComparisonResultRow {

    String vendorId();

    String employeeId();

    String netId();

    String active();

    String hireDate();

    String terminationDate();

    String terminationDateGreaterThanProcessingDate();

    String vendorType() default KFSConstants.EMPTY_STRING;

    public static final class Converters {
        public static String toCsvRow(final VendorComparisonResultRow resultRow) {
            return Stream.of(
                    resultRow.vendorId(), resultRow.employeeId(), resultRow.netId(), resultRow.active(),
                    resultRow.hireDate(), resultRow.terminationDate(),
                    resultRow.terminationDateGreaterThanProcessingDate()
            ).collect(Collectors.joining(
                    CUKFSConstants.COMMA_WITH_QUOTES, CUKFSConstants.DOUBLE_QUOTE, CUKFSConstants.DOUBLE_QUOTE));
        }

        public static boolean canConvertToVendorDetail(final VendorComparisonResultRow resultRow) {
            return StringUtils.isNoneBlank(resultRow.vendorId(), resultRow.vendorType());
        }

        public static VendorDetail toVendorDetail(final VendorComparisonResultRow resultRow) {
            final VendorDetail vendorDetail = new VendorDetail();
            final VendorHeader vendorHeader = new VendorHeader();
            final String[] vendorIdParts = StringUtils.split(resultRow.vendorId(), KFSConstants.DASH);
            Validate.isTrue(vendorIdParts.length == 2, "Vendor ID was not formatted as two dash-delimited numbers");

            vendorHeader.setVendorHeaderGeneratedIdentifier(Integer.valueOf(vendorIdParts[0]));
            vendorHeader.setVendorTypeCode(resultRow.vendorType());
            vendorDetail.setVendorHeaderGeneratedIdentifier(vendorHeader.getVendorHeaderGeneratedIdentifier());
            vendorDetail.setVendorDetailAssignedIdentifier(Integer.valueOf(vendorIdParts[1]));
            vendorDetail.setVendorHeader(vendorHeader);

            return vendorDetail;
        }
    }

}
