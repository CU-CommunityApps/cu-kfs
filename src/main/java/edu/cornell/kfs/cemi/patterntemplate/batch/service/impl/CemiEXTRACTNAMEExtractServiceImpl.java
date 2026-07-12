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

import edu.cornell.kfs.cemi.patterntemplate.CemiEXTRACTNAMEConstants;
import edu.cornell.kfs.cemi.patterntemplate.batch.CreateCemiEXTRACTNAMEExtractStep;
import edu.cornell.kfs.cemi.patterntemplate.batch.businessobject.CemiEXTRACTNAMEFileTABNAMERowBo;
import edu.cornell.kfs.cemi.patterntemplate.batch.service.CemiEXTRACTNAMEExtractService;
import edu.cornell.kfs.cemi.patterntemplate.dataaccess.CemiEXTRACTNAMEDao;
import edu.cornell.kfs.cemi.patterntemplate.dataaccess.CemiEXTRACTNAMEOrmDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseParameterConstants;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants.FileExtensions;
import edu.cornell.kfs.cemi.sys.batch.CemiOutputDefinitionFileType;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.sys.CUKFSConstants;

// {EXTRACTNAME} is throughout this file and needs to be replaced with the correct value for the
// data extraction being created.
//
// Class CemiBaseExtractServiceImpl should not be modified/customized when using this pattern.
// First consult with the rest of the team and only then should you override any method in 
// base class which you believe requries specialization.

public class CemiEXTRACTNAMEExtractServiceImpl extends CemiDataExtractServiceBase implements CemiEXTRACTNAMEExtractService {
    private static final Logger LOG = LogManager.getLogger();

    private CemiEXTRACTNAMEOrmDao cemiEXTRACTNAMEOrmDao;
    private CemiEXTRACTNAMEDao cemiEXTRACTNAMEDao;
    private DateTimeService dateTimeService;
    
    public CemiEXTRACTNAMEExtractServiceImpl(final Environment environment) {
        super(environment);
    }
    
    // This routine should clear and/or reset all values so that the batch job extraction runs cleanly.
    // One-to-many method service calls specific to each data extraction may need to be performed in
    // order to achive that result.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState(String dataExtractName) {
        LOG.info("resetState, Deleting the list keys representing extractable business objects for {} "
                + "from the previous run (if present)...", dataExtractName);
        getCemiEXTRACTNAMEDao().clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
    }
    
    // This routine will utilize database atrifacts (tables, views, etc) to obtain the population of data defined
    // to be in scope for the data extraction.
    //
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable(String dataExtractName) {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of keys "
                + "representing the extractable business objects for {}...", dataExtractName);
        getCemiEXTRACTNAMEDao().queryAndStoreInScopeBusinessObjectKeysForDataExtract(CemiEXTRACTNAMEConstants.CEMI_IN_SCOPE_BUSINESS_OBJECT_NAME);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(String dataExtractName, final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for {} spreadsheet "
                + "and placing in intermediate storage...", dataExtractName);
        try {
            generateEXTRACTNAMEExtractData(jobRunDate);
        } catch (final Exception e) {
            LOG.error("generateIntermediateExtractData, Creation of {} Extract data failed", dataExtractName, e);
            throw new RuntimeException(e);
        }
    }

    //THIS STILL NEED WORK TRYING TO RECONCILE AWARD SCHEDULE csv with table population.
    // Actual KFS classes should be used wherever CemiEXTRACTNAMEOrmDao.LEGACYOBJECT is used.
    // That class is a pattern template only class created and required to get the pattern template code to compile.
    private void generateEXTRACTNAMEExtractData(final LocalDateTime jobRunDate) throws IOException {
        try (
                final Stream<CemiEXTRACTNAMEOrmDao.LEGACYOBJECT> legacyObjectForEachRow = cemiEXTRACTNAMEOrmDao
                        .getLEGACYOBJECTForCemiEXTRACTNAMEExtractAsCloseableStream();
            ) {
                final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
                final String supplierJobRunDate = getSupplierJobRunDate();
                final CemiEXTRACTNAMEDataBuilderDefaultImpl dataBuilder = new CemiEXTRACTNAMEDataBuilderDefaultImpl(
                        businessObjectService, jobRunDateString, cemiRemitToSupplierOrmDao, supplierJobRunDate,
                        shouldMaskCemiSensitiveData());
                final Iterator<CemiSupplierAddressBo> addressesIterator = addresses.iterator();
                dataBuilder.writeRemitToSupplierDataToIntermediateStorage(addressesIterator);
            }
//        try (
//            // Replace this builder with a temp table implementation when ready.
//            final CemiEXTRACTNAMEDataBuilderDefaultImpl dataBuilder = new CemiEXTRACTNAMEDataBuilderDefaultImpl(
//                    getOutputDefinitionForAwardScheduleExtract(), getCemiEXTRACTNAMEDao(), getDateTimeService(),
//                    getBusinessObjectService(), jobRunDate, getEXTRACTNAMEFileCreationDirectory(), super.shouldMaskCemiSensitiveData());
//            final Stream<Award> awards = getCemiAwardScheduleOrmDao().getAwardsForCemiAwardScheduleExtractAsCloseableStream();
//        ) {
//            final Iterator<Award> awardsIterator = awards.iterator();
//            dataBuilder.writeAwardScheduleDataToIntermediateStorage(awardsIterator, jobRunDate);
//        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(String dataExtractName, final LocalDateTime jobRunDate) {
        try {
            LOG.info("generateDataConversionExtractFile, Starting creation of CEMI {} Extract file...", dataExtractName);
            final String newFileName = CemiUtils.generateFileNameContainingDateTime(jobRunDate, 
                    CemiEXTRACTNAMEConstants.EXTRACTNAME_EXTRACT_FILENAME_PREFIX, FileExtensions.XLSX);
            
            final File tempFile = qualifyAndGetFilePath(EXTRACTNAMEFileCreationDirectory, newFileName);
            final File finalFile = qualifyAndGetFilePath(EXTRACTNAMEFileOutboundDirectory, CemiEXTRACTNAMEConstants.EXTRACTNAME_EXTRACT_PLAIN_FILENAME);
            
            Validate.validState(!tempFile.exists(), "Temporary file already exists: %s", newFileName);

            createAndPopulateAwardScheduleExtractFile(tempFile, jobRunDate);

            if (super.shouldCopyAwardScheduleExtractFileToOutboundDirectory(
                    CemiEXTRACTNAMEParameterConstants.COPY_CEMI_EXTRACTNAME_FILE_TO_OUTBOUND_FOLDER)) {
                LOG.info("generateDataConversionExtractFile, Copying file to outbound folder under the {} name...",
                        CemiEXTRACTNAMEConstants.EXTRACTNAME_EXTRACT_PLAIN_FILENAME);
                copyExtractFileToOutboundDirectory(tempFile, finalFile, dataExtractName);
            } else {
                LOG.info("generateDataConversionExtractFile, Copying of the data extract file to the outbound folder"
                        + " has been disabled. The copying operation will be skipped for data extract {}.", dataExtractName );
            }
            LOG.info("generateDataConversionExtractFile, Success! Created the following extract file: {}", newFileName);
        } catch (final Exception e) {
            LOG.error("generateDataConversionExtractFile, Creation of {} Extract file failed", dataExtractName, e);
            throw new RuntimeException(e);
        }
    }
   
    @Override
    protected Class<?> getComponentClassForDataMaskingParameter() {
        return CreateCemiEXTRACTNAMEExtractStep.class;
    }
    
    @Override
    protected Class<?> getComponentClassForCopyFileToOutboundFolderParameter() {
        return CreateCemiEXTRACTNAMEExtractStep.class;
    }

    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiEXTRACTNAMEConstants.EXTRACTNAME_OUTPUT_DEFINITION_FILE_PATH_SUFFIX;
    }

    @Override
    protected String getTemplateWorkbookFilePath() {
        return CemiEXTRACTNAMEConstants.EXTRACTNAME_TEMPLATE_WORKBOOK_FILE_PATH;
    }

    public CemiEXTRACTNAMEOrmDao getCemiEXTRACTNAMEOrmDao() {
        return cemiEXTRACTNAMEOrmDao;
    }

    public void setCemiEXTRACTNAMEOrmDao(CemiEXTRACTNAMEOrmDao cemiEXTRACTNAMEOrmDao) {
        this.cemiEXTRACTNAMEOrmDao = cemiEXTRACTNAMEOrmDao;
    }

    public CemiEXTRACTNAMEDao getCemiEXTRACTNAMEDao() {
        return cemiEXTRACTNAMEDao;
    }

    public void setCemiEXTRACTNAMEDao(CemiEXTRACTNAMEDao cemiEXTRACTNAMEDao) {
        this.cemiEXTRACTNAMEDao = cemiEXTRACTNAMEDao;
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
