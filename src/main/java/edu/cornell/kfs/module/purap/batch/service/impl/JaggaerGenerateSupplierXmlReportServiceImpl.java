package edu.cornell.kfs.module.purap.batch.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.mail.BodyMailMessage;
import org.kuali.kfs.sys.service.EmailService;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.batch.JaggaerGenerateSupplierXmlStep;
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
        writeSubTitle(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_HEADER, SUMMARY);
        int dtoCount = 0;
        for (JaggaerUploadSupplierXmlFileDetailsDto dto : xmlFileDtos) {
            if (dtoCount > 0) {
                reportWriterService.writeNewLines(2);
            }
            writeFormattedMessageLine(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_FILE_NAME, dto.getXmlFileName());

            writeFormattedMessageLine(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_TOTAL, ACTIVE,
                    dto.getActiveVendors().size());
            writeFormattedMessageLine(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL, ACTIVE,
                    WITH, filterDetailDtoWithNotes(dto.getActiveVendors()).size());
            writeFormattedMessageLine(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL, ACTIVE,
                    WITHOUT, filterDetailDtoWithoutNotes(dto.getActiveVendors()).size());

            writeFormattedMessageLine(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_TOTAL, INACTIVE,
                    dto.getInactiveVendors().size());
            writeFormattedMessageLine(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL,
                    INACTIVE, WITH, filterDetailDtoWithNotes(dto.getInactiveVendors()).size());
            writeFormattedMessageLine(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL,
                    INACTIVE, WITHOUT, filterDetailDtoWithoutNotes(dto.getInactiveVendors()).size());

            dtoCount++;
        }
        writeSubTitle(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_FOOTER);
    }
    
   
    private void writeSubTitle(String propertyName, Object... args) {
        String propertyValue = getPropertyValue(propertyName);
        reportWriterService.writeSubTitle(String.format(propertyValue, args));
    }

    private String getPropertyValue(String propertyName) {
        String propertyValue = configurationService.getPropertyValueAsString(propertyName);
        return propertyValue;
    }
    
    private void writeFormattedMessageLine(String propertyName, Object... args) {
        String propertyValue = getPropertyValue(propertyName);
        reportWriterService.writeFormattedMessageLine(propertyValue, args);
    }
    
    private List<JaggaerUploadSupplierVendorDetailDto> filterDetailDtoWithNotes(List<JaggaerUploadSupplierVendorDetailDto> dtos) {
        return filterDetailDto(dtos, true);
    }
    
    private List<JaggaerUploadSupplierVendorDetailDto> filterDetailDtoWithoutNotes(List<JaggaerUploadSupplierVendorDetailDto> dtos) {
        return filterDetailDto(dtos, false);
    }

    private List<JaggaerUploadSupplierVendorDetailDto> filterDetailDto(List<JaggaerUploadSupplierVendorDetailDto> dtos, boolean hasNotes) {
        return dtos.stream()
                .filter(dto -> dto.hasNotes() == hasNotes)
                .collect(Collectors.toUnmodifiableList());
    }

    private void buildDetailSections(List<JaggaerUploadSupplierXmlFileDetailsDto> xmlFileDtos) {
        xmlFileDtos.stream().forEach(dto -> {
            reportWriterService.writeNewLines(4);
            writeSubTitle(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_HEADER, dto.getXmlFileName());
            buildDetailLines(filterDetailDtoWithNotes(dto.getActiveVendors()));
            buildDetailLines(filterDetailDtoWithoutNotes(dto.getActiveVendors()));
            buildDetailLines(filterDetailDtoWithNotes(dto.getInactiveVendors()));
            buildDetailLines(filterDetailDtoWithoutNotes(dto.getInactiveVendors()));
            writeSubTitle(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_FOOTER);
        });
    }

    private void buildDetailLines(List<JaggaerUploadSupplierVendorDetailDto> details) {
        details.stream().forEach(detail -> {
            writeFormattedMessageLine(CUPurapKeyConstants.JAGGAER_XML_REPORT_DETAIL_LINE,
                    detail.isActive() ? ACTIVE : INACTIVE, detail.getVendorName(), detail.getVendorNumber());

            detail.getNotes().stream().forEach(
                    note -> writeFormattedMessageLine(CUPurapKeyConstants.JAGGAER_XML_REPORT_DETAIL_NOTE_LINE, note));
        });
    }
    
    private void emailReport() {
        final String toAddress = findReportToAddress();
        final String fromAddress = emailService.getDefaultFromAddress();
        final String body = LoadFileUtils.safelyLoadFileString(reportWriterService.getReportFile().getAbsolutePath());
        
        if (StringUtils.isNotBlank(toAddress)) {        
            BodyMailMessage message = new BodyMailMessage();
            message.setFromAddress(fromAddress);
            String subject = reportWriterService.getTitle();
            message.setSubject(subject);
            message.getToAddresses().add(toAddress);
            message.setMessage(body);
    
            boolean htmlMessage = false;
            LOG.debug("emailReport, from address: {}  to address: {}", fromAddress, toAddress);
            LOG.debug("emailReport, the email subject: {}",  subject);
            LOG.debug("emailReport, the email body: {}",  body);
                
            try {
                emailService.sendMessage(message, htmlMessage);
            } catch (Exception e) {
                LOG.error("emailReport, the email could not be sent", e);
            }
        } else {
            LOG.warn("emailReport, the JAGGAER_XML_REPORT_EMAIL parameter is empty, so not emailing the report");
        }
    }
    
    protected String findReportToAddress() {
        return parameterService.getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class, CUPurapParameterConstants.JAGGAER_XML_REPORT_EMAIL);
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
