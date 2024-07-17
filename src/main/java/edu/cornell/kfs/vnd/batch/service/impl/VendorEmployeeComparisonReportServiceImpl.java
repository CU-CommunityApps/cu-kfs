package edu.cornell.kfs.vnd.batch.service.impl;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.OptionLabels;

import edu.cornell.kfs.sys.batch.CuBatchFileUtils;
import edu.cornell.kfs.sys.service.ReportWriterService;
import edu.cornell.kfs.sys.util.ForUnitTestConvenience;
import edu.cornell.kfs.vnd.CUVendorKeyConstants.EmployeeComparisonReportKeys;
import edu.cornell.kfs.vnd.batch.VendorEmployeeComparisonResultCsv;
import edu.cornell.kfs.vnd.batch.service.VendorEmployeeComparisonReportService;
import edu.cornell.kfs.vnd.businessobject.VendorEmployeeComparisonResult;

public class VendorEmployeeComparisonReportServiceImpl implements VendorEmployeeComparisonReportService {

    private enum ReportStatistic {
        TOTAL_EMPLOYEES(resultRow -> true),
        TOTAL_ACTIVE_EMPLOYEES(VendorEmployeeComparisonResult::isActive),
        TOTAL_ACTIVE_REHIRED_EMPLOYEES(resultRow -> resultRow.isActive() && resultRow.getTerminationDate() != null),
        TOTAL_EMPLOYEES_PENDING_TERMINATION(
                resultRow -> resultRow.isActive() && resultRow.getTerminationDateGreaterThanProcessingDate() != null),
        TOTAL_EMPLOYEES_RECENTLY_TERMINATED(VendorEmployeeComparisonResult::isInactive),
        TOTAL_ROWS_WITH_MISSING_DATA(resultRow ->
                StringUtils.isAnyBlank(resultRow.getVendorId(), resultRow.getEmployeeId(), resultRow.getNetId())
                         || resultRow.getActive() == null
                         || resultRow.getHireDate() == null);

        private final Predicate<VendorEmployeeComparisonResult> rowFilter;

        private ReportStatistic(final Predicate<VendorEmployeeComparisonResult> rowFilter) {
            this.rowFilter = rowFilter;
        }
    }

    private static final int SECTION_TITLE_LENGTH = 49;

    private ReportWriterService reportWriterService;
    private ConfigurationService configurationService;

    @Override
    public File generateReportForVendorEmployeeComparisonResults(final String csvFileName,
            final List<VendorEmployeeComparisonResult> resultRows) {
        initializeReportTitleAndFileName();
        writeTwoEmptyLines();
        writeSummarySection(csvFileName, resultRows);
        writeTwoEmptyLines();
        writeDetailSection(resultRows);
        reportWriterService.destroy();
        return reportWriterService.getReportFile();
    }

    private void initializeReportTitleAndFileName() {
        reportWriterService.setFileNamePrefix(
                getProperty(EmployeeComparisonReportKeys.FILE_NAME));
        reportWriterService.setTitle(
                getProperty(EmployeeComparisonReportKeys.TITLE));
        reportWriterService.initialize();
    }

    private void writeSummarySection(final String csvFileName,
            final List<VendorEmployeeComparisonResult> resultRows) {
        final String csvFileNameWithoutPath = CuBatchFileUtils.getFileNameWithoutPath(csvFileName);
        final String fileNameLabel = getProperty(EmployeeComparisonReportKeys.SUMMARY_PROCESSED_FILE_NAME_LABEL);
        writeSectionHeader(EmployeeComparisonReportKeys.SUMMARY_SECTION_TITLE);
        writeMessageLine(EmployeeComparisonReportKeys.SUMMARY_PROCESSED_FILE_NAME,
                fileNameLabel, csvFileNameWithoutPath);
        writeEmptyLine();
        for (final ReportStatistic statistic : ReportStatistic.values()) {
            final long computedValue = resultRows.stream()
                    .filter(statistic.rowFilter)
                    .count();
            writeSummaryLine(statistic, computedValue);
        }
    }

    private void writeSummaryLine(final ReportStatistic statistic, final long value) {
        final String lineLabel = getProperty(EmployeeComparisonReportKeys.SUMMARY_LABEL_PREFIX + statistic.name());
        writeMessageLine(EmployeeComparisonReportKeys.SUMMARY_LINE, lineLabel, value);
    }

    private void writeDetailSection(final List<VendorEmployeeComparisonResult> resultRows) {
        writeSectionHeader(EmployeeComparisonReportKeys.DETAIL_SECTION_TITLE);
        if (resultRows.isEmpty()) {
            writeMessageLine(EmployeeComparisonReportKeys.EMPTY_DETAIL_SECTION_MESSAGE);
            writeEmptyLine();
            return;
        }
        writeMessageLine(EmployeeComparisonReportKeys.DETAIL_TABLE_HEADER);
        writeMessageLine(EmployeeComparisonReportKeys.DETAIL_TABLE_SEPARATOR);
        writeEmptyLine();
        for (final VendorEmployeeComparisonResult resultRow : resultRows) {
            writeDetailRow(resultRow);
            writeEmptyLine();
        }
    }

    private void writeDetailRow(final VendorEmployeeComparisonResult resultRow) {
        writeMessageLine(EmployeeComparisonReportKeys.DETAIL_TABLE_ROW,
                formatStringCell(resultRow.getVendorId()),
                formatStringCell(resultRow.getEmployeeId()),
                formatStringCell(resultRow.getNetId()),
                formatBooleanCell(resultRow.getActive()),
                formatDateCell(resultRow.getHireDate()),
                formatDateCell(resultRow.getTerminationDate()),
                formatDateCell(resultRow.getTerminationDateGreaterThanProcessingDate()));
    }

    private String formatStringCell(final String value) {
        return StringUtils.defaultIfBlank(value, KFSConstants.NOT_AVAILABLE_STRING);
    }

    private String formatBooleanCell(final Boolean value) {
        if (value == null) {
            return KFSConstants.NOT_AVAILABLE_STRING;
        }
        return value ? OptionLabels.YES : OptionLabels.NO;
    }

    private String formatDateCell(final LocalDate value) {
        return value != null
                ? value.format(VendorEmployeeComparisonResultCsv.Utils.DATE_FORMATTER)
                : KFSConstants.NOT_AVAILABLE_STRING;
    }

    private void writeSectionHeader(final String sectionTitleKey) {
        final String sectionTitle = getProperty(sectionTitleKey);
        final String paddedTitle = StringUtils.center(sectionTitle, SECTION_TITLE_LENGTH);
        writeMessageLine(EmployeeComparisonReportKeys.SECTION_SEPARATOR);
        writeMessageLine(EmployeeComparisonReportKeys.SECTION_HEADER, paddedTitle);
        writeMessageLine(EmployeeComparisonReportKeys.SECTION_SEPARATOR);
        writeTwoEmptyLines();
    }

    private void writeMessageLine(final String propertyName, final Object... arguments) {
        final String linePattern = getProperty(propertyName);
        reportWriterService.writeFormattedMessageLine(linePattern, arguments);
    }

    private void writeEmptyLine() {
        reportWriterService.writeNewLines(1);
    }

    private void writeTwoEmptyLines() {
        reportWriterService.writeNewLines(2);
    }

    private String getProperty(final String propertyName) {
        return configurationService.getPropertyValueAsString(propertyName);
    }

    @ForUnitTestConvenience
    protected void manuallyCleanUpInternalReportWriter() {
        reportWriterService.destroy();
    }

    public void setReportWriterService(final ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
