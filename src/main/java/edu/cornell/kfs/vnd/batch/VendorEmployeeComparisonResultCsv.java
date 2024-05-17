package edu.cornell.kfs.vnd.batch;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.util.Truth;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.vnd.businessobject.VendorEmployeeComparisonResult;

public enum VendorEmployeeComparisonResultCsv {
    KFS_VENDOR_ID(
            Utils.dtoStringPropertySetter(VendorEmployeeComparisonResult::setVendorId)),
    EMPLOYEE_ID(
            Utils.dtoStringPropertySetter(VendorEmployeeComparisonResult::setEmployeeId)),
    NET_ID(
            Utils.dtoStringPropertySetter(VendorEmployeeComparisonResult::setNetId)),
    ACTIVE_STATUS(
            Utils.dtoBooleanPropertySetter(VendorEmployeeComparisonResult::setActive)),
    HIRE_DATE(
            Utils.dtoDatePropertySetter(VendorEmployeeComparisonResult::setHireDate)),
    TERMINATION_DATE(
            Utils.dtoDatePropertySetter(VendorEmployeeComparisonResult::setTerminationDate));

    private final BiConsumer<VendorEmployeeComparisonResult, String> dtoPropertySetter;

    private VendorEmployeeComparisonResultCsv(
            final BiConsumer<VendorEmployeeComparisonResult, String> dtoPropertySetter) {
        this.dtoPropertySetter = dtoPropertySetter;
    }

    public BiConsumer<VendorEmployeeComparisonResult, String> getDtoPropertySetterWithAutomaticStringConversion() {
        return dtoPropertySetter;
    }

    public static final class Utils {
        public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(
                CUKFSConstants.DATE_FORMAT_yyyy_MM_ddxxx, Locale.US);

        private static BiConsumer<VendorEmployeeComparisonResult, String> dtoStringPropertySetter(
                final BiConsumer<VendorEmployeeComparisonResult, String> setter) {
            return setter;
        }

        private static BiConsumer<VendorEmployeeComparisonResult, String> dtoBooleanPropertySetter(
                final BiConsumer<VendorEmployeeComparisonResult, Boolean> setter) {
            return (resultRow, propertyStringValue) -> {
                final Boolean propertyBooleanValue = Truth.strToBooleanIgnoreCase(propertyStringValue, Boolean.FALSE);
                setter.accept(resultRow, propertyBooleanValue);
            };
        }

        private static BiConsumer<VendorEmployeeComparisonResult, String> dtoDatePropertySetter(
                final BiConsumer<VendorEmployeeComparisonResult, Date> setter) {
            return (resultRow, propertyStringValue) -> {
                if (StringUtils.isBlank(propertyStringValue)) {
                    setter.accept(resultRow, null);
                } else {
                    final ZonedDateTime dateTime = ZonedDateTime.parse(propertyStringValue, DATE_FORMATTER);
                    final Date propertyDateValue = Date.from(dateTime.toInstant());
                    setter.accept(resultRow, propertyDateValue);
                }
            };
        }
    }

}
