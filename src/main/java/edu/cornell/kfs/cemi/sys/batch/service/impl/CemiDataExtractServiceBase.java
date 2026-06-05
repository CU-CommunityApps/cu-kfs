package edu.cornell.kfs.cemi.sys.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants.FileExtensions;
import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.CemiFileAppenderService;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiRemitToSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorParameterConstants;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;

public abstract class CemiDataExtractServiceBase {

    private static final Logger LOG = LogManager.getLogger();

    protected final Environment environment;

    protected String dataFileCreationDirectory;
    protected String dataFileOutputDirectory;
    protected CemiFileAppenderService cemiFileAppenderService;
    protected CemiOutputDefinitionFileType cemiOutputDefinitionFileType;
    protected ParameterService parameterService;

    protected CemiDataExtractServiceBase(final Environment environment) {
        Validate.notNull(environment, "environment cannot be null");
        this.environment = environment;
    }

    protected boolean isCemiEnvironment() {
        return Strings.CI.equals(environment.getLane(), CemiBaseConstants.CEMI_ENVIRONMENT_LANE_NAME);
    }

    protected boolean shouldUnmaskCemiSensitiveData() {
        return isCemiEnvironment() && isCemiSensitiveDataSetToUnmask();
    }

    protected boolean shouldMaskCemiSensitiveData() {
        return !shouldUnmaskCemiSensitiveData();
    }

    protected boolean isCemiSensitiveDataSetToUnmask() {
        String maskingParameterValue = parameterService.getParameterValueAsString(
                getComponentClassForDataMaskingParameter(),
                CemiVendorParameterConstants.CEMI_SENSITIVE_DATA_MASKING_SETTING);
        return Strings.CI.equals(maskingParameterValue, CemiBaseConstants.UNMASK);
    }

    protected void generateFileForDataExtract(final LocalDateTime jobRunDate, final String plainFileName,
            final String datedFileNamePrefix) {
        try {
            final String newFileName = CemiUtils.generateFileNameContainingDateTime(
                    jobRunDate, datedFileNamePrefix, FileExtensions.XLSX);
            final File tempFile = qualifyAndGetFilePath(dataFileCreationDirectory, newFileName);
            final File finalFile = qualifyAndGetFilePath(dataFileOutputDirectory, plainFileName);
            Validate.validState(!tempFile.exists(), "Temporary file already exists: %s", newFileName);

            createAndPopulateDataExtractFile(tempFile, jobRunDate);

            if (shouldCopyDataFileToOutboundDirectory()) {
                LOG.info("generateFileForDataExtract, Copying file to outbound folder under the {} name...",
                        plainFileName);
                copyDataExtractFileToOutboundDirectory(tempFile, finalFile);
            } else {
                LOG.info("generateFileForDataExtract, Copying of the file to the outbound folder has been disabled. "
                        + "The copying operation will be skipped.");
            }

            LOG.info("generateFileForDataExtract, Success! Created the following extract file: {}", newFileName);
        } catch (final Exception e) {
            LOG.error("generateFileForDataExtract, Creation of data file failed", e);
            throw new RuntimeException(e);
        }
    }

    protected File qualifyAndGetFilePath(final String prefix, final String fileName) {
        return new File(StringUtils.join(prefix, CUKFSConstants.SLASH, fileName));
    }

    protected void createAndPopulateDataExtractFile(final File file, final LocalDateTime jobRunDate)
            throws IOException, InvalidFormatException {
        final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
        final CemiOutputDefinition outputDefinition = CemiUtils.getOutputDefinitionFromCemiResourcesFile(
                cemiOutputDefinitionFileType, getOutputDefinitionFilePathSuffix());

        try (
            final InputStream templateFileStream = CuCoreUtilities.getResourceAsStream(
                    CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_TEMPLATE_FILE_PATH);
            final CemiExcelWriter writer = new CemiExcelWriter(outputDefinition, templateFileStream, file);
        ) {
            cemiFileAppenderService.populateFileFromOrmDataStorage(writer, outputDefinition, jobRunDateString);
            writer.commit();
        }
    }

    protected void copyDataExtractFileToOutboundDirectory(final File sourceFile, final File targetFile) {
        try {
            final Path creationFilePath = sourceFile.toPath();
            final Path outboundFilePath = targetFile.toPath();
            Files.copy(creationFilePath, outboundFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            LOG.error("copyDataExtractFileToOutboundDirectory, Failed to copy file to outbound directory", e);
            throw new UncheckedIOException(e);
        }
    }

    protected abstract Class<?> getComponentClassForDataMaskingParameter();

    protected abstract String getOutputDefinitionFilePathSuffix();

    protected abstract boolean shouldCopyDataFileToOutboundDirectory();

    public void setDataFileCreationDirectory(final String dataFileCreationDirectory) {
        this.dataFileCreationDirectory = dataFileCreationDirectory;
    }

    public void setDataFileOutputDirectory(final String dataFileOutputDirectory) {
        this.dataFileOutputDirectory = dataFileOutputDirectory;
    }

    public void setCemiFileAppenderService(final CemiFileAppenderService cemiFileAppenderService) {
        this.cemiFileAppenderService = cemiFileAppenderService;
    }

    public void setCemiOutputDefinitionFileType(final CemiOutputDefinitionFileType cemiOutputDefinitionFileType) {
        this.cemiOutputDefinitionFileType = cemiOutputDefinitionFileType;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

}
