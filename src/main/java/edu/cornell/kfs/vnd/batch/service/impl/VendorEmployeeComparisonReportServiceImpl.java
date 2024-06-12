package edu.cornell.kfs.vnd.batch.service.impl;

import java.text.MessageFormat;
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
        writeSummarySection(csvFileName, resultRows);
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

    private void writeSummarySection(final String csvFileName,
            final List<VendorEmployeeComparisonResult> resultRows) {
        final long totalActiveEmployees = resultRows.stream()
                .filter(resultRow -> resultRow.isActive())
                .count();
        final long totalEmployeesWithUpcomingTerminations = resultRows.stream()
                .filter(resultRow -> resultRow.isActive()
                        && resultRow.getTerminationDateGreaterThanProcessingDate() != null)
                .count();
        final long totalRecentEmployees = resultRows.size() - totalActiveEmployees;
        
        writeSectionHeader("Summary");
        writeSummaryLine("Total Vendors Representing Current or Recent Employees", resultRows.size());
        writeSummaryLine("Total Vendors Representing Active Employees", totalActiveEmployees);
        writeSummaryLine("Total Vendors Representing Active Employees with Upcoming Termination Dates",
                totalEmployeesWithUpcomingTerminations);
        writeSummaryLine("Total Vendors Representing Employees Terminated within the Past Year", totalRecentEmployees);
        writeSectionFooter();
        reportWriterService.writeNewLines(2);
    }

    private void writeSectionHeader(final String sectionTitle) {
        writeMessageLine(CUVendorKeyConstants.VENDOR_EMPLOYEE_COMPARISON_REPORT_SECTION_OPENING, sectionTitle);
    }

    private void writeSectionFooter() {
        writeMessageLine(CUVendorKeyConstants.VENDOR_EMPLOYEE_COMPARISON_REPORT_SECTION_OPENING);
    }

    private void writeSummaryLine(final String label, final Object value) {
        writeMessageLine(CUVendorKeyConstants.VENDOR_EMPLOYEE_COMPARISON_REPORT_SUMMARY_LINE, label, value);
    }

    private void writeDetailSummaryLine(final String label, final String description) {
        writeMessageLine(CUVendorKeyConstants.VENDOR_EMPLOYEE_COMPARISON_REPORT_DETAIL_SUMMARY, label, description);
    }

    private void writeDetailItemLine(final String label, final Object value) {
        writeMessageLine(CUVendorKeyConstants.VENDOR_EMPLOYEE_COMPARISON_REPORT_DETAIL_ITEM, label, value);
    }

    private void writeMessageLine(final String propertyName, final Object... arguments) {
        final String linePattern = getProperty(propertyName);
        final String reportLine = MessageFormat.format(linePattern, arguments);
        reportWriterService.writeFormattedMessageLine(reportLine);
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
