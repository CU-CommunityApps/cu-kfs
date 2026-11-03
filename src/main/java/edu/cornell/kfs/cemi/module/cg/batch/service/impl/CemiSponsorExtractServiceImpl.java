package  edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Agency;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.module.cg.CemiSponsorConstants;
import edu.cornell.kfs.cemi.module.cg.batch.CreateCemiSponsorExtractStep;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiSponsorExtractService;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiSponsorExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiSponsorExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

// {EXTRACTNAME} is throughout this file and needs to be replaced with the correct value for the
// data extraction being created.
//
// Abstract class CemiBaseExtractServiceImpl should NOT be modified/customized when using this pattern.
// First consult with the rest of the team and only then should you override that base classes standard implementation.
//
// The general terms used in this class file should be adjusted to correctly reflect the types and kind of data 
// being processed. Meaning, if the routine being called returns a stream of awards or accounting lines or suppliers,
// replace the generic terms legacyObjects with ones that correctly reflect the objects so that down stream code 
// is self documenting.
//
// There are four base class abstract methods that must be implemented in this concrete class. There is NO default
// implementation possible due to the returned values needing to be specific to the extract being created. Each
// method is marked with "Required overriding method for base class CemiDataExtractServiceBase" in the examples below.

public class CemiSponsorExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiSponsorExtractService {
    
    private static final Logger LOG = LogManager.getLogger();
    
    private CemiSponsorExtractOrmDao cemiSponsorExtractOrmDao;
    private CemiSponsorExtractDao cemiSponsorExtractDao;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    
    public CemiSponsorExtractServiceImpl(final Environment environment) {
        super(environment);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of keys representing extractable business objects from"
                + " the previous run (if present)...");
        cemiSponsorExtractDao.clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of keys "
                + "representing the extractable business objects...");
        cemiSponsorExtractDao.queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for {} spreadsheet and placing in "
                + "intermediate storage...", CemiSponsorConstants.SPONSOR_EXTRACT_PLAIN_FILENAME);
        
        try ( 
                final Stream<Agency> agencies = 
                        cemiSponsorExtractOrmDao.getAgenciesForCemiSponsorExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final CemiSponsorFileExtractDataBuilderDefaultImpl dataBuilder = new CemiSponsorFileExtractDataBuilderDefaultImpl(
                    businessObjectService, jobRunDateString, dateTimeService, cemiSponsorExtractOrmDao,
                    cemiSponsorExtractDao, shouldMaskCemiSensitiveData());
            final Iterator<Agency> legacyObjectIterator = agencies.iterator();
            dataBuilder.writeSponsorFileSponsorTabExtractDataToIntermediateStorage(legacyObjectIterator);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateEXTRACTNAMErExtractFile, Starting creation of CEMI Extract file {}",
                CemiSponsorConstants.SPONSOR_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiSponsorConstants.SPONSOR_EXTRACT_PLAIN_FILENAME,
                CemiSponsorConstants.SPONSOR_EXTRACT_FILENAME_PREFIX);
    }
    
    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiSponsorExtractStep.class;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiSponsorConstants.SPONSOR_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiSponsorConstants.SPONSOR_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    // Optional overriding method for base class CemiDataExtractServiceBase
    // When the data extraction uses its own system parameters (such as a date range or a dependent extract's
    // run date), override this method so those parameter values are logged when the batch job runs.
    // The COPY_CEMI_FILE_TO_OUTBOUND_FOLDER and CEMI_SENSITIVE_DATA_MASKING_SETTING parameters are always logged
    // by the base class and should NOT be included here.
    //
    //      Example:
    //          @Override
    //          protected List<String> getExtractSpecificParameterNamesToLog() {
    //              return List.of(CemiEXTRACTNAMEParameterConstants.CEMI_EXTRACTNAME_EXTRACT_DATE_RANGE);
    //          }

    public void setCemiSponsorExtractOrmDao(CemiSponsorExtractOrmDao cemiSponsorExtractOrmDao) {
        this.cemiSponsorExtractOrmDao = cemiSponsorExtractOrmDao;
    }

    public void setCemiSponsorExtractDao(CemiSponsorExtractDao cemiSponsorExtractDao) {
        this.cemiSponsorExtractDao = cemiSponsorExtractDao;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
