package edu.cornell.kfs.rass.batch.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.module.cg.businessobject.Proposal;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;

import edu.cornell.kfs.rass.RassConstants;
import edu.cornell.kfs.rass.RassConstants.RassObjectUpdateResultCode;
import edu.cornell.kfs.rass.RassParameterConstants;
import edu.cornell.kfs.rass.batch.RassBatchJobReport;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResult;
import edu.cornell.kfs.rass.batch.RassBusinessObjectUpdateResultGrouping;
import edu.cornell.kfs.rass.batch.RassStep;
import edu.cornell.kfs.rass.batch.RassXmlFileParseResult;
import edu.cornell.kfs.rass.batch.RassXmlProcessingResults;
import edu.cornell.kfs.rass.batch.service.RassReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class RassReportServiceImpl implements RassReportService {
    private static final Logger LOG = LogManager.getLogger(RassReportServiceImpl.class);

    protected EmailService emailService;
    protected ParameterService parameterService;
    protected ReportWriterService reportWriterService;

    @Override
    public void sendReportEmail() {
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
    public void writeBatchJobReport(RassBatchJobReport rassBatchJobReport) {
        LOG.info("writeBatchJobReport: start writing RASS batch job report");
        reportWriterService.initialize();
        List<RassXmlFileParseResult> fileParseResults = rassBatchJobReport.getFileParseResults();

        for (RassXmlFileParseResult fileParseResult : fileParseResults) {
            if (RassConstants.RassParseResultCode.ERROR.equals(fileParseResult.getResultCode())) {
                reportWriterService.writeFormattedMessageLine("RASS file: " + fileParseResult.getRassXmlFileName() + " hasn't been processed successfully");
            } else {
                reportWriterService.writeFormattedMessageLine("RASS file: " + fileParseResult.getRassXmlFileName() + " has been processed successfully");

                writeReportSummary(rassBatchJobReport);
                writeReportDetails(rassBatchJobReport);
            }
        }
        reportWriterService.destroy();
        LOG.info("writeBatchJobReport: finished writing RASS batch job report");
    }

    private void writeReportSummary(RassBatchJobReport rassBatchJobReport) {
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** Report Summary ***");
        reportWriterService.writeNewLines(1);
        writeAgencySummaryToReport(rassBatchJobReport);
        reportWriterService.writeNewLines(1);
        writeProposalSummaryToReport(rassBatchJobReport);
        reportWriterService.writeNewLines(1);
        writeAwardSummaryToReport(rassBatchJobReport);
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** End of Report Summary ***");
        reportWriterService.writeNewLines(1);
    }

    private void writeAgencySummaryToReport(RassBatchJobReport rassBatchJobReport) {
        RassXmlProcessingResults processingResults = rassBatchJobReport.getProcessingResults();
        RassBusinessObjectUpdateResultGrouping<Agency> agencyResultGrouping = processingResults.getAgencyResults();
        List<RassBusinessObjectUpdateResult<Agency>> agencyResults = agencyResultGrouping.getObjectResults();
        writeBOResultsSummaryToReport(agencyResults, Agency.class, true);
    }

    private void writeProposalSummaryToReport(RassBatchJobReport rassBatchJobReport) {
        RassXmlProcessingResults processingResults = rassBatchJobReport.getProcessingResults();
        RassBusinessObjectUpdateResultGrouping<Proposal> proposalResultGrouping = processingResults.getProposalResults();
        List<RassBusinessObjectUpdateResult<Proposal>> proposalResults = proposalResultGrouping.getObjectResults();
        writeBOResultsSummaryToReport(proposalResults, Proposal.class, false);
    }

    private void writeAwardSummaryToReport(RassBatchJobReport rassBatchJobReport) {
        RassXmlProcessingResults processingResults = rassBatchJobReport.getProcessingResults();
        RassBusinessObjectUpdateResultGrouping<Award> awardResultGrouping = processingResults.getAwardResults();
        List<RassBusinessObjectUpdateResult<Award>> awardResults = awardResultGrouping.getObjectResults();
        writeBOResultsSummaryToReport(awardResults, Award.class, true);
    }

    private <R extends PersistableBusinessObject> void writeBOResultsSummaryToReport(List<RassBusinessObjectUpdateResult<R>> results, Class businessObjectClass,
            boolean shouldReportUpdates) {
        writeResultsSummaryToReportByResultCode(results, RassConstants.RassObjectUpdateResultCode.SUCCESS_NEW, businessObjectClass);
        if (shouldReportUpdates) {
            writeResultsSummaryToReportByResultCode(results, RassConstants.RassObjectUpdateResultCode.SUCCESS_EDIT, businessObjectClass);
        }
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
                resultMessage = " updated";
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

    private void writeReportDetails(RassBatchJobReport rassBatchJobReport) {
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** Report Details ***");
        reportWriterService.writeNewLines(1);
        writeAgencyDetailsToReport(rassBatchJobReport);
        reportWriterService.writeNewLines(1);
        writeProposalDetailsToReport(rassBatchJobReport);
        reportWriterService.writeNewLines(1);
        writeAwardDetailsToReport(rassBatchJobReport);
        reportWriterService.writeNewLines(1);
        reportWriterService.writeSubTitle("*** End of Report Details ***");
        reportWriterService.writeNewLines(1);
    }

    private void writeAgencyDetailsToReport(RassBatchJobReport rassBatchJobReport) {
        RassXmlProcessingResults processingResults = rassBatchJobReport.getProcessingResults();
        RassBusinessObjectUpdateResultGrouping<Agency> agencyResultGrouping = processingResults.getAgencyResults();
        List<RassBusinessObjectUpdateResult<Agency>> agencyResults = agencyResultGrouping.getObjectResults();
        writeResultsDetailsToReport(agencyResults, Agency.class, true);
    }

    private void writeProposalDetailsToReport(RassBatchJobReport rassBatchJobReport) {
        RassXmlProcessingResults processingResults = rassBatchJobReport.getProcessingResults();
        RassBusinessObjectUpdateResultGrouping<Proposal> proposalResultGrouping = processingResults.getProposalResults();
        List<RassBusinessObjectUpdateResult<Proposal>> proposalResults = proposalResultGrouping.getObjectResults();
        writeResultsDetailsToReport(proposalResults, Proposal.class, false);
    }

    private void writeAwardDetailsToReport(RassBatchJobReport rassBatchJobReport) {
        RassXmlProcessingResults processingResults = rassBatchJobReport.getProcessingResults();
        RassBusinessObjectUpdateResultGrouping<Award> awardResultGrouping = processingResults.getAwardResults();
        List<RassBusinessObjectUpdateResult<Award>> awardResults = awardResultGrouping.getObjectResults();
        writeResultsDetailsToReport(awardResults, Award.class, true);
    }

    private <R extends PersistableBusinessObject> void writeResultsDetailsToReport(List<RassBusinessObjectUpdateResult<R>> results, Class businessObjectClass,
            boolean shouldReportUpdates) {
        writeResultsDetailsToReportByResultCode(results, businessObjectClass, RassConstants.RassObjectUpdateResultCode.SUCCESS_NEW);
        if (shouldReportUpdates) {
            writeResultsDetailsToReportByResultCode(results, businessObjectClass, RassConstants.RassObjectUpdateResultCode.SUCCESS_EDIT);
        }
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
        String objectName = businessObjectClass.getSimpleName();
        switch (resultCode) {
            case SUCCESS_NEW:
                header = "** " + objectName + " objects successfully created:";
                break;
            case SUCCESS_EDIT:
                header = "** " + objectName + " objects successfully updated:";
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
        StringBuffer errorLines = new StringBuffer();
        String[] lines = errorMessage.split(KFSConstants.NEWLINE);
        for (String line : lines) {
            if (StringUtils.isNoneBlank(line)) {
                errorLines.append(prefix);
                errorLines.append(line);
                errorLines.append(KFSConstants.NEWLINE);
            }
        }
        return errorLines.toString();
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

}
