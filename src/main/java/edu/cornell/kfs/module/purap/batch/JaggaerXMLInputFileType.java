package edu.cornell.kfs.module.purap.batch;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.CUPurapKeyConstants;
import edu.cornell.kfs.module.purap.CUPurapParameterConstants;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.module.purap.PurapKeyConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.XmlBatchInputFileTypeBase;

import java.io.File;

import static org.kuali.kfs.pdp.PdpConstants.FILE_NAME_PART_DELIMITER;

public class JaggaerXMLInputFileType extends XmlBatchInputFileTypeBase<SupplierSyncMessage> {

    protected String fileNamePrefix;
    protected String reportPath;
    protected String reportPrefix;
    protected String reportExtension;
    protected ParameterService parameterService;
    protected ConfigurationService configurationService;
    protected DateTimeService dateTimeService;

    @Override
    public String getFileTypeIdentifier() {
        return CUPurapConstants.JAGGAER_XML_INPUT_FILE_TYPE_IDENTIFIER;
    }

    @Override
    public boolean validate(final Object parsedFileContents) {
        return true;
    }

    @Override
    public String getTitleKey() {
        return CUPurapKeyConstants.MESSAGE_BATCH_UPLOAD_TITLE_JAGGAER;
    }

    @Override
    public String getAuthorPrincipalName(final File file) {
        String filename = file.getName();
        if (StringUtils.isBlank(filename) || !filename.contains(FILE_NAME_PART_DELIMITER) || !filename.contains(fileNamePrefix)) {
            return StringUtils.EMPTY;
        }

        String authorPrincipalName = filename.substring(filename.indexOf(fileNamePrefix + fileNamePrefix.length() + 1));
        authorPrincipalName = filename.substring(0, authorPrincipalName.indexOf(FILE_NAME_PART_DELIMITER));
        return authorPrincipalName;
    }

    @Override
    public String getFileName(String principalName, Object parsedFileContents, String fileUserIdentifier) {
        String fileName = new StringBuilder()
                .append(fileNamePrefix)
                .append(principalName)
                .append(StringUtils.isBlank(fileUserIdentifier) ? StringUtils.EMPTY : FILE_NAME_PART_DELIMITER + fileUserIdentifier)
                .append(FILE_NAME_PART_DELIMITER)
                .append(dateTimeService.toDateTimeStringForFilename(dateTimeService.getCurrentDate()))
                .toString();

        return StringUtils.remove(fileName, KFSConstants.BLANK_SPACE);
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

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
