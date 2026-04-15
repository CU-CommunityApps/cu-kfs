package edu.cornell.kfs.cemi.vnd.batch.service.impl;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants.FileExtensions;
import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.CemiFileAppenderService;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiRemitToSupplierConstants;
import edu.cornell.kfs.cemi.vnd.CemiRemitToSupplierParameterConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiRemitToSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierAddressBo;
import edu.cornell.kfs.cemi.vnd.batch.service.CemiRemitToSupplierExtractService;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiRemitToSupplierDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiRemitToSupplierOrmDao;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiRemitToSupplierExtractServiceImpl implements CemiRemitToSupplierExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private final Environment environment;
    private String remitToSupplierFileCreationDirectory;
    private String remitToSupplierFileOutboundDirectory;
    private CemiRemitToSupplierOrmDao cemiRemitToSupplierOrmDao;
    private CemiRemitToSupplierDao cemiRemitToSupplierDao;
    private CemiFileAppenderService cemiFileAppenderService;
    private CemiOutputDefinitionFileType cemiOutputDefinitionFileType;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;

    public CemiRemitToSupplierExtractServiceImpl(final Environment environment) {
        this.environment = environment;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of extractable addresses from the previous run (if present)...");
        cemiRemitToSupplierDao.clearExistingListOfExtractableRemitAddressIds();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void initializeExtractDateSettings() {
        LOG.info("initializeExtractDateSettings, Setting Supplier extraction date-time to use for address queries...");
        final String supplierJobRunDate = getSupplierJobRunDate();
        cemiRemitToSupplierDao.updateRemitToSupplierExtractQuerySettings(supplierJobRunDate);
    }

    private String getSupplierJobRunDate() {
        final String supplierJobRunDate = parameterService.getParameterValueAsString(
                CreateCemiRemitToSupplierExtractStep.class,
                CemiRemitToSupplierParameterConstants.CEMI_REMIT_TO_SUPPLIER_EXTRACT_SUPPLIER_DATETIME);
        Validate.validState(StringUtils.isNotBlank(supplierJobRunDate), "Parameter %s should not have been blank",
                CemiRemitToSupplierParameterConstants.CEMI_REMIT_TO_SUPPLIER_EXTRACT_SUPPLIER_DATETIME);
        return supplierJobRunDate;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfInScopeAddresses() {
        LOG.info("populateListOfInScopeAddresses, Querying and storing the list of extractable addresses...");
        cemiRemitToSupplierDao.queryAndStoreAddressIdsForRemitToSupplierExtract();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateRemitToSupplierExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateRemitToSupplierExtractData, Generating data rows for Remit To Supplier spreadsheet "
                + "and placing in intermediate storage...");
        try {
            generateRemitToSupplierExtractData(jobRunDate);
        } catch (final Exception e) {
            LOG.error("generateIntermediateRemitToSupplierExtractData, Creation of Remit To Supplier Extract data failed", e);
            throw new RuntimeException(e);
        }
    }

    private void generateRemitToSupplierExtractData(final LocalDateTime jobRunDate) throws IOException {
        try (
            final Stream<CemiSupplierAddressBo> addresses = cemiRemitToSupplierOrmDao
                    .getAddressesForCemiRemitToSupplierExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final String supplierJobRunDate = getSupplierJobRunDate();
            final CemiRemitToSupplierDataBuilderDefaultImpl dataBuilder = new CemiRemitToSupplierDataBuilderDefaultImpl(
                    businessObjectService, jobRunDateString, cemiRemitToSupplierOrmDao, supplierJobRunDate,
                    shouldMaskCemiSensitiveData());
            final Iterator<CemiSupplierAddressBo> addressesIterator = addresses.iterator();
            dataBuilder.writeRemitToSupplierDataToIntermediateStorage(addressesIterator);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateRemitToSupplierExtractFile(final LocalDateTime jobRunDate) {
        try {
            LOG.info("generateRemitToSupplierExtractFile, Starting creation of CEMI RemitToSupplier Extract file...");
            final String newFileName = CemiUtils.generateFileNameContainingDateTime(
                    jobRunDate, CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_EXTRACT_FILENAME_PREFIX, FileExtensions.XLSX);
            final File tempFile = qualifyAndGetFilePath(remitToSupplierFileCreationDirectory, newFileName);
            final File finalFile = qualifyAndGetFilePath(remitToSupplierFileOutboundDirectory,
                    CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_EXTRACT_PLAIN_FILENAME);
            Validate.validState(!tempFile.exists(), "Temporary file already exists: %s", newFileName);

            createAndPopulateRemitToSupplierExtractFile(tempFile, jobRunDate);

            if (shouldCopyRemitToSupplierExtractFileToOutboundDirectory()) {
                LOG.info("generateRemitToSupplierExtractFile, Copying file to outbound folder under the {} name...",
                        CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_EXTRACT_PLAIN_FILENAME);
                copyRemitToSupplierExtractFileToOutboundDirectory(tempFile, finalFile);
            } else {
                LOG.info("generateRemitToSupplierExtractFile, Copying of the file to the outbound folder has been disabled. "
                        + "The copying operation will be skipped.");
            }

            LOG.info("generateRemitToSupplierExtractFile, Success! Created the following extract file: {}", newFileName);
        } catch (final Exception e) {
            LOG.error("generateRemitToSupplierExtractFile, Creation of Remit To Supplier Extract file failed", e);
            throw new RuntimeException(e);
        }
    }

    private File qualifyAndGetFilePath(final String prefix, final String fileName) {
        return new File(StringUtils.join(prefix, CUKFSConstants.SLASH, fileName));
    }

    private void createAndPopulateRemitToSupplierExtractFile(final File file, final LocalDateTime jobRunDate)
            throws IOException, InvalidFormatException {
        final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
        final CemiOutputDefinition outputDefinition = getOutputDefinitionForRemitToSupplierExtract();

        try (
            final InputStream templateFileStream = CuCoreUtilities.getResourceAsStream(
                    CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_TEMPLATE_FILE_PATH);
            final CemiExcelWriter writer = new CemiExcelWriter(outputDefinition, templateFileStream, file);
        ) {
            cemiFileAppenderService.populateFileFromOrmDataStorage(writer, outputDefinition, jobRunDateString);
            writer.commit();
        }
    }

    private CemiOutputDefinition getOutputDefinitionForRemitToSupplierExtract() throws IOException {
        return CemiUtils.getOutputDefinitionFromCemiResourcesFile(cemiOutputDefinitionFileType,
                CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_OUTPUT_DEFINITION_PATH_SUFFIX);
    }

    private void copyRemitToSupplierExtractFileToOutboundDirectory(final File sourceFile, final File targetFile) {
        try {
            final Path creationFilePath = sourceFile.toPath();
            final Path outboundFilePath = targetFile.toPath();
            Files.copy(creationFilePath, outboundFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            LOG.error("copyRemitToSupplierExtractFileToOutboundDirectory, Failed to copy file to outbound directory", e);
            throw new UncheckedIOException(e);
        }
    }

    private boolean shouldCopyRemitToSupplierExtractFileToOutboundDirectory() {
        return parameterService.getParameterValueAsBoolean(
                CreateCemiRemitToSupplierExtractStep.class,
                CemiRemitToSupplierParameterConstants.COPY_CEMI_REMIT_TO_SUPPLIER_FILE_TO_OUTBOUND_FOLDER);
    }

    private boolean isCemiSensitiveDataSetToUnmask() {
        String maskingParameterValue =  parameterService.getParameterValueAsString(
                CreateCemiRemitToSupplierExtractStep.class,
                CemiVendorParameterConstants.CEMI_SENSITIVE_DATA_MASKING_SETTING);
        return Strings.CI.equals(maskingParameterValue, CemiBaseConstants.UNMASK);
    }

    private boolean isCemiEnvironment() {
        return Strings.CI.equals(environment.getLane(), CemiBaseConstants.CEMI_ENVIRONMENT_LANE_NAME);
    }

    private boolean shouldUnmaskCemiSensitiveData() {
        return isCemiEnvironment() && isCemiSensitiveDataSetToUnmask();
    }

    private boolean shouldMaskCemiSensitiveData() {
        return !shouldUnmaskCemiSensitiveData();
    }

    public void setRemitToSupplierFileCreationDirectory(final String remitToSupplierFileCreationDirectory) {
        this.remitToSupplierFileCreationDirectory = remitToSupplierFileCreationDirectory;
    }

    public void setRemitToSupplierFileOutboundDirectory(final String remitToSupplierFileOutboundDirectory) {
        this.remitToSupplierFileOutboundDirectory = remitToSupplierFileOutboundDirectory;
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setCemiRemitToSupplierOrmDao(final CemiRemitToSupplierOrmDao cemiRemitToSupplierOrmDao) {
        this.cemiRemitToSupplierOrmDao = cemiRemitToSupplierOrmDao;
    }

    public void setCemiRemitToSupplierDao(final CemiRemitToSupplierDao cemiRemitToSupplierDao) {
        this.cemiRemitToSupplierDao = cemiRemitToSupplierDao;
    }

    public void setCemiOutputDefinitionFileType(final CemiOutputDefinitionFileType cemiOutputDefinitionFileType) {
        this.cemiOutputDefinitionFileType = cemiOutputDefinitionFileType;
    }

    public void setCemiFileAppenderService(final CemiFileAppenderService cemiFileAppenderService) {
        this.cemiFileAppenderService = cemiFileAppenderService;
    }

}
