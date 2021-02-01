package edu.cornell.kfs.pdp.batch.service.impl;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.core.api.datetime.DateTimeService;

import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractFileResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractReportData;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractResult;
import edu.cornell.kfs.pdp.batch.PayeeACHAccountExtractRetryResult;
import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountExtractReportService;
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
        
        LOG.info("writeBatchJobReport: Finished writing ACH Extract for retries" );
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
              reportWriterService.writeFormattedMessageLine("Report for ACH File: %s",
                      getFileNameWithoutPath(fileResult.getFileName()));

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
        for(String error :  processingResult.getErrors()) {
            reportWriterService.writeFormattedMessageLine(error);
        }
        reportWriterService.writeSubTitle("*** End of Report Details ***");
        reportWriterService.writeNewLines(1);
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

}
