package edu.cornell.kfs.vnd.batch.service;

import java.io.File;
import java.util.List;

import edu.cornell.kfs.vnd.businessobject.VendorEmployeeComparisonResult;

public interface VendorEmployeeComparisonReportService {

    File generateReportForVendorEmployeeComparisonResults(final String csvFileName,
            final List<VendorEmployeeComparisonResult> resultRows);

    void cleanUpFailedReportGenerationQuietly();

}
