package edu.cornell.kfs.pdp.batch.service.impl;

import java.text.MessageFormat;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractFileResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractReportData;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractRetryResult;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractReportService;
import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class PayeeACHAccountExtractReportServiceImpl implements PayeeACHAccountExtractReportService {

    private static final Logger LOG = LogManager.getLogger(PayeeACHAccountExtractReportServiceImpl.class);

    protected String reportFileNamePrefixFormat;
    protected ReportWriterService reportWriterService;

    @Override
    public void writeBatchJobReports(PayeeACHAccountExtractReportData achReport) {
        LOG.info("writeBatchJobReports: Start writing ACH Extract batch job reports");

        PayeeACHAccountExtractRetryResult retryResults = achReport.getAchAccountExtractRetryResults();
        writeBatchJobRetryReport(retryResults);
        for (PayeeACHAccountExtractFileResult fileResult : achReport.getAchAccountExtractFileResults()) {
            writeBatchJobReport(fileResult);
        }

        LOG.info("writeBatchJobReports: Finished writing ACH Extract batch job reports");
    }

    private void writeBatchJobRetryReport(PayeeACHAccountExtractRetryResult retryResult) {

        LOG.info("writeBatchJobReport: Start writing ACH Extract report for retries ");

        initializeNewReport("retry");
        writeReportSummary(retryResult);
        writeReportDetails(retryResult);
        finalizeReportForCurrentFile();

        LOG.info("writeBatchJobReport: Finished writing ACH Extract for retries");
    }

    private void writeBatchJobReport(PayeeACHAccountExtractFileResult fileResult) {
        String baseName = getFileNameWithoutPathOrExtension(fileResult.getFileName());

        LOG.info("writeBatchJobReport: Start writing ACH Extract report for file: " + fileResult.getFileName());

        initializeNewReport(baseName);
        writeReportSummaryTitleForFile(fileResult);
        writeReportSummary(fileResult);
        writeReportDetails(fileResult);
        finalizeReportForCurrentFile();

        LOG.info("writeBatchJobReport: Finished writing ACH Extract for file: " + fileResult.getFileName());
    }

    private String getFileNameWithoutPathOrExtension(String fileName) {
        String result = getFileNameWithoutPath(fileName);
        result = StringUtils.substringBefore(result, KFSConstants.DELIMITER);
        return result;
    }

    public static String getFileNameWithoutPath(String fileName) {
        String result = fileName;
        if (StringUtils.contains(result, CUKFSConstants.SLASH)) {
            result = StringUtils.substringAfterLast(fileName, CUKFSConstants.SLASH);
        }
        if (StringUtils.contains(result, CUKFSConstants.BACKSLASH)) {
            result = StringUtils.substringAfterLast(result, CUKFSConstants.BACKSLASH);
        }
        return result;
    }

    private void initializeNewReport(String baseName) {
        String fileNamePrefix = MessageFormat.format(reportFileNamePrefixFormat, baseName);
        reportWriterService.setFileNamePrefix(fileNamePrefix);
        reportWriterService.initialize();
    }

    private void writeReportSummaryTitleForFile(PayeeACHAccountExtractFileResult fileResult) {
        reportWriterService.writeFormattedMessageLine("Report for ACH File: %s", getFileNameWithoutPath(fileResult.getFileName()));

    }

    private void writeReportSummary(PayeeACHAccountExtractResult processingResult) {
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** Report Summary ***");
        reportWriterService.writeNewLines(1);
        writeResultCountSummariesToReport(processingResult);
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** End of Report Summary ***");
        reportWriterService.writeNewLines(1);
    }

    private void writeResultCountSummariesToReport(PayeeACHAccountExtractResult processingResult) {
        reportWriterService.writeFormattedMessageLine("Number of ACH rows successfully processed: %s", processingResult.getNumberOfRowsProcessedSuccessfully());
        reportWriterService.writeFormattedMessageLine("Number of ACH rows that failed processing: %s", processingResult.getNumberOfRowsWithFailures());
    }

    private void writeReportDetails(PayeeACHAccountExtractResult processingResult) {
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** Report Details ***");
        reportWriterService.writeNewLines(1);

        writeSuccessDetails(processingResult);
        writeErrorDetails(processingResult);

        reportWriterService.writeSubTitle("*** End of Report Details ***");
        reportWriterService.writeNewLines(1);
    }

    private void writeSuccessDetails(PayeeACHAccountExtractResult processingResult) {
        reportWriterService.writeSubTitle("*** Entries processed successfully ***");
        if (CollectionUtils.isEmpty(processingResult.getSuccessEntries())) {
            reportWriterService.writeFormattedMessageLine("* No entries successfully processed.");
        } else {
            for (PayeeACHAccountExtractDetail achDetail : processingResult.getSuccessEntries()) {
                String rowFormat = "%30s %-70s";
                reportWriterService.writeFormattedMessageLine(rowFormat, "Net id:", achDetail.getNetID());
                reportWriterService.writeFormattedMessageLine(rowFormat, "Creation Date:", achDetail.getCreateDate());
            }
        }

        reportWriterService.writeNewLines(1);
        reportWriterService.writeNewLines(1);
    }

    private void writeErrorDetails(PayeeACHAccountExtractResult processingResult) {
        reportWriterService.writeSubTitle("*** Entries processed with errors ***");
        if (processingResult.getErrorEntries().isEmpty()) {
            reportWriterService.writeFormattedMessageLine("* No entries processed with errors.");
        } else {
            for (PayeeACHAccountExtractDetail achDetail : processingResult.getErrorEntries().keySet().stream().sorted((o1,o2)-> o1.getId().compareTo(o2.getId())).collect(Collectors.toList())) {
                String rowFormat = "%30s %-70s";
                reportWriterService.writeFormattedMessageLine(rowFormat, "Net id:", achDetail.getNetID());
                reportWriterService.writeFormattedMessageLine(rowFormat, "Creation Date:", achDetail.getCreateDate());
                reportWriterService.writeFormattedMessageLine("* Errors:");
                for (String error : processingResult.getErrorEntries().get(achDetail)) {
                    reportWriterService.writeFormattedMessageLine("** " + error);
                    reportWriterService.writeNewLines(1);
                }
                reportWriterService.writeNewLines(1);
            }
        }
    }

    private void finalizeReportForCurrentFile() {
        reportWriterService.destroy();
    }

    public String getReportFileNamePrefixFormat() {
        return reportFileNamePrefixFormat;
    }

    public void setReportFileNamePrefixFormat(String reportFileNamePrefixFormat) {
        this.reportFileNamePrefixFormat = reportFileNamePrefixFormat;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

}
