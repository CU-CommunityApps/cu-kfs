package  edu.cornell.kfs.cemi.module.cg.batch.service.impl;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.Environment;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
import edu.cornell.kfs.cemi.module.cg.CemiAwardParameterConstants;
import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants.AwardTranslateTables;
import edu.cornell.kfs.cemi.module.cg.batch.CreateCemiAwardExtractStep;
import edu.cornell.kfs.cemi.module.cg.batch.service.CemiAwardExtractService;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.CemiAwardTranslateTableFactory;
import edu.cornell.kfs.cemi.module.cg.batch.translatetable.CemiAwardTranslateTableMaps;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractDao;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiDataExtractServiceBase;
import edu.cornell.kfs.cemi.sys.util.CemiUtils;

public class CemiAwardExtractServiceImpl extends CemiDataExtractServiceBase implements CemiAwardExtractService {
    
    private static final Logger LOG = LogManager.getLogger();
    
    private CemiAwardExtractOrmDao cemiAwardExtractOrmDao;
    private CemiAwardExtractDao cemiAwardExtractDao;
    private BusinessObjectService businessObjectService;
    private DateTimeService dateTimeService;
    private CemiAwardTranslateTableMaps allAwardTranslateTableMaps;
    
    public CemiAwardExtractServiceImpl(final Environment environment) {
        super(environment);
        populateAllAwardTranslateTableMaps();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void resetState() {
        LOG.info("resetState, Deleting the list of keys representing extractable business objects from"
                + " the previous run (if present)...");
        cemiAwardExtractDao.clearAnyExistingInScopeBusinessObjectKeysFromPreviousExecution();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void initializeExtractDateSettings() {
        LOG.info("initializeExtractDateSettings, Setting Award Schedule extraction date-time to use for any dependent queries...");
        final String awardScheduleJobRunDate = getAwardScheduleJobRunDate();
        cemiAwardExtractDao.updateAwardScheduleExtractDependentQuerySettings(awardScheduleJobRunDate);

    }
    
    private String getAwardScheduleJobRunDate() {
        final String awardScheduleJobRunDate = parameterService.getParameterValueAsString(
                CreateCemiAwardExtractStep.class,
                CemiAwardParameterConstants.CEMI_AWARD_EXTRACT_AWARD_SCHEDULE_DATETIME);
        Validate.validState(StringUtils.isNotBlank(awardScheduleJobRunDate), "Parameter %s should not have been blank",
                CemiAwardParameterConstants.CEMI_AWARD_EXTRACT_AWARD_SCHEDULE_DATETIME);
        return awardScheduleJobRunDate;
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void captureInScopeBusinessObjectKeysToProcessingTable() {
        LOG.info("captureInScopeBusinessObjectKeysToProcessingTable, Querying and storing the list of keys "
                + "representing the extractable KFS business objects...");
        cemiAwardExtractDao.queryAndStoreInScopeBusinessObjectKeysForDataExtract();
//    cemiAwardExtractDao.buildDynamicLookupTables();
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateIntermediateExtractData(final LocalDateTime jobRunDate) {
        LOG.info("generateIntermediateExtractData, Generating data rows for Award spreadsheet "
                + "and placing in intermediate database table storage...");

        try (
                final Stream<Award> awards = 
                        cemiAwardExtractOrmDao.getAwardsForCemiAwardExtractAsCloseableStream();
        ) {
            final String jobRunDateString = CemiUtils.generateBatchJobRunDateAsString(jobRunDate);
            final CemiAwardFileExtractDataBuilderDefaultImpl dataBuilder = 
                    new CemiAwardFileExtractDataBuilderDefaultImpl(
                            businessObjectService, jobRunDateString, dateTimeService, cemiAwardExtractOrmDao,
                            cemiAwardExtractDao, allAwardTranslateTableMaps, shouldMaskCemiSensitiveData());
            final Iterator<Award> awardsIterator = awards.iterator();
            dataBuilder.writeAwardFileSubmitAwardTabExtractDataToIntermediateStorage(awardsIterator);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void generateDataConversionExtractFile(final LocalDateTime jobRunDate) {
        LOG.info("generateAwardrExtractFile, Starting creation of CEMI Extract file {}",
                CemiAwardConstants.AWARD_EXTRACT_PLAIN_FILENAME);
        generateFileForDataExtract(jobRunDate, CemiAwardConstants.AWARD_EXTRACT_PLAIN_FILENAME,
                CemiAwardConstants.AWARD_EXTRACT_FILENAME_PREFIX);
    }
    
    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected Class<?> getComponentClassForDataExtractParameter() {
        return CreateCemiAwardExtractStep.class;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getOutputDefinitionFilePathSuffix() {
        return CemiAwardConstants.AWARD_OUTPUT_DEFINITION_PATH_SUFFIX;
    }

    // Required overriding method for base class CemiDataExtractServiceBase
    @Override
    protected String getTemplateWorkbookFilePathSuffix() {
        return CemiAwardConstants.AWARD_TEMPLATE_WORKBOOK_FILE_PATH_SUFFIX;
    }

    public void setCemiAwardExtractOrmDao(CemiAwardExtractOrmDao cemiAwardExtractOrmDao) {
        this.cemiAwardExtractOrmDao = cemiAwardExtractOrmDao;
    }

    public void setCemiAwardExtractDao(CemiAwardExtractDao cemiAwardExtractDao) {
        this.cemiAwardExtractDao = cemiAwardExtractDao;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    private void populateAllAwardTranslateTableMaps() {
        allAwardTranslateTableMaps = new CemiAwardTranslateTableMaps();
        
        allAwardTranslateTableMaps.setSponsorAwardTypesMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.SPONSOR_AWARD_TYPES_QUERY, cemiAwardExtractDao));
        
        allAwardTranslateTableMaps.setAwardPurposeMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.AWARD_PURPOSE_QUERY, cemiAwardExtractDao));
        
        allAwardTranslateTableMaps.setAwardLineLifecycleStatusMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.AWARD_LINE_LIFECYCLE_STATUS_QUERY, cemiAwardExtractDao));
        
        allAwardTranslateTableMaps.setAwardLineTypesMap(CemiAwardTranslateTableFactory.createAwardTranslateTableFor(
                AwardTranslateTables.AWARD_LINE_TYPES_QUERY, cemiAwardExtractDao));
    }

    public CemiAwardExtractOrmDao getCemiAwardExtractOrmDao() {
        return cemiAwardExtractOrmDao;
    }

    public CemiAwardExtractDao getCemiAwardExtractDao() {
        return cemiAwardExtractDao;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public DateTimeService getDateTimeService() {
        return dateTimeService;
    }
}
