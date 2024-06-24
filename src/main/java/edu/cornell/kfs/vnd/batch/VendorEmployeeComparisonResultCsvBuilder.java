package edu.cornell.kfs.vnd.batch;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import edu.cornell.kfs.vnd.businessobject.VendorEmployeeComparisonResult;

public final class VendorEmployeeComparisonResultCsvBuilder {

    private VendorEmployeeComparisonResultCsvBuilder() {
        throw new UnsupportedOperationException("do not call");
    }

    public static List<VendorEmployeeComparisonResult> buildVendorEmployeeComparisonResults(
            final List<Map<String,String>> parseDataList) {
        return parseDataList.stream()
                .map(VendorEmployeeComparisonResultCsvBuilder::buildVendorEmployeeComparisonResultRow)
                .collect(Collectors.toUnmodifiableList());
    }

    private static VendorEmployeeComparisonResult buildVendorEmployeeComparisonResultRow(
            final Map<String, String> rowData) {
        final VendorEmployeeComparisonResult resultRow = new VendorEmployeeComparisonResult();
        for (final VendorEmployeeComparisonResultCsv columnDefinition : VendorEmployeeComparisonResultCsv.values()) {
            final BiConsumer<VendorEmployeeComparisonResult, String> propertySetter = columnDefinition
                    .getDtoPropertySetterWithAutomaticStringConversion();
            final String columnValue = rowData.get(columnDefinition.name());
            propertySetter.accept(resultRow, columnValue);
        }
        return resultRow;
    }

}
