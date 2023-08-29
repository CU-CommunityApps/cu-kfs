package edu.cornell.kfs.module.purap.batch.service.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.soap.MessageFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.service.EmailService;

import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.batch.service.JaggaerGenerateSupplierXmlReportService;
import edu.cornell.kfs.sys.service.ReportWriterService;

public class JaggaerGenerateSupplierXmlReportServiceImpl implements JaggaerGenerateSupplierXmlReportService {
    private static final Logger LOG = LogManager.getLogger();

    protected ConfigurationService configurationService;
    protected ParameterService parameterService;
    protected ReportWriterService reportWriterService;
    protected EmailService emailService;

    private static final String SUMMARY = "Summary";
    private static final String ACTIVE = "Active";
    private static final String INACTIVE = "Inactive";
    private static final String WITH = "with";
    private static final String WITHOUT = "without";

    public void generateAndEmailResultsReport(List<JaggaerUploadSupplierXmlFileDetailsDto> xmlFileDtos) {
        reportWriterService.initialize();
        buildSummarySection(xmlFileDtos);
        buildDetailSections(xmlFileDtos);
        reportWriterService.destroy();
    }

    private void buildSummarySection(List<JaggaerUploadSupplierXmlFileDetailsDto> xmlFileDtos) {
        reportWriterService
                .writeSubTitle(formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_HEADER, SUMMARY));
        int dtoCount = 0;
        for (JaggaerUploadSupplierXmlFileDetailsDto dto : xmlFileDtos) {
            if (dtoCount > 0) {
                reportWriterService.writeNewLines(2);
            }
            reportWriterService.writeSubTitle(formatPropertyValue(
                    CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_FILE_NAME, dto.getXmlFileName()));

            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_TOTAL, ACTIVE,
                            dto.getActiveVendors().size()));
            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL,
                            ACTIVE, WITH, filterDetailDto(dto.getActiveVendors(), true).size()));
            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL,
                            ACTIVE, WITHOUT, filterDetailDto(dto.getActiveVendors(), false).size()));

            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_TOTAL, INACTIVE,
                            dto.getInactiveVendors().size()));
            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL,
                            INACTIVE, WITH, filterDetailDto(dto.getInactiveVendors(), true).size()));
            reportWriterService.writeFormattedMessageLine(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SUMMARY_ACTIVE_INACTIVE_NOTE_TOTAL,
                            INACTIVE, WITHOUT, filterDetailDto(dto.getInactiveVendors(), false).size()));

            dtoCount++;
        }
        reportWriterService.writeSubTitle(formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_FOOTER));
    }

    private String formatPropertyValue(String propertyName, Object... args) {
        String propertyValue = configurationService.getPropertyValueAsString(propertyName);
        return MessageFormat.format(propertyValue, args);
    }

    private List<JaggaerUploadSupplierVendorDetailDto> filterDetailDto(List<JaggaerUploadSupplierVendorDetailDto> dtos,
            boolean hasNotes) {
        return dtos.stream().filter(dto -> dto.hasNotes() == hasNotes).collect(Collectors.toList());
    }

    private void buildDetailSections(List<JaggaerUploadSupplierXmlFileDetailsDto> xmlFileDtos) {
        for (JaggaerUploadSupplierXmlFileDetailsDto dto : xmlFileDtos) {
            reportWriterService.writeNewLines(4);
            reportWriterService.writeSubTitle(
                    formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_HEADER, dto.getXmlFileName()));
            buildDetailLines(filterDetailDto(dto.getActiveVendors(), true));
            buildDetailLines(filterDetailDto(dto.getActiveVendors(), false));
            buildDetailLines(filterDetailDto(dto.getInactiveVendors(), true));
            buildDetailLines(filterDetailDto(dto.getInactiveVendors(), false));
            reportWriterService
                    .writeSubTitle(formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_SECTION_FOOTER));
        }
    }

    private void buildDetailLines(List<JaggaerUploadSupplierVendorDetailDto> details) {
        for (JaggaerUploadSupplierVendorDetailDto detail : details) {
            reportWriterService.writeFormattedMessageLine(formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_DETAIL_LINE,
                    detail.isActive() ? ACTIVE : INACTIVE, detail.getVendorName(), detail.getVendorNumber()));
            if (detail.hasNotes()) {
                for (String note : detail.getNotes()) {
                    reportWriterService.writeFormattedMessageLine(
                            formatPropertyValue(CUPurapKeyConstants.JAGGAER_XML_REPORT_DETAIL_NOTE_LINE, note));
                }
            }
        }

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
