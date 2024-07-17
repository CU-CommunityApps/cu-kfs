package edu.cornell.kfs.vnd.batch;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants.OptionLabels;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.vnd.businessobject.VendorEmployeeComparisonResult;

public enum VendorEmployeeComparisonResultCsv {
    KFS_VENDOR_ID(
            Utils.dtoStringPropertySetter(VendorEmployeeComparisonResult::setVendorId)),
    Employee_ID(
            Utils.dtoStringPropertySetter(VendorEmployeeComparisonResult::setEmployeeId)),
    NetID(
            Utils.dtoStringPropertySetter(VendorEmployeeComparisonResult::setNetId)),
    Active_Status(
            Utils.dtoBooleanPropertySetter(VendorEmployeeComparisonResult::setActive)),
    Hire_Date(
            Utils.dtoDatePropertySetter(VendorEmployeeComparisonResult::setHireDate)),
    Termination_Date(
            Utils.dtoDatePropertySetter(VendorEmployeeComparisonResult::setTerminationDate)),
    Termination_Date_Greater_Than_Processing_Date(
            Utils.dtoDatePropertySetter(VendorEmployeeComparisonResult::setTerminationDateGreaterThanProcessingDate));

    private final BiConsumer<VendorEmployeeComparisonResult, String> dtoPropertySetter;

    private VendorEmployeeComparisonResultCsv(
            final BiConsumer<VendorEmployeeComparisonResult, String> dtoPropertySetter) {
        this.dtoPropertySetter = dtoPropertySetter;
    }

    public BiConsumer<VendorEmployeeComparisonResult, String> getDtoPropertySetterWithAutomaticStringConversion() {
        return dtoPropertySetter;
    }

    public static final class Utils {
        public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
                .ofPattern(CUKFSConstants.DATE_FORMAT_yyyy_MM_dd, Locale.US);

        private static BiConsumer<VendorEmployeeComparisonResult, String> dtoStringPropertySetter(
                final BiConsumer<VendorEmployeeComparisonResult, String> setter) {
            return setter;
        }

        private static BiConsumer<VendorEmployeeComparisonResult, String> dtoBooleanPropertySetter(
                final BiConsumer<VendorEmployeeComparisonResult, Boolean> setter) {
            return (resultRow, propertyStringValue) -> {
                if (StringUtils.isBlank(propertyStringValue)) {
                    setter.accept(resultRow, null);
                } else if (StringUtils.equalsIgnoreCase(propertyStringValue, OptionLabels.YES)) {
                    setter.accept(resultRow, Boolean.TRUE);
                } else if (StringUtils.equalsIgnoreCase(propertyStringValue, OptionLabels.NO)) {
                    setter.accept(resultRow, Boolean.FALSE);
                } else {
                    throw new IllegalArgumentException("Invalid Yes/No string value detected: " + propertyStringValue);
                }
            };
        }

        private static BiConsumer<VendorEmployeeComparisonResult, String> dtoDatePropertySetter(
                final BiConsumer<VendorEmployeeComparisonResult, LocalDate> setter) {
            return (resultRow, propertyStringValue) -> {
                if (StringUtils.isBlank(propertyStringValue)) {
                    setter.accept(resultRow, null);
                } else {
                    final LocalDate propertyLocalDateValue = LocalDate.parse(propertyStringValue, DATE_FORMATTER);
                    setter.accept(resultRow, propertyLocalDateValue);
                }
            };
        }
    }

}
