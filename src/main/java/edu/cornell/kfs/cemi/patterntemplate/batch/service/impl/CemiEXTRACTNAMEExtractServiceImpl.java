package  edu.cornell.kfs.cemi.patterntemplate.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.patterntemplate.batch.service.CemiEXTRACTNAMEExtractService;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseParameterConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants.FileExtensions;
import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiEXTRACTNAMEExtractServiceImpl implements CemiEXTRACTNAMEExtractService {
    
    private static final Logger LOG = LogManager.getLogger();

    private final Environment environment;
    
    private String awardScheduleFileCreationDirectory;
    private String awardScheduleFileOutboundDirectory;
    private CemiAwardScheduleOrmDao cemiAwardScheduleOrmDao;
    private CemiEXTRACTNAMEDao getCemiEXTRACTNAMEDao;
    private CemiOutputDefinitionFileType cemiOutputDefinitionFileType;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    private DateTimeService dateTimeService;
    
    public CemiEXTRACTNAMEExtractServiceImpl(final Environment environment) {
        this.environment = environment;
    }
    
    // This routine should clear and/or reset all values so that the batch job extraction runs cleanly.
    // One-to-many method service calls specific to each data extraction may need to be performed in
    // order to achive that result.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState(String dataExtractName) {
        LOG.info("resetState, Deleting the list keys representing extractable business objects for {} from the previous run (if present)...", dataExtractName);
        getCemiEXTRACTNAMEDao().clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable(String dataExtractName) {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of keys representing the extractable business objects for {}...", dataExtractName);
        getCemiEXTRACTNAMEDao().queryAndStoreInScopeBusinessObjectKeysForDataExtract(CemiEXTRACTNAMEConstants.CEMI_IN_SCOPE_BUSINESS_OBJECT_NAME);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateAwardScheduleExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateAwardScheduleExtractData, Generating data rows for Award Schedule spreadsheet "
                + "and placing in intermediate storage...");
        try {
            final CemiOutputDefinition outputDefinition = getOutputDefinitionForAwardScheduleExtract();
            generateAwardScheduleExtractData(outputDefinition, jobRunDate);
        } catch (final Exception e) {
            LOG.error("generateIntermediateAwardScheduleExtractData, Creation of Award Schedule Extract data failed", e);
            throw new RuntimeException(e);
        }
    }

    private void generateAwardScheduleExtractData(final CemiOutputDefinition outputDefinition,
            final LocalDateTime jobRunDate) throws IOException {
        try (
            // Replace this builder with a temp table implementation when ready.
            final CemiAwardScheduleDataBuilderCsvImpl dataBuilder = new CemiAwardScheduleDataBuilderCsvImpl(
                    getOutputDefinitionForAwardScheduleExtract(), getCemiAwardScheduleDao(), getDateTimeService(),
                    getBusinessObjectService(), jobRunDate, getAwardScheduleFileCreationDirectory(), shouldMaskCemiSensitiveData());
            final Stream<Award> awards = getCemiAwardScheduleOrmDao().getAwardsForCemiAwardScheduleExtractAsCloseableStream();
        ) {
            final Iterator<Award> awardsIterator = awards.iterator();
            dataBuilder.writeAwardScheduleDataToIntermediateStorage(awardsIterator, jobRunDate);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateAwardScheduleExtractFile(final LocalDateTime jobRunDate) {
        try {
            LOG.info("generateAwardScheduleExtractFile, Starting creation of CEMI Award Schedule Extract file...");
            final String newFileName = CemiUtils.generateFileNameContainingDateTime(
                    jobRunDate, CemiAwardScheduleConstants.AWARD_SCHEDULE_EXTRACT_FILENAME_PREFIX, FileExtensions.XLSX);
            final File tempFile = qualifyAndGetFilePath(awardScheduleFileCreationDirectory, newFileName);
            final File finalFile = qualifyAndGetFilePath(
                    awardScheduleFileOutboundDirectory, CemiAwardScheduleConstants.AWARD_SCHEDULE_EXTRACT_PLAIN_FILENAME);
            Validate.validState(!tempFile.exists(), "Temporary file already exists: %s", newFileName);

            createAndPopulateAwardScheduleExtractFile(tempFile, jobRunDate);

            if (shouldCopyAwardScheduleExtractFileToOutboundDirectory()) {
                LOG.info("generateAwardScheduleExtractFile, Copying file to outbound folder under the {} name...", CemiAwardScheduleConstants.AWARD_SCHEDULE_EXTRACT_PLAIN_FILENAME);
                copyAwardScheduleExtractFileToOutboundDirectory(tempFile, finalFile);
            } else {
                LOG.info("generateAwardScheduleExtractFile, Copying of the file to the outbound folder has been disabled. "
                        + "The copying operation will be skipped.");
            }

            LOG.info("generateAwardScheduleExtractFile, Success! Created the following extract file: {}", newFileName);
        } catch (final Exception e) {
            LOG.error("generateAwardScheduleExtractFile, Creation of Award Schedule Extract file failed", e);
            throw new RuntimeException(e);
        }
    }

    private File qualifyAndGetFilePath(final String prefix, final String fileName) {
        return new File(StringUtils.join(prefix, CUKFSConstants.SLASH, fileName));
    }
    
    private void createAndPopulateAwardScheduleExtractFile(final File file, final LocalDateTime jobRunDate)
            throws IOException, InvalidFormatException {
        final CemiOutputDefinition outputDefinition = getOutputDefinitionForAwardScheduleExtract();

        try (
            final InputStream templateFileStream = CuCoreUtilities.getResourceAsStream(
                    CemiAwardScheduleConstants.AWARD_SCHEDULE_TEMPLATE_FILE_PATH);
            final CemiExcelWriter writer = new CemiExcelWriter(outputDefinition, templateFileStream, file);
        ) {
            // Replace this appender with a temp table implementation when ready.
            final CemiAwardScheduleFileAppenderCsvImpl awardScheduleFileAppender = new CemiAwardScheduleFileAppenderCsvImpl(
                outputDefinition, jobRunDate, awardScheduleFileCreationDirectory);
            awardScheduleFileAppender.populateAwardScheduleFileFromIntermediateDataStorage(writer);
            writer.commit();
            awardScheduleFileAppender.cleanUpIntermediateStorage();
        }
    }

    private CemiOutputDefinition getOutputDefinitionForAwardScheduleExtract() throws IOException {
        try (
            final InputStream inputStream = CuCoreUtilities.getResourceAsStream(
                    CemiAwardScheduleConstants.AWARD_SCHEDULE_OUTPUT_DEFINITION_FILE_PATH);
        ) {
            final byte[] fileContents = IOUtils.toByteArray(inputStream);
            return cemiOutputDefinitionFileType.parse(fileContents);
        }
    }

    private boolean shouldCopyAwardScheduleExtractFileToOutboundDirectory() {
        return parameterService.getParameterValueAsBoolean(
                CreateCemiAwardScheduleExtractStep.class, CemiAwardScheduleParameterConstants.COPY_CEMI_AWARD_SCHEDULE_FILE_TO_OUTBOUND_FOLDER);
    }

    private boolean isCemiSensitiveDataSetToUnmask() {
        String maskingParameterValue =  parameterService.getParameterValueAsString(
                CreateCemiAwardScheduleExtractStep.class, CemiBaseParameterConstants.CEMI_SENSITIVE_DATA_MASKING_SETTING);
        return StringUtils.equalsIgnoreCase(maskingParameterValue, CemiBaseConstants.UNMASK);
    }

    private boolean isCemiEnvironment() {
        return StringUtils.equalsIgnoreCase(environment.getLane(), CemiBaseConstants.CEMI_ENVIRONMENT_LANE_NAME);
    }

    private boolean shouldUnmaskCemiSensitiveData() {
        return isCemiEnvironment() && isCemiSensitiveDataSetToUnmask();
    }

    private boolean shouldMaskCemiSensitiveData() {
        return !shouldUnmaskCemiSensitiveData();
    }
    
    private void copyAwardScheduleExtractFileToOutboundDirectory(final File sourceFile, final File targetFile) {
        try {
            final Path creationFilePath = sourceFile.toPath();
            final Path outboundFilePath = targetFile.toPath();
            Files.copy(creationFilePath, outboundFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.error("copyAwardExtractExtractFileToOutboundDirectory, Failed to copy file to outbound directory", e);
            throw new UncheckedIOException(e);
        }
    }

    public String getAwardScheduleFileCreationDirectory() {
        return awardScheduleFileCreationDirectory;
    }

    public void setAwardScheduleFileCreationDirectory(String awardScheduleFileCreationDirectory) {
        this.awardScheduleFileCreationDirectory = awardScheduleFileCreationDirectory;
    }

    public String getAwardScheduleFileOutboundDirectory() {
        return awardScheduleFileOutboundDirectory;
    }

    public void setAwardScheduleFileOutboundDirectory(String awardScheduleFileOutboundDirectory) {
        this.awardScheduleFileOutboundDirectory = awardScheduleFileOutboundDirectory;
    }

    public CemiAwardScheduleOrmDao getCemiAwardScheduleOrmDao() {
        return cemiAwardScheduleOrmDao;
    }

    public void setCemiAwardScheduleOrmDao(CemiAwardScheduleOrmDao cemiAwardScheduleOrmDao) {
        this.cemiAwardScheduleOrmDao = cemiAwardScheduleOrmDao;
    }

    public CemiAwardScheduleDao getCemiAwardScheduleDao() {
        return cemiAwardScheduleDao;
    }

    public void setCemiAwardScheduleDao(CemiAwardScheduleDao cemiAwardScheduleDao) {
        this.cemiAwardScheduleDao = cemiAwardScheduleDao;
    }

    public CemiOutputDefinitionFileType getCemiOutputDefinitionFileType() {
        return cemiOutputDefinitionFileType;
    }

    public void setCemiOutputDefinitionFileType(CemiOutputDefinitionFileType cemiOutputDefinitionFileType) {
        this.cemiOutputDefinitionFileType = cemiOutputDefinitionFileType;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
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
