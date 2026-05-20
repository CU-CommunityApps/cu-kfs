package edu.cornell.kfs.cemi.scm.remitto.batch.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.vnd.VendorConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import edu.cornell.kfs.cemi.scm.remitto.CemiRemitToSupplierConstants;
import edu.cornell.kfs.cemi.scm.remitto.CemiRemitToSupplierParameterConstants;
import edu.cornell.kfs.cemi.scm.remitto.batch.CreateCemiRemitToSupplierExtractStep;
import edu.cornell.kfs.cemi.scm.remitto.batch.dto.CemiRemitToSupplier;
import edu.cornell.kfs.cemi.scm.remitto.batch.service.CemiRemitToSupplierExtractService;
import edu.cornell.kfs.cemi.scm.remitto.dataaccess.CemiRemitToSupplierDao;
import edu.cornell.kfs.cemi.scm.remitto.dataaccess.CemiRemitToSupplierOrmDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants.FileExtensions;
import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.cemi.vnd.CemiVendorConstants;
import edu.cornell.kfs.cemi.vnd.CemiVendorParameterConstants;
import edu.cornell.kfs.cemi.vnd.batch.CreateCemiSupplierExtractStep;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierBo;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorDao;
import edu.cornell.kfs.cemi.vnd.dataaccess.CemiVendorOrmDao;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;

/**
 * Implementation of CemiRemitToSupplierExtractService.
 * 
 * Generates the Remit To Supplier Connection extract file by:
 * 1. Querying vendors with remit-to addresses
 * 2. Building connection DTOs
 * 3. Writing to Excel file based on template
 */
public class CemiRemitToSupplierExtractServiceImpl implements CemiRemitToSupplierExtractService {

    private static final Logger LOG = LogManager.getLogger();

    private final Environment environment;
    private String remitToSupplierFileCreationDirectory;
    private String remitToSupplierFileOutboundDirectory;
    private CemiRemitToSupplierOrmDao cemiRemitToSupplierOrmDao;
    private CemiRemitToSupplierDao cemiRemitToSupplierDao;
    private CemiOutputDefinitionFileType cemiOutputDefinitionFileType;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    private ParameterService parameterService;

    private DecimalFormat supplierIdFormatter;

    public CemiRemitToSupplierExtractServiceImpl(final Environment environment) {
        this.environment = environment;
        this.supplierIdFormatter = new DecimalFormat(CemiVendorConstants.SUPPLIER_ID_FORMAT);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.debug("resetState, Service state has been reset");
    }

    @Override
    public void initializeExtractDateRangeSettings() {
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateRemitToSupplierExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateRemitToSupplierExtractData, Generating data rows for Remit To Supplier spreadsheet "
                + "and placing in intermediate storage...");
        try {
            final CemiOutputDefinition outputDefinition = getOutputDefinitionForRemitToSupplierExtract();
            generateRemitToSupplierExtractData(outputDefinition, jobRunDate);
        } catch (final Exception e) {
            LOG.error("generateIntermediateRemitToSupplierExtractData, Creation of Remit To Supplier Extract data failed", e);
            throw new RuntimeException(e);
        }
    }
    
    private void generateRemitToSupplierExtractData(final CemiOutputDefinition outputDefinition,
            final LocalDateTime jobRunDate) throws IOException {
        try (
            // Replace this builder with a temp table implementation when ready.
            final CemiRemitToSupplierDataBuilderCsvImpl dataBuilder = new CemiRemitToSupplierDataBuilderCsvImpl(
                    getOutputDefinitionForRemitToSupplierExtract(), getCemiRemitToSupplierDao(), getDateTimeService(),
                    getBusinessObjectService(), jobRunDate, getRemitToSupplierFileCreationDirectory(), shouldMaskCemiSensitiveData());

            final Stream<CemiSupplierBo> suppliers = getCemiRemitToSupplierOrmDao().getCemiSuppliersExtractAsCloseableStream();
        ) {
            final Iterator<CemiSupplierBo> suppliersIterator = suppliers.iterator();
            dataBuilder.writeRemitToSupplierDataToIntermediateStorage(suppliersIterator, jobRunDate);
        }
    }
    
    private CemiOutputDefinition getOutputDefinitionForRemitToSupplierExtract() throws IOException {
        try (
            final InputStream inputStream = CuCoreUtilities.getResourceAsStream(
                    CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_OUTPUT_DEFINITION_FILE_PATH);
        ) {
            final byte[] fileContents = IOUtils.toByteArray(inputStream);
            return cemiOutputDefinitionFileType.parse(fileContents);
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
            final File finalFile = qualifyAndGetFilePath(
                    remitToSupplierFileOutboundDirectory, CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_EXTRACT_PLAIN_FILENAME);
            Validate.validState(!tempFile.exists(), "Temporary file already exists: %s", newFileName);

            createAndPopulateRemitToSupplierExtractFile(tempFile, jobRunDate);

            if (shouldCopyRemitToSpplierExtractFileToOutboundDirectory()) {
                LOG.info("generateRemitToSupplierExtractFile, Copying file to outbound folder under the {} name...", CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_EXTRACT_PLAIN_FILENAME);
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
        final CemiOutputDefinition outputDefinition = getOutputDefinitionForRemitToSupplierExtract();

        try (
            final InputStream templateFileStream = CuCoreUtilities.getResourceAsStream(
                    CemiRemitToSupplierConstants.REMIT_TO_SUPPLIER_TEMPLATE_FILE_PATH);
            final CemiExcelWriter writer = new CemiExcelWriter(outputDefinition, templateFileStream, file);
        ) {
            // Replace this appender with a temp table implementation when ready.
            final CemiRemitToSupplierFileAppenderCsvImpl remitToSupplierFileAppender = new CemiRemitToSupplierFileAppenderCsvImpl(
                outputDefinition, jobRunDate, remitToSupplierFileCreationDirectory);
            remitToSupplierFileAppender.populateRemitToSupplierFileFromIntermediateDataStorage(writer);
            writer.commit();
            remitToSupplierFileAppender.cleanUpIntermediateStorage();
        }
    }
    
    private void copyRemitToSupplierExtractFileToOutboundDirectory(final File sourceFile, final File targetFile) {
        try {
            final Path creationFilePath = sourceFile.toPath();
            final Path outboundFilePath = targetFile.toPath();
            Files.copy(creationFilePath, outboundFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.error("copyRemitToSupplierExtractFileToOutboundDirectory, Failed to copy file to outbound directory", e);
            throw new UncheckedIOException(e);
        }
    }
    
    private boolean shouldCopyRemitToSpplierExtractFileToOutboundDirectory() {
        return parameterService.getParameterValueAsBoolean(
                CreateCemiRemitToSupplierExtractStep.class, CemiRemitToSupplierParameterConstants.COPY_CEMI_REMIT_TO_SUPPLIER_FILE_TO_OUTBOUND_FOLDER);
    }
    
    
    private boolean isCemiSensitiveDataSetToUnmask() {
        String maskingParameterValue =  parameterService.getParameterValueAsString(
                CreateCemiSupplierExtractStep.class, CemiVendorParameterConstants.CEMI_SENSITIVE_DATA_MASKING_SETTING);
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

    private void copyToOutboundDirectory(final Path sourcePath, final String fileName) {
        try {
            final Path outboundPath = Paths.get(remitToSupplierFileOutboundDirectory, fileName);
            Files.copy(sourcePath, outboundPath);
            LOG.info("copyToOutboundDirectory, Copied file to outbound directory: {}", outboundPath);
        } catch (final IOException e) {
            LOG.error("copyToOutboundDirectory, Error copying file to outbound directory", e);
        }
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

    public CemiRemitToSupplierDao getCemiRemitToSupplierDao() {
        return cemiRemitToSupplierDao;
    }

    public void setCemiRemitToSupplierDao(CemiRemitToSupplierDao cemiRemitToSupplierDao) {
        this.cemiRemitToSupplierDao = cemiRemitToSupplierDao;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public String getRemitToSupplierFileCreationDirectory() {
        return remitToSupplierFileCreationDirectory;
    }

    public String getRemitToSupplierFileOutboundDirectory() {
        return remitToSupplierFileOutboundDirectory;
    }

    public CemiRemitToSupplierOrmDao getCemiRemitToSupplierOrmDao() {
        return cemiRemitToSupplierOrmDao;
    }

    public void setCemiRemitToSupplierOrmDao(CemiRemitToSupplierOrmDao cemiRemitToSupplierOrmDao) {
        this.cemiRemitToSupplierOrmDao = cemiRemitToSupplierOrmDao;
    }

}