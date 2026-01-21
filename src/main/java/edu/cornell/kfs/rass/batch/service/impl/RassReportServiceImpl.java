package edu.cornell.kfs.rass.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;
import org.kuali.kfs.core.api.config.property.ConfigurationService;

import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;
import edu.cornell.kfs.rass.RassKeyConstants;
import edu.cornell.kfs.rass.RassParameterConstants;
import edu.cornell.kfs.rass.batch.RassBatchJobReport;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResult;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResultGrouping;
import edu.cornell.kfs.rass.batch.RassStep;
import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.RassXmlFileProcessingResult;
import edu.cornell.kfs.rass.batch.service.RassReportService;
import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.ReportWriterService;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class RassReportServiceImpl implements RassReportService {
    private static final Logger LOG = LogManager.getLogger(RassReportServiceImpl.class);

    protected String reportFileNamePrefixFormat;
    protected EmailService emailService;
    protected ParameterService parameterService;
    protected ReportWriterService reportWriterService;
    protected ConfigurationService configurationService;

    protected void sendReportEmail() {
        BodyMailMessage message = new BodyMailMessage();
        String fromAddress = getFromAddress();
        Collection<String> toAddresses = getToAddresses();
        message.setFromAddress(fromAddress);
        String subject = reportWriterService.getTitle();
        message.setSubject(subject);
        message.getToAddresses().addAll(toAddresses);
        String body = getFileContents(reportWriterService.getReportFile().getAbsolutePath());
        message.setMessage(body);

        boolean htmlMessage = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmail, from address: " + fromAddress + "  to address: " + toAddresses.toString());
            LOG.debug("sendEmail, the email subject: " + subject);
            LOG.debug("sendEmail, the email budy: " + body);
        }
        try {
            emailService.sendMessage(message, htmlMessage);
        } catch (Exception e) {
            LOG.error("sendEmail, the email could not be sent", e);
        }
    }

    private Collection<String> getToAddresses() {
        return parameterService.getParameterValuesAsString(RassStep.class, RassParameterConstants.TO_REPORT_EMAIL_ADDRESSES);
    }

    private String getFromAddress() {
        return parameterService.getParameterValueAsString(RassStep.class, RassParameterConstants.FROM_REPORT_EMAIL_ADDRESS);
    }

    public String getFileContents(String fileName) {
        try {
            byte[] fileByteArray = LoadFileUtils.safelyLoadFileBytes(fileName);
            String formattedString = new String(fileByteArray);
            return formattedString;
        } catch (RuntimeException e) {
            LOG.error("getFileContents, unable to read the file.", e);
            return StringUtils.EMPTY;
        }
    }

    @Override
    public void writeBatchJobReports(RassBatchJobReport rassBatchJobReport) {
        LOG.info("writeBatchJobReports: start writing RASS batch job reports for each file");

        List<RassXmlFileParseResult> fileParseResults = rassBatchJobReport.getFileParseResults();
        Map<String, RassXmlFileProcessingResult> fileProcessingResults = rassBatchJobReport.getFileProcessingResults();
        List<RassXmlFileParseResult> errorResults = new ArrayList<>();

        for (RassXmlFileParseResult fileParseResult : fileParseResults) {
            if (RassConstants.RassParseResultCode.ERROR.equals(fileParseResult.getResultCode())) {
                errorResults.add(fileParseResult);
                continue;
            }
            RassXmlFileProcessingResult fileResult = fileProcessingResults.get(fileParseResult.getRassXmlFileName());
            initializeNewReportForFile(fileParseResult);
            writeReportSummary(fileResult);
            writeReportDetails(fileResult);
            finalizeReportForCurrentFile();
            sendReportEmail();
        }
        
        if (!errorResults.isEmpty()) {
            LOG.info("writeBatchJobReports: One or more RASS files failed to be parsed; writing error report file");
            writeErrorReport(errorResults);
            sendReportEmail();
        }
        
        LOG.info("writeBatchJobReports: finished writing RASS batch job reports");
    }

    private void writeErrorReport(List<RassXmlFileParseResult> errorResults) {
        initializeNewReport(RassConstants.RASS_PARSE_ERRORS_BASE_FILENAME);
        reportWriterService.writeFormattedMessageLine("RASS Error Report");
        reportWriterService.writeNewLines(1);
        writeFormattedMessageLineForConfigProperty(RassKeyConstants.MESSAGE_RASS_REPORT_ERROR_HEADER_LINE1);
        writeFormattedMessageLineForConfigProperty(RassKeyConstants.MESSAGE_RASS_REPORT_ERROR_HEADER_LINE2);
        writeFormattedMessageLineForConfigProperty(RassKeyConstants.MESSAGE_RASS_REPORT_ERROR_HEADER_LINE3);
        reportWriterService.writeNewLines(1);
        for (RassXmlFileParseResult errorResult : errorResults) {
            String xmlFileName = getFileNameWithoutPath(errorResult.getRassXmlFileName());
            reportWriterService.writeFormattedMessageLine(xmlFileName);
        }
        finalizeReportForCurrentFile();
    }

    private void initializeNewReportForFile(RassXmlFileParseResult fileParseResult) {
        String bareXmlFileName = getFileNameWithoutPathOrExtension(fileParseResult.getRassXmlFileName());
        initializeNewReport(bareXmlFileName);
    }

    private void initializeNewReport(String baseFileName) {
        String fileNamePrefix = MessageFormat.format(reportFileNamePrefixFormat, baseFileName);
        reportWriterService.setFileNamePrefix(fileNamePrefix);
        reportWriterService.initialize();
    }

    private void writeFormattedMessageLineForConfigProperty(String configPropertyName) {
        String message = configurationService.getPropertyValueAsString(configPropertyName);
        reportWriterService.writeFormattedMessageLine(message);
    }

    private String getFileNameWithoutPathOrExtension(String xmlFileName) {
        String result = getFileNameWithoutPath(xmlFileName);
        result = StringUtils.substringBefore(result, KFSConstants.DELIMITER);
        return result;
    }

    private String getFileNameWithoutPath(String xmlFileName) {
        String result = xmlFileName;
        if (StringUtils.contains(result, CUKFSConstants.SLASH)) {
            result = StringUtils.substringAfterLast(xmlFileName, CUKFSConstants.SLASH);
        }
        if (StringUtils.contains(result, CUKFSConstants.BACKSLASH)) {
            result = StringUtils.substringAfterLast(result, CUKFSConstants.BACKSLASH);
        }
        return result;
    }

    private void writeReportSummary(RassXmlFileProcessingResult fileResult) {
        reportWriterService.writeFormattedMessageLine("Report for RASS XML File: %s",
                getFileNameWithoutPath(fileResult.getRassXmlFileName()));
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** Report Summary ***");
        reportWriterService.writeNewLines(1);
        writeAgencySummaryToReport(fileResult);
        reportWriterService.writeNewLines(1);
        writeAwardSummaryToReport(fileResult);
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** End of Report Summary ***");
        reportWriterService.writeNewLines(1);
    }

    private void writeAgencySummaryToReport(RassXmlFileProcessingResult fileResult) {
        RassBusinessObjectUpdateResultGrouping<Agency> agencyResultGrouping = fileResult.getAgencyResults();
        List<RassBusinessObjectUpdateResult<Agency>> agencyResults = agencyResultGrouping.getObjectResults();
        writeBOResultsSummaryToReport(agencyResults, Agency.class);
    }

    private void writeAwardSummaryToReport(RassXmlFileProcessingResult fileResult) {
        RassBusinessObjectUpdateResultGrouping<Award> awardResultGrouping = fileResult.getAwardResults();
        List<RassBusinessObjectUpdateResult<Award>> awardResults = awardResultGrouping.getObjectResults();
        writeBOResultsSummaryToReport(awardResults, Award.class);
    }

    private <R extends PersistableBusinessObject> void writeBOResultsSummaryToReport(List<RassBusinessObjectUpdateResult<R>> results, Class businessObjectClass) {
        writeResultsSummaryToReportByResultCode(results, RassConstants.RassObjectUpdateResultCode.SUCCESS_NEW, businessObjectClass);
        writeResultsSummaryToReportByResultCode(results, RassConstants.RassObjectUpdateResultCode.SUCCESS_EDIT, businessObjectClass);
        writeResultsSummaryToReportByResultCode(results, RassConstants.RassObjectUpdateResultCode.ERROR, businessObjectClass);
        writeResultsSummaryToReportByResultCode(results, RassConstants.RassObjectUpdateResultCode.SKIPPED, businessObjectClass);
    }

    private <R extends PersistableBusinessObject> void writeResultsSummaryToReportByResultCode(List<RassBusinessObjectUpdateResult<R>> results,
            RassObjectUpdateResultCode resultCode, Class businessObjectClass) {
        List<RassBusinessObjectUpdateResult<R>> filteredResults = results.stream().filter(result -> resultCode.equals(result.getResultCode()))
                .collect(Collectors.toList());
        writeSummaryLineToReport(filteredResults, resultCode, businessObjectClass);
    }

    private <R extends PersistableBusinessObject> void writeSummaryLineToReport(List<RassBusinessObjectUpdateResult<R>> filteredResults,
            RassObjectUpdateResultCode resultCode, Class businessObjectClass) {

        String resultMessage;
        switch (resultCode) {
            case SUCCESS_NEW:
                resultMessage = " created";
                break;
            case SUCCESS_EDIT:
                resultMessage = Award.class.isAssignableFrom(businessObjectClass)
                        ? " with updates to Grant Number" : " updated";
                break;
            case ERROR:
                resultMessage = " that resulted in error";
                break;
            case SKIPPED:
                resultMessage = " skipped";
                break;
            default:
                resultMessage = StringUtils.EMPTY;
                break;
        }

        String reportMessage = "Number of " + businessObjectClass.getSimpleName() + " objects" + resultMessage + ": " + filteredResults.size() + ".";
        reportWriterService.writeFormattedMessageLine(reportMessage);
    }

    private void writeReportDetails(RassXmlFileProcessingResult fileResult) {
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** Report Details ***");
        reportWriterService.writeNewLines(1);
        writeAgencyDetailsToReport(fileResult);
        reportWriterService.writeNewLines(1);
        writeAwardDetailsToReport(fileResult);
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** End of Report Details ***");
        reportWriterService.writeNewLines(1);
    }

    private void writeAgencyDetailsToReport(RassXmlFileProcessingResult fileResult) {
        RassBusinessObjectUpdateResultGrouping<Agency> agencyResultGrouping = fileResult.getAgencyResults();
        List<RassBusinessObjectUpdateResult<Agency>> agencyResults = agencyResultGrouping.getObjectResults();
        writeResultsDetailsToReport(agencyResults, Agency.class);
    }

    private void writeAwardDetailsToReport(RassXmlFileProcessingResult fileResult) {
        RassBusinessObjectUpdateResultGrouping<Award> awardResultGrouping = fileResult.getAwardResults();
        List<RassBusinessObjectUpdateResult<Award>> awardResults = awardResultGrouping.getObjectResults();
        writeResultsDetailsToReport(awardResults, Award.class);
    }

    private <R extends PersistableBusinessObject> void writeResultsDetailsToReport(List<RassBusinessObjectUpdateResult<R>> results, Class businessObjectClass) {
        writeResultsDetailsToReportByResultCode(results, businessObjectClass, RassConstants.RassObjectUpdateResultCode.SUCCESS_NEW);
        writeResultsDetailsToReportByResultCode(results, businessObjectClass, RassConstants.RassObjectUpdateResultCode.SUCCESS_EDIT);
        writeResultsDetailsToReportByResultCode(results, businessObjectClass, RassConstants.RassObjectUpdateResultCode.ERROR);
        writeResultsDetailsToReportByResultCode(results, businessObjectClass, RassConstants.RassObjectUpdateResultCode.SKIPPED);
    }

    private <R extends PersistableBusinessObject> void writeResultsDetailsToReportByResultCode(List<RassBusinessObjectUpdateResult<R>> results,
            Class businessObjectClass, RassObjectUpdateResultCode resultCode) {
        List<RassBusinessObjectUpdateResult<R>> filteredResults = results.stream().filter(result -> resultCode.equals(result.getResultCode()))
                .collect(Collectors.toList());

        writeHeaderForResultsDetailsByResultCode(businessObjectClass, resultCode);

        for (RassBusinessObjectUpdateResult<R> result : filteredResults) {
            writeResultDetailToReport(result);
        }
    }

    private void writeHeaderForResultsDetailsByResultCode(Class businessObjectClass, RassObjectUpdateResultCode resultCode) {
        String header;
        String updateMessageSuffix;
        String objectName = businessObjectClass.getSimpleName();
        switch (resultCode) {
            case SUCCESS_NEW:
                header = "** " + objectName + " objects successfully created:";
                break;
            case SUCCESS_EDIT:
                updateMessageSuffix = Award.class.isAssignableFrom(businessObjectClass)
                        ? " objects with successful updates to Grant Number:" : " objects successfully updated:";
                header = "** " + objectName + updateMessageSuffix;
                break;
            case ERROR:
                header = "** " + objectName + " objects that had errors:";
                break;
            case SKIPPED:
                header = "** " + objectName + " objects that were skipped:";
                break;
            default:
                header = StringUtils.EMPTY;
                break;
        }
        reportWriterService.writeNewLines(1);
        reportWriterService.writeFormattedMessageLine(header);
    }

    private <R extends PersistableBusinessObject> void writeResultDetailToReport(RassBusinessObjectUpdateResult<R> result) {
        String reportMessage;
        String messagePrefix = result.getBusinessObjectClass().getSimpleName() + " #: " + result.getPrimaryKey();
        switch (result.getResultCode()) {
            case SUCCESS_NEW:
                reportMessage = messagePrefix + " created by document #: "
                        + result.getDocumentId();
                break;
            case SUCCESS_EDIT:
                reportMessage = messagePrefix + " updated by document #: "
                        + result.getDocumentId();
                break;
            case ERROR:
                reportMessage = buildErrorLines(messagePrefix + " error: ", result.getErrorMessage());
                break;
            case SKIPPED:
                reportMessage = messagePrefix + " skipped.";
                break;
            default:
                reportMessage = StringUtils.EMPTY;
                break;
        }

        reportWriterService.writeFormattedMessageLine(reportMessage);
    }
    
    private String buildErrorLines(String prefix, String errorMessage) {
        StringBuilder errorLines = new StringBuilder();
        String[] lines = errorMessage.split(KFSConstants.NEWLINE);
        for (String line : lines) {
            if (StringUtils.isNotBlank(line)) {
                errorLines.append(prefix);
                errorLines.append(line);
                errorLines.append(KFSConstants.NEWLINE);
            }
        }
        return errorLines.toString();
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

    public EmailService getEmailService() {
        return emailService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public ReportWriterService getReportWriterService() {
        return reportWriterService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
