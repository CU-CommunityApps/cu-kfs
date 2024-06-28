package edu.cornell.kfs.vnd.batch.service.impl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static final class Converters {
        public static String toCsvRow(final VendorComparisonResultRow resultRow) {
            return Stream.of(
                    resultRow.vendorId(), resultRow.employeeId(), resultRow.netId(), resultRow.active(),
                    resultRow.hireDate(), resultRow.terminationDate(),
                    resultRow.terminationDateGreaterThanProcessingDate()
            ).collect(Collectors.joining(
                    CUKFSConstants.COMMA_WITH_QUOTES, CUKFSConstants.DOUBLE_QUOTE, CUKFSConstants.DOUBLE_QUOTE));
        }
    }

}
