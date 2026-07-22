package  edu.cornell.kfs.cemi.patterntemplate.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.patterntemplate.CemiEXTRACTNAMEConstants;
import edu.cornell.kfs.cemi.patterntemplate.batch.CreateCemiEXTRACTNAMEExtractStep;
import edu.cornell.kfs.cemi.patterntemplate.batch.businessobject.CemiExampleLEGACYOBJECT;
import edu.cornell.kfs.cemi.patterntemplate.batch.service.CemiEXTRACTNAMEExtractService;
import edu.cornell.kfs.cemi.patterntemplate.dataaccess.CemiEXTRACTNAMEExtractDao;
import edu.cornell.kfs.cemi.patterntemplate.dataaccess.CemiEXTRACTNAMEExtractOrmDao;
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

public class CemiEXTRACTNAMEExtractServiceImpl extends CemiDataExtractServiceBase
        implements CemiEXTRACTNAMEExtractService {
    
    private static final Logger LOG = LogManager.getLogger();
    
    private CemiEXTRACTNAMEExtractOrmDao cemiEXTRACTNAMEExtractOrmDao;
    private CemiEXTRACTNAMEExtractDao cemiEXTRACTNAMEExtractDao;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    
    public CemiEXTRACTNAMEExtractServiceImpl(final Environment environment) {
        super(environment);
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of keys representing extractable business objects from"
                + " the previous run (if present)...");
        cemiEXTRACTNAMEExtractDao.clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of keys "
                + "representing the extractable business objects...");
        cemiEXTRACTNAMEExtractDao.queryAndStoreInScopeBusinessObjectKeysForDataExtract();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for {} spreadsheet and placing in "
                + "intermediate storage...", CemiEXTRACTNAMEConstants.EXTRACTNAME_EXTRACT_PLAIN_FILENAME);
        
        try ( 
                final Stream<CemiExampleLEGACYOBJECT> legacyObjects = 
                        cemiEXTRACTNAMEExtractOrmDao.getLEGACYOBJECTForCemiEXTRACTNAMEExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final CemiEXTRACTNAMEFileExtractDataBuilderDefaultImpl dataBuilder = new CemiEXTRACTNAMEFileExtractDataBuilderDefaultImpl(
                    businessObjectService, jobRunDateString, dateTimeService, cemiEXTRACTNAMEExtractOrmDao,
                    cemiEXTRACTNAMEExtractDao, shouldMaskCemiSensitiveData());
            final Iterator<CemiExampleLEGACYOBJECT> legacyObjectIterator = legacyObjects.iterator();
            dataBuilder.writeEXTRACTNAMEFileTABNAMETabExtractDataToIntermediateStorage(legacyObjectIterator);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateEXTRACTNAMErExtractFile, Starting creation of CEMI Extract file {}",
                CemiEXTRACTNAMEConstants.EXTRACTNAME_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiEXTRACTNAMEConstants.EXTRACTNAME_EXTRACT_PLAIN_FILENAME,
                CemiEXTRACTNAMEConstants.EXTRACTNAME_EXTRACT_FILENAME_PREFIX);
    }
    
    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiEXTRACTNAMEExtractStep.class;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiEXTRACTNAMEConstants.EXTRACTNAME_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiEXTRACTNAMEConstants.EXTRACTNAME_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setCemiEXTRACTNAMEExtractOrmDao(CemiEXTRACTNAMEExtractOrmDao cemiEXTRACTNAMEExtractOrmDao) {
        this.cemiEXTRACTNAMEExtractOrmDao = cemiEXTRACTNAMEExtractOrmDao;
    }

    public void setCemiEXTRACTNAMEExtractDao(CemiEXTRACTNAMEExtractDao cemiEXTRACTNAMEExtractDao) {
        this.cemiEXTRACTNAMEExtractDao = cemiEXTRACTNAMEExtractDao;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

}
