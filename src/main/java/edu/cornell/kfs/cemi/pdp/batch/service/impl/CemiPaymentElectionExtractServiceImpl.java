package  edu.cornell.kfs.cemi.pdp.batch.service.impl;

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
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchBankService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.pdp.CemiPaymentElectionConstants;
import edu.cornell.kfs.cemi.pdp.CemiPaymentElectionParameterConstants;
import edu.cornell.kfs.cemi.pdp.batch.CreateCemiPaymentElectionExtractStep;
import edu.cornell.kfs.cemi.pdp.batch.service.CemiPaymentElectionExtractService;
import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionDao;
import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionOrmDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants.FileExtensions;
import edu.cornell.kfs.cemi.sys.CemiBaseParameterConstants;
import edu.cornell.kfs.cemi.sys.batch.CemiCsvBatchInputFileType;
import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.CemiCsvDataImportService;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiExcelWriter;
import edu.cornell.kfs.cemi.sys.batch.xml.CemiOutputDefinition;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;
import edu.cornell.kfs.core.api.util.CuCoreUtilities;
import edu.cornell.kfs.sys.CUKFSConstants;

public class CemiPaymentElectionExtractServiceImpl implements CemiPaymentElectionExtractService {
    
    private static final Logger LOG = LogManager.getLogger();

    private Environment environment;
    
    private String paymentElectionFileCreationDirectory;
    private String paymentElectionFileOutboundDirectory;
    private CemiPaymentElectionOrmDao cemiPaymentElectionOrmDao;
    private CemiPaymentElectionDao cemiPaymentElectionDao;
    private CemiOutputDefinitionFileType cemiOutputDefinitionFileType;
    private AchBankService achBankService;
    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    private DateTimeService dateTimeService;
    private CemiCsvBatchInputFileType externalEmployeeIdDataFileType;
    private CemiCsvDataImportService cemiCsvDataImportService;
    
    public CemiPaymentElectionExtractServiceImpl(final Environment environment) {
        this.environment = environment;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of extractable PayeeACHAccount KFS generated identifiers for"
                + "Payment Election from the previous run (if present)...");
        getCemiPaymentElectionDao().clearExistingListOfExtractablePayeeAchAccountGeneratedIds();
        if (shouldImportExternalStagingData()) {
            LOG.info("resetState, Also deleting the list of in-scope employee IDs (if present)...");
            cemiCsvDataImportService.truncateDestinationTable(externalEmployeeIdDataFileType);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfInScopeEmployeeIdsIfNecessary() {
        if (shouldImportExternalStagingData()) {
            LOG.info("populateListOfInScopeEmployeeIdsIfNecessary, Loading in-scope employee IDs from staged file...");
            cemiCsvDataImportService.importCsvData(externalEmployeeIdDataFileType);
        } else {
            LOG.info("populateListOfInScopeEmployeeIdsIfNecessary, Skipping updates to list of in-scope employee IDs");
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void populateListOfInScopeEmployeePaymentElections() {
        LOG.info("populateListOfInScopeEmployeePaymentElections, Querying and storing the list of extractable "
                + "PayeeACHAcount generated identifiers for Payment Election...");
        getCemiPaymentElectionDao().queryAndStorePayeeAchAccountGeneratedIdsForPaymentElectionExtract();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediatePaymentElectionExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediatePaymentElectionExtractData, Generating data rows for Payment Election spreadsheet "
                + "and placing in intermediate storage...");
        try {
            final CemiOutputDefinition outputDefinition = getOutputDefinitionForPaymentElectionExtract();
            generatePaymentElectionExtractData(outputDefinition, jobRunDate);
        } catch (final Exception e) {
            LOG.error("generateIntermediatePaymentElectionExtractData, Creation of Payment Election Extract data failed", e);
            throw new RuntimeException(e);
        }
    }

    private void generatePaymentElectionExtractData(final CemiOutputDefinition outputDefinition,
            final LocalDateTime jobRunDate) throws IOException {
        try (
            // Replace this builder with a temp table implementation when ready.
            final CemiPaymentElectionDataBuilderCsvImpl dataBuilder = new CemiPaymentElectionDataBuilderCsvImpl(
                    getOutputDefinitionForPaymentElectionExtract(), getCemiPaymentElectionDao(), getAchBankService(),
                    getDateTimeService(), getBusinessObjectService(), jobRunDate,
                    getPaymentElectionFileCreationDirectory(), shouldMaskCemiSensitiveData());
            final Stream<PayeeACHAccount> payeeAchAccounts = getCemiPaymentElectionOrmDao().getPayeeAchAccountIdsForCemiPaymentElectionExtractAsCloseableStream();
        ) {
            final Iterator<PayeeACHAccount> payeeAchAccountIterator = payeeAchAccounts.iterator();
            dataBuilder.writePaymentElectionDataToIntermediateStorage(payeeAchAccountIterator, jobRunDate);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generatePaymentElectionExtractFile(final LocalDateTime jobRunDate) {
        try {
            LOG.info("generatePaymentElectionExtractFile, Starting creation of CEMI Payment Election Extract file...");
            final String newFileName = CemiUtils.generateFileNameContainingDateTime(
                    jobRunDate, CemiPaymentElectionConstants.PAYMENT_ELECTION_EXTRACT_FILENAME_PREFIX, FileExtensions.XLSX);
            final File tempFile = qualifyAndGetFilePath(paymentElectionFileCreationDirectory, newFileName);
            final File finalFile = qualifyAndGetFilePath(
                    paymentElectionFileOutboundDirectory, CemiPaymentElectionConstants.PAYMENT_ELECTION_EXTRACT_PLAIN_FILENAME);
            Validate.validState(!tempFile.exists(), "Temporary file already exists: %s", newFileName);

            createAndPopulatePaymentElectionExtractFile(tempFile, jobRunDate);

            if (shouldCopyPaymentElectionExtractFileToOutboundDirectory()) {
                LOG.info("generatePaymentElectionExtractFile, Copying file to outbound folder under the {} name...", CemiPaymentElectionConstants.PAYMENT_ELECTION_EXTRACT_PLAIN_FILENAME);
                copyPaymentElectionExtractFileToOutboundDirectory(tempFile, finalFile);
            } else {
                LOG.info("generatePaymentElectionExtractFile, Copying of the file to the outbound folder has been disabled. "
                        + "The copying operation will be skipped.");
            }

            LOG.info("generatePaymentElectionExtractFile, Success! Created the following extract file: {}", newFileName);
        } catch (final Exception e) {
            LOG.error("generatePaymentElectionExtractFile, Creation of Payment Election Extract file failed", e);
            throw new RuntimeException(e);
        }
    }

    private File qualifyAndGetFilePath(final String prefix, final String fileName) {
        return new File(StringUtils.join(prefix, CUKFSConstants.SLASH, fileName));
    }
    
    private void createAndPopulatePaymentElectionExtractFile(final File file, final LocalDateTime jobRunDate)
            throws IOException, InvalidFormatException {
        final CemiOutputDefinition outputDefinition = getOutputDefinitionForPaymentElectionExtract();

        try (
            final InputStream templateFileStream = CuCoreUtilities.getResourceAsStream(
                    CemiPaymentElectionConstants.PAYMENT_ELECTION_TEMPLATE_FILE_PATH);
            final CemiExcelWriter writer = new CemiExcelWriter(outputDefinition, templateFileStream, file);
        ) {
            // Replace this appender with a temp table implementation when ready.
            final CemiPaymentElectionFileAppenderCsvImpl paymentElectionFileAppender = new CemiPaymentElectionFileAppenderCsvImpl(
                outputDefinition, jobRunDate, paymentElectionFileCreationDirectory);
            paymentElectionFileAppender.populatePaymentElectionFileFromIntermediateDataStorage(writer);
            writer.commit();
            paymentElectionFileAppender.cleanUpIntermediateStorage();
        }
    }

    private CemiOutputDefinition getOutputDefinitionForPaymentElectionExtract() throws IOException {
        try (
            final InputStream inputStream = CuCoreUtilities.getResourceAsStream(
                    CemiPaymentElectionConstants.PAYMENT_ELECTION_OUTPUT_DEFINITION_FILE_PATH);
        ) {
            final byte[] fileContents = IOUtils.toByteArray(inputStream);
            return cemiOutputDefinitionFileType.parse(fileContents);
        }
    }

    private boolean shouldCopyPaymentElectionExtractFileToOutboundDirectory() {
        return parameterService.getParameterValueAsBoolean(
                CreateCemiPaymentElectionExtractStep.class, CemiPaymentElectionParameterConstants.COPY_CEMI_PAYMENT_ELECTION_FILE_TO_OUTBOUND_FOLDER);
    }

    private boolean shouldImportExternalStagingData() {
        return parameterService.getParameterValueAsBoolean(
                CreateCemiPaymentElectionExtractStep.class, CemiBaseParameterConstants.CEMI_LOAD_EXTERNAL_STAGING_DATA);
    }

    private boolean isCemiSensitiveDataSetToUnmask() {
        String maskingParameterValue =  parameterService.getParameterValueAsString(
                CreateCemiPaymentElectionExtractStep.class, CemiBaseParameterConstants.CEMI_SENSITIVE_DATA_MASKING_SETTING);
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
    
    private void copyPaymentElectionExtractFileToOutboundDirectory(final File sourceFile, final File targetFile) {
        try {
            final Path creationFilePath = sourceFile.toPath();
            final Path outboundFilePath = targetFile.toPath();
            Files.copy(creationFilePath, outboundFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.error("copyPaymentElectionExtractFileToOutboundDirectory, Failed to copy file to outbound directory", e);
            throw new UncheckedIOException(e);
        }
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public String getPaymentElectionFileCreationDirectory() {
        return paymentElectionFileCreationDirectory;
    }

    public void setPaymentElectionFileCreationDirectory(String paymentElectionFileCreationDirectory) {
        this.paymentElectionFileCreationDirectory = paymentElectionFileCreationDirectory;
    }

    public String getPaymentElectionFileOutboundDirectory() {
        return paymentElectionFileOutboundDirectory;
    }

    public void setPaymentElectionFileOutboundDirectory(String paymentElectionFileOutboundDirectory) {
        this.paymentElectionFileOutboundDirectory = paymentElectionFileOutboundDirectory;
    }

    public CemiPaymentElectionOrmDao getCemiPaymentElectionOrmDao() {
        return cemiPaymentElectionOrmDao;
    }

    public void setCemiPaymentElectionOrmDao(CemiPaymentElectionOrmDao cemiPaymentElectionOrmDao) {
        this.cemiPaymentElectionOrmDao = cemiPaymentElectionOrmDao;
    }

    public CemiPaymentElectionDao getCemiPaymentElectionDao() {
        return cemiPaymentElectionDao;
    }

    public void setCemiPaymentElectionDao(CemiPaymentElectionDao cemiPaymentElectionDao) {
        this.cemiPaymentElectionDao = cemiPaymentElectionDao;
    }

    public CemiOutputDefinitionFileType getCemiOutputDefinitionFileType() {
        return cemiOutputDefinitionFileType;
    }

    public void setCemiOutputDefinitionFileType(CemiOutputDefinitionFileType cemiOutputDefinitionFileType) {
        this.cemiOutputDefinitionFileType = cemiOutputDefinitionFileType;
    }

    public AchBankService getAchBankService() {
        return achBankService;
    }

    public void setAchBankService(AchBankService achBankService) {
        this.achBankService = achBankService;
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

    public void setExternalEmployeeIdDataFileType(CemiCsvBatchInputFileType externalEmployeeIdDataFileType) {
        this.externalEmployeeIdDataFileType = externalEmployeeIdDataFileType;
    }

    public void setCemiCsvDataImportService(CemiCsvDataImportService cemiCsvDataImportService) {
        this.cemiCsvDataImportService = cemiCsvDataImportService;
    }

}
