package edu.cornell.kfs.module.purap.batch.service.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateSupplierXmlReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;
import edu.cornell.kfs.sys.util.LoadFileUtils;

public class JaggaerGenerateSupplierXmlReportServiceImpl implements JaggaerGenerateSupplierXmlReportService {
    private static final Logger LOG = LogManager.getLogger();
    private static final String SUMMARY = "Summary";
    private static final String ACTIVE = "Active";
    private static final String INACTIVE = "Inactive";
    private static final String WITH = "with";
    private static final String WITHOUT = "without";
    
    protected ConfigurationService configurationService;
    protected ParameterService parameterService;
    protected ReportWriterService reportWriterService;
    protected EmailService emailService;

    public void generateAndEmailResultsReport(List<JaggaerUploadSupplierXmlFileDetailsDto> xmlFileDtos) {
        reportWriterService.initialize();
        buildSummarySection(xmlFileDtos);
        buildDetailSections(xmlFileDtos);
        emailReport();
        reportWriterService.destroy();
    }

    private void buildSummarySection(List<JaggaerUploadSupplierXmlFileDetailsDto> xmlFileDtos) {
        reportWriterService.writeSubTitle(formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_HEADER, SUMMARY));
        int dtoCount = 0;
        for (JaggaerUploadSupplierXmlFileDetailsDto dto : xmlFileDtos) {
            if (dtoCount > 0) {
                reportWriterService.writeNewLines(2);
            }
            reportWriterService.writeFormattedMessageLine(formatPropertyValue(
                    CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_FILE_NAME, dto.getXmlFileName()));

            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_TOTAL, ACTIVE,
                            String.valueOf(dto.getActiveVendors().size())));
            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL,
                            ACTIVE, WITH, String.valueOf(filterDetailDto(dto.getActiveVendors(), true).size())));
            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL,
                            ACTIVE, WITHOUT, String.valueOf(filterDetailDto(dto.getActiveVendors(), false).size())));

            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_TOTAL, INACTIVE,
                            String.valueOf(dto.getInactiveVendors().size())));
            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL,
                            INACTIVE, WITH, String.valueOf(filterDetailDto(dto.getInactiveVendors(), true).size())));
            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL,
                            INACTIVE, WITHOUT, String.valueOf(filterDetailDto(dto.getInactiveVendors(), false).size())));

            dtoCount++;
        }
        reportWriterService.writeSubTitle(formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_FOOTER));
    }

    private String formatPropertyValue(String propertyName, String... args) {
        String propertyValue = configurationService.getPropertyValueAsString(propertyName);
        return MessageFormat.format(propertyValue, args);
    }

    private List<JaggaerUploadSupplierVendorDetailDto> filterDetailDto(List<JaggaerUploadSupplierVendorDetailDto> dtos, boolean hasNotes) {
        return dtos.stream()
                .filter(dto -> dto.hasNotes() == hasNotes)
                .collect(Collectors.toList());
    }

    private void buildDetailSections(List<JaggaerUploadSupplierXmlFileDetailsDto> xmlFileDtos) {
        xmlFileDtos.stream().forEach(dto -> {
            reportWriterService.writeNewLines(4);
            reportWriterService.writeSubTitle(formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_HEADER, dto.getXmlFileName()));
            buildDetailLines(filterDetailDto(dto.getActiveVendors(), true));
            buildDetailLines(filterDetailDto(dto.getActiveVendors(), false));
            buildDetailLines(filterDetailDto(dto.getInactiveVendors(), true));
            buildDetailLines(filterDetailDto(dto.getInactiveVendors(), false));
            reportWriterService.writeSubTitle(formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_FOOTER));
        });
    }

    private void buildDetailLines(List<JaggaerUploadSupplierVendorDetailDto> details) {
        details.stream().forEach(detail -> {
            reportWriterService.writeFormattedMessageLine(formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_DETAIL_LINE,
                        detail.isActive() ? ACTIVE : INACTIVE, detail.getVendorName(), detail.getVendorNumber()));
            
            detail.getNotes().stream().forEach(note -> 
                reportWriterService.writeFormattedMessageLine(
                        formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_DETAIL_NOTE_LINE, note)));
        });
    }
    
    private void emailReport() {
        final String toAddress = findReportToAddress();
        final String fromAddress = toAddress;
        
        BodyMailMessage message = new BodyMailMessage();
        message.setFromAddress(fromAddress);
        String subject = reportWriterService.getTitle();
        message.setSubject(subject);
        message.getToAddresses().add(toAddress);
        String body = LoadFileUtils.safelyLoadFileString(reportWriterService.getReportFile().getAbsolutePath());
        message.setMessage(body);

        boolean htmlMessage = false;
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendEmail, from address: " + fromAddress + "  to address: " + toAddress);
            LOG.debug("sendEmail, the email subject: " + subject);
            LOG.debug("sendEmail, the email budy: " + body);
        }
        try {
            emailService.sendMessage(message, htmlMessage);
        } catch (Exception e) {
            LOG.error("sendEmail, the email could not be sent", e);
        }
    }
    
    protected String findReportToAddress() {
        /*
         * @todo pull this from a paremeter
         */
        return "Contract-support@cornell.edu";
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setReportWriterService(ReportWriterService reportWriterService) {
        this.reportWriterService = reportWriterService;
    }

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }
}
