package edu.cornell.kfs.vnd.batch.service;

import java.util.List;

import edu.cornell.kfs.vnd.businessobject.VendorEmployeeComparisonResult;

public interface VendorEmployeeComparisonReportService {

    void generateReportForVendorEmployeeComparisonResults(final String csvFileName,
            final List<VendorEmployeeComparisonResult> resultRows);

}
