package edu.cornell.kfs.module.purap.batch;

import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.batch.service.impl.JaggaerUploadSupplierXmlFileDetailsDto;
import edu.cornell.kfs.sys.CUKFSConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.module.purap.PurapConstants;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.sys.batch.XmlBatchInputFileTypeBase;

import java.io.File;
import java.util.Locale;

public class JaggaerXMLInputFileType extends XmlBatchInputFileTypeBase<JaggaerUploadSupplierXmlFileDetailsDto> {

    protected static final DateTimeFormatter DATE_FORMATTER_FOR_FILE_NAME = DateTimeFormat
            .forPattern(CUKFSConstants.DATE_FORMAT_yyyyMMdd_HHmmssSSS).withLocale(Locale.US);

    protected String reportPath;
    protected String reportPrefix;
    protected String reportExtension;
//    protected String jaggaerXmlDirectory;   replace with base class property
    protected ParameterService parameterService;
    protected ConfigurationService configurationService;
    protected String fileNamePrefix;

    /**
     * Returns the identifier of the electronic invoice file type
     *
     * @return the electronic invoice file type identifier
     */
    @Override
    public String getFileTypeIdentifier() {
        return PurapConstants.ELECTRONIC_INVOICE_FILE_TYPE_INDENTIFIER;
    }

    @Override
    public boolean validate(final Object parsedFileContents) {
        return true;
    }

    @Override
    public String getTitleKey() {
        return PurapKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_EINVOICE;
    }

    @Override
    public String getAuthorPrincipalName(final File file) {
        return null;
    }

    @Override
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        return "";
//        String outputFileName = jaggaerXmlDirectory + findOutputFileNameStarter()
//                + DATE_FORMATTER_FOR_FILE_NAME.print(dateTimeService.getCurrentDate().getTime())
//                + CUKFSConstants.XML_FILE_EXTENSION;
//
//        final String fileName = ((ElectronicInvoice) parsedFileContents).getFileName();
//        if (fileName == null) {
//            return fileUserIdentifier;
//        }
//        final int whereDot = fileName.lastIndexOf('.');
//
//        return fileName.substring(0, whereDot);
    }

    protected String findOutputFileNameStarter() {
        return getParameterValueString(CUPurapParameterConstants.JAGGAER_DEFAULT_SUPPLIER_OUTPUT_FILE_NAME_STARTER);
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(final String reportPath) {
        this.reportPath = reportPath;
    }

    public void setReportExtension(final String reportExtension) {
        this.reportExtension = reportExtension;
    }


    public String getReportExtension() {
        return reportExtension;
    }

    public String getReportPrefix() {
        return reportPrefix;
    }

    public void setReportPrefix(final String reportPrefix) {
        this.reportPrefix = reportPrefix;
    }

    protected String getParameterValueString(String parameterName) {
        return parameterService.getParameterValueAsString(JaggaerGenerateSupplierXmlStep.class, parameterName);
    }

    public ConfigurationService getConfigurationService() {
        return configurationService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
