package edu.cornell.kfs.vnd.batch.service.impl;

import java.util.List;

import org.kuali.kfs.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.sys.service.ReportWriterService;
import edu.cornell.kfs.vnd.CUVendorKeyConstants;
import edu.cornell.kfs.vnd.batch.service.VendorEmployeeComparisonReportService;
import edu.cornell.kfs.vnd.businessobject.VendorEmployeeComparisonResult;

public class VendorEmployeeComparisonReportServiceImpl implements VendorEmployeeComparisonReportService {

    private ReportWriterService reportWriterService;
    private ConfigurationService configurationService;

    @Override
    public void generateReportForVendorEmployeeComparisonResults(final String csvFileName,
            final List<VendorEmployeeComparisonResult> resultRows) {
        initializeReportTitleAndFileName();
        buildSummarySection(csvFileName, resultRows);
        reportWriterService.destroy();
    }

    private void initializeReportTitleAndFileName() {
        reportWriterService.setFileNamePrefix(
                getProperty(CUVendorKeyConstants.VENDOR_EMPLOYEE_COMPARISON_REPORT_FILE_NAME));
        reportWriterService.setTitle(
                getProperty(CUVendorKeyConstants.VENDOR_EMPLOYEE_COMPARISON_REPORT_TITLE));
        reportWriterService.initialize();
        reportWriterService.writeNewLines(2);
    }

    private void buildSummarySection(final String csvFileName,
            final List<VendorEmployeeComparisonResult> resultRows) {
        // TODO: Implement!
    }

    private String getProperty(final String propertyName) {
        return configurationService.getPropertyValueAsString(propertyName);
    }

    public void setReportWriterService(final ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
