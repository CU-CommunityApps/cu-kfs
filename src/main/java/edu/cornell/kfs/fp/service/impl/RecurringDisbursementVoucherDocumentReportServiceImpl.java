package edu.cornell.kfs.fp.service.impl;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;

import edu.cornell.kfs.fp.CuFPParameterConstants.DisbursementVoucherDocument;
import edu.cornell.kfs.fp.batch.ApproveDvsSpawnedByRecurringDvStep;
import edu.cornell.kfs.fp.batch.RecurringDisbursementVoucherDocumentRoutingReportItem;
import edu.cornell.kfs.fp.service.RecurringDisbursementVoucherDocumentReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class RecurringDisbursementVoucherDocumentReportServiceImpl
        implements RecurringDisbursementVoucherDocumentReportService {

    private static final Logger LOG = LogManager.getLogger();

    private static final String DV_ERROR_HEADER_LINE_FORMAT = "DV Document: %s, RCDV Document: %s, Errors: [";
    private static final String INDENTED_ERROR_MESSAGE_FORMAT = "    %s";

    private ReportWriterService reportWriterService;
    private EmailService emailService;
    private ParameterService parameterService;

    @Override
    public File buildDvAutoApproveErrorReport(
            List<RecurringDisbursementVoucherDocumentRoutingReportItem> reportItems) {
        List<RecurringDisbursementVoucherDocumentRoutingReportItem> errorItems = reportItems.stream()
                .filter(RecurringDisbursementVoucherDocumentRoutingReportItem::hasErrors)
                .collect(Collectors.toUnmodifiableList());
        if (errorItems.isEmpty()) {
            throw new IllegalArgumentException("No DV routing errors were found among the list of report items");
        }
        writeErrorReport(errorItems);
        return reportWriterService.getReportFile();
    }

    private void writeErrorReport(List<RecurringDisbursementVoucherDocumentRoutingReportItem> errorItems) {
        try {
            reportWriterService.initialize();
            reportWriterService.writeFormattedMessageLine("The following DVs were spawned from Recurring DVs");
            reportWriterService.writeFormattedMessageLine("but could not be auto-approved:");
            reportWriterService.writeNewLines(1);
            reportWriterService.writeFormattedMessageLine("--------------------");
            reportWriterService.writeNewLines(1);
            for (RecurringDisbursementVoucherDocumentRoutingReportItem errorItem : errorItems) {
                reportWriterService.writeFormattedMessageLine(DV_ERROR_HEADER_LINE_FORMAT,
                        errorItem.getSpawnedDvDocumentNumber(), errorItem.getRecurringDvDocumentNumber());
                for (String errorMessage : errorItem.getErrors()) {
                    reportWriterService.writeFormattedMessageLine(INDENTED_ERROR_MESSAGE_FORMAT, errorMessage);
                }
                reportWriterService.writeFormattedMessageLine(KFSConstants.SQUARE_BRACKET_RIGHT);
                reportWriterService.writeNewLines(1);
            }
            reportWriterService.writeFormattedMessageLine("--------------------");
            reportWriterService.writeNewLines(1);
            reportWriterService.writeFormattedMessageLine("End of Report");
        } finally {
            reportWriterService.destroy();
        }
    }

    @Override
    public void sendDvAutoApproveErrorReportEmail(File reportFile) {
        String body = getReportFileContents(reportFile);
        String subject = getDvAutoApproveReportParameterValue(
                DisbursementVoucherDocument.DV_AUTO_APPROVE_ERROR_EMAIL_SUBJECT);
        String fromAddress = getDvAutoApproveReportParameterValue(
                DisbursementVoucherDocument.DV_AUTO_APPROVE_ERROR_FROM_EMAIL_ADDRESS);
        Collection<String> toAddresses = getDvAutoApproveReportParameterValues(
                DisbursementVoucherDocument.DV_AUTO_APPROVE_ERROR_TO_EMAIL_ADDRESSES);

        BodyMailMessage mailMessage = new BodyMailMessage();
        mailMessage.setFromAddress(fromAddress);
        mailMessage.getToAddresses().addAll(toAddresses);
        mailMessage.setSubject(subject);
        mailMessage.setMessage(body);

        try {
            emailService.sendMessage(mailMessage, false);
        } catch (RuntimeException e) {
            LOG.error("sendDvAutoApproveErrorReportEmail, Could not send error report email from {} to {}",
                    fromAddress, toAddresses, e);
            throw e;
        }
    }

    private String getDvAutoApproveReportParameterValue(String parameterName) {
        return parameterService.getParameterValueAsString(ApproveDvsSpawnedByRecurringDvStep.class, parameterName);
    }

    private Collection<String> getDvAutoApproveReportParameterValues(String parameterName) {
        return parameterService.getParameterValuesAsString(ApproveDvsSpawnedByRecurringDvStep.class, parameterName);
    }

    private String getReportFileContents(File reportFile) {
        byte[] fileContents = LoadFileUtils.safelyLoadFileBytes(reportFile);
        return new String(fileContents, StandardCharsets.UTF_8);
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
